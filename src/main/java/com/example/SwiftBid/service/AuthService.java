package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.auth.AuthResponse;
import com.example.SwiftBid.dto.auth.LoginRequest;
import com.example.SwiftBid.dto.auth.RegisterRequest;

public interface AuthService {

    /** FR-AUTH-01 */
    AuthResponse register(RegisterRequest request);

    /** FR-AUTH-02 */
    AuthResponse login(LoginRequest request);

    /** FR-AUTH-04 — always completes without revealing whether the email exists. */
    void forgotPassword(String email);

    /** FR-AUTH-05 */
    void resetPassword(String token, String newPassword);
}
