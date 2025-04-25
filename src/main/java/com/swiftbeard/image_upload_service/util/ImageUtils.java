package com.swiftbeard.image_upload_service.util;

import org.springframework.util.StringUtils;

public class ImageUtils {

    private ImageUtils() {
        // Private constructor to prevent instantiation
    }

    public static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }

        String cleanedFilename = StringUtils.cleanPath(filename);
        int dotIndex = cleanedFilename.lastIndexOf('.');

        if (dotIndex == -1) {
            return "";
        }

        return cleanedFilename.substring(dotIndex + 1);
    }

    public static boolean isImageFile(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/");
    }
}
