package com.gbm.taskapi.service;

import com.gbm.taskapi.dto.TokenInfoDto;
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

    @Transactional
    public AuthResult register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String token = generateToken(savedUser);

        return toAuthResult(token, savedUser);
    }

    public AuthResult login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.email())
                .filter(found -> (passwordEncoder.matches(request.password(), found.getPassword())))
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = generateToken(user);

        return toAuthResult(token, user);
    }

    private String generateToken(User user) {
        var tokenInfo = TokenInfoDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        return jwtTokenProvider.generateToken(tokenInfo);
    }

    private AuthResult toAuthResult(String token, User user) {
        return new AuthResult(
                token, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }
}
