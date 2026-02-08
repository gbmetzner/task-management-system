package com.gbm.taskapi.service;

import com.gbm.taskapi.dto.TokenInfoDto;
import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.response.AuthResponse;
import com.gbm.taskapi.exception.BadRequestException;
import com.gbm.taskapi.exception.ResourceNotFoundException;
import com.gbm.taskapi.model.Role;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.UserRepository;
import com.gbm.taskapi.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        // Generate JWT token
        var tokenInfo = TokenInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();
        String token = jwtTokenProvider.generateToken(tokenInfo);

        return new AuthResponse(
                token, savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName());
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        // Generate JWT token
        var tokenInfo = TokenInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();
        String token = jwtTokenProvider.generateToken(tokenInfo);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }
}
