# S4-06: Đặc Tả Backend - Quy Tắc Phân Bổ Lead Tự Động & Hàng Chờ Phân Bổ

## 1. Luồng Xử lý Nghiệp vụ
- Khi lead mới được ghi nhận vào hệ thống từ biểu mẫu hoặc import:
  1. Duyệt qua danh sách quy tắc phân bổ (Lead Assignment Rules) theo thứ tự ưu tiên (Priority order).
  2. Khớp điều kiện theo Khu vực địa lý (Miền Bắc, Trung, Nam) hoặc Ngành nghề.
  3. Áp dụng thuật toán Round-Robin: Xoay vòng đều cho các nhân viên trong nhóm bán hàng được chỉ định.
  4. Nếu không khớp quy tắc nào: Lead được đưa vào Hàng chờ (Lead Queue) để Trưởng nhóm phân công thủ công.

## 2. Cam kết Hiệu năng (SLA)
- Tiến trình phân bổ chạy nền bất đồng bộ (Asynchronous Background Job), hoàn thành phân bổ trong vòng tối đa 5 phút.
