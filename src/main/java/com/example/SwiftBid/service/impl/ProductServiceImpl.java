package com.example.SwiftBid.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.payload.product.MyProductResponse;
import com.example.SwiftBid.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.repository.ProductRepository;
import com.example.SwiftBid.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setInitialPrice(productDetails.getInitialPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setSeller(productDetails.getSeller());
        
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    // Sửa kiểu trả về
    public List<MyProductResponse> getMyProducts(String username) {

        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Lấy Entity List
        List<Product> products = productRepository.findBySellerId(seller.getId());

        // 2. Chuyển đổi Entity List sang DTO List (Đây là bước làm sạch)
        return products.stream()
                .map(MyProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
