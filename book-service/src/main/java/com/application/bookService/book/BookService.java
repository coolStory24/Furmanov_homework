package com.application.bookService.book;

import com.application.bookService.author.AuthorRepository;
import com.application.bookService.author.dto.response.GetAuthorResponse;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.authorRegistry.dto.GetAuthorRegistryRequest;
import com.application.bookService.authorRegistry.dto.GetAuthorRegistryResponse;
import com.application.bookService.book.dto.response.BuyBookResponse;
import com.application.bookService.book.dto.response.CreateBookResponse;
import com.application.bookService.book.dto.response.GetBookResponse;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.application.bookService.book.exceptions.CreateBookException;
import com.application.bookService.book.exceptions.IsNotAuthorException;
import com.application.bookService.tag.Tag;
import com.application.bookService.tag.TagRepository;
import com.application.bookService.tag.dto.response.GetTagResponse;
import com.application.bookService.tag.exceptions.TagNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class BookService {
  private final BookRepository bookRepository;
  private final TagRepository tagRepository;
  private final AuthorRepository authorRepository;
  public final RestTemplate restTemplate;

  @Autowired
  public BookService(
      BookRepository bookRepository,
      TagRepository tagRepository,
      AuthorRepository authorRepository,
      RestTemplate restTemplate) {
    this.bookRepository = bookRepository;
    this.tagRepository = tagRepository;
    this.authorRepository = authorRepository;
    this.restTemplate = restTemplate;
  }

  @RateLimiter(name = "createBook", fallbackMethod = "fallbackRateLimiter")
  @CircuitBreaker(name = "createBook", fallbackMethod = "fallbackCircuitBreaker")
  @Retry(name = "createBook", fallbackMethod = "fallbackRetry")
  @Transactional(
      propagation = Propagation.REQUIRES_NEW,
      rollbackFor = {Throwable.class})
  public CreateBookResponse createBook(String title, Long authorId, String requestId)
      throws AuthorNotFoundException, IsNotAuthorException {
    try {
      var author = authorRepository.findById(authorId).orElse(null);
      if (author == null) {
        throw new AuthorNotFoundException(authorId);
      }

      HttpHeaders headers = new HttpHeaders();

      headers.add("X-REQUEST-ID", requestId);

      ResponseEntity<GetAuthorRegistryResponse> authorRegistry =
          restTemplate.exchange(
              "/api/author-registry",
              HttpMethod.POST,
              new HttpEntity<>(
                  new GetAuthorRegistryRequest(author.getFirstName(), author.getLastName(), title),
                  headers),
              GetAuthorRegistryResponse.class);

      if (!Objects.requireNonNull(authorRegistry.getBody()).isAuthor()) {
        throw new IsNotAuthorException(authorId);
      }

      var createdBook = this.bookRepository.save(new Book(title, authorId));
      return new CreateBookResponse(
          createdBook.getId(), createdBook.getTitle(), createdBook.getAuthorId());
    } catch (DataIntegrityViolationException e) {
      throw new AuthorNotFoundException(authorId);
    } catch (RestClientException e) {
      throw new RestClientException("Unsuccessful request to author registry service");
    }
  }

  @Transactional
  public GetBookResponse getBookById(Long id) throws BookNotFoundException {
    var book = bookRepository.findById(id).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(id);
    }
    var author = book.getAuthor();

    return new GetBookResponse(
        book.getId(),
        book.getTitle(),
        new GetAuthorResponse(author.getId(), author.getFirstName(), author.getLastName()),
        book.getTags().stream()
            .map(tag -> new GetTagResponse(tag.getId().toString(), tag.getName()))
            .toList());
  }

  @Transactional
  public List<GetBookResponse> getAllBooks() {
    var books = bookRepository.findAll();
    return books.stream()
        .map(
            (book ->
                new GetBookResponse(
                    book.getId(),
                    book.getTitle(),
                    new GetAuthorResponse(
                        book.getAuthor().getId(),
                        book.getAuthor().getFirstName(),
                        book.getAuthor().getLastName()),
                    book.getTags().stream()
                        .map(tag -> new GetTagResponse(tag.getId().toString(), tag.getName()))
                        .toList())))
        .toList();
  }

  @Transactional
  public void updateBook(Long id, String title, Long authorId) throws BookNotFoundException {
    var book = bookRepository.findById(id).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(id);
    }

    book.setTitle(title);
    book.setAuthorId(authorId);

    bookRepository.save(book);
  }

  @Transactional
  public void addTag(Long bookId, String tagId) throws BookNotFoundException, TagNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);
    var tag = tagRepository.findById(UUID.fromString(tagId)).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }
    if (tag == null) {
      throw new TagNotFoundException(tagId);
    }

    book.addTag(new Tag(UUID.fromString(tagId), tag.getName()));

    bookRepository.save(book);
  }

  @Transactional
  public void removeTag(Long bookId, String tagId)
      throws BookNotFoundException, TagNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);
    var tag = tagRepository.findById(UUID.fromString(tagId)).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }
    if (tag == null) {
      throw new TagNotFoundException(tagId);
    }

    book.getTags().remove(tag);

    bookRepository.save(book);
  }

  public void updateRating(Long bookId, Double rating) throws BookNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }

    book.setRating(BigDecimal.valueOf(rating));

    bookRepository.save(book);
  }

  @Transactional
  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }

  public CreateBookResponse fallbackRateLimiter(
      String title, Long authorId, String requestId, Throwable e) throws CreateBookException {
    throw new CreateBookException(e.getMessage(), e);
  }

  public CreateBookResponse fallbackCircuitBreaker(
      String title, Long authorId, String requestId, Throwable e) throws CreateBookException {
    throw new CreateBookException(e.getMessage(), e);
  }

  public CreateBookResponse fallbackRetry(
      String title, Long authorId, String requestId, Throwable e) throws CreateBookException {
    throw new CreateBookException(e.getMessage(), e);
  }

  public BuyBookResponse buyById(Long bookId) throws BookNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }

    switch (book.getStatus()) {
      case PAYMENT_PENDING -> {
        return new BuyBookResponse("Payment is already in progress");
      }
      case PAYMENT_SUCCEED -> {
        return new BuyBookResponse("Payment has been already finished");
      }
    }

    book.setPaymentStatusPending(UUID.randomUUID());
    bookRepository.save(book);

    return new BuyBookResponse("Waiting for payment...");
  }

  public void updateBookPurchaseStatus(Long bookId, PaymentStatus status)
      throws BookNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }

    book.setStatus(status);
  }
}
