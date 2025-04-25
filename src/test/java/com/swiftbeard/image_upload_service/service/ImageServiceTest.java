package com.swiftbeard.image_upload_service.service;

import com.swiftbeard.image_upload_service.dto.ImageDto;
import com.swiftbeard.image_upload_service.exception.ImageNotFoundException;
import com.swiftbeard.image_upload_service.model.Image;
import com.swiftbeard.image_upload_service.model.User;
import com.swiftbeard.image_upload_service.repository.ImageRepository;
import com.swiftbeard.image_upload_service.repository.UserRepository;
import com.swiftbeard.image_upload_service.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageServiceImpl imageService;

    private User testUser;
    private Image testImage;
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = User.builder()
                .id(USER_ID)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        // Set up test image
        testImage = Image.builder()
                .id(1L)
                .name("Test Image")
                .description("Test Description")
                .filePath("/path/to/image.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getImage_withExistingId_returnsImage() {
        // Arrange
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        // Act
        ImageDto result = imageService.getImage(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testImage.getId(), result.getId());
        assertEquals(testImage.getName(), result.getName());
        verify(imageRepository, times(1)).findById(1L);
    }

    @Test
    void getImage_withNonExistingId_throwsException() {
        // Arrange
        when(imageRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.getImage(99L));
        verify(imageRepository, times(1)).findById(99L);
    }

    @Test
    void getAllImages_returnsAllImages() {
        // Arrange
        Image secondImage = Image.builder()
                .id(2L)
                .name("Second Image")
                .filePath("/path/to/second.jpg")
                .contentType("image/jpeg")
                .size(2048L)
                .user(testUser)
                .build();

        when(imageRepository.findAll()).thenReturn(Arrays.asList(testImage, secondImage));

        // Act
        List<ImageDto> results = imageService.getAllImages();

        // Assert
        assertEquals(2, results.size());
        verify(imageRepository, times(1)).findAll();
    }

    @Test
    void getUserImages_withExistingUserId_returnsUserImages() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(imageRepository.findByUserId(USER_ID)).thenReturn(List.of(testImage));

        // Act
        List<ImageDto> results = imageService.getUserImages(USER_ID);

        // Assert
        assertEquals(1, results.size());
        assertEquals(testImage.getName(), results.get(0).getName());
        verify(userRepository, times(1)).findById(USER_ID);
        verify(imageRepository, times(1)).findByUserId(USER_ID);
    }

    // Additional tests would continue here...
}