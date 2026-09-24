package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.exception.BadRequestException;
import com.example.SwiftBid.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5MB, see FR-USER-03/FR-PROD-01/FR-AUC-01
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final Path uploadRoot;
    private final String baseUrl;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir,
                                   @Value("${app.base-url:}") String baseUrl) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Không thể khởi tạo thư mục lưu trữ file: " + uploadRoot, e);
        }
    }

    @Override
    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("Kích thước ảnh không được vượt quá 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh (jpg, png, gif, webp)");
        }

        String extension = extensionFor(contentType);
        String fileName = UUID.randomUUID() + extension;

        try {
            Path folder = uploadRoot.resolve(subFolder).normalize();
            if (!folder.startsWith(uploadRoot)) {
                throw new BadRequestException("Đường dẫn lưu trữ không hợp lệ");
            }
            Files.createDirectories(folder);
            Path target = folder.resolve(fileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu file tải lên", e);
        }

        String publicPath = "/uploads/" + subFolder + "/" + fileName;
        return StringUtils.hasText(baseUrl) ? baseUrl + publicPath : publicPath;
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
