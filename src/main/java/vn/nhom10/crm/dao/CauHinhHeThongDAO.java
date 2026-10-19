package vn.nhom10.crm.dao;

import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng `cau_hinh_he_thong` (Story S3-08).
 * Dùng để đọc cấu hình ngưỡng số yêu cầu chưa xử lý để gắn cờ rủi ro rời bỏ
 * (`NGUONG_YEU_CAU_HO_TRO_RUI_RO`).
 */
public class CauHinhHeThongDAO {

    private static final Logger LOGGER = Logger.getLogger(CauHinhHeThongDAO.class.getName());

    public static final String NGUONG_YEU_CAU_HO_TRO_RUI_RO = "NGUONG_YEU_CAU_HO_TRO_RUI_RO";
    public static final int NGUONG_RUI_RO_MAC_DINH = 3;

    /**
     * Lấy giá trị cấu hình dạng số nguyên. Nếu không tồn tại hoặc giá trị là NULL, trả về `defaultVal`.
     */
    public int layGiaTriInt(String maCauHinh, int defaultVal) {
        if (maCauHinh == null || maCauHinh.isBlank()) {
            return defaultVal;
        }

        String sql = "SELECT gia_tri FROM cau_hinh_he_thong WHERE ma_cau_hinh = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCauHinh.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String strVal = rs.getString("gia_tri");
                    if (strVal != null && !strVal.isBlank()) {
                        try {
                            int val = Integer.parseInt(strVal.trim());
                            if (val > 0) {
                                return val;
                            }
                        } catch (NumberFormatException e) {
                            LOGGER.log(Level.WARNING, "Không thể parse giá trị cấu hình số: " + strVal, e);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi truy vấn cấu hình hệ thống [" + maCauHinh + "]: " + e.getMessage(), e);
        }

        return defaultVal;
    }

    /**
     * Cập nhật hoặc lưu giá trị cấu hình.
     */
    public boolean datGiaTri(String maCauHinh, String giaTri, Long updatedBy) {
        if (maCauHinh == null || maCauHinh.isBlank()) {
            return false;
        }

        String sqlUpdate = "UPDATE cau_hinh_he_thong SET gia_tri = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE ma_cau_hinh = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setString(1, giaTri);
            if (updatedBy != null && updatedBy > 0) {
                ps.setLong(2, updatedBy);
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, maCauHinh.trim());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                return true;
            }

            // Nếu chưa có thì chèn mới
            String sqlInsert = "INSERT INTO cau_hinh_he_thong (ma_cau_hinh, nhom_cau_hinh, kieu_du_lieu, gia_tri, updated_by) " +
                    "VALUES (?, 'KHACH_HANG', 'INTEGER', ?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                psInsert.setString(1, maCauHinh.trim());
                psInsert.setString(2, giaTri);
                if (updatedBy != null && updatedBy > 0) {
                    psInsert.setLong(3, updatedBy);
                } else {
                    psInsert.setNull(3, java.sql.Types.BIGINT);
                }
                return psInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu cấu hình hệ thống: " + e.getMessage(), e);
            return false;
        }
    }
}
