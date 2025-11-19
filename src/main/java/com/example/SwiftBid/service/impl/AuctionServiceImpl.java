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
        // 1. Kiểm tra quyền và tìm kiếm
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND)); // Giả sử dùng mã lỗi này

        // Security Check: Đảm bảo chỉ Seller/Admin mới được tạo auction
        boolean isSellerOrAdmin = seller.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SELLER") || r.getName().equals("ADMIN"));

        if (!isSellerOrAdmin) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }

        // 2. Tạo Entity Auction
        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartTime(request.startTime());
        auction.setEndTime(request.endTime());

        // Logic nghiệp vụ: Set giá khởi điểm và trạng thái
        auction.setCurrentHighestBidAmount(product.getInitialPrice());
        auction.setStatus(AuctionStatus.PENDING); // Mặc định là chờ

        // 3. Tạo Entity AuctionDetail (và thiết lập quan hệ 1-1)
        AuctionDetail detail = new AuctionDetail();

        // Lấy ID từ Auction để thiết lập quan hệ 1-1
        detail.setAuction(auction);
        detail.setAuctionDescription(request.auctionDescription());
        detail.setTargetAudience(request.targetAudience());
        detail.setAdditionalTerms(request.additionalTerms());
        detail.setBannerImageUrl(request.bannerImageUrl()); // URL từ Cloudinary

        // Thiết lập quan hệ hai chiều (để đảm bảo cascade hoạt động)
        auction.setAuctionDetail(detail);

        // 4. Lưu Auction (JPA sẽ tự động lưu AuctionDetail nhờ CascadeType.ALL)
        Auction savedAuction = auctionRepository.save(auction);

        // 5. Trả về DTO
        return AuctionSummaryResponse.fromEntity(savedAuction);
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
