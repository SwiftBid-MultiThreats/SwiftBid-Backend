package com.example.SwiftBid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.Bid;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionIdOrderByTimestampDesc(Long auctionId);

    List<Bid> findByUserIdOrderByTimestampDesc(Long userId);

    long countByAuctionId(Long auctionId);
}
