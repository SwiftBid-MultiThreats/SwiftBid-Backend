# SwiftBid — Đặc tả yêu cầu chức năng (spec.md)

- **Hệ thống**: SwiftBid — Website đấu giá trực tuyến đa luồng (Multi-threaded Online Auction System)
- **Repo liên quan**: `SwiftBid-Backend` (Spring Boot), `swiftbid-frontend` (React)
- **Trạng thái tài liệu**: Draft v1.0 — tổng hợp từ mã nguồn hiện có (backend CRUD sơ khai + frontend đã scaffold UI/flow đầy đủ)
- **Ngày lập**: 2026-09-24

> Tài liệu này mô tả **toàn bộ yêu cầu chức năng (FR)** của hệ thống SwiftBid, bao gồm cả phần đã cài đặt và phần frontend đã thiết kế UI/flow nhưng backend chưa hỗ trợ. Mỗi FR có cột **Trạng thái** để phân biệt việc "viết tài liệu cho cái đã có" và "đặc tả cho cái cần làm".

---

## 1. Tổng quan hệ thống

SwiftBid cho phép:
- Người dùng đăng ký/đăng nhập, nâng cấp thành **Seller** để đăng bán sản phẩm.
- Seller tạo **sản phẩm (Product)** và mở **phiên đấu giá (Auction)** cho sản phẩm đó, có thời gian bắt đầu/kết thúc.
- Người dùng đã đăng nhập **đặt giá (Bid)** trong thời gian phiên đấu giá đang hoạt động; hệ thống phải xử lý an toàn khi **nhiều người đặt giá đồng thời** (yếu tố "multi-threaded" cốt lõi của hệ thống).
- Hệ thống cập nhật giá cao nhất theo thời gian thực, gửi email (quên/đặt lại mật khẩu), và cung cấp trang quản trị cơ bản.

### 1.1 Actor (Đối tượng người dùng)

| Actor | Mô tả |
|---|---|
| **Guest** (Khách) | Chưa đăng nhập. Xem trang chủ, danh sách/chi tiết đấu giá, giới thiệu, liên hệ. |
| **User/Bidder** (Người dùng) | Đã đăng ký & đăng nhập, vai trò mặc định `USER`. Có thể đặt giá, quản lý hồ sơ, nâng cấp thành Seller. |
| **Seller** (Người bán) | User đã nâng cấp vai trò `SELLER`. Có thêm quyền tạo/quản lý sản phẩm và phiên đấu giá. |
| **Admin** (Quản trị viên) | Vai trò `ADMIN`. Toàn quyền quản lý người dùng, sản phẩm, phiên đấu giá. |

**Ghi chú mô hình quyền**: Frontend gọi `GET /api/account/roles` và mong đợi trả về **mảng roles** (vd `["USER","SELLER"]`), tức một user có thể có nhiều vai trò cùng lúc. Trong khi đó, entity `User` hiện tại (`model/User.java`) chỉ có **1 trường `role` enum đơn** (`USER`/`ADMIN`, chưa có `SELLER`). Đây là khoảng cách cần giải quyết ở `plan.md`.

### 1.2 Phạm vi tài liệu

- **Trong phạm vi**: Toàn bộ FR liên quan Auth, Hồ sơ người dùng, Sản phẩm, Phiên đấu giá, Đặt giá, Trang chủ/Khám phá, Trang tĩnh, Thông báo/Email, Quản trị.
- **Ngoài phạm vi**: Thanh toán thực tế, vận chuyển, tích hợp cổng thanh toán (chỉ nhắc tới ở FAQ frontend, chưa có FR chi tiết), tính năng AI (spring-ai đã có dependency nhưng chưa dùng — không đặc tả FR ở phiên bản này).

### 1.3 Chú giải trạng thái FR

| Ký hiệu | Ý nghĩa |
|---|---|
| ✅ **Đã có** | Đã cài đặt đầy đủ ở backend, khớp với frontend. |
| 🟡 **Một phần** | Có FE hoặc BE nhưng thiếu logic nghiệp vụ / chưa khớp nhau. |
| ⛔ **Chưa có** | FE đã thiết kế UI/gọi API nhưng BE hoàn toàn chưa có endpoint/logic tương ứng. |

