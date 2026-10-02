# KẾ HOẠCH TỔNG THỂ TÍCH HỢP CRM BÁN HÀNG B2B — STORY-BY-STORY

> **Mục đích:** Đây là bộ nhớ kỹ thuật và bảng theo dõi thực thi duy nhất cho quy trình tích hợp sản phẩm chính. Mỗi lần AI chỉ được làm **01 User Story**. Mỗi Acceptance Criteria (AC) được giữ **nguyên văn**, không tóm tắt, không viết gọn, không đổi nghĩa.
>
> **Backlog chuẩn:** 8 Sprint · 76 User Story · 350 Story Point · **249 Acceptance Criteria**.
>
> **Trạng thái khởi tạo:** Toàn bộ Story và AC đều quay về `TODO`. Không kế thừa kết luận DONE/PASS từ các lần chạy AI trước.

---

## 1. NGUYÊN TẮC THỰC THI MỚI — 01 PROMPT = 01 STORY

1. Mỗi lần thực thi chỉ được xử lý **một Story ID duy nhất** đã được chỉ định trong prompt.
2. Không được code trước Story kế tiếp, không tạo placeholder cho module tương lai, không thêm menu/link tới route chưa tồn tại.
3. Trước khi code phải đọc **nguyên văn User Story + toàn bộ AC** của Story đó trong sheet `4. Product Backlog` và đối chiếu với mục tương ứng trong file này.
4. AI không được tự đánh dấu Story là `DONE`. Sau khi code/build xong, Story tối đa chỉ được chuyển sang `READY_FOR_USER_QA`. Chỉ khi người dùng tự chạy web, kiểm tra và xác nhận rõ ràng thì lần chạy sau mới được đổi Story đó thành `DONE`.
5. Nếu người dùng phát hiện lỗi khi QA, chạy lại đúng Story đó ở chế độ `FIX_QA`; không chuyển Story tiếp theo cho đến khi lỗi được xử lý.
6. Sau mỗi Story đã được người dùng xác nhận đạt, nên tạo **một Git checkpoint/commit riêng** để có thể quay lại chính xác nếu Story sau gây regression.

### Trạng thái Story

`TODO` → `IN_PROGRESS` → `READY_FOR_USER_QA` → `DONE`

Ngoài ra có thể dùng `BLOCKED` khi tồn tại trở ngại thật sự không thể giải quyết mà không vi phạm Source of Truth hoặc canonical schema.

### Trạng thái AC

`TODO` · `IN_PROGRESS` · `READY_FOR_USER_QA` · `PASS` · `FAIL` · `BLOCKED`

---

## 2. SOURCE OF TRUTH — THỨ TỰ BẮT BUỘC

Khi có mâu thuẫn, dùng thứ tự sau:

1. **Acceptance Criteria trong sheet `4. Product Backlog` của `PLAN/HỆ THỐNG QUẢN LÝ KHÁCH HÀNG.xlsx`.**
2. `DATABASE_RULES.md`.
3. `CODING_RULES.md`.
4. `src/main/resources/db/001_crm_ban_hang_full_schema_8_sprints.sql`.
5. `PLAN/Phân Tích Yêu Cầu Nghiệp Vụ CRM.docx`.
6. `README.md`.
7. `PLAN/infoTechnology.md`.
8. Code hiện tại đã được người dùng QA và giữ lại.
9. File `MASTER_INTEGRATION/Ke_Hoach.md` này.

**Cấm:** dùng tên module, code cũ hoặc suy luận của AI để ghi đè ý nghĩa Story/AC trong Product Backlog.

---

## 3. CANONICAL DATABASE — BẤT BIẾN

File duy nhất: `src/main/resources/db/001_crm_ban_hang_full_schema_8_sprints.sql`.

Trong quá trình làm Story, **không được**:

- ALTER TABLE.
- DROP TABLE.
- RENAME TABLE/COLUMN.
- ADD/DROP COLUMN.
- đổi datatype.
- đổi foreign key.
- tạo table mới ngoài schema.
- tạo field đồng nghĩa để né schema.
- tạo file SQL Story riêng để thay canonical schema.
- sửa nội dung canonical SQL.

Code phải thích nghi với schema. Nếu một AC thật sự không thể triển khai với canonical schema, dừng Story và ghi `BLOCKED`; không tự sửa DB.

---

## 4. KIẾN TRÚC VÀ STACK CỐ ĐỊNH

```text
Browser
→ JSP / HTML5 / CSS3 / Vanilla JavaScript
→ Filter
→ Servlet / Controller
→ Service
→ DAO / JDBC PreparedStatement
→ MySQL 8.4 LTS
```

- Java 25 LTS.
- Jakarta Servlet 6.1.x; **không dùng `javax.servlet.*`**.
- JSP / Jakarta Pages 4.0.x.
- Tomcat 11.0.x.
- Maven 3.9.x, WAR `crm-ban-hang.war`.
- Frontend: JSP + CSS thuần + Vanilla JS; không tự chuyển React/Vue/Angular/Tailwind/Bootstrap.
- Không tự đổi dependency/version. Chỉ thêm thư viện khi Story hiện tại thật sự cần và phải ghi rõ lý do.
- `PLAN/infoTechnology.md` chỉ cập nhật khi **công nghệ/dependency thực tế thay đổi**, không cập nhật theo mỗi Story.

---

## 5. QUY TẮC CHỐNG LẶP LẠI CÁC LỖI ĐÃ GẶP

1. Không tự tạo Story không tồn tại (ví dụ `S1-11`). Story ID và ý nghĩa phải lấy trực tiếp từ Product Backlog.
2. Không tóm tắt AC thành một câu rồi coi như đã làm xong. Mỗi AC phải có trạng thái và evidence riêng.
3. Không tự báo `DONE` chỉ vì class/JSP tồn tại hoặc `mvn test` PASS. Phải chờ người dùng QA.
4. Không hiển thị menu, dashboard card hoặc link đến module của Sprint tương lai nếu route/chức năng đó chưa được triển khai; tuyệt đối không để navigation bình thường dẫn tới 404.
5. `AuthFilter`/`RBACFilter` không được chặn static assets `/assets/**`; CSS/JS/image phải load được HTTP 200 và đúng MIME.
6. Không hiển thị tài khoản demo, mật khẩu, token, App Password, SMTP secret hoặc DB password trên UI, log hoặc HTML.
7. File chứa secret cục bộ (ví dụ `db.properties`) phải nằm ngoài Git; nếu cần file mẫu thì dùng `.example` không có secret.
8. Nếu Story có email: phải phân biệt rõ MOCK và REAL. Không được báo email PASS khi cấu hình đang mock hoặc chưa nhận được email thật trong QA.
9. Thời hạn/token/status phải lấy đúng AC; không tự đổi 30 phút thành 24 giờ hay giá trị khác.
10. UI phải dùng được thật nhưng không “trang trí AI”: ưu tiên text, typography, spacing; không emoji hàng loạt, không icon ở mọi card/menu, không gradient/glow vô ích.
11. Không tạo route alias, getter alias hoặc tên attribute song song chỉ để vá nhanh nếu có thể sửa contract thống nhất tại nguồn. Trước tiên phải xác định tên canonical rồi sửa các nơi sử dụng.
12. Không sửa business logic ngoài phạm vi Story hiện tại chỉ để làm UI đẹp hoặc làm test dễ PASS.
13. Không tạo dữ liệu hard-code để giả chức năng backend khi DB/DAO thật đã hoặc phải tồn tại.
14. Không tự commit, push hoặc merge. Git chỉ thực hiện khi người dùng yêu cầu sau QA.
15. Mỗi Story phải regression các chức năng đã được người dùng xác nhận DONE trước đó; nếu làm hỏng phải sửa trước khi READY_FOR_USER_QA.

---

## 6. DEFINITION OF READY CHO MỖI STORY

Trước khi sửa code, AI phải chứng minh đã:

- [ ] đọc đúng Story ID trong Product Backlog.
- [ ] đọc nguyên văn User Story.
- [ ] đọc đủ từng AC, không bỏ sót.
- [ ] xác định Story thuộc Sprint/Epic nào.
- [ ] đọc canonical schema liên quan.
- [ ] tìm toàn bộ Model/DAO/Service/Servlet/JSP/Filter/Util đã có để tránh duplicate.
- [ ] đọc các quyết định kỹ thuật đã được người dùng QA từ Story trước.
- [ ] xác định rõ phần nào là FE, BE, DB integration và test của Story hiện tại.

