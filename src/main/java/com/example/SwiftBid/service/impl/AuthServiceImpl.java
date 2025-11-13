// trong com.example.SwiftBid.service.impl.AuthServiceImpl.java
package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.config.JwtUtil;
import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.enums.UserRole;
import com.example.SwiftBid.payload.*;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    // Chúng ta sẽ cần inject MailService sau
    // private final MailService mailService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // 1. Kiểm tra User/Email đã tồn tại chưa
        if (userRepository.findByUsername(request.username()).isPresent() ||
                userRepository.findByEmail(request.email()).isPresent()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 2. Tạo User mới
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER); // Mặc định là USER

        // 3. Lưu vào CSDL
        userRepository.save(user);

        // 4. Tạo Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // 1. Xác thực (Spring Security sẽ kiểm tra pass)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 2. Nếu thành công, lấy thông tin User
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Tạo Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // 2. Cập nhật mật khẩu mới (đã hash)
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Tạo token reset
        String token = UUID.randomUUID().toString();

        // 2. Set token và thời gian hết hạn (ví dụ: 1 giờ)
        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plusSeconds(3600)); // 1 giờ
        userRepository.save(user);

        // 3. Gửi Email (TODO)
        log.info("Gửi email reset pass tới {}. Token: {}", user.getEmail(), token);
        // mailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(() -> new AppException(ErrorCode.RESET_TOKEN_NOT_FOUND));

        // 1. Kiểm tra token hết hạn
        if (user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        // 2. Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        // 3. Xóa token sau khi dùng
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}