package com.gbm.taskapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthE2ETest extends TestContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Full Auth Flow: Register then Login")
    void fullAuthFlow_ShouldWorkSuccessfully() throws Exception {
        // 1. Register
        RegisterRequest registerRequest = new RegisterRequest("e2e@example.com", "password123", "E2E", "Tester");

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("e2e@example.com"))
                .andExpect(jsonPath("$.firstName").value("E2E"))
                .andExpect(jsonPath("$.lastName").value("Tester"))
                .andReturn();

        // Verify DB state
        assertThat(userRepository.existsByEmail("e2e@example.com")).isTrue();

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("e2e@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("e2e@example.com"));
    }

    @Test
    @DisplayName("Should fail registration with duplicate email")
    void register_ShouldFail_WhenEmailExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("duplicate@example.com", "password", "First", "Last");

        // Register once
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Register again with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void login_ShouldFail_WhenPasswordIncorrect() throws Exception {
        // Given
        RegisterRequest register = new RegisterRequest("wrongpass@example.com", "correct-password", "User", "Test");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // When & Then
        LoginRequest login = new LoginRequest("wrongpass@example.com", "wrong-password");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Should escape HTML in response fields")
    void register_ShouldEscapeHtml() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("html@example.com", "password", "<b>John</b>", "Doe");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("&amp;lt;b&amp;gt;John&amp;lt;/b&amp;gt;"));
    }
}
