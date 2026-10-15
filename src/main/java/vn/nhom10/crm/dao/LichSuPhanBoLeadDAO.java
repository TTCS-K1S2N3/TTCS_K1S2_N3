package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.LichSuPhanBoLead;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng `lich_su_phan_bo_lead` (Schema 001).
 * Ghi nhận lịch sử nhận lead, từ chối lead và phân bổ.
 */
public class LichSuPhanBoLeadDAO {

    private static final Logger LOGGER = Logger.getLogger(LichSuPhanBoLeadDAO.class.getName());

    public Long them(Connection conn, LichSuPhanBoLead ls) throws SQLException {
        if (ls == null || ls.getLeadId() == null || ls.getHanhDong() == null) {
            return null;
        }

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

            ps.setString(6, ls.getHanhDong());
            ps.setString(7, ls.getLyDo());

            if (ls.getThucHienBoiId() != null) {
                ps.setLong(8, ls.getThucHienBoiId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }

            LocalDateTime createdAt = ls.getCreatedAt() != null ? ls.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(9, Timestamp.valueOf(createdAt));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        ls.setId(id);
                        return id;
                    }
                }
            }
        }
        return null;
    }

    public Long them(LichSuPhanBoLead ls) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return them(conn, ls);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm lịch sử phân bổ lead: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi lưu lịch sử phân bổ: " + e.getMessage(), e);
        }
    }

    public List<LichSuPhanBoLead> layLichSuTheoLead(Long leadId) {
        List<LichSuPhanBoLead> ds = new ArrayList<>();
        if (leadId == null || leadId <= 0) {
            return ds;
        }

        String sql = "SELECT id, lead_id, quy_tac_id, nguoi_dung_cu_id, nguoi_dung_moi_id, nhom_id, " +
                "hanh_dong, ly_do, thuc_hien_boi_id, created_at " +
                "FROM lich_su_phan_bo_lead WHERE lead_id = ? ORDER BY created_at DESC, id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuPhanBoLead item = new LichSuPhanBoLead();
                    item.setId(rs.getLong("id"));
                    item.setLeadId(rs.getLong("lead_id"));

                    long qtId = rs.getLong("quy_tac_id");
                    if (!rs.wasNull()) item.setQuyTacId(qtId);

                    long cuId = rs.getLong("nguoi_dung_cu_id");
                    if (!rs.wasNull()) item.setNguoiDungCuId(cuId);

                    long moiId = rs.getLong("nguoi_dung_moi_id");
                    if (!rs.wasNull()) item.setNguoiDungMoiId(moiId);

                    long nhomId = rs.getLong("nhom_id");
                    if (!rs.wasNull()) item.setNhomId(nhomId);

                    item.setHanhDong(rs.getString("hanh_dong"));
                    item.setLyDo(rs.getString("ly_do"));

                    long thId = rs.getLong("thuc_hien_boi_id");
                    if (!rs.wasNull()) item.setThucHienBoiId(thId);

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) item.setCreatedAt(ts.toLocalDateTime());

                    ds.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn lịch sử phân bổ lead: " + e.getMessage(), e);
        }

        return ds;
    }
}