Nếu chưa đủ các mục trên: **không code**.

---

## 7. DEFINITION OF DONE — USER GATE

AI **không được tự chốt DONE**. Sau khi thực thi, AI chỉ đưa Story về `READY_FOR_USER_QA` khi:

- [ ] toàn bộ AC đã có code tương ứng.
- [ ] Maven test PASS.
- [ ] Maven package PASS.
- [ ] không sửa canonical schema.
- [ ] không có secret mới bị track.
- [ ] không tạo route/menu tương lai ngoài Story.
- [ ] các Story DONE trước đó không bị regression rõ ràng.
- [ ] đã ghi evidence cụ thể cho từng AC.

Sau đó người dùng tự deploy Tomcat, kiểm tra giao diện + chức năng + DB/email nếu liên quan. Chỉ sau khi người dùng xác nhận, lần chạy kế tiếp mới chuyển Story đó từ `READY_FOR_USER_QA` sang `DONE` và từng AC từ `READY_FOR_USER_QA` sang `PASS`.

---

## 8. KẾ HOẠCH 4 PHASE / 8 SPRINT — NHƯNG THỰC THI TỪNG STORY

### PHASE 1 — TUẦN 1

- Sprint 1 + Sprint 2.
- Tổng: **20 Story / 85 Point**.
- Mỗi Story là **một prompt riêng**, theo đúng thứ tự backlog trừ khi người dùng chỉ định khác.

### PHASE 2 — TUẦN 2

- Sprint 3 + Sprint 4.
- Tổng: **18 Story / 87 Point**.
- Mỗi Story là **một prompt riêng**, theo đúng thứ tự backlog trừ khi người dùng chỉ định khác.

### PHASE 3 — TUẦN 3

- Sprint 5 + Sprint 6.
- Tổng: **18 Story / 89 Point**.
- Mỗi Story là **một prompt riêng**, theo đúng thứ tự backlog trừ khi người dùng chỉ định khác.

### PHASE 4 — TUẦN 4

- Sprint 7 + Sprint 8.
- Tổng: **20 Story / 89 Point**.
- Mỗi Story là **một prompt riêng**, theo đúng thứ tự backlog trừ khi người dùng chỉ định khác.

---

## 9. PRODUCT BACKLOG CHI TIẾT — 76 STORY / 249 AC NGUYÊN VĂN

## SPRINT 1 — Tài khoản và phân quyền theo dữ liệu sở hữu   ·   10 story · 42 point

**Sprint Goal (nguyên văn):** Quản trị viên tạo được tài khoản cho toàn khối kinh doanh, và mô hình phân quyền theo dữ liệu sở hữu hoạt động đúng ngay từ sprint đầu — đây là story quan trọng nhất của cả dự án.

### S1-01 — 5 point · Must · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn đăng nhập bằng email công ty và mật khẩu, để truy cập được danh mục khách hàng của mình một cách an toàn.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-01-AC1** — Đăng nhập đúng thì vào được trang chủ tương ứng với vai trò [READY_FOR_USER_QA]
- [ ] **S1-01-AC2** — Sai thông tin hiển thị thông báo chung, không tiết lộ email có tồn tại hay không [READY_FOR_USER_QA]
- [ ] **S1-01-AC3** — Khoá tạm 15 phút sau 5 lần sai liên tiếp [READY_FOR_USER_QA]

**Theo dõi thực thi:**

- Story status: `READY_FOR_USER_QA`
- FE: `READY_FOR_USER_QA`
- BE: `READY_FOR_USER_QA`
- DB integration: `READY_FOR_USER_QA`
- Automated test: `READY_FOR_USER_QA`
- Manual QA của người dùng: `TODO`
- Evidence:
  - S1-01-AC1: `AuthServiceTest.testS1_01_AC1_DangNhapDung_ThanhCongVaTraVeVaiTro` (PASS), `LoginServletTest.testDoPost_DangNhapThanhCong_ChuyenHuongTrangChu` (PASS), `AuthDatabaseIntegrationTest.testDangNhapDung_MySQL` (PASS). Đăng nhập đúng điều hướng tới `/home` với view tùy biến theo vai trò.
  - S1-01-AC2: `AuthServiceTest.testS1_01_AC2_EmailKhongTonTai_ThongBaoChung` (PASS), `AuthServiceTest.testS1_01_AC2_MatKhauSai_ThongBaoChungDongNhat` (PASS), `LoginServletTest.testDoPost_DangNhapThatBai_TraVeFormVoiThongBao` (PASS), `AuthDatabaseIntegrationTest.testSaiThongTin_MySQL` (PASS). Email không tồn tại hoặc sai mật khẩu đều trả về cùng một thông báo chung: "Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại."
  - S1-01-AC3: `AuthServiceTest.testS1_01_AC3_DangNhapSai5Lan_KhoaTam15Phut` (PASS), `AuthServiceTest.testS1_01_AC3_DangBiKhoaTam_TuChoiDangNhap` (PASS), `AuthServiceTest.testS1_01_AC3_SauKhiHetHanKhoa_DangNhapDung_ThanhCongVaReset` (PASS), `AuthDatabaseIntegrationTest.testKhoaTam15PhutSau5LanSai_MySQL` (PASS). Sau 5 lần nhập sai liên tiếp, ghi nhận `khoa_den = NOW() + 15 phút` vào MySQL và từ chối các phiên đăng nhập trong thời gian khóa.
  - Maven tests: 16/16 test PASS (`mvn clean test`).
  - Maven package: WAR `target/crm-ban-hang.war` build thành công (`mvn clean package`).
- File tạo/sửa:
  - Tạo mới: `DatabaseConfig.java`, `NguoiDung.java`, `VaiTro.java`, `DangNhapResult.java`, `CauHinhHeThongDAO.java`, `NguoiDungDAO.java`, `PhienDangNhapDAO.java`, `AuthService.java`, `EncodingFilter.java`, `AuthFilter.java`, `LoginServlet.java`, `HomeServlet.java`, `LogoutServlet.java`, `PasswordUtil.java`, `login.jsp`, `home/index.jsp`, `style.css`, `PasswordUtilTest.java`, `AuthServiceTest.java`, `LoginServletTest.java`, `AuthDatabaseIntegrationTest.java`.
  - Sửa: `pom.xml`, `PLAN/infoTechnology.md`, `MASTER_INTEGRATION/Ke_Hoach.md`.
- Lỗi QA / ghi chú: Chờ người dùng deploy WAR lên Tomcat và tiến hành manual QA.

---

### S1-02 — 3 point · Must · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn duy trì phiên đăng nhập và đăng xuất an toàn, để không mất ghi chú cuộc gặp đang nhập dở khi đang ngồi ở quán cà phê.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-02-AC1** — Phiên được gia hạn tự động khi còn hoạt động
- [ ] **S1-02-AC2** — Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server
- [ ] **S1-02-AC3** — Phiên hết hạn đưa về trang đăng nhập kèm thông báo rõ ràng

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-03 — 5 point · Must · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn đặt lại mật khẩu khi quên thông qua email, để tự lấy lại quyền truy cập khi đang đi gặp khách.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-03-AC1** — Nhập email nhận được liên kết đặt lại có hiệu lực 30 phút
- [ ] **S1-03-AC2** — Liên kết chỉ dùng được một lần
- [ ] **S1-03-AC3** — Email không tồn tại vẫn hiển thị cùng một thông báo

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-04 — 2 point · Must · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn đổi mật khẩu khi đang đăng nhập, để chủ động bảo vệ danh mục khách hàng của mình.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-04-AC1** — Bắt buộc nhập mật khẩu hiện tại
- [ ] **S1-04-AC2** — Mật khẩu mới tối thiểu 8 ký tự, có chữ và số
- [ ] **S1-04-AC3** — Đổi xong thu hồi các phiên đăng nhập khác

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-05 — 8 point · Must · EP-01

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn có phân quyền vừa theo vai trò vừa theo dữ liệu sở hữu, để nhân viên chỉ thấy khách của mình, trưởng nhóm thấy toàn nhóm, còn tôi thấy tất cả.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-05-AC1** — Ba phạm vi dữ liệu: của tôi, của nhóm tôi, tất cả — áp dụng cho khách hàng, cơ hội, hoạt động, báo giá
- [ ] **S1-05-AC2** — Mọi truy vấn danh sách tự động lọc theo phạm vi, kể cả tìm kiếm và xuất Excel
- [ ] **S1-05-AC3** — Truy cập bản ghi ngoài phạm vi hiển thị thông báo tiếng Việt rõ ràng
- [ ] **S1-05-AC4** — Có kiểm thử tự động chứng minh nhân viên A không đọc được khách hàng của nhân viên B

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-06 — 5 point · Must · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn thấy menu điều hướng đúng theo quyền của mình, để không bị rối bởi những chức năng mình không được dùng.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-06-AC1** — Mục menu không thuộc quyền thì không hiển thị
- [ ] **S1-06-AC2** — Hiển thị tên, vai trò và nhóm kinh doanh đang thuộc về
- [ ] **S1-06-AC3** — Dùng được thuận tiện trên màn hình 360px

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-07 — 1 point · Should · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn nhận thông báo rõ ràng khi truy cập nhầm chỗ hoặc không đủ quyền, để biết mình nên làm gì tiếp thay vì gặp một trang trắng.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-07-AC1** — Trang báo lỗi dùng chung giao diện ứng dụng
- [ ] **S1-07-AC2** — Mỗi trang lỗi có một hành động gợi ý để quay lại luồng làm việc

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-08 — 8 point · Must · EP-01

