package com.application.bookService;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TestConfig {

  @Bean
  public RestTemplate restTemplate(
      @Value("${author-registry.service.base.url}") String baseUrl,
      @Value("${book.service.timeout.seconds}") long secondsTimeout) {
    Duration timeout = Duration.ofSeconds(secondsTimeout);
    return new RestTemplateBuilder()
        .setConnectTimeout(timeout)
        .setReadTimeout(timeout)
        .rootUri(baseUrl)
        .build();
  }
}
