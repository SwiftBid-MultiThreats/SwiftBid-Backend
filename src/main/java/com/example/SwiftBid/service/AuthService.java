// trong com.example.SwiftBid.service.AuthService.java
package com.example.SwiftBid.service;

import com.example.SwiftBid.payload.authentication.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(ChangePasswordRequest request, String username);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}