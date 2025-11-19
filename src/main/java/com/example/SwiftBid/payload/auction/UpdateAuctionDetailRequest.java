package com.example.SwiftBid.payload.auction;

// Dùng record cho các trường text
public record UpdateAuctionDetailRequest(
        String auctionDescription,
        String targetAudience,
        String additionalTerms
) {}