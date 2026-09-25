package com.example.SwiftBid.service;

import com.example.SwiftBid.dto.product.ProductResponse;
import com.example.SwiftBid.dto.product.UpdateProductRequest;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    /** FR-PROD-02 — only the caller's own products. */
    List<ProductResponse> getMyProducts(Long sellerId);

    /** FR-PROD-06 */
    List<ProductResponse> search(String query);

    /** FR-PROD-01 — seller is always the authenticated caller, never taken from the request body. */
    ProductResponse createProduct(Long sellerId, String name, String description, String category,
                                   BigDecimal initialPrice, MultipartFile image);

    /** FR-PROD-04 — only the owning seller or an admin may update. */
    ProductResponse updateProduct(Long id, Long requesterId, boolean isAdmin, UpdateProductRequest request);

    /** FR-PROD-05 — only the owning seller or an admin may delete. */
    void deleteProduct(Long id, Long requesterId, boolean isAdmin);
}
