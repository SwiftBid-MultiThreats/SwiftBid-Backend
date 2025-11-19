package com.example.SwiftBid.service;

import com.example.SwiftBid.payload.user.UpdateUserDetailRequest;
import com.example.SwiftBid.payload.user.UserDetailResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserDetailService {
    UserDetailResponse getUserDetail(String username);
    UserDetailResponse updateUserDetail(String username, UpdateUserDetailRequest request);
    String updateAvatar(String username, MultipartFile file);
}