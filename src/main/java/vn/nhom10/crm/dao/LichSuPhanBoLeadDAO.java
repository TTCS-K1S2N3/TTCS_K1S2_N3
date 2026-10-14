package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.HanhDongPhanBoEnum;
import vn.nhom10.crm.model.LichSuPhanBoLead;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng 'lich_su_phan_bo_lead' (Story S4-06).
 */
public class LichSuPhanBoLeadDAO {

    private static final Logger LOGGER = Logger.getLogger(LichSuPhanBoLeadDAO.class.getName());

    private static final String COT_SELECT =
            "ls.id, ls.lead_id, ls.quy_tac_id, ls.nguoi_dung_cu_id, ls.nguoi_dung_moi_id, " +
            "ls.nhom_id, ls.hanh_dong, ls.ly_do, ls.thuc_hien_boi_id, ls.created_at, " +
            "l.ho_ten AS ten_lead, " +
            "qt.ten_quy_tac AS ten_quy_tac, " +
            "ndm.ho_ten AS ten_nguoi_dung_moi, " +
            "th.ho_ten AS ten_thuc_hien_boi ";

    private static final String FROM_JOIN =
            "FROM lich_su_phan_bo_lead ls " +
            "LEFT JOIN `lead` l ON ls.lead_id = l.id " +
            "LEFT JOIN quy_tac_phan_bo_lead qt ON ls.quy_tac_id = qt.id " +
            "LEFT JOIN nguoi_dung ndm ON ls.nguoi_dung_moi_id = ndm.id " +
            "LEFT JOIN nguoi_dung th ON ls.thuc_hien_boi_id = th.id ";

    /**
     * Thêm mới bản ghi lịch sử phân bổ qua Connection (hỗ trợ transaction).
     */
    public Long them(Connection conn, LichSuPhanBoLead ls) throws SQLException {
        String sql = "INSERT INTO lich_su_phan_bo_lead (" +
                "lead_id, quy_tac_id, nguoi_dung_cu_id, nguoi_dung_moi_id, nhom_id, " +
                "hanh_dong, ly_do, thuc_hien_boi_id, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ls.getLeadId());

            if (ls.getQuyTacId() != null) {
                ps.setLong(2, ls.getQuyTacId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            if (ls.getNguoiDungCuId() != null) {
                ps.setLong(3, ls.getNguoiDungCuId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            if (ls.getNguoiDungMoiId() != null) {
                ps.setLong(4, ls.getNguoiDungMoiId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (ls.getNhomId() != null) {
                ps.setLong(5, ls.getNhomId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.setString(6, ls.getHanhDong() != null ? ls.getHanhDong().getMa() : HanhDongPhanBoEnum.TU_DONG_THEO_QUY_TAC.getMa());
            ps.setString(7, ls.getLyDo());

            if (ls.getThucHienBoiId() != null) {
                ps.setLong(8, ls.getThucHienBoiId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }

            LocalDateTime now = ls.getCreatedAt() != null ? ls.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(9, Timestamp.valueOf(now));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    ls.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Long them(LichSuPhanBoLead ls) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return them(conn, ls);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm lịch sử phân bổ lead ID=" + ls.getLeadId(), e);
            throw new RuntimeException("Lỗi lưu lịch sử phân bổ: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy lịch sử phân bổ của một Lead.
     */
    public List<LichSuPhanBoLead> layTheoLeadId(long leadId) {
        List<LichSuPhanBoLead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT + FROM_JOIN + "WHERE ls.lead_id = ? ORDER BY ls.created_at DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangModel(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy lịch sử phân bổ lead ID=" + leadId + ": " + e.getMessage(), e);
        }
        return danhSach;
    }

    private LichSuPhanBoLead mapResultSetSangModel(ResultSet rs) throws SQLException {
        LichSuPhanBoLead item = new LichSuPhanBoLead();
        item.setId(rs.getLong("id"));
        item.setLeadId(rs.getLong("lead_id"));

        long quyTacId = rs.getLong("quy_tac_id");
        item.setQuyTacId(rs.wasNull() ? null : quyTacId);

        long nguoiDungCuId = rs.getLong("nguoi_dung_cu_id");
        item.setNguoiDungCuId(rs.wasNull() ? null : nguoiDungCuId);

        long nguoiDungMoiId = rs.getLong("nguoi_dung_moi_id");
        item.setNguoiDungMoiId(rs.wasNull() ? null : nguoiDungMoiId);

        long nhomId = rs.getLong("nhom_id");
        item.setNhomId(rs.wasNull() ? null : nhomId);

        item.setHanhDong(HanhDongPhanBoEnum.tuChuoi(rs.getString("hanh_dong")));
        item.setLyDo(rs.getString("ly_do"));

        long thucHienBoiId = rs.getLong("thuc_hien_boi_id");
        item.setThucHienBoiId(rs.wasNull() ? null : thucHienBoiId);

        Timestamp tsCreated = rs.getTimestamp("created_at");
        if (tsCreated != null) {
            item.setCreatedAt(tsCreated.toLocalDateTime());
        }

        item.setTenLead(rs.getString("ten_lead"));
        item.setTenQuyTac(rs.getString("ten_quy_tac"));
        item.setTenNguoiDungMoi(rs.getString("ten_nguoi_dung_moi"));
        item.setTenThucHienBoi(rs.getString("ten_thuc_hien_boi"));

        return item;
    }
}
