-- SỬ DỤNG CSDL CỦA BẠN
USE swiftbid_db;

-- ----------
-- BƯỚC 1: TẠO NGƯỜI DÙNG (USERS)
-- Mật khẩu mẫu cho cả 3 user là 'password123' (BCrypt). Sinh hash thật khi seed dữ liệu,
-- ví dụ bằng PasswordEncoder.encode("password123") trong 1 test/CommandLineRunner tạm thời.
-- ----------
INSERT INTO users (id, username, email, password_hash) VALUES
(1, 'admin_alice', 'alice@admin.com', '$2a$10$replace-with-real-bcrypt-hash'),
(2, 'user_bob', 'bob@user.com', '$2a$10$replace-with-real-bcrypt-hash'),
(3, 'user_charlie', 'charlie@user.com', '$2a$10$replace-with-real-bcrypt-hash');

-- Gán vai trò: alice = ADMIN, bob = USER + SELLER, charlie = USER
INSERT INTO user_roles (user_id, role_id)
SELECT 1, id FROM roles WHERE name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT 2, id FROM roles WHERE name = 'USER';
INSERT INTO user_roles (user_id, role_id)
SELECT 2, id FROM roles WHERE name = 'SELLER';
INSERT INTO user_roles (user_id, role_id)
SELECT 3, id FROM roles WHERE name = 'USER';

-- ----------
-- BƯỚC 2: TẠO SẢN PHẨM (PRODUCTS)
-- ----------
INSERT INTO products (id, seller_id, name, description, category, initial_price) VALUES
(1, 1, 'Máy ảnh Sony A7 III', 'Máy ảnh Mirrorless Full-frame còn mới 99%', 'Điện tử', 100000.00),
(2, 2, 'Laptop Dell XPS 15', 'Laptop cao cấp, Core i7, 16GB RAM, 512GB SSD', 'Điện tử', 500000.00),
(3, 3, 'Bàn phím cơ Filco', 'Bàn phím cơ Filco Majestouch 2, Blue Switch', 'Điện tử', 300000.00);

-- ----------
-- BƯỚC 3: TẠO PHIÊN ĐẤU GIÁ (AUCTIONS)
-- Chúng ta sẽ giả định thời gian hiện tại là '2025-11-11 19:00:00'
-- ----------

-- Phiên 1: ĐANG HOẠT ĐỘNG (ACTIVE)
-- Bắt đầu 1 ngày trước, kết thúc 1 ngày sau
INSERT INTO auctions (id, product_id, start_time, end_time, current_highest_bid_amount, current_highest_bidder_id, status) VALUES
(1, 1,
 '2025-11-10 19:00:00', -- Đã bắt đầu
 '2025-11-12 19:00:00', -- Chưa kết thúc
 100000.00,             -- Giá khởi điểm
 NULL,                  -- Chưa có ai đặt
 'ACTIVE');

-- Phiên 2: SẮP DIỄN RA (PENDING)
-- Bắt đầu 1 ngày sau
INSERT INTO auctions (id, product_id, start_time, end_time, current_highest_bid_amount, status) VALUES
(2, 2,
 '2025-11-12 19:00:00', -- Bắt đầu trong tương lai
 '2025-11-14 19:00:00',
 500000.00,             -- Giá khởi điểm
 'PENDING');

-- Phiên 3: ĐÃ KẾT THÚC (COMPLETED)
-- Đã kết thúc 1 ngày trước
INSERT INTO auctions (id, product_id, start_time, end_time, current_highest_bid_amount, current_highest_bidder_id, status) VALUES
(3, 3,
 '2025-11-09 19:00:00', -- Đã bắt đầu
 '2025-11-10 19:00:00', -- Đã kết thúc
 300000.00,
 NULL,
 'COMPLETED');

INSERT INTO auction_details (auction_id, auction_description, target_audience, additional_terms, banner_image_url) VALUES
(1, 'Máy ảnh chính hãng, đầy đủ phụ kiện đi kèm.', 'Nhiếp ảnh gia, người mới bắt đầu', 'Người thắng thanh toán trong 48h.', NULL);

-- ----------
-- BƯỚC 4: TẠO LỊCH SỬ ĐẶT GIÁ (BIDS)
-- ----------

-- Thêm một số Bid cho phiên 1 (ĐANG HOẠT ĐỘNG)
INSERT INTO bids (auction_id, user_id, bid_amount, timestamp) VALUES
(1, 2, 110000.00, '2025-11-11 10:00:00'), -- Bob đặt giá 110k
(1, 3, 120000.00, '2025-11-11 11:00:00'); -- Charlie đặt giá 120k

-- Thêm một số Bid cho phiên 3 (ĐÃ KẾT THÚC)
INSERT INTO bids (auction_id, user_id, bid_amount, timestamp) VALUES
(3, 1, 310000.00, '2025-11-09 20:00:00'), -- Alice đặt giá 310k
(3, 2, 320000.00, '2025-11-09 21:00:00'); -- Bob đặt giá 320k

-- ----------
-- BƯỚC 5: CẬP NHẬT LẠI GIÁ CAO NHẤT (QUAN TRỌNG)
-- Trong ứng dụng thật, việc này do BidService cập nhật tự động trong cùng transaction
-- với việc ghi bid (xem BidServiceImpl.placeBid). Ở đây chỉ để dữ liệu mẫu nhất quán.
-- ----------

-- Cập nhật phiên 1: Charlie (id=3) đang giữ giá 120000.00
UPDATE auctions
SET current_highest_bid_amount = 120000.00, current_highest_bidder_id = 3
WHERE id = 1;

-- Cập nhật phiên 3: Bob (id=2) đã thắng với giá 320000.00
UPDATE auctions
SET current_highest_bid_amount = 320000.00, current_highest_bidder_id = 2
WHERE id = 3;
