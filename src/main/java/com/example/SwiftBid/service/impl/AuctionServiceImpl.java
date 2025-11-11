package com.example.SwiftBid.service.impl;

import java.util.List;

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

    @Override
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    @Override
    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + id));
    }

    @Override
    public Auction createAuction(Auction auction, Long productId) {
        // Lấy Product từ productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        // Set product cho auction
        auction.setProduct(product);
        
        // Set currentHighestBidAmount bằng initialPrice của product
        auction.setCurrentHighestBidAmount(product.getInitialPrice());
        
        // Set status là PENDING (hoặc ACTIVE tùy logic)
        auction.setStatus(AuctionStatus.PENDING);
        
        return auctionRepository.save(auction);
    }

    @Override
    public Auction updateAuction(Long id, Auction auctionDetails) {
        Auction auction = getAuctionById(id);
        
        auction.setStartTime(auctionDetails.getStartTime());
        auction.setEndTime(auctionDetails.getEndTime());
        auction.setCurrentHighestBidAmount(auctionDetails.getCurrentHighestBidAmount());
        auction.setCurrentHighestBidder(auctionDetails.getCurrentHighestBidder());
        auction.setStatus(auctionDetails.getStatus());
        
        return auctionRepository.save(auction);
    }

    @Override
    public void deleteAuction(Long id) {
        Auction auction = getAuctionById(id);
        auctionRepository.delete(auction);
    }
}
