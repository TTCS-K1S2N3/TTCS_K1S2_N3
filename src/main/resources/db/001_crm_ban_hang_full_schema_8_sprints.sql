-- =====================================================================
-- CRM BAN HANG B2B - CANONICAL DATABASE SCHEMA
-- 8 SPRINT / 76 USER STORY / 350 STORY POINT
-- MySQL 8.4 LTS - utf8mb4 - Asia/Ho_Chi_Minh
--
-- MUC DICH:
--   - Day la schema nen DUY NHAT cho nhanh dev khi bat dau lai du an.
--   - Bao phu toan bo 76 User Story trong sheet "4. Product Backlog".
--   - Da hop nhat schema/migration Sprint 1 de code S1-01 -> S1-10 chay truc tiep tren bootstrap nay.
--   - Sau khi merge vao dev, cac feature branch KHONG DUOC tu y ALTER/DROP/RENAME
--     table/column/foreign key/type/status dung chung.
--   - Truong tuy chinh, cau hinh, permission, automation, notification... duoc thiet
--     ke theo du lieu de han che nhu cau ALTER TABLE trong cac Sprint sau.
--
-- CANH BAO:
--   Script nay la BOOTSTRAP TU DAU va co DROP DATABASE.
--   CHI chay tren database local/dev moi khoi tao hoac khi chu dong reset du lieu.
--   KHONG chay lai tren staging/production da co du lieu.
--
-- NGUYEN TAC:
--   - Money: DECIMAL(18,2)
--   - Date/time: DATETIME theo Asia/Ho_Chi_Minh
--   - ID noi bo: BIGINT UNSIGNED AUTO_INCREMENT
--   - Table/column: snake_case
--   - File upload nam ngoai DB; DB chi luu metadata + duong dan
--   - Status/code dung VARCHAR de code co the mo rong gia tri ma KHONG can ALTER TABLE
--   - Business transaction, RBAC, Data Scope va validation nam o Service/DAO
--   - `lead` la tu khoa cua MySQL 8.x, vi vay DAO PHAI dung backtick: FROM `lead`
--   - Tuyet doi khong ghi password/token raw/SMTP secret vao log JSON hoac email log
-- =====================================================================

SET NAMES utf8mb4;
SET time_zone = '+07:00';

DROP DATABASE IF EXISTS crm_ban_hang;

CREATE DATABASE crm_ban_hang
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crm_ban_hang;

-- =====================================================================
-- 0. PHIEN BAN SCHEMA
-- =====================================================================

CREATE TABLE phien_ban_schema (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_phien_ban            VARCHAR(50) NOT NULL UNIQUE,
    so_sprint               INT NOT NULL,
    so_user_story           INT NOT NULL,
    tong_story_point        INT NOT NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'DANG_SU_DUNG',
    mo_ta                   VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO phien_ban_schema
    (ma_phien_ban, so_sprint, so_user_story, tong_story_point, trang_thai, mo_ta)
VALUES
    ('CRM-8SPRINT-76STORY-V2-S1-COMPAT', 8, 76, 350, 'DANG_SU_DUNG',
     'Canonical schema 8 Sprint da dong bo contract runtime Sprint 1 (S1-01 -> S1-10) va du lieu seed local/dev.');

-- =====================================================================
-- 1. TO CHUC, TAI KHOAN, ROLE, DATA SCOPE - SPRINT 1-2
-- =====================================================================

CREATE TABLE khu_vuc_dia_ly (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_khu_vuc              VARCHAR(50) NOT NULL UNIQUE,
    ten_khu_vuc             VARCHAR(150) NOT NULL,
    loai_khu_vuc            VARCHAR(50) NULL,
    khu_vuc_cha_id          BIGINT UNSIGNED NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kv_cha
        FOREIGN KEY (khu_vuc_cha_id) REFERENCES khu_vuc_dia_ly(id)
        ON DELETE SET NULL,
    INDEX idx_kv_cha (khu_vuc_cha_id),
    INDEX idx_kv_hd (hoat_dong, thu_tu_hien_thi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nhom_kinh_doanh (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_nhom                 VARCHAR(50) NOT NULL UNIQUE,
    ten_nhom                VARCHAR(150) NOT NULL,
    mo_ta                   TEXT NULL,
    nhom_cha_id             BIGINT UNSIGNED NULL,
    khu_vuc_id              BIGINT UNSIGNED NULL,
    truong_nhom_id          BIGINT UNSIGNED NULL,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nhom_cha
        FOREIGN KEY (nhom_cha_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_nhom_kv
        FOREIGN KEY (khu_vuc_id) REFERENCES khu_vuc_dia_ly(id)
        ON DELETE SET NULL,
    INDEX idx_nhom_cha (nhom_cha_id),
    INDEX idx_nhom_kv (khu_vuc_id),
    INDEX idx_nhom_hd (hoat_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE vai_tro (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_vai_tro              VARCHAR(50) NOT NULL UNIQUE,
    ten_vai_tro             VARCHAR(100) NOT NULL,
    mo_ta                   VARCHAR(500) NULL,
    pham_vi_toi_da          VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN',
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE module_he_thong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_module               VARCHAR(60) NOT NULL UNIQUE,
    ten_module              VARCHAR(150) NOT NULL,
    mo_ta                   VARCHAR(500) NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hien_thi_menu           TINYINT(1) NOT NULL DEFAULT 1,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE vai_tro_module (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    vai_tro_id              BIGINT UNSIGNED NOT NULL,
    module_id               BIGINT UNSIGNED NOT NULL,
    muc_quyen               VARCHAR(20) NOT NULL,
    pham_vi_du_lieu         VARCHAR(20) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vtm_vt
        FOREIGN KEY (vai_tro_id) REFERENCES vai_tro(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vtm_md
        FOREIGN KEY (module_id) REFERENCES module_he_thong(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_vtm (vai_tro_id, module_id),
    INDEX idx_vtm_md (module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nguoi_dung (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ho_ten                  VARCHAR(150) NOT NULL,
    email                   VARCHAR(255) NOT NULL,
    mat_khau                VARCHAR(255) NOT NULL,
    so_dien_thoai           VARCHAR(20) NULL,
    chu_ky_email            TEXT NULL,
    anh_dai_dien_path       VARCHAR(500) NULL,
    anh_dai_dien_thumb_path VARCHAR(500) NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG',
    so_lan_sai              INT UNSIGNED NOT NULL DEFAULT 0,
    thoi_gian_khoa          DATETIME NULL,
    bat_buoc_doi_mat_khau   TINYINT(1) NOT NULL DEFAULT 0,
    ngay_doi_mat_khau       DATETIME NULL,
    session_version         INT UNSIGNED NOT NULL DEFAULT 1,
    nhom_kinh_doanh_id      BIGINT UNSIGNED NULL,
    email_da_xac_thuc_luc   DATETIME NULL,
    lan_dang_nhap_cuoi      DATETIME NULL,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nd_nhom
        FOREIGN KEY (nhom_kinh_doanh_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_nd_email (email),
    INDEX idx_nguoi_dung_ho_ten (ho_ten),
    INDEX idx_nguoi_dung_email (email),
    INDEX idx_nguoi_dung_nhom (nhom_kinh_doanh_id),
    INDEX idx_nguoi_dung_trang_thai (trang_thai),
    INDEX idx_nd_sdt (so_dien_thoai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE nguoi_dung
    ADD CONSTRAINT fk_nd_tao_boi
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL;

ALTER TABLE nhom_kinh_doanh
    ADD CONSTRAINT fk_nhom_truong
        FOREIGN KEY (truong_nhom_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL;

CREATE TABLE nguoi_dung_vai_tro (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    vai_tro_id              BIGINT UNSIGNED NOT NULL,
    duoc_gan_boi_id         BIGINT UNSIGNED NULL,
    duoc_gan_luc            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thu_hoi_luc             DATETIME NULL,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_ndvt_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ndvt_vt
        FOREIGN KEY (vai_tro_id) REFERENCES vai_tro(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ndvt_gan
        FOREIGN KEY (duoc_gan_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_ndvt (nguoi_dung_id, vai_tro_id),
    INDEX idx_ndvt_vt (vai_tro_id),
    INDEX idx_ndvt_hd (nguoi_dung_id, hoat_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE phien_dang_nhap (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    ma_phien                VARCHAR(255) NOT NULL,
    session_version         INT UNSIGNED NOT NULL DEFAULT 1,
    dia_chi_ip              VARCHAR(50) NULL,
    thong_tin_thiet_bi      VARCHAR(500) NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG',
    thoi_gian_tao           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoi_gian_hoat_dong_cuoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    het_han_luc             DATETIME NULL,
    thoi_gian_thu_hoi       DATETIME NULL,
    ly_do_thu_hoi           VARCHAR(255) NULL,
    CONSTRAINT fk_phien_nguoi_dung
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_phien_ma_phien (ma_phien),
    INDEX idx_phien_nguoi_dung (nguoi_dung_id),
    INDEX idx_phien_ma_phien (ma_phien),
    INDEX idx_phien_trang_thai (trang_thai),
    INDEX idx_phien_het_han (het_han_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dat_lai_mat_khau_token (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    token                   VARCHAR(255) NOT NULL,
    thoi_gian_tao           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoi_gian_het_han       DATETIME NOT NULL,
    da_su_dung              TINYINT(1) NOT NULL DEFAULT 0,
    thoi_gian_su_dung       DATETIME NULL,
    dia_chi_ip_yeu_cau      VARCHAR(45) NULL,
    CONSTRAINT fk_token_nguoi_dung
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_token_value (token),
    INDEX idx_token_value (token),
    INDEX idx_token_nguoi_dung (nguoi_dung_id),
    INDEX idx_token_het_han (thoi_gian_het_han)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE token_kich_hoat_tai_khoan (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    token_hash              CHAR(64) NOT NULL,
    tao_luc                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    het_han_luc             DATETIME NOT NULL,
    da_su_dung_luc          DATETIME NULL,
    CONSTRAINT fk_tkhtk_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_tkhtk_token (token_hash),
    INDEX idx_tkhtk_nd (nguoi_dung_id),
    INDEX idx_tkhtk_hh (het_han_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nhat_ky_ban_giao (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_bi_khoa_id        BIGINT UNSIGNED NOT NULL,
    nguoi_tiep_nhan_id      BIGINT UNSIGNED NOT NULL,
    nguoi_thuc_hien_id      BIGINT UNSIGNED NOT NULL,
    so_khach_hang_chuyen    INT UNSIGNED NOT NULL DEFAULT 0,
    so_co_hoi_chuyen        INT UNSIGNED NOT NULL DEFAULT 0,
    so_lead_chuyen          INT UNSIGNED NOT NULL DEFAULT 0,
    so_cong_viec_chuyen     INT UNSIGNED NOT NULL DEFAULT 0,
    ly_do                   VARCHAR(500) NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'HOAN_TAT',
    hoan_tat_luc            DATETIME NULL,
    ghi_chu                 TEXT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nkbg_nguoi_bi_khoa
        FOREIGN KEY (nguoi_bi_khoa_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_nkbg_nguoi_tiep_nhan
        FOREIGN KEY (nguoi_tiep_nhan_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_nkbg_nguoi_thuc_hien
        FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    INDEX idx_nkbg_nguoi_bi_khoa (nguoi_bi_khoa_id),
    INDEX idx_nkbg_nguoi_tiep_nhan (nguoi_tiep_nhan_id),
    INDEX idx_nkbg_nguoi_thuc_hien (nguoi_thuc_hien_id),
    INDEX idx_nkbg_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cac bang log/thong bao co cap (loai_doi_tuong, doi_tuong_id) la tham chieu da hinh.
-- MySQL khong the tao 1 FK den nhieu table, vi vay Service phai validate doi_tuong_id theo loai_doi_tuong.
CREATE TABLE nhat_ky_ban_giao_chi_tiet (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nhat_ky_ban_giao_id     BIGINT UNSIGNED NOT NULL,
    loai_doi_tuong          VARCHAR(50) NOT NULL,
    doi_tuong_id            BIGINT UNSIGNED NOT NULL,
    chu_so_huu_cu_id        BIGINT UNSIGNED NULL,
    chu_so_huu_moi_id       BIGINT UNSIGNED NOT NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'THANH_CONG',
    loi_rut_gon             VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nkbgct_bg
        FOREIGN KEY (nhat_ky_ban_giao_id) REFERENCES nhat_ky_ban_giao(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_nkbgct_cu
        FOREIGN KEY (chu_so_huu_cu_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_nkbgct_moi
        FOREIGN KEY (chu_so_huu_moi_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    INDEX idx_nkbgct_dt (loai_doi_tuong, doi_tuong_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nhat_ky_he_thong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_thuc_hien_id      BIGINT UNSIGNED NULL,
    hanh_dong               VARCHAR(80) NOT NULL,
    loai_doi_tuong          VARCHAR(80) NOT NULL,
    doi_tuong_id            BIGINT UNSIGNED NULL,
    gia_tri_truoc_json      JSON NULL,
    gia_tri_sau_json        JSON NULL,
    ly_do                   VARCHAR(1000) NULL,
    dia_chi_ip              VARCHAR(45) NULL,
    thong_tin_thiet_bi      VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nkht_nd
        FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_nkht_nd_time (nguoi_thuc_hien_id, created_at),
    INDEX idx_nkht_dt (loai_doi_tuong, doi_tuong_id, created_at),
    INDEX idx_nkht_hd (hanh_dong, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 2. CAU HINH, IMPORT, BO LOC DUNG CHUNG
-- =====================================================================

CREATE TABLE cau_hinh_he_thong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_cau_hinh             VARCHAR(100) NOT NULL UNIQUE,
    nhom_cau_hinh           VARCHAR(60) NOT NULL,
    kieu_du_lieu            VARCHAR(30) NOT NULL,
    gia_tri                 TEXT NULL,
    mo_ta                   VARCHAR(1000) NULL,
    bat_buoc_cau_hinh       TINYINT(1) NOT NULL DEFAULT 0,
    bao_mat                 TINYINT(1) NOT NULL DEFAULT 0,
    updated_by              BIGINT UNSIGNED NULL,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chht_nd
        FOREIGN KEY (updated_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_chht_nhom (nhom_cau_hinh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dot_nhap_du_lieu (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    loai_doi_tuong          VARCHAR(50) NOT NULL,
    ten_file_goc            VARCHAR(255) NOT NULL,
    duong_dan_file          VARCHAR(500) NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'TAI_LEN',
    tong_dong               INT UNSIGNED NOT NULL DEFAULT 0,
    so_dong_hop_le          INT UNSIGNED NOT NULL DEFAULT 0,
    so_dong_loi             INT UNSIGNED NOT NULL DEFAULT 0,
    so_dong_da_nhap         INT UNSIGNED NOT NULL DEFAULT 0,
    xem_truoc_json          JSON NULL,
    nguoi_thuc_hien_id      BIGINT UNSIGNED NOT NULL,
    tao_luc                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hoan_tat_luc            DATETIME NULL,
    CONSTRAINT fk_dndl_nd
        FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    INDEX idx_dndl_loai (loai_doi_tuong, tao_luc),
    INDEX idx_dndl_tt (trang_thai, tao_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loi_nhap_du_lieu (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dot_nhap_id             BIGINT UNSIGNED NOT NULL,
    so_dong                 INT UNSIGNED NOT NULL,
    ma_loi                  VARCHAR(80) NULL,
    thong_bao_loi           VARCHAR(1000) NOT NULL,
    du_lieu_dong_json       JSON NULL,
    co_the_bo_qua           TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_lndl_dot
        FOREIGN KEY (dot_nhap_id) REFERENCES dot_nhap_du_lieu(id)
        ON DELETE CASCADE,
    INDEX idx_lndl_dot_dong (dot_nhap_id, so_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bo_loc_da_luu (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    loai_doi_tuong          VARCHAR(50) NOT NULL,
    ten_bo_loc              VARCHAR(150) NOT NULL,
    tieu_chi_json           JSON NOT NULL,
    mac_dinh                TINYINT(1) NOT NULL DEFAULT 0,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bldl_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_bldl_ten (nguoi_dung_id, loai_doi_tuong, ten_bo_loc),
    INDEX idx_bldl_loai (nguoi_dung_id, loai_doi_tuong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 3. DANH MUC & CAU HINH BAN HANG - SPRINT 2
-- =====================================================================

CREATE TABLE nganh_nghe (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_nganh                VARCHAR(50) NOT NULL UNIQUE,
    ten_nganh               VARCHAR(150) NOT NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quy_mo_doanh_nghiep (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_quy_mo               VARCHAR(50) NOT NULL UNIQUE,
    ten_quy_mo              VARCHAR(150) NOT NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nguon_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_nguon                VARCHAR(50) NOT NULL UNIQUE,
    ten_nguon               VARCHAR(150) NOT NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loai_hoat_dong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_loai                 VARCHAR(50) NOT NULL UNIQUE,
    ten_loai                VARCHAR(150) NOT NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ly_do_thang_thua (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_ly_do                VARCHAR(50) NOT NULL UNIQUE,
    ten_ly_do               VARCHAR(150) NOT NULL,
    loai                    VARCHAR(20) NOT NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ldtt_loai (loai, hoat_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doi_thu (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_doi_thu              VARCHAR(50) NOT NULL UNIQUE,
    ten_doi_thu             VARCHAR(200) NOT NULL,
    website                 VARCHAR(255) NULL,
    ghi_chu                 TEXT NULL,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE san_pham (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_san_pham             VARCHAR(60) NOT NULL UNIQUE,
    ten_san_pham            VARCHAR(200) NOT NULL,
    loai_san_pham           VARCHAR(30) NOT NULL,
    don_vi_tinh             VARCHAR(50) NOT NULL,
    gia_niem_yet            DECIMAL(18,2) NOT NULL DEFAULT 0,
    gia_san                 DECIMAL(18,2) NULL,
    gia_von                 DECIMAL(18,2) NULL,
    mo_ta                   TEXT NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'DANG_KINH_DOANH',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sp_tt (trang_thai),
    INDEX idx_sp_ten (ten_san_pham)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pipeline_ban_hang (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_pipeline             VARCHAR(50) NOT NULL UNIQUE,
    ten_pipeline            VARCHAR(150) NOT NULL,
    mo_ta                   TEXT NULL,
    mac_dinh                TINYINT(1) NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE giai_doan_pipeline (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    pipeline_id             BIGINT UNSIGNED NOT NULL,
    ma_giai_doan            VARCHAR(60) NOT NULL,
    ten_giai_doan           VARCHAR(150) NOT NULL,
    thu_tu                  INT NOT NULL,
    xac_suat_mac_dinh       DECIMAL(5,2) NOT NULL DEFAULT 0,
    so_ngay_dinh_tre        INT UNSIGNED NULL,
    loai_ket_thuc           VARCHAR(20) NULL,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_gdp_pl
        FOREIGN KEY (pipeline_id) REFERENCES pipeline_ban_hang(id)
        ON DELETE RESTRICT,
    UNIQUE KEY uk_gdp_ma (pipeline_id, ma_giai_doan),
    UNIQUE KEY uk_gdp_tt (pipeline_id, thu_tu),
    INDEX idx_gdp_hd (pipeline_id, hoat_dong, thu_tu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dieu_kien_giai_doan (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    giai_doan_nguon_id      BIGINT UNSIGNED NOT NULL,
    giai_doan_dich_id       BIGINT UNSIGNED NULL,
    ma_dieu_kien            VARCHAR(80) NOT NULL,
    ten_dieu_kien           VARCHAR(200) NOT NULL,
    loai_dieu_kien          VARCHAR(50) NOT NULL,
    cau_hinh_json           JSON NULL,
    thong_bao_thieu         VARCHAR(500) NOT NULL,
    thu_tu                  INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_dkgd_tu
        FOREIGN KEY (giai_doan_nguon_id) REFERENCES giai_doan_pipeline(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dkgd_den
        FOREIGN KEY (giai_doan_dich_id) REFERENCES giai_doan_pipeline(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_dkgd_ma (giai_doan_nguon_id, ma_dieu_kien),
    INDEX idx_dkgd_hd (giai_doan_nguon_id, hoat_dong, thu_tu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE truong_tuy_chinh (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    loai_doi_tuong          VARCHAR(30) NOT NULL,
    ma_truong               VARCHAR(80) NOT NULL,
    ten_truong              VARCHAR(150) NOT NULL,
    kieu_du_lieu            VARCHAR(30) NOT NULL,
    bat_buoc                TINYINT(1) NOT NULL DEFAULT 0,
    lua_chon_json           JSON NULL,
    gia_tri_mac_dinh_json   JSON NULL,
    thu_tu_hien_thi         INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ttc_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_ttc (loai_doi_tuong, ma_truong),
    INDEX idx_ttc_loai (loai_doi_tuong, hoat_dong, thu_tu_hien_thi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 4. KHACH HANG & NGUOI LIEN HE - SPRINT 3
-- =====================================================================

CREATE TABLE khach_hang (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_khach_hang           VARCHAR(50) NULL UNIQUE,
    ten_cong_ty             VARCHAR(255) NOT NULL,
    ten_chuan_hoa           VARCHAR(255) NULL,
    ma_so_thue              VARCHAR(50) NULL,
    nganh_nghe_id           BIGINT UNSIGNED NULL,
    quy_mo_id               BIGINT UNSIGNED NULL,
    website                 VARCHAR(255) NULL,
    website_chuan_hoa       VARCHAR(255) NULL,
    dia_chi                 VARCHAR(500) NULL,
    khu_vuc_id              BIGINT UNSIGNED NULL,
    nguoi_so_huu_id         BIGINT UNSIGNED NOT NULL,
    nhom_kinh_doanh_id      BIGINT UNSIGNED NULL,
    doanh_thu_uoc_tinh      DECIMAL(18,2) NULL DEFAULT 0.00,
    cong_ty_me_id            BIGINT UNSIGNED NULL,
    trang_thai              VARCHAR(50) NOT NULL DEFAULT 'TIEM_NANG',
    co_rui_ro               TINYINT(1) NOT NULL DEFAULT 0,
    rui_ro_cap_nhat_luc     DATETIME NULL,
    lan_tuong_tac_cuoi      DATETIME NULL,
    gop_vao_khach_hang_id   BIGINT UNSIGNED NULL,
    mo_ta_chi_tiet          TEXT NULL,
    ngay_tao                DATE NOT NULL DEFAULT (CURRENT_DATE),
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kh_nganh
        FOREIGN KEY (nganh_nghe_id) REFERENCES nganh_nghe(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_kh_qm
        FOREIGN KEY (quy_mo_id) REFERENCES quy_mo_doanh_nghiep(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_kh_kv
        FOREIGN KEY (khu_vuc_id) REFERENCES khu_vuc_dia_ly(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_kh_nguoi_so_huu
        FOREIGN KEY (nguoi_so_huu_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_kh_nhom
        FOREIGN KEY (nhom_kinh_doanh_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_kh_me
        FOREIGN KEY (cong_ty_me_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_kh_gop
        FOREIGN KEY (gop_vao_khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_kh_mst (ma_so_thue),
    INDEX idx_kh_nguoi_so_huu (nguoi_so_huu_id),
    INDEX idx_kh_nhom (nhom_kinh_doanh_id),
    INDEX idx_kh_ten_cong_ty (ten_cong_ty),
    INDEX idx_kh_trang_thai (trang_thai),
    INDEX idx_kh_nganh (nganh_nghe_id),
    INDEX idx_kh_qm (quy_mo_id),
    INDEX idx_kh_kv (khu_vuc_id),
    INDEX idx_kh_ten_chuan (ten_chuan_hoa),
    INDEX idx_kh_web_chuan (website_chuan_hoa),
    INDEX idx_kh_ltt (lan_tuong_tac_cuoi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nguoi_lien_he (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    khach_hang_id           BIGINT UNSIGNED NOT NULL,
    ho_ten                  VARCHAR(150) NOT NULL,
    chuc_danh               VARCHAR(150) NULL,
    email                   VARCHAR(255) NULL,
    so_dien_thoai           VARCHAR(20) NULL,
    vai_tro_quyet_dinh      VARCHAR(50) NULL,
    la_dau_moi_chinh        TINYINT(1) NOT NULL DEFAULT 0,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'DANG_HOAT_DONG',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nlh_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE RESTRICT,
    INDEX idx_nlh_kh (khach_hang_id, trang_thai),
    INDEX idx_nlh_email (email),
    INDEX idx_nlh_sdt (so_dien_thoai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lich_su_lien_he_cong_ty (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_lien_he_id        BIGINT UNSIGNED NOT NULL,
    khach_hang_id           BIGINT UNSIGNED NOT NULL,
    chuc_danh               VARCHAR(150) NULL,
    vai_tro_quyet_dinh      VARCHAR(50) NULL,
    tu_ngay                 DATE NULL,
    den_ngay                DATE NULL,
    ghi_chu                 VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lslh_nlh
        FOREIGN KEY (nguoi_lien_he_id) REFERENCES nguoi_lien_he(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_lslh_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE RESTRICT,
    INDEX idx_lslh_nlh (nguoi_lien_he_id, tu_ngay)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lich_su_gop_khach_hang (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    khach_hang_nguon_id     BIGINT UNSIGNED NOT NULL,
    khach_hang_dich_id      BIGINT UNSIGNED NOT NULL,
    thuc_hien_boi_id        BIGINT UNSIGNED NOT NULL,
    du_lieu_so_sanh_json    JSON NULL,
    ket_qua_chuyen_json     JSON NULL,
    ly_do                   VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lsgkh_nguon
        FOREIGN KEY (khach_hang_nguon_id) REFERENCES khach_hang(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lsgkh_dich
        FOREIGN KEY (khach_hang_dich_id) REFERENCES khach_hang(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lsgkh_nd
        FOREIGN KEY (thuc_hien_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    INDEX idx_lsgkh_src (khach_hang_nguon_id, created_at),
    INDEX idx_lsgkh_dst (khach_hang_dich_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE yeu_cau_ho_tro (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_yeu_cau              VARCHAR(50) NOT NULL UNIQUE,
    khach_hang_id           BIGINT UNSIGNED NOT NULL,
    nguoi_lien_he_id        BIGINT UNSIGNED NULL,
    nguoi_xu_ly_id          BIGINT UNSIGNED NULL,
    tieu_de                 VARCHAR(255) NOT NULL,
    noi_dung                TEXT NULL,
    muc_uu_tien             VARCHAR(20) NOT NULL DEFAULT 'BINH_THUONG',
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'MOI',
    tao_luc                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    xu_ly_luc               DATETIME NULL,
    hoan_tat_luc            DATETIME NULL,
    CONSTRAINT fk_ycht_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ycht_nlh
        FOREIGN KEY (nguoi_lien_he_id) REFERENCES nguoi_lien_he(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ycht_nd
        FOREIGN KEY (nguoi_xu_ly_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_ycht_kh_tt (khach_hang_id, trang_thai),
    INDEX idx_ycht_xl_tt (nguoi_xu_ly_id, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gia_tri_truong_tuy_chinh_khach_hang (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    khach_hang_id           BIGINT UNSIGNED NOT NULL,
    truong_tuy_chinh_id     BIGINT UNSIGNED NOT NULL,
    gia_tri_van_ban         TEXT NULL,
    gia_tri_so              DECIMAL(30,6) NULL,
    gia_tri_ngay            DATE NULL,
    gia_tri_json            JSON NULL,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_gttc_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_gttc_kh_ttc
        FOREIGN KEY (truong_tuy_chinh_id) REFERENCES truong_tuy_chinh(id)
        ON DELETE RESTRICT,
    UNIQUE KEY uk_gttc_kh (khach_hang_id, truong_tuy_chinh_id),
    INDEX idx_gttc_kh_truong (truong_tuy_chinh_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================================
-- 5. LEAD & CHIEN DICH - SPRINT 4
-- =====================================================================

CREATE TABLE chien_dich (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_chien_dich           VARCHAR(60) NOT NULL UNIQUE,
    ten_chien_dich          VARCHAR(200) NOT NULL,
    ngan_sach               DECIMAL(18,2) NULL,
    kenh                    VARCHAR(100) NULL,
    ngay_bat_dau            DATE NULL,
    ngay_ket_thuc           DATE NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'NHAP',
    mo_ta                   TEXT NULL,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cd_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_cd_time (ngay_bat_dau, ngay_ket_thuc),
    INDEX idx_cd_tt (trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bieu_mau_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_bieu_mau             VARCHAR(60) NOT NULL UNIQUE,
    ten_bieu_mau            VARCHAR(200) NOT NULL,
    token_cong_khai         VARCHAR(128) NOT NULL UNIQUE,
    nguon_lead_id           BIGINT UNSIGNED NOT NULL,
    chien_dich_id           BIGINT UNSIGNED NULL,
    cau_hinh_truong_json    JSON NULL,
    gioi_han_gui_moi_gio    INT UNSIGNED NULL,
    bat_honeypot            TINYINT(1) NOT NULL DEFAULT 1,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bml_nguon
        FOREIGN KEY (nguon_lead_id) REFERENCES nguon_lead(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_bml_cd
        FOREIGN KEY (chien_dich_id) REFERENCES chien_dich(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bml_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_bml_hd (hoat_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nhat_ky_gui_form_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    bieu_mau_id             BIGINT UNSIGNED NOT NULL,
    dia_chi_ip              VARCHAR(45) NOT NULL,
    thanh_cong              TINYINT(1) NOT NULL DEFAULT 0,
    ly_do_tu_choi           VARCHAR(255) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nkgfl_bm
        FOREIGN KEY (bieu_mau_id) REFERENCES bieu_mau_lead(id)
        ON DELETE CASCADE,
    INDEX idx_nkgfl_rate (bieu_mau_id, dia_chi_ip, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lead` (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_lead                 VARCHAR(50) NOT NULL UNIQUE,
    ho_ten                  VARCHAR(150) NOT NULL,
    email                   VARCHAR(255) NULL,
    email_chuan_hoa         VARCHAR(255) NULL,
    so_dien_thoai           VARCHAR(20) NULL,
    so_dien_thoai_chuan_hoa VARCHAR(20) NULL,
    ten_cong_ty             VARCHAR(255) NULL,
    ten_cong_ty_chuan_hoa   VARCHAR(255) NULL,
    nhu_cau_quan_tam        TEXT NULL,
    muc_do_quan_tam         VARCHAR(50) NULL,
    nganh_nghe_id           BIGINT UNSIGNED NULL,
    quy_mo_id               BIGINT UNSIGNED NULL,
    khu_vuc_id              BIGINT UNSIGNED NULL,
    nguon_lead_id           BIGINT UNSIGNED NOT NULL,
    chien_dich_id           BIGINT UNSIGNED NULL,
    bieu_mau_id             BIGINT UNSIGNED NULL,
    diem                    INT NOT NULL DEFAULT 0,
    phan_loai               VARCHAR(30) NULL,
    trang_thai              VARCHAR(40) NOT NULL DEFAULT 'MOI',
    nguoi_phu_trach_id      BIGINT UNSIGNED NULL,
    nhom_phan_bo_id         BIGINT UNSIGNED NULL,
    duoc_phan_bo_luc        DATETIME NULL,
    han_phan_hoi_sla        DATETIME NULL,
    duoc_nhan_luc           DATETIME NULL,
    lien_he_dau_luc         DATETIME NULL,
    ly_do_tu_choi           VARCHAR(500) NULL,
    qua_sla                 TINYINT(1) NOT NULL DEFAULT 0,
    khach_hang_chuyen_doi_id BIGINT UNSIGNED NULL,
    nguoi_lien_he_chuyen_doi_id BIGINT UNSIGNED NULL,
    co_hoi_chuyen_doi_id    BIGINT UNSIGNED NULL,
    chuyen_doi_luc          DATETIME NULL,
    gop_vao_lead_id         BIGINT UNSIGNED NULL,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lead_nganh
        FOREIGN KEY (nganh_nghe_id) REFERENCES nganh_nghe(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_qm
        FOREIGN KEY (quy_mo_id) REFERENCES quy_mo_doanh_nghiep(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_kv
        FOREIGN KEY (khu_vuc_id) REFERENCES khu_vuc_dia_ly(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_nguon
        FOREIGN KEY (nguon_lead_id) REFERENCES nguon_lead(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lead_cd
        FOREIGN KEY (chien_dich_id) REFERENCES chien_dich(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_bm
        FOREIGN KEY (bieu_mau_id) REFERENCES bieu_mau_lead(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_pt
        FOREIGN KEY (nguoi_phu_trach_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_nhom
        FOREIGN KEY (nhom_phan_bo_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_khcd
        FOREIGN KEY (khach_hang_chuyen_doi_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_nlhcd
        FOREIGN KEY (nguoi_lien_he_chuyen_doi_id) REFERENCES nguoi_lien_he(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_gop
        FOREIGN KEY (gop_vao_lead_id) REFERENCES `lead`(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lead_tao
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_lead_tt_pt (trang_thai, nguoi_phu_trach_id),
    INDEX idx_lead_nguon (nguon_lead_id, created_at),
    INDEX idx_lead_cd (chien_dich_id, created_at),
    INDEX idx_lead_diem (phan_loai, diem),
    INDEX idx_lead_sla (qua_sla, han_phan_hoi_sla),
    INDEX idx_lead_email (email_chuan_hoa),
    INDEX idx_lead_sdt (so_dien_thoai_chuan_hoa),
    INDEX idx_lead_cty (ten_cong_ty_chuan_hoa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quy_tac_cham_diem_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_quy_tac              VARCHAR(60) NOT NULL UNIQUE,
    ten_quy_tac             VARCHAR(200) NOT NULL,
    truong_du_lieu          VARCHAR(80) NOT NULL,
    toan_tu                 VARCHAR(30) NOT NULL,
    gia_tri_so_sanh_json    JSON NOT NULL,
    so_diem                 INT NOT NULL,
    thu_tu_uu_tien          INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_qtcd_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_qtcd_hd (hoat_dong, thu_tu_uu_tien)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nguong_phan_loai_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_phan_loai            VARCHAR(30) NOT NULL UNIQUE,
    ten_phan_loai           VARCHAR(100) NOT NULL,
    diem_tu                 INT NULL,
    diem_den                INT NULL,
    thu_tu                  INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quy_tac_phan_bo_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_quy_tac              VARCHAR(60) NOT NULL UNIQUE,
    ten_quy_tac             VARCHAR(200) NOT NULL,
    thu_tu_uu_tien          INT NOT NULL,
    kieu_phan_bo            VARCHAR(30) NOT NULL,
    dieu_kien_json          JSON NULL,
    nhom_dich_id            BIGINT UNSIGNED NULL,
    nguoi_dung_dich_id      BIGINT UNSIGNED NULL,
    nguoi_dung_lan_cuoi_id  BIGINT UNSIGNED NULL,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_qtpb_nhom
        FOREIGN KEY (nhom_dich_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_qtpb_nd
        FOREIGN KEY (nguoi_dung_dich_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_qtpb_last
        FOREIGN KEY (nguoi_dung_lan_cuoi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_qtpb_tao
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_qtpb_tt (thu_tu_uu_tien),
    INDEX idx_qtpb_hd (hoat_dong, thu_tu_uu_tien)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lich_su_phan_bo_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    lead_id                 BIGINT UNSIGNED NOT NULL,
    quy_tac_id              BIGINT UNSIGNED NULL,
    nguoi_dung_cu_id        BIGINT UNSIGNED NULL,
    nguoi_dung_moi_id       BIGINT UNSIGNED NULL,
    nhom_id                 BIGINT UNSIGNED NULL,
    hanh_dong               VARCHAR(30) NOT NULL,
    ly_do                   VARCHAR(500) NULL,
    thuc_hien_boi_id        BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lspb_lead
        FOREIGN KEY (lead_id) REFERENCES `lead`(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_lspb_qt
        FOREIGN KEY (quy_tac_id) REFERENCES quy_tac_phan_bo_lead(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lspb_cu
        FOREIGN KEY (nguoi_dung_cu_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lspb_moi
        FOREIGN KEY (nguoi_dung_moi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lspb_nhom
        FOREIGN KEY (nhom_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_lspb_th
        FOREIGN KEY (thuc_hien_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_lspb_lead (lead_id, created_at),
    INDEX idx_lspb_nd (nguoi_dung_moi_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lich_su_gop_lead (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    lead_nguon_id           BIGINT UNSIGNED NOT NULL,
    lead_dich_id            BIGINT UNSIGNED NOT NULL,
    thuc_hien_boi_id        BIGINT UNSIGNED NOT NULL,
    du_lieu_gop_json        JSON NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lsgl_nguon
        FOREIGN KEY (lead_nguon_id) REFERENCES `lead`(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lsgl_dich
        FOREIGN KEY (lead_dich_id) REFERENCES `lead`(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lsgl_nd
        FOREIGN KEY (thuc_hien_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    INDEX idx_lsgl_src (lead_nguon_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 6. CO HOI & PIPELINE - SPRINT 5-6
-- =====================================================================

CREATE TABLE co_hoi (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_co_hoi               VARCHAR(50) NOT NULL UNIQUE,
    ten_co_hoi              VARCHAR(255) NOT NULL,
    khach_hang_id           BIGINT UNSIGNED NULL,
    nguoi_lien_he_chinh_id  BIGINT UNSIGNED NULL,
    lead_id                 BIGINT UNSIGNED NULL,
    chien_dich_id           BIGINT UNSIGNED NULL,
    nguon_lead_id           BIGINT UNSIGNED NULL,
    giai_doan_id            BIGINT UNSIGNED NULL,
    nguoi_phu_trach_id      BIGINT UNSIGNED NOT NULL,
    nhom_kinh_doanh_id      BIGINT UNSIGNED NULL,
    gia_tri_du_kien         DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    xac_suat                INT UNSIGNED NOT NULL DEFAULT 0,
    ly_do_sua_xac_suat      VARCHAR(500) NULL,
    ngay_chot_du_kien       DATE NULL,
    trang_thai              VARCHAR(50) NOT NULL DEFAULT 'MO',
    gia_tri_chot_thuc_te    DECIMAL(18,2) NULL,
    ngay_ky                 DATE NULL,
    ly_do_thang_thua_id     BIGINT UNSIGNED NULL,
    doi_thu_id              BIGINT UNSIGNED NULL,
    ngay_hoat_dong_cuoi     DATETIME NULL,
    bi_dinh_tre             TINYINT(1) NOT NULL DEFAULT 0,
    dinh_tre_tu_luc         DATETIME NULL,
    so_lan_mo_lai           INT UNSIGNED NOT NULL DEFAULT 0,
    mo_lai_lan_cuoi         DATETIME NULL,
    ly_do_mo_lai            VARCHAR(500) NULL,
    mo_ta_chi_tiet          TEXT NULL,
    ngay_tao                DATE NOT NULL DEFAULT (CURRENT_DATE),
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ch_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_nlh
        FOREIGN KEY (nguoi_lien_he_chinh_id) REFERENCES nguoi_lien_he(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_lead
        FOREIGN KEY (lead_id) REFERENCES `lead`(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_cd
        FOREIGN KEY (chien_dich_id) REFERENCES chien_dich(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_nguon
        FOREIGN KEY (nguon_lead_id) REFERENCES nguon_lead(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_gd
        FOREIGN KEY (giai_doan_id) REFERENCES giai_doan_pipeline(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_nguoi_phu_trach
        FOREIGN KEY (nguoi_phu_trach_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ch_nhom
        FOREIGN KEY (nhom_kinh_doanh_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_ld
        FOREIGN KEY (ly_do_thang_thua_id) REFERENCES ly_do_thang_thua(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_dt
        FOREIGN KEY (doi_thu_id) REFERENCES doi_thu(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ch_tao
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_ch_nguoi_phu_trach (nguoi_phu_trach_id),
    INDEX idx_ch_nhom (nhom_kinh_doanh_id),
    INDEX idx_ch_khach_hang (khach_hang_id),
    INDEX idx_ch_trang_thai (trang_thai),
    INDEX idx_ch_gd (giai_doan_id, trang_thai),
    INDEX idx_ch_chot (ngay_chot_du_kien, trang_thai),
    INDEX idx_ch_dinh_tre (bi_dinh_tre, trang_thai),
    INDEX idx_ch_sp_report (nguon_lead_id, chien_dich_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `lead`
    ADD CONSTRAINT fk_lead_chcd
        FOREIGN KEY (co_hoi_chuyen_doi_id) REFERENCES co_hoi(id)
        ON DELETE SET NULL;

CREATE TABLE co_hoi_san_pham (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    co_hoi_id               BIGINT UNSIGNED NOT NULL,
    san_pham_id             BIGINT UNSIGNED NOT NULL,
    so_luong                DECIMAL(18,4) NOT NULL DEFAULT 1,
    don_gia                 DECIMAL(18,2) NOT NULL,
    so_ky                   INT UNSIGNED NULL,
    don_vi_ky               VARCHAR(30) NULL,
    gia_tri_hang_nam        DECIMAL(18,2) NULL,
    thanh_tien              DECIMAL(18,2) NOT NULL,
    ghi_chu                 VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chsp_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_chsp_sp
        FOREIGN KEY (san_pham_id) REFERENCES san_pham(id)
        ON DELETE RESTRICT,
    INDEX idx_chsp_ch (co_hoi_id),
    INDEX idx_chsp_sp (san_pham_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE co_hoi_thanh_vien (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    co_hoi_id               BIGINT UNSIGNED NOT NULL,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    quyen_tham_gia          VARCHAR(30) NOT NULL DEFAULT 'GHI_HOAT_DONG',
    duoc_them_boi_id        BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chtv_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_chtv_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_chtv_add
        FOREIGN KEY (duoc_them_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_chtv (co_hoi_id, nguoi_dung_id),
    INDEX idx_chtv_nd (nguoi_dung_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lich_su_giai_doan_co_hoi (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    co_hoi_id               BIGINT UNSIGNED NOT NULL,
    giai_doan_id            BIGINT UNSIGNED NOT NULL,
    bat_dau_luc             DATETIME NOT NULL,
    ket_thuc_luc            DATETIME NULL,
    chuyen_boi_id           BIGINT UNSIGNED NULL,
    ghi_de_dieu_kien        TINYINT(1) NOT NULL DEFAULT 0,
    ly_do_ghi_de            VARCHAR(500) NULL,
    CONSTRAINT fk_lsgd_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_lsgd_gd
        FOREIGN KEY (giai_doan_id) REFERENCES giai_doan_pipeline(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lsgd_nd
        FOREIGN KEY (chuyen_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_lsgd_ch_time (co_hoi_id, bat_dau_luc),
    INDEX idx_lsgd_gd_time (giai_doan_id, bat_dau_luc, ket_thuc_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lich_su_co_hoi (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    co_hoi_id               BIGINT UNSIGNED NOT NULL,
    loai_thay_doi           VARCHAR(50) NOT NULL,
    gia_tri_truoc_json      JSON NULL,
    gia_tri_sau_json        JSON NULL,
    nguoi_thuc_hien_id      BIGINT UNSIGNED NULL,
    ly_do                   VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lsch_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_lsch_nd
        FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_lsch_ch (co_hoi_id, created_at),
    INDEX idx_lsch_loai (loai_thay_doi, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gia_tri_truong_tuy_chinh_co_hoi (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    co_hoi_id               BIGINT UNSIGNED NOT NULL,
    truong_tuy_chinh_id     BIGINT UNSIGNED NOT NULL,
    gia_tri_van_ban         TEXT NULL,
    gia_tri_so              DECIMAL(30,6) NULL,
    gia_tri_ngay            DATE NULL,
    gia_tri_json            JSON NULL,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_gttc_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_gttc_ch_ttc
        FOREIGN KEY (truong_tuy_chinh_id) REFERENCES truong_tuy_chinh(id)
        ON DELETE RESTRICT,
    UNIQUE KEY uk_gttc_ch (co_hoi_id, truong_tuy_chinh_id),
    INDEX idx_gttc_ch_truong (truong_tuy_chinh_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 7. HOAT DONG, CONG VIEC, LICH - SPRINT 6
-- =====================================================================

CREATE TABLE hoat_dong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_hoat_dong            VARCHAR(50) NOT NULL UNIQUE,
    tieu_de                 VARCHAR(255) NOT NULL,
    loai_hoat_dong          VARCHAR(50) NOT NULL DEFAULT 'CUOC_GOI',
    lead_id                 BIGINT UNSIGNED NULL,
    khach_hang_id           BIGINT UNSIGNED NULL,
    nguoi_lien_he_id        BIGINT UNSIGNED NULL,
    co_hoi_id               BIGINT UNSIGNED NULL,
    nguoi_phu_trach_id      BIGINT UNSIGNED NOT NULL,
    nhom_kinh_doanh_id      BIGINT UNSIGNED NULL,
    chi_phi                 DECIMAL(18,2) NULL DEFAULT 0.00,
    thoi_gian_bat_dau       DATETIME NULL,
    thoi_gian_ket_thuc      DATETIME NULL,
    trang_thai              VARCHAR(50) NOT NULL DEFAULT 'HOAN_THANH',
    mo_ta_chi_tiet          TEXT NULL,
    noi_dung                TEXT NULL,
    ket_qua                 TEXT NULL,
    thoi_luong_phut         INT UNSIGNED NULL,
    la_ghi_nhan_qua_khu     TINYINT(1) NOT NULL DEFAULT 0,
    la_ghi_nhan_nhanh       TINYINT(1) NOT NULL DEFAULT 0,
    ngay_tao                DATE NOT NULL DEFAULT (CURRENT_DATE),
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hd_lead
        FOREIGN KEY (lead_id) REFERENCES `lead`(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_hd_khach_hang
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_hd_nlh
        FOREIGN KEY (nguoi_lien_he_id) REFERENCES nguoi_lien_he(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_hd_co_hoi
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_hd_nguoi_phu_trach
        FOREIGN KEY (nguoi_phu_trach_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_hd_nhom
        FOREIGN KEY (nhom_kinh_doanh_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    INDEX idx_hd_nguoi_phu_trach (nguoi_phu_trach_id),
    INDEX idx_hd_nhom (nhom_kinh_doanh_id),
    INDEX idx_hd_khach_hang (khach_hang_id),
    INDEX idx_hd_co_hoi (co_hoi_id),
    INDEX idx_hd_tieu_de (tieu_de),
    INDEX idx_hd_lead_time (lead_id, thoi_gian_bat_dau),
    INDEX idx_hd_time (thoi_gian_bat_dau)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cong_viec (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tieu_de                 VARCHAR(255) NOT NULL,
    mo_ta                   TEXT NULL,
    muc_uu_tien             VARCHAR(20) NOT NULL DEFAULT 'BINH_THUONG',
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'CHUA_LAM',
    han_hoan_thanh          DATETIME NOT NULL,
    nguoi_thuc_hien_id      BIGINT UNSIGNED NOT NULL,
    giao_boi_id             BIGINT UNSIGNED NULL,
    lead_id                 BIGINT UNSIGNED NULL,
    khach_hang_id           BIGINT UNSIGNED NULL,
    co_hoi_id               BIGINT UNSIGNED NULL,
    hoan_thanh_luc          DATETIME NULL,
    hoan_den_luc            DATETIME NULL,
    ly_do_hoan              VARCHAR(500) NULL,
    nhac_truoc_phut         INT UNSIGNED NULL,
    da_gui_nhac             TINYINT(1) NOT NULL DEFAULT 0,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cv_nd
        FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_cv_giao
        FOREIGN KEY (giao_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_cv_lead
        FOREIGN KEY (lead_id) REFERENCES `lead`(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_cv_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_cv_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE SET NULL,
    INDEX idx_cv_nd_han (nguoi_thuc_hien_id, trang_thai, han_hoan_thanh),
    INDEX idx_cv_kh (khach_hang_id, trang_thai),
    INDEX idx_cv_ch (co_hoi_id, trang_thai),
    INDEX idx_cv_han (trang_thai, han_hoan_thanh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 8. BAO GIA, DUYET, HOP DONG - SPRINT 7
-- =====================================================================

CREATE TABLE bao_gia (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_bao_gia              VARCHAR(60) NOT NULL UNIQUE,
    tieu_de                 VARCHAR(255) NOT NULL,
    co_hoi_id               BIGINT UNSIGNED NULL,
    khach_hang_id           BIGINT UNSIGNED NULL,
    nguoi_lien_he_id        BIGINT UNSIGNED NULL,
    nguoi_phu_trach_id      BIGINT UNSIGNED NOT NULL,
    nhom_kinh_doanh_id      BIGINT UNSIGNED NULL,
    phien_ban               INT UNSIGNED NOT NULL DEFAULT 1,
    tong_tien               DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    trang_thai              VARCHAR(50) NOT NULL DEFAULT 'CHO_DUYET',
    ngay_het_hieu_luc       DATE NULL,
    dieu_khoan_thanh_toan   TEXT NULL,
    dieu_khoan_giao_hang    TEXT NULL,
    tong_truoc_chiet_khau   DECIMAL(18,2) NOT NULL DEFAULT 0,
    tong_chiet_khau         DECIMAL(18,2) NOT NULL DEFAULT 0,
    tong_sau_chiet_khau     DECIMAL(18,2) NOT NULL DEFAULT 0,
    ty_le_chiet_khau_tong   DECIMAL(7,4) NOT NULL DEFAULT 0,
    cap_duyet_can_thiet     VARCHAR(30) NULL,
    gui_khach_luc           DATETIME NULL,
    het_hieu_luc_luc        DATETIME NULL,
    mo_ta_chi_tiet          TEXT NULL,
    ngay_tao                DATE NOT NULL DEFAULT (CURRENT_DATE),
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bg_co_hoi
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bg_khach_hang
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bg_nlh
        FOREIGN KEY (nguoi_lien_he_id) REFERENCES nguoi_lien_he(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bg_nguoi_phu_trach
        FOREIGN KEY (nguoi_phu_trach_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_bg_nhom
        FOREIGN KEY (nhom_kinh_doanh_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bg_tao
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_bg_nguoi_phu_trach (nguoi_phu_trach_id),
    INDEX idx_bg_nhom (nhom_kinh_doanh_id),
    INDEX idx_bg_co_hoi (co_hoi_id),
    INDEX idx_bg_khach_hang (khach_hang_id),
    INDEX idx_bg_trang_thai (trang_thai),
    INDEX idx_bg_hhl (ngay_het_hieu_luc, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bao_gia_phien_ban (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    bao_gia_id              BIGINT UNSIGNED NOT NULL,
    so_phien_ban            INT UNSIGNED NOT NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'NHAP',
    ngay_het_hieu_luc       DATE NULL,
    dieu_khoan_thanh_toan   TEXT NULL,
    dieu_khoan_giao_hang    TEXT NULL,
    tong_truoc_chiet_khau   DECIMAL(18,2) NOT NULL DEFAULT 0,
    tong_chiet_khau         DECIMAL(18,2) NOT NULL DEFAULT 0,
    tong_sau_chiet_khau     DECIMAL(18,2) NOT NULL DEFAULT 0,
    ty_le_chiet_khau_tong   DECIMAL(7,4) NOT NULL DEFAULT 0,
    snapshot_json           JSON NULL,
    tao_boi_id              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bgp_bg
        FOREIGN KEY (bao_gia_id) REFERENCES bao_gia(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bgp_nd
        FOREIGN KEY (tao_boi_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_bgp_ver (bao_gia_id, so_phien_ban),
    INDEX idx_bgp_bg_tt (bao_gia_id, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bao_gia_chi_tiet (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phien_ban_id            BIGINT UNSIGNED NOT NULL,
    san_pham_id             BIGINT UNSIGNED NULL,
    ma_san_pham_snapshot    VARCHAR(60) NULL,
    ten_san_pham_snapshot   VARCHAR(200) NOT NULL,
    don_vi_tinh_snapshot    VARCHAR(50) NULL,
    so_luong                DECIMAL(18,4) NOT NULL DEFAULT 1,
    don_gia_niem_yet        DECIMAL(18,2) NULL,
    gia_san_snapshot        DECIMAL(18,2) NULL,
    gia_von_snapshot        DECIMAL(18,2) NULL,
    don_gia                 DECIMAL(18,2) NOT NULL,
    kieu_chiet_khau         VARCHAR(20) NULL,
    gia_tri_chiet_khau      DECIMAL(18,4) NOT NULL DEFAULT 0,
    tien_chiet_khau         DECIMAL(18,2) NOT NULL DEFAULT 0,
    thanh_tien              DECIMAL(18,2) NOT NULL,
    so_ky                   INT UNSIGNED NULL,
    don_vi_ky               VARCHAR(30) NULL,
    ghi_chu                 VARCHAR(500) NULL,
    CONSTRAINT fk_bgct_pb
        FOREIGN KEY (phien_ban_id) REFERENCES bao_gia_phien_ban(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bgct_sp
        FOREIGN KEY (san_pham_id) REFERENCES san_pham(id)
        ON DELETE RESTRICT,
    INDEX idx_bgct_pb (phien_ban_id),
    INDEX idx_bgct_sp (san_pham_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE duyet_bao_gia (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phien_ban_id            BIGINT UNSIGNED NOT NULL,
    cap_duyet               VARCHAR(30) NOT NULL,
    nguoi_duyet_id          BIGINT UNSIGNED NULL,
    nguon_duyet             VARCHAR(20) NOT NULL DEFAULT 'NGUOI_DUNG',
    hanh_dong               VARCHAR(30) NOT NULL,
    y_kien                  VARCHAR(1000) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dbg_pb
        FOREIGN KEY (phien_ban_id) REFERENCES bao_gia_phien_ban(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_dbg_nd
        FOREIGN KEY (nguoi_duyet_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_dbg_pb (phien_ban_id, created_at),
    INDEX idx_dbg_nd (nguoi_duyet_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lien_ket_bao_gia (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phien_ban_id            BIGINT UNSIGNED NOT NULL,
    token_hash              CHAR(64) NOT NULL,
    het_han_luc             DATETIME NULL,
    thu_hoi_luc             DATETIME NULL,
    so_lan_xem              INT UNSIGNED NOT NULL DEFAULT 0,
    xem_lan_dau_luc         DATETIME NULL,
    xem_lan_cuoi_luc        DATETIME NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lkbg_pb
        FOREIGN KEY (phien_ban_id) REFERENCES bao_gia_phien_ban(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_lkbg_token (token_hash),
    INDEX idx_lkbg_hh (het_han_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE phan_hoi_bao_gia (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    lien_ket_id             BIGINT UNSIGNED NOT NULL,
    phien_ban_id            BIGINT UNSIGNED NOT NULL,
    phan_hoi                VARCHAR(30) NOT NULL,
    y_kien_khach            TEXT NULL,
    dia_chi_ip              VARCHAR(45) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_phbg_lk
        FOREIGN KEY (lien_ket_id) REFERENCES lien_ket_bao_gia(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_phbg_pb
        FOREIGN KEY (phien_ban_id) REFERENCES bao_gia_phien_ban(id)
        ON DELETE RESTRICT,
    INDEX idx_phbg_pb (phien_ban_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hop_dong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    so_hop_dong             VARCHAR(80) NOT NULL UNIQUE,
    bao_gia_id              BIGINT UNSIGNED NOT NULL,
    phien_ban_bao_gia_id    BIGINT UNSIGNED NOT NULL,
    co_hoi_id               BIGINT UNSIGNED NOT NULL,
    khach_hang_id           BIGINT UNSIGNED NOT NULL,
    ngay_ky                 DATE NOT NULL,
    ngay_hieu_luc           DATE NOT NULL,
    ngay_het_han            DATE NULL,
    gia_tri_hop_dong        DECIMAL(18,2) NOT NULL,
    dieu_khoan_thanh_toan   TEXT NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'DA_KY',
    hop_dong_goc_id         BIGINT UNSIGNED NULL,
    co_hoi_gia_han_id       BIGINT UNSIGNED NULL,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hd_bg
        FOREIGN KEY (bao_gia_id) REFERENCES bao_gia(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_hd_pb
        FOREIGN KEY (phien_ban_bao_gia_id) REFERENCES bao_gia_phien_ban(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_hopdong_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_hopdong_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_hd_goc
        FOREIGN KEY (hop_dong_goc_id) REFERENCES hop_dong(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_hd_chgh
        FOREIGN KEY (co_hoi_gia_han_id) REFERENCES co_hoi(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_hd_tao
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_hd_bg (bao_gia_id),
    INDEX idx_hd_kh_tt (khach_hang_id, trang_thai),
    INDEX idx_hd_hh (ngay_het_han, trang_thai),
    INDEX idx_hd_ch (co_hoi_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hop_dong_chi_tiet (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    hop_dong_id             BIGINT UNSIGNED NOT NULL,
    san_pham_id             BIGINT UNSIGNED NULL,
    ma_san_pham_snapshot    VARCHAR(60) NULL,
    ten_san_pham_snapshot   VARCHAR(200) NOT NULL,
    don_vi_tinh_snapshot    VARCHAR(50) NULL,
    so_luong                DECIMAL(18,4) NOT NULL DEFAULT 1,
    don_gia                 DECIMAL(18,2) NOT NULL,
    chiet_khau              DECIMAL(18,2) NOT NULL DEFAULT 0,
    thanh_tien              DECIMAL(18,2) NOT NULL,
    so_ky                   INT UNSIGNED NULL,
    don_vi_ky               VARCHAR(30) NULL,
    CONSTRAINT fk_hdct_hd
        FOREIGN KEY (hop_dong_id) REFERENCES hop_dong(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_hdct_sp
        FOREIGN KEY (san_pham_id) REFERENCES san_pham(id)
        ON DELETE RESTRICT,
    INDEX idx_hdct_hd (hop_dong_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 9. KPI, THONG BAO, EMAIL, AUTOMATION - SPRINT 7-8
-- =====================================================================

CREATE TABLE chi_tieu (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NULL,
    nhom_kinh_doanh_id      BIGINT UNSIGNED NULL,
    loai_ky                 VARCHAR(20) NOT NULL,
    nam                     INT NOT NULL,
    so_ky                   INT NOT NULL,
    tu_ngay                 DATE NOT NULL,
    den_ngay                DATE NOT NULL,
    gia_tri_muc_tieu        DECIMAL(18,2) NOT NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'DANG_AP_DUNG',
    khoa_luc                DATETIME NULL,
    ly_do_dieu_chinh        VARCHAR(1000) NULL,
    created_by              BIGINT UNSIGNED NOT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ct_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ct_nhom
        FOREIGN KEY (nhom_kinh_doanh_id) REFERENCES nhom_kinh_doanh(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ct_tao
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_ct_doi_tuong
        CHECK (
            (nguoi_dung_id IS NOT NULL AND nhom_kinh_doanh_id IS NULL)
            OR
            (nguoi_dung_id IS NULL AND nhom_kinh_doanh_id IS NOT NULL)
        ),
    CONSTRAINT ck_ct_ngay CHECK (tu_ngay <= den_ngay),
    UNIQUE KEY uk_ct_nd (nguoi_dung_id, loai_ky, nam, so_ky),
    UNIQUE KEY uk_ct_nhom (nhom_kinh_doanh_id, loai_ky, nam, so_ky),
    INDEX idx_ct_time (tu_ngay, den_ngay),
    INDEX idx_ct_tt (trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loai_thong_bao (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_loai                 VARCHAR(80) NOT NULL UNIQUE,
    ten_loai                VARCHAR(200) NOT NULL,
    bat_buoc                TINYINT(1) NOT NULL DEFAULT 0,
    cho_phep_in_app         TINYINT(1) NOT NULL DEFAULT 1,
    cho_phep_email          TINYINT(1) NOT NULL DEFAULT 1,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE thong_bao (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    loai_thong_bao_id       BIGINT UNSIGNED NOT NULL,
    tieu_de                 VARCHAR(255) NOT NULL,
    noi_dung                TEXT NULL,
    loai_doi_tuong          VARCHAR(50) NULL,
    doi_tuong_id            BIGINT UNSIGNED NULL,
    duong_dan_mo            VARCHAR(500) NULL,
    da_doc                  TINYINT(1) NOT NULL DEFAULT 0,
    doc_luc                 DATETIME NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tb_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tb_loai
        FOREIGN KEY (loai_thong_bao_id) REFERENCES loai_thong_bao(id)
        ON DELETE RESTRICT,
    INDEX idx_tb_nd_doc (nguoi_dung_id, da_doc, created_at),
    INDEX idx_tb_dt (loai_doi_tuong, doi_tuong_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cau_hinh_thong_bao_nguoi_dung (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id           BIGINT UNSIGNED NOT NULL,
    loai_thong_bao_id       BIGINT UNSIGNED NOT NULL,
    bat_in_app              TINYINT(1) NOT NULL DEFAULT 1,
    bat_email               TINYINT(1) NOT NULL DEFAULT 1,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chtb_nd
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_chtb_loai
        FOREIGN KEY (loai_thong_bao_id) REFERENCES loai_thong_bao(id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_chtb (nguoi_dung_id, loai_thong_bao_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mau_email (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_mau                  VARCHAR(80) NOT NULL UNIQUE,
    ten_mau                 VARCHAR(200) NOT NULL,
    tieu_de_mau             VARCHAR(500) NOT NULL,
    noi_dung_html           MEDIUMTEXT NOT NULL,
    danh_sach_bien_json     JSON NULL,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_me_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_gui (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    mau_email_id            BIGINT UNSIGNED NULL,
    nguoi_gui_id            BIGINT UNSIGNED NULL,
    email_nguoi_gui         VARCHAR(255) NULL,
    email_nguoi_nhan        VARCHAR(255) NOT NULL,
    ten_nguoi_nhan          VARCHAR(200) NULL,
    tieu_de                 VARCHAR(500) NOT NULL,
    khach_hang_id           BIGINT UNSIGNED NULL,
    nguoi_lien_he_id        BIGINT UNSIGNED NULL,
    co_hoi_id               BIGINT UNSIGNED NULL,
    bao_gia_id              BIGINT UNSIGNED NULL,
    hop_dong_id             BIGINT UNSIGNED NULL,
    hoat_dong_id            BIGINT UNSIGNED NULL,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'CHO_GUI',
    so_lan_thu              INT UNSIGNED NOT NULL DEFAULT 0,
    gui_lai_luc             DATETIME NULL,
    gui_thanh_cong_luc      DATETIME NULL,
    loi_rut_gon             VARCHAR(1000) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eg_me
        FOREIGN KEY (mau_email_id) REFERENCES mau_email(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_nd
        FOREIGN KEY (nguoi_gui_id) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_nlh
        FOREIGN KEY (nguoi_lien_he_id) REFERENCES nguoi_lien_he(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_bg
        FOREIGN KEY (bao_gia_id) REFERENCES bao_gia(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_hd
        FOREIGN KEY (hop_dong_id) REFERENCES hop_dong(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_eg_hoatdong
        FOREIGN KEY (hoat_dong_id) REFERENCES hoat_dong(id)
        ON DELETE SET NULL,
    INDEX idx_eg_tt_retry (trang_thai, gui_lai_luc),
    INDEX idx_eg_kh (khach_hang_id, created_at),
    INDEX idx_eg_bg (bao_gia_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quy_tac_tu_dong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ma_quy_tac              VARCHAR(80) NOT NULL UNIQUE,
    ten_quy_tac             VARCHAR(255) NOT NULL,
    loai_kich_hoat          VARCHAR(50) NOT NULL,
    dieu_kien_json          JSON NULL,
    hanh_dong_json          JSON NOT NULL,
    thu_tu_uu_tien          INT NOT NULL DEFAULT 0,
    hoat_dong               TINYINT(1) NOT NULL DEFAULT 1,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_qttd_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_qttd_hd (hoat_dong, loai_kich_hoat, thu_tu_uu_tien)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nhat_ky_quy_tac_tu_dong (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    quy_tac_id              BIGINT UNSIGNED NOT NULL,
    loai_doi_tuong          VARCHAR(50) NULL,
    doi_tuong_id            BIGINT UNSIGNED NULL,
    dau_vao_json            JSON NULL,
    ket_qua_json            JSON NULL,
    trang_thai              VARCHAR(30) NOT NULL,
    loi_rut_gon             VARCHAR(1000) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nkqttd_qt
        FOREIGN KEY (quy_tac_id) REFERENCES quy_tac_tu_dong(id)
        ON DELETE CASCADE,
    INDEX idx_nkqttd_qt (quy_tac_id, created_at),
    INDEX idx_nkqttd_tt (trang_thai, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tac_vu_nen (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    loai_tac_vu             VARCHAR(80) NOT NULL,
    payload_json            JSON NULL,
    payload_ma_hoa          MEDIUMBLOB NULL,
    chua_du_lieu_nhay_cam   TINYINT(1) NOT NULL DEFAULT 0,
    trang_thai              VARCHAR(30) NOT NULL DEFAULT 'CHO_XU_LY',
    do_uu_tien              INT NOT NULL DEFAULT 0,
    so_lan_thu              INT UNSIGNED NOT NULL DEFAULT 0,
    so_lan_thu_toi_da       INT UNSIGNED NOT NULL DEFAULT 3,
    chay_som_nhat_luc       DATETIME NULL,
    khoa_boi                VARCHAR(100) NULL,
    khoa_luc                DATETIME NULL,
    loi_rut_gon             VARCHAR(1000) NULL,
    created_by              BIGINT UNSIGNED NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tvn_nd
        FOREIGN KEY (created_by) REFERENCES nguoi_dung(id)
        ON DELETE SET NULL,
    INDEX idx_tvn_queue (trang_thai, chay_som_nhat_luc, do_uu_tien)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 10. FILE METADATA & BACKUP
-- =====================================================================

CREATE TABLE tep_dinh_kem (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    khach_hang_id           BIGINT UNSIGNED NULL,
    co_hoi_id               BIGINT UNSIGNED NULL,
    hoat_dong_id            BIGINT UNSIGNED NULL,
    bao_gia_id              BIGINT UNSIGNED NULL,
    hop_dong_id             BIGINT UNSIGNED NULL,
    loai_tep                VARCHAR(50) NULL,
    ten_file_goc            VARCHAR(255) NOT NULL,
    ten_file_luu            VARCHAR(255) NOT NULL,
    duong_dan               VARCHAR(1000) NOT NULL,
    mime_type               VARCHAR(150) NULL,
    kich_thuoc_byte         BIGINT UNSIGNED NOT NULL,
    nguoi_tai_len_id        BIGINT UNSIGNED NOT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tdk_kh
        FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tdk_ch
        FOREIGN KEY (co_hoi_id) REFERENCES co_hoi(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tdk_hd
        FOREIGN KEY (hoat_dong_id) REFERENCES hoat_dong(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tdk_bg
        FOREIGN KEY (bao_gia_id) REFERENCES bao_gia(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tdk_hopdong
        FOREIGN KEY (hop_dong_id) REFERENCES hop_dong(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tdk_nd
        FOREIGN KEY (nguoi_tai_len_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT,
    INDEX idx_tdk_kh (khach_hang_id, created_at),
    INDEX idx_tdk_ch (co_hoi_id, created_at),
    INDEX idx_tdk_bg (bao_gia_id, created_at),
    INDEX idx_tdk_hopdong (hop_dong_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nhat_ky_sao_luu (
    id                      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ten_file                VARCHAR(255) NOT NULL,
    duong_dan               VARCHAR(1000) NOT NULL,
    kich_thuoc_byte         BIGINT UNSIGNED NULL,
    trang_thai              VARCHAR(30) NOT NULL,
    bat_dau_luc             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hoan_tat_luc            DATETIME NULL,
    da_kiem_tra_restore     TINYINT(1) NOT NULL DEFAULT 0,
    restore_kiem_tra_luc    DATETIME NULL,
    loi_rut_gon             VARCHAR(1000) NULL,
    INDEX idx_nksl_time (bat_dau_luc, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 11. SEED DU LIEU NEN - LOCAL/DEV
-- Script nay la bootstrap local/dev (co DROP DATABASE), vi vay co seed tai khoan mau
-- de cac may trong nhom co the chay/test cung mot bo du lieu khoi tao.
-- KHONG dung cac credential mau nay tren staging/production.
-- Mat khau mac dinh cua cac tai khoan mau: 123456@Aa
-- BCrypt rounds=12:
-- $2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e
-- =====================================================================

INSERT INTO vai_tro
    (id, ma_vai_tro, ten_vai_tro, mo_ta, pham_vi_toi_da)
VALUES
    (1, 'ADMIN', 'Quản trị hệ thống', 'Quản lý tài khoản, quyền, cấu hình và nhật ký.', 'TOAN_BO'),
    (2, 'DIRECTOR', 'Giám đốc kinh doanh', 'Xem và quản lý toàn bộ dữ liệu kinh doanh.', 'TOAN_BO'),
    (3, 'TEAM_LEAD', 'Trưởng nhóm kinh doanh', 'Theo dõi và quản lý dữ liệu của nhóm.', 'NHOM'),
    (4, 'SALES_REP', 'Nhân viên kinh doanh', 'Quản lý khách và cơ hội mình phụ trách.', 'CA_NHAN'),
    (5, 'MARKETING', 'Nhân viên Marketing', 'Quản lý chiến dịch và lead.', 'CA_NHAN'),
    (6, 'CUST_SUCCESS', 'Chăm sóc khách hàng', 'Theo dõi lịch sử sau bán và rủi ro khách hàng.', 'CA_NHAN'),
    (7, 'ACCOUNTANT', 'Kế toán', 'Theo dõi báo giá, hợp đồng và công nợ.', 'TOAN_BO');

INSERT INTO nhom_kinh_doanh
    (id, ma_nhom, ten_nhom, mo_ta, nhom_cha_id)
VALUES
    (1, 'KHOI_KD', 'Khối Kinh Doanh Tổng', 'Khối kinh doanh trực thuộc ban giám đốc', NULL),
    (2, 'KD_MIEN_BAC', 'Nhóm Kinh Doanh Miền Bắc', 'Phụ trách thị trường phía Bắc', 1),
    (3, 'KD_MIEN_NAM', 'Nhóm Kinh Doanh Miền Nam', 'Phụ trách thị trường phía Nam', 1),
    (4, 'PHONG_MKT', 'Phòng Marketing', 'Bộ phận truyền thông và thu hút khách hàng', NULL),
    (5, 'PHONG_CSKH', 'Phòng Chăm Sóc Khách Hàng', 'Bộ phận dịch vụ sau bán hàng', NULL),
    (6, 'PHONG_KT', 'Phòng Tài Chính Kế Toán', 'Bộ phận tài chính và hợp đồng', NULL);

INSERT INTO module_he_thong
    (id, ma_module, ten_module, thu_tu_hien_thi, hien_thi_menu)
VALUES
    (1, 'DANH_MUC', 'Danh mục & cấu hình bán hàng', 10, 1),
    (2, 'KHACH_HANG', 'Khách hàng & liên hệ', 20, 1),
    (3, 'LEAD', 'Lead & phân bổ', 30, 1),
    (4, 'CO_HOI', 'Cơ hội & pipeline', 40, 1),
    (5, 'HOAT_DONG', 'Hoạt động & lịch làm việc', 50, 1),
    (6, 'BAO_GIA_HOP_DONG', 'Báo giá & hợp đồng', 60, 1),
    (7, 'KPI', 'Chỉ tiêu & KPI', 70, 1),
    (8, 'BAO_CAO', 'Báo cáo & dashboard', 80, 1),
    (9, 'TU_DONG_HOA', 'Tự động hoá & thông báo', 90, 1),
    (10, 'NGUOI_DUNG_NHAT_KY', 'Người dùng & nhật ký', 100, 1);

-- muc_quyen: NONE / READ / WRITE / FULL
-- pham_vi_du_lieu: CA_NHAN / NHOM / TOAN_BO / NULL neu module khong loc ownership.
INSERT INTO vai_tro_module
    (vai_tro_id, module_id, muc_quyen, pham_vi_du_lieu)
VALUES
    -- Admin (id=1)
    (1,1,'FULL','TOAN_BO'),(1,2,'FULL','TOAN_BO'),(1,3,'FULL','TOAN_BO'),(1,4,'FULL','TOAN_BO'),
    (1,5,'FULL','TOAN_BO'),(1,6,'FULL','TOAN_BO'),(1,7,'FULL','TOAN_BO'),(1,8,'FULL','TOAN_BO'),
    (1,9,'FULL','TOAN_BO'),(1,10,'FULL','TOAN_BO'),
    -- Director (id=2)
    (2,1,'FULL','TOAN_BO'),(2,2,'FULL','TOAN_BO'),(2,3,'FULL','TOAN_BO'),(2,4,'FULL','TOAN_BO'),
    (2,5,'FULL','TOAN_BO'),(2,6,'FULL','TOAN_BO'),(2,7,'FULL','TOAN_BO'),(2,8,'FULL','TOAN_BO'),
    (2,9,'FULL','TOAN_BO'),(2,10,'READ','TOAN_BO'),
    -- Team Lead (id=3)
    (3,1,'READ',NULL),(3,2,'FULL','NHOM'),(3,3,'FULL','NHOM'),(3,4,'FULL','NHOM'),
    (3,5,'FULL','NHOM'),(3,6,'WRITE','NHOM'),(3,7,'WRITE','NHOM'),(3,8,'READ','NHOM'),
    (3,9,'READ','NHOM'),(3,10,'NONE',NULL),
    -- Sales Rep (id=4)
    (4,1,'READ',NULL),(4,2,'WRITE','CA_NHAN'),(4,3,'WRITE','CA_NHAN'),(4,4,'WRITE','CA_NHAN'),
    (4,5,'WRITE','CA_NHAN'),(4,6,'WRITE','CA_NHAN'),(4,7,'READ','CA_NHAN'),(4,8,'READ','CA_NHAN'),
    (4,9,'READ',NULL),(4,10,'NONE',NULL),
    -- Marketing (id=5)
    (5,1,'READ',NULL),(5,2,'WRITE','TOAN_BO'),(5,3,'FULL','TOAN_BO'),(5,4,'READ','TOAN_BO'),
    (5,5,'WRITE','TOAN_BO'),(5,6,'NONE',NULL),(5,7,'NONE',NULL),(5,8,'READ','CA_NHAN'),
    (5,9,'WRITE','TOAN_BO'),(5,10,'NONE',NULL),
    -- Customer Success (id=6)
    (6,1,'READ',NULL),(6,2,'WRITE','CA_NHAN'),(6,3,'NONE',NULL),(6,4,'READ','CA_NHAN'),
    (6,5,'WRITE','CA_NHAN'),(6,6,'READ','CA_NHAN'),(6,7,'NONE',NULL),(6,8,'READ','CA_NHAN'),
    (6,9,'READ',NULL),(6,10,'NONE',NULL),
    -- Accountant (id=7)
    (7,1,'READ',NULL),(7,2,'READ','TOAN_BO'),(7,3,'NONE',NULL),(7,4,'READ','TOAN_BO'),
    (7,5,'NONE',NULL),(7,6,'WRITE','TOAN_BO'),(7,7,'READ','TOAN_BO'),(7,8,'READ','TOAN_BO'),
    (7,9,'NONE',NULL),(7,10,'NONE',NULL);

INSERT INTO nguoi_dung
    (id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, so_lan_sai,
     thoi_gian_khoa, bat_buoc_doi_mat_khau, nhom_kinh_doanh_id)
VALUES
    (1, 'Nguyễn Quản Trị', 'admin@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0901234567', 'HOAT_DONG', 0, NULL, 0, NULL),
    (2, 'Trần Giám Đốc', 'director@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0902345678', 'HOAT_DONG', 0, NULL, 0, 1),
    (3, 'Lê Trưởng Nhóm', 'teamlead@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0903456789', 'HOAT_DONG', 0, NULL, 0, 2),
    (4, 'Nhân Viên Kinh Doanh Mẫu', 'sales@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0904567890', 'HOAT_DONG', 0, NULL, 0, 2),
    (5, 'Phạm Marketing', 'marketing@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0905678901', 'HOAT_DONG', 0, NULL, 0, 4),
    (6, 'Hoàng CSKH', 'cskh@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0906789012', 'HOAT_DONG', 0, NULL, 0, 5),
    (7, 'Đỗ Kế Toán', 'accountant@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0907890123', 'HOAT_DONG', 0, NULL, 0, 6),
    (8, 'Tài Khoản Bị Khóa', 'locked@crm.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     '0908888888', 'KHOA', 0, NULL, 0, 2),
    (9, 'Phan Duy Hưng', 'dtc245200134@ictu.edu.vn',
     '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e',
     NULL, 'HOAT_DONG', 0, NULL, 0, 2);

INSERT INTO nguoi_dung_vai_tro
    (nguoi_dung_id, vai_tro_id)
VALUES
    (1,1), -- admin@crm.vn -> ADMIN
    (2,2), -- director@crm.vn -> DIRECTOR
    (3,3), -- teamlead@crm.vn -> TEAM_LEAD
    (4,4), -- sales@crm.vn -> SALES_REP
    (5,5), -- marketing@crm.vn -> MARKETING
    (6,6), -- cskh@crm.vn -> CUST_SUCCESS
    (7,7), -- accountant@crm.vn -> ACCOUNTANT
    (8,4), -- locked@crm.vn -> SALES_REP
    (9,4); -- dtc245200134@ictu.edu.vn -> SALES_REP

UPDATE nhom_kinh_doanh
SET truong_nhom_id = 3
WHERE id = 2;

INSERT INTO loai_hoat_dong
    (id, ma_loai, ten_loai, thu_tu_hien_thi, hoat_dong)
VALUES
    (1, 'CUOC_GOI', 'Cuộc gọi', 10, 1),
    (2, 'CUOC_GAP', 'Cuộc gặp', 20, 1),
    (3, 'EMAIL', 'Email', 30, 1),
    (4, 'GHI_CHU', 'Ghi chú', 40, 1);

INSERT INTO loai_thong_bao
    (id, ma_loai, ten_loai, bat_buoc, cho_phep_in_app, cho_phep_email)
VALUES
    (1, 'LEAD_DUOC_PHAN', 'Được phân lead', 0, 1, 1),
    (2, 'BAO_GIA_CHO_DUYET', 'Báo giá chờ duyệt', 1, 1, 1),
    (3, 'KHACH_PHAN_HOI_BAO_GIA', 'Khách phản hồi báo giá', 0, 1, 1),
    (4, 'DUOC_GIAO_VIEC', 'Được giao việc', 0, 1, 1),
    (5, 'CO_HOI_DINH_TRE', 'Cơ hội bị gắn cờ đình trệ', 0, 1, 1),
    (6, 'CONG_VIEC_SAP_DEN_HAN', 'Công việc sắp đến hạn', 0, 1, 1),
    (7, 'CONG_VIEC_QUA_HAN', 'Công việc quá hạn', 0, 1, 1),
    (8, 'LEAD_QUA_SLA', 'Lead quá SLA phản hồi', 0, 1, 1),
    (9, 'HOP_DONG_SAP_HET_HAN', 'Hợp đồng sắp hết hạn', 0, 1, 1),
    (10, 'KHACH_HANG_RUI_RO', 'Khách hàng có rủi ro rời bỏ', 0, 1, 1);

-- Luu y: gia_tri = NULL o cac cau hinh ma backlog chi yeu cau "cau hinh duoc" nhung KHONG quy dinh con so cu the.
-- Khong tu bia nguong nghiep vu; Director/Admin se thiet lap qua chuc nang cau hinh.
INSERT INTO cau_hinh_he_thong
    (ma_cau_hinh, nhom_cau_hinh, kieu_du_lieu, gia_tri, mo_ta, bat_buoc_cau_hinh, bao_mat)
VALUES
    ('MUI_GIO', 'HE_THONG', 'TEXT', 'Asia/Ho_Chi_Minh', 'Múi giờ hiển thị và xử lý nghiệp vụ.', 1, 0),
    ('TIEN_TE', 'HE_THONG', 'TEXT', 'VND', 'Đơn vị tiền tệ mặc định.', 1, 0),
    ('SO_LAN_DANG_NHAP_SAI_TOI_DA', 'BAO_MAT', 'INTEGER', '5', 'Khoá tạm sau số lần sai liên tiếp.', 1, 0),
    ('PHUT_KHOA_DANG_NHAP', 'BAO_MAT', 'INTEGER', '15', 'Thời gian khoá tạm tài khoản.', 1, 0),
    ('PHUT_HET_HAN_RESET_MAT_KHAU', 'BAO_MAT', 'INTEGER', '30', 'Thời hạn link đặt lại mật khẩu.', 1, 0),
    ('PHUT_PHAN_BO_LEAD_TOI_DA', 'LEAD', 'INTEGER', '5', 'Lead phải được phân bổ trong thời gian này.', 1, 0),
    ('SLA_PHAN_HOI_LEAD_PHUT', 'LEAD', 'INTEGER', NULL, 'SLA phản hồi lead; Director cấu hình.', 1, 0),
    ('PHAN_TRANG_MAC_DINH', 'GIAO_DIEN', 'INTEGER', '20', 'Số dòng mặc định ở danh sách tài khoản.', 1, 0),
    ('KICH_THUOC_AVATAR_TOI_DA_MB', 'UPLOAD', 'INTEGER', '2', 'Ảnh đại diện JPG/PNG tối đa 2MB.', 1, 0),
    ('KICH_THUOC_TEP_CO_HOI_TOI_DA_MB', 'UPLOAD', 'INTEGER', '20', 'Tệp đính kèm cơ hội tối đa 20MB.', 1, 0),
    ('NGUONG_TEN_CONG_TY_GAN_GIONG', 'KHACH_HANG', 'DECIMAL', '0.90', 'Ngưỡng cảnh báo tên công ty gần giống; không tự merge.', 1, 0),
    ('SO_NGAY_CHAM_SOC_DINH_KY', 'KHACH_HANG', 'INTEGER', NULL, 'N ngày chưa tương tác để đưa vào danh sách chăm sóc.', 1, 0),
    ('NGUONG_YEU_CAU_HO_TRO_RUI_RO', 'KHACH_HANG', 'INTEGER', NULL, 'Số yêu cầu chưa xử lý để gắn cờ rủi ro.', 0, 0),
    ('NGUONG_CHIET_KHAU_1', 'BAO_GIA', 'DECIMAL', NULL, 'Ngưỡng chiết khấu thứ nhất; Team Lead duyệt khi vượt.', 1, 0),
    ('NGUONG_CHIET_KHAU_2', 'BAO_GIA', 'DECIMAL', NULL, 'Ngưỡng chiết khấu thứ hai; Director duyệt khi vượt.', 1, 0),
    ('SO_NGAY_CANH_BAO_GIA_HAN_HOP_DONG', 'HOP_DONG', 'INTEGER', NULL, 'Cảnh báo hợp đồng sắp hết hạn.', 0, 0),
    ('NHAC_CONG_VIEC_TRUOC_PHUT', 'CONG_VIEC', 'INTEGER', NULL, 'Mốc nhắc mặc định trước hạn công việc.', 0, 0),
    ('BANG_XEP_HANG_BAT', 'BAO_CAO', 'BOOLEAN', NULL, 'Bật/tắt bảng xếp hạng doanh số trong nhóm.', 0, 0),
    ('TEN_CONG_TY', 'PDF', 'TEXT', NULL, 'Tên công ty hiển thị trên PDF.', 1, 0),
    ('DIA_CHI_CONG_TY', 'PDF', 'TEXT', NULL, 'Địa chỉ công ty hiển thị trên PDF.', 1, 0),
    ('MA_SO_THUE_CONG_TY', 'PDF', 'TEXT', NULL, 'Mã số thuế công ty hiển thị trên PDF.', 0, 0),
    ('THONG_TIN_NGAN_HANG', 'PDF', 'TEXT', NULL, 'Thông tin ngân hàng trên PDF.', 0, 0),
    ('LOGO_PATH', 'PDF', 'TEXT', NULL, 'Đường dẫn logo dùng trong PDF.', 0, 0),
    ('DIEU_KHOAN_PDF_MAC_DINH', 'PDF', 'TEXT', NULL, 'Điều khoản mặc định cho PDF.', 0, 0),
    ('FOOTER_PDF', 'PDF', 'TEXT', NULL, 'Footer PDF.', 0, 0);

-- =====================================================================
-- 12. STORY COVERAGE - 76 USER STORY
-- Ban do nay nhung truc tiep User Story + Acceptance Criteria tu sheet
-- "4. Product Backlog" de BE/AI khong tu dien giai lai schema.
-- S1-07 la Story UI/error page, khong can table rieng.
-- Bao cao/dashboard/export chu yeu query tu schema nghiep vu da co.
-- =====================================================================

-- SPRINT 1
-- S1-01 [Must 5pt]
-- STORY: Là người dùng của hệ thống, tôi muốn đăng nhập bằng email công ty và mật khẩu, để truy cập được danh mục khách hàng của mình một cách an toàn.
-- AC: • Đăng nhập đúng thì vào được trang chủ tương ứng với vai trò
-- AC: • Sai thông tin hiển thị thông báo chung, không tiết lộ email có tồn tại hay không
-- AC: • Khoá tạm 15 phút sau 5 lần sai liên tiếp
-- DB: nguoi_dung, phien_dang_nhap, cau_hinh_he_thong
-- S1-02 [Must 3pt]
-- STORY: Là người dùng của hệ thống, tôi muốn duy trì phiên đăng nhập và đăng xuất an toàn, để không mất ghi chú cuộc gặp đang nhập dở khi đang ngồi ở quán cà phê.
-- AC: • Phiên được gia hạn tự động khi còn hoạt động
-- AC: • Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server
-- AC: • Phiên hết hạn đưa về trang đăng nhập kèm thông báo rõ ràng
-- DB: phien_dang_nhap, nguoi_dung
-- S1-03 [Must 5pt]
-- STORY: Là người dùng của hệ thống, tôi muốn đặt lại mật khẩu khi quên thông qua email, để tự lấy lại quyền truy cập khi đang đi gặp khách.
-- AC: • Nhập email nhận được liên kết đặt lại có hiệu lực 30 phút
-- AC: • Liên kết chỉ dùng được một lần
-- AC: • Email không tồn tại vẫn hiển thị cùng một thông báo
-- DB: dat_lai_mat_khau_token, email_gui, cau_hinh_he_thong
-- S1-04 [Must 2pt]
-- STORY: Là người dùng của hệ thống, tôi muốn đổi mật khẩu khi đang đăng nhập, để chủ động bảo vệ danh mục khách hàng của mình.
-- AC: • Bắt buộc nhập mật khẩu hiện tại
-- AC: • Mật khẩu mới tối thiểu 8 ký tự, có chữ và số
-- AC: • Đổi xong thu hồi các phiên đăng nhập khác
-- DB: nguoi_dung, phien_dang_nhap, nhat_ky_he_thong
-- S1-05 [Must 8pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn có phân quyền vừa theo vai trò vừa theo dữ liệu sở hữu, để nhân viên chỉ thấy khách của mình, trưởng nhóm thấy toàn nhóm, còn tôi thấy tất cả.
-- AC: • Ba phạm vi dữ liệu: của tôi, của nhóm tôi, tất cả — áp dụng cho khách hàng, cơ hội, hoạt động, báo giá
-- AC: • Mọi truy vấn danh sách tự động lọc theo phạm vi, kể cả tìm kiếm và xuất Excel
-- AC: • Truy cập bản ghi ngoài phạm vi hiển thị thông báo tiếng Việt rõ ràng
-- AC: • Có kiểm thử tự động chứng minh nhân viên A không đọc được khách hàng của nhân viên B
-- DB: vai_tro_module, nguoi_dung, nhom_kinh_doanh, khach_hang, co_hoi, hoat_dong, bao_gia
-- S1-06 [Must 5pt]
-- STORY: Là người dùng của hệ thống, tôi muốn thấy menu điều hướng đúng theo quyền của mình, để không bị rối bởi những chức năng mình không được dùng.
-- AC: • Mục menu không thuộc quyền thì không hiển thị
-- AC: • Hiển thị tên, vai trò và nhóm kinh doanh đang thuộc về
-- AC: • Dùng được thuận tiện trên màn hình 360px
-- DB: vai_tro_module, module_he_thong, nguoi_dung, nguoi_dung_vai_tro, nhom_kinh_doanh
-- S1-07 [Should 1pt]
-- STORY: Là người dùng của hệ thống, tôi muốn nhận thông báo rõ ràng khi truy cập nhầm chỗ hoặc không đủ quyền, để biết mình nên làm gì tiếp thay vì gặp một trang trắng.
-- AC: • Trang báo lỗi dùng chung giao diện ứng dụng
-- AC: • Mỗi trang lỗi có một hành động gợi ý để quay lại luồng làm việc
-- DB: KHONG CAN TABLE RIENG
-- S1-08 [Must 8pt]
-- STORY: Là Quản trị hệ thống, tôi muốn tạo, sửa và tìm kiếm tài khoản người dùng, để cấp quyền cho nhân viên kinh doanh mới ngay ngày đầu nhận địa bàn.
-- AC: • Tạo tài khoản gửi email kích hoạt kèm mật khẩu tạm
-- AC: • Email trùng bị từ chối kèm thông báo cụ thể
-- AC: • Tìm theo tên, email, nhóm; lọc theo vai trò và trạng thái
-- AC: • Danh sách phân trang, mặc định 20 dòng
-- DB: nguoi_dung, token_kich_hoat_tai_khoan, nguoi_dung_vai_tro, nhom_kinh_doanh, email_gui, cau_hinh_he_thong
-- S1-09 [Must 3pt]
-- STORY: Là Quản trị hệ thống, tôi muốn gán vai trò và gắn người dùng vào nhóm kinh doanh, để cây tổ chức quyết định đúng phạm vi dữ liệu mỗi người nhìn thấy.
-- AC: • Một người dùng có thể giữ nhiều vai trò cùng lúc
-- AC: • Người giữ vai trò Trưởng nhóm phải được gán một nhóm cụ thể
-- AC: • Không thể tự thu hồi vai trò quản trị của chính mình
-- DB: nguoi_dung_vai_tro, vai_tro, nguoi_dung, nhom_kinh_doanh, nhat_ky_he_thong
-- S1-10 [Must 2pt]
-- STORY: Là Quản trị hệ thống, tôi muốn khoá tài khoản và bàn giao dữ liệu khi nhân viên nghỉ, để khách hàng và cơ hội không bị mất chủ khi người phụ trách rời công ty.
-- AC: • Tài khoản bị khoá không đăng nhập được và bị thu hồi phiên đang mở
-- AC: • Bắt buộc chọn người tiếp nhận toàn bộ khách hàng và cơ hội trước khi khoá
-- AC: • Việc bàn giao được ghi nhật ký, dữ liệu không bị mất chủ sở hữu
-- DB: nguoi_dung, phien_dang_nhap, nhat_ky_ban_giao, nhat_ky_ban_giao_chi_tiet, nhat_ky_he_thong, khach_hang, co_hoi, lead, cong_viec

-- SPRINT 2
-- S2-01 [Should 5pt]
-- STORY: Là Quản trị hệ thống, tôi muốn nhập danh sách người dùng hàng loạt từ tệp Excel, để tạo tài khoản cho cả khối kinh doanh trong vài phút.
-- AC: • Tải được tệp mẫu
-- AC: • Xem trước và báo lỗi theo từng dòng trước khi nhập
-- AC: • Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết
-- DB: dot_nhap_du_lieu, loi_nhap_du_lieu, nguoi_dung, nguoi_dung_vai_tro
-- S2-02 [Must 3pt]
-- STORY: Là người dùng của hệ thống, tôi muốn xem và cập nhật hồ sơ cá nhân, để chữ ký email của tôi luôn đúng khi gửi báo giá cho khách.
-- AC: • Sửa được họ tên, số điện thoại, chữ ký email
-- AC: • Không tự đổi được email, nhóm và vai trò
-- AC: • Kiểm tra định dạng số điện thoại Việt Nam
-- DB: nguoi_dung
-- S2-03 [Could 2pt]
-- STORY: Là người dùng của hệ thống, tôi muốn tải lên ảnh đại diện, để đồng nghiệp nhận ra ai đang phụ trách khách hàng khi xem hồ sơ.
-- AC: • Chấp nhận JPG/PNG tối đa 2MB
-- AC: • Ảnh được cắt vuông và tạo bản thu nhỏ
-- DB: nguoi_dung, tep_dinh_kem
-- S2-04 [Must 3pt]
-- STORY: Là Quản trị hệ thống, tôi muốn xem nhật ký thay đổi trên dữ liệu nhạy cảm, để truy được ai đã sửa chiết khấu hoặc chỉ tiêu khi cuối quý số liệu không khớp.
-- AC: • Ghi lại mọi thay đổi trên chiết khấu, chỉ tiêu, quyền sở hữu dữ liệu và vai trò người dùng
-- AC: • Mỗi bản ghi có người thực hiện, thời điểm, giá trị trước và sau
-- AC: • Lọc theo người dùng, loại đối tượng, khoảng thời gian
-- DB: nhat_ky_he_thong
-- S2-05 [Must 8pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn quản lý danh mục sản phẩm dịch vụ và bảng giá niêm yết, để mọi báo giá đều xuất phát từ một bảng giá chuẩn thay vì giá tự nghĩ.
-- AC: • Khai báo mã, tên, loại (sản phẩm một lần hoặc dịch vụ thuê bao), đơn vị tính, giá niêm yết, giá sàn
-- AC: • Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không
-- AC: • Giá vốn chỉ Giám đốc kinh doanh xem và sửa được
-- AC: • Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh
-- DB: san_pham, bao_gia_chi_tiet
-- S2-06 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn khai báo cơ cấu tổ chức kinh doanh, để phạm vi dữ liệu của trưởng nhóm bám đúng cây tổ chức thật.
-- AC: • Nhóm kinh doanh có cấu trúc cây, mỗi nhóm có một trưởng nhóm
-- AC: • Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm
-- AC: • Cây tổ chức này quyết định phạm vi dữ liệu mà Trưởng nhóm nhìn thấy
-- AC: • Khai báo khu vực địa lý và gán khu vực cho nhóm
-- DB: nhom_kinh_doanh, khu_vuc_dia_ly, nguoi_dung
-- S2-07 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn khai báo các danh mục dùng chung của bán hàng, để cả khối gọi tên nguồn lead và ngành nghề giống nhau để báo cáo gộp được.
-- AC: • Ngành nghề khách hàng, quy mô doanh nghiệp, nguồn lead, loại hoạt động
-- AC: • Giá trị đang được tham chiếu thì không xoá được
-- AC: • Sắp xếp được thứ tự hiển thị
-- DB: nganh_nghe, quy_mo_doanh_nghiep, nguon_lead, loai_hoat_dong
-- S2-08 [Should 2pt]
-- STORY: Là Quản trị hệ thống, tôi muốn khai báo trường tuỳ chỉnh cho khách hàng và cơ hội, để đưa được những cột mà nhân viên đang tự thêm trong Excel vào hệ thống.
-- AC: • Thêm trường kiểu văn bản, số, ngày, danh sách chọn
-- AC: • Đặt được trường là bắt buộc hay không
-- AC: • Trường tuỳ chỉnh xuất hiện trong biểu mẫu, bộ lọc và bản xuất Excel
-- DB: truong_tuy_chinh, gia_tri_truong_tuy_chinh_khach_hang, gia_tri_truong_tuy_chinh_co_hoi
-- S2-09 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn cấu hình các giai đoạn pipeline và xác suất thắng, để con số dự báo doanh số có cơ sở thay vì dựa vào cảm nhận.
-- AC: • Khai báo chuỗi giai đoạn, ví dụ Tiếp cận → Xác định nhu cầu → Đề xuất giải pháp → Báo giá → Đàm phán → Chốt
-- AC: • Mỗi giai đoạn có xác suất thắng mặc định dùng để tính dự báo
-- AC: • Khai báo điều kiện bắt buộc để rời một giai đoạn, ví dụ phải có ít nhất một cuộc gặp
-- AC: • Thay đổi cấu hình không làm hỏng cơ hội đang chạy
-- DB: pipeline_ban_hang, giai_doan_pipeline, dieu_kien_giai_doan, co_hoi
-- S2-10 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn khai báo danh mục lý do thắng thua và đối thủ, để năm sau không thua lại đúng chỗ đã thua năm nay.
-- AC: • Danh sách lý do thắng và lý do thua khai báo riêng
-- AC: • Danh sách đối thủ cạnh tranh
-- AC: • Đây là dữ liệu bắt buộc khi đóng một cơ hội ở Sprint 5
-- DB: ly_do_thang_thua, doi_thu, co_hoi

-- SPRINT 3
-- S3-01 [Must 8pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn quản lý hồ sơ khách hàng doanh nghiệp, để có một danh sách khách chuẩn thay vì file Excel riêng của từng người.
-- AC: • Khai báo tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ, người sở hữu
-- AC: • Mã số thuế nếu có thì phải là duy nhất
-- AC: • Khách hàng có trạng thái: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác
-- AC: • Nhân viên chỉ thấy khách hàng mình sở hữu; trưởng nhóm thấy toàn nhóm
-- DB: khach_hang, nganh_nghe, quy_mo_doanh_nghiep, khu_vuc_dia_ly, nguoi_dung
-- S3-02 [Must 8pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn quản lý người liên hệ và vai trò của họ trong quyết định mua, để biết phải thuyết phục ai và ai là người có thể cản thương vụ.
-- AC: • Mỗi khách hàng có nhiều người liên hệ, mỗi người có chức danh, email, số điện thoại
-- AC: • Đánh dấu vai trò trong quyết định mua: người quyết định, người ảnh hưởng, người dùng cuối, người cản trở
-- AC: • Đánh dấu một người là đầu mối chính
-- AC: • Một người liên hệ chuyển sang công ty khác thì gắn lại được sang khách hàng mới, giữ nguyên lịch sử
-- DB: nguoi_lien_he, lich_su_lien_he_cong_ty, khach_hang
-- S3-03 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn xem trang 360 của một khách hàng, để nắm toàn bộ bối cảnh trước cuộc gặp mà không phải mở năm chỗ khác nhau.
-- AC: • Một trang gom: thông tin công ty, danh sách người liên hệ, cơ hội đang mở và đã đóng, dòng thời gian hoạt động, tệp đính kèm
-- AC: • Hiển thị tổng giá trị đã ký và giá trị cơ hội đang mở
-- AC: • Tải xong dưới 1,5 giây với 500 hoạt động
-- DB: khach_hang, nguoi_lien_he, co_hoi, hoat_dong, tep_dinh_kem, hop_dong
-- S3-04 [Must 5pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn được cảnh báo và gộp khách hàng trùng, để hai nhân viên không cùng chào một công ty mà không biết nhau.
-- AC: • Phát hiện trùng theo mã số thuế, tên công ty gần giống và website
-- AC: • Hiển thị so sánh cạnh nhau trước khi gộp
-- AC: • Gộp giữ lại toàn bộ người liên hệ, cơ hội và hoạt động của cả hai bản ghi
-- AC: • Chỉ Trưởng nhóm trở lên được thực hiện gộp
-- DB: khach_hang, lich_su_gop_khach_hang, nguoi_lien_he, co_hoi, hoat_dong, cau_hinh_he_thong
-- S3-05 [Should 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn khai báo quan hệ công ty mẹ và công ty con, để nhìn được tổng giá trị của cả tập đoàn chứ không chỉ từng pháp nhân.
-- AC: • Gắn một khách hàng làm công ty con của khách hàng khác
-- AC: • Trang công ty mẹ hiển thị tổng giá trị hợp đồng của cả nhóm công ty
-- DB: khach_hang, hop_dong
-- S3-06 [Should 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn nhập danh sách khách hàng hàng loạt từ Excel, để đưa danh mục khách đang có vào hệ thống mà không gõ lại.
-- AC: • Tải được tệp mẫu, xem trước và báo lỗi theo từng dòng
-- AC: • Bản ghi trùng được đánh dấu rõ trong bản xem trước để chọn bỏ qua hoặc cập nhật
-- DB: dot_nhap_du_lieu, loi_nhap_du_lieu, khach_hang
-- S3-07 [Must 2pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn tìm kiếm và lọc khách hàng theo nhiều điều kiện, để dựng nhanh danh sách khách cần gọi trong tuần.
-- AC: • Lọc theo trạng thái, ngành nghề, quy mô, khu vực, người sở hữu
-- AC: • Tìm theo tên, mã số thuế, số điện thoại người liên hệ
-- AC: • Lưu lại được bộ lọc hay dùng
-- DB: khach_hang, nguoi_lien_he, bo_loc_da_luu
-- S3-08 [Should 5pt]
-- STORY: Là Chăm sóc khách hàng, tôi muốn ghi nhận yêu cầu hỗ trợ sau bán và gắn cờ khách có rủi ro rời bỏ, để giữ được khách hiện có thay vì chỉ chạy theo khách mới.
-- AC: • Ghi nhận yêu cầu hỗ trợ với mức độ ưu tiên, người xử lý và trạng thái
-- AC: • Khách có nhiều yêu cầu chưa xử lý được gắn cờ rủi ro tự động
-- AC: • Cờ rủi ro hiển thị trên trang 360 và cảnh báo cho nhân viên kinh doanh phụ trách
-- DB: yeu_cau_ho_tro, khach_hang, thong_bao, cau_hinh_he_thong
-- S3-09 [Should 3pt]
-- STORY: Là Chăm sóc khách hàng, tôi muốn xem danh sách khách hàng cần chăm sóc định kỳ, để không để khách đã ký hợp đồng bị bỏ quên tới lúc gia hạn.
-- AC: • Danh sách khách chưa có tương tác nào trong N ngày, N cấu hình được
-- AC: • Sắp xếp theo giá trị hợp đồng giảm dần
-- AC: • Đánh dấu đã liên hệ ngay trên danh sách
-- DB: khach_hang, hoat_dong, hop_dong, cau_hinh_he_thong

-- SPRINT 4
-- S4-01 [Must 5pt]
-- STORY: Là Nhân viên Marketing, tôi muốn thu thập lead từ biểu mẫu nhúng trên website, để mọi lead vào thẳng hệ thống thay vì nằm trong hộp thư chung.
-- AC: • Sinh mã nhúng cho một biểu mẫu, dán được vào website bất kỳ
-- AC: • Biểu mẫu gồm họ tên, email, số điện thoại, công ty, nhu cầu quan tâm
-- AC: • Có chống spam và giới hạn tần suất theo địa chỉ IP
-- AC: • Gửi thành công tạo lead ở trạng thái Mới và gắn đúng nguồn của biểu mẫu
-- DB: bieu_mau_lead, nhat_ky_gui_form_lead, lead, nguon_lead, chien_dich
-- S4-02 [Must 5pt]
-- STORY: Là Nhân viên Marketing, tôi muốn tạo lead thủ công và nhập lead hàng loạt từ Excel, để đưa danh sách thu được từ hội thảo vào hệ thống ngay hôm sau.
-- AC: • Nhập tay một lead từ sự kiện hoặc danh thiếp
-- AC: • Nhập hàng loạt có tệp mẫu, xem trước và báo lỗi theo từng dòng
-- AC: • Mọi lead nhập vào đều bắt buộc có nguồn
-- DB: lead, dot_nhap_du_lieu, loi_nhap_du_lieu, nguon_lead
-- S4-03 [Should 5pt]
-- STORY: Là Nhân viên Marketing, tôi muốn theo dõi lead theo từng chiến dịch, để đo được chiến dịch nào thực sự ra doanh thu chứ không chỉ ra nhiều lead.
-- AC: • Khai báo chiến dịch với ngân sách, thời gian chạy, kênh
-- AC: • Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng
-- AC: • Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch
-- DB: chien_dich, lead, co_hoi, hop_dong
-- S4-04 [Must 5pt]
-- STORY: Là Nhân viên Marketing, tôi muốn được cảnh báo và gộp lead trùng, để không để hai nhân viên cùng gọi một người trong một buổi sáng.
-- AC: • Phát hiện trùng theo email, số điện thoại và tên công ty
-- AC: • Lead trùng với khách hàng đã có được gợi ý gắn thẳng vào khách hàng đó
-- AC: • Gộp giữ nguyên lịch sử của cả hai bản ghi
-- DB: lead, lich_su_gop_lead, khach_hang
-- S4-05 [Must 8pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn cấu hình chấm điểm lead theo tiêu chí khai báo được, để nhân viên gọi những lead có khả năng nhất trước.
-- AC: • Khai báo tiêu chí và số điểm: ngành nghề phù hợp, quy mô doanh nghiệp, nguồn, mức độ quan tâm
-- AC: • Điểm được tính lại tự động khi thông tin lead thay đổi
-- AC: • Phân loại Nóng, Ấm, Lạnh theo ngưỡng điểm khai báo được
-- AC: • Điểm chỉ để sắp xếp ưu tiên, không tự động loại lead
-- DB: quy_tac_cham_diem_lead, nguong_phan_loai_lead, lead
-- S4-06 [Must 8pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn cấu hình quy tắc phân bổ lead tự động, để lead tới tay người phụ trách trong vài phút thay vì chờ họp giao ban.
-- AC: • Phân bổ theo khu vực, theo ngành nghề, hoặc xoay vòng đều trong nhóm
-- AC: • Nhiều quy tắc xếp theo thứ tự ưu tiên, quy tắc đầu tiên khớp sẽ thắng
-- AC: • Lead không khớp quy tắc nào rơi vào hàng chờ để trưởng nhóm phân tay
-- AC: • Phân bổ chạy nền, hoàn tất trong vòng 5 phút kể từ khi lead vào
-- DB: quy_tac_phan_bo_lead, lich_su_phan_bo_lead, lead, tac_vu_nen
-- S4-07 [Must 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn nhận hoặc từ chối lead được phân, có ràng buộc SLA phản hồi, để lead không nằm im ba ngày rồi nguội hẳn.
-- AC: • Nhân viên nhận lead thì lead chuyển sang Đang chăm sóc
-- AC: • Từ chối bắt buộc nhập lý do, lead quay lại hàng chờ phân bổ
-- AC: • Quá SLA phản hồi mà chưa liên hệ thì lead được gắn cờ và báo cho trưởng nhóm
-- DB: lead, lich_su_phan_bo_lead, thong_bao, cau_hinh_he_thong
-- S4-08 [Must 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn chuyển một lead đủ điều kiện thành khách hàng và cơ hội, để không phải nhập lại thông tin đã hỏi khách ba lần.
-- AC: • Một thao tác sinh đồng thời khách hàng doanh nghiệp, người liên hệ và cơ hội bán hàng
-- AC: • Dữ liệu lead được chuyển sang, không phải nhập lại
-- AC: • Lead chuyển sang trạng thái Đã chuyển đổi và không sửa được nữa
-- AC: • Toàn bộ hoạt động đã ghi trên lead được giữ lại trên khách hàng mới
-- DB: lead, khach_hang, nguoi_lien_he, co_hoi, hoat_dong
-- S4-09 [Must 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn xem danh sách lead với bộ lọc và bộ lọc lưu sẵn, để mở máy buổi sáng là biết ngay hôm nay cần gọi ai.
-- AC: • Lọc theo trạng thái, nguồn, phân loại nóng ấm lạnh, người phụ trách, khoảng thời gian
-- AC: • Lead quá SLA hiển thị nổi bật
-- AC: • Lưu và đặt tên cho bộ lọc hay dùng
-- DB: lead, bo_loc_da_luu

-- SPRINT 5
-- S5-01 [Must 8pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn tạo và quản lý một cơ hội bán hàng, để thương vụ nằm trong hệ thống thay vì trong trí nhớ của tôi.
-- AC: • Khai báo tên cơ hội, khách hàng, người liên hệ chính, giai đoạn, giá trị dự kiến, ngày dự kiến chốt, nguồn
-- AC: • Xác suất thắng lấy mặc định theo giai đoạn, sửa tay được kèm ghi chú
-- AC: • Một khách hàng có thể có nhiều cơ hội song song
-- AC: • Ngày dự kiến chốt không được ở quá khứ khi tạo mới
-- DB: co_hoi, khach_hang, nguoi_lien_he, giai_doan_pipeline, lead, chien_dich
-- S5-02 [Must 8pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn điều hành các cơ hội trên bảng pipeline dạng kanban, để nhìn một màn hình là biết thương vụ nào đang tắc.
-- AC: • Mỗi cột là một giai đoạn, hiển thị số cơ hội và tổng giá trị của cột
-- AC: • Kéo thả để chuyển giai đoạn
-- AC: • Thẻ cơ hội hiển thị tên khách, giá trị, ngày dự kiến chốt và cảnh báo nếu đình trệ
-- AC: • Lọc theo người sở hữu, nhóm, khoảng ngày chốt; nhân viên mặc định chỉ thấy cơ hội của mình
-- DB: co_hoi, giai_doan_pipeline, lich_su_giai_doan_co_hoi
-- S5-03 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn thêm sản phẩm dịch vụ vào cơ hội để ra giá trị hợp đồng, để giá trị cơ hội là con số có căn cứ chứ không phải ước lượng.
-- AC: • Chọn sản phẩm từ danh mục, nhập số lượng và đơn giá, đơn giá mặc định lấy từ bảng giá niêm yết
-- AC: • Giá trị cơ hội được tính lại tự động từ các dòng sản phẩm
-- AC: • Với dịch vụ thuê bao, khai báo được số kỳ và giá trị hợp đồng theo năm
-- DB: co_hoi_san_pham, san_pham, co_hoi
-- S5-04 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn có điều kiện bắt buộc khi cơ hội rời một giai đoạn, để pipeline phản ánh thực tế thay vì bị đẩy giai đoạn cho đẹp báo cáo.
-- AC: • Không cho chuyển giai đoạn nếu chưa thoả điều kiện đã cấu hình, ví dụ chưa có cuộc gặp nào được ghi nhận
-- AC: • Thông báo nêu rõ còn thiếu điều kiện gì
-- AC: • Trưởng nhóm trở lên có thể ghi đè kèm lý do
-- DB: dieu_kien_giai_doan, lich_su_giai_doan_co_hoi, lich_su_co_hoi, co_hoi
-- S5-05 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn đóng một cơ hội với kết quả thắng hoặc thua, để công ty học được từ cả thương vụ thắng lẫn thua.
-- AC: • Đóng Thắng bắt buộc nhập giá trị chốt thực tế và ngày ký
-- AC: • Đóng Thua bắt buộc chọn lý do thua và đối thủ thắng thầu nếu có
-- AC: • Cơ hội đã đóng không sửa được, chỉ Trưởng nhóm trở lên mở lại được kèm lý do
-- AC: • Cơ hội thắng được tính vào chỉ tiêu của người sở hữu
-- DB: co_hoi, ly_do_thang_thua, doi_thu, lich_su_co_hoi
-- S5-06 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn xem dự báo doanh số tính theo trọng số xác suất, để trả lời được câu hỏi tháng này về bao nhiêu bằng số liệu.
-- AC: • Dự báo bằng tổng của giá trị cơ hội nhân xác suất thắng của giai đoạn
-- AC: • Nhóm theo kỳ dự kiến chốt: tháng này, tháng sau, quý này
-- AC: • So sánh dự báo với chỉ tiêu và với số đã chốt thực tế
-- AC: • Xem được theo từng nhân viên và từng nhóm
-- DB: co_hoi, giai_doan_pipeline, chi_tieu, hop_dong
-- S5-07 [Must 5pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn được cảnh báo khi cơ hội đình trệ, để can thiệp sớm thay vì phát hiện khi thương vụ đã nguội.
-- AC: • Cơ hội không có hoạt động nào trong N ngày được gắn cờ, N cấu hình theo từng giai đoạn
-- AC: • Cơ hội quá ngày dự kiến chốt mà chưa đóng cũng được gắn cờ
-- AC: • Danh sách cơ hội có cờ hiển thị riêng cho trưởng nhóm
-- AC: • Tác vụ đánh giá chạy mỗi ngày một lần
-- DB: co_hoi, giai_doan_pipeline, thong_bao, tac_vu_nen
-- S5-08 [Must 3pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn phân bổ lại cơ hội cho người khác trong nhóm, để thương vụ không đứng yên khi người phụ trách nghỉ dài hoặc quá tải.
-- AC: • Chuyển quyền sở hữu một hoặc nhiều cơ hội cùng lúc
-- AC: • Người nhận thấy ngay cơ hội trong danh sách của mình cùng toàn bộ lịch sử
-- AC: • Mỗi lần chuyển được ghi nhật ký kèm lý do
-- DB: co_hoi, lich_su_co_hoi, nhat_ky_he_thong

-- SPRINT 6
-- S6-01 [Must 2pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn xem lịch sử thay đổi của một cơ hội, để biết vì sao giá trị hay ngày chốt bị đổi mà không phải hỏi lại.
-- AC: • Ghi lại mọi thay đổi giai đoạn, giá trị, ngày chốt và người sở hữu
-- AC: • Mỗi dòng có giá trị trước, giá trị sau, người thực hiện và thời điểm
-- DB: lich_su_co_hoi, lich_su_giai_doan_co_hoi, co_hoi
-- S6-02 [Must 2pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn tìm kiếm và lọc cơ hội theo nhiều điều kiện, để dựng nhanh danh sách thương vụ cần đẩy trong tuần.
-- AC: • Lọc theo giai đoạn, khoảng giá trị, khoảng ngày chốt, sản phẩm, người sở hữu
-- AC: • Lưu lại được bộ lọc hay dùng
-- DB: co_hoi, co_hoi_san_pham, bo_loc_da_luu
-- S6-03 [Should 5pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn chia sẻ một cơ hội cho nhiều người cùng theo, để thương vụ lớn có cả đội cùng vào mà vẫn rõ ai chịu trách nhiệm chính.
-- AC: • Ngoài người sở hữu, thêm được thành viên cùng tham gia cơ hội
-- AC: • Thành viên được chia sẻ có quyền ghi hoạt động nhưng không đổi được người sở hữu
-- AC: • Chuyển quyền sở hữu cơ hội được ghi nhật ký
-- DB: co_hoi_thanh_vien, co_hoi, hoat_dong, lich_su_co_hoi
-- S6-04 [Should 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn đính kèm tài liệu vào cơ hội, để hồ sơ thầu và biên bản họp nằm cùng chỗ với thương vụ.
-- AC: • Tải lên nhiều tệp, mỗi tệp tối đa 20MB
-- AC: • Chỉ người sở hữu và người được chia sẻ mới tải xuống được
-- AC: • Hiển thị người tải lên và thời điểm
-- DB: tep_dinh_kem, co_hoi, co_hoi_thanh_vien
-- S6-05 [Must 8pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn ghi nhận một hoạt động với khách hàng, để lịch sử trao đổi không nằm trong hộp thư cá nhân của riêng tôi.
-- AC: • Loại hoạt động: cuộc gọi, cuộc gặp, email, ghi chú
-- AC: • Gắn hoạt động với khách hàng, người liên hệ và cơ hội liên quan
-- AC: • Ghi thời điểm, thời lượng, nội dung trao đổi và kết quả
-- AC: • Ghi được hoạt động đã diễn ra trong quá khứ, không chỉ hoạt động sắp tới
-- AC: • Dùng được nhanh trên điện thoại ngay sau khi rời văn phòng khách
-- DB: hoat_dong, loai_hoat_dong, khach_hang, nguoi_lien_he, co_hoi
-- S6-06 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn tạo và theo dõi công việc có hạn hoàn thành, để không quên lời hứa gửi báo giá cho khách vào thứ Sáu.
-- AC: • Công việc có tiêu đề, hạn, mức ưu tiên, người thực hiện, gắn với khách hàng hoặc cơ hội
-- AC: • Đánh dấu hoàn thành, hoãn kèm lý do
-- AC: • Trưởng nhóm giao việc được cho thành viên trong nhóm
-- DB: cong_viec, khach_hang, co_hoi, nguoi_dung
-- S6-07 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn xem lịch làm việc của mình và của nhóm, để bố trí lịch gặp mà không chồng chéo với đồng đội.
-- AC: • Chế độ xem theo ngày, tuần và tháng
-- AC: • Hiển thị cuộc gặp đã đặt và công việc có hạn trong cùng một khung nhìn
-- AC: • Trưởng nhóm xem được lịch chồng của cả nhóm để bố trí
-- AC: • Lịch hiển thị tốt trên điện thoại
-- DB: hoat_dong, cong_viec, nguoi_dung, nhom_kinh_doanh
-- S6-08 [Must 5pt]
-- STORY: Là Chăm sóc khách hàng, tôi muốn xem dòng thời gian tương tác đầy đủ của một khách hàng, để tiếp nhận khách từ đồng nghiệp mà nắm được bối cảnh trong năm phút.
-- AC: • Gộp toàn bộ hoạt động, thay đổi cơ hội, báo giá đã gửi theo thứ tự thời gian
-- AC: • Lọc theo loại hoạt động và theo người thực hiện
-- DB: hoat_dong, lich_su_co_hoi, bao_gia, email_gui
-- S6-09 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn được nhắc việc sắp đến hạn và việc quá hạn, để không để việc trôi vì bận chạy thương vụ khác.
-- AC: • Nhắc trong ứng dụng trước hạn theo mốc cấu hình được
-- AC: • Việc quá hạn hiển thị nổi bật trên trang chủ cá nhân
-- AC: • Trưởng nhóm thấy tổng số việc quá hạn của từng thành viên
-- DB: cong_viec, thong_bao, cau_hinh_he_thong
-- S6-10 [Should 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn ghi nhanh kết quả cuộc gọi ngay trên điện thoại, để ghi lại khi còn nhớ thay vì để tối về quên mất.
-- AC: • Biểu mẫu rút gọn: kết quả cuộc gọi, ghi chú ngắn, đặt lịch gọi lại
-- AC: • Hoàn tất trong không quá ba thao tác chạm
-- AC: • Tự động gắn vào cơ hội đang mở gần nhất của khách hàng đó
-- DB: hoat_dong, cong_viec, co_hoi

-- SPRINT 7
-- S7-01 [Must 8pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn soạn báo giá từ một cơ hội, để gửi báo giá trong buổi chiều cùng ngày khách hỏi.
-- AC: • Dòng sản phẩm được kế thừa từ cơ hội, sửa lại được
-- AC: • Mỗi dòng nhập chiết khấu theo phần trăm hoặc số tiền, hệ thống tính lại thành tiền ngay
-- AC: • Hiển thị mức chiết khấu tổng và cảnh báo khi chạm ngưỡng cần duyệt
-- AC: • Khai báo hiệu lực báo giá, điều khoản thanh toán và điều khoản giao hàng
-- AC: • Lưu nháp và sửa tiếp được
-- DB: bao_gia, bao_gia_phien_ban, bao_gia_chi_tiet, co_hoi_san_pham, san_pham
-- S7-02 [Must 8pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn duyệt báo giá theo mức chiết khấu, để kiểm soát biên lợi nhuận mà không phải trả lời từng tin nhắn xin giảm giá.
-- AC: • Chiết khấu dưới ngưỡng thứ nhất thì tự động thông qua, không cần duyệt
-- AC: • Vượt ngưỡng thứ nhất do Trưởng nhóm duyệt; vượt ngưỡng thứ hai hoặc dưới giá sàn thì bắt buộc Giám đốc kinh doanh duyệt
-- AC: • Ba hành động: Duyệt, Từ chối, Trả lại sửa — hai hành động sau bắt buộc nhập ý kiến
-- AC: • Chỉ báo giá đã duyệt mới gửi được cho khách
-- AC: • Lịch sử duyệt không sửa và không xoá được
-- DB: bao_gia, bao_gia_phien_ban, duyet_bao_gia, cau_hinh_he_thong, san_pham
-- S7-03 [Must 5pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn quản lý nhiều phiên bản của một báo giá, để biết chính xác đã cam kết gì với khách ở lần gửi trước.
-- AC: • Mỗi lần sửa sau khi đã gửi sẽ tạo phiên bản mới, phiên bản cũ được giữ nguyên
-- AC: • So sánh cạnh nhau hai phiên bản để thấy đã thay đổi gì
-- AC: • Chỉ một phiên bản ở trạng thái đang hiệu lực tại một thời điểm
-- DB: bao_gia, bao_gia_phien_ban, bao_gia_chi_tiet
-- S7-04 [Must 5pt]
-- STORY: Là Kế toán, tôi muốn chuyển một báo giá được chấp nhận thành hợp đồng, để số liệu hợp đồng khớp báo giá mà không phải gõ lại.
-- AC: • Hợp đồng kế thừa toàn bộ dòng sản phẩm và giá đã chốt, không nhập lại
-- AC: • Khai báo số hợp đồng, ngày ký, ngày hiệu lực, ngày hết hạn, điều khoản thanh toán
-- AC: • Cơ hội tương ứng tự động chuyển sang Đóng thắng với giá trị đúng bằng giá trị hợp đồng
-- AC: • Số hợp đồng là duy nhất
-- DB: hop_dong, hop_dong_chi_tiet, bao_gia, bao_gia_phien_ban, co_hoi
-- S7-05 [Must 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn xuất báo giá và hợp đồng ra PDF theo mẫu công ty, để gửi khách tài liệu đúng nhận diện thương hiệu.
-- AC: • Mẫu có logo, thông tin công ty, bảng dòng sản phẩm, tổng tiền bằng số và bằng chữ
-- AC: • Mẫu khai báo được ở phần cấu hình, không cố định trong mã nguồn
-- DB: tep_dinh_kem, cau_hinh_he_thong, bao_gia, hop_dong
-- S7-06 [Must 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn gửi báo giá cho khách và nhận phản hồi qua liên kết, để biết khách đã xem và quyết định thế nào mà không phải gọi hỏi.
-- AC: • Gửi email kèm liên kết xem báo giá, liên kết chỉ dùng cho đúng báo giá đó
-- AC: • Khách bấm Chấp nhận hoặc Từ chối, có ô ghi ý kiến
-- AC: • Phản hồi cập nhật ngay trạng thái báo giá và ghi vào dòng thời gian của cơ hội
-- AC: • Quá hạn hiệu lực mà không phản hồi thì báo giá tự chuyển sang Hết hiệu lực
-- DB: lien_ket_bao_gia, phan_hoi_bao_gia, email_gui, bao_gia, hoat_dong, tac_vu_nen
-- S7-07 [Should 2pt]
-- STORY: Là Kế toán, tôi muốn theo dõi hiệu lực và gia hạn hợp đồng, để không mất doanh thu vì quên gia hạn hợp đồng sắp hết hạn.
-- AC: • Danh sách hợp đồng kèm ngày hết hạn và số ngày còn lại
-- AC: • Cảnh báo hợp đồng sắp hết hạn theo ngưỡng cấu hình được
-- AC: • Tạo cơ hội gia hạn từ một hợp đồng sắp hết hạn
-- DB: hop_dong, co_hoi, thong_bao, cau_hinh_he_thong
-- S7-08 [Must 5pt]
-- STORY: Là người dùng của hệ thống, tôi muốn nhận thông báo trong ứng dụng cho các sự kiện quan trọng, để không bỏ lỡ báo giá chờ mình duyệt hay phản hồi của khách.
-- AC: • Kích hoạt khi: được phân lead, có báo giá chờ mình duyệt, khách phản hồi báo giá, được giao việc, cơ hội bị gắn cờ đình trệ
-- AC: • Biểu tượng chuông hiển thị số thông báo chưa đọc
-- AC: • Bấm vào thông báo mở đúng màn hình liên quan
-- AC: • Thông báo sinh bất đồng bộ, không làm chậm thao tác gốc
-- DB: loai_thong_bao, thong_bao, tac_vu_nen
-- S7-09 [Should 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn khai báo quy tắc tự động hoá đơn giản theo mẫu khi–thì, để quy trình bán hàng tự chạy thay vì phải nhắc nhân viên từng bước.
-- AC: • Ví dụ: khi cơ hội chuyển sang giai đoạn Báo giá thì tự tạo công việc nhắc gọi lại sau 3 ngày
-- AC: • Điều kiện dựa trên thay đổi trạng thái hoặc mốc thời gian
-- AC: • Hành động gồm: tạo công việc, gửi thông báo, đổi người sở hữu
-- AC: • Bật tắt từng quy tắc và xem nhật ký quy tắc đã chạy
-- DB: quy_tac_tu_dong, nhat_ky_quy_tac_tu_dong, tac_vu_nen, cong_viec, thong_bao

-- SPRINT 8
-- S8-01 [Should 3pt]
-- STORY: Là Nhân viên Marketing, tôi muốn soạn mẫu email và gửi email cho khách từ trong hệ thống, để thư gửi khách được ghi vào lịch sử thay vì nằm trong hộp thư cá nhân.
-- AC: • Mẫu chèn được biến: tên người liên hệ, tên công ty, tên nhân viên phụ trách
-- AC: • Email gửi đi được ghi vào dòng thời gian của khách hàng
-- AC: • Gửi bất đồng bộ qua hàng đợi, gửi lại được thư thất bại
-- DB: mau_email, email_gui, hoat_dong, tac_vu_nen
-- S8-02 [Could 2pt]
-- STORY: Là người dùng của hệ thống, tôi muốn bật hoặc tắt từng loại thông báo cho riêng mình, để chỉ nhận những thông báo thực sự liên quan tới công việc của tôi.
-- AC: • Cấu hình riêng cho kênh trong ứng dụng và kênh email
-- AC: • Thông báo bắt buộc như báo giá chờ duyệt thì không tắt được
-- DB: cau_hinh_thong_bao_nguoi_dung, loai_thong_bao
-- S8-03 [Must 8pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn khai báo chỉ tiêu doanh số theo nhân viên, nhóm và kỳ, để đo được kết quả thực tế so với cam kết đầu kỳ.
-- AC: • Đặt chỉ tiêu theo tháng và theo quý, cho từng nhân viên và từng nhóm
-- AC: • Chỉ tiêu của nhóm phải bằng tổng chỉ tiêu các thành viên, lệch thì cảnh báo
-- AC: • Chỉ tiêu đã qua kỳ thì khoá lại, sửa phải có lý do và được ghi nhật ký
-- AC: • Nhân viên chỉ xem được chỉ tiêu của chính mình
-- DB: chi_tieu, nhat_ky_he_thong, nguoi_dung, nhom_kinh_doanh
-- S8-04 [Must 8pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn xem dashboard kinh doanh theo đúng vai trò của mình, để mỗi cấp thấy đúng thứ mình cần quyết định, không nhiều hơn.
-- AC: • Nhân viên thấy: chỉ tiêu và tiến độ của mình, cơ hội đang mở, việc hôm nay, lead chưa liên hệ
-- AC: • Trưởng nhóm thấy thêm: xếp hạng thành viên, cơ hội đình trệ của nhóm, việc quá hạn của nhóm
-- AC: • Giám đốc thấy toàn khối: dự báo so với chỉ tiêu, giá trị pipeline theo giai đoạn, doanh số đã chốt theo tháng
-- AC: • Toàn bộ dữ liệu tải xong dưới 2 giây
-- DB: chi_tieu, co_hoi, cong_viec, lead, hop_dong, lich_su_giai_doan_co_hoi
-- S8-05 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn xem báo cáo phễu và tỷ lệ chuyển đổi từng giai đoạn, để biết nên cải thiện khâu nào để tăng tỷ lệ chốt.
-- AC: • Số cơ hội và giá trị ở từng giai đoạn, tỷ lệ chuyển đổi giữa các giai đoạn
-- AC: • Thời gian trung bình một cơ hội nằm ở mỗi giai đoạn
-- AC: • Chỉ ra giai đoạn rơi rụng nhiều nhất và giai đoạn tắc lâu nhất
-- AC: • Lọc theo nhóm, nhân viên, sản phẩm, khoảng thời gian
-- DB: co_hoi, giai_doan_pipeline, lich_su_giai_doan_co_hoi, co_hoi_san_pham
-- S8-06 [Must 5pt]
-- STORY: Là Giám đốc kinh doanh, tôi muốn xem báo cáo dự báo doanh số và tiến độ chỉ tiêu, để điều chỉnh kịp trong kỳ thay vì biết kết quả khi đã hết quý.
-- AC: • So sánh ba con số: đã chốt, dự báo theo trọng số, chỉ tiêu — theo từng kỳ
-- AC: • Xem theo nhân viên, nhóm và toàn khối
-- AC: • Phân tích lý do thua theo danh mục và theo đối thủ
-- AC: • Xuất Excel
-- DB: chi_tieu, hop_dong, co_hoi, ly_do_thang_thua, doi_thu
-- S8-07 [Should 3pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn xem báo cáo hoạt động và năng suất của nhóm, để biết thành viên nào cần kèm thêm chứ không chỉ nhìn doanh số.
-- AC: • Số cuộc gọi, cuộc gặp, báo giá gửi đi theo từng thành viên và theo tuần
-- AC: • Tỷ lệ lead được phản hồi trong SLA
-- AC: • Xuất Excel
-- DB: hoat_dong, lead, bao_gia, nguoi_dung, nhom_kinh_doanh
-- S8-08 [Could 3pt]
-- STORY: Là Nhân viên kinh doanh, tôi muốn xem bảng xếp hạng doanh số trong nhóm, để biết mình đang đứng đâu so với đồng đội.
-- AC: • Xếp hạng theo doanh số đã chốt trong kỳ, hiển thị tiến độ so với chỉ tiêu cá nhân
-- AC: • Nhân viên chỉ thấy xếp hạng trong nhóm của mình
-- AC: • Bật tắt được tính năng này ở cấp cấu hình
-- DB: chi_tieu, hop_dong, cau_hinh_he_thong, nguoi_dung, nhom_kinh_doanh
-- S8-09 [Should 3pt]
-- STORY: Là Nhân viên Marketing, tôi muốn xem báo cáo hiệu quả từng nguồn lead, để dồn ngân sách vào nguồn thực sự ra doanh thu.
-- AC: • Số lead, tỷ lệ được nhận, tỷ lệ chuyển đổi thành cơ hội theo từng nguồn và từng chiến dịch
-- AC: • Lọc theo khoảng thời gian
-- AC: • Xuất Excel
-- DB: nguon_lead, chien_dich, lead, co_hoi, hop_dong
-- S8-10 [Should 3pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn xuất danh sách cơ hội ra tệp Excel, để làm báo cáo riêng theo mẫu ban giám đốc yêu cầu.
-- AC: • Xuất theo bộ lọc đang áp dụng, gồm cả trường tuỳ chỉnh
-- AC: • Không xuất giá vốn và biên lợi nhuận cho người không có quyền
-- DB: co_hoi, gia_tri_truong_tuy_chinh_co_hoi, truong_tuy_chinh, san_pham
-- S8-11 [Must 2pt]
-- STORY: Là Trưởng nhóm kinh doanh, tôi muốn lọc và xem hoạt động theo loại và theo người thực hiện, để kiểm tra được mức độ chăm sóc khách của từng thành viên.
-- AC: • Lọc theo khoảng thời gian, loại hoạt động, thành viên
-- AC: • Hiển thị tổng số hoạt động theo từng thành viên
-- DB: hoat_dong, loai_hoat_dong, nguoi_dung, nhom_kinh_doanh

-- =====================================================================
-- 13. KIEM TRA NHANH SAU KHI CHAY SCRIPT
-- =====================================================================

SELECT
    ma_phien_ban,
    so_sprint,
    so_user_story,
    tong_story_point,
    trang_thai
FROM phien_ban_schema
WHERE ma_phien_ban = 'CRM-8SPRINT-76STORY-V2-S1-COMPAT';

SELECT COUNT(*) AS so_vai_tro FROM vai_tro;
SELECT COUNT(*) AS so_module FROM module_he_thong;
SELECT COUNT(*) AS so_loai_hoat_dong FROM loai_hoat_dong;
SELECT COUNT(*) AS so_loai_thong_bao FROM loai_thong_bao;

-- =====================================================================
-- HET FILE
-- =====================================================================