**Vai trò (nguyên văn):** Quản trị hệ thống

**User Story (nguyên văn):** Là Quản trị hệ thống, tôi muốn tạo, sửa và tìm kiếm tài khoản người dùng, để cấp quyền cho nhân viên kinh doanh mới ngay ngày đầu nhận địa bàn.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-08-AC1** — Tạo tài khoản gửi email kích hoạt kèm mật khẩu tạm
- [ ] **S1-08-AC2** — Email trùng bị từ chối kèm thông báo cụ thể
- [ ] **S1-08-AC3** — Tìm theo tên, email, nhóm; lọc theo vai trò và trạng thái
- [ ] **S1-08-AC4** — Danh sách phân trang, mặc định 20 dòng

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-09 — 3 point · Must · EP-01

**Vai trò (nguyên văn):** Quản trị hệ thống

**User Story (nguyên văn):** Là Quản trị hệ thống, tôi muốn gán vai trò và gắn người dùng vào nhóm kinh doanh, để cây tổ chức quyết định đúng phạm vi dữ liệu mỗi người nhìn thấy.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-09-AC1** — Một người dùng có thể giữ nhiều vai trò cùng lúc
- [ ] **S1-09-AC2** — Người giữ vai trò Trưởng nhóm phải được gán một nhóm cụ thể
- [ ] **S1-09-AC3** — Không thể tự thu hồi vai trò quản trị của chính mình

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S1-10 — 2 point · Must · EP-01

**Vai trò (nguyên văn):** Quản trị hệ thống

**User Story (nguyên văn):** Là Quản trị hệ thống, tôi muốn khoá tài khoản và bàn giao dữ liệu khi nhân viên nghỉ, để khách hàng và cơ hội không bị mất chủ khi người phụ trách rời công ty.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S1-10-AC1** — Tài khoản bị khoá không đăng nhập được và bị thu hồi phiên đang mở
- [ ] **S1-10-AC2** — Bắt buộc chọn người tiếp nhận toàn bộ khách hàng và cơ hội trước khi khoá
- [ ] **S1-10-AC3** — Việc bàn giao được ghi nhật ký, dữ liệu không bị mất chủ sở hữu

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 2 — Danh mục bán hàng và cấu hình pipeline   ·   10 story · 43 point

**Sprint Goal (nguyên văn):** Hệ thống biết công ty đang bán những gì với giá niêm yết bao nhiêu, và một thương vụ đi qua những giai đoạn nào với xác suất thắng ra sao.

### S2-01 — 5 point · Should · EP-01

**Vai trò (nguyên văn):** Quản trị hệ thống

**User Story (nguyên văn):** Là Quản trị hệ thống, tôi muốn nhập danh sách người dùng hàng loạt từ tệp Excel, để tạo tài khoản cho cả khối kinh doanh trong vài phút.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-01-AC1** — Tải được tệp mẫu
- [ ] **S2-01-AC2** — Xem trước và báo lỗi theo từng dòng trước khi nhập
- [ ] **S2-01-AC3** — Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-02 — 3 point · Must · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn xem và cập nhật hồ sơ cá nhân, để chữ ký email của tôi luôn đúng khi gửi báo giá cho khách.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-02-AC1** — Sửa được họ tên, số điện thoại, chữ ký email
- [ ] **S2-02-AC2** — Không tự đổi được email, nhóm và vai trò
- [ ] **S2-02-AC3** — Kiểm tra định dạng số điện thoại Việt Nam

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-03 — 2 point · Could · EP-01

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn tải lên ảnh đại diện, để đồng nghiệp nhận ra ai đang phụ trách khách hàng khi xem hồ sơ.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-03-AC1** — Chấp nhận JPG/PNG tối đa 2MB
- [ ] **S2-03-AC2** — Ảnh được cắt vuông và tạo bản thu nhỏ

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-04 — 3 point · Must · EP-01

**Vai trò (nguyên văn):** Quản trị hệ thống

**User Story (nguyên văn):** Là Quản trị hệ thống, tôi muốn xem nhật ký thay đổi trên dữ liệu nhạy cảm, để truy được ai đã sửa chiết khấu hoặc chỉ tiêu khi cuối quý số liệu không khớp.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-04-AC1** — Ghi lại mọi thay đổi trên chiết khấu, chỉ tiêu, quyền sở hữu dữ liệu và vai trò người dùng
- [ ] **S2-04-AC2** — Mỗi bản ghi có người thực hiện, thời điểm, giá trị trước và sau
- [ ] **S2-04-AC3** — Lọc theo người dùng, loại đối tượng, khoảng thời gian

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-05 — 8 point · Must · EP-02

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn quản lý danh mục sản phẩm dịch vụ và bảng giá niêm yết, để mọi báo giá đều xuất phát từ một bảng giá chuẩn thay vì giá tự nghĩ.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-05-AC1** — Khai báo mã, tên, loại (sản phẩm một lần hoặc dịch vụ thuê bao), đơn vị tính, giá niêm yết, giá sàn
- [ ] **S2-05-AC2** — Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không
- [ ] **S2-05-AC3** — Giá vốn chỉ Giám đốc kinh doanh xem và sửa được
- [ ] **S2-05-AC4** — Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-06 — 5 point · Must · EP-02

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn khai báo cơ cấu tổ chức kinh doanh, để phạm vi dữ liệu của trưởng nhóm bám đúng cây tổ chức thật.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-06-AC1** — Nhóm kinh doanh có cấu trúc cây, mỗi nhóm có một trưởng nhóm
- [ ] **S2-06-AC2** — Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm
- [ ] **S2-06-AC3** — Cây tổ chức này quyết định phạm vi dữ liệu mà Trưởng nhóm nhìn thấy
- [ ] **S2-06-AC4** — Khai báo khu vực địa lý và gán khu vực cho nhóm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-07 — 5 point · Must · EP-02

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn khai báo các danh mục dùng chung của bán hàng, để cả khối gọi tên nguồn lead và ngành nghề giống nhau để báo cáo gộp được.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-07-AC1** — Ngành nghề khách hàng, quy mô doanh nghiệp, nguồn lead, loại hoạt động
- [ ] **S2-07-AC2** — Giá trị đang được tham chiếu thì không xoá được
- [ ] **S2-07-AC3** — Sắp xếp được thứ tự hiển thị

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-08 — 2 point · Should · EP-02

**Vai trò (nguyên văn):** Quản trị hệ thống

