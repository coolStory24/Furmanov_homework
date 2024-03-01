package com.application.bookService.exception;

public class BookExceptions {
  public static class BookNotFoundException extends Exception {
    public BookNotFoundException(Integer bookId) {
      super("Book with id " + bookId + " not found");
    }
  }
}
