package com.application.bookService.book.dto.kafka;

public record BuyBookMessageRequest(Long bookId, String messageId) {}
