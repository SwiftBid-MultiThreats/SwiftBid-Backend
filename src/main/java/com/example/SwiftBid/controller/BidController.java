package com.example.SwiftBid.controller;

import java.util.List;
import java.util.Map;

import com.example.SwiftBid.payload.bid.BidRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.example.SwiftBid.model.Bid;
import com.example.SwiftBid.service.BidService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @GetMapping
    public ResponseEntity<List<Bid>> getAllBids() {
        return ResponseEntity.ok(bidService.getAllBids());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bid> getBidById(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.getBidById(id));
    }

    @PostMapping
    public ResponseEntity<Bid> createBid(@RequestBody Bid bid) {
        Bid createdBid = bidService.createBid(bid);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBid);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bid> updateBid(@PathVariable Long id, @RequestBody Bid bidDetails) {
        return ResponseEntity.ok(bidService.updateBid(id, bidDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBid(@PathVariable Long id) {
        bidService.deleteBid(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{auctionId}")
    public ResponseEntity<?> placeBid(
            @PathVariable Long auctionId,
            @RequestBody BidRequest request,
            Authentication authentication) {

        try {
            bidService.placeBid(auctionId, request, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Đặt giá thành công!"));

        } catch (ObjectOptimisticLockingFailureException e) {
            // ĐÂY LÀ CHỖ XỬ LÝ CONCURRENCY
            // Nếu lỗi này xảy ra, nghĩa là có người khác đã đặt giá nhanh hơn mili-giây trước đó
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Giá đã thay đổi. Vui lòng cập nhật trang!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
