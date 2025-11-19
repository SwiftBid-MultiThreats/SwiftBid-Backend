package com.example.SwiftBid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Kích hoạt tính năng Bất đồng bộ
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Cấu hình hồ chứa luồng (Thread Pool)
        executor.setCorePoolSize(5);   // Luôn giữ 5 luồng chạy nền
        executor.setMaxPoolSize(20);   // Tối đa 20 luồng khi quá tải
        executor.setQueueCapacity(500); // Hàng đợi chứa tối đa 500 tác vụ
        executor.setThreadNamePrefix("SwiftBid-Async-");
        executor.initialize();
        return executor;
    }
}