Độ ưu tiên: **P0** (chặn ra mắt MVP), **P1** (quan trọng), **P2** (mở rộng).

---

## 2. Module AUTH — Xác thực & Phân quyền

### FR-AUTH-01 — Đăng ký tài khoản
- **Actor**: Guest
- **Mô tả**: Guest tạo tài khoản mới bằng `username`, `email`, `password`.
- **Input**: `username` (≥3 ký tự), `email` (định dạng hợp lệ), `password` (≥6 ký tự theo validate ở FE `RegisterPage`, nhưng hằng số `VALIDATION.MIN_PASSWORD_LENGTH` ở `constants.js` lại quy định 8 — **cần thống nhất**), `confirmPassword` (đối chiếu ở FE, không gửi lên server).
- **Output/Acceptance Criteria**:
  - Username & email phải duy nhất (unique constraint DB) → lỗi rõ ràng nếu trùng.
  - Mật khẩu được **băm (hash)** trước khi lưu (không lưu plaintext).
  - Trả về `{ token, user }` để FE tự động đăng nhập ngay sau đăng ký.
  - Vai trò mặc định: `USER`.
- **API liên quan**: `POST /api/auth/register`
- **Trạng thái**: ⛔ Chưa có (không có `AuthController`, không có Spring Security/JWT trong backend). **Ưu tiên**: P0

### FR-AUTH-02 — Đăng nhập
- **Actor**: Guest
- **Mô tả**: Đăng nhập bằng `username` + `password`, nhận JWT token.
- **Output**: `{ token, user }`. Sai thông tin → HTTP 401 với message rõ ràng ("Sai tên đăng nhập hoặc mật khẩu").
- **API liên quan**: `POST /api/auth/login`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P0

### FR-AUTH-03 — Đăng xuất
- **Actor**: User đã đăng nhập
- **Mô tả**: Xóa token khỏi client (`localStorage`) và header `Authorization` mặc định của axios. Vì dùng JWT stateless, không nhất thiết cần API backend (có thể chỉ xử lý phía client), nhưng cần đảm bảo token cũ hết hạn hợp lý (xem FR-AUTH-06).
- **API liên quan**: `POST /api/auth/logout` (khai báo trong `constants.js` của FE nhưng chưa thấy được gọi ở đâu — có thể dự phòng cho cơ chế blacklist token).
- **Trạng thái**: 🟡 Một phần (FE xử lý client-side qua `authService.logout()`; BE endpoint optional). **Ưu tiên**: P2

### FR-AUTH-04 — Quên mật khẩu (Forgot Password)
- **Actor**: Guest/User
- **Mô tả**: Người dùng nhập email → hệ thống gửi email chứa link đặt lại mật khẩu kèm token có thời hạn.
- **Acceptance Criteria**:
  - Luôn trả về thông báo thành công chung chung dù email có tồn tại hay không (chống dò email — FE đã hiển thị message dạng "Nếu email này tồn tại...").
  - Token reset có thời hạn (ví dụ 15–30 phút), one-time-use.
  - Gửi email qua `spring-boot-starter-mail` (đã có dependency, chưa cấu hình `application.properties`).
- **API liên quan**: `POST /api/auth/forgot-password`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-AUTH-05 — Đặt lại mật khẩu (Reset Password)
- **Actor**: Guest (có token từ email)
- **Mô tả**: Người dùng truy cập link `/reset-password/:token`, nhập mật khẩu mới (≥6 ký tự) + xác nhận.
- **Acceptance Criteria**: Token không hợp lệ/hết hạn → lỗi rõ ràng, FE hiển thị trạng thái "Token không hợp lệ". Token hợp lệ → cập nhật `password_hash`, vô hiệu hóa token đã dùng.
- **API liên quan**: `POST /api/auth/reset-password` — body `{ token, newPassword }`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-AUTH-06 — Xác thực phiên bằng JWT
- **Actor**: Hệ thống
- **Mô tả**: Mọi request tới API cần xác thực (trừ các endpoint public: xem danh sách/chi tiết đấu giá, trang tĩnh) phải kèm header `Authorization: Bearer <token>`. Backend cần Spring Security filter chain + JWT provider để validate token, gắn `Principal` cho request.
- **Acceptance Criteria**: Token hết hạn/không hợp lệ → 401. Không có quyền → 403.
- **Trạng thái**: ⛔ Chưa có (pom.xml không có `spring-boot-starter-security`; `spring-session-jdbc` có sẵn nhưng chưa được cấu hình dùng cho session/JWT). **Ưu tiên**: P0

