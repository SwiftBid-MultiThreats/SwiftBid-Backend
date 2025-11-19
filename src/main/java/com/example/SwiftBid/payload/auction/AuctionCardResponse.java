package com.example.SwiftBid.payload.auction;

import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.payload.product.ProductCardInfo;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionCardResponse(
        Long id,
        String status,
        Instant endTime,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        ProductCardInfo product
) {
    public static AuctionCardResponse fromEntity(Auction auction) {
        BigDecimal startPrice = (auction.getProduct() != null)
                ? auction.getProduct().getInitialPrice()
                : BigDecimal.ZERO;

        return new AuctionCardResponse(
                auction.getId(),
                auction.getStatus().name(),
                auction.getEndTime(),
                startPrice,
                auction.getCurrentHighestBidAmount(),
                ProductCardInfo.fromEntity(auction.getProduct())
        );
    }
}