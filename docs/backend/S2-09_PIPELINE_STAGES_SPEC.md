# S2-09: Đặc Tả Backend - Cấu Hình Giai Đoạn Pipeline & Dự Báo Doanh Số

## 1. Tổng quan
- Thiết lập quy trình bán hàng theo các giai đoạn chuẩn (Phễu bán hàng - Sales Pipeline).
- Mỗi giai đoạn gắn với một tỷ lệ phần trăm xác suất thắng (Win Probability) mặc định từ 0% đến 100%.

## 2. Thuật toán Tính Doanh số Dự báo (Weighted Forecast)
- Công thức: `Doanh số dự báo = Tổng (Giá trị cơ hội * Xác suất giai đoạn / 100)`.
- Cho phép nhân viên kinh doanh điều chỉnh xác suất thủ công kèm theo ghi chú lý do thay đổi.
- Đảm bảo tính toán chính xác số liệu tổng hợp theo từng quý và toàn bộ chi nhánh.
