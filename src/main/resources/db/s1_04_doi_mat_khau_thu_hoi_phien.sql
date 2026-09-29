-- ====================================================================
-- SCRIPT CƠ SỞ DỮ LIỆU CHO STORY S1-04: ĐỔI MẬT KHẨU VÀ THU HỒI PHIÊN
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4)
-- ====================================================================

-- 1. Bảng người dùng hệ thống CRM (nếu chưa có)
CREATE TABLE IF NOT EXISTS `nguoi_dung` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ho_ten` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `mat_khau` VARCHAR(255) NOT NULL,
    `so_dien_thoai` VARCHAR(20) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'HOAT_DONG',
    `so_lan_sai` INT NOT NULL DEFAULT 0,
    `thoi_gian_khoa` DATETIME NULL,
    `ngay_doi_mat_khau` DATETIME NULL,
    `session_version` INT NOT NULL DEFAULT 1,
    `ngay_tao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ngay_cap_nhat` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_nguoi_dung_email` (`email`),
    INDEX `idx_nguoi_dung_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng quản lý phiên đăng nhập người dùng (AC 3: Thu hồi các phiên đăng nhập khác)
CREATE TABLE IF NOT EXISTS `phien_dang_nhap` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nguoi_dung_id` BIGINT NOT NULL,
    `ma_phien` VARCHAR(255) NOT NULL UNIQUE,
    `dia_chi_ip` VARCHAR(50) NULL,
    `thong_tin_thiet_bi` VARCHAR(500) NULL,
    `trang_thai` VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG', -- HOAT_DONG, DA_THU_HOI, HET_HAN
    `thoi_gian_tao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `thoi_gian_hoat_dong_cuoi` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `thoi_gian_thu_hoi` DATETIME NULL,
    INDEX `idx_phien_nguoi_dung` (`nguoi_dung_id`),
    INDEX `idx_phien_ma_phien` (`ma_phien`),
    INDEX `idx_phien_trang_thai` (`trang_thai`),
    CONSTRAINT `fk_phien_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Dữ liệu mẫu khởi tạo người dùng phục vụ kiểm thử (Mật khẩu mặc định: MatKhau@123)
-- Hash BCrypt log_rounds=12 của 'MatKhau@123': $2a$12$uY4Vj83s92E4g1F4xX640OCU.yWdC8tT.WlY5f5k5MvVZZkQ0g4qK
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `trang_thai`, `so_lan_sai`, `ngay_tao`, `ngay_cap_nhat`)
VALUES (1, 'Khoàng Tuấn Hùng', 'sales@crm.vn', '$2a$12$uY4Vj83s92E4g1F4xX640OCU.yWdC8tT.WlY5f5k5MvVZZkQ0g4qK', '0987654321', 'HOAT_DONG', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `ho_ten` = VALUES(`ho_ten`);
