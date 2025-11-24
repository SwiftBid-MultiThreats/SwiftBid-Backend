package com.example.SwiftBid.payload.product;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.payload.user.SellerInfo; // Tái sử dụng SellerInfo đã có
import java.math.BigDecimal;

public record ProductInfo(
        Long id,
        String name,
        String description,
        String imageUrl,
        BigDecimal initialPrice, // Frontend cần giá khởi điểm ở đây
        SellerInfo seller
) {
    public static ProductInfo fromEntity(Product product) {
        if (product == null) return null;
        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getInitialPrice(),
                SellerInfo.fromEntity(product.getSeller())
        );
    }
}