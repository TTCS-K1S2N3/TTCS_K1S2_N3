package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO.LoaiNghiepVu;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn dữ liệu từ MySQL và áp dụng Data Scope (Story S1-05).
 * Tự động lọc dữ liệu ngay tại tầng SQL bằng PreparedStatement:
 * - CA_NHAN: WHERE nguoi_phu_trach_id = ?
 * - NHOM: WHERE nhom_kinh_doanh_id = ?
 * - TOAN_BO: Không áp điều kiện sở hữu (truy cập toàn bộ)
 * Áp dụng thống nhất cho cả 4 nghiệp vụ: Khách hàng, Cơ hội, Báo giá, Hoạt động.
 */
public class PhanQuyenDuLieuDAO {

    private static final Logger LOGGER = Logger.getLogger(PhanQuyenDuLieuDAO.class.getName());

    /**
     * Lấy danh sách bản ghi theo phạm vi dữ liệu, từ khóa tìm kiếm và loại nghiệp vụ.
     */
    public List<BanGhiNghiepVuDTO> layDanhSachTongHopTheoPhamVi(Long userId,
                                                                 Long nhomId,
                                                                 PhamViDuLieu phamVi,
                                                                 String tuKhoa,
                                                                 LoaiNghiepVu loaiNghiepVu) throws SQLException {
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }

        List<BanGhiNghiepVuDTO> ketQua = new ArrayList<>();

        if (loaiNghiepVu != null) {
            ketQua.addAll(layDanhSachTheoBang(loaiNghiepVu, userId, nhomId, phamVi, tuKhoa));
        } else {
            // Khi không chỉ định loại, truy vấn tổng hợp cả 4 đối tượng nghiệp vụ cốt lõi
            ketQua.addAll(layDanhSachTheoBang(LoaiNghiepVu.KHACH_HANG, userId, nhomId, phamVi, tuKhoa));
            ketQua.addAll(layDanhSachTheoBang(LoaiNghiepVu.CO_HOI, userId, nhomId, phamVi, tuKhoa));
            ketQua.addAll(layDanhSachTheoBang(LoaiNghiepVu.BAO_GIA, userId, nhomId, phamVi, tuKhoa));
            ketQua.addAll(layDanhSachTheoBang(LoaiNghiepVu.HOAT_DONG, userId, nhomId, phamVi, tuKhoa));
        }

