package com.gbm.taskapi.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CacheServiceTest extends TestContainerSupport {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testRedisConnection() {
        // Given
        String key = "test:key";
        String value = "test value";

        // When
        cacheService.set(key, value);
        Object retrieved = cacheService.get(key);

        // Then
        assertThat(retrieved).isEqualTo(value);

        // Cleanup
        cacheService.delete(key);
    }
}
