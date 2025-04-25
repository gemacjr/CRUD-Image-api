package com.swiftbeard.image_upload_service.repository;

import com.swiftbeard.image_upload_service.model.Image;
import com.swiftbeard.image_upload_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ImageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Image testImage1;
    private Image testImage2;

    @BeforeEach
    void setUp() {
        // Create and persist test user
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .build();
        testUser = userRepository.save(testUser);

        // Create and persist test images
        testImage1 = Image.builder()
                .name("Test Image 1")
                .description("First test image")
                .filePath("/path/to/image1.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .user(testUser)
                .build();

        testImage2 = Image.builder()
                .name("Another Image")
                .description("Second test image")
                .filePath("/path/to/image2.jpg")
                .contentType("image/jpeg")
                .size(2048L)
                .user(testUser)
                .build();

        entityManager.persist(testImage1);
        entityManager.persist(testImage2);
        entityManager.flush();
    }

    @Test
    void findByUserId_returnsUserImages() {
        // Act
        List<Image> foundImages = imageRepository.findByUserId(testUser.getId());

        // Assert
        assertEquals(2, foundImages.size());
        assertTrue(foundImages.contains(testImage1));
        assertTrue(foundImages.contains(testImage2));
    }

    @Test
    void findByNameContainingIgnoreCase_withMatchingString_returnsImages() {
        // Act
        List<Image> foundImages = imageRepository.findByNameContainingIgnoreCase("test");

        // Assert
        assertEquals(1, foundImages.size());
        assertEquals(testImage1.getId(), foundImages.get(0).getId());
    }

    @Test
    void findByNameContainingIgnoreCase_withNonMatchingString_returnsEmptyList() {
        // Act
        List<Image> foundImages = imageRepository.findByNameContainingIgnoreCase("nonexistent");

        // Assert
        assertTrue(foundImages.isEmpty());
    }
}