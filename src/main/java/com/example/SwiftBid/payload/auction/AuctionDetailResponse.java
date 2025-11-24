package com.example.SwiftBid.payload.auction;

import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.payload.product.ProductInfo; // Cần tạo file này
import com.example.SwiftBid.payload.auction.DetailInfo; // Cần tạo file này

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionDetailResponse(
        Long id,
        String status,
        Instant startTime,
        Instant endTime,
        BigDecimal currentHighestBidAmount, // Khớp với frontend
        ProductInfo product,                // Nested object
        DetailInfo auctionDetail            // Nested object (đổi tên từ 'details' thành 'auctionDetail' cho khớp frontend)
) {
    public static AuctionDetailResponse fromEntity(Auction auction) {
        return new AuctionDetailResponse(
                auction.getId(),
                auction.getStatus().name(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getCurrentHighestBidAmount(),
                ProductInfo.fromEntity(auction.getProduct()),
                DetailInfo.fromEntity(auction.getAuctionDetail())
        );
    }
}