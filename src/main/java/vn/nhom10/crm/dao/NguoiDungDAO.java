package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.NguoiDung;

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
 * Data Access Object cho bảng nguoi_dung.
 * Truy vấn cơ sở dữ liệu MySQL bằng PreparedStatement.
 */
public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    /**
     * Tìm người dùng theo ID.
     *
     * @param id ID người dùng
     * @return NguoiDung nếu tìm thấy, null nếu không tồn tại
     */
    public NguoiDung findById(Long id) {
        if (id == null) {
            return null;
        }

        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                   + "       so_lan_sai, thoi_gian_khoa, ngay_tao, ngay_cap_nhat "
                   + "FROM nguoi_dung WHERE id = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToNguoiDung(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn người dùng theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm người dùng theo email.
     *
     * @param email địa chỉ email
     * @return NguoiDung nếu tìm thấy, null nếu không tồn tại
     */
    public NguoiDung findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                   + "       so_lan_sai, thoi_gian_khoa, ngay_tao, ngay_cap_nhat "
                   + "FROM nguoi_dung WHERE LOWER(email) = LOWER(?) LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToNguoiDung(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn người dùng theo email: " + email, e);
        }
        return null;
    }

    /**
     * Cập nhật mật khẩu mới cho người dùng trong transaction.
     *
     * @param nguoiDungId ID người dùng
     * @param matKhauHash mật khẩu đã băm BCrypt
     * @param conn        kết nối JDBC thuộc transaction
     * @return true nếu cập nhật thành công
     * @throws SQLException khi lỗi truy vấn
     */
    public boolean updateMatKhau(Long nguoiDungId, String matKhauHash, Connection conn) throws SQLException {
        String sql = "UPDATE nguoi_dung "
                   + "SET mat_khau = ?, so_lan_sai = 0, thoi_gian_khoa = NULL, "
                   + "    ngay_doi_mat_khau = ?, session_version = session_version + 1, ngay_cap_nhat = ? "
                   + "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, matKhauHash);
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, now);
            ps.setLong(4, nguoiDungId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Thêm mới người dùng vào hệ thống (phục vụ kiểm thử).
     *
     * @param nguoiDung thông tin người dùng
     * @return ID người dùng vừa tạo
     */
    public Long save(NguoiDung nguoiDung) {
        String sql = "INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, trang_thai, so_lan_sai, ngay_tao, ngay_cap_nhat) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, nguoiDung.getHoTen());
            ps.setString(2, nguoiDung.getEmail().trim().toLowerCase());
            ps.setString(3, nguoiDung.getMatKhau());
            ps.setString(4, nguoiDung.getSoDienThoai());
            ps.setString(5, nguoiDung.getTrangThai() != null ? nguoiDung.getTrangThai() : "HOAT_DONG");
            ps.setInt(6, nguoiDung.getSoLanSai());
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.setTimestamp(8, Timestamp.valueOf(now));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        nguoiDung.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm người dùng: " + nguoiDung.getEmail(), e);
        }
        return null;
    }

    private NguoiDung mapRowToNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung user = new NguoiDung();
        user.setId(rs.getLong("id"));
        user.setHoTen(rs.getString("ho_ten"));
        user.setEmail(rs.getString("email"));
        user.setMatKhau(rs.getString("mat_khau"));
        user.setSoDienThoai(rs.getString("so_dien_thoai"));
        user.setTrangThai(rs.getString("trang_thai"));
        user.setSoLanSai(rs.getInt("so_lan_sai"));

        Timestamp khoaTs = rs.getTimestamp("thoi_gian_khoa");
        if (khoaTs != null) {
            user.setThoiGianKhoa(khoaTs.toLocalDateTime());
        }

        Timestamp taoTs = rs.getTimestamp("ngay_tao");
        if (taoTs != null) {
            user.setNgayTao(taoTs.toLocalDateTime());
        }

        Timestamp capNhatTs = rs.getTimestamp("ngay_cap_nhat");
        if (capNhatTs != null) {
            user.setNgayCapNhat(capNhatTs.toLocalDateTime());
        }

        return user;
    }
}
