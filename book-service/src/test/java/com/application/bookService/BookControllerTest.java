package com.application.bookService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.application.bookService.dto.BookRequest;
import com.application.bookService.dto.BookResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookControllerTest {
  @Autowired private TestRestTemplate rest;

  @Test
  void shouldCreateAndGetBook() {
    ResponseEntity<BookResponse.CreateBook> createBookResponseEntity =
      rest.postForEntity(
        "/api/books",
        new BookRequest.CreateBook(
          "New book", "Michael", List.of("Breathtaking", "Science fiction")),
        BookResponse.CreateBook.class);
    assertTrue(
      createBookResponseEntity.getStatusCode().is2xxSuccessful(),
      "Unexpected status code: " + createBookResponseEntity);
    BookResponse.CreateBook createBookResponseBody = createBookResponseEntity.getBody();

    assertNotNull(createBookResponseBody);

    ResponseEntity<Book> getBookResponse =
      rest.getForEntity("/api/books/{id}", Book.class, Map.of("id", createBookResponseBody.id()));
    assertTrue(
      getBookResponse.getStatusCode().is2xxSuccessful(),
      "Unexpected status code: " + getBookResponse.getStatusCode());

    Book getBookResponseBody = getBookResponse.getBody();
    assertNotNull(getBookResponseBody);
    assertEquals("Michael", getBookResponseBody.author());
    assertEquals("New book", getBookResponseBody.title());
  }

  @Test
  void update() {
    ResponseEntity<BookResponse.CreateBook> createBookResponseEntity =
      rest.postForEntity(
        "/api/books",
        new BookRequest.CreateBook("Book to Update", "John", List.of("Adventure")),
        BookResponse.CreateBook.class);
    assertTrue(createBookResponseEntity.getStatusCode().is2xxSuccessful());

    BookResponse.CreateBook createdBook = createBookResponseEntity.getBody();
    assertNotNull(createdBook);

    ResponseEntity<Void> updateResponseEntity =
      rest.exchange(
        "/api/books/{id}",
        HttpMethod.PUT,
        new HttpEntity<>(new BookRequest.UpdateBook("Updated Book", "Jane", List.of("Sci-Fi"))),
        Void.class,
        createdBook.id());

    assertTrue(updateResponseEntity.getStatusCode().is2xxSuccessful());

    ResponseEntity<Book> getUpdatedBookResponse =
      rest.getForEntity("/api/books/{id}", Book.class, createdBook.id());
    assertTrue(getUpdatedBookResponse.getStatusCode().is2xxSuccessful());

    Book updatedBook = getUpdatedBookResponse.getBody();
    assertNotNull(updatedBook);
    assertEquals("Jane", updatedBook.author());
    assertEquals("Updated Book", updatedBook.title());
    assertEquals(new HashSet<>(List.of("Sci-Fi")), updatedBook.tags());
  }

  @Test
  void delete() {
    ResponseEntity<BookResponse.CreateBook> createBookResponseEntity =
      rest.postForEntity(
        "/api/books",
        new BookRequest.CreateBook("Book to Delete", "John", List.of("Thriller")),
        BookResponse.CreateBook.class);
    assertTrue(createBookResponseEntity.getStatusCode().is2xxSuccessful());

    BookResponse.CreateBook createdBook = createBookResponseEntity.getBody();
    assertNotNull(createdBook);

    ResponseEntity<Void> deleteResponseEntity =
      rest.exchange("/api/books/{id}", HttpMethod.DELETE, null, Void.class, createdBook.id());
    assertTrue(deleteResponseEntity.getStatusCode().is2xxSuccessful());

    ResponseEntity<Void> getDeletedBookResponse =
      rest.getForEntity("/api/books/{id}", Void.class, createdBook.id());
    assertEquals(HttpStatus.NOT_FOUND, getDeletedBookResponse.getStatusCode());
  }


  @Test
  void bookNotFoundException() {
    ResponseEntity<Void> notFoundResponseEntity =
      rest.exchange("/api/books/{id}", HttpMethod.GET, null, Void.class, Map.of("id", 999));
    assertEquals(HttpStatus.NOT_FOUND, notFoundResponseEntity.getStatusCode());
  }
}
