package com.example.SwiftBid.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank String name,
        String description,
        String category,
        @NotNull @DecimalMin("0.01") BigDecimal initialPrice
) {
}
