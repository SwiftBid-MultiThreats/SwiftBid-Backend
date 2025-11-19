package com.example.SwiftBid.controller;

import com.example.SwiftBid.payload.auction.AuctionCardResponse;
import com.example.SwiftBid.payload.auction.AuctionSummaryResponse;
import com.example.SwiftBid.payload.auction.CreateAuctionRequest; // DTO mới
import com.example.SwiftBid.payload.auction.UpdateAuctionRequest;
import com.example.SwiftBid.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * Lấy tất cả các phiên đấu giá (Public)
     */
    @GetMapping
    public ResponseEntity<List<AuctionSummaryResponse>> getAllAuctions() {
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionSummaryResponse> getAuctionById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    @PostMapping
    public ResponseEntity<AuctionSummaryResponse> createAuction(
            @RequestBody CreateAuctionRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        AuctionSummaryResponse createdAuction = auctionService.createAuction(request, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionSummaryResponse> updateAuction(
            @PathVariable Long id,
            @RequestBody UpdateAuctionRequest request,
            Authentication authentication) {


        // Service sẽ kiểm tra quyền trước khi cập nhật
        AuctionSummaryResponse updatedAuction = auctionService.updateAuction(id, request);

        return ResponseEntity.ok(updatedAuction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuction(
            @PathVariable Long id,
            Authentication authentication) {
        auctionService.deleteAuction(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    public ResponseEntity<List<AuctionSummaryResponse>> getFeaturedAuctions() {
        List<AuctionSummaryResponse> featuredAuctions = auctionService.getFeaturedAuctions();
        return ResponseEntity.ok(featuredAuctions);
    }


}