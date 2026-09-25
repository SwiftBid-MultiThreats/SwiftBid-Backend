package com.example.SwiftBid.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Validates (type/size) and persists an uploaded image under the given sub-folder,
     * returning its publicly reachable URL (served from {@code /uploads/**}, see {@code WebConfig}).
     */
    String store(MultipartFile file, String subFolder);
}
