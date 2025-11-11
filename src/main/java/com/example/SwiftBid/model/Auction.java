package com.example.SwiftBid.model;

import com.example.SwiftBid.model.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing an auction
 * Uses Optimistic Locking with @Version to handle concurrent updates
 */
@Setter
@Getter
@Entity
@Table(name = "auctions")
public class Auction {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    
    @Column(name = "current_highest_bid_amount", nullable = false)
    private BigDecimal currentHighestBidAmount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_highest_bidder_id")
    private User currentHighestBidder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionStatus status;
    
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
    
    // Constructors
    public Auction() {
        this.createdAt = Instant.now();
        this.version = 0;
    }

}
