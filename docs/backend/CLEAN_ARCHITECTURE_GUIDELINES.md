# Chuẩn Hóa Kiến Trúc Backend CRM - Nhóm 10

## 1. Phân chia tầng trách nhiệm rõ ràng
- `Controller / Servlet`: Chỉ tiếp nhận HTTP request, trích xuất tham số, gọi Service và điều hướng view/JSON.
- `Service`: Chứa toàn bộ Business Logic, Transaction Management, kiểm tra quyền và validation nghiệp vụ.
- `DAO`: Tương tác trực tiếp với cơ sở dữ liệu qua PreparedStatement, quản lý đóng mở Connection an toàn.
- `DTO`: Truyền tải dữ liệu độc lập giữa các tầng, loại bỏ rủi ro rò rỉ các trường nhạy cảm ra ngoài giao diện.

## 2. Tiêu chuẩn Mã nguồn
- Tuân thủ nghiêm ngặt Coding Conventions Java, đặt tên biến rõ ràng, xử lý ngoại lệ tường minh và ghi log có cấu trúc.
