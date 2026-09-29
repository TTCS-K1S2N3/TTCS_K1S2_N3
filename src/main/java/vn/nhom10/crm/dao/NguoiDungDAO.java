package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý bảng nguoi_dung, tìm kiếm đa điều kiện, phân trang và quản lý phân quyền vai trò qua Transaction.
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
        this.vaiTroDAO = vaiTroDAO;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
    }

    public NguoiDung timTheoId(int id) {
        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id, created_at " +
                     "FROM nguoi_dung WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaNguoiDung(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm người dùng theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public NguoiDung timTheoEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT id, ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id, created_at " +
                     "FROM nguoi_dung WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaNguoiDung(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm người dùng theo email=" + email + ": " + e.getMessage(), e);
        }
        return null;
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

        try (Connection conn = DatabaseConfig.getConnection();
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

        try (Connection conn = DatabaseConfig.getConnection();
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

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(anhXaNguoiDung(rs));
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
     * @return ID người dùng vừa tạo, hoặc -1 nếu thất bại
     * @throws SQLException khi thao tác database lỗi
     */
    public int themNguoiDung(NguoiDung nguoiDung, List<Integer> dsVaiTroIds) throws SQLException {
        String sqlUser = "INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, trang_thai, nhom_kinh_doanh_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlRole = "INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int generatedId;
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
                        generatedId = rsKey.getInt(1);
                    } else {
                        throw new SQLException("Không thể lấy generated ID người dùng");
                    }
                }
            }

            if (dsVaiTroIds != null && !dsVaiTroIds.isEmpty()) {
                try (PreparedStatement psRole = conn.prepareStatement(sqlRole)) {
                    for (Integer vtId : dsVaiTroIds) {
                        if (vtId != null) {
                            psRole.setInt(1, generatedId);
                            psRole.setInt(2, vtId);
                            psRole.addBatch();
                        }
                    }
                    psRole.executeBatch();
                }
            }

            conn.commit();
            nguoiDung.setId(generatedId);
            return generatedId;

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
            conn = DatabaseConfig.getConnection();
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
                psUser.setInt(6, nguoiDung.getId());

                int affected = psUser.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            if (dsVaiTroIds != null) {
                try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteRoles)) {
                    psDel.setInt(1, nguoiDung.getId());
                    psDel.executeUpdate();
                }

                if (!dsVaiTroIds.isEmpty()) {
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsertRoles)) {
                        for (Integer vtId : dsVaiTroIds) {
                            if (vtId != null) {
                                psIns.setInt(1, nguoiDung.getId());
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

    private NguoiDung anhXaNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung nd = new NguoiDung();
        nd.setId(rs.getInt("id"));
        nd.setHoTen(rs.getString("ho_ten"));
        nd.setEmail(rs.getString("email"));
        nd.setMatKhau(rs.getString("mat_khau"));
        nd.setSoDienThoai(rs.getString("so_dien_thoai"));
        nd.setTrangThai(rs.getString("trang_thai"));
        nd.setCreatedAt(rs.getTimestamp("created_at"));

        Integer nhomId = rs.getObject("nhom_kinh_doanh_id") != null ? rs.getInt("nhom_kinh_doanh_id") : null;
        nd.setNhomKinhDoanhId(nhomId);

        if (nhomId != null) {
            NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomId);
            nd.setNhomKinhDoanh(nhom);
        }

        Set<VaiTro> dsVaiTro = vaiTroDAO.layVaiTroTheoNguoiDungId(nd.getId());
        nd.setDanhSachVaiTro(dsVaiTro);

        return nd;
    }
}
