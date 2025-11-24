package com.example.SwiftBid.controller;

import com.example.SwiftBid.payload.auction.AuctionDetailResponse;
import com.example.SwiftBid.payload.auction.UpdateAuctionDetailRequest;
import com.example.SwiftBid.service.AuctionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auctions/{auctionId}/details")
@RequiredArgsConstructor
public class AuctionDetailController {

    private final AuctionDetailService auctionDetailService;

    @GetMapping
    public ResponseEntity<AuctionDetailResponse> getAuctionDetails(
            @PathVariable Long auctionId) {

        AuctionDetailResponse response = auctionDetailService.getAuctionDetail(auctionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<AuctionDetailResponse> updateAuctionDetails(
            @PathVariable Long auctionId,
            @RequestBody UpdateAuctionDetailRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        AuctionDetailResponse response = auctionDetailService.updateAuctionDetail(auctionId, request, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAuctionBanner(
            @PathVariable Long auctionId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String username = authentication.getName();
        String newBannerUrl = auctionDetailService.updateBannerImage(auctionId, file, username);

        return ResponseEntity.ok(Map.of("bannerImageUrl", newBannerUrl));
    }
}