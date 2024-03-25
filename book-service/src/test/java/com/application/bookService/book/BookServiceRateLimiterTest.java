package com.application.bookService.book;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import com.application.bookService.DatabaseSuite;
import com.application.bookService.author.AuthorService;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.authorRegistry.dto.GetAuthorRegistryRequest;
import com.application.bookService.authorRegistry.dto.GetAuthorRegistryResponse;
import com.application.bookService.book.exceptions.CreateBookException;
import com.application.bookService.book.exceptions.IsNotAuthorException;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookService.class, AuthorService.class, RateLimiterAutoConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAspectJAutoProxy
@TestPropertySource(locations = "classpath:test.properties")
public class BookServiceRateLimiterTest extends DatabaseSuite {
  @MockBean private RestTemplate restTemplate;
  @Autowired private BookService bookService;
  @Autowired private AuthorService authorService;

  @Test
  void shouldThrowTimeoutException() throws AuthorNotFoundException, IsNotAuthorException {
    var author = authorService.createAuthor("Aldous", "Huxley");

    var uuid = UUID.randomUUID().toString();

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-REQUEST-ID", uuid);

    when(restTemplate.exchange(
            "/api/author-registry",
            HttpMethod.POST,
            new HttpEntity<>(
                new GetAuthorRegistryRequest(
                    author.firstName(), author.lastName(), "Brave new World"),
                headers),
            GetAuthorRegistryResponse.class))
        .thenAnswer(
            (Answer<ResponseEntity<GetAuthorRegistryResponse>>)
                invocation -> {
                  Thread.sleep(2000);
                  return new ResponseEntity<>(new GetAuthorRegistryResponse(true), HttpStatus.OK);
                });

    assertDoesNotThrow(() -> bookService.createBook("Brave new World", author.id(), uuid));

    assertThrows(
        CreateBookException.class,
        () -> bookService.createBook("The War of the Worlds", author.id(), uuid));
  }
}
