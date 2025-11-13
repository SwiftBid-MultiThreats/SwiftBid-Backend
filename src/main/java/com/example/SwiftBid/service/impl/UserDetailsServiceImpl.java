package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.model.User;
import com.example.SwiftBid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// 1. Đánh dấu đây là một Bean (@Service)
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService { // 2. Implement interface

    private final UserRepository userRepository;

    // 3. Đây là phương thức mà Spring Security sẽ gọi
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 4. Tìm người dùng trong CSDL bằng phương thức ta vừa tạo
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 5. Lấy quyền (role) của họ
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // 6. Trả về đối tượng UserDetails mà Spring Security hiểu
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(), // Trả về mật khẩu hash
                Collections.singletonList(authority) // Trả về danh sách quyền
        );
    }
}