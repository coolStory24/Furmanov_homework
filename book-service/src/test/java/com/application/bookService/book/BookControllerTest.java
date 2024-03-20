package com.application.bookService.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;

import com.application.bookService.TestDataSourceConfiguration;
import com.application.bookService.author.dto.request.CreateAuthorRequest;
import com.application.bookService.author.dto.response.CreateAuthorResponse;
import com.application.bookService.book.dto.request.CreateBookRequest;
import com.application.bookService.book.dto.response.CreateBookResponse;
import com.application.bookService.book.dto.response.GetBookResponse;
import com.application.bookService.tag.dto.request.CreateTagRequest;
import com.application.bookService.tag.dto.response.CreateTagResponse;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestDataSourceConfiguration.class)
class BookControllerTest {
  @Autowired private TestRestTemplate rest;

  @Container
  public static final MockServerContainer mockServer =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"));

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("author-registry.service.base.url", mockServer::getEndpoint);
  }

  @Test
  void bookE2ETest() {
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

    ResponseEntity<CreateTagResponse> createTagResponseEntity1 =
        rest.postForEntity(
            "/api/tags", new CreateTagRequest("Science Fiction"), CreateTagResponse.class);

    assertNotNull(createTagResponseEntity1.getBody());
    assertEquals(createTagResponseEntity1.getBody().name(), "Science Fiction");

    ResponseEntity<CreateTagResponse> createTagResponseEntity2 =
        rest.postForEntity(
            "/api/tags", new CreateTagRequest("Future Society"), CreateTagResponse.class);

    assertNotNull(createTagResponseEntity2.getBody());

    ResponseEntity<CreateAuthorResponse> createAuthorResponseEntity =
        rest.postForEntity(
            "/api/authors",
            new CreateAuthorRequest("Aldous", "Huxley"),
            CreateAuthorResponse.class);

    assertNotNull(createAuthorResponseEntity.getBody());
    assertEquals(createAuthorResponseEntity.getBody().firstName(), "Aldous");
    assertEquals(createAuthorResponseEntity.getBody().lastName(), "Huxley");

    ResponseEntity<CreateBookResponse> createBookResponseEntity =
        rest.postForEntity(
            "/api/books",
            new CreateBookRequest("Brave New World", createAuthorResponseEntity.getBody().id()),
            CreateBookResponse.class);

    assertNotNull(createBookResponseEntity.getBody());
    assertEquals(createBookResponseEntity.getBody().title(), "Brave New World");

    rest.postForEntity(
        "/api/books/"
            + createBookResponseEntity.getBody().id()
            + "/add_tag/"
            + createTagResponseEntity1.getBody().id(),
        null,
        Object.class);

    var res =
        rest.postForEntity(
            "/api/books/"
                + createBookResponseEntity.getBody().id()
                + "/add_tag/"
                + createTagResponseEntity2.getBody().id(),
            null,
            Object.class);

    ResponseEntity<GetBookResponse> getBookResponseEntity =
        rest.getForEntity(
            "/api/books/" + createBookResponseEntity.getBody().id(), GetBookResponse.class);

    assertNotNull(getBookResponseEntity.getBody());
    assertEquals(getBookResponseEntity.getBody().author().firstName(), "Aldous");
    assertEquals(getBookResponseEntity.getBody().author().lastName(), "Huxley");
    assertEquals(getBookResponseEntity.getBody().tags().size(), 2);
  }
}
