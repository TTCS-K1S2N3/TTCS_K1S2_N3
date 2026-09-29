-- ====================================================================
-- CƠ SỞ DỮ LIỆU DỰ ÁN CRM - STORY S1-01
-- Đăng nhập bằng email công ty và mật khẩu, bảo mật và khóa tạm 15 phút sau 5 lần sai
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4)
-- ====================================================================

-- 1. Bảng Nhóm Kinh Doanh (Tổ chức dạng cây phân cấp)
CREATE TABLE IF NOT EXISTS `nhom_kinh_doanh` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_nhom` VARCHAR(50) NOT NULL UNIQUE,
    `ten_nhom` VARCHAR(255) NOT NULL,
    `mo_ta` TEXT NULL,
    `nhom_cha_id` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_nhom_kinh_doanh_cha` FOREIGN KEY (`nhom_cha_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Vai Trò (7 vai trò chuẩn của hệ thống CRM)
CREATE TABLE IF NOT EXISTS `vai_tro` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_vai_tro` VARCHAR(50) NOT NULL UNIQUE,
    `ten_vai_tro` VARCHAR(100) NOT NULL,
    `mo_ta` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Người Dùng (Hỗ trợ theo dõi số lần sai và thời gian khóa tạm)
CREATE TABLE IF NOT EXISTS `nguoi_dung` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ho_ten` VARCHAR(150) NOT NULL,
    `email` VARCHAR(200) NOT NULL UNIQUE,
    `mat_khau` VARCHAR(255) NOT NULL,
    `so_dien_thoai` VARCHAR(20) NULL,
    `trang_thai` VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG',
    `so_lan_sai` INT NOT NULL DEFAULT 0,
    `thoi_gian_khoa` DATETIME NULL,
    `nhom_kinh_doanh_id` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_nguoi_dung_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL,
    INDEX `idx_nguoi_dung_email` (`email`),
    INDEX `idx_nguoi_dung_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_nguoi_dung_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Quan Hệ Nhiều - Nhiều Người Dùng - Vai Trò
CREATE TABLE IF NOT EXISTS `nguoi_dung_vai_tro` (
    `nguoi_dung_id` BIGINT NOT NULL,
    `vai_tro_id` INT NOT NULL,
    PRIMARY KEY (`nguoi_dung_id`, `vai_tro_id`),
    CONSTRAINT `fk_ndvt_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ndvt_vai_tro` FOREIGN KEY (`vai_tro_id`) REFERENCES `vai_tro`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- SEED DATA (DỮ LIỆU KHỞI TẠO MẪU)
-- ====================================================================

-- Chèn 7 vai trò chuẩn
INSERT INTO `vai_tro` (`id`, `ma_vai_tro`, `ten_vai_tro`, `mo_ta`) VALUES
(1, 'ADMIN', 'Quản trị hệ thống', 'Admin - Quản lý tài khoản, cấu hình và nhật ký'),
(2, 'DIRECTOR', 'Giám đốc kinh doanh', 'Director - Chủ sở hữu toàn bộ hoạt động kinh doanh'),
(3, 'TEAM_LEAD', 'Trưởng nhóm kinh doanh', 'Team Lead - Quản lý nhóm kinh doanh, duyệt chiết khấu'),
(4, 'SALES_REP', 'Nhân viên kinh doanh', 'Sales Rep - Trực tiếp bán hàng, sở hữu danh mục khách'),
(5, 'MARKETING', 'Nhân viên Marketing', 'Marketing - Tạo và chăm sóc lead từ các chiến dịch'),
(6, 'CUST_SUCCESS', 'Chăm sóc khách hàng', 'Cust. Success - Phụ trách khách sau bán, hỗ trợ'),
(7, 'ACCOUNTANT', 'Kế toán', 'Accountant - Theo dõi hợp đồng, thanh toán, công nợ')
ON DUPLICATE KEY UPDATE `ten_vai_tro` = VALUES(`ten_vai_tro`);

-- Chèn các nhóm kinh doanh mẫu
INSERT INTO `nhom_kinh_doanh` (`id`, `ma_nhom`, `ten_nhom`, `mo_ta`, `nhom_cha_id`) VALUES
(1, 'KHOI_KD', 'Khối Kinh Doanh Tổng', 'Khối kinh doanh trực thuộc ban giám đốc', NULL),
(2, 'KD_MIEN_BAC', 'Nhóm Kinh Doanh Miền Bắc', 'Phụ trách thị trường phía Bắc', 1),
(3, 'KD_MIEN_NAM', 'Nhóm Kinh Doanh Miền Nam', 'Phụ trách thị trường phía Nam', 1),
(4, 'PHONG_MKT', 'Phòng Marketing', 'Bộ phận truyền thông và thu hút khách hàng', NULL),
(5, 'PHONG_CSKH', 'Phòng Chăm Sóc Khách Hàng', 'Bộ phận dịch vụ sau bán hàng', NULL),
(6, 'PHONG_KT', 'Phòng Tài Chính Kế Toán', 'Bộ phận tài chính và hợp đồng', NULL)
ON DUPLICATE KEY UPDATE `ten_nhom` = VALUES(`ten_nhom`);

-- Mật khẩu mặc định cho các tài khoản mẫu là: 123456@Aa
-- Chuỗi hash BCrypt (rounds=12): $2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `trang_thai`, `so_lan_sai`, `thoi_gian_khoa`, `nhom_kinh_doanh_id`) VALUES
(1, 'Nguyễn Quản Trị', 'admin@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0901234567', 'HOAT_DONG', 0, NULL, NULL),
(2, 'Trần Giám Đốc', 'director@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0902345678', 'HOAT_DONG', 0, NULL, 1),
(3, 'Lê Trưởng Nhóm', 'teamlead@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0903456789', 'HOAT_DONG', 0, NULL, 2),
(4, 'Phan Duy Hưng', 'sales@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0904567890', 'HOAT_DONG', 0, NULL, 2),
(5, 'Phạm Marketing', 'marketing@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0905678901', 'HOAT_DONG', 0, NULL, 4),
(6, 'Hoàng CSKH', 'cskh@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0906789012', 'HOAT_DONG', 0, NULL, 5),
(7, 'Đỗ Kế Toán', 'accountant@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0907890123', 'HOAT_DONG', 0, NULL, 6),
(8, 'Tài Khoản Bị Khóa', 'locked@crm.vn', '$2a$12$mBj7Zkgc3Nou2Odwz/LozeNA2tJiBuksCZLzrtmGWYfmlrPa/hs2e', '0908888888', 'KHOA', 0, NULL, 2)
ON DUPLICATE KEY UPDATE `mat_khau` = VALUES(`mat_khau`), `ho_ten` = VALUES(`ho_ten`);

-- Gán vai trò cho người dùng
INSERT INTO `nguoi_dung_vai_tro` (`nguoi_dung_id`, `vai_tro_id`) VALUES
(1, 1), -- Admin
(2, 2), -- Director
(3, 3), -- Team Lead
(4, 4), -- Sales Rep (Phan Duy Hưng)
(5, 5), -- Marketing
(6, 6), -- Cust. Success
(7, 7), -- Accountant
(8, 4)  -- Locked user
ON DUPLICATE KEY UPDATE `vai_tro_id` = VALUES(`vai_tro_id`);
