package com.example.SwiftBid.dto.bid;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Body for {@code POST /api/bids}. {@code userId} is intentionally absent — always taken from JWT (FR-BID-01). */
public record PlaceBidRequest(
        @NotNull Long auctionId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal bidAmount
) {
}
