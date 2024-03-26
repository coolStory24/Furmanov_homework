package com.application.authorRegistryService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.application.authorRegistryService.authorRegistry.dto.GetAuthorRegistryRequest;
import com.application.authorRegistryService.authorRegistry.dto.GetAuthorRegistryResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthorRegistryControllerTest {
  @Autowired private TestRestTemplate rest;

  @Test
  void shouldReturnYesIfIsAuthor() {
    HttpHeaders headers = new HttpHeaders();

    headers.add("X-REQUEST-ID", UUID.randomUUID().toString());

    ResponseEntity<GetAuthorRegistryResponse> checkAuthorRegistryResponse =
        rest.exchange(
            "/api/author-registry",
            HttpMethod.POST,
            new HttpEntity<>(
                new GetAuthorRegistryRequest("Aldous", "Huxley", "Brave New World"), headers),
            GetAuthorRegistryResponse.class);

    assertNotNull(checkAuthorRegistryResponse.getBody());
    assertEquals(checkAuthorRegistryResponse.getBody().isAuthor(), true);
  }

  @Test
  void shouldReturnNoIfIsNotAuthor() {
    HttpHeaders headers = new HttpHeaders();

    headers.add("X-REQUEST-ID", UUID.randomUUID().toString());

    ResponseEntity<GetAuthorRegistryResponse> checkAuthorRegistryResponse =
        rest.exchange(
            "/api/author-registry",
            HttpMethod.POST,
            new HttpEntity<>(new GetAuthorRegistryRequest("Aldous", "Huxley", "1984"), headers),
            GetAuthorRegistryResponse.class);

    assertNotNull(checkAuthorRegistryResponse.getBody());
    assertEquals(checkAuthorRegistryResponse.getBody().isAuthor(), false);
  }
}