### FR-AUTH-07 — Phân quyền theo vai trò (RBAC) & xem danh sách vai trò
- **Actor**: User đã đăng nhập
- **Mô tả**: Trả về danh sách vai trò hiện có của user (`["USER"]` hoặc `["USER","SELLER"]` hoặc thêm `"ADMIN"`).
- **API liên quan**: `GET /api/account/roles`
- **Acceptance Criteria**: Các trang `CreateAuctionPage`, `MyProductsPage`, `ProfilePage` dựa vào kết quả này để hiện/ẩn nút chức năng dành cho Seller.
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P0

### FR-AUTH-08 — Nâng cấp tài khoản thành Seller
- **Actor**: User (role `USER`)
- **Mô tả**: User bấm "Nâng cấp thành Seller" ở trang hồ sơ → xác nhận trong modal → gọi API gán thêm vai trò `SELLER`.
- **Acceptance Criteria**: Idempotent (gọi lại khi đã là Seller không lỗi, hoặc trả lỗi rõ ràng "đã là Seller"). Sau khi nâng cấp, `GET /api/account/roles` phải phản ánh ngay vai trò mới.
- **API liên quan**: `POST /api/account/become-seller`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P0 (chặn toàn bộ luồng bán hàng/đấu giá)

---

## 3. Module USER — Hồ sơ người dùng

### FR-USER-01 — Xem thông tin cá nhân
- **Actor**: User đã đăng nhập
- **Mô tả**: Lấy thông tin chi tiết của chính mình: `username`, `fullName`, `email`, `phoneNumber`, `address`, `bio`, `avatarUrl`, `role`, `createdAt`.
- **API liên quan**: `GET /api/user-details/me`
- **Trạng thái**: ⛔ Chưa có endpoint; entity `User` hiện tại chỉ có `username/email/passwordHash/role/createdAt`, thiếu `fullName/phoneNumber/address/bio/avatarUrl` (cần entity `UserDetail` mở rộng — xem `plan.md`). **Ưu tiên**: P1

### FR-USER-02 — Cập nhật thông tin cá nhân
- **Actor**: User đã đăng nhập
- **Mô tả**: Cập nhật `fullName`, `email`, `phoneNumber`, `address`, `bio`. `username` không được sửa.
- **API liên quan**: `PUT /api/user-details/me`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-USER-03 — Cập nhật ảnh đại diện
- **Actor**: User đã đăng nhập
- **Mô tả**: Tải ảnh (jpg/png/gif/webp, ≤5MB) làm avatar.
- **API liên quan**: `POST /api/user-details/me/avatar` (multipart `file`) → trả `{ avatarUrl }`
- **Trạng thái**: ⛔ Chưa có (cần cơ chế lưu trữ file — local disk hoặc cloud storage). **Ưu tiên**: P2

### FR-USER-04 — Thống kê tài khoản
- **Actor**: User đã đăng nhập
- **Mô tả**: Hiển thị số đấu giá đã tạo (nếu Seller), số đấu giá đã tham gia, số đấu giá đã thắng, ngày tham gia.
- **Trạng thái**: ⛔ Chưa có (FE hiện đang hard-code giá trị `0`). **Ưu tiên**: P2

### FR-USER-05 — Quản lý người dùng (CRUD cơ bản)
- **Actor**: Hệ thống/Admin
- **Mô tả**: Liệt kê, xem, tạo, sửa, xóa user.
- **API liên quan**: `GET/POST /api/users`, `GET/PUT/DELETE /api/users/{id}`
- **Trạng thái**: ✅ Đã có (CRUD cơ bản trong `UserController`/`UserServiceImpl`), nhưng **chưa có kiểm soát quyền** (bất kỳ ai gọi cũng được — cần giới hạn Admin, xem FR-ADMIN-01) và **chưa hash password khi tạo/sửa qua endpoint này**. **Ưu tiên**: P1 (bổ sung bảo mật)

