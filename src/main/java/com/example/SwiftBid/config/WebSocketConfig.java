package com.example.SwiftBid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Điểm kết nối (Handshake endpoint)
        // Client (React) sẽ kết nối vào: http://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000") // Cho phép React gọi
                .withSockJS(); // Hỗ trợ fallback nếu trình duyệt không có WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Cấu hình đường dẫn Topic (Pub/Sub)
        // Client sẽ subscribe vào các đường dẫn bắt đầu bằng /topic
        registry.enableSimpleBroker("/topic");

        // Tiền tố cho các tin nhắn từ Client gửi lên (nếu dùng)
        registry.setApplicationDestinationPrefixes("/app");
    }
}