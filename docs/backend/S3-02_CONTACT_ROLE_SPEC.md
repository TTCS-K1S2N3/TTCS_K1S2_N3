# S3-02: Đặc Tả Backend - Người Liên Hệ & Vai Trò Trong Quyết Định Mua

## 1. Ý nghĩa Nghiệp vụ
- Một doanh nghiệp có nhiều nhân sự liên hệ (Người ra quyết định, Kế toán, Chuyên viên kỹ thuật, v.v.).
- Định danh chính xác người giữ vai trò 'Decision Maker' (Người ký duyệt hợp đồng) để tối ưu chiến lược đàm phán.

## 2. Thiết kế Dữ liệu
- Bảng `nguoi_lien_he`: Khóa ngoại trỏ về `khach_hang_id`.
- Trường `vai_tro_quyet_dinh`: Enum (DECISION_MAKER, INFLUENCER, USER, CHAMPION, GATEKEEPER).
- Cờ `is_primary`: Đánh dấu người liên hệ đại diện chính.
