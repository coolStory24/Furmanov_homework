package com.application.bookService;

import com.application.bookService.book.BookService;
import com.application.bookService.book.PaymentStatus;
import com.application.bookService.book.dto.kafka.BuyBookMessageResponse;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookPurchaseConsumer {
  private final ObjectMapper objectMapper;

  @Autowired private final BookService bookService;

  private static final Logger LOGGER = LoggerFactory.getLogger(BookPurchaseConsumer.class);

  public BookPurchaseConsumer(ObjectMapper objectMapper, BookService bookService) {
    this.objectMapper = objectMapper;
    this.bookService = bookService;
  }

  @KafkaListener(topics = {"${topic-to-consume-buy_book-message}"})
  public void consumeBookPurchaseResponse(String message)
      throws JsonProcessingException, BookNotFoundException {
    BuyBookMessageResponse parsedMessage =
        objectMapper.readValue(message, BuyBookMessageResponse.class);

    if (parsedMessage.success()) {
      LOGGER.info("Book {}, payment succeed", parsedMessage.bookId());
      bookService.updateBookPurchaseStatus(parsedMessage.bookId(), PaymentStatus.PAYMENT_SUCCEED);
    } else {
      LOGGER.info("Book {}, payment failed: {}", parsedMessage.bookId(), parsedMessage.message());
      bookService.updateBookPurchaseStatus(parsedMessage.bookId(), PaymentStatus.PAYMENT_PENDING);
    }
  }
}
