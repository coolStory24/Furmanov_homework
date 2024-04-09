package com.application.bookPurchaseService.account;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "account")
@Entity
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Account {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Getter
  @Setter
  @Column(nullable = false)
  private Long amount = 0L;

  public void decreaseBy(Long delta) {
    this.amount -= delta;
  }
}
