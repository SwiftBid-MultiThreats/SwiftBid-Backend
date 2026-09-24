package com.example.SwiftBid;

import com.example.SwiftBid.dto.auth.AuthResponse;
import com.example.SwiftBid.dto.auth.ForgotPasswordRequest;
import com.example.SwiftBid.dto.auth.LoginRequest;
import com.example.SwiftBid.dto.auth.RegisterRequest;
import com.example.SwiftBid.dto.common.MessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Covers TC-AUTH-01/02/04/05/08/11 from docs/tests.md. */
class AuthFlowTest extends AbstractIntegrationTest {

    @Test
    void register_thenLogin_succeeds() {
        AuthResponse registered = register("alice");
        assertThat(registered.token()).isNotBlank();
        assertThat(registered.user().roles()).containsExactly("USER");

        String token = login(registered.user().username(), "password123");
        assertThat(token).isNotBlank();
    }

    @Test
    void register_duplicateUsername_isRejected() {
        AuthResponse first = register("bob");
        RegisterRequest duplicate = new RegisterRequest(first.user().username(), "another_" + uniqueSuffix() + "@test.local", "password123");

        ResponseEntity<Object> response = restTemplate.postForEntity("/api/auth/register", duplicate, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_wrongPassword_isUnauthorized() {
        AuthResponse registered = register("carol");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest(registered.user().username(), "wrong-password"), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void becomeSeller_addsSellerRole_andIsIdempotent() {
        AuthResponse registered = register("dave");
        String sellerToken = registerSeller("dave2");

        ResponseEntity<List> roles = restTemplate.exchange(
                "/api/account/roles", org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(null, authHeaders(sellerToken)), List.class);

        assertThat(roles.getBody()).contains("USER", "SELLER");

        // Calling become-seller again must not fail or duplicate the role (FR-AUTH-08).
        ResponseEntity<MessageResponse> again = restTemplate.exchange(
                "/api/account/become-seller", org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(null, authHeaders(sellerToken)), MessageResponse.class);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void forgotPassword_alwaysReturnsGenericSuccess_regardlessOfEmailExisting() {
        AuthResponse registered = register("erin");

        ResponseEntity<MessageResponse> existing = restTemplate.postForEntity(
                "/api/auth/forgot-password", new ForgotPasswordRequest(registered.user().email()), MessageResponse.class);
        ResponseEntity<MessageResponse> nonExisting = restTemplate.postForEntity(
                "/api/auth/forgot-password", new ForgotPasswordRequest("nobody_" + uniqueSuffix() + "@test.local"), MessageResponse.class);

        assertThat(existing.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(nonExisting.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existing.getBody().message()).isEqualTo(nonExisting.getBody().message());
    }

    @Test
    void protectedEndpoint_withoutToken_isUnauthorized() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/api/user-details/me", Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