**User Story (nguyên văn):** Là Quản trị hệ thống, tôi muốn khai báo trường tuỳ chỉnh cho khách hàng và cơ hội, để đưa được những cột mà nhân viên đang tự thêm trong Excel vào hệ thống.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-08-AC1** — Thêm trường kiểu văn bản, số, ngày, danh sách chọn
- [ ] **S2-08-AC2** — Đặt được trường là bắt buộc hay không
- [ ] **S2-08-AC3** — Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-09 — 5 point · Must · EP-02

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn cấu hình các giai đoạn pipeline và xác suất thắng, để con số dự báo doanh số có cơ sở thay vì dựa vào cảm nhận.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-09-AC1** — Khai báo chuỗi giai đoạn, ví dụ Tiếp cận → Xác định nhu cầu → Đề xuất giải pháp → Báo giá → Đàm phán → Chốt
- [ ] **S2-09-AC2** — Mỗi giai đoạn có xác suất thắng mặc định dùng để tính dự báo
- [ ] **S2-09-AC3** — Khai báo điều kiện bắt buộc để rời một giai đoạn, ví dụ phải có ít nhất một cuộc gặp
- [ ] **S2-09-AC4** — Thay đổi cấu hình không làm hỏng cơ hội đang chạy

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S2-10 — 5 point · Must · EP-02

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn khai báo danh mục lý do thắng thua và đối thủ, để năm sau không thua lại đúng chỗ đã thua năm nay.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S2-10-AC1** — Danh sách lý do thắng và lý do thua khai báo riêng
- [ ] **S2-10-AC2** — Danh sách đối thủ cạnh tranh
- [ ] **S2-10-AC3** — Đây là dữ liệu bắt buộc khi đóng một cơ hội ở Sprint 5

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 3 — Khách hàng, người liên hệ và trang 360   ·   9 story · 42 point

**Sprint Goal (nguyên văn):** Mỗi khách hàng doanh nghiệp có một trang duy nhất gom đủ thông tin mà cả kinh doanh lẫn chăm sóc khách hàng cùng dùng — người mới tiếp nhận nắm được bối cảnh trong năm phút.

### S3-01 — 8 point · Must · EP-03

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn quản lý hồ sơ khách hàng doanh nghiệp, để có một danh sách khách chuẩn thay vì file Excel riêng của từng người.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-01-AC1** — Khai báo tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ, người sở hữu
- [ ] **S3-01-AC2** — Mã số thuế nếu có thì phải là duy nhất
- [ ] **S3-01-AC3** — Khách hàng có trạng thái: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác
- [ ] **S3-01-AC4** — Nhân viên chỉ thấy khách hàng mình sở hữu; trưởng nhóm thấy toàn nhóm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-02 — 8 point · Must · EP-03

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn quản lý người liên hệ và vai trò của họ trong quyết định mua, để biết phải thuyết phục ai và ai là người có thể cản thương vụ.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-02-AC1** — Mỗi khách hàng có nhiều người liên hệ, mỗi người có chức danh, email, số điện thoại
- [ ] **S3-02-AC2** — Đánh dấu vai trò trong quyết định mua: người quyết định, người ảnh hưởng, người dùng cuối, người cản trở
- [ ] **S3-02-AC3** — Đánh dấu một người là đầu mối chính
- [ ] **S3-02-AC4** — Một người liên hệ chuyển sang công ty khác thì gắn lại được sang khách hàng mới, giữ nguyên lịch sử

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-03 — 5 point · Must · EP-03

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn xem trang 360 của một khách hàng, để nắm toàn bộ bối cảnh trước cuộc gặp mà không phải mở năm chỗ khác nhau.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-03-AC1** — Một trang gom: thông tin công ty, danh sách người liên hệ, cơ hội đang mở và đã đóng, dòng thời gian hoạt động, tệp đính kèm
- [ ] **S3-03-AC2** — Hiển thị tổng giá trị đã ký và giá trị cơ hội đang mở
- [ ] **S3-03-AC3** — Tải xong dưới 1,5 giây với 500 hoạt động

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-04 — 5 point · Must · EP-03

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn được cảnh báo và gộp khách hàng trùng, để hai nhân viên không cùng chào một công ty mà không biết nhau.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-04-AC1** — Phát hiện trùng theo mã số thuế, tên công ty gần giống và website
- [ ] **S3-04-AC2** — Hiển thị so sánh cạnh nhau trước khi gộp
- [ ] **S3-04-AC3** — Gộp giữ lại toàn bộ người liên hệ, cơ hội và hoạt động của cả hai bản ghi
- [ ] **S3-04-AC4** — Chỉ Trưởng nhóm trở lên được thực hiện gộp

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-05 — 3 point · Should · EP-03

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn khai báo quan hệ công ty mẹ và công ty con, để nhìn được tổng giá trị của cả tập đoàn chứ không chỉ từng pháp nhân.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-05-AC1** — Gắn một khách hàng làm công ty con của khách hàng khác
- [ ] **S3-05-AC2** — Trang công ty mẹ hiển thị tổng giá trị hợp đồng của cả nhóm công ty

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-06 — 3 point · Should · EP-03

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn nhập danh sách khách hàng hàng loạt từ Excel, để đưa danh mục khách đang có vào hệ thống mà không gõ lại.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-06-AC1** — Tải được tệp mẫu, xem trước và báo lỗi theo từng dòng
- [ ] **S3-06-AC2** — Bản ghi trùng được đánh dấu rõ trong bản xem trước để chọn bỏ qua hoặc cập nhật

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-07 — 2 point · Must · EP-03

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn tìm kiếm và lọc khách hàng theo nhiều điều kiện, để dựng nhanh danh sách khách cần gọi trong tuần.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-07-AC1** — Lọc theo trạng thái, ngành nghề, quy mô, khu vực, người sở hữu
- [ ] **S3-07-AC2** — Tìm theo tên, mã số thuế, số điện thoại người liên hệ
- [ ] **S3-07-AC3** — Lưu lại được bộ lọc hay dùng

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-08 — 5 point · Should · EP-03

**Vai trò (nguyên văn):** Chăm sóc khách hàng

**User Story (nguyên văn):** Là Chăm sóc khách hàng, tôi muốn ghi nhận yêu cầu hỗ trợ sau bán và gắn cờ khách có rủi ro rời bỏ, để giữ được khách hiện có thay vì chỉ chạy theo khách mới.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-08-AC1** — Ghi nhận yêu cầu hỗ trợ với mức độ ưu tiên, người xử lý và trạng thái
- [ ] **S3-08-AC2** — Khách có nhiều yêu cầu chưa xử lý được gắn cờ rủi ro tự động
- [ ] **S3-08-AC3** — Cờ rủi ro hiển thị trên trang 360 và cảnh báo cho nhân viên kinh doanh phụ trách

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S3-09 — 3 point · Should · EP-03

**Vai trò (nguyên văn):** Chăm sóc khách hàng

**User Story (nguyên văn):** Là Chăm sóc khách hàng, tôi muốn xem danh sách khách hàng cần chăm sóc định kỳ, để không để khách đã ký hợp đồng bị bỏ quên tới lúc gia hạn.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S3-09-AC1** — Danh sách khách chưa có tương tác nào trong N ngày, N cấu hình được
- [ ] **S3-09-AC2** — Sắp xếp theo giá trị hợp đồng giảm dần
- [ ] **S3-09-AC3** — Đánh dấu đã liên hệ ngay trên danh sách

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 4 — Lead: thu thập, chấm điểm và phân bổ   ·   9 story · 45 point

**Sprint Goal (nguyên văn):** Một lead để lại thông tin trên website được hệ thống chấm điểm, phân bổ đúng người trong 5 phút, và không lead nào nằm quá SLA mà không ai gọi lại.

### S4-01 — 5 point · Must · EP-04

**Vai trò (nguyên văn):** Nhân viên Marketing

**User Story (nguyên văn):** Là Nhân viên Marketing, tôi muốn thu thập lead từ biểu mẫu nhúng trên website, để mọi lead vào thẳng hệ thống thay vì nằm trong hộp thư chung.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-01-AC1** — Sinh mã nhúng cho một biểu mẫu, dán được vào website bất kỳ
- [ ] **S4-01-AC2** — Biểu mẫu gồm họ tên, email, số điện thoại, công ty, nhu cầu quan tâm
- [ ] **S4-01-AC3** — Có chống spam và giới hạn tần suất theo địa chỉ IP
- [ ] **S4-01-AC4** — Gửi thành công tạo lead ở trạng thái Mới và gắn đúng nguồn của biểu mẫu

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-02 — 5 point · Must · EP-04

**Vai trò (nguyên văn):** Nhân viên Marketing

