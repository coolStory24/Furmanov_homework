package com.application.bookPurchaseService;

import com.application.bookPurchaseService.account.AccountRepository;
import com.application.bookPurchaseService.dto.kafka.BuyBookMessageResponse;
import com.application.bookPurchaseService.outbox.OutboxEntity;
import com.application.bookPurchaseService.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookPurchaseService {
  private final OutboxRepository outboxRepository;
  private final AccountRepository accountRepository;
  private final ObjectMapper objectMapper;

  @Autowired
  public BookPurchaseService(
      OutboxRepository outboxRepository,
      AccountRepository accountRepository,
      ObjectMapper objectMapper) {
    this.outboxRepository = outboxRepository;
    this.accountRepository = accountRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void buyBook(Long bookId) throws JsonProcessingException {
    var bookCost = 100L;

    var account = accountRepository.findAll().get(0);

    String messageToSend;

    if (account.getAmount() < bookCost) {
      messageToSend =
          objectMapper.writeValueAsString(
              new BuyBookMessageResponse(bookId, false, "Not enough money on the account"));

    } else {
      account.decreaseBy(bookCost);
      accountRepository.save(account);

      messageToSend =
          objectMapper.writeValueAsString(new BuyBookMessageResponse(bookId, true, "Success"));
    }

    outboxRepository.save(new OutboxEntity(messageToSend));
  }

  public void setAccountMoney(Long amount) {
    var account = accountRepository.findAll().get(0);

    account.setAmount(amount);

    accountRepository.save(account);
  }
}
