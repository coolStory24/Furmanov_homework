package com.application.bookRatingService;

import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
  private final Random random;

  public RatingService() {
    this.random = new Random();
  }

  public double getRating(Long bookId) {
    return random.nextDouble(0, 5);
  }
}
