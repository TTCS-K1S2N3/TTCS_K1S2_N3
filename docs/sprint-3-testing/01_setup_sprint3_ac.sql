-- ============================================================
-- BO DU LIEU MAU TEST TOAN BO ACCEPTANCE CRITERIA SPRINT 3
-- Story S3-01 -> S3-09
-- Yeu cau: schema crm_ban_hang canonical da duoc bootstrap va co seed accounts.
--
-- Tai khoan seed can co:
--   sales@crm.vn
--   dtc245200134@ictu.edu.vn
--   teamlead@crm.vn
--   cskh@crm.vn
--   admin@crm.vn
--
-- Script an toan khi chay lai: xoa CHI du lieu prefix AC3_ truoc khi tao moi.
-- ============================================================
SET NAMES utf8mb4;
SET time_zone = '+07:00';
USE crm_ban_hang;

-- ===== 0. CLEANUP FIXTURE CU =====


SET @ac3_ids = NULL;

-- Xoa thong bao lien quan fixture truoc
DELETE tb
FROM thong_bao tb
LEFT JOIN khach_hang kh ON tb.doi_tuong_id = kh.id AND tb.loai_doi_tuong = 'KHACH_HANG'
WHERE kh.ma_khach_hang LIKE 'AC3=_%' ESCAPE '='
   OR tb.tieu_de LIKE '[Cảnh báo rủi ro rời bỏ] Khách hàng %AC3%';

-- Xoa lich su gop neu da test S3-04
DELETE lsg
FROM lich_su_gop_khach_hang lsg
JOIN khach_hang src ON src.id = lsg.khach_hang_nguon_id
WHERE src.ma_khach_hang LIKE 'AC3=_%' ESCAPE '=';

DELETE lsg
FROM lich_su_gop_khach_hang lsg
JOIN khach_hang dst ON dst.id = lsg.khach_hang_dich_id
WHERE dst.ma_khach_hang LIKE 'AC3=_%' ESCAPE '=';

DELETE FROM yeu_cau_ho_tro WHERE ma_yeu_cau LIKE 'AC3=_%' ESCAPE '=';
DELETE FROM tep_dinh_kem WHERE ten_file_luu LIKE 'AC3=_%' ESCAPE '=';
DELETE FROM hoat_dong WHERE ma_hoat_dong LIKE 'AC3=_%' ESCAPE '=';

DELETE FROM hop_dong WHERE so_hop_dong LIKE 'AC3=_%' ESCAPE '=';
DELETE bgpb
FROM bao_gia_phien_ban bgpb
JOIN bao_gia bg ON bg.id = bgpb.bao_gia_id
WHERE bg.ma_bao_gia LIKE 'AC3=_%' ESCAPE '=';
DELETE FROM bao_gia WHERE ma_bao_gia LIKE 'AC3=_%' ESCAPE '=';
DELETE FROM co_hoi WHERE ma_co_hoi LIKE 'AC3=_%' ESCAPE '=';

DELETE FROM nguoi_lien_he WHERE email LIKE '%@ac3.test';
DELETE FROM bo_loc_da_luu WHERE ten_bo_loc LIKE 'AC3 - %';

DELETE FROM khach_hang WHERE ma_khach_hang LIKE 'AC3=_%' ESCAPE '=';

-- Danh muc test chi xoa sau khi khach hang da bi xoa
DELETE FROM nganh_nghe WHERE ma_nganh LIKE 'AC3=_%' ESCAPE '=';
DELETE FROM quy_mo_doanh_nghiep WHERE ma_quy_mo LIKE 'AC3=_%' ESCAPE '=';
DELETE FROM khu_vuc_dia_ly WHERE ma_khu_vuc LIKE 'AC3=_%' ESCAPE '=';




-- ===== 1. PRE-FLIGHT / LOOKUP SEED =====
SET @sales1   := (SELECT id FROM nguoi_dung WHERE email='sales@crm.vn' LIMIT 1);
SET @sales2   := (SELECT id FROM nguoi_dung WHERE email='dtc245200134@ictu.edu.vn' LIMIT 1);
SET @teamlead := (SELECT id FROM nguoi_dung WHERE email='teamlead@crm.vn' LIMIT 1);
SET @cskh     := (SELECT id FROM nguoi_dung WHERE email='cskh@crm.vn' LIMIT 1);
SET @admin    := (SELECT id FROM nguoi_dung WHERE email='admin@crm.vn' LIMIT 1);

SET @nhom_bac  := (SELECT id FROM nhom_kinh_doanh WHERE ma_nhom='KD_MIEN_BAC' LIMIT 1);
SET @nhom_cskh := (SELECT id FROM nhom_kinh_doanh WHERE ma_nhom='PHONG_CSKH' LIMIT 1);

SELECT @sales1 AS sales_1, @sales2 AS sales_2, @teamlead AS team_lead,
       @cskh AS cskh, @admin AS admin, @nhom_bac AS nhom_bac, @nhom_cskh AS nhom_cskh;

-- Neu co gia tri NULL o dong tren, hay bootstrap lai DB bang canonical schema truoc khi chay fixture.


-- ===== 2. DANH MUC TEST CHO S3-01 / S3-07 =====
INSERT INTO nganh_nghe (ma_nganh, ten_nganh, thu_tu_hien_thi, hoat_dong) VALUES
('AC3_CN', 'Công nghệ thông tin - Test AC', 901, 1),
('AC3_LOG', 'Logistics - Test AC', 902, 1),
('AC3_RETAIL', 'Bán lẻ - Test AC', 903, 1),
('AC3_DV', 'Dịch vụ - Test AC', 904, 1);

