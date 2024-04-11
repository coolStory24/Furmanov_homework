package com.application.bookPurchaseService.outbox;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "outbox")
@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String data;

  @Setter private boolean send;

  public OutboxEntity(String data) {
    this.data = data;
  }
}
