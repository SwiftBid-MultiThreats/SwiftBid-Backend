package com.example.SwiftBid.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SwiftBid.model.Bid;
import com.example.SwiftBid.repository.BidRepository;
import com.example.SwiftBid.service.BidService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;

    @Override
    public List<Bid> getAllBids() {
        return bidRepository.findAll();
    }

    @Override
    public Bid getBidById(Long id) {
        return bidRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bid not found with id: " + id));
    }

    @Override
    public Bid createBid(Bid bid) {
        return bidRepository.save(bid);
    }

    @Override
    public Bid updateBid(Long id, Bid bidDetails) {
        Bid bid = getBidById(id);
        
        bid.setAuction(bidDetails.getAuction());
        bid.setUser(bidDetails.getUser());
        bid.setBidAmount(bidDetails.getBidAmount());
        
        return bidRepository.save(bid);
    }

    @Override
    public void deleteBid(Long id) {
        Bid bid = getBidById(id);
        bidRepository.delete(bid);
    }
}
