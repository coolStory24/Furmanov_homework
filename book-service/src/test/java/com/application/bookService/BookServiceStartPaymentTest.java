package com.application.bookService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;

import com.application.bookService.author.AuthorService;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.book.BookService;
import com.application.bookService.book.dto.kafka.BuyBookMessageRequest;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.application.bookService.book.exceptions.IsNotAuthorException;
import com.application.bookService.scheduling.OutboxScheduler;
import com.application.bookService.scheduling.SchedulerConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    properties = {
      "topic-to-send-buy_book-message=some-test-topic",
      "book.service.timeout.seconds=10",
      "resilience4j.ratelimiter.instances.createBook.limitForPeriod=1000",
      "resilience4j.ratelimiter.instances.createBook.limitRefreshPeriod=1s",
      "resilience4j.ratelimiter.instances.createBook.timeoutDuration=10s",
      "resilience4j.circuitbreaker.instances.createBook.failureRateThreshold=100",
      "resilience4j.circuitbreaker.instances.createBook.slowCallRateThreshold=100",
      "resilience4j.circuitbreaker.instances.createBook.slowCallDurationThreshold=1000ms",
      "resilience4j.circuitbreaker.instances.createBook.slidingWindowSize=100",
      "resilience4j.circuitbreaker.instances.createBook.slidingWindowType=COUNT_BASED",
      "resilience4j.circuitbreaker.instances.createBook.minimumNumberOfCalls=100",
      "resilience4j.circuitbreaker.instances.createBook.waitDurationInOpenState=1s",
      "resilience4j.retry.instances.createBook.max-attempts=1",
      "spring.kafka.consumer.auto-offset-reset=earliest"
    })
@Import({
  BookService.class,
  KafkaAutoConfiguration.class,
  SchedulerConfig.class,
  OutboxScheduler.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookServiceStartPaymentTest extends DatabaseSuite {
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

  @Autowired private BookService bookService;
  @Autowired private AuthorService authorService;
  @Autowired private ObjectMapper objectMapper;

  @Container
  public static final MockServerContainer mockServer =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("author-registry.service.base.url", mockServer::getEndpoint);
  }

  @Test
  void shouldSendMessageToKafkaSuccessfully()
      throws AuthorNotFoundException, IsNotAuthorException, BookNotFoundException {
    var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    client
        .when(
            request()
                .withMethod(String.valueOf(HttpMethod.POST))
                .withHeader("X-REQUEST-ID")
                .withPath("/api/author-registry"))
        .respond(
            new HttpResponse()
                .withBody("{\"isAuthor\": \"true\"}")
                .withHeader("Content-Type", "application/json"));

    var author = authorService.createAuthor("Some", "Author");

    var uuid = UUID.randomUUID().toString();
    var book = bookService.createBook("Some book", author.id(), uuid);
    assertDoesNotThrow(() -> bookService.buyById(book.id()));

    KafkaTestConsumer consumer =
        new KafkaTestConsumer(KAFKA.getBootstrapServers(), "some-group-id");
    consumer.subscribe(List.of("some-test-topic"));

    ConsumerRecords<String, String> records = consumer.poll();

    assertEquals(1, records.count());
    records
        .iterator()
        .forEachRemaining(
            record -> {
              BuyBookMessageRequest message;
              try {
                message = objectMapper.readValue(record.value(), BuyBookMessageRequest.class);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
              assertEquals(book.id(), message.bookId());
              assertDoesNotThrow(() -> UUID.fromString(message.messageId()));
            });
  }

  private static class KafkaTestConsumer {
    private final KafkaConsumer<String, String> consumer;

    public KafkaTestConsumer(String bootstrapServers, String groupId) {
      Properties props = new Properties();

      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

      this.consumer = new KafkaConsumer<>(props);
    }

    public void subscribe(List<String> topics) {
      consumer.subscribe(topics);
    }

    public ConsumerRecords<String, String> poll() {
      return consumer.poll(Duration.ofSeconds(5));
    }
  }
}
