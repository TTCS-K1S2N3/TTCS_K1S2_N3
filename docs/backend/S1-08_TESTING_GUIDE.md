# S1-08: Kịch Bản Kiểm Thử Tự Động & Nghiệm Thu (AC Testing)

## 1. Acceptance Criteria cần nghiệm thu
- AC1: Chỉ tài khoản có vai trò Quản trị hệ thống (Admin) mới truy cập được các chức năng CRUD người dùng.
- AC2: Tạo tài khoản với email trùng lặp phải trả về mã lỗi và thông báo thân thiện bằng tiếng Việt.
- AC3: Mật khẩu tạm thời phải gửi thành công qua SMTP và ghi log an toàn không chứa plain-text password.
- AC4: Phân trang danh sách đúng pageSize (10, 25, 50) và giữ nguyên bộ lọc khi chuyển trang.

## 2. Kết quả kiểm thử JUnit
- `NguoiDungServletTest`: 100% test cases pass.
- `EmailUtilTest`: Đảm bảo cấu hình TLS/SSL hoạt động ổn định.
