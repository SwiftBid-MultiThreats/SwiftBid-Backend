package com.example.SwiftBid.service;

import java.util.List;

import com.example.SwiftBid.model.Bid;
import com.example.SwiftBid.payload.bid.BidRequest;

public interface BidService {
    List<Bid> getAllBids();
    Bid getBidById(Long id);
    Bid createBid(Bid bid);
    Bid updateBid(Long id, Bid bidDetails);
    void deleteBid(Long id);
    void placeBid(Long auctionId, BidRequest request, String username);

}
