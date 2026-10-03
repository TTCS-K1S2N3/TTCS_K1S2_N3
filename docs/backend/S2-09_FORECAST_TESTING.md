# S2-09: Kiểm Thử Nghiệp Vụ Tính Toán Dự Báo Doanh Số

## 1. Các trường hợp biên (Edge Cases)
- Cơ hội ở giai đoạn 'Đã đóng - Thắng': Xác suất 100%, doanh số dự báo bằng giá trị thực tế.
- Cơ hội ở giai đoạn 'Đã đóng - Thua': Xác suất 0%, không tính vào doanh số dự báo.
- Thay đổi thứ tự các bước trong Pipeline: Kiểm tra tính toàn vẹn dữ liệu của các cơ hội đang mở.
