package com.example.SwiftBid.service;

import java.util.List;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.payload.product.MyProductResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    MyProductResponse createProduct(Product product, MultipartFile imageFile, String username);
    Product updateProduct(Long id, Product productDetails);
    List<MyProductResponse> getMyProducts(String username);
    void deleteProduct(Long id);
}
