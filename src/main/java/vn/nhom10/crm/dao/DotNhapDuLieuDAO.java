package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BaoCaoNhapExcelDTO;
import vn.nhom10.crm.dto.DongExcelNguoiDungDTO;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO phụ trách lưu vết đợt nhập dữ liệu và các dòng lỗi vào bảng dot_nhap_du_lieu, loi_nhap_du_lieu
 * theo chuẩn DATABASE_RULES.md và schema canonical 001_crm_ban_hang_full_schema_8_sprints.sql.
 */
public class DotNhapDuLieuDAO {

    private static final Logger LOGGER = Logger.getLogger(DotNhapDuLieuDAO.class.getName());

    /**
     * Ghi nhận đợt nhập dữ liệu người dùng và các lỗi dòng tương ứng.
     *
     * @param baoCao          báo cáo tổng kết đợt nhập
     * @param nguoiThucHienId ID người dùng thực hiện (admin)
     * @return ID đợt nhập vừa tạo (hoặc null nếu lỗi)
     */
    public Long luuDotNhap(BaoCaoNhapExcelDTO baoCao, Long nguoiThucHienId) {
        if (baoCao == null) {
            return null;
        }

        String sqlDot = "INSERT INTO dot_nhap_du_lieu (loai_doi_tuong, ten_file_goc, trang_thai, " +
                "tong_dong, so_dong_hop_le, so_dong_loi, so_dong_da_nhap, nguoi_thuc_hien_id, tao_luc, hoan_tat_luc) " +
                "VALUES ('NGUOI_DUNG', ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        String sqlLoi = "INSERT INTO loi_nhap_du_lieu (dot_nhap_id, so_dong, ma_loi, thong_bao_loi, co_the_bo_qua) " +
                "VALUES (?, ?, 'LOI_DU_LIEU_DONG', ?, 1)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false);

            long dotNhapId;
            try (PreparedStatement psDot = conn.prepareStatement(sqlDot, Statement.RETURN_GENERATED_KEYS)) {
                psDot.setString(1, baoCao.getTenTep() != null ? baoCao.getTenTep() : "import_nguoi_dung.xlsx");
                psDot.setString(2, baoCao.isDaThucHienNhap() ? "HOAN_TAT" : "XEM_TRUOC");
                psDot.setInt(3, baoCao.getTongSoDong());
                psDot.setInt(4, baoCao.getSoDongHopLe());
                psDot.setInt(5, baoCao.getSoDongLoi());
                psDot.setInt(6, baoCao.getSoDongThanhCong());
                psDot.setLong(7, (nguoiThucHienId != null && nguoiThucHienId > 0) ? nguoiThucHienId : 1L);

                psDot.executeUpdate();
                try (ResultSet rs = psDot.getGeneratedKeys()) {
                    if (rs.next()) {
                        dotNhapId = rs.getLong(1);
                    } else {
                        conn.rollback();
                        return null;
                    }
                }
            }

            // Lưu các dòng lỗi vào loi_nhap_du_lieu
            if (baoCao.getDanhSachDongLoi() != null && !baoCao.getDanhSachDongLoi().isEmpty()) {
                try (PreparedStatement psLoi = conn.prepareStatement(sqlLoi)) {
                    for (DongExcelNguoiDungDTO dong : baoCao.getDanhSachDongLoi()) {
                        psLoi.setLong(1, dotNhapId);
                        psLoi.setInt(2, dong.getSoDong());
                        psLoi.setString(3, dong.getChuoiLoi());
                        psLoi.addBatch();
                    }
                    psLoi.executeBatch();
                }
            }

            conn.commit();
            baoCao.setDotNhapId(dotNhapId);
            return dotNhapId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            LOGGER.log(Level.WARNING, "Không thể ghi vết dot_nhap_du_lieu (có thể chưa tạo bảng): " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
