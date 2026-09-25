package com.example.SwiftBid.dto.account;

/** FR-USER-04: account stats shown on ProfilePage. */
public record AccountStatsResponse(long auctionsCreated, long auctionsParticipated, long auctionsWon) {
}
