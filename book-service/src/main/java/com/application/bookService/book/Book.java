package com.application.bookService.book;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

import com.application.bookService.author.Author;
import com.application.bookService.book.events.BookStatusUpdatedEvent;
import com.application.bookService.tag.Tag;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
@Table(name = "books")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Book extends AbstractAggregateRoot<Book> {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @NotNull(message = "Book title have to be filled")
  private String title;

  @Setter
  @Column(name = "rating", nullable = false, precision = 3, scale = 2)
  private BigDecimal rating = BigDecimal.ZERO;

  @ManyToOne(fetch = EAGER)
  @JoinColumn(name = "author_id", insertable = false, updatable = false)
  private Author author;

  @Setter
  @Column(name = "author_id")
  private Long authorId;

  @ManyToMany(fetch = LAZY, cascade = PERSIST)
  @JoinTable(
      name = "book_tag",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Setter
  @Column(name = "status", nullable = false)
  private PaymentStatus status = PaymentStatus.NO_PAYMENT;

  public Book(String title, Long authorId, PaymentStatus paymentStatus) {}

  public void setPaymentStatusPending(UUID messageId) {
    this.setStatus(PaymentStatus.PAYMENT_PENDING);
    registerEvent(new BookStatusUpdatedEvent(this.id, this.status, messageId.toString()));
  }

  public Book(String title, Long authorId) {
    this.title = title;
    this.authorId = authorId;
  }

  public void addTag(Tag tag) {
    tags.add(tag);
  }
}
