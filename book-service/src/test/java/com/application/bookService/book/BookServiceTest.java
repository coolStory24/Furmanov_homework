package com.application.bookService.book;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;

import com.application.bookService.DatabaseSuite;
import com.application.bookService.TestConfig;
import com.application.bookService.author.AuthorService;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.application.bookService.book.exceptions.IsNotAuthorException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookService.class, AuthorService.class, TestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookServiceTest extends DatabaseSuite {
  @Autowired private AuthorService authorService;

  @Autowired private BookService bookService;

  @Container
  public static final MockServerContainer mockServer =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

  @BeforeAll
  static void setUp() {
    var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    client
        .when(
            request()
                .withMethod(String.valueOf(HttpMethod.POST))
                .withHeader("X-REQUEST-ID")
                .withPath("/api/author-registry"))
        .respond(
            new HttpResponse()
                .withBody("{\"isAuthor\": \"true\"}")
                .withHeader("Content-Type", "application/json"));
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("author-registry.service.base.url", mockServer::getEndpoint);
  }

  @Test
  public void CreateBookTest() throws AuthorNotFoundException, IsNotAuthorException {

    var author = authorService.createAuthor("George", "Orwell");
    var book = bookService.createBook("1984", author.id(), UUID.randomUUID().toString());

    assertEquals(book.title(), "1984");
  }

  @Test
  void getBookByIdTest()
      throws BookNotFoundException, AuthorNotFoundException, IsNotAuthorException {
    var author = authorService.createAuthor("J.K.", "Rowling");

    var book = bookService.createBook("Harry Potter", author.id(), UUID.randomUUID().toString());

    var retrievedBook = bookService.getBookById(book.id());

    assertEquals(retrievedBook.id(), book.id());
    assertEquals(retrievedBook.title(), "Harry Potter");
    assertEquals(retrievedBook.author().firstName(), "J.K.");
    assertEquals(retrievedBook.author().lastName(), "Rowling");
  }

  @Test
  void updateBookTest()
      throws BookNotFoundException, AuthorNotFoundException, IsNotAuthorException {
    var author = authorService.createAuthor("Jane", "Austen");

    var book =
        bookService.createBook("Pride and Prejudice", author.id(), UUID.randomUUID().toString());

    bookService.updateBook(book.id(), "Sense and Sensibility", author.id());

    var updatedBook = bookService.getBookById(book.id());

    assertEquals(updatedBook.title(), "Sense and Sensibility");
  }

  @Test
  void deleteBookTest()
      throws BookNotFoundException, AuthorNotFoundException, IsNotAuthorException {
    var author = authorService.createAuthor("Leo", "Tolstoy");

    var book = bookService.createBook("War and Peace", author.id(), UUID.randomUUID().toString());

    bookService.deleteBook(book.id());

    assertThrows(BookNotFoundException.class, () -> bookService.getBookById(book.id()));
  }
}
