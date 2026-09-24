package com.example.SwiftBid.dto.auction;

import com.example.SwiftBid.dto.product.ProductResponse;
import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.model.Auction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * List-view DTO for an auction. Carries both the nested {@code product} object and a few
 * flattened convenience fields ({@code productName}, {@code bannerImageUrl}, {@code currentPrice})
 * because different frontend pages read the shape differently.
 */
public record AuctionResponse(
        Long id,
        ProductResponse product,
        String productName,
        Instant startTime,
        Instant endTime,
        BigDecimal currentHighestBidAmount,
        BigDecimal currentPrice,
        UserSummaryResponse currentHighestBidder,
        String status,
        String bannerImageUrl,
        long bidCount,
        Instant createdAt
) {
    public static AuctionResponse from(Auction auction, String bannerImageUrl, long bidCount) {
        ProductResponse product = auction.getProduct() != null ? ProductResponse.from(auction.getProduct()) : null;
        return new AuctionResponse(
                auction.getId(),
                product,
                product != null ? product.name() : null,
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getCurrentHighestBidAmount(),
                auction.getCurrentHighestBidAmount(),
                auction.getCurrentHighestBidder() != null ? UserSummaryResponse.from(auction.getCurrentHighestBidder()) : null,
                auction.getStatus() != null ? auction.getStatus().name() : null,
                bannerImageUrl,
                bidCount,
                auction.getCreatedAt()
        );
    }
}
