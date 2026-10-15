package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng `hoat_dong` phục vụ kiểm tra bằng chứng liên hệ (Story S4-07, AC3, Q2).
 * Các loại hoạt động được công nhận là liên hệ: CUOC_GOI, EMAIL, CUOC_HOP với trạng thái HOAN_THANH.
 */
public class HoatDongDAO {

    private static final Logger LOGGER = Logger.getLogger(HoatDongDAO.class.getName());

    /**
     * Kiểm tra xem lead đã có hoạt động liên hệ hợp lệ (CUOC_GOI, EMAIL, CUOC_HOP ở trạng thái HOAN_THANH)
     * trong khoảng thời gian từ lúc phân bổ đến thời điểm kiểm tra / hạn SLA hay chưa.
     */
    public boolean coHoatDongLienHeHopLe(Long leadId, LocalDateTime tuLuc, LocalDateTime denLuc) {
        if (leadId == null || leadId <= 0) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM hoat_dong WHERE lead_id = ? ")
                .append("AND loai_hoat_dong IN ('CUOC_GOI', 'EMAIL', 'CUOC_HOP') ")
                .append("AND UPPER(trang_thai) = 'HOAN_THANH' ");

        if (tuLuc != null) {
            sql.append("AND (COALESCE(thoi_gian_bat_dau, created_at) >= ?) ");
        }
        if (denLuc != null) {
            sql.append("AND (COALESCE(thoi_gian_bat_dau, created_at) <= ?) ");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setLong(1, leadId);
            int idx = 2;
            if (tuLuc != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(tuLuc));
            }
            if (denLuc != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(denLuc));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra hoạt động liên hệ của lead " + leadId + ": " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Lấy mốc thời gian của hoạt động liên hệ đầu tiên sau khi được phân bổ.
     */
    public LocalDateTime layMocLienHeDauTien(Long leadId, LocalDateTime tuLuc) {
        if (leadId == null || leadId <= 0) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT COALESCE(thoi_gian_bat_dau, created_at) AS moc_lien_he ")
                .append("FROM hoat_dong WHERE lead_id = ? ")
                .append("AND loai_hoat_dong IN ('CUOC_GOI', 'EMAIL', 'CUOC_HOP') ")
                .append("AND UPPER(trang_thai) = 'HOAN_THANH' ");

        if (tuLuc != null) {
            sql.append("AND (COALESCE(thoi_gian_bat_dau, created_at) >= ?) ");
        }
        sql.append("ORDER BY COALESCE(thoi_gian_bat_dau, created_at) ASC LIMIT 1");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setLong(1, leadId);
            if (tuLuc != null) {
                ps.setTimestamp(2, Timestamp.valueOf(tuLuc));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("moc_lien_he");
                    if (ts != null) {
                        return ts.toLocalDateTime();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy mốc liên hệ đầu tiên của lead " + leadId + ": " + e.getMessage(), e);
        }

        return null;
    }
}
