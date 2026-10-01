package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object phụ trách truy vấn và cập nhật dữ liệu bảng nguoi_dung.
 */
public class NguoiDungDAO {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungDAO.class.getName());

    private final VaiTroDAO vaiTroDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;

    public NguoiDungDAO() {
        this.vaiTroDAO = new VaiTroDAO();
        this.nhomKinhDoanhDAO = new NhomKinhDoanhDAO();
    }

    public NguoiDungDAO(VaiTroDAO vaiTroDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO) {
        this.vaiTroDAO = vaiTroDAO != null ? vaiTroDAO : new VaiTroDAO();
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO != null ? nhomKinhDoanhDAO : new NhomKinhDoanhDAO();
    }

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
        String sql = "SELECT vt.id, vt.ma_vai_tro, vt.ten_vai_tro, vt.mo_ta, vt.pham_vi_toi_da "
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
                    vt.setPhamViToiDa(rs.getString("pham_vi_toi_da"));
                    danhSach.add(vt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi hoặc bảng vai_tro chưa có pham_vi_toi_da, dùng truy vấn cơ bản: " + e.getMessage());
            return layDanhSachVaiTroTheoNguoiDungIdCoBan(nguoiDungId);
        }
        return danhSach;
    }

    private Set<VaiTro> layDanhSachVaiTroTheoNguoiDungIdCoBan(long nguoiDungId) {
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
                    // Mặc định fail-closed: CA_NHAN
                    vt.setPhamViToiDa(vn.nhom10.crm.model.PhamViDuLieu.CA_NHAN);
                    danhSach.add(vt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách vai trò cơ bản cho user id: " + nguoiDungId, e);
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

    /**
     * Cập nhật mật khẩu mới của người dùng trong transaction đặt lại mật khẩu (S1-03).
     *
     * @param nguoiDungId ID người dùng
     * @param matKhauHash Mật khẩu đã băm BCrypt
     * @param conn        Connection JDBC đang quản lý transaction
     * @return true nếu cập nhật thành công
     * @throws SQLException khi truy vấn gặp lỗi
     */
    public boolean capNhatMatKhau(long nguoiDungId, String matKhauHash, Connection conn) throws SQLException {
        String sql = "UPDATE nguoi_dung SET mat_khau = ?, so_lan_sai = 0, thoi_gian_khoa = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHash);
            ps.setLong(2, nguoiDungId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật mật khẩu mới của người dùng (tự mở kết nối).
     *
     * @param nguoiDungId ID người dùng
     * @param matKhauHash Mật khẩu đã băm BCrypt
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatMatKhau(long nguoiDungId, String matKhauHash) {
        String sql = "UPDATE nguoi_dung SET mat_khau = ?, so_lan_sai = 0, thoi_gian_khoa = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHash);
            ps.setLong(2, nguoiDungId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật mật khẩu cho user: " + nguoiDungId, e);
            return false;
        }
    }

    public NguoiDung timTheoId(int id) {
        return timTheoId((long) id);
    }

    /**
     * AC: Kiểm tra email đã tồn tại trong hệ thống chưa (hỗ trợ loại trừ chính id khi sửa).
     *
     * @param email     email cần kiểm tra
     * @param excludeId ID người dùng bỏ qua (null nếu là tạo mới)
     * @return true nếu email đã bị trùng
     */
    public boolean kiemTraEmailTonTai(String email, Integer excludeId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM nguoi_dung WHERE LOWER(email) = LOWER(?)");
        if (excludeId != null) {
            sql.append(" AND id <> ?");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, email.trim());
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra email trùng lặp: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * AC: Tìm theo tên, email, nhóm; lọc theo vai trò và trạng thái; đếm tổng số bản ghi.
     */
    public int demSoLuong(String tuKhoa, Integer nhomId, Integer vaiTroId, String trangThai) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT nd.id) FROM nguoi_dung nd ");
        if (vaiTroId != null) {
            sql.append("INNER JOIN nguoi_dung_vai_tro ndvt ON nd.id = ndvt.nguoi_dung_id AND ndvt.vai_tro_id = ? ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (vaiTroId != null) {
            params.add(vaiTroId);
        }

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(nd.ho_ten) LIKE ? OR LOWER(nd.email) LIKE ?) ");
            String kw = "%" + tuKhoa.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (nhomId != null) {
            sql.append("AND nd.nhom_kinh_doanh_id = ? ");
            params.add(nhomId);
        }

        if (trangThai != null && !trangThai.trim().isEmpty()) {
            sql.append("AND nd.trang_thai = ? ");
            params.add(trangThai.trim());
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng người dùng: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * AC: Tìm theo tên, email, nhóm; lọc theo vai trò và trạng thái; Danh sách phân trang, mặc định 20 dòng.
     */
    public List<NguoiDung> timKiemVaPhanTrang(String tuKhoa, Integer nhomId, Integer vaiTroId, String trangThai, int limit, int offset) {
        List<NguoiDung> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT nd.id, nd.ho_ten, nd.email, nd.mat_khau, nd.so_dien_thoai, " +
                "nd.trang_thai, nd.nhom_kinh_doanh_id, nd.created_at FROM nguoi_dung nd ");

        if (vaiTroId != null) {
            sql.append("INNER JOIN nguoi_dung_vai_tro ndvt ON nd.id = ndvt.nguoi_dung_id AND ndvt.vai_tro_id = ? ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (vaiTroId != null) {
            params.add(vaiTroId);
        }

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(nd.ho_ten) LIKE ? OR LOWER(nd.email) LIKE ?) ");
            String kw = "%" + tuKhoa.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (nhomId != null) {
            sql.append("AND nd.nhom_kinh_doanh_id = ? ");
            params.add(nhomId);
        }

        if (trangThai != null && !trangThai.trim().isEmpty()) {
            sql.append("AND nd.trang_thai = ? ");
            params.add(trangThai.trim());
        }

        sql.append("ORDER BY nd.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToNguoiDung(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn tìm kiếm và phân trang người dùng: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Thêm tài khoản người dùng mới và phân quyền vai trò trong một TRANSACTION duy nhất.
     *
     * @param nguoiDung  thông tin người dùng
     * @param dsVaiTroIds danh sách ID vai trò được cấp
     * @return ID người dùng vừa tạo
     * @throws SQLException khi thao tác database lỗi
     */
    public int themNguoiDung(NguoiDung nguoiDung, List<Integer> dsVaiTroIds) throws SQLException {
        String sqlUser = "INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlRole = "INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false);

            long generatedId;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, nguoiDung.getHoTen());
                psUser.setString(2, nguoiDung.getEmail());
                psUser.setString(3, nguoiDung.getMatKhau());
                psUser.setString(4, nguoiDung.getSoDienThoai());
                psUser.setString(5, nguoiDung.getTrangThai() != null ? nguoiDung.getTrangThai() : NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
                if (nguoiDung.getNhomKinhDoanhId() != null) {
                    psUser.setInt(6, nguoiDung.getNhomKinhDoanhId());
                } else {
                    psUser.setNull(6, java.sql.Types.INTEGER);
                }

                psUser.executeUpdate();
                try (ResultSet rsKey = psUser.getGeneratedKeys()) {
                    if (rsKey.next()) {
                        generatedId = rsKey.getLong(1);
                    } else {
                        throw new SQLException("Không thể lấy generated ID người dùng");
                    }
                }
            }

            if (dsVaiTroIds != null && !dsVaiTroIds.isEmpty()) {
                try (PreparedStatement psRole = conn.prepareStatement(sqlRole)) {
                    for (Integer vtId : dsVaiTroIds) {
                        if (vtId != null) {
                            psRole.setLong(1, generatedId);
                            psRole.setInt(2, vtId);
                            psRole.addBatch();
                        }
                    }
                    psRole.executeBatch();
                }
            }

            conn.commit();
            nguoiDung.setId(generatedId);
            return (int) generatedId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback thất bại: " + ex.getMessage(), ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Lỗi thêm người dùng: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Cập nhật thông tin tài khoản người dùng và gán lại vai trò trong một TRANSACTION duy nhất.
     *
     * @param nguoiDung  thông tin người dùng cần cập nhật
     * @param dsVaiTroIds danh sách ID vai trò mới
     * @return true nếu cập nhật thành công
     * @throws SQLException khi thao tác database lỗi
     */
    public boolean capNhatNguoiDung(NguoiDung nguoiDung, List<Integer> dsVaiTroIds) throws SQLException {
        String sqlUser = "UPDATE nguoi_dung SET ho_ten = ?, email = ?, trang_thai = ?, nhom_kinh_doanh_id = ?, " +
                         "so_dien_thoai = ? WHERE id = ?";
        String sqlDeleteRoles = "DELETE FROM nguoi_dung_vai_tro WHERE nguoi_dung_id = ?";
        String sqlInsertRoles = "INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false);

            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setString(1, nguoiDung.getHoTen());
                psUser.setString(2, nguoiDung.getEmail());
                psUser.setString(3, nguoiDung.getTrangThai());
                if (nguoiDung.getNhomKinhDoanhId() != null) {
                    psUser.setInt(4, nguoiDung.getNhomKinhDoanhId());
                } else {
                    psUser.setNull(4, java.sql.Types.INTEGER);
                }
                psUser.setString(5, nguoiDung.getSoDienThoai());
                psUser.setLong(6, nguoiDung.getId());

                int affected = psUser.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            if (dsVaiTroIds != null) {
                try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteRoles)) {
                    psDel.setLong(1, nguoiDung.getId());
                    psDel.executeUpdate();
                }

                if (!dsVaiTroIds.isEmpty()) {
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsertRoles)) {
                        for (Integer vtId : dsVaiTroIds) {
                            if (vtId != null) {
                                psIns.setLong(1, nguoiDung.getId());
                                psIns.setInt(2, vtId);
                                psIns.addBatch();
                            }
                        }
                        psIns.executeBatch();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback cập nhật người dùng thất bại: " + ex.getMessage(), ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật người dùng: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
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

        try {
            int soLanSai = rs.getInt("so_lan_sai");
            if (!rs.wasNull()) {
                nd.setSoLanSai(soLanSai);
            }
        } catch (SQLException ignored) {
        }

        try {
            Timestamp tgKhoa = rs.getTimestamp("thoi_gian_khoa");
            nd.setThoiGianKhoa(tgKhoa);
        } catch (SQLException ignored) {
        }

        try {
            int nhomId = rs.getInt("nhom_kinh_doanh_id");
            if (!rs.wasNull()) {
                nd.setNhomKinhDoanhId(nhomId);
                if (nhomKinhDoanhDAO != null) {
                    nd.setNhomKinhDoanh(nhomKinhDoanhDAO.timTheoId(nhomId));
                }
            }
        } catch (SQLException ignored) {
        }

        try {
            nd.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {
        }

        try {
            nd.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException ignored) {
        }

        // Tự động nạp vai trò
        nd.setDanhSachVaiTro(layDanhSachVaiTroTheoNguoiDungId(nd.getId()));

        return nd;
    }
    /**
     * Lấy danh sách toàn bộ người dùng (phục vụ trang phân quyền S1-09).
     */
    public List<NguoiDung> layTatCa() {
        List<NguoiDung> danhSach = new ArrayList<>();
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                + "so_lan_sai, thoi_gian_khoa, nhom_kinh_doanh_id, created_at, updated_at "
                + "FROM nguoi_dung ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NguoiDung nd = mapResultSetToNguoiDung(rs);
                danhSach.add(nd);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách người dùng: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Gán danh sách vai trò và nhóm kinh doanh cho người dùng trong một TRANSACTION an toàn.
     * Nếu xảy ra bất kỳ lỗi nào, toàn bộ thay đổi sẽ được ROLLBACK (Story S1-09).
     */
    public boolean capNhatVaiTroVaNhomTransaction(int nguoiDungId, List<Integer> danhSachVaiTroId, Integer nhomKinhDoanhId) throws SQLException {
        String sqlCapNhatNhom = "UPDATE nguoi_dung SET nhom_kinh_doanh_id = ? WHERE id = ?";
        String sqlXoaVaiTroCu = "DELETE FROM nguoi_dung_vai_tro WHERE nguoi_dung_id = ?";
        String sqlThemVaiTro = "INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false);

            // 1. Cập nhật nhóm kinh doanh
            try (PreparedStatement psNhom = conn.prepareStatement(sqlCapNhatNhom)) {
                if (nhomKinhDoanhId != null && nhomKinhDoanhId > 0) {
                    psNhom.setInt(1, nhomKinhDoanhId);
                } else {
                    psNhom.setNull(1, Types.INTEGER);
                }
                psNhom.setInt(2, nguoiDungId);
                psNhom.executeUpdate();
            }

            // 2. Xoá tất cả vai trò cũ của người dùng
            try (PreparedStatement psXoa = conn.prepareStatement(sqlXoaVaiTroCu)) {
                psXoa.setInt(1, nguoiDungId);
                psXoa.executeUpdate();
            }

            // 3. Chèn các vai trò mới
            if (danhSachVaiTroId != null && !danhSachVaiTroId.isEmpty()) {
                try (PreparedStatement psThem = conn.prepareStatement(sqlThemVaiTro)) {
                    for (Integer vaiTroId : danhSachVaiTroId) {
                        if (vaiTroId != null) {
                            psThem.setInt(1, nguoiDungId);
                            psThem.setInt(2, vaiTroId);
                            psThem.addBatch();
                        }
                    }
                    psThem.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    LOGGER.log(Level.WARNING, "Lỗi khi gán vai trò & nhóm, đang rollback: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi rollback: " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Lỗi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Khoá tài khoản người dùng theo ID trong Connection được cung cấp (thuộc Transaction).
     * Đặt trang_thai = KHOA (Story S1-10).
     */
    public boolean khoaTaiKhoan(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE nguoi_dung SET trang_thai = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, NguoiDung.TRANG_THAI_KHOA);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Khoá tài khoản người dùng tự mở kết nối riêng.
     */
    public boolean khoaTaiKhoan(int userId) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return khoaTaiKhoan(conn, userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi khoá tài khoản ID=" + userId + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kích hoạt tài khoản từ trạng thái CHO_KICH_HOAT sang HOAT_DONG trong Connection (thuộc Transaction).
     * Chỉ kích hoạt nếu tài khoản đang ở trạng thái CHO_KICH_HOAT (Story S1-08).
     * Tuyệt đối không thay đổi nếu tài khoản đang KHOA hoặc đã HOAT_DONG.
     *
     * @param nguoiDungId ID người dùng
     * @param conn        Connection JDBC đang quản lý transaction
     * @return true nếu có bản ghi được cập nhật sang HOAT_DONG
     * @throws SQLException khi truy vấn gặp lỗi
     */
    public boolean kichHoatTaiKhoan(long nguoiDungId, Connection conn) throws SQLException {
        String sql = "UPDATE nguoi_dung SET trang_thai = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND trang_thai = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, NguoiDung.TRANG_THAI_HOAT_DONG);
            ps.setLong(2, nguoiDungId);
            ps.setString(3, NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean kichHoatTaiKhoan(int userId, Connection conn) throws SQLException {
        return kichHoatTaiKhoan((long) userId, conn);
    }

    /**
     * Kích hoạt tài khoản tự mở kết nối riêng.
     */
    public boolean kichHoatTaiKhoan(long nguoiDungId) {
        String sql = "UPDATE nguoi_dung SET trang_thai = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND trang_thai = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, NguoiDung.TRANG_THAI_HOAT_DONG);
            ps.setLong(2, nguoiDungId);
            ps.setString(3, NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi kích hoạt tài khoản ID=" + nguoiDungId + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy danh sách người dùng khả dụng để tiếp nhận dữ liệu bàn giao (Story S1-10).
     * Chỉ những tài khoản đang hoạt động (HOAT_DONG) và khác nhân viên bị khoá mới được chọn.
     */
    public List<NguoiDung> layDanhSachNguoiDungKhaDungTiepNhan(int excludeUserId) {
        List<NguoiDung> danhSach = new ArrayList<>();
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, "
                + "so_lan_sai, thoi_gian_khoa, nhom_kinh_doanh_id, created_at, updated_at "
                + "FROM nguoi_dung WHERE trang_thai = ? AND id != ? ORDER BY ho_ten ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, NguoiDung.TRANG_THAI_HOAT_DONG);
            ps.setInt(2, excludeUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToNguoiDung(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách người dùng khả dụng tiếp nhận: " + e.getMessage(), e);
        }
        return danhSach;
    }
}
