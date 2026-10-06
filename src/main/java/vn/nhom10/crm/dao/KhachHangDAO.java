package vn.nhom10.crm.dao;

import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý các thao tác dữ liệu với bảng `khach_hang` (Story S3-01).
 * Tuân thủ nghiêm ngặt CODING_RULES.md và DATABASE_RULES.md:
 * - PreparedStatement chống SQL Injection.
 * - Danh sách cột tường minh (không SELECT *).
 * - Kiểm tra duy nhất mã số thuế (AC2).
 * - Hỗ trợ lọc Data Scope chuẩn: Cá nhân, Nhóm, Toàn bộ (AC4).
 */
public class KhachHangDAO {

    private static final Logger LOGGER = Logger.getLogger(KhachHangDAO.class.getName());

    private static final String COT_SELECT_DAY_DU =
            "kh.id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.ma_so_thue, " +
            "kh.nganh_nghe_id, nn.ten_nganh AS ten_nganh_nghe, " +
            "kh.quy_mo_id, qm.ten_quy_mo AS ten_quy_mo, " +
            "kh.website, kh.website_chuan_hoa, kh.dia_chi, " +
            "kh.khu_vuc_id, kv.ten_khu_vuc AS ten_khu_vuc, " +
            "kh.nguoi_so_huu_id, nd.ho_ten AS ten_nguoi_so_huu, " +
            "kh.nhom_kinh_doanh_id, nkd.ten_nhom AS ten_nhom_kinh_doanh, " +
            "kh.doanh_thu_uoc_tinh, kh.cong_ty_me_id, ctm.ten_cong_ty AS ten_cong_ty_me, " +
            "kh.trang_thai, kh.co_rui_ro, kh.rui_ro_cap_nhat_luc, kh.lan_tuong_tac_cuoi, " +
            "kh.gop_vao_khach_hang_id, kh.mo_ta_chi_tiet, kh.ngay_tao, kh.created_at, kh.updated_at";

    private static final String BANG_JOIN_CO_BAN =
            "FROM khach_hang kh " +
            "LEFT JOIN nguoi_dung nd ON kh.nguoi_so_huu_id = nd.id " +
            "LEFT JOIN nhom_kinh_doanh nkd ON kh.nhom_kinh_doanh_id = nkd.id " +
            "LEFT JOIN nganh_nghe nn ON kh.nganh_nghe_id = nn.id " +
            "LEFT JOIN quy_mo_doanh_nghiep qm ON kh.quy_mo_id = qm.id " +
            "LEFT JOIN khu_vuc_dia_ly kv ON kh.khu_vuc_id = kv.id " +
            "LEFT JOIN khach_hang ctm ON kh.cong_ty_me_id = ctm.id";

