package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.model.User;
import com.example.SwiftBid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

// 1. Đánh dấu đây là một Bean (@Service)
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService { // 2. Implement interface

    private final UserRepository userRepository;

    // 3. Đây là phương thức mà Spring Security sẽ gọi
    @Override
    @Transactional // Quan trọng để đảm bảo roles được load
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // ----- PHẦN CẬP NHẬT -----
        // 1. Chuyển đổi Set<Role> (từ DB) thành Set<GrantedAuthority>
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());

        // 2. Trả về UserDetails với danh sách quyền MỚI
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities // Truyền danh sách quyền (Set) vào đây
        );
    }
}