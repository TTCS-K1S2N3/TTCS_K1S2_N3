# S1-08: Đặc Tả Kỹ Thuật Backend - Quản Lý Tài Khoản Người Dùng

## 1. Mục tiêu và Phạm vi
- Cung cấp API và Servlet phục vụ quản trị viên quản lý danh sách tài khoản nhân viên trong hệ thống CRM.
- Hỗ trợ tạo mới tài khoản với cơ chế sinh mật khẩu ngẫu nhiên an toàn, mã hóa BCrypt và gửi email thông báo qua giao thức SMTP.
- Tìm kiếm linh hoạt theo họ tên, email, vai trò và trạng thái hoạt động với phân trang phía server (Server-side Pagination).

## 2. Thiết kế Cơ sở dữ liệu & Model
- Bảng `nguoi_dung`: Lưu trữ thông tin tài khoản, mật khẩu băm, cờ trạng thái khóa, số lần đăng nhập sai.
- Bảng `vai_tro` và `vai_tro_nguoi_dung`: Quan hệ n-n phân quyền vai trò.

## 3. Kiến trúc Xử lý
- `NguoiDungServlet`: Tiếp nhận request GET (xem danh sách, tìm kiếm, lọc) và POST (tạo mới, cập nhật, khóa).
- `NguoiDungService`: Thực hiện kiểm tra tính hợp lệ dữ liệu, sinh mật khẩu tạm thời, kích hoạt luồng gửi mail ngầm.
- `EmailService`: Đảm bảo định dạng UTF-8, ẩn mật khẩu trong log hệ thống để tuân thủ bảo mật thông tin.
