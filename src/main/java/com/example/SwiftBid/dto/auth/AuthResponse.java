package com.example.SwiftBid.dto.auth;

import com.example.SwiftBid.dto.user.UserSummaryResponse;

/** Response for {@code POST /api/auth/register} and {@code POST /api/auth/login}. */
public record AuthResponse(String token, UserSummaryResponse user) {
}
