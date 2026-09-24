package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.dto.product.ProductResponse;
import com.example.SwiftBid.dto.product.UpdateProductRequest;
import com.example.SwiftBid.exception.BadRequestException;
import com.example.SwiftBid.exception.ForbiddenException;
import com.example.SwiftBid.exception.ResourceNotFoundException;
import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.repository.ProductRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.FileStorageService;
import com.example.SwiftBid.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return ProductResponse.from(getProductEntity(id));
    }

    @Override
    public List<ProductResponse> getMyProducts(Long sellerId) {
        return productRepository.findBySellerId(sellerId).stream().map(ProductResponse::from).toList();
    }

    @Override
    public List<ProductResponse> search(String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        return productRepository.search(query.trim()).stream().map(ProductResponse::from).toList();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(Long sellerId, String name, String description, String category,
                                          BigDecimal initialPrice, MultipartFile image) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException("Tên sản phẩm không được để trống");
        }
        if (initialPrice == null || initialPrice.signum() <= 0) {
            throw new BadRequestException("Giá khởi điểm phải lớn hơn 0");
        }

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Product product = new Product();
        product.setSeller(seller);
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setInitialPrice(initialPrice);
        if (image != null && !image.isEmpty()) {
            product.setImageUrl(fileStorageService.store(image, "products"));
        }

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, Long requesterId, boolean isAdmin, UpdateProductRequest request) {
        Product product = getProductEntity(id);
        assertOwnerOrAdmin(product, requesterId, isAdmin);

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setInitialPrice(request.initialPrice());

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long requesterId, boolean isAdmin) {
        Product product = getProductEntity(id);
        assertOwnerOrAdmin(product, requesterId, isAdmin);

        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Không thể xóa sản phẩm vì đã có phiên đấu giá liên kết. Hãy hủy phiên đấu giá trước.");
        }
    }

    private Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + id));
    }

    private void assertOwnerOrAdmin(Product product, Long requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (product.getSeller() == null || !product.getSeller().getId().equals(requesterId)) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên sản phẩm này");
        }
    }
}
