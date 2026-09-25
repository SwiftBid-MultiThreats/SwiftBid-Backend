# SwiftBid — Kế hoạch triển khai (plan.md)

> Dựa trên khoảng cách (gap) giữa `spec.md` và mã nguồn hiện tại. Tài liệu này trả lời: **làm gì, theo thứ tự nào, thay đổi kiến trúc/dữ liệu gì**, để hiện thực hóa các FR còn ⛔/🟡.

---

## 1. Kiến trúc hiện tại vs. kiến trúc mục tiêu

### 1.1 Hiện tại
```
Controller (REST, không auth) → Service (interface) → ServiceImpl → Repository (Spring Data JPA) → MySQL
```
Không có: Security filter chain, JWT, WebSocket handler, Scheduler, file storage, mail sender thực thi, DTO layer (controller trả thẳng Entity JPA).

### 1.2 Mục tiêu
```
Client (React)
   │  Authorization: Bearer <JWT>
   ▼
[JwtAuthenticationFilter] → SecurityContext (userId, roles)
   ▼
Controller (nhận DTO Request, trả DTO Response — không lộ Entity trực tiếp)
   ▼
Service (nghiệp vụ + transaction + validate quyền sở hữu)
   ▼
Repository (JPA) ──────────────► MySQL (users, products, auctions, bids, user_details,
   │                                     auction_details, roles, user_roles,
   │                                     password_reset_tokens, contact_messages)
   ▼
[AuctionStatusScheduler] (mỗi phút): PENDING→ACTIVE→COMPLETED
   ▼
[BidService] khi có bid hợp lệ → cập nhật Auction (optimistic lock + retry)
                                → publish sự kiện qua WebSocket /topic/auctions/{id}
                                → (tuỳ chọn) gửi email khi kết thúc
```

---

## 2. Thay đổi mô hình dữ liệu (Data Model)

### 2.1 Bảng/entity cần bổ sung

| Bảng mới | Mục đích | FR liên quan |
|---|---|---|
| `roles` (`id`, `name`) + `user_roles` (`user_id`, `role_id`) | Hỗ trợ user có **nhiều role** (USER, SELLER, ADMIN) thay vì 1 cột enum đơn | FR-AUTH-07/08 |
| `user_details` (`user_id` FK 1-1, `full_name`, `phone_number`, `address`, `bio`, `avatar_url`) | Hồ sơ mở rộng, tách khỏi bảng `users` lõi | FR-USER-01/02/03 |
| `auction_details` (`auction_id` FK 1-1, `auction_description`, `target_audience`, `additional_terms`, `banner_image_url`) | Thông tin mở rộng phiên đấu giá | FR-AUC-01/03 |
| `password_reset_tokens` (`id`, `user_id`, `token`, `expires_at`, `used`) | Token đặt lại mật khẩu có hạn, 1 lần dùng | FR-AUTH-04/05 |
| `contact_messages` (tuỳ chọn, P2) (`id`, `name`, `email`, `subject`, `message`, `created_at`) | Lưu form liên hệ nếu chuyển từ EmailJS sang backend | FR-STATIC-02 mở rộng |

### 2.2 Thay đổi bảng hiện có

| Bảng | Thay đổi | Lý do |
|---|---|---|
| `users` | Bỏ cột `role` đơn (hoặc giữ làm "role chính" để tương thích ngược) → chuyển sang `user_roles` | Hỗ trợ multi-role |
| `products` | Thêm cột `category VARCHAR(100) NULL` | FR-PROD-07 |
| `auctions` | Enum `status` đổi giá trị `ENDED` → **`COMPLETED`** để khớp toàn bộ frontend (hoặc giữ `ENDED` ở DB và map ở DTO — khuyến nghị đổi thẳng DB vì ít rủi ro hơn về lâu dài) | Khớp FE (FR-AUC-06) |

### 2.3 Entity Java cần sửa
- `User.java`: bỏ field `role` đơn (hoặc giữ để migrate dần), thêm quan hệ `@ManyToMany` tới `Role`; **thêm `@JsonIgnore` cho `passwordHash`** (NFR-02, chặn rò rỉ ngay cả khi vẫn còn nơi trả entity trực tiếp).
- `Auction.java`: enum `AuctionStatus` sửa `ENDED` → `COMPLETED`.
- Thêm `UserDetail.java`, `AuctionDetail.java`, `PasswordResetToken.java`, `Role.java`.

---

## 3. Thiết kế API mới/sửa (tóm tắt hợp đồng)

| Method | Path | Ghi chú |
|---|---|---|
| POST | `/api/auth/register` | body `{username,email,password}` → `{token,user}` |
| POST | `/api/auth/login` | body `{username,password}` → `{token,user}` |
| POST | `/api/auth/forgot-password` | body `{email}` → 200 luôn (chống dò email) |
| POST | `/api/auth/reset-password` | body `{token,newPassword}` |
| GET | `/api/account/roles` | → `["USER","SELLER"]` |
| POST | `/api/account/become-seller` | idempotent |
| GET/PUT | `/api/user-details/me` | |
| POST | `/api/user-details/me/avatar` | multipart `file` |
| POST | `/api/products` | **đổi từ JSON → multipart** (`name,description,initialPrice,category?,image?`), seller lấy từ JWT |
| GET | `/api/products/my-products` | lọc theo seller hiện tại, response bọc `{status,data,timestamp}` để khớp FE |
| GET | `/api/products/search?q=` | |
| POST | `/api/auctions` | **đổi từ JSON → multipart** (`productId,startTime,endTime,auctionDescription?,targetAudience?,additionalTerms?,bannerImage?`) |
| GET | `/api/auctions` | thêm query `status,category,search,sort,page,size` (server-side) |
| GET | `/api/auctions/{id}/details` | trả DTO gộp `product + auctionDetail + bidCount` |
| GET | `/api/auctions/active` | |
| GET | `/api/auctions/featured` | |
| POST | `/api/bids` | body `{auctionId,bidAmount}`; `userId` lấy từ JWT |
| GET | `/api/bids/auction/{auctionId}` | sort theo `timestamp DESC` |
| GET | `/api/bids/user/{userId}` | |
| WS | `/ws` (SockJS/STOMP) → topic `/topic/auctions/{id}` | broadcast khi có bid mới |

**Nguyên tắc chung**: Controller không nhận `sellerId`/`userId` từ body khi hành động đó gắn với "chính người gọi" — luôn lấy từ `SecurityContext` (JWT) để chống giả mạo danh tính (đóng FR-BID-01, FR-PROD-01 lỗ hổng hiện tại).

---

## 4. Xử lý concurrency cho đặt giá (trọng tâm kỹ thuật, FR-BID-02)

Hai phương án, chọn 1 làm chuẩn:

**Phương án A — Optimistic Locking + Retry (khuyến nghị, tận dụng `@Version` đã có sẵn)**
```
@Transactional
placeBid(auctionId, userId, amount):
  retry tối đa N lần (vd 3):
    auction = auctionRepository.findById(auctionId)  // đọc version hiện tại
    validate: status == ACTIVE, now in [start,end], amount > auction.currentHighestBidAmount, userId != product.seller.id
    bid = new Bid(...); bidRepository.save(bid)
    auction.currentHighestBidAmount = amount
    auction.currentHighestBidder = userId
    try: auctionRepository.save(auction)  // ném OptimisticLockException nếu version lệch
        return success
    catch OptimisticLockException: retry (đọc lại auction mới nhất)
  nếu hết retry: trả lỗi 409 "Vui lòng thử lại"
```
- Ưu điểm: không khóa DB lâu, phù hợp tải vừa/cao với tranh chấp ngắn.
- Cần đo: tỉ lệ retry dưới tải giả lập nhiều bid đồng thời (xem `tests.md` — test concurrency).

**Phương án B — Pessimistic Lock (`SELECT ... FOR UPDATE`)**
- Dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)` trên query lấy `Auction` khi đặt giá.
- Đơn giản hơn để đảm bảo đúng, nhưng giữ khóa DB lâu hơn dưới tải cao → có thể là điểm nghẽn (bottleneck).

→ **Quyết định**: dùng **Phương án A** làm mặc định vì tận dụng thiết kế `@Version` sẵn có trong `Auction.java`; giữ Phương án B làm phương án dự phòng nếu benchmark cho thấy tỉ lệ retry quá cao.

---

## 5. Phân kỳ triển khai (Phases)

### Phase 0 — Nền tảng bảo mật (P0, chặn mọi phase sau)
- Thêm `spring-boot-starter-security` + `jjwt` (hoặc `nimbus-jose-jwt`) vào `pom.xml`.
- `JwtService` (sinh/validate token), `JwtAuthenticationFilter`, `SecurityConfig` (khai báo endpoint public vs. cần auth theo role).
- `PasswordEncoder` (BCrypt) dùng khi tạo/xác thực user.
- **FR đóng**: FR-AUTH-06, một phần FR-ADMIN-01.

### Phase 1 — Auth & Account (P0)
- `AuthController` (register/login), `Role`/`UserRole` entity + migration data (gán role `USER` mặc định cho user cũ).
- `GET /api/account/roles`, `POST /api/account/become-seller`.
- **FR đóng**: FR-AUTH-01/02/07/08.

### Phase 2 — Hồ sơ & Sản phẩm (P0/P1)
- `UserDetail` entity + `UserDetailController` (`GET/PUT /me`, avatar upload — cần cấu hình lưu file, vd local `/uploads` + serve static, hoặc để P2 nếu chưa cần production-grade).
- Sửa `ProductController.createProduct` sang multipart, tự gán seller từ JWT, thêm `getMyProducts`, `search`, thêm cột `category`.
- Bổ sung kiểm tra quyền sở hữu cho update/delete Product.
- **FR đóng**: FR-USER-01/02, FR-PROD-01/02/04/05/06/07.

### Phase 3 — Auction & Bidding core (P0 — trọng tâm hệ thống)
- `AuctionDetail` entity, sửa `createAuction` sang multipart + kiểm tra quyền sở hữu sản phẩm.
- `AuctionStatusScheduler` (`@Scheduled(fixedRate=60000)`): PENDING→ACTIVE→COMPLETED dựa trên `idx_auctions_end_time`.
- Viết lại `BidService.createBid` theo Phương án A (mục 4): validate đầy đủ + cập nhật Auction trong transaction + optimistic retry.
- `GET /api/auctions/{id}/details` (DTO gộp), `GET /api/auctions/active`, `GET /api/bids/auction/{auctionId}`.
- Đổi enum `ENDED`→`COMPLETED`.
- **FR đóng**: FR-AUC-01/03/04/05/06/07, FR-BID-01/02/03/06/07.

### Phase 4 — Real-time & Thông báo (P1)
- `WebSocketConfig` (STOMP endpoint `/ws`, broker `/topic`), publish khi `BidService` cập nhật giá thành công.
- FE: subscribe `/topic/auctions/{id}` ở `AuctionDetailPage`, nối nút "Đặt giá" (hiện chưa có `onClick`) vào `bidService.placeBid`.
- Cấu hình `spring.mail.*`, `MailService.sendPasswordResetEmail`, hoàn thiện `forgot-password`/`reset-password`.
- **FR đóng**: FR-BID-05, FR-AUTH-04/05, FR-NOTIF-01.

### Phase 5 — Trang chủ động & Quản trị & Hoàn thiện (P2)
- `GET /api/auctions/featured`, thống kê trang chủ (`FR-HOME-01/02`) — có thể bắt đầu bằng query đơn giản (COUNT, SUM) trước khi tối ưu cache.
- `@PreAuthorize` đầy đủ cho `UserController`/Admin endpoints (FR-ADMIN-01/02).
- Avatar upload, `getBidsByUser`, `searchProducts`, category filter, FR-NOTIF-02.
- **FR đóng**: các FR còn lại (P2).

---

## 6. Rủi ro & Giảm thiểu

| Rủi ro | Mức độ | Giảm thiểu |
|---|---|---|
| Lost update khi nhiều bid đồng thời | Cao | Phase 3 dùng optimistic locking + retry, có test tải chuyên biệt (`tests.md`) |
| Đổi enum `ENDED→COMPLETED` phá vỡ dữ liệu cũ | Trung bình | Viết migration script `UPDATE auctions SET status='COMPLETED' WHERE status='ENDED'` trước khi đổi enum Java |
| Đổi `users.role` đơn → multi-role phá vỡ dữ liệu cũ | Trung bình | Migration: tạo `user_roles` từ giá trị `role` hiện có trước khi bỏ cột cũ |
| Thiếu security ở Phase 0 làm chậm mọi phase sau | Cao nếu trì hoãn | Làm Phase 0 trước tiên, không song song với phase nghiệp vụ |
| Upload file (avatar/banner) chưa có storage production-grade | Trung bình | MVP dùng local disk + static resource mapping; ghi rõ nợ kỹ thuật để chuyển sang S3/Cloud Storage sau |

---

## 7. Ma trận truy vết FR ↔ Phase

| Phase | FR đóng |
|---|---|
| Phase 0 | FR-AUTH-06 |
| Phase 1 | FR-AUTH-01, 02, 07, 08 |
| Phase 2 | FR-USER-01, 02, FR-PROD-01, 02, 04, 05, 06, 07 |
| Phase 3 | FR-AUC-01, 03, 04, 05, 06, 07, FR-BID-01, 02, 03, 06, 07 |
| Phase 4 | FR-BID-05, FR-AUTH-04, 05, FR-NOTIF-01 |
| Phase 5 | FR-AUC-02(nâng cấp server-side), 08, FR-HOME-01, 02, FR-USER-03, 04, FR-BID-04, FR-ADMIN-01, 02, FR-NOTIF-02, FR-PROD-06(search) |
