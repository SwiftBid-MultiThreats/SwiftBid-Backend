package com.example.SwiftBid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.Bid;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
}
