// com.example.SwiftBid.model.Bid.java
package com.example.SwiftBid.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bids")
@Data
@NoArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bid_amount", nullable = false)
    private BigDecimal bidAmount;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public Bid(Auction auction, User user, BigDecimal bidAmount) {
        this.auction = auction;
        this.user = user;
        this.bidAmount = bidAmount;
        this.timestamp = Instant.now();
    }
}