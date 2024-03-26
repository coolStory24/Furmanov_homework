package com.application.bookService;

import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestDataSourceConfiguration {
  private static final PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:13");

  @Bean
  @Primary
  public DataSource dataSource() {
    postgreSQLContainer.start();

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(postgreSQLContainer.getJdbcUrl());
    dataSource.setUsername(postgreSQLContainer.getUsername());
    dataSource.setPassword(postgreSQLContainer.getPassword());

    return dataSource;
  }

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
