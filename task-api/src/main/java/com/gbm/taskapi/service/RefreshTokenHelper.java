package com.gbm.taskapi.service;

import com.gbm.taskapi.exception.InvalidTokenException;
import com.gbm.taskapi.model.RefreshToken;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.RefreshTokenRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenHelper {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenValidityInSeconds;

    public RefreshTokenHelper(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration}") long refreshTokenValidityInSeconds) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        var refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenValidityInSeconds));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String token) {
        var existing = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found"));

        if (existing.isExpired()) {
            refreshTokenRepository.delete(existing);
            throw new InvalidTokenException("Token expired");
        }

        var user = existing.getUser();
        refreshTokenRepository.delete(existing);
        return createRefreshToken(user);
    }

    @Transactional
    public void revokeRefreshTokensForUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
