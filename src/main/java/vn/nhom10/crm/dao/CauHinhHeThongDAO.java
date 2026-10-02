package vn.nhom10.crm.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Truy vấn cấu hình hệ thống từ bảng cau_hinh_he_thong.
 */
public class CauHinhHeThongDAO {

    private static final Logger logger = LoggerFactory.getLogger(CauHinhHeThongDAO.class);

    public int layGiaTriInt(String maCauHinh, int macDinh) {
        String sql = "SELECT gia_tri FROM cau_hinh_he_thong WHERE ma_cau_hinh = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCauHinh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("gia_tri");
                    if (val != null && !val.isBlank()) {
                        return Integer.parseInt(val.trim());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi đọc cấu hình {}: {}", maCauHinh, e.getMessage());
        } catch (NumberFormatException e) {
            logger.warn("Cấu hình {} có giá trị không phải số nguyên, sử dụng mặc định {}", maCauHinh, macDinh);
        }
        return macDinh;
    }

    public String layGiaTriString(String maCauHinh, String macDinh) {
        String sql = "SELECT gia_tri FROM cau_hinh_he_thong WHERE ma_cau_hinh = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCauHinh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("gia_tri");
                    if (val != null) {
                        return val;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi đọc cấu hình {}: {}", maCauHinh, e.getMessage());
        }
        return macDinh;
    }
}
