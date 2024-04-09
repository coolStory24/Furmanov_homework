package com.application.bookPurchaseService;

import static org.junit.jupiter.api.Assertions.*;

import com.application.bookPurchaseService.dto.kafka.BuyBookMessageRequest;
import com.application.bookPurchaseService.dto.kafka.BuyBookMessageResponse;
import com.application.bookPurchaseService.scheduling.OutboxScheduler;
import com.application.bookPurchaseService.scheduling.SchedulerConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    properties = {
      "topic-to-send-message=test-response-topic",
      "topic-to-consume-message=test-request-topic",
      "spring.kafka.consumer.auto-offset-reset=earliest"
    })
@Import({
  BookPurchaseConsumer.class,
  KafkaAutoConfiguration.class,
  SchedulerConfig.class,
  OutboxScheduler.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookPurchaseServiceSuccessTest extends DatabaseSuite {
  @Container @ServiceConnection
  public static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

  @Autowired private BookPurchaseService bookService;
  @Autowired private BookPurchaseConsumer purchaseConsumer;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private KafkaTemplate<String, String> kafkaTemplate;

  @Test
  void shouldSendSuccessMessageToKafkaE2ETest()
      throws JsonProcessingException, InterruptedException {
    var uuid = UUID.randomUUID().toString();

    bookService.setAccountMoney(1000L);

    kafkaTemplate.send(
        "test-request-topic", objectMapper.writeValueAsString(new BuyBookMessageRequest(1L, uuid)));

    KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "some-group");
    consumer.subscribe(List.of("test-response-topic"));

    Thread.sleep(10_000);

    ConsumerRecords<String, String> records = consumer.poll();
    assertEquals(1, records.count());
    records
        .iterator()
        .forEachRemaining(
            record -> {
              BuyBookMessageResponse message;
              try {
                message = objectMapper.readValue(record.value(), BuyBookMessageResponse.class);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
              assertEquals(1L, message.bookId());
              assertTrue(message.success());
              assertEquals(message.message(), "Success");
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
