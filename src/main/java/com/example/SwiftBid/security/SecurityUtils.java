package com.example.SwiftBid.security;

import com.example.SwiftBid.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper to read the currently authenticated user out of the {@link SecurityContextHolder},
 * so controllers/services never trust a client-supplied {@code userId}/{@code sellerId} (FR-BID-01, FR-PROD-01).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Không có người dùng đăng nhập trong phiên hiện tại");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static boolean hasRole(String role) {
        return currentUser().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
