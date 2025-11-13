package com.example.SwiftBid.config;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor // Dùng @RequiredArgsConstructor thay vì constructor thủ công
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil; // Bạn sẽ cần tạo lớp này

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String role = null;

        // 1. Kiểm tra header và lấy Token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                role = jwtUtil.extractRole(jwt); // Giữ logic của bạn: lấy role từ token
            } catch (ExpiredJwtException e) {
                // Thay đổi 2: Không dùng setErrorResponse, để cho Spring Security xử lý
                // Lỗi này sẽ được bắt bởi AuthenticationEntryPoint trong SecurityConfig
                logger.warn("JWT token has expired", e);
            } catch (JwtException e) {
                logger.warn("Invalid JWT token", e);
            }
        }

        // 2. Xác thực
        // Nếu đã có username VÀ chưa có ai được xác thực trong SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Nếu token hợp lệ, set Authentication trong SecurityContext
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // Giữ logic cũ của bạn: tạo Authority từ Role có trong Token
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, Collections.singletonList(authority));

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // Thay đổi 3: Bỏ hoàn toàn logic isPublicEndpoint.
        // Luôn chạy filter, SecurityConfig sẽ lo phần phân quyền.
        chain.doFilter(request, response);
    }
}