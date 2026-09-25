package com.example.SwiftBid.dto.auction;

import com.example.SwiftBid.dto.product.ProductResponse;
import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.AuctionDetail;

import java.math.BigDecimal;
import java.time.Instant;

/** Full detail DTO for {@code GET /api/auctions/{id}/details} (FR-AUC-03). */
public record AuctionDetailResponse(
        Long id,
        ProductResponse product,
        Instant startTime,
        Instant endTime,
        BigDecimal currentHighestBidAmount,
        UserSummaryResponse currentHighestBidder,
        String status,
        AuctionDetailInfo auctionDetail,
        long bidCount,
        Instant createdAt
) {
    public record AuctionDetailInfo(String auctionDescription, String targetAudience, String additionalTerms, String bannerImageUrl) {
    }

    public static AuctionDetailResponse from(Auction auction, AuctionDetail detail, long bidCount) {
        AuctionDetailInfo info = detail != null
                ? new AuctionDetailInfo(detail.getAuctionDescription(), detail.getTargetAudience(),
                    detail.getAdditionalTerms(), detail.getBannerImageUrl())
                : null;
        return new AuctionDetailResponse(
                auction.getId(),
                auction.getProduct() != null ? ProductResponse.from(auction.getProduct()) : null,
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getCurrentHighestBidAmount(),
                auction.getCurrentHighestBidder() != null ? UserSummaryResponse.from(auction.getCurrentHighestBidder()) : null,
                auction.getStatus() != null ? auction.getStatus().name() : null,
                info,
                bidCount,
                auction.getCreatedAt()
        );
    }
}
