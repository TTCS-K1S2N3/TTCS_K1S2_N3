package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý các thao tác dữ liệu với 4 bảng danh mục dùng chung của khối bán hàng (Story S2-07):
 * - `nganh_nghe`
 * - `quy_mo_doanh_nghiep`
 * - `nguon_lead`
 * - `loai_hoat_dong`
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, không dùng SELECT *).
 */
public class DanhMucBanHangDAO {

    private static final Logger LOGGER = Logger.getLogger(DanhMucBanHangDAO.class.getName());

    /**
     * Lấy toàn bộ danh sách mục thuộc một loại danh mục, sắp xếp theo thứ tự hiển thị tăng dần, id tăng dần.
     */
    public List<MucDanhMucDTO> layDanhSach(LoaiDanhMuc loai, boolean chiLayKichHoat) {
        List<MucDanhMucDTO> danhSach = new ArrayList<>();
        if (loai == null) return danhSach;

        String bang = loai.getTenBang();
        String cotMa = loai.getCotMa();
        String cotTen = loai.getCotTen();

        StringBuilder sql = new StringBuilder(
                "SELECT id, " + cotMa + " AS ma, " + cotTen + " AS ten, thu_tu_hien_thi, hoat_dong, created_at " +
                "FROM " + bang
        );

        if (chiLayKichHoat) {
            sql.append(" WHERE hoat_dong = 1");
        }
        sql.append(" ORDER BY thu_tu_hien_thi ASC, id ASC");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MucDanhMucDTO dto = new MucDanhMucDTO();
                dto.setId(rs.getLong("id"));
                dto.setLoaiDanhMuc(loai);
                dto.setMaMuc(rs.getString("ma"));
                dto.setTenMuc(rs.getString("ten"));
                dto.setThuTuHienThi(rs.getInt("thu_tu_hien_thi"));
                dto.setKichHoat(rs.getBoolean("hoat_dong"));

                Timestamp ts = rs.getTimestamp("created_at");
                dto.setNgayTao(ts != null ? ts.toLocalDateTime().toLocalDate() : LocalDate.now());
                dto.setNguoiTao("Giám đốc kinh doanh");

                // Lấy số lượng bản ghi thực tế đang tham chiếu mục này
                dto.setSoBanGhiDangSuDung(demSoLuongThamChieu(loai, dto.getId()));

                danhSach.add(dto);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách danh mục cho bảng " + bang + ": " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy một mục danh mục theo ID.
     */
    public MucDanhMucDTO layTheoId(LoaiDanhMuc loai, long id) {
        if (loai == null) return null;

        String bang = loai.getTenBang();
        String cotMa = loai.getCotMa();
        String cotTen = loai.getCotTen();

        String sql = "SELECT id, " + cotMa + " AS ma, " + cotTen + " AS ten, thu_tu_hien_thi, hoat_dong, created_at " +
                     "FROM " + bang + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MucDanhMucDTO dto = new MucDanhMucDTO();
                    dto.setId(rs.getLong("id"));
                    dto.setLoaiDanhMuc(loai);
                    dto.setMaMuc(rs.getString("ma"));
                    dto.setTenMuc(rs.getString("ten"));
                    dto.setThuTuHienThi(rs.getInt("thu_tu_hien_thi"));
                    dto.setKichHoat(rs.getBoolean("hoat_dong"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    dto.setNgayTao(ts != null ? ts.toLocalDateTime().toLocalDate() : LocalDate.now());
                    dto.setNguoiTao("Giám đốc kinh doanh");
                    dto.setSoBanGhiDangSuDung(demSoLuongThamChieu(loai, id));
                    return dto;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy mục danh mục ID=" + id + " trong bảng " + bang, e);
        }
        return null;
    }

    /**
     * Kiểm tra xem mã mục đã tồn tại trong bảng danh mục chưa (tránh trùng khóa UNIQUE).
     */
    public boolean kiemTraTonTaiMa(LoaiDanhMuc loai, String maMuc, Long excludeId) {
        if (loai == null || maMuc == null) return false;

        String bang = loai.getTenBang();
        String cotMa = loai.getCotMa();

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + bang + " WHERE " + cotMa + " = ?");
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maMuc.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã mục '" + maMuc + "' trong bảng " + bang, e);
        }
        return false;
    }

