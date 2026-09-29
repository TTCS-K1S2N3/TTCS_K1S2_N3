package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object cho bảng nguoi_dung.
 */
public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    public NguoiDung timTheoEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                + "so_lan_sai, thoi_gian_khoa, nhom_kinh_doanh_id, created_at, updated_at "
                + "FROM nguoi_dung WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nd = mapResultSetToNguoiDung(rs);
                    nd.setDanhSachVaiTro(layDanhSachVaiTroTheoNguoiDungId(nd.getId()));
                    return nd;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn người dùng theo email: " + email, e);
        }
        return null;
    }

    public NguoiDung timTheoId(long id) {
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                + "so_lan_sai, thoi_gian_khoa, nhom_kinh_doanh_id, created_at, updated_at "
                + "FROM nguoi_dung WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nd = mapResultSetToNguoiDung(rs);
                    nd.setDanhSachVaiTro(layDanhSachVaiTroTheoNguoiDungId(nd.getId()));
                    return nd;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn người dùng theo ID: " + id, e);
        }
        return null;
    }

    public Set<VaiTro> layDanhSachVaiTroTheoNguoiDungId(long nguoiDungId) {
        Set<VaiTro> danhSach = new HashSet<>();
        String sql = "SELECT vt.id, vt.ma_vai_tro, vt.ten_vai_tro, vt.mo_ta "
                + "FROM vai_tro vt "
                + "INNER JOIN nguoi_dung_vai_tro ndvt ON vt.id = ndvt.vai_tro_id "
                + "WHERE ndvt.nguoi_dung_id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, nguoiDungId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VaiTro vt = new VaiTro();
                    vt.setId(rs.getInt("id"));
                    vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                    vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                    vt.setMoTa(rs.getString("mo_ta"));
                    danhSach.add(vt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách vai trò cho user id: " + nguoiDungId, e);
        }
        return danhSach;
    }

    private NguoiDung mapResultSetToNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung nd = new NguoiDung();
        nd.setId(rs.getLong("id"));
        nd.setHoTen(rs.getString("ho_ten"));
        nd.setEmail(rs.getString("email"));
        nd.setMatKhau(rs.getString("mat_khau"));
        nd.setSoDienThoai(rs.getString("so_dien_thoai"));
        nd.setTrangThai(rs.getString("trang_thai"));
        nd.setSoLanSai(rs.getInt("so_lan_sai"));
        nd.setThoiGianKhoa(rs.getTimestamp("thoi_gian_khoa"));
        int nhomId = rs.getInt("nhom_kinh_doanh_id");
        if (!rs.wasNull()) {
            nd.setNhomKinhDoanhId(nhomId);
        }
        nd.setCreatedAt(rs.getTimestamp("created_at"));
        nd.setUpdatedAt(rs.getTimestamp("updated_at"));
        return nd;
    }

    public void capNhatDangNhapThanhCong(long nguoiDungId, Timestamp lanDangNhapCuoi) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = 0, thoi_gian_khoa = NULL, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, lanDangNhapCuoi != null ? lanDangNhapCuoi : new Timestamp(System.currentTimeMillis()));
            ps.setLong(2, nguoiDungId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật đăng nhập thành công cho user: " + nguoiDungId, e);
        }
    }

    public void capNhatDangNhapThatBai(long nguoiDungId, int soLanSai, Timestamp thoiGianKhoa) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = ?, thoi_gian_khoa = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLanSai);
            ps.setTimestamp(2, thoiGianKhoa);
            ps.setLong(3, nguoiDungId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật đăng nhập thất bại cho user: " + nguoiDungId, e);
        }
    }
}

