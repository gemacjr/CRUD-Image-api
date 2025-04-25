package com.swiftbeard.image_upload_service.controller;

import com.swiftbeard.image_upload_service.dto.ImageDto;
import com.swiftbeard.image_upload_service.dto.MessageResponse;
import com.swiftbeard.image_upload_service.service.ImageService;
import com.swiftbeard.image_upload_service.util.ImageUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!ImageUtils.isImageFile(file.getContentType())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        Long userId = getUserIdFromAuthentication(authentication);
        ImageDto uploadedImage = imageService.uploadImage(file, name, description, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageDto> getImage(@PathVariable Long id) {
        return ResponseEntity.ok(imageService.getImage(id));
    }

    @GetMapping
    public ResponseEntity<List<ImageDto>> getAllImages() {
        return ResponseEntity.ok(imageService.getAllImages());
    }

    @GetMapping("/user")
    public ResponseEntity<List<ImageDto>> getUserImages(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(imageService.getUserImages(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ImageDto>> searchImages(@RequestParam String name) {
        return ResponseEntity.ok(imageService.searchImages(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageDto> updateImage(
            @PathVariable Long id,
            @Valid @RequestBody ImageDto imageDto,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        ImageDto updatedImage = imageService.updateImage(id, imageDto, userId);

        return ResponseEntity.ok(updatedImage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteImage(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        imageService.deleteImage(id, userId);

        return ResponseEntity.ok(new MessageResponse("Image deleted successfully", 200));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return Long.valueOf(userDetails.getUsername().split("_")[1]); // Assuming format "user_123"
    }
}
