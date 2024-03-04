package com.application.bookService.author.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;

public record CreateAuthorRequest(
    @NonNull @Size(max = 200, message = "{validation.name.size.too_long}") String firstName,
    @NonNull @Size(max = 200, message = "{validation.name.size.too_long}") String lastName) {}
