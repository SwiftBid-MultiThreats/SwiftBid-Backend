package com.example.SwiftBid.payload.product;

import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public record CreateProductRequest(
        String name,
        String description,
        BigDecimal initialPrice,
        MultipartFile imageFile // Trường này để nhận file ảnh
) {}