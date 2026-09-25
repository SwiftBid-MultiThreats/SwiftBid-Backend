# SwiftBid — Test Plan (tests.md)

> Viết **trước khi cài đặt** (test-first). Mỗi test case tham chiếu `FR-ID` (`spec.md`) và `Task-ID` (`tasks.md`). Khi làm task nào, viết test tương ứng trước (đỏ), rồi cài đặt tới khi pass (xanh). Định dạng: **Given / When / Then**.
>
> Cấp độ test: **Unit** (service, không DB thật/mock repository), **Integration** (`@SpringBootTest` + DB test/Testcontainers, có gọi thật repository/transaction), **E2E** (gọi qua HTTP, `MockMvc`/`WebTestClient` hoặc FE thật).

---

## 1. Module AUTH

### TC-AUTH-01 — Đăng ký thành công (Unit + Integration) — FR-AUTH-01, T-1.2
- **Given**: chưa có user nào với `username="alice"`, `email="alice@mail.com"`.
- **When**: `POST /api/auth/register` với `{username:"alice", email:"alice@mail.com", password:"secret123"}`.
- **Then**: HTTP 200/201; response có `token` không rỗng; `user.username=="alice"`; DB có bản ghi `users` với `password_hash != "secret123"` (đã hash); user có role `USER`.

### TC-AUTH-02 — Đăng ký trùng username (Integration) — FR-AUTH-01
- **Given**: đã tồn tại user `username="alice"`.
- **When**: `POST /api/auth/register` với `username="alice"` (email khác).
- **Then**: HTTP 409/400, message chứa "username" hoặc tương đương; không tạo thêm bản ghi user.

### TC-AUTH-03 — Đăng ký trùng email (Integration) — FR-AUTH-01
- Tương tự TC-AUTH-02 nhưng trùng `email`.

### TC-AUTH-04 — Đăng nhập đúng thông tin (Integration) — FR-AUTH-02
- **Given**: user `alice/secret123` đã tồn tại (đã hash đúng).
- **When**: `POST /api/auth/login {username:"alice", password:"secret123"}`.
- **Then**: HTTP 200; `token` hợp lệ (decode được, đúng `sub=alice`).

### TC-AUTH-05 — Đăng nhập sai mật khẩu (Integration) — FR-AUTH-02
- **When**: login với password sai.
- **Then**: HTTP 401; response message không tiết lộ "username đúng, password sai" một cách chi tiết dễ bị dò (message chung).

### TC-AUTH-06 — Truy cập endpoint bảo vệ không có token (Integration) — FR-AUTH-06
- **When**: `POST /api/bids` không có header `Authorization`.
- **Then**: HTTP 401.

### TC-AUTH-07 — Truy cập endpoint bảo vệ với token hết hạn/giả (Integration) — FR-AUTH-06
- **When**: gửi token đã hết hạn hoặc ký sai secret.
- **Then**: HTTP 401.

### TC-AUTH-08 — Lấy danh sách roles (Integration) — FR-AUTH-07
- **Given**: user `alice` chỉ có role `USER`.
- **When**: `GET /api/account/roles` với token của alice.
- **Then**: HTTP 200; body `== ["USER"]`.

### TC-AUTH-09 — Nâng cấp Seller thành công (Integration) — FR-AUTH-08
- **Given**: user `alice` chỉ có role `USER`.
- **When**: `POST /api/account/become-seller` (token alice).
- **Then**: HTTP 200; sau đó `GET /api/account/roles` trả `["USER","SELLER"]`.

### TC-AUTH-10 — Nâng cấp Seller khi đã là Seller (idempotent) (Integration) — FR-AUTH-08
- **Given**: alice đã có role `SELLER`.
- **When**: gọi lại `POST /api/account/become-seller`.
- **Then**: không tạo role trùng lặp; HTTP 200 (hoặc 409 với message rõ — quyết định 1 hành vi và test đúng hành vi đó).

### TC-AUTH-11 — Quên mật khẩu với email tồn tại và không tồn tại đều trả cùng 1 message (Integration) — FR-AUTH-04
- **When**: `POST /api/auth/forgot-password` với email tồn tại, rồi với email không tồn tại.
- **Then**: cả 2 lần đều HTTP 200 với message giống nhau (chống dò email); với email tồn tại, có 1 bản ghi mới trong `password_reset_tokens`.

### TC-AUTH-12 — Đặt lại mật khẩu với token hợp lệ (Integration) — FR-AUTH-05
- **Given**: token reset hợp lệ, chưa hết hạn, chưa dùng.
- **When**: `POST /api/auth/reset-password {token, newPassword:"newpass123"}`.
- **Then**: HTTP 200; login lại bằng mật khẩu mới thành công; token đó dùng lại lần 2 → thất bại (đã bị đánh dấu used).

