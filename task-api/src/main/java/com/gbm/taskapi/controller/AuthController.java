package com.gbm.taskapi.controller;

import static org.springframework.http.ResponseEntity.*;

import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.response.AuthResponse;
import com.gbm.taskapi.helper.UserMapper;
import com.gbm.taskapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        var result = authService.register(request);
        var response = userMapper.toAuthResponse(result);

        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(result.userId())
                .toUri();

        return created(location).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(request);
        var response = userMapper.toAuthResponse(result);

        return ok(response);
    }
}
