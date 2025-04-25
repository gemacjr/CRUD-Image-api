package com.swiftbeard.image_upload_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageService imageService;

    private ImageDto testImageDto;

    @BeforeEach
    void setUp() {
        // Set up test image DTO
        testImageDto = ImageDto.builder()
                .id(1L)
                .name("Test Image")
                .description("Test Description")
                .contentType("image/jpeg")
                .size(1024L)
                .userId(1L)
                .filePath("/path/to/image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "user_1") // Using format "user_[id]" to extract userId
    void uploadImage_withValidImage_returnsCreated() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(imageService.uploadImage(
                any(MultipartFile.class),
                anyString(),
                anyString(),
                anyLong()
        )).thenReturn(testImageDto);

        // Act & Assert
        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .param("name", "Test Image")
                        .param("description", "Test Description")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Image"));

        verify(imageService, times(1)).uploadImage(
                any(MultipartFile.class),
                eq("Test Image"),
                eq("Test Description"),
                eq(1L)
        );
    }

    @Test
    @WithMockUser
    void getImage_withExistingId_returnsImage() throws Exception {
        // Arrange
        when(imageService.getImage(1L)).thenReturn(testImageDto);

        // Act & Assert
        mockMvc.perform(get("/api/images/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Image"));

        verify(imageService, times(1)).getImage(1L);
    }

    @Test
    @WithMockUser
    void getImage_withNonExistingId_returnsNotFound() throws Exception {
        // Arrange
        when(imageService.getImage(99L)).thenThrow(new ImageNotFoundException("Image not found"));

        // Act & Assert
        mockMvc.perform(get("/api/images/99"))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).getImage(99L);
    }

    @Test
    @WithMockUser
    void getAllImages_returnsAllImages() throws Exception {
        // Arrange
        ImageDto secondImageDto = ImageDto.builder()
                .id(2L)
                .name("Second Image")
                .build();

        List<ImageDto> images = Arrays.asList(testImageDto, secondImageDto);
        when(imageService.getAllImages()).thenReturn(images);

        // Act & Assert
        mockMvc.perform(get("/api/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[0].name").value("Test Image"))
                .andExpect(jsonPath("$[1].name").value("Second Image"));

        verify(imageService, times(1)).getAllImages();
    }

    @Test
    @WithMockUser(username = "user_1")
    void getUserImages_returnsUserImages() throws Exception {
        // Arrange
        List<ImageDto> userImages = List.of(testImageDto);
        when(imageService.getUserImages(1L)).thenReturn(userImages);

        // Act & Assert
        mockMvc.perform(get("/api/images/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Image"));

        verify(imageService, times(1)).getUserImages(1L);
    }

    @Test
    @WithMockUser
    void searchImages_returnsMatchingImages() throws Exception {
        // Arrange
        List<ImageDto> matchingImages = List.of(testImageDto);
        when(imageService.searchImages("Test")).thenReturn(matchingImages);

        // Act & Assert
        mockMvc.perform(get("/api/images/search")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Image"));

        verify(imageService, times(1)).searchImages("Test");
    }

    @Test
    @WithMockUser(username = "user_1")
    void updateImage_withOwnerUser_updatesImage() throws Exception {
        // Arrange
        ImageDto updateDto = ImageDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        when(imageService.updateImage(eq(1L), any(ImageDto.class), eq(1L)))
                .thenReturn(testImageDto.toBuilder().name("Updated Name").build());

        // Act & Assert
        mockMvc.perform(put("/api/images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(imageService, times(1)).updateImage(eq(1L), any(ImageDto.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "user_1")
    void updateImage_withNonOwnerUser_returnsForbidden() throws Exception {
        // Arrange
        ImageDto updateDto = ImageDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        when(imageService.updateImage(eq(1L), any(ImageDto.class), eq(1L)))
                .thenThrow(new AccessDeniedException("Not allowed"));

        // Act & Assert
        mockMvc.perform(put("/api/images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(imageService, times(1)).updateImage(eq(1L), any(ImageDto.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "user_1")
    void deleteImage_withOwnerUser_deletesImage() throws Exception {
        // Arrange
        doNothing().when(imageService).deleteImage(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/api/images/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image deleted successfully"))
                .andExpect(jsonPath("$.status").value(200));

        verify(imageService, times(1)).deleteImage(1L, 1L);
    }

    @Test
    @WithMockUser(username = "user_1")
    void deleteImage_withNonOwnerUser_returnsForbidden() throws Exception {
        // Arrange
        doThrow(new AccessDeniedException("Not allowed"))
                .when(imageService).deleteImage(1L, 1L);

        // Act & Assert
        mockMvc.perform(delete("/api/images/1").with(csrf()))
                .andExpect(status().isForbidden());

        verify(imageService, times(1)).deleteImage(1L, 1L);
    }
}