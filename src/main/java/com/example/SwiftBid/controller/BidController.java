package com.example.SwiftBid.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SwiftBid.dto.bid.BidResponse;
import com.example.SwiftBid.dto.bid.PlaceBidRequest;
import com.example.SwiftBid.security.SecurityUtils;
import com.example.SwiftBid.service.BidService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** FR-BID-01..07. Placing a bid requires auth; reading auction bid history is public. */
@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @PostMapping
    public ResponseEntity<BidResponse> placeBid(@Valid @RequestBody PlaceBidRequest request) {
        BidResponse created = bidService.placeBid(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getBidsByAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getBidsByAuction(auctionId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BidResponse>> getBidsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bidService.getBidsByUser(userId));
    }
}
