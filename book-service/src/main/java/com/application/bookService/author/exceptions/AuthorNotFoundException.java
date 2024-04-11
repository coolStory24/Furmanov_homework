package com.application.bookService.author.exceptions;

public class AuthorNotFoundException extends Exception {
  public AuthorNotFoundException(Long id) {
    super("Author with bookId " + id + " not found");
  }
}
