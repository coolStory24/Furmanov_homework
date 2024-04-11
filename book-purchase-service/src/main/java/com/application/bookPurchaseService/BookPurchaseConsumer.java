package com.application.bookPurchaseService;

import com.application.bookPurchaseService.dto.kafka.BuyBookMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookPurchaseConsumer {
  private final ObjectMapper objectMapper;
  @Autowired private final BookPurchaseService bookPurchaseService;

  private final Set<String> proceededMessages;

  private static final Logger LOGGER = LoggerFactory.getLogger(BookPurchaseConsumer.class);

  public BookPurchaseConsumer(ObjectMapper objectMapper, BookPurchaseService bookPurchaseService) {
    this.objectMapper = objectMapper;
    this.bookPurchaseService = bookPurchaseService;
    this.proceededMessages = new HashSet<>();
  }

  @KafkaListener(topics = {"${topic-to-consume-message}"})
  @Transactional(propagation = Propagation.REQUIRED)
  public void consumeBookPurchaseResponse(String message) throws JsonProcessingException {
    BuyBookMessageRequest parsedMessage =
        objectMapper.readValue(message, BuyBookMessageRequest.class);

    if (proceededMessages.contains(parsedMessage.messageId())) return;

    proceededMessages.add(parsedMessage.messageId());

    bookPurchaseService.buyBook(parsedMessage.bookId());
  }
}
