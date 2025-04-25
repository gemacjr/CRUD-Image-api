package com.swiftbeard.image_upload_service.exception;


public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
