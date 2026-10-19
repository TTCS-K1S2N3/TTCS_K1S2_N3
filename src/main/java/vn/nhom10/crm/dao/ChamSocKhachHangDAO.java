package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn danh sách khách hàng cần chăm sóc định kỳ và đánh dấu đã liên hệ (Story S3-09).
 * Đáp ứng các tiêu chí:
 * • AC1: Khách chưa có tương tác nào trong N ngày, N cấu hình được từ cau_hinh_he_thong
 * • AC2: Sắp xếp theo giá trị hợp đồng giảm dần
 * • AC3: Đánh dấu đã liên hệ ngay trên danh sách (Transaction nguyên tử ghi nhận hoat_dong và lan_tuong_tac_cuoi)
 */
public class ChamSocKhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(ChamSocKhachHangDAO.class.getName());

    /**
     * Lấy chu kỳ số ngày chưa tương tác mặc định từ bảng cau_hinh_he_thong (AC1).
     *
     * @return số ngày N cấu hình, mặc định 30 ngày nếu chưa thiết lập hoặc lỗi kết nối
     */
    public int layCauHinhSoNgayMacDinh() {
        String sql = "SELECT gia_tri FROM cau_hinh_he_thong WHERE ma_cau_hinh = 'SO_NGAY_CHAM_SOC_DINH_KY'";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String val = rs.getString("gia_tri");
                if (val != null && !val.trim().isEmpty()) {
                    try {
                        int n = Integer.parseInt(val.trim());
                        if (n > 0) {
                            return n;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Không thể đọc cấu hình SO_NGAY_CHAM_SOC_DINH_KY: " + e.getMessage());
        }
        return 30; // Giá trị tiêu chuẩn mặc định
    }

    /**
     * Truy vấn danh sách khách hàng cần chăm sóc định kỳ theo Data Scope và bộ lọc N ngày (AC1, AC2).
     * Sắp xếp mặc định theo giá trị hợp đồng giảm dần.
     *
     * @param userId          ID người dùng hiện tại
     * @param dsNhomIds       Tập hợp các nhóm kinh doanh thuộc quyền quản lý
     * @param phamVi          Phạm vi dữ liệu (CA_NHAN, NHOM, TOAN_BO)
     * @param soNgay          Số ngày N chưa có tương tác
     * @param tuKhoa          Từ khóa tìm kiếm (tên, mã khách, người phụ trách, nhóm)
     * @param sapXepGiamDan   true nếu sắp xếp giá trị HĐ giảm dần (AC2), false nếu tăng dần
     * @return Danh sách KhachHangChamSocDTO
     * @throws SQLException khi có lỗi truy vấn cơ sở dữ liệu
     */
    public List<KhachHangChamSocDTO> layDanhSachCanChamSoc(Long userId,
                                                          Set<Long> dsNhomIds,
                                                          PhamViDuLieu phamVi,
                                                          int soNgay,
                                                          String tuKhoa,
                                                          Boolean sapXepGiamDan) throws SQLException {
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }
        if (soNgay <= 0) {
            soNgay = 30;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.sql.Timestamp cutoffTimestamp = java.sql.Timestamp.valueOf(today.minusDays(soNgay).atStartOfDay());
        java.sql.Date cutoffDate = java.sql.Date.valueOf(today.minusDays(soNgay));
        java.sql.Timestamp todayStart = java.sql.Timestamp.valueOf(today.atStartOfDay());

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.nguoi_so_huu_id, ")
           .append("nd.ho_ten AS ten_nguoi_phu_trach, kh.nhom_kinh_doanh_id, nkd.ten_nhom, ")
           .append("COALESCE(SUM(hd.gia_tri_hop_dong), kh.doanh_thu_uoc_tinh, 0) AS tong_gia_tri_hd, ")
           .append("GROUP_CONCAT(DISTINCT hd.so_hop_dong ORDER BY hd.id DESC SEPARATOR ', ') AS ds_so_hop_dong, ")
           .append("kh.lan_tuong_tac_cuoi, kh.trang_thai, kh.ngay_tao, kh.mo_ta_chi_tiet ")
           .append("FROM khach_hang kh ")
           .append("LEFT JOIN hop_dong hd ON (kh.id = hd.khach_hang_id AND hd.trang_thai != 'DA_HUY') ")
           .append("LEFT JOIN nguoi_dung nd ON (kh.nguoi_so_huu_id = nd.id) ")
           .append("LEFT JOIN nhom_kinh_doanh nkd ON (kh.nhom_kinh_doanh_id = nkd.id) ")
           .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // 1. Phân quyền Data Scope tại tầng SQL
        if (phamVi == PhamViDuLieu.CA_NHAN) {
            sql.append("AND kh.nguoi_so_huu_id = ? ");
            params.add(userId != null ? userId : -1L);
        } else if (phamVi == PhamViDuLieu.NHOM) {
            if (dsNhomIds != null && !dsNhomIds.isEmpty()) {
                sql.append("AND kh.nhom_kinh_doanh_id IN (");
                int idx = 0;
                for (Long nId : dsNhomIds) {
                    if (idx > 0) sql.append(", ");
                    sql.append("?");
                    params.add(nId);
                    idx++;
                }
                sql.append(") ");
            } else {
                sql.append("AND kh.nhom_kinh_doanh_id = -1 ");
            }
        }
        // TOAN_BO: Không thêm điều kiện người sở hữu

        // 2. Tìm kiếm theo từ khóa
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(kh.ten_cong_ty) LIKE ? OR LOWER(COALESCE(kh.ma_khach_hang, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(nd.ho_ten, '')) LIKE ? OR LOWER(COALESCE(nkd.ten_nhom, '')) LIKE ?) ");
            String p = "%" + tuKhoa.trim().toLowerCase() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
            params.add(p);
        }

        // 3. Lọc chu kỳ N ngày (AC1): Chưa tương tác >= N ngày HOẶC đã liên hệ hôm nay
        sql.append("AND (kh.lan_tuong_tac_cuoi <= ? OR (kh.lan_tuong_tac_cuoi IS NULL AND kh.ngay_tao <= ?) OR kh.lan_tuong_tac_cuoi >= ?) ");
        params.add(cutoffTimestamp);
        params.add(cutoffDate);
        params.add(todayStart);

        // 4. Gom nhóm theo từng khách hàng
        sql.append("GROUP BY kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.nguoi_so_huu_id, ")
           .append("nd.ho_ten, kh.nhom_kinh_doanh_id, nkd.ten_nhom, kh.lan_tuong_tac_cuoi, ")
           .append("kh.trang_thai, kh.ngay_tao, kh.mo_ta_chi_tiet ");

        // 5. Sắp xếp theo giá trị hợp đồng giảm dần (AC2)
        boolean desc = (sapXepGiamDan == null || sapXepGiamDan);
        sql.append("ORDER BY tong_gia_tri_hd ").append(desc ? "DESC" : "ASC").append(", kh.id DESC");

        List<KhachHangChamSocDTO> ketQua = new ArrayList<>();
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHangChamSocDTO dto = new KhachHangChamSocDTO();
                    dto.setId(rs.getLong("id"));
                    dto.setMaKhachHang(rs.getString("ma_khach_hang"));
                    dto.setTenCongTy(rs.getString("ten_cong_ty"));
                    dto.setNguoiPhuTrachId(rs.getLong("nguoi_so_huu_id"));
                    dto.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
                    long nhomId = rs.getLong("nhom_kinh_doanh_id");
                    dto.setNhomKinhDoanhId(rs.wasNull() ? null : nhomId);
                    dto.setTenNhom(rs.getString("ten_nhom"));

                    BigDecimal giaTri = rs.getBigDecimal("tong_gia_tri_hd");
                    dto.setTongGiaTriHopDong(giaTri != null ? giaTri : BigDecimal.ZERO);

                    String dsHd = rs.getString("ds_so_hop_dong");
                    dto.setSoHopDong(dsHd != null && !dsHd.isBlank() ? dsHd : "HĐ-CHUA-KY");

                    Timestamp ltt = rs.getTimestamp("lan_tuong_tac_cuoi");
                    java.sql.Date nt = rs.getDate("ngay_tao");
                    dto.setLanTuongTacCuoi(ltt);

                    boolean daLienHeHomNay = (ltt != null && !ltt.before(todayStart));
                    int soNgayChuaTt = 0;
                    if (daLienHeHomNay) {
                        soNgayChuaTt = 0;
                    } else if (ltt != null) {
                        soNgayChuaTt = (int) java.time.temporal.ChronoUnit.DAYS.between(ltt.toLocalDateTime().toLocalDate(), today);
                    } else if (nt != null) {
                        soNgayChuaTt = (int) java.time.temporal.ChronoUnit.DAYS.between(nt.toLocalDate(), today);
                    }

                    dto.setSoNgayChuaTuongTac(Math.max(0, soNgayChuaTt));
                    dto.setDaLienHeHomNay(daLienHeHomNay);
                    dto.setTrangThai(rs.getString("trang_thai"));

                    dto.setNgayTao(nt != null ? nt.toString() : "");
                    dto.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));

                    ketQua.add(dto);
                }
            }
        }

        return ketQua;
    }

    /**
     * Thực hiện đánh dấu đã liên hệ khách hàng trong một Database Transaction nguyên tử (AC3).
     * 1. Cập nhật lan_tuong_tac_cuoi = CURRENT_TIMESTAMP trên bảng khach_hang
     * 2. Thêm bản ghi hoạt động chăm sóc trên bảng hoat_dong
     * Nếu xảy ra bất kỳ lỗi nào, toàn bộ giao dịch sẽ tự động Rollback để đảm bảo tính toàn vẹn dữ liệu.
     *
     * @param khachHangId ID khách hàng
     * @param userId      ID người dùng thực hiện liên hệ
     * @param nhomId      ID nhóm của người dùng
     * @param kenhLienHe  Kênh liên hệ (CUOC_GOI, GAP_MAT, EMAIL, ZALO_TIN_NHAN)
     * @param ghiChu      Nội dung ghi chú kết quả trao đổi
     * @return true nếu ghi nhận thành công
     * @throws SQLException khi lỗi cơ sở dữ liệu
     */
    public boolean danhDauDaLienHe(Long khachHangId,
                                  Long userId,
                                  Long nhomId,
                                  String kenhLienHe,
                                  String ghiChu) throws SQLException {
        if (khachHangId == null || khachHangId <= 0) {
            return false;
        }

        String sqlUpdateKh = "UPDATE khach_hang SET lan_tuong_tac_cuoi = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String sqlInsertHd = "INSERT INTO hoat_dong (" +
                "ma_hoat_dong, tieu_de, loai_hoat_dong, khach_hang_id, nguoi_phu_trach_id, " +
                "nhom_kinh_doanh_id, thoi_gian_bat_dau, thoi_gian_ket_thuc, trang_thai, " +
                "noi_dung, ket_qua, la_ghi_nhan_nhanh, ngay_tao" +
                ") VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'HOAN_THANH', ?, 'Đã liên hệ thành công', 1, CURRENT_DATE)";

        String maHoatDong = "HD-CSKH-" + System.currentTimeMillis() + "-" + ((int) (Math.random() * 900) + 100);
        String loaiHd = (kenhLienHe != null && !kenhLienHe.isBlank()) ? kenhLienHe.trim() : "CUOC_GOI";
        String tieuDe = "Chăm sóc khách hàng định kỳ";
        String noiDungHd = (ghiChu != null && !ghiChu.isBlank())
                ? ghiChu.trim()
                : "Đã liên hệ trao đổi tình hình vận hành hệ thống định kỳ.";

        Connection conn = DatabaseConnection.layKetNoi();
        boolean originalAutoCommit = conn.getAutoCommit();

        try {
            // Khởi tạo Transaction nguyên tử
            conn.setAutoCommit(false);

            // 1. Cập nhật lần tương tác cuối của khách hàng
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateKh)) {
                psUpdate.setLong(1, khachHangId);
                int rows = psUpdate.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Thêm bản ghi nhật ký hoạt động
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertHd)) {
                psInsert.setString(1, maHoatDong);
                psInsert.setString(2, tieuDe);
                psInsert.setString(3, loaiHd);
                psInsert.setLong(4, khachHangId);
                psInsert.setLong(5, userId != null ? userId : 1L);
                if (nhomId != null) {
                    psInsert.setLong(6, nhomId);
                } else {
                    psInsert.setNull(6, java.sql.Types.BIGINT);
                }
                psInsert.setString(7, noiDungHd);
                psInsert.executeUpdate();
            }

            // Xác nhận giao dịch thành công
            conn.commit();
            return true;
        } catch (SQLException ex) {
            // Rollback toàn bộ khi gặp lỗi
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Lỗi khi rollback transaction: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw ex;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignored) {
            }
            conn.close();
        }
    }
}
