package com.example.SwiftBid.exception;

import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các lỗi nghiệp vụ tùy chỉnh (AppException)
     * URL: Trả về 400 Bad Request
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleAppException(AppException e) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST; // Mặc định là 400

        // Bạn có thể tùy chỉnh status code dựa trên loại lỗi (ví dụ: 404 NOT_FOUND)
        if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (e.getErrorCode() == ErrorCode.FORBIDDEN_ACTION) {
            httpStatus = HttpStatus.FORBIDDEN; // 403
        }

        // Đóng gói lỗi theo cấu trúc chuẩn
        ApiResponse<Map<String, String>> errorResponse = ApiResponse.<Map<String, String>>builder()
                .status(e.getErrorCode().getErrorCode()) // Mã lỗi tùy chỉnh (ví dụ: 2001)
                .message(e.getMessage())
                .data(Map.of("errorType", e.getErrorCode().name()))
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    // Bạn có thể thêm các @ExceptionHandler khác để xử lý lỗi hệ thống 500, v.v.
}