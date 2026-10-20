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

Dự án hỗ trợ 3 tầng cấu hình linh hoạt để mỗi thành viên clone về và cấu hình local một lần là có thể chạy độc lập,
không phụ thuộc mật khẩu MySQL hoặc đường dẫn máy của thành viên khác.

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

> **Cảnh báo:** script này RESET toàn bộ database `crm_ban_hang`.
> Chỉ chạy trên máy dev mới hoặc khi chủ động muốn làm sạch toàn bộ dữ liệu.

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

Các tài khoản dưới đây chỉ dùng cho môi trường local/dev.

Mật khẩu chung:

`123456@Aa`

| Vai trò | Email đăng nhập | Mật khẩu | Ghi chú |
| :--- | :--- | :--- | :--- |
| Quản trị hệ thống | `admin@crm.vn` | `123456@Aa` | Quản trị hệ thống |
| Giám đốc kinh doanh | `director@crm.vn` | `123456@Aa` | Toàn bộ dữ liệu kinh doanh |
| Trưởng nhóm kinh doanh | `teamlead@crm.vn` | `123456@Aa` | Phạm vi nhóm |
| Nhân viên kinh doanh | `sales@crm.vn` | `123456@Aa` | Phạm vi cá nhân |
| Marketing | `marketing@crm.vn` | `123456@Aa` | Marketing / Lead |
| Chăm sóc khách hàng | `cskh@crm.vn` | `123456@Aa` | CSKH |
| Kế toán | `accountant@crm.vn` | `123456@Aa` | Báo giá / Hợp đồng |

> Không sử dụng các credential mẫu này trên staging hoặc production.

---

## 8. Cấu Hình Email / SMTP (Tùy Chọn)

Hệ thống hỗ trợ gửi email thông báo và kích hoạt tài khoản qua SMTP.

- **Chế độ phát triển (Dev Fallback)**: Mặc định nếu không có thông tin SMTP trong `src/main/resources/config/email.properties`, ứng dụng **không bao giờ bị crash**, hệ thống sẽ ghi log nội dung email ra console để lập trình viên kiểm tra.
- **Cấu hình SMTP thực tế qua biến môi trường (Khuyến nghị)**:
  ```powershell
  $env:SMTP_USERNAME = "your-email@gmail.com"
  $env:SMTP_PASSWORD = "YOUR_GMAIL_APP_PASSWORD"
  $env:SMTP_USERNAME="emailcuaban@gmail.com"
  $env:SMTP_PASSWORD="APP_PASSWORD_CUA_BAN"
  $env:SMTP_FROM="emailcuaban@gmail.com"
  $env:SMTP_FROM_NAME="CRM Bán Hàng"
  ```
  > **Lưu ý bảo mật quan trọng:** Tuyệt đối KHÔNG commit mật khẩu hoặc App Password cá nhân vào mã nguồn.

Sau khi thiết lập biến môi trường, phải restart Tomcat từ terminal đã có các biến trên.

Nếu chưa cấu hình SMTP, ứng dụng chạy DEV/TEST fallback:
yêu cầu có thể được ghi nhận nhưng email thật sẽ không được gửi.

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
