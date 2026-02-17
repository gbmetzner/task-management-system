package com.gbm.taskapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.exception.InvalidTokenException;
import com.gbm.taskapi.model.RefreshToken;
import com.gbm.taskapi.model.Role;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.RefreshTokenRepository;
import com.gbm.taskapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RefreshTokenHelperTest extends TestContainerSupport {

    @Autowired
    private RefreshTokenHelper refreshTokenHelper;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user = userRepository.save(user);
    }

    @Test
    @DisplayName("Should create a refresh token for a user")
    void shouldCreateRefreshToken() {
        RefreshToken token = refreshTokenHelper.createRefreshToken(user);

        assertThat(token.getId()).isNotNull();
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getUser().getId()).isEqualTo(user.getId());
        assertThat(token.getExpiresAt()).isAfter(token.getCreatedAt());
    }

    @Test
    @DisplayName("Should rotate refresh token — delete old, create new")
    void shouldRotateRefreshToken() {
        RefreshToken original = refreshTokenHelper.createRefreshToken(user);
        String originalTokenValue = original.getToken();

        RefreshToken rotated = refreshTokenHelper.rotateRefreshToken(originalTokenValue);

        assertThat(rotated.getToken()).isNotEqualTo(originalTokenValue);
        assertThat(rotated.getUser().getId()).isEqualTo(user.getId());
        assertThat(refreshTokenRepository.findByToken(originalTokenValue)).isEmpty();
    }

    @Test
    @DisplayName("Should throw when rotating a non-existent token")
    void shouldThrowWhenTokenNotFound() {
        assertThatThrownBy(() -> refreshTokenHelper.rotateRefreshToken("non-existent-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token not found");
    }

    @Test
    @DisplayName("Should throw and delete when rotating an expired token")
    void shouldThrowWhenTokenExpired() {
        RefreshToken token = refreshTokenHelper.createRefreshToken(user);
        // Force expiration
        token.setExpiresAt(token.getCreatedAt().minusSeconds(1));
        refreshTokenRepository.save(token);

        String tokenValue = token.getToken();

        assertThatThrownBy(() -> refreshTokenHelper.rotateRefreshToken(tokenValue))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token expired");

        assertThat(refreshTokenRepository.findByToken(tokenValue)).isEmpty();
    }

    @Test
    @DisplayName("Should revoke all refresh tokens for a user")
    void shouldRevokeAllTokensForUser() {
        refreshTokenHelper.createRefreshToken(user);
        refreshTokenHelper.createRefreshToken(user);

        assertThat(refreshTokenRepository.findAll()).hasSize(2);

        refreshTokenHelper.revokeRefreshTokensForUser(user.getId());

        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }
}