### TC-AUTH-13 — Đặt lại mật khẩu với token hết hạn (Integration) — FR-AUTH-05
- **Given**: token đã hết hạn (`expires_at` trong quá khứ).
- **When**: gọi reset-password.
- **Then**: HTTP 400, message "token không hợp lệ hoặc đã hết hạn".

---

## 2. Module USER

### TC-USER-01 — Lấy thông tin cá nhân (Integration) — FR-USER-01
- **When**: `GET /api/user-details/me` (token alice).
- **Then**: HTTP 200; body chứa `username,email,fullName,phoneNumber,address,bio,avatarUrl,role`; **không chứa `passwordHash`**.

### TC-USER-02 — Cập nhật thông tin cá nhân (Integration) — FR-USER-02
- **When**: `PUT /api/user-details/me {fullName:"Alice Nguyen", phoneNumber:"0900000000"}`.
- **Then**: HTTP 200; `GET /me` sau đó phản ánh đúng giá trị mới; `username` không đổi dù có gửi lên.

### TC-USER-03 — Upload avatar hợp lệ (Integration) — FR-USER-03
- **When**: `POST /api/user-details/me/avatar` với file `.png` 1MB.
- **Then**: HTTP 200; `avatarUrl` trả về truy cập được (HTTP 200 khi GET); `GET /me` phản ánh `avatarUrl` mới.

### TC-USER-04 — Upload avatar quá dung lượng (Unit/Integration) — FR-USER-03
- **When**: upload file 6MB.
- **Then**: HTTP 400, message "vượt quá 5MB".

### TC-USER-05 — Upload avatar sai định dạng (Integration) — FR-USER-03
- **When**: upload file `.pdf`.
- **Then**: HTTP 400.

### TC-USER-06 — User thường không được gọi CRUD `/api/users` của người khác (Integration) — FR-ADMIN-01
- **When**: alice (role `USER`) gọi `DELETE /api/users/{otherUserId}`.
- **Then**: HTTP 403.

---

## 3. Module PRODUCT

### TC-PROD-01 — Tạo sản phẩm khi là Seller (Integration) — FR-PROD-01
- **Given**: bob có role `SELLER`.
- **When**: `POST /api/products` multipart `{name,description,initialPrice:100000, image}` (token bob).
- **Then**: HTTP 201; `product.seller.id == bob.id` (server tự gán, **không** dùng giá trị client gửi nếu có gửi kèm `sellerId` khác); `imageUrl` không rỗng.

### TC-PROD-02 — Tạo sản phẩm khi chưa là Seller (Integration) — FR-PROD-01
- **Given**: alice chỉ có role `USER`.
- **When**: `POST /api/products` (token alice).
- **Then**: HTTP 403.

### TC-PROD-03 — Lấy sản phẩm của tôi (Integration) — FR-PROD-02
- **Given**: bob có 3 sản phẩm, carol (seller khác) có 2 sản phẩm.
- **When**: `GET /api/products/my-products` (token bob).
- **Then**: trả đúng 3 sản phẩm của bob, không lẫn sản phẩm của carol.

### TC-PROD-04 — Sửa sản phẩm không phải chủ sở hữu (Integration) — FR-PROD-04
- **When**: carol gọi `PUT /api/products/{bobProductId}`.
- **Then**: HTTP 403.

### TC-PROD-05 — Xóa sản phẩm đã có auction liên kết (Integration) — FR-PROD-05
- **Given**: sản phẩm X của bob đã có 1 auction.
- **When**: bob gọi `DELETE /api/products/{X}`.
- **Then**: HTTP 400 với message rõ ràng (không phải lỗi 500 do FK constraint).

### TC-PROD-06 — Tìm kiếm sản phẩm (Integration) — FR-PROD-06
- **Given**: sản phẩm "Đồng hồ cổ", "Bàn gỗ".
- **When**: `GET /api/products/search?q=đồng hồ`.
- **Then**: chỉ trả "Đồng hồ cổ" (không phân biệt hoa/thường, có dấu).

---

## 4. Module AUCTION

### TC-AUC-01 — Tạo auction cho sản phẩm của chính mình (Integration) — FR-AUC-01
- **Given**: bob sở hữu product X (chưa có auction).
- **When**: `POST /api/auctions` multipart `{productId:X, startTime:t1, endTime:t2 (t2>t1)}` (token bob).
- **Then**: HTTP 201; `status=="PENDING"`; `currentHighestBidAmount == product.initialPrice`.

### TC-AUC-02 — Tạo auction cho sản phẩm không phải của mình (Integration) — FR-AUC-01
- **When**: carol tạo auction cho product X của bob.
- **Then**: HTTP 403.

