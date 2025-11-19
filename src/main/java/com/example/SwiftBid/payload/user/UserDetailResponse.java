package com.example.SwiftBid.payload.user;

import com.example.SwiftBid.model.UserDetail;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailResponse {
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatarUrl;

    public static UserDetailResponse fromEntity(UserDetail userDetail) {
        return UserDetailResponse.builder()
                .username(userDetail.getUser().getUsername())
                .email(userDetail.getUser().getEmail())
                .fullName(userDetail.getFullName())
                .phoneNumber(userDetail.getPhoneNumber())
                .address(userDetail.getAddress())
                .avatarUrl(userDetail.getAvatarUrl())
                .build();
    }
}