package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.util.DatabaseConnection;

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
 * Đảm nhiệm quản lý quan hệ công ty mẹ và công ty con (Story S3-05).
 * Đảm nhiệm quản lý cờ rủi ro rời bỏ và danh sách khách hàng rủi ro (Story S3-08).
 */
public class KhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHangDAO.class.getName());

    /**
     * Tìm khách hàng theo ID kèm tên công ty mẹ và tên người sở hữu.
     */
    public KhachHang timTheoId(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String sql = "SELECT kh.*, " +
                "me.ten_cong_ty AS ten_cong_ty_me, me.ma_khach_hang AS ma_cong_ty_me, " +
                "nd.ho_ten AS ten_nguoi_so_huu, " +
                "nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                "FROM khach_hang kh " +
                "LEFT JOIN khach_hang me ON kh.cong_ty_me_id = me.id " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "WHERE kh.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm khách hàng theo id [" + id + "]: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Lấy danh sách các công ty con của một công ty mẹ (Story S3-05).
     */
    public List<KhachHang> layDanhSachCongTyCon(Long congTyMeId) throws SQLException {
        List<KhachHang> ds = new ArrayList<>();
        if (congTyMeId == null) {
            return ds;
        }

        String sql = "SELECT kh.*, " +
                "me.ten_cong_ty AS ten_cong_ty_me, me.ma_khach_hang AS ma_cong_ty_me, " +
                "nd.ho_ten AS ten_nguoi_so_huu, " +
                "nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                "FROM khach_hang kh " +
                "LEFT JOIN khach_hang me ON kh.cong_ty_me_id = me.id " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "WHERE kh.cong_ty_me_id = ? " +
                "ORDER BY kh.ten_cong_ty ASC";

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

    /**
     * Gắn hoặc gỡ công ty mẹ cho một khách hàng con (Story S3-05 AC1).
     * @param khachHangConId ID của khách hàng con
     * @param congTyMeId ID của công ty mẹ (hoặc null nếu gỡ bỏ quan hệ)
     * @return true nếu cập nhật thành công
     */
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

    /**
     * Kiểm tra xem việc gán congTyMeMoiId cho khachHangConId có tạo ra vòng lặp chu kỳ mẹ - con hay không.
     * Ví dụ: A là mẹ của B -> B không thể là mẹ của A; A -> B -> C -> C không thể là mẹ của A.
     */
    public boolean kiemTraVongLapCongTyMeCon(Long khachHangConId, Long congTyMeMoiId) throws SQLException {
        if (khachHangConId == null || congTyMeMoiId == null) {
            return false;
        }
        // Trường hợp tự chọn chính mình làm mẹ
        if (khachHangConId.equals(congTyMeMoiId)) {
            return true;
        }

        // Truy vết ngược dòng chuỗi công ty mẹ của congTyMeMoiId
        Set<Long> daDuyet = new HashSet<>();
        Long hienTaiId = congTyMeMoiId;

        while (hienTaiId != null) {
            if (hienTaiId.equals(khachHangConId)) {
                return true; // Phát hiện chu kỳ: congTyMeMoiId có khachHangConId là tổ tiên!
            }
            if (!daDuyet.add(hienTaiId)) {
                // Đã bị vòng lặp sẵn trong DB, dừng để tránh lặp vô tận
                return true;
            }

            // Lấy công ty mẹ của hienTaiId
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

    /**
     * Lấy danh sách khách hàng khả dụng có thể chọn làm công ty mẹ cho khachHangId.
     * Loại trừ chính nó và các công ty con/cháu thuộc cây của nó (để tránh chu kỳ).
     */
    public List<KhachHang> layDanhSachKhachHangKhaDungLamCongTyMe(Long khachHangId) throws SQLException {
        List<KhachHang> tatCa = layTatCa();
        if (khachHangId == null) {
            return tatCa;
        }

        // Tìm tập hợp tất cả các con cháu của khachHangId
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

    /**
     * Lấy danh sách khách hàng khả dụng có thể gắn làm công ty con cho congTyMeId.
     * Loại trừ chính congTyMeId, các khách hàng đã là con của nó, và các khách hàng là tổ tiên của nó.
     */
    public List<KhachHang> layDanhSachKhachHangKhaDungLamCongTyCon(Long congTyMeId) throws SQLException {
        List<KhachHang> tatCa = layTatCa();
        if (congTyMeId == null) {
            return tatCa;
        }

        // Tìm tập hợp tổ tiên của congTyMeId
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
            // Không phải chính nó, không phải tổ tiên của nó, và chưa là con của nó
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

    /**
     * Lấy tất cả khách hàng trong hệ thống sắp xếp theo tên công ty.
     */
    public List<KhachHang> layTatCa() {
        List<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT kh.*, " +
                "me.ten_cong_ty AS ten_cong_ty_me, me.ma_khach_hang AS ma_cong_ty_me, " +
                "nd.ho_ten AS ten_nguoi_so_huu, " +
                "nkd.ten_nhom AS ten_nhom_kinh_doanh " +
                "FROM khach_hang kh " +
                "LEFT JOIN khach_hang me ON kh.cong_ty_me_id = me.id " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "ORDER BY kh.ten_cong_ty ASC";

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

    /**
     * Cập nhật cờ rủi ro rời bỏ của khách hàng (Story S3-08).
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
     * Kiểm tra nhanh khách hàng có đang bị gắn cờ rủi ro hay không (Story S3-08).
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
     * Lấy danh sách khách hàng có rủi ro rời bỏ (co_rui_ro = 1) (Story S3-08).
     *
     * @param nguoiSoHuuId ID nhân viên kinh doanh (nếu null thì lấy tất cả)
     */
    public List<KhachHang> layDanhSachKhachHangRuiRo(Long nguoiSoHuuId) {
        List<KhachHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT kh.*, ")
                .append("me.ten_cong_ty AS ten_cong_ty_me, me.ma_khach_hang AS ma_cong_ty_me, ")
                .append("nd.ho_ten AS ten_nguoi_so_huu, nkd.ten_nhom AS ten_nhom_kinh_doanh ")
                .append("FROM khach_hang kh ")
                .append("LEFT JOIN khach_hang me ON kh.cong_ty_me_id = me.id ")
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
                    list.add(mapResultSetToKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách khách hàng rủi ro: " + e.getMessage(), e);
        }

        return list;
    }

    /**
     * Thêm mới một khách hàng vào cơ sở dữ liệu (Story S3-05).
     */
    public Long themMoi(KhachHang khachHang) throws SQLException {
        if (khachHang == null) {
            return null;
        }

        String sqlChuan = "INSERT INTO khach_hang (ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, " +
                "nganh_nghe_id, quy_mo_id, website, website_chuan_hoa, dia_chi, khu_vuc_id, " +
                "nguoi_so_huu_id, nhom_kinh_doanh_id, doanh_thu_uoc_tinh, cong_ty_me_id, trang_thai, " +
                "co_rui_ro, mo_ta_chi_tiet, ngay_tao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sqlChuan, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, khachHang.getMaKhachHang());
            ps.setString(2, khachHang.getTenCongTy());
            ps.setString(3, khachHang.getTenChuanHoa());
            ps.setString(4, khachHang.getMaSoThue());

            if (khachHang.getNganhNgheId() != null) ps.setLong(5, khachHang.getNganhNgheId());
            else ps.setNull(5, Types.BIGINT);

            if (khachHang.getQuyMoId() != null) ps.setLong(6, khachHang.getQuyMoId());
            else ps.setNull(6, Types.BIGINT);

            ps.setString(7, khachHang.getWebsite());
            ps.setString(8, khachHang.getWebsiteChuanHoa());
            ps.setString(9, khachHang.getDiaChi());

            if (khachHang.getKhuVucId() != null) ps.setLong(10, khachHang.getKhuVucId());
            else ps.setNull(10, Types.BIGINT);

            ps.setLong(11, khachHang.getNguoiSoHuuId() != null ? khachHang.getNguoiSoHuuId() : 1L);

            if (khachHang.getNhomKinhDoanhId() != null) ps.setLong(12, khachHang.getNhomKinhDoanhId());
            else ps.setNull(12, Types.BIGINT);

            ps.setBigDecimal(13, khachHang.getDoanhThuUocTinh());

            if (khachHang.getCongTyMeId() != null) ps.setLong(14, khachHang.getCongTyMeId());
            else ps.setNull(14, Types.BIGINT);

            ps.setString(15, khachHang.getTrangThai() != null ? khachHang.getTrangThai() : "Tiềm năng");
            ps.setInt(16, khachHang.isCoRuiRo() ? 1 : 0);
            ps.setString(17, khachHang.getMoTaChiTiet());
            ps.setDate(18, Date.valueOf(khachHang.getNgayTao() != null ? khachHang.getNgayTao() : LocalDate.now()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long sinhId = rs.getLong(1);
                        khachHang.setId(sinhId);
                        return sinhId;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Thêm khách hàng (hỗ trợ gọi từ Story S3-08).
     */
    public Long themKhachHang(KhachHang kh) {
        try {
            return themMoi(kh);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm khách hàng: " + e.getMessage(), e);
            return null;
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
            Timestamp ruiRoLuc = rs.getTimestamp("rui_ro_cap_nhat_luc");
            kh.setRuiRoCapNhatLuc(ruiRoLuc);
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
            kh.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException ignored) {}

        // Join columns nếu có
        try {
            kh.setTenCongTyMe(rs.getString("ten_cong_ty_me"));
            kh.setMaCongTyMe(rs.getString("ma_cong_ty_me"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNguoiSoHuu(rs.getString("ten_nguoi_so_huu"));
        } catch (SQLException ignored) {}

        try {
            kh.setTenNhomKinhDoanh(rs.getString("ten_nhom_kinh_doanh"));
        } catch (SQLException ignored) {}

        return kh;
    }

    public KhachHang mapResultSet(ResultSet rs) throws SQLException {
        return mapResultSetToKhachHang(rs);
    }
}