### TC-AUC-03 — Tạo auction với `endTime <= startTime` (Unit/Integration) — FR-AUC-01
- **Then**: HTTP 400.

### TC-AUC-04 — Tạo auction lần 2 cho cùng 1 sản phẩm (Integration) — FR-AUC-01
- **Given**: product X đã có 1 auction.
- **When**: tạo thêm auction cho X.
- **Then**: HTTP 409 (vi phạm `uk_product_id`), không phải lỗi 500.

### TC-AUC-05 — Lấy chi tiết auction (Integration) — FR-AUC-03
- **When**: `GET /api/auctions/{id}/details`.
- **Then**: body có `product`, `product.seller.username`, `auctionDetail` (có thể null nếu không nhập), `bidCount`.

### TC-AUC-06 — Scheduler chuyển PENDING→ACTIVE (Integration, dùng đồng hồ giả lập/Clock injectable) — FR-AUC-06
- **Given**: auction có `startTime` trong quá khứ gần, `status=PENDING`.
- **When**: chạy job scheduler (gọi trực tiếp method, không chờ `@Scheduled` thật trong test).
- **Then**: `status=="ACTIVE"`.

### TC-AUC-07 — Scheduler chuyển ACTIVE→COMPLETED (Integration) — FR-AUC-06
- **Given**: auction `status=ACTIVE`, `endTime` trong quá khứ.
- **When**: chạy job.
- **Then**: `status=="COMPLETED"`.

### TC-AUC-08 — Danh sách active auctions (Integration) — FR-AUC-07
- **Then**: chỉ trả các auction `status=="ACTIVE"`.

---

## 5. Module BID — trọng tâm (kèm test concurrency)

### TC-BID-01 — Đặt giá hợp lệ đầu tiên (Integration) — FR-BID-01, FR-BID-02
- **Given**: auction `status=ACTIVE`, `currentHighestBidAmount=100000`.
- **When**: alice đặt giá `120000`.
- **Then**: HTTP 201; `auction.currentHighestBidAmount==120000`; `auction.currentHighestBidder.id==alice.id`; có 1 bản ghi `bids` mới.

### TC-BID-02 — Đặt giá thấp hơn/bằng giá hiện tại (Integration) — FR-BID-01
- **Given**: `currentHighestBidAmount=120000`.
- **When**: alice đặt giá `120000` hoặc `100000`.
- **Then**: HTTP 400, không tạo bản ghi `bids`, không đổi `auction`.

### TC-BID-03 — Đặt giá khi auction chưa `ACTIVE` (PENDING) (Integration) — FR-BID-07
- **Then**: HTTP 400/409.

### TC-BID-04 — Đặt giá khi auction đã `COMPLETED`/`CANCELLED` (Integration) — FR-BID-07
- **Then**: HTTP 400/409.

### TC-BID-05 — Seller tự đặt giá sản phẩm của mình (Integration) — FR-BID-06
- **Given**: bob là seller của product X, auction ACTIVE.
- **When**: bob đặt giá cho auction của X.
- **Then**: HTTP 403.

### TC-BID-06 — `userId` lấy từ JWT, không tin body (Integration, kiểm thử bảo mật) — FR-BID-01
- **When**: alice gửi `POST /api/bids {auctionId, bidAmount, userId: bob.id}` (cố tình giả mạo `userId`).
- **Then**: bản ghi `bids` được tạo với `user==alice`, **không phải** `bob` — chứng minh server bỏ qua `userId` từ client.

### TC-BID-07 — **[QUAN TRỌNG] Concurrency: N bid đồng thời cho cùng 1 auction, chỉ giá cao nhất hợp lệ thắng** (Integration, đa luồng) — FR-BID-02
- **Given**: auction ACTIVE, `currentHighestBidAmount=100000`; chuẩn bị 20 user, mỗi user gửi 1 bid với giá tăng dần ngẫu nhiên (vd `100000 + i*1000` với `i=1..20`, xáo trộn thứ tự thực thi).
- **When**: dùng `ExecutorService` với 20 thread, `CountDownLatch` để bắn 20 request `placeBid` **cùng lúc** (đồng bộ thời điểm bắt đầu bằng latch), `awaitTermination`.
- **Then**:
  - Không có exception "mất tích" (uncaught) làm sập test — mọi request phải trả về success hoặc lỗi nghiệp vụ có kiểm soát (409 "vui lòng thử lại" nếu hết retry).
  - Sau khi tất cả hoàn tất: `auction.currentHighestBidAmount == MAX(tất cả bidAmount đã gửi thành công)`.
  - `auction.currentHighestBidder` là user đã gửi giá cao nhất đó.
  - Số bản ghi trong bảng `bids` == số request **thành công** (không thiếu, không thừa — chứng minh không lost update, không double-write).
  - (Đo thêm, không fail test) tỉ lệ request phải retry do `OptimisticLockException` — log lại để tham khảo hiệu năng.

