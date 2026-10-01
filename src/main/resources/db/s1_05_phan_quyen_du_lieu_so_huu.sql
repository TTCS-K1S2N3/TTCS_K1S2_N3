-- ====================================================================
-- CƠ SỞ DỮ LIỆU DỰ ÁN CRM - STORY S1-05
-- Phân quyền vừa theo vai trò vừa theo dữ liệu sở hữu (Data Scope)
-- Ba phạm vi: CA_NHAN (Của tôi), NHOM (Của nhóm tôi), TOAN_BO (Tất cả)
-- Áp dụng cho 4 đối tượng: Khách hàng, Cơ hội, Hoạt động, Báo giá
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4)
-- ====================================================================

-- 1. Cập nhật phạm vi tối đa cho bảng vai_tro (đã tạo ở S1-02)
ALTER TABLE `vai_tro` ADD COLUMN IF NOT EXISTS `pham_vi_toi_da` VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN';

UPDATE `vai_tro` SET `pham_vi_toi_da` = 'TOAN_BO' WHERE `ma_vai_tro` IN ('ADMIN', 'DIRECTOR', 'ACCOUNTANT');
UPDATE `vai_tro` SET `pham_vi_toi_da` = 'NHOM' WHERE `ma_vai_tro` = 'TEAM_LEAD';
UPDATE `vai_tro` SET `pham_vi_toi_da` = 'CA_NHAN' WHERE `ma_vai_tro` IN ('SALES_REP', 'MARKETING', 'CUST_SUCCESS');

-- 2. Bảng Khách Hàng (Customer)
CREATE TABLE IF NOT EXISTS `khach_hang` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'HOAT_DONG',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_kh_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_kh_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_kh_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_kh_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_kh_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Cơ Hội Bán Hàng (Opportunity)
CREATE TABLE IF NOT EXISTS `co_hoi` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'MOI_TAO',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_ch_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_ch_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_ch_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_ch_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ch_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Báo Giá (Quote)
CREATE TABLE IF NOT EXISTS `bao_gia` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `co_hoi_id` BIGINT NULL,
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'KHOI_TAO',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_bg_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_bg_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_bg_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_bg_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bg_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Hoạt Động Chăm Sóc (Activity)
CREATE TABLE IF NOT EXISTS `hoat_dong` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `loai_hoat_dong` VARCHAR(50) NOT NULL DEFAULT 'CUOC_GOI',
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'HOAN_THANH',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_hd_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_hd_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_hd_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_hd_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_hd_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
