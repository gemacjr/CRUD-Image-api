package com.swiftbeard.image_upload_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.image_upload_service.config.TestSecurityConfig;
import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthRequest.AuthRequestBuilder validRequestBuilder;
    private AuthResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequestBuilder = AuthRequest.builder()
            .username("testuser")
            .password("password123");

        mockResponse = AuthResponse.builder()
            .token("mock.jwt.token")
            .username("testuser")
            .userId(1L)
            .build();

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(mockResponse);
        when(authService.register(any(AuthRequest.class))).thenReturn(mockResponse);
    }

    @Test
    void register_withValidCredentials_returnsToken() throws Exception {
        AuthRequest request = validRequestBuilder.build();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(mockResponse.getToken()))
                .andExpect(jsonPath("$.username").value(mockResponse.getUsername()))
                .andExpect(jsonPath("$.userId").value(mockResponse.getUserId()));
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        AuthRequest request = validRequestBuilder.build();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(mockResponse.getToken()))
                .andExpect(jsonPath("$.username").value(mockResponse.getUsername()))
                .andExpect(jsonPath("$.userId").value(mockResponse.getUserId()));
    }
}