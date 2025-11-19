package com.example.SwiftBid.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auction_details")
@Data
@NoArgsConstructor
@Getter
@Setter
public class AuctionDetail {

    @Id
    @Column(name = "auction_id")
    private Long id;

    @Lob // Dùng @Lob cho kiểu TEXT
    @Column(name = "auction_description", columnDefinition = "TEXT")
    private String auctionDescription;

    @Column(name = "target_audience")
    private String targetAudience;

    @Lob // Dùng @Lob cho kiểu TEXT
    @Column(name = "additional_terms", columnDefinition = "TEXT")
    private String additionalTerms;

    @Column(name = "banner_image_url", length = 512)
    private String bannerImageUrl;

    // ----- Mối quan hệ 1-1 -----
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Báo cho JPA biết rằng PK (id) cũng là FK (auction_id)
    @JoinColumn(name = "auction_id")
    private Auction auction;

}