package com.application.bookService.tag.exceptions;

public class TagNotFoundException extends Exception {
  public TagNotFoundException(String id) {
    super("Tag with bookId " + id + " not found");
  }
}
