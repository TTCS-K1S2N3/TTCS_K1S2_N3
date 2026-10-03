package vn.nhom10.crm.dao;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.LoaiSanPhamEnum;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.TrangThaiSanPhamEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn bảng san_pham và kiểm tra ràng buộc với bảng bao_gia_chi_tiet (S2-05).
 */
public class SanPhamDAO {

    private static final Logger LOGGER = Logger.getLogger(SanPhamDAO.class.getName());

    public SanPham timTheoId(int id, boolean coQuyenGiaVon) {
        String sql = "SELECT id, ma_san_pham, ten_san_pham, loai_san_pham, don_vi_tinh, gia_niem_yet, gia_san, " +
                     "gia_von, mo_ta, trang_thai, created_at, updated_at FROM san_pham WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SanPham sp = anhXaSanPham(rs, coQuyenGiaVon);
                    sp.setDaXuatHienTrongBaoGia(kiemTraXuatHienTrongBaoGiaAnToan(id));
                    return sp;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm sản phẩm theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public SanPham timTheoMa(String maSanPham, boolean coQuyenGiaVon) {
        if (maSanPham == null || maSanPham.isBlank()) {
            return null;
        }

        String sql = "SELECT id, ma_san_pham, ten_san_pham, loai_san_pham, don_vi_tinh, gia_niem_yet, gia_san, " +
                     "gia_von, mo_ta, trang_thai, created_at, updated_at FROM san_pham WHERE LOWER(ma_san_pham) = LOWER(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maSanPham.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SanPham sp = anhXaSanPham(rs, coQuyenGiaVon);
                    sp.setDaXuatHienTrongBaoGia(kiemTraXuatHienTrongBaoGiaAnToan(sp.getId()));
                    return sp;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm sản phẩm theo mã=" + maSanPham + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Kiểm tra mã sản phẩm đã tồn tại chưa (loại trừ excludeId khi đang sửa sản phẩm).
     */
    public boolean kiemTraMaTonTai(String maSanPham, Integer excludeId) {
        if (maSanPham == null || maSanPham.isBlank()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM san_pham WHERE LOWER(ma_san_pham) = LOWER(?)");
        if (excludeId != null) {
            sql.append(" AND id <> ?");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, maSanPham.trim());
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã sản phẩm: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * AC: Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh.
     * Kiểm tra xem sản phẩm đã có mặt trong bất kỳ dòng chi tiết báo giá nào chưa.
     */
    public boolean kiemTraXuatHienTrongBaoGia(int sanPhamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bao_gia_chi_tiet WHERE san_pham_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sanPhamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Helper fail-closed: Nếu xảy ra lỗi truy vấn, coi như đã có tham chiếu để bảo vệ dữ liệu.
     */
    public boolean kiemTraXuatHienTrongBaoGiaAnToan(int sanPhamId) {
        try {
            return kiemTraXuatHienTrongBaoGia(sanPhamId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi kiểm tra tham chiếu báo giá cho sản phẩm ID=" + sanPhamId + ": " + e.getMessage(), e);
            return true;
        }
    }

    /**
     * Thêm sản phẩm dịch vụ mới vào danh mục bảng giá niêm yết.
     * AC: Giá vốn chỉ Giám đốc kinh doanh xem và sửa được.
     */
    public int themSanPham(SanPham sp, boolean coQuyenGiaVon) throws SQLException {
        String sql = "INSERT INTO san_pham (ma_san_pham, ten_san_pham, loai_san_pham, don_vi_tinh, " +
                     "gia_niem_yet, gia_san, gia_von, mo_ta, trang_thai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sp.getMaSanPham().trim());
            ps.setString(2, sp.getTenSanPham().trim());
            ps.setString(3, sp.getLoai() != null ? sp.getLoai().getMaLoai() : LoaiSanPhamEnum.SAN_PHAM_MOT_LAN.getMaLoai());
            ps.setString(4, sp.getDonViTinh() != null ? sp.getDonViTinh().trim() : "");
            ps.setBigDecimal(5, sp.getGiaNiemYet());
            ps.setBigDecimal(6, sp.getGiaSan());

            // Chỉ lưu giá vốn nếu người thực hiện có quyền Giám đốc kinh doanh
            if (coQuyenGiaVon && sp.getGiaVon() != null) {
                ps.setBigDecimal(7, sp.getGiaVon());
            } else {
                ps.setNull(7, java.sql.Types.DECIMAL);
            }

            ps.setString(8, sp.getMoTa());
            ps.setString(9, sp.getTrangThai() != null ? sp.getTrangThai().getMaTrangThai() : TrangThaiSanPhamEnum.DANG_KINH_DOANH.getMaTrangThai());

            ps.executeUpdate();
            try (ResultSet rsKey = ps.getGeneratedKeys()) {
                if (rsKey.next()) {
                    int genId = rsKey.getInt(1);
                    sp.setId(genId);
                    return genId;
                }
            }
        }
        return -1;
    }

    /**
     * Cập nhật thông tin sản phẩm dịch vụ.
     * Nếu không có quyền xem/sửa giá vốn thì không được can thiệp vào cột gia_von.
     */
    public boolean capNhatSanPham(SanPham sp, boolean coQuyenGiaVon) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE san_pham SET ma_san_pham = ?, ten_san_pham = ?, loai_san_pham = ?, " +
                "don_vi_tinh = ?, gia_niem_yet = ?, gia_san = ?, mo_ta = ?, trang_thai = ? ");

        if (coQuyenGiaVon) {
            sql.append(", gia_von = ? ");
        }
        sql.append("WHERE id = ?");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, sp.getMaSanPham().trim());
            ps.setString(2, sp.getTenSanPham().trim());
            ps.setString(3, sp.getLoai() != null ? sp.getLoai().getMaLoai() : LoaiSanPhamEnum.SAN_PHAM_MOT_LAN.getMaLoai());
            ps.setString(4, sp.getDonViTinh() != null ? sp.getDonViTinh().trim() : "");
            ps.setBigDecimal(5, sp.getGiaNiemYet());
            ps.setBigDecimal(6, sp.getGiaSan());
            ps.setString(7, sp.getMoTa());
            ps.setString(8, sp.getTrangThai() != null ? sp.getTrangThai().getMaTrangThai() : TrangThaiSanPhamEnum.DANG_KINH_DOANH.getMaTrangThai());

            int idx = 9;
            if (coQuyenGiaVon) {
                if (sp.getGiaVon() != null) {
                    ps.setBigDecimal(idx++, sp.getGiaVon());
                } else {
                    ps.setNull(idx++, java.sql.Types.DECIMAL);
                }
            }
            ps.setInt(idx, sp.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật trạng thái kinh doanh (vd: chuyển sang NGUNG_KINH_DOANH).
     */
    public boolean capNhatTrangThai(int id, TrangThaiSanPhamEnum trangThaiMoi) throws SQLException {
        String sql = "UPDATE san_pham SET trang_thai = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThaiMoi.getMaTrangThai());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa sản phẩm khỏi cơ sở dữ liệu (chỉ được gọi khi sản phẩm chưa có trong báo giá).
     */
    public boolean xoaSanPham(int id) throws SQLException {
        String sql = "DELETE FROM san_pham WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đếm tổng số lượng bản ghi theo bộ lọc để phân trang.
     */
    public int demSoLuong(String tuKhoa, LoaiSanPhamEnum loai, TrangThaiSanPhamEnum trangThai) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM san_pham WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(ma_san_pham) LIKE ? OR LOWER(ten_san_pham) LIKE ?) ");
            String kw = "%" + tuKhoa.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (loai != null) {
            sql.append("AND loai_san_pham = ? ");
            params.add(loai.getMaLoai());
        }

        if (trangThai != null) {
            sql.append("AND trang_thai = ? ");
            params.add(trangThai.getMaTrangThai());
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
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng sản phẩm: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Tìm kiếm và phân trang danh mục sản phẩm.
     * AC: Giá vốn chỉ Giám đốc kinh doanh xem được -> Nếu coQuyenGiaVon = false thì giaVon = null.
     */
    public List<SanPham> timKiemVaPhanTrang(String tuKhoa, LoaiSanPhamEnum loai, TrangThaiSanPhamEnum trangThai,
                                            int limit, int offset, boolean coQuyenGiaVon) {
        List<SanPham> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, ma_san_pham, ten_san_pham, loai_san_pham, don_vi_tinh, " +
                "gia_niem_yet, gia_san, gia_von, mo_ta, trang_thai, created_at, updated_at FROM san_pham WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(ma_san_pham) LIKE ? OR LOWER(ten_san_pham) LIKE ?) ");
            String kw = "%" + tuKhoa.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (loai != null) {
            sql.append("AND loai_san_pham = ? ");
            params.add(loai.getMaLoai());
        }

        if (trangThai != null) {
            sql.append("AND trang_thai = ? ");
            params.add(trangThai.getMaTrangThai());
        }

        sql.append("ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = anhXaSanPham(rs, coQuyenGiaVon);
                    sp.setDaXuatHienTrongBaoGia(kiemTraXuatHienTrongBaoGiaAnToan(sp.getId()));
                    danhSach.add(sp);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
        return danhSach;
    }

    private SanPham anhXaSanPham(ResultSet rs, boolean coQuyenGiaVon) throws SQLException {
        SanPham sp = new SanPham();
        sp.setId(rs.getInt("id"));
        sp.setMaSanPham(rs.getString("ma_san_pham"));
        sp.setTenSanPham(rs.getString("ten_san_pham"));

        String loaiStr = null;
        try {
            loaiStr = rs.getString("loai_san_pham");
        } catch (SQLException e) {
            try {
                loaiStr = rs.getString("loai");
            } catch (SQLException ignored) {}
        }
        sp.setLoai(LoaiSanPhamEnum.tuMa(loaiStr));
        sp.setDonViTinh(rs.getString("don_vi_tinh"));
        sp.setGiaNiemYet(rs.getBigDecimal("gia_niem_yet"));
        sp.setGiaSan(rs.getBigDecimal("gia_san"));

        // Ràng buộc bảo mật phía server: Nếu không có quyền Director/Admin thì không trả ra giá vốn
        if (coQuyenGiaVon) {
            sp.setGiaVon(rs.getBigDecimal("gia_von"));
        } else {
            sp.setGiaVon(null);
        }

        sp.setMoTa(rs.getString("mo_ta"));
        sp.setTrangThai(TrangThaiSanPhamEnum.tuMa(rs.getString("trang_thai")));
        sp.setCreatedAt(rs.getTimestamp("created_at"));
        sp.setUpdatedAt(rs.getTimestamp("updated_at"));
        return sp;
    }
}
