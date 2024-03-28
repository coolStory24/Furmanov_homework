package com.application.bookService;

import com.application.bookService.book.BookService;
import com.application.bookService.book.dto.kafka.BookServiceMessageResponse;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookRatingConsumer {
  private final ObjectMapper objectMapper;

  @Autowired private final BookService bookService;

  private static final Logger LOGGER = LoggerFactory.getLogger(BookRatingConsumer.class);

  public BookRatingConsumer(ObjectMapper objectMapper, BookService bookService) {
    this.objectMapper = objectMapper;
    this.bookService = bookService;
  }

  @KafkaListener(topics = {"${topic-to-consume-message}"})
  public void consumeCalculatedBookRating(String message)
      throws JsonProcessingException, BookNotFoundException {
    BookServiceMessageResponse parsedMessage =
        objectMapper.readValue(message, BookServiceMessageResponse.class);

    LOGGER.info("Retrieved message {}", message);

    bookService.updateRating(parsedMessage.bookId(), parsedMessage.rating());
  }
}
