package com.example.SwiftBid;

import com.example.SwiftBid.dto.auction.AuctionResponse;
import com.example.SwiftBid.dto.bid.BidResponse;
import com.example.SwiftBid.dto.bid.PlaceBidRequest;
import com.example.SwiftBid.service.AuctionService;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-BID-07/08 (docs/tests.md): the core "multi-threaded auction" guarantee — when many clients
 * place bids on the same auction at the same instant, the system must never lose an update.
 * Real concurrency is exercised here (a thread pool firing real HTTP requests against the embedded
 * server, synchronized to start together via a {@link CountDownLatch}), not a single-threaded
 * simulation, so it actually exercises {@code BidServiceImpl}'s optimistic-locking retry loop.
 */
class BidConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private AuctionService auctionService;

    @RepeatedTest(5) // per docs/tests.md DoD: must not be flaky across repeated runs.
    void concurrentBids_neverLoseAnUpdate_finalPriceIsTheMaxOfAllSuccessfulBids() throws InterruptedException {
        String seller = registerSeller("owner");
        Long productId = createProduct(seller, "Sản phẩm đấu giá đông người", new BigDecimal("100000"));
        Long auctionId = createAuction(seller, productId,
                Instant.now().minus(1, ChronoUnit.MINUTES).toString(),
                Instant.now().plus(1, ChronoUnit.DAYS).toString());
        auctionService.activatePendingAuctions();

        int bidderCount = 20;
        List<String> bidderTokens = IntStream.range(0, bidderCount)
                .mapToObj(i -> register("bidder" + i).token())
                .toList();

        // Strictly increasing target amounts so there is always a well-defined "correct" winner,
        // but the ORDER in which threads actually commit is randomized by the JVM scheduler.
        List<BigDecimal> amounts = IntStream.range(0, bidderCount)
                .mapToObj(i -> new BigDecimal("100000").add(BigDecimal.valueOf((i + 1) * 1000L)))
                .toList();

        ExecutorService pool = Executors.newFixedThreadPool(bidderCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<ResponseEntity<BidResponse>>> futures = new ArrayList<>();

        for (int i = 0; i < bidderCount; i++) {
            String token = bidderTokens.get(i);
            BigDecimal amount = amounts.get(i);
            futures.add(pool.submit(() -> {
                startLatch.await();
                return restTemplate.postForEntity("/api/bids",
                        new HttpEntity<>(new PlaceBidRequest(auctionId, amount), authHeaders(token)), BidResponse.class);
            }));
        }

        startLatch.countDown(); // release all threads at (approximately) the same instant
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).as("all bid requests must finish within 30s").isTrue();

        List<ResponseEntity<BidResponse>> results = new ArrayList<>();
        for (Future<ResponseEntity<BidResponse>> f : futures) {
            try {
                results.add(f.get());
            } catch (ExecutionException e) {
                throw new AssertionError("A bid request threw instead of returning a controlled HTTP response", e.getCause());
            }
        }

        // Every request must resolve to one of three *controlled* outcomes — never a 5xx / unhandled
        // exception, and never silently lost:
        //  - 201: this bid won.
        //  - 400: legitimately outbid by a concurrent winner (its amount was no longer the highest
        //    by the time its transaction ran).
        //  - 409: exhausted BidServiceImpl's MAX_RETRIES optimistic-lock retries under heavy
        //    contention on this single row (all 20 threads racing the same auction) — this is the
        //    documented, deliberate fallback in plan.md §4, not a lost update.
        assertThat(results).allSatisfy(r ->
                assertThat(r.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT));

        List<BigDecimal> successfulAmounts = results.stream()
                .filter(r -> r.getStatusCode() == HttpStatus.CREATED)
                .map(r -> r.getBody().bidAmount())
                .toList();
        assertThat(successfulAmounts).as("at least one bid must win the race").isNotEmpty();

        BigDecimal expectedMax = successfulAmounts.stream().max(BigDecimal::compareTo).orElseThrow();

        ResponseEntity<AuctionResponse> auctionAfter = restTemplate.getForEntity(
                "/api/auctions/{id}", AuctionResponse.class, auctionId);
        assertThat(auctionAfter.getBody().currentHighestBidAmount())
                .as("no lost update: auction's highest bid must equal the max of all bids the server actually accepted")
                .isEqualByComparingTo(expectedMax);

        ResponseEntity<BidResponse[]> history = restTemplate.getForEntity(
                "/api/bids/auction/{id}", BidResponse[].class, auctionId);
        assertThat(history.getBody())
                .as("exactly one bid row per successful request: no double-write, no silently dropped write")
                .hasSize(successfulAmounts.size());
    }

    @RepeatedTest(5)
    void twoIdenticalConcurrentBids_onlyOneWins_theOtherIsRejectedAsNoLongerHighEnough() throws InterruptedException {
        String seller = registerSeller("owner2");
        Long productId = createProduct(seller, "Sản phẩm 2 người đặt cùng giá", new BigDecimal("100000"));
        Long auctionId = createAuction(seller, productId,
                Instant.now().minus(1, ChronoUnit.MINUTES).toString(),
                Instant.now().plus(1, ChronoUnit.DAYS).toString());
        auctionService.activatePendingAuctions();

        String tokenA = register("racerA").token();
        String tokenB = register("racerB").token();
        BigDecimal sameAmount = new BigDecimal("150000");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<ResponseEntity<BidResponse>> task1 = () -> {
            startLatch.await();
            return restTemplate.postForEntity("/api/bids",
                    new HttpEntity<>(new PlaceBidRequest(auctionId, sameAmount), authHeaders(tokenA)), BidResponse.class);
        };
        Callable<ResponseEntity<BidResponse>> task2 = () -> {
            startLatch.await();
            return restTemplate.postForEntity("/api/bids",
                    new HttpEntity<>(new PlaceBidRequest(auctionId, sameAmount), authHeaders(tokenB)), BidResponse.class);
        };

        Future<ResponseEntity<BidResponse>> f1 = pool.submit(task1);
        Future<ResponseEntity<BidResponse>> f2 = pool.submit(task2);
        startLatch.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(15, TimeUnit.SECONDS)).isTrue();

        List<HttpStatus> statuses;
        try {
            statuses = List.of((HttpStatus) f1.get().getStatusCode(), (HttpStatus) f2.get().getStatusCode());
        } catch (ExecutionException e) {
            throw new AssertionError(e.getCause());
        }

        long created = statuses.stream().filter(s -> s == HttpStatus.CREATED).count();
        long rejected = statuses.stream().filter(s -> s == HttpStatus.BAD_REQUEST).count();
        assertThat(created).as("exactly one of two identical concurrent bids must win").isEqualTo(1);
        assertThat(rejected).isEqualTo(1);

        ResponseEntity<AuctionResponse> auctionAfter = restTemplate.getForEntity(
                "/api/auctions/{id}", AuctionResponse.class, auctionId);
        assertThat(auctionAfter.getBody().currentHighestBidAmount()).isEqualByComparingTo(sameAmount);
    }
}
