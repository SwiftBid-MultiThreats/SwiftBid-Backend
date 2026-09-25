package com.example.SwiftBid.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.SwiftBid.dto.common.ApiResponse;
import com.example.SwiftBid.dto.product.ProductResponse;
import com.example.SwiftBid.dto.product.UpdateProductRequest;
import com.example.SwiftBid.security.SecurityUtils;
import com.example.SwiftBid.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** FR-PROD-01..07. GET endpoints are public; write endpoints require SELLER/ADMIN + ownership. */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/my-products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyProducts() {
        List<ProductResponse> products = productService.getMyProducts(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.search(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam BigDecimal initialPrice,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        ProductResponse created = productService.createProduct(
                SecurityUtils.currentUserId(), name, description, category, initialPrice, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse updated = productService.updateProduct(
                id, SecurityUtils.currentUserId(), SecurityUtils.hasRole("ADMIN"), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id, SecurityUtils.currentUserId(), SecurityUtils.hasRole("ADMIN"));
        return ResponseEntity.noContent().build();
    }
}