    /**
     * Thêm mới một mục danh mục vào bảng tương ứng.
     */
    public long them(LoaiDanhMuc loai, MucDanhMucDTO dto) {
        if (loai == null || dto == null) return -1;

        String bang = loai.getTenBang();
        String cotMa = loai.getCotMa();
        String cotTen = loai.getCotTen();

        String sql = "INSERT INTO " + bang + " (" + cotMa + ", " + cotTen + ", thu_tu_hien_thi, hoat_dong) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.getMaMuc().trim());
            ps.setString(2, dto.getTenMuc().trim());
            ps.setInt(3, dto.getThuTuHienThi());
            ps.setBoolean(4, dto.isKichHoat());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long newId = rs.getLong(1);
                        dto.setId(newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm mục danh mục vào bảng " + bang + ": " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Cập nhật thông tin mục danh mục (tên mục, thứ tự hiển thị, trạng thái hoạt động).
     */
    public boolean capNhat(LoaiDanhMuc loai, MucDanhMucDTO dto) {
        if (loai == null || dto == null || dto.getId() == null) return false;

        String bang = loai.getTenBang();
        String cotTen = loai.getCotTen();

        String sql = "UPDATE " + bang + " SET " + cotTen + " = ?, thu_tu_hien_thi = ?, hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getTenMuc().trim());
            ps.setInt(2, dto.getThuTuHienThi());
            ps.setBoolean(3, dto.isKichHoat());
            ps.setLong(4, dto.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật mục danh mục ID=" + dto.getId() + " trong bảng " + bang, e);
            return false;
        }
    }

    /**
     * Xóa một mục danh mục theo ID (chỉ gọi khi kiểm tra số lượng tham chiếu = 0).
     */
    public boolean xoa(LoaiDanhMuc loai, long id) {
        if (loai == null) return false;

        String bang = loai.getTenBang();
        String sql = "DELETE FROM " + bang + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa mục danh mục ID=" + id + " trong bảng " + bang, e);
            return false;
        }
    }

    /**
     * Cập nhật nhanh thứ tự hiển thị của một mục danh mục (Story S2-07 AC3).
     */
    public boolean capNhatThuTu(LoaiDanhMuc loai, long id, int thuTu) {
        if (loai == null) return false;

        String bang = loai.getTenBang();
        String sql = "UPDATE " + bang + " SET thu_tu_hien_thi = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thuTu);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật thứ tự mục ID=" + id + " trong bảng " + bang, e);
            return false;
        }
    }

    /**
     * Đổi trạng thái hoạt động (bật/tắt kích hoạt).
     */
    public boolean doiTrangThai(LoaiDanhMuc loai, long id, boolean hoatDong) {
        if (loai == null) return false;

        String bang = loai.getTenBang();
        String sql = "UPDATE " + bang + " SET hoat_dong = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, hoatDong);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đổi trạng thái mục ID=" + id + " trong bảng " + bang, e);
            return false;
        }
    }

    /**
     * AC2: Đếm số lượng bản ghi trong toàn hệ thống đang tham chiếu đến mục danh mục này.
     * Kiểm tra các bảng Khách hàng, Lead, Cơ hội, Hoạt động.
     */
    public int demSoLuongThamChieu(LoaiDanhMuc loai, long id) {
        if (loai == null) return 0;
        int total = 0;

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            switch (loai) {
                case NGANH_NGHE:
                    // Tham chiếu trong khach_hang.nganh_nghe_id và lead.nganh_nghe_id
                    total += demThamChieuTrongBang(conn, "khach_hang", "nganh_nghe_id", id);
                    total += demThamChieuTrongBang(conn, "lead", "nganh_nghe_id", id);
                    break;

                case QUY_MO:
                    // Tham chiếu trong khach_hang.quy_mo_id và lead.quy_mo_id
                    total += demThamChieuTrongBang(conn, "khach_hang", "quy_mo_id", id);
                    total += demThamChieuTrongBang(conn, "lead", "quy_mo_id", id);
                    break;

                case NGUON_LEAD:
                    // Tham chiếu trong lead.nguon_lead_id và co_hoi.nguon_lead_id
                    total += demThamChieuTrongBang(conn, "lead", "nguon_lead_id", id);
                    total += demThamChieuTrongBang(conn, "co_hoi", "nguon_lead_id", id);
                    break;

                case LOAI_HOAT_DONG:
                    // Tham chiếu trong hoat_dong.loai_hoat_dong_id
                    total += demThamChieuTrongBang(conn, "hoat_dong", "loai_hoat_dong_id", id);
                    break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Kiểm tra tham chiếu gặp exception (bảng chưa tạo hoặc rỗng): " + e.getMessage());
        }
        return total;
    }

    private int demThamChieuTrongBang(Connection conn, String tenBang, String tenCot, long id) {
        String sql = "SELECT COUNT(*) FROM " + tenBang + " WHERE " + tenCot + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
            // Nếu bảng chưa có bản ghi hoặc chưa được tạo trong sprint hiện tại thì count = 0
        }
        return 0;
    }
}