---

## 4. Module PRODUCT — Quản lý sản phẩm

### FR-PROD-01 — Tạo sản phẩm
- **Actor**: Seller/Admin
- **Mô tả**: Tạo sản phẩm với `name`, `description`, `initialPrice`, ảnh (`image`, multipart, ≤5MB). `seller` được gán tự động từ JWT của người gọi (không nhận `sellerId` từ client).
- **Acceptance Criteria**: `initialPrice > 0`. Ảnh được lưu và trả về `imageUrl` công khai.
- **API liên quan**: `POST /api/products` (multipart/form-data)
- **Trạng thái**: 🟡 Một phần — endpoint `POST /api/products` tồn tại nhưng nhận **JSON `Product`** (bao gồm cả `seller` do client gửi lên, không tự gán từ JWT) chứ **không hỗ trợ multipart/ảnh**, và **không kiểm tra quyền Seller**. **Ưu tiên**: P0

### FR-PROD-02 — Xem sản phẩm của tôi
- **Actor**: Seller/Admin
- **Mô tả**: Liệt kê sản phẩm do chính Seller đang đăng nhập sở hữu.
- **API liên quan**: `GET /api/products/my-products` — response dạng `{ status, data: [...], timestamp }`
- **Trạng thái**: ⛔ Chưa có (backend hiện chỉ có `GET /api/products` liệt kê toàn bộ, không lọc theo seller, và không bọc response theo format `{status,data,timestamp}`). **Ưu tiên**: P0

### FR-PROD-03 — Xem chi tiết sản phẩm
- **Actor**: Guest/User
- **API liên quan**: `GET /api/products/{id}`
- **Trạng thái**: ✅ Đã có. **Ưu tiên**: —

### FR-PROD-04 — Cập nhật sản phẩm
- **Actor**: Seller sở hữu sản phẩm/Admin
- **Acceptance Criteria**: Chỉ chủ sở hữu hoặc Admin được sửa; không cho sửa nếu sản phẩm đã gắn với phiên đấu giá đang `ACTIVE`.
- **API liên quan**: `PUT /api/products/{id}`
- **Trạng thái**: 🟡 Một phần — CRUD tồn tại nhưng **không kiểm tra quyền sở hữu**. **Ưu tiên**: P1

### FR-PROD-05 — Xóa sản phẩm
- **Actor**: Seller sở hữu sản phẩm/Admin
- **Acceptance Criteria**: Không cho xóa nếu đã có phiên đấu giá liên kết (ràng buộc FK `auctions.product_id`); trả lỗi rõ ràng thay vì lỗi SQL constraint.
- **API liên quan**: `DELETE /api/products/{id}`
- **Trạng thái**: 🟡 Một phần — chưa kiểm tra quyền sở hữu & chưa xử lý ràng buộc FK gracefully. **Ưu tiên**: P1

### FR-PROD-06 — Tìm kiếm sản phẩm
- **Actor**: Guest/User
- **Mô tả**: Tìm theo tên hoặc mô tả (query param `q`).
- **API liên quan**: `GET /api/products/search?q=...`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P2

### FR-PROD-07 — Phân loại sản phẩm theo danh mục (category)
- **Actor**: Seller (khi tạo), Guest/User (khi lọc)
- **Mô tả**: `AuctionsPage` (FE) lọc theo `auction.product?.category`, nhưng entity `Product` **chưa có trường `category`**.
- **Trạng thái**: ⛔ Chưa có (cần thêm cột `category` vào bảng `products`). **Ưu tiên**: P2

---

## 5. Module AUCTION — Quản lý phiên đấu giá

