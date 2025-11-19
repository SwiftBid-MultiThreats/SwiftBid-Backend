package com.example.SwiftBid.service;

import com.example.SwiftBid.payload.auction.AuctionDetailResponse;
import com.example.SwiftBid.payload.auction.UpdateAuctionDetailRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuctionDetailService {
    AuctionDetailResponse getAuctionDetail(Long auctionId);

    AuctionDetailResponse updateAuctionDetail(Long auctionId, UpdateAuctionDetailRequest request, String username);

    String updateBannerImage(Long auctionId, MultipartFile file, String username);
}