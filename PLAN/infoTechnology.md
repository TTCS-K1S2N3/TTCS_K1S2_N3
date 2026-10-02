# Technology Information

File này ghi các công nghệ THỰC TẾ đang được sử dụng trong project.

Không sử dụng file này để ghi tiến độ Story.

## Java

- Java JDK: 25 LTS
- Mục đích: Ngôn ngữ và runtime chính của hệ thống.

## Backend Web

- Jakarta Servlet: 6.1
- Mục đích: Controller xử lý HTTP request và response.

## View

- JSP / Jakarta Pages: 4.0
- Mục đích: Server-side rendering.

## Web Server

- Apache Tomcat: 11.0.x
- Mục đích: Servlet/JSP container.

## Build

- Apache Maven: 3.9.x
- Mục đích:
  - Quản lý dependency.
  - Build project.
  - Chạy test.
  - Đóng gói WAR.

## Database

- MySQL Server: 8.4 LTS
- Mục đích: Database quan hệ chính.

## Database Access

- JDBC / MySQL Connector/J: 8.4.0
- HikariCP: 6.2.0
- Mục đích: Kết nối và quản lý connection pool Java với MySQL.

## Security & Utilities

- jBCrypt: 0.4 (Băm mật khẩu an toàn)
- SLF4J: 2.0.16 (Ghi log hệ thống)

## Testing

- JUnit Jupiter: 5.11.4
- Mockito: 5.14.2
- Mục đích: Kiểm thử đơn vị và tích hợp tự động.

## Frontend

- HTML5
- CSS3
- Vanilla JavaScript
- JSP

Không sử dụng React, Vue hoặc Angular trong kiến trúc hiện tại.

## Version Source

Version dependency Java phải ưu tiên theo:

1. `pom.xml`
2. Cấu hình thực tế của project
3. Môi trường local
4. Tài liệu chính thức

Không tự bịa version.

## Quy tắc cập nhật

Mặc định feature branch KHÔNG sửa file này.

Chỉ cập nhật khi project thực sự:

- Thêm dependency mới.
- Xóa dependency.
- Thay đổi version.
- Thay đổi runtime.
- Thêm công nghệ mới.
- Thay đổi công nghệ hiện tại.

Không cập nhật file chỉ vì có Story hoặc module mới.