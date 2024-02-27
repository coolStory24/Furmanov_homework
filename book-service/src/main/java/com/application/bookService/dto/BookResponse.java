package com.application.bookService.dto;

import java.util.List;

public class BookResponse {
  public record CreateBook(Integer id, String title, String author, List<String> tags) {}
  public record GetBook(Number id, String title, String author, List<String> tags) {}
}
