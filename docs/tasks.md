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
  - ✅ `Projects` gọi `auctionService.getFeaturedAuctions()` thật, đã sửa các trường không khớp (`ENDED`→`COMPLETED`, `startingPrice`→`product.initialPrice`).
  - ✅ `HomePage.js` nay gọi `homeService.getStats()` (`GET /api/home/stats`) cho 2/4 ô thống kê (số phiên hoàn thành, số người dùng — có animation đếm số); ô "Tổng giá trị giao dịch" hiển thị số thật định dạng VNĐ. Ô "Khách hàng hài lòng" (99.9%) giữ tĩnh vì hệ thống chưa có cơ chế đánh giá/rating để tính ra con số này.
- [x] `T-5.4` [BE] `@PreAuthorize("hasRole('ADMIN')")` cho toàn bộ `UserController`; audit lại mọi endpoint quản trị. — FR-ADMIN-01
- [x] `T-5.5` [BE] Cho phép Admin bỏ qua kiểm tra "chủ sở hữu" ở Product/Auction update/delete. — FR-ADMIN-02
- [x] `T-5.6` [BE] `GET /api/bids/user/{userId}`. — FR-BID-04
- [x] `T-5.7` [BE] Thống kê tài khoản cho `ProfilePage` (số đấu giá tạo/tham gia/thắng). — FR-USER-04
  - `GET /api/account/stats` (`AccountStatsResponse`), wired vào `ProfilePage.js`.
- [x] `T-5.8` [BE] (Tùy chọn) `POST /api/contact` lưu `contact_messages`, thay dần EmailJS client-side. — FR-STATIC-02 mở rộng
- [x] `T-5.9` [BE] Email thông báo thắng/thua khi auction chuyển `COMPLETED` (nối vào `T-3.5`). — FR-NOTIF-02
  - `MailService.sendAuctionWonEmail` (người thắng) + `sendAuctionEndedEmailToSeller` (người bán, có/không người thắng), gọi từ `AuctionServiceImpl.completeActiveAuctions()`.

### Bổ sung ngoài checklist gốc (phát hiện khi audit UI vòng 2)

- [x] `T-5.10` [BE+FE] `GET /api/auctions/my-auctions` + trang `MyAuctionsPage` (sửa lịch khi PENDING, hủy phiên) — nav đã có link `/my-auctions` từ trước nhưng route/API chưa tồn tại (404 thật). — FR-AUC-04/05
- [x] `T-5.11` [FE] Trang `MyBidsPage` (`/my-bids`) hiển thị lịch sử đặt giá của chính user, đánh dấu bid đang thắng — nav đã có link nhưng route chưa tồn tại (404 thật). — FR-BID-04
- [x] `T-5.12` [FE] Bộ lọc "Danh mục" ở `AuctionsPage`: state `selectedCategory` và logic filter đã có sẵn nhưng **không có UI để đổi giá trị** — filter chết, luôn kẹt ở "ALL". Thêm dropdown chọn danh mục (lấy động từ dữ liệu). — FR-AUC-02
- [x] `T-5.13` [FE] Sửa `auctionService.updateAuction` gửi JSON body trong khi backend nhận `startTime`/`endTime` qua request param — sẽ luôn lỗi 400 nếu dùng. — FR-AUC-04
- [x] `T-5.14` [FE] Đồng hồ đếm ngược ở `AuctionDetailPage` giờ tick mỗi giây thay vì chỉ tính 1 lần lúc render.

### Tính năng nâng cao (vòng 3, theo yêu cầu "đảm bảo dự án chạy đầy đủ + tính năng nâng cao")

- [x] `T-6.1` [BE] `GET /api/auctions/search` — lọc/sắp xếp/phân trang **phía server** (status, category, từ khóa, sort, page, size) qua `JpaSpecificationExecutor`, thay vì tải toàn bộ auction về rồi lọc ở client (không scale). `MOST_BIDS` sort rơi về NEWEST ở server (ghi chú rõ lý do trong code) và được sắp lại phía client trong phạm vi 1 trang. — FR-AUC-02
- [x] `T-6.2` [FE] `AuctionsPage` chuyển hẳn sang gọi `searchAuctions()`, bỏ toàn bộ logic lọc/sắp xếp/cắt trang thủ công ở client.
- [x] `T-6.3` [FE] Sửa sản phẩm (`MyProductsPage`): trước đây chỉ có Tạo/Xóa. Thêm nút "Sửa" dùng lại modal tạo sản phẩm (không đổi được ảnh khi sửa — ghi chú rõ trong UI, muốn đổi ảnh thì tạo sản phẩm mới). — FR-PROD-04
- [x] `T-6.4` [BE+FE] Trang **Admin** (`/admin`, chỉ ADMIN thấy trong menu): danh sách người dùng + xóa, dùng API `GET/DELETE /api/users` đã có sẵn ở backend nhưng chưa từng có UI. — FR-ADMIN-01
- [x] `T-6.5` [FE] Thông báo real-time "Bạn đã bị vượt giá" (toast, `react-toastify`) trên `AuctionDetailPage` khi WebSocket báo có người khác vượt giá của chính mình — tận dụng hạ tầng WebSocket đã có từ Phase 4. — FR-BID-05

