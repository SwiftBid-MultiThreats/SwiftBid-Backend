package com.example.SwiftBid.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.AuctionDetail;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.payload.auction.AuctionSummaryResponse;
import com.example.SwiftBid.payload.auction.CreateAuctionRequest;
import com.example.SwiftBid.payload.auction.UpdateAuctionRequest;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.CloudinaryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.model.enums.AuctionStatus;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.ProductRepository;
import com.example.SwiftBid.service.AuctionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<AuctionSummaryResponse> getAllAuctions() {
        List<Auction> auctions = auctionRepository.findAllAuctionsWithDetails();

        return auctions.stream()
                .map(AuctionSummaryResponse::fromEntity)
                .collect(Collectors.toList()).reversed();
    }

    @Override
    public AuctionSummaryResponse getAuctionById(Long id) {
        Auction auction = auctionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        return AuctionSummaryResponse.fromEntity(auction);
    }

    @Override
    @Transactional
    public AuctionSummaryResponse updateAuction(Long id, UpdateAuctionRequest request) {
        Auction auction = auctionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));
        auction.setStartTime(request.startTime());
        auction.setEndTime(request.endTime());
        auction.setStatus(request.status());
        return AuctionSummaryResponse.fromEntity(auction);
    }

    @Override
    public void deleteAuction(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));
        if (auction.getStatus() == AuctionStatus.ACTIVE || auction.getStatus() == AuctionStatus.ENDED) {
            throw new AppException(ErrorCode.AUCTION_CANNOT_BE_DELETED);
        }
        auctionRepository.delete(auction);
    }

    @Override
    @Transactional
    public AuctionSummaryResponse createAuction(CreateAuctionRequest request, String sellerUsername) {
        // 1. Kiểm tra quyền và tìm kiếm (Giữ nguyên logic cũ)
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean isSellerOrAdmin = seller.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SELLER") || r.getName().equals("ADMIN"));

        if (!isSellerOrAdmin) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }

        // Kiểm tra: Người tạo Auction phải là chủ sở hữu Product (trừ khi là Admin)
        // (Bạn có thể thêm logic này nếu cần chặt chẽ hơn)

        // 2. Tạo Entity Auction (Giữ nguyên)
        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartTime(request.startTime());
        auction.setEndTime(request.endTime());
        auction.setCurrentHighestBidAmount(product.getInitialPrice());
        auction.setStatus(AuctionStatus.PENDING);

        // 3. Tạo Entity AuctionDetail
        AuctionDetail detail = new AuctionDetail();
        detail.setAuction(auction);
        detail.setAuctionDescription(request.auctionDescription());
        detail.setTargetAudience(request.targetAudience());
        detail.setAdditionalTerms(request.additionalTerms());

        // --- LOGIC UPLOAD ẢNH BANNER (MỚI) ---
        if (request.bannerImage() != null && !request.bannerImage().isEmpty()) {
            // Upload vào folder "auction_banners"
            String bannerUrl = cloudinaryService.uploadImage(request.bannerImage(), "auction_banners");
            detail.setBannerImageUrl(bannerUrl);
        } else {
             detail.setBannerImageUrl("https://media1.thehungryjpeg.com/thumbs2/ori_3880868_5glgyjhuq6907sry3ecj3rlv5smguypju3eysdp6_white-boxes-mockup-blank-product-package-3d-in-various-size-templates.jpg");
        }
        // -------------------------------------

        auction.setAuctionDetail(detail);

        // 4. Lưu và Tải lại đầy đủ (Để tránh lỗi Lazy Loading khi map sang DTO)
        Auction savedAuction = auctionRepository.save(auction);

        // Quan trọng: Fetch lại để lấy đủ thông tin cho DTO
        Auction fullyLoadedAuction = auctionRepository.findByIdWithDetails(savedAuction.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        // 5. Trả về DTO
        return AuctionSummaryResponse.fromEntity(fullyLoadedAuction);
    }
    @Override
    public List<AuctionSummaryResponse> getFeaturedAuctions() {
        // 1. Gọi phương thức đã tối ưu (ví dụ: chỉ lấy các phiên ACTIVE)
        List<Auction> auctions = auctionRepository.findAuctionsByStatusWithProduct(AuctionStatus.ACTIVE);

        // (Nếu muốn lấy tất cả, dùng: List<Auction> auctions = auctionRepository.findAllWithProduct();)

        // 2. Chuyển đổi (Map) từ List<Auction> sang List<AuctionCardResponse>
        return auctions.stream()
                .map(AuctionSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }


}
