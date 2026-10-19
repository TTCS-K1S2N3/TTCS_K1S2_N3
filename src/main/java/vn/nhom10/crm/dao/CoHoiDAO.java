package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.CoHoi;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý cơ hội bán hàng (co_hoi).
 * Đáp ứng các yêu cầu:
 * - Story S4-08: Tự động tạo cơ hội bán hàng khi chuyển đổi Lead (AC1, AC2).
 * - Story S3-03: Cung cấp thông tin cơ hội cho trang Khách hàng 360°.
 */
public class CoHoiDAO {

    private static final Logger LOGGER = Logger.getLogger(CoHoiDAO.class.getName());

    private static final String COT_SELECT_CO_HOI =
            "ch.id, ch.ma_co_hoi, ch.ten_co_hoi, ch.khach_hang_id, " +
            "ch.nguoi_lien_he_chinh_id, ch.lead_id, ch.chien_dich_id, ch.nguon_lead_id, " +
            "ch.giai_doan_id, ch.nguoi_phu_trach_id, ch.nhom_kinh_doanh_id, " +
            "ch.gia_tri_du_kien, ch.xac_suat, ch.ly_do_sua_xac_suat, ch.ngay_chot_du_kien, " +
            "ch.trang_thai, ch.gia_tri_chot_thuc_te, ch.ngay_ky, ch.ly_do_thang_thua_id, " +
            "ch.doi_thu_id, ch.ngay_hoat_dong_cuoi, ch.bi_dinh_tre, ch.mo_ta_chi_tiet, " +
            "ch.ngay_tao, ch.created_at, ch.updated_at, " +
            "gd.ten_giai_doan, nd.ho_ten AS ten_nguoi_phu_trach, " +
            "nlh.ho_ten AS ten_nguoi_lien_he_chinh, " +
            "ld.ten_ly_do AS ten_ly_do_thang_thua ";

    private static final String BANG_JOIN_CO_HOI =
            "FROM co_hoi ch " +
            "LEFT JOIN giai_doan_pipeline gd ON ch.giai_doan_id = gd.id " +
            "LEFT JOIN nguoi_dung nd ON ch.nguoi_phu_trach_id = nd.id " +
            "LEFT JOIN nguoi_lien_he nlh ON ch.nguoi_lien_he_chinh_id = nlh.id " +
            "LEFT JOIN ly_do_thang_thua ld ON ch.ly_do_thang_thua_id = ld.id ";

