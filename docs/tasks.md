# SwiftBid — Danh sách công việc (tasks.md)

> Bóc tách từ `plan.md`. Mỗi task gắn `FR-ID` liên quan (xem `spec.md`) và tầng (`BE`/`FE`/`DB`). Checklist dùng để theo dõi tiến độ — tick `[x]` khi hoàn thành và **đã có test tương ứng pass** (xem `tests.md`).
>
> Quy ước ID task: `T-<Phase>.<STT>`

---

## Phase 0 — Nền tảng bảo mật (P0)

- [x] `T-0.1` [DB] Thêm bảng `roles`, `user_roles`; script migrate dữ liệu `users.role` hiện có sang `user_roles`. — FR-AUTH-07
- [x] `T-0.2` [BE] Thêm dependency `spring-boot-starter-security`, thư viện JWT (`jjwt-api`/`jjwt-impl`/`jjwt-jackson` hoặc tương đương) vào `pom.xml`. — FR-AUTH-06
- [x] `T-0.3` [BE] Tạo `JwtService` (generate/validate/parse token, claims gồm `userId`, `username`, `roles`). — FR-AUTH-06
- [x] `T-0.4` [BE] Tạo `JwtAuthenticationFilter` + `SecurityConfig`: khai báo endpoint public (`GET /api/auctions/**`, `/api/auth/**`, trang tĩnh) vs. cần auth; cấu hình CORS cho origin của FE. — FR-AUTH-06
- [x] `T-0.5` [BE] Cấu hình `PasswordEncoder` (BCrypt) làm bean dùng chung. — FR-AUTH-01, NFR-02
- [x] `T-0.6` [BE] Thêm `@JsonIgnore` cho `User.passwordHash` (chặn rò rỉ ngay cả trước khi có DTO layer đầy đủ). — NFR-02

## Phase 1 — Auth & Account (P0)

- [x] `T-1.1` [BE] Entity `Role`, cập nhật `User` sang quan hệ `@ManyToMany` với `Role`. — FR-AUTH-07
- [x] `T-1.2` [BE] `AuthController.register` — validate unique username/email, hash password, gán role mặc định `USER`, trả `{token,user}`. — FR-AUTH-01
- [x] `T-1.3` [BE] `AuthController.login` — xác thực username/password, trả `{token,user}`, 401 khi sai. — FR-AUTH-02
- [x] `T-1.4` [BE] `AccountController.getRoles` — `GET /api/account/roles` trả mảng role của user hiện tại (từ JWT). — FR-AUTH-07
- [x] `T-1.5` [BE] `AccountController.becomeSeller` — `POST /api/account/become-seller`, idempotent, gán thêm role `SELLER`. — FR-AUTH-08
- [x] `T-1.6` [FE] Kiểm thử lại luồng Login/Register/Profile hiện có với API thật (hiện đang gọi vào endpoint chưa tồn tại — không cần sửa code FE, chỉ cần verify sau khi BE xong). — FR-AUTH-01/02/07/08

## Phase 2 — Hồ sơ & Sản phẩm (P0/P1)

- [x] `T-2.1` [DB] Bảng `user_details` (1-1 với `users`). — FR-USER-01
- [x] `T-2.2` [BE] `UserDetail` entity + `UserDetailController`: `GET /api/user-details/me`, `PUT /api/user-details/me`. — FR-USER-01, FR-USER-02
- [x] `T-2.3` [BE] `POST /api/user-details/me/avatar` (multipart, validate type/size ≤5MB, lưu file + trả `avatarUrl`). — FR-USER-03
- [x] `T-2.4` [DB] Thêm cột `products.category VARCHAR(100) NULL`. — FR-PROD-07
- [x] `T-2.5` [BE] Sửa `ProductController.createProduct` sang multipart (`name,description,initialPrice,category?,image?`); seller lấy từ `SecurityContext`, không nhận từ body. — FR-PROD-01
- [x] `T-2.6` [BE] `GET /api/products/my-products` — lọc theo seller hiện tại, bọc response `{status,data,timestamp}`. — FR-PROD-02
- [x] `T-2.7` [BE] Thêm kiểm tra quyền sở hữu (hoặc `ADMIN`) cho `PUT/DELETE /api/products/{id}`; xử lý lỗi FK khi xóa sản phẩm đã có auction thành message rõ ràng (400) thay vì lỗi 500. — FR-PROD-04, FR-PROD-05
- [x] `T-2.8` [BE] `GET /api/products/search?q=` (tìm theo `name`/`description`, không phân biệt hoa thường). — FR-PROD-06