        return ketQua;
    }

    /**
     * Truy vấn bản ghi từ một bảng cụ thể với điều kiện Data Scope trong SQL.
     */
    public List<BanGhiNghiepVuDTO> layDanhSachTheoBang(LoaiNghiepVu loaiNghiepVu,
                                                       Long userId,
                                                       Long nhomId,
                                                       PhamViDuLieu phamVi,
                                                       String tuKhoa) throws SQLException {
        String tenBang = layTenBang(loaiNghiepVu);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.id, t.ma_ban_ghi, t.tieu_de, t.nguoi_phu_trach_id, ")
           .append("nd.ho_ten AS ten_nguoi_phu_trach, t.nhom_kinh_doanh_id, ")
           .append("nkd.ten_nhom, t.giaTri, t.trang_thai, t.ngay_tao, t.mo_ta_chi_tiet ")
           .append("FROM ").append(tenBang).append(" t ")
           .append("LEFT JOIN nguoi_dung nd ON t.nguoi_phu_trach_id = nd.id ")
           .append("LEFT JOIN nhom_kinh_doanh nkd ON t.nhom_kinh_doanh_id = nkd.id ")
           .append("WHERE 1=1 ");

        List<Object> thamSo = new ArrayList<>();

        // Áp dụng điều kiện Data Scope tại tầng SQL
        if (phamVi == PhamViDuLieu.CA_NHAN) {
            sql.append("AND t.nguoi_phu_trach_id = ? ");
            thamSo.add(userId != null ? userId : -1L);
        } else if (phamVi == PhamViDuLieu.NHOM) {
            sql.append("AND t.nhom_kinh_doanh_id = ? ");
            thamSo.add(nhomId != null ? nhomId : -1L);
        }
        // TOAN_BO: Không thêm điều kiện người phụ trách hay nhóm

        // Áp dụng tìm kiếm từ khóa an toàn qua PreparedStatement
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(t.ma_ban_ghi) LIKE ? OR LOWER(t.tieu_de) LIKE ? ")
               .append("OR LOWER(nd.ho_ten) LIKE ? OR LOWER(COALESCE(nkd.ten_nhom, '')) LIKE ?) ");
            String kwPattern = "%" + tuKhoa.trim().toLowerCase() + "%";
            thamSo.add(kwPattern);
            thamSo.add(kwPattern);
            thamSo.add(kwPattern);
            thamSo.add(kwPattern);
        }

        sql.append("ORDER BY t.ngay_tao DESC, t.id DESC");

        List<BanGhiNghiepVuDTO> danhSach = new ArrayList<>();
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToDTO(rs, loaiNghiepVu));
                }
            }
        }

        return danhSach;
    }

    /**
     * Tìm một bản ghi theo ID và loại nghiệp vụ.
     */
    public BanGhiNghiepVuDTO timBanGhiTheoId(Long id, LoaiNghiepVu loaiNghiepVu) throws SQLException {
        if (id == null) {
            return null;
        }

        if (loaiNghiepVu != null) {
            return timTrongBangTheoId(id, loaiNghiepVu);
        }

        // Nếu không biết rõ loại, thử tìm lần lượt trong 4 bảng
        for (LoaiNghiepVu loai : LoaiNghiepVu.values()) {
            BanGhiNghiepVuDTO dto = timTrongBangTheoId(id, loai);
            if (dto != null) {
                return dto;
            }
        }
        return null;
    }

    private BanGhiNghiepVuDTO timTrongBangTheoId(Long id, LoaiNghiepVu loai) throws SQLException {
        String tenBang = layTenBang(loai);
        String sql = "SELECT t.id, t.ma_ban_ghi, t.tieu_de, t.nguoi_phu_trach_id, " +
                "nd.ho_ten AS ten_nguoi_phu_trach, t.nhom_kinh_doanh_id, " +
                "nkd.ten_nhom, t.giaTri, t.trang_thai, t.ngay_tao, t.mo_ta_chi_tiet " +
                "FROM " + tenBang + " t " +
                "LEFT JOIN nguoi_dung nd ON t.nguoi_phu_trach_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON t.nhom_kinh_doanh_id = nkd.id " +
                "WHERE t.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs, loai);
                }
            }
        }
        return null;
    }

    private BanGhiNghiepVuDTO mapResultSetToDTO(ResultSet rs, LoaiNghiepVu loai) throws SQLException {
        Date sqlDate = rs.getDate("ngay_tao");
        LocalDate ngayTao = sqlDate != null ? sqlDate.toLocalDate() : LocalDate.now();

        return new BanGhiNghiepVuDTO(
                rs.getLong("id"),
                rs.getString("ma_ban_ghi"),
                rs.getString("tieu_de"),
                loai,
                rs.getLong("nguoi_phu_trach_id"),
                rs.getString("ten_nguoi_phu_trach"),
                rs.getLong("nhom_kinh_doanh_id"),
                rs.getString("ten_nhom"),
                rs.getString("giaTri"),
                rs.getString("trang_thai"),
                ngayTao,
                rs.getString("mo_ta_chi_tiet")
        );
    }

    private String layTenBang(LoaiNghiepVu loai) {
        switch (loai) {
            case KHACH_HANG:
                return "khach_hang";
            case CO_HOI:
                return "co_hoi";
            case BAO_GIA:
                return "bao_gia";
            case HOAT_DONG:
                return "hoat_dong";
            default:
                throw new IllegalArgumentException("Loại nghiệp vụ không hợp lệ: " + loai);
        }
    }
}
