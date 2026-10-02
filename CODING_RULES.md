# CODING RULES - CRM

File này quy định cách viết code chung cho toàn bộ dự án.
Mọi thành viên và AI phải đọc trước khi sửa code.

## 1. Kiến trúc bắt buộc

Dự án sử dụng:

Browser
→ JSP / HTML / CSS / JavaScript
→ Servlet / Controller
→ Service
→ DAO
→ MySQL

Trách nhiệm:

- JSP: hiển thị giao diện và dữ liệu.
- Servlet: nhận HTTP request, validate cơ bản, gọi Service và điều hướng.
- Service: xử lý business logic, transaction và permission nghiệp vụ.
- DAO: truy vấn database và mapping dữ liệu.
- Model: mô hình dữ liệu nghiệp vụ.
- DTO: dữ liệu truyền giữa các tầng khi cần.
- Filter: authentication, authorization, encoding và request logic dùng chung.
- Util: logic dùng chung không thuộc một nghiệp vụ cụ thể.

## 2. Không được làm

- Không viết SQL trong JSP.
- Không mở JDBC Connection trong JSP.
- Không viết SQL trực tiếp trong Servlet.
- Không đặt business logic lớn trong Servlet.
- Không đặt business logic trong DAO.
- Không render HTML trong DAO hoặc Service.
- Không dùng Java scriptlet trong JSP cho business logic.
- Không dùng `javax.servlet.*`.
- Phải dùng `jakarta.servlet.*`.
- Không hard-code password, API key, SMTP password hoặc DB password.
- Không tạo class/Service/DAO/util trùng chức năng.
- Không tự ý đổi package structure.
- Không tự ý đổi framework hoặc architecture của project.
- Không refactor ngoài phạm vi Story nếu không cần thiết.

## 3. Quy tắc đặt tên Java

### Class

PascalCase.

Ví dụ:

- KhachHang
- NguoiLienHe
- KhachHangDAO
- KhachHangService
- KhachHangServlet
- BaoGiaService

### Biến

camelCase.

Ví dụ:

- khachHang
- dsKhachHang
- tongDoanhThu
- giaTriCoHoi
- ngayHetHan

### Method

camelCase và bắt đầu bằng động từ.

Ví dụ:

- taoKhachHang()
- capNhatKhachHang()
- timKhachHangTheoId()
- layDsKhachHang()
- kiemTraQuyen()
- tinhTongTien()

### Boolean

Tên phải thể hiện rõ trạng thái đúng/sai.

Ví dụ:

- daDuyet
- dangHoatDong
- daKhoa
- coQuyen
- quaHan

### Constant và Enum

UPPER_SNAKE_CASE.

Ví dụ:

- DANG_CHAM_SOC
- DA_CHUYEN_DOI
- DA_DUYET
- DA_TU_CHOI

## 4. Thuật ngữ nghiệp vụ

Ưu tiên tiếng Việt không dấu trong Java.

Ví dụ:

- KhachHang
- NguoiLienHe
- CoHoi
- BaoGia
- HopDong

Không dùng nhiều tên khác nhau cho cùng một khái niệm.

Ví dụ nếu đã dùng `KhachHang` thì không tạo thêm:

- Customer
- Client
- Khach

Ngoại lệ cho các thuật ngữ phổ biến:

- id
- dao
- dto
- lead
- email
- url

## 5. Database

Tên table và column dùng snake_case.

Ví dụ:

- khach_hang
- nguoi_lien_he
- co_hoi
- bao_gia
- nguoi_dung_id
- ngay_het_han

## 6. JSP

Tên JSP dùng kebab-case.

Ví dụ:

- danh-sach-khach-hang.jsp
- chi-tiet-khach-hang.jsp
- tao-bao-gia.jsp

JSP sử dụng EL và Jakarta Tags khi phù hợp.

Không đặt business logic trong JSP.

## 7. CSS

CSS class dùng kebab-case.

Ví dụ:

- khach-hang-card
- login-form
- pipeline-column

Không sửa CSS global nếu style cục bộ của module có thể giải quyết được.

## 8. URL

URL dùng kebab-case.

Ví dụ:

- /khach-hang
- /khach-hang/chi-tiet
- /co-hoi
- /bao-gia

Không tự ý đổi URL contract đã được module khác sử dụng.

## 9. Bảo mật

- Authorization phải kiểm tra phía server.
- Không coi việc ẩn button/menu là cơ chế bảo mật.
- Không tin owner, role, status, total money hoặc dữ liệu nhạy cảm do client gửi lên.
- Không log password, secret hoặc token nhạy cảm.
- Truy vấn có input phải sử dụng PreparedStatement.
- Escape dữ liệu đầu ra để hạn chế XSS.

## 10. Quy tắc làm việc theo Story

Mỗi feature branch chỉ sửa những file cần thiết cho Story.

Không format lại toàn project.

Không di chuyển file dùng chung nếu Story không yêu cầu.

Các file có nguy cơ conflict cao:

- pom.xml
- web.xml
- README.md
- CODING_RULES.md
- DATABASE_RULES.md
- PLAN/infoTechnology.md
- CSS global
- config dùng chung
- enum dùng chung
- schema database

Chỉ sửa các file trên khi Story thực sự yêu cầu.