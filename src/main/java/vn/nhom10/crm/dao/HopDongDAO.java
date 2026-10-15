package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BaoGiaHopLeDTO;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn dữ liệu từ bảng 'hop_dong'.
 * Phục vụ tính toán và hiển thị tổng giá trị hợp đồng của nhóm công ty (Story S3-05).
 */
public class HopDongDAO {

    private static final Logger LOGGER = Logger.getLogger(HopDongDAO.class.getName());
    private static final AtomicLong TEST_BG_COUNTER = new AtomicLong(100000L);

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
            ps.setLong(2, hd.getBaoGiaId() != null ? hd.getBaoGiaId() : TEST_BG_COUNTER.incrementAndGet());
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

    /**
     * Lấy danh sách báo giá hợp lệ của khách hàng để tạo hợp đồng.
     * Chỉ lấy các báo giá chưa từng được gán cho hợp đồng nào, trạng thái hợp lệ.
     */
    public List<BaoGiaHopLeDTO> layDanhSachBaoGiaHopLe(Long khachHangId) throws SQLException {
        List<BaoGiaHopLeDTO> ds = new ArrayList<>();
        if (khachHangId == null) {
            return ds;
        }

        String sql = "SELECT bg.id, bg.ma_bao_gia, bg.tieu_de, " +
                "COALESCE(bg.khach_hang_id, ch.khach_hang_id) AS khach_hang_id, " +
                "bg.co_hoi_id, ch.ten_co_hoi, " +
                "COALESCE(bg.tong_sau_chiet_khau, bg.tong_tien, 0) AS gia_tri_bao_gia, " +
                "bg.trang_thai, bg.dieu_khoan_thanh_toan, bg.nguoi_phu_trach_id, bg.nhom_kinh_doanh_id, " +
                "pb.id AS phien_ban_id " +
                "FROM bao_gia bg " +
                "LEFT JOIN co_hoi ch ON bg.co_hoi_id = ch.id " +
                "LEFT JOIN ( " +
                "    SELECT p1.id, p1.bao_gia_id, p1.so_phien_ban " +
                "    FROM bao_gia_phien_ban p1 " +
                "    INNER JOIN ( " +
                "        SELECT bao_gia_id, MAX(so_phien_ban) AS max_ver " +
                "        FROM bao_gia_phien_ban " +
                "        GROUP BY bao_gia_id " +
                "    ) p2 ON p1.bao_gia_id = p2.bao_gia_id AND p1.so_phien_ban = p2.max_ver " +
                ") pb ON pb.bao_gia_id = bg.id " +
                "WHERE (bg.khach_hang_id = ? OR (bg.khach_hang_id IS NULL AND ch.khach_hang_id = ?)) " +
                "  AND bg.id NOT IN ( " +
                "      SELECT hd.bao_gia_id FROM hop_dong hd WHERE hd.bao_gia_id IS NOT NULL " +
                "  ) " +
                "  AND UPPER(COALESCE(bg.trang_thai, '')) NOT IN ('TU_CHOI', 'DA_HUY', 'HET_HAN') " +
                "ORDER BY bg.id DESC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            ps.setLong(2, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BaoGiaHopLeDTO dto = new BaoGiaHopLeDTO();
                    dto.setId(rs.getLong("id"));
                    dto.setMaBaoGia(rs.getString("ma_bao_gia"));
                    dto.setTieuDe(rs.getString("tieu_de"));
                    dto.setKhachHangId(rs.getLong("khach_hang_id"));

                    long chId = rs.getLong("co_hoi_id");
                    dto.setCoHoiId(rs.wasNull() ? null : chId);
                    dto.setTenCoHoi(rs.getString("ten_co_hoi"));

                    long pbId = rs.getLong("phien_ban_id");
                    dto.setPhienBanId(rs.wasNull() ? null : pbId);

                    dto.setGiaTriBaoGia(rs.getBigDecimal("gia_tri_bao_gia"));
                    dto.setTrangThai(rs.getString("trang_thai"));
                    dto.setDieuKhoanThanhToan(rs.getString("dieu_khoan_thanh_toan"));

                    long npt = rs.getLong("nguoi_phu_trach_id");
                    dto.setNguoiPhuTrachId(rs.wasNull() ? null : npt);

                    long nkd = rs.getLong("nhom_kinh_doanh_id");
                    dto.setNhomKinhDoanhId(rs.wasNull() ? null : nkd);

                    ds.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể truy vấn danh sách báo giá hợp lệ: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Tìm thông tin báo giá theo ID.
     */
    public BaoGiaHopLeDTO timBaoGiaTheoId(Long baoGiaId) throws SQLException {
        if (baoGiaId == null) return null;
        String sql = "SELECT bg.id, bg.ma_bao_gia, bg.tieu_de, " +
                "COALESCE(bg.khach_hang_id, ch.khach_hang_id) AS khach_hang_id, " +
                "bg.co_hoi_id, ch.ten_co_hoi, " +
                "COALESCE(bg.tong_sau_chiet_khau, bg.tong_tien, 0) AS gia_tri_bao_gia, " +
                "bg.trang_thai, bg.dieu_khoan_thanh_toan, bg.nguoi_phu_trach_id, bg.nhom_kinh_doanh_id, " +
                "pb.id AS phien_ban_id " +
                "FROM bao_gia bg " +
                "LEFT JOIN co_hoi ch ON bg.co_hoi_id = ch.id " +
                "LEFT JOIN ( " +
                "    SELECT p1.id, p1.bao_gia_id, p1.so_phien_ban " +
                "    FROM bao_gia_phien_ban p1 " +
                "    INNER JOIN ( " +
                "        SELECT bao_gia_id, MAX(so_phien_ban) AS max_ver " +
                "        FROM bao_gia_phien_ban " +
                "        GROUP BY bao_gia_id " +
                "    ) p2 ON p1.bao_gia_id = p2.bao_gia_id AND p1.so_phien_ban = p2.max_ver " +
                ") pb ON pb.bao_gia_id = bg.id " +
                "WHERE bg.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, baoGiaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BaoGiaHopLeDTO dto = new BaoGiaHopLeDTO();
                    dto.setId(rs.getLong("id"));
                    dto.setMaBaoGia(rs.getString("ma_bao_gia"));
                    dto.setTieuDe(rs.getString("tieu_de"));
                    dto.setKhachHangId(rs.getLong("khach_hang_id"));

                    long chId = rs.getLong("co_hoi_id");
                    dto.setCoHoiId(rs.wasNull() ? null : chId);
                    dto.setTenCoHoi(rs.getString("ten_co_hoi"));

                    long pbId = rs.getLong("phien_ban_id");
                    dto.setPhienBanId(rs.wasNull() ? null : pbId);

                    dto.setGiaTriBaoGia(rs.getBigDecimal("gia_tri_bao_gia"));
                    dto.setTrangThai(rs.getString("trang_thai"));
                    dto.setDieuKhoanThanhToan(rs.getString("dieu_khoan_thanh_toan"));

                    long npt = rs.getLong("nguoi_phu_trach_id");
                    dto.setNguoiPhuTrachId(rs.wasNull() ? null : npt);

                    long nkd = rs.getLong("nhom_kinh_doanh_id");
                    dto.setNhomKinhDoanhId(rs.wasNull() ? null : nkd);

                    return dto;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể tìm báo giá ID " + baoGiaId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Kiểm tra xem báo giá đã được dùng để tạo hợp đồng nào hay chưa (uk_hd_bg).
     */
    public boolean kiemTraBaoGiaDaSuDung(Long baoGiaId) throws SQLException {
        if (baoGiaId == null) return false;
        String sql = "SELECT COUNT(*) FROM hop_dong WHERE bao_gia_id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, baoGiaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi kiểm tra báo giá đã sử dụng: " + e.getMessage());
        }
        return false;
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
