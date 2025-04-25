package com.swiftbeard.image_upload_service.repository;

import com.swiftbeard.image_upload_service.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserId(Long userId);
    List<Image> findByNameContainingIgnoreCase(String name);
}
