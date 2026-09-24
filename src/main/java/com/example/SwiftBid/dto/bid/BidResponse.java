package com.example.SwiftBid.dto.bid;

import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.model.Bid;

import java.math.BigDecimal;
import java.time.Instant;

public record BidResponse(Long id, Long auctionId, UserSummaryResponse user, BigDecimal bidAmount, Instant timestamp) {
    public static BidResponse from(Bid bid) {
        return new BidResponse(
                bid.getId(),
                bid.getAuction() != null ? bid.getAuction().getId() : null,
                bid.getUser() != null ? UserSummaryResponse.from(bid.getUser()) : null,
                bid.getBidAmount(),
                bid.getTimestamp()
        );
    }
}
