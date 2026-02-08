package com.gbm.taskapi.security;

import static org.junit.jupiter.api.Assertions.*;

import com.gbm.taskapi.dto.TokenInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret =
            "bXlTZWNyZXRLZXlGb3JKV1RUb2tlbkdlbmVyYXRpb25UaGF0SXNBdExlYXN0MjU2Qml0c0xvbmVGb3JIUzI1NkFsZ29yaXRobQ==";
    private final long expiration = 3600000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    void testGenerateToken() {
        TokenInfoDto dto =
                TokenInfoDto.builder().userId(1L).email("test@example.com").build();

        String token = jwtTokenProvider.generateToken(dto);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}
