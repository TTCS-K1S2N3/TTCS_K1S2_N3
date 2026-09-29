-- ====================================================================
-- CSDL KHOÁ TÀI KHOẢN VÀ BÀN GIAO DỮ LIỆU KHI NHÂN VIÊN NGHỈ (EP-01 / S1-10)
-- 1. Tài khoản bị khoá không đăng nhập được và bị thu hồi phiên đang mở
-- 2. Bắt buộc chọn người tiếp nhận toàn bộ khách hàng và cơ hội trước khi khoá
-- 3. Việc bàn giao được ghi nhật ký, dữ liệu không bị mất chủ sở hữu
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4, Transaction)
-- ====================================================================

-- 1. Bảng Khách Hàng Doanh Nghiệp (nếu chưa tồn tại)
CREATE TABLE IF NOT EXISTS `khach_hang` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ten_cong_ty` VARCHAR(255) NOT NULL,
    `ma_so_thue` VARCHAR(50) NULL,
    `nganh_nghe` VARCHAR(100) NULL,
    `quy_mo` VARCHAR(100) NULL,
    `website` VARCHAR(255) NULL,
    `dia_chi` VARCHAR(255) NULL,
    `nguoi_so_huu_id` INT NOT NULL,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'TIEM_NANG',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_kh_nguoi_so_huu` FOREIGN KEY (`nguoi_so_huu_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE RESTRICT,
    INDEX `idx_kh_nguoi_so_huu` (`nguoi_so_huu_id`),
    INDEX `idx_kh_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Cơ Hội Bán Hàng (nếu chưa tồn tại)
CREATE TABLE IF NOT EXISTS `co_hoi` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ma_co_hoi` VARCHAR(50) NOT NULL UNIQUE,
    `ten_co_hoi` VARCHAR(255) NOT NULL,
    `khach_hang_id` INT NULL,
    `nguoi_phu_trach_id` INT NOT NULL,
    `gia_tri_du_kien` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `xac_suat` INT NOT NULL DEFAULT 0,
    `trang_thai` VARCHAR(50) NOT NULL DEFAULT 'MO',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ch_khach_hang` FOREIGN KEY (`khach_hang_id`) REFERENCES `khach_hang`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_ch_nguoi_phu_trach` FOREIGN KEY (`nguoi_phu_trach_id`) REFERENCES `nguoi_dung`(`id`) ON DELETE RESTRICT,
    INDEX `idx_ch_nguoi_phu_trach` (`nguoi_phu_trach_id`),
    INDEX `idx_ch_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Nhật Ký Bàn Giao Dữ Liệu
CREATE TABLE IF NOT EXISTS `nhat_ky_ban_giao` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nguoi_bi_khoa_id` INT NOT NULL,
    `nguoi_tiep_nhan_id` INT NOT NULL,
    `nguoi_thuc_hien_id` INT NOT NULL,
    `so_khach_hang_chuyen` INT NOT NULL DEFAULT 0,
    `so_co_hoi_chuyen` INT NOT NULL DEFAULT 0,
    `ly_do` VARCHAR(500) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_nkbg_nguoi_bi_khoa` FOREIGN KEY (`nguoi_bi_khoa_id`) REFERENCES `nguoi_dung`(`id`),
    CONSTRAINT `fk_nkbg_nguoi_tiep_nhan` FOREIGN KEY (`nguoi_tiep_nhan_id`) REFERENCES `nguoi_dung`(`id`),
    CONSTRAINT `fk_nkbg_nguoi_thuc_hien` FOREIGN KEY (`nguoi_thuc_hien_id`) REFERENCES `nguoi_dung`(`id`),
    INDEX `idx_nkbg_nguoi_bi_khoa` (`nguoi_bi_khoa_id`),
    INDEX `idx_nkbg_nguoi_tiep_nhan` (`nguoi_tiep_nhan_id`),
    INDEX `idx_nkbg_nguoi_thuc_hien` (`nguoi_thuc_hien_id`),
    INDEX `idx_nkbg_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- SEED DATA (DỮ LIỆU MẪU KIỂM THỬ BÀN GIAO & KHOÁ TÀI KHOẢN)
-- ====================================================================

-- Thêm tài khoản nhân viên sắp nghỉ việc để kiểm thử
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `trang_thai`, `nhom_kinh_doanh_id`) VALUES
(9, 'Nguyễn Văn Nghỉ Việc', 'nhanvien_nghi@crm.vn', '123456@Aa', '0912345678', 'HOAT_DONG', 2)
ON DUPLICATE KEY UPDATE `ho_ten` = VALUES(`ho_ten`);

-- Gán vai trò Sales Rep cho nhân viên sắp nghỉ
INSERT INTO `nguoi_dung_vai_tro` (`nguoi_dung_id`, `vai_tro_id`) VALUES
(9, 4)
ON DUPLICATE KEY UPDATE `vai_tro_id` = VALUES(`vai_tro_id`);

-- Dữ liệu khách hàng mẫu của nhân viên sắp nghỉ (id = 9)
INSERT INTO `khach_hang` (`id`, `ten_cong_ty`, `ma_so_thue`, `nganh_nghe`, `quy_mo`, `website`, `dia_chi`, `nguoi_so_huu_id`, `trang_thai`) VALUES
(101, 'Công ty TNHH Giải Pháp Công Nghệ Sao Mai', '0108991234', 'Công nghệ thông tin', '50-100 người', 'https://saomai-tech.vn', 'Tòa nhà Sao Mai, Cầu Giấy, Hà Nội', 9, 'TIEM_NANG'),
(102, 'Tập đoàn Dược Phẩm An Khang', '0107884321', 'Dược phẩm & Y tế', '100-200 người', 'https://ankhang-pharma.vn', 'Số 45 Lê Duẩn, Hoàn Kiếm, Hà Nội', 9, 'DANG_GIAO_DICH'),
(103, 'Công ty Cổ phần Vận Tải Biển Á Châu', '0309112233', 'Logistics & Vận tải', '200-500 người', 'https://achau-logistics.vn', '88 Cảng Sài Gòn, Quận 4, TP.HCM', 9, 'TIEM_NANG')
ON DUPLICATE KEY UPDATE `nguoi_so_huu_id` = VALUES(`nguoi_so_huu_id`);

-- Dữ liệu cơ hội mẫu của nhân viên sắp nghỉ (id = 9)
INSERT INTO `co_hoi` (`id`, `ma_co_hoi`, `ten_co_hoi`, `khach_hang_id`, `nguoi_phu_trach_id`, `gia_tri_du_kien`, `xac_suat`, `trang_thai`) VALUES
(201, 'CH-SAOMAI-01', 'Gói phần mềm CRM Cloud - Sao Mai Tech', 101, 9, 85000000.00, 40, 'MO'),
(202, 'CH-ANKHANG-02', 'Dự án Chuyển đổi số Dược Phẩm An Khang', 102, 9, 240000000.00, 75, 'MO')
ON DUPLICATE KEY UPDATE `nguoi_phu_trach_id` = VALUES(`nguoi_phu_trach_id`);
