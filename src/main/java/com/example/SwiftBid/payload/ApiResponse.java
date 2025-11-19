package com.example.SwiftBid.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

/**
 * Global API Response Wrapper
 * @param <T> Kiểu dữ liệu của payload (data)
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các trường null (ví dụ: message khi thành công)
public class ApiResponse<T> {
    private final int status;
    private final String message;
    private final Instant timestamp = Instant.now();
    private final T data;

    // Phương thức tĩnh tiện ích cho phản hồi thành công (ví dụ: HTTP 200 OK)
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .data(data)
                .build());
    }

    // Phương thức tĩnh tiện ích cho phản hồi tạo mới (ví dụ: HTTP 201 CREATED)
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<T>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Success")
                        .data(data)
                        .build());
    }
}