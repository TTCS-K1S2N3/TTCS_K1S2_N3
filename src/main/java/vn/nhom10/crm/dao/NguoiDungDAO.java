package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object phụ trách truy vấn và cập nhật dữ liệu bảng nguoi_dung.
 */
public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    /**
     * Tìm người dùng theo địa chỉ email (không phân biệt chữ hoa/thường).
     * Tự động nạp danh sách vai trò kèm theo.
     *
     * @param email Địa chỉ email công ty
     * @return NguoiDung nếu tìm thấy, hoặc null nếu không tồn tại
     */
    public NguoiDung timTheoEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                + "so_lan_sai, thoi_gian_khoa, nhom_kinh_doanh_id, created_at, updated_at "
                + "FROM nguoi_dung WHERE LOWER(email) = LOWER(?) LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nd = mapResultSetToNguoiDung(rs);
                    nd.setDanhSachVaiTro(layDanhSachVaiTroTheoNguoiDungId(nd.getId()));
                    return nd;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn người dùng theo email: " + email, e);
        }
        return null;
    }

    /**
     * Tìm người dùng theo ID khóa chính.
     *
     * @param id Khóa chính ID
     * @return NguoiDung nếu tìm thấy, hoặc null
     */
    public NguoiDung timTheoId(long id) {
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                + "so_lan_sai, thoi_gian_khoa, nhom_kinh_doanh_id, created_at, updated_at "
                + "FROM nguoi_dung WHERE id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nd = mapResultSetToNguoiDung(rs);
                    nd.setDanhSachVaiTro(layDanhSachVaiTroTheoNguoiDungId(nd.getId()));
                    return nd;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn người dùng theo id: " + id, e);
        }
        return null;
    }

    /**
     * Tăng số lần đăng nhập sai liên tiếp lên 1.
     *
     * @param id ID người dùng
     */
    public void tangSoLanSai(long id) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = so_lan_sai + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tăng số lần sai cho user id: " + id, e);
        }
    }

    /**
     * Khóa tạm tài khoản người dùng trong một số phút quy định (mặc định 15 phút).
     *
     * @param id     ID người dùng
     * @param soPhut Số phút khóa tạm
     */
    public void khoaTam(long id, int soPhut) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = 5, thoi_gian_khoa = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        Timestamp thoiGianKhoa = new Timestamp(System.currentTimeMillis() + soPhut * 60 * 1000L);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, thoiGianKhoa);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khóa tạm tài khoản user id: " + id, e);
        }
    }

    /**
     * Xóa bộ đếm số lần sai và thời gian khóa tạm sau khi đăng nhập thành công
     * hoặc sau khi thời gian khóa tạm đã hết hạn.
     *
     * @param id ID người dùng
     */
    public void resetSoLanSai(long id) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = 0, thoi_gian_khoa = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi reset số lần sai cho user id: " + id, e);
        }
    }

    /**
     * Lấy danh sách các vai trò được gán cho người dùng qua bảng nối nguoi_dung_vai_tro.
     *
     * @param nguoiDungId ID người dùng
     * @return Set<VaiTro>
     */
    public Set<VaiTro> layDanhSachVaiTroTheoNguoiDungId(long nguoiDungId) {
        Set<VaiTro> danhSach = new HashSet<>();
        String sql = "SELECT vt.id, vt.ma_vai_tro, vt.ten_vai_tro, vt.mo_ta "
                + "FROM vai_tro vt "
                + "INNER JOIN nguoi_dung_vai_tro ndvt ON vt.id = ndvt.vai_tro_id "
                + "WHERE ndvt.nguoi_dung_id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, nguoiDungId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VaiTro vt = new VaiTro();
                    vt.setId(rs.getInt("id"));
                    vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                    vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                    vt.setMoTa(rs.getString("mo_ta"));
                    danhSach.add(vt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách vai trò cho user id: " + nguoiDungId, e);
        }
        return danhSach;
    }

    public void capNhatDangNhapThanhCong(long nguoiDungId, Timestamp lanDangNhapCuoi) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = 0, thoi_gian_khoa = NULL, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, lanDangNhapCuoi != null ? lanDangNhapCuoi : new Timestamp(System.currentTimeMillis()));
            ps.setLong(2, nguoiDungId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật đăng nhập thành công cho user: " + nguoiDungId, e);
        }
    }

    public void capNhatDangNhapThatBai(long nguoiDungId, int soLanSai, Timestamp thoiGianKhoa) {
        String sql = "UPDATE nguoi_dung SET so_lan_sai = ?, thoi_gian_khoa = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLanSai);
            ps.setTimestamp(2, thoiGianKhoa);
            ps.setLong(3, nguoiDungId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật đăng nhập thất bại cho user: " + nguoiDungId, e);
        }
    }

    private NguoiDung mapResultSetToNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung nd = new NguoiDung();
        nd.setId(rs.getLong("id"));
        nd.setHoTen(rs.getString("ho_ten"));
        nd.setEmail(rs.getString("email"));
        nd.setMatKhau(rs.getString("mat_khau"));
        nd.setSoDienThoai(rs.getString("so_dien_thoai"));
        nd.setTrangThai(rs.getString("trang_thai"));
        nd.setSoLanSai(rs.getInt("so_lan_sai"));
        nd.setThoiGianKhoa(rs.getTimestamp("thoi_gian_khoa"));
        int nhomId = rs.getInt("nhom_kinh_doanh_id");
        if (!rs.wasNull()) {
            nd.setNhomKinhDoanhId(nhomId);
        }
        nd.setCreatedAt(rs.getTimestamp("created_at"));
        nd.setUpdatedAt(rs.getTimestamp("updated_at"));
        return nd;
    }
}
