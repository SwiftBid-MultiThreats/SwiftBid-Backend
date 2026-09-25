package com.example.SwiftBid.controller;

import com.example.SwiftBid.dto.account.AccountStatsResponse;
import com.example.SwiftBid.dto.common.MessageResponse;
import com.example.SwiftBid.security.SecurityUtils;
import com.example.SwiftBid.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** FR-AUTH-07/08, FR-USER-04. */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        return ResponseEntity.ok(accountService.getRoles(SecurityUtils.currentUserId()));
    }

    @PostMapping("/become-seller")
    public ResponseEntity<MessageResponse> becomeSeller() {
        accountService.becomeSeller(SecurityUtils.currentUserId());
        return ResponseEntity.ok(new MessageResponse("Nâng cấp tài khoản thành SELLER thành công!"));
    }

    @GetMapping("/stats")
    public ResponseEntity<AccountStatsResponse> getStats() {
        return ResponseEntity.ok(accountService.getStats(SecurityUtils.currentUserId()));
    }
}
