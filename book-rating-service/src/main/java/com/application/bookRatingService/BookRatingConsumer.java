package com.application.bookRatingService;

import com.application.bookRatingService.dto.kafka.BookRatingMessageRequest;
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

  @Autowired private final BookRatingProducer bookRatingProducer;
  private static final Logger LOGGER = LoggerFactory.getLogger(BookRatingConsumer.class);

  public BookRatingConsumer(ObjectMapper objectMapper, BookRatingProducer bookRatingProducer) {
    this.objectMapper = objectMapper;
    this.bookRatingProducer = bookRatingProducer;
  }

  @KafkaListener(topics = {"${topic-to-consume-message}"})
  public void processBookRatingCalculationRequest(String message)
      throws JsonProcessingException, InterruptedException {
    BookRatingMessageRequest parsedMessage =
        objectMapper.readValue(message, BookRatingMessageRequest.class);

    LOGGER.info("Retrieved message {}", message);
    bookRatingProducer.stubBookRating(parsedMessage.bookId());
  }
}
