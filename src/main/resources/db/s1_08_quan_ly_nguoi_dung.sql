-- ====================================================================
-- CSDL PHAN HE QUAN TRI NGUOI DUNG VA CAP QUYEN (EP-01 / S1-08)
-- Story: S1-08: Tao, sua va tim kiem tai khoan nguoi dung, cap quyen cho NVKD
-- Tuan thu DATABASE_RULES.md (MySQL 8.4 LTS, snake_case, UTF8MB4)
-- Migration nay thich ung voi schema baseline hien tai (S1-01 -> S1-05).
-- KHONG tao lai bang nguoi_dung, vai_tro, nhom_kinh_doanh da ton tai.
-- ====================================================================

-- Kiem tra va bo sung index ho tro tim kiem ho_ten neu chua co
-- (Tren MySQL 8.4 dung procedure an toan tranh loi duplicate index)
DELIMITER $$
CREATE PROCEDURE sp_s1_08_add_index_ho_ten()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
          AND TABLE_NAME = 'nguoi_dung' 
          AND INDEX_NAME = 'idx_nguoi_dung_ho_ten'
    ) THEN
        ALTER TABLE `nguoi_dung` ADD INDEX `idx_nguoi_dung_ho_ten` (`ho_ten`);
    END IF;
END$$
DELIMITER ;

CALL sp_s1_08_add_index_ho_ten();
DROP PROCEDURE IF EXISTS sp_s1_08_add_index_ho_ten;
