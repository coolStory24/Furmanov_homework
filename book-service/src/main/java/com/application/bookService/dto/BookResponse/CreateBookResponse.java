package com.application.bookService.dto.BookResponse;

import java.util.List;

public record CreateBookResponse(Integer id, String title, String author, List<String> tags) {}
