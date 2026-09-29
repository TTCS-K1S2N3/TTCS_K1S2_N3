package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn bảng vai_tro và bảng liên kết nguoi_dung_vai_tro.
 */
public class VaiTroDAO {

    private static final Logger LOGGER = Logger.getLogger(VaiTroDAO.class.getName());

    public List<VaiTro> layTatCa() {
        List<VaiTro> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_vai_tro, ten_vai_tro, mo_ta FROM vai_tro ORDER BY id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                VaiTro vt = new VaiTro(
                        rs.getInt("id"),
                        rs.getString("ma_vai_tro"),
                        rs.getString("ten_vai_tro"),
                        rs.getString("mo_ta")
                );
                danhSach.add(vt);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách vai trò: " + e.getMessage(), e);
        }
        return danhSach;
    }

    public VaiTro timTheoId(int id) {
        String sql = "SELECT id, ma_vai_tro, ten_vai_tro, mo_ta FROM vai_tro WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VaiTro(
                            rs.getInt("id"),
                            rs.getString("ma_vai_tro"),
                            rs.getString("ten_vai_tro"),
                            rs.getString("mo_ta")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm vai trò theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public Set<VaiTro> layVaiTroTheoNguoiDungId(int nguoiDungId) {
        Set<VaiTro> danhSach = new HashSet<>();
        String sql = "SELECT vt.id, vt.ma_vai_tro, vt.ten_vai_tro, vt.mo_ta " +
                     "FROM vai_tro vt " +
                     "INNER JOIN nguoi_dung_vai_tro ndvt ON vt.id = ndvt.vai_tro_id " +
                     "WHERE ndvt.nguoi_dung_id = ? " +
                     "ORDER BY vt.id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VaiTro vt = new VaiTro(
                            rs.getInt("id"),
                            rs.getString("ma_vai_tro"),
                            rs.getString("ten_vai_tro"),
                            rs.getString("mo_ta")
                    );
                    danhSach.add(vt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn vai trò của người dùng ID=" + nguoiDungId + ": " + e.getMessage(), e);
        }
        return danhSach;
    }
}
