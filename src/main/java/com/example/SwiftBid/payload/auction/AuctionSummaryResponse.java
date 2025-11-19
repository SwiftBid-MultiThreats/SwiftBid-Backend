package com.example.SwiftBid.payload.auction;

import com.example.SwiftBid.model.Auction;
import java.math.BigDecimal;
import java.time.Instant;

// Dùng record cho DTO
public record AuctionSummaryResponse(
        Long id,
        String productName,
        String bannerImageUrl,
        BigDecimal startingPrice,
        BigDecimal currentHighestBidAmount,
        Instant endTime,
        String status
) {
    // Phương thức chuyển đổi (Mapper)
    public static AuctionSummaryResponse fromEntity(Auction auction) {
        // Lấy thông tin an toàn (vì chúng ta sẽ JOIN FETCH)
        String productName = auction.getProduct() != null ? auction.getProduct().getName() : null;
        String bannerUrl = auction.getAuctionDetail() != null ? auction.getAuctionDetail().getBannerImageUrl() : null;

        // Lấy giá khởi điểm từ Product
        BigDecimal startingPrice = auction.getProduct() != null
                ? auction.getProduct().getInitialPrice()
                : BigDecimal.ZERO;


        return new AuctionSummaryResponse(
                auction.getId(),
                productName,
                bannerUrl,
                startingPrice, // <--- THÊM VÀO ĐÂY
                auction.getCurrentHighestBidAmount(),
                auction.getEndTime(),
                auction.getStatus().name()
        );
    }
}