package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng nguoi_dung, gán vai trò & nhóm kinh doanh với Transaction an toàn (Story S1-09).
 */
public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    private final VaiTroDAO vaiTroDAO = new VaiTroDAO();
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO = new NhomKinhDoanhDAO();

    public NguoiDung timTheoId(int id) {
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id " +
                     "FROM nguoi_dung WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaNguoiDung(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm người dùng ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public NguoiDung timTheoEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id " +
                     "FROM nguoi_dung WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaNguoiDung(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm người dùng theo email=" + email + ": " + e.getMessage(), e);
        }
        return null;
    }

    public List<NguoiDung> layTatCa() {
        List<NguoiDung> danhSach = new ArrayList<>();
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id " +
                     "FROM nguoi_dung ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(anhXaNguoiDung(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách người dùng: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Gán danh sách vai trò và nhóm kinh doanh cho người dùng trong một TRANSACTION an toàn.
     * Nếu xảy ra bất kỳ lỗi nào, toàn bộ thay đổi sẽ được ROLLBACK.
     */
    public boolean capNhatVaiTroVaNhomTransaction(int nguoiDungId, List<Integer> danhSachVaiTroId, Integer nhomKinhDoanhId) throws SQLException {
        String sqlCapNhatNhom = "UPDATE nguoi_dung SET nhom_kinh_doanh_id = ? WHERE id = ?";
        String sqlXoaVaiTroCu = "DELETE FROM nguoi_dung_vai_tro WHERE nguoi_dung_id = ?";
        String sqlThemVaiTro = "INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Cập nhật nhóm kinh doanh
            try (PreparedStatement psNhom = conn.prepareStatement(sqlCapNhatNhom)) {
                if (nhomKinhDoanhId != null && nhomKinhDoanhId > 0) {
                    psNhom.setInt(1, nhomKinhDoanhId);
                } else {
                    psNhom.setNull(1, Types.INTEGER);
                }
                psNhom.setInt(2, nguoiDungId);
                psNhom.executeUpdate();
            }

            // 2. Xoá tất cả vai trò cũ của người dùng
            try (PreparedStatement psXoa = conn.prepareStatement(sqlXoaVaiTroCu)) {
                psXoa.setInt(1, nguoiDungId);
                psXoa.executeUpdate();
            }

            // 3. Chèn các vai trò mới
            if (danhSachVaiTroId != null && !danhSachVaiTroId.isEmpty()) {
                try (PreparedStatement psThem = conn.prepareStatement(sqlThemVaiTro)) {
                    for (Integer vaiTroId : danhSachVaiTroId) {
                        if (vaiTroId != null) {
                            psThem.setInt(1, nguoiDungId);
                            psThem.setInt(2, vaiTroId);
                            psThem.addBatch();
                        }
                    }
                    psThem.executeBatch();
                }
            }

            conn.commit(); // Hoàn tất transaction thành công
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    LOGGER.log(Level.WARNING, "Lỗi khi gán vai trò & nhóm, đang rollback transaction: " + e.getMessage());
                    conn.rollback(); // Rollback toàn bộ khi gặp sự cố
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi rollback: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Lỗi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    private NguoiDung anhXaNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung nd = new NguoiDung();
        nd.setId(rs.getInt("id"));
        nd.setHoTen(rs.getString("ho_ten"));
        nd.setEmail(rs.getString("email"));
        nd.setMatKhau(rs.getString("mat_khau"));
        nd.setSoDienThoai(rs.getString("so_dien_thoai"));
        nd.setTrangThai(rs.getString("trang_thai"));

        Integer nhomId = rs.getObject("nhom_kinh_doanh_id") != null ? rs.getInt("nhom_kinh_doanh_id") : null;
        nd.setNhomKinhDoanhId(nhomId);

        if (nhomId != null) {
            NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomId);
            nd.setNhomKinhDoanh(nhom);
        }

        Set<VaiTro> dsVaiTro = vaiTroDAO.layVaiTroTheoNguoiDungId(nd.getId());
        nd.setDanhSachVaiTro(dsVaiTro);

        return nd;
    }
}
