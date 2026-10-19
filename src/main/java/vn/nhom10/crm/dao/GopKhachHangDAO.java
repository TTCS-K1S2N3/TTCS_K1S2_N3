package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.KetQuaGopKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * DAO xử lý giao dịch cơ sở dữ liệu gộp khách hàng trùng lặp (Story S3-04, AC3).
 * Đảm bảo tính nguyên tử (Atomicity) qua Database Transaction:
 * - Chuyển toàn bộ người liên hệ (nguoi_lien_he)
 * - Chuyển toàn bộ cơ hội bán hàng (co_hoi)
 * - Chuyển toàn bộ lịch sử hoạt động (hoat_dong)
 * - Chuyển toàn bộ tệp đính kèm (tep_dinh_kem), báo giá (bao_gia), hợp đồng (hop_dong)
 * - Đánh dấu khách hàng nguồn: gop_vao_khach_hang_id = ? và trang_thai = 'DA_GOP'
 * - Lưu vết kiểm toán vào bảng lich_su_gop_khach_hang
 * Bất kỳ lỗi nào phát sinh đều ROLLBACK 100% dữ liệu.
 */
public class GopKhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(GopKhachHangDAO.class.getName());

    public static class ThongKeLienQuan {
        public int soNguoiLienHe = 0;
        public int soCoHoi = 0;
        public int soHoatDong = 0;
        public int soTepDinhKem = 0;
    }

    /**
     * Đếm số lượng các đối tượng liên quan thuộc khách hàng (phục vụ hiển thị so sánh trước khi gộp).
     */
    public ThongKeLienQuan layThongKeLienQuan(Connection conn, Long khachHangId) throws SQLException {
        ThongKeLienQuan tk = new ThongKeLienQuan();
        if (khachHangId == null) {
            return tk;
        }

        // Đếm người liên hệ
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM nguoi_lien_he WHERE khach_hang_id = ?")) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soNguoiLienHe = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm nguoi_lien_he: " + e.getMessage());
        }

        // Đếm cơ hội
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM co_hoi WHERE khach_hang_id = ?")) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soCoHoi = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm co_hoi: " + e.getMessage());
        }

        // Đếm hoạt động
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM hoat_dong WHERE khach_hang_id = ?")) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soHoatDong = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm hoat_dong: " + e.getMessage());
        }

        // Đếm tệp đính kèm
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tep_dinh_kem WHERE khach_hang_id = ?")) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soTepDinhKem = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm tep_dinh_kem: " + e.getMessage());
        }

        return tk;
    }

    public ThongKeLienQuan layThongKeLienQuan(Long khachHangId) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return layThongKeLienQuan(conn, khachHangId);
        }
    }

    /**
     * Lấy danh sách khách hàng chưa bị gộp kèm thông tin mã số thuế, website, người phụ trách
     * để phục vụ phát hiện trùng lặp theo thẩm quyền Data Scope (AC1, AC4).
     */
    public List<BanGhiNghiepVuDTO> layDanhSachKhachHangChuaGop(NguoiDungDTO user, Set<Long> danhSachNhomIds) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return layDanhSachKhachHangChuaGop(conn, user, danhSachNhomIds);
        }
    }

    public List<BanGhiNghiepVuDTO> layDanhSachKhachHangChuaGop(Connection conn, NguoiDungDTO user, Set<Long> danhSachNhomIds) throws SQLException {
        List<BanGhiNghiepVuDTO> ds = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.ma_khach_hang AS ma_ban_ghi, t.ten_cong_ty AS tieu_de, " +
                "       t.ma_so_thue, t.website, " +
                "       t.nguoi_so_huu_id AS nguoi_phu_trach_id, nd.ho_ten AS ten_nguoi_phu_trach, " +
                "       t.nhom_kinh_doanh_id, nkd.ten_nhom, t.doanh_thu_uoc_tinh AS gia_tri, " +
                "       t.trang_thai, t.ngay_tao " +
                "FROM khach_hang t " +
                "LEFT JOIN nguoi_dung nd ON t.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON t.nhom_kinh_doanh_id = nkd.id " +
                "WHERE (t.gop_vao_khach_hang_id IS NULL OR t.trang_thai != 'DA_GOP') "
        );

        List<Object> params = new ArrayList<>();
        if (user != null) {
            VaiTroEnum vaiTro = user.getVaiTro();
            if (vaiTro == VaiTroEnum.SALES_REP) {
                sql.append("AND t.nguoi_so_huu_id = ? ");
                params.add(user.getId());
            } else if (vaiTro == VaiTroEnum.TEAM_LEAD) {
                if (danhSachNhomIds != null && !danhSachNhomIds.isEmpty()) {
                    String placeholders = danhSachNhomIds.stream().map(id -> "?").collect(Collectors.joining(","));
                    sql.append("AND t.nhom_kinh_doanh_id IN (").append(placeholders).append(") ");
                    params.addAll(danhSachNhomIds);
                } else if (user.getNhomKinhDoanhId() != null) {
                    sql.append("AND t.nhom_kinh_doanh_id = ? ");
                    params.add(user.getNhomKinhDoanhId());
                }
            }
        }

        sql.append("ORDER BY t.ngay_tao DESC, t.id DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanGhiNghiepVuDTO bg = new BanGhiNghiepVuDTO();
                    bg.setId(rs.getLong("id"));
                    bg.setMaBanGhi(rs.getString("ma_ban_ghi"));
                    bg.setTieuDe(rs.getString("tieu_de"));
                    bg.setLoaiNghiepVu(BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG);
                    bg.setMaSoThue(rs.getString("ma_so_thue"));
                    bg.setWebsite(rs.getString("website"));
                    bg.setNguoiPhuTrachId(rs.getObject("nguoi_phu_trach_id", Long.class));
                    bg.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
                    bg.setNhomKinhDoanhId(rs.getObject("nhom_kinh_doanh_id", Long.class));
                    bg.setTenNhom(rs.getString("ten_nhom"));
                    bg.setTrangThai(rs.getString("trang_thai"));
                    Date ngayTao = rs.getDate("ngay_tao");
                    if (ngayTao != null) bg.setNgayTao(ngayTao.toLocalDate());
                    ds.add(bg);
                }
            }
        }
        return ds;
    }

    public BanGhiNghiepVuDTO layChiTietKhachHang(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return layChiTietKhachHang(conn, id);
        }
    }

    public BanGhiNghiepVuDTO layChiTietKhachHang(Connection conn, Long id) throws SQLException {
        if (id == null) return null;
        String sql = "SELECT t.id, t.ma_khach_hang AS ma_ban_ghi, t.ten_cong_ty AS tieu_de, " +
                "       t.ma_so_thue, t.website, " +
                "       t.nguoi_so_huu_id AS nguoi_phu_trach_id, nd.ho_ten AS ten_nguoi_phu_trach, " +
                "       t.nhom_kinh_doanh_id, nkd.ten_nhom, t.doanh_thu_uoc_tinh AS gia_tri, " +
                "       t.trang_thai, t.ngay_tao " +
                "FROM khach_hang t " +
                "LEFT JOIN nguoi_dung nd ON t.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON t.nhom_kinh_doanh_id = nkd.id " +
                "WHERE t.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BanGhiNghiepVuDTO bg = new BanGhiNghiepVuDTO();
                    bg.setId(rs.getLong("id"));
                    bg.setMaBanGhi(rs.getString("ma_ban_ghi"));
                    bg.setTieuDe(rs.getString("tieu_de"));
                    bg.setLoaiNghiepVu(BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG);
                    bg.setMaSoThue(rs.getString("ma_so_thue"));
                    bg.setWebsite(rs.getString("website"));
                    bg.setNguoiPhuTrachId(rs.getObject("nguoi_phu_trach_id", Long.class));
                    bg.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
                    bg.setNhomKinhDoanhId(rs.getObject("nhom_kinh_doanh_id", Long.class));
                    bg.setTenNhom(rs.getString("ten_nhom"));
                    bg.setTrangThai(rs.getString("trang_thai"));
                    Date ngayTao = rs.getDate("ngay_tao");
                    if (ngayTao != null) bg.setNgayTao(ngayTao.toLocalDate());
                    return bg;
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra xem khách hàng đã từng bị gộp vào hồ sơ khác hay chưa.
     */
    public boolean kiemTraDaBiGop(Connection conn, Long khachHangId) throws SQLException {
        if (khachHangId == null) {
            return false;
        }
        String sql = "SELECT gop_vao_khach_hang_id, trang_thai FROM khach_hang WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long gopVaoId = rs.getObject("gop_vao_khach_hang_id", Long.class);
                    String trangThai = rs.getString("trang_thai");
                    return gopVaoId != null || "DA_GOP".equalsIgnoreCase(trangThai);
                }
            }
        }
        return false;
    }

    /**
     * Thực hiện gộp khách hàng trong một DB Transaction duy nhất (AC3).
     *
     * @param khachHangNguonId   ID khách hàng nguồn (bị gộp)
     * @param khachHangDichId    ID khách hàng đích (giữ lại làm hồ sơ chính)
     * @param thucHienBoiId      ID người dùng thực hiện (Trưởng nhóm trở lên)
     * @param lyDo               Lý do gộp
     * @param duLieuSoSanhJson   Snapshot so sánh trước khi gộp
     * @return KetQuaGopKhachHangDTO chi tiết kết quả và số lượng bản ghi đã chuyển
     * @throws SQLException nếu xảy ra lỗi cơ sở dữ liệu (tự động rollback)
     */
    public KetQuaGopKhachHangDTO thucHienGop(Long khachHangNguonId,
                                            Long khachHangDichId,
                                            Long thucHienBoiId,
                                            String lyDo,
                                            String duLieuSoSanhJson) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return thucHienGopTrenKetNoi(conn, khachHangNguonId, khachHangDichId, thucHienBoiId, lyDo, duLieuSoSanhJson, false);
        }
    }

    /**
     * Phương thức thực hiện gộp trực tiếp trên Connection hỗ trợ Transaction và kiểm thử Rollback.
     */
    public KetQuaGopKhachHangDTO thucHienGopTrenKetNoi(Connection conn,
                                                      Long khachHangNguonId,
                                                      Long khachHangDichId,
                                                      Long thucHienBoiId,
                                                      String lyDo,
                                                      String duLieuSoSanhJson,
                                                      boolean batBuocLoiDeTestRollback) throws SQLException {
        if (khachHangNguonId == null || khachHangDichId == null) {
            throw new IllegalArgumentException("Mã khách hàng nguồn và đích không được để trống.");
        }
        if (khachHangNguonId.equals(khachHangDichId)) {
            throw new IllegalArgumentException("Không thể gộp một khách hàng vào chính nó.");
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

        try {
            // 1. Kiểm tra trạng thái đã gộp
            if (kiemTraDaBiGop(conn, khachHangNguonId)) {
                throw new IllegalStateException("Khách hàng nguồn (#" + khachHangNguonId + ") đã được gộp trước đó.");
            }

            // 2. Chuyển Người liên hệ (nguoi_lien_he - S3-02)
            // Kiểm tra nếu khách hàng đích đã có đầu mối chính, hạ cờ đầu mối chính từ khách hàng nguồn để đảm bảo AC3 (tối đa 1 đầu mối chính)
            boolean dichDaCoDauMoi = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM nguoi_lien_he WHERE khach_hang_id = ? AND la_dau_moi_chinh = 1")) {
                ps.setLong(1, khachHangDichId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        dichDaCoDauMoi = true;
                    }
                }
            } catch (SQLException e) {
                if (!laLoiKhongTonTaiBang(e)) throw e;
            }

            if (dichDaCoDauMoi) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE nguoi_lien_he SET la_dau_moi_chinh = 0 WHERE khach_hang_id = ? AND la_dau_moi_chinh = 1")) {
                    ps.setLong(1, khachHangNguonId);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    if (!laLoiKhongTonTaiBang(e)) throw e;
                }
            }

            int soNlhChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE nguoi_lien_he SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                soNlhChuyen = ps.executeUpdate();
            }

            // 3. Chuyển Cơ hội bán hàng (co_hoi)
            int soCoHoiChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE co_hoi SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                soCoHoiChuyen = ps.executeUpdate();
            }

            // 4. Chuyển Lịch sử hoạt động (hoat_dong)
            int soHoatDongChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE hoat_dong SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                soHoatDongChuyen = ps.executeUpdate();
            }

            // 5. Chuyển Tệp đính kèm (tep_dinh_kem)
            int soTepChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tep_dinh_kem SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                soTepChuyen = ps.executeUpdate();
            }

            // 6. Chuyển Báo giá (bao_gia) nếu có
            int soBaoGiaChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE bao_gia SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                soBaoGiaChuyen = ps.executeUpdate();
            } catch (SQLException e) {
                if (!laLoiKhongTonTaiBang(e)) throw e;
            }

            // 7. Chuyển Hợp đồng (hop_dong) nếu có
            int soHopDongChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE hop_dong SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                soHopDongChuyen = ps.executeUpdate();
            } catch (SQLException e) {
                if (!laLoiKhongTonTaiBang(e)) throw e;
            }

            // 7b. Chuyển Yêu cầu hỗ trợ (yeu_cau_ho_tro - S3-08) nếu có
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE yeu_cau_ho_tro SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!laLoiKhongTonTaiBang(e)) throw e;
            }

            // 7c. Chuyển Lịch chăm sóc định kỳ (cham_soc_khach_hang - S3-09) nếu có
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cham_soc_khach_hang SET khach_hang_id = ? WHERE khach_hang_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!laLoiKhongTonTaiBang(e)) throw e;
            }

            // 7d. Cập nhật quan hệ công ty mẹ-con (cong_ty_me_id - S3-05) tránh mồ côi
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE khach_hang SET cong_ty_me_id = ? WHERE cong_ty_me_id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!laLoiKhongTonTaiBang(e)) throw e;
            }

            // 8. Đánh dấu khách hàng nguồn là đã gộp (AC3)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE khach_hang SET gop_vao_khach_hang_id = ?, trang_thai = 'DA_GOP', updated_at = NOW() WHERE id = ?")) {
                ps.setLong(1, khachHangDichId);
                ps.setLong(2, khachHangNguonId);
                ps.executeUpdate();
            }

            // Chuẩn bị kết quả chuyển dạng JSON
            String ketQuaChuyenJson = String.format(
                    "{\"so_nguoi_lien_he\":%d,\"so_co_hoi\":%d,\"so_hoat_dong\":%d,\"so_tep\":%d,\"so_bao_gia\":%d,\"so_hop_dong\":%d}",
                    soNlhChuyen, soCoHoiChuyen, soHoatDongChuyen, soTepChuyen, soBaoGiaChuyen, soHopDongChuyen
            );

            // 9. Lưu vết vào bảng lich_su_gop_khach_hang (bắt buộc, không nuốt ngoại lệ)
            Long lichSuId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO lich_su_gop_khach_hang " +
                    "(khach_hang_nguon_id, khach_hang_dich_id, thuc_hien_boi_id, du_lieu_so_sanh_json, ket_qua_chuyen_json, ly_do, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, khachHangNguonId);
                ps.setLong(2, khachHangDichId);
                ps.setLong(3, thucHienBoiId != null ? thucHienBoiId : 1L);
                ps.setString(4, duLieuSoSanhJson != null ? duLieuSoSanhJson : "{}");
                ps.setString(5, ketQuaChuyenJson);
                ps.setString(6, lyDo != null ? lyDo : "Gộp khách hàng trùng lặp");
                ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        lichSuId = rs.getLong(1);
                    }
                }
            }

            // KIỂM THỬ ROLLBACK: Nếu cờ bắt buộc lỗi được kích hoạt -> cố tình ném SQLException
            if (batBuocLoiDeTestRollback) {
                throw new SQLException("Cố tình giả lập sự cố giao dịch để kiểm tra tính năng ROLLBACK an toàn dữ liệu.");
            }

            // 10. COMMIT TRANSACTION
            conn.commit();

            KetQuaGopKhachHangDTO ketQua = KetQuaGopKhachHangDTO.thanhCong(
                    "Gộp khách hàng thành công.", khachHangNguonId, khachHangDichId
            );
            ketQua.setSoNguoiLienHeDaChuyen(soNlhChuyen);
            ketQua.setSoCoHoiDaChuyen(soCoHoiChuyen);
            ketQua.setSoHoatDongDaChuyen(soHoatDongChuyen);
            ketQua.setSoTepDinhKemDaChuyen(soTepChuyen);
            ketQua.setSoBaoGiaDaChuyen(soBaoGiaChuyen);
            ketQua.setSoHopDongDaChuyen(soHopDongChuyen);
            ketQua.setLichSuGopId(lichSuId);
            return ketQua;

        } catch (SQLException | RuntimeException e) {
            // ROLLBACK GIAO DỊCH
            try {
                conn.rollback();
                LOGGER.log(Level.INFO, "Đã thực hiện ROLLBACK giao dịch gộp khách hàng do phát sinh lỗi: " + e.getMessage());
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Lỗi khi thực hiện rollback giao dịch: " + ex.getMessage(), ex);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignored) {}
        }
    }

    private boolean laLoiKhongTonTaiBang(SQLException e) {
        if (e == null) return false;
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        String sqlState = e.getSQLState() != null ? e.getSQLState() : "";
        return e.getErrorCode() == 1146
                || sqlState.equals("42S02")
                || sqlState.equals("42102")
                || msg.contains("not found")
                || msg.contains("doesn't exist")
                || msg.contains("does not exist");
    }
}
