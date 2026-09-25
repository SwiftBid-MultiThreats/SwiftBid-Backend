package com.example.SwiftBid.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.SwiftBid.dto.auction.AuctionDetailResponse;
import com.example.SwiftBid.dto.auction.AuctionResponse;
import com.example.SwiftBid.security.SecurityUtils;
import com.example.SwiftBid.service.AuctionService;

import lombok.RequiredArgsConstructor;

/** FR-AUC-01..08. GET endpoints are public; write endpoints require SELLER/ADMIN + ownership. */
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AuctionResponse>> getActiveAuctions() {
        return ResponseEntity.ok(auctionService.getActiveAuctions());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<AuctionResponse>> getFeaturedAuctions(
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(auctionService.getFeaturedAuctions(limit));
    }

    @GetMapping("/my-auctions")
    public ResponseEntity<List<AuctionResponse>> getMyAuctions() {
        return ResponseEntity.ok(auctionService.getMyAuctions(SecurityUtils.currentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<AuctionDetailResponse> getAuctionDetails(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionDetails(id));
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<AuctionResponse> createAuction(
            @RequestParam Long productId,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime,
            @RequestParam(required = false) String auctionDescription,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String additionalTerms,
            @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage) {
        AuctionResponse created = auctionService.createAuction(
                SecurityUtils.currentUserId(), SecurityUtils.hasRole("ADMIN"), productId, startTime, endTime,
                auctionDescription, targetAudience, additionalTerms, bannerImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponse> updateAuction(@PathVariable Long id,
                                                           @RequestParam Instant startTime,
                                                           @RequestParam Instant endTime) {
        AuctionResponse updated = auctionService.updateAuction(
                id, SecurityUtils.currentUserId(), SecurityUtils.hasRole("ADMIN"), startTime, endTime);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAuction(@PathVariable Long id) {
        auctionService.cancelAuction(id, SecurityUtils.currentUserId(), SecurityUtils.hasRole("ADMIN"));
        return ResponseEntity.noContent().build();
    }
}
