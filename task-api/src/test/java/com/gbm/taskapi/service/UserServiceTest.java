package com.gbm.taskapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.model.Role;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.UserRepository;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

@SpringBootTest
public class UserServiceTest extends TestContainerSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        // Clear cache before each test
        Objects.requireNonNull(cacheManager.getCache("users")).clear();
        userRepository.deleteAll();
    }

    @Test
    public void testUserCaching() {
        // Given
        User user = new User();
        user.setEmail("cache@test.com");
        user.setPassword("password");
        user.setFirstName("Cache");
        user.setLastName("Test");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        // When - First call (cache miss)
        System.out.println("=== First call (should hit DB) ===");
        userService.findById(user.getId());

        // When - Second call (cache hit)
        System.out.println("=== Second call (should hit cache) ===");
        userService.findById(user.getId());

        // Then
        // Check logs to verify cache is working
        // First call should print "DB QUERY: Finding user by ID..."
        // Second call should NOT print that message
    }

    @Test
    public void testCacheEvictionOnDelete() {
        // Given
        User user = new User();
        user.setEmail("evict@test.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        // Cache the user
        userService.findById(user.getId());

        // When
        userService.deleteUser(user.getId(), user.getEmail());

        // Then
        var cached = cacheManager.getCache("users").get(user.getId());
        assertThat(cached).isNull();
    }
}
