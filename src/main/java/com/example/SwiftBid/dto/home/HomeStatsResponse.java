package com.example.SwiftBid.dto.home;

import java.math.BigDecimal;

/** FR-HOME-01. */
public record HomeStatsResponse(long completedAuctions, long totalUsers, BigDecimal totalTransactionValue) {
}
