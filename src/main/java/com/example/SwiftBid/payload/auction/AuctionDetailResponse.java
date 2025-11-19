package com.example.SwiftBid.payload.auction;

import com.example.SwiftBid.model.AuctionDetail;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuctionDetailResponse {
    private Long auctionId;
    private String auctionDescription;
    private String targetAudience;
    private String additionalTerms;
    private String bannerImageUrl;

    public static AuctionDetailResponse fromEntity(AuctionDetail detail) {
        return AuctionDetailResponse.builder()
                .auctionId(detail.getId())
                .auctionDescription(detail.getAuctionDescription())
                .targetAudience(detail.getTargetAudience())
                .additionalTerms(detail.getAdditionalTerms())
                .bannerImageUrl(detail.getBannerImageUrl())
                .build();
    }
}