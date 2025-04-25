package com.swiftbeard.image_upload_service.service;

import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.model.User;
import com.swiftbeard.image_upload_service.repository.UserRepository;
import com.swiftbeard.image_upload_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        // Set up auth request
        authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password")
                .build();
    }

    @Test
    void register_withNewUser_returnsAuthResponse() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getId(), response.getUserId());

        verify(userRepository, times(1)).existsByUsername(authRequest.getUsername());
        verify(passwordEncoder, times(1)).encode(authRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void register_withExistingUsername_throwsException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(authRequest));
        verify(userRepository, times(1)).existsByUsername(authRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_withValidCredentials_returnsAuthResponse() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getId(), response.getUserId());

        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );
        verify(userRepository, times(1)).findByUsername(authRequest.getUsername());
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void userDetailsService_withExistingUsername_returnsUserDetails() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = authService.userDetailsService().loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}