package com.application.bookRatingService;

import com.application.bookRatingService.dto.kafka.BookRatingMessageResponse;
import com.application.bookRatingService.exceptions.BookRatingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class BookRatingProducer {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  @Autowired private final RatingService ratingService;

  public BookRatingProducer(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${topic-to-send-message}") String topic,
      RatingService ratingService) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = topic;
    this.ratingService = ratingService;
  }

  public void stubBookRating(@PathVariable @NotNull Long bookId)
      throws JsonProcessingException, InterruptedException {
    // mocking calculation

    var rating = ratingService.getRating(bookId);

    String message = objectMapper.writeValueAsString(new BookRatingMessageResponse(bookId, rating));

    var key = "book-" + bookId;
    CompletableFuture<SendResult<String, String>> sendResult =
        kafkaTemplate.send(topic, key, message);

    try {
      sendResult.get(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Unexpected thread interruption", e);
    } catch (ExecutionException e) {
      throw new BookRatingException("Couldn't send message to Kafka");
    } catch (TimeoutException e) {
      throw new BookRatingException("Couldn't send message to Kafka due to timeout");
    }
  }
}
