package com.example.SwiftBid.dto.common;

import java.time.Instant;

/**
 * Generic {status,data,timestamp} envelope. The frontend's {@code productService.getMyProducts()}
 * expects list endpoints wrapped this way (see FR-PROD-02).
 */
public record ApiResponse<T>(int status, T data, Instant timestamp) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, data, Instant.now());
    }
}
