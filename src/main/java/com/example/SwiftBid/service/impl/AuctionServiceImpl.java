package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.dto.auction.AuctionDetailResponse;
import com.example.SwiftBid.dto.auction.AuctionResponse;
import com.example.SwiftBid.exception.BadRequestException;
import com.example.SwiftBid.exception.ConflictException;
import com.example.SwiftBid.exception.ForbiddenException;
import com.example.SwiftBid.exception.ResourceNotFoundException;
import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.AuctionDetail;
import com.example.SwiftBid.model.Product;
import com.example.SwiftBid.model.enums.AuctionStatus;
import com.example.SwiftBid.repository.AuctionDetailRepository;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.BidRepository;
import com.example.SwiftBid.repository.ProductRepository;
import com.example.SwiftBid.service.AuctionService;
import com.example.SwiftBid.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionDetailRepository auctionDetailRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AuctionResponse getAuctionById(Long id) {
        return toResponse(getAuctionEntity(id));
    }

    @Override
    public AuctionDetailResponse getAuctionDetails(Long id) {
        Auction auction = getAuctionEntity(id);
        AuctionDetail detail = auctionDetailRepository.findById(id).orElse(null);
        long bidCount = bidRepository.countByAuctionId(id);
        return AuctionDetailResponse.from(auction, detail, bidCount);
    }

    @Override
    public List<AuctionResponse> getActiveAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.ACTIVE).stream().map(this::toResponse).toList();
    }

    @Override
    public List<AuctionResponse> getFeaturedAuctions(int limit) {
        return auctionRepository.findFeatured(PageRequest.of(0, Math.max(limit, 1)))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AuctionResponse createAuction(Long requesterId, boolean isAdmin, Long productId, Instant startTime,
                                          Instant endTime, String auctionDescription, String targetAudience,
                                          String additionalTerms, MultipartFile bannerImage) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + productId));

        if (!isAdmin && (product.getSeller() == null || !product.getSeller().getId().equals(requesterId))) {
            throw new ForbiddenException("Bạn chỉ có thể tạo phiên đấu giá cho sản phẩm của chính mình");
        }

        if (auctionRepository.existsByProductId(productId)) {
            throw new ConflictException("Sản phẩm này đã có một phiên đấu giá");
        }

        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        auction.setCurrentHighestBidAmount(product.getInitialPrice());
        auction.setStatus(AuctionStatus.PENDING);
        auction = auctionRepository.save(auction);

        if (hasDetailContent(auctionDescription, targetAudience, additionalTerms, bannerImage)) {
            AuctionDetail detail = new AuctionDetail(auction);
            detail.setAuctionDescription(auctionDescription);
            detail.setTargetAudience(targetAudience);
            detail.setAdditionalTerms(additionalTerms);
            if (bannerImage != null && !bannerImage.isEmpty()) {
                detail.setBannerImageUrl(fileStorageService.store(bannerImage, "auction-banners"));
            }
            auctionDetailRepository.save(detail);
        }

        return toResponse(auction);
    }

    @Override
    @Transactional
    public AuctionResponse updateAuction(Long id, Long requesterId, boolean isAdmin, Instant startTime, Instant endTime) {
        Auction auction = getAuctionEntity(id);
        assertOwnerOrAdmin(auction, requesterId, isAdmin);

        if (auction.getStatus() != AuctionStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể chỉnh sửa thời gian khi phiên đấu giá chưa bắt đầu");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        return toResponse(auctionRepository.save(auction));
    }

    @Override
    @Transactional
    public void cancelAuction(Long id, Long requesterId, boolean isAdmin) {
        Auction auction = getAuctionEntity(id);
        assertOwnerOrAdmin(auction, requesterId, isAdmin);

        if (auction.getStatus() == AuctionStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy phiên đấu giá đã kết thúc");
        }
        auction.setStatus(AuctionStatus.CANCELLED);
        auctionRepository.save(auction);
    }

    @Override
    @Transactional
    public int activatePendingAuctions() {
        List<Auction> due = auctionRepository.findByStatusAndStartTimeLessThanEqual(AuctionStatus.PENDING, Instant.now());
        due.forEach(a -> a.setStatus(AuctionStatus.ACTIVE));
        auctionRepository.saveAll(due);
        return due.size();
    }

    @Override
    @Transactional
    public int completeActiveAuctions() {
        List<Auction> due = auctionRepository.findByStatusAndEndTimeLessThanEqual(AuctionStatus.ACTIVE, Instant.now());
        due.forEach(a -> a.setStatus(AuctionStatus.COMPLETED));
        auctionRepository.saveAll(due);
        return due.size();
    }

    private Auction getAuctionEntity(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên đấu giá với id: " + id));
    }

    private void assertOwnerOrAdmin(Auction auction, Long requesterId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        Product product = auction.getProduct();
        if (product == null || product.getSeller() == null || !product.getSeller().getId().equals(requesterId)) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên phiên đấu giá này");
        }
    }

    private boolean hasDetailContent(String description, String targetAudience, String terms, MultipartFile banner) {
        return (description != null && !description.isBlank())
                || (targetAudience != null && !targetAudience.isBlank())
                || (terms != null && !terms.isBlank())
                || (banner != null && !banner.isEmpty());
    }

    private AuctionResponse toResponse(Auction auction) {
        String bannerImageUrl = auctionDetailRepository.findById(auction.getId())
                .map(AuctionDetail::getBannerImageUrl)
                .orElse(null);
        long bidCount = bidRepository.countByAuctionId(auction.getId());
        return AuctionResponse.from(auction, bannerImageUrl, bidCount);
    }
}
