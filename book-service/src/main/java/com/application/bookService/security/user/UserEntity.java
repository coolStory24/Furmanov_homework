package com.application.bookService.security.user;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String username;

  private String password;

  @JdbcTypeCode(SqlTypes.JSON)
  private Set<Role> roles;

  public enum Role {
    READER,
    ADMIN
  }

  public UserEntity(String username, String password, Set<Role> roles) {
    this.username = username;
    this.password = password;
    this.roles = roles;
  }
}
