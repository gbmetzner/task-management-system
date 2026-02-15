package com.gbm.taskapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gbm.taskapi.TaskApiApplication;
import com.gbm.taskapi.config.HtmlEscapingSerializer;
import com.gbm.taskapi.dto.request.LoginRequest;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.service.AuthResult;
import com.gbm.taskapi.helper.UserMapperImpl;
import com.gbm.taskapi.model.Role;
import com.gbm.taskapi.security.JwtAuthenticationFilter;
import com.gbm.taskapi.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({HtmlEscapingSerializer.class, TaskApiApplication.class, UserMapperImpl.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should register a new user successfully")
    void register_ShouldReturnCreated() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "John", "Doe");
        AuthResult result = new AuthResult("token", 1L, "test@example.com", "John", "Doe", Role.USER);

        when(authService.register(any(RegisterRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("Should login successfully")
    void login_ShouldReturnOk() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        AuthResult result = new AuthResult("token", 1L, "test@example.com", "John", "Doe", Role.USER);

        when(authService.login(any(LoginRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 400 when registration request is invalid")
    void register_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("invalid-email", "", "", "");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when login request is invalid")
    void login_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("invalid-email", "");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should escape HTML in response fields")
    void register_ShouldEscapeHtml() throws Exception {
        // Given
        RegisterRequest request =
                new RegisterRequest("test@example.com", "password", "<b>John</b>", "<script>alert('Doe')</script>");
        AuthResult result = new AuthResult(
                "token", 1L, "test@example.com", "<b>John</b>", "<script>alert('Doe')</script>", Role.USER);

        when(authService.register(any(RegisterRequest.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("&lt;b&gt;John&lt;/b&gt;"))
                .andExpect(jsonPath("$.lastName").value("&lt;script&gt;alert(&#39;Doe&#39;)&lt;/script&gt;"));
    }
}
