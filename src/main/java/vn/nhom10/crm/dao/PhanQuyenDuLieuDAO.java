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
        String cotGiaTri = layCotGiaTri(loaiNghiepVu);
        String bieuThucTieuDe = layBieuThucTieuDe(loaiNghiepVu);
        String bieuThucMa = layBieuThucMaBanGhi(loaiNghiepVu);
        String bieuThucOwner = layBieuThucNguoiPhuTrach(loaiNghiepVu);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.id, ").append(bieuThucMa).append(" AS ma_ban_ghi, ")
           .append(bieuThucTieuDe).append(" AS tieu_de, ")
           .append(bieuThucOwner).append(" AS nguoi_phu_trach_id, ")
           .append("nd.ho_ten AS ten_nguoi_phu_trach, t.nhom_kinh_doanh_id, ")
           .append("nkd.ten_nhom, t.").append(cotGiaTri).append(" AS gia_tri, ")
           .append("t.trang_thai, t.ngay_tao, t.mo_ta_chi_tiet ")
           .append("FROM ").append(tenBang).append(" t ")
           .append("LEFT JOIN nguoi_dung nd ON (").append(bieuThucOwner).append(" = nd.id) ")
           .append("LEFT JOIN nhom_kinh_doanh nkd ON t.nhom_kinh_doanh_id = nkd.id ")
           .append("WHERE 1=1 ");

        List<Object> thamSo = new ArrayList<>();

        // Áp dụng điều kiện Data Scope tại tầng SQL
        if (phamVi == PhamViDuLieu.CA_NHAN) {
            sql.append("AND ").append(bieuThucOwner).append(" = ? ");
            thamSo.add(userId != null ? userId : -1L);
        } else if (phamVi == PhamViDuLieu.NHOM) {
            sql.append("AND t.nhom_kinh_doanh_id = ? ");
            thamSo.add(nhomId != null ? nhomId : -1L);
        }
        // TOAN_BO: Không thêm điều kiện người phụ trách hay nhóm

        // Áp dụng tìm kiếm từ khóa an toàn qua PreparedStatement
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (LOWER(").append(bieuThucMa).append(") LIKE ? OR LOWER(").append(bieuThucTieuDe).append(") LIKE ? ")
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
        String cotGiaTri = layCotGiaTri(loai);
        String bieuThucTieuDe = layBieuThucTieuDe(loai);
        String bieuThucMa = layBieuThucMaBanGhi(loai);
        String bieuThucOwner = layBieuThucNguoiPhuTrach(loai);

        String sql = "SELECT t.id, " + bieuThucMa + " AS ma_ban_ghi, " + bieuThucTieuDe + " AS tieu_de, " +
                bieuThucOwner + " AS nguoi_phu_trach_id, " +
                "nd.ho_ten AS ten_nguoi_phu_trach, t.nhom_kinh_doanh_id, " +
                "nkd.ten_nhom, t." + cotGiaTri + " AS gia_tri, " +
                "t.trang_thai, t.ngay_tao, t.mo_ta_chi_tiet " +
                "FROM " + tenBang + " t " +
                "LEFT JOIN nguoi_dung nd ON (" + bieuThucOwner + " = nd.id) " +
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
                rs.getString("gia_tri"),
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

    private String layCotGiaTri(LoaiNghiepVu loai) {
        switch (loai) {
            case KHACH_HANG:
                return "doanh_thu_uoc_tinh";
            case CO_HOI:
                return "gia_tri_du_kien";
            case BAO_GIA:
                return "tong_tien";
            case HOAT_DONG:
                return "chi_phi";
            default:
                throw new IllegalArgumentException("Loại nghiệp vụ không hợp lệ: " + loai);
        }
    }

    private java.math.BigDecimal parseGiaTri(String giaTri) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        try {
            String clean = giaTri.replaceAll("[^0-9.]", "");
            if (clean.isEmpty()) {
                return java.math.BigDecimal.ZERO;
            }
            return new java.math.BigDecimal(clean);
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * Cập nhật bản ghi nghiệp vụ (yêu cầu phân quyền kiểm tra trước khi gọi).
     */
    public boolean capNhatBanGhi(BanGhiNghiepVuDTO banGhi) throws SQLException {
        if (banGhi == null || banGhi.getId() == null || banGhi.getLoaiNghiepVu() == null) {
            return false;
        }
        String tenBang = layTenBang(banGhi.getLoaiNghiepVu());
        String cotGiaTri = layCotGiaTri(banGhi.getLoaiNghiepVu());
        String sql = "UPDATE " + tenBang + " SET tieu_de = ?, " + cotGiaTri + " = ?, trang_thai = ?, mo_ta_chi_tiet = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, banGhi.getTieuDe());
            ps.setBigDecimal(2, parseGiaTri(banGhi.getGiaTri()));
            ps.setString(3, banGhi.getTrangThai());
            ps.setString(4, banGhi.getMoTaChiTiet());
            ps.setLong(5, banGhi.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private String layBieuThucTieuDe(LoaiNghiepVu loai) {
        if (loai == LoaiNghiepVu.KHACH_HANG) {
            return "COALESCE(t.tieu_de, t.ten_cong_ty, '')";
        }
        if (loai == LoaiNghiepVu.CO_HOI) {
            return "COALESCE(t.tieu_de, t.ten_co_hoi, '')";
        }
        return "t.tieu_de";
    }

    private String layBieuThucMaBanGhi(LoaiNghiepVu loai) {
        switch (loai) {
            case KHACH_HANG:
                return "COALESCE(t.ma_ban_ghi, CAST(t.id AS CHAR), '')";
            case CO_HOI:
                return "COALESCE(t.ma_ban_ghi, t.ma_co_hoi, CAST(t.id AS CHAR), '')";
            case BAO_GIA:
                return "COALESCE(t.ma_ban_ghi, t.ma_bao_gia, CAST(t.id AS CHAR), '')";
            default:
                return "COALESCE(t.ma_ban_ghi, CAST(t.id AS CHAR), '')";
        }
    }

    private String layBieuThucNguoiPhuTrach(LoaiNghiepVu loai) {
        if (loai == LoaiNghiepVu.KHACH_HANG) {
            return "COALESCE(t.nguoi_phu_trach_id, t.nguoi_so_huu_id, 0)";
        }
        return "t.nguoi_phu_trach_id";
    }
}
