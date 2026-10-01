-- ====================================================================
-- CSDL KHOÁ TÀI KHOẢN VÀ BÀN GIAO DỮ LIỆU KHI NHÂN VIÊN NGHỈ (EP-01 / S1-10)
-- 1. Tài khoản bị khoá không đăng nhập được và bị thu hồi phiên đang mở
-- 2. Bắt buộc chọn người tiếp nhận toàn bộ khách hàng và cơ hội trước khi khoá
-- 3. Việc bàn giao được ghi nhật ký, dữ liệu không bị mất chủ sở hữu
-- Tuân thủ DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4, Transaction)
-- Lưu ý: khach_hang (nguoi_so_huu_id) và co_hoi (nguoi_phu_trach_id) đã được tạo chuẩn ở Story S1-05.
-- ====================================================================

-- 1. Bảng Nhật Ký Bàn Giao Dữ Liệu
CREATE TABLE IF NOT EXISTS `nhat_ky_ban_giao` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nguoi_bi_khoa_id` BIGINT NOT NULL,
    `nguoi_tiep_nhan_id` BIGINT NOT NULL,
    `nguoi_thuc_hien_id` BIGINT NOT NULL,
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
