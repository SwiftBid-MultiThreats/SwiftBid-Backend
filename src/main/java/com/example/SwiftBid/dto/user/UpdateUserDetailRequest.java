package com.example.SwiftBid.dto.user;

import jakarta.validation.constraints.Email;

/** Body for {@code PUT /api/user-details/me}. {@code username} is intentionally absent — immutable. */
public record UpdateUserDetailRequest(
        String fullName,
        @Email String email,
        String phoneNumber,
        String address,
        String bio
) {
}
