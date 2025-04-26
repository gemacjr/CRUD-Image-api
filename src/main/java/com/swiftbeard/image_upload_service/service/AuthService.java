package com.swiftbeard.image_upload_service.service;

import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    AuthResponse authenticate(AuthRequest request);
    AuthResponse register(AuthRequest request);
}
