package com.example.SwiftBid.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Giữ nguyên SECRET_KEY của bạn
    private final String SECRET_KEY = "5ygyOVnyfL0hlFRfCsi3ViVFvoXsvOvb0WFXwHZGEPdXPWuPtQGoyfpCeVJjWyvm";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaim(token);
        return claimsResolver.apply(claims);
    }

    // THAY ĐỔI 1: Cập nhật phương thức giải mã (parse) token
    private Claims extractAllClaim(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignKey()) // Dùng .verifyWith() thay vì .setSigningKey()
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Phương thức helper để tạo Key an toàn
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // (Giữ nguyên phương thức này, nó được dùng trong JwtRequestFilter)
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // (Bạn có thể giữ lại phương thức này nếu muốn)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public Set<String> extractRoles(String token) {
        // Claims.get() trả về List, chúng ta chuyển sang Set
        List<String> rolesList = extractClaim(token, claims -> claims.get("roles", List.class));
        if (rolesList == null) {
            return Set.of();
        }
        return new HashSet<>(rolesList);
    }

    public String generateToken(String username, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // Đưa Set<String> vào claims
        return createToken(claims, username);
    }

    // THAY ĐỔI 2: Cập nhật phương thức tạo (build) token
    // THAY ĐỔI 3: Xóa try-catch không cần thiết
    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                // Thời gian hết hạn: 1 giờ
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                // Dùng .signWith(key, algorithm)
                .signWith(getSignKey())
                .compact();
    }
}