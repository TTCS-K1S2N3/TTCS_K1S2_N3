package vn.nhom10.crm.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu người dùng và vai trò qua JDBC PreparedStatement.
 */
public class NguoiDungDAO {

    private static final Logger logger = LoggerFactory.getLogger(NguoiDungDAO.class);

    /**
     * Tìm người dùng theo email.
     */
    public NguoiDung timTheoEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM nguoi_dung WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nd = mapResultSetToNguoiDung(rs);
                    nd.setDanhSachVaiTro(layDanhSachVaiTro(conn, nd.getId()));
                    return nd;
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi tìm người dùng theo email {}: {}", email, e.getMessage());
        }
        return null;
    }

    /**
     * Tìm người dùng theo ID.
     */
    public NguoiDung timTheoId(Long id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM nguoi_dung WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung nd = mapResultSetToNguoiDung(rs);
                    nd.setDanhSachVaiTro(layDanhSachVaiTro(conn, nd.getId()));
                    return nd;
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi khi tìm người dùng theo id {}: {}", id, e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách các vai trò đang hoạt động của người dùng.
     */
    public List<VaiTro> layDanhSachVaiTro(Connection conn, Long nguoiDungId) throws SQLException {
        List<VaiTro> list = new ArrayList<>();
        String sql = "SELECT vt.id, vt.ma_vai_tro, vt.ten_vai_tro, vt.mo_ta, vt.pham_vi_mac_dinh, vt.hoat_dong " +
                     "FROM vai_tro vt " +
                     "INNER JOIN nguoi_dung_vai_tro ndvt ON vt.id = ndvt.vai_tro_id " +
                     "WHERE ndvt.nguoi_dung_id = ? AND ndvt.hoat_dong = 1 AND vt.hoat_dong = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VaiTro vt = new VaiTro();
                    vt.setId(rs.getLong("id"));
                    vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                    vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                    vt.setMoTa(rs.getString("mo_ta"));
                    vt.setPhamViMacDinh(rs.getString("pham_vi_mac_dinh"));
                    vt.setHoatDong(rs.getBoolean("hoat_dong"));
                    list.add(vt);
                }
            }
        }
        return list;
    }

    /**
     * Ghi nhận đăng nhập thành công: reset số lần sai về 0, xóa khóa tạm, cập nhật lần đăng nhập cuối.
     */
    public boolean capNhatDangNhapThanhCong(Long id) {
        String sql = "UPDATE nguoi_dung SET so_lan_dang_nhap_sai = 0, khoa_den = NULL, lan_dang_nhap_cuoi = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi cập nhật đăng nhập thành công cho user id {}: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật số lần đăng nhập sai và thời điểm khóa tạm nếu có.
     */
    public boolean capNhatDangNhapSai(Long id, int soLanSai, Timestamp khoaDen) {
        String sql = "UPDATE nguoi_dung SET so_lan_dang_nhap_sai = ?, khoa_den = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLanSai);
            if (khoaDen != null) {
                ps.setTimestamp(2, khoaDen);
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi cập nhật đăng nhập sai cho user id {}: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * Mở khóa và reset số lần sai sau khi hết hạn 15 phút.
     */
    public boolean moKhoaVaResetDangNhapSai(Long id) {
        String sql = "UPDATE nguoi_dung SET so_lan_dang_nhap_sai = 0, khoa_den = NULL WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi mở khóa cho user id {}: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * Thêm mới người dùng (dùng cho seed dữ liệu kiểm thử hoặc khởi tạo tài khoản).
     */
    public Long taoNguoiDung(NguoiDung nd) {
        String sql = "INSERT INTO nguoi_dung (ho_ten, email, mat_khau_hash, so_dien_thoai, chu_ky_email, " +
                     "anh_dai_dien_path, anh_dai_dien_thumb_path, trang_thai, so_lan_dang_nhap_sai, " +
                     "bat_buoc_doi_mat_khau, session_version, nhom_kinh_doanh_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nd.getHoTen());
            ps.setString(2, nd.getEmail().trim().toLowerCase());
            ps.setString(3, nd.getMatKhauHash());
            ps.setString(4, nd.getSoDienThoai());
            ps.setString(5, nd.getChuKyEmail());
            ps.setString(6, nd.getAnhDaiDienPath());
            ps.setString(7, nd.getAnhDaiDienThumbPath());
            ps.setString(8, nd.getTrangThai() != null ? nd.getTrangThai() : "HOAT_DONG");
            ps.setInt(9, nd.getSoLanDangNhapSai());
            ps.setBoolean(10, nd.isBatBuocDoiMatKhau());
            ps.setInt(11, nd.getSessionVersion() > 0 ? nd.getSessionVersion() : 1);
            if (nd.getNhomKinhDoanhId() != null) {
                ps.setLong(12, nd.getNhomKinhDoanhId());
            } else {
                ps.setNull(12, Types.BIGINT);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        nd.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi tạo người dùng {}: {}", nd.getEmail(), e.getMessage());
        }
        return null;
    }

    /**
     * Gán vai trò cho người dùng trong bảng nguoi_dung_vai_tro.
     */
    public boolean ganVaiTro(Long nguoiDungId, Long vaiTroId) {
        String sql = "INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id, hoat_dong) VALUES (?, ?, 1) " +
                     "ON DUPLICATE KEY UPDATE hoat_dong = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            ps.setLong(2, vaiTroId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Lỗi gán vai trò {} cho user {}: {}", vaiTroId, nguoiDungId, e.getMessage());
            return false;
        }
    }

    /**
     * Tìm vai trò theo mã (SALES_REP, ADMIN, ...).
     */
    public VaiTro timVaiTroTheoMa(String maVaiTro) {
        String sql = "SELECT * FROM vai_tro WHERE ma_vai_tro = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VaiTro vt = new VaiTro();
                    vt.setId(rs.getLong("id"));
                    vt.setMaVaiTro(rs.getString("ma_vai_tro"));
                    vt.setTenVaiTro(rs.getString("ten_vai_tro"));
                    vt.setMoTa(rs.getString("mo_ta"));
                    vt.setPhamViMacDinh(rs.getString("pham_vi_mac_dinh"));
                    vt.setHoatDong(rs.getBoolean("hoat_dong"));
                    return vt;
                }
            }
        } catch (SQLException e) {
            logger.error("Lỗi tìm vai trò {}: {}", maVaiTro, e.getMessage());
        }
        return null;
    }

    private NguoiDung mapResultSetToNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung nd = new NguoiDung();
        nd.setId(rs.getLong("id"));
        nd.setHoTen(rs.getString("ho_ten"));
        nd.setEmail(rs.getString("email"));
        nd.setMatKhauHash(rs.getString("mat_khau_hash"));
        nd.setSoDienThoai(rs.getString("so_dien_thoai"));
        nd.setChuKyEmail(rs.getString("chu_ky_email"));
        nd.setAnhDaiDienPath(rs.getString("anh_dai_dien_path"));
        nd.setAnhDaiDienThumbPath(rs.getString("anh_dai_dien_thumb_path"));
        nd.setTrangThai(rs.getString("trang_thai"));
        nd.setSoLanDangNhapSai(rs.getInt("so_lan_dang_nhap_sai"));
        nd.setKhoaDen(rs.getTimestamp("khoa_den"));
        nd.setBatBuocDoiMatKhau(rs.getBoolean("bat_buoc_doi_mat_khau"));
        nd.setNgayDoiMatKhau(rs.getTimestamp("ngay_doi_mat_khau"));
        nd.setSessionVersion(rs.getInt("session_version"));

        long nhomId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) {
            nd.setNhomKinhDoanhId(nhomId);
        }

        nd.setEmailDaXacThucLuc(rs.getTimestamp("email_da_xac_thuc_luc"));
        nd.setLanDangNhapCuoi(rs.getTimestamp("lan_dang_nhap_cuoi"));

        long createdBy = rs.getLong("created_by");
        if (!rs.wasNull()) {
            nd.setCreatedBy(createdBy);
        }

        nd.setCreatedAt(rs.getTimestamp("created_at"));
        nd.setUpdatedAt(rs.getTimestamp("updated_at"));
        return nd;
    }
}
