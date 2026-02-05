package com.gbm.taskapi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class TestContainerSupport {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine");

    @BeforeAll
    public static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    public static void afterAll() {
        postgreSQLContainer.stop();
    }

    //  @DynamicPropertySource
    //  static void postgreSQLProperties(DynamicPropertyRegistry registry) {
    //    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    //    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    //    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    //  }
}
