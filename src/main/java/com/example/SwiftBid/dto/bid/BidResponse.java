package com.example.SwiftBid.dto.bid;

import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.Bid;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A few auction-context fields ({@code productName}, {@code auctionStatus},
 * {@code currentHighestBidAmount}, {@code winning}) are flattened onto each bid so pages like
 * "Lịch sử đặt giá của tôi" (My Bids) can render/link without a second round-trip per row.
 */
public record BidResponse(
        Long id,
        Long auctionId,
        UserSummaryResponse user,
        BigDecimal bidAmount,
        Instant timestamp,
        String productName,
        String auctionStatus,
        BigDecimal currentHighestBidAmount,
        boolean winning
) {
    public static BidResponse from(Bid bid) {
        Auction auction = bid.getAuction();
        boolean winning = auction != null
                && auction.getCurrentHighestBidder() != null
                && bid.getUser() != null
                && auction.getCurrentHighestBidder().getId().equals(bid.getUser().getId())
                && auction.getCurrentHighestBidAmount() != null
                && auction.getCurrentHighestBidAmount().compareTo(bid.getBidAmount()) == 0;

        return new BidResponse(
                bid.getId(),
                auction != null ? auction.getId() : null,
                bid.getUser() != null ? UserSummaryResponse.from(bid.getUser()) : null,
                bid.getBidAmount(),
                bid.getTimestamp(),
                auction != null && auction.getProduct() != null ? auction.getProduct().getName() : null,
                auction != null && auction.getStatus() != null ? auction.getStatus().name() : null,
                auction != null ? auction.getCurrentHighestBidAmount() : null,
                winning
        );
    }
}
