package com.example.SwiftBid.repository;

import com.example.SwiftBid.model.AuctionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionDetailRepository extends JpaRepository<AuctionDetail, Long> {
    // Không cần thêm phương thức nào
}