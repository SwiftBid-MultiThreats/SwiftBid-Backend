package com.example.SwiftBid.service;

import java.util.List;

import com.example.SwiftBid.model.Bid;

public interface BidService {
    List<Bid> getAllBids();
    Bid getBidById(Long id);
    Bid createBid(Bid bid);
    Bid updateBid(Long id, Bid bidDetails);
    void deleteBid(Long id);
}
