package com.swiftbeard.image_upload_service.service.impl;

import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.model.User;
import com.swiftbeard.image_upload_service.repository.UserRepository;
import com.swiftbeard.image_upload_service.service.AuthService;
import com.swiftbeard.image_upload_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(AuthRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getUsername() + "@example.com") // Default email for simplicity
                .build();

        var savedUser = userRepository.save(user);
        var jwt = jwtService.generateToken(userDetailsService().loadUserByUsername(request.getUsername()));

        return AuthResponse.builder()
                .token(jwt)
                .username(savedUser.getUsername())
                .userId(savedUser.getId())
                .build();
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwt = jwtService.generateToken(userDetailsService().loadUserByUsername(request.getUsername()));

        return AuthResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        java.util.Collections.emptyList()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
