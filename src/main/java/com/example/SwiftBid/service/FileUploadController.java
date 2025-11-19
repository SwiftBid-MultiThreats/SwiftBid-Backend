package com.example.SwiftBid.service;

import com.example.SwiftBid.payload.FileUploadResponse;
import com.example.SwiftBid.service.CloudinaryService;
import com.example.SwiftBid.utils.FileValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload/book")
    public ResponseEntity<FileUploadResponse> uploadBookImage(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload book image: {}", file.getOriginalFilename());
        
        // Validate file
        String validationError = FileValidationUtils.getValidationErrorMessage(file);
        if (validationError != null) {
            log.warn("File validation failed: {}", validationError);
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error(validationError));
        }

        try {
            String imageUrl = cloudinaryService.uploadBookImage(file);
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            
            log.info("Book image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(FileUploadResponse.success(imageUrl, publicId));
            
        } catch (Exception e) {
            log.error("Error uploading book image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/shop")
    public ResponseEntity<FileUploadResponse> uploadShopImage(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload shop image: {}", file.getOriginalFilename());
        
        // Validate file
        String validationError = FileValidationUtils.getValidationErrorMessage(file);
        if (validationError != null) {
            log.warn("File validation failed: {}", validationError);
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error(validationError));
        }

        try {
            String imageUrl = cloudinaryService.uploadShopImage(file);
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            
            log.info("Shop image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(FileUploadResponse.success(imageUrl, publicId));
            
        } catch (Exception e) {
            log.error("Error uploading shop image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/category")
    public ResponseEntity<FileUploadResponse> uploadCategoryImage(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload category image: {}", file.getOriginalFilename());
        
        // Validate file
        String validationError = FileValidationUtils.getValidationErrorMessage(file);
        if (validationError != null) {
            log.warn("File validation failed: {}", validationError);
            return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error(validationError));
        }

        try {
            String imageUrl = cloudinaryService.uploadCategoryImage(file);
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            
            log.info("Category image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(FileUploadResponse.success(imageUrl, publicId));
            
        } catch (Exception e) {
            log.error("Error uploading category image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<FileUploadResponse> deleteImage(@PathVariable String publicId) {
        log.info("Received request to delete image: {}", publicId);
        
        try {
            boolean deleted = cloudinaryService.deleteFile(publicId);
            
            if (deleted) {
                log.info("Image deleted successfully: {}", publicId);
                return ResponseEntity.ok(
                        new FileUploadResponse(true, "Image deleted successfully", null, publicId)
                );
            } else {
                log.warn("Failed to delete image: {}", publicId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(FileUploadResponse.error("Image not found or already deleted"));
            }
            
        } catch (Exception e) {
            log.error("Error deleting image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> getUploadConfig() {
        return ResponseEntity.ok(java.util.Map.of(
                "maxFileSize", "10MB",
                "allowedTypes", java.util.Arrays.asList("JPEG", "PNG", "GIF", "WebP"),
                "maxFileSizeBytes", 10 * 1024 * 1024
        ));
    }
}