    /**
     * Tự động sinh mã cơ hội tiếp theo có dạng CH000001.
     */
    public String sinhMaCoHoi() {
        String sql = "SELECT MAX(id) FROM co_hoi";
        long nextNum = 1;
        try (Connection conn = DatabaseConnection.layKetNoi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                nextNum = rs.getLong(1) + 1;
            }
        } catch (Exception e) {
            nextNum = System.currentTimeMillis() % 1000000;
        }
        return String.format("CH%06d", nextNum);
    }

    /**
     * Thêm mới cơ hội bán hàng bằng Connection ngoài (phục vụ Transaction).
     */
    public Long themCoHoi(Connection conn, CoHoi ch) throws SQLException {
        if (ch == null) {
            return null;
        }

        if (ch.getMaCoHoi() == null || ch.getMaCoHoi().trim().isEmpty()) {
            ch.setMaCoHoi(sinhMaCoHoi());
        }

        String sql = "INSERT INTO co_hoi (" +
                "ma_co_hoi, ten_co_hoi, khach_hang_id, nguoi_lien_he_chinh_id, lead_id, " +
                "chien_dich_id, nguon_lead_id, giai_doan_id, nguoi_phu_trach_id, nhom_kinh_doanh_id, " +
                "gia_tri_du_kien, xac_suat, ly_do_sua_xac_suat, ngay_chot_du_kien, trang_thai, " +
                "gia_tri_chot_thuc_te, ngay_ky, ly_do_thang_thua_id, doi_thu_id, ngay_hoat_dong_cuoi, " +
                "bi_dinh_tre, mo_ta_chi_tiet, ngay_tao" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            datThamSoLuu(ps, ch);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        ch.setId(id);
                        return id;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Thêm mới cơ hội bán hàng độc lập (không nằm trong transaction ngoài).
     */
    public Long themCoHoi(CoHoi ch) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return themCoHoi(conn, ch);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm cơ hội: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Tìm cơ hội theo ID.
     */
    public CoHoi timTheoId(Long id) {
        if (id == null || id <= 0) return null;
        String sql = "SELECT " + COT_SELECT_CO_HOI + BANG_JOIN_CO_HOI + "WHERE ch.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCoHoi(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm cơ hội theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm cơ hội theo Lead ID (nếu được chuyển đổi từ lead).
     */
    public CoHoi timTheoLeadId(Long leadId) {
        if (leadId == null || leadId <= 0) return null;
        String sql = "SELECT " + COT_SELECT_CO_HOI + BANG_JOIN_CO_HOI + "WHERE ch.lead_id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCoHoi(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm cơ hội theo Lead ID: " + leadId, e);
        }
        return null;
    }

    private void datThamSoLuu(PreparedStatement ps, CoHoi ch) throws SQLException {
        int idx = 1;
        ps.setString(idx++, ch.getMaCoHoi() != null ? ch.getMaCoHoi().trim() : sinhMaCoHoi());
        ps.setString(idx++, ch.getTenCoHoi() != null ? ch.getTenCoHoi().trim() : "");

        if (ch.getKhachHangId() != null && ch.getKhachHangId() > 0) {
            ps.setLong(idx++, ch.getKhachHangId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getNguoiLienHeChinhId() != null && ch.getNguoiLienHeChinhId() > 0) {
            ps.setLong(idx++, ch.getNguoiLienHeChinhId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getLeadId() != null && ch.getLeadId() > 0) {
            ps.setLong(idx++, ch.getLeadId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getChienDichId() != null && ch.getChienDichId() > 0) {
            ps.setLong(idx++, ch.getChienDichId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getNguonLeadId() != null && ch.getNguonLeadId() > 0) {
            ps.setLong(idx++, ch.getNguonLeadId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getGiaiDoanId() != null && ch.getGiaiDoanId() > 0) {
            ps.setLong(idx++, ch.getGiaiDoanId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getNguoiPhuTrachId() != null && ch.getNguoiPhuTrachId() > 0) {
            ps.setLong(idx++, ch.getNguoiPhuTrachId());
        } else {
            throw new SQLException("nguoi_phu_trach_id là bắt buộc trong co_hoi");
        }

        if (ch.getNhomKinhDoanhId() != null && ch.getNhomKinhDoanhId() > 0) {
            ps.setLong(idx++, ch.getNhomKinhDoanhId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setBigDecimal(idx++, ch.getGiaTriDuKien() != null ? ch.getGiaTriDuKien() : BigDecimal.ZERO);
        ps.setInt(idx++, ch.getXacSuat() != null ? ch.getXacSuat() : 0);
        ps.setString(idx++, ch.getLyDoSuaXacSuat());

        if (ch.getNgayChotDuKien() != null) {
            ps.setDate(idx++, Date.valueOf(ch.getNgayChotDuKien()));
        } else {
            ps.setNull(idx++, Types.DATE);
        }

        ps.setString(idx++, ch.getTrangThai() != null ? ch.getTrangThai() : "MO");

        if (ch.getGiaTriChotThucTe() != null) {
            ps.setBigDecimal(idx++, ch.getGiaTriChotThucTe());
        } else {
            ps.setNull(idx++, Types.DECIMAL);
        }

        if (ch.getNgayKy() != null) {
            ps.setDate(idx++, Date.valueOf(ch.getNgayKy()));
        } else {
            ps.setNull(idx++, Types.DATE);
        }

        if (ch.getLyDoThangThuaId() != null && ch.getLyDoThangThuaId() > 0) {
            ps.setLong(idx++, ch.getLyDoThangThuaId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getDoiThuId() != null && ch.getDoiThuId() > 0) {
            ps.setLong(idx++, ch.getDoiThuId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (ch.getNgayHoatDongCuoi() != null) {
            ps.setTimestamp(idx++, ch.getNgayHoatDongCuoi());
        } else {
            ps.setNull(idx++, Types.TIMESTAMP);
        }

        ps.setInt(idx++, ch.isBiDinhTre() ? 1 : 0);
        ps.setString(idx++, ch.getMoTaChiTiet());

        LocalDate ngayTaoSql = ch.getNgayTao() != null ? ch.getNgayTao() : LocalDate.now();
        ps.setDate(idx++, Date.valueOf(ngayTaoSql));
    }

    private CoHoi mapResultSetToCoHoi(ResultSet rs) throws SQLException {
        CoHoi ch = new CoHoi();
        ch.setId(rs.getLong("id"));
        ch.setMaCoHoi(rs.getString("ma_co_hoi"));
        ch.setTenCoHoi(rs.getString("ten_co_hoi"));

        long khId = rs.getLong("khach_hang_id");
        if (!rs.wasNull()) ch.setKhachHangId(khId);

        long nlhId = rs.getLong("nguoi_lien_he_chinh_id");
        if (!rs.wasNull()) ch.setNguoiLienHeChinhId(nlhId);

        long leadId = rs.getLong("lead_id");
        if (!rs.wasNull()) ch.setLeadId(leadId);

        long cdId = rs.getLong("chien_dich_id");
        if (!rs.wasNull()) ch.setChienDichId(cdId);

        long nguonId = rs.getLong("nguon_lead_id");
        if (!rs.wasNull()) ch.setNguonLeadId(nguonId);

        long gdId = rs.getLong("giai_doan_id");
        if (!rs.wasNull()) ch.setGiaiDoanId(gdId);

        ch.setNguoiPhuTrachId(rs.getLong("nguoi_phu_trach_id"));

        long nhomId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) ch.setNhomKinhDoanhId(nhomId);

        ch.setGiaTriDuKien(rs.getBigDecimal("gia_tri_du_kien"));
        ch.setXacSuat(rs.getInt("xac_suat"));
        ch.setLyDoSuaXacSuat(rs.getString("ly_do_sua_xac_suat"));

        Date dChot = rs.getDate("ngay_chot_du_kien");
        if (dChot != null) ch.setNgayChotDuKien(dChot.toLocalDate());

        ch.setTrangThai(rs.getString("trang_thai"));
        ch.setGiaTriChotThucTe(rs.getBigDecimal("gia_tri_chot_thuc_te"));

        Date dKy = rs.getDate("ngay_ky");
        if (dKy != null) ch.setNgayKy(dKy.toLocalDate());

        long ldId = rs.getLong("ly_do_thang_thua_id");
        if (!rs.wasNull()) ch.setLyDoThangThuaId(ldId);

        long dtId = rs.getLong("doi_thu_id");
        if (!rs.wasNull()) ch.setDoiThuId(dtId);

        ch.setNgayHoatDongCuoi(rs.getTimestamp("ngay_hoat_dong_cuoi"));
        ch.setBiDinhTre(rs.getBoolean("bi_dinh_tre"));
        ch.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));

        Date dTao = rs.getDate("ngay_tao");
        if (dTao != null) ch.setNgayTao(dTao.toLocalDate());

        ch.setCreatedAt(rs.getTimestamp("created_at"));
        ch.setUpdatedAt(rs.getTimestamp("updated_at"));

        ch.setTenGiaiDoan(rs.getString("ten_giai_doan"));
        ch.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
        ch.setTenNguoiLienHeChinh(rs.getString("ten_nguoi_lien_he_chinh"));
        ch.setTenLyDoThangThua(rs.getString("ten_ly_do_thang_thua"));

        return ch;
    }
}
