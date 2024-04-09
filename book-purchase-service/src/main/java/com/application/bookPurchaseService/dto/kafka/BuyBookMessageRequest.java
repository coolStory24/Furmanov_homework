package com.application.bookPurchaseService.dto.kafka;

public record BuyBookMessageRequest(Long bookId, String messageId) {}
