package com.application.bookService.book;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockserver.model.HttpRequest.request;

import com.application.bookService.TestConfig;
import com.application.bookService.author.AuthorService;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
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
@Import({
  BookService.class,
  AuthorService.class,
  TestConfig.class,
  RateLimiterAutoConfiguration.class
})
@TestPropertySource(
    properties = {
      "resilience4j.ratelimiter.instances.createBook.limitForPeriod=1",
      "resilience4j.ratelimiter.instances.createBook.limitRefreshPeriod=5s",
      "resilience4j.ratelimiter.instances.createBook.timeoutDuration=5s"
    })
public class BookServiceRateLimiterTest {
  @Autowired private BookService bookService;

  @Autowired private AuthorService authorService;

  @Container
  public static final MockServerContainer mockServer =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("author-registry.service.base.url", mockServer::getEndpoint);
  }

  @Test
  void shouldThrowTimeoutException() {
    var client = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
    client
        .when(
            request()
                .withMethod(String.valueOf(HttpMethod.POST))
                .withHeader("X-REQUEST-ID")
                .withPath("/api/author-registry"))
        .respond(
            new HttpResponse()
                .withBody("{\"isAuthor\": \"true\"}")
                .withHeader("Content-Type", "application/json"));

    var author = authorService.createAuthor("J.K.", "Rowling");

    assertDoesNotThrow(
        () -> bookService.createBook("Harry Porter", author.id(), UUID.randomUUID().toString()));

    assertThrows(
        RestClientException.class,
        () -> bookService.createBook("Harry Porter 1", author.id(), UUID.randomUUID().toString()));
  }
}
