package com.gbm.taskapi.service;

import com.gbm.taskapi.dto.TokenInfoDto;
import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.response.AuthResponse;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        // Generate JWT token
        var tokenInfo = TokenInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        String token = jwtTokenProvider.generateToken(tokenInfo);

        return userMapper.toAuth(token, savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository
                .findByEmail(request.email())
                .filter(found -> (passwordEncoder.matches(request.password(), found.getPassword())))
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Generate JWT token
        var tokenInfo = TokenInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        String token = jwtTokenProvider.generateToken(tokenInfo);

        return userMapper.toAuth(token, user);
    }
}
