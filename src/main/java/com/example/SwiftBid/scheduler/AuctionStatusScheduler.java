package com.example.SwiftBid.scheduler;

import com.example.SwiftBid.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * FR-AUC-06: periodically promotes auctions PENDING -> ACTIVE -> COMPLETED based on their
 * start/end time, using the {@code idx_auctions_end_time} index for the completion query.
 * Disabled in tests (see {@code src/test/resources/application.properties}) so integration tests
 * control auction-state transitions explicitly instead of racing a background thread.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auction.status-scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AuctionStatusScheduler {

    private final AuctionService auctionService;

    @Scheduled(fixedRateString = "${auction.status-scheduler.fixed-rate-ms:60000}")
    public void updateAuctionStatuses() {
        int activated = auctionService.activatePendingAuctions();
        int completed = auctionService.completeActiveAuctions();
        if (activated > 0 || completed > 0) {
            log.info("AuctionStatusScheduler: {} auction(s) activated, {} auction(s) completed", activated, completed);
        }
    }
}
