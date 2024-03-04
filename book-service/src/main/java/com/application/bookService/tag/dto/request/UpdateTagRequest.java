package com.application.bookService.tag.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;

public record UpdateTagRequest(
    @NonNull @Size(max = 200, message = "{validation.name.size.too_long}") String name) {}
