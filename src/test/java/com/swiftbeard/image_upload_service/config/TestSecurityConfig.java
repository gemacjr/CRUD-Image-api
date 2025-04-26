package com.swiftbeard.image_upload_service.config;

import com.swiftbeard.image_upload_service.service.AuthService;
import com.swiftbeard.image_upload_service.service.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public JwtService testJwtService() {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("test.jwt.token");
        when(jwtService.extractUsername(any())).thenReturn("testuser");
        when(jwtService.isTokenValid(any(), any())).thenReturn(true);
        return jwtService;
    }

    @Bean
    @Primary
    public AuthService testAuthService() {
        AuthService authService = mock(AuthService.class);
        UserDetails testUser = new User("testuser", "password123", Collections.emptyList());
        when(authService.loadUserByUsername("testuser")).thenReturn(testUser);
        return authService;
    }

    @Bean
    @Primary
    public AuthenticationProvider testAuthenticationProvider() {
        AuthenticationProvider provider = mock(AuthenticationProvider.class);
        when(provider.authenticate(any(Authentication.class))).thenAnswer(invocation -> {
            Authentication auth = invocation.getArgument(0);
            return new UsernamePasswordAuthenticationToken(
                auth.getPrincipal(),
                auth.getCredentials(),
                Collections.emptyList()
            );
        });
        return provider;
    }

    @Bean
    @Primary
    public AuthenticationManager testAuthenticationManager() {
        return mock(AuthenticationManager.class);
    }
} 