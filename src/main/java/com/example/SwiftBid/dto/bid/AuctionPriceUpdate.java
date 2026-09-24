package com.example.SwiftBid.dto.bid;

import java.math.BigDecimal;

/** Real-time payload broadcast over {@code /topic/auctions/{auctionId}} (FR-BID-05). */
public record AuctionPriceUpdate(
        Long auctionId,
        BigDecimal currentHighestBidAmount,
        String currentHighestBidderUsername,
        long bidCount
) {
}
