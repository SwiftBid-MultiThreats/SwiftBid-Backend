package com.example.SwiftBid.repository;

import com.example.SwiftBid.model.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.Auction;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query("SELECT a FROM Auction a " +
            "LEFT JOIN FETCH a.product p " +
            "LEFT JOIN FETCH a.auctionDetail ad")
    List<Auction> findAllAuctionsWithDetails();

    // Bạn cũng nên làm tương tự cho findById
    @Query("SELECT a FROM Auction a " +
            "LEFT JOIN FETCH a.product p " +
            "LEFT JOIN FETCH a.auctionDetail ad " +
            "WHERE a.id = :id")
    Optional<Auction> findByIdWithDetails(Long id);

    @Query("SELECT a FROM Auction a JOIN FETCH a.product p WHERE a.status = :status")
    List<Auction> findAuctionsByStatusWithProduct(AuctionStatus status);

    // (Bạn cũng có thể tạo một phương thức tương tự cho findAll)
    @Query("SELECT a FROM Auction a JOIN FETCH a.product p")
    List<Auction> findAllWithProduct();
}
