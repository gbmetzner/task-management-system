package com.gbm.taskapi.service;

import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.service.AuthResult;
import com.gbm.taskapi.exception.BadRequestException;
import com.gbm.taskapi.helper.UserMapper;
import com.gbm.taskapi.model.Role;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.UserRepository;
import com.gbm.taskapi.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RefreshTokenHelper refreshTokenHelper;

    @Transactional
    public AuthResult register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        var user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        var savedUser = userRepository.save(user);

        return buildAuthResult(savedUser);
    }

    public AuthResult login(LoginRequest request) {
        var user = userRepository
                .findByEmail(request.email())
                .filter(found -> (passwordEncoder.matches(request.password(), found.getPassword())))
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        return buildAuthResult(user);
    }

    @Transactional
    public AuthResult refreshToken(String token) {
        var newRefreshToken = refreshTokenHelper.rotateRefreshToken(token);
        var user = newRefreshToken.getUser();
        return userMapper.toAuthResult(token, newRefreshToken.getToken(), user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenHelper.revokeRefreshTokensForUser(userId);
    }

    private AuthResult buildAuthResult(User user) {
        var accessToken = generateAccessToken(user);
        var refreshToken = refreshTokenHelper.createRefreshToken(user);
        return userMapper.toAuthResult(accessToken, refreshToken.getToken(), user);
    }

    private String generateAccessToken(User user) {
        var tokenInfo = userMapper.toTokenInfoDto(user);
        return jwtTokenProvider.generateToken(tokenInfo);
    }
}
