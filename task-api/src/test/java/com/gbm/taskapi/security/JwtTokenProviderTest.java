package com.gbm.taskapi.security;

import static org.junit.jupiter.api.Assertions.*;

import com.gbm.taskapi.dto.TokenInfoDto;
import com.gbm.taskapi.exception.InvalidTokenException;
import com.gbm.taskapi.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET =
            "bXlTZWNyZXRLZXlGb3JKV1RUb2tlbkdlbmVyYXRpb25UaGF0SXNBdExlYXN0MjU2Qml0c0xvbmVGb3JIUzI1NkFsZ29yaXRobQ==";
    private static final long EXPIRATION = 3600000;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    private TokenInfoDto defaultTokenInfo() {
        return TokenInfoDto.builder()
                .userId(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    private String generateValidToken() {
        return jwtTokenProvider.generateToken(defaultTokenInfo());
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("Should return a non-empty token")
        void shouldReturnNonEmptyToken() {
            String token = generateValidToken();
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        @DisplayName("Should produce a valid JWT with 3 parts")
        void shouldProduceValidJwtStructure() {
            String token = generateValidToken();
            assertEquals(3, token.split("\\.").length);
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("Should return true for a valid token")
        void shouldReturnTrueForValidToken() {
            String token = generateValidToken();

            assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("Should return false for a tampered token")
        void shouldReturnFalseForTamperedToken() {
            String token = generateValidToken();
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken(tampered));
        }

        @Test
        @DisplayName("Should return false for a token signed with a different key")
        void shouldReturnFalseForWrongKey() {
            SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                    "YW5vdGhlclNlY3JldEtleVRoYXRJc0RpZmZlcmVudEZyb21UaGVPcmlnaW5hbE9uZUZvclRlc3Rpbmc="));
            String token = Jwts.builder().subject("1").signWith(otherKey).compact();
            assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken(token));
        }

        @Test
        @DisplayName("Should return false for an expired token")
        void shouldReturnFalseForExpiredToken() {
            String expiredToken = Jwts.builder()
                    .subject("1")
                    .expiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(KEY)
                    .compact();
            assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken(expiredToken));
        }

        @Test
        @DisplayName("Should return false for malformed input")
        void shouldReturnFalseForMalformedToken() {
            assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken("not.a.jwt"));
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken")
    class GetUserIdFromToken {

        @Test
        @DisplayName("Should extract the user ID from the token subject")
        void shouldExtractUserId() {
            String token = generateValidToken();
            assertEquals(1L, jwtTokenProvider.getUserIdFromToken(token));
        }

        @Test
        @DisplayName("Should extract different user IDs correctly")
        void shouldExtractDifferentUserIds() {
            TokenInfoDto info = TokenInfoDto.builder()
                    .userId(42L)
                    .email("other@example.com")
                    .role(Role.ADMIN)
                    .build();
            String token = jwtTokenProvider.generateToken(info);
            assertEquals(42L, jwtTokenProvider.getUserIdFromToken(token));
        }
    }

    @Nested
    @DisplayName("getEmailFromToken")
    class GetEmailFromToken {

        @Test
        @DisplayName("Should extract the email from claims")
        void shouldExtractEmail() {
            String token = generateValidToken();
            assertEquals("test@example.com", jwtTokenProvider.getEmailFromToken(token));
        }
    }

    @Nested
    @DisplayName("getTokenInfoFromToken")
    class GetTokenInfoFromTokenDto {

        @Test
        @DisplayName("Should extract all fields from the token")
        void shouldExtractAllFields() {
            TokenInfoDto original = TokenInfoDto.builder()
                    .userId(99L)
                    .email("admin@example.com")
                    .role(Role.ADMIN)
                    .build();
            String token = jwtTokenProvider.generateToken(original);

            TokenInfoDto extracted = jwtTokenProvider.getTokenInfoFromToken(token);

            assertEquals(99L, extracted.userId());
            assertEquals("admin@example.com", extracted.email());
            assertEquals(Role.ADMIN, extracted.role());
        }

        @Test
        @DisplayName("Should round-trip USER role correctly")
        void shouldRoundTripUserRole() {
            String token = generateValidToken();
            TokenInfoDto extracted = jwtTokenProvider.getTokenInfoFromToken(token);
            assertEquals(Role.USER, extracted.role());
        }
    }
}
