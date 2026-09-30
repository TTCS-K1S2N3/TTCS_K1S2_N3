-- ====================================================================
-- CƠ SỞ DỮ LIỆU DỰ ÁN CRM - STORY S1-05
-- Phân quyền vừa theo vai trò vừa theo dữ liệu sở hữu (Data Scope)
-- Ba phạm vi: CA_NHAN (Của tôi), NHOM (Của nhóm tôi), TOAN_BO (Tất cả)
-- Áp dụng cho 4 đối tượng: Khách hàng, Cơ hội, Hoạt động, Báo giá
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4)
-- ====================================================================

-- 1. Bảng Nhóm Kinh Doanh (Tổ chức dạng cây phân cấp)
CREATE TABLE IF NOT EXISTS `nhom_kinh_doanh` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_nhom` VARCHAR(50) NOT NULL UNIQUE,
    `ten_nhom` VARCHAR(150) NOT NULL,
    `mo_ta` TEXT NULL,
    `nhom_cha_id` BIGINT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_nkd_nhom_cha` FOREIGN KEY (`nhom_cha_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Vai Trò (Vai trò người dùng trong hệ thống)
CREATE TABLE IF NOT EXISTS `vai_tro` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_vai_tro` VARCHAR(50) NOT NULL UNIQUE,
    `ten_vai_tro` VARCHAR(100) NOT NULL,
    `pham_vi_toi_da` VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN',
    `mo_ta` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Người Dùng
CREATE TABLE IF NOT EXISTS `nguoi_dung` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ho_ten` VARCHAR(150) NOT NULL,
    `email` VARCHAR(200) NOT NULL UNIQUE,
    `mat_khau` VARCHAR(255) NOT NULL,
    `so_dien_thoai` VARCHAR(20) NULL,
    `vai_tro` VARCHAR(50) NOT NULL DEFAULT 'SALES_REP',
    `nhom_kinh_doanh_id` BIGINT NULL,
    `trang_thai` VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_nd_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL,
    INDEX `idx_nd_email` (`email`),
    INDEX `idx_nd_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_nd_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Khách Hàng (Customer)
CREATE TABLE IF NOT EXISTS `khach_hang` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` BIGINT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'HOAT_DONG',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_kh_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_kh_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_kh_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_kh_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`),
    CONSTRAINT `fk_kh_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Cơ Hội Bán Hàng (Opportunity)
CREATE TABLE IF NOT EXISTS `co_hoi` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` BIGINT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'KHAO_SAT',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_ch_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_ch_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_ch_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_ch_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`),
    CONSTRAINT `fk_ch_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng Báo Giá (Quote)
CREATE TABLE IF NOT EXISTS `bao_gia` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `co_hoi_id` BIGINT NULL,
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` BIGINT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'KHOI_TAO',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_bg_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_bg_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_bg_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_bg_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`),
    CONSTRAINT `fk_bg_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng Hoạt Động Chăm Sóc (Activity)
CREATE TABLE IF NOT EXISTS `hoat_dong` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ma_ban_ghi` VARCHAR(50) NOT NULL UNIQUE,
    `tieu_de` VARCHAR(255) NOT NULL,
    `loai_hoat_dong` VARCHAR(50) NOT NULL DEFAULT 'CUOC_GOI',
    `khach_hang_id` BIGINT NULL,
    `nguoi_phu_trach_id` BIGINT NOT NULL,
    `nhom_kinh_doanh_id` BIGINT NULL,
    `gia_tri` VARCHAR(100) NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'HOAN_THANH',
    `mo_ta_chi_tiet` TEXT NULL,
    `ngay_tao` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_hd_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_hd_nhom` (`nhom_kinh_doanh_id`),
    INDEX `idx_hd_tieu_de` (`tieu_de`),
    CONSTRAINT `fk_hd_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`),
    CONSTRAINT `fk_hd_nhom` FOREIGN KEY (`nhom_kinh_doanh_id`) REFERENCES `nhom_kinh_doanh`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- SEED DATA MẪU STORY S1-05
-- ====================================================================

-- Nhóm kinh doanh
INSERT INTO `nhom_kinh_doanh` (`id`, `ma_nhom`, `ten_nhom`, `mo_ta`, `nhom_cha_id`) VALUES
(1, 'KD_MIEN_BAC', 'Nhóm Miền Bắc', 'Khu vực Hà Nội và các tỉnh phía Bắc', NULL),
(2, 'KD_MIEN_NAM', 'Nhóm Miền Nam', 'Khu vực TP.HCM và các tỉnh phía Nam', NULL)
ON DUPLICATE KEY UPDATE `ten_nhom` = VALUES(`ten_nhom`);

-- Vai trò
INSERT INTO `vai_tro` (`id`, `ma_vai_tro`, `ten_vai_tro`, `pham_vi_toi_da`, `mo_ta`) VALUES
(1, 'SALES_REP', 'Nhân viên kinh doanh', 'CA_NHAN', 'Chỉ xem dữ liệu do chính mình phụ trách'),
(2, 'TEAM_LEAD', 'Trưởng nhóm kinh doanh', 'NHOM', 'Xem dữ liệu thuộc nhóm quản lý'),
(3, 'DIRECTOR', 'Giám đốc kinh doanh', 'TOAN_BO', 'Xem toàn bộ dữ liệu trên toàn hệ thống'),
(4, 'ADMIN', 'Quản trị hệ thống', 'TOAN_BO', 'Toàn quyền cấu hình và xem dữ liệu')
ON DUPLICATE KEY UPDATE `ten_vai_tro` = VALUES(`ten_vai_tro`);

-- Người dùng
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `vai_tro`, `nhom_kinh_doanh_id`, `trang_thai`) VALUES
(1, 'Bàn Thị Linh (Giám đốc)', 'linh.ban@crm.vn', '123456@Aa', '0901111111', 'DIRECTOR', NULL, 'HOAT_DONG'),
(100, 'Lê Thị Trưởng Nhóm', 'lead.bac@crm.vn', '123456@Aa', '0902222222', 'TEAM_LEAD', 1, 'HOAT_DONG'),
(101, 'Nguyễn Văn A (Sales)', 'sales.a@crm.vn', '123456@Aa', '0903333333', 'SALES_REP', 1, 'HOAT_DONG'),
(102, 'Trần Thị B (Sales)', 'sales.b@crm.vn', '123456@Aa', '0904444444', 'SALES_REP', 1, 'HOAT_DONG'),
(201, 'Lê Văn C (Sales HCM)', 'sales.c@crm.vn', '123456@Aa', '0905555555', 'SALES_REP', 2, 'HOAT_DONG')
ON DUPLICATE KEY UPDATE `ho_ten` = VALUES(`ho_ten`);

-- Khách hàng
INSERT INTO `khach_hang` (`id`, `ma_ban_ghi`, `tieu_de`, `nguoi_phu_trach_id`, `nhom_kinh_doanh_id`, `gia_tri`, `trang_thai`, `mo_ta_chi_tiet`, `ngay_tao`) VALUES
(1, 'KH-001', 'Công ty Cổ phần Công nghệ FPT', 101, 1, 'Khách hàng VIP', 'Đang hợp tác', 'Khách hàng doanh nghiệp công nghệ lớn tại Hà Nội.', CURRENT_DATE - INTERVAL 15 DAY),
(5, 'KH-002', 'Tập đoàn Viễn thông Viettel', 102, 1, 'Khách hàng trọng điểm', 'Tiềm năng', 'Khách hàng quy mô tập đoàn viễn thông.', CURRENT_DATE - INTERVAL 20 DAY),
(9, 'KH-003', 'Công ty TNHH VNG Corporation', 201, 2, 'Khách hàng chiến lược', 'Đang hợp tác', 'Khách hàng Internet và Game tại TP.HCM.', CURRENT_DATE - INTERVAL 25 DAY)
ON DUPLICATE KEY UPDATE `tieu_de` = VALUES(`tieu_de`);

-- Cơ hội
INSERT INTO `co_hoi` (`id`, `ma_ban_ghi`, `tieu_de`, `khach_hang_id`, `nguoi_phu_trach_id`, `nhom_kinh_doanh_id`, `gia_tri`, `trang_thai`, `mo_ta_chi_tiet`, `ngay_tao`) VALUES
(2, 'CH-101', 'Triển khai hệ thống Cloud CRM cho FPT', 1, 101, 1, '850,000,000 đ', 'Đàm phán hợp đồng', 'Giai đoạn chốt điều khoản thanh toán.', CURRENT_DATE - INTERVAL 10 DAY),
(6, 'CH-102', 'Dự án CRM Bán hàng cho Trung tâm Viettel IDC', 5, 102, 1, '1,200,000,000 đ', 'Khảo sát nhu cầu', 'Cơ hội giá trị cao đang phối hợp kỹ thuật.', CURRENT_DATE - INTERVAL 8 DAY),
(10, 'CH-103', 'Nâng cấp hạ tầng CRM cho VNG Campus', 9, 201, 2, '650,000,000 đ', 'Đã ký hợp đồng', 'Thương vụ đã thắng trong tháng.', CURRENT_DATE - INTERVAL 12 DAY)
ON DUPLICATE KEY UPDATE `tieu_de` = VALUES(`tieu_de`);

-- Báo giá
INSERT INTO `bao_gia` (`id`, `ma_ban_ghi`, `tieu_de`, `co_hoi_id`, `khach_hang_id`, `nguoi_phu_trach_id`, `nhom_kinh_doanh_id`, `gia_tri`, `trang_thai`, `mo_ta_chi_tiet`, `ngay_tao`) VALUES
(3, 'BG-201', 'Báo giá gói Enterprise 100 User - FPT', 2, 1, 101, 1, '850,000,000 đ', 'Đã gửi khách', 'Báo giá chiết khấu 10% đã được phê duyệt.', CURRENT_DATE - INTERVAL 5 DAY),
(7, 'BG-202', 'Báo giá gói Hạ tầng dữ liệu Viettel IDC', 6, 5, 102, 1, '1,200,000,000 đ', 'Chờ duyệt chiết khấu', 'Xin chiết khấu vượt ngưỡng 15%.', CURRENT_DATE - INTERVAL 4 DAY),
(11, 'BG-203', 'Báo giá gia hạn bảo trì thường niên VNG', 10, 9, 201, 2, '150,000,000 đ', 'Khách đã chấp nhận', 'Đã bàn giao hợp đồng cho kế toán.', CURRENT_DATE - INTERVAL 6 DAY)
ON DUPLICATE KEY UPDATE `tieu_de` = VALUES(`tieu_de`);

-- Hoạt động
INSERT INTO `hoat_dong` (`id`, `ma_ban_ghi`, `tieu_de`, `loai_hoat_dong`, `khach_hang_id`, `nguoi_phu_trach_id`, `nhom_kinh_doanh_id`, `gia_tri`, `trang_thai`, `mo_ta_chi_tiet`, `ngay_tao`) VALUES
(4, 'HD-301', 'Cuộc họp demo tính năng bảo mật với Giám đốc IT FPT', 'HOP_TRUC_TIEP', 1, 101, 1, 'Họp trực tiếp', 'Hoàn thành', 'Khách hàng rất hài lòng về phân quyền đa cấp.', CURRENT_DATE - INTERVAL 2 DAY),
(8, 'HD-302', 'Gọi điện trao đổi yêu cầu bảo mật với Viettel', 'CUOC_GOI', 5, 102, 1, 'Cuộc gọi', 'Hoàn thành', 'Đã thống nhất gửi tài liệu kỹ thuật.', CURRENT_DATE - INTERVAL 1 DAY),
(12, 'HD-303', 'Gặp mặt đánh giá chất lượng dịch vụ định kỳ VNG', 'HOP_TRUC_TIEP', 9, 201, 2, 'Gặp trực tiếp', 'Hoàn thành', 'Khách hàng phản hồi rất tích cực.', CURRENT_DATE - INTERVAL 3 DAY)
ON DUPLICATE KEY UPDATE `tieu_de` = VALUES(`tieu_de`);
