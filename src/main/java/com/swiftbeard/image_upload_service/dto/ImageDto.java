package com.swiftbeard.image_upload_service.dto;


import com.swiftbeard.image_upload_service.model.Image;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private Long id;

    @NotBlank(message = "Image name is required")
    @Size(min = 3, max = 255, message = "Image name must be between 3 and 255 characters")
    private String name;

    private String description;
    private String contentType;
    private Long size;
    private Long userId;
    private String filePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Used for updating image details
    public static ImageDto fromImage(Image image) {
        return ImageDto.builder()
                .id(image.getId())
                .name(image.getName())
                .description(image.getDescription())
                .contentType(image.getContentType())
                .size(image.getSize())
                .userId(image.getUser().getId())
                .filePath(image.getFilePath())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
