package com.swiftbeard.image_upload_service.service;


import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
    AuthResponse register(AuthRequest request);
    UserDetailsService userDetailsService();
}