**User Story (nguyên văn):** Là Nhân viên Marketing, tôi muốn tạo lead thủ công và nhập lead hàng loạt từ Excel, để đưa danh sách thu được từ hội thảo vào hệ thống ngay hôm sau.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-02-AC1** — Nhập tay một lead từ sự kiện hoặc danh thiếp
- [ ] **S4-02-AC2** — Nhập hàng loạt có tệp mẫu, xem trước và báo lỗi theo từng dòng
- [ ] **S4-02-AC3** — Mọi lead nhập vào đều bắt buộc có nguồn

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-03 — 5 point · Should · EP-04

**Vai trò (nguyên văn):** Nhân viên Marketing

**User Story (nguyên văn):** Là Nhân viên Marketing, tôi muốn theo dõi lead theo từng chiến dịch, để đo được chiến dịch nào thực sự ra doanh thu chứ không chỉ ra nhiều lead.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-03-AC1** — Khai báo chiến dịch với ngân sách, thời gian chạy, kênh
- [ ] **S4-03-AC2** — Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng
- [ ] **S4-03-AC3** — Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-04 — 5 point · Must · EP-04

**Vai trò (nguyên văn):** Nhân viên Marketing

**User Story (nguyên văn):** Là Nhân viên Marketing, tôi muốn được cảnh báo và gộp lead trùng, để không để hai nhân viên cùng gọi một người trong một buổi sáng.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-04-AC1** — Phát hiện trùng theo email, số điện thoại và tên công ty
- [ ] **S4-04-AC2** — Lead trùng với khách hàng đã có được gợi ý gắn thẳng vào khách hàng đó
- [ ] **S4-04-AC3** — Gộp giữ nguyên lịch sử của cả hai bản ghi

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-05 — 8 point · Must · EP-04

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn cấu hình chấm điểm lead theo tiêu chí khai báo được, để nhân viên gọi những lead có khả năng nhất trước.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-05-AC1** — Khai báo tiêu chí và số điểm: ngành nghề phù hợp, quy mô doanh nghiệp, nguồn, mức độ quan tâm
- [ ] **S4-05-AC2** — Điểm được tính lại tự động khi thông tin lead thay đổi
- [ ] **S4-05-AC3** — Phân loại Nóng, Ấm, Lạnh theo ngưỡng điểm khai báo được
- [ ] **S4-05-AC4** — Điểm chỉ để sắp xếp ưu tiên, không tự động loại lead

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-06 — 8 point · Must · EP-04

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn cấu hình quy tắc phân bổ lead tự động, để lead tới tay người phụ trách trong vài phút thay vì chờ họp giao ban.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-06-AC1** — Phân bổ theo khu vực, theo ngành nghề, hoặc xoay vòng đều trong nhóm
- [ ] **S4-06-AC2** — Nhiều quy tắc xếp theo thứ tự ưu tiên, quy tắc đầu tiên khớp sẽ thắng
- [ ] **S4-06-AC3** — Lead không khớp quy tắc nào rơi vào hàng chờ để trưởng nhóm phân tay
- [ ] **S4-06-AC4** — Phân bổ chạy nền, hoàn tất trong vòng 5 phút kể từ khi lead vào

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-07 — 3 point · Must · EP-04

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn nhận hoặc từ chối lead được phân, có ràng buộc SLA phản hồi, để lead không nằm im ba ngày rồi nguội hẳn.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-07-AC1** — Nhân viên nhận lead thì lead chuyển sang Đang chăm sóc
- [ ] **S4-07-AC2** — Từ chối bắt buộc nhập lý do, lead quay lại hàng chờ phân bổ
- [ ] **S4-07-AC3** — Quá SLA phản hồi mà chưa liên hệ thì lead được gắn cờ và báo cho trưởng nhóm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-08 — 3 point · Must · EP-04

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn chuyển một lead đủ điều kiện thành khách hàng và cơ hội, để không phải nhập lại thông tin đã hỏi khách ba lần.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-08-AC1** — Một thao tác sinh đồng thời khách hàng doanh nghiệp, người liên hệ và cơ hội bán hàng
- [ ] **S4-08-AC2** — Dữ liệu lead được chuyển sang, không phải nhập lại
- [ ] **S4-08-AC3** — Lead chuyển sang trạng thái Đã chuyển đổi và không sửa được nữa
- [ ] **S4-08-AC4** — Toàn bộ hoạt động đã ghi trên lead được giữ lại trên khách hàng mới

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S4-09 — 3 point · Must · EP-04

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn xem danh sách lead với bộ lọc và bộ lọc lưu sẵn, để mở máy buổi sáng là biết ngay hôm nay cần gọi ai.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S4-09-AC1** — Lọc theo trạng thái, nguồn, phân loại nóng ấm lạnh, người phụ trách, khoảng thời gian
- [ ] **S4-09-AC2** — Lead quá SLA hiển thị nổi bật
- [ ] **S4-09-AC3** — Lưu và đặt tên cho bộ lọc hay dùng

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 5 — Cơ hội bán hàng và dự báo doanh số   ·   8 story · 44 point

**Sprint Goal (nguyên văn):** Toàn bộ thương vụ đang chạy nằm trên một bảng pipeline duy nhất, và con số dự báo doanh số của tháng được tính ra từ dữ liệu thật chứ không phải từ cảm nhận.

### S5-01 — 8 point · Must · EP-05

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn tạo và quản lý một cơ hội bán hàng, để thương vụ nằm trong hệ thống thay vì trong trí nhớ của tôi.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-01-AC1** — Khai báo tên cơ hội, khách hàng, người liên hệ chính, giai đoạn, giá trị dự kiến, ngày dự kiến chốt, nguồn
- [ ] **S5-01-AC2** — Xác suất thắng lấy mặc định theo giai đoạn, sửa tay được kèm ghi chú
- [ ] **S5-01-AC3** — Một khách hàng có thể có nhiều cơ hội song song
- [ ] **S5-01-AC4** — Ngày dự kiến chốt không được ở quá khứ khi tạo mới

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-02 — 8 point · Must · EP-05

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn điều hành các cơ hội trên bảng pipeline dạng kanban, để nhìn một màn hình là biết thương vụ nào đang tắc.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-02-AC1** — Mỗi cột là một giai đoạn, hiển thị số cơ hội và tổng giá trị của cột
- [ ] **S5-02-AC2** — Kéo thả để chuyển giai đoạn
- [ ] **S5-02-AC3** — Thẻ cơ hội hiển thị tên khách, giá trị, ngày dự kiến chốt và cảnh báo nếu đình trệ
- [ ] **S5-02-AC4** — Lọc theo người sở hữu, nhóm, khoảng ngày chốt; nhân viên mặc định chỉ thấy cơ hội của mình

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-03 — 5 point · Must · EP-05

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn thêm sản phẩm dịch vụ vào cơ hội để ra giá trị hợp đồng, để giá trị cơ hội là con số có căn cứ chứ không phải ước lượng.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-03-AC1** — Chọn sản phẩm từ danh mục, nhập số lượng và đơn giá, đơn giá mặc định lấy từ bảng giá niêm yết
- [ ] **S5-03-AC2** — Giá trị cơ hội được tính lại tự động từ các dòng sản phẩm
- [ ] **S5-03-AC3** — Với dịch vụ thuê bao, khai báo được số kỳ và giá trị hợp đồng theo năm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-04 — 5 point · Must · EP-05

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn có điều kiện bắt buộc khi cơ hội rời một giai đoạn, để pipeline phản ánh thực tế thay vì bị đẩy giai đoạn cho đẹp báo cáo.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-04-AC1** — Không cho chuyển giai đoạn nếu chưa thoả điều kiện đã cấu hình, ví dụ chưa có cuộc gặp nào được ghi nhận
- [ ] **S5-04-AC2** — Thông báo nêu rõ còn thiếu điều kiện gì
- [ ] **S5-04-AC3** — Trưởng nhóm trở lên có thể ghi đè kèm lý do

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-05 — 5 point · Must · EP-05

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn đóng một cơ hội với kết quả thắng hoặc thua, để công ty học được từ cả thương vụ thắng lẫn thua.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-05-AC1** — Đóng Thắng bắt buộc nhập giá trị chốt thực tế và ngày ký
- [ ] **S5-05-AC2** — Đóng Thua bắt buộc chọn lý do thua và đối thủ thắng thầu nếu có
- [ ] **S5-05-AC3** — Cơ hội đã đóng không sửa được, chỉ Trưởng nhóm trở lên mở lại được kèm lý do
- [ ] **S5-05-AC4** — Cơ hội thắng được tính vào chỉ tiêu của người sở hữu

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-06 — 5 point · Must · EP-05

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn xem dự báo doanh số tính theo trọng số xác suất, để trả lời được câu hỏi tháng này về bao nhiêu bằng số liệu.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-06-AC1** — Dự báo bằng tổng của giá trị cơ hội nhân xác suất thắng của giai đoạn
- [ ] **S5-06-AC2** — Nhóm theo kỳ dự kiến chốt: tháng này, tháng sau, quý này
- [ ] **S5-06-AC3** — So sánh dự báo với chỉ tiêu và với số đã chốt thực tế
- [ ] **S5-06-AC4** — Xem được theo từng nhân viên và từng nhóm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-07 — 5 point · Must · EP-05

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn được cảnh báo khi cơ hội đình trệ, để can thiệp sớm thay vì phát hiện khi thương vụ đã nguội.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-07-AC1** — Cơ hội không có hoạt động nào trong N ngày được gắn cờ, N cấu hình theo từng giai đoạn
- [ ] **S5-07-AC2** — Cơ hội quá ngày dự kiến chốt mà chưa đóng cũng được gắn cờ
- [ ] **S5-07-AC3** — Danh sách cơ hội có cờ hiển thị riêng cho trưởng nhóm
- [ ] **S5-07-AC4** — Tác vụ đánh giá chạy mỗi ngày một lần

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S5-08 — 3 point · Must · EP-05

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn phân bổ lại cơ hội cho người khác trong nhóm, để thương vụ không đứng yên khi người phụ trách nghỉ dài hoặc quá tải.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S5-08-AC1** — Chuyển quyền sở hữu một hoặc nhiều cơ hội cùng lúc
- [ ] **S5-08-AC2** — Người nhận thấy ngay cơ hội trong danh sách của mình cùng toàn bộ lịch sử
- [ ] **S5-08-AC3** — Mỗi lần chuyển được ghi nhật ký kèm lý do

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 6 — Hoạt động, công việc và lịch làm việc   ·   10 story · 45 point