INSERT INTO quy_mo_doanh_nghiep (ma_quy_mo, ten_quy_mo, thu_tu_hien_thi, hoat_dong) VALUES
('AC3_SMALL', 'Dưới 50 người - Test AC', 901, 1),
('AC3_MEDIUM', '50-200 người - Test AC', 902, 1),
('AC3_LARGE', 'Trên 200 người - Test AC', 903, 1);

INSERT INTO khu_vuc_dia_ly (ma_khu_vuc, ten_khu_vuc, loai_khu_vuc, thu_tu_hien_thi, hoat_dong) VALUES
('AC3_HN', 'Hà Nội - Test AC', 'TINH_THANH', 901, 1),
('AC3_HCM', 'TP. Hồ Chí Minh - Test AC', 'TINH_THANH', 902, 1);

SET @nganh_cn     := (SELECT id FROM nganh_nghe WHERE ma_nganh='AC3_CN');
SET @nganh_log    := (SELECT id FROM nganh_nghe WHERE ma_nganh='AC3_LOG');
SET @nganh_retail := (SELECT id FROM nganh_nghe WHERE ma_nganh='AC3_RETAIL');
SET @nganh_dv     := (SELECT id FROM nganh_nghe WHERE ma_nganh='AC3_DV');

SET @qm_small  := (SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='AC3_SMALL');
SET @qm_medium := (SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='AC3_MEDIUM');
SET @qm_large  := (SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='AC3_LARGE');

SET @kv_hn  := (SELECT id FROM khu_vuc_dia_ly WHERE ma_khu_vuc='AC3_HN');
SET @kv_hcm := (SELECT id FROM khu_vuc_dia_ly WHERE ma_khu_vuc='AC3_HCM');