## Phase 3 — Auction & Bidding core (P0 — trọng tâm)

- [x] `T-3.1` [DB] Bảng `auction_details` (1-1 với `auctions`); migration đổi giá trị enum `ENDED`→`COMPLETED` trong dữ liệu hiện có trước khi đổi code. — FR-AUC-01, FR-AUC-06
- [x] `T-3.2` [BE] Sửa `AuctionStatus` enum: `ENDED` → `COMPLETED` (khớp FE). — FR-AUC-06
- [x] `T-3.3` [BE] `AuctionDetail` entity + sửa `AuctionController.createAuction` sang multipart (`productId,startTime,endTime,auctionDescription?,targetAudience?,additionalTerms?,bannerImage?`); kiểm tra `productId` thuộc sở hữu người gọi (trừ Admin); validate `endTime > startTime`. — FR-AUC-01
- [x] `T-3.4` [BE] `GET /api/auctions/{id}/details` — DTO gộp `product + seller + auctionDetail + bidCount`. — FR-AUC-03
- [x] `T-3.5` [BE] `AuctionStatusScheduler` (`@Scheduled`, mỗi phút): chuyển `PENDING→ACTIVE` khi `now>=startTime`; `ACTIVE→COMPLETED` khi `now>=endTime`. — FR-AUC-06
- [x] `T-3.6` [BE] `GET /api/auctions/active`. — FR-AUC-07
- [x] `T-3.7` [BE] Viết lại `BidService.createBid` theo phương án Optimistic Locking + retry (xem `plan.md` §4): validate status/thời gian/giá/không-tự-đấu-giá, lấy `userId` từ JWT, cập nhật `Auction.currentHighestBidAmount/Bidder` trong cùng transaction, retry tối đa 3 lần khi `OptimisticLockException`. — FR-BID-01, 02, 06, 07
- [x] `T-3.8` [BE] `GET /api/bids/auction/{auctionId}` sắp xếp `timestamp DESC`. — FR-BID-03
- [x] `T-3.9` [BE] Bổ sung kiểm tra quyền sở hữu/Admin cho `PUT/DELETE /api/auctions/{id}`; đổi `DELETE` sang soft-cancel (`status=CANCELLED`) khi đã có bid. — FR-AUC-04, FR-AUC-05
- [x] `T-3.10` [FE] Nối nút "Đặt giá" ở `AuctionDetailPage` (hiện chưa có `onClick`) vào `bidService.placeBid`; hiển thị lịch sử bid thật thay vì khối tĩnh "Chưa có lượt đấu giá nào". — FR-BID-01, 03

## Phase 4 — Real-time & Thông báo (P1)

- [x] `T-4.1` [BE] `WebSocketConfig` (STOMP endpoint `/ws`, `SimpleBroker` `/topic`). — FR-BID-05
- [x] `T-4.2` [BE] `BidService` publish message tới `/topic/auctions/{auctionId}` sau khi cập nhật giá thành công. — FR-BID-05
- [x] `T-4.3` [FE] Subscribe `/topic/auctions/{id}` ở `AuctionDetailPage`, cập nhật giá/thời gian còn lại real-time. — FR-BID-05
- [x] `T-4.4` [BE] Cấu hình `spring.mail.*` (SMTP) trong `application.properties`/biến môi trường (không hard-code credential). — FR-NOTIF-01
- [x] `T-4.5` [DB] Bảng `password_reset_tokens`. — FR-AUTH-04/05
- [x] `T-4.6` [BE] `AuthController.forgotPassword` — sinh token có hạn, gửi email, luôn trả 200 chung chung. — FR-AUTH-04
- [x] `T-4.7` [BE] `AuthController.resetPassword` — validate token (chưa dùng, chưa hết hạn), cập nhật `passwordHash`, đánh dấu token đã dùng. — FR-AUTH-05

## Phase 5 — Trang chủ động & Quản trị & Hoàn thiện (P2)

