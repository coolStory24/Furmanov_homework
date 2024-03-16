package com.application.bookService.book;

import static org.junit.jupiter.api.Assertions.*;

import com.application.bookService.DatabaseSuite;
import com.application.bookService.author.AuthorService;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.application.bookService.book.exceptions.IsNotAuthorException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookService.class, AuthorService.class})
class BookServiceTest extends DatabaseSuite {
  @Autowired private BookService bookService;

  @Autowired private AuthorService authorService;

  @Test
  void CreateBookTest() throws AuthorNotFoundException, IsNotAuthorException {
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
