# TTCS_K1S2_N3

Dự án Thực tập cơ sở - Hệ thống quản lý khách hàng và quy trình bán hàng CRM.

## Nhánh chính

- `main`: phiên bản ổn định.
- `dev`: nhánh tích hợp phát triển.
- `feature/*`: nhánh thực hiện từng User Story.

## Tài liệu yêu cầu

Tài liệu nguồn và tài liệu phân tích nằm trong thư mục `PLAN/`.

## Công nghệ

Dự án sử dụng:

- Java JSP
- Jakarta Servlet
- Apache Tomcat
- Apache Maven
- MySQL
- HTML5
- CSS3
- Vanilla JavaScript

Thông tin version thực tế được quản lý trong:

`PLAN/infoTechnology.md`

## Cấu hình SMTP (Email Service)

Hệ thống hỗ trợ gửi email kích hoạt tài khoản qua SMTP. Bạn có thể cấu hình qua file `.env` hoặc các biến môi trường:

- `SMTP_HOST`: Máy chủ SMTP (mặc định: `smtp.gmail.com`)
- `SMTP_PORT`: Cổng SMTP (mặc định: `587` cho TLS hoặc `465` cho SSL)
- `SMTP_USER` / `SMTP_USERNAME`: Tài khoản đăng nhập SMTP
- `SMTP_PASSWORD`: Mật khẩu ứng dụng (App Password)
- `SMTP_FROM`: Email người gửi
- `SMTP_FROM_NAME`: Tên người gửi hiển thị (mặc định: `CRM Bán Hàng`)

Chi tiết tham khảo file `.env.example`.