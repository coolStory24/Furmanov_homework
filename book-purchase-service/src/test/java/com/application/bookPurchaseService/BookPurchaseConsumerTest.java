package com.application.bookPurchaseService;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;

import com.application.bookPurchaseService.dto.kafka.BuyBookMessageRequest;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    classes = {BookPurchaseConsumer.class, BookPurchaseService.class},
    properties = {
      "topic-to-consume-message=some-test-topic",
      "spring.kafka.consumer.auto-offset-reset=earliest"
    })
@Import({KafkaAutoConfiguration.class, BookPurchaseConsumerTest.ObjectMapperTestConfig.class})
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookPurchaseConsumerTest {
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
  @MockBean private BookPurchaseService bookService;
  @Autowired private BookPurchaseConsumer bookPurchaseConsumer;

  @Test
  void shouldReceiveSuccessMessageFromKafkaSuccessfully() throws JsonProcessingException {
    kafkaTemplate.send(
        "some-test-topic", objectMapper.writeValueAsString(new BuyBookMessageRequest(1L, "1")));

    await()
        .atMost(Duration.ofSeconds(10))
        .pollDelay(Duration.ofSeconds(1))
        .untilAsserted(() -> Mockito.verify(bookService, times(1)).buyBook(1L));
  }
}
