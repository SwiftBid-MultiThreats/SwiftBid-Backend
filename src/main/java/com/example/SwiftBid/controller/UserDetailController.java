package com.example.SwiftBid.controller;

import com.example.SwiftBid.dto.user.AvatarResponse;
import com.example.SwiftBid.dto.user.UpdateUserDetailRequest;
import com.example.SwiftBid.dto.user.UserDetailResponse;
import com.example.SwiftBid.security.SecurityUtils;
import com.example.SwiftBid.service.UserDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** FR-USER-01/02/03. Every endpoint here operates on the authenticated caller ("me"). */
@RestController
@RequestMapping("/api/user-details")
@RequiredArgsConstructor
public class UserDetailController {

    private final UserDetailService userDetailService;

    @GetMapping("/me")
    public ResponseEntity<UserDetailResponse> getMe() {
        return ResponseEntity.ok(userDetailService.getMyDetails(SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDetailResponse> updateMe(@Valid @RequestBody UpdateUserDetailRequest request) {
        return ResponseEntity.ok(userDetailService.updateMyDetails(SecurityUtils.currentUserId(), request));
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<AvatarResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userDetailService.updateAvatar(SecurityUtils.currentUserId(), file);
        return ResponseEntity.ok(new AvatarResponse(avatarUrl));
    }
}