**Sprint Goal (nguyên văn):** Mọi lần chạm khách hàng đều được ghi lại ở một chỗ, và nhân viên mở hệ thống buổi sáng là biết hôm nay phải gọi ai, gặp ai.

### S6-01 — 2 point · Must · EP-05

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn xem lịch sử thay đổi của một cơ hội, để biết vì sao giá trị hay ngày chốt bị đổi mà không phải hỏi lại.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-01-AC1** — Ghi lại mọi thay đổi giai đoạn, giá trị, ngày chốt và người sở hữu
- [ ] **S6-01-AC2** — Mỗi dòng có giá trị trước, giá trị sau, người thực hiện và thời điểm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-02 — 2 point · Must · EP-05

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn tìm kiếm và lọc cơ hội theo nhiều điều kiện, để dựng nhanh danh sách thương vụ cần đẩy trong tuần.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-02-AC1** — Lọc theo giai đoạn, khoảng giá trị, khoảng ngày chốt, sản phẩm, người sở hữu
- [ ] **S6-02-AC2** — Lưu lại được bộ lọc hay dùng

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-03 — 5 point · Should · EP-05

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn chia sẻ một cơ hội cho nhiều người cùng theo, để thương vụ lớn có cả đội cùng vào mà vẫn rõ ai chịu trách nhiệm chính.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-03-AC1** — Ngoài người sở hữu, thêm được thành viên cùng tham gia cơ hội
- [ ] **S6-03-AC2** — Thành viên được chia sẻ có quyền ghi hoạt động nhưng không đổi được người sở hữu
- [ ] **S6-03-AC3** — Chuyển quyền sở hữu cơ hội được ghi nhật ký

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-04 — 3 point · Should · EP-05

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn đính kèm tài liệu vào cơ hội, để hồ sơ thầu và biên bản họp nằm cùng chỗ với thương vụ.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-04-AC1** — Tải lên nhiều tệp, mỗi tệp tối đa 20MB
- [ ] **S6-04-AC2** — Chỉ người sở hữu và người được chia sẻ mới tải xuống được
- [ ] **S6-04-AC3** — Hiển thị người tải lên và thời điểm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-05 — 8 point · Must · EP-06

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn ghi nhận một hoạt động với khách hàng, để lịch sử trao đổi không nằm trong hộp thư cá nhân của riêng tôi.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-05-AC1** — Loại hoạt động: cuộc gọi, cuộc gặp, email, ghi chú
- [ ] **S6-05-AC2** — Gắn hoạt động với khách hàng, người liên hệ và cơ hội liên quan
- [ ] **S6-05-AC3** — Ghi thời điểm, thời lượng, nội dung trao đổi và kết quả
- [ ] **S6-05-AC4** — Ghi được hoạt động đã diễn ra trong quá khứ, không chỉ hoạt động sắp tới
- [ ] **S6-05-AC5** — Dùng được nhanh trên điện thoại ngay sau khi rời văn phòng khách

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-06 — 5 point · Must · EP-06

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn tạo và theo dõi công việc có hạn hoàn thành, để không quên lời hứa gửi báo giá cho khách vào thứ Sáu.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-06-AC1** — Công việc có tiêu đề, hạn, mức ưu tiên, người thực hiện, gắn với khách hàng hoặc cơ hội
- [ ] **S6-06-AC2** — Đánh dấu hoàn thành, hoãn kèm lý do
- [ ] **S6-06-AC3** — Trưởng nhóm giao việc được cho thành viên trong nhóm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-07 — 5 point · Must · EP-06

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn xem lịch làm việc của mình và của nhóm, để bố trí lịch gặp mà không chồng chéo với đồng đội.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-07-AC1** — Chế độ xem theo ngày, tuần và tháng
- [ ] **S6-07-AC2** — Hiển thị cuộc gặp đã đặt và công việc có hạn trong cùng một khung nhìn
- [ ] **S6-07-AC3** — Trưởng nhóm xem được lịch chồng của cả nhóm để bố trí
- [ ] **S6-07-AC4** — Lịch hiển thị tốt trên điện thoại

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-08 — 5 point · Must · EP-06

**Vai trò (nguyên văn):** Chăm sóc khách hàng

**User Story (nguyên văn):** Là Chăm sóc khách hàng, tôi muốn xem dòng thời gian tương tác đầy đủ của một khách hàng, để tiếp nhận khách từ đồng nghiệp mà nắm được bối cảnh trong năm phút.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-08-AC1** — Gộp toàn bộ hoạt động, thay đổi cơ hội, báo giá đã gửi theo thứ tự thời gian
- [ ] **S6-08-AC2** — Lọc theo loại hoạt động và theo người thực hiện

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-09 — 5 point · Must · EP-06

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn được nhắc việc sắp đến hạn và việc quá hạn, để không để việc trôi vì bận chạy thương vụ khác.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-09-AC1** — Nhắc trong ứng dụng trước hạn theo mốc cấu hình được
- [ ] **S6-09-AC2** — Việc quá hạn hiển thị nổi bật trên trang chủ cá nhân
- [ ] **S6-09-AC3** — Trưởng nhóm thấy tổng số việc quá hạn của từng thành viên

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S6-10 — 5 point · Should · EP-06

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn ghi nhanh kết quả cuộc gọi ngay trên điện thoại, để ghi lại khi còn nhớ thay vì để tối về quên mất.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S6-10-AC1** — Biểu mẫu rút gọn: kết quả cuộc gọi, ghi chú ngắn, đặt lịch gọi lại
- [ ] **S6-10-AC2** — Hoàn tất trong không quá ba thao tác chạm
- [ ] **S6-10-AC3** — Tự động gắn vào cơ hội đang mở gần nhất của khách hàng đó

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 7 — Báo giá, duyệt chiết khấu và hợp đồng   ·   9 story · 44 point

**Sprint Goal (nguyên văn):** Nhân viên soạn báo giá từ cơ hội trong vài phút, mọi mức chiết khấu vượt ngưỡng đều đi qua đúng cấp duyệt và để lại dấu vết, và báo giá được khách chấp nhận trở thành hợp đồng mà không phải nhập lại.

