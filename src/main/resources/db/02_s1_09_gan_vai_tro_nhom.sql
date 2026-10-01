-- ====================================================================
-- MIGRATION S1-09: Gán Vai Trò & Nhóm Kinh Doanh (NO-OP)
-- ====================================================================
-- Baseline schema hiện tại đã chứa đầy đủ cấu trúc cần thiết cho Story S1-09:
-- 1. Bảng quan hệ nhiều-nhiều: `nguoi_dung_vai_tro` (nguoi_dung_id, vai_tro_id)
-- 2. Cột liên kết nhóm kinh doanh: `nguoi_dung.nhom_kinh_doanh_id`
-- 3. Bảng danh mục nhóm kinh doanh: `nhom_kinh_doanh`
-- 4. Bảng vai trò và cột phạm vi tối đa: `vai_tro`, `vai_tro.pham_vi_toi_da`
--
-- File migration này là NO-OP cho môi trường production:
-- Không áp dụng thay đổi cấu trúc bảng và không chèn dữ liệu mẫu (seed/demo data).
-- ====================================================================

-- NO-OP: Không yêu cầu thay đổi schema hoặc chèn dữ liệu mẫu trên môi trường production.
SELECT 1;
