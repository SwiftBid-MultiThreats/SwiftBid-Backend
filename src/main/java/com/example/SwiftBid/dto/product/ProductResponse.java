package com.example.SwiftBid.dto.product;

import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.model.Product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal initialPrice,
        String imageUrl,
        UserSummaryResponse seller,
        Instant createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getInitialPrice(),
                product.getImageUrl(),
                product.getSeller() != null ? UserSummaryResponse.from(product.getSeller()) : null,
                product.getCreatedAt()
        );
    }
}
