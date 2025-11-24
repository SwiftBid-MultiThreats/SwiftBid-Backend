package com.example.SwiftBid.payload.user;

import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.UserDetail;

public record SellerInfo(
        Long id,
        String username
) {
    public static SellerInfo fromEntity(User user) {
        if (user == null) return null;

        return new SellerInfo(user.getId(), user.getUsername());
    }
}