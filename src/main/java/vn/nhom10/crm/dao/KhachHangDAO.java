package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object quản lý dữ liệu khách hàng (Story S3-07).
 * Hỗ trợ tìm kiếm và lọc đa điều kiện, phân quyền Data Scope, hiển thị số điện thoại liên hệ.
 */
public class KhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHangDAO.class.getName());

    /**
     * Tìm kiếm và lọc danh sách khách hàng theo nhiều điều kiện kết hợp Data Scope.
     * Đáp ứng AC1 & AC2:
     * - Lọc: trạng thái, ngành nghề, quy mô, khu vực, người sở hữu
     * - Tìm: tên công ty, mã số thuế, số điện thoại người liên hệ
     */
    public List<KhachHang> timKiemVaLoc(NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        List<KhachHang> danhSach = new ArrayList<>();
        List<Object> thamSo = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
                "kh.nganh_nghe_id, nn.ten_nganh AS ten_nganh_nghe, " +
                "kh.quy_mo_id, qm.ten_quy_mo AS ten_quy_mo, " +
                "kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
                "kh.khu_vuc_id, kv.ten_khu_vuc AS ten_khu_vuc, " +
                "kh.nguoi_so_huu_id, nd.ho_ten AS ten_nguoi_so_huu, " +
                "kh.nhom_kinh_doanh_id, nkd.ten_nhom AS ten_nhom_kinh_doanh, " +
                "kh.doanh_thu_uoc_tinh, kh.cong_ty_me_id, ctm.ten_cong_ty AS ten_cong_ty_me, " +
                "kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, kh.lan_tuong_tac_cuoi, " +
                "kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, kh.ngay_tao, kh.created_at, kh.updated_at, " +
                "nlh.ho_ten AS ten_nguoi_lien_he_chinh, " +
                "nlh.so_dien_thoai AS so_dien_thoai_lien_he, " +
                "nlh.email AS email_lien_he " +
                "FROM khach_hang kh " +
                "LEFT JOIN nganh_nghe nn ON kh.nganh_nghe_id = nn.id " +
                "LEFT JOIN quy_mo_doanh_nghiep qm ON kh.quy_mo_id = qm.id " +
                "LEFT JOIN khu_vuc_dia_ly kv ON kh.khu_vuc_id = kv.id " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "LEFT JOIN khach_hang ctm ON kh.cong_ty_me_id = ctm.id " +
                "LEFT JOIN ( " +
                "    SELECT nlh1.khach_hang_id, nlh1.ho_ten, nlh1.so_dien_thoai, nlh1.email " +
                "    FROM nguoi_lien_he nlh1 " +
                "    INNER JOIN ( " +
                "        SELECT khach_hang_id, MAX(id) AS max_nlh_id " +
                "        FROM nguoi_lien_he " +
                "        WHERE trang_thai = 'DANG_HOAT_DONG' " +
                "        GROUP BY khach_hang_id " +
                "    ) latest ON nlh1.id = latest.max_nlh_id " +
                ") nlh ON kh.id = nlh.khach_hang_id " +
                "WHERE (kh.gop_vao_khach_hang_id IS NULL) "
        );

        apDungDieuKienLoc(sql, thamSo, user, boLoc);

        sql.append(" ORDER BY kh.created_at DESC, kh.id DESC");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToModel(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm kiếm và lọc khách hàng: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Đếm tổng số lượng khách hàng thỏa mãn tiêu chí lọc và Data Scope.
     */
    public int demSoLuong(NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT kh.id) " +
                "FROM khach_hang kh " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "WHERE (kh.gop_vao_khach_hang_id IS NULL) "
        );

        apDungDieuKienLoc(sql, thamSo, user, boLoc);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng khách hàng: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Tìm khách hàng theo ID kèm thông tin liên hệ chính.
     */
    public KhachHang timTheoId(long id) {
        String sql = "SELECT kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
                "kh.nganh_nghe_id, nn.ten_nganh AS ten_nganh_nghe, " +
                "kh.quy_mo_id, qm.ten_quy_mo AS ten_quy_mo, " +
                "kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
                "kh.khu_vuc_id, kv.ten_khu_vuc AS ten_khu_vuc, " +
                "kh.nguoi_so_huu_id, nd.ho_ten AS ten_nguoi_so_huu, " +
                "kh.nhom_kinh_doanh_id, nkd.ten_nhom AS ten_nhom_kinh_doanh, " +
                "kh.doanh_thu_uoc_tinh, kh.cong_ty_me_id, ctm.ten_cong_ty AS ten_cong_ty_me, " +
                "kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, kh.lan_tuong_tac_cuoi, " +
                "kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, kh.ngay_tao, kh.created_at, kh.updated_at, " +
                "nlh.ho_ten AS ten_nguoi_lien_he_chinh, " +
                "nlh.so_dien_thoai AS so_dien_thoai_lien_he, " +
                "nlh.email AS email_lien_he " +
                "FROM khach_hang kh " +
                "LEFT JOIN nganh_nghe nn ON kh.nganh_nghe_id = nn.id " +
                "LEFT JOIN quy_mo_doanh_nghiep qm ON kh.quy_mo_id = qm.id " +
                "LEFT JOIN khu_vuc_dia_ly kv ON kh.khu_vuc_id = kv.id " +
                "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
                "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
                "LEFT JOIN khach_hang ctm ON kh.cong_ty_me_id = ctm.id " +
                "LEFT JOIN ( " +
                "    SELECT nlh1.khach_hang_id, nlh1.ho_ten, nlh1.so_dien_thoai, nlh1.email " +
                "    FROM nguoi_lien_he nlh1 " +
                "    INNER JOIN ( " +
                "        SELECT khach_hang_id, MAX(id) AS max_nlh_id " +
                "        FROM nguoi_lien_he " +
                "        WHERE trang_thai = 'DANG_HOAT_DONG' " +
                "        GROUP BY khach_hang_id " +
                "    ) latest ON nlh1.id = latest.max_nlh_id " +
                ") nlh ON kh.id = nlh.khach_hang_id " +
                "WHERE kh.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm khách hàng theo ID=" + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Thêm mới khách hàng vào database.
     */
    public long themKhachHang(KhachHang kh) throws SQLException {
        if (kh == null || kh.getTenCongTy() == null || kh.getTenCongTy().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên công ty / khách hàng không được để trống.");
        }

        String sql = "INSERT INTO khach_hang (" +
                "ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, " +
                "website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                "doanh_thu_uoc_tinh, cong_ty_me_id, trang_thai, mo_ta_chi_tiet, ngay_tao, created_at, updated_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setKhachHangParams(ps, kh);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1L;
    }

    /**
     * Cập nhật thông tin khách hàng.
     */
    public boolean capNhatKhachHang(KhachHang kh) throws SQLException {
        if (kh == null || kh.getId() == null) {
            return false;
        }

        String sql = "UPDATE khach_hang SET " +
                "ma_khach_hang = ?, ten_cong_ty = ?, ten_chuan_hoa = ?, ma_so_thue = ?, " +
                "nganh_nghe_id = ?, quy_mo_id = ?, website = ?, website_chuan_hoa = ?, " +
                "dia_chi = ?, khu_vuc_id = ?, nguoi_so_huu_id = ?, nhom_kinh_doanh_id = ?, " +
                "doanh_thu_uoc_tinh = ?, cong_ty_me_id = ?, trang_thai = ?, mo_ta_chi_tiet = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, kh.getMaKhachHang() != null ? kh.getMaKhachHang().trim() : null);
            ps.setString(idx++, kh.getTenCongTy() != null ? kh.getTenCongTy().trim() : null);
            ps.setString(idx++, chuanHoaTen(kh.getTenCongTy()));
            ps.setString(idx++, kh.getMaSoThue() != null ? kh.getMaSoThue().trim() : null);

            if (kh.getNganhNgheId() != null && kh.getNganhNgheId() > 0) ps.setLong(idx++, kh.getNganhNgheId());
            else ps.setNull(idx++, Types.BIGINT);

            if (kh.getQuyMoId() != null && kh.getQuyMoId() > 0) ps.setLong(idx++, kh.getQuyMoId());
            else ps.setNull(idx++, Types.BIGINT);

            ps.setString(idx++, kh.getWebsite() != null ? kh.getWebsite().trim() : null);
            ps.setString(idx++, chuanHoaWebsite(kh.getWebsite()));
            ps.setString(idx++, kh.getDiaChi() != null ? kh.getDiaChi().trim() : null);

            if (kh.getKhuVucId() != null && kh.getKhuVucId() > 0) ps.setLong(idx++, kh.getKhuVucId());
            else ps.setNull(idx++, Types.BIGINT);

            ps.setLong(idx++, kh.getNguoiSoHuuId() != null ? kh.getNguoiSoHuuId() : -1L);

            if (kh.getNhomKinhDoanhId() != null && kh.getNhomKinhDoanhId() > 0) ps.setLong(idx++, kh.getNhomKinhDoanhId());
            else ps.setNull(idx++, Types.BIGINT);

            ps.setBigDecimal(idx++, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);

            if (kh.getCongTyMeId() != null && kh.getCongTyMeId() > 0) ps.setLong(idx++, kh.getCongTyMeId());
            else ps.setNull(idx++, Types.BIGINT);

            ps.setString(idx++, kh.getTrangThai() != null ? kh.getTrangThai() : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
            ps.setString(idx++, kh.getMoTaChiTiet() != null ? kh.getMoTaChiTiet().trim() : null);
            ps.setLong(idx++, kh.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean kiemTraTonTaiMaKhachHang(String maKhachHang, Long excludeId) {
        if (maKhachHang == null || maKhachHang.isBlank()) return false;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM khach_hang WHERE LOWER(ma_khach_hang) = LOWER(?)");
        if (excludeId != null && excludeId > 0) sql.append(" AND id <> ?");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maKhachHang.trim());
            if (excludeId != null && excludeId > 0) ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã khách hàng: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean kiemTraTonTaiMaSoThue(String maSoThue, Long excludeId) {
        if (maSoThue == null || maSoThue.isBlank()) return false;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM khach_hang WHERE LOWER(ma_so_thue) = LOWER(?)");
        if (excludeId != null && excludeId > 0) sql.append(" AND id <> ?");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maSoThue.trim());
            if (excludeId != null && excludeId > 0) ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã số thuế: " + e.getMessage(), e);
        }
        return false;
    }

    // Helper: Áp dụng điều kiện lọc và phân quyền Data Scope
    private void apDungDieuKienLoc(StringBuilder sql, List<Object> params, NguoiDungDTO user, BoLocKhachHangDTO boLoc) {
        // 1. Phân quyền Data Scope (Server-side Enforcement)
        if (user != null) {
            PhamViDuLieu phamVi = (boLoc != null && boLoc.getPhamVi() != null)
                    ? boLoc.getPhamVi()
                    : user.getPhamViHienTai();

            PhamViDuLieu maxScope = user.getPhamViToiDa() != null ? user.getPhamViToiDa() : PhamViDuLieu.CA_NHAN;
            if (phamVi == null || maxScope == PhamViDuLieu.CA_NHAN || phamVi == PhamViDuLieu.CA_NHAN) {
                sql.append(" AND kh.nguoi_so_huu_id = ?");
                params.add(user.getId() != null ? user.getId() : -1L);
            } else if (maxScope == PhamViDuLieu.NHOM || phamVi == PhamViDuLieu.NHOM) {
                Long nhomId = user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : -1L;
                sql.append(" AND (kh.nhom_kinh_doanh_id = ? OR kh.nguoi_so_huu_id IN (SELECT id FROM nguoi_dung WHERE nhom_kinh_doanh_id = ?))");
                params.add(nhomId);
                params.add(nhomId);
            }
            // TOAN_BO: Không áp điều kiện sở hữu
        }

        if (boLoc == null) {
            return;
        }

        // 2. AC1: Lọc theo trạng thái
        if (boLoc.getTrangThai() != null && !boLoc.getTrangThai().isBlank()) {
            TrangThaiKhachHangEnum en = TrangThaiKhachHangEnum.tuChuoi(boLoc.getTrangThai());
            if (en != null) {
                sql.append(" AND (kh.trang_thai = ? OR kh.trang_thai = ?)");
                params.add(en.getMa());
                params.add(en.getTenHienThi());
            } else {
                sql.append(" AND kh.trang_thai = ?");
                params.add(boLoc.getTrangThai().trim());
            }
        }

        // 3. AC1: Lọc theo ngành nghề
        if (boLoc.getNganhNgheId() != null && boLoc.getNganhNgheId() > 0) {
            sql.append(" AND kh.nganh_nghe_id = ?");
            params.add(boLoc.getNganhNgheId());
        }

        // 4. AC1: Lọc theo quy mô doanh nghiệp
        if (boLoc.getQuyMoId() != null && boLoc.getQuyMoId() > 0) {
            sql.append(" AND kh.quy_mo_id = ?");
            params.add(boLoc.getQuyMoId());
        }

        // 5. AC1: Lọc theo khu vực
        if (boLoc.getKhuVucId() != null && boLoc.getKhuVucId() > 0) {
            sql.append(" AND kh.khu_vuc_id = ?");
            params.add(boLoc.getKhuVucId());
        }

        // 6. AC1: Lọc theo người sở hữu
        if (boLoc.getNguoiSoHuuId() != null && boLoc.getNguoiSoHuuId() > 0) {
            sql.append(" AND kh.nguoi_so_huu_id = ?");
            params.add(boLoc.getNguoiSoHuuId());
        }

        // 7. AC2: Tìm theo tên công ty / tên khách hàng
        if (boLoc.getTenCongTy() != null && !boLoc.getTenCongTy().isBlank()) {
            String p = "%" + boLoc.getTenCongTy().trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(kh.ten_cong_ty) LIKE ? OR LOWER(COALESCE(kh.ten_chuan_hoa, '')) LIKE ?)");
            params.add(p);
            params.add(p);
        }

        // 8. AC2: Tìm theo mã số thuế
        if (boLoc.getMaSoThue() != null && !boLoc.getMaSoThue().isBlank()) {
            sql.append(" AND LOWER(COALESCE(kh.ma_so_thue, '')) LIKE ?");
            params.add("%" + boLoc.getMaSoThue().trim().toLowerCase() + "%");
        }

        // 9. AC2: Tìm theo số điện thoại người liên hệ
        if (boLoc.getSoDienThoai() != null && !boLoc.getSoDienThoai().isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM nguoi_lien_he nlh_sdt WHERE nlh_sdt.khach_hang_id = kh.id AND nlh_sdt.so_dien_thoai LIKE ?)");
            params.add("%" + boLoc.getSoDienThoai().trim() + "%");
        }

        // 10. Tìm kiếm tổng hợp theo từ khóa chung (Tên, MST, Mã KH, SĐT/Tên liên hệ)
        if (boLoc.getTuKhoa() != null && !boLoc.getTuKhoa().isBlank()) {
            String kw = "%" + boLoc.getTuKhoa().trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(kh.ten_cong_ty) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ten_chuan_hoa, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_so_thue, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_khach_hang, '')) LIKE ? ")
               .append("OR EXISTS (SELECT 1 FROM nguoi_lien_he nlh_kw WHERE nlh_kw.khach_hang_id = kh.id AND (nlh_kw.so_dien_thoai LIKE ? OR LOWER(nlh_kw.ho_ten) LIKE ?)))");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add("%" + boLoc.getTuKhoa().trim() + "%");
            params.add(kw);
        }
    }

    private void setKhachHangParams(PreparedStatement ps, KhachHang kh) throws SQLException {
        int idx = 1;
        ps.setString(idx++, kh.getMaKhachHang() != null ? kh.getMaKhachHang().trim() : null);
        ps.setString(idx++, kh.getTenCongTy() != null ? kh.getTenCongTy().trim() : null);
        ps.setString(idx++, chuanHoaTen(kh.getTenCongTy()));
        ps.setString(idx++, kh.getMaSoThue() != null ? kh.getMaSoThue().trim() : null);

        if (kh.getNganhNgheId() != null && kh.getNganhNgheId() > 0) ps.setLong(idx++, kh.getNganhNgheId());
        else ps.setNull(idx++, Types.BIGINT);

        if (kh.getQuyMoId() != null && kh.getQuyMoId() > 0) ps.setLong(idx++, kh.getQuyMoId());
        else ps.setNull(idx++, Types.BIGINT);

        ps.setString(idx++, kh.getWebsite() != null ? kh.getWebsite().trim() : null);
        ps.setString(idx++, chuanHoaWebsite(kh.getWebsite()));
        ps.setString(idx++, kh.getDiaChi() != null ? kh.getDiaChi().trim() : null);

        if (kh.getKhuVucId() != null && kh.getKhuVucId() > 0) ps.setLong(idx++, kh.getKhuVucId());
        else ps.setNull(idx++, Types.BIGINT);

        ps.setLong(idx++, kh.getNguoiSoHuuId() != null ? kh.getNguoiSoHuuId() : -1L);

        if (kh.getNhomKinhDoanhId() != null && kh.getNhomKinhDoanhId() > 0) ps.setLong(idx++, kh.getNhomKinhDoanhId());
        else ps.setNull(idx++, Types.BIGINT);

        ps.setBigDecimal(idx++, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);

        if (kh.getCongTyMeId() != null && kh.getCongTyMeId() > 0) ps.setLong(idx++, kh.getCongTyMeId());
        else ps.setNull(idx++, Types.BIGINT);

        ps.setString(idx++, kh.getTrangThai() != null ? kh.getTrangThai() : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        ps.setString(idx++, kh.getMoTaChiTiet() != null ? kh.getMoTaChiTiet().trim() : null);
        ps.setDate(idx++, Date.valueOf(kh.getNgayTao() != null ? kh.getNgayTao() : LocalDate.now()));
    }

    private KhachHang mapResultSetToModel(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setId(rs.getLong("id"));
        kh.setMaKhachHang(rs.getString("ma_khach_hang"));
        kh.setTenCongTy(rs.getString("ten_cong_ty"));
        kh.setTenChuanHoa(rs.getString("ten_chuan_hoa"));
        kh.setMaSoThue(rs.getString("ma_so_thue"));

        long nnId = rs.getLong("nganh_nghe_id");
        if (!rs.wasNull()) kh.setNganhNgheId(nnId);
        kh.setTenNganhNghe(rs.getString("ten_nganh_nghe"));

        long qmId = rs.getLong("quy_mo_id");
        if (!rs.wasNull()) kh.setQuyMoId(qmId);
        kh.setTenQuyMo(rs.getString("ten_quy_mo"));

        kh.setWebsite(rs.getString("website"));
        kh.setWebsiteChuanHoa(rs.getString("website_chuan_hoa"));
        kh.setDiaChi(rs.getString("dia_chi"));

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) kh.setKhuVucId(kvId);
        kh.setTenKhuVuc(rs.getString("ten_khu_vuc"));

        long nshId = rs.getLong("nguoi_so_huu_id");
        if (!rs.wasNull()) kh.setNguoiSoHuuId(nshId);
        kh.setTenNguoiSoHuu(rs.getString("ten_nguoi_so_huu"));

        long nkdId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) kh.setNhomKinhDoanhId(nkdId);
        kh.setTenNhomKinhDoanh(rs.getString("ten_nhom_kinh_doanh"));

        kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));

        long ctmId = rs.getLong("cong_ty_me_id");
        if (!rs.wasNull()) kh.setCongTyMeId(ctmId);
        kh.setTenCongTyMe(rs.getString("ten_cong_ty_me"));

        kh.setTrangThai(rs.getString("trang_thai"));
        kh.setCoRuiRo(rs.getBoolean("co_rui_ro"));
        kh.setRuiRoCapNhatLuc(rs.getTimestamp("rui_ro_cap_nhat_luc"));
        kh.setLanTuongTacCuoi(rs.getTimestamp("lan_tuong_tac_cuoi"));

        long gopId = rs.getLong("gop_vao_khach_hang_id");
        if (!rs.wasNull()) kh.setGopVaoKhachHangId(gopId);

        kh.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));

        Date d = rs.getDate("ngay_tao");
        if (d != null) kh.setNgayTao(d.toLocalDate());

        kh.setCreatedAt(rs.getTimestamp("created_at"));
        kh.setUpdatedAt(rs.getTimestamp("updated_at"));

        kh.setTenNguoiLienHeChinh(rs.getString("ten_nguoi_lien_he_chinh"));
        kh.setSoDienThoaiLienHe(rs.getString("so_dien_thoai_lien_he"));
        kh.setEmailLienHe(rs.getString("email_lien_he"));

        return kh;
    }

    private String chuanHoaTen(String ten) {
        if (ten == null) return null;
        return ten.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String chuanHoaWebsite(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        String clean = url.trim().toLowerCase();
        clean = clean.replaceFirst("^https?://", "");
        clean = clean.replaceFirst("^www\\.", "");
        if (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean;
    }
}
