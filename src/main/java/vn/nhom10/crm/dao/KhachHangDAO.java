package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý bảng `khach_hang` (Story S3-08).
 * Quản lý trạng thái cờ rủi ro rời bỏ `co_rui_ro` và thông tin khách hàng.
 */
public class KhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHangDAO.class.getName());

    /**
     * Tìm khách hàng theo ID.
     */
    public KhachHang timTheoId(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
                "kh.nganh_nghe_id, kh.quy_mo_id, kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
                "kh.khu_vuc_id, kh.nguoi_so_huu_id, kh.nhom_kinh_doanh_id, kh.doanh_thu_uoc_tinh, " +
                "kh.cong_ty_me_id, kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, " +
                "kh.lan_tuong_tac_cuoi, kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, " +
                "kh.ngay_tao, kh.created_at, kh.updated_at, " +
                "nd.ho_ten AS ten_nguoi_so_huu, nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                "FROM khach_hang kh " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "WHERE kh.id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm khách hàng theo id [" + id + "]: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Cập nhật cờ rủi ro rời bỏ của khách hàng.
     *
     * @param khachHangId ID khách hàng
     * @param coRuiRo     true nếu gắn cờ rủi ro, false nếu gỡ cờ
     * @return true nếu cập nhật thành công
     */
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

    /**
     * Kiểm tra nhanh khách hàng có đang bị gắn cờ rủi ro hay không.
     */
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

    /**
     * Lấy danh sách khách hàng có rủi ro rời bỏ (co_rui_ro = 1).
     *
     * @param nguoiSoHuuId ID nhân viên kinh doanh (nếu null thì lấy tất cả)
     */
    public List<KhachHang> layDanhSachKhachHangRuiRo(Long nguoiSoHuuId) {
        List<KhachHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, ")
                .append("kh.nganh_nghe_id, kh.quy_mo_id, kh.website, kh.website_chuan_hoa, kh.dia_chi, ")
                .append("kh.khu_vuc_id, kh.nguoi_so_huu_id, kh.nhom_kinh_doanh_id, kh.doanh_thu_uoc_tinh, ")
                .append("kh.cong_ty_me_id, kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, ")
                .append("kh.lan_tuong_tac_cuoi, kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, ")
                .append("kh.ngay_tao, kh.created_at, kh.updated_at, ")
                .append("nd.ho_ten AS ten_nguoi_so_huu, nkd.ten_nhom AS ten_nhom_kinh_doanh ")
                .append("FROM khach_hang kh ")
                .append("LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id ")
                .append("LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id ")
                .append("WHERE kh.co_rui_ro = 1 ");

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
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khách hàng rủi ro: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Thêm mới một khách hàng vào cơ sở dữ liệu.
     */
    public Long themKhachHang(KhachHang kh) {
        if (kh == null || kh.getTenCongTy() == null || kh.getNguoiSoHuuId() == null) {
            return null;
        }

        String sql = "INSERT INTO khach_hang (ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, " +
                "nganh_nghe_id, quy_mo_id, website, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                "doanh_thu_uoc_tinh, trang_thai, co_rui_ro, mo_ta_chi_tiet, ngay_tao, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kh.getMaKhachHang());
            ps.setString(2, kh.getTenCongTy());
            ps.setString(3, kh.getTenChuanHoa());
            ps.setString(4, kh.getMaSoThue());

            if (kh.getNganhNgheId() != null) ps.setLong(5, kh.getNganhNgheId()); else ps.setNull(5, Types.BIGINT);
            if (kh.getQuyMoId() != null) ps.setLong(6, kh.getQuyMoId()); else ps.setNull(6, Types.BIGINT);
            ps.setString(7, kh.getWebsite());
            ps.setString(8, kh.getDiaChi());
            if (kh.getKhuVucId() != null) ps.setLong(9, kh.getKhuVucId()); else ps.setNull(9, Types.BIGINT);
            ps.setLong(10, kh.getNguoiSoHuuId());
            if (kh.getNhomKinhDoanhId() != null) ps.setLong(11, kh.getNhomKinhDoanhId()); else ps.setNull(11, Types.BIGINT);
            ps.setBigDecimal(12, kh.getDoanhThuUocTinh());
            ps.setString(13, kh.getTrangThai() != null ? kh.getTrangThai() : "TIEM_NANG");
            ps.setInt(14, kh.isCoRuiRo() ? 1 : 0);
            ps.setString(15, kh.getMoTaChiTiet());

            LocalDate ngayTao = kh.getNgayTao() != null ? kh.getNgayTao() : LocalDate.now();
            ps.setDate(16, Date.valueOf(ngayTao));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long genId = rs.getLong(1);
                        kh.setId(genId);
                        return genId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm khách hàng: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Lấy toàn bộ danh sách khách hàng.
     */
    public List<KhachHang> layTatCa() {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
                "kh.nganh_nghe_id, kh.quy_mo_id, kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
                "kh.khu_vuc_id, kh.nguoi_so_huu_id, kh.nhom_kinh_doanh_id, kh.doanh_thu_uoc_tinh, " +
                "kh.cong_ty_me_id, kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, " +
                "kh.lan_tuong_tac_cuoi, kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, " +
                "kh.ngay_tao, kh.created_at, kh.updated_at, " +
                "nd.ho_ten AS ten_nguoi_so_huu, nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                "FROM khach_hang kh " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "ORDER BY kh.id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khách hàng: " + e.getMessage(), e);
        }

        return list;
    }

    private KhachHang mapResultSet(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setId(rs.getLong("id"));
        kh.setMaKhachHang(rs.getString("ma_khach_hang"));
        kh.setTenCongTy(rs.getString("ten_cong_ty"));
        kh.setTenChuanHoa(rs.getString("ten_chuan_hoa"));
        kh.setMaSoThue(rs.getString("ma_so_thue"));

        long nnId = rs.getLong("nganh_nghe_id");
        if (!rs.wasNull()) kh.setNganhNgheId(nnId);

        long qmId = rs.getLong("quy_mo_id");
        if (!rs.wasNull()) kh.setQuyMoId(qmId);

        kh.setWebsite(rs.getString("website"));
        kh.setWebsiteChuanHoa(rs.getString("website_chuan_hoa"));
        kh.setDiaChi(rs.getString("dia_chi"));

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) kh.setKhuVucId(kvId);

        kh.setNguoiSoHuuId(rs.getLong("nguoi_so_huu_id"));

        long nkdId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) kh.setNhomKinhDoanhId(nkdId);

        kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));

        long ctMeId = rs.getLong("cong_ty_me_id");
        if (!rs.wasNull()) kh.setCongTyMeId(ctMeId);

        kh.setTrangThai(rs.getString("trang_thai"));
        kh.setCoRuiRo(rs.getInt("co_rui_ro") == 1);

        Timestamp ruiRoLuc = rs.getTimestamp("rui_ro_cap_nhat_luc");
        if (ruiRoLuc != null) kh.setRuiRoCapNhatLuc(ruiRoLuc.toLocalDateTime());

        Timestamp ttCuoi = rs.getTimestamp("lan_tuong_tac_cuoi");
        if (ttCuoi != null) kh.setLanTuongTacCuoi(ttCuoi.toLocalDateTime());

        long gopId = rs.getLong("gop_vao_khach_hang_id");
        if (!rs.wasNull()) kh.setGopVaoKhachHangId(gopId);

        kh.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));

        Date ngayTao = rs.getDate("ngay_tao");
        if (ngayTao != null) kh.setNgayTao(ngayTao.toLocalDate());

        Timestamp crAt = rs.getTimestamp("created_at");
        if (crAt != null) kh.setCreatedAt(crAt.toLocalDateTime());

        Timestamp upAt = rs.getTimestamp("updated_at");
        if (upAt != null) kh.setUpdatedAt(upAt.toLocalDateTime());

        try {
            kh.setTenNguoiSoHuu(rs.getString("ten_nguoi_so_huu"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNhomKinhDoanh(rs.getString("ten_nhom_kinh_doanh"));
        } catch (SQLException ignored) {}

        return kh;
    }
}
