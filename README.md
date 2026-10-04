# TTCS_K1S2_N3 - Hệ Thống CRM Quản Lý Khách Hàng & Bán Hàng

Dự án Thực tập cơ sở - Hệ thống quản lý khách hàng và quy trình bán hàng CRM đa vai trò (Admin, Director, Team Lead, Sales Rep, Marketing, Customer Success, Accountant).

---

## 1. Yêu Cầu Môi Trường (Prerequisites)

- **Java JDK**: Version 17 trở lên (khuyến nghị OpenJDK 17 LTS).
- **Apache Maven**: Version 3.8 trở lên.
- **Apache Tomcat**: Version 10.1+ (Hỗ trợ Jakarta EE 10 / Servlet 6.0).
- **MySQL Server**: Version 8.0 hoặc 8.4 LTS.
- **PowerShell**: Windows PowerShell 5.1+ hoặc PowerShell Core 7+.

Chi tiết công nghệ và phiên bản: Xem [PLAN/infoTechnology.md](PLAN/infoTechnology.md).

---

## 2. Nhánh Git & Quy Ước Phát Triển

- `main`: Phiên bản phát hành chính thức ổn định.
- `dev`: Nhánh tích hợp chung toàn đội.
- `merge/sprint-2-team`: Nhánh tổng hợp và tích hợp cuối của Sprint 2.
- `feature/*`: Nhánh phát triển User Story độc lập.

---

## 3. Cấu Hình Cơ Sở Dữ Liệu (Portable DB Configuration)

Dự án hỗ trợ 3 tầng cấu hình linh hoạt để mọi thành viên clone về đều chạy được ngay mà không xung đột mật khẩu cá nhân:

1. **Mặc định (Tracked)**: `src/main/resources/config/database.properties` chứa cấu hình mặc định (password để trống).
2. **Cấu hình cá nhân (Gitignored)**: Sao chép file mẫu:
   ```powershell
   Copy-Item database-local.properties.example database-local.properties
   ```
   và điền mật khẩu MySQL của máy bạn. File này đã được `.gitignore` bảo vệ, tuyệt đối không bị commit lên kho mã nguồn.
3. **Biến môi trường (Ưu tiên cao nhất)**:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `AVATAR_UPLOAD_DIR`

---

## 4. Khởi Tạo Cơ Sở Dữ Liệu (Database Bootstrap)

Chạy script khởi tạo bảng và nạp dữ liệu mẫu ban đầu:

```powershell
$env:DB_PASSWORD = "mat_khau_mysql_cua_ban"
.\scripts\setup-db.ps1
```

Script sẽ tự động thực thi canonical schema:
`src/main/resources/db/001_crm_ban_hang_full_schema_8_sprints.sql`

---

## 5. Biên Dịch & Chạy Kiểm Thử Tự Động

Chạy toàn bộ unit test và build gói triển khai:

```powershell
# Chạy kiểm thử tự động (sử dụng H2 in-memory cách ly, không chạm MySQL)
mvn clean test

# Đóng gói tệp WAR
mvn clean package
```

Kết quả build sẽ tạo ra tệp:
`target/crm-ban-hang.war`

---

## 6. Triển Khai Lên Apache Tomcat (Deployment)

1. Sao chép tệp `target/crm-ban-hang.war` vào thư mục `webapps/` của Apache Tomcat 10.1+.
2. Khởi động Tomcat (`bin/startup.bat`).
3. Truy cập hệ thống tại:
   `http://localhost:8080/crm-ban-hang`

---

## 7. Tài Khoản Kiểm Thử Mẫu (Seed Accounts)

Hệ thống đã thiết lập sẵn các tài khoản phân quyền mẫu để phục vụ kiểm thử theo role:

| Vai trò | Email đăng nhập | Mật khẩu mẫu | Ghi chú quyền hạn |
| :--- | :--- | :--- | :--- |
| **Quản trị hệ thống** | `admin@crm.vn` | `Admin@123` | Quản trị tài khoản, cơ cấu tổ chức, trường tùy chỉnh, nhật ký thay đổi |
| **Giám đốc kinh doanh** | `giamdoc@crm.vn` | `Giamdoc@123` | Cấu hình pipeline giai đoạn, giá vốn sản phẩm, lý do thắng/thua |
| **Trưởng nhóm kinh doanh**| `lead@crm.vn` | `Lead@123` | Phân bổ cơ hội, quản lý nhóm bán hàng, xem danh mục |
| **Nhân viên kinh doanh** | `sales@crm.vn` | `Sales@123` | Tra cứu sản phẩm, khai báo cơ hội, cập nhật hồ sơ cá nhân |

---

## 8. Cấu Hình Email / SMTP (Tùy Chọn)

Hệ thống hỗ trợ gửi email thông báo và kích hoạt tài khoản qua SMTP.

- **Chế độ phát triển (Dev Fallback)**: Mặc định nếu không có thông tin SMTP trong `src/main/resources/config/email.properties`, ứng dụng **không bao giờ bị crash**, hệ thống sẽ ghi log nội dung email ra console để lập trình viên kiểm tra.
- **Cấu hình SMTP thực tế qua biến môi trường (Khuyến nghị)**:
  ```powershell
  $env:SMTP_USERNAME = "your-email@gmail.com"
  $env:SMTP_PASSWORD = "YOUR_GMAIL_APP_PASSWORD"
  ```
  > **Lưu ý bảo mật quan trọng:** Tuyệt đối KHÔNG commit mật khẩu hoặc App Password cá nhân vào mã nguồn.

---

## 9. Quản Lý Ảnh Đại Diện & Fallback

- Thư mục lưu trữ ảnh tải lên: `uploads/avatars/` (đã được cấu hình trong `.gitignore`).
- **Cơ chế Fallback thông minh**: Nếu người dùng chưa tải ảnh hoặc chạy trên máy mới chưa có thư mục local uploads, hệ thống tự động sinh ảnh đại diện SVG chứa chữ cái viết tắt (Initials) với màu sắc nhận diện chuẩn. Không gây lỗi 404 hay vỡ layout.

---

## 10. Quy Tắc Bảo Mật & Không Lưu Trữ Bí Mật (Zero Tracked Secrets)

- Toàn bộ secret thật (MySQL password, App Password, Secret Token, API Key) **không được commit**.
- File `.gitignore` được thiết lập chặn:
  - `.env`, `.env.*`
  - `database-local.properties`, `application-local.properties`
  - `uploads/`
  - `target/`
  - `logs/`
