package com.application.bookService.dto.BookResponse;

import java.util.List;

public record GetBookResponse(Number id, String title, String author, List<String> tags) {}
