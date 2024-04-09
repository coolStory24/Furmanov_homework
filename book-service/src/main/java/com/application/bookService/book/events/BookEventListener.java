package com.application.bookService.book.events;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;

import com.application.bookService.book.dto.kafka.BuyBookMessageRequest;
import com.application.bookService.outbox.OutboxEntity;
import com.application.bookService.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class BookEventListener {
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public BookEventListener(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @TransactionalEventListener(phase = BEFORE_COMMIT)
  public void onUserRolesUpdated(BookStatusUpdatedEvent event) throws JsonProcessingException {
    var outboxEntity =
        new OutboxEntity(
            objectMapper.writeValueAsString(
                new BuyBookMessageRequest(event.bookId(), event.messageId())));

    outboxRepository.save(outboxEntity);
  }
}
