package com.gbm.taskapi;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
public abstract class TestContainerSupport {

    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine");

    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer("redis:7-alpine");

    static {
        postgreSQLContainer.start();
        redisContainer.start();
    }
}
