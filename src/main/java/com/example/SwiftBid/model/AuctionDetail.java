package com.example.SwiftBid.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Extended, optional information for an {@link Auction} (1-1 relationship):
 * long-form description, target audience, extra terms and a banner image.
 */
@Setter
@Getter
@Entity
@Table(name = "auction_details")
public class AuctionDetail {

    @Id
    @Column(name = "auction_id")
    private Long auctionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "auction_id")
    private Auction auction;

    // Not @Lob — see the comment on Product.description for why.
    @Column(name = "auction_description", columnDefinition = "TEXT")
    private String auctionDescription;

    @Column(name = "target_audience")
    private String targetAudience;

    @Column(name = "additional_terms", columnDefinition = "TEXT")
    private String additionalTerms;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    public AuctionDetail() {
    }

    public AuctionDetail(Auction auction) {
        this.auction = auction;
        this.auctionId = auction.getId();
    }
}
