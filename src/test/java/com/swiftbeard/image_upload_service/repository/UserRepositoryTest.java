package com.swiftbeard.image_upload_service.repository;

import com.swiftbeard.image_upload_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and persist test user
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .build();

        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void findByUsername_withExistingUsername_returnsUser() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
    }

    @Test
    void findByUsername_withNonExistingUsername_returnsEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void existsByUsername_withExistingUsername_returnsTrue() {
        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUsername_withNonExistingUsername_returnsFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByEmail_withExistingEmail_returnsTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_withNonExistingEmail_returnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }
}