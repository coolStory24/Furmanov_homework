package com.application.bookService.book;

import com.application.bookService.BookRatingProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books-rating")
@Validated
public class RatingController {
  @Autowired private final BookRatingProducer bookRatingProducer;

  public RatingController(BookRatingProducer bookRatingProducer) {
    this.bookRatingProducer = bookRatingProducer;
  }

  @PostMapping("/{bookId}")
  public void calculateRating(@PathVariable @NotNull Long bookId) throws JsonProcessingException {
    bookRatingProducer.sendBookRatingCalculationRequest(bookId);
  }
}