### S7-01 — 8 point · Must · EP-07

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn soạn báo giá từ một cơ hội, để gửi báo giá trong buổi chiều cùng ngày khách hỏi.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-01-AC1** — Dòng sản phẩm được kế thừa từ cơ hội, sửa lại được
- [ ] **S7-01-AC2** — Mỗi dòng nhập chiết khấu theo phần trăm hoặc số tiền, hệ thống tính lại thành tiền ngay
- [ ] **S7-01-AC3** — Hiển thị mức chiết khấu tổng và cảnh báo khi chạm ngưỡng cần duyệt
- [ ] **S7-01-AC4** — Khai báo hiệu lực báo giá, điều khoản thanh toán và điều khoản giao hàng
- [ ] **S7-01-AC5** — Lưu nháp và sửa tiếp được

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-02 — 8 point · Must · EP-07

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn duyệt báo giá theo mức chiết khấu, để kiểm soát biên lợi nhuận mà không phải trả lời từng tin nhắn xin giảm giá.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-02-AC1** — Chiết khấu dưới ngưỡng thứ nhất thì tự động thông qua, không cần duyệt
- [ ] **S7-02-AC2** — Vượt ngưỡng thứ nhất do Trưởng nhóm duyệt; vượt ngưỡng thứ hai hoặc dưới giá sàn thì bắt buộc Giám đốc kinh doanh duyệt
- [ ] **S7-02-AC3** — Ba hành động: Duyệt, Từ chối, Trả lại sửa — hai hành động sau bắt buộc nhập ý kiến
- [ ] **S7-02-AC4** — Chỉ báo giá đã duyệt mới gửi được cho khách
- [ ] **S7-02-AC5** — Lịch sử duyệt không sửa và không xoá được

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-03 — 5 point · Must · EP-07

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn quản lý nhiều phiên bản của một báo giá, để biết chính xác đã cam kết gì với khách ở lần gửi trước.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-03-AC1** — Mỗi lần sửa sau khi đã gửi sẽ tạo phiên bản mới, phiên bản cũ được giữ nguyên
- [ ] **S7-03-AC2** — So sánh cạnh nhau hai phiên bản để thấy đã thay đổi gì
- [ ] **S7-03-AC3** — Chỉ một phiên bản ở trạng thái đang hiệu lực tại một thời điểm

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-04 — 5 point · Must · EP-07

**Vai trò (nguyên văn):** Kế toán

**User Story (nguyên văn):** Là Kế toán, tôi muốn chuyển một báo giá được chấp nhận thành hợp đồng, để số liệu hợp đồng khớp báo giá mà không phải gõ lại.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-04-AC1** — Hợp đồng kế thừa toàn bộ dòng sản phẩm và giá đã chốt, không nhập lại
- [ ] **S7-04-AC2** — Khai báo số hợp đồng, ngày ký, ngày hiệu lực, ngày hết hạn, điều khoản thanh toán
- [ ] **S7-04-AC3** — Cơ hội tương ứng tự động chuyển sang Đóng thắng với giá trị đúng bằng giá trị hợp đồng
- [ ] **S7-04-AC4** — Số hợp đồng là duy nhất

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-05 — 3 point · Must · EP-07

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn xuất báo giá và hợp đồng ra PDF theo mẫu công ty, để gửi khách tài liệu đúng nhận diện thương hiệu.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-05-AC1** — Mẫu có logo, thông tin công ty, bảng dòng sản phẩm, tổng tiền bằng số và bằng chữ
- [ ] **S7-05-AC2** — Mẫu khai báo được ở phần cấu hình, không cố định trong mã nguồn

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-06 — 3 point · Must · EP-07

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn gửi báo giá cho khách và nhận phản hồi qua liên kết, để biết khách đã xem và quyết định thế nào mà không phải gọi hỏi.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-06-AC1** — Gửi email kèm liên kết xem báo giá, liên kết chỉ dùng cho đúng báo giá đó
- [ ] **S7-06-AC2** — Khách bấm Chấp nhận hoặc Từ chối, có ô ghi ý kiến
- [ ] **S7-06-AC3** — Phản hồi cập nhật ngay trạng thái báo giá và ghi vào dòng thời gian của cơ hội
- [ ] **S7-06-AC4** — Quá hạn hiệu lực mà không phản hồi thì báo giá tự chuyển sang Hết hiệu lực

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-07 — 2 point · Should · EP-07

**Vai trò (nguyên văn):** Kế toán

**User Story (nguyên văn):** Là Kế toán, tôi muốn theo dõi hiệu lực và gia hạn hợp đồng, để không mất doanh thu vì quên gia hạn hợp đồng sắp hết hạn.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-07-AC1** — Danh sách hợp đồng kèm ngày hết hạn và số ngày còn lại
- [ ] **S7-07-AC2** — Cảnh báo hợp đồng sắp hết hạn theo ngưỡng cấu hình được
- [ ] **S7-07-AC3** — Tạo cơ hội gia hạn từ một hợp đồng sắp hết hạn

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-08 — 5 point · Must · EP-09

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn nhận thông báo trong ứng dụng cho các sự kiện quan trọng, để không bỏ lỡ báo giá chờ mình duyệt hay phản hồi của khách.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-08-AC1** — Kích hoạt khi: được phân lead, có báo giá chờ mình duyệt, khách phản hồi báo giá, được giao việc, cơ hội bị gắn cờ đình trệ
- [ ] **S7-08-AC2** — Biểu tượng chuông hiển thị số thông báo chưa đọc
- [ ] **S7-08-AC3** — Bấm vào thông báo mở đúng màn hình liên quan
- [ ] **S7-08-AC4** — Thông báo sinh bất đồng bộ, không làm chậm thao tác gốc

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S7-09 — 5 point · Should · EP-09

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn khai báo quy tắc tự động hoá đơn giản theo mẫu khi–thì, để quy trình bán hàng tự chạy thay vì phải nhắc nhân viên từng bước.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S7-09-AC1** — Ví dụ: khi cơ hội chuyển sang giai đoạn Báo giá thì tự tạo công việc nhắc gọi lại sau 3 ngày
- [ ] **S7-09-AC2** — Điều kiện dựa trên thay đổi trạng thái hoặc mốc thời gian
- [ ] **S7-09-AC3** — Hành động gồm: tạo công việc, gửi thông báo, đổi người sở hữu
- [ ] **S7-09-AC4** — Bật tắt từng quy tắc và xem nhật ký quy tắc đã chạy

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## SPRINT 8 — Chỉ tiêu, dashboard và báo cáo   ·   11 story · 45 point

**Sprint Goal (nguyên văn):** Giám đốc kinh doanh mở một màn hình là biết cả khối đang ở đâu so với chỉ tiêu, và mỗi nhân viên biết mình đang đứng ở đâu.

### S8-01 — 3 point · Should · EP-09

**Vai trò (nguyên văn):** Nhân viên Marketing

**User Story (nguyên văn):** Là Nhân viên Marketing, tôi muốn soạn mẫu email và gửi email cho khách từ trong hệ thống, để thư gửi khách được ghi vào lịch sử thay vì nằm trong hộp thư cá nhân.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-01-AC1** — Mẫu chèn được biến: tên người liên hệ, tên công ty, tên nhân viên phụ trách
- [ ] **S8-01-AC2** — Email gửi đi được ghi vào dòng thời gian của khách hàng
- [ ] **S8-01-AC3** — Gửi bất đồng bộ qua hàng đợi, gửi lại được thư thất bại

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-02 — 2 point · Could · EP-09

**Vai trò (nguyên văn):** người dùng của hệ thống

