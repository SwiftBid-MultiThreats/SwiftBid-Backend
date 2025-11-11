-- Kịch bản này sẽ tạo CSDL, 4 bảng chính, và các chỉ mục (Indexes)
-- Sử dụng InnoDB để hỗ trợ Transaction và Foreign Keys
-- Sử dụng utf8mb4 để hỗ trợ đầy đủ Unicode

-- ----------
-- BƯỚC 1: TẠO CSDL VÀ SỬ DỤNG NÓ
-- ----------
DROP DATABASE IF EXISTS swiftbid_db;
CREATE DATABASE swiftbid_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;
USE swiftbid_db;

-- ----------
-- BƯỚC 2: TẠO BẢNG
-- ----------

-- Bảng 1: Người dùng
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL COMMENT 'Lưu mật khẩu đã được băm (hashed)',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 2: Sản phẩm
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL COMMENT 'Khóa ngoại tới users(id)',
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_price DECIMAL(19, 4) NOT NULL COMMENT 'Giá khởi điểm',
    image_url VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 3: Phiên đấu giá (Bảng cốt lõi)
CREATE TABLE auctions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT 'Khóa ngoại tới products(id)',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    
    current_highest_bid_amount DECIMAL(19, 4) NOT NULL COMMENT 'Giá cao nhất hiện tại',
    current_highest_bidder_id BIGINT NULL COMMENT 'Khóa ngoại tới users(id)',
    
    status ENUM('PENDING', 'ACTIVE', 'ENDED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    
    version INT NOT NULL DEFAULT 0 COMMENT 'Dành cho Optimistic Locking của JPA',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ràng buộc: Một sản phẩm chỉ có thể được đấu giá trong 1 phiên duy nhất
    UNIQUE KEY uk_product_id (product_id), 
    
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (current_highest_bidder_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 4: Lịch sử Đặt giá
CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_id BIGINT NOT NULL COMMENT 'Khóa ngoại tới auctions(id)',
    user_id BIGINT NOT NULL COMMENT 'Khóa ngoại tới users(id)',
    bid_amount DECIMAL(19, 4) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------
-- BƯỚC 3: TẠO CHỈ MỤC (INDEXES) ĐỂ TĂNG HIỆU SUẤT
-- ----------

-- Tăng tốc độ tìm kiếm các phiên đấu giá đang 'ACTIVE'
CREATE INDEX idx_auctions_status ON auctions(status);

-- Tăng tốc độ tìm kiếm các phiên đấu giá sắp kết thúc (cho Scheduled Job)
CREATE INDEX idx_auctions_end_time ON auctions(end_time);

-- Tăng tốc độ lấy lịch sử đặt giá cho một phiên đấu giá cụ thể
CREATE INDEX idx_bids_auction_id ON bids(auction_id);

-- TTăng tốc độ tìm tất cả các phiên đặt giá của một người dùng
CREATE INDEX idx_bids_user_id ON bids(user_id);

-- ----------
-- KẾT THÚC KỊCH BẢN
-- ----------