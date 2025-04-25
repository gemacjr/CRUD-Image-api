package com.swiftbeard.image_upload_service.controller;

import com.swiftbeard.image_upload_service.dto.AuthRequest;
import com.swiftbeard.image_upload_service.dto.AuthResponse;
import com.swiftbeard.image_upload_service.dto.MessageResponse;
import com.swiftbeard.image_upload_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        log.info("Received registration request for user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Received login request for user: {}", request.getUsername());
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/test")
    public ResponseEntity<MessageResponse> testEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Auth service is working", 200));
    }
}
