package com.application.bookService;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public record Book(
    @NotNull Integer id, @NotNull String title, @NotNull String author, @NotNull Set<String> tags) {
  public List<String> getTagList() {
    return tags.stream().toList();
  }
}
