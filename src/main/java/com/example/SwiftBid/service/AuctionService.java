package com.example.SwiftBid.service;

import java.util.List;

import com.example.SwiftBid.payload.auction.AuctionSummaryResponse;
import com.example.SwiftBid.payload.auction.CreateAuctionRequest;
import com.example.SwiftBid.payload.auction.UpdateAuctionRequest;

public interface AuctionService {
    List<AuctionSummaryResponse> getAllAuctions();
    AuctionSummaryResponse getAuctionById(Long id);
    AuctionSummaryResponse updateAuction(Long id, UpdateAuctionRequest auctionDetails);
    void deleteAuction(Long id);

    AuctionSummaryResponse createAuction(CreateAuctionRequest request, String sellerUsername);
    List<AuctionSummaryResponse> getFeaturedAuctions();
}
