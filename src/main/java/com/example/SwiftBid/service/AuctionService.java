package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.auction.AuctionDetailResponse;
import com.example.SwiftBid.dto.auction.AuctionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public interface AuctionService {

    List<AuctionResponse> getAllAuctions();

    AuctionResponse getAuctionById(Long id);

    /** FR-AUC-03 */
    AuctionDetailResponse getAuctionDetails(Long id);

    /** FR-AUC-07 */
    List<AuctionResponse> getActiveAuctions();

    /** FR-AUC-08 */
    List<AuctionResponse> getFeaturedAuctions(int limit);

    /** Seller's own auctions ("My Auctions" page). */
    List<AuctionResponse> getMyAuctions(Long sellerId);

    /** FR-AUC-01 — requester must own {@code productId}, unless admin. */
    AuctionResponse createAuction(Long requesterId, boolean isAdmin, Long productId, Instant startTime, Instant endTime,
                                   String auctionDescription, String targetAudience, String additionalTerms,
                                   MultipartFile bannerImage);

    /** FR-AUC-04 — only allowed while the auction is still PENDING. */
    AuctionResponse updateAuction(Long id, Long requesterId, boolean isAdmin, Instant startTime, Instant endTime);

    /** FR-AUC-05 — soft-cancels (never hard-deletes an auction that may already have bids). */
    void cancelAuction(Long id, Long requesterId, boolean isAdmin);

    /** FR-AUC-06 — used by {@code AuctionStatusScheduler}; returns the number of auctions transitioned. */
    int activatePendingAuctions();

    /** FR-AUC-06 */
    int completeActiveAuctions();
}
