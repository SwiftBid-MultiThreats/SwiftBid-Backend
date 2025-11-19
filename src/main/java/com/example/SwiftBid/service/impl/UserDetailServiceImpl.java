package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.UserDetail;
import com.example.SwiftBid.payload.user.UpdateUserDetailRequest;
import com.example.SwiftBid.payload.user.UserDetailResponse;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.CloudinaryService;
import com.example.SwiftBid.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService; // Sử dụng service bạn cung cấp

    // Helper: Tìm User và UserDetail (đảm bảo UserDetail luôn tồn tại)
    private UserDetail findUserAndDetail(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Sử dụng phương thức helper trong User.java để khởi tạo nếu null
        return user.getUserDetail();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(String username) {
        UserDetail userDetail = findUserAndDetail(username);
        return UserDetailResponse.fromEntity(userDetail);
    }

    @Override
    @Transactional
    public UserDetailResponse updateUserDetail(String username, UpdateUserDetailRequest request) {
        UserDetail userDetail = findUserAndDetail(username);

        // Cập nhật các trường text
        userDetail.setFullName(request.fullName());
        userDetail.setPhoneNumber(request.phoneNumber());
        userDetail.setAddress(request.address());

        // Lưu UserDetail (vì UserDetail cascade từ User,
        // chúng ta nên lưu qua user, nhưng vì userDetail đã tồn tại
        // ta có thể lưu trực tiếp (hoặc lưu user)
        // userRepository.save(userDetail.getUser()); // Lưu user mẹ

        return UserDetailResponse.fromEntity(userDetail); // JPA sẽ tự save khi Transaction kết thúc
    }

    @Override
    @Transactional
    public String updateAvatar(String username, MultipartFile file) {
        UserDetail userDetail = findUserAndDetail(username);
        String oldAvatarUrl = userDetail.getAvatarUrl();

        // 1. Tải ảnh mới lên Cloudinary (dùng folder "avatars")
        String newAvatarUrl = cloudinaryService.uploadImage(file, "avatars");

        // 2. Cập nhật CSDL
        userDetail.setAvatarUrl(newAvatarUrl);
        // userRepository.save(userDetail.getUser()); // Lưu user mẹ

        // 3. Xóa ảnh cũ (nếu có)
        // (Chúng ta làm việc này SAU KHI CSDL đã cập nhật thành công)
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            cloudinaryService.deleteImage(oldAvatarUrl);
        }

        return newAvatarUrl;
    }
}