package com.application.bookService.tag;

import com.application.bookService.tag.dto.request.CreateTagRequest;
import com.application.bookService.tag.dto.response.CreateTagResponse;
import com.application.bookService.tag.dto.response.GetTagResponse;
import com.application.bookService.tag.exceptions.TagNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@ControllerAdvice
@RequestMapping("/api/tags")
@Validated
public class TagController {
  private final TagService tagService;

  @Autowired
  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @Operation(summary = "Create a tag")
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public CreateTagResponse create(@RequestBody @Valid CreateTagRequest body) {
    return tagService.createTag(body.name());
  }

  @Operation(summary = "Get tag by bookId")
  @GetMapping("/{bookId}")
  @ResponseStatus(HttpStatus.OK)
  public GetTagResponse getById(
      @Pattern(
              regexp =
                  "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
              message = "Invalid UUID format")
          @PathVariable
          String id)
      throws TagNotFoundException {
    return tagService.getTagById(UUID.fromString(id));
  }

  @Operation(summary = "Update tag")
  @PutMapping("/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void update(
      @Pattern(
              regexp =
                  "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
              message = "Invalid UUID format")
          @PathVariable
          String id,
      @RequestBody @Valid CreateTagRequest body)
      throws TagNotFoundException {
    tagService.updateTag(UUID.fromString(id), body.name());
  }

  @Operation(summary = "Delete tag")
  @DeleteMapping("/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Pattern(
              regexp =
                  "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
              message = "Invalid UUID format")
          @PathVariable
          String id)
      throws TagNotFoundException {
    tagService.deleteTag(UUID.fromString(id));
  }

  @ExceptionHandler
  public ResponseEntity<String> tagNotFoundException(TagNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }
}
