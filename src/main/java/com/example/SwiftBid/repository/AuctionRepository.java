package com.example.SwiftBid.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.enums.AuctionStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {

    List<Auction> findByStatus(AuctionStatus status);

    List<Auction> findByProductSellerId(Long sellerId);

    long countByStatus(AuctionStatus status);

    boolean existsByProductId(Long productId);

    // FR-USER-04: account stats.
    long countByProductSellerId(Long sellerId);

    long countByStatusAndCurrentHighestBidderId(AuctionStatus status, Long userId);

    @Query("SELECT COALESCE(SUM(a.currentHighestBidAmount), 0) FROM Auction a "
            + "WHERE a.status = com.example.SwiftBid.model.enums.AuctionStatus.COMPLETED AND a.currentHighestBidder IS NOT NULL")
    java.math.BigDecimal sumCompletedTransactionValue();

    List<Auction> findByStatusAndStartTimeLessThanEqual(AuctionStatus status, Instant now);

    List<Auction> findByStatusAndEndTimeLessThanEqual(AuctionStatus status, Instant now);

    @Query("SELECT a FROM Auction a LEFT JOIN Bid b ON b.auction = a "
            + "WHERE a.status = com.example.SwiftBid.model.enums.AuctionStatus.ACTIVE "
            + "GROUP BY a ORDER BY COUNT(b) DESC")
    List<Auction> findFeatured(Pageable pageable);
}
