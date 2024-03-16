package com.application.bookService.book;

import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.book.dto.request.CreateBookRequest;
import com.application.bookService.book.dto.request.UpdateBookRequest;
import com.application.bookService.book.dto.response.CreateBookResponse;
import com.application.bookService.book.dto.response.GetBookResponse;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.application.bookService.book.exceptions.CreateBookException;
import com.application.bookService.book.exceptions.IsNotAuthorException;
import com.application.bookService.tag.exceptions.TagNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@ControllerAdvice
@RequestMapping("/api/books")
@Validated
public class BookController {
  private final BookService bookService;

  @Autowired
  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @Operation(summary = "Create a book")
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public CreateBookResponse create(@NotNull @RequestBody @Valid CreateBookRequest body)
      throws AuthorNotFoundException, IsNotAuthorException {
    return this.bookService.createBook(body.title(), body.authorId(), UUID.randomUUID().toString());
  }

  @Operation(summary = "Get a book by its id")
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public GetBookResponse getById(@NotNull @PathVariable @Min(value = 0) Long id)
      throws BookNotFoundException {
    return this.bookService.getBookById(id);
  }

  @Operation(summary = "Get all books")
  @GetMapping("/all")
  @ResponseStatus(HttpStatus.OK)
  public List<GetBookResponse> getAll() {
    return this.bookService.getAllBooks();
  }

  @Operation(summary = "Update a book")
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void update(
      @NotNull @PathVariable @Min(value = 0) Long id,
      @NotNull @RequestBody @Valid UpdateBookRequest body)
      throws BookNotFoundException {
    this.bookService.updateBook(id, body.title(), body.authorId());
  }

  @Operation(summary = "Add tag by id")
  @PostMapping("/{bookId}/add_tag/{tagId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addTag(
      @NotNull @PathVariable("bookId") @Min(value = 0) Long bookId,
      @NotNull @PathVariable("tagId") String tagId)
      throws BookNotFoundException, TagNotFoundException {
    this.bookService.addTag(bookId, tagId);
  }

  @Operation(summary = "Delete a book")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@NotNull @PathVariable @Min(value = 0) Long id) {
    this.bookService.deleteBook(id);
  }

  @Operation(summary = "Delete tag from book by id")
  @DeleteMapping("/{bookId}/remove_tag/{tagId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeTag(
      @NotNull @PathVariable("bookId") @Min(value = 0) Long bookId,
      @NotNull @PathVariable("tagId") String tagId)
      throws BookNotFoundException, TagNotFoundException {
    this.bookService.removeTag(bookId, tagId);
  }

  @ExceptionHandler
  public ResponseEntity<String> bookNotFoundException(BookNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }
}
