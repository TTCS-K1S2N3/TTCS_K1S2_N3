-- ====================================================================
-- MIGRATION S1-09: Gán Vai Trò & Nhóm Kinh Doanh
-- Phục vụ:
-- 1. Một người dùng có thể giữ nhiều vai trò cùng lúc (nguoi_dung_vai_tro)
-- 2. Người giữ vai trò Trưởng nhóm phải được gán một nhóm cụ thể
-- 3. Không thể tự thu hồi vai trò quản trị của chính mình
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4)
-- KHÔNG DROP cột/bảng hiện tại. Dùng CREATE TABLE IF NOT EXISTS / INSERT ... ON DUPLICATE KEY UPDATE
-- ====================================================================

-- 1. Bảng Nhóm Kinh Doanh (nếu chưa tồn tại)
CREATE TABLE IF NOT EXISTS `nhom_kinh_doanh` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_nhom` VARCHAR(50) NOT NULL UNIQUE,
    `ten_nhom` VARCHAR(255) NOT NULL,
    `mo_ta` TEXT NULL,
    `nhom_cha_id` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_nhom_kd_cha` FOREIGN KEY (`nhom_cha_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Vai Trò (nếu chưa tồn tại)
CREATE TABLE IF NOT EXISTS `vai_tro` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_vai_tro` VARCHAR(50) NOT NULL UNIQUE,
    `ten_vai_tro` VARCHAR(100) NOT NULL,
    `mo_ta` VARCHAR(255) NULL,
    `pham_vi_toi_da` VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Quan Hệ Nhiều-Nhiều: Người Dùng - Vai Trò (nếu chưa tồn tại)
CREATE TABLE IF NOT EXISTS `nguoi_dung_vai_tro` (
    `nguoi_dung_id` INT NOT NULL,
    `vai_tro_id` INT NOT NULL,
    PRIMARY KEY (`nguoi_dung_id`, `vai_tro_id`),
    CONSTRAINT `fk_nd_vt_user` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_nd_vt_role` FOREIGN KEY (`vai_tro_id`) REFERENCES `vai_tro`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- SEED DATA
-- ====================================================================

-- 7 vai trò chuẩn hệ thống
INSERT INTO `vai_tro` (`id`, `ma_vai_tro`, `ten_vai_tro`, `mo_ta`, `pham_vi_toi_da`) VALUES
(1, 'ADMIN',        'Quản trị hệ thống',        'Admin - Quản lý tài khoản, cấu hình và nhật ký', 'TOAN_BO'),
(2, 'DIRECTOR',     'Giám đốc kinh doanh',       'Director - Chủ sở hữu toàn bộ hoạt động kinh doanh', 'TOAN_BO'),
(3, 'TEAM_LEAD',    'Trưởng nhóm kinh doanh',    'Team Lead - Quản lý nhóm kinh doanh, duyệt chiết khấu', 'NHOM'),
(4, 'SALES_REP',    'Nhân viên kinh doanh',      'Sales Rep - Trực tiếp bán hàng, sở hữu danh mục khách', 'CA_NHAN'),
(5, 'MARKETING',    'Nhân viên Marketing',        'Marketing - Tạo và chăm sóc lead từ các chiến dịch', 'CA_NHAN'),
(6, 'CUST_SUCCESS', 'Chăm sóc khách hàng',       'Cust. Success - Phụ trách khách sau bán, hỗ trợ', 'CA_NHAN'),
(7, 'ACCOUNTANT',   'Kế toán',                   'Accountant - Theo dõi hợp đồng, thanh toán, công nợ', 'TOAN_BO')
ON DUPLICATE KEY UPDATE `ten_vai_tro` = VALUES(`ten_vai_tro`);

-- Nhóm kinh doanh mẫu
INSERT INTO `nhom_kinh_doanh` (`id`, `ma_nhom`, `ten_nhom`, `mo_ta`, `nhom_cha_id`) VALUES
(1, 'KHOI_KD',     'Khối Kinh Doanh Tổng',           'Khối kinh doanh trực thuộc ban giám đốc', NULL),
(2, 'KD_MIEN_BAC', 'Nhóm Kinh Doanh Miền Bắc',       'Phụ trách thị trường phía Bắc', 1),
(3, 'KD_MIEN_NAM', 'Nhóm Kinh Doanh Miền Nam',       'Phụ trách thị trường phía Nam', 1),
(4, 'PHONG_MKT',   'Phòng Marketing',                 'Bộ phận truyền thông và thu hút khách hàng', NULL),
(5, 'PHONG_CSKH',  'Phòng Chăm Sóc Khách Hàng',      'Bộ phận dịch vụ sau bán hàng', NULL),
(6, 'PHONG_KT',    'Phòng Tài Chính Kế Toán',         'Bộ phận tài chính và hợp đồng', NULL)
ON DUPLICATE KEY UPDATE `ten_nhom` = VALUES(`ten_nhom`);

-- Người dùng mẫu (ON DUPLICATE KEY để không xung đột dữ liệu hiện tại)
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `trang_thai`, `nhom_kinh_doanh_id`) VALUES
(1, 'Nguyễn Quản Trị', 'admin@crm.vn',       '123456@Aa', '0901234567', 'HOAT_DONG', NULL),
(2, 'Trần Giám Đốc',   'director@crm.vn',    '123456@Aa', '0902345678', 'HOAT_DONG', 1),
(3, 'Lê Trưởng Nhóm',  'teamlead@crm.vn',    '123456@Aa', '0903456789', 'HOAT_DONG', 2),
(4, 'Thào A Khua',     'sales@crm.vn',       '123456@Aa', '0904567890', 'HOAT_DONG', 2),
(5, 'Phạm Marketing',  'marketing@crm.vn',   '123456@Aa', '0905678901', 'HOAT_DONG', 4),
(6, 'Hoàng CSKH',      'cskh@crm.vn',        '123456@Aa', '0906789012', 'HOAT_DONG', 5),
(7, 'Đỗ Kế Toán',      'accountant@crm.vn',  '123456@Aa', '0907890123', 'HOAT_DONG', 6),
(8, 'Vũ Đa Năng',      'multirole@crm.vn',   '123456@Aa', '0908901234', 'HOAT_DONG', 2)
ON DUPLICATE KEY UPDATE `ho_ten` = VALUES(`ho_ten`);

-- Gán vai trò mẫu (hỗ trợ người dùng giữ nhiều vai trò cùng lúc)
INSERT INTO `nguoi_dung_vai_tro` (`nguoi_dung_id`, `vai_tro_id`) VALUES
(1, 1), -- Admin
(2, 2), -- Director
(3, 3), -- Team Lead
(4, 4), -- Sales Rep
(5, 5), -- Marketing
(6, 6), -- Cust. Success
(7, 7), -- Accountant
(8, 3), -- Vũ Đa Năng: Team Lead
(8, 4)  -- Vũ Đa Năng: Sales Rep (đa vai trò)
ON DUPLICATE KEY UPDATE `vai_tro_id` = VALUES(`vai_tro_id`);
