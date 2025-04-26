package com.swiftbeard.image_upload_service.service.impl;
import com.swiftbeard.image_upload_service.dto.ImageDto;
import com.swiftbeard.image_upload_service.exception.ImageNotFoundException;
import com.swiftbeard.image_upload_service.model.Image;
import com.swiftbeard.image_upload_service.repository.ImageRepository;
import com.swiftbeard.image_upload_service.repository.UserRepository;
import com.swiftbeard.image_upload_service.service.ImageService;
import com.swiftbeard.image_upload_service.util.ImageUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    private final Path fileStorageLocation = Paths.get("uploads/images");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            log.error("Failed to create image storage directory", e);
        }
    }

    @Override
    @Transactional
    public ImageDto uploadImage(MultipartFile file, String name, String description, Long userId) {
        log.info("Uploading image: {} for user: {}", name, userId);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            // Validate and sanitize file name
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new IllegalArgumentException("Invalid file path");
            }

            // Generate unique file name to prevent overwriting
            String fileExtension = ImageUtils.getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + fileExtension;
            Path targetLocation = fileStorageLocation.resolve(uniqueFilename);

            // Save the file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create image entity and save
            Image image = Image.builder()
                    .name(name)
                    .description(description)
                    .filePath(targetLocation.toString())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .user(user)
                    .build();

            Image savedImage = imageRepository.save(image);
            return ImageDto.fromImage(savedImage);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImageDto getImage(Long id) {
        log.info("Fetching image with id: {}", id);

        return imageRepository.findById(id)
                .map(ImageDto::fromImage)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageDto> getAllImages() {
        log.info("Fetching all images");

        return imageRepository.findAll().stream()
                .map(ImageDto::fromImage)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageDto> getUserImages(Long userId) {
        log.info("Fetching images for user id: {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return imageRepository.findByUserId(userId).stream()
                .map(ImageDto::fromImage)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageDto> searchImages(String name) {
        log.info("Searching images by name: {}", name);

        return imageRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ImageDto::fromImage)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ImageDto updateImage(Long id, ImageDto imageDto, Long userId) {
        log.info("Updating image: {} for user: {}", id, userId);

        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + id));

        // Check if the user owns the image
        if (!image.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this image");
        }

        // Update fields
        image.setName(imageDto.getName());
        image.setDescription(imageDto.getDescription());

        Image updatedImage = imageRepository.save(image);
        return ImageDto.fromImage(updatedImage);
    }

    @Override
    @Transactional
    public void deleteImage(Long id, Long userId) {
        log.info("Deleting image: {} for user: {}", id, userId);

        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with id: " + id));

        // Check if the user owns the image
        if (!image.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this image");
        }

        // Delete the actual file
        try {
            Path imagePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            log.error("Failed to delete image file", e);
        }

        // Delete from database
        imageRepository.delete(image);
    }
}
