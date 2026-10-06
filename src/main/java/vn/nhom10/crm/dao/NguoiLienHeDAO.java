package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng `nguoi_lien_he` (Story S3-07).
 * Hỗ trợ tìm kiếm khách hàng theo thông tin liên hệ và số điện thoại.
 */
public class NguoiLienHeDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiLienHeDAO.class.getName());

    public List<NguoiLienHe> layDanhSachTheoKhachHang(long khachHangId) {
        List<NguoiLienHe> danhSach = new ArrayList<>();
        String sql = "SELECT id, khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, " +
                "vai_tro_quyet_dinh, la_dau_moi_chinh, trang_thai, created_at, updated_at " +
                "FROM nguoi_lien_he WHERE khach_hang_id = ? ORDER BY la_dau_moi_chinh DESC, id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách người liên hệ của khách hàng " + khachHangId + ": " + e.getMessage(), e);
        }
        return danhSach;
    }

    public NguoiLienHe timDauMoiChinh(long khachHangId) {
        String sql = "SELECT id, khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, " +
                "vai_tro_quyet_dinh, la_dau_moi_chinh, trang_thai, created_at, updated_at " +
                "FROM nguoi_lien_he WHERE khach_hang_id = ? AND la_dau_moi_chinh = 1 LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi tìm đầu mối chính của khách hàng " + khachHangId + ": " + e.getMessage());
        }
        return null;
    }

    public long themNguoiLienHe(NguoiLienHe nlh) throws SQLException {
        if (nlh == null || nlh.getKhachHangId() == null || nlh.getHoTen() == null) {
            throw new IllegalArgumentException("Thông tin người liên hệ không hợp lệ.");
        }

        String sql = "INSERT INTO nguoi_lien_he (khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, " +
                "vai_tro_quyet_dinh, la_dau_moi_chinh, trang_thai, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, nlh.getKhachHangId());
            ps.setString(2, nlh.getHoTen().trim());
            ps.setString(3, nlh.getChucDanh() != null ? nlh.getChucDanh().trim() : null);
            ps.setString(4, nlh.getEmail() != null ? nlh.getEmail().trim() : null);
            ps.setString(5, nlh.getSoDienThoai() != null ? nlh.getSoDienThoai().trim() : null);
            ps.setString(6, nlh.getVaiTroQuyetDinh() != null ? nlh.getVaiTroQuyetDinh().trim() : null);
            ps.setInt(7, nlh.isLaDauMoiChinh() ? 1 : 0);
            ps.setString(8, nlh.getTrangThai() != null ? nlh.getTrangThai() : "DANG_HOAT_DONG");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1L;
    }

    public NguoiLienHe timTheoId(long id) {
        String sql = "SELECT id, khach_hang_id, ho_ten, chuc_danh, email, so_dien_thoai, " +
                "vai_tro_quyet_dinh, la_dau_moi_chinh, trang_thai, created_at, updated_at " +
                "FROM nguoi_lien_he WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm người liên hệ theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    private NguoiLienHe mapResultSet(ResultSet rs) throws SQLException {
        NguoiLienHe item = new NguoiLienHe();
        item.setId(rs.getLong("id"));
        item.setKhachHangId(rs.getLong("khach_hang_id"));
        item.setHoTen(rs.getString("ho_ten"));
        item.setChucDanh(rs.getString("chuc_danh"));
        item.setEmail(rs.getString("email"));
        item.setSoDienThoai(rs.getString("so_dien_thoai"));
        item.setVaiTroQuyetDinh(rs.getString("vai_tro_quyet_dinh"));
        item.setLaDauMoiChinh(rs.getInt("la_dau_moi_chinh") == 1);
        item.setTrangThai(rs.getString("trang_thai"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));
        return item;
    }
}
