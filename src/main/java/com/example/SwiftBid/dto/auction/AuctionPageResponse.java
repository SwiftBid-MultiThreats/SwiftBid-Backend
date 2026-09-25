package com.example.SwiftBid.dto.auction;

import java.util.List;

/** Server-side paginated result for {@code GET /api/auctions/search} (FR-AUC-02). */
public record AuctionPageResponse(
        List<AuctionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
