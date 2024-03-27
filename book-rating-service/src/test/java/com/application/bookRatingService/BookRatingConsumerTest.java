package com.application.bookRatingService;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;

import com.application.bookRatingService.dto.kafka.BookRatingMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    classes = {BookRatingConsumer.class},
    properties = {
      "topic-to-consume-message=some-test-topic",
      "spring.kafka.consumer.group-id=some-consumer-group",
      "spring.kafka.consumer.auto-offset-reset=earliest"
    })
@Import({
  KafkaAutoConfiguration.class,
  BookRatingConsumerTest.ObjectMapperTestConfig.class,
})
@Testcontainers
@DirtiesContext()
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

  @MockBean private BookRatingProducer messageProcessor;
  @Autowired private BookRatingConsumer bookRatingConsumer;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldSendMessageToKafkaSuccessfully() throws JsonProcessingException {
    kafkaTemplate.send(
        new ProducerRecord<>(
            "some-test-topic", objectMapper.writeValueAsString(new BookRatingMessageRequest(56L))));

    await()
        .atMost(Duration.ofSeconds(5))
        .pollDelay(Duration.ofSeconds(1))
        .untilAsserted(() -> Mockito.verify(messageProcessor, times(1)).stubBookRating(56L));
  }
}
