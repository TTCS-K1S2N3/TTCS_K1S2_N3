package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
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
 * Data Access Object phụ trách bảng vai_tro.
 */
public class VaiTroDAO {

    private static final Logger LOGGER = Logger.getLogger(VaiTroDAO.class.getName());

    public List<VaiTro> layTatCa() {
        List<VaiTro> ds = new ArrayList<>();
        String sql = "SELECT id, ma_vai_tro, ten_vai_tro, mo_ta, pham_vi_toi_da FROM vai_tro ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                VaiTro vt = new VaiTro();
                vt.setId(rs.getInt("id"));
                vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                vt.setMoTa(rs.getString("mo_ta"));
                vt.setPhamViToiDa(rs.getString("pham_vi_toi_da"));
                ds.add(vt);
            }
            return ds;
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Bảng vai_tro chưa có pham_vi_toi_da, dùng truy vấn cơ bản: " + e.getMessage());
            return layTatCaCoBan();
        }
    }

    private List<VaiTro> layTatCaCoBan() {
        List<VaiTro> ds = new ArrayList<>();
        String sql = "SELECT id, ma_vai_tro, ten_vai_tro, mo_ta FROM vai_tro ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VaiTro vt = new VaiTro();
                vt.setId(rs.getInt("id"));
                vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                vt.setMoTa(rs.getString("mo_ta"));
                ds.add(vt);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách vai trò cơ bản", e);
        }
        return ds;
    }

    public VaiTro timTheoMa(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isBlank()) {
            return null;
        }

        String sql = "SELECT id, ma_vai_tro, ten_vai_tro, mo_ta, pham_vi_toi_da FROM vai_tro WHERE UPPER(ma_vai_tro) = UPPER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maVaiTro.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VaiTro vt = new VaiTro();
                    vt.setId(rs.getInt("id"));
                    vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                    vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                    vt.setMoTa(rs.getString("mo_ta"));
                    vt.setPhamViToiDa(rs.getString("pham_vi_toi_da"));
                    return vt;
                }
            }
        } catch (SQLException e) {
            return timTheoMaCoBan(maVaiTro);
        }
        return null;
    }

    private VaiTro timTheoMaCoBan(String maVaiTro) {
        String sql = "SELECT id, ma_vai_tro, ten_vai_tro, mo_ta FROM vai_tro WHERE UPPER(ma_vai_tro) = UPPER(?)";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VaiTro vt = new VaiTro();
                    vt.setId(rs.getInt("id"));
                    vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                    vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                    vt.setMoTa(rs.getString("mo_ta"));
                    return vt;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm vai trò cơ bản theo mã: " + maVaiTro, e);
        }
        return null;
    }

    /**
     * Truy vấn phạm vi dữ liệu tối đa từ cơ sở dữ liệu cho một vai trò (Single Source of Truth).
     * Áp dụng nguyên tắc FAIL-CLOSED:
     * Nếu lỗi kết nối, bảng chưa có cột, hoặc vai trò không tồn tại trong DB,
     * LUÔN trả về PhamViDuLieu.CA_NHAN.
     * TUYỆT ĐỐI không fallback dựa trên role hardcode (ngăn chặn rủi ro fail-open).
     */
    public PhamViDuLieu layPhamViToiDaCuaVaiTro(String maVaiTro) {
        if (maVaiTro == null || maVaiTro.isBlank()) {
            return vn.nhom10.crm.model.PhamViDuLieu.CA_NHAN;
        }
        String sql = "SELECT pham_vi_toi_da FROM vai_tro WHERE UPPER(ma_vai_tro) = UPPER(?)";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String pv = rs.getString("pham_vi_toi_da");
                    vn.nhom10.crm.model.PhamViDuLieu res = vn.nhom10.crm.model.PhamViDuLieu.tuMa(pv);
                    return res != null ? res : vn.nhom10.crm.model.PhamViDuLieu.CA_NHAN;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi truy vấn phạm vi tối đa cho vai trò [" + maVaiTro + "], fail-closed về CA_NHAN: " + e.getMessage());
        }
        return vn.nhom10.crm.model.PhamViDuLieu.CA_NHAN;
    }
}
