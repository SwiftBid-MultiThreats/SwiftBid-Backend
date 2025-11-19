package com.example.SwiftBid.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload file to Cloudinary
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary (e.g., "books", "shops")
     * @return Cloudinary URL of uploaded image
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Generate unique public_id
            String publicId = folder + "/" + UUID.randomUUID().toString();
            
            // Upload options
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "resource_type", "image",
                    "format", "jpg", // Convert to JPG for consistency
                    "quality", "auto:good", // Auto quality optimization
                    "fetch_format", "auto" // Auto format selection
            );

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully to Cloudinary: {}", imageUrl);
            
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    /**
     * Delete file from Cloudinary
     * @param publicId Public ID of the image to delete
     * @return true if deletion was successful
     */
    public boolean deleteFile(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            
            boolean isDeleted = "ok".equals(resultStatus);
            if (isDeleted) {
                log.info("File deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete file from Cloudinary: {}", publicId);
            }
            
            return isDeleted;
            
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     * @param imageUrl Cloudinary URL
     * @return Public ID
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Extract public ID from URL
            // Format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{folder}/{public_id}.{format}
            // or: https://res.cloudinary.com/{cloud_name}/image/upload/{folder}/{public_id}.{format}
            
            String[] parts = imageUrl.split("/");
            
            // Find the "upload" part
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }
            
            if (uploadIndex != -1 && uploadIndex + 1 < parts.length) {
                // Get everything after "upload" and before the file extension
                StringBuilder publicIdBuilder = new StringBuilder();
                
                for (int i = uploadIndex + 1; i < parts.length; i++) {
                    String part = parts[i];
                    
                    // Skip version part (starts with 'v' followed by numbers)
                    if (part.matches("^v\\d+$")) {
                        continue;
                    }
                    
                    if (i > uploadIndex + 1) {
                        publicIdBuilder.append("/");
                    }

                    if (i == parts.length - 1 && part.contains(".")) {
                        part = part.substring(0, part.lastIndexOf('.'));
                    }
                    
                    publicIdBuilder.append(part);
                }
                
                String publicId = publicIdBuilder.toString();
                log.debug("Extracted public ID: {} from URL: {}", publicId, imageUrl);
                return publicId;
            }
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", imageUrl, e);
        }
        
        return null;
    }

    /**
     * Upload image for book
     * @param file MultipartFile to upload
     * @return Cloudinary URL
     */
    public String uploadBookImage(MultipartFile file) {
        return uploadFile(file, "books");
    }

    /**
     * Upload image for shop
     * @param file MultipartFile to upload
     * @return Cloudinary URL
     */
    public String uploadShopImage(MultipartFile file) {
        return uploadFile(file, "shops");
    }

    /**
     * Upload image for category
     * @param file MultipartFile to upload
     * @return Cloudinary URL
     */
    public String uploadCategoryImage(MultipartFile file) {
        return uploadFile(file, "categories");
    }

    /**
     * Upload image with specific folder (generic method for UserDetailsService)
     * @param file MultipartFile to upload
     * @param folder Folder name
     * @return Cloudinary URL
     */
    public String uploadImage(MultipartFile file, String folder) {
        return uploadFile(file, folder);
    }

    /**
     * Delete image by URL (for UserDetailsService)
     * @param imageUrl Cloudinary URL
     * @return true if deletion was successful
     */
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return true; // Nothing to delete
        }
        
        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            log.warn("Cannot extract public ID from URL: {}", imageUrl);
            return false;
        }
        
        return deleteFile(publicId);
    }
}