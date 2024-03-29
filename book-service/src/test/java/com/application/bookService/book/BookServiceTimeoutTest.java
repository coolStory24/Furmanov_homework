package com.application.bookService.book;

import static org.junit.Assert.assertThrows;
import static org.mockserver.model.HttpRequest.request;

import com.application.bookService.DatabaseSuite;
import com.application.bookService.TestConfig;
import com.application.bookService.author.AuthorService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookService.class, AuthorService.class, TestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookServiceTimeoutTest extends DatabaseSuite {
  @Autowired private AuthorService authorService;

  @Autowired private BookService bookService;

  @Container
  public static final MockServerContainer mockServer =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

  @BeforeEach
  void setUp() {
    var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    client
        .when(
            request()
                .withMethod(String.valueOf(HttpMethod.POST))
                .withHeader("X-REQUEST-ID")
                .withPath("/api/author-registry"))
        .respond(
            req -> {
              Thread.sleep(10000);
              return new HttpResponse()
                  .withBody("{\"isAuthor\": \"true\"}")
                  .withHeader("Content-Type", "application/json");
            });
  }

  @Test
  void shouldThrowTimeoutException() {

    var author = authorService.createAuthor("J.K.", "Rowling");

    assertThrows(
        RestClientException.class,
        () -> bookService.createBook("Harry Porter", author.id(), UUID.randomUUID().toString()));
  }
}