**User Story (nguyên văn):** Là người dùng của hệ thống, tôi muốn bật hoặc tắt từng loại thông báo cho riêng mình, để chỉ nhận những thông báo thực sự liên quan tới công việc của tôi.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-02-AC1** — Cấu hình riêng cho kênh trong ứng dụng và kênh email
- [ ] **S8-02-AC2** — Thông báo bắt buộc như báo giá chờ duyệt thì không tắt được

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-03 — 8 point · Must · EP-08

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn khai báo chỉ tiêu doanh số theo nhân viên, nhóm và kỳ, để đo được kết quả thực tế so với cam kết đầu kỳ.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-03-AC1** — Đặt chỉ tiêu theo tháng và theo quý, cho từng nhân viên và từng nhóm
- [ ] **S8-03-AC2** — Chỉ tiêu của nhóm phải bằng tổng chỉ tiêu các thành viên, lệch thì cảnh báo
- [ ] **S8-03-AC3** — Chỉ tiêu đã qua kỳ thì khoá lại, sửa phải có lý do và được ghi nhật ký
- [ ] **S8-03-AC4** — Nhân viên chỉ xem được chỉ tiêu của chính mình

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-04 — 8 point · Must · EP-08

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn xem dashboard kinh doanh theo đúng vai trò của mình, để mỗi cấp thấy đúng thứ mình cần quyết định, không nhiều hơn.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-04-AC1** — Nhân viên thấy: chỉ tiêu và tiến độ của mình, cơ hội đang mở, việc hôm nay, lead chưa liên hệ
- [ ] **S8-04-AC2** — Trưởng nhóm thấy thêm: xếp hạng thành viên, cơ hội đình trệ của nhóm, việc quá hạn của nhóm
- [ ] **S8-04-AC3** — Giám đốc thấy toàn khối: dự báo so với chỉ tiêu, giá trị pipeline theo giai đoạn, doanh số đã chốt theo tháng
- [ ] **S8-04-AC4** — Toàn bộ dữ liệu tải xong dưới 2 giây

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-05 — 5 point · Must · EP-08

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn xem báo cáo phễu và tỷ lệ chuyển đổi từng giai đoạn, để biết nên cải thiện khâu nào để tăng tỷ lệ chốt.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-05-AC1** — Số cơ hội và giá trị ở từng giai đoạn, tỷ lệ chuyển đổi giữa các giai đoạn
- [ ] **S8-05-AC2** — Thời gian trung bình một cơ hội nằm ở mỗi giai đoạn
- [ ] **S8-05-AC3** — Chỉ ra giai đoạn rơi rụng nhiều nhất và giai đoạn tắc lâu nhất
- [ ] **S8-05-AC4** — Lọc theo nhóm, nhân viên, sản phẩm, khoảng thời gian

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-06 — 5 point · Must · EP-08

**Vai trò (nguyên văn):** Giám đốc kinh doanh

**User Story (nguyên văn):** Là Giám đốc kinh doanh, tôi muốn xem báo cáo dự báo doanh số và tiến độ chỉ tiêu, để điều chỉnh kịp trong kỳ thay vì biết kết quả khi đã hết quý.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-06-AC1** — So sánh ba con số: đã chốt, dự báo theo trọng số, chỉ tiêu — theo từng kỳ
- [ ] **S8-06-AC2** — Xem theo nhân viên, nhóm và toàn khối
- [ ] **S8-06-AC3** — Phân tích lý do thua theo danh mục và theo đối thủ
- [ ] **S8-06-AC4** — Xuất Excel

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-07 — 3 point · Should · EP-08

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn xem báo cáo hoạt động và năng suất của nhóm, để biết thành viên nào cần kèm thêm chứ không chỉ nhìn doanh số.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-07-AC1** — Số cuộc gọi, cuộc gặp, báo giá gửi đi theo từng thành viên và theo tuần
- [ ] **S8-07-AC2** — Tỷ lệ lead được phản hồi trong SLA
- [ ] **S8-07-AC3** — Xuất Excel

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-08 — 3 point · Could · EP-08

**Vai trò (nguyên văn):** Nhân viên kinh doanh

**User Story (nguyên văn):** Là Nhân viên kinh doanh, tôi muốn xem bảng xếp hạng doanh số trong nhóm, để biết mình đang đứng đâu so với đồng đội.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-08-AC1** — Xếp hạng theo doanh số đã chốt trong kỳ, hiển thị tiến độ so với chỉ tiêu cá nhân
- [ ] **S8-08-AC2** — Nhân viên chỉ thấy xếp hạng trong nhóm của mình
- [ ] **S8-08-AC3** — Bật tắt được tính năng này ở cấp cấu hình

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-09 — 3 point · Should · EP-04

**Vai trò (nguyên văn):** Nhân viên Marketing

**User Story (nguyên văn):** Là Nhân viên Marketing, tôi muốn xem báo cáo hiệu quả từng nguồn lead, để dồn ngân sách vào nguồn thực sự ra doanh thu.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-09-AC1** — Số lead, tỷ lệ được nhận, tỷ lệ chuyển đổi thành cơ hội theo từng nguồn và từng chiến dịch
- [ ] **S8-09-AC2** — Lọc theo khoảng thời gian
- [ ] **S8-09-AC3** — Xuất Excel

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-10 — 3 point · Should · EP-05

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn xuất danh sách cơ hội ra tệp Excel, để làm báo cáo riêng theo mẫu ban giám đốc yêu cầu.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-10-AC1** — Xuất theo bộ lọc đang áp dụng, gồm cả trường tuỳ chỉnh
- [ ] **S8-10-AC2** — Không xuất giá vốn và biên lợi nhuận cho người không có quyền

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

### S8-11 — 2 point · Must · EP-06

**Vai trò (nguyên văn):** Trưởng nhóm kinh doanh

**User Story (nguyên văn):** Là Trưởng nhóm kinh doanh, tôi muốn lọc và xem hoạt động theo loại và theo người thực hiện, để kiểm tra được mức độ chăm sóc khách của từng thành viên.

**Acceptance Criteria — nguyên văn, không tóm tắt:**

- [ ] **S8-11-AC1** — Lọc theo khoảng thời gian, loại hoạt động, thành viên
- [ ] **S8-11-AC2** — Hiển thị tổng số hoạt động theo từng thành viên

**Theo dõi thực thi:**

- Story status: `TODO`
- FE: `TODO`
- BE: `TODO`
- DB integration: `TODO`
- Automated test: `TODO`
- Manual QA của người dùng: `TODO`
- Evidence: _chưa có_
- File tạo/sửa: _chưa có_
- Lỗi QA / ghi chú: _chưa có_

---

## 10. STORY EXECUTION LOG

Mỗi lần chạy Prompt 2, chỉ bổ sung **một entry** theo mẫu sau; không xóa log cũ:

```text
Story: Sx-xx
Mode: IMPLEMENT | FIX_QA
Started:
Completed coding:
Story status after AI run: READY_FOR_USER_QA | BLOCKED
AC status:
- Sx-xx-AC1: ...
- Sx-xx-AC2: ...
Evidence:
- ...
Files created:
- ...
Files modified:
- ...
Tests:
- mvn clean test: PASS/FAIL
- mvn clean package: PASS/FAIL
Known issues:
- ...
User QA result: PENDING | PASS | FAIL
Git checkpoint: NOT CREATED | <commit hash>
```

---

## 11. REGRESSION CHECKLIST TỐI THIỂU SAU MỖI STORY

- [ ] Ứng dụng build/package được.
- [ ] Trang `/` và luồng login hiện có không vỡ.
- [ ] CSS/JS static assets không bị Filter redirect.
- [ ] Không xuất hiện link/menu dẫn tới route chưa triển khai.
- [ ] Không lộ credential/token/secret trong HTML/log.
- [ ] RBAC/Data Scope của các Story DONE trước vẫn hoạt động.
- [ ] Không có thay đổi canonical SQL.
- [ ] Không phát sinh dependency ngoài phạm vi.
- [ ] Không có JSP/EL exception mới.
- [ ] Không có HTTP 500 mới trên các route của Story DONE trước.

---

## 12. QUY TẮC GIT CHECKPOINT

Sau khi người dùng QA Story và xác nhận PASS:

1. Cập nhật Story/AC tương ứng thành `DONE`/`PASS`.
2. Chạy lại `git status` và `git diff --stat`.
3. Commit **riêng Story đó** trên integration branch.
4. Sau commit mới chuyển sang Story kế tiếp.
5. Không merge `dev/main` cho đến mốc tích hợp mà người dùng quyết định.

---

## 13. THỐNG KÊ KIỂM TRA FILE

- Tổng Story: **76**.
- Tổng Story Point: **350**.
- Tổng Acceptance Criteria nguyên văn: **249**.
- Sprint 1: 10 Story / 42 Point / 31 AC.
- Sprint 2: 10 Story / 43 Point / 32 AC.
- Sprint 3: 9 Story / 42 Point / 28 AC.
- Sprint 4: 9 Story / 45 Point / 31 AC.
- Sprint 5: 8 Story / 44 Point / 29 AC.
- Sprint 6: 10 Story / 45 Point / 30 AC.
- Sprint 7: 9 Story / 44 Point / 34 AC.
- Sprint 8: 11 Story / 45 Point / 34 AC.

> File này được tạo lại từ sheet `4. Product Backlog`; không kế thừa bảng mapping Module/Test sai của bản Ke_Hoach trước.