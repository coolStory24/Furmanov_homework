package com.application.bookPurchaseService.scheduling;

import com.application.bookPurchaseService.outbox.OutboxEntity;
import com.application.bookPurchaseService.outbox.OutboxRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxScheduler {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final String topic;
  private final OutboxRepository outboxRepository;

  public OutboxScheduler(
      KafkaTemplate<String, String> kafkaTemplate,
      OutboxRepository outboxRepository,
      @Value("${topic-to-send-message}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
    this.outboxRepository = outboxRepository;
  }

  @Transactional
  @Scheduled(fixedDelay = 10000)
  public void processOutbox() {
    List<OutboxEntity> result = outboxRepository.findAllBySendEquals(false);
    for (OutboxEntity outboxRecord : result) {
      send(outboxRecord);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void send(OutboxEntity outboxEntity) {
    CompletableFuture<SendResult<String, String>> sendResult =
        kafkaTemplate.send(topic, outboxEntity.getData());
    outboxEntity.setSend(true);

    outboxRepository.save(outboxEntity);
  }
}
