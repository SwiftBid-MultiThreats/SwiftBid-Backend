package com.example.SwiftBid.controller;

import com.example.SwiftBid.dto.home.HomeStatsResponse;
import com.example.SwiftBid.model.enums.AuctionStatus;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-HOME-01: real aggregate numbers to replace the frontend's hard-coded homepage stats. */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<HomeStatsResponse> getStats() {
        long completedAuctions = auctionRepository.countByStatus(AuctionStatus.COMPLETED);
        long totalUsers = userRepository.count();
        var totalTransactionValue = auctionRepository.sumCompletedTransactionValue();
        return ResponseEntity.ok(new HomeStatsResponse(completedAuctions, totalUsers, totalTransactionValue));
    }
}
