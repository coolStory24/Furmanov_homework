package com.application.bookService.tag;

import com.application.bookService.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import lombok.*;

@Entity
@Table(name = "tags")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Setter
  @NotNull(message = "Tag name have to be filled")
  private String name;

  @ManyToMany(mappedBy = "tags")
  private Set<Book> users = new HashSet<>();

  public Tag(String name) {
    this.name = name;
  }

  public Tag(UUID tagId, String name) {
    id = tagId;
    this.name = name;
  }
}
