package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.KieuPhanBoEnum;
import vn.nhom10.crm.model.QuyTacPhanBoLead;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng 'quy_tac_phan_bo_lead' (Story S4-06).
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, xử lý transaction an toàn).
 */
public class QuyTacPhanBoLeadDAO {

    private static final Logger LOGGER = Logger.getLogger(QuyTacPhanBoLeadDAO.class.getName());

    private static final String COT_SELECT =
            "qt.id, qt.ma_quy_tac, qt.ten_quy_tac, qt.thu_tu_uu_tien, qt.kieu_phan_bo, " +
            "qt.dieu_kien_json, qt.nhom_dich_id, qt.nguoi_dung_dich_id, qt.nguoi_dung_lan_cuoi_id, " +
            "qt.hoat_dong, qt.created_by, qt.created_at, qt.updated_at, " +
            "nkd.ten_nhom AS ten_nhom_dich, " +
            "nd.ho_ten AS ten_nguoi_dung_dich, " +
            "ndlc.ho_ten AS ten_nguoi_dung_lan_cuoi ";

    private static final String FROM_JOIN =
            "FROM quy_tac_phan_bo_lead qt " +
            "LEFT JOIN nhom_kinh_doanh nkd ON qt.nhom_dich_id = nkd.id " +
            "LEFT JOIN nguoi_dung nd ON qt.nguoi_dung_dich_id = nd.id " +
            "LEFT JOIN nguoi_dung ndlc ON qt.nguoi_dung_lan_cuoi_id = ndlc.id ";

