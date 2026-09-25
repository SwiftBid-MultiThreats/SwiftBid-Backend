package com.example.SwiftBid;

import com.example.SwiftBid.dto.auth.AuthResponse;
import com.example.SwiftBid.dto.auth.LoginRequest;
import com.example.SwiftBid.dto.auth.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Base class for full-stack (real HTTP, real H2 DB) integration tests. Using {@code TestRestTemplate}
 * against a random port exercises the whole request pipeline (JWT filter, controller, service,
 * transaction, repository) exactly as a real client would, which matters most for the
 * concurrency-sensitive bidding tests (see {@code BidConcurrencyTest}).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected AuthResponse register(String usernamePrefix) {
        String suffix = uniqueSuffix();
        RegisterRequest request = new RegisterRequest(
                usernamePrefix + "_" + suffix, usernamePrefix + "_" + suffix + "@test.local", "password123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new IllegalStateException("Register failed: " + response.getStatusCode());
        }
        return response.getBody();
    }

    protected String login(String username, String password) {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(username, password), AuthResponse.class);
        return response.getBody().token();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /** Registers a user and immediately upgrades it to SELLER (FR-AUTH-08), returning its token. */
    protected String registerSeller(String usernamePrefix) {
        AuthResponse auth = register(usernamePrefix);
        restTemplate.exchange("/api/account/become-seller", HttpMethod.POST,
                new HttpEntity<>(null, authHeaders(auth.token())), Void.class);
        // Roles changed server-side; re-login not required since our JWT already carries the
        // *old* role list, so callers relying on becomeSeller must use a fresh token — fetch one.
        return login(auth.user().username(), "password123");
    }

    protected Long createProduct(String sellerToken, String name, BigDecimal initialPrice) {
        HttpHeaders headers = authHeaders(sellerToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("name", name);
        form.add("description", "Mô tả test cho " + name);
        form.add("category", "Test");
        form.add("initialPrice", initialPrice.toPlainString());
        ResponseEntity<java.util.Map> response = restTemplate.postForEntity(
                "/api/products", new HttpEntity<>(form, headers), java.util.Map.class);
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new IllegalStateException("Create product failed: " + response.getStatusCode() + " " + response.getBody());
        }
        return Long.valueOf(response.getBody().get("id").toString());
    }

    protected Long createAuction(String sellerToken, Long productId, String startTimeIso, String endTimeIso) {
        HttpHeaders headers = authHeaders(sellerToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("productId", productId.toString());
        form.add("startTime", startTimeIso);
        form.add("endTime", endTimeIso);
        ResponseEntity<java.util.Map> response = restTemplate.postForEntity(
                "/api/auctions", new HttpEntity<>(form, headers), java.util.Map.class);
        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new IllegalStateException("Create auction failed: " + response.getStatusCode() + " " + response.getBody());
        }
        return Long.valueOf(response.getBody().get("id").toString());
    }
}
