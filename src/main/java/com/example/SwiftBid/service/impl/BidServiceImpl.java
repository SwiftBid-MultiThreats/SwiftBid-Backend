package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.dto.bid.AuctionPriceUpdate;
import com.example.SwiftBid.dto.bid.BidResponse;
import com.example.SwiftBid.dto.bid.PlaceBidRequest;
import com.example.SwiftBid.exception.BadRequestException;
import com.example.SwiftBid.exception.ConflictException;
import com.example.SwiftBid.exception.ForbiddenException;
import com.example.SwiftBid.exception.ResourceNotFoundException;
import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.Bid;
import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.enums.AuctionStatus;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.BidRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.BidService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

/**
 * Core concurrency-critical service (FR-BID-02, see {@code plan.md} §4 "Phương án A").
 *
 * <p>Each attempt to place a bid runs in its own, brand-new transaction (via
 * {@link TransactionTemplate} with {@code PROPAGATION_REQUIRES_NEW} — deliberately <b>not</b>
 * a self-invoked {@code @Transactional} method, which Spring AOP would silently ignore). When two
 * requests race for the same {@link Auction}, JPA's optimistic locking (the entity's
 * {@code @Version} column) lets only one commit succeed; the loser's transaction is rolled back
 * entirely (its {@link Bid} row included) and the request is retried against the now-current
 * highest bid, up to {@link #MAX_RETRIES} times.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class BidServiceImpl implements BidService {

    private static final int MAX_RETRIES = 3;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public BidServiceImpl(AuctionRepository auctionRepository,
                           BidRepository bidRepository,
                           UserRepository userRepository,
                           PlatformTransactionManager transactionManager,
                           SimpMessagingTemplate messagingTemplate) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setName("placeBid");
    }

    @Override
    public BidResponse placeBid(Long userId, PlaceBidRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                BidResponse response = transactionTemplate.execute(status -> doPlaceBid(userId, request));
                broadcastUpdate(request.auctionId());
                return response;
            } catch (ObjectOptimisticLockingFailureException conflict) {
                log.debug("Optimistic lock conflict placing bid on auction {} (attempt {}/{})",
                        request.auctionId(), attempt, MAX_RETRIES);
                if (attempt == MAX_RETRIES) {
                    throw new ConflictException("Có nhiều người đang đặt giá cùng lúc, vui lòng thử lại");
                }
            }
        }
        // Unreachable: loop either returns or throws on the last attempt.
        throw new ConflictException("Có nhiều người đang đặt giá cùng lúc, vui lòng thử lại");
    }

    /**
     * Runs inside a single fresh transaction. Any {@link RuntimeException} here rolls that
     * transaction back — including the just-inserted {@link Bid} row when the concurrent
     * {@link Auction} update loses the optimistic-lock race.
     */
    private BidResponse doPlaceBid(Long userId, PlaceBidRequest request) {
        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đấu giá với id: " + request.auctionId()));

        Instant now = Instant.now();
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BadRequestException("Phiên đấu giá hiện không ở trạng thái cho phép đặt giá");
        }
        if (now.isBefore(auction.getStartTime()) || now.isAfter(auction.getEndTime())) {
            throw new BadRequestException("Phiên đấu giá không trong thời gian hoạt động");
        }

        Product product = auction.getProduct();
        if (product != null && product.getSeller() != null && product.getSeller().getId().equals(userId)) {
            throw new ForbiddenException("Người bán không thể tự đặt giá cho sản phẩm của chính mình");
        }

        if (request.bidAmount().compareTo(auction.getCurrentHighestBidAmount()) <= 0) {
            throw new BadRequestException(
                    "Giá đặt phải cao hơn giá hiện tại (" + auction.getCurrentHighestBidAmount() + ")");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(request.bidAmount());
        bid = bidRepository.save(bid);

        auction.setCurrentHighestBidAmount(request.bidAmount());
        auction.setCurrentHighestBidder(user);
        // Flush now (rather than at commit) so a losing concurrent update surfaces here, inside
        // the try/catch in placeBid(), instead of failing later during an unrelated operation.
        auctionRepository.saveAndFlush(auction);

        return BidResponse.from(bid);
    }

    @Override
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByTimestampDesc(auctionId).stream().map(BidResponse::from).toList();
    }

    @Override
    public List<BidResponse> getBidsByUser(Long userId) {
        return bidRepository.findByUserIdOrderByTimestampDesc(userId).stream().map(BidResponse::from).toList();
    }

    private void broadcastUpdate(Long auctionId) {
        try {
            Auction auction = auctionRepository.findById(auctionId).orElse(null);
            if (auction == null) {
                return;
            }
            long bidCount = bidRepository.countByAuctionId(auctionId);
            AuctionPriceUpdate update = new AuctionPriceUpdate(
                    auctionId,
                    auction.getCurrentHighestBidAmount(),
                    auction.getCurrentHighestBidder() != null ? auction.getCurrentHighestBidder().getUsername() : null,
                    bidCount
            );
            messagingTemplate.convertAndSend("/topic/auctions/" + auctionId, update);
        } catch (Exception e) {
            // A broadcast failure must never fail a bid that was already committed successfully.
            log.warn("Không thể broadcast cập nhật giá cho auction {}: {}", auctionId, e.getMessage());
        }
    }
}
