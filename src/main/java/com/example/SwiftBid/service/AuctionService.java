package com.example.SwiftBid.service;

import java.util.List;

import com.example.SwiftBid.model.Auction;

public interface AuctionService {
    List<Auction> getAllAuctions();
    Auction getAuctionById(Long id);
    Auction createAuction(Auction auction, Long productId);
    Auction updateAuction(Long id, Auction auctionDetails);
    void deleteAuction(Long id);
}
