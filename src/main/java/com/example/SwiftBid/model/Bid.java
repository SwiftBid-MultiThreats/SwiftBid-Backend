package com.example.SwiftBid.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing a bid placed on an auction
 */
@Setter
@Getter
@Entity
@Table(name = "bids")
public class Bid {

    // Getters and Setters
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
    
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
    
    // Constructors
    public Bid() {
        this.timestamp = Instant.now();
    }


}
