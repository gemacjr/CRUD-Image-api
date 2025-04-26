package com.swiftbeard.image_upload_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.image_upload_service.config.TestSecurityConfig;
import com.swiftbeard.image_upload_service.dto.ImageDto;
import com.swiftbeard.image_upload_service.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@Import(TestSecurityConfig.class)
@WithMockUser(username = "user_1", roles = "USER")
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ObjectMapper objectMapper;

    private ImageDto testImageDto;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testImageDto = ImageDto.builder()
                .id(1L)
                .name("test-image.jpg")
                .description("Test image description")
                .userId(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void uploadImage_withValidImage_returnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        when(imageService.uploadImage(any(), eq("test-image"), eq("Test Description"), eq(TEST_USER_ID)))
            .thenReturn(testImageDto);

        mockMvc.perform(multipart("/api/images")
                .file(file)
                .param("name", "test-image")
                .param("description", "Test Description")
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("test-image.jpg"))
            .andExpect(jsonPath("$.userId").value(TEST_USER_ID));
    }

    @Test
    void getImage_withValidId_returnsImage() throws Exception {
        when(imageService.getImage(1L)).thenReturn(testImageDto);

        mockMvc.perform(get("/api/images/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("test-image.jpg"))
            .andExpect(jsonPath("$.userId").value(TEST_USER_ID));
    }

    @Test
    void getAllImages_returnsImageList() throws Exception {
        List<ImageDto> images = Arrays.asList(testImageDto);
        when(imageService.getAllImages()).thenReturn(images);

        mockMvc.perform(get("/api/images"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("test-image.jpg"))
            .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID));
    }

    @Test
    void getUserImages_returnsUserImages() throws Exception {
        List<ImageDto> images = Arrays.asList(testImageDto);
        when(imageService.getUserImages(TEST_USER_ID)).thenReturn(images);

        mockMvc.perform(get("/api/images/user"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("test-image.jpg"))
            .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID));
    }

    @Test
    void searchImages_withValidName_returnsMatchingImages() throws Exception {
        List<ImageDto> images = Arrays.asList(testImageDto);
        when(imageService.searchImages("test")).thenReturn(images);

        mockMvc.perform(get("/api/images/search")
                .param("name", "test"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("test-image.jpg"))
            .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID));
    }

    @Test
    void updateImage_withValidData_returnsUpdatedImage() throws Exception {
        when(imageService.updateImage(eq(1L), any(ImageDto.class), eq(TEST_USER_ID))).thenReturn(testImageDto);

        mockMvc.perform(put("/api/images/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testImageDto))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("test-image.jpg"))
            .andExpect(jsonPath("$.userId").value(TEST_USER_ID));
    }

    @Test
    void deleteImage_withOwnerUser_deletesImage() throws Exception {
        doNothing().when(imageService).deleteImage(eq(1L), eq(TEST_USER_ID));

        mockMvc.perform(delete("/api/images/1")
                .with(csrf()))
            .andExpect(status().isNoContent());
    }
}