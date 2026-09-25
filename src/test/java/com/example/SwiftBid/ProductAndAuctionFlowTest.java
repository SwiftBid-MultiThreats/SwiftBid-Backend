package com.example.SwiftBid;

import com.example.SwiftBid.dto.auction.AuctionDetailResponse;
import com.example.SwiftBid.dto.auth.AuthResponse;
import com.example.SwiftBid.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Covers TC-PROD-01/02/04, TC-AUC-01/02/05 from docs/tests.md. */
class ProductAndAuctionFlowTest extends AbstractIntegrationTest {

    @Autowired
    private AuctionService auctionService;

    @Test
    void createProduct_asPlainUser_isForbidden() {
        AuthResponse plainUser = register("frank");
        HttpHeaders headers = authHeaders(plainUser.token());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        org.springframework.util.MultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
        form.add("name", "Sản phẩm test");
        form.add("initialPrice", "100000");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/products", new HttpEntity<>(form, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createProduct_asSeller_sellerIsTakenFromToken_notFromClient() {
        String sellerToken = registerSeller("grace");
        Long productId = createProduct(sellerToken, "Đồng hồ cổ", new BigDecimal("100000"));
        assertThat(productId).isNotNull();
    }

    @Test
    void myProducts_onlyReturnsCallersOwnProducts() {
        String seller1 = registerSeller("henry");
        String seller2 = registerSeller("irene");
        createProduct(seller1, "Sản phẩm của Henry", new BigDecimal("50000"));
        createProduct(seller2, "Sản phẩm của Irene", new BigDecimal("70000"));

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/products/my-products", HttpMethod.GET, new HttpEntity<>(null, authHeaders(seller1)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        assertThat(data).hasSize(1);
        assertThat(data.get(0).get("name")).isEqualTo("Sản phẩm của Henry");
    }

    @Test
    void createAuction_forSomeoneElsesProduct_isForbidden() {
        String owner = registerSeller("jack");
        String otherSeller = registerSeller("kate");
        Long productId = createProduct(owner, "Sản phẩm của Jack", new BigDecimal("80000"));

        HttpHeaders headers = authHeaders(otherSeller);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        org.springframework.util.MultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
        form.add("productId", productId.toString());
        form.add("startTime", Instant.now().toString());
        form.add("endTime", Instant.now().plus(1, ChronoUnit.DAYS).toString());

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auctions", new HttpEntity<>(form, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createAuction_thenFetchDetails_returnsProductAndZeroBids() {
        String seller = registerSeller("liam");
        Long productId = createProduct(seller, "Bàn phím cơ", new BigDecimal("300000"));
        Long auctionId = createAuction(seller, productId,
                Instant.now().plus(1, ChronoUnit.HOURS).toString(),
                Instant.now().plus(2, ChronoUnit.DAYS).toString());

        ResponseEntity<AuctionDetailResponse> response = restTemplate.getForEntity(
                "/api/auctions/{id}/details", AuctionDetailResponse.class, auctionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().product().name()).isEqualTo("Bàn phím cơ");
        assertThat(response.getBody().status()).isEqualTo("PENDING");
        assertThat(response.getBody().bidCount()).isZero();
    }

    @Test
    void createAuction_secondTimeForSameProduct_isConflict() {
        String seller = registerSeller("mia");
        Long productId = createProduct(seller, "Laptop Dell", new BigDecimal("500000"));
        createAuction(seller, productId, Instant.now().toString(), Instant.now().plus(1, ChronoUnit.DAYS).toString());

        HttpHeaders headers = authHeaders(seller);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        org.springframework.util.MultiValueMap<String, Object> form = new org.springframework.util.LinkedMultiValueMap<>();
        form.add("productId", productId.toString());
        form.add("startTime", Instant.now().toString());
        form.add("endTime", Instant.now().plus(2, ChronoUnit.DAYS).toString());

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auctions", new HttpEntity<>(form, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void myAuctions_onlyReturnsCallersOwnAuctions() {
        String seller1 = registerSeller("nolan");
        String seller2 = registerSeller("olive");
        Long product1 = createProduct(seller1, "Sản phẩm của Nolan", new BigDecimal("60000"));
        Long product2 = createProduct(seller2, "Sản phẩm của Olive", new BigDecimal("70000"));
        createAuction(seller1, product1, Instant.now().toString(), Instant.now().plus(1, ChronoUnit.DAYS).toString());
        createAuction(seller2, product2, Instant.now().toString(), Instant.now().plus(1, ChronoUnit.DAYS).toString());

        ResponseEntity<Map[]> response = restTemplate.exchange(
                "/api/auctions/my-auctions", HttpMethod.GET, new HttpEntity<>(null, authHeaders(seller1)), Map[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void myAuctions_withoutToken_isUnauthorized() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/api/auctions/my-auctions", Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void accountStats_reflectCreatedParticipatedAndWonAuctions() {
        String seller = registerSeller("penny");
        String bidder = register("quincy").token();
        Long productId = createProduct(seller, "Sản phẩm cho account stats", new BigDecimal("100000"));
        Long auctionId = createAuction(seller, productId,
                Instant.now().minus(1, ChronoUnit.MINUTES).toString(), Instant.now().plus(1, ChronoUnit.DAYS).toString());
        auctionService.activatePendingAuctions(); // PENDING -> ACTIVE so the bid below is accepted

        // Seller side: exactly 1 auction created.
        ResponseEntity<Map> sellerStats = restTemplate.exchange(
                "/api/account/stats", HttpMethod.GET, new HttpEntity<>(null, authHeaders(seller)), Map.class);
        assertThat(sellerStats.getBody().get("auctionsCreated")).isEqualTo(1);

        // Bidder side: 0 participated before bidding.
        ResponseEntity<Map> beforeBid = restTemplate.exchange(
                "/api/account/stats", HttpMethod.GET, new HttpEntity<>(null, authHeaders(bidder)), Map.class);
        assertThat(beforeBid.getBody().get("auctionsParticipated")).isEqualTo(0);

        restTemplate.postForEntity("/api/bids", new HttpEntity<>(
                new com.example.SwiftBid.dto.bid.PlaceBidRequest(auctionId, new BigDecimal("150000")), authHeaders(bidder)),
                Object.class);

        ResponseEntity<Map> afterBid = restTemplate.exchange(
                "/api/account/stats", HttpMethod.GET, new HttpEntity<>(null, authHeaders(bidder)), Map.class);
        assertThat(afterBid.getBody().get("auctionsParticipated")).isEqualTo(1);
        assertThat(afterBid.getBody().get("auctionsWon")).isEqualTo(0); // auction still ACTIVE, not COMPLETED yet
    }
}
