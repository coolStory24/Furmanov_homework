package com.application.bookService.book.exceptions;

public class CreateBookException extends RuntimeException {
  public CreateBookException(String message, Throwable cause) {
    super(message, cause);
  }
}
