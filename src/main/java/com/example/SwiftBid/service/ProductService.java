package com.example.SwiftBid.service;

import java.util.List;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.payload.product.MyProductResponse;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    List<MyProductResponse> getMyProducts(String username);
    void deleteProduct(Long id);
}
