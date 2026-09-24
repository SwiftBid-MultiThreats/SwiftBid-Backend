package com.example.SwiftBid.dto.user;

import com.example.SwiftBid.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Safe, public view of a {@link User} — never includes {@code passwordHash} (NFR-02).
 */
public record UserSummaryResponse(Long id, String username, String email, Set<String> roles, Instant createdAt) {

    public static UserSummaryResponse from(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getEmail(), roles, user.getCreatedAt());
    }
}