### FR-AUC-01 — Tạo phiên đấu giá
- **Actor**: Seller sở hữu sản phẩm/Admin
- **Mô tả**: Chọn 1 sản phẩm **chưa có phiên đấu giá** của chính mình, nhập `startTime`, `endTime` (`endTime > startTime`), giá khởi điểm (mặc định = `product.initialPrice`), và thông tin mở rộng tùy chọn: `auctionDescription`, `targetAudience`, `additionalTerms`, ảnh `bannerImage` (multipart, ≤5MB).
- **Acceptance Criteria**:
  - `productId` phải thuộc sở hữu người gọi (trừ Admin).
  - 1 sản phẩm chỉ có tối đa 1 phiên đấu giá (đã có `UNIQUE KEY uk_product_id` ở DB).
  - Trạng thái khởi tạo: `PENDING`.
- **API liên quan**: `POST /api/auctions` (multipart/form-data: `productId, startTime, endTime, auctionDescription?, targetAudience?, additionalTerms?, bannerImage?`)
- **Trạng thái**: 🟡 Một phần — `AuctionController.createAuction` hiện nhận JSON `Auction` + query param `productId`, **không hỗ trợ multipart/banner, không có entity `AuctionDetail`, không kiểm tra quyền sở hữu sản phẩm**. **Ưu tiên**: P0

### FR-AUC-02 — Danh sách phiên đấu giá (khám phá, lọc, sắp xếp, phân trang)
- **Actor**: Guest/User
- **Mô tả**: Liệt kê phiên đấu giá, hỗ trợ:
  - Lọc theo `status` (PENDING/ACTIVE/COMPLETED/CANCELLED) và `category`.
  - Tìm kiếm theo tên/mô tả sản phẩm.
  - Sắp xếp: mới nhất, sắp kết thúc, giá thấp→cao, giá cao→thấp, nhiều lượt đấu giá nhất.
  - Phân trang (12 items/trang).
- **API liên quan**: `GET /api/auctions` (hiện tại FE tự lọc/sắp xếp/phân trang **ở client** sau khi lấy toàn bộ dữ liệu — không tối ưu khi dữ liệu lớn; nên chuyển sang server-side filter/sort/paginate qua query params).
- **Trạng thái**: 🟡 Một phần — `GET /api/auctions` trả toàn bộ list, thiếu field `bidCount`, `productName`, `bannerImageUrl` phẳng ở object trả về mà FE đang cần (FE đọc `auction.productName`, `auction.bannerImageUrl`, `auction.bidCount` trực tiếp — cần DTO chuyên biệt). **Ưu tiên**: P1

### FR-AUC-03 — Chi tiết phiên đấu giá
- **Actor**: Guest/User
- **Mô tả**: Trả về đầy đủ: thông tin `product` (bao gồm `seller.username`), `auctionDetail` (banner, mô tả, đối tượng, điều khoản), giá hiện tại, thời gian, trạng thái.
- **API liên quan**: `GET /api/auctions/{id}/details`
- **Trạng thái**: ⛔ Chưa có (backend chỉ có `GET /api/auctions/{id}` trả entity `Auction` trần, thiếu `auctionDetail`). **Ưu tiên**: P0

### FR-AUC-04 — Cập nhật phiên đấu giá
- **Actor**: Seller sở hữu/Admin
- **Acceptance Criteria**: Chỉ cho sửa thời gian khi phiên còn `PENDING` (chưa bắt đầu); không cho sửa `currentHighestBidAmount`/`currentHighestBidder` thủ công qua API này (chỉ được cập nhật qua luồng đặt giá — hiện `updateAuction` đang cho set trực tiếp, là lỗ hổng nghiệp vụ).
- **API liên quan**: `PUT /api/auctions/{id}`
- **Trạng thái**: 🟡 Một phần — tồn tại nhưng thiếu kiểm tra quyền & cho phép ghi đè giá/người thắng trực tiếp (rủi ro gian lận). **Ưu tiên**: P1

### FR-AUC-05 — Hủy/xóa phiên đấu giá
- **Actor**: Seller sở hữu/Admin
- **Acceptance Criteria**: Không cho xóa cứng phiên đã có bid; nên chuyển trạng thái `CANCELLED` thay vì xóa (soft state).
- **API liên quan**: `DELETE /api/auctions/{id}`
- **Trạng thái**: 🟡 Một phần — hiện là xóa cứng (hard delete), không có `CANCELLED` flow, không kiểm tra quyền. **Ưu tiên**: P1

