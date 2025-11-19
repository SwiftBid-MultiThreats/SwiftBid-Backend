package com.example.SwiftBid.payload.auction;

import java.time.Instant;

// DTO tạo mới: Bao gồm trường Auction cơ bản và AuctionDetail mở rộng
public record CreateAuctionRequest(
        // --- Trường Auction cơ bản ---
        Long productId,
        Instant startTime,
        Instant endTime,

        // --- Trường AuctionDetail mở rộng ---
        String auctionDescription,
        String targetAudience,
        String additionalTerms,
        String bannerImageUrl // URL ảnh banner đã upload (Tùy chọn)
) {}