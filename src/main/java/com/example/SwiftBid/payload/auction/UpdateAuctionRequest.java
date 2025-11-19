package com.example.SwiftBid.payload.auction;

import com.example.SwiftBid.model.enums.AuctionStatus;
import java.time.Instant;

public record UpdateAuctionRequest(
        Instant startTime,
        Instant endTime,
        AuctionStatus status // Ví dụ: Cập nhật thành CANCELLED
) {}