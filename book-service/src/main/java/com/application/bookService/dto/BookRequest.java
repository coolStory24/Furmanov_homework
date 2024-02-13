package com.application.bookService.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.lang.NonNull;

public class BookRequest {
  public record CreateBook(
      @NonNull @Size(max = 50, message = "{validation.name.size.too_long}") String title,
      @NonNull @Size(max = 50, message = "{validation.name.size.too_long}") String author,
      @NonNull @Size(max = 10, message = "{validation.name.size.too_long}") List<String> tags) {}

  public record UpdateBook(
    @NonNull @Size(max = 50, message = "{validation.name.size.too_long}") String title,
    @NonNull @Size(max = 50, message = "{validation.name.size.too_long}") String author,
    @NonNull @Size(max = 10, message = "{validation.name.size.too_long}") List<String> tags) {}
}
