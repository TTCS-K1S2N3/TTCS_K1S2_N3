package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.KhachHang360DTO;
import vn.nhom10.crm.model.CoHoi;
import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.TepDinhKem;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object phục vụ truy vấn dữ liệu toàn diện cho Trang 360 Khách hàng (Story S3-03).
 * Đáp ứng các tiêu chí chấp nhận:
 * - AC1: Gom thông tin công ty, danh sách người liên hệ, cơ hội đang mở và đã đóng,
 *        dòng thời gian hoạt động, tệp đính kèm.
 * - AC2: Tổng hợp chính xác tổng giá trị đã ký và giá trị cơ hội đang mở.
 * - AC3: Tối ưu chỉ mục và truy vấn PreparedStatement đạt thời gian tải dưới 1.5s với 500 hoạt động.
 */
public class KhachHang360DAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHang360DAO.class.getName());

    /**
     * Tìm thông tin công ty chi tiết theo ID khách hàng.
     */
    public KhachHang timKhachHangTheoId(Long khachHangId) throws SQLException {
        if (khachHangId == null || khachHangId <= 0) {
            return null;
        }

        String sql = "SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
                "kh.nganh_nghe_id, nn.ten_nganh AS ten_nganh_nghe, " +
                "kh.quy_mo_id, qm.ten_quy_mo AS ten_quy_mo, " +
                "kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
                "kh.khu_vuc_id, kv.ten_khu_vuc AS ten_khu_vuc, " +
                "kh.nguoi_so_huu_id, nd.ho_ten AS ten_nguoi_so_huu, " +
                "kh.nhom_kinh_doanh_id, nkd.ten_nhom AS ten_nhom_kinh_doanh, " +
                "kh.doanh_thu_uoc_tinh, kh.cong_ty_me_id, ctm.ten_cong_ty AS ten_cong_ty_me, " +
                "kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, kh.lan_tuong_tac_cuoi, " +
                "kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, kh.ngay_tao, kh.created_at, kh.updated_at " +
                "FROM khach_hang kh " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "LEFT JOIN nganh_nghe nn ON kh.nganh_nghe_id = nn.id " +
                "LEFT JOIN quy_mo_doanh_nghiep qm ON kh.quy_mo_id = qm.id " +
                "LEFT JOIN khu_vuc_dia_ly kv ON kh.khu_vuc_id = kv.id " +
                "LEFT JOIN khach_hang ctm ON kh.cong_ty_me_id = ctm.id " +
                "WHERE kh.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKhachHang(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh sách người liên hệ của khách hàng, ưu tiên đầu mối chính lên đầu.
     */
    public List<NguoiLienHe> layDsNguoiLienHe(Long khachHangId) throws SQLException {
        List<NguoiLienHe> ketQua = new ArrayList<>();
        if (khachHangId == null || khachHangId <= 0) {
            return ketQua;
        }

        String sql = "SELECT nlh.id, nlh.khach_hang_id, nlh.ho_ten, nlh.chuc_danh, nlh.email, " +
                "nlh.so_dien_thoai, nlh.vai_tro_quyet_dinh, nlh.la_dau_moi_chinh, " +
                "nlh.trang_thai, nlh.created_at, nlh.updated_at, " +
                "kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang " +
                "FROM nguoi_lien_he nlh " +
                "JOIN khach_hang kh ON nlh.khach_hang_id = kh.id " +
                "WHERE nlh.khach_hang_id = ? " +
                "ORDER BY nlh.la_dau_moi_chinh DESC, nlh.ho_ten ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NguoiLienHe nlh = new NguoiLienHe();
                    nlh.setId(rs.getLong("id"));
                    nlh.setKhachHangId(rs.getLong("khach_hang_id"));
                    nlh.setHoTen(rs.getString("ho_ten"));
                    nlh.setChucDanh(rs.getString("chuc_danh"));
                    nlh.setEmail(rs.getString("email"));
                    nlh.setSoDienThoai(rs.getString("so_dien_thoai"));
                    nlh.setVaiTroQuyetDinh(rs.getString("vai_tro_quyet_dinh"));
                    nlh.setLaDauMoiChinh(rs.getBoolean("la_dau_moi_chinh"));
                    nlh.setTrangThai(rs.getString("trang_thai"));
                    nlh.setCreatedAt(rs.getTimestamp("created_at"));
                    nlh.setUpdatedAt(rs.getTimestamp("updated_at"));
                    nlh.setTenKhachHang(rs.getString("ten_khach_hang"));
                    nlh.setMaKhachHang(rs.getString("ma_khach_hang"));
                    ketQua.add(nlh);
                }
            }
        }
        return ketQua;
    }

    /**
     * Lấy toàn bộ cơ hội bán hàng của khách hàng (cả mở và đóng).
     */
    public List<CoHoi> layDsCoHoi(Long khachHangId) throws SQLException {
        List<CoHoi> ketQua = new ArrayList<>();
        if (khachHangId == null || khachHangId <= 0) {
            return ketQua;
        }

        String sql = "SELECT ch.id, ch.ma_co_hoi, ch.ten_co_hoi, ch.khach_hang_id, " +
                "ch.nguoi_lien_he_chinh_id, ch.lead_id, ch.chien_dich_id, ch.nguon_lead_id, " +
                "ch.giai_doan_id, ch.nguoi_phu_trach_id, ch.nhom_kinh_doanh_id, " +
                "ch.gia_tri_du_kien, ch.xac_suat, ch.ly_do_sua_xac_suat, ch.ngay_chot_du_kien, " +
                "ch.trang_thai, ch.gia_tri_chot_thuc_te, ch.ngay_ky, ch.ly_do_thang_thua_id, " +
                "ch.doi_thu_id, ch.ngay_hoat_dong_cuoi, ch.bi_dinh_tre, ch.mo_ta_chi_tiet, " +
                "ch.ngay_tao, ch.created_at, ch.updated_at, " +
                "gd.ten_giai_doan, nd.ho_ten AS ten_nguoi_phu_trach, " +
                "nlh.ho_ten AS ten_nguoi_lien_he_chinh, " +
                "ld.ten_ly_do AS ten_ly_do_thang_thua " +
                "FROM co_hoi ch " +
                "LEFT JOIN giai_doan_pipeline gd ON ch.giai_doan_id = gd.id " +
                "LEFT JOIN nguoi_dung nd ON ch.nguoi_phu_trach_id = nd.id " +
                "LEFT JOIN nguoi_lien_he nlh ON ch.nguoi_lien_he_chinh_id = nlh.id " +
                "LEFT JOIN ly_do_thang_thua ld ON ch.ly_do_thang_thua_id = ld.id " +
                "WHERE ch.khach_hang_id = ? " +
                "ORDER BY ch.id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapResultSetToCoHoi(rs));
                }
            }
        }
        return ketQua;
    }

    /**
     * Lấy danh sách hợp đồng đã ký của khách hàng.
     */
    public List<HopDong> layDsHopDong(Long khachHangId) throws SQLException {
        List<HopDong> ketQua = new ArrayList<>();
        if (khachHangId == null || khachHangId <= 0) {
            return ketQua;
        }

        String sql = "SELECT id, so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, " +
                "khach_hang_id, ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, " +
                "dieu_khoan_thanh_toan, trang_thai, created_at, updated_at " +
                "FROM hop_dong " +
                "WHERE khach_hang_id = ? " +
                "ORDER BY ngay_ky DESC, id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HopDong hd = new HopDong();
                    hd.setId(rs.getLong("id"));
                    hd.setSoHopDong(rs.getString("so_hop_dong"));
                    hd.setBaoGiaId(rs.getLong("bao_gia_id"));
                    hd.setPhienBanBaoGiaId(rs.getLong("phien_ban_bao_gia_id"));
                    hd.setCoHoiId(rs.getLong("co_hoi_id"));
                    hd.setKhachHangId(rs.getLong("khach_hang_id"));
                    Date dKy = rs.getDate("ngay_ky");
                    if (dKy != null) hd.setNgayKy(dKy.toLocalDate());
                    Date dHl = rs.getDate("ngay_hieu_luc");
                    if (dHl != null) hd.setNgayHieuLuc(dHl.toLocalDate());
                    Date dHh = rs.getDate("ngay_het_han");
                    if (dHh != null) hd.setNgayHetHan(dHh.toLocalDate());
                    hd.setGiaTriHopDong(rs.getBigDecimal("gia_tri_hop_dong"));
                    hd.setDieuKhoanThanhToan(rs.getString("dieu_khoan_thanh_toan"));
                    hd.setTrangThai(rs.getString("trang_thai"));
                    hd.setCreatedAt(rs.getTimestamp("created_at"));
                    hd.setUpdatedAt(rs.getTimestamp("updated_at"));
                    ketQua.add(hd);
                }
            }
        }
        return ketQua;
    }

    /**
     * Tính tổng giá trị đã ký của khách hàng (AC2).
     * Ưu tiên tổng giá trị từ bảng `hop_dong` (trạng thái khác DA_HUY).
     * Nếu chưa có bản ghi hợp đồng, tính từ cơ hội Đóng Thắng (DONG_THANG).
     */
    public BigDecimal tinhTongGiaTriDaKy(Long khachHangId) throws SQLException {
        if (khachHangId == null || khachHangId <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tongHopDong = BigDecimal.ZERO;
        String sqlHopDong = "SELECT COALESCE(SUM(gia_tri_hop_dong), 0) AS tong " +
                "FROM hop_dong " +
                "WHERE khach_hang_id = ? AND trang_thai != 'DA_HUY'";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlHopDong)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tongHopDong = rs.getBigDecimal("tong");
                }
            }
        }

        if (tongHopDong != null && tongHopDong.compareTo(BigDecimal.ZERO) > 0) {
            return tongHopDong;
        }

        // Fallback kiểm tra tổng giá trị từ cơ hội đóng thắng
        String sqlCoHoiThang = "SELECT COALESCE(SUM(COALESCE(gia_tri_chot_thuc_te, gia_tri_du_kien)), 0) AS tong " +
                "FROM co_hoi " +
                "WHERE khach_hang_id = ? AND UPPER(trang_thai) = 'DONG_THANG'";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlCoHoiThang)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal tongThang = rs.getBigDecimal("tong");
                    return tongThang != null ? tongThang : BigDecimal.ZERO;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * Tính tổng giá trị cơ hội đang mở của khách hàng (AC2).
     */
    public BigDecimal tinhTongGiaTriCoHoiDangMo(Long khachHangId) throws SQLException {
        if (khachHangId == null || khachHangId <= 0) {
            return BigDecimal.ZERO;
        }

        String sql = "SELECT COALESCE(SUM(gia_tri_du_kien), 0) AS tong " +
                "FROM co_hoi " +
                "WHERE khach_hang_id = ? AND (UPPER(trang_thai) = 'MO' OR UPPER(trang_thai) NOT LIKE 'DONG%')";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal tong = rs.getBigDecimal("tong");
                    return tong != null ? tong : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy danh sách hoạt động tương tác của khách hàng theo phân trang / giới hạn (AC1, AC3).
     * Tận dụng index idx_hd_khach_hang để đạt hiệu năng dưới 1.5s với 500 hoạt động.
     */
    public List<HoatDong> layDsHoatDong(Long khachHangId, int limit, int offset) throws SQLException {
        List<HoatDong> ketQua = new ArrayList<>();
        if (khachHangId == null || khachHangId <= 0) {
            return ketQua;
        }

        if (limit <= 0) {
            limit = 500;
        }
        if (offset < 0) {
            offset = 0;
        }

        String sql = "SELECT hd.id, hd.ma_hoat_dong, hd.tieu_de, hd.loai_hoat_dong, hd.lead_id, " +
                "hd.khach_hang_id, hd.nguoi_lien_he_id, hd.co_hoi_id, hd.nguoi_phu_trach_id, " +
                "hd.nhom_kinh_doanh_id, hd.chi_phi, hd.thoi_gian_bat_dau, hd.thoi_gian_ket_thuc, " +
                "hd.trang_thai, hd.mo_ta_chi_tiet, hd.noi_dung, hd.ket_qua, hd.thoi_luong_phut, " +
                "hd.la_ghi_nhan_qua_khu, hd.la_ghi_nhan_nhanh, hd.ngay_tao, hd.created_at, hd.updated_at, " +
                "nd.ho_ten AS ten_nguoi_thuc_hien, " +
                "nlh.ho_ten AS ten_nguoi_lien_he " +
                "FROM hoat_dong hd " +
                "LEFT JOIN nguoi_dung nd ON hd.nguoi_phu_trach_id = nd.id " +
                "LEFT JOIN nguoi_lien_he nlh ON hd.nguoi_lien_he_id = nlh.id " +
                "WHERE hd.khach_hang_id = ? " +
                "ORDER BY COALESCE(hd.thoi_gian_bat_dau, hd.created_at) DESC, hd.id DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapResultSetToHoatDong(rs));
                }
            }
        }
        return ketQua;
    }

    /**
     * Đếm tổng số lượng hoạt động của khách hàng.
     */
    public int demTongSoHoatDong(Long khachHangId) throws SQLException {
        if (khachHangId == null || khachHangId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) AS total FROM hoat_dong WHERE khach_hang_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Lấy danh sách tệp đính kèm và tài liệu của khách hàng (AC1).
     */
    public List<TepDinhKem> layDsTepDinhKem(Long khachHangId) throws SQLException {
        List<TepDinhKem> ketQua = new ArrayList<>();
        if (khachHangId == null || khachHangId <= 0) {
            return ketQua;
        }

        String sql = "SELECT tdk.id, tdk.khach_hang_id, tdk.co_hoi_id, tdk.hoat_dong_id, tdk.bao_gia_id, " +
                "tdk.hop_dong_id, tdk.loai_tep, tdk.ten_file_goc, tdk.ten_file_luu, tdk.duong_dan, " +
                "tdk.mime_type, tdk.kich_thuoc_byte, tdk.nguoi_tai_len_id, tdk.created_at, " +
                "nd.ho_ten AS ten_nguoi_tai_len " +
                "FROM tep_dinh_kem tdk " +
                "LEFT JOIN nguoi_dung nd ON tdk.nguoi_tai_len_id = nd.id " +
                "WHERE tdk.khach_hang_id = ? " +
                "ORDER BY tdk.created_at DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TepDinhKem tdk = new TepDinhKem();
                    tdk.setId(rs.getLong("id"));
                    tdk.setKhachHangId(rs.getLong("khach_hang_id"));
                    long chId = rs.getLong("co_hoi_id");
                    if (!rs.wasNull()) tdk.setCoHoiId(chId);
                    long hdId = rs.getLong("hoat_dong_id");
                    if (!rs.wasNull()) tdk.setHoatDongId(hdId);
                    long bgId = rs.getLong("bao_gia_id");
                    if (!rs.wasNull()) tdk.setBaoGiaId(bgId);
                    long hopDongId = rs.getLong("hop_dong_id");
                    if (!rs.wasNull()) tdk.setHopDongId(hopDongId);
                    tdk.setLoaiTep(rs.getString("loai_tep"));
                    tdk.setTenFileGoc(rs.getString("ten_file_goc"));
                    tdk.setTenFileLuu(rs.getString("ten_file_luu"));
                    tdk.setDuongDan(rs.getString("duong_dan"));
                    tdk.setMimeType(rs.getString("mime_type"));
                    tdk.setKichThuocByte(rs.getLong("kich_thuoc_byte"));
                    tdk.setNguoiTaiLenId(rs.getLong("nguoi_tai_len_id"));
                    tdk.setCreatedAt(rs.getTimestamp("created_at"));
                    tdk.setTenNguoiTaiLen(rs.getString("ten_nguoi_tai_len"));
                    ketQua.add(tdk);
                }
            }
        }
        return ketQua;
    }

    /**
     * Thêm mới một hoạt động vào database (phục vụ ghi nhanh tương tác).
     */
    public Long themHoatDong(HoatDong hd) throws SQLException {
        if (hd == null) {
            return null;
        }

        if (hd.getMaHoatDong() == null || hd.getMaHoatDong().trim().isEmpty()) {
            hd.setMaHoatDong("HD" + System.currentTimeMillis());
        }

        String sql = "INSERT INTO hoat_dong (ma_hoat_dong, tieu_de, loai_hoat_dong, khach_hang_id, " +
                "nguoi_lien_he_id, co_hoi_id, nguoi_phu_trach_id, nhom_kinh_doanh_id, chi_phi, " +
                "thoi_gian_bat_dau, thoi_gian_ket_thuc, trang_thai, mo_ta_chi_tiet, noi_dung, ket_qua, " +
                "thoi_luong_phut, la_ghi_nhan_qua_khu, la_ghi_nhan_nhanh, ngay_tao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hd.getMaHoatDong());
            ps.setString(2, hd.getTieuDe());
            ps.setString(3, hd.getLoaiHoatDong() != null ? hd.getLoaiHoatDong() : "CUOC_GOI");
            if (hd.getKhachHangId() != null) ps.setLong(4, hd.getKhachHangId()); else ps.setNull(4, java.sql.Types.BIGINT);
            if (hd.getNguoiLienHeId() != null) ps.setLong(5, hd.getNguoiLienHeId()); else ps.setNull(5, java.sql.Types.BIGINT);
            if (hd.getCoHoiId() != null) ps.setLong(6, hd.getCoHoiId()); else ps.setNull(6, java.sql.Types.BIGINT);
            ps.setLong(7, hd.getNguoiPhuTrachId() != null ? hd.getNguoiPhuTrachId() : 1L);
            if (hd.getNhomKinhDoanhId() != null) ps.setLong(8, hd.getNhomKinhDoanhId()); else ps.setNull(8, java.sql.Types.BIGINT);
            ps.setBigDecimal(9, hd.getChiPhi() != null ? hd.getChiPhi() : BigDecimal.ZERO);
            ps.setTimestamp(10, hd.getThoiGianBatDau() != null ? hd.getThoiGianBatDau() : new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(11, hd.getThoiGianKetThuc());
            ps.setString(12, hd.getTrangThai() != null ? hd.getTrangThai() : "HOAN_THANH");
            ps.setString(13, hd.getMoTaChiTiet());
            ps.setString(14, hd.getNoiDung());
            ps.setString(15, hd.getKetQua());
            if (hd.getThoiLuongPhut() != null) ps.setInt(16, hd.getThoiLuongPhut()); else ps.setNull(16, java.sql.Types.INTEGER);
            ps.setBoolean(17, hd.isLaGhiNhanQuaKhu());
            ps.setBoolean(18, hd.isLaGhiNhanNhanh());
            ps.setDate(19, Date.valueOf(LocalDate.now()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        hd.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Tổng hợp toàn bộ dữ liệu bức tranh 360 độ của một khách hàng vào DTO (AC1, AC2, AC3).
     */
    public KhachHang360DTO layDuLieu360(Long khachHangId, int limitHoatDong) throws SQLException {
        if (khachHangId == null || khachHangId <= 0) {
            return null;
        }

        KhachHang khachHang = timKhachHangTheoId(khachHangId);
        if (khachHang == null) {
            return null;
        }

        KhachHang360DTO dto = new KhachHang360DTO();
        dto.setKhachHang(khachHang);

        // 1. Danh sách người liên hệ (AC1)
        List<NguoiLienHe> dsNlh = layDsNguoiLienHe(khachHangId);
        dto.setDsNguoiLienHe(dsNlh);

        // 2. Cơ hội bán hàng phân loại đang mở và đã đóng (AC1)
        List<CoHoi> allCoHoi = layDsCoHoi(khachHangId);
        List<CoHoi> openDeals = new ArrayList<>();
        List<CoHoi> closedDeals = new ArrayList<>();
        for (CoHoi ch : allCoHoi) {
            if (ch.isDangMo()) {
                openDeals.add(ch);
            } else {
                closedDeals.add(ch);
            }
        }
        dto.setDsCoHoiDangMo(openDeals);
        dto.setDsCoHoiDaDong(closedDeals);

        // 3. Hợp đồng pháp lý
        List<HopDong> dsHopDong = layDsHopDong(khachHangId);
        dto.setDsHopDong(dsHopDong);

        // 4. Chỉ số tài chính (AC2)
        dto.setTongGiaTriDaKy(tinhTongGiaTriDaKy(khachHangId));
        dto.setTongGiaTriCoHoiDangMo(tinhTongGiaTriCoHoiDangMo(khachHangId));

        // 5. Tệp đính kèm (AC1)
        List<TepDinhKem> dsTep = layDsTepDinhKem(khachHangId);
        dto.setDsTepDinhKem(dsTep);

        // 6. Dòng thời gian hoạt động (AC1 & AC3: tải 500 hoạt động dưới 1.5s)
        int tongSoHd = demTongSoHoatDong(khachHangId);
        dto.setTongSoHoatDong(tongSoHd);

        int maxLimit = limitHoatDong > 0 ? limitHoatDong : 500;
        List<HoatDong> dsHd = layDsHoatDong(khachHangId, maxLimit, 0);
        dto.setDsHoatDong(dsHd);

        return dto;
    }

    private KhachHang mapResultSetToKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setId(rs.getLong("id"));
        kh.setMaKhachHang(rs.getString("ma_khach_hang"));
        kh.setTenCongTy(rs.getString("ten_cong_ty"));
        kh.setTenChuanHoa(rs.getString("ten_chuan_hoa"));
        kh.setMaSoThue(rs.getString("ma_so_thue"));

        long nnId = rs.getLong("nganh_nghe_id");
        if (!rs.wasNull()) kh.setNganhNgheId(nnId);
        kh.setTenNganhNghe(rs.getString("ten_nganh_nghe"));

        long qmId = rs.getLong("quy_mo_id");
        if (!rs.wasNull()) kh.setQuyMoId(qmId);
        kh.setTenQuyMo(rs.getString("ten_quy_mo"));

        kh.setWebsite(rs.getString("website"));
        kh.setWebsiteChuanHoa(rs.getString("website_chuan_hoa"));
        kh.setDiaChi(rs.getString("dia_chi"));

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) kh.setKhuVucId(kvId);
        kh.setTenKhuVuc(rs.getString("ten_khu_vuc"));

        long nshId = rs.getLong("nguoi_so_huu_id");
        if (!rs.wasNull()) kh.setNguoiSoHuuId(nshId);
        kh.setTenNguoiSoHuu(rs.getString("ten_nguoi_so_huu"));

        long nkdId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) kh.setNhomKinhDoanhId(nkdId);
        kh.setTenNhomKinhDoanh(rs.getString("ten_nhom_kinh_doanh"));

        kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));

        long ctmId = rs.getLong("cong_ty_me_id");
        if (!rs.wasNull()) kh.setCongTyMeId(ctmId);
        kh.setTenCongTyMe(rs.getString("ten_cong_ty_me"));

        kh.setTrangThai(rs.getString("trang_thai"));
        kh.setCoRuiRo(rs.getBoolean("co_rui_ro"));
        kh.setRuiRoCapNhatLuc(rs.getTimestamp("rui_ro_cap_nhat_luc"));
        kh.setLanTuongTacCuoi(rs.getTimestamp("lan_tuong_tac_cuoi"));

        long gopId = rs.getLong("gop_vao_khach_hang_id");
        if (!rs.wasNull()) kh.setGopVaoKhachHangId(gopId);

        kh.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));
        Date dTao = rs.getDate("ngay_tao");
        if (dTao != null) kh.setNgayTao(dTao.toLocalDate());
        kh.setCreatedAt(rs.getTimestamp("created_at"));
        kh.setUpdatedAt(rs.getTimestamp("updated_at"));

        return kh;
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

    private HoatDong mapResultSetToHoatDong(ResultSet rs) throws SQLException {
        HoatDong hd = new HoatDong();
        hd.setId(rs.getLong("id"));
        hd.setMaHoatDong(rs.getString("ma_hoat_dong"));
        hd.setTieuDe(rs.getString("tieu_de"));
        hd.setLoaiHoatDong(rs.getString("loai_hoat_dong"));
        long lId = rs.getLong("lead_id");
        if (!rs.wasNull()) hd.setLeadId(lId);
        long khId = rs.getLong("khach_hang_id");
        if (!rs.wasNull()) hd.setKhachHangId(khId);
        long nlhId = rs.getLong("nguoi_lien_he_id");
        if (!rs.wasNull()) hd.setNguoiLienHeId(nlhId);
        long chId = rs.getLong("co_hoi_id");
        if (!rs.wasNull()) hd.setCoHoiId(chId);
        hd.setNguoiPhuTrachId(rs.getLong("nguoi_phu_trach_id"));
        long nhomId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) hd.setNhomKinhDoanhId(nhomId);

        hd.setChiPhi(rs.getBigDecimal("chi_phi"));
        hd.setThoiGianBatDau(rs.getTimestamp("thoi_gian_bat_dau"));
        hd.setThoiGianKetThuc(rs.getTimestamp("thoi_gian_ket_thuc"));
        hd.setTrangThai(rs.getString("trang_thai"));
        hd.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));
        hd.setNoiDung(rs.getString("noi_dung"));
        hd.setKetQua(rs.getString("ket_qua"));
        int tl = rs.getInt("thoi_luong_phut");
        if (!rs.wasNull()) hd.setThoiLuongPhut(tl);
        hd.setLaGhiNhanQuaKhu(rs.getBoolean("la_ghi_nhan_qua_khu"));
        hd.setLaGhiNhanNhanh(rs.getBoolean("la_ghi_nhan_nhanh"));
        Date dTao = rs.getDate("ngay_tao");
        if (dTao != null) hd.setNgayTao(dTao.toLocalDate());
        hd.setCreatedAt(rs.getTimestamp("created_at"));
        hd.setUpdatedAt(rs.getTimestamp("updated_at"));

        hd.setTenNguoiThucHien(rs.getString("ten_nguoi_thuc_hien"));
        hd.setTenNguoiLienHe(rs.getString("ten_nguoi_lien_he"));

        return hd;
    }
}
