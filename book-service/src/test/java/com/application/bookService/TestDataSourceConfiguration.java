package com.application.bookService;

import javax.sql.DataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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
}