-- ===== 3. KHACH HANG MAU =====
INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_KH_001', 'Công ty Cổ phần An Phát Tech', LOWER('Công ty Cổ phần An Phát Tech'), '0109000001', @nganh_cn, @qm_small,
 'https://anphat-tech.vn', LOWER(REPLACE(REPLACE('https://anphat-tech.vn', 'https://www.', ''), 'https://', '')),
 'Cầu Giấy, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 350000000, 'TIEM_NANG', 0, CURRENT_TIMESTAMP - INTERVAL 7 DAY, 'S3-01/02/06/07: khách của sales@crm.vn', CURRENT_DATE - INTERVAL 120 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_KH_002', 'Công ty TNHH Bắc Việt Logistics', LOWER('Công ty TNHH Bắc Việt Logistics'), '0109000002', @nganh_log, @qm_medium,
 'https://bacviet-logistics.vn', LOWER(REPLACE(REPLACE('https://bacviet-logistics.vn', 'https://www.', ''), 'https://', '')),
 'Long Biên, Hà Nội', @kv_hn, @sales2, @nhom_bac,
 800000000, 'DANG_GIAO_DICH', 0, CURRENT_TIMESTAMP - INTERVAL 20 DAY, 'S3-01/02: khách của sales thứ hai cùng nhóm', CURRENT_DATE - INTERVAL 150 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_KH_003', 'Công ty Hưng Thịnh Retail', LOWER('Công ty Hưng Thịnh Retail'), '0109000003', @nganh_retail, @qm_large,
 'https://hungthinh-retail.vn', LOWER(REPLACE(REPLACE('https://hungthinh-retail.vn', 'https://www.', ''), 'https://', '')),
 'Quận 1, TP.HCM', @kv_hcm, @sales1, @nhom_bac,
 1800000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 12 DAY, 'S3-01: trạng thái KHACH_HANG', CURRENT_DATE - INTERVAL 200 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_KH_004', 'Công ty Di Sản Cũ', LOWER('Công ty Di Sản Cũ'), '0109000004', @nganh_retail, @qm_small,
 'https://disancu.vn', LOWER(REPLACE(REPLACE('https://disancu.vn', 'https://www.', ''), 'https://', '')),
 'Ba Đình, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 120000000, 'NGUNG_HOP_TAC', 0, CURRENT_TIMESTAMP - INTERVAL 180 DAY, 'S3-01: trạng thái NGUNG_HOP_TAC', CURRENT_DATE - INTERVAL 400 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_360_001', 'Công ty Demo 360 Toàn Cảnh', LOWER('Công ty Demo 360 Toàn Cảnh'), '0109360001', @nganh_cn, @qm_large,
 'https://demo360.vn', LOWER(REPLACE(REPLACE('https://demo360.vn', 'https://www.', ''), 'https://', '')),
 'Nam Từ Liêm, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 2500000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 2 DAY, 'S3-03: khách chính để test trang 360 và 500 hoạt động', CURRENT_DATE - INTERVAL 300 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_DUP_A', 'Công ty Cổ phần Sao Việt', LOWER('Công ty Cổ phần Sao Việt'), '0109400001', @nganh_cn, @qm_medium,
 'https://www.saoviet.vn', LOWER(REPLACE(REPLACE('https://www.saoviet.vn', 'https://www.', ''), 'https://', '')),
 'Thanh Xuân, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 900000000, 'DANG_GIAO_DICH', 0, CURRENT_TIMESTAMP - INTERVAL 25 DAY, 'S3-04: bản ghi trùng A', CURRENT_DATE - INTERVAL 220 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_DUP_B', 'CTY CP Sao Viet', LOWER('CTY CP Sao Viet'), '0109400002', @nganh_cn, @qm_medium,
 'http://saoviet.vn/about', LOWER(REPLACE(REPLACE('http://saoviet.vn/about', 'https://www.', ''), 'https://', '')),
 'Đống Đa, Hà Nội', @kv_hn, @sales2, @nhom_bac,
 700000000, 'DANG_GIAO_DICH', 0, CURRENT_TIMESTAMP - INTERVAL 28 DAY, 'S3-04: bản ghi trùng B; tên + website trùng, owner khác', CURRENT_DATE - INTERVAL 210 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_PARENT', 'Tập đoàn AC3 Holdings', LOWER('Tập đoàn AC3 Holdings'), '0109500001', @nganh_cn, @qm_large,
 'https://ac3holdings.vn', LOWER(REPLACE(REPLACE('https://ac3holdings.vn', 'https://www.', ''), 'https://', '')),
 'Hoàn Kiếm, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 4000000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 'S3-05: công ty mẹ', CURRENT_DATE - INTERVAL 500 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CHILD_A', 'AC3 Holdings - Công ty Con A', LOWER('AC3 Holdings - Công ty Con A'), '0109500002', @nganh_cn, @qm_medium,
 'https://a.ac3holdings.vn', LOWER(REPLACE(REPLACE('https://a.ac3holdings.vn', 'https://www.', ''), 'https://', '')),
 'Cầu Giấy, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 1000000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 15 DAY, 'S3-05: đã là công ty con', CURRENT_DATE - INTERVAL 450 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CHILD_B', 'AC3 Holdings - Công ty Con B', LOWER('AC3 Holdings - Công ty Con B'), '0109500003', @nganh_cn, @qm_medium,
 'https://b.ac3holdings.vn', LOWER(REPLACE(REPLACE('https://b.ac3holdings.vn', 'https://www.', ''), 'https://', '')),
 'Thủ Đức, TP.HCM', @kv_hcm, @sales1, @nhom_bac,
 1200000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 16 DAY, 'S3-05: ứng viên gắn làm công ty con khi test AC1', CURRENT_DATE - INTERVAL 440 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_RISK_TRIGGER', 'Công ty Rủi Ro Chờ Kích Hoạt', LOWER('Công ty Rủi Ro Chờ Kích Hoạt'), '0109800001', @nganh_dv, @qm_medium,
 'https://risk-trigger.vn', LOWER(REPLACE(REPLACE('https://risk-trigger.vn', 'https://www.', ''), 'https://', '')),
 'Hà Đông, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 650000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 14 DAY, 'S3-08: có sẵn 2 ticket mở, thêm ticket thứ 3 để tự gắn cờ', CURRENT_DATE - INTERVAL 160 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_RISK_FLAGGED', 'Công ty Rủi Ro Đã Gắn Cờ', LOWER('Công ty Rủi Ro Đã Gắn Cờ'), '0109800002', @nganh_dv, @qm_medium,
 'https://risk-flagged.vn', LOWER(REPLACE(REPLACE('https://risk-flagged.vn', 'https://www.', ''), 'https://', '')),
 'Hai Bà Trưng, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 720000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 17 DAY, 'S3-08: dữ liệu sẵn 3 ticket mở + co_rui_ro=1 để test hiển thị', CURRENT_DATE - INTERVAL 170 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CARE_HIGH', 'Khách Chăm Sóc Giá Trị Cao', LOWER('Khách Chăm Sóc Giá Trị Cao'), '0109900001', @nganh_dv, @qm_large,
 'https://care-high.vn', LOWER(REPLACE(REPLACE('https://care-high.vn', 'https://www.', ''), 'https://', '')),
 'Hà Nội', @kv_hn, @cskh, @nhom_cskh,
 900000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 61 DAY, 'S3-09: 61 ngày chưa liên hệ, HĐ 900 triệu', CURRENT_DATE - INTERVAL 400 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CARE_MED', 'Khách Chăm Sóc Giá Trị Vừa', LOWER('Khách Chăm Sóc Giá Trị Vừa'), '0109900002', @nganh_dv, @qm_medium,
 'https://care-med.vn', LOWER(REPLACE(REPLACE('https://care-med.vn', 'https://www.', ''), 'https://', '')),
 'Hà Nội', @kv_hn, @cskh, @nhom_cskh,
 500000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 45 DAY, 'S3-09: 45 ngày chưa liên hệ, HĐ 500 triệu', CURRENT_DATE - INTERVAL 350 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CARE_LOW', 'Khách Chăm Sóc Giá Trị Thấp', LOWER('Khách Chăm Sóc Giá Trị Thấp'), '0109900003', @nganh_dv, @qm_small,
 'https://care-low.vn', LOWER(REPLACE(REPLACE('https://care-low.vn', 'https://www.', ''), 'https://', '')),
 'Hà Nội', @kv_hn, @cskh, @nhom_cskh,
 200000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 31 DAY, 'S3-09: 31 ngày chưa liên hệ, HĐ 200 triệu', CURRENT_DATE - INTERVAL 330 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CARE_RECENT', 'Khách Mới Liên Hệ Gần Đây', LOWER('Khách Mới Liên Hệ Gần Đây'), '0109900004', @nganh_dv, @qm_large,
 'https://care-recent.vn', LOWER(REPLACE(REPLACE('https://care-recent.vn', 'https://www.', ''), 'https://', '')),
 'Hà Nội', @kv_hn, @cskh, @nhom_cskh,
 1100000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 'S3-09: HĐ lớn nhưng chỉ 10 ngày, không được vào danh sách N=30', CURRENT_DATE - INTERVAL 300 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_CARE_NEVER', 'Khách Chưa Từng Tương Tác', LOWER('Khách Chưa Từng Tương Tác'), '0109900005', @nganh_dv, @qm_medium,
 'https://care-never.vn', LOWER(REPLACE(REPLACE('https://care-never.vn', 'https://www.', ''), 'https://', '')),
 'Hà Nội', @kv_hn, @cskh, @nhom_cskh,
 300000000, 'KHACH_HANG', 0, NULL, 'S3-09: lan_tuong_tac_cuoi NULL, ngày tạo 90 ngày trước', CURRENT_DATE - INTERVAL 90 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_FILTER_TECH', 'Công ty Lọc Công Nghệ Hà Nội', LOWER('Công ty Lọc Công Nghệ Hà Nội'), '0109700001', @nganh_cn, @qm_small,
 'https://filter-tech.vn', LOWER(REPLACE(REPLACE('https://filter-tech.vn', 'https://www.', ''), 'https://', '')),
 'Cầu Giấy, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 450000000, 'TIEM_NANG', 0, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 'S3-07: lọc công nghệ + nhỏ + Hà Nội', CURRENT_DATE - INTERVAL 100 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_FILTER_RETAIL', 'Công ty Lọc Bán Lẻ TP.HCM', LOWER('Công ty Lọc Bán Lẻ TP.HCM'), '0109700002', @nganh_retail, @qm_large,
 'https://filter-retail.vn', LOWER(REPLACE(REPLACE('https://filter-retail.vn', 'https://www.', ''), 'https://', '')),
 'Quận 7, TP.HCM', @kv_hcm, @sales2, @nhom_bac,
 1500000000, 'KHACH_HANG', 0, CURRENT_TIMESTAMP - INTERVAL 9 DAY, 'S3-07: lọc bán lẻ + lớn + HCM', CURRENT_DATE - INTERVAL 130 DAY);

INSERT INTO khach_hang
(ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id,
 website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id,
 doanh_thu_uoc_tinh, trang_thai, co_rui_ro, lan_tuong_tac_cuoi, mo_ta_chi_tiet, ngay_tao)
VALUES
('AC3_FILTER_LOG', 'Công ty Lọc Logistics Hà Nội', LOWER('Công ty Lọc Logistics Hà Nội'), '0109700003', @nganh_log, @qm_medium,
 'https://filter-log.vn', LOWER(REPLACE(REPLACE('https://filter-log.vn', 'https://www.', ''), 'https://', '')),
 'Long Biên, Hà Nội', @kv_hn, @sales1, @nhom_bac,
 980000000, 'DANG_GIAO_DICH', 0, CURRENT_TIMESTAMP - INTERVAL 11 DAY, 'S3-07: tìm theo MST/owner/trạng thái', CURRENT_DATE - INTERVAL 140 DAY);


SET @parent_id := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_PARENT');
UPDATE khach_hang
SET cong_ty_me_id=@parent_id
WHERE ma_khach_hang='AC3_CHILD_A';

-- S3-08: mot ban ghi da gan co san de test AC3 hien thi
UPDATE khach_hang
SET co_rui_ro=1, rui_ro_cap_nhat_luc=CURRENT_TIMESTAMP - INTERVAL 1 HOUR
WHERE ma_khach_hang='AC3_RISK_FLAGGED';


-- ===== 4. S3-02 NGUOI LIEN HE + VAI TRO QUYET DINH =====
SET @kh1 := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_KH_001');
SET @kh2 := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_KH_002');
SET @kh360 := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_360_001');

INSERT INTO nguoi_lien_he
(khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, vai_tro_quyet_dinh, la_dau_moi_chinh, trang_thai)
VALUES
(@kh1, 'Nguyễn Minh Quyết', 'Tổng Giám đốc', 'quyet.dinh@ac3.test', '0988000001', 'NGUOI_QUYET_DINH', 1, 'DANG_HOAT_DONG'),
(@kh1, 'Trần Anh Hưởng', 'Giám đốc CNTT', 'anh.huong@ac3.test', '0988000002', 'NGUOI_ANH_HUONG', 0, 'DANG_HOAT_DONG'),
(@kh1, 'Lê Dùng Cuối', 'Trưởng phòng Vận hành', 'dung.cuoi@ac3.test', '0988000003', 'NGUOI_DUNG_CUOI', 0, 'DANG_HOAT_DONG'),
(@kh1, 'Phạm Cản Trở', 'Kiểm soát nội bộ', 'can.tro@ac3.test', '0988000004', 'NGUOI_CAN_TRO', 0, 'DANG_HOAT_DONG'),
(@kh2, 'Đỗ Chuyển Công Ty', 'Quản lý mua hàng', 'move.contact@ac3.test', '0988000099', 'NGUOI_ANH_HUONG', 1, 'DANG_HOAT_DONG'),

(@kh360, 'Vũ Quyết Định 360', 'CEO', 'ceo360@ac3.test', '0977000001', 'NGUOI_QUYET_DINH', 1, 'DANG_HOAT_DONG'),
(@kh360, 'Ngô Ảnh Hưởng 360', 'CTO', 'cto360@ac3.test', '0977000002', 'NGUOI_ANH_HUONG', 0, 'DANG_HOAT_DONG'),
(@kh360, 'Phan Người Dùng 360', 'Ops Lead', 'ops360@ac3.test', '0977000003', 'NGUOI_DUNG_CUOI', 0, 'DANG_HOAT_DONG');

SET @contact_move := (SELECT id FROM nguoi_lien_he WHERE email='move.contact@ac3.test');
INSERT INTO hoat_dong
(ma_hoat_dong, tieu_de, loai_hoat_dong, khach_hang_id, nguoi_lien_he_id,
 nguoi_phu_trach_id, nhom_kinh_doanh_id, thoi_gian_bat_dau, thoi_gian_ket_thuc,
 trang_thai, noi_dung, ket_qua, ngay_tao)
VALUES
('AC3_ACT_CONTACT_HISTORY', 'Lịch sử trước khi chuyển công ty', 'CUOC_GOI', @kh2, @contact_move,
 @sales2, @nhom_bac, CURRENT_TIMESTAMP - INTERVAL 40 DAY, CURRENT_TIMESTAMP - INTERVAL 40 DAY,
 'HOAN_THANH', 'Hoạt động lịch sử phải còn sau khi chuyển người liên hệ sang công ty khác.', 'Đã trao đổi', CURRENT_DATE - INTERVAL 40 DAY);

-- ===== 5. S3-03 / S3-05 / S3-09 HOP DONG =====

-- Contract fixture 360 for AC3_360_001
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_360_001');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_360', 'Cơ hội hợp đồng 360', @kh_contract, @kh_contract_owner, @kh_contract_group,
 750000000, 100, CURRENT_DATE - INTERVAL 120 DAY, 'DONG_THANG', 750000000,
 CURRENT_DATE - INTERVAL 120 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 150 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_360', 'Báo giá test 360', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 750000000, 'DA_DUYET', 750000000, 0, 750000000, 0, CURRENT_DATE - INTERVAL 125 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 750000000, 0, 750000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_360', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 120 DAY, CURRENT_DATE - INTERVAL 120 DAY,
 CURRENT_DATE + INTERVAL 245 DAY,
 750000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture PARENT for AC3_PARENT
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_PARENT');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_PARENT', 'Cơ hội hợp đồng PARENT', @kh_contract, @kh_contract_owner, @kh_contract_group,
 1000000000, 100, CURRENT_DATE - INTERVAL 200 DAY, 'DONG_THANG', 1000000000,
 CURRENT_DATE - INTERVAL 200 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 230 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_PARENT', 'Báo giá test PARENT', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 1000000000, 'DA_DUYET', 1000000000, 0, 1000000000, 0, CURRENT_DATE - INTERVAL 205 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 1000000000, 0, 1000000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_PARENT', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 200 DAY, CURRENT_DATE - INTERVAL 200 DAY,
 CURRENT_DATE + INTERVAL 165 DAY,
 1000000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CHILDA for AC3_CHILD_A
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CHILD_A');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CHILDA', 'Cơ hội hợp đồng CHILDA', @kh_contract, @kh_contract_owner, @kh_contract_group,
 400000000, 100, CURRENT_DATE - INTERVAL 180 DAY, 'DONG_THANG', 400000000,
 CURRENT_DATE - INTERVAL 180 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 210 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CHILDA', 'Báo giá test CHILDA', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 400000000, 'DA_DUYET', 400000000, 0, 400000000, 0, CURRENT_DATE - INTERVAL 185 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 400000000, 0, 400000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CHILDA', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 180 DAY, CURRENT_DATE - INTERVAL 180 DAY,
 CURRENT_DATE + INTERVAL 185 DAY,
 400000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CHILDB for AC3_CHILD_B
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CHILD_B');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CHILDB', 'Cơ hội hợp đồng CHILDB', @kh_contract, @kh_contract_owner, @kh_contract_group,
 600000000, 100, CURRENT_DATE - INTERVAL 160 DAY, 'DONG_THANG', 600000000,
 CURRENT_DATE - INTERVAL 160 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 190 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CHILDB', 'Báo giá test CHILDB', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 600000000, 'DA_DUYET', 600000000, 0, 600000000, 0, CURRENT_DATE - INTERVAL 165 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 600000000, 0, 600000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CHILDB', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 160 DAY, CURRENT_DATE - INTERVAL 160 DAY,
 CURRENT_DATE + INTERVAL 205 DAY,
 600000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CAREHIGH for AC3_CARE_HIGH
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CARE_HIGH');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CAREHIGH', 'Cơ hội hợp đồng CAREHIGH', @kh_contract, @kh_contract_owner, @kh_contract_group,
 900000000, 100, CURRENT_DATE - INTERVAL 240 DAY, 'DONG_THANG', 900000000,
 CURRENT_DATE - INTERVAL 240 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 270 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CAREHIGH', 'Báo giá test CAREHIGH', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 900000000, 'DA_DUYET', 900000000, 0, 900000000, 0, CURRENT_DATE - INTERVAL 245 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 900000000, 0, 900000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CAREHIGH', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 240 DAY, CURRENT_DATE - INTERVAL 240 DAY,
 CURRENT_DATE + INTERVAL 125 DAY,
 900000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CAREMED for AC3_CARE_MED
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CARE_MED');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CAREMED', 'Cơ hội hợp đồng CAREMED', @kh_contract, @kh_contract_owner, @kh_contract_group,
 500000000, 100, CURRENT_DATE - INTERVAL 220 DAY, 'DONG_THANG', 500000000,
 CURRENT_DATE - INTERVAL 220 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 250 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CAREMED', 'Báo giá test CAREMED', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 500000000, 'DA_DUYET', 500000000, 0, 500000000, 0, CURRENT_DATE - INTERVAL 225 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 500000000, 0, 500000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CAREMED', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 220 DAY, CURRENT_DATE - INTERVAL 220 DAY,
 CURRENT_DATE + INTERVAL 145 DAY,
 500000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CARELOW for AC3_CARE_LOW
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CARE_LOW');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CARELOW', 'Cơ hội hợp đồng CARELOW', @kh_contract, @kh_contract_owner, @kh_contract_group,
 200000000, 100, CURRENT_DATE - INTERVAL 210 DAY, 'DONG_THANG', 200000000,
 CURRENT_DATE - INTERVAL 210 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 240 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CARELOW', 'Báo giá test CARELOW', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 200000000, 'DA_DUYET', 200000000, 0, 200000000, 0, CURRENT_DATE - INTERVAL 215 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 200000000, 0, 200000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CARELOW', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 210 DAY, CURRENT_DATE - INTERVAL 210 DAY,
 CURRENT_DATE + INTERVAL 155 DAY,
 200000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CARERECENT for AC3_CARE_RECENT
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CARE_RECENT');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CARERECENT', 'Cơ hội hợp đồng CARERECENT', @kh_contract, @kh_contract_owner, @kh_contract_group,
 1100000000, 100, CURRENT_DATE - INTERVAL 190 DAY, 'DONG_THANG', 1100000000,
 CURRENT_DATE - INTERVAL 190 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 220 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CARERECENT', 'Báo giá test CARERECENT', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 1100000000, 'DA_DUYET', 1100000000, 0, 1100000000, 0, CURRENT_DATE - INTERVAL 195 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 1100000000, 0, 1100000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CARERECENT', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 190 DAY, CURRENT_DATE - INTERVAL 190 DAY,
 CURRENT_DATE + INTERVAL 175 DAY,
 1100000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- Contract fixture CARENEVER for AC3_CARE_NEVER
SET @kh_contract := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_CARE_NEVER');
SET @kh_contract_owner := (SELECT nguoi_so_huu_id FROM khach_hang WHERE id=@kh_contract);
SET @kh_contract_group := (SELECT nhom_kinh_doanh_id FROM khach_hang WHERE id=@kh_contract);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, gia_tri_chot_thuc_te,
 ngay_ky, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_HD_CARENEVER', 'Cơ hội hợp đồng CARENEVER', @kh_contract, @kh_contract_owner, @kh_contract_group,
 300000000, 100, CURRENT_DATE - INTERVAL 170 DAY, 'DONG_THANG', 300000000,
 CURRENT_DATE - INTERVAL 170 DAY, 'Cơ hội fixture tạo hợp đồng test.', CURRENT_DATE - INTERVAL 200 DAY, @kh_contract_owner);
SET @ch_contract := LAST_INSERT_ID();

INSERT INTO bao_gia
(ma_bao_gia, tieu_de, co_hoi_id, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 phien_ban, tong_tien, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, ngay_tao, created_by)
VALUES
('AC3_BG_CARENEVER', 'Báo giá test CARENEVER', @ch_contract, @kh_contract, @kh_contract_owner, @kh_contract_group,
 1, 300000000, 'DA_DUYET', 300000000, 0, 300000000, 0, CURRENT_DATE - INTERVAL 175 DAY, @kh_contract_owner);
SET @bg_contract := LAST_INSERT_ID();

INSERT INTO bao_gia_phien_ban
(bao_gia_id, so_phien_ban, trang_thai, tong_truoc_chiet_khau, tong_chiet_khau,
 tong_sau_chiet_khau, ty_le_chiet_khau_tong, snapshot_json, tao_boi_id)
VALUES
(@bg_contract, 1, 'DA_DUYET', 300000000, 0, 300000000, 0,
 JSON_OBJECT('fixture','AC3','story','S3'), @kh_contract_owner);
SET @bgpb_contract := LAST_INSERT_ID();

INSERT INTO hop_dong
(so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, khach_hang_id,
 ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, dieu_khoan_thanh_toan,
 trang_thai, created_by)
VALUES
('AC3_HD_CARENEVER', @bg_contract, @bgpb_contract, @ch_contract, @kh_contract,
 CURRENT_DATE - INTERVAL 170 DAY, CURRENT_DATE - INTERVAL 170 DAY,
 CURRENT_DATE + INTERVAL 195 DAY,
 300000000, 'Thanh toán theo điều khoản test AC', 'DA_KY', @kh_contract_owner);


-- ===== 6. S3-03 TRANG 360: CO HOI MO, DONG, FILE, 500 HOAT DONG =====
SET @kh360 := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_360_001');
SET @nlh360 := (SELECT id FROM nguoi_lien_he WHERE email='ceo360@ac3.test');

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_lien_he_chinh_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, ngay_chot_du_kien, trang_thai, mo_ta_chi_tiet, ngay_tao, created_by)
VALUES
('AC3_CH_360_OPEN_1', 'Mở rộng CRM giai đoạn 2', @kh360, @nlh360, @sales1, @nhom_bac, 300000000, 60,
 CURRENT_DATE + INTERVAL 30 DAY, 'MO', 'Cơ hội đang mở số 1', CURRENT_DATE - INTERVAL 20 DAY, @sales1),
('AC3_CH_360_OPEN_2', 'Tích hợp BI nâng cao', @kh360, @nlh360, @sales1, @nhom_bac, 200000000, 40,
 CURRENT_DATE + INTERVAL 45 DAY, 'MO', 'Cơ hội đang mở số 2', CURRENT_DATE - INTERVAL 15 DAY, @sales1),
('AC3_CH_360_LOST', 'Gói đào tạo bổ sung', @kh360, @nlh360, @sales1, @nhom_bac, 100000000, 0,
 CURRENT_DATE - INTERVAL 60 DAY, 'DONG_THUA', 'Cơ hội đã đóng thua', CURRENT_DATE - INTERVAL 100 DAY, @sales1);

INSERT INTO tep_dinh_kem
(khach_hang_id, loai_tep, ten_file_goc, ten_file_luu, duong_dan, mime_type, kich_thuoc_byte, nguoi_tai_len_id)
VALUES
(@kh360, 'BROCHURE', 'brochure-demo-360.pdf', 'AC3_360_brochure.pdf', 'test-data/AC3_360_brochure.pdf', 'application/pdf', 20480, @sales1),
(@kh360, 'BIEN_BAN', 'bien-ban-hop-demo-360.txt', 'AC3_360_bienban.txt', 'test-data/AC3_360_bienban.txt', 'text/plain', 4096, @sales1);

-- 500 hoạt động chính xác: 10 x 10 x 5 = 500
INSERT INTO hoat_dong
(ma_hoat_dong, tieu_de, loai_hoat_dong, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 thoi_gian_bat_dau, thoi_gian_ket_thuc, trang_thai, noi_dung, ket_qua, la_ghi_nhan_qua_khu, ngay_tao)
SELECT
 CONCAT('AC3_ACT_360_', LPAD((h.n*100 + t.n*10 + u.n + 1), 3, '0')),
 CONCAT('Hoạt động timeline #', (h.n*100 + t.n*10 + u.n + 1)),
 CASE MOD((h.n*100 + t.n*10 + u.n),4)
      WHEN 0 THEN 'CUOC_GOI' WHEN 1 THEN 'EMAIL' WHEN 2 THEN 'GAP_MAT' ELSE 'GHI_CHU' END,
 @kh360, @sales1, @nhom_bac,
 TIMESTAMPADD(HOUR, -(h.n*100 + t.n*10 + u.n + 1), CURRENT_TIMESTAMP),
 TIMESTAMPADD(HOUR, -(h.n*100 + t.n*10 + u.n), CURRENT_TIMESTAMP),
 'HOAN_THANH',
 'Dữ liệu hiệu năng AC3 - trang 360.',
 'Hoàn thành',
 1,
 DATE(TIMESTAMPADD(HOUR, -(h.n*100 + t.n*10 + u.n + 1), CURRENT_TIMESTAMP))
FROM
(SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
 SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) u
