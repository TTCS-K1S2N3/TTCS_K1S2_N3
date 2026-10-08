package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn và cập nhật bảng 'khach_hang'.
 * - Story S3-01: Hồ sơ khách hàng doanh nghiệp, duy nhất MST, 4 trạng thái chuẩn, lọc Data Scope.
 * - Story S3-03: Cung cấp thông tin khách hàng cho trang 360°.
 * - Story S3-05: Quản lý quan hệ công ty mẹ - con và chống vòng lặp.
 * - Story S3-06: Import dữ liệu khách hàng.
 * - Story S3-08: Quản lý cờ rủi ro rời bỏ và danh sách rủi ro.
 * - Story S3-09: Quản lý chăm sóc định kỳ và tương tác gần nhất.
 */
public class KhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHangDAO.class.getName());

    private static final String COT_SELECT_DAY_DU =
            "kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
            "kh.nganh_nghe_id, nn.ten_nganh AS ten_nganh_nghe, " +
            "kh.quy_mo_id, qm.ten_quy_mo AS ten_quy_mo, " +
            "kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
            "kh.khu_vuc_id, kv.ten_khu_vuc AS ten_khu_vuc, " +
            "kh.nguoi_so_huu_id, nd.ho_ten AS ten_nguoi_so_huu, " +
            "kh.nhom_kinh_doanh_id, nkd.ten_nhom AS ten_nhom_kinh_doanh, " +
            "kh.doanh_thu_uoc_tinh, kh.cong_ty_me_id, ctm.ten_cong_ty AS ten_cong_ty_me, ctm.ma_khach_hang AS ma_cong_ty_me, " +
            "kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, kh.lan_tuong_tac_cuoi, " +
            "kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, kh.ngay_tao, kh.created_at, kh.updated_at";

    private static final String BANG_JOIN_CO_BAN =
            "FROM khach_hang kh " +
            "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
            "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
            "LEFT JOIN nganh_nghe nn ON kh.nganh_nghe_id = nn.id " +
            "LEFT JOIN quy_mo_doanh_nghiep qm ON kh.quy_mo_id = qm.id " +
            "LEFT JOIN khu_vuc_dia_ly kv ON kh.khu_vuc_id = kv.id " +
            "LEFT JOIN khach_hang ctm ON kh.cong_ty_me_id = ctm.id";

    /**
     * Thêm mới một khách hàng vào cơ sở dữ liệu (Story S3-01 & S3-05).
     * Tự động sinh mã khách hàng nếu chưa có.
     * @return ID bản ghi vừa tạo hoặc null nếu thất bại.
     */
    public Long themKhachHang(KhachHang kh) {
        if (kh == null) {
            return null;
        }

        if (kh.getMaKhachHang() == null || kh.getMaKhachHang().trim().isEmpty()) {
            kh.setMaKhachHang(sinhMaKhachHang());
        }

        String sql = "INSERT INTO khach_hang (" +
                "ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, " +
                "website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                "doanh_thu_uoc_tinh, cong_ty_me_id, trang_thai, co_rui_ro, mo_ta_chi_tiet, ngay_tao" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            datThamSoLuu(ps, kh);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        kh.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm khách hàng: " + e.getMessage(), e);
            return null;
        }
        return null;
    }

    /**
     * Tương thích ngược với các test suite và module cũ (Story S3-05 & S3-06).
     */
    public Long themMoi(KhachHang khachHang) throws SQLException {
        return themKhachHang(khachHang);
    }

    /**
     * Cập nhật thông tin khách hàng doanh nghiệp (Story S3-01).
     */
    public boolean capNhatKhachHang(KhachHang kh) throws SQLException {
        if (kh == null || kh.getId() == null) {
            return false;
        }

        String sql = "UPDATE khach_hang SET " +
                "ten_cong_ty = ?, ten_chuan_hoa = ?, ma_so_thue = ?, nganh_nghe_id = ?, quy_mo_id = ?, " +
                "website = ?, website_chuan_hoa = ?, dia_chi = ?, khu_vuc_id = ?, nguoi_so_huu_id = ?, " +
                "nhom_kinh_doanh_id = ?, doanh_thu_uoc_tinh = ?, cong_ty_me_id = ?, trang_thai = ?, " +
                "mo_ta_chi_tiet = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, kh.getTenCongTy() != null ? kh.getTenCongTy().trim() : "");
            ps.setString(idx++, kh.getTenChuanHoa() != null ? kh.getTenChuanHoa() : chuanHoaTen(kh.getTenCongTy()));

            if (kh.getMaSoThue() != null && !kh.getMaSoThue().trim().isEmpty()) {
                ps.setString(idx++, kh.getMaSoThue().trim());
            } else {
                ps.setNull(idx++, Types.VARCHAR);
            }

            if (kh.getNganhNgheId() != null && kh.getNganhNgheId() > 0) {
                ps.setLong(idx++, kh.getNganhNgheId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            if (kh.getQuyMoId() != null && kh.getQuyMoId() > 0) {
                ps.setLong(idx++, kh.getQuyMoId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            ps.setString(idx++, kh.getWebsite() != null ? kh.getWebsite().trim() : null);
            ps.setString(idx++, kh.getWebsiteChuanHoa() != null ? kh.getWebsiteChuanHoa() : chuanHoaWebsite(kh.getWebsite()));
            ps.setString(idx++, kh.getDiaChi() != null ? kh.getDiaChi().trim() : null);

            if (kh.getKhuVucId() != null && kh.getKhuVucId() > 0) {
                ps.setLong(idx++, kh.getKhuVucId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            if (kh.getNguoiSoHuuId() != null && kh.getNguoiSoHuuId() > 0) {
                ps.setLong(idx++, kh.getNguoiSoHuuId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            if (kh.getNhomKinhDoanhId() != null && kh.getNhomKinhDoanhId() > 0) {
                ps.setLong(idx++, kh.getNhomKinhDoanhId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            ps.setBigDecimal(idx++, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);

            if (kh.getCongTyMeId() != null && kh.getCongTyMeId() > 0) {
                ps.setLong(idx++, kh.getCongTyMeId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            ps.setString(idx++, kh.getTrangThai() != null ? kh.getTrangThai() : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
            ps.setString(idx++, kh.getMoTaChiTiet() != null ? kh.getMoTaChiTiet().trim() : null);
            ps.setLong(idx++, kh.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tìm khách hàng theo ID đầy đủ thông tin JOIN.
     * Không ném checked exception để bảo đảm tương thích toàn hệ thống.
     */
        public KhachHang timTheoId(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT " + COT_SELECT_DAY_DU + " " + BANG_JOIN_CO_BAN + " WHERE kh.id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            // Fallback cho môi trường test khi các bảng lookup không tồn tại
            String sqlFallback = "SELECT kh.*, " +
                    "me.ten_cong_ty AS ten_cong_ty_me, me.ma_khach_hang AS ma_cong_ty_me, " +
                    "nd.ho_ten AS ten_nguoi_so_huu, " +
                    "nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                    "FROM khach_hang kh " +
                    "LEFT JOIN khach_hang me ON kh.cong_ty_me_id = me.id " +
                    "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                    "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                    "WHERE kh.id = ?";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sqlFallback)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToKhachHang(rs);
                    }
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi tìm khách hàng theo ID [" + id + "]: " + ex.getMessage(), ex);
            }
        }
        return null;
    }

    /**
     * Tìm khách hàng theo mã khách hàng (Story S3-01).
     */
    public KhachHang timTheoMa(String maKhachHang) throws SQLException {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT " + COT_SELECT_DAY_DU + " " + BANG_JOIN_CO_BAN + " WHERE kh.ma_khach_hang = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            String sqlFallback = "SELECT kh.*, " +
                    "me.ten_cong_ty AS ten_cong_ty_me, me.ma_khach_hang AS ma_cong_ty_me, " +
                    "nd.ho_ten AS ten_nguoi_so_huu, " +
                    "nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                    "FROM khach_hang kh " +
                    "LEFT JOIN khach_hang me ON kh.cong_ty_me_id = me.id " +
                    "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                    "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                    "WHERE kh.ma_khach_hang = ?";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sqlFallback)) {
                ps.setString(1, maKhachHang.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToKhachHang(rs);
                    }
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi tìm khách hàng theo mã [" + maKhachHang + "]: " + ex.getMessage(), ex);
            }
        }
        return null;
    }

    /**
     * Kiểm tra trùng mã số thuế (AC2).
     */
    public boolean kiemTraTrungMaSoThue(String maSoThue, Long excludeId) throws SQLException {
        if (maSoThue == null || maSoThue.trim().isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM khach_hang WHERE ma_so_thue = ? ");
        if (excludeId != null && excludeId > 0) {
            sql.append("AND id <> ? ");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maSoThue.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Lấy tên công ty đang sở hữu mã số thuế bị trùng để cảnh báo chi tiết (AC2).
     */
    public String layTenCongTyTheoMaSoThue(String maSoThue, Long excludeId) throws SQLException {
        if (maSoThue == null || maSoThue.trim().isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ten_cong_ty FROM khach_hang WHERE ma_so_thue = ? ");
        if (excludeId != null && excludeId > 0) {
            sql.append("AND id <> ? ");
        }
        sql.append("LIMIT 1");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maSoThue.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ten_cong_ty");
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra trùng mã khách hàng.
     */
    public boolean kiemTraTrungMaKhachHang(String maKhachHang, Long excludeId) throws SQLException {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM khach_hang WHERE ma_khach_hang = ? ");
        if (excludeId != null && excludeId > 0) {
            sql.append("AND id <> ? ");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maKhachHang.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Lấy danh sách khách hàng doanh nghiệp theo Data Scope và các bộ lọc nghiệp vụ (AC1, AC3, AC4).
     */
    public List<KhachHang> layDanhSach(Long userId,
                                       Set<Long> dsNhomIds,
                                       PhamViDuLieu phamVi,
                                       String tuKhoa,
                                       String trangThai,
                                       Long nganhNgheId,
                                       Long quyMoId,
                                       int offset,
                                       int limit) throws SQLException {
        List<KhachHang> ketQua = new ArrayList<>();
        List<Object> thamSo = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(COT_SELECT_DAY_DU).append(" ").append(BANG_JOIN_CO_BAN).append(" WHERE 1=1 ");

        apDungDieuKienLoc(sql, thamSo, userId, dsNhomIds, phamVi, tuKhoa, trangThai, nganhNgheId, quyMoId);

        sql.append(" ORDER BY kh.id DESC");

        if (limit > 0) {
            sql.append(" LIMIT ? OFFSET ?");
            thamSo.add(limit);
            thamSo.add(Math.max(0, offset));
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapResultSetToKhachHang(rs));
                }
            }
        }
        return ketQua;
    }

    /**
     * Đếm tổng số khách hàng thỏa mãn điều kiện lọc và Data Scope.
     */
    public int demTongSo(Long userId,
                         Set<Long> dsNhomIds,
                         PhamViDuLieu phamVi,
                         String tuKhoa,
                         String trangThai,
                         Long nganhNgheId,
                         Long quyMoId) throws SQLException {
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total ").append(BANG_JOIN_CO_BAN).append(" WHERE 1=1 ");

        apDungDieuKienLoc(sql, thamSo, userId, dsNhomIds, phamVi, tuKhoa, trangThai, nganhNgheId, quyMoId);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Tự động sinh mã khách hàng tiếp theo có dạng KH000001 (Story S3-01).
     */
    public String sinhMaKhachHang() {
        String sql = "SELECT MAX(id) FROM khach_hang";
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
        return String.format("KH%06d", nextNum);
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC QUẢN LÝ QUAN HỆ CÔNG TY MẸ - CON (STORY S3-05)
    // =========================================================================

    public List<KhachHang> layDanhSachCongTyCon(Long congTyMeId) throws SQLException {
        List<KhachHang> ds = new ArrayList<>();
        if (congTyMeId == null) {
            return ds;
        }

        String sql = "SELECT " + COT_SELECT_DAY_DU + " " + BANG_JOIN_CO_BAN +
                " WHERE kh.cong_ty_me_id = ? ORDER BY kh.ten_cong_ty ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, congTyMeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSetToKhachHang(rs));
                }
            }
        }
        return ds;
    }

    public boolean ganCongTyMe(Long khachHangConId, Long congTyMeId) throws SQLException {
        if (khachHangConId == null) {
            return false;
        }

        String sql = "UPDATE khach_hang SET cong_ty_me_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (congTyMeId != null) {
                ps.setLong(1, congTyMeId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, khachHangConId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean kiemTraVongLapCongTyMeCon(Long khachHangConId, Long congTyMeMoiId) throws SQLException {
        if (khachHangConId == null || congTyMeMoiId == null) {
            return false;
        }
        if (khachHangConId.equals(congTyMeMoiId)) {
            return true;
        }

        Set<Long> daDuyet = new HashSet<>();
        Long hienTaiId = congTyMeMoiId;

        while (hienTaiId != null) {
            if (hienTaiId.equals(khachHangConId)) {
                return true;
            }
            if (!daDuyet.add(hienTaiId)) {
                return true;
            }

            String sql = "SELECT cong_ty_me_id FROM khach_hang WHERE id = ?";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, hienTaiId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long meId = rs.getLong("cong_ty_me_id");
                        hienTaiId = rs.wasNull() ? null : meId;
                    } else {
                        hienTaiId = null;
                    }
                }
            }
        }

        return false;
    }

    public List<KhachHang> layDanhSachKhachHangKhaDungLamCongTyMe(Long khachHangId) throws SQLException {
        List<KhachHang> tatCa = layTatCa();
        if (khachHangId == null) {
            return tatCa;
        }

        Set<Long> tapConChau = new HashSet<>();
        tapConChau.add(khachHangId);
        timConChauDeQuy(khachHangId, tapConChau);

        List<KhachHang> ketQua = new ArrayList<>();
        for (KhachHang kh : tatCa) {
            if (!tapConChau.contains(kh.getId())) {
                ketQua.add(kh);
            }
        }
        return ketQua;
    }

    public List<KhachHang> layDanhSachKhachHangKhaDungLamCongTyCon(Long congTyMeId) throws SQLException {
        List<KhachHang> tatCa = layTatCa();
        if (congTyMeId == null) {
            return tatCa;
        }

        Set<Long> toTien = new HashSet<>();
        toTien.add(congTyMeId);
        Long hienTaiId = congTyMeId;
        while (hienTaiId != null) {
            String sql = "SELECT cong_ty_me_id FROM khach_hang WHERE id = ?";
            try (Connection conn = DatabaseConnection.layKetNoi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, hienTaiId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long meId = rs.getLong("cong_ty_me_id");
                        hienTaiId = rs.wasNull() ? null : meId;
                        if (hienTaiId != null) {
                            toTien.add(hienTaiId);
                        }
                    } else {
                        hienTaiId = null;
                    }
                }
            }
        }

        List<KhachHang> ketQua = new ArrayList<>();
        for (KhachHang kh : tatCa) {
            if (!toTien.contains(kh.getId()) && !congTyMeId.equals(kh.getCongTyMeId())) {
                ketQua.add(kh);
            }
        }
        return ketQua;
    }

    private void timConChauDeQuy(Long chaId, Set<Long> tapConChau) throws SQLException {
        List<KhachHang> dsCon = layDanhSachCongTyCon(chaId);
        for (KhachHang con : dsCon) {
            if (tapConChau.add(con.getId())) {
                timConChauDeQuy(con.getId(), tapConChau);
            }
        }
    }

    public List<KhachHang> layTatCa() {
        List<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_DAY_DU + " " + BANG_JOIN_CO_BAN + " ORDER BY kh.ten_cong_ty ASC";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(mapResultSetToKhachHang(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy tất cả khách hàng: " + e.getMessage(), e);
        }
        return ds;
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC QUẢN LÝ CỜ RỦI RO RỜI BỎ (STORY S3-08)
    // =========================================================================

    public boolean capNhatCoRuiRo(Long khachHangId, boolean coRuiRo) {
        if (khachHangId == null || khachHangId <= 0) {
            return false;
        }

        String sql = "UPDATE khach_hang SET co_rui_ro = ?, rui_ro_cap_nhat_luc = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coRuiRo ? 1 : 0);
            ps.setLong(2, khachHangId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật cờ rủi ro cho khách hàng [" + khachHangId + "]: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean kiemTraCoRuiRo(Long khachHangId) {
        if (khachHangId == null || khachHangId <= 0) {
            return false;
        }

        String sql = "SELECT co_rui_ro FROM khach_hang WHERE id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("co_rui_ro") == 1;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra cờ rủi ro khách hàng [" + khachHangId + "]: " + e.getMessage(), e);
        }

        return false;
    }

    public List<KhachHang> layDanhSachKhachHangRuiRo(Long nguoiSoHuuId) {
        List<KhachHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(COT_SELECT_DAY_DU).append(" ").append(BANG_JOIN_CO_BAN)
                .append(" WHERE kh.co_rui_ro = 1 ");

        if (nguoiSoHuuId != null && nguoiSoHuuId > 0) {
            sql.append("AND kh.nguoi_so_huu_id = ? ");
        }

        sql.append("ORDER BY kh.rui_ro_cap_nhat_luc DESC, kh.id DESC");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (nguoiSoHuuId != null && nguoiSoHuuId > 0) {
                ps.setLong(1, nguoiSoHuuId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khách hàng rủi ro: " + e.getMessage(), e);
        }

        return list;
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH ÁNH XẠ RESULTSET & LỌC
    // =========================================================================


    /**
     * Tìm kiếm và lọc danh sách khách hàng (Story S3-07 AC1, AC2).
     * Server-side Data Scope, hỗ trợ tìm kiếm theo SĐT người liên hệ không sinh bản ghi trùng.
     */
    public List<KhachHang> timKiemVaLoc(NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        List<KhachHang> danhSach = new ArrayList<>();
        List<Object> thamSo = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
                "kh.nganh_nghe_id, nn.ten_nganh AS ten_nganh_nghe, " +
                "kh.quy_mo_id, qm.ten_quy_mo AS ten_quy_mo, " +
                "kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
                "kh.khu_vuc_id, kv.ten_khu_vuc AS ten_khu_vuc, " +
                "kh.nguoi_so_huu_id, nd.ho_ten AS ten_nguoi_so_huu, " +
                "kh.nhom_kinh_doanh_id, nkd.ten_nhom AS ten_nhom_kinh_doanh, " +
                "kh.doanh_thu_uoc_tinh, kh.cong_ty_me_id, ctm.ten_cong_ty AS ten_cong_ty_me, " +
                "kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, kh.lan_tuong_tac_cuoi, " +
                "kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, kh.ngay_tao, kh.created_at, kh.updated_at, " +
                "nlh.ho_ten AS ten_nguoi_lien_he_chinh, " +
                "nlh.so_dien_thoai AS so_dien_thoai_lien_he, " +
                "nlh.email AS email_lien_he " +
                "FROM khach_hang kh " +
                "LEFT JOIN nganh_nghe nn ON kh.nganh_nghe_id = nn.id " +
                "LEFT JOIN quy_mo_doanh_nghiep qm ON kh.quy_mo_id = qm.id " +
                "LEFT JOIN khu_vuc_dia_ly kv ON kh.khu_vuc_id = kv.id " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "LEFT JOIN khach_hang ctm ON kh.cong_ty_me_id = ctm.id " +
                "LEFT JOIN ( " +
                "    SELECT nlh1.khach_hang_id, nlh1.ho_ten, nlh1.so_dien_thoai, nlh1.email " +
                "    FROM nguoi_lien_he nlh1 " +
                "    INNER JOIN ( " +
                "        SELECT khach_hang_id, MAX(id) AS max_nlh_id " +
                "        FROM nguoi_lien_he " +
                "        WHERE (trang_thai = 'DANG_HOAT_DONG' OR trang_thai IS NULL) " +
                "        GROUP BY khach_hang_id " +
                "    ) latest ON nlh1.id = latest.max_nlh_id " +
                ") nlh ON kh.id = nlh.khach_hang_id " +
                "WHERE (kh.gop_vao_khach_hang_id IS NULL) "
        );

        apDungDieuKienLoc(sql, thamSo, user, boLoc);

        sql.append(" ORDER BY kh.created_at DESC, kh.id DESC");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm kiếm và lọc khách hàng: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Đếm tổng số lượng khách hàng theo bộ lọc và Data Scope (Story S3-07).
     */
    public int demSoLuong(NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT kh.id) " +
                "FROM khach_hang kh " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "WHERE (kh.gop_vao_khach_hang_id IS NULL) "
        );

        apDungDieuKienLoc(sql, thamSo, user, boLoc);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng khách hàng: " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean kiemTraTonTaiMaKhachHang(String maKhachHang, Long excludeId) {
        try {
            return kiemTraTrungMaKhachHang(maKhachHang, excludeId);
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi kiểm tra mã KH: " + e.getMessage());
            return false;
        }
    }

    public boolean kiemTraTonTaiMaSoThue(String maSoThue, Long excludeId) {
        try {
            return kiemTraTrungMaSoThue(maSoThue, excludeId);
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi kiểm tra MST: " + e.getMessage());
            return false;
        }
    }

    private void apDungDieuKienLoc(StringBuilder sql, List<Object> params, NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        // 1. Phân quyền Data Scope (Server-side Enforcement)
        if (user != null) {
            PhamViDuLieu phamVi = (boLoc != null && boLoc.getPhamVi() != null)
                    ? boLoc.getPhamVi()
                    : user.getPhamViHienTai();

            PhamViDuLieu maxScope = user.getPhamViToiDa() != null ? user.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
            if (phamVi == null || maxScope == PhamViDuLieu.CA_NHAN || phamVi == PhamViDuLieu.CA_NHAN) {
                sql.append(" AND kh.nguoi_so_huu_id = ?");
                params.add(user.getId() != null ? user.getId() : -1L);
            } else if (maxScope == PhamViDuLieu.NHOM || phamVi == PhamViDuLieu.NHOM) {
                Long nhomId = user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : -1L;
                sql.append(" AND (kh.nhom_kinh_doanh_id = ? OR kh.nguoi_so_huu_id IN (SELECT id FROM nguoi_dung WHERE nhom_kinh_doanh_id = ?))");
                params.add(nhomId);
                params.add(nhomId);
            }
            // TOAN_BO: Không áp điều kiện sở hữu
        }

        if (boLoc == null) {
            return;
        }

        // 2. AC1: Lọc theo trạng thái
        if (boLoc.getTrangThai() != null && !boLoc.getTrangThai().isBlank()) {
            TrangThaiKhachHangEnum en = TrangThaiKhachHangEnum.tuChuoi(boLoc.getTrangThai());
            if (en != null) {
                sql.append(" AND (kh.trang_thai = ? OR kh.trang_thai = ?)");
                params.add(en.getMa());
                params.add(en.getTenHienThi());
            } else {
                sql.append(" AND kh.trang_thai = ?");
                params.add(boLoc.getTrangThai().trim());
            }
        }

        // 3. AC1: Lọc theo ngành nghề
        if (boLoc.getNganhNgheId() != null && boLoc.getNganhNgheId() > 0) {
            sql.append(" AND kh.nganh_nghe_id = ?");
            params.add(boLoc.getNganhNgheId());
        }

        // 4. AC1: Lọc theo quy mô doanh nghiệp
        if (boLoc.getQuyMoId() != null && boLoc.getQuyMoId() > 0) {
            sql.append(" AND kh.quy_mo_id = ?");
            params.add(boLoc.getQuyMoId());
        }

        // 5. AC1: Lọc theo khu vực
        if (boLoc.getKhuVucId() != null && boLoc.getKhuVucId() > 0) {
            sql.append(" AND kh.khu_vuc_id = ?");
            params.add(boLoc.getKhuVucId());
        }

        // 6. AC1: Lọc theo người sở hữu
        if (boLoc.getNguoiSoHuuId() != null && boLoc.getNguoiSoHuuId() > 0) {
            sql.append(" AND kh.nguoi_so_huu_id = ?");
            params.add(boLoc.getNguoiSoHuuId());
        }

        // 7. AC2: Tìm theo tên công ty / tên khách hàng
        if (boLoc.getTenCongTy() != null && !boLoc.getTenCongTy().isBlank()) {
            String p = "%" + boLoc.getTenCongTy().trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(kh.ten_cong_ty) LIKE ? OR LOWER(COALESCE(kh.ten_chuan_hoa, '')) LIKE ?)");
            params.add(p);
            params.add(p);
        }

        // 8. AC2: Tìm theo mã số thuế
        if (boLoc.getMaSoThue() != null && !boLoc.getMaSoThue().isBlank()) {
            sql.append(" AND LOWER(COALESCE(kh.ma_so_thue, '')) LIKE ?");
            params.add("%" + boLoc.getMaSoThue().trim().toLowerCase() + "%");
        }

        // 9. AC2: Tìm theo số điện thoại người liên hệ
        if (boLoc.getSoDienThoai() != null && !boLoc.getSoDienThoai().isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM nguoi_lien_he nlh_sdt WHERE nlh_sdt.khach_hang_id = kh.id AND nlh_sdt.so_dien_thoai LIKE ?)");
            params.add("%" + boLoc.getSoDienThoai().trim() + "%");
        }

        // 10. Tìm kiếm tổng hợp theo từ khóa chung (Tên, MST, Mã KH, SĐT/Tên liên hệ)
        if (boLoc.getTuKhoa() != null && !boLoc.getTuKhoa().isBlank()) {
            String kw = "%" + boLoc.getTuKhoa().trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(kh.ten_cong_ty) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ten_chuan_hoa, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_so_thue, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_khach_hang, '')) LIKE ? ")
               .append("OR EXISTS (SELECT 1 FROM nguoi_lien_he nlh_kw WHERE nlh_kw.khach_hang_id = kh.id AND (nlh_kw.so_dien_thoai LIKE ? OR LOWER(nlh_kw.ho_ten) LIKE ?)))");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add("%" + boLoc.getTuKhoa().trim() + "%");
            params.add(kw);
        }
    }

    public KhachHang mapResultSetToKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setId(rs.getLong("id"));
        kh.setMaKhachHang(rs.getString("ma_khach_hang"));
        kh.setTenCongTy(rs.getString("ten_cong_ty"));
        kh.setTenChuanHoa(rs.getString("ten_chuan_hoa"));
        kh.setMaSoThue(rs.getString("ma_so_thue"));

        long nnId = rs.getLong("nganh_nghe_id");
        kh.setNganhNgheId(rs.wasNull() ? null : nnId);

        long qmId = rs.getLong("quy_mo_id");
        kh.setQuyMoId(rs.wasNull() ? null : qmId);

        kh.setWebsite(rs.getString("website"));
        kh.setWebsiteChuanHoa(rs.getString("website_chuan_hoa"));
        kh.setDiaChi(rs.getString("dia_chi"));

        long kvId = rs.getLong("khu_vuc_id");
        kh.setKhuVucId(rs.wasNull() ? null : kvId);

        long nshId = rs.getLong("nguoi_so_huu_id");
        kh.setNguoiSoHuuId(rs.wasNull() ? null : nshId);

        long nkdId = rs.getLong("nhom_kinh_doanh_id");
        kh.setNhomKinhDoanhId(rs.wasNull() ? null : nkdId);

        kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));

        long meId = rs.getLong("cong_ty_me_id");
        kh.setCongTyMeId(rs.wasNull() ? null : meId);

        kh.setTrangThai(rs.getString("trang_thai"));

        try {
            kh.setCoRuiRo(rs.getInt("co_rui_ro") == 1);
        } catch (SQLException ignored) {
            try {
                kh.setCoRuiRo(rs.getBoolean("co_rui_ro"));
            } catch (SQLException ignored2) {}
        }

        try {
            kh.setRuiRoCapNhatLuc(rs.getTimestamp("rui_ro_cap_nhat_luc"));
        } catch (SQLException ignored) {}

        try {
            kh.setLanTuongTacCuoi(rs.getTimestamp("lan_tuong_tac_cuoi"));
        } catch (SQLException ignored) {}

        long gopId = rs.getLong("gop_vao_khach_hang_id");
        kh.setGopVaoKhachHangId(rs.wasNull() ? null : gopId);

        kh.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));

        Date d = rs.getDate("ngay_tao");
        kh.setNgayTao(d != null ? d.toLocalDate() : LocalDate.now());

        try {
            kh.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNguoiLienHeChinh(rs.getString("ten_nguoi_lien_he_chinh"));
        } catch (SQLException ignored) {}
        try {
            kh.setSoDienThoaiLienHe(rs.getString("so_dien_thoai_lien_he"));
        } catch (SQLException ignored) {}
        try {
            kh.setEmailLienHe(rs.getString("email_lien_he"));
        } catch (SQLException ignored) {}

        try {
            kh.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException ignored) {}

        // Join columns nếu có trong result set
        try {
            kh.setTenCongTyMe(rs.getString("ten_cong_ty_me"));
        } catch (SQLException ignored) {}
        try {
            kh.setMaCongTyMe(rs.getString("ma_cong_ty_me"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNguoiSoHuu(rs.getString("ten_nguoi_so_huu"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNhomKinhDoanh(rs.getString("ten_nhom_kinh_doanh"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNganhNghe(rs.getString("ten_nganh_nghe"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenQuyMo(rs.getString("ten_quy_mo"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenKhuVuc(rs.getString("ten_khu_vuc"));
        } catch (SQLException ignored) {}

        return kh;
    }

    public KhachHang mapResultSet(ResultSet rs) throws SQLException {
        return mapResultSetToKhachHang(rs);
    }

    private void datThamSoLuu(PreparedStatement ps, KhachHang kh) throws SQLException {
        int idx = 1;
        ps.setString(idx++, kh.getMaKhachHang() != null ? kh.getMaKhachHang().trim() : sinhMaKhachHang());
        ps.setString(idx++, kh.getTenCongTy() != null ? kh.getTenCongTy().trim() : "");
        ps.setString(idx++, kh.getTenChuanHoa() != null ? kh.getTenChuanHoa() : chuanHoaTen(kh.getTenCongTy()));

        if (kh.getMaSoThue() != null && !kh.getMaSoThue().trim().isEmpty()) {
            ps.setString(idx++, kh.getMaSoThue().trim());
        } else {
            ps.setNull(idx++, Types.VARCHAR);
        }

        if (kh.getNganhNgheId() != null && kh.getNganhNgheId() > 0) {
            ps.setLong(idx++, kh.getNganhNgheId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (kh.getQuyMoId() != null && kh.getQuyMoId() > 0) {
            ps.setLong(idx++, kh.getQuyMoId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setString(idx++, kh.getWebsite() != null ? kh.getWebsite().trim() : null);
        ps.setString(idx++, kh.getWebsiteChuanHoa() != null ? kh.getWebsiteChuanHoa() : chuanHoaWebsite(kh.getWebsite()));
        ps.setString(idx++, kh.getDiaChi() != null ? kh.getDiaChi().trim() : null);

        if (kh.getKhuVucId() != null && kh.getKhuVucId() > 0) {
            ps.setLong(idx++, kh.getKhuVucId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        long nguoiSoHuuId = (kh.getNguoiSoHuuId() != null && kh.getNguoiSoHuuId() > 0) ? kh.getNguoiSoHuuId() : 1L;
        ps.setLong(idx++, nguoiSoHuuId);

        if (kh.getNhomKinhDoanhId() != null && kh.getNhomKinhDoanhId() > 0) {
            ps.setLong(idx++, kh.getNhomKinhDoanhId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setBigDecimal(idx++, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);

        if (kh.getCongTyMeId() != null && kh.getCongTyMeId() > 0) {
            ps.setLong(idx++, kh.getCongTyMeId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setString(idx++, kh.getTrangThai() != null ? kh.getTrangThai() : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        ps.setInt(idx++, kh.isCoRuiRo() ? 1 : 0);
        ps.setString(idx++, kh.getMoTaChiTiet() != null ? kh.getMoTaChiTiet().trim() : null);

        Date ngayTaoSql = kh.getNgayTao() != null ? Date.valueOf(kh.getNgayTao()) : Date.valueOf(LocalDate.now());
        ps.setDate(idx++, ngayTaoSql);
    }

    private void apDungDieuKienLoc(StringBuilder sql,
                                  List<Object> thamSo,
                                  Long userId,
                                  Set<Long> dsNhomIds,
                                  PhamViDuLieu phamVi,
                                  String tuKhoa,
                                  String trangThai,
                                  Long nganhNgheId,
                                  Long quyMoId) {
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }

        // 1. Phân quyền Data Scope (AC4)
        if (phamVi == PhamViDuLieu.CA_NHAN) {
            sql.append("AND kh.nguoi_so_huu_id = ? ");
            thamSo.add(userId != null ? userId : -1L);
        } else if (phamVi == PhamViDuLieu.NHOM) {
            if (dsNhomIds != null && !dsNhomIds.isEmpty()) {
                sql.append("AND (kh.nhom_kinh_doanh_id IN (");
                int i = 0;
                for (Long nId : dsNhomIds) {
                    if (i > 0) sql.append(", ");
                    sql.append("?");
                    thamSo.add(nId);
                    i++;
                }
                sql.append(") OR kh.nguoi_so_huu_id IN (SELECT id FROM nguoi_dung WHERE nhom_kinh_doanh_id IN (");
                i = 0;
                for (Long nId : dsNhomIds) {
                    if (i > 0) sql.append(", ");
                    sql.append("?");
                    thamSo.add(nId);
                    i++;
                }
                sql.append("))) ");
            } else {
                sql.append("AND kh.nhom_kinh_doanh_id = -1 ");
            }
        }
        // TOAN_BO: Không thêm điều kiện sở hữu

        // 2. Lọc theo từ khóa tìm kiếm
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(kh.ten_cong_ty) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_khach_hang, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_so_thue, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.website, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.dia_chi, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(nd.ho_ten, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(nkd.ten_nhom, '')) LIKE ?) ");
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
        }

        // 3. Lọc theo trạng thái (AC3: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác)
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            TrangThaiKhachHangEnum en = TrangThaiKhachHangEnum.tuChuoi(trangThai);
            if (en != null) {
                sql.append("AND (kh.trang_thai = ? OR kh.trang_thai = ?) ");
                thamSo.add(en.getMa());
                thamSo.add(en.getTenHienThi());
            } else {
                sql.append("AND kh.trang_thai = ? ");
                thamSo.add(trangThai.trim());
            }
        }

        // 4. Lọc theo ngành nghề
        if (nganhNgheId != null && nganhNgheId > 0) {
            sql.append("AND kh.nganh_nghe_id = ? ");
            thamSo.add(nganhNgheId);
        }

        // 5. Lọc theo quy mô doanh nghiệp
        if (quyMoId != null && quyMoId > 0) {
            sql.append("AND kh.quy_mo_id = ? ");
            thamSo.add(quyMoId);
        }
    }

    private String chuanHoaTen(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String chuanHoaWebsite(String s) {
        if (s == null) return null;
        String w = s.trim().toLowerCase();
        w = w.replaceFirst("^https?://", "");
        w = w.replaceFirst("^www\\.", "");
        if (w.endsWith("/")) {
            w = w.substring(0, w.length() - 1);
        }
        return w;
    }
}
