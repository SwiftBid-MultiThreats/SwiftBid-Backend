package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.dto.user.UpdateUserDetailRequest;
import com.example.SwiftBid.dto.user.UserDetailResponse;
import com.example.SwiftBid.exception.ConflictException;
import com.example.SwiftBid.exception.ResourceNotFoundException;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.UserDetail;
import com.example.SwiftBid.repository.UserDetailRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.FileStorageService;
import com.example.SwiftBid.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailServiceImpl implements UserDetailService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final FileStorageService fileStorageService;

    @Override
    public UserDetailResponse getMyDetails(Long userId) {
        User user = getUser(userId);
        UserDetail detail = userDetailRepository.findById(userId).orElse(null);
        return toResponse(user, detail);
    }

    @Override
    @Transactional
    public UserDetailResponse updateMyDetails(Long userId, UpdateUserDetailRequest request) {
        User user = getUser(userId);

        if (StringUtils.hasText(request.email()) && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("Email đã được sử dụng");
            }
            user.setEmail(request.email());
            userRepository.save(user);
        }

        UserDetail detail = userDetailRepository.findById(userId).orElseGet(() -> new UserDetail(user));
        detail.setFullName(request.fullName());
        detail.setPhoneNumber(request.phoneNumber());
        detail.setAddress(request.address());
        detail.setBio(request.bio());
        userDetailRepository.save(detail);

        return toResponse(user, detail);
    }

    @Override
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        User user = getUser(userId);
        String avatarUrl = fileStorageService.store(file, "avatars");

        UserDetail detail = userDetailRepository.findById(userId).orElseGet(() -> new UserDetail(user));
        detail.setAvatarUrl(avatarUrl);
        userDetailRepository.save(detail);
        return avatarUrl;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private UserDetailResponse toResponse(User user, UserDetail detail) {
        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                detail != null ? detail.getFullName() : null,
                detail != null ? detail.getPhoneNumber() : null,
                detail != null ? detail.getAddress() : null,
                detail != null ? detail.getBio() : null,
                detail != null ? detail.getAvatarUrl() : null,
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}
