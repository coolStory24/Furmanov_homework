package com.application.authorRegistryService;

import com.application.authorRegistryService.authorRegistry.dto.GetAuthorRegistryRequest;
import com.application.authorRegistryService.authorRegistry.dto.GetAuthorRegistryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@ControllerAdvice
@RequestMapping("/api")
@Validated
public class AuthorRegistryController {
  private final AuthorRegistryRepository authorRegistryRepository;

  private final Map<String, Boolean> requestIdMap = new ConcurrentHashMap<String, Boolean>();

  @Autowired
  public AuthorRegistryController(AuthorRegistryRepository authorRegistryRepository) {
    this.authorRegistryRepository = authorRegistryRepository;
  }

  @Operation(summary = "Check author registry")
  @PostMapping("/author-registry")
  @ResponseStatus(HttpStatus.OK)
  public GetAuthorRegistryResponse getAuthorRegistry(
      @NotNull @RequestHeader("X-REQUEST-ID") String requestId,
      @NotNull @RequestBody @Valid GetAuthorRegistryRequest getAuthorRegistryRequest) {
    boolean isAuthor =
        requestIdMap.computeIfAbsent(
            requestId,
            k ->
                authorRegistryRepository.isAuthor(
                    getAuthorRegistryRequest.bookName(),
                    new AuthorInfo(
                        getAuthorRegistryRequest.firstName(),
                        getAuthorRegistryRequest.lastName())));

    return new GetAuthorRegistryResponse(
        authorRegistryRepository.isAuthor(
            getAuthorRegistryRequest.bookName(),
            new AuthorInfo(
                getAuthorRegistryRequest.firstName(), getAuthorRegistryRequest.lastName())));
  }
}
