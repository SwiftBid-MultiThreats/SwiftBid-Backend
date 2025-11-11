package com.example.SwiftBid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.Auction;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
