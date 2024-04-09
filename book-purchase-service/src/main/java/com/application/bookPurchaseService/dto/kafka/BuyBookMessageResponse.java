package com.application.bookPurchaseService.dto.kafka;

public record BuyBookMessageResponse(Long bookId, Boolean success, String message) {}
