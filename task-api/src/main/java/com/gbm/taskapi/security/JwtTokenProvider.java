package com.gbm.taskapi.security;

import com.gbm.taskapi.dto.TokenInfo;
import com.gbm.taskapi.exception.InvalidTokenException;
import com.gbm.taskapi.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

    private final long jwtExpirationInMs;
    private final SecretKey key;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") long jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(TokenInfo tokenInfo) {
        log.debug("Generate token for user {}", tokenInfo.userId());
        var now = new Date();
        var expiration = new Date(now.getTime() + jwtExpirationInMs);
        return Jwts.builder()
                .subject(String.valueOf(tokenInfo.userId()))
                .claims()
                .add("email", tokenInfo.email())
                .add("role", tokenInfo.role().name())
                .and()
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public void validateToken(String token) {
        log.debug("Validate token {}...", token.substring(0, Math.min(token.length(), 10)));
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (Exception e) {
            log.error("Invalid JWT token", e);
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    public Long getUserIdFromToken(String token) {
        var claims =
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public TokenInfo getTokenInfoFromToken(String token) {
        var payload =
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        var userId = Long.parseLong(payload.getSubject());
        var email = payload.get("email", String.class);
        var role = Role.valueOf(payload.get("role", String.class));
        return TokenInfo.builder().userId(userId).email(email).role(role).build();
    }

    public String getEmailFromToken(String token) {
        var claims =
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.get("email", String.class);
    }
}
