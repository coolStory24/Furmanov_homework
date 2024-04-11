package com.application.bookService.book.events;

import com.application.bookService.book.PaymentStatus;

public record BookStatusUpdatedEvent(Long bookId, PaymentStatus status, String messageId) {}
