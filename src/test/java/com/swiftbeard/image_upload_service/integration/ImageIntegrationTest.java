package com.swiftbeard.image_upload_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.model.Image;
import com.swiftbeard.image_upload_service.model.User;
import com.swiftbeard.image_upload_service.repository.ImageRepository;
import com.swiftbeard.image_upload_service.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        imageRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .username("imageuser")
                .password(passwordEncoder.encode("password"))
                .email("image@example.com")
                .build();

        testUser = userRepository.save(testUser);

        // Log in to get JWT token
        AuthRequest authRequest = AuthRequest.builder()
                .username("imageuser")
                .password("password")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        jwtToken = authResponse.getToken();

        // Create a test image
        Image testImage = Image.builder()
                .name("Test Image")
                .description("Integration test image")
                .filePath("/path/to/test/image.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .user(testUser)
                .build();

        imageRepository.save(testImage);
    }

    @AfterEach
    void tearDown() {
        imageRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void uploadImage_withValidImage_createsAndReturnsImage() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "newimage.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act
        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .param("name", "New Image")
                        .param("description", "Uploaded in integration test")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Image"))
                .andExpect(jsonPath("$.description").value("Uploaded in integration test"));

        // Assert
        List<Image> images = imageRepository.findByUserId(testUser.getId());
        assertEquals(2, images.size()); // Original + new one
        assertTrue(images.stream().anyMatch(img -> img.getName().equals("New Image")));
    }

    // Additional tests would continue here...
}
