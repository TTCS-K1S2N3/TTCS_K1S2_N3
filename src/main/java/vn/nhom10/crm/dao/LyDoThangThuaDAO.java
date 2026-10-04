package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.LyDoThangThua;
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
 * Data Access Object quản lý dữ liệu danh mục lý do thắng thua (Story S2-10).
 * Thao tác trên bảng 'ly_do_thang_thua' và kiểm tra liên kết tham chiếu từ 'co_hoi'.
 */
public class LyDoThangThuaDAO {

    private static final Logger LOGGER = Logger.getLogger(LyDoThangThuaDAO.class.getName());

    /**
     * Lấy toàn bộ danh sách lý do (cả thắng và thua), sắp xếp theo thứ tự hiển thị và ngày tạo.
     */
    public List<LyDoThangThua> layTatCa() {
        List<LyDoThangThua> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_ly_do, ten_ly_do, loai, thu_tu_hien_thi, hoat_dong, created_at " +
                     "FROM ly_do_thang_thua " +
                     "ORDER BY loai ASC, thu_tu_hien_thi ASC, id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LyDoThangThua item = mapResultSet(rs);
                item.setSoCoHoiThamChieu(Math.max(0, demSoCoHoiThamChieu(conn, item.getId())));
                danhSach.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách lý do thắng thua: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách lý do theo loại (THANG hoặc THUA).
     * Phục vụ AC1: "Danh sách lý do thắng và lý do thua khai báo riêng".
     */
    public List<LyDoThangThua> layTheoLoai(String loai) {
        List<LyDoThangThua> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_ly_do, ten_ly_do, loai, thu_tu_hien_thi, hoat_dong, created_at " +
                     "FROM ly_do_thang_thua " +
                     "WHERE UPPER(loai) = UPPER(?) " +
                     "ORDER BY thu_tu_hien_thi ASC, id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LyDoThangThua item = mapResultSet(rs);
                    item.setSoCoHoiThamChieu(Math.max(0, demSoCoHoiThamChieu(conn, item.getId())));
                    danhSach.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách lý do theo loại [" + loai + "]: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách lý do đang hoạt động theo loại (dùng cho dropdown khi đóng cơ hội ở Sprint 5).
     */
    public List<LyDoThangThua> layDangHoatDongTheoLoai(String loai) {
        List<LyDoThangThua> danhSach = new ArrayList<>();
        String sql = "SELECT id, ma_ly_do, ten_ly_do, loai, thu_tu_hien_thi, hoat_dong, created_at " +
                     "FROM ly_do_thang_thua " +
                     "WHERE UPPER(loai) = UPPER(?) AND hoat_dong = 1 " +
                     "ORDER BY thu_tu_hien_thi ASC, id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách lý do đang hoạt động theo loại [" + loai + "]: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm lý do theo ID.
     */
    public LyDoThangThua timTheoId(Long id) {
        if (id == null) return null;
        String sql = "SELECT id, ma_ly_do, ten_ly_do, loai, thu_tu_hien_thi, hoat_dong, created_at " +
                     "FROM ly_do_thang_thua WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LyDoThangThua item = mapResultSet(rs);
                    item.setSoCoHoiThamChieu(Math.max(0, demSoCoHoiThamChieu(conn, item.getId())));
                    return item;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm lý do theo ID [" + id + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tìm lý do theo mã định danh duy nhất.
     */
    public LyDoThangThua timTheoMa(String maLyDo) {
        if (maLyDo == null || maLyDo.isBlank()) return null;
        String sql = "SELECT id, ma_ly_do, ten_ly_do, loai, thu_tu_hien_thi, hoat_dong, created_at " +
                     "FROM ly_do_thang_thua WHERE UPPER(ma_ly_do) = UPPER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLyDo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm lý do theo mã [" + maLyDo + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Kiểm tra mã lý do đã tồn tại hay chưa (trừ ID hiện tại nếu là cập nhật).
     */
    public boolean tonTaiMa(String maLyDo, Long excludeId) {
        if (maLyDo == null || maLyDo.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM ly_do_thang_thua WHERE UPPER(ma_ly_do) = UPPER(?)";
        if (excludeId != null) {
            sql += " AND id <> ?";
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLyDo.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra tồn tại mã lý do [" + maLyDo + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Tạo mới một bản ghi lý do thắng hoặc thua.
     */
    public Long taoMoi(LyDoThangThua lyDo) {
        String sql = "INSERT INTO ly_do_thang_thua (ma_ly_do, ten_ly_do, loai, thu_tu_hien_thi, hoat_dong) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, lyDo.getMaLyDo().trim().toUpperCase());
            ps.setString(2, lyDo.getTenLyDo().trim());
            ps.setString(3, lyDo.getLoai().trim().toUpperCase());
            ps.setInt(4, lyDo.getThuTuHienThi());
            ps.setBoolean(5, lyDo.isHoatDong());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        lyDo.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tạo mới lý do thắng thua: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Cập nhật thông tin lý do.
     */
    public boolean capNhat(LyDoThangThua lyDo) {
        String sql = "UPDATE ly_do_thang_thua " +
                     "SET ma_ly_do = ?, ten_ly_do = ?, loai = ?, thu_tu_hien_thi = ?, hoat_dong = ? " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lyDo.getMaLyDo().trim().toUpperCase());
            ps.setString(2, lyDo.getTenLyDo().trim());
            ps.setString(3, lyDo.getLoai().trim().toUpperCase());
            ps.setInt(4, lyDo.getThuTuHienThi());
            ps.setBoolean(5, lyDo.isHoatDong());
            ps.setLong(6, lyDo.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật lý do ID [" + lyDo.getId() + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Bật/tắt trạng thái hoạt động nhanh.
     */
    public boolean capNhatTrangThai(Long id, boolean hoatDong) {
        String sql = "UPDATE ly_do_thang_thua SET hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, hoatDong);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái lý do ID [" + id + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Cập nhật thứ tự hiển thị.
     */
    public boolean capNhatThuTu(Long id, int thuTu) {
        String sql = "UPDATE ly_do_thang_thua SET thu_tu_hien_thi = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thuTu);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật thứ tự lý do ID [" + id + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Xóa lý do.
     */
    public boolean xoa(Long id) {
        String sql = "DELETE FROM ly_do_thang_thua WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa lý do ID [" + id + "]: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Đếm số lượng cơ hội bán hàng đang tham chiếu lý do này trong bảng 'co_hoi'.
     * Áp dụng nguyên tắc Fail-closed: trả về -1 khi gặp lỗi truy vấn cơ sở dữ liệu.
     */
    public int demSoCoHoiThamChieu(Long lyDoId) {
        if (lyDoId == null) return 0;
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return demSoCoHoiThamChieu(conn, lyDoId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối khi đếm số cơ hội tham chiếu lý do [" + lyDoId + "]: " + e.getMessage(), e);
            return -1;
        }
    }

    private int demSoCoHoiThamChieu(Connection conn, Long lyDoId) {
        if (lyDoId == null || conn == null) return 0;
        String sql = "SELECT COUNT(*) FROM co_hoi WHERE ly_do_thang_thua_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lyDoId);
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
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra tham chiếu cơ hội cho lý do [" + lyDoId + "]: " + e.getMessage(), e);
            return -1;
        }
        return 0;
    }

    private LyDoThangThua mapResultSet(ResultSet rs) throws SQLException {
        LyDoThangThua item = new LyDoThangThua();
        item.setId(rs.getLong("id"));
        item.setMaLyDo(rs.getString("ma_ly_do"));
        item.setTenLyDo(rs.getString("ten_ly_do"));
        item.setLoai(rs.getString("loai"));
        item.setThuTuHienThi(rs.getInt("thu_tu_hien_thi"));
        item.setHoatDong(rs.getBoolean("hoat_dong"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        return item;
    }
}