### FR-AUC-06 — Tự động chuyển trạng thái theo thời gian (Scheduled Job)
- **Actor**: Hệ thống (background job)
- **Mô tả**: Job chạy định kỳ (ví dụ mỗi phút):
  - `PENDING` → `ACTIVE` khi `now >= startTime`.
  - `ACTIVE` → `ENDED`/`COMPLETED` khi `now >= endTime`. (**Ghi chú mismatch**: DB & enum backend dùng `ENDED`, còn FE dùng chuỗi `"COMPLETED"` ở nhiều nơi (`AuctionsPage`, `AuctionDetailPage`, `constants.js`) — cần **thống nhất 1 giá trị enum duy nhất**, khuyến nghị đổi backend `ENDED` → `COMPLETED` để khớp FE, hoặc map ở DTO tầng API.)
  - Khi phiên kết thúc có người thắng → có thể trigger gửi email thông báo (FR-NOTIF-02, P2).
- **Trạng thái**: ⛔ Chưa có (không có class `@Scheduled` nào trong backend — `idx_auctions_end_time` đã được tạo index sẵn cho DB nhưng chưa dùng tới). **Ưu tiên**: P0

### FR-AUC-07 — Danh sách phiên đấu giá đang hoạt động
- **API liên quan**: `GET /api/auctions/active`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-AUC-08 — Danh sách phiên đấu giá nổi bật (Featured)
- **Actor**: Guest (trang chủ)
- **Mô tả**: Trả về danh sách rút gọn (vd top N theo số lượt bid hoặc do Admin gắn cờ "nổi bật") để hiển thị ở `HomePage` (component `Projects`).
- **API liên quan**: `GET /api/auctions/featured`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P2

---

## 6. Module BID — Đặt giá (lõi nghiệp vụ đa luồng)

### FR-BID-01 — Đặt giá hợp lệ
- **Actor**: User đã đăng nhập (không phải Seller của chính sản phẩm đó — xem FR-BID-06)
- **Mô tả**: Gửi `{ auctionId, bidAmount }` (userId lấy từ JWT, **không nhận từ client** để tránh giả mạo).
- **Acceptance Criteria**:
  - Auction phải ở trạng thái `ACTIVE` và trong khoảng `[startTime, endTime]`.
  - `bidAmount > currentHighestBidAmount` (có thể cấu hình bước giá tối thiểu, vd +1 hoặc +% — P2).
  - Ghi 1 bản ghi `bids` mới.
- **API liên quan**: `POST /api/bids`
- **Trạng thái**: 🟡 Một phần — `BidServiceImpl.createBid()` chỉ `save()` bản ghi `Bid` thẳng, **không validate** trạng thái auction, thời gian, hay `bidAmount` so với giá hiện tại; **không lấy userId từ JWT** (nhận trực tiếp từ body — lỗ hổng giả mạo danh tính). **Ưu tiên**: P0

### FR-BID-02 — Cập nhật giá cao nhất an toàn khi có tranh chấp đồng thời (Concurrency)
- **Actor**: Hệ thống
- **Mô tả**: Khi 1 bid hợp lệ được ghi nhận, hệ thống phải **cập nhật `auctions.current_highest_bid_amount` và `current_highest_bidder_id` trong cùng 1 transaction**, đảm bảo tính nhất quán khi **nhiều thread/request đặt giá đồng thời cho cùng 1 auction**.
- **Acceptance Criteria**:
  - Sử dụng **Optimistic Locking** sẵn có (`Auction.version` + `@Version`) kèm cơ chế **retry** khi gặp `OptimisticLockException` (vd tối đa 3 lần retry), HOẶC dùng `SELECT ... FOR UPDATE` / pessimistic lock ở tầng repository cho auction đang được đặt giá.
  - Không được xảy ra tình huống 2 bid cùng lúc đều "thắng" tạm thời (lost update).
  - Đây là yêu cầu **cốt lõi nhất của hệ thống** ("Multi-threaded Online Auction System") — cần test tải đồng thời (xem `tests.md`).
