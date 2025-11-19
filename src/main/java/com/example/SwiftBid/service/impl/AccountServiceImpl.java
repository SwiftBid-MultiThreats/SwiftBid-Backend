package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.exception.AppException;
import com.example.SwiftBid.exception.ErrorCode;
import com.example.SwiftBid.model.Role;
import com.example.SwiftBid.model.User;
import com.example.SwiftBid.repository.RoleRepository;
import com.example.SwiftBid.repository.UserRepository;
import com.example.SwiftBid.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void becomeSeller(String username) {
        // 1. Tìm người dùng đang đăng nhập
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Tìm vai trò "SELLER" trong CSDL
        Role sellerRole = roleRepository.findByName("SELLER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // 3. Kiểm tra xem họ đã là SELLER chưa
        boolean alreadySeller = user.getRoles().contains(sellerRole);

        if (alreadySeller) {
            throw new AppException(ErrorCode.USER_ALREADY_HAS_ROLE);
        }

        // 4. Thêm vai trò SELLER và lưu lại
        user.getRoles().add(sellerRole);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public Set<String> getCurrentUserRoles(String username) {
        // 1. Tìm User từ DB (để đảm bảo lấy dữ liệu mới nhất, không phải từ Token cũ)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        System.out.println( "User: " + user);

        // 2. Map từ Set<Role> sang Set<String> (ví dụ: ["USER", "SELLER"])
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}