package com.gbm.taskapi.controller;

import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.response.AuthResponse;
import com.gbm.taskapi.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        AuthResponse sanitizedResponse = new AuthResponse(
                response.token(),
                response.userId(),
                HtmlUtils.htmlEscape(response.email()),
                HtmlUtils.htmlEscape(response.firstName()),
                HtmlUtils.htmlEscape(response.lastName()));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(sanitizedResponse.userId())
                .toUri();

        return ResponseEntity.created(location).body(sanitizedResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        AuthResponse sanitizedResponse = new AuthResponse(
                response.token(),
                response.userId(),
                HtmlUtils.htmlEscape(response.email()),
                HtmlUtils.htmlEscape(response.firstName()),
                HtmlUtils.htmlEscape(response.lastName()));

        return ResponseEntity.ok(sanitizedResponse);
    }
}
