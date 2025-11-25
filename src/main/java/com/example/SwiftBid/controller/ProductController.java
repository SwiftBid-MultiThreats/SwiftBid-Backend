package com.example.SwiftBid.controller;

import java.math.BigDecimal;
import java.util.List;

import com.example.SwiftBid.payload.ApiResponse;
import com.example.SwiftBid.payload.product.MyProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/my-products")
    public ResponseEntity<ApiResponse<List<MyProductResponse>>> getMyProducts(Authentication authentication) {
        String username = authentication.getName();
        List<MyProductResponse> myProducts = productService.getMyProducts(username);

        // Sử dụng phương thức tĩnh tiện ích
        return ApiResponse.success(myProducts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Bắt buộc dòng này
    public ResponseEntity<MyProductResponse> createProduct(
            // Nhận các trường text qua @RequestParam (hoặc @RequestPart nếu dùng JSON string)
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("initialPrice") BigDecimal initialPrice,

            // Nhận file ảnh
            @RequestParam(value = "image", required = false) MultipartFile image,

            Authentication authentication) {

        String username = authentication.getName();

        // Tạo đối tượng Product từ các param
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setInitialPrice(initialPrice);

        // Gọi Service
        MyProductResponse createdProduct = productService.createProduct(product, image, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return ResponseEntity.ok(productService.updateProduct(id, productDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