### TC-BID-08 — **Concurrency: 2 bid cùng giá trị gửi đồng thời** (Integration, đa luồng) — FR-BID-02
- **Given**: 2 thread cùng gửi `bidAmount=150000` cho cùng auction tại cùng thời điểm (giá hợp lệ so với giá hiện tại).
- **When**: chạy đồng thời.
- **Then**: đúng 1 trong 2 được ghi nhận là "thắng" hợp lệ theo tiêu chí xử lý trước (first-committed-wins); yêu cầu nghiệp vụ bổ sung cần làm rõ: **bid thứ 2 với giá bằng giá vừa thắng có được coi là hợp lệ không?** → theo FR-BID-01 (`bidAmount > currentHighestBidAmount`), bid thứ 2 phải bị từ chối vì lúc nó chạy tới, giá cao nhất đã bằng nó. Test phải xác nhận đúng 1 bid thành công, 1 bid nhận lỗi "giá không đủ cao".

### TC-BID-09 — Lịch sử bid theo auction sắp xếp đúng thứ tự (Integration) — FR-BID-03
- **Given**: 3 bid tạo lần lượt cách nhau vài giây.
- **When**: `GET /api/bids/auction/{id}`.
- **Then**: thứ tự trả về giảm dần theo `timestamp` (mới nhất trước).

### TC-BID-10 — WebSocket broadcast khi có bid mới (Integration, STOMP test client) — FR-BID-05
- **Given**: client subscribe `/topic/auctions/{id}`.
- **When**: 1 bid hợp lệ được tạo qua `POST /api/bids`.
- **Then**: client nhận được message chứa `currentHighestBidAmount` mới trong vòng ví dụ 2 giây.

---

## 6. Module HOME / STATIC / NOTIF / ADMIN (tóm lược)

### TC-HOME-01 — Featured auctions trả đúng số lượng giới hạn (Integration) — FR-AUC-08
- **Then**: `size(response) <= N` (N cấu hình, vd 6), sắp theo tiêu chí đã chọn (bidCount hoặc featured flag).

### TC-ADMIN-01 — Admin có thể sửa/xóa sản phẩm không phải của mình (Integration) — FR-ADMIN-02
- **Given**: admin không sở hữu product X (của bob).
- **When**: admin gọi `PUT/DELETE /api/products/{X}`.
- **Then**: HTTP 200/204 (được phép, không bị chặn bởi kiểm tra chủ sở hữu).

### TC-NOTIF-01 — Email đặt lại mật khẩu được gửi (Integration, dùng `GreenMail`/mock `JavaMailSender`) — FR-NOTIF-01
- **When**: `forgot-password` với email tồn tại.
- **Then**: `JavaMailSender.send()` được gọi đúng 1 lần với `to==email`, nội dung chứa link chứa token.

---

## 7. Công cụ & Hạ tầng test đề xuất

| Loại | Công cụ |
|---|---|
| Unit | JUnit 5 + Mockito |
| Integration | `@SpringBootTest` + Testcontainers (MySQL) hoặc H2 tương thích (lưu ý: cần kiểm tra tương thích `ENUM`, JSON functions nếu MySQL-specific) |
| Concurrency | `ExecutorService` + `CountDownLatch` (như TC-BID-07/08), chạy trong `@SpringBootTest` với `TransactionTemplate` riêng cho từng thread (tránh dùng chung 1 transaction test) |
| E2E API | `MockMvc` hoặc `RestAssured` |
| WebSocket | Spring `WebSocketStompClient` test client |
| Mail | `GreenMail` (SMTP giả lập) hoặc mock `JavaMailSender` |
| FE (khi cần) | React Testing Library (đã có `@testing-library/*` trong `package.json`) cho các luồng form (Login/Register/CreateAuction) |

## 8. Tiêu chí hoàn thành (Definition of Done) cho mỗi Phase (đối chiếu `tasks.md`)

- Phase 0/1: TC-AUTH-01 → TC-AUTH-13 pass.
- Phase 2: TC-USER-01 → TC-USER-06, TC-PROD-01 → TC-PROD-06 pass.
- Phase 3: TC-AUC-01 → TC-AUC-08, **TC-BID-01 → TC-BID-09 pass, đặc biệt TC-BID-07/08 (concurrency) không flaky qua ≥20 lần chạy liên tiếp trong CI**.
- Phase 4: TC-BID-10, TC-NOTIF-01, TC-AUTH-11 → TC-AUTH-13 (phần email thật) pass.
- Phase 5: TC-HOME-01, TC-ADMIN-01 và các test còn lại pass.