- **Trạng thái**: ⛔ Chưa có (entity `Auction` đã có `@Version` nhưng **chưa được service nào sử dụng để cập nhật giá khi có bid mới** — hiện `BidService` và `AuctionService` hoàn toàn tách rời, không gọi nhau). **Ưu tiên**: P0 (rủi ro kỹ thuật cao nhất)

### FR-BID-03 — Lịch sử đặt giá theo phiên đấu giá
- **Actor**: Guest/User (xem trang chi tiết đấu giá)
- **API liên quan**: `GET /api/bids/auction/{auctionId}` (nên sắp xếp giảm dần theo `timestamp`)
- **Trạng thái**: ⛔ Chưa có (FE `AuctionDetailPage` hiện hiển thị khối "Chưa có lượt đấu giá nào" tĩnh, chưa gọi API). **Ưu tiên**: P1

### FR-BID-04 — Lịch sử đặt giá theo người dùng
- **API liên quan**: `GET /api/bids/user/{userId}`
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P2

### FR-BID-05 — Cập nhật giá thời gian thực (Real-time)
- **Actor**: User đang xem trang chi tiết đấu giá
- **Mô tả**: Khi có bid mới hợp lệ, mọi client đang mở trang chi tiết của auction đó nhận cập nhật ngay (giá mới, người giữ giá) mà không cần reload.
- **Đề xuất kỹ thuật**: WebSocket/STOMP qua `spring-boot-starter-websocket` (đã có dependency, chưa cấu hình `WebSocketConfig`), topic dạng `/topic/auctions/{auctionId}`.
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-BID-06 — Chặn Seller tự đấu giá sản phẩm của mình
- **Acceptance Criteria**: Nếu `bid.userId == product.sellerId` → từ chối với lỗi rõ ràng.
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-BID-07 — Chặn đặt giá ngoài thời gian hợp lệ
- **Acceptance Criteria**: Đặt giá khi auction `PENDING`, `ENDED`/`COMPLETED`, hoặc `CANCELLED` → từ chối (400) với message rõ ràng.
- **Trạng thái**: ⛔ Chưa có (gộp logic trong FR-BID-01/02). **Ưu tiên**: P0

---

## 7. Module HOME — Trang chủ & Khám phá

### FR-HOME-01 — Thống kê tổng quan trang chủ
- **Mô tả**: Số phiên đấu giá hoàn thành, số người dùng, tổng giá trị giao dịch, tỷ lệ hài lòng.
- **Trạng thái**: ⛔ Chưa có (FE hiện **hard-code** số liệu tĩnh: "10,000+", "50,000+", "150 Tỷ VNĐ+", "99.9%"). **Ưu tiên**: P2 (không chặn MVP, có thể giữ hard-code ban đầu)

### FR-HOME-02 — Phiên đấu giá nổi bật trên trang chủ
- Xem FR-AUC-08. Component `Projects` hiện dùng dữ liệu tĩnh (`src/data.js`), cần chuyển sang gọi API thật.
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P2

---

## 8. Module STATIC — Trang tĩnh

### FR-STATIC-01 — Trang Giới thiệu (About)
- **Trạng thái**: ✅ Đã có (frontend-only, nội dung tĩnh, không cần API). **Ưu tiên**: —

### FR-STATIC-02 — Trang Liên hệ (Contact)
- **Mô tả**: Form gửi Họ tên/Email/Tiêu đề/Nội dung. Hiện gửi trực tiếp qua **EmailJS (client-side)**, không qua backend SwiftBid.
- **Trạng thái**: ✅ Đã có (nhưng phụ thuộc dịch vụ bên thứ 3 `@emailjs/browser`, không lưu lại lịch sử liên hệ trong hệ thống — có thể nâng cấp thành `POST /api/contact` để lưu DB, P2). **Ưu tiên**: —

---

## 9. Module NOTIF — Thông báo/Email

### FR-NOTIF-01 — Email đặt lại mật khẩu
- Xem FR-AUTH-04. Cần cấu hình SMTP thật trong `application.properties` (`spring.mail.*`), hiện chưa có.
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P1

