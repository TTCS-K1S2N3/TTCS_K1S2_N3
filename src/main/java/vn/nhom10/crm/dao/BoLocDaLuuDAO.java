package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.BoLocDaLuu;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng `bo_loc_da_luu` (Story S3-07, AC3).
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, transaction khi cập nhật trạng thái mặc định).
 */
public class BoLocDaLuuDAO {

    private static final Logger LOGGER = Logger.getLogger(BoLocDaLuuDAO.class.getName());

    /**
     * Lưu bộ lọc: Nếu tên bộ lọc đã tồn tại cho người dùng và đối tượng, thực hiện cập nhật tiêu chí;
     * ngược lại, thêm bản ghi mới.
     */
    public long luuBoLoc(BoLocDaLuu boLoc) throws SQLException {
        if (boLoc == null || boLoc.getNguoiDungId() == null || boLoc.getTenBoLoc() == null) {
            throw new IllegalArgumentException("Thông tin bộ lọc không hợp lệ để lưu.");
        }

        BoLocDaLuu daCo = timTheoTen(boLoc.getNguoiDungId(), boLoc.getLoaiDoiTuong(), boLoc.getTenBoLoc().trim());
        if (daCo != null) {
            String sqlUpdate = "UPDATE bo_loc_da_luu " +
                    "SET tieu_chi_json = ?, mac_dinh = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND nguoi_dung_id = ?";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, boLoc.getTieuChiJson());
                ps.setInt(2, boLoc.isMacDinh() ? 1 : 0);
                ps.setLong(3, daCo.getId());
                ps.setLong(4, boLoc.getNguoiDungId());
                ps.executeUpdate();
                return daCo.getId();
            }
        }

        String sqlInsert = "INSERT INTO bo_loc_da_luu (nguoi_dung_id, loai_doi_tuong, ten_bo_loc, tieu_chi_json, mac_dinh, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, boLoc.getNguoiDungId());
            ps.setString(2, boLoc.getLoaiDoiTuong() != null ? boLoc.getLoaiDoiTuong() : BoLocDaLuu.LOAI_KHACH_HANG);
            ps.setString(3, boLoc.getTenBoLoc().trim());
            ps.setString(4, boLoc.getTieuChiJson());
            ps.setInt(5, boLoc.isMacDinh() ? 1 : 0);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1L;
    }

    /**
     * Tìm bộ lọc theo ID và mã người dùng (đảm bảo quyền truy cập cá nhân).
     */
    public BoLocDaLuu timTheoId(long id, long nguoiDungId) {
        String sql = "SELECT id, nguoi_dung_id, loai_doi_tuong, ten_bo_loc, tieu_chi_json, mac_dinh, created_at, updated_at " +
                "FROM bo_loc_da_luu WHERE id = ? AND nguoi_dung_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm bộ lọc đã lưu theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tìm bộ lọc theo tên của người dùng đối với một loại đối tượng.
     */
    public BoLocDaLuu timTheoTen(long nguoiDungId, String loaiDoiTuong, String tenBoLoc) {
        if (tenBoLoc == null || tenBoLoc.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id, nguoi_dung_id, loai_doi_tuong, ten_bo_loc, tieu_chi_json, mac_dinh, created_at, updated_at " +
                "FROM bo_loc_da_luu WHERE nguoi_dung_id = ? AND loai_doi_tuong = ? AND LOWER(ten_bo_loc) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setString(2, loaiDoiTuong != null ? loaiDoiTuong : BoLocDaLuu.LOAI_KHACH_HANG);
            ps.setString(3, tenBoLoc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi tìm bộ lọc theo tên: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả bộ lọc đã lưu của người dùng theo loại đối tượng.
     */
    public List<BoLocDaLuu> layDanhSachTheoNguoiDung(long nguoiDungId, String loaiDoiTuong) {
        List<BoLocDaLuu> danhSach = new ArrayList<>();
        String sql = "SELECT id, nguoi_dung_id, loai_doi_tuong, ten_bo_loc, tieu_chi_json, mac_dinh, created_at, updated_at " +
                "FROM bo_loc_da_luu WHERE nguoi_dung_id = ? AND loai_doi_tuong = ? " +
                "ORDER BY mac_dinh DESC, updated_at DESC, id DESC";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setString(2, loaiDoiTuong != null ? loaiDoiTuong : BoLocDaLuu.LOAI_KHACH_HANG);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách bộ lọc đã lưu của người dùng: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm bộ lọc mặc định của người dùng cho loại đối tượng.
     */
    public BoLocDaLuu timBoLocMacDinh(long nguoiDungId, String loaiDoiTuong) {
        String sql = "SELECT id, nguoi_dung_id, loai_doi_tuong, ten_bo_loc, tieu_chi_json, mac_dinh, created_at, updated_at " +
                "FROM bo_loc_da_luu WHERE nguoi_dung_id = ? AND loai_doi_tuong = ? AND mac_dinh = 1 LIMIT 1";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setString(2, loaiDoiTuong != null ? loaiDoiTuong : BoLocDaLuu.LOAI_KHACH_HANG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi tìm bộ lọc mặc định: " + e.getMessage());
        }
        return null;
    }

    /**
     * Xóa bộ lọc đã lưu của người dùng.
     */
    public boolean xoaBoLoc(long id, long nguoiDungId) throws SQLException {
        String sql = "DELETE FROM bo_loc_da_luu WHERE id = ? AND nguoi_dung_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, nguoiDungId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đặt một bộ lọc làm mặc định (Transaction an toàn):
     * Bước 1: Gỡ bỏ mặc định của tất cả bộ lọc khác của người dùng thuộc đối tượng này.
     * Bước 2: Bật mặc định cho bộ lọc được chỉ định.
     */
    public boolean datMacDinh(long id, long nguoiDungId, String loaiDoiTuong) throws SQLException {
        String sqlBoMacDinh = "UPDATE bo_loc_da_luu SET mac_dinh = 0 WHERE nguoi_dung_id = ? AND loai_doi_tuong = ?";
        String sqlBatMacDinh = "UPDATE bo_loc_da_luu SET mac_dinh = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND nguoi_dung_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlBoMacDinh)) {
                ps1.setLong(1, nguoiDungId);
                ps1.setString(2, loaiDoiTuong != null ? loaiDoiTuong : BoLocDaLuu.LOAI_KHACH_HANG);
                ps1.executeUpdate();
            }

            int rowsUpdated = 0;
            if (id > 0) {
                try (PreparedStatement ps2 = conn.prepareStatement(sqlBatMacDinh)) {
                    ps2.setLong(1, id);
                    ps2.setLong(2, nguoiDungId);
                    rowsUpdated = ps2.executeUpdate();
                }
            }

            conn.commit();
            return id <= 0 || rowsUpdated > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi rollback transaction đặt mặc định: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private BoLocDaLuu mapResultSet(ResultSet rs) throws SQLException {
        BoLocDaLuu item = new BoLocDaLuu();
        item.setId(rs.getLong("id"));
        item.setNguoiDungId(rs.getLong("nguoi_dung_id"));
        item.setLoaiDoiTuong(rs.getString("loai_doi_tuong"));
        item.setTenBoLoc(rs.getString("ten_bo_loc"));
        item.setTieuChiJson(rs.getString("tieu_chi_json"));
        item.setMacDinh(rs.getInt("mac_dinh") == 1);
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));
        return item;
    }
}
