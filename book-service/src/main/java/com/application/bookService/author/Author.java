package com.application.bookService.author;

import com.application.bookService.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "authors")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Author {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @NotNull(message = "Author's firstname have to be filled")
  @Column(name = "first_name")
  @Size(max = 200)
  private String firstName;

  @Setter
  @Column(name = "last_name")
  @NotNull(message = "Author's lastname have to be filled")
  @Size(max = 200)
  private String lastName;

  @OneToMany(mappedBy = "author", orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Book> books = new ArrayList<>();

  public Author(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
