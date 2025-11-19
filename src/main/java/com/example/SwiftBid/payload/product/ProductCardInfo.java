package com.example.SwiftBid.payload.product;

import com.example.SwiftBid.model.Product;

public record ProductCardInfo(
        String name,
        String description,
        String imageUrl
) {
    public static ProductCardInfo fromEntity(Product product) {
        if (product == null) {
            return new ProductCardInfo("Sản phẩm không tồn tại", "Không có mô tả", null);
        }
        return new ProductCardInfo(
                product.getName(),
                product.getDescription(),
                product.getImageUrl()
        );
    }
}