package com.swiftbeard.image_upload_service.service;

import com.swiftbeard.image_upload_service.dto.ImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    ImageDto uploadImage(MultipartFile file, String name, String description, Long userId);
    ImageDto getImage(Long id);
    List getAllImages();
    List getUserImages(Long userId);
    List searchImages(String name);
    ImageDto updateImage(Long id, ImageDto imageDto, Long userId);
    void deleteImage(Long id, Long userId);
}
