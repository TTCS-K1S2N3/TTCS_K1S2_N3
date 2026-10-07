package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn và nhập hàng loạt khách hàng từ Excel vào database (Story S3-06).
 * Đảm bảo:
 * - Đối chiếu trùng theo Mã số thuế, Mã KH, Tên công ty
 * - Tuân thủ Data Scope (Cá nhân, Nhóm, Toàn bộ)
 * - Quản lý Transaction an toàn và rollback khi lỗi
 */
public class KhachHangImportDAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHangImportDAO.class.getName());

    /**
     * Lấy danh sách khách hàng phục vụ đối chiếu trùng lặp theo Data Scope của người dùng.
     */
    public List<KhachHang> layDanhSachDoiChieuTrung(Long userId, Long nhomId, PhamViDuLieu phamVi) throws SQLException {
        List<KhachHang> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, ma_khach_hang, ten_cong_ty, ma_so_thue, website, dia_chi, " +
                "       doanh_thu_uoc_tinh, trang_thai, nguoi_so_huu_id, nhom_kinh_doanh_id " +
                "FROM khach_hang WHERE 1=1 "
        );

        if (phamVi == PhamViDuLieu.CA_NHAN) {
            sql.append(" AND nguoi_so_huu_id = ?");
        } else if (phamVi == PhamViDuLieu.NHOM) {
            sql.append(" AND (nhom_kinh_doanh_id = ? OR nguoi_so_huu_id = ?)");
        }
        // PhamViDuLieu.TOAN_BO: không giới hạn điều kiện where

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (phamVi == PhamViDuLieu.CA_NHAN) {
                ps.setLong(1, userId != null ? userId : -1L);
            } else if (phamVi == PhamViDuLieu.NHOM) {
                ps.setLong(1, nhomId != null ? nhomId : -1L);
                ps.setLong(2, userId != null ? userId : -1L);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setId(rs.getLong("id"));
                    kh.setMaKhachHang(rs.getString("ma_khach_hang"));
                    kh.setTenCongTy(rs.getString("ten_cong_ty"));
                    kh.setMaSoThue(rs.getString("ma_so_thue"));
                    kh.setWebsite(rs.getString("website"));
                    kh.setDiaChi(rs.getString("dia_chi"));
                    kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));
                    kh.setTrangThai(rs.getString("trang_thai"));
                    kh.setNguoiSoHuuId(rs.getLong("nguoi_so_huu_id"));
                    long nhom = rs.getLong("nhom_kinh_doanh_id");
                    if (!rs.wasNull()) {
                        kh.setNhomKinhDoanhId(nhom);
                    }
                    danhSach.add(kh);
                }
            }
        }

        return danhSach;
    }

    /**
     * Tìm khách hàng theo ID.
     */
    public KhachHang timTheoId(Connection conn, Long id) throws SQLException {
        if (id == null) return null;
        String sql = "SELECT * FROM khach_hang WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKhachHang(rs);
                }
            }
        }
        return null;
    }

    public KhachHang timTheoId(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return timTheoId(conn, id);
        }
    }

    /**
     * Thêm mới một khách hàng vào cơ sở dữ liệu trong cùng một transaction.
     */
    public Long themKhachHang(Connection conn, KhachHang kh) throws SQLException {
        if (kh == null) return null;

        String sql = "INSERT INTO khach_hang (" +
                "ma_khach_hang, ten_cong_ty, ma_so_thue, website, dia_chi, " +
                "doanh_thu_uoc_tinh, trang_thai, mo_ta_chi_tiet, nguoi_so_huu_id, nhom_kinh_doanh_id, ngay_tao" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kh.getMaKhachHang());
            ps.setString(2, kh.getTenCongTy());
            if (kh.getMaSoThue() != null && !kh.getMaSoThue().isBlank()) {
                ps.setString(3, kh.getMaSoThue().trim());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, kh.getWebsite());
            ps.setString(5, kh.getDiaChi());
            ps.setBigDecimal(6, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);
            ps.setString(7, kh.getTrangThai() != null && !kh.getTrangThai().isBlank() ? kh.getTrangThai() : "TIEM_NANG");
            ps.setString(8, kh.getMoTaChiTiet());
            ps.setLong(9, kh.getNguoiSoHuuId());

            if (kh.getNhomKinhDoanhId() != null) {
                ps.setLong(10, kh.getNhomKinhDoanhId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            ps.setDate(11, Date.valueOf(kh.getNgayTao() != null ? kh.getNgayTao() : LocalDate.now()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        kh.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        }
        return null;
    }

    public Long themKhachHang(KhachHang kh) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return themKhachHang(conn, kh);
        }
    }

    /**
     * Cập nhật thông tin khách hàng bị trùng theo lựa chọn của người dùng trong transaction.
     */
    public boolean capNhatKhachHang(Connection conn, KhachHang kh) throws SQLException {
        if (kh == null || kh.getId() == null) return false;

        StringBuilder sql = new StringBuilder("UPDATE khach_hang SET ");
        List<Object> params = new ArrayList<>();

        sql.append("ten_cong_ty = ?, ");
        params.add(kh.getTenCongTy());

        if (kh.getMaSoThue() != null && !kh.getMaSoThue().isBlank()) {
            sql.append("ma_so_thue = ?, ");
            params.add(kh.getMaSoThue().trim());
        }

        if (kh.getWebsite() != null && !kh.getWebsite().isBlank()) {
            sql.append("website = ?, ");
            params.add(kh.getWebsite().trim());
        }

        if (kh.getDiaChi() != null && !kh.getDiaChi().isBlank()) {
            sql.append("dia_chi = ?, ");
            params.add(kh.getDiaChi().trim());
        }

        if (kh.getDoanhThuUocTinh() != null) {
            sql.append("doanh_thu_uoc_tinh = ?, ");
            params.add(kh.getDoanhThuUocTinh());
        }

        if (kh.getTrangThai() != null && !kh.getTrangThai().isBlank()) {
            sql.append("trang_thai = ?, ");
            params.add(kh.getTrangThai().trim());
        }

        if (kh.getMoTaChiTiet() != null && !kh.getMoTaChiTiet().isBlank()) {
            sql.append("mo_ta_chi_tiet = ?, ");
            params.add(kh.getMoTaChiTiet().trim());
        }

        sql.append("updated_at = CURRENT_TIMESTAMP WHERE id = ?");
        params.add(kh.getId());

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    ps.setString(i + 1, (String) p);
                } else if (p instanceof BigDecimal) {
                    ps.setBigDecimal(i + 1, (BigDecimal) p);
                } else if (p instanceof Long) {
                    ps.setLong(i + 1, (Long) p);
                } else {
                    ps.setObject(i + 1, p);
                }
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatKhachHang(KhachHang kh) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return capNhatKhachHang(conn, kh);
        }
    }

    /**
     * Kiểm tra xem mã khách hàng đã tồn tại hay chưa.
     */
    public boolean kiemTraTonTaiMaKhachHang(String maKhachHang, Long excludeId) throws SQLException {
        if (maKhachHang == null || maKhachHang.isBlank()) return false;
        String sql = "SELECT 1 FROM khach_hang WHERE LOWER(ma_khach_hang) = LOWER(?)";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        sql += " LIMIT 1";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Map ResultSet sang entity KhachHang.
     */
    private KhachHang mapResultSetToKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setId(rs.getLong("id"));
        kh.setMaKhachHang(rs.getString("ma_khach_hang"));
        kh.setTenCongTy(rs.getString("ten_cong_ty"));
        kh.setTenChuanHoa(rs.getString("ten_chuan_hoa"));
        kh.setMaSoThue(rs.getString("ma_so_thue"));
        kh.setWebsite(rs.getString("website"));
        kh.setDiaChi(rs.getString("dia_chi"));
        kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));
        kh.setTrangThai(rs.getString("trang_thai"));
        kh.setNguoiSoHuuId(rs.getLong("nguoi_so_huu_id"));
        long nhom = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) {
            kh.setNhomKinhDoanhId(nhom);
        }
        kh.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));
        Date d = rs.getDate("ngay_tao");
        if (d != null) {
            kh.setNgayTao(d.toLocalDate());
        }
        return kh;
    }
}
