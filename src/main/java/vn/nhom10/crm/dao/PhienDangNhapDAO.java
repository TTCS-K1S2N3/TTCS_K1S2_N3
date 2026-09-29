package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.PhienDangNhap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object cho bảng phien_dang_nhap.
 * Quản lý phiên đăng nhập và thu hồi phiên (AC 3).
 */
public class PhienDangNhapDAO {

    private static final Logger LOGGER = Logger.getLogger(PhienDangNhapDAO.class.getName());

    /**
     * Lưu thông tin phiên đăng nhập mới.
     *
     * @param phien thông tin phiên
     * @param conn  kết nối JDBC (nếu null sẽ lấy từ DatabaseConfig)
     * @return ID phiên vừa lưu
     */
    public Long save(PhienDangNhap phien, Connection conn) throws SQLException {
        boolean autoClose = false;
        if (conn == null) {
            conn = DatabaseConfig.getConnection();
            autoClose = true;
        }

        String sql = "INSERT INTO phien_dang_nhap (nguoi_dung_id, ma_phien, dia_chi_ip, thong_tin_thiet_bi, "
                   + "       trang_thai, thoi_gian_tao, thoi_gian_hoat_dong_cuoi) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setLong(1, phien.getNguoiDungId());
            ps.setString(2, phien.getMaPhien());
            ps.setString(3, phien.getDiaChiIp());
            ps.setString(4, phien.getThongTinThietBi());
            ps.setString(5, phien.getTrangThai() != null ? phien.getTrangThai() : PhienDangNhap.TRANG_THAI_HOAT_DONG);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        phien.setId(id);
                        return id;
                    }
                }
            }
        } finally {
            if (autoClose) {
                conn.close();
            }
        }
        return null;
    }

    /**
     * Tìm phiên đăng nhập theo mã phiên (session ID).
     *
     * @param maPhien mã định danh phiên
     * @return PhienDangNhap nếu tìm thấy, null nếu không có
     */
    public PhienDangNhap findByMaPhien(String maPhien) {
        if (maPhien == null || maPhien.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, nguoi_dung_id, ma_phien, dia_chi_ip, thong_tin_thiet_bi, "
                   + "       trang_thai, thoi_gian_tao, thoi_gian_hoat_dong_cuoi, thoi_gian_thu_hoi "
                   + "FROM phien_dang_nhap WHERE ma_phien = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPhienDangNhap(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm phiên theo mã phiên: " + maPhien, e);
        }
        return null;
    }

    /**
     * Lấy danh sách các phiên còn hoạt động của người dùng.
     *
     * @param nguoiDungId ID người dùng
     * @return danh sách PhienDangNhap
     */
    public List<PhienDangNhap> findActiveByNguoiDungId(Long nguoiDungId) {
        List<PhienDangNhap> ds = new ArrayList<>();
        if (nguoiDungId == null) {
            return ds;
        }

        String sql = "SELECT id, nguoi_dung_id, ma_phien, dia_chi_ip, thong_tin_thiet_bi, "
                   + "       trang_thai, thoi_gian_tao, thoi_gian_hoat_dong_cuoi, thoi_gian_thu_hoi "
                   + "FROM phien_dang_nhap WHERE nguoi_dung_id = ? AND trang_thai = 'HOAT_DONG' "
                   + "ORDER BY thoi_gian_hoat_dong_cuoi DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRowToPhienDangNhap(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách phiên hoạt động của user: " + nguoiDungId, e);
        }
        return ds;
    }

    /**
     * Cập nhật thời gian hoạt động cuối của phiên.
     *
     * @param maPhien mã phiên
     */
    public void updateHoatDongCuoi(String maPhien) {
        if (maPhien == null || maPhien.trim().isEmpty()) {
            return;
        }

        String sql = "UPDATE phien_dang_nhap SET thoi_gian_hoat_dong_cuoi = ? WHERE ma_phien = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, maPhien.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể cập nhật hoạt động cuối cho phiên: " + maPhien, e);
        }
    }

    /**
     * AC 3: Thu hồi toàn bộ các phiên khác của người dùng ngoại trừ phiên hiện tại.
     * Thực hiện trong transaction.
     *
     * @param nguoiDungId     ID người dùng
     * @param maPhienHienTai  mã phiên hiện tại (không thu hồi)
     * @param conn            kết nối JDBC thuộc transaction
     * @return số lượng phiên đã thu hồi trong DB
     * @throws SQLException khi lỗi truy vấn
     */
    public int thuHoiCacPhienKhac(Long nguoiDungId, String maPhienHienTai, Connection conn) throws SQLException {
        if (nguoiDungId == null) {
            return 0;
        }

        String sql = "UPDATE phien_dang_nhap "
                   + "SET trang_thai = ?, thoi_gian_thu_hoi = ? "
                   + "WHERE nguoi_dung_id = ? AND ma_phien <> ? AND trang_thai = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PhienDangNhap.TRANG_THAI_DA_THU_HOI);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, nguoiDungId);
            ps.setString(4, maPhienHienTai != null ? maPhienHienTai : "");
            ps.setString(5, PhienDangNhap.TRANG_THAI_HOAT_DONG);
            return ps.executeUpdate();
        }
    }

    /**
     * Thu hồi một phiên cụ thể (khi người dùng đăng xuất).
     *
     * @param maPhien mã phiên
     * @param conn    kết nối JDBC
     * @return true nếu thu hồi thành công
     */
    public boolean thuHoiPhien(String maPhien, Connection conn) throws SQLException {
        boolean autoClose = false;
        if (conn == null) {
            conn = DatabaseConfig.getConnection();
            autoClose = true;
        }

        String sql = "UPDATE phien_dang_nhap SET trang_thai = ?, thoi_gian_thu_hoi = ? WHERE ma_phien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PhienDangNhap.TRANG_THAI_DA_THU_HOI);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, maPhien);
            return ps.executeUpdate() > 0;
        } finally {
            if (autoClose) {
                conn.close();
            }
        }
    }

    private PhienDangNhap mapRowToPhienDangNhap(ResultSet rs) throws SQLException {
        PhienDangNhap phien = new PhienDangNhap();
        phien.setId(rs.getLong("id"));
        phien.setNguoiDungId(rs.getLong("nguoi_dung_id"));
        phien.setMaPhien(rs.getString("ma_phien"));
        phien.setDiaChiIp(rs.getString("dia_chi_ip"));
        phien.setThongTinThietBi(rs.getString("thong_tin_thiet_bi"));
        phien.setTrangThai(rs.getString("trang_thai"));

        Timestamp taoTs = rs.getTimestamp("thoi_gian_tao");
        if (taoTs != null) {
            phien.setThoiGianTao(taoTs.toLocalDateTime());
        }

        Timestamp hoatDongTs = rs.getTimestamp("thoi_gian_hoat_dong_cuoi");
        if (hoatDongTs != null) {
            phien.setThoiGianHoatDongCuoi(hoatDongTs.toLocalDateTime());
        }

        Timestamp thuHoiTs = rs.getTimestamp("thoi_gian_thu_hoi");
        if (thuHoiTs != null) {
            phien.setThoiGianThuHoi(thuHoiTs.toLocalDateTime());
        }

        return phien;
    }
}
