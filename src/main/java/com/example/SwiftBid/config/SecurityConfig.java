package com.example.SwiftBid.config; // Đổi tên package

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SecurityConfig {

    // Chúng ta sẽ cần tạo lớp JwtRequestFilter này
    JwtRequestFilter jwtRequestFilter;

    // (Đã loại bỏ các Handler của OAuth2)

    /*
    // TODO: SPRINT 2 - Kích hoạt và cấu hình WebSocket Security
    // Đây là cấu hình bảo mật cho WebSocket, chúng ta sẽ làm ở Sprint 2
    @Configuration
    public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
        @Override
        protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
            messages
                    // Yêu cầu xác thực cho các tin nhắn gửi đến server (ví dụ: đặt giá)
                    .simpDestMatchers("/app/**").authenticated()
                    .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT).permitAll() // Cho phép kết nối/ngắt kết nối
                    // Yêu cầu xác thực để subscribe vào các topic
                    .simpSubscribeDestMatchers("/topic/**").authenticated()
                    .anyMessage().authenticated();
        }

        @Override
        protected boolean sameOriginDisabled() {
            return true; // Tắt kiểm tra same-origin (cho phép localhost:3000 gọi)
        }
    }
    */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF cho API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // --- Endpoints Xác thực ---
                                "/api/auth/register", // Đăng ký
                                "/api/auth/login",    // Đăng nhập
                                "/api/products/**",
                                "/api/auctions/**",
                                "/ws/**", // Endpoint cho WebSocket Handshake
                                "/topic/**", // (Tạm thời cho phép)
                                "/app/**",   // (Tạm thời cho phép)

                                "/error"
                        ).permitAll()

                        // --- Endpoints ADMIN (Quản lý) ---
                        .requestMatchers(
                                HttpMethod.POST, "/api/products", "/api/auctions"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PUT, "/api/products/**", "/api/auctions/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/products/**", "/api/auctions/**"
                        ).hasRole("ADMIN")

                        // --- Endpoints USER (Hành động) ---
                        .requestMatchers(
                                // Quan trọng: Đặt giá phải được xác thực
                                "/api/auctions/**/bid",
                                "/api/bids/**"
                        ).authenticated()

                        // Tất cả các request còn lại đều phải xác thực
                        .anyRequest().authenticated()
                )
                // (Đã loại bỏ cấu hình .oauth2Login())
                .sessionManagement(session -> session
                        // Cấu hình STATELESS (Không dùng Session) vì chúng ta dùng JWT
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        // Xử lý lỗi 401 (Chưa xác thực)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                        })
                        // Xử lý lỗi 403 (Không có quyền)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
                        })
                );

        // Thêm Filter JWT vào trước Filter mặc định
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép React (localhost:3000) gọi API
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Luôn dùng BCrypt
        return new BCryptPasswordEncoder();
    }

    // (Đã loại bỏ oidcUserService() của OAuth2)

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}