### FR-NOTIF-02 — Email thông báo thắng/thua đấu giá (mở rộng)
- Khi phiên đấu giá kết thúc (từ FR-AUC-06), gửi email cho người thắng và (tùy chọn) người bán.
- **Trạng thái**: ⛔ Chưa có. **Ưu tiên**: P2

---

## 10. Module ADMIN — Quản trị hệ thống

### FR-ADMIN-01 — Quản lý người dùng (giới hạn quyền Admin)
- Bổ sung kiểm soát quyền (`@PreAuthorize("hasRole('ADMIN')")`) cho toàn bộ `UserController`.
- **Trạng thái**: 🟡 Một phần (CRUD có sẵn nhưng không giới hạn quyền — xem FR-USER-05). **Ưu tiên**: P0 (lỗ hổng bảo mật nghiêm trọng nếu deploy production)

### FR-ADMIN-02 — Quản lý toàn bộ sản phẩm/phiên đấu giá bất kể chủ sở hữu
- Admin có thể sửa/xóa mọi sản phẩm và phiên đấu giá, bỏ qua kiểm tra "chủ sở hữu" ở FR-PROD-04/05, FR-AUC-04/05.
- **Trạng thái**: ⛔ Chưa có (chưa có khái niệm kiểm tra chủ sở hữu để mà "bỏ qua"). **Ưu tiên**: P1

---

## 11. Bảng tổng hợp trạng thái theo Module

| Module | Tổng số FR | ✅ Đã có | 🟡 Một phần | ⛔ Chưa có |
|---|---|---|---|---|
| AUTH | 8 | 0 | 1 | 7 |
| USER | 5 | 0 | 1 | 4 |
| PRODUCT | 7 | 1 | 3 | 3 |
| AUCTION | 8 | 0 | 3 | 5 |
| BID | 7 | 0 | 1 | 6 |
| HOME | 2 | 0 | 0 | 2 |
| STATIC | 2 | 2 | 0 | 0 |
| NOTIF | 2 | 0 | 0 | 2 |
| ADMIN | 2 | 0 | 1 | 1 |
| **Tổng** | **43** | **3** | **10** | **30** |

**Kết luận**: ~70% FR liên quan trực tiếp tới nghiệp vụ đấu giá & bảo mật hiện **chưa được cài đặt** ở backend, mặc dù frontend đã dựng UI/flow đầy đủ như thể backend đã sẵn sàng. Xem `plan.md` để biết lộ trình lấp khoảng cách.

---

## 12. Yêu cầu phi chức năng liên quan trực tiếp tới FR (tóm tắt)

- **NFR-01 Concurrency**: Đặt giá đồng thời (FR-BID-02) không được mất dữ liệu/mất cập nhật (lost update), không deadlock.
- **NFR-02 Bảo mật**: Mật khẩu hash (bcrypt/argon2), JWT có thời hạn, không lộ `passwordHash` trong response JSON (hiện `User.java` dùng Lombok `@Getter` sinh getter cho cả `passwordHash` → **rò rỉ dữ liệu nhạy cảm qua REST**, cần `@JsonIgnore` hoặc DTO).
- **NFR-03 Toàn vẹn dữ liệu**: Ràng buộc FK & unique constraint đã có ở DB script, service cần xử lý lỗi FK/unique thành message thân thiện thay vì lỗi 500.
- **NFR-04 Khả năng mở rộng**: Thiết kế endpoint hỗ trợ phân trang/lọc/sắp xếp server-side cho danh sách lớn (FR-AUC-02).

---

## 13. Thuật ngữ (Glossary)

| Thuật ngữ | Giải thích |
|---|---|
| Auction | Phiên đấu giá gắn với đúng 1 Product |
| Bid | Một lượt đặt giá của User cho 1 Auction |
| Seller | Vai trò bổ sung cho User, cho phép tạo Product & Auction |
| Optimistic Locking | Cơ chế khóa lạc quan dùng cột `version`, phát hiện xung đột khi save |
| DTO | Data Transfer Object — object trung gian giữa Entity và JSON response, tránh lộ field nhạy cảm |
