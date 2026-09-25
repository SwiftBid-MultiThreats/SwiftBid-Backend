package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.bid.BidResponse;
import com.example.SwiftBid.dto.bid.PlaceBidRequest;

import java.util.List;

public interface BidService {

    /**
     * FR-BID-01/02/06/07 — {@code userId} is always the authenticated caller (never client-supplied).
     * Concurrency-safe: internally retries on optimistic-locking conflicts (see {@code plan.md} §4).
     */
    BidResponse placeBid(Long userId, PlaceBidRequest request);

    /** FR-BID-03 — newest first. */
    List<BidResponse> getBidsByAuction(Long auctionId);

    /** FR-BID-04 — newest first. */
    List<BidResponse> getBidsByUser(Long userId);
}
