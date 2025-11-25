package com.example.SwiftBid.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.payload.product.MyProductResponse;
import com.example.SwiftBid.payload.product.ProductInfo;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.CloudinaryService;
import org.springframework.stereotype.Service;

import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.repository.ProductRepository;
import com.example.SwiftBid.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

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
    @Transactional
    public MyProductResponse createProduct(Product product, MultipartFile imageFile, String username) {
        // 1. Tìm User
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Xử lý ảnh (Nếu có file ảnh được gửi lên)
        if (imageFile != null && !imageFile.isEmpty()) {
            // Upload ảnh vào folder "products" trên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(imageFile, "products");
            product.setImageUrl(imageUrl); // Gán URL vào Product
        } else {
            // Nếu không có ảnh, có thể set ảnh mặc định hoặc null
            product.setImageUrl("https://via.placeholder.com/300?text=No+Image");
        }

        // 3. Gán Seller
        product.setSeller(seller);
        productRepository.save(product);
        MyProductResponse response = MyProductResponse.fromEntity(product);

        // 4. Lưu Product
        return response;
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
