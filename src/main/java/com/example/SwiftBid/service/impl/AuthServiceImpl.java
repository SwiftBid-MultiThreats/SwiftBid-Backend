package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.dto.auth.AuthResponse;
import com.example.SwiftBid.dto.auth.LoginRequest;
import com.example.SwiftBid.dto.auth.RegisterRequest;
import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.exception.BadRequestException;
import com.example.SwiftBid.exception.ConflictException;
import com.example.SwiftBid.exception.UnauthorizedException;
import com.example.SwiftBid.model.PasswordResetToken;
import com.example.SwiftBid.model.Role;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.enums.RoleName;
import com.example.SwiftBid.repository.PasswordResetTokenRepository;
import com.example.SwiftBid.repository.RoleRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.security.JwtService;
import com.example.SwiftBid.service.AuthService;
import com.example.SwiftBid.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email đã được sử dụng");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.USER)));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername(), roleNames(user));
        return new AuthResponse(token, UserSummaryResponse.from(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), roleNames(user));
        return new AuthResponse(token, UserSummaryResponse.from(user));
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        // Always behave the same way whether or not the email exists (chống dò email, FR-AUTH-04).
        userRepository.findByEmail(email).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password/" + resetToken.getToken();
            mailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token không hợp lệ hoặc đã hết hạn"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new BadRequestException("Token không hợp lệ hoặc đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(r -> r.getName().name()).toList();
    }
}
