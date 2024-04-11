package com.application.bookService.book.dto.kafka;

public record BuyBookMessageResponse(Long bookId, Boolean success, String message) {}
