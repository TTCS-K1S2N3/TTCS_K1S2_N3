# S4-01: Đặc Tả Backend - Thu Thập Lead Từ Biểu Mẫu Nhúng Trên Website

## 1. Cơ chế Hoạt động
- Cho phép sinh đoạn mã nhúng JavaScript / iFrame gắn vào các website quảng bá, landing page.
- Khách truy cập điền form: Dữ liệu được đẩy về endpoint tiếp nhận công khai (Public Servlet).

## 2. Bảo mật & Chống Spam
- Sinh mã nhúng duy nhất (Token bảo mật) cho từng biểu mẫu để xác định nguồn chiến dịch Marketing.
- Cơ chế Rate Limiting theo địa chỉ IP: Giới hạn số lần gửi trong một khoảng thời gian để ngăn chặn DDOS / bot spam.
- Tự động chuẩn hóa số điện thoại theo định dạng chuẩn Việt Nam (+84 / 0x).
