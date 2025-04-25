package com.swiftbeard.image_upload_service.exception;


public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) {
        super(message);
    }
}
