package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyBanGiao;
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
 * DAO xử lý truy vấn dữ liệu khách hàng, cơ hội và lưu vết nhật ký bàn giao (Story S1-10).
 * Cung cấp các hàm thực thi nhận Connection phục vụ Transaction an toàn.
 */
public class BanGiaoDuLieuDAO {

    private static final Logger LOGGER = Logger.getLogger(BanGiaoDuLieuDAO.class.getName());

    /**
     * Đếm số lượng khách hàng mà nhân viên đang sở hữu/phụ trách.
     */
    public int demKhachHangCuaUser(Connection conn, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM khach_hang WHERE nguoi_so_huu_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không đếm được khách hàng (bảng có thể chưa tạo): " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số lượng khách hàng tự động mở kết nối.
     */
    public int demKhachHangCuaUser(int userId) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return demKhachHangCuaUser(conn, userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đếm khách hàng của user " + userId + ": " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Đếm số lượng cơ hội mà nhân viên đang phụ trách.
     */
    public int demCoHoiCuaUser(Connection conn, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM co_hoi WHERE nguoi_phu_trach_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không đếm được cơ hội (bảng có thể chưa tạo): " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số lượng cơ hội tự động mở kết nối.
     */
    public int demCoHoiCuaUser(int userId) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return demCoHoiCuaUser(conn, userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đếm cơ hội của user " + userId + ": " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Chuyển quyền sở hữu toàn bộ khách hàng từ nhân viên nghỉ việc sang nhân viên tiếp nhận.
     */
    public int chuyenKhachHang(Connection conn, int tuUserId, int sangUserId) throws SQLException {
        String sql = "UPDATE khach_hang SET nguoi_so_huu_id = ? WHERE nguoi_so_huu_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sangUserId);
            ps.setInt(2, tuUserId);
            return ps.executeUpdate();
        }
    }

    /**
     * Chuyển quyền phụ trách toàn bộ cơ hội từ nhân viên nghỉ việc sang nhân viên tiếp nhận.
     */
    public int chuyenCoHoi(Connection conn, int tuUserId, int sangUserId) throws SQLException {
        String sql = "UPDATE co_hoi SET nguoi_phu_trach_id = ? WHERE nguoi_phu_trach_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sangUserId);
            ps.setInt(2, tuUserId);
            return ps.executeUpdate();
        }
    }

    /**
     * Ghi nhận lịch sử bàn giao vào bảng nhat_ky_ban_giao.
     */
    public int ghiNhatKyBanGiao(Connection conn, NhatKyBanGiao nk) throws SQLException {
        String sql = "INSERT INTO nhat_ky_ban_giao (nguoi_bi_khoa_id, nguoi_tiep_nhan_id, nguoi_thuc_hien_id, " +
                     "so_khach_hang_chuyen, so_co_hoi_chuyen, ly_do) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, nk.getNguoiBiKhoaId());
            ps.setInt(2, nk.getNguoiTiepNhanId());
            ps.setInt(3, nk.getNguoiThucHienId());
            ps.setInt(4, nk.getSoKhachHangChuyen());
            ps.setInt(5, nk.getSoCoHoiChuyen());
            ps.setString(6, nk.getLyDo());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    nk.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    /**
     * Lấy danh sách lịch sử bàn giao gần đây.
     */
    public List<NhatKyBanGiao> layLichSuBanGiao(int limit) {
        List<NhatKyBanGiao> danhSach = new ArrayList<>();
        String sql = "SELECT nk.id, nk.nguoi_bi_khoa_id, nk.nguoi_tiep_nhan_id, nk.nguoi_thuc_hien_id, " +
                     "nk.so_khach_hang_chuyen, nk.so_co_hoi_chuyen, nk.ly_do, nk.created_at, " +
                     "u1.ho_ten AS ten_nguoi_khoa, u1.email AS email_nguoi_khoa, " +
                     "u2.ho_ten AS ten_nguoi_nhan, u2.email AS email_nguoi_nhan, " +
                     "u3.ho_ten AS ten_nguoi_thuc_hien " +
                     "FROM nhat_ky_ban_giao nk " +
                     "LEFT JOIN nguoi_dung u1 ON nk.nguoi_bi_khoa_id = u1.id " +
                     "LEFT JOIN nguoi_dung u2 ON nk.nguoi_tiep_nhan_id = u2.id " +
                     "LEFT JOIN nguoi_dung u3 ON nk.nguoi_thuc_hien_id = u3.id " +
                     "ORDER BY nk.id DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 20);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhatKyBanGiao nk = new NhatKyBanGiao();
                    nk.setId(rs.getInt("id"));
                    nk.setNguoiBiKhoaId(rs.getInt("nguoi_bi_khoa_id"));
                    nk.setNguoiTiepNhanId(rs.getInt("nguoi_tiep_nhan_id"));
                    nk.setNguoiThucHienId(rs.getInt("nguoi_thuc_hien_id"));
                    nk.setSoKhachHangChuyen(rs.getInt("so_khach_hang_chuyen"));
                    nk.setSoCoHoiChuyen(rs.getInt("so_co_hoi_chuyen"));
                    nk.setLyDo(rs.getString("ly_do"));
                    nk.setCreatedAt(rs.getTimestamp("created_at"));

                    NguoiDung u1 = new NguoiDung(nk.getNguoiBiKhoaId(), rs.getString("ten_nguoi_khoa"), rs.getString("email_nguoi_khoa"));
                    NguoiDung u2 = new NguoiDung(nk.getNguoiTiepNhanId(), rs.getString("ten_nguoi_nhan"), rs.getString("email_nguoi_nhan"));
                    NguoiDung u3 = new NguoiDung(nk.getNguoiThucHienId(), rs.getString("ten_nguoi_thuc_hien"), "");

                    nk.setNguoiBiKhoa(u1);
                    nk.setNguoiTiepNhan(u2);
                    nk.setNguoiThucHien(u3);

                    danhSach.add(nk);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy lịch sử bàn giao: " + e.getMessage(), e);
        }
        return danhSach;
    }
}
