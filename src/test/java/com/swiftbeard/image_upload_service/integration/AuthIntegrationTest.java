package com.swiftbeard.image_upload_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.image_upload_service.config.TestConfig;
import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.model.User;
import com.swiftbeard.image_upload_service.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();

        // Create a test user
        testUser = User.builder()
                .username("integrationuser")
                .password(passwordEncoder.encode("password"))
                .email("integration@example.com")
                .build();

        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void register_withNewUser_createsUserAndReturnsToken() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .username("newuser")
                .password("password")
                .build();

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseContent, AuthResponse.class);

        assertNotNull(response.getToken());
        assertEquals("newuser", response.getUsername());

        // Verify user is created in the database
        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    void register_withExistingUsername_returnsBadRequest() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .username("integrationuser") // Existing username
                .password("password")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .username("integrationuser")
                .password("password")
                .build();

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseContent, AuthResponse.class);

        assertNotNull(response.getToken());
        assertEquals("integrationuser", response.getUsername());
        assertEquals(testUser.getId(), response.getUserId());
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .username("integrationuser")
                .password("wrongpassword")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
