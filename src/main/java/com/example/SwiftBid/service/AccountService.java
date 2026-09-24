package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.account.AccountStatsResponse;

import java.util.List;

public interface AccountService {

    /** FR-AUTH-07 */
    List<String> getRoles(Long userId);

    /** FR-AUTH-08 — idempotent: calling it again when already a SELLER is a no-op. */
    void becomeSeller(Long userId);

    /** FR-USER-04 */
    AccountStatsResponse getStats(Long userId);
}
