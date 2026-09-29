package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng nhom_kinh_doanh.
 */
public class NhomKinhDoanhDAO {

    private static final Logger LOGGER = Logger.getLogger(NhomKinhDoanhDAO.class.getName());

    public NhomKinhDoanh timTheoId(int id) {
        String sql = "SELECT id, ma_nhom, ten_nhom, mo_ta, nhom_cha_id FROM nhom_kinh_doanh WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer nhomChaId = rs.getObject("nhom_cha_id") != null ? rs.getInt("nhom_cha_id") : null;
                    return new NhomKinhDoanh(
                            rs.getInt("id"),
                            rs.getString("ma_nhom"),
                            rs.getString("ten_nhom"),
                            rs.getString("mo_ta"),
                            nhomChaId
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm nhóm kinh doanh theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public List<NhomKinhDoanh> layTatCa() {
        List<NhomKinhDoanh> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_nhom, ten_nhom, mo_ta, nhom_cha_id FROM nhom_kinh_doanh ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer nhomChaId = rs.getObject("nhom_cha_id") != null ? rs.getInt("nhom_cha_id") : null;
                danhSach.add(new NhomKinhDoanh(
                        rs.getInt("id"),
                        rs.getString("ma_nhom"),
                        rs.getString("ten_nhom"),
                        rs.getString("mo_ta"),
                        nhomChaId
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách nhóm kinh doanh: " + e.getMessage(), e);
        }
        return danhSach;
    }
}
