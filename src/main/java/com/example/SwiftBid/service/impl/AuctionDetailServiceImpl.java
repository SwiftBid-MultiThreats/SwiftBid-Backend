package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.AuctionDetail;
import com.example.SwiftBid.model.Role;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.payload.auction.AuctionDetailResponse;
import com.example.SwiftBid.payload.auction.UpdateAuctionDetailRequest;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.AuctionDetailService;
import com.example.SwiftBid.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuctionDetailServiceImpl implements AuctionDetailService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // Helper: Lấy Auction và kiểm tra quyền (Chỉ Admin hoặc người bán)
    private Auction findAndCheckPermission(Long auctionId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        // Kiểm tra: Hoặc là Admin, hoặc là người bán (seller)   của sản phẩm
        boolean isAdmin = currentUser.getRoles().equals("ADMIN");
        boolean isSeller = auction.getProduct().getSeller().equals(currentUser);

        if (!isAdmin && !isSeller) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }

        return auction;
    }

    @Override
    @Transactional(readOnly = true)
    public AuctionDetailResponse getAuctionDetail(Long auctionId) {
        Auction auction = auctionRepository.findByIdWithDetails(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));
        return AuctionDetailResponse.fromEntity(auction);
    }

    @Override
    @Transactional
    public AuctionDetailResponse updateAuctionDetail(Long auctionId, UpdateAuctionDetailRequest request, String username) {
        Auction auction = findAndCheckPermission(auctionId, username);
        AuctionDetail detail = auction.getAuctionDetail();
        detail.setAuctionDescription(request.auctionDescription());
        detail.setTargetAudience(request.targetAudience());
        detail.setAdditionalTerms(request.additionalTerms());
        return AuctionDetailResponse.fromEntity(auction);
    }

    @Override
    @Transactional
    public String updateBannerImage(Long auctionId, MultipartFile file, String username) {
        Auction auction = findAndCheckPermission(auctionId, username);
        AuctionDetail detail = auction.getAuctionDetail();

        String oldBannerUrl = detail.getBannerImageUrl();

        // 1. Tải ảnh mới lên (dùng folder "auction_banners")
        String newBannerUrl = cloudinaryService.uploadImage(file, "auction_banners");

        // 2. Cập nhật CSDL
        detail.setBannerImageUrl(newBannerUrl);

        // 3. Xóa ảnh cũ (nếu có)
        if (oldBannerUrl != null && !oldBannerUrl.isEmpty()) {
            cloudinaryService.deleteImage(oldBannerUrl);
        }

        return newBannerUrl;
    }
}