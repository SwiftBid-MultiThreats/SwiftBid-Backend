-- Kịch bản này sẽ tạo CSDL, các bảng và chỉ mục (Indexes) cho hệ thống SwiftBid
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

-- Bảng 1: Người dùng (lõi)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL COMMENT 'Lưu mật khẩu đã được băm (hashed)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 1b: Vai trò (hỗ trợ 1 user có nhiều vai trò)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (name) VALUES ('USER'), ('SELLER'), ('ADMIN');

-- Bảng 1c: Hồ sơ mở rộng của người dùng
CREATE TABLE user_details (
    user_id BIGINT PRIMARY KEY,
    full_name VARCHAR(255),
    phone_number VARCHAR(30),
    address VARCHAR(500),
    bio TEXT,
    avatar_url VARCHAR(1024),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 1d: Token đặt lại mật khẩu
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 2: Sản phẩm
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL COMMENT 'Khóa ngoại tới users(id)',
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NULL,
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

    status ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',

    version INT NOT NULL DEFAULT 0 COMMENT 'Dành cho Optimistic Locking của JPA',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ràng buộc: Một sản phẩm chỉ có thể được đấu giá trong 1 phiên duy nhất
    UNIQUE KEY uk_product_id (product_id),

    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (current_highest_bidder_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng 3b: Thông tin mở rộng của phiên đấu giá
CREATE TABLE auction_details (
    auction_id BIGINT PRIMARY KEY,
    auction_description TEXT,
    target_audience VARCHAR(500),
    additional_terms TEXT,
    banner_image_url VARCHAR(1024),
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE
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

-- Bảng 5: Tin nhắn liên hệ (tuỳ chọn, FR-STATIC-02 mở rộng)
CREATE TABLE contact_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

-- Tăng tốc độ tìm tất cả các phiên đặt giá của một người dùng
CREATE INDEX idx_bids_user_id ON bids(user_id);

-- Tăng tốc độ lấy sản phẩm theo seller (FR-PROD-02)
CREATE INDEX idx_products_seller_id ON products(seller_id);

-- Tăng tốc độ lấy token reset password còn hiệu lực
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);



-- Script tạo bảng cho Spring Session JDBC trên MySQL
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;
-- ----------
-- KẾT THÚC KỊCH BẢN
-- ----------
