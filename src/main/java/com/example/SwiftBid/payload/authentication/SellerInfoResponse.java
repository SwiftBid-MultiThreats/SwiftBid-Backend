package com.example.SwiftBid.payload.authentication;

import com.example.SwiftBid.model.User;

public record SellerInfoResponse(
        Long id,
        String username,
        String avatarUrl // Lấy từ UserDetail
) {
    public static SellerInfoResponse fromUser(User user) {
        return new SellerInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getUserDetail() != null ? user.getUserDetail().getAvatarUrl() : null
        );
    }
}