    /**
     * Thêm mới một khách hàng vào cơ sở dữ liệu.
     * Tự động sinh mã khách hàng nếu chưa có.
     * @return ID bản ghi vừa tạo hoặc null nếu thất bại.
     */
    public Long themKhachHang(KhachHang kh) throws SQLException {
        if (kh == null) {
            return null;
        }

        if (kh.getMaKhachHang() == null || kh.getMaKhachHang().trim().isEmpty()) {
            kh.setMaKhachHang(sinhMaKhachHang());
        }

        String sql = "INSERT INTO khach_hang (" +
                "ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, " +
                "website, website_chuan_hoa, dia_chi, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, " +
                "doanh_thu_uoc_tinh, trang_thai, mo_ta_chi_tiet, ngay_tao" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            datThamSoLuu(ps, kh);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        kh.setId(id);
                        return id;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật thông tin khách hàng doanh nghiệp.
     */
    public boolean capNhatKhachHang(KhachHang kh) throws SQLException {
        if (kh == null || kh.getId() == null) {
            return false;
        }

        String sql = "UPDATE khach_hang SET " +
                "ten_cong_ty = ?, ten_chuan_hoa = ?, ma_so_thue = ?, nganh_nghe_id = ?, quy_mo_id = ?, " +
                "website = ?, website_chuan_hoa = ?, dia_chi = ?, khu_vuc_id = ?, nguoi_so_huu_id = ?, " +
                "nhom_kinh_doanh_id = ?, doanh_thu_uoc_tinh = ?, trang_thai = ?, mo_ta_chi_tiet = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, kh.getTenCongTy() != null ? kh.getTenCongTy().trim() : "");
            ps.setString(idx++, kh.getTenChuanHoa() != null ? kh.getTenChuanHoa() : chuanHoaTen(kh.getTenCongTy()));

            if (kh.getMaSoThue() != null && !kh.getMaSoThue().trim().isEmpty()) {
                ps.setString(idx++, kh.getMaSoThue().trim());
            } else {
                ps.setNull(idx++, Types.VARCHAR);
            }

            if (kh.getNganhNgheId() != null && kh.getNganhNgheId() > 0) {
                ps.setLong(idx++, kh.getNganhNgheId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            if (kh.getQuyMoId() != null && kh.getQuyMoId() > 0) {
                ps.setLong(idx++, kh.getQuyMoId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            ps.setString(idx++, kh.getWebsite() != null ? kh.getWebsite().trim() : null);
            ps.setString(idx++, kh.getWebsiteChuanHoa() != null ? kh.getWebsiteChuanHoa() : chuanHoaWebsite(kh.getWebsite()));
            ps.setString(idx++, kh.getDiaChi() != null ? kh.getDiaChi().trim() : null);

            if (kh.getKhuVucId() != null && kh.getKhuVucId() > 0) {
                ps.setLong(idx++, kh.getKhuVucId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            ps.setLong(idx++, kh.getNguoiSoHuuId());

            if (kh.getNhomKinhDoanhId() != null && kh.getNhomKinhDoanhId() > 0) {
                ps.setLong(idx++, kh.getNhomKinhDoanhId());
            } else {
                ps.setNull(idx++, Types.BIGINT);
            }

            ps.setBigDecimal(idx++, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);
            ps.setString(idx++, kh.getTrangThai() != null ? kh.getTrangThai() : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
            ps.setString(idx++, kh.getMoTaChiTiet() != null ? kh.getMoTaChiTiet().trim() : null);
            ps.setLong(idx++, kh.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tìm một khách hàng theo ID kèm đầy đủ thông tin liên kết.
     */
    public KhachHang timTheoId(Long id) throws SQLException {
        if (id == null) {
            return null;
        }

        String sql = "SELECT " + COT_SELECT_DAY_DU + " " + BANG_JOIN_CO_BAN + " WHERE kh.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm khách hàng theo mã khách hàng.
     */
    public KhachHang timTheoMa(String maKhachHang) throws SQLException {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT " + COT_SELECT_DAY_DU + " " + BANG_JOIN_CO_BAN + " WHERE LOWER(kh.ma_khach_hang) = LOWER(?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToModel(rs);
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra tính duy nhất của mã số thuế (AC2: Mã số thuế nếu có thì phải là duy nhất).
     * @param maSoThue Mã số thuế cần kiểm tra
     * @param excludeId ID khách hàng hiện tại cần loại trừ (khi sửa), hoặc null (khi tạo mới)
     * @return true nếu mã số thuế đã bị khách hàng khác chiếm dụng; false nếu chưa có ai dùng.
     */
    public boolean kiemTraTrungMaSoThue(String maSoThue, Long excludeId) throws SQLException {
        if (maSoThue == null || maSoThue.trim().isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT id FROM khach_hang WHERE LOWER(TRIM(ma_so_thue)) = LOWER(?)");
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }
        sql.append(" LIMIT 1");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maSoThue.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Lấy tên công ty đang sở hữu mã số thuế trùng (phục vụ thông báo lỗi chi tiết).
     */
    public String layTenCongTyTheoMaSoThue(String maSoThue, Long excludeId) throws SQLException {
        if (maSoThue == null || maSoThue.trim().isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ten_cong_ty FROM khach_hang WHERE LOWER(TRIM(ma_so_thue)) = LOWER(?)");
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }
        sql.append(" LIMIT 1");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, maSoThue.trim());
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ten_cong_ty");
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra trùng lặp mã khách hàng.
     */
    public boolean kiemTraTrungMaKhachHang(String maKhachHang, Long excludeId) throws SQLException {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT id FROM khach_hang WHERE LOWER(TRIM(ma_khach_hang)) = LOWER(?)");
        if (excludeId != null) {
            sql.append(" AND id != ?");
        }
        sql.append(" LIMIT 1");

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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
     * Lấy danh sách khách hàng doanh nghiệp theo Data Scope và các bộ lọc nghiệp vụ (AC1, AC3, AC4).
     */
    public List<KhachHang> layDanhSach(Long userId,
                                       Set<Long> dsNhomIds,
                                       PhamViDuLieu phamVi,
                                       String tuKhoa,
                                       String trangThai,
                                       Long nganhNgheId,
                                       Long quyMoId,
                                       int offset,
                                       int limit) throws SQLException {
        List<KhachHang> ketQua = new ArrayList<>();
        List<Object> thamSo = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(COT_SELECT_DAY_DU).append(" ").append(BANG_JOIN_CO_BAN).append(" WHERE 1=1 ");

        apDungDieuKienLoc(sql, thamSo, userId, dsNhomIds, phamVi, tuKhoa, trangThai, nganhNgheId, quyMoId);

        sql.append(" ORDER BY kh.created_at DESC, kh.id DESC");

        if (limit > 0) {
            sql.append(" LIMIT ? OFFSET ?");
            thamSo.add(limit);
            thamSo.add(Math.max(0, offset));
        }

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapResultSetToModel(rs));
                }
            }
        }
        return ketQua;
    }

    /**
     * Đếm tổng số khách hàng thỏa mãn điều kiện lọc và Data Scope.
     */
    public int demTongSo(Long userId,
                         Set<Long> dsNhomIds,
                         PhamViDuLieu phamVi,
                         String tuKhoa,
                         String trangThai,
                         Long nganhNgheId,
                         Long quyMoId) throws SQLException {
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total ").append(BANG_JOIN_CO_BAN).append(" WHERE 1=1 ");

        apDungDieuKienLoc(sql, thamSo, userId, dsNhomIds, phamVi, tuKhoa, trangThai, nganhNgheId, quyMoId);

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Tự động sinh mã khách hàng tiếp theo có dạng KH000001.
     */
    public String sinhMaKhachHang() {
        String sql = "SELECT MAX(id) AS max_id FROM khach_hang";
        long tiepTheo = 1;
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                tiepTheo = rs.getLong("max_id") + 1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi lấy max id khách hàng, dùng timestamp: " + e.getMessage());
            tiepTheo = System.currentTimeMillis() % 1000000;
        }
        return String.format("KH%06d", tiepTheo);
    }

    private void datThamSoLuu(PreparedStatement ps, KhachHang kh) throws SQLException {
        int idx = 1;
        ps.setString(idx++, kh.getMaKhachHang() != null ? kh.getMaKhachHang().trim() : sinhMaKhachHang());
        ps.setString(idx++, kh.getTenCongTy() != null ? kh.getTenCongTy().trim() : "");
        ps.setString(idx++, kh.getTenChuanHoa() != null ? kh.getTenChuanHoa() : chuanHoaTen(kh.getTenCongTy()));

        if (kh.getMaSoThue() != null && !kh.getMaSoThue().trim().isEmpty()) {
            ps.setString(idx++, kh.getMaSoThue().trim());
        } else {
            ps.setNull(idx++, Types.VARCHAR);
        }

        if (kh.getNganhNgheId() != null && kh.getNganhNgheId() > 0) {
            ps.setLong(idx++, kh.getNganhNgheId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        if (kh.getQuyMoId() != null && kh.getQuyMoId() > 0) {
            ps.setLong(idx++, kh.getQuyMoId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setString(idx++, kh.getWebsite() != null ? kh.getWebsite().trim() : null);
        ps.setString(idx++, kh.getWebsiteChuanHoa() != null ? kh.getWebsiteChuanHoa() : chuanHoaWebsite(kh.getWebsite()));
        ps.setString(idx++, kh.getDiaChi() != null ? kh.getDiaChi().trim() : null);

        if (kh.getKhuVucId() != null && kh.getKhuVucId() > 0) {
            ps.setLong(idx++, kh.getKhuVucId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setLong(idx++, kh.getNguoiSoHuuId() != null ? kh.getNguoiSoHuuId() : -1L);

        if (kh.getNhomKinhDoanhId() != null && kh.getNhomKinhDoanhId() > 0) {
            ps.setLong(idx++, kh.getNhomKinhDoanhId());
        } else {
            ps.setNull(idx++, Types.BIGINT);
        }

        ps.setBigDecimal(idx++, kh.getDoanhThuUocTinh() != null ? kh.getDoanhThuUocTinh() : BigDecimal.ZERO);
        ps.setString(idx++, kh.getTrangThai() != null ? kh.getTrangThai() : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        ps.setString(idx++, kh.getMoTaChiTiet() != null ? kh.getMoTaChiTiet().trim() : null);
        ps.setDate(idx++, Date.valueOf(kh.getNgayTao() != null ? kh.getNgayTao() : LocalDate.now()));
    }

    private void apDungDieuKienLoc(StringBuilder sql,
                                  List<Object> thamSo,
                                  Long userId,
                                  Set<Long> dsNhomIds,
                                  PhamViDuLieu phamVi,
                                  String tuKhoa,
                                  String trangThai,
                                  Long nganhNgheId,
                                  Long quyMoId) {
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }

        // 1. Phân quyền Data Scope (AC4: Nhân viên chỉ thấy của mình; Trưởng nhóm thấy toàn nhóm)
        if (phamVi == PhamViDuLieu.CA_NHAN) {
            sql.append("AND kh.nguoi_so_huu_id = ? ");
            thamSo.add(userId != null ? userId : -1L);
        } else if (phamVi == PhamViDuLieu.NHOM) {
            if (dsNhomIds != null && !dsNhomIds.isEmpty()) {
                sql.append("AND (kh.nhom_kinh_doanh_id IN (");
                int i = 0;
                for (Long nId : dsNhomIds) {
                    if (i > 0) sql.append(", ");
                    sql.append("?");
                    thamSo.add(nId);
                    i++;
                }
                sql.append(") OR kh.nguoi_so_huu_id IN (SELECT id FROM nguoi_dung WHERE nhom_kinh_doanh_id IN (");
                i = 0;
                for (Long nId : dsNhomIds) {
                    if (i > 0) sql.append(", ");
                    sql.append("?");
                    thamSo.add(nId);
                    i++;
                }
                sql.append("))) ");
            } else {
                sql.append("AND kh.nhom_kinh_doanh_id = -1 ");
            }
        }
        // TOAN_BO: Không thêm điều kiện sở hữu

        // 2. Lọc theo từ khóa tìm kiếm
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(kh.ten_cong_ty) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_khach_hang, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.ma_so_thue, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.website, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(kh.dia_chi, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(nd.ho_ten, '')) LIKE ? ")
               .append("OR LOWER(COALESCE(nkd.ten_nhom, '')) LIKE ?) ");
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
        }

        // 3. Lọc theo trạng thái (AC3: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác)
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            TrangThaiKhachHangEnum en = TrangThaiKhachHangEnum.tuChuoi(trangThai);
            if (en != null) {
                sql.append("AND (kh.trang_thai = ? OR kh.trang_thai = ?) ");
                thamSo.add(en.getMa());
                thamSo.add(en.getTenHienThi());
            } else {
                sql.append("AND kh.trang_thai = ? ");
                thamSo.add(trangThai.trim());
            }
        }

        // 4. Lọc theo ngành nghề
        if (nganhNgheId != null && nganhNgheId > 0) {
            sql.append("AND kh.nganh_nghe_id = ? ");
            thamSo.add(nganhNgheId);
        }

        // 5. Lọc theo quy mô doanh nghiệp
        if (quyMoId != null && quyMoId > 0) {
            sql.append("AND kh.quy_mo_id = ? ");
            thamSo.add(quyMoId);
        }
    }

    private KhachHang mapResultSetToModel(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setId(rs.getLong("id"));
        kh.setMaKhachHang(rs.getString("ma_khach_hang"));
        kh.setTenCongTy(rs.getString("ten_cong_ty"));
        kh.setTenChuanHoa(rs.getString("ten_chuan_hoa"));
        kh.setMaSoThue(rs.getString("ma_so_thue"));

        long nnId = rs.getLong("nganh_nghe_id");
        if (!rs.wasNull()) {
            kh.setNganhNgheId(nnId);
        }
        kh.setTenNganhNghe(rs.getString("ten_nganh_nghe"));

        long qmId = rs.getLong("quy_mo_id");
        if (!rs.wasNull()) {
            kh.setQuyMoId(qmId);
        }
        kh.setTenQuyMo(rs.getString("ten_quy_mo"));

        kh.setWebsite(rs.getString("website"));
        kh.setWebsiteChuanHoa(rs.getString("website_chuan_hoa"));
        kh.setDiaChi(rs.getString("dia_chi"));

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) {
            kh.setKhuVucId(kvId);
        }
        kh.setTenKhuVuc(rs.getString("ten_khu_vuc"));

        long nshId = rs.getLong("nguoi_so_huu_id");
        if (!rs.wasNull()) {
            kh.setNguoiSoHuuId(nshId);
        }
        kh.setTenNguoiSoHuu(rs.getString("ten_nguoi_so_huu"));

        long nkdId = rs.getLong("nhom_kinh_doanh_id");
        if (!rs.wasNull()) {
            kh.setNhomKinhDoanhId(nkdId);
        }
        kh.setTenNhomKinhDoanh(rs.getString("ten_nhom_kinh_doanh"));

        kh.setDoanhThuUocTinh(rs.getBigDecimal("doanh_thu_uoc_tinh"));

        long ctmId = rs.getLong("cong_ty_me_id");
        if (!rs.wasNull()) {
            kh.setCongTyMeId(ctmId);
        }
        kh.setTenCongTyMe(rs.getString("ten_cong_ty_me"));

        kh.setTrangThai(rs.getString("trang_thai"));
        kh.setCoRuiRo(rs.getBoolean("co_rui_ro"));
        kh.setRuiRoCapNhatLuc(rs.getTimestamp("rui_ro_cap_nhat_luc"));
        kh.setLanTuongTacCuoi(rs.getTimestamp("lan_tuong_tac_cuoi"));

        long gopId = rs.getLong("gop_vao_khach_hang_id");
        if (!rs.wasNull()) {
            kh.setGopVaoKhachHangId(gopId);
        }

        kh.setMoTaChiTiet(rs.getString("mo_ta_chi_tiet"));

        Date d = rs.getDate("ngay_tao");
        if (d != null) {
            kh.setNgayTao(d.toLocalDate());
        }

        kh.setCreatedAt(rs.getTimestamp("created_at"));
        kh.setUpdatedAt(rs.getTimestamp("updated_at"));

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
