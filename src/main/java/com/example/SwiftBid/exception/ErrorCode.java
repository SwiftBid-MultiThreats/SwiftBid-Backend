package com.example.SwiftBid.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {


    // Lỗi liên quan đến Token
    TOKEN_EXPIRED(1001, "Token đã hết hạn"),
    INVALID_TOKEN(1002, "Token không hợp lệ"),
    RESET_TOKEN_NOT_FOUND(1003, "Reset token không tồn tại"),
    RESET_TOKEN_EXPIRED(1004, "Reset token đã hết hạn"),

    // Lỗi Người dùng
    USER_ALREADY_EXISTS(2001, "Username hoặc Email đã tồn tại"),
    USER_NOT_FOUND(2002, "Người dùng không tồn tại"),
    INVALID_PASSWORD(2003, "Mật khẩu cũ không chính xác"),
    INVALID_CREDENTIALS(2004, "Sai username hoặc mật khẩu");

    private final int errorCode;
    private final String errorMessage;

    ErrorCode(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}