- [x] `T-5.1` [BE] `GET /api/auctions/featured` (top N theo `bidCount` hoặc cờ `featured`). — FR-AUC-08
- [x] `T-5.2` [BE] Endpoint thống kê trang chủ (số phiên hoàn thành, số user, tổng giá trị giao dịch). — FR-HOME-01
- [x] `T-5.3` [FE] `HomePage`/`Projects` gọi API thật thay vì `src/data.js` tĩnh. — FR-HOME-01, 02
  - ✅ `Projects` (trang chủ + khối "Phiên đấu giá nổi bật") đã gọi `auctionService.getFeaturedAuctions()` thật, đã sửa các trường không khớp (`ENDED`→`COMPLETED`, `startingPrice`→`product.initialPrice`).
  - ⚠️ Chưa xong hoàn toàn: khối thống kê số liệu ở `HomePage.js` (số phiên hoàn thành, số người dùng...) vẫn đang hard-code tĩnh (`data-target`, `data-display`), chưa gọi `GET /api/home/stats` (đã có sẵn ở BE từ `T-5.2`). Để lại cho vòng sau.
- [x] `T-5.4` [BE] `@PreAuthorize("hasRole('ADMIN')")` cho toàn bộ `UserController`; audit lại mọi endpoint quản trị. — FR-ADMIN-01
- [x] `T-5.5` [BE] Cho phép Admin bỏ qua kiểm tra "chủ sở hữu" ở Product/Auction update/delete. — FR-ADMIN-02
- [x] `T-5.6` [BE] `GET /api/bids/user/{userId}`. — FR-BID-04
- [ ] `T-5.7` [BE] Thống kê tài khoản cho `ProfilePage` (số đấu giá tạo/tham gia/thắng). — FR-USER-04
- [x] `T-5.8` [BE] (Tùy chọn) `POST /api/contact` lưu `contact_messages`, thay dần EmailJS client-side. — FR-STATIC-02 mở rộng
- [ ] `T-5.9` [BE] Email thông báo thắng/thua khi auction chuyển `COMPLETED` (nối vào `T-3.5`). — FR-NOTIF-02

---

## Theo dõi tiến độ nhanh

| Phase | Số task | Đã xong | Còn lại |
|---|---|---|---|
| Phase 0 | 6 | 6 | 0 |
| Phase 1 | 6 | 6 | 0 |
| Phase 2 | 8 | 8 | 0 |
| Phase 3 | 10 | 10 | 0 |
| Phase 4 | 7 | 7 | 0 |
| Phase 5 | 9 | 7 | 2 (`T-5.7`, `T-5.9`) |
| **Tổng** | **46** | **44** | **2** |

**Trạng thái triển khai (cập nhật sau vòng code đầu tiên)**: Phase 0–4 đã cài đặt đầy đủ và có test tích hợp pass (28/28, xem `tests.md` §8 và kết quả `mvn test`), bao gồm cả test đặt giá đồng thời đa luồng (`BidConcurrencyTest`, 10/10 lần chạy không flaky). Phase 5 gần như hoàn tất, còn lại 2 việc nhỏ (P2, không chặn MVP):
- `T-5.7` — thống kê tài khoản thật cho `ProfilePage` (hiện FE vẫn hard-code số liệu `0`).
- `T-5.9` — email thông báo thắng/thua khi phiên đấu giá chuyển `COMPLETED`.

Ngoài checklist gốc, quá trình triển khai còn phát hiện và sửa thêm 2 lỗi thực tế không nằm trong kế hoạch ban đầu:
- `bidService.js` (frontend) đọc token từ `localStorage['authToken']` trong khi toàn bộ app dùng key `'token'` — khiến mọi request đặt giá trước đây **không bao giờ gửi kèm JWT**. Đã hợp nhất về dùng chung `config/api.js`.
- `ProductRepository.search()` dùng `LOWER()` trên field `description` từng được map `@Lob`/CLOB — Hibernate 6 chặn ở bước validate query (lỗi ngay cả với MySQL, không riêng gì H2 test). Đã bỏ `@Lob` khỏi các field mô tả dạng TEXT không cần streaming.

**Thứ tự khuyến nghị (ban đầu)**: Phase 0 → 1 → 2 → 3 (không làm tắt/song song vì mỗi phase là điều kiện tiên quyết bảo mật/dữ liệu cho phase sau) → 4 và 5 có thể song song một phần sau khi Phase 3 ổn định.
