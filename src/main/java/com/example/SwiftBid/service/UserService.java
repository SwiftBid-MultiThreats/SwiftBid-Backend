package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.user.UserSummaryResponse;

import java.util.List;

/**
 * Admin-only user management (FR-ADMIN-01, FR-USER-05).
 *
 * <p>User <em>creation</em> intentionally lives only in {@code AuthService.register} (password
 * hashing, default role) and role changes only in {@code AccountService.becomeSeller}, so there is
 * a single, consistent path for both — this service does not expose a raw create/update-everything
 * endpoint, which was the previous (insecure) design.</p>
 */
public interface UserService {

    List<UserSummaryResponse> getAllUsers();

    UserSummaryResponse getUserById(Long id);

    void deleteUser(Long id);
}
