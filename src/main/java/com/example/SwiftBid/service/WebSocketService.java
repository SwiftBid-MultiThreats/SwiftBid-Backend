package com.example.SwiftBid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo cập nhật giá tới tất cả người xem phiên đấu giá này.
     * Chạy trên một luồng riêng biệt (Multithreading).
     */
    @Async("taskExecutor")
    public void broadcastNewBid(Long auctionId, Object bidData) {
        log.info("Đang broadcast giá mới cho Auction ID: {} trên luồng: {}", auctionId, Thread.currentThread().getName());

        // Gửi tin nhắn vào topic: /topic/auctions/{id}
        messagingTemplate.convertAndSend("/topic/auctions/" + auctionId, bidData);
    }
}