package com.application.bookService.author;

import com.application.bookService.author.dto.request.CreateAuthorRequest;
import com.application.bookService.author.dto.response.CreateAuthorResponse;
import com.application.bookService.author.dto.response.GetAuthorResponse;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@ControllerAdvice
@RequestMapping("/api/authors")
@Validated
public class AuthorController {
  private final AuthorService authorService;

  @Autowired
  public AuthorController(AuthorService authorService) {
    this.authorService = authorService;
  }

  @Operation(summary = "Create an author")
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('ADMIN')")
  public CreateAuthorResponse create(@NotNull @RequestBody @Valid CreateAuthorRequest body) {
    return authorService.createAuthor(body.firstName(), body.lastName());
  }

  @Operation(summary = "Get author by bookId")
  @GetMapping("/{bookId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("isAuthenticated()")
  public GetAuthorResponse getById(@NotNull @PathVariable @Min(value = 0L) Long id)
      throws AuthorNotFoundException {
    return authorService.getAuthorById(id);
  }

  @Operation(summary = "Update author")
  @PutMapping("/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAuthority('ADMIN')")
  public void update(
      @NotNull @PathVariable @Min(value = 0L) Long id,
      @NotNull @RequestBody @Valid CreateAuthorRequest body)
      throws AuthorNotFoundException {
    authorService.updateAuthor(id, body.firstName(), body.lastName());
  }

  @Operation(summary = "Delete author")
  @DeleteMapping("/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAuthority('ADMIN')")
  public void delete(@NotNull @PathVariable @Min(value = 0L) Long id)
      throws AuthorNotFoundException {
    authorService.deleteAuthor(id);
  }

  @ExceptionHandler
  public ResponseEntity<String> authorNotFoundException(AuthorNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }
}
