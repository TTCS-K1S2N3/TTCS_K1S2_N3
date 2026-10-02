package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.NguoiDungOptionDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.NhatKyThayDoi;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object phụ trách truy vấn và ghi nhận nhật ký hệ thống (bảng nhat_ky_he_thong).
 * Đáp ứng AC của Story S2-04:
 * - Ghi lại mọi thay đổi trên chiết khấu, chỉ tiêu, quyền sở hữu dữ liệu và vai trò người dùng
 * - Mỗi bản ghi có người thực hiện, thời điểm, giá trị trước và sau
 * - Lọc theo người dùng, loại đối tượng, khoảng thời gian
 */
public class NhatKyThayDoiDAO {

    private static final Logger LOGGER = Logger.getLogger(NhatKyThayDoiDAO.class.getName());

    /**
     * Ghi lại một bản ghi nhật ký thay đổi mới vào CSDL.
     * Sử dụng kết nối mặc định của hệ thống.
     *
     * @param nk Đối tượng NhatKyThayDoi
     * @return ID bản ghi vừa được tạo, hoặc -1 nếu thất bại
     */
    public long ghiNhatKy(NhatKyThayDoi nk) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return ghiNhatKy(conn, nk);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối CSDL khi ghi nhật ký hệ thống", e);
            return -1;
        }
    }

    /**
     * Ghi lại một bản ghi nhật ký thay đổi trong Transaction hiện tại.
     *
     * @param conn Connection JDBC đang hoạt động
     * @param nk   Đối tượng NhatKyThayDoi
     * @return ID bản ghi vừa được tạo
     * @throws SQLException khi truy vấn lỗi
     */
    public long ghiNhatKy(Connection conn, NhatKyThayDoi nk) throws SQLException {
        if (nk == null) {
            return -1;
        }

        String sql = "INSERT INTO nhat_ky_he_thong (" +
                "nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id, " +
                "gia_tri_truoc_json, gia_tri_sau_json, ly_do, dia_chi_ip, thong_tin_thiet_bi, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (nk.getNguoiThucHienId() != null && nk.getNguoiThucHienId() > 0) {
                ps.setLong(1, nk.getNguoiThucHienId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }

            String maHanhDong = nk.getHanhDong() != null ? nk.getHanhDong().getMa() : "CAP_NHAT";
            ps.setString(2, maHanhDong);

            String maLoaiDoiTuong = nk.getLoaiDoiTuong() != null ? nk.getLoaiDoiTuong().getMa() : "CHIET_KHAU";
            ps.setString(3, maLoaiDoiTuong);

            if (nk.getDoiTuongId() != null && nk.getDoiTuongId() > 0) {
                ps.setLong(4, nk.getDoiTuongId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            String truocJson = nk.getGiaTriTruocJson();
            if (truocJson != null && !truocJson.isBlank()) {
                ps.setString(5, truocJson);
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            String sauJson = nk.getGiaTriSauJson();
            if (sauJson != null && !sauJson.isBlank()) {
                ps.setString(6, sauJson);
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            ps.setString(7, nk.getLyDoThayDoi());
            ps.setString(8, nk.getDiaChiIp());
            ps.setString(9, nk.getThietBi());

            LocalDateTime thoiDiem = nk.getThoiDiem() != null ? nk.getThoiDiem() : LocalDateTime.now();
            ps.setTimestamp(10, Timestamp.valueOf(thoiDiem));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        nk.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thực thi ghi nhật ký thay đổi nhạy cảm: " + e.getMessage(), e);
            throw e;
        }

        return -1;
    }

    /**
     * Lấy danh sách nhật ký thay đổi theo bộ lọc đa tiêu chí (AC 3).
     *
     * @param boLoc Điều kiện lọc
     * @return Danh sách NhatKyThayDoi
     */
    public List<NhatKyThayDoi> layDanhSach(BoLocNhatKyDTO boLoc) {
        List<NhatKyThayDoi> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT nk.id, nk.nguoi_thuc_hien_id, nk.hanh_dong, nk.loai_doi_tuong, nk.doi_tuong_id, " +
                "nk.gia_tri_truoc_json, nk.gia_tri_sau_json, nk.ly_do, nk.dia_chi_ip, nk.thong_tin_thiet_bi, nk.created_at, " +
                "nd.ho_ten, nd.email " +
                "FROM nhat_ky_he_thong nk " +
                "LEFT JOIN nguoi_dung nd ON nk.nguoi_thuc_hien_id = nd.id " +
                "WHERE 1=1 "
        );

        List<Object> params = buildFilterParams(sql, boLoc);

        sql.append("ORDER BY nk.created_at DESC, nk.id DESC ");

        int soBanGhi = (boLoc != null && boLoc.getSoBanGhiTrenTrang() > 0) ? boLoc.getSoBanGhiTrenTrang() : 10;
        int trang = (boLoc != null && boLoc.getTrang() > 0) ? boLoc.getTrang() : 1;
        int offset = (trang - 1) * soBanGhi;

        sql.append("LIMIT ? OFFSET ?");
        params.add(soBanGhi);
        params.add(offset);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToModel(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách nhật ký thay đổi: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Đếm tổng số bản ghi nhật ký phù hợp với bộ lọc (phục vụ phân trang).
     *
     * @param boLoc Điều kiện lọc
     * @return Tổng số bản ghi
     */
    public long demTongSoBanGhi(BoLocNhatKyDTO boLoc) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                "FROM nhat_ky_he_thong nk " +
                "LEFT JOIN nguoi_dung nd ON nk.nguoi_thuc_hien_id = nd.id " +
                "WHERE 1=1 "
        );

        List<Object> params = buildFilterParams(sql, boLoc);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng bản ghi nhật ký: " + e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Lấy số liệu thống kê tổng hợp số lượt thay đổi theo từng loại đối tượng nhạy cảm.
     *
     * @param boLoc Điều kiện lọc
     * @return ThongKeNhatKyDTO
     */
    public ThongKeNhatKyDTO layThongKe(BoLocNhatKyDTO boLoc) {
        ThongKeNhatKyDTO dto = new ThongKeNhatKyDTO();

        // 1. Thống kê theo từng loại đối tượng
        StringBuilder sql = new StringBuilder(
                "SELECT nk.loai_doi_tuong, COUNT(*) AS so_luong " +
                "FROM nhat_ky_he_thong nk " +
                "LEFT JOIN nguoi_dung nd ON nk.nguoi_thuc_hien_id = nd.id " +
                "WHERE 1=1 "
        );

        List<Object> params = buildFilterParams(sql, boLoc);
        sql.append("GROUP BY nk.loai_doi_tuong");

        long tong = 0;
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loai_doi_tuong");
                    long cnt = rs.getLong("so_luong");
                    tong += cnt;

                    if ("CHIET_KHAU".equalsIgnoreCase(loai)) {
                        dto.setSoThayDoiChietKhau(cnt);
                    } else if ("CHI_TIEU".equalsIgnoreCase(loai)) {
                        dto.setSoThayDoiChiTieu(cnt);
                    } else if ("QUYEN_SO_HUU".equalsIgnoreCase(loai)) {
                        dto.setSoThayDoiQuyenSoHuu(cnt);
                    } else if ("VAI_TRO_NGUOI_DUNG".equalsIgnoreCase(loai)) {
                        dto.setSoThayDoiVaiTro(cnt);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thống kê số lượng nhật ký theo loại: " + e.getMessage(), e);
        }

        dto.setTongSoBanGhi(tong);

        // 2. Thống kê số người thực hiện duy nhất
        StringBuilder sqlUsers = new StringBuilder(
                "SELECT COUNT(DISTINCT nk.nguoi_thuc_hien_id) " +
                "FROM nhat_ky_he_thong nk " +
                "LEFT JOIN nguoi_dung nd ON nk.nguoi_thuc_hien_id = nd.id " +
                "WHERE nk.nguoi_thuc_hien_id IS NOT NULL "
        );
        List<Object> paramsUsers = buildFilterParams(sqlUsers, boLoc);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlUsers.toString())) {

            setParameters(ps, paramsUsers);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setSoNguoiThucHien(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số người thực hiện: " + e.getMessage(), e);
        }

        return dto;
    }

    /**
     * Tìm một bản ghi nhật ký theo ID chi tiết.
     *
     * @param id Khóa chính ID
     * @return NhatKyThayDoi hoặc null
     */
    public NhatKyThayDoi timTheoId(long id) {
        String sql = "SELECT nk.id, nk.nguoi_thuc_hien_id, nk.hanh_dong, nk.loai_doi_tuong, nk.doi_tuong_id, " +
                "nk.gia_tri_truoc_json, nk.gia_tri_sau_json, nk.ly_do, nk.dia_chi_ip, nk.thong_tin_thiet_bi, nk.created_at, " +
                "nd.ho_ten, nd.email " +
                "FROM nhat_ky_he_thong nk " +
                "LEFT JOIN nguoi_dung nd ON nk.nguoi_thuc_hien_id = nd.id " +
                "WHERE nk.id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm nhật ký theo id: " + id, e);
        }

        return null;
    }

    /**
     * Lấy danh sách người dùng để nạp vào dropdown bộ lọc (AC 3).
     *
     * @return Danh sách NguoiDungOptionDTO
     */
    public List<NguoiDungOptionDTO> layDanhSachNguoiDungOption() {
        List<NguoiDungOptionDTO> danhSach = new ArrayList<>();
        String sql = "SELECT id, ho_ten, email FROM nguoi_dung ORDER BY ho_ten ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String hoTen = rs.getString("ho_ten");
                String email = rs.getString("email");
                danhSach.add(new NguoiDungOptionDTO(id, hoTen, email, null));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách người dùng cho dropdown bộ lọc: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // =========================================================================
    // HÀM PHỤ TRỢ (HELPER METHODS)
    // =========================================================================

    private List<Object> buildFilterParams(StringBuilder sql, BoLocNhatKyDTO boLoc) {
        List<Object> params = new ArrayList<>();
        if (boLoc == null) {
            return params;
        }

        if (boLoc.getNguoiDungId() != null && boLoc.getNguoiDungId() > 0) {
            sql.append("AND nk.nguoi_thuc_hien_id = ? ");
            params.add(boLoc.getNguoiDungId());
        }

        if (boLoc.getLoaiDoiTuong() != null && !boLoc.getLoaiDoiTuong().trim().isEmpty()) {
            sql.append("AND nk.loai_doi_tuong = ? ");
            params.add(boLoc.getLoaiDoiTuong().trim());
        }

        if (boLoc.getTuNgay() != null) {
            sql.append("AND nk.created_at >= ? ");
            params.add(Timestamp.valueOf(boLoc.getTuNgay().atStartOfDay()));
        }

        if (boLoc.getDenNgay() != null) {
            sql.append("AND nk.created_at <= ? ");
            params.add(Timestamp.valueOf(boLoc.getDenNgay().atTime(23, 59, 59)));
        }

        if (boLoc.getTuKhoa() != null && !boLoc.getTuKhoa().trim().isEmpty()) {
            String keyword = "%" + boLoc.getTuKhoa().trim().toLowerCase() + "%";
            sql.append("AND (LOWER(nd.ho_ten) LIKE ? OR LOWER(nd.email) LIKE ? " +
                    "OR LOWER(nk.ly_do) LIKE ? OR LOWER(nk.hanh_dong) LIKE ? " +
                    "OR LOWER(nk.gia_tri_truoc_json) LIKE ? OR LOWER(nk.gia_tri_sau_json) LIKE ?) ");
            for (int i = 0; i < 6; i++) {
                params.add(keyword);
            }
        }

        return params;
    }

    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Long) {
                ps.setLong(i + 1, (Long) p);
            } else if (p instanceof Integer) {
                ps.setInt(i + 1, (Integer) p);
            } else if (p instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) p);
            } else if (p instanceof String) {
                ps.setString(i + 1, (String) p);
            } else {
                ps.setObject(i + 1, p);
            }
        }
    }

    private NhatKyThayDoi mapResultSetToModel(ResultSet rs) throws SQLException {
        NhatKyThayDoi nk = new NhatKyThayDoi();
        nk.setId(rs.getLong("id"));

        long ndId = rs.getLong("nguoi_thuc_hien_id");
        if (!rs.wasNull()) {
            nk.setNguoiThucHienId(ndId);
        }

        nk.setTenNguoiThucHien(rs.getString("ho_ten"));
        nk.setEmailNguoiThucHien(rs.getString("email"));

        String hanhDong = rs.getString("hanh_dong");
        nk.setHanhDong(HanhDongThayDoi.tuMa(hanhDong));

        String loai = rs.getString("loai_doi_tuong");
        nk.setLoaiDoiTuong(LoaiDoiTuongNhayCam.tuMa(loai));

        long dtId = rs.getLong("doi_tuong_id");
        if (!rs.wasNull()) {
            nk.setDoiTuongId(dtId);
        }

        nk.setGiaTriTruocJson(rs.getString("gia_tri_truoc_json"));
        nk.setGiaTriSauJson(rs.getString("gia_tri_sau_json"));

        nk.setLyDoThayDoi(rs.getString("ly_do"));
        nk.setDiaChiIp(rs.getString("dia_chi_ip"));
        nk.setThietBi(rs.getString("thong_tin_thiet_bi"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            nk.setCreatedAt(ts.toLocalDateTime());
        }

        return nk;
    }
}
