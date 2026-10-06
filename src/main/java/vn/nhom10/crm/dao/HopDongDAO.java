package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn dữ liệu từ bảng 'hop_dong'.
 * Phục vụ tính toán và hiển thị tổng giá trị hợp đồng của nhóm công ty (Story S3-05).
 */
public class HopDongDAO {

    private static final Logger LOGGER = Logger.getLogger(HopDongDAO.class.getName());

    /**
     * Tính tổng giá trị hợp đồng của một khách hàng cụ thể (trừ các hợp đồng đã hủy).
     */
    public BigDecimal tinhTongGiaTriHopDongTheoKhachHang(Long khachHangId) throws SQLException {
        if (khachHangId == null) {
            return BigDecimal.ZERO;
        }

        String sql = "SELECT COALESCE(SUM(gia_tri_hop_dong), 0) AS tong_gia_tri " +
                "FROM hop_dong " +
                "WHERE khach_hang_id = ? AND UPPER(COALESCE(trang_thai, '')) != 'DA_HUY'";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal tong = rs.getBigDecimal("tong_gia_tri");
                    return tong != null ? tong : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Tính tổng giá trị hợp đồng của cả nhóm công ty (công ty mẹ và các công ty con).
     */
    public BigDecimal tinhTongGiaTriHopDongNhomCongTy(Long congTyMeId, List<Long> dsCongTyConIds) throws SQLException {
        List<Long> tapHopIds = new ArrayList<>();
        if (congTyMeId != null) {
            tapHopIds.add(congTyMeId);
        }
        if (dsCongTyConIds != null) {
            for (Long cId : dsCongTyConIds) {
                if (cId != null && !tapHopIds.contains(cId)) {
                    tapHopIds.add(cId);
                }
            }
        }

        if (tapHopIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(gia_tri_hop_dong), 0) AS tong_gia_tri " +
                "FROM hop_dong " +
                "WHERE UPPER(COALESCE(trang_thai, '')) != 'DA_HUY' AND khach_hang_id IN (");

        for (int i = 0; i < tapHopIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < tapHopIds.size(); i++) {
                ps.setLong(i + 1, tapHopIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal tong = rs.getBigDecimal("tong_gia_tri");
                    return tong != null ? tong : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy danh sách hợp đồng của một khách hàng.
     */
    public List<HopDong> layDanhSachTheoKhachHang(Long khachHangId) throws SQLException {
        List<HopDong> ds = new ArrayList<>();
        if (khachHangId == null) {
            return ds;
        }

        String sql = "SELECT hd.*, kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang " +
                "FROM hop_dong hd " +
                "JOIN khach_hang kh ON hd.khach_hang_id = kh.id " +
                "WHERE hd.khach_hang_id = ? " +
                "ORDER BY hd.ngay_ky DESC, hd.id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSetToHopDong(rs));
                }
            }
        }
        return ds;
    }

