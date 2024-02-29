package com.application.bookService;

import com.application.bookService.dto.BookRequest.CreateBookRequest;
import com.application.bookService.dto.BookRequest.UpdateBookRequest;
import com.application.bookService.dto.BookResponse.CreateBookResponse;
import com.application.bookService.dto.BookResponse.GetBookResponse;
import com.application.bookService.exception.BookExceptions;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
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
  public CreateBookResponse create(@NotNull @RequestBody @Valid CreateBookRequest body) {
    return this.bookService.createBook(body.title(), body.author(), body.tags());
  }

  @Operation(summary = "Get a book by its id")
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public GetBookResponse getById(
      @NotNull @PathVariable @Max(value = Integer.MAX_VALUE) @Min(value = 0) Integer id)
      throws BookExceptions.BookNotFoundException {
    return this.bookService.findBookById(id);
  }

  @Operation(summary = "Update a book")
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void update(
      @NotNull @PathVariable @Max(value = Integer.MAX_VALUE) @Min(value = 0) Integer id,
      @NotNull @RequestBody @Valid UpdateBookRequest body)
      throws BookExceptions.BookNotFoundException {
    this.bookService.updateBook(id, body.title(), body.author(), body.tags());
  }

  @Operation(summary = "Delete a book")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @NotNull @PathVariable @Max(value = Integer.MAX_VALUE) @Min(value = 0) Integer id) {
    this.bookService.deleteBook(id);
  }

  // get books by tag if tag is not null, all books otherwise
  @GetMapping("/view")
  public String viewBooks(Model model, @RequestParam(required = false) String tag) {
    List<GetBookResponse> books = bookService.getAllBooks(tag);

    model.addAttribute("books", books);
    return "books";
  }

  @ExceptionHandler
  public ResponseEntity<String> bookNotFoundException(BookExceptions.BookNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }
}
