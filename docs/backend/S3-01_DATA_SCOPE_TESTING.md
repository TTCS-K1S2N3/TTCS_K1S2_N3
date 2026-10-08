# S3-01: Kịch Bản Kiểm Thử Phân Quyền Dữ Liệu Khách Hàng

## 1. Kiểm thử chéo giữa các vai trò
- Nhân viên A thử truy cập URL chi tiết khách hàng của nhân viên B: Hệ thống chặn và trả về trang báo lỗi 403 tiếng Việt.
- Trưởng nhóm kiểm tra danh sách: Hiển thị đúng và đủ khách hàng thuộc về tất cả thành viên trong nhóm mình.
- Tìm kiếm và xuất file: Toàn bộ kết quả tìm kiếm tự động lọc theo scope của tài khoản đang đăng nhập.
