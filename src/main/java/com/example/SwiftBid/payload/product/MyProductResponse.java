package com.example.SwiftBid.payload.product;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.payload.authentication.SellerInfoResponse;

import java.math.BigDecimal;
import java.time.Instant;

public record MyProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal initialPrice,
        String imageUrl,
        Instant createdAt,
        SellerInfoResponse seller // Sử dụng DTO Seller đã làm sạch
) {
    public static MyProductResponse fromEntity(Product product) {
        return new MyProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getInitialPrice(),
                product.getImageUrl(),
                product.getCreatedAt(),
                SellerInfoResponse.fromUser(product.getSeller())
        );
    }
}