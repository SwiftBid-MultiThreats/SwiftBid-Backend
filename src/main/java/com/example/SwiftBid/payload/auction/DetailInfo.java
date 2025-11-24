package com.example.SwiftBid.payload.auction;

import com.example.SwiftBid.model.AuctionDetail;

public record DetailInfo(
        String auctionDescription,
        String targetAudience,
        String additionalTerms,
        String bannerImageUrl
) {
    public static DetailInfo fromEntity(AuctionDetail detail) {
        if (detail == null) return null;
        return new DetailInfo(
                detail.getAuctionDescription(),
                detail.getTargetAudience(),
                detail.getAdditionalTerms(),
                detail.getBannerImageUrl()
        );
    }
}