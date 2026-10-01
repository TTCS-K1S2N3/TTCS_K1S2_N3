-- ====================================================================
-- CƠ SỞ DỮ LIỆU DỰ ÁN CRM - STORY S1-05
-- Phân quyền vừa theo vai trò vừa theo dữ liệu sở hữu (Data Scope)
-- Ba phạm vi: CA_NHAN (Của tôi), NHOM (Của nhóm tôi), TOAN_BO (Tất cả)
-- Áp dụng cho 4 đối tượng: Khách hàng, Cơ hội, Hoạt động, Báo giá
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4, DECIMAL cho tiền tệ)
-- ====================================================================

-- 1. Cập nhật phạm vi tối đa cho bảng vai_tro (đã tạo ở S1-02)
ALTER TABLE `vai_tro` ADD COLUMN `pham_vi_toi_da` VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN';

UPDATE `vai_tro` SET `pham_vi_toi_da` = 'TOAN_BO' WHERE `ma_vai_tro` IN ('ADMIN', 'DIRECTOR', 'ACCOUNTANT');
UPDATE `vai_tro` SET `pham_vi_toi_da` = 'NHOM' WHERE `ma_vai_tro` = 'TEAM_LEAD';
UPDATE `vai_tro` SET `pham_vi_toi_da` = 'CA_NHAN' WHERE `ma_vai_tro` IN ('SALES_REP', 'MARKETING', 'CUST_SUCCESS');

-- 2. Bảng Khách Hàng Doanh Nghiệp (Customer)
CREATE TABLE IF NOT EXISTS `khach_hang` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_khach_hang` VARCHAR(50) NULL UNIQUE,
    `ten_cong_ty` VARCHAR(255) NOT NULL,
    `ma_so_thue` VARCHAR(50) NULL,
    `nguoi_so_huu_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `doanh_thu_uoc_tinh` DECIMAL(15, 2) NULL DEFAULT 0.00,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'TIEM_NANG',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL DEFAULT (CURRENT_DATE),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_kh_nguoi_so_huu` (`nguoi_so_huu_id`),
    INDEX `idx_kh_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_kh_ten_cong_ty` (`ten_cong_ty`),
    INDEX `idx_kh_trang_thai` (`trang_thai`),
    CONSTRAINT `fk_kh_nguoi_so_huu` FOREIGN KEY (`nguoi_so_huu_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_kh_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Cơ Hội Bán Hàng (Opportunity)
CREATE TABLE IF NOT EXISTS `co_hoi` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_co_hoi` VARCHAR(50) NOT NULL UNIQUE,
    `ten_co_hoi` VARCHAR(255) NOT NULL,
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `gia_tri_du_kien` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `xac_suat` INT NOT NULL DEFAULT 0,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'MO',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL DEFAULT (CURRENT_DATE),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_ch_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_ch_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_ch_khach_hang` (`khach_hang_id`),
    INDEX `idx_ch_trang_thai` (`trang_thai`),
    CONSTRAINT `fk_ch_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_ch_khach_hang` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_ch_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Báo Giá (Quote)
CREATE TABLE IF NOT EXISTS `bao_gia` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_bao_gia` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `co_hoi_id` BIGINT NULL,
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `phien_ban` INT NOT NULL DEFAULT 1,
    `tong_tien` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'CHO_DUYET',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL DEFAULT (CURRENT_DATE),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_bg_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_bg_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_bg_co_hoi` (`co_hoi_id`),
    INDEX `idx_bg_khach_hang` (`khach_hang_id`),
    INDEX `idx_bg_trang_thai` (`trang_thai`),
    CONSTRAINT `fk_bg_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_bg_co_hoi` FOREIGN KEY (`co_hoi_id`) REFERENCES `co_hoi`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_bg_khach_hang` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_bg_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Hoạt Động Chăm Sóc (Activity)
CREATE TABLE IF NOT EXISTS `hoat_dong` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_hoat_dong` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `loai_hoat_dong` VARCHAR(50) NOT NULL DEFAULT 'CUOC_GOI',
    `khach_hang_id` BIGINT NULL,
    `co_hoi_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `chi_phi` DECIMAL(15, 2) NULL DEFAULT 0.00,
    `thoi_gian_bat_dau` DATETIME NULL,
    `thoi_gian_ket_thuc` DATETIME NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'HOAN_THANH',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL DEFAULT (CURRENT_DATE),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_hd_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_hd_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_hd_khach_hang` (`khach_hang_id`),
    INDEX `idx_hd_co_hoi` (`co_hoi_id`),
    INDEX `idx_hd_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_hd_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_hd_khach_hang` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_hd_co_hoi` FOREIGN KEY (`co_hoi_id`) REFERENCES `co_hoi`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_hd_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
