package com.example.SwiftBid.controller;

import com.example.SwiftBid.payload.user.UpdateUserDetailRequest;
import com.example.SwiftBid.payload.user.UserDetailResponse;
import com.example.SwiftBid.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/user-details")
@RequiredArgsConstructor
public class UserDetailController {

    private final UserDetailService userDetailService;

    // Helper lấy username của người đang đăng nhập
    private String getUsername(Authentication authentication) {
        return authentication.getName();
    }

    /**
     * Lấy thông tin chi tiết của người dùng đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<UserDetailResponse> getMyDetails(Authentication authentication) {
        String username = getUsername(authentication);
        UserDetailResponse response = userDetailService.getUserDetail(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật thông tin text (Fullname, Phone, Address)
     */
    @PutMapping("/me")
    public ResponseEntity<UserDetailResponse> updateMyDetails(
            Authentication authentication,
            @RequestBody UpdateUserDetailRequest request) {

        String username = getUsername(authentication);
        UserDetailResponse response = userDetailService.updateUserDetail(username, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật ảnh đại diện
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMyAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String username = getUsername(authentication);
        String newAvatarUrl = userDetailService.updateAvatar(username, file);

        return ResponseEntity.ok(Map.of("avatarUrl", newAvatarUrl));
    }
}