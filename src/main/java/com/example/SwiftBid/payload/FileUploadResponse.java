package com.example.SwiftBid.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private boolean success;
    private String message;
    private String imageUrl;
    private String publicId;
    
    public static FileUploadResponse success(String imageUrl, String publicId) {
        return new FileUploadResponse(true, "File uploaded successfully", imageUrl, publicId);
    }
    
    public static FileUploadResponse error(String message) {
        return new FileUploadResponse(false, message, null, null);
    }
}