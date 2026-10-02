package vn.nhom10.crm.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Quản lý phiên đăng nhập trong bảng phien_dang_nhap.
 */
public class PhienDangNhapDAO {

    private static final Logger logger = LoggerFactory.getLogger(PhienDangNhapDAO.class);

    /**
     * Tạo bản ghi phiên đăng nhập mới trong cơ sở dữ liệu.
     */
    public boolean taoPhien(Long nguoiDungId, String maPhienHash, int sessionVersion,
                            String diaChiIp, String thongTinThietBi, int phutHetHan) {
        String sql = "INSERT INTO phien_dang_nhap (nguoi_dung_id, ma_phien_hash, session_version, " +
                     "dia_chi_ip, thong_tin_thiet_bi, trang_thai, het_han_luc) " +
                     "VALUES (?, ?, ?, ?, ?, 'HOAT_DONG', DATE_ADD(NOW(), INTERVAL ? MINUTE))";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setString(2, maPhienHash);
            ps.setInt(3, sessionVersion);
            ps.setString(4, diaChiIp);
            ps.setString(5, thongTinThietBi);
            ps.setInt(6, phutHetHan > 0 ? phutHetHan : 30);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi tạo phiên đăng nhập cho user {}: {}", nguoiDungId, e.getMessage());
            return false;
        }
    }

    /**
     * Thu hồi một phiên đăng nhập cụ thể theo mã băm phiên.
     */
    public boolean thuHoiPhien(String maPhienHash, String lyDo) {
        String sql = "UPDATE phien_dang_nhap SET trang_thai = 'THU_HOI', thu_hoi_luc = NOW(), ly_do_thu_hoi = ? " +
                     "WHERE ma_phien_hash = ? AND trang_thai = 'HOAT_DONG'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lyDo);
            ps.setString(2, maPhienHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi thu hồi phiên: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thời điểm hoạt động cuối cùng của phiên.
     */
    public boolean capNhatHoatDongCuoi(String maPhienHash) {
        String sql = "UPDATE phien_dang_nhap SET hoat_dong_cuoi_luc = NOW() WHERE ma_phien_hash = ? AND trang_thai = 'HOAT_DONG'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhienHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi cập nhật hoạt động cuối của phiên: {}", e.getMessage());
            return false;
        }
    }
}