CROSS JOIN
(SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
 SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t
CROSS JOIN
(SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) h;


-- ===== 7. S3-04 CAP KHACH HANG TRUNG + DU LIEU LIEN QUAN =====
SET @dupA := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_DUP_A');
SET @dupB := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_DUP_B');

INSERT INTO nguoi_lien_he
(khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, vai_tro_quyet_dinh, la_dau_moi_chinh)
VALUES
(@dupA, 'Liên hệ Sao Việt A', 'Giám đốc', 'dup.a@ac3.test', '0966000001', 'NGUOI_QUYET_DINH', 1),
(@dupB, 'Liên hệ Sao Việt B', 'CTO', 'dup.b@ac3.test', '0966000002', 'NGUOI_ANH_HUONG', 1);

INSERT INTO co_hoi
(ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 gia_tri_du_kien, xac_suat, trang_thai, ngay_tao, created_by)
VALUES
('AC3_CH_DUP_A', 'Cơ hội bản ghi trùng A', @dupA, @sales1, @nhom_bac, 250000000, 50, 'MO', CURRENT_DATE - INTERVAL 40 DAY, @sales1),
('AC3_CH_DUP_B', 'Cơ hội bản ghi trùng B', @dupB, @sales2, @nhom_bac, 350000000, 60, 'MO', CURRENT_DATE - INTERVAL 35 DAY, @sales2);

INSERT INTO hoat_dong
(ma_hoat_dong, tieu_de, loai_hoat_dong, khach_hang_id, nguoi_phu_trach_id, nhom_kinh_doanh_id,
 thoi_gian_bat_dau, trang_thai, noi_dung, ngay_tao)
VALUES
('AC3_ACT_DUP_A', 'Gọi khách bản ghi A', 'CUOC_GOI', @dupA, @sales1, @nhom_bac,
 CURRENT_TIMESTAMP - INTERVAL 10 DAY, 'HOAN_THANH', 'Dữ liệu phải được chuyển khi gộp.', CURRENT_DATE - INTERVAL 10 DAY),
('AC3_ACT_DUP_B', 'Email khách bản ghi B', 'EMAIL', @dupB, @sales2, @nhom_bac,
 CURRENT_TIMESTAMP - INTERVAL 9 DAY, 'HOAN_THANH', 'Dữ liệu phải được giữ sau gộp.', CURRENT_DATE - INTERVAL 9 DAY);

INSERT INTO tep_dinh_kem
(khach_hang_id, loai_tep, ten_file_goc, ten_file_luu, duong_dan, mime_type, kich_thuoc_byte, nguoi_tai_len_id)
VALUES
(@dupA, 'TAI_LIEU', 'dup-a.txt', 'AC3_DUP_A_file.txt', 'test-data/AC3_DUP_A_file.txt', 'text/plain', 1024, @sales1),
(@dupB, 'TAI_LIEU', 'dup-b.txt', 'AC3_DUP_B_file.txt', 'test-data/AC3_DUP_B_file.txt', 'text/plain', 1024, @sales2);


-- ===== 8. S3-07 DU LIEU TIM KIEM / LOC / BO LOC DA LUU =====
SET @filterTech := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_FILTER_TECH');
INSERT INTO nguoi_lien_he
(khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, vai_tro_quyet_dinh, la_dau_moi_chinh)
VALUES
(@filterTech, 'Người Liên Hệ Tìm Theo SĐT', 'Mua hàng', 'search.phone@ac3.test', '0988123456', 'NGUOI_ANH_HUONG', 1);

INSERT INTO bo_loc_da_luu
(nguoi_dung_id, loai_doi_tuong, ten_bo_loc, tieu_chi_json, mac_dinh)
VALUES
(@sales1, 'KHACH_HANG', 'AC3 - Công nghệ Hà Nội',
 JSON_OBJECT('trangThai','TIEM_NANG','nganhNgheId',@nganh_cn,'quyMoId',@qm_small,
             'khuVucId',@kv_hn,'nguoiSoHuuId',@sales1,'phamVi','CA_NHAN'), 1);


-- ===== 9. S3-08 YEU CAU HO TRO + CO RUI RO =====
INSERT INTO cau_hinh_he_thong
(ma_cau_hinh, nhom_cau_hinh, kieu_du_lieu, gia_tri, mo_ta, updated_by)
VALUES
('NGUONG_YEU_CAU_HO_TRO_RUI_RO', 'KHACH_HANG', 'INTEGER', '3',
 'Fixture AC: đủ 3 yêu cầu chưa xử lý thì gắn cờ rủi ro', @admin)
ON DUPLICATE KEY UPDATE gia_tri='3', updated_by=@admin, updated_at=CURRENT_TIMESTAMP;

SET @riskTrigger := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_RISK_TRIGGER');
SET @riskFlagged := (SELECT id FROM khach_hang WHERE ma_khach_hang='AC3_RISK_FLAGGED');

INSERT INTO yeu_cau_ho_tro
(ma_yeu_cau, khach_hang_id, nguoi_xu_ly_id, tieu_de, noi_dung, muc_uu_tien, trang_thai, tao_luc)
VALUES
('AC3_YC_TRIGGER_01', @riskTrigger, @cskh, 'Lỗi đăng nhập sau bán', 'Ticket mở #1.', 'CAO', 'MOI', CURRENT_TIMESTAMP - INTERVAL 3 DAY),
('AC3_YC_TRIGGER_02', @riskTrigger, @cskh, 'Báo cáo tải chậm', 'Ticket mở #2.', 'BINH_THUONG', 'DANG_XU_LY', CURRENT_TIMESTAMP - INTERVAL 2 DAY),

('AC3_YC_FLAG_01', @riskFlagged, @cskh, 'Không đồng bộ dữ liệu', 'Ticket mở 1.', 'KHAN_CAP', 'MOI', CURRENT_TIMESTAMP - INTERVAL 5 DAY),
('AC3_YC_FLAG_02', @riskFlagged, @cskh, 'Sai số dashboard', 'Ticket mở 2.', 'CAO', 'DANG_XU_LY', CURRENT_TIMESTAMP - INTERVAL 4 DAY),
('AC3_YC_FLAG_03', @riskFlagged, @cskh, 'Chờ hướng dẫn nghiệp vụ', 'Ticket mở 3.', 'BINH_THUONG', 'CHO_KHACH_HANG', CURRENT_TIMESTAMP - INTERVAL 3 DAY),
('AC3_YC_FLAG_DONE', @riskFlagged, @cskh, 'Ticket đã xử lý', 'Không tính vào ngưỡng.', 'THAP', 'DA_XU_LY', CURRENT_TIMESTAMP - INTERVAL 10 DAY);

-- Không set co_rui_ro cho AC3_RISK_TRIGGER: hãy thêm ticket thứ 3 từ UI để chứng minh tự động.


-- ===== 10. S3-09 CAU HINH CHAM SOC DINH KY =====
INSERT INTO cau_hinh_he_thong
(ma_cau_hinh, nhom_cau_hinh, kieu_du_lieu, gia_tri, mo_ta, updated_by)
VALUES
('SO_NGAY_CHAM_SOC_DINH_KY', 'KHACH_HANG', 'INTEGER', '30',
 'Fixture AC S3-09: chu kỳ mặc định 30 ngày', @admin)
ON DUPLICATE KEY UPDATE gia_tri='30', updated_by=@admin, updated_at=CURRENT_TIMESTAMP;

-- ===== 11. KET QUA TOM TAT =====
SELECT
  (SELECT COUNT(*) FROM khach_hang WHERE ma_khach_hang LIKE 'AC3=_%' ESCAPE '=') AS so_khach_hang_fixture,
  (SELECT COUNT(*) FROM nguoi_lien_he WHERE email LIKE '%@ac3.test') AS so_nguoi_lien_he,
  (SELECT COUNT(*) FROM hoat_dong WHERE ma_hoat_dong LIKE 'AC3=_%' ESCAPE '=') AS so_hoat_dong,
  (SELECT COUNT(*) FROM yeu_cau_ho_tro WHERE ma_yeu_cau LIKE 'AC3=_%' ESCAPE '=') AS so_ticket,
  (SELECT COUNT(*) FROM hop_dong WHERE so_hop_dong LIKE 'AC3=_%' ESCAPE '=') AS so_hop_dong;

SELECT 'SETUP SPRINT 3 AC FIXTURE HOAN TAT' AS ket_qua;