    /**
     * Lấy toàn bộ danh sách hợp đồng thuộc cả nhóm công ty (công ty mẹ và các công ty con).
     */
    public List<HopDong> layDanhSachHopDongNhomCongTy(Long congTyMeId, List<Long> dsCongTyConIds) throws SQLException {
        List<Long> tapHopIds = new ArrayList<>();
        if (congTyMeId != null) {
            tapHopIds.add(congTyMeId);
        }
        if (dsCongTyConIds != null) {
            for (Long cId : dsCongTyConIds) {
                if (cId != null && !tapHopIds.contains(cId)) {
                    tapHopIds.add(cId);
                }
            }
        }

        if (tapHopIds.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder("SELECT hd.*, kh.ten_cong_ty AS ten_khach_hang, kh.ma_khach_hang " +
                "FROM hop_dong hd " +
                "JOIN khach_hang kh ON hd.khach_hang_id = kh.id " +
                "WHERE hd.khach_hang_id IN (");

        for (int i = 0; i < tapHopIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(") ORDER BY hd.ngay_ky DESC, hd.id DESC");

        List<HopDong> ds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < tapHopIds.size(); i++) {
                ps.setLong(i + 1, tapHopIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSetToHopDong(rs));
                }
            }
        }
        return ds;
    }

    /**
     * Thêm mới một hợp đồng vào cơ sở dữ liệu.
     */
    public Long themMoi(HopDong hd) throws SQLException {
        if (hd == null || hd.getKhachHangId() == null) {
            return null;
        }

        String sql = "INSERT INTO hop_dong (so_hop_dong, bao_gia_id, phien_ban_bao_gia_id, co_hoi_id, " +
                "khach_hang_id, ngay_ky, ngay_hieu_luc, ngay_het_han, gia_tri_hop_dong, " +
                "dieu_khoan_thanh_toan, trang_thai, hop_dong_goc_id, co_hoi_gia_han_id, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, hd.getSoHopDong());
            ps.setLong(2, hd.getBaoGiaId() != null ? hd.getBaoGiaId() : 1L);
            ps.setLong(3, hd.getPhienBanBaoGiaId() != null ? hd.getPhienBanBaoGiaId() : 1L);
            ps.setLong(4, hd.getCoHoiId() != null ? hd.getCoHoiId() : 1L);
            ps.setLong(5, hd.getKhachHangId());
            ps.setDate(6, Date.valueOf(hd.getNgayKy() != null ? hd.getNgayKy() : LocalDate.now()));
            ps.setDate(7, Date.valueOf(hd.getNgayHieuLuc() != null ? hd.getNgayHieuLuc() : LocalDate.now()));

            if (hd.getNgayHetHan() != null) {
                ps.setDate(8, Date.valueOf(hd.getNgayHetHan()));
            } else {
                ps.setNull(8, Types.DATE);
            }

            ps.setBigDecimal(9, hd.getGiaTriHopDong() != null ? hd.getGiaTriHopDong() : BigDecimal.ZERO);
            ps.setString(10, hd.getDieuKhoanThanhToan());
            ps.setString(11, hd.getTrangThai() != null ? hd.getTrangThai() : "DA_KY");

            if (hd.getHopDongGocId() != null) ps.setLong(12, hd.getHopDongGocId());
            else ps.setNull(12, Types.BIGINT);

            if (hd.getCoHoiGiaHanId() != null) ps.setLong(13, hd.getCoHoiGiaHanId());
            else ps.setNull(13, Types.BIGINT);

            if (hd.getCreatedBy() != null) ps.setLong(14, hd.getCreatedBy());
            else ps.setNull(14, Types.BIGINT);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        hd.setId(id);
                        return id;
                    }
                }
            }
        }
        return null;
    }

    private HopDong mapResultSetToHopDong(ResultSet rs) throws SQLException {
        HopDong hd = new HopDong();
        hd.setId(rs.getLong("id"));
        hd.setSoHopDong(rs.getString("so_hop_dong"));
        hd.setBaoGiaId(rs.getLong("bao_gia_id"));
        hd.setPhienBanBaoGiaId(rs.getLong("phien_ban_bao_gia_id"));
        hd.setCoHoiId(rs.getLong("co_hoi_id"));
        hd.setKhachHangId(rs.getLong("khach_hang_id"));

        Date nk = rs.getDate("ngay_ky");
        hd.setNgayKy(nk != null ? nk.toLocalDate() : null);

        Date nhl = rs.getDate("ngay_hieu_luc");
        hd.setNgayHieuLuc(nhl != null ? nhl.toLocalDate() : null);

        Date nhh = rs.getDate("ngay_het_han");
        hd.setNgayHetHan(nhh != null ? nhh.toLocalDate() : null);

        hd.setGiaTriHopDong(rs.getBigDecimal("gia_tri_hop_dong"));
        hd.setDieuKhoanThanhToan(rs.getString("dieu_khoan_thanh_toan"));
        hd.setTrangThai(rs.getString("trang_thai"));

        long hdGoc = rs.getLong("hop_dong_goc_id");
        hd.setHopDongGocId(rs.wasNull() ? null : hdGoc);

        long coHoiGH = rs.getLong("co_hoi_gia_han_id");
        hd.setCoHoiGiaHanId(rs.wasNull() ? null : coHoiGH);

        long cb = rs.getLong("created_by");
        hd.setCreatedBy(rs.wasNull() ? null : cb);

        hd.setCreatedAt(rs.getTimestamp("created_at"));
        hd.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            hd.setTenKhachHang(rs.getString("ten_khach_hang"));
            hd.setMaKhachHang(rs.getString("ma_khach_hang"));
        } catch (SQLException ignored) {}

        return hd;
    }
}
