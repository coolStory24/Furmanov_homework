package com.application.bookService.book.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;

public record CreateBookRequest(
    @NonNull @Size(max = 50, message = "{validation.name.size.too_long}") String title,
    @NonNull @Min(0) Long authorId) {}
