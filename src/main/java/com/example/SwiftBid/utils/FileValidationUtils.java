package com.example.SwiftBid.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidationUtils {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public static boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    public static String getValidationErrorMessage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is required";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size must be less than 10MB";
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return "File must be an image (JPEG, PNG, GIF, WebP)";
        }

        return null; // Valid file
    }

    public static boolean isFileSizeValid(MultipartFile file) {
        return file != null && file.getSize() <= MAX_FILE_SIZE;
    }

    public static boolean isFileTypeAllowed(MultipartFile file) {
        if (file == null) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }
}