    /**
     * Lấy danh sách quy tắc phân bổ sắp xếp theo thứ tự ưu tiên tăng dần (thu_tu_uu_tien ASC).
     */
    public List<QuyTacPhanBoLead> layDanhSach(Connection conn, boolean chiLayHoatDong) throws SQLException {
        List<QuyTacPhanBoLead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT + FROM_JOIN;
        if (chiLayHoatDong) {
            sql += "WHERE qt.hoat_dong = 1 ";
        }
        sql += "ORDER BY qt.thu_tu_uu_tien ASC, qt.id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(mapResultSetSangModel(rs));
            }
        }
        return danhSach;
    }

    /**
     * Lấy danh sách quy tắc phân bổ sắp xếp theo thứ tự ưu tiên tăng dần (thu_tu_uu_tien ASC).
     */
    public List<QuyTacPhanBoLead> layDanhSach(boolean chiLayHoatDong) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return layDanhSach(conn, chiLayHoatDong);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách quy tắc phân bổ: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Tìm quy tắc theo ID.
     */
    public QuyTacPhanBoLead timTheoId(long id) {
        String sql = "SELECT " + COT_SELECT + FROM_JOIN + "WHERE qt.id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm quy tắc theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tìm quy tắc theo Mã.
     */
    public QuyTacPhanBoLead timTheoMa(String maQuyTac) {
        if (maQuyTac == null || maQuyTac.isBlank()) return null;
        String sql = "SELECT " + COT_SELECT + FROM_JOIN + "WHERE qt.ma_quy_tac = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maQuyTac.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm quy tắc theo mã=" + maQuyTac + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lấy giá trị thứ tự ưu tiên lớn nhất hiện tại.
     */
    public int layThuTuUuTienLonNhat() {
        String sql = "SELECT COALESCE(MAX(thu_tu_uu_tien), 0) FROM quy_tac_phan_bo_lead";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy MAX thu_tu_uu_tien: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Thêm mới một quy tắc phân bổ (hỗ trợ truyền Connection ngoài phục vụ transaction).
     */
    public Long them(Connection conn, QuyTacPhanBoLead qt) throws SQLException {
        String sql = "INSERT INTO quy_tac_phan_bo_lead (" +
                "ma_quy_tac, ten_quy_tac, thu_tu_uu_tien, kieu_phan_bo, dieu_kien_json, " +
                "nhom_dich_id, nguoi_dung_dich_id, nguoi_dung_lan_cuoi_id, hoat_dong, created_by, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, qt.getMaQuyTac());
            ps.setString(2, qt.getTenQuyTac());
            ps.setInt(3, qt.getThuTuUuTien());
            ps.setString(4, qt.getKieuPhanBo() != null ? qt.getKieuPhanBo().getMa() : KieuPhanBoEnum.THEO_KHU_VUC.getMa());
            ps.setString(5, qt.getDieuKienJson());

            if (qt.getNhomDichId() != null) {
                ps.setLong(6, qt.getNhomDichId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }

            if (qt.getNguoiDungDichId() != null) {
                ps.setLong(7, qt.getNguoiDungDichId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }

            if (qt.getNguoiDungLanCuoiId() != null) {
                ps.setLong(8, qt.getNguoiDungLanCuoiId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }

            ps.setBoolean(9, qt.isHoatDong());

            if (qt.getCreatedBy() != null) {
                ps.setLong(10, qt.getCreatedBy());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            LocalDateTime now = qt.getCreatedAt() != null ? qt.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(11, Timestamp.valueOf(now));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    qt.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Long them(QuyTacPhanBoLead qt) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return them(conn, qt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm quy tắc phân bổ: " + qt.getMaQuyTac(), e);
            throw new RuntimeException("Lỗi lưu quy tắc phân bổ: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin quy tắc phân bổ.
     */
    public boolean capNhat(Connection conn, QuyTacPhanBoLead qt) throws SQLException {
        String sql = "UPDATE quy_tac_phan_bo_lead SET " +
                "ten_quy_tac = ?, kieu_phan_bo = ?, dieu_kien_json = ?, " +
                "nhom_dich_id = ?, nguoi_dung_dich_id = ?, hoat_dong = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qt.getTenQuyTac());
            ps.setString(2, qt.getKieuPhanBo() != null ? qt.getKieuPhanBo().getMa() : KieuPhanBoEnum.THEO_KHU_VUC.getMa());
            ps.setString(3, qt.getDieuKienJson());

            if (qt.getNhomDichId() != null) {
                ps.setLong(4, qt.getNhomDichId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (qt.getNguoiDungDichId() != null) {
                ps.setLong(5, qt.getNguoiDungDichId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setBoolean(6, qt.isHoatDong());
            ps.setLong(7, qt.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(QuyTacPhanBoLead qt) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return capNhat(conn, qt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật quy tắc ID=" + qt.getId(), e);
            throw new RuntimeException("Lỗi cập nhật quy tắc: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật người dùng vừa được gán lần cuối (phục vụ xoay vòng Round-Robin nội bộ nhóm).
     */
    public boolean capNhatNguoiDungLanCuoi(Connection conn, long quyTacId, long nguoiDungId) throws SQLException {
        String sql = "UPDATE quy_tac_phan_bo_lead SET nguoi_dung_lan_cuoi_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setLong(2, quyTacId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bật / Tắt trạng thái hoạt động của quy tắc.
     */
    public boolean capNhatTrangThai(long id, boolean hoatDong) {
        String sql = "UPDATE quy_tac_phan_bo_lead SET hoat_dong = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, hoatDong);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đổi trạng thái quy tắc ID=" + id + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Đổi thứ tự ưu tiên (LÊN hoặc XUỐNG) bằng cách hoán đổi an toàn với quy tắc liền kề.
     */
    public boolean doiThuTuUuTien(long quyTacId, String huongDiChuyen) {
        QuyTacPhanBoLead current = timTheoId(quyTacId);
        if (current == null) return false;

        int currentOrder = current.getThuTuUuTien();
        String sqlFindAdjacent;
        if ("LEN".equalsIgnoreCase(huongDiChuyen)) {
            sqlFindAdjacent = "SELECT id, thu_tu_uu_tien FROM quy_tac_phan_bo_lead WHERE thu_tu_uu_tien < ? ORDER BY thu_tu_uu_tien DESC LIMIT 1";
        } else if ("XUONG".equalsIgnoreCase(huongDiChuyen)) {
            sqlFindAdjacent = "SELECT id, thu_tu_uu_tien FROM quy_tac_phan_bo_lead WHERE thu_tu_uu_tien > ? ORDER BY thu_tu_uu_tien ASC LIMIT 1";
        } else {
            return false;
        }

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            try {
                Long adjacentId = null;
                int adjacentOrder = 0;

                try (PreparedStatement ps = conn.prepareStatement(sqlFindAdjacent)) {
                    ps.setInt(1, currentOrder);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            adjacentId = rs.getLong("id");
                            adjacentOrder = rs.getInt("thu_tu_uu_tien");
                        }
                    }
                }

                if (adjacentId == null) {
                    // Đã ở đầu hoặc cuối danh sách, không cần hoán đổi
                    conn.rollback();
                    return true;
                }

                // Dùng giá trị tạm thời âm để tránh trùng Unique Key `uk_qtpb_tt`
                try (PreparedStatement psTemp = conn.prepareStatement("UPDATE quy_tac_phan_bo_lead SET thu_tu_uu_tien = -99999 WHERE id = ?");
                     PreparedStatement psSwap1 = conn.prepareStatement("UPDATE quy_tac_phan_bo_lead SET thu_tu_uu_tien = ? WHERE id = ?");
                     PreparedStatement psSwap2 = conn.prepareStatement("UPDATE quy_tac_phan_bo_lead SET thu_tu_uu_tien = ? WHERE id = ?")) {

                    psTemp.setLong(1, current.getId());
                    psTemp.executeUpdate();

                    psSwap1.setInt(1, currentOrder);
                    psSwap1.setLong(2, adjacentId);
                    psSwap1.executeUpdate();

                    psSwap2.setInt(1, adjacentOrder);
                    psSwap2.setLong(2, current.getId());
                    psSwap2.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Lỗi hoán đổi thứ tự ưu tiên quy tắc ID=" + quyTacId, ex);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối khi đổi thứ tự quy tắc: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa một quy tắc phân bổ.
     */
    public boolean xoa(long id) {
        String sql = "DELETE FROM quy_tac_phan_bo_lead WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa quy tắc ID=" + id + ": " + e.getMessage(), e);
            return false;
        }
    }

    private QuyTacPhanBoLead mapResultSetSangModel(ResultSet rs) throws SQLException {
        QuyTacPhanBoLead item = new QuyTacPhanBoLead();
        item.setId(rs.getLong("id"));
        item.setMaQuyTac(rs.getString("ma_quy_tac"));
        item.setTenQuyTac(rs.getString("ten_quy_tac"));
        item.setThuTuUuTien(rs.getInt("thu_tu_uu_tien"));
        item.setKieuPhanBo(KieuPhanBoEnum.tuChuoi(rs.getString("kieu_phan_bo")));
        item.setDieuKienJson(rs.getString("dieu_kien_json"));

        long nhomId = rs.getLong("nhom_dich_id");
        item.setNhomDichId(rs.wasNull() ? null : nhomId);

        long nguoiDungDichId = rs.getLong("nguoi_dung_dich_id");
        item.setNguoiDungDichId(rs.wasNull() ? null : nguoiDungDichId);

        long nguoiDungLanCuoiId = rs.getLong("nguoi_dung_lan_cuoi_id");
        item.setNguoiDungLanCuoiId(rs.wasNull() ? null : nguoiDungLanCuoiId);

        item.setHoatDong(rs.getBoolean("hoat_dong"));

        long createdBy = rs.getLong("created_by");
        item.setCreatedBy(rs.wasNull() ? null : createdBy);

        Timestamp tsCreated = rs.getTimestamp("created_at");
        if (tsCreated != null) {
            item.setCreatedAt(tsCreated.toLocalDateTime());
        }

        Timestamp tsUpdated = rs.getTimestamp("updated_at");
        if (tsUpdated != null) {
            item.setUpdatedAt(tsUpdated.toLocalDateTime());
        }

        item.setTenNhomDich(rs.getString("ten_nhom_dich"));
        item.setTenNguoiDungDich(rs.getString("ten_nguoi_dung_dich"));
        item.setTenNguoiDungLanCuoi(rs.getString("ten_nguoi_dung_lan_cuoi"));

        return item;
    }
}
