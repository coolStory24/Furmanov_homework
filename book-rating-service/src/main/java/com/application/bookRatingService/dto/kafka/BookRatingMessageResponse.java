package com.application.bookRatingService.dto.kafka;

public record BookRatingMessageResponse(Long bookId, Double rating) {}
