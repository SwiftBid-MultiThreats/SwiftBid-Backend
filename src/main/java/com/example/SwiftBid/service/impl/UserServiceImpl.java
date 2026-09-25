package com.example.SwiftBid.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SwiftBid.dto.user.UserSummaryResponse;
import com.example.SwiftBid.exception.ResourceNotFoundException;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserSummaryResponse::from).toList();
    }

    @Override
    public UserSummaryResponse getUserById(Long id) {
        return UserSummaryResponse.from(getUserEntity(id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(getUserEntity(id));
    }

    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));
    }
}
