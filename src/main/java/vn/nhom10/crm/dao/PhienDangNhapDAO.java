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

    /**
     * S1-02-AC1: Gia hạn phiên tự động khi còn hoạt động.
     * Cập nhật thời điểm hoạt động cuối và đẩy lùi thời điểm hết hạn tương ứng.
     */
    public boolean giaHanPhien(String maPhienHash, int phutGiaHan) {
        if (maPhienHash == null || maPhienHash.isBlank()) {
            return false;
        }
        String sql = "UPDATE phien_dang_nhap " +
                     "SET hoat_dong_cuoi_luc = NOW(), " +
                     "    het_han_luc = DATE_ADD(NOW(), INTERVAL ? MINUTE) " +
                     "WHERE ma_phien_hash = ? AND trang_thai = 'HOAT_DONG'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phutGiaHan > 0 ? phutGiaHan : 30);
            ps.setString(2, maPhienHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi gia hạn phiên {}: {}", maPhienHash, e.getMessage());
            return false;
        }
    }

    public enum KetQuaKiemTraPhien {
        HOP_LE,
        HET_HAN,
        THU_HOI,
        KHONG_TON_TAI
    }

    /**
     * S1-02-AC3: Truy vấn MySQL kiểm tra chi tiết tính hợp lệ của phiên:
     * - Bản ghi tồn tại
     * - trang_thai = HOAT_DONG
     * - thu_hoi_luc IS NULL
     * - het_han_luc > NOW()
     */
    public KetQuaKiemTraPhien kiemTraChiTietPhien(String maPhienHash) {
        if (maPhienHash == null || maPhienHash.isBlank()) {
            return KetQuaKiemTraPhien.KHONG_TON_TAI;
        }
        String sql = "SELECT trang_thai, thu_hoi_luc, het_han_luc, NOW() AS db_now " +
                     "FROM phien_dang_nhap WHERE ma_phien_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhienHash);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return KetQuaKiemTraPhien.KHONG_TON_TAI;
                }
                String trangThai = rs.getString("trang_thai");
                Timestamp thuHoiLuc = rs.getTimestamp("thu_hoi_luc");
                Timestamp hetHanLuc = rs.getTimestamp("het_han_luc");
                Timestamp dbNow = rs.getTimestamp("db_now");

                if ("THU_HOI".equalsIgnoreCase(trangThai) || thuHoiLuc != null) {
                    return KetQuaKiemTraPhien.THU_HOI;
                }

                if (hetHanLuc == null || !hetHanLuc.after(dbNow)) {
                    // het_han_luc <= dbNow -> Đã quá hạn
                    return KetQuaKiemTraPhien.HET_HAN;
                }

                if ("HOAT_DONG".equalsIgnoreCase(trangThai)) {
                    return KetQuaKiemTraPhien.HOP_LE;
                }

                return KetQuaKiemTraPhien.HET_HAN;
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi kiểm tra chi tiết phiên {}: {}", maPhienHash, e.getMessage());
            return KetQuaKiemTraPhien.KHONG_TON_TAI;
        }
    }

    /**
     * Kiểm tra phiên có đang ở trạng thái hoạt động và chưa quá hạn hay không.
     */
    public boolean kiemTraPhienHopLe(String maPhienHash) {
        return kiemTraChiTietPhien(maPhienHash) == KetQuaKiemTraPhien.HOP_LE;
    }

    /**
     * S1-02-AC3: Đánh dấu phiên đã hết hạn trong database.
     */
    public boolean danhDauHetHan(String maPhienHash) {
        if (maPhienHash == null || maPhienHash.isBlank()) {
            return false;
        }
        String sql = "UPDATE phien_dang_nhap SET trang_thai = 'HET_HAN' WHERE ma_phien_hash = ? AND trang_thai = 'HOAT_DONG'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhienHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi đánh dấu hết hạn phiên {}: {}", maPhienHash, e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thời điểm hết hạn của phiên (dùng cho test và kiểm tra).
     */
    public Timestamp layThoiGianHetHan(String maPhienHash) {
        if (maPhienHash == null || maPhienHash.isBlank()) {
            return null;
        }
        String sql = "SELECT het_han_luc FROM phien_dang_nhap WHERE ma_phien_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhienHash);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("het_han_luc");
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy thời gian hết hạn phiên {}: {}", maPhienHash, e.getMessage());
        }
        return null;
    }

    /**
     * Lấy trạng thái của phiên (HOAT_DONG, THU_HOI, HET_HAN).
     */
    public String layTrangThaiPhien(String maPhienHash) {
        if (maPhienHash == null || maPhienHash.isBlank()) {
            return null;
        }
        String sql = "SELECT trang_thai FROM phien_dang_nhap WHERE ma_phien_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhienHash);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("trang_thai");
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi lấy trạng thái phiên {}: {}", maPhienHash, e.getMessage());
        }
        return null;
    }
}
