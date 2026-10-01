-- Script cơ sở dữ liệu cho Story S1-03: Đặt lại mật khẩu qua email
-- Bảng lưu trữ token đặt lại mật khẩu
-- AC1: Token có hiệu lực trong 30 phút (thoi_gian_het_han = thoi_gian_tao + 30 phút)
-- AC2: Token chỉ dùng được một lần (da_su_dung = 1 sau khi đổi mật khẩu)
CREATE TABLE IF NOT EXISTS `dat_lai_mat_khau_token` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nguoi_dung_id` BIGINT NOT NULL,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `thoi_gian_tao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `thoi_gian_het_han` DATETIME NOT NULL,
    `da_su_dung` TINYINT(1) NOT NULL DEFAULT 0,
    `thoi_gian_su_dung` DATETIME NULL,
    INDEX `idx_token_value` (`token`),
    INDEX `idx_token_nguoi_dung` (`nguoi_dung_id`),
    CONSTRAINT `fk_token_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
