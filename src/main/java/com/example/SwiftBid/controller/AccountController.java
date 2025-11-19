package com.example.SwiftBid.controller;

import com.example.SwiftBid.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/become-seller")
    public ResponseEntity<?> becomeSeller(Authentication authentication) {
        String username = authentication.getName();

        accountService.becomeSeller(username);

        return ResponseEntity.ok(Map.of("message", "Nâng cấp tài khoản thành SELLER thành công!"));
    }

    @GetMapping("/roles")
    public ResponseEntity<Set<String>> getCurrentRoles(Authentication authentication) {
        String username = authentication.getName();

        // Gọi service
        Set<String> roles = accountService.getCurrentUserRoles(username);

        // Trả về mảng JSON: ["USER", "SELLER"]
        return ResponseEntity.ok(roles);
    }

}