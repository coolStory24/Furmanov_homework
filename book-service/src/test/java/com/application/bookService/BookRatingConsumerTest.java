package com.application.bookService;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import com.application.bookService.book.BookService;
import com.application.bookService.book.dto.kafka.BookServiceMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    classes = {BookRatingConsumer.class},
    properties = {
      "topic-to-consume-message=some-test-topic",
      "spring.kafka.consumer.group-id=some-consumer-group"
    })
@Import({KafkaAutoConfiguration.class, BookRatingConsumerTest.ObjectMapperTestConfig.class})
@Testcontainers
class BookRatingConsumerTest {
  @TestConfiguration
  static class ObjectMapperTestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  @Container @ServiceConnection
  public static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

  @Autowired private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private BookService bookService;
  @Autowired private BookRatingConsumer bookRatingConsumer;
  @Autowired private KafkaAdmin kafkaAdmin;

  @Test
  void shouldSendMessageToKafkaSuccessfully() throws JsonProcessingException {
    System.out.println(bookRatingConsumer);

    kafkaTemplate.send(
        "some-test-topic",
        objectMapper.writeValueAsString(new BookServiceMessageResponse(1L, 4.5D)));

    await()
        .atMost(Duration.ofSeconds(5))
        .pollDelay(Duration.ofSeconds(1))
        .untilAsserted(() -> Mockito.verify(bookService, times(1)).updateRating(eq(1L), eq(4.5)));
  }
}
