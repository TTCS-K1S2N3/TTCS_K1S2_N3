package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.DatLaiMatKhauToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object cho bảng dat_lai_mat_khau_token.
 * Đáp ứng các yêu cầu:
 * - Lưu token kèm thời hạn 30 phút.
 * - Kiểm tra tính hợp lệ và thời hạn.
 * - Đánh dấu đã sử dụng (đảm bảo chỉ dùng 1 lần).
 */
public class DatLaiMatKhauTokenDAO {

    private static final Logger LOGGER = Logger.getLogger(DatLaiMatKhauTokenDAO.class.getName());

    /**
     * Lưu token đặt lại mật khẩu mới vào cơ sở dữ liệu.
     *
     * @param token đối tượng DatLaiMatKhauToken
     * @return ID bản ghi vừa tạo
     */
    public Long save(DatLaiMatKhauToken token) {
        String sql = "INSERT INTO dat_lai_mat_khau_token "
                   + "(nguoi_dung_id, token, thoi_gian_tao, thoi_gian_het_han, da_su_dung, thoi_gian_su_dung) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, token.getNguoiDungId());
            ps.setString(2, token.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(token.getThoiGianTao() != null ? token.getThoiGianTao() : LocalDateTime.now()));
            ps.setTimestamp(4, Timestamp.valueOf(token.getThoiGianHetHan()));
            ps.setBoolean(5, token.isDaSuDung());
            if (token.getThoiGianSuDung() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(token.getThoiGianSuDung()));
            } else {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        token.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu token đặt lại mật khẩu", e);
        }
        return null;
    }

    /**
     * Tìm token đặt lại mật khẩu theo chuỗi token.
     *
     * @param tokenString chuỗi token cần tìm
     * @return DatLaiMatKhauToken nếu có, null nếu không tồn tại
     */
    public DatLaiMatKhauToken findByToken(String tokenString) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, nguoi_dung_id, token, thoi_gian_tao, thoi_gian_het_han, da_su_dung, thoi_gian_su_dung "
                   + "FROM dat_lai_mat_khau_token WHERE token = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tokenString.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToToken(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm kiếm token: " + tokenString, e);
        }
        return null;
    }

    /**
     * Tìm và khóa dòng token trong transaction (SELECT FOR UPDATE) để chống race condition.
     *
     * @param tokenString chuỗi token
     * @param conn        kết nối JDBC trong transaction
     * @return DatLaiMatKhauToken nếu có
     * @throws SQLException khi lỗi truy vấn
     */
    public DatLaiMatKhauToken findByTokenForUpdate(String tokenString, Connection conn) throws SQLException {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, nguoi_dung_id, token, thoi_gian_tao, thoi_gian_het_han, da_su_dung, thoi_gian_su_dung "
                   + "FROM dat_lai_mat_khau_token WHERE token = ? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenString.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToToken(rs);
                }
            }
        }
        return null;
    }

    /**
     * Đánh dấu token đã được sử dụng thành công (AC2: chỉ dùng 1 lần).
     *
     * @param tokenId ID token cần cập nhật
     * @param conn    kết nối JDBC trong transaction
     * @return true nếu cập nhật thành công
     * @throws SQLException khi lỗi truy vấn
     */
    public boolean markAsUsed(Long tokenId, Connection conn) throws SQLException {
        String sql = "UPDATE dat_lai_mat_khau_token "
                   + "SET da_su_dung = 1, thoi_gian_su_dung = ? "
                   + "WHERE id = ? AND da_su_dung = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, tokenId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Vô hiệu hóa tất cả các token đặt lại mật khẩu còn lại của một người dùng.
     *
     * @param nguoiDungId ID người dùng
     * @param conn        kết nối JDBC trong transaction
     * @throws SQLException khi lỗi truy vấn
     */
    public void invalidateTokensByNguoiDungId(Long nguoiDungId, Connection conn) throws SQLException {
        String sql = "UPDATE dat_lai_mat_khau_token "
                   + "SET da_su_dung = 1, thoi_gian_su_dung = ? "
                   + "WHERE nguoi_dung_id = ? AND da_su_dung = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, nguoiDungId);
            ps.executeUpdate();
        }
    }

    /**
     * Vô hiệu hóa tất cả các token đặt lại mật khẩu chưa sử dụng của một người dùng (tự mở kết nối).
     * Đảm bảo khi yêu cầu liên kết mới thì toàn bộ token cũ không còn hợp lệ.
     *
     * @param nguoiDungId ID người dùng
     */
    public void invalidateTokensByNguoiDungId(Long nguoiDungId) {
        String sql = "UPDATE dat_lai_mat_khau_token "
                   + "SET da_su_dung = 1, thoi_gian_su_dung = ? "
                   + "WHERE nguoi_dung_id = ? AND da_su_dung = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, nguoiDungId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi vô hiệu hóa token cũ của user id: " + nguoiDungId, e);
        }
    }

    /**
     * Lấy token đặt lại mật khẩu gần nhất của người dùng để phục vụ kiểm tra thời gian cooldown.
     *
     * @param nguoiDungId ID người dùng
     * @return DatLaiMatKhauToken gần nhất hoặc null nếu chưa có
     */
    public DatLaiMatKhauToken findLatestTokenByNguoiDungId(Long nguoiDungId) {
        String sql = "SELECT id, nguoi_dung_id, token, thoi_gian_tao, thoi_gian_het_han, da_su_dung, thoi_gian_su_dung "
                   + "FROM dat_lai_mat_khau_token WHERE nguoi_dung_id = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToToken(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy token gần nhất của user id: " + nguoiDungId, e);
        }
        return null;
    }

    /**
     * Xóa token theo ID nếu gửi email thất bại để tránh token rác tồn tại.
     *
     * @param tokenId ID token cần xóa
     */
    public void deleteById(Long tokenId) {
        String sql = "DELETE FROM dat_lai_mat_khau_token WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tokenId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa token id: " + tokenId, e);
        }
    }

    private DatLaiMatKhauToken mapRowToToken(ResultSet rs) throws SQLException {
        DatLaiMatKhauToken token = new DatLaiMatKhauToken();
        token.setId(rs.getLong("id"));
        token.setNguoiDungId(rs.getLong("nguoi_dung_id"));
        token.setToken(rs.getString("token"));

        Timestamp taoTs = rs.getTimestamp("thoi_gian_tao");
        if (taoTs != null) {
            token.setThoiGianTao(taoTs.toLocalDateTime());
        }

        Timestamp hetHanTs = rs.getTimestamp("thoi_gian_het_han");
        if (hetHanTs != null) {
            token.setThoiGianHetHan(hetHanTs.toLocalDateTime());
        }

        token.setDaSuDung(rs.getBoolean("da_su_dung"));

        Timestamp suDungTs = rs.getTimestamp("thoi_gian_su_dung");
        if (suDungTs != null) {
            token.setThoiGianSuDung(suDungTs.toLocalDateTime());
        }

        return token;
    }
}
