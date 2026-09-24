package com.example.SwiftBid;

import com.example.SwiftBid.dto.auction.AuctionResponse;
import com.example.SwiftBid.dto.bid.BidResponse;
import com.example.SwiftBid.dto.bid.PlaceBidRequest;
import com.example.SwiftBid.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Covers TC-BID-01/02/03/05/06 from docs/tests.md — single-request business-rule validation. */
class BidValidationTest extends AbstractIntegrationTest {

    @Autowired
    private AuctionService auctionService;

    private Long createActiveAuction(String sellerToken, String productName, BigDecimal initialPrice) {
        Long productId = createProduct(sellerToken, productName, initialPrice);
        Long auctionId = createAuction(sellerToken, productId,
                Instant.now().minus(1, ChronoUnit.MINUTES).toString(),
                Instant.now().plus(1, ChronoUnit.DAYS).toString());
        auctionService.activatePendingAuctions(); // deterministic PENDING -> ACTIVE (scheduler disabled in tests)
        return auctionId;
    }

    @Test
    void placeBid_higherThanCurrent_succeedsAndUpdatesAuction() {
        String seller = registerSeller("nina");
        String bidder = register("oscar").token();
        Long auctionId = createActiveAuction(seller, "Máy ảnh Sony", new BigDecimal("100000"));

        ResponseEntity<BidResponse> response = restTemplate.postForEntity(
                "/api/bids", new HttpEntity<>(new PlaceBidRequest(auctionId, new BigDecimal("150000")), authHeaders(bidder)),
                BidResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().bidAmount()).isEqualByComparingTo("150000");

        ResponseEntity<AuctionResponse> auction = restTemplate.getForEntity("/api/auctions/{id}", AuctionResponse.class, auctionId);
        assertThat(auction.getBody().currentHighestBidAmount()).isEqualByComparingTo("150000");
    }

    @Test
    void placeBid_notHigherThanCurrent_isRejected() {
        String seller = registerSeller("peter");
        String bidder = register("quinn").token();
        Long auctionId = createActiveAuction(seller, "Bàn gỗ", new BigDecimal("200000"));

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/bids", new HttpEntity<>(new PlaceBidRequest(auctionId, new BigDecimal("200000")), authHeaders(bidder)),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void placeBid_onPendingAuction_isRejected() {
        String seller = registerSeller("rachel");
        String bidder = register("sam").token();
        Long productId = createProduct(seller, "Sản phẩm chưa mở bán", new BigDecimal("90000"));
        Long auctionId = createAuction(seller, productId,
                Instant.now().plus(1, ChronoUnit.DAYS).toString(), Instant.now().plus(2, ChronoUnit.DAYS).toString());
        // No activatePendingAuctions() call: stays PENDING.

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/bids", new HttpEntity<>(new PlaceBidRequest(auctionId, new BigDecimal("95000")), authHeaders(bidder)),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void placeBid_bySellerOnOwnProduct_isForbidden() {
        String seller = registerSeller("tom");
        Long auctionId = createActiveAuction(seller, "Sản phẩm của chính Tom", new BigDecimal("60000"));

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/bids", new HttpEntity<>(new PlaceBidRequest(auctionId, new BigDecimal("70000")), authHeaders(seller)),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void placeBid_userIdIsAlwaysFromJwt_biddingHistoryReflectsCaller() {
        String seller = registerSeller("uma");
        String bidder = register("victor").token();
        Long auctionId = createActiveAuction(seller, "Sản phẩm chống giả mạo userId", new BigDecimal("100000"));

        restTemplate.postForEntity("/api/bids",
                new HttpEntity<>(new PlaceBidRequest(auctionId, new BigDecimal("120000")), authHeaders(bidder)), BidResponse.class);

        ResponseEntity<BidResponse[]> history = restTemplate.getForEntity(
                "/api/bids/auction/{id}", BidResponse[].class, auctionId);
        assertThat(history.getBody()).hasSize(1);
        // The recorded bidder must be "victor" (the token owner), regardless of anything the client could claim.
        assertThat(history.getBody()[0].user().username()).startsWith("victor_");
    }
}
