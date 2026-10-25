-- CRM DEMO SEED - local/dev only, synthetic data
-- Prerequisite: execute 001_crm_ban_hang_full_schema_8_sprints.sql on a NEW dev DB first.
-- SAFE: no DROP/DELETE/UPDATE of existing rows. Re-running skips existing demo records.
-- Demo prefix DEMO-. No personal/customer information is real.
-- Note: read-only demo fixtures for presentation; they do not prove end-to-end workflows.
USE crm_ban_hang;
SET NAMES utf8mb4;
SET time_zone = '+07:00';
START TRANSACTION;
-- Own demo sources: do not depend on a previous bootstrap's optional source rows.
INSERT INTO nguon_lead (ma_nguon,ten_nguon,thu_tu_hien_thi,hoat_dong)
SELECT 'DEMO-WEBSITE','Website (demo)',101,1
WHERE NOT EXISTS (SELECT 1 FROM nguon_lead WHERE ma_nguon='DEMO-WEBSITE');

INSERT INTO nguon_lead (ma_nguon,ten_nguon,thu_tu_hien_thi,hoat_dong)
SELECT 'DEMO-GIOI_THIEU','Giới thiệu (demo)',102,1
WHERE NOT EXISTS (SELECT 1 FROM nguon_lead WHERE ma_nguon='DEMO-GIOI_THIEU');

INSERT INTO nguon_lead (ma_nguon,ten_nguon,thu_tu_hien_thi,hoat_dong)
SELECT 'DEMO-SU_KIEN','Sự kiện (demo)',103,1
WHERE NOT EXISTS (SELECT 1 FROM nguon_lead WHERE ma_nguon='DEMO-SU_KIEN');

-- Fail early if canonical users absent (constraint failure avoids partial seed).
INSERT INTO nganh_nghe(ma_nganh,ten_nganh,thu_tu_hien_thi)
 SELECT 'DEMO-CNTT','Công nghệ thông tin (Demo)',99 WHERE NOT EXISTS (SELECT 1 FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT');
INSERT INTO nganh_nghe(ma_nganh,ten_nganh,thu_tu_hien_thi)
 SELECT 'DEMO-TMDV','Thương mại dịch vụ (Demo)',100 WHERE NOT EXISTS (SELECT 1 FROM nganh_nghe WHERE ma_nganh='DEMO-TMDV');
INSERT INTO quy_mo_doanh_nghiep(ma_quy_mo,ten_quy_mo,thu_tu_hien_thi)
 SELECT 'DEMO-VUA','51–200 nhân viên (Demo)',99 WHERE NOT EXISTS (SELECT 1 FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA');
INSERT INTO pipeline_ban_hang(ma_pipeline,ten_pipeline,mo_ta,mac_dinh,hoat_dong)
 SELECT 'DEMO-PIPELINE','Quy trình bán hàng Demo','Dùng cho chấm và trình diễn, không đổi pipeline hiện hữu',0,1
 WHERE NOT EXISTS (SELECT 1 FROM pipeline_ban_hang WHERE ma_pipeline='DEMO-PIPELINE');

INSERT INTO giai_doan_pipeline(pipeline_id,ma_giai_doan,ten_giai_doan,thu_tu,xac_suat_mac_dinh,so_ngay_dinh_tre,loai_ket_thuc)
SELECT p.id,'DEMO-TIEP_CAN','Tiếp cận',1,10,14,NULL FROM pipeline_ban_hang p WHERE p.ma_pipeline='DEMO-PIPELINE'
AND NOT EXISTS (SELECT 1 FROM giai_doan_pipeline g WHERE g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-TIEP_CAN');

INSERT INTO giai_doan_pipeline(pipeline_id,ma_giai_doan,ten_giai_doan,thu_tu,xac_suat_mac_dinh,so_ngay_dinh_tre,loai_ket_thuc)
SELECT p.id,'DEMO-KHAO_SAT','Khảo sát nhu cầu',2,30,14,NULL FROM pipeline_ban_hang p WHERE p.ma_pipeline='DEMO-PIPELINE'
AND NOT EXISTS (SELECT 1 FROM giai_doan_pipeline g WHERE g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-KHAO_SAT');

INSERT INTO giai_doan_pipeline(pipeline_id,ma_giai_doan,ten_giai_doan,thu_tu,xac_suat_mac_dinh,so_ngay_dinh_tre,loai_ket_thuc)
SELECT p.id,'DEMO-DE_XUAT','Đề xuất giải pháp',3,50,14,NULL FROM pipeline_ban_hang p WHERE p.ma_pipeline='DEMO-PIPELINE'
AND NOT EXISTS (SELECT 1 FROM giai_doan_pipeline g WHERE g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-DE_XUAT');

INSERT INTO giai_doan_pipeline(pipeline_id,ma_giai_doan,ten_giai_doan,thu_tu,xac_suat_mac_dinh,so_ngay_dinh_tre,loai_ket_thuc)
SELECT p.id,'DEMO-BAO_GIA','Báo giá và đàm phán',4,80,14,NULL FROM pipeline_ban_hang p WHERE p.ma_pipeline='DEMO-PIPELINE'
AND NOT EXISTS (SELECT 1 FROM giai_doan_pipeline g WHERE g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-BAO_GIA');

INSERT INTO giai_doan_pipeline(pipeline_id,ma_giai_doan,ten_giai_doan,thu_tu,xac_suat_mac_dinh,so_ngay_dinh_tre,loai_ket_thuc)
SELECT p.id,'DEMO-THANG','Đóng thắng',5,100,14,'WIN' FROM pipeline_ban_hang p WHERE p.ma_pipeline='DEMO-PIPELINE'
AND NOT EXISTS (SELECT 1 FROM giai_doan_pipeline g WHERE g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THANG');

INSERT INTO giai_doan_pipeline(pipeline_id,ma_giai_doan,ten_giai_doan,thu_tu,xac_suat_mac_dinh,so_ngay_dinh_tre,loai_ket_thuc)
SELECT p.id,'DEMO-THUA','Đóng thua',6,0,14,'LOST' FROM pipeline_ban_hang p WHERE p.ma_pipeline='DEMO-PIPELINE'
AND NOT EXISTS (SELECT 1 FROM giai_doan_pipeline g WHERE g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THUA');

INSERT INTO ly_do_thang_thua(ma_ly_do,ten_ly_do,loai) SELECT 'DEMO-WIN','Phù hợp nhu cầu','THANG'
WHERE NOT EXISTS (SELECT 1 FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-WIN');

INSERT INTO ly_do_thang_thua(ma_ly_do,ten_ly_do,loai) SELECT 'DEMO-LOST','Ngân sách hạn chế','THUA'
WHERE NOT EXISTS (SELECT 1 FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-LOST');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-001','Dịch vụ CRM Demo 1','DICH_VU_THUE_BAO','Gói',12500000,10000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-001');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-002','Dịch vụ CRM Demo 2','DICH_VU_THUE_BAO','Gói',15000000,12000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-002');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-003','Dịch vụ CRM Demo 3','DICH_VU_THUE_BAO','Gói',17500000,14000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-003');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-004','Dịch vụ CRM Demo 4','DICH_VU_THUE_BAO','Gói',20000000,16000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-004');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-005','Dịch vụ CRM Demo 5','DICH_VU_THUE_BAO','Gói',22500000,18000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-005');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-006','Dịch vụ CRM Demo 6','DICH_VU_THUE_BAO','Gói',25000000,20000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-006');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-007','Dịch vụ CRM Demo 7','DICH_VU_THUE_BAO','Gói',27500000,22000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-007');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-008','Dịch vụ CRM Demo 8','DICH_VU_THUE_BAO','Gói',30000000,24000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-008');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-009','Dịch vụ CRM Demo 9','DICH_VU_THUE_BAO','Gói',32500000,26000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-009');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-010','Dịch vụ CRM Demo 10','DICH_VU_THUE_BAO','Gói',35000000,28000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-010');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-011','Dịch vụ CRM Demo 11','DICH_VU_THUE_BAO','Gói',37500000,30000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-011');

INSERT INTO san_pham(ma_san_pham,ten_san_pham,loai_san_pham,don_vi_tinh,gia_niem_yet,gia_san,mo_ta,trang_thai)
SELECT 'DEMO-SP-012','Dịch vụ CRM Demo 12','DICH_VU_THUE_BAO','Gói',40000000,32000000,'Dữ liệu giả lập phục vụ nghiệm thu','DANG_KINH_DOANH'
WHERE NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham='DEMO-SP-012');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-001','Công ty Demo 001 - Thương mại','công ty demo 001 - thương mại','DEMO-MST-00001',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 1, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-001');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 1-1','Trưởng phòng mua hàng','contact1-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-001' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact1-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 1-2','Chuyên viên','contact1-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-001' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact1-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-002','Công ty Demo 002 - Dịch vụ','công ty demo 002 - dịch vụ','DEMO-MST-00002',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 2, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-002');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 2-1','Trưởng phòng mua hàng','contact2-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-002' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact2-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 2-2','Chuyên viên','contact2-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-002' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact2-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-003','Công ty Demo 003 - Giải pháp số','công ty demo 003 - giải pháp số','DEMO-MST-00003',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 3, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-003');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 3-1','Trưởng phòng mua hàng','contact3-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-003' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact3-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 3-2','Chuyên viên','contact3-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-003' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact3-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-004','Công ty Demo 004 - Công nghệ','công ty demo 004 - công nghệ','DEMO-MST-00004',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 4, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-004');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 4-1','Trưởng phòng mua hàng','contact4-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-004' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact4-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 4-2','Chuyên viên','contact4-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-004' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact4-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-005','Công ty Demo 005 - Thương mại','công ty demo 005 - thương mại','DEMO-MST-00005',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 5, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-005');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 5-1','Trưởng phòng mua hàng','contact5-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-005' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact5-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 5-2','Chuyên viên','contact5-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-005' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact5-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-006','Công ty Demo 006 - Dịch vụ','công ty demo 006 - dịch vụ','DEMO-MST-00006',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 6, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-006');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 6-1','Trưởng phòng mua hàng','contact6-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-006' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact6-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 6-2','Chuyên viên','contact6-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-006' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact6-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-007','Công ty Demo 007 - Giải pháp số','công ty demo 007 - giải pháp số','DEMO-MST-00007',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 7, Hà Nội',u.id,u.nhom_kinh_doanh_id,'KHACH_HANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-007');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 7-1','Trưởng phòng mua hàng','contact7-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-007' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact7-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 7-2','Chuyên viên','contact7-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-007' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact7-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-008','Công ty Demo 008 - Công nghệ','công ty demo 008 - công nghệ','DEMO-MST-00008',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 8, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-008');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 8-1','Trưởng phòng mua hàng','contact8-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-008' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact8-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 8-2','Chuyên viên','contact8-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-008' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact8-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-009','Công ty Demo 009 - Thương mại','công ty demo 009 - thương mại','DEMO-MST-00009',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 9, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-009');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 9-1','Trưởng phòng mua hàng','contact9-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-009' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact9-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 9-2','Chuyên viên','contact9-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-009' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact9-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-010','Công ty Demo 010 - Dịch vụ','công ty demo 010 - dịch vụ','DEMO-MST-00010',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 10, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-010');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 10-1','Trưởng phòng mua hàng','contact10-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-010' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact10-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 10-2','Chuyên viên','contact10-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-010' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact10-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-011','Công ty Demo 011 - Giải pháp số','công ty demo 011 - giải pháp số','DEMO-MST-00011',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 11, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-011');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 11-1','Trưởng phòng mua hàng','contact11-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-011' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact11-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 11-2','Chuyên viên','contact11-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-011' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact11-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-012','Công ty Demo 012 - Công nghệ','công ty demo 012 - công nghệ','DEMO-MST-00012',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 12, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-012');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 12-1','Trưởng phòng mua hàng','contact12-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-012' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact12-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 12-2','Chuyên viên','contact12-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-012' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact12-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-013','Công ty Demo 013 - Thương mại','công ty demo 013 - thương mại','DEMO-MST-00013',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 13, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-013');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 13-1','Trưởng phòng mua hàng','contact13-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-013' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact13-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 13-2','Chuyên viên','contact13-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-013' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact13-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-014','Công ty Demo 014 - Dịch vụ','công ty demo 014 - dịch vụ','DEMO-MST-00014',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 14, Hà Nội',u.id,u.nhom_kinh_doanh_id,'KHACH_HANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-014');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 14-1','Trưởng phòng mua hàng','contact14-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-014' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact14-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 14-2','Chuyên viên','contact14-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-014' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact14-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-015','Công ty Demo 015 - Giải pháp số','công ty demo 015 - giải pháp số','DEMO-MST-00015',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 15, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-015');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 15-1','Trưởng phòng mua hàng','contact15-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-015' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact15-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 15-2','Chuyên viên','contact15-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-015' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact15-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-016','Công ty Demo 016 - Công nghệ','công ty demo 016 - công nghệ','DEMO-MST-00016',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 16, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-016');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 16-1','Trưởng phòng mua hàng','contact16-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-016' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact16-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 16-2','Chuyên viên','contact16-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-016' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact16-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-017','Công ty Demo 017 - Thương mại','công ty demo 017 - thương mại','DEMO-MST-00017',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 17, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-017');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 17-1','Trưởng phòng mua hàng','contact17-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-017' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact17-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 17-2','Chuyên viên','contact17-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-017' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact17-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-018','Công ty Demo 018 - Dịch vụ','công ty demo 018 - dịch vụ','DEMO-MST-00018',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 18, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-018');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 18-1','Trưởng phòng mua hàng','contact18-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-018' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact18-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 18-2','Chuyên viên','contact18-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-018' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact18-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-019','Công ty Demo 019 - Giải pháp số','công ty demo 019 - giải pháp số','DEMO-MST-00019',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 19, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-019');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 19-1','Trưởng phòng mua hàng','contact19-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-019' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact19-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 19-2','Chuyên viên','contact19-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-019' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact19-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-020','Công ty Demo 020 - Công nghệ','công ty demo 020 - công nghệ','DEMO-MST-00020',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 20, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-020');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 20-1','Trưởng phòng mua hàng','contact20-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-020' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact20-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 20-2','Chuyên viên','contact20-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-020' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact20-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-021','Công ty Demo 021 - Thương mại','công ty demo 021 - thương mại','DEMO-MST-00021',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 21, Hà Nội',u.id,u.nhom_kinh_doanh_id,'KHACH_HANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-021');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 21-1','Trưởng phòng mua hàng','contact21-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-021' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact21-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 21-2','Chuyên viên','contact21-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-021' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact21-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-022','Công ty Demo 022 - Dịch vụ','công ty demo 022 - dịch vụ','DEMO-MST-00022',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 22, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-022');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 22-1','Trưởng phòng mua hàng','contact22-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-022' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact22-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 22-2','Chuyên viên','contact22-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-022' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact22-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-023','Công ty Demo 023 - Giải pháp số','công ty demo 023 - giải pháp số','DEMO-MST-00023',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 23, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-023');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 23-1','Trưởng phòng mua hàng','contact23-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-023' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact23-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 23-2','Chuyên viên','contact23-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-023' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact23-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-024','Công ty Demo 024 - Công nghệ','công ty demo 024 - công nghệ','DEMO-MST-00024',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 24, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-024');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 24-1','Trưởng phòng mua hàng','contact24-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-024' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact24-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 24-2','Chuyên viên','contact24-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-024' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact24-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-025','Công ty Demo 025 - Thương mại','công ty demo 025 - thương mại','DEMO-MST-00025',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 25, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-025');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 25-1','Trưởng phòng mua hàng','contact25-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-025' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact25-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 25-2','Chuyên viên','contact25-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-025' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact25-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-026','Công ty Demo 026 - Dịch vụ','công ty demo 026 - dịch vụ','DEMO-MST-00026',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 26, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-026');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 26-1','Trưởng phòng mua hàng','contact26-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-026' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact26-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 26-2','Chuyên viên','contact26-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-026' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact26-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-027','Công ty Demo 027 - Giải pháp số','công ty demo 027 - giải pháp số','DEMO-MST-00027',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 27, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-027');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 27-1','Trưởng phòng mua hàng','contact27-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-027' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact27-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 27-2','Chuyên viên','contact27-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-027' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact27-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-028','Công ty Demo 028 - Công nghệ','công ty demo 028 - công nghệ','DEMO-MST-00028',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 28, Hà Nội',u.id,u.nhom_kinh_doanh_id,'KHACH_HANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-028');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 28-1','Trưởng phòng mua hàng','contact28-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-028' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact28-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 28-2','Chuyên viên','contact28-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-028' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact28-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-029','Công ty Demo 029 - Thương mại','công ty demo 029 - thương mại','DEMO-MST-00029',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 29, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-029');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 29-1','Trưởng phòng mua hàng','contact29-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-029' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact29-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 29-2','Chuyên viên','contact29-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-029' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact29-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-030','Công ty Demo 030 - Dịch vụ','công ty demo 030 - dịch vụ','DEMO-MST-00030',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 30, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-030');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 30-1','Trưởng phòng mua hàng','contact30-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-030' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact30-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 30-2','Chuyên viên','contact30-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-030' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact30-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-031','Công ty Demo 031 - Giải pháp số','công ty demo 031 - giải pháp số','DEMO-MST-00031',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 31, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-031');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 31-1','Trưởng phòng mua hàng','contact31-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-031' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact31-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 31-2','Chuyên viên','contact31-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-031' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact31-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-032','Công ty Demo 032 - Công nghệ','công ty demo 032 - công nghệ','DEMO-MST-00032',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 32, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-032');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 32-1','Trưởng phòng mua hàng','contact32-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-032' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact32-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 32-2','Chuyên viên','contact32-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-032' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact32-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-033','Công ty Demo 033 - Thương mại','công ty demo 033 - thương mại','DEMO-MST-00033',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 33, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-033');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 33-1','Trưởng phòng mua hàng','contact33-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-033' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact33-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 33-2','Chuyên viên','contact33-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-033' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact33-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-034','Công ty Demo 034 - Dịch vụ','công ty demo 034 - dịch vụ','DEMO-MST-00034',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 34, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-034');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 34-1','Trưởng phòng mua hàng','contact34-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-034' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact34-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 34-2','Chuyên viên','contact34-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-034' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact34-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-035','Công ty Demo 035 - Giải pháp số','công ty demo 035 - giải pháp số','DEMO-MST-00035',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 35, Hà Nội',u.id,u.nhom_kinh_doanh_id,'KHACH_HANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-035');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 35-1','Trưởng phòng mua hàng','contact35-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-035' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact35-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 35-2','Chuyên viên','contact35-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-035' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact35-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-036','Công ty Demo 036 - Công nghệ','công ty demo 036 - công nghệ','DEMO-MST-00036',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 36, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-036');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 36-1','Trưởng phòng mua hàng','contact36-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-036' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact36-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 36-2','Chuyên viên','contact36-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-036' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact36-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-037','Công ty Demo 037 - Thương mại','công ty demo 037 - thương mại','DEMO-MST-00037',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 37, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-037');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 37-1','Trưởng phòng mua hàng','contact37-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-037' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact37-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 37-2','Chuyên viên','contact37-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-037' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact37-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-038','Công ty Demo 038 - Dịch vụ','công ty demo 038 - dịch vụ','DEMO-MST-00038',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 38, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-038');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 38-1','Trưởng phòng mua hàng','contact38-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-038' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact38-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 38-2','Chuyên viên','contact38-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-038' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact38-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-039','Công ty Demo 039 - Giải pháp số','công ty demo 039 - giải pháp số','DEMO-MST-00039',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 39, Hà Nội',u.id,u.nhom_kinh_doanh_id,'TIEM_NANG','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='teamlead@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-039');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 39-1','Trưởng phòng mua hàng','contact39-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-039' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact39-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 39-2','Chuyên viên','contact39-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-039' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact39-2@example.com');

INSERT INTO khach_hang(ma_khach_hang,ten_cong_ty,ten_chuan_hoa,ma_so_thue,nganh_nghe_id,quy_mo_id,dia_chi,nguoi_so_huu_id,nhom_kinh_doanh_id,trang_thai,mo_ta_chi_tiet)
SELECT 'DEMO-KH-040','Công ty Demo 040 - Công nghệ','công ty demo 040 - công nghệ','DEMO-MST-00040',
(SELECT id FROM nganh_nghe WHERE ma_nganh='DEMO-CNTT'),(SELECT id FROM quy_mo_doanh_nghiep WHERE ma_quy_mo='DEMO-VUA'),
'Địa chỉ mô phỏng số 40, Hà Nội',u.id,u.nhom_kinh_doanh_id,'DANG_GIAO_DICH','Dữ liệu hoàn toàn giả lập'
FROM nguoi_dung u WHERE u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang='DEMO-KH-040');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 40-1','Trưởng phòng mua hàng','contact40-1@example.com','NGUOI_QUYET_DINH',1
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-040' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact40-1@example.com');

INSERT INTO nguoi_lien_he(khach_hang_id,ho_ten,chuc_danh,email,vai_tro_quyet_dinh,la_dau_moi_chinh)
SELECT k.id,'Người liên hệ Demo 40-2','Chuyên viên','contact40-2@example.com','NGUOI_ANH_HUONG',0
FROM khach_hang k WHERE k.ma_khach_hang='DEMO-KH-040' AND NOT EXISTS
(SELECT 1 FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id AND c.email='contact40-2@example.com');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-001','Lead Demo 1','lead1@example.com','lead1@example.com','0987000001','Doanh nghiệp quan tâm Demo 1',
n.id,27,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-001');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-002','Lead Demo 2','lead2@example.com','lead2@example.com','0987000002','Doanh nghiệp quan tâm Demo 2',
n.id,34,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-002');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-003','Lead Demo 3','lead3@example.com','lead3@example.com','0987000003','Doanh nghiệp quan tâm Demo 3',
n.id,41,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-003');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-004','Lead Demo 4','lead4@example.com','lead4@example.com','0987000004','Doanh nghiệp quan tâm Demo 4',
n.id,48,'AM','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-004');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-005','Lead Demo 5','lead5@example.com','lead5@example.com','0987000005','Doanh nghiệp quan tâm Demo 5',
n.id,55,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-005');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-006','Lead Demo 6','lead6@example.com','lead6@example.com','0987000006','Doanh nghiệp quan tâm Demo 6',
n.id,62,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-006');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-007','Lead Demo 7','lead7@example.com','lead7@example.com','0987000007','Doanh nghiệp quan tâm Demo 7',
n.id,69,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-007');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-008','Lead Demo 8','lead8@example.com','lead8@example.com','0987000008','Doanh nghiệp quan tâm Demo 8',
n.id,76,'AM','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-008');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-009','Lead Demo 9','lead9@example.com','lead9@example.com','0987000009','Doanh nghiệp quan tâm Demo 9',
n.id,83,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-009');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-010','Lead Demo 10','lead10@example.com','lead10@example.com','0987000010','Doanh nghiệp quan tâm Demo 10',
n.id,90,'AM','TU_CHOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-010');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-011','Lead Demo 11','lead11@example.com','lead11@example.com','0987000011','Doanh nghiệp quan tâm Demo 11',
n.id,97,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-011');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-012','Lead Demo 12','lead12@example.com','lead12@example.com','0987000012','Doanh nghiệp quan tâm Demo 12',
n.id,23,'NONG','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-012');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-013','Lead Demo 13','lead13@example.com','lead13@example.com','0987000013','Doanh nghiệp quan tâm Demo 13',
n.id,30,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-013');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-014','Lead Demo 14','lead14@example.com','lead14@example.com','0987000014','Doanh nghiệp quan tâm Demo 14',
n.id,37,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-014');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-015','Lead Demo 15','lead15@example.com','lead15@example.com','0987000015','Doanh nghiệp quan tâm Demo 15',
n.id,44,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-015');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-016','Lead Demo 16','lead16@example.com','lead16@example.com','0987000016','Doanh nghiệp quan tâm Demo 16',
n.id,51,'AM','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-016');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-017','Lead Demo 17','lead17@example.com','lead17@example.com','0987000017','Doanh nghiệp quan tâm Demo 17',
n.id,58,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-017');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-018','Lead Demo 18','lead18@example.com','lead18@example.com','0987000018','Doanh nghiệp quan tâm Demo 18',
n.id,65,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-018');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-019','Lead Demo 19','lead19@example.com','lead19@example.com','0987000019','Doanh nghiệp quan tâm Demo 19',
n.id,72,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-019');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-020','Lead Demo 20','lead20@example.com','lead20@example.com','0987000020','Doanh nghiệp quan tâm Demo 20',
n.id,79,'AM','TU_CHOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-020');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-021','Lead Demo 21','lead21@example.com','lead21@example.com','0987000021','Doanh nghiệp quan tâm Demo 21',
n.id,86,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-021');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-022','Lead Demo 22','lead22@example.com','lead22@example.com','0987000022','Doanh nghiệp quan tâm Demo 22',
n.id,93,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-022');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-023','Lead Demo 23','lead23@example.com','lead23@example.com','0987000023','Doanh nghiệp quan tâm Demo 23',
n.id,100,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-023');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-024','Lead Demo 24','lead24@example.com','lead24@example.com','0987000024','Doanh nghiệp quan tâm Demo 24',
n.id,26,'NONG','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-024');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-025','Lead Demo 25','lead25@example.com','lead25@example.com','0987000025','Doanh nghiệp quan tâm Demo 25',
n.id,33,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-025');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-026','Lead Demo 26','lead26@example.com','lead26@example.com','0987000026','Doanh nghiệp quan tâm Demo 26',
n.id,40,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-026');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-027','Lead Demo 27','lead27@example.com','lead27@example.com','0987000027','Doanh nghiệp quan tâm Demo 27',
n.id,47,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-027');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-028','Lead Demo 28','lead28@example.com','lead28@example.com','0987000028','Doanh nghiệp quan tâm Demo 28',
n.id,54,'AM','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-028');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-029','Lead Demo 29','lead29@example.com','lead29@example.com','0987000029','Doanh nghiệp quan tâm Demo 29',
n.id,61,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-029');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-030','Lead Demo 30','lead30@example.com','lead30@example.com','0987000030','Doanh nghiệp quan tâm Demo 30',
n.id,68,'NONG','TU_CHOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-030');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-031','Lead Demo 31','lead31@example.com','lead31@example.com','0987000031','Doanh nghiệp quan tâm Demo 31',
n.id,75,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-031');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-032','Lead Demo 32','lead32@example.com','lead32@example.com','0987000032','Doanh nghiệp quan tâm Demo 32',
n.id,82,'AM','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-032');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-033','Lead Demo 33','lead33@example.com','lead33@example.com','0987000033','Doanh nghiệp quan tâm Demo 33',
n.id,89,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-033');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-034','Lead Demo 34','lead34@example.com','lead34@example.com','0987000034','Doanh nghiệp quan tâm Demo 34',
n.id,96,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-034');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-035','Lead Demo 35','lead35@example.com','lead35@example.com','0987000035','Doanh nghiệp quan tâm Demo 35',
n.id,22,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-035');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-036','Lead Demo 36','lead36@example.com','lead36@example.com','0987000036','Doanh nghiệp quan tâm Demo 36',
n.id,29,'NONG','DANG_CHAM_SOC',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),1,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-036');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-037','Lead Demo 37','lead37@example.com','lead37@example.com','0987000037','Doanh nghiệp quan tâm Demo 37',
n.id,36,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-037');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-038','Lead Demo 38','lead38@example.com','lead38@example.com','0987000038','Doanh nghiệp quan tâm Demo 38',
n.id,43,'AM','MOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='SU_KIEN'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-038');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-039','Lead Demo 39','lead39@example.com','lead39@example.com','0987000039','Doanh nghiệp quan tâm Demo 39',
n.id,50,'NONG','DA_PHAN_BO',u.id,u.nhom_kinh_doanh_id,
DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='WEBSITE'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-039');

INSERT INTO `lead`(ma_lead,ho_ten,email,email_chuan_hoa,so_dien_thoai,ten_cong_ty,nguon_lead_id,diem,phan_loai,trang_thai,nguoi_phu_trach_id,nhom_phan_bo_id,duoc_phan_bo_luc,han_phan_hoi_sla,qua_sla,created_by)
SELECT 'DEMO-LEAD-040','Lead Demo 40','lead40@example.com','lead40@example.com','0987000040','Doanh nghiệp quan tâm Demo 40',
n.id,57,'AM','TU_CHOI',NULL,NULL,
NULL,NULL,0,u.id
FROM nguon_lead n JOIN nguoi_dung u ON u.email='sales@crm.vn' WHERE n.ma_nguon='GIOI_THIEU'
AND NOT EXISTS (SELECT 1 FROM `lead` WHERE ma_lead='DEMO-LEAD-040');

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-001','Thương vụ Demo 1',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,19000000,30,DATE_ADD(CURDATE(),INTERVAL 16 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-KHAO_SAT'
WHERE k.ma_khach_hang='DEMO-KH-001' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-001');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-002'
WHERE o.ma_co_hoi='DEMO-CH-001' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-002','Thương vụ Demo 2',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,23000000,50,DATE_ADD(CURDATE(),INTERVAL 17 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-DE_XUAT'
WHERE k.ma_khach_hang='DEMO-KH-002' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-002');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-003'
WHERE o.ma_co_hoi='DEMO-CH-002' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-003','Thương vụ Demo 3',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,27000000,80,DATE_ADD(CURDATE(),INTERVAL 18 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-BAO_GIA'
WHERE k.ma_khach_hang='DEMO-KH-003' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-003');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-004'
WHERE o.ma_co_hoi='DEMO-CH-003' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-004','Thương vụ Demo 4',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,31000000,10,DATE_ADD(CURDATE(),INTERVAL 19 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-TIEP_CAN'
WHERE k.ma_khach_hang='DEMO-KH-004' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-004');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-005'
WHERE o.ma_co_hoi='DEMO-CH-004' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-005','Thương vụ Demo 5',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,35000000,30,DATE_ADD(CURDATE(),INTERVAL 20 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-KHAO_SAT'
WHERE k.ma_khach_hang='DEMO-KH-005' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-005');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-006'
WHERE o.ma_co_hoi='DEMO-CH-005' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-006','Thương vụ Demo 6',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,39000000,50,DATE_ADD(CURDATE(),INTERVAL 21 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-DE_XUAT'
WHERE k.ma_khach_hang='DEMO-KH-006' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-006');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-007'
WHERE o.ma_co_hoi='DEMO-CH-006' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-007','Thương vụ Demo 7',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,43000000,80,DATE_ADD(CURDATE(),INTERVAL 22 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-BAO_GIA'
WHERE k.ma_khach_hang='DEMO-KH-007' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-007');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-008'
WHERE o.ma_co_hoi='DEMO-CH-007' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-008','Thương vụ Demo 8',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,47000000,10,DATE_ADD(CURDATE(),INTERVAL 23 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-TIEP_CAN'
WHERE k.ma_khach_hang='DEMO-KH-008' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-008');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-009'
WHERE o.ma_co_hoi='DEMO-CH-008' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-009','Thương vụ Demo 9',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,51000000,30,DATE_ADD(CURDATE(),INTERVAL 24 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-KHAO_SAT'
WHERE k.ma_khach_hang='DEMO-KH-009' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-009');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-010'
WHERE o.ma_co_hoi='DEMO-CH-009' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-010','Thương vụ Demo 10',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,55000000,50,DATE_ADD(CURDATE(),INTERVAL 25 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-DE_XUAT'
WHERE k.ma_khach_hang='DEMO-KH-010' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-010');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-011'
WHERE o.ma_co_hoi='DEMO-CH-010' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-011','Thương vụ Demo 11',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,59000000,80,DATE_ADD(CURDATE(),INTERVAL 26 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-BAO_GIA'
WHERE k.ma_khach_hang='DEMO-KH-011' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-011');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-012'
WHERE o.ma_co_hoi='DEMO-CH-011' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-012','Thương vụ Demo 12',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,63000000,10,DATE_ADD(CURDATE(),INTERVAL 27 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-TIEP_CAN'
WHERE k.ma_khach_hang='DEMO-KH-012' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-012');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-001'
WHERE o.ma_co_hoi='DEMO-CH-012' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-013','Thương vụ Demo 13',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,67000000,30,DATE_ADD(CURDATE(),INTERVAL 28 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-KHAO_SAT'
WHERE k.ma_khach_hang='DEMO-KH-013' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-013');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-002'
WHERE o.ma_co_hoi='DEMO-CH-013' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-014','Thương vụ Demo 14',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,71000000,50,DATE_ADD(CURDATE(),INTERVAL 29 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-DE_XUAT'
WHERE k.ma_khach_hang='DEMO-KH-014' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-014');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-003'
WHERE o.ma_co_hoi='DEMO-CH-014' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-015','Thương vụ Demo 15',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,75000000,80,DATE_ADD(CURDATE(),INTERVAL 30 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-BAO_GIA'
WHERE k.ma_khach_hang='DEMO-KH-015' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-015');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-004'
WHERE o.ma_co_hoi='DEMO-CH-015' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-016','Thương vụ Demo 16',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,79000000,10,DATE_ADD(CURDATE(),INTERVAL 31 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-TIEP_CAN'
WHERE k.ma_khach_hang='DEMO-KH-016' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-016');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-005'
WHERE o.ma_co_hoi='DEMO-CH-016' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-017','Thương vụ Demo 17',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,83000000,30,DATE_ADD(CURDATE(),INTERVAL 32 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-KHAO_SAT'
WHERE k.ma_khach_hang='DEMO-KH-017' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-017');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-006'
WHERE o.ma_co_hoi='DEMO-CH-017' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-018','Thương vụ Demo 18',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,87000000,50,DATE_ADD(CURDATE(),INTERVAL 33 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-DE_XUAT'
WHERE k.ma_khach_hang='DEMO-KH-018' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-018');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-007'
WHERE o.ma_co_hoi='DEMO-CH-018' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-019','Thương vụ Demo 19',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,91000000,80,DATE_ADD(CURDATE(),INTERVAL 34 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-BAO_GIA'
WHERE k.ma_khach_hang='DEMO-KH-019' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-019');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-008'
WHERE o.ma_co_hoi='DEMO-CH-019' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-020','Thương vụ Demo 20',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,95000000,10,DATE_ADD(CURDATE(),INTERVAL 35 DAY),'MO',
NULL,NULL,
NULL,'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-TIEP_CAN'
WHERE k.ma_khach_hang='DEMO-KH-020' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-020');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-009'
WHERE o.ma_co_hoi='DEMO-CH-020' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-021','Thương vụ Demo 21',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,99000000,100,DATE_ADD(CURDATE(),INTERVAL 36 DAY),'DONG_THANG',
99000000,DATE_SUB(CURDATE(), INTERVAL 5 DAY),
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-WIN'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THANG'
WHERE k.ma_khach_hang='DEMO-KH-021' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-021');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-010'
WHERE o.ma_co_hoi='DEMO-CH-021' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-022','Thương vụ Demo 22',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,103000000,100,DATE_ADD(CURDATE(),INTERVAL 37 DAY),'DONG_THANG',
103000000,DATE_SUB(CURDATE(), INTERVAL 5 DAY),
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-WIN'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THANG'
WHERE k.ma_khach_hang='DEMO-KH-022' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-022');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-011'
WHERE o.ma_co_hoi='DEMO-CH-022' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-023','Thương vụ Demo 23',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,107000000,100,DATE_ADD(CURDATE(),INTERVAL 38 DAY),'DONG_THANG',
107000000,DATE_SUB(CURDATE(), INTERVAL 5 DAY),
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-WIN'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THANG'
WHERE k.ma_khach_hang='DEMO-KH-023' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-023');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-012'
WHERE o.ma_co_hoi='DEMO-CH-023' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-024','Thương vụ Demo 24',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,111000000,100,DATE_ADD(CURDATE(),INTERVAL 39 DAY),'DONG_THANG',
111000000,DATE_SUB(CURDATE(), INTERVAL 5 DAY),
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-WIN'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THANG'
WHERE k.ma_khach_hang='DEMO-KH-024' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-024');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-001'
WHERE o.ma_co_hoi='DEMO-CH-024' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-025','Thương vụ Demo 25',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,115000000,100,DATE_ADD(CURDATE(),INTERVAL 40 DAY),'DONG_THANG',
115000000,DATE_SUB(CURDATE(), INTERVAL 5 DAY),
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-WIN'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THANG'
WHERE k.ma_khach_hang='DEMO-KH-025' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-025');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-002'
WHERE o.ma_co_hoi='DEMO-CH-025' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-026','Thương vụ Demo 26',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,119000000,0,DATE_ADD(CURDATE(),INTERVAL 41 DAY),'DONG_THUA',
NULL,NULL,
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-LOST'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THUA'
WHERE k.ma_khach_hang='DEMO-KH-026' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-026');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-003'
WHERE o.ma_co_hoi='DEMO-CH-026' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-027','Thương vụ Demo 27',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,123000000,0,DATE_ADD(CURDATE(),INTERVAL 42 DAY),'DONG_THUA',
NULL,NULL,
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-LOST'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='teamlead@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THUA'
WHERE k.ma_khach_hang='DEMO-KH-027' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-027');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-004'
WHERE o.ma_co_hoi='DEMO-CH-027' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO co_hoi(ma_co_hoi,ten_co_hoi,khach_hang_id,nguoi_lien_he_chinh_id,giai_doan_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,gia_tri_du_kien,xac_suat,ngay_chot_du_kien,trang_thai,gia_tri_chot_thuc_te,ngay_ky,ly_do_thang_thua_id,mo_ta_chi_tiet,created_by)
SELECT 'DEMO-CH-028','Thương vụ Demo 28',k.id,
(SELECT c.id FROM nguoi_lien_he c WHERE c.khach_hang_id=k.id ORDER BY c.id LIMIT 1),
g.id,u.id,u.nhom_kinh_doanh_id,127000000,0,DATE_ADD(CURDATE(),INTERVAL 43 DAY),'DONG_THUA',
NULL,NULL,
(SELECT id FROM ly_do_thang_thua WHERE ma_ly_do='DEMO-LOST'),'Dữ liệu demo pipeline',u.id
FROM khach_hang k JOIN nguoi_dung u ON u.email='sales@crm.vn'
JOIN pipeline_ban_hang p ON p.ma_pipeline='DEMO-PIPELINE'
JOIN giai_doan_pipeline g ON g.pipeline_id=p.id AND g.ma_giai_doan='DEMO-THUA'
WHERE k.ma_khach_hang='DEMO-KH-028' AND NOT EXISTS (SELECT 1 FROM co_hoi WHERE ma_co_hoi='DEMO-CH-028');

INSERT INTO co_hoi_san_pham(co_hoi_id,san_pham_id,so_luong,don_gia,thanh_tien,ghi_chu)
SELECT o.id,p.id,1,p.gia_niem_yet,p.gia_niem_yet,'Sản phẩm minh họa'
FROM co_hoi o JOIN san_pham p ON p.ma_san_pham='DEMO-SP-005'
WHERE o.ma_co_hoi='DEMO-CH-028' AND NOT EXISTS
(SELECT 1 FROM co_hoi_san_pham x WHERE x.co_hoi_id=o.id AND x.san_pham_id=p.id);

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-001','Trao đổi nhu cầu khách hàng Demo 1','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 1 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-002'
WHERE k.ma_khach_hang='DEMO-KH-002' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-001');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-002','Trao đổi nhu cầu khách hàng Demo 2','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 2 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-003'
WHERE k.ma_khach_hang='DEMO-KH-003' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-002');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-003','Trao đổi nhu cầu khách hàng Demo 3','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 3 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-004'
WHERE k.ma_khach_hang='DEMO-KH-004' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-003');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-004','Trao đổi nhu cầu khách hàng Demo 4','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 4 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 4 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-005'
WHERE k.ma_khach_hang='DEMO-KH-005' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-004');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-005','Trao đổi nhu cầu khách hàng Demo 5','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 5 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-006'
WHERE k.ma_khach_hang='DEMO-KH-006' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-005');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-006','Trao đổi nhu cầu khách hàng Demo 6','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 6 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 6 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-007'
WHERE k.ma_khach_hang='DEMO-KH-007' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-006');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-007','Trao đổi nhu cầu khách hàng Demo 7','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 7 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 7 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-008'
WHERE k.ma_khach_hang='DEMO-KH-008' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-007');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-008','Trao đổi nhu cầu khách hàng Demo 8','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 8 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 8 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-009'
WHERE k.ma_khach_hang='DEMO-KH-009' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-008');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-009','Trao đổi nhu cầu khách hàng Demo 9','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 9 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 9 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-010'
WHERE k.ma_khach_hang='DEMO-KH-010' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-009');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-010','Trao đổi nhu cầu khách hàng Demo 10','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 10 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-011'
WHERE k.ma_khach_hang='DEMO-KH-011' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-010');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-011','Trao đổi nhu cầu khách hàng Demo 11','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 11 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 11 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-012'
WHERE k.ma_khach_hang='DEMO-KH-012' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-011');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-012','Trao đổi nhu cầu khách hàng Demo 12','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 12 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 12 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-013'
WHERE k.ma_khach_hang='DEMO-KH-013' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-012');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-013','Trao đổi nhu cầu khách hàng Demo 13','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 13 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 13 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-014'
WHERE k.ma_khach_hang='DEMO-KH-014' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-013');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-014','Trao đổi nhu cầu khách hàng Demo 14','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 14 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 14 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-015'
WHERE k.ma_khach_hang='DEMO-KH-015' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-014');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-015','Trao đổi nhu cầu khách hàng Demo 15','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 15 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 15 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-016'
WHERE k.ma_khach_hang='DEMO-KH-016' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-015');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-016','Trao đổi nhu cầu khách hàng Demo 16','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 16 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 16 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-017'
WHERE k.ma_khach_hang='DEMO-KH-017' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-016');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-017','Trao đổi nhu cầu khách hàng Demo 17','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 17 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 17 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-018'
WHERE k.ma_khach_hang='DEMO-KH-018' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-017');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-018','Trao đổi nhu cầu khách hàng Demo 18','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 18 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 18 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-019'
WHERE k.ma_khach_hang='DEMO-KH-019' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-018');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-019','Trao đổi nhu cầu khách hàng Demo 19','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 19 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 19 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-020'
WHERE k.ma_khach_hang='DEMO-KH-020' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-019');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-020','Trao đổi nhu cầu khách hàng Demo 20','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 20 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 20 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-021'
WHERE k.ma_khach_hang='DEMO-KH-021' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-020');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-021','Trao đổi nhu cầu khách hàng Demo 21','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 21 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 21 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-022'
WHERE k.ma_khach_hang='DEMO-KH-022' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-021');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-022','Trao đổi nhu cầu khách hàng Demo 22','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 22 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 22 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-023'
WHERE k.ma_khach_hang='DEMO-KH-023' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-022');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-023','Trao đổi nhu cầu khách hàng Demo 23','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 23 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 23 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-024'
WHERE k.ma_khach_hang='DEMO-KH-024' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-023');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-024','Trao đổi nhu cầu khách hàng Demo 24','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 24 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 24 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-025'
WHERE k.ma_khach_hang='DEMO-KH-025' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-024');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-025','Trao đổi nhu cầu khách hàng Demo 25','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 25 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 25 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-026'
WHERE k.ma_khach_hang='DEMO-KH-026' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-025');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-026','Trao đổi nhu cầu khách hàng Demo 26','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 26 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 26 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-027'
WHERE k.ma_khach_hang='DEMO-KH-027' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-026');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-027','Trao đổi nhu cầu khách hàng Demo 27','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 27 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 27 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-028'
WHERE k.ma_khach_hang='DEMO-KH-028' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-027');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-028','Trao đổi nhu cầu khách hàng Demo 28','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 28 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 28 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-001'
WHERE k.ma_khach_hang='DEMO-KH-029' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-028');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-029','Trao đổi nhu cầu khách hàng Demo 29','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 29 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 29 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-002'
WHERE k.ma_khach_hang='DEMO-KH-030' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-029');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-030','Trao đổi nhu cầu khách hàng Demo 30','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 30 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 30 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-003'
WHERE k.ma_khach_hang='DEMO-KH-031' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-030');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-031','Trao đổi nhu cầu khách hàng Demo 31','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 31 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 31 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-004'
WHERE k.ma_khach_hang='DEMO-KH-032' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-031');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-032','Trao đổi nhu cầu khách hàng Demo 32','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 32 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 32 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-005'
WHERE k.ma_khach_hang='DEMO-KH-033' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-032');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-033','Trao đổi nhu cầu khách hàng Demo 33','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 33 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 33 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-006'
WHERE k.ma_khach_hang='DEMO-KH-034' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-033');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-034','Trao đổi nhu cầu khách hàng Demo 34','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 34 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 34 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-007'
WHERE k.ma_khach_hang='DEMO-KH-035' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-034');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-035','Trao đổi nhu cầu khách hàng Demo 35','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 35 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 35 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-008'
WHERE k.ma_khach_hang='DEMO-KH-036' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-035');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-036','Trao đổi nhu cầu khách hàng Demo 36','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 36 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 36 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-009'
WHERE k.ma_khach_hang='DEMO-KH-037' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-036');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-037','Trao đổi nhu cầu khách hàng Demo 37','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 37 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 37 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-010'
WHERE k.ma_khach_hang='DEMO-KH-038' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-037');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-038','Trao đổi nhu cầu khách hàng Demo 38','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 38 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 38 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-011'
WHERE k.ma_khach_hang='DEMO-KH-039' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-038');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-039','Trao đổi nhu cầu khách hàng Demo 39','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 39 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 39 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-012'
WHERE k.ma_khach_hang='DEMO-KH-040' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-039');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-040','Trao đổi nhu cầu khách hàng Demo 40','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 40 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 40 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-013'
WHERE k.ma_khach_hang='DEMO-KH-001' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-040');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-041','Trao đổi nhu cầu khách hàng Demo 41','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 41 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 41 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-014'
WHERE k.ma_khach_hang='DEMO-KH-002' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-041');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-042','Trao đổi nhu cầu khách hàng Demo 42','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 42 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 42 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-015'
WHERE k.ma_khach_hang='DEMO-KH-003' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-042');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-043','Trao đổi nhu cầu khách hàng Demo 43','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 43 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 43 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-016'
WHERE k.ma_khach_hang='DEMO-KH-004' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-043');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-044','Trao đổi nhu cầu khách hàng Demo 44','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 44 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 44 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-017'
WHERE k.ma_khach_hang='DEMO-KH-005' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-044');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-045','Trao đổi nhu cầu khách hàng Demo 45','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 45 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 45 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-018'
WHERE k.ma_khach_hang='DEMO-KH-006' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-045');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-046','Trao đổi nhu cầu khách hàng Demo 46','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 46 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 46 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-019'
WHERE k.ma_khach_hang='DEMO-KH-007' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-046');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-047','Trao đổi nhu cầu khách hàng Demo 47','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 47 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 47 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-020'
WHERE k.ma_khach_hang='DEMO-KH-008' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-047');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-048','Trao đổi nhu cầu khách hàng Demo 48','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 48 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 48 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-021'
WHERE k.ma_khach_hang='DEMO-KH-009' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-048');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-049','Trao đổi nhu cầu khách hàng Demo 49','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 49 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 49 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-022'
WHERE k.ma_khach_hang='DEMO-KH-010' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-049');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-050','Trao đổi nhu cầu khách hàng Demo 50','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 50 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 50 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-023'
WHERE k.ma_khach_hang='DEMO-KH-011' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-050');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-051','Trao đổi nhu cầu khách hàng Demo 51','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 51 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 51 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-024'
WHERE k.ma_khach_hang='DEMO-KH-012' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-051');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-052','Trao đổi nhu cầu khách hàng Demo 52','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 52 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 52 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-025'
WHERE k.ma_khach_hang='DEMO-KH-013' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-052');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-053','Trao đổi nhu cầu khách hàng Demo 53','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 53 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 53 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-026'
WHERE k.ma_khach_hang='DEMO-KH-014' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-053');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-054','Trao đổi nhu cầu khách hàng Demo 54','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 54 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 54 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-027'
WHERE k.ma_khach_hang='DEMO-KH-015' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-054');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-055','Trao đổi nhu cầu khách hàng Demo 55','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 55 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 55 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-028'
WHERE k.ma_khach_hang='DEMO-KH-016' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-055');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-056','Trao đổi nhu cầu khách hàng Demo 56','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 56 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 56 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-001'
WHERE k.ma_khach_hang='DEMO-KH-017' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-056');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-057','Trao đổi nhu cầu khách hàng Demo 57','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 57 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 57 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-002'
WHERE k.ma_khach_hang='DEMO-KH-018' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-057');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-058','Trao đổi nhu cầu khách hàng Demo 58','CUOC_GAP',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 58 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 58 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-003'
WHERE k.ma_khach_hang='DEMO-KH-019' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-058');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-059','Trao đổi nhu cầu khách hàng Demo 59','EMAIL',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 59 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 59 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-004'
WHERE k.ma_khach_hang='DEMO-KH-020' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-059');

INSERT INTO hoat_dong(ma_hoat_dong,tieu_de,loai_hoat_dong,khach_hang_id,co_hoi_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,thoi_gian_bat_dau,thoi_gian_ket_thuc,trang_thai,noi_dung,ket_qua)
SELECT 'DEMO-HD-060','Trao đổi nhu cầu khách hàng Demo 60','CUOC_GOI',k.id,o.id,u.id,u.nhom_kinh_doanh_id,DATE_SUB(NOW(),INTERVAL 60 DAY),DATE_ADD(DATE_SUB(NOW(),INTERVAL 60 DAY),INTERVAL 30 MINUTE),'HOAN_THANH','Cuộc trao đổi mô phỏng','Đã trao đổi, cần theo dõi'
FROM khach_hang k CROSS JOIN nguoi_dung u LEFT JOIN co_hoi o ON o.ma_co_hoi='DEMO-CH-005'
WHERE k.ma_khach_hang='DEMO-KH-021' AND u.email='sales@crm.vn'
AND NOT EXISTS (SELECT 1 FROM hoat_dong WHERE ma_hoat_dong='DEMO-HD-060');

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-001','Báo giá Demo 1',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,83000000,'DA_CHAP_NHAN',DATE_ADD(CURDATE(),INTERVAL 30 DAY),83000000,0,83000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-017' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-001');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_CHAP_NHAN',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-001' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-002'
WHERE b.ma_bao_gia='DEMO-BG-001' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO hop_dong(so_hop_dong,bao_gia_id,phien_ban_bao_gia_id,co_hoi_id,khach_hang_id,ngay_ky,ngay_hieu_luc,ngay_het_han,gia_tri_hop_dong,trang_thai,created_by)
SELECT 'DEMO-HOP-DONG-001',b.id,v.id,b.co_hoi_id,b.khach_hang_id,DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_ADD(CURDATE(),INTERVAL 360 DAY),b.tong_tien,'DA_KY',b.nguoi_phu_trach_id
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
WHERE b.ma_bao_gia='DEMO-BG-001' AND NOT EXISTS (SELECT 1 FROM hop_dong WHERE so_hop_dong='DEMO-HOP-DONG-001');

INSERT INTO hop_dong_chi_tiet(hop_dong_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia,chiet_khau,thanh_tien)
SELECT h.id,d.san_pham_id,d.ma_san_pham_snapshot,d.ten_san_pham_snapshot,d.don_vi_tinh_snapshot,d.so_luong,d.don_gia,0,d.thanh_tien
FROM hop_dong h JOIN bao_gia_phien_ban v ON v.id=h.phien_ban_bao_gia_id JOIN bao_gia_chi_tiet d ON d.phien_ban_id=v.id
WHERE h.so_hop_dong='DEMO-HOP-DONG-001' AND NOT EXISTS (SELECT 1 FROM hop_dong_chi_tiet c WHERE c.hop_dong_id=h.id AND c.ma_san_pham_snapshot=d.ma_san_pham_snapshot);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-002','Báo giá Demo 2',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,87000000,'DA_CHAP_NHAN',DATE_ADD(CURDATE(),INTERVAL 30 DAY),87000000,0,87000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-018' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-002');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_CHAP_NHAN',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-002' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-003'
WHERE b.ma_bao_gia='DEMO-BG-002' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO hop_dong(so_hop_dong,bao_gia_id,phien_ban_bao_gia_id,co_hoi_id,khach_hang_id,ngay_ky,ngay_hieu_luc,ngay_het_han,gia_tri_hop_dong,trang_thai,created_by)
SELECT 'DEMO-HOP-DONG-002',b.id,v.id,b.co_hoi_id,b.khach_hang_id,DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_ADD(CURDATE(),INTERVAL 360 DAY),b.tong_tien,'DA_KY',b.nguoi_phu_trach_id
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
WHERE b.ma_bao_gia='DEMO-BG-002' AND NOT EXISTS (SELECT 1 FROM hop_dong WHERE so_hop_dong='DEMO-HOP-DONG-002');

INSERT INTO hop_dong_chi_tiet(hop_dong_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia,chiet_khau,thanh_tien)
SELECT h.id,d.san_pham_id,d.ma_san_pham_snapshot,d.ten_san_pham_snapshot,d.don_vi_tinh_snapshot,d.so_luong,d.don_gia,0,d.thanh_tien
FROM hop_dong h JOIN bao_gia_phien_ban v ON v.id=h.phien_ban_bao_gia_id JOIN bao_gia_chi_tiet d ON d.phien_ban_id=v.id
WHERE h.so_hop_dong='DEMO-HOP-DONG-002' AND NOT EXISTS (SELECT 1 FROM hop_dong_chi_tiet c WHERE c.hop_dong_id=h.id AND c.ma_san_pham_snapshot=d.ma_san_pham_snapshot);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-003','Báo giá Demo 3',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,91000000,'DA_CHAP_NHAN',DATE_ADD(CURDATE(),INTERVAL 30 DAY),91000000,0,91000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-019' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-003');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_CHAP_NHAN',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-003' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-004'
WHERE b.ma_bao_gia='DEMO-BG-003' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO hop_dong(so_hop_dong,bao_gia_id,phien_ban_bao_gia_id,co_hoi_id,khach_hang_id,ngay_ky,ngay_hieu_luc,ngay_het_han,gia_tri_hop_dong,trang_thai,created_by)
SELECT 'DEMO-HOP-DONG-003',b.id,v.id,b.co_hoi_id,b.khach_hang_id,DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_ADD(CURDATE(),INTERVAL 360 DAY),b.tong_tien,'DA_KY',b.nguoi_phu_trach_id
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
WHERE b.ma_bao_gia='DEMO-BG-003' AND NOT EXISTS (SELECT 1 FROM hop_dong WHERE so_hop_dong='DEMO-HOP-DONG-003');

INSERT INTO hop_dong_chi_tiet(hop_dong_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia,chiet_khau,thanh_tien)
SELECT h.id,d.san_pham_id,d.ma_san_pham_snapshot,d.ten_san_pham_snapshot,d.don_vi_tinh_snapshot,d.so_luong,d.don_gia,0,d.thanh_tien
FROM hop_dong h JOIN bao_gia_phien_ban v ON v.id=h.phien_ban_bao_gia_id JOIN bao_gia_chi_tiet d ON d.phien_ban_id=v.id
WHERE h.so_hop_dong='DEMO-HOP-DONG-003' AND NOT EXISTS (SELECT 1 FROM hop_dong_chi_tiet c WHERE c.hop_dong_id=h.id AND c.ma_san_pham_snapshot=d.ma_san_pham_snapshot);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-004','Báo giá Demo 4',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,95000000,'DA_CHAP_NHAN',DATE_ADD(CURDATE(),INTERVAL 30 DAY),95000000,0,95000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-020' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-004');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_CHAP_NHAN',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-004' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-005'
WHERE b.ma_bao_gia='DEMO-BG-004' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO hop_dong(so_hop_dong,bao_gia_id,phien_ban_bao_gia_id,co_hoi_id,khach_hang_id,ngay_ky,ngay_hieu_luc,ngay_het_han,gia_tri_hop_dong,trang_thai,created_by)
SELECT 'DEMO-HOP-DONG-004',b.id,v.id,b.co_hoi_id,b.khach_hang_id,DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_ADD(CURDATE(),INTERVAL 360 DAY),b.tong_tien,'DA_KY',b.nguoi_phu_trach_id
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
WHERE b.ma_bao_gia='DEMO-BG-004' AND NOT EXISTS (SELECT 1 FROM hop_dong WHERE so_hop_dong='DEMO-HOP-DONG-004');

INSERT INTO hop_dong_chi_tiet(hop_dong_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia,chiet_khau,thanh_tien)
SELECT h.id,d.san_pham_id,d.ma_san_pham_snapshot,d.ten_san_pham_snapshot,d.don_vi_tinh_snapshot,d.so_luong,d.don_gia,0,d.thanh_tien
FROM hop_dong h JOIN bao_gia_phien_ban v ON v.id=h.phien_ban_bao_gia_id JOIN bao_gia_chi_tiet d ON d.phien_ban_id=v.id
WHERE h.so_hop_dong='DEMO-HOP-DONG-004' AND NOT EXISTS (SELECT 1 FROM hop_dong_chi_tiet c WHERE c.hop_dong_id=h.id AND c.ma_san_pham_snapshot=d.ma_san_pham_snapshot);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-005','Báo giá Demo 5',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,99000000,'DA_CHAP_NHAN',DATE_ADD(CURDATE(),INTERVAL 30 DAY),99000000,0,99000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-021' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-005');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_CHAP_NHAN',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-005' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-006'
WHERE b.ma_bao_gia='DEMO-BG-005' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO hop_dong(so_hop_dong,bao_gia_id,phien_ban_bao_gia_id,co_hoi_id,khach_hang_id,ngay_ky,ngay_hieu_luc,ngay_het_han,gia_tri_hop_dong,trang_thai,created_by)
SELECT 'DEMO-HOP-DONG-005',b.id,v.id,b.co_hoi_id,b.khach_hang_id,DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_SUB(CURDATE(),INTERVAL 5 DAY),DATE_ADD(CURDATE(),INTERVAL 360 DAY),b.tong_tien,'DA_KY',b.nguoi_phu_trach_id
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
WHERE b.ma_bao_gia='DEMO-BG-005' AND NOT EXISTS (SELECT 1 FROM hop_dong WHERE so_hop_dong='DEMO-HOP-DONG-005');

INSERT INTO hop_dong_chi_tiet(hop_dong_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia,chiet_khau,thanh_tien)
SELECT h.id,d.san_pham_id,d.ma_san_pham_snapshot,d.ten_san_pham_snapshot,d.don_vi_tinh_snapshot,d.so_luong,d.don_gia,0,d.thanh_tien
FROM hop_dong h JOIN bao_gia_phien_ban v ON v.id=h.phien_ban_bao_gia_id JOIN bao_gia_chi_tiet d ON d.phien_ban_id=v.id
WHERE h.so_hop_dong='DEMO-HOP-DONG-005' AND NOT EXISTS (SELECT 1 FROM hop_dong_chi_tiet c WHERE c.hop_dong_id=h.id AND c.ma_san_pham_snapshot=d.ma_san_pham_snapshot);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-006','Báo giá Demo 6',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,103000000,'DA_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),103000000,0,103000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-022' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-006');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-006' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-007'
WHERE b.ma_bao_gia='DEMO-BG-006' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-007','Báo giá Demo 7',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,107000000,'DA_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),107000000,0,107000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-023' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-007');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-007' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-008'
WHERE b.ma_bao_gia='DEMO-BG-007' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-008','Báo giá Demo 8',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,111000000,'DA_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),111000000,0,111000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-024' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-008');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'DA_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-008' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-009'
WHERE b.ma_bao_gia='DEMO-BG-008' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-009','Báo giá Demo 9',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,115000000,'CHO_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),115000000,0,115000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-025' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-009');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'CHO_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-009' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-010'
WHERE b.ma_bao_gia='DEMO-BG-009' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-010','Báo giá Demo 10',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,119000000,'CHO_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),119000000,0,119000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-026' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-010');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'CHO_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-010' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-011'
WHERE b.ma_bao_gia='DEMO-BG-010' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-011','Báo giá Demo 11',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,123000000,'CHO_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),123000000,0,123000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-027' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-011');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'CHO_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-011' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-012'
WHERE b.ma_bao_gia='DEMO-BG-011' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

INSERT INTO bao_gia(ma_bao_gia,tieu_de,co_hoi_id,khach_hang_id,nguoi_lien_he_id,nguoi_phu_trach_id,nhom_kinh_doanh_id,phien_ban,tong_tien,trang_thai,ngay_het_hieu_luc,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,created_by)
SELECT 'DEMO-BG-012','Báo giá Demo 12',o.id,o.khach_hang_id,o.nguoi_lien_he_chinh_id,o.nguoi_phu_trach_id,o.nhom_kinh_doanh_id,1,127000000,'CHO_DUYET',DATE_ADD(CURDATE(),INTERVAL 30 DAY),127000000,0,127000000,0,o.nguoi_phu_trach_id
FROM co_hoi o WHERE o.ma_co_hoi='DEMO-CH-028' AND NOT EXISTS (SELECT 1 FROM bao_gia WHERE ma_bao_gia='DEMO-BG-012');

INSERT INTO bao_gia_phien_ban(bao_gia_id,so_phien_ban,trang_thai,tong_truoc_chiet_khau,tong_chiet_khau,tong_sau_chiet_khau,ty_le_chiet_khau_tong,tao_boi_id)
SELECT b.id,1,'CHO_DUYET',b.tong_tien,0,b.tong_tien,0,b.nguoi_phu_trach_id
FROM bao_gia b WHERE b.ma_bao_gia='DEMO-BG-012' AND NOT EXISTS
(SELECT 1 FROM bao_gia_phien_ban v WHERE v.bao_gia_id=b.id AND v.so_phien_ban=1);

INSERT INTO bao_gia_chi_tiet(phien_ban_id,san_pham_id,ma_san_pham_snapshot,ten_san_pham_snapshot,don_vi_tinh_snapshot,so_luong,don_gia_niem_yet,don_gia,thanh_tien)
SELECT v.id,p.id,p.ma_san_pham,p.ten_san_pham,p.don_vi_tinh,1,b.tong_tien,b.tong_tien,b.tong_tien
FROM bao_gia b JOIN bao_gia_phien_ban v ON v.bao_gia_id=b.id AND v.so_phien_ban=1
JOIN san_pham p ON p.ma_san_pham='DEMO-SP-001'
WHERE b.ma_bao_gia='DEMO-BG-012' AND NOT EXISTS
(SELECT 1 FROM bao_gia_chi_tiet d WHERE d.phien_ban_id=v.id AND d.ma_san_pham_snapshot=p.ma_san_pham);

COMMIT;
-- Verify seeded records independently (these SELECTs do not change data)
SELECT 'customers' AS module, COUNT(*) AS demo_rows FROM khach_hang WHERE ma_khach_hang LIKE 'DEMO-KH-%'
UNION ALL SELECT 'leads',COUNT(*) FROM `lead` WHERE ma_lead LIKE 'DEMO-LEAD-%'
UNION ALL SELECT 'opportunities',COUNT(*) FROM co_hoi WHERE ma_co_hoi LIKE 'DEMO-CH-%'
UNION ALL SELECT 'activities',COUNT(*) FROM hoat_dong WHERE ma_hoat_dong LIKE 'DEMO-HD-%'
UNION ALL SELECT 'quotes',COUNT(*) FROM bao_gia WHERE ma_bao_gia LIKE 'DEMO-BG-%'
UNION ALL SELECT 'contracts',COUNT(*) FROM hop_dong WHERE so_hop_dong LIKE 'DEMO-HOP-DONG-%';
