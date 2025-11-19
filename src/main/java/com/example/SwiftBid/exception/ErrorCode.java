package com.example.SwiftBid.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    // Lỗi liên quan đến Token
    TOKEN_EXPIRED(1001, "Token đã hết hạn"),
    INVALID_TOKEN(1002, "Token không hợp lệ"),
    RESET_TOKEN_NOT_FOUND(1003, "Reset token không tồn tại"),
    RESET_TOKEN_EXPIRED(1004, "Reset token đã hết hạn"),
    ROLE_NOT_FOUND(1006, "Vai trò không tồn tại trong hệ thống"),
    USER_ALREADY_HAS_ROLE(2006, "Người dùng đã có vai trò này"),

    // Lỗi Người dùng
    USER_ALREADY_EXISTS(2001, "Username hoặc Email đã tồn tại"),
    USER_NOT_FOUND(2002, "Người dùng không tồn tại"),
    INVALID_PASSWORD(2003, "Mật khẩu cũ không chính xác"),
    INVALID_CREDENTIALS(2004, "Sai username hoặc mật khẩu"),


    AUCTION_NOT_FOUND(3001, "Phiên đấu giá không tồn tại"),
    AUCTION_DETAIL_NOT_FOUND(3002, "Không tìm thấy chi tiết phiên đấu giá"),
    FORBIDDEN_ACTION(1005, "Bạn không có quyền thực hiện hành động này"),
    AUCTION_CANNOT_BE_DELETED(1006, "Phiên đấu giá này không thể cập nhật");

    int errorCode;
    String errorMessage;
}