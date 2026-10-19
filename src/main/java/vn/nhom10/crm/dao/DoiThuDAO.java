package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.DoiThu;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý danh sách đối thủ cạnh tranh (Story S2-10 AC2).
 * Thao tác trên bảng 'doi_thu' và kiểm tra tham chiếu từ 'co_hoi'.
 */
public class DoiThuDAO {

    private static final Logger LOGGER = Logger.getLogger(DoiThuDAO.class.getName());

    /**
     * Lấy tất cả đối thủ cạnh tranh, sắp xếp theo tên công ty.
     */
    public List<DoiThu> layTatCa() {
        List<DoiThu> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_doi_thu, ten_doi_thu, website, ghi_chu, hoat_dong, created_at, updated_at " +
                     "FROM doi_thu ORDER BY hoat_dong DESC, ten_doi_thu ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DoiThu item = mapResultSet(rs);
                item.setSoCoHoiThamChieu(Math.max(0, demSoCoHoiThamChieu(conn, item.getId())));
                danhSach.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách đối thủ: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách đối thủ đang hoạt động (dùng cho dropdown khi đóng cơ hội thua trong Sprint 5).
     */
    public List<DoiThu> layDangHoatDong() {
        List<DoiThu> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_doi_thu, ten_doi_thu, website, ghi_chu, hoat_dong, created_at, updated_at " +
                     "FROM doi_thu WHERE hoat_dong = 1 ORDER BY ten_doi_thu ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách đối thủ đang hoạt động: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm đối thủ theo ID.
     */
    public DoiThu timTheoId(Long id) {
        if (id == null) return null;
        String sql = "SELECT id, ma_doi_thu, ten_doi_thu, website, ghi_chu, hoat_dong, created_at, updated_at " +
                     "FROM doi_thu WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DoiThu item = mapResultSet(rs);
                    item.setSoCoHoiThamChieu(Math.max(0, demSoCoHoiThamChieu(conn, item.getId())));
                    return item;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm đối thủ theo ID [" + id + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tìm đối thủ theo mã duy nhất.
     */
    public DoiThu timTheoMa(String maDoiThu) {
        if (maDoiThu == null || maDoiThu.isBlank()) return null;
        String sql = "SELECT id, ma_doi_thu, ten_doi_thu, website, ghi_chu, hoat_dong, created_at, updated_at " +
                     "FROM doi_thu WHERE UPPER(ma_doi_thu) = UPPER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDoiThu.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm đối thủ theo mã [" + maDoiThu + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Kiểm tra trùng mã đối thủ.
     */
    public boolean tonTaiMa(String maDoiThu, Long excludeId) {
        if (maDoiThu == null || maDoiThu.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM doi_thu WHERE UPPER(ma_doi_thu) = UPPER(?)";
        if (excludeId != null) {
            sql += " AND id <> ?";
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDoiThu.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã đối thủ [" + maDoiThu + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Tạo mới một đối thủ cạnh tranh.
     */
    public Long taoMoi(DoiThu doiThu) {
        String sql = "INSERT INTO doi_thu (ma_doi_thu, ten_doi_thu, website, ghi_chu, hoat_dong) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, doiThu.getMaDoiThu().trim().toUpperCase());
            ps.setString(2, doiThu.getTenDoiThu().trim());
            ps.setString(3, doiThu.getWebsite() != null ? doiThu.getWebsite().trim() : null);
            ps.setString(4, doiThu.getGhiChu() != null ? doiThu.getGhiChu().trim() : null);
            ps.setBoolean(5, doiThu.isHoatDong());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        doiThu.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo mới đối thủ: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Cập nhật thông tin đối thủ.
     */
    public boolean capNhat(DoiThu doiThu) {
        String sql = "UPDATE doi_thu " +
                     "SET ma_doi_thu = ?, ten_doi_thu = ?, website = ?, ghi_chu = ?, hoat_dong = ? " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doiThu.getMaDoiThu().trim().toUpperCase());
            ps.setString(2, doiThu.getTenDoiThu().trim());
            ps.setString(3, doiThu.getWebsite() != null ? doiThu.getWebsite().trim() : null);
            ps.setString(4, doiThu.getGhiChu() != null ? doiThu.getGhiChu().trim() : null);
            ps.setBoolean(5, doiThu.isHoatDong());
            ps.setLong(6, doiThu.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật đối thủ ID [" + doiThu.getId() + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Bật/tắt trạng thái hoạt động nhanh.
     */
    public boolean capNhatTrangThai(Long id, boolean hoatDong) {
        String sql = "UPDATE doi_thu SET hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, hoatDong);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái đối thủ ID [" + id + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Xóa đối thủ.
     */
    public boolean xoa(Long id) {
        String sql = "DELETE FROM doi_thu WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa đối thủ ID [" + id + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Đếm số lượng cơ hội bán hàng đang tham chiếu đối thủ này trong bảng 'co_hoi'.
     * Áp dụng nguyên tắc Fail-closed: trả về -1 khi gặp lỗi truy vấn cơ sở dữ liệu.
     */
    public int demSoCoHoiThamChieu(Long doiThuId) {
        if (doiThuId == null) return 0;
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return demSoCoHoiThamChieu(conn, doiThuId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối khi đếm số cơ hội tham chiếu đối thủ [" + doiThuId + "]: " + e.getMessage(), e);
            return -1;
        }
    }

    private int demSoCoHoiThamChieu(Connection conn, Long doiThuId) {
        if (doiThuId == null || conn == null) return 0;
        String sql = "SELECT COUNT(*) FROM co_hoi WHERE doi_thu_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doiThuId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            String state = e.getSQLState();
            int code = e.getErrorCode();
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if ("42S02".equalsIgnoreCase(state) || code == 1146 || code == 42102 || msg.contains("not found") || msg.contains("doesn't exist")) {
                LOGGER.log(Level.FINE, "Bảng co_hoi chưa tồn tại trong môi trường hiện tại: " + e.getMessage());
                return 0;
            }
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra tham chiếu cơ hội cho đối thủ [" + doiThuId + "]: " + e.getMessage(), e);
            return -1;
        }
        return 0;
    }

    private DoiThu mapResultSet(ResultSet rs) throws SQLException {
        DoiThu item = new DoiThu();
        item.setId(rs.getLong("id"));
        item.setMaDoiThu(rs.getString("ma_doi_thu"));
        item.setTenDoiThu(rs.getString("ten_doi_thu"));
        item.setWebsite(rs.getString("website"));
        item.setGhiChu(rs.getString("ghi_chu"));
        item.setHoatDong(rs.getBoolean("hoat_dong"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));
        return item;
    }
}