---

## Theo dõi tiến độ nhanh

| Phase | Số task | Đã xong | Còn lại |
|---|---|---|---|
| Phase 0 | 6 | 6 | 0 |
| Phase 1 | 6 | 6 | 0 |
| Phase 2 | 8 | 8 | 0 |
| Phase 3 | 10 | 10 | 0 |
| Phase 4 | 7 | 7 | 0 |
| Phase 5 | 9 + 5 bổ sung | 14 | 0 |
| Phase 6 (nâng cao) | 5 | 5 | 0 |
| **Tổng** | **56** | **56** | **0** |

**Trạng thái triển khai (cập nhật sau vòng 3 — "đảm bảo chạy đầy đủ + tính năng nâng cao")**: Toàn bộ 56/56 task đã hoàn thành, có test tích hợp pass (**33/33**, xem `tests.md` §8 và kết quả `mvn test`), bao gồm cả test đặt giá đồng thời đa luồng (`BidConcurrencyTest`, ổn định qua nhiều lần chạy — 1 lần bị timeout do tải hệ thống lúc benchmark, chạy lại riêng lẻ pass 10/10 ngay). Không còn FR nào ở mức ⛔ trong `spec.md`.

Ngoài checklist gốc, quá trình triển khai (3 vòng) còn phát hiện và sửa thêm các lỗi/khoảng trống thực tế không nằm trong kế hoạch ban đầu:
- `bidService.js` (frontend) đọc token từ `localStorage['authToken']` trong khi toàn bộ app dùng key `'token'` — khiến mọi request đặt giá trước đây **không bao giờ gửi kèm JWT**. Đã hợp nhất về dùng chung `config/api.js`.
- `ProductRepository.search()` dùng `LOWER()` trên field `description` từng được map `@Lob`/CLOB — Hibernate 6 chặn ở bước validate query (lỗi ngay cả với MySQL, không riêng gì H2 test). Đã bỏ `@Lob` khỏi các field mô tả dạng TEXT không cần streaming.
- Menu người dùng (`Navigation.js`) trỏ tới `/my-auctions`, `/my-bids`, `/settings` — **cả 3 route đều 404 thật** vì trang/route chưa từng được tạo. Đã bổ sung `MyAuctionsPage`, `MyBidsPage` (+ API `GET /api/auctions/my-auctions`, `GET /api/account/stats`); `Settings` trỏ về `/profile`.
- Bộ lọc "Danh mục" ở `AuctionsPage` có state + logic filter đầy đủ nhưng **không có UI để đổi giá trị** — filter chết, không ai dùng được.
- `auctionService.updateAuction` gửi JSON body trong khi backend nhận `startTime`/`endTime` qua request param — sẽ lỗi 400 nếu có trang nào gọi tới (đúng lúc `MyAuctionsPage` mới cần dùng).
- Không có trang Admin nào dù backend đã có sẵn API quản trị (`GET/DELETE /api/users`, cờ bỏ qua kiểm tra chủ sở hữu cho Product/Auction) — không ai dùng được các API đó qua UI.
- `MyProductsPage` chỉ có Tạo/Xóa, không có Sửa, dù `PUT /api/products/{id}` đã tồn tại từ vòng 1.

**Thứ tự khuyến nghị (ban đầu)**: Phase 0 → 1 → 2 → 3 (không làm tắt/song song vì mỗi phase là điều kiện tiên quyết bảo mật/dữ liệu cho phase sau) → 4 và 5 có thể song song một phần sau khi Phase 3 ổn định. Phase 6 (nâng cao) chỉ nên làm sau khi Phase 0–5 đã ổn định.
