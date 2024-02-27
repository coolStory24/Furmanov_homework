package com.application.bookService;

import com.application.bookService.exception.BookExceptions;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookRepository implements BookRepository {
  private final AtomicInteger counter;
  private final Map<Number, Book> storage;

  public InMemoryBookRepository() {
    this.counter = new AtomicInteger(0);
    this.storage = new HashMap<>();
  }

  @Override
  public Book createBook(String title, String author, Set<String> tags) {
    var book = new Book(counter.getAndIncrement(), title, author, tags);
    storage.put(book.id(), book);
    return book;
  }

  @Override
  public Book findBookById(Integer id) throws BookExceptions.BookNotFoundException {
    var book = storage.get(id);
    if (book == null) {
      throw new BookExceptions.BookNotFoundException(id);
    }

    return book;
  }

  @Override
  public List<Book> getAllBooks() {
    return storage.values().stream().toList();
  }

  @Override
  public List<Book> getAllBooksByTag(String tag) {
    return storage.values().stream().filter(book -> book.tags().contains(tag)).toList();
  }

  @Override
  public void updateBook(Integer id, String title, String author, Set<String> tags)
      throws BookExceptions.BookNotFoundException {
    var book = storage.get(id);
    if (book == null) {
      throw new BookExceptions.BookNotFoundException(id);
    }

    storage.put(book.id(), new Book(book.id(), title, author, tags));
  }

  @Override
  public void deleteBook(Integer id) {
    storage.remove(id);
  }
}
