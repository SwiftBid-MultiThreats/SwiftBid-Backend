package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.dto.account.AccountStatsResponse;
import com.example.SwiftBid.exception.ResourceNotFoundException;
import com.example.SwiftBid.model.Role;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.model.enums.AuctionStatus;
import com.example.SwiftBid.model.enums.RoleName;
import com.example.SwiftBid.repository.AuctionRepository;
import com.example.SwiftBid.repository.BidRepository;
import com.example.SwiftBid.repository.RoleRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    @Override
    public List<String> getRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return user.getRoles().stream().map(r -> r.getName().name()).toList();
    }

    @Override
    @Transactional
    public void becomeSeller(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        boolean alreadySeller = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.SELLER);
        if (alreadySeller) {
            return; // idempotent, see FR-AUTH-08
        }

        Role sellerRole = roleRepository.findByName(RoleName.SELLER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.SELLER)));
        user.getRoles().add(sellerRole);
        userRepository.save(user);
    }

    @Override
    public AccountStatsResponse getStats(Long userId) {
        long auctionsCreated = auctionRepository.countByProductSellerId(userId);
        long auctionsParticipated = bidRepository.countDistinctAuctionsByUserId(userId);
        long auctionsWon = auctionRepository.countByStatusAndCurrentHighestBidderId(AuctionStatus.COMPLETED, userId);
        return new AccountStatsResponse(auctionsCreated, auctionsParticipated, auctionsWon);
    }
}
