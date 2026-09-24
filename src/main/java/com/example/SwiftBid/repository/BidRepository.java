package com.example.SwiftBid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.Bid;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionIdOrderByTimestampDesc(Long auctionId);

    List<Bid> findByUserIdOrderByTimestampDesc(Long userId);

    long countByAuctionId(Long auctionId);

    // FR-USER-04: number of distinct auctions a user has ever placed a bid on.
    @Query("SELECT COUNT(DISTINCT b.auction.id) FROM Bid b WHERE b.user.id = :userId")
    long countDistinctAuctionsByUserId(@Param("userId") Long userId);
}
