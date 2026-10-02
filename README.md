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

## Khởi tạo cơ sở dữ liệu (Database Bootstrap)

Lần đầu / reset database dev:

```powershell
$env:DB_PASSWORD = 'mat_khau_mysql'
.\scripts\setup-db.ps1
```

Script sử dụng:
`src/main/resources/db/001_crm_ban_hang_full_schema_8_sprints.sql`

Không yêu cầu chạy từng migration Sprint 1 riêng lẻ.
