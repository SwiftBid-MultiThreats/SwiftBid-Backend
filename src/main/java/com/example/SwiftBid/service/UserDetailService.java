package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.user.UpdateUserDetailRequest;
import com.example.SwiftBid.dto.user.UserDetailResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserDetailService {

    /** FR-USER-01 */
    UserDetailResponse getMyDetails(Long userId);

    /** FR-USER-02 */
    UserDetailResponse updateMyDetails(Long userId, UpdateUserDetailRequest request);

    /** FR-USER-03 */
    String updateAvatar(Long userId, MultipartFile file);
}
