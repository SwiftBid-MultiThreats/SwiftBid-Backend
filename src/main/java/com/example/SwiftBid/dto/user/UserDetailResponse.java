package com.example.SwiftBid.dto.user;

import java.time.Instant;
import java.util.Set;

/** Response for {@code GET/PUT /api/user-details/me} (FR-USER-01/02). */
public record UserDetailResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        String address,
        String bio,
        String avatarUrl,
        Set<String> roles,
        Instant createdAt
) {
}
