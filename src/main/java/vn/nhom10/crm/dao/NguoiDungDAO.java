package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng nguoi_dung và mapping quan hệ nhiều-nhiều với vai_tro, quan hệ với nhom_kinh_doanh.
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
            LOGGER.log(Level.SEVERE, "Lỗi tìm người dùng theo ID=" + id + ": " + e.getMessage(), e);
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
