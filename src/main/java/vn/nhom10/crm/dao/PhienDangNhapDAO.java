package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.PhienDangNhap;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý phiên đăng nhập trong bảng phien_dang_nhap.
 * Hỗ trợ Story S1-02 (quản lý phiên) và S1-04 (thu hồi các phiên khác khi đổi mật khẩu).
 */
public class PhienDangNhapDAO {

    private static final Logger LOGGER = Logger.getLogger(PhienDangNhapDAO.class.getName());

    /**
     * Lưu phiên đăng nhập mới vào database.
     *
     * @param phien Bản ghi phiên đăng nhập
     */
    public void luuPhien(PhienDangNhap phien) {
        if (phien == null || phien.getMaPhien() == null) {
            return;
        }

        String sql = "INSERT INTO phien_dang_nhap (nguoi_dung_id, ma_phien, dia_chi_ip, "
                + "thong_tin_thiet_bi, trang_thai, thoi_gian_tao, thoi_gian_hoat_dong_cuoi) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setLong(1, phien.getNguoiDungId());
            ps.setString(2, phien.getMaPhien());
            ps.setString(3, phien.getDiaChiIp());
            ps.setString(4, phien.getThongTinThietBi());
            ps.setString(5, phien.getTrangThai() != null ? phien.getTrangThai() : PhienDangNhap.TRANG_THAI_HOAT_DONG);
            ps.setTimestamp(6, phien.getThoiGianTao() != null ? phien.getThoiGianTao() : now);
            ps.setTimestamp(7, phien.getThoiGianHoatDongCuoi() != null ? phien.getThoiGianHoatDongCuoi() : now);

            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu phiên đăng nhập: " + phien.getMaPhien(), e);
        }
    }

    /**
     * Tìm phiên đăng nhập theo mã phiên (Session ID).
     *
     * @param maPhien Mã phiên đăng nhập
     * @return PhienDangNhap hoặc null nếu không tìm thấy
     */
    public PhienDangNhap timTheoMaPhien(String maPhien) {
        if (maPhien == null || maPhien.isBlank()) {
            return null;
        }

        String sql = "SELECT id, nguoi_dung_id, ma_phien, dia_chi_ip, thong_tin_thiet_bi, "
                + "trang_thai, thoi_gian_tao, thoi_gian_hoat_dong_cuoi, thoi_gian_thu_hoi "
                + "FROM phien_dang_nhap WHERE ma_phien = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhien.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhienDangNhap p = new PhienDangNhap();
                    p.setId(rs.getLong("id"));
                    p.setNguoiDungId(rs.getLong("nguoi_dung_id"));
                    p.setMaPhien(rs.getString("ma_phien"));
                    p.setDiaChiIp(rs.getString("dia_chi_ip"));
                    p.setThongTinThietBi(rs.getString("thong_tin_thiet_bi"));
                    p.setTrangThai(rs.getString("trang_thai"));
                    p.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
                    p.setThoiGianHoatDongCuoi(rs.getTimestamp("thoi_gian_hoat_dong_cuoi"));
                    p.setThoiGianThuHoi(rs.getTimestamp("thoi_gian_thu_hoi"));
                    return p;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm phiên theo mã: " + maPhien, e);
        }
        return null;
    }

    /**
     * Gia hạn tự động thời gian hoạt động cuối của phiên khi người dùng gửi request/hoạt động.
     * Đáp ứng AC1: "Phiên được gia hạn tự động khi còn hoạt động".
     *
     * @param maPhien Mã phiên đăng nhập
     * @return true nếu gia hạn thành công, false nếu phiên không tồn tại hoặc không còn HOAT_DONG
     */
    public boolean capNhatHoatDongCuoi(String maPhien) {
        if (maPhien == null || maPhien.isBlank()) {
            return false;
        }

        String sql = "UPDATE phien_dang_nhap SET thoi_gian_hoat_dong_cuoi = ? "
                + "WHERE ma_phien = ? AND trang_thai = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString(2, maPhien.trim());
            ps.setString(3, PhienDangNhap.TRANG_THAI_HOAT_DONG);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi gia hạn thời gian hoạt động cuối cho phiên: " + maPhien, e);
            return false;
        }
    }

    /**
     * Vô hiệu hóa phiên làm việc ngay lập tức.
     * Đáp ứng AC2: "Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server".
     *
     * @param maPhien       Mã phiên đăng nhập
     * @param trangThaiMoi  Trạng thái mới (ví dụ DA_DANG_XUAT, HET_HAN, DA_THU_HOI)
     */
    public void voHieuHoaPhien(String maPhien, String trangThaiMoi) {
        if (maPhien == null || maPhien.isBlank()) {
            return;
        }

        String sql = "UPDATE phien_dang_nhap SET trang_thai = ?, thoi_gian_thu_hoi = ? "
                + "WHERE ma_phien = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThaiMoi != null ? trangThaiMoi : PhienDangNhap.TRANG_THAI_DA_DANG_XUAT);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, maPhien.trim());

            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thu hồi phiên đăng nhập: " + maPhien, e);
        }
    }

    /**
     * Quét và cập nhật trạng thái các phiên không hoạt động quá số phút timeout thành HET_HAN.
     * Đáp ứng AC3: "Phiên hết hạn".
     *
     * @param timeoutPhut Thời gian timeout (ví dụ 30 phút)
     */
    public void quetVaCapNhatPhienHetHan(int timeoutPhut) {
        String sql = "UPDATE phien_dang_nhap SET trang_thai = ?, thoi_gian_thu_hoi = ? "
                + "WHERE trang_thai = ? AND thoi_gian_hoat_dong_cuoi < ?";

        Timestamp nguongHetHan = new Timestamp(System.currentTimeMillis() - timeoutPhut * 60 * 1000L);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PhienDangNhap.TRANG_THAI_HET_HAN);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, PhienDangNhap.TRANG_THAI_HOAT_DONG);
            ps.setTimestamp(4, nguongHetHan);

            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi quét và cập nhật phiên hết hạn", e);
        }
    }

    /**
     * AC 3 (S1-04): Thu hồi toàn bộ các phiên khác của người dùng ngoại trừ phiên hiện tại.
     * Thực hiện trong connection/transaction đã được truyền vào.
     *
     * @param nguoiDungId     ID người dùng đổi mật khẩu
     * @param maPhienHienTai  Mã phiên hiện tại (không thu hồi)
     * @param conn            Kết nối JDBC đang quản lý transaction
     * @return số lượng phiên đã thu hồi trong DB
     * @throws SQLException khi lỗi truy vấn SQL
     */
    public int thuHoiCacPhienKhac(long nguoiDungId, String maPhienHienTai, Connection conn) throws SQLException {
        String sql = "UPDATE phien_dang_nhap "
                   + "SET trang_thai = ?, thoi_gian_thu_hoi = ? "
                   + "WHERE nguoi_dung_id = ? AND ma_phien <> ? AND trang_thai = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PhienDangNhap.TRANG_THAI_DA_THU_HOI);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setLong(3, nguoiDungId);
            ps.setString(4, maPhienHienTai != null ? maPhienHienTai : "");
            ps.setString(5, PhienDangNhap.TRANG_THAI_HOAT_DONG);
            return ps.executeUpdate();
        }
    }

    /**
     * Overload thu hồi các phiên khác tự mở kết nối (khi không dùng transaction riêng).
     */
    public int thuHoiCacPhienKhac(long nguoiDungId, String maPhienHienTai) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return thuHoiCacPhienKhac(nguoiDungId, maPhienHienTai, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thu hồi các phiên khác cho user: " + nguoiDungId, e);
            return 0;
        }
    }
}
