package com.application.bookService;

import com.application.bookService.exception.BookExceptions;
import java.util.List;
import java.util.Set;

public interface BookRepository {
  Book createBook(String title, String author, Set<String> tags);

  Book findBookById(Integer id) throws BookExceptions.BookNotFoundException;

  List<Book> getAllBooks();

  List<Book> getAllBooksByTag(String tag);

  void updateBook(Integer id, String title, String author, Set<String> tags)
      throws BookExceptions.BookNotFoundException;

  void deleteBook(Integer id);
}
