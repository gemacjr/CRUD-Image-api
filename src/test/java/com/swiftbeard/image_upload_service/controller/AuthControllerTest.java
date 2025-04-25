package com.swiftbeard.image_upload_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        // Set up test auth request and response
        authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .username("testuser")
                .userId(1L)
                .build();
    }

    @Test
    void register_withValidRequest_returnsCreated() throws Exception {
        // Arrange
        when(authService.register(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(authService, times(1)).register(any(AuthRequest.class));
    }

    @Test
    void register_withExistingUsername_returnsBadRequest() throws Exception {
        // Arrange
        when(authService.register(any(AuthRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(any(AuthRequest.class));
    }

    @Test
    void login_withValidCredentials_returnsOk() throws Exception {
        // Arrange
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
        // Arrange
        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    @WithMockUser
    void testEndpoint_withAuthenticatedUser_returnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auth service is working"))
                .andExpect(jsonPath("$.status").value(200));
    }
}