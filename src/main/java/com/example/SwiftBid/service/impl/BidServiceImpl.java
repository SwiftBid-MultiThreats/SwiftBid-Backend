package com.example.SwiftBid.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.enums.AuctionStatus;
import com.example.SwiftBid.payload.auction.AuctionCardResponse;
import com.example.SwiftBid.payload.bid.BidRequest;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.WebSocketService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.example.SwiftBid.model.Bid;
import com.example.SwiftBid.repository.BidRepository;
import com.example.SwiftBid.service.BidService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final WebSocketService webSocketService;

    @Override
    public List<Bid> getAllBids() {
        return bidRepository.findAll();
    }

    @Override
    public Bid getBidById(Long id) {
        return bidRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bid not found with id: " + id));
    }

    @Override
    public Bid createBid(Bid bid) {
        return bidRepository.save(bid);
    }

    @Override
    public Bid updateBid(Long id, Bid bidDetails) {
        Bid bid = getBidById(id);
        
        bid.setAuction(bidDetails.getAuction());
        bid.setUser(bidDetails.getUser());
        bid.setBidAmount(bidDetails.getBidAmount());
        
        return bidRepository.save(bid);
    }

    @Override
    public void deleteBid(Long id) {
        Bid bid = getBidById(id);
        bidRepository.delete(bid);
    }

    @Transactional
    public void placeBid(Long auctionId, BidRequest request, String username) {
        // 1. Tìm User
        User bidder = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Tìm Auction (Cơ chế Optimistic Lock bắt đầu từ lúc đọc này)
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        // 3. Validate Logic (Quan trọng)
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new RuntimeException("Phiên đấu giá chưa bắt đầu hoặc đã kết thúc");
        }
        if (Instant.now().isAfter(auction.getEndTime())) {
            throw new RuntimeException("Phiên đấu giá đã hết giờ");
        }

        // Giá đặt phải cao hơn giá hiện tại
        BigDecimal newAmount = request.amount();
        if (newAmount.compareTo(auction.getCurrentHighestBidAmount()) <= 0) {
            throw new RuntimeException("Giá đặt phải cao hơn giá hiện tại: " + auction.getCurrentHighestBidAmount());
        }

        // 4. Cập nhật Auction (Critical Section)
        // JPA sẽ tự động kiểm tra cột @Version tại đây khi commit transaction
        auction.setCurrentHighestBidAmount(newAmount);
        auction.setCurrentHighestBidder(bidder);

        // 5. Lưu lịch sử Bid
        Bid newBid = new Bid(auction, bidder, newAmount);
        bidRepository.save(newBid);
        auctionRepository.save(auction); // Kích hoạt Optimistic Check

        // 6. Gửi WebSocket (Async)
        // Dữ liệu gửi xuống Client
        AuctionCardResponse updatedData = AuctionCardResponse.fromEntity(auction);
        webSocketService.broadcastNewBid(auction.getId(), updatedData);

        log.info("User {} đặt giá {} thành công cho Auction {}", username, newAmount, auctionId);
    }
}
