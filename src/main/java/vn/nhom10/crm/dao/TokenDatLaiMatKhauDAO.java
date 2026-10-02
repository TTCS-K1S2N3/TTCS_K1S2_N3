package vn.nhom10.crm.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.TokenDatLaiMatKhau;

import java.sql.*;

/**
 * DAO xử lý thao tác với bảng token_dat_lai_mat_khau (S1-03).
 */
public class TokenDatLaiMatKhauDAO {

    private static final Logger logger = LoggerFactory.getLogger(TokenDatLaiMatKhauDAO.class);

    /**
     * S1-03-AC1: Tạo token đặt lại mật khẩu với thời hạn het_han_luc (mặc định 30 phút).
     */
    public boolean taoToken(Long nguoiDungId, String tokenHash, int phutHetHan, String diaChiIp) {
        String sql = "INSERT INTO token_dat_lai_mat_khau (nguoi_dung_id, token_hash, het_han_luc, dia_chi_ip_yeu_cau) " +
                     "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setString(2, tokenHash);
            ps.setInt(3, phutHetHan > 0 ? phutHetHan : 30);
            ps.setString(4, diaChiIp);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi tạo token đặt lại mật khẩu cho user id {}: {}", nguoiDungId, e.getMessage());
            return false;
        }
    }

    /**
     * S1-03-AC1, AC2: Tìm kiếm thông tin token và đối soát với thời gian hiện tại của MySQL.
     */
    public TokenDatLaiMatKhau timToken(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return null;
        }
        String sql = "SELECT t.id, t.nguoi_dung_id, t.token_hash, t.tao_luc, t.het_han_luc, " +
                     "       t.da_su_dung_luc, t.dia_chi_ip_yeu_cau, NOW() AS db_now, " +
                     "       u.email, u.trang_thai AS user_status " +
                     "FROM token_dat_lai_mat_khau t " +
                     "INNER JOIN nguoi_dung u ON t.nguoi_dung_id = u.id " +
                     "WHERE t.token_hash = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TokenDatLaiMatKhau token = new TokenDatLaiMatKhau();
                    token.setId(rs.getLong("id"));
                    token.setNguoiDungId(rs.getLong("nguoi_dung_id"));
                    token.setTokenHash(rs.getString("token_hash"));
                    token.setTaoLuc(rs.getTimestamp("tao_luc"));
                    token.setHetHanLuc(rs.getTimestamp("het_han_luc"));
                    token.setDaSuDungLuc(rs.getTimestamp("da_su_dung_luc"));
                    token.setDiaChiIpYeuCau(rs.getString("dia_chi_ip_yeu_cau"));
                    token.setUserEmail(rs.getString("email"));
                    token.setUserTrangThai(rs.getString("user_status"));
                    return token;
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi tìm token đặt lại mật khẩu: {}", e.getMessage());
        }
        return null;
    }

    /**
     * S1-03-AC2: Đánh dấu token đã được sử dụng (chỉ update nếu da_su_dung_luc IS NULL).
     */
    public boolean danhDauDaSuDung(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return false;
        }
        String sql = "UPDATE token_dat_lai_mat_khau SET da_su_dung_luc = NOW() " +
                     "WHERE token_hash = ? AND da_su_dung_luc IS NULL";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi khi đánh dấu đã sử dụng token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thời gian hiện tại của cơ sở dữ liệu MySQL (dùng để so sánh chính xác mốc thời gian).
     */
    public Timestamp layThoiGianHienTaiDB() {
        String sql = "SELECT NOW() AS db_now";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getTimestamp("db_now");
            }
        } catch (SQLException e) {
            logger.error("Lỗi lấy thời gian DB: {}", e.getMessage());
        }
        return new Timestamp(System.currentTimeMillis());
    }
}
