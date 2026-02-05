package com.gbm.taskapi.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.model.Role;
import com.gbm.taskapi.model.User;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest extends TestContainerSupport {

    @Autowired
    UserRepository userRepository;

    @Test
    @Order(1)
    public void testSaveUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @Order(2)
    public void testFindByEmail() {
        // Given
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @Order(3)
    public void testExistsByEmail() {
        // Given
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }
}
