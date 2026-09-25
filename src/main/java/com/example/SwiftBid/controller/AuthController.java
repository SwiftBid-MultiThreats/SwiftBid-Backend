package com.example.SwiftBid.controller;

import com.example.SwiftBid.dto.auth.AuthResponse;
import com.example.SwiftBid.dto.auth.ForgotPasswordRequest;
import com.example.SwiftBid.dto.auth.LoginRequest;
import com.example.SwiftBid.dto.auth.RegisterRequest;
import com.example.SwiftBid.dto.auth.ResetPasswordRequest;
import com.example.SwiftBid.dto.common.MessageResponse;
import com.example.SwiftBid.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-AUTH-01/02/04/05. All endpoints here are public (see {@code SecurityConfig}). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "Nếu email này tồn tại trong hệ thống, bạn sẽ nhận được link đặt lại mật khẩu qua email trong vài phút."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Đặt lại mật khẩu thành công"));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        // Stateless JWT: nothing to invalidate server-side in this MVP; client drops the token.
        return ResponseEntity.ok(new MessageResponse("Đăng xuất thành công"));
    }
}
