package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý các thao tác dữ liệu với bảng '`lead`' (Epic EP-04, Story S4-01).
 * Lưu ý: 'lead' là từ khóa của MySQL 8.x, bắt buộc dùng backtick: `lead`.
 * Tuân thủ DATABASE_RULES.md (PreparedStatement, SQL chuẩn MySQL 8.4 LTS, không dùng SELECT *).
 */
public class LeadDAO {

    private static final Logger LOGGER = Logger.getLogger(LeadDAO.class.getName());

    private static final String COT_SELECT_LEAD =
            "l.id, l.ma_lead, l.ho_ten, l.email, l.email_chuan_hoa, " +
            "l.so_dien_thoai, l.so_dien_thoai_chuan_hoa, l.ten_cong_ty, l.ten_cong_ty_chuan_hoa, " +
            "l.nhu_cau_quan_tam, l.muc_do_quan_tam, l.nganh_nghe_id, l.quy_mo_id, l.khu_vuc_id, " +
            "l.nguon_lead_id, nl.ten_nguon AS ten_nguon_lead, " +
            "l.chien_dich_id, cd.ten_chien_dich AS ten_chien_dich, " +
            "l.bieu_mau_id, bm.ten_bieu_mau AS ten_bieu_mau, " +
            "l.diem, l.phan_loai, l.trang_thai, " +
            "l.nguoi_phu_trach_id, nd.ho_ten AS ten_nguoi_phu_trach, " +
            "l.nhom_phan_bo_id, l.duoc_phan_bo_luc, l.han_phan_hoi_sla, l.duoc_nhan_luc, l.lien_he_dau_luc, " +
            "l.ly_do_tu_choi, l.qua_sla, l.khach_hang_chuyen_doi_id, l.nguoi_lien_he_chuyen_doi_id, " +
            "l.co_hoi_chuyen_doi_id, l.chuyen_doi_luc, l.gop_vao_lead_id, l.created_by, l.created_at, l.updated_at ";

    private static final String FROM_JOIN_LEAD =
            "FROM `lead` l " +
            "LEFT JOIN nguon_lead nl ON l.nguon_lead_id = nl.id " +
            "LEFT JOIN chien_dich cd ON l.chien_dich_id = cd.id " +
            "LEFT JOIN bieu_mau_lead bm ON l.bieu_mau_id = bm.id " +
            "LEFT JOIN nguoi_dung nd ON l.nguoi_phu_trach_id = nd.id ";

    /**
     * Thêm mới một Lead vào bảng `lead` qua Connection được truyền vào (phục vụ Transaction).
     */
    public Long them(Connection conn, Lead lead) throws SQLException {
        String sql = "INSERT INTO `lead` (" +
                "ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, " +
                "ten_cong_ty, ten_cong_ty_chuan_hoa, nhu_cau_quan_tam, muc_do_quan_tam, " +
                "nganh_nghe_id, quy_mo_id, khu_vuc_id, nguon_lead_id, chien_dich_id, bieu_mau_id, " +
                "diem, phan_loai, trang_thai, nguoi_phu_trach_id, nhom_phan_bo_id, " +
                "qua_sla, created_by, created_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            datThamSoLead(ps, lead);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    lead.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Thêm mới Lead độc lập không dùng transaction ngoài.
     */
    public Long them(Lead lead) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return them(conn, lead);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm Lead: " + lead.getMaLead(), e);
            throw new RuntimeException("Lỗi lưu Lead: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm Lead theo ID.
     */
    public Lead timTheoId(Long id) {
        if (id == null) return null;
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE l.id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangLead(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Lead theo ID: " + id, e);
        }
        return null;
    }

    /**
     * Tìm Lead theo mã định danh.
     */
    public Lead timTheoMa(String maLead) {
        if (maLead == null || maLead.trim().isEmpty()) return null;
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE l.ma_lead = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLead.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangLead(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Lead theo mã: " + maLead, e);
        }
        return null;
    }

    /**
     * Lấy danh sách Lead theo biểu mẫu.
     */
    public List<Lead> layDanhSachTheoBieuMau(Long bieuMauId, int limit) {
        List<Lead> danhSach = new ArrayList<>();
        if (bieuMauId == null) return danhSach;

        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "WHERE l.bieu_mau_id = ? ORDER BY l.created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bieuMauId);
            ps.setInt(2, limit > 0 ? limit : 50);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead theo biểu mẫu: " + bieuMauId, e);
        }
        return danhSach;
    }

    /**
     * AC2: Lấy danh sách Lead theo chiến dịch sinh ra chúng.
     */
    public List<Lead> layDanhSachTheoChienDich(Long chienDichId, int limit) {
        List<Lead> danhSach = new ArrayList<>();
        if (chienDichId == null) return danhSach;

        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "WHERE l.chien_dich_id = ? ORDER BY l.created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, chienDichId);
            ps.setInt(2, limit > 0 ? limit : 50);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead theo chiến dịch: " + chienDichId, e);
        }
        return danhSach;
    }

    /**
     * AC2: Cập nhật liên kết chiến dịch cho Lead.
     */
    public boolean capNhatChienDich(Long leadId, Long chienDichId) {
        if (leadId == null) return false;
        String sql = "UPDATE `lead` SET chien_dich_id = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (chienDichId != null) {
                ps.setLong(1, chienDichId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, leadId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật chiến dịch cho Lead: " + leadId, e);
        }
        return false;
    }

    /**
     * Lấy danh sách Lead tổng quan.
     */
    public List<Lead> layDanhSach(int limit, int offset) {
        List<Lead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "ORDER BY l.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 50);
            ps.setInt(2, Math.max(0, offset));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead", e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách Lead sắp xếp theo điểm số ưu tiên giảm dần (AC4 - Không tự loại lead điểm thấp).
     */
    public List<Lead> layDanhSachSapXepTheoDiem(int limit, int offset) {
        List<Lead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "ORDER BY l.diem DESC, l.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 50);
            ps.setInt(2, Math.max(0, offset));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead sắp xếp theo điểm", e);
        }
        return danhSach;
    }

    /**
     * Lấy tất cả Lead trong hệ thống (phục vụ tính lại điểm hàng loạt khi cấu hình thay đổi).
     */
    public List<Lead> layTatCaLead() {
        List<Lead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "ORDER BY l.id ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(mapResultSetSangLead(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy toàn bộ danh sách Lead", e);
        }
        return danhSach;
    }

    /**
     * Cập nhật điểm và phân loại của Lead theo transaction Connection truyền vào (AC2, AC3).
     */
    public boolean capNhatDiemVaPhanLoai(Connection conn, Long leadId, int diem, String phanLoai) throws SQLException {
        if (leadId == null) return false;
        String sql = "UPDATE `lead` SET diem = ?, phan_loai = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diem);
            ps.setString(2, phanLoai);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, leadId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật điểm và phân loại của Lead độc lập (AC2, AC3).
     */
    public boolean capNhatDiemVaPhanLoai(Long leadId, int diem, String phanLoai) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return capNhatDiemVaPhanLoai(conn, leadId, diem, phanLoai);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật điểm và phân loại Lead ID: " + leadId, e);
            return false;
        }
    }

    /**
     * Cập nhật đầy đủ thông tin Lead qua Connection truyền vào.
     * Chặn sửa bản ghi đã chuyển đổi DA_CHUYEN_DOI (Story S4-08 AC3).
     */
    public boolean capNhat(Connection conn, Lead lead) throws SQLException {
        if (lead == null || lead.getId() == null) return false;

        // Guard AC3: Chặn mọi chỉnh sửa nếu trạng thái hiện tại là DA_CHUYEN_DOI
        if (kiemTraDaChuyenDoi(conn, lead.getId())) {
            throw new IllegalStateException("Lead đã ở trạng thái Đã chuyển đổi, không được phép chỉnh sửa nữa.");
        }

        String sql = "UPDATE `lead` SET " +
                "ho_ten = ?, email = ?, email_chuan_hoa = ?, so_dien_thoai = ?, so_dien_thoai_chuan_hoa = ?, " +
                "ten_cong_ty = ?, ten_cong_ty_chuan_hoa = ?, nhu_cau_quan_tam = ?, muc_do_quan_tam = ?, " +
                "nganh_nghe_id = ?, quy_mo_id = ?, khu_vuc_id = ?, nguon_lead_id = ?, chien_dich_id = ?, bieu_mau_id = ?, " +
                "diem = ?, phan_loai = ?, trang_thai = ?, nguoi_phu_trach_id = ?, nhom_phan_bo_id = ?, " +
                "qua_sla = ?, updated_at = ? " +
                "WHERE id = ? AND trang_thai != 'DA_CHUYEN_DOI'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lead.getHoTen());
            ps.setString(2, lead.getEmail());
            ps.setString(3, lead.getEmailChuanHoa());
            ps.setString(4, lead.getSoDienThoai());
            ps.setString(5, lead.getSoDienThoaiChuanHoa());
            ps.setString(6, lead.getTenCongTy());
            ps.setString(7, lead.getTenCongTyChuanHoa());
            ps.setString(8, lead.getNhuCauQuanTam());
            ps.setString(9, lead.getMucDoQuanTam());

            if (lead.getNganhNgheId() != null) {
                ps.setLong(10, lead.getNganhNgheId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            if (lead.getQuyMoId() != null) {
                ps.setLong(11, lead.getQuyMoId());
            } else {
                ps.setNull(11, Types.BIGINT);
            }

            if (lead.getKhuVucId() != null) {
                ps.setLong(12, lead.getKhuVucId());
            } else {
                ps.setNull(12, Types.BIGINT);
            }

            ps.setLong(13, lead.getNguonLeadId());

            if (lead.getChienDichId() != null) {
                ps.setLong(14, lead.getChienDichId());
            } else {
                ps.setNull(14, Types.BIGINT);
            }

            if (lead.getBieuMauId() != null) {
                ps.setLong(15, lead.getBieuMauId());
            } else {
                ps.setNull(15, Types.BIGINT);
            }

            ps.setInt(16, lead.getDiem());
            ps.setString(17, lead.getPhanLoai());
            ps.setString(18, lead.getTrangThai() != null ? lead.getTrangThai().getMa() : TrangThaiLeadEnum.MOI.getMa());

            if (lead.getNguoiPhuTrachId() != null) {
                ps.setLong(19, lead.getNguoiPhuTrachId());
            } else {
                ps.setNull(19, Types.BIGINT);
            }

            if (lead.getNhomPhanBoId() != null) {
                ps.setLong(20, lead.getNhomPhanBoId());
            } else {
                ps.setNull(20, Types.BIGINT);
            }

            ps.setBoolean(21, lead.isQuaSla());
            LocalDateTime updatedAt = LocalDateTime.now();
            ps.setTimestamp(22, Timestamp.valueOf(updatedAt));
            ps.setLong(23, lead.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                lead.setUpdatedAt(updatedAt);
                return true;
            }
            return false;
        }
    }

    /**
     * Cập nhật thông tin Lead độc lập (kèm guard chặn sửa khi Lead đã chuyển đổi - Story S4-08 AC3).
     */
    public boolean capNhat(Lead lead) {
        if (lead == null || lead.getId() == null) return false;

        // Guard AC3: Chặn mọi chỉnh sửa nếu trạng thái hiện tại là DA_CHUYEN_DOI
        if (kiemTraDaChuyenDoi(lead.getId())) {
            throw new IllegalStateException("Lead đã ở trạng thái Đã chuyển đổi, không được phép chỉnh sửa nữa.");
        }

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return capNhat(conn, lead);
        } catch (IllegalStateException e) {
            throw e;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật Lead ID: " + lead.getId(), e);
            return false;
        }
    }

    /**
     * Kiểm tra trùng mã Lead.
     */
    public boolean kiemTraTonTaiMa(String maLead) {
        if (maLead == null || maLead.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(id) FROM `lead` WHERE ma_lead = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLead.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trùng mã Lead: " + maLead, e);
        }
        return false;
    }

    /**
     * Khóa dòng Lead bằng SELECT ... FOR UPDATE trong transaction (Story S4-08).
     * Ngăn chặn race condition khi hai người cùng bấm chuyển đổi hoặc chỉnh sửa đồng thời.
     */
    public Lead timTheoIdDeKhoa(Connection conn, Long id) throws SQLException {
        if (id == null) return null;
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE l.id = ? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangLead(rs);
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật chuyển đổi Lead thành công sang trạng thái DA_CHUYEN_DOI (Story S4-08 AC3).
     * Gắn liên kết tới Khách hàng, Người liên hệ, Cơ hội và ghi nhận thời điểm chuyển đổi.
     * @return true nếu cập nhật thành công 1 bản ghi, false nếu lead đã được chuyển đổi trước đó.
     */
    public boolean chuyenDoiLead(Connection conn, Long leadId, Long khachHangId, Long nlhId, Long coHoiId, LocalDateTime thoiDiem) throws SQLException {
        if (leadId == null) return false;
        String sql = "UPDATE `lead` SET " +
                "trang_thai = 'DA_CHUYEN_DOI', " +
                "khach_hang_chuyen_doi_id = ?, " +
                "nguoi_lien_he_chuyen_doi_id = ?, " +
                "co_hoi_chuyen_doi_id = ?, " +
                "chuyen_doi_luc = ?, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND trang_thai != 'DA_CHUYEN_DOI'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (khachHangId != null) {
                ps.setLong(1, khachHangId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            if (nlhId != null) {
                ps.setLong(2, nlhId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            if (coHoiId != null) {
                ps.setLong(3, coHoiId);
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setTimestamp(4, Timestamp.valueOf(thoiDiem != null ? thoiDiem : LocalDateTime.now()));
            ps.setLong(5, leadId);

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * Kiểm tra trạng thái Lead đã chuyển đổi chưa qua Connection truyền vào (Story S4-08 AC3).
     */
    public boolean kiemTraDaChuyenDoi(Connection conn, Long leadId) throws SQLException {
        if (leadId == null) return false;
        String sql = "SELECT trang_thai FROM `lead` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tt = rs.getString("trang_thai");
                    return "DA_CHUYEN_DOI".equalsIgnoreCase(tt);
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra trạng thái Lead đã chuyển đổi chưa (Story S4-08 AC3).
     */
    public boolean kiemTraDaChuyenDoi(Long leadId) {
        if (leadId == null) return false;
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return kiemTraDaChuyenDoi(conn, leadId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kiểm tra trạng thái chuyển đổi Lead: " + leadId, e);
            return false;
        }
    }

    /**
     * Lấy danh sách Lead chưa được phân bổ để tiến trình nền xử lý (Story S4-06, AC4).
     */
    public List<Lead> layDanhSachChuaPhanBo(int limit) {
        List<Lead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "WHERE l.trang_thai = 'MOI' AND l.nguoi_phu_trach_id IS NULL " +
                "ORDER BY l.created_at ASC LIMIT ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead chưa phân bổ: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách Lead trong hàng chờ phân bổ (Story S4-06, AC3).
     */
    public List<Lead> layDanhSachHangCho(int limit, int offset) {
        List<Lead> danhSach = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "WHERE l.trang_thai = 'HANG_CHO_PHAN_BO' " +
                "ORDER BY l.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 50);
            ps.setInt(2, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách Lead hàng chờ: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Đếm tổng số Lead trong hàng chờ phân bổ (Story S4-06, AC3).
     */
    public int demLeadHangCho() {
        String sql = "SELECT COUNT(id) FROM `lead` WHERE trang_thai = 'HANG_CHO_PHAN_BO'";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm số Lead trong hàng chờ: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Cập nhật thông tin phân bổ cho Lead trong transaction (Story S4-06).
     */
    public boolean capNhatPhanBo(Connection conn, Long leadId, Long nguoiPhuTrachId, Long nhomPhanBoId,
                                 TrangThaiLeadEnum trangThai, LocalDateTime duocPhanBoLuc, LocalDateTime hanPhanHoiSla) throws SQLException {
        String sql = "UPDATE `lead` SET " +
                "nguoi_phu_trach_id = ?, nhom_phan_bo_id = ?, trang_thai = ?, " +
                "duoc_phan_bo_luc = ?, han_phan_hoi_sla = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (nguoiPhuTrachId != null) {
                ps.setLong(1, nguoiPhuTrachId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }

            if (nhomPhanBoId != null) {
                ps.setLong(2, nhomPhanBoId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            ps.setString(3, trangThai != null ? trangThai.getMa() : TrangThaiLeadEnum.DA_PHAN_BO.getMa());

            if (duocPhanBoLuc != null) {
                ps.setTimestamp(4, Timestamp.valueOf(duocPhanBoLuc));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            if (hanPhanHoiSla != null) {
                ps.setTimestamp(5, Timestamp.valueOf(hanPhanHoiSla));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setLong(6, leadId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chuyển Lead vào hàng chờ phân bổ khi không khớp bất kỳ quy tắc nào (Story S4-06, AC3).
     */
    public boolean chuyenVaoHangCho(Connection conn, Long leadId) throws SQLException {
        String sql = "UPDATE `lead` SET " +
                "trang_thai = 'HANG_CHO_PHAN_BO', nguoi_phu_trach_id = NULL, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, leadId);
            return ps.executeUpdate() > 0;
        }
    }
    private void datThamSoLead(PreparedStatement ps, Lead lead) throws SQLException {
        ps.setString(1, lead.getMaLead());
        ps.setString(2, lead.getHoTen());
        ps.setString(3, lead.getEmail());
        ps.setString(4, lead.getEmailChuanHoa());
        ps.setString(5, lead.getSoDienThoai());
        ps.setString(6, lead.getSoDienThoaiChuanHoa());
        ps.setString(7, lead.getTenCongTy());
        ps.setString(8, lead.getTenCongTyChuanHoa());
        ps.setString(9, lead.getNhuCauQuanTam());
        ps.setString(10, lead.getMucDoQuanTam());

        if (lead.getNganhNgheId() != null) {
            ps.setLong(11, lead.getNganhNgheId());
        } else {
            ps.setNull(11, Types.BIGINT);
        }

        if (lead.getQuyMoId() != null) {
            ps.setLong(12, lead.getQuyMoId());
        } else {
            ps.setNull(12, Types.BIGINT);
        }

        if (lead.getKhuVucId() != null) {
            ps.setLong(13, lead.getKhuVucId());
        } else {
            ps.setNull(13, Types.BIGINT);
        }

        ps.setLong(14, lead.getNguonLeadId());

        if (lead.getChienDichId() != null) {
            ps.setLong(15, lead.getChienDichId());
        } else {
            ps.setNull(15, Types.BIGINT);
        }

        if (lead.getBieuMauId() != null) {
            ps.setLong(16, lead.getBieuMauId());
        } else {
            ps.setNull(16, Types.BIGINT);
        }

        ps.setInt(17, lead.getDiem());
        ps.setString(18, lead.getPhanLoai());
        ps.setString(19, lead.getTrangThai() != null ? lead.getTrangThai().getMa() : TrangThaiLeadEnum.MOI.getMa());

        if (lead.getNguoiPhuTrachId() != null) {
            ps.setLong(20, lead.getNguoiPhuTrachId());
        } else {
            ps.setNull(20, Types.BIGINT);
        }

        if (lead.getNhomPhanBoId() != null) {
            ps.setLong(21, lead.getNhomPhanBoId());
        } else {
            ps.setNull(21, Types.BIGINT);
        }

        ps.setBoolean(22, lead.isQuaSla());

        if (lead.getCreatedBy() != null) {
            ps.setLong(23, lead.getCreatedBy());
        } else {
            ps.setNull(23, Types.BIGINT);
        }

        LocalDateTime createdAt = lead.getCreatedAt() != null ? lead.getCreatedAt() : LocalDateTime.now();
        ps.setTimestamp(24, Timestamp.valueOf(createdAt));
    }

    private Lead mapResultSetSangLead(ResultSet rs) throws SQLException {
        Lead lead = new Lead();
        lead.setId(rs.getLong("id"));
        lead.setMaLead(rs.getString("ma_lead"));
        lead.setHoTen(rs.getString("ho_ten"));
        lead.setEmail(rs.getString("email"));
        lead.setEmailChuanHoa(rs.getString("email_chuan_hoa"));
        lead.setSoDienThoai(rs.getString("so_dien_thoai"));
        lead.setSoDienThoaiChuanHoa(rs.getString("so_dien_thoai_chuan_hoa"));
        lead.setTenCongTy(rs.getString("ten_cong_ty"));
        lead.setTenCongTyChuanHoa(rs.getString("ten_cong_ty_chuan_hoa"));
        lead.setNhuCauQuanTam(rs.getString("nhu_cau_quan_tam"));
        lead.setMucDoQuanTam(rs.getString("muc_do_quan_tam"));

        long nnId = rs.getLong("nganh_nghe_id");
        if (!rs.wasNull()) lead.setNganhNgheId(nnId);

        long qmId = rs.getLong("quy_mo_id");
        if (!rs.wasNull()) lead.setQuyMoId(qmId);

        long kvId = rs.getLong("khu_vuc_id");
        if (!rs.wasNull()) lead.setKhuVucId(kvId);

        lead.setNguonLeadId(rs.getLong("nguon_lead_id"));
        lead.setTenNguonLead(rs.getString("ten_nguon_lead"));

        long cdId = rs.getLong("chien_dich_id");
        if (!rs.wasNull()) {
            lead.setChienDichId(cdId);
            lead.setTenChienDich(rs.getString("ten_chien_dich"));
        }

        long bmId = rs.getLong("bieu_mau_id");
        if (!rs.wasNull()) {
            lead.setBieuMauId(bmId);
            lead.setTenBieuMau(rs.getString("ten_bieu_mau"));
        }

        lead.setDiem(rs.getInt("diem"));
        lead.setPhanLoai(rs.getString("phan_loai"));

        String tt = rs.getString("trang_thai");
        lead.setTrangThai(TrangThaiLeadEnum.tuChuoi(tt));

        long nptId = rs.getLong("nguoi_phu_trach_id");
        if (!rs.wasNull()) {
            lead.setNguoiPhuTrachId(nptId);
            lead.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
        }

        long npbId = rs.getLong("nhom_phan_bo_id");
        if (!rs.wasNull()) lead.setNhomPhanBoId(npbId);

        Timestamp dpb = rs.getTimestamp("duoc_phan_bo_luc");
        if (dpb != null) lead.setDuocPhanBoLuc(dpb.toLocalDateTime());

        Timestamp sla = rs.getTimestamp("han_phan_hoi_sla");
        if (sla != null) lead.setHanPhanHoiSla(sla.toLocalDateTime());

        Timestamp dn = rs.getTimestamp("duoc_nhan_luc");
        if (dn != null) lead.setDuocNhanLuc(dn.toLocalDateTime());

        Timestamp lh = rs.getTimestamp("lien_he_dau_luc");
        if (lh != null) lead.setLienHeDauLuc(lh.toLocalDateTime());

        lead.setLyDoTuChoi(rs.getString("ly_do_tu_choi"));
        lead.setQuaSla(rs.getBoolean("qua_sla"));

        long cdKhId = rs.getLong("khach_hang_chuyen_doi_id");
        if (!rs.wasNull()) lead.setKhachHangChuyenDoiId(cdKhId);

        long cdNlhId = rs.getLong("nguoi_lien_he_chuyen_doi_id");
        if (!rs.wasNull()) lead.setNguoiLienHeChuyenDoiId(cdNlhId);

        long cdChId = rs.getLong("co_hoi_chuyen_doi_id");
        if (!rs.wasNull()) lead.setCoHoiChuyenDoiId(cdChId);

        Timestamp cdl = rs.getTimestamp("chuyen_doi_luc");
        if (cdl != null) lead.setChuyenDoiLuc(cdl.toLocalDateTime());

        long gopId = rs.getLong("gop_vao_lead_id");
        if (!rs.wasNull()) lead.setGopVaoLeadId(gopId);

        long cb = rs.getLong("created_by");
        if (!rs.wasNull()) lead.setCreatedBy(cb);

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) lead.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) lead.setUpdatedAt(updated.toLocalDateTime());

        return lead;
    }

    /**
     * Tìm kiếm và lọc danh sách Lead theo 5 nhóm tiêu chí và Data Scope (Story S4-09, AC1, AC2).
     * Mặc định sắp xếp: Lead quá SLA lên trước, sau đó ngày tạo mới nhất (qua_sla DESC, created_at DESC, id DESC).
     */
    public List<Lead> layDanhSachLead(BoLocLeadDTO boLoc, NguoiDung user, PhamViDuLieu phamVi, int limit, int offset) {
        List<Lead> danhSach = new ArrayList<>();
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD + "WHERE 1=1 ");

        apDungDieuKienLocLead(sql, thamSo, boLoc, user, phamVi);

        // Baseline Q5: Ưu tiên Lead quá SLA lên trước, sau đó ngày tạo mới nhất, cuối cùng ID giảm dần
        sql.append(" ORDER BY l.qua_sla DESC, l.created_at DESC, l.id DESC");

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
                    danhSach.add(mapResultSetSangLead(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách Lead theo bộ lọc: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Đếm tổng số Lead thỏa mãn điều kiện lọc và Data Scope (Story S4-09).
     */
    public int demTongSoLead(BoLocLeadDTO boLoc, NguoiDung user, PhamViDuLieu phamVi) {
        List<Object> thamSo = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(l.id) FROM `lead` l WHERE 1=1 ");

        apDungDieuKienLocLead(sql, thamSo, boLoc, user, phamVi);

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
            LOGGER.log(Level.SEVERE, "Lỗi đếm số lượng Lead theo bộ lọc: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Áp dụng các điều kiện lọc và Data Scope cho truy vấn Lead (AC1 & AC2).
     */
    private void apDungDieuKienLocLead(StringBuilder sql, List<Object> thamSo, BoLocLeadDTO boLoc, NguoiDung user, PhamViDuLieu phamVi) {
        // 1. Phân quyền Data Scope (Server-side Enforcement - Q2)
        if (user != null) {
            if (phamVi == null) {
                phamVi = PhamViDuLieu.CA_NHAN;
            }
            if (phamVi == PhamViDuLieu.CA_NHAN) {
                // Sales Rep chỉ xem Lead do chính mình phụ trách
                sql.append(" AND l.nguoi_phu_trach_id = ?");
                thamSo.add(user.getId());
            } else if (phamVi == PhamViDuLieu.NHOM) {
                // Team Lead xem Lead thuộc nhóm mình quản lý
                Long nhomId = user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : -1L;
                sql.append(" AND (l.nhom_phan_bo_id = ? OR l.nguoi_phu_trach_id IN (SELECT id FROM nguoi_dung WHERE nhom_kinh_doanh_id = ?))");
                thamSo.add(nhomId);
                thamSo.add(nhomId);
            }
            // TOAN_BO (Director / Admin): Không giới hạn theo phạm vi sở hữu
        }

        if (boLoc == null) {
            return;
        }

        // 2. Tiêu chí 1: Trạng thái Lead
        if (boLoc.getTrangThai() != null && !boLoc.getTrangThai().isBlank()) {
            sql.append(" AND l.trang_thai = ?");
            thamSo.add(boLoc.getTrangThai().trim());
        }

        // 3. Tiêu chí 2: Nguồn Lead
        if (boLoc.getNguonLeadId() != null && boLoc.getNguonLeadId() > 0) {
            sql.append(" AND l.nguon_lead_id = ?");
            thamSo.add(boLoc.getNguonLeadId());
        }

        // 4. Tiêu chí 3: Phân loại Nóng / Ấm / Lạnh
        if (boLoc.getPhanLoai() != null && !boLoc.getPhanLoai().isBlank()) {
            String pl = boLoc.getPhanLoai().trim().toUpperCase();
            if ("NONG".equals(pl) || "NÓNG".equals(pl)) {
                sql.append(" AND (UPPER(l.phan_loai) = 'NONG' OR l.phan_loai = 'Nóng')");
            } else if ("AM".equals(pl) || "ẤM".equals(pl)) {
                sql.append(" AND (UPPER(l.phan_loai) = 'AM' OR l.phan_loai = 'Ấm')");
            } else if ("LANH".equals(pl) || "LẠNH".equals(pl)) {
                sql.append(" AND (UPPER(l.phan_loai) = 'LANH' OR l.phan_loai = 'Lạnh')");
            } else {
                sql.append(" AND l.phan_loai = ?");
                thamSo.add(boLoc.getPhanLoai().trim());
            }
        }

        // 5. Tiêu chí 4: Người phụ trách cụ thể (nếu được phép xem toàn bộ/nhóm và chọn lọc)
        if (boLoc.getNguoiPhuTrachId() != null && boLoc.getNguoiPhuTrachId() > 0) {
            if (phamVi != PhamViDuLieu.CA_NHAN) {
                sql.append(" AND l.nguoi_phu_trach_id = ?");
                thamSo.add(boLoc.getNguoiPhuTrachId());
            }
        }

        // 6. Tiêu chí 5: Khoảng thời gian tạo (tuNgay - denNgay, Q1)
        if (boLoc.getTuNgay() != null && !boLoc.getTuNgay().isBlank()) {
            try {
                String tuNgayStr = boLoc.getTuNgay().trim();
                if (!tuNgayStr.contains(":")) {
                    tuNgayStr += " 00:00:00";
                }
                sql.append(" AND l.created_at >= ?");
                thamSo.add(Timestamp.valueOf(tuNgayStr));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể parse tuNgay: " + boLoc.getTuNgay(), e);
            }
        }

        if (boLoc.getDenNgay() != null && !boLoc.getDenNgay().isBlank()) {
            try {
                String denNgayStr = boLoc.getDenNgay().trim();
                if (!denNgayStr.contains(":")) {
                    denNgayStr += " 23:59:59";
                }
                sql.append(" AND l.created_at <= ?");
                thamSo.add(Timestamp.valueOf(denNgayStr));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể parse denNgay: " + boLoc.getDenNgay(), e);
            }
        }
    }

    /**
     * Nhận Lead (AC1 của S4-07):
     * Cập nhật trạng thái thành DANG_CHAM_SOC và ghi nhận thời điểm duoc_nhan_luc.
     * Chỉ cho phép nhận khi lead đang ở trạng thái MOI và thuộc về chính nhân viên thao tác.
     */
    public boolean nhanLead(Long leadId, Long nguoiDungId, LocalDateTime duocNhanLuc) {
        if (leadId == null || nguoiDungId == null) {
            return false;
        }
        String sql = "UPDATE `lead` SET trang_thai = 'DANG_CHAM_SOC', duoc_nhan_luc = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND nguoi_phu_trach_id = ? AND trang_thai IN ('MOI', 'DA_PHAN_BO')";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDateTime thoiGian = (duocNhanLuc != null) ? duocNhanLuc : LocalDateTime.now();
            ps.setTimestamp(1, Timestamp.valueOf(thoiGian));
            ps.setLong(2, leadId);
            ps.setLong(3, nguoiDungId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tiếp nhận Lead id=" + leadId + ": " + e.getMessage(), e);
            throw new RuntimeException("Lỗi nhận lead: " + e.getMessage(), e);
        }
    }

    /**
     * Từ chối Lead trong Connection ngoài (hỗ trợ Transaction):
     * Trả lead về hàng chờ phân bổ (nguoi_phu_trach_id = NULL, trạng thái = HANG_CHO_PHAN_BO), lưu lý do từ chối.
     * Tuân thủ Q3: Chỉ cho phép từ chối khi đang ở trạng thái MOI hoặc DA_PHAN_BO (chưa nhận).
     */
    public boolean tuChoiLead(Connection conn, Long leadId, Long nguoiDungId, String lyDoTongHop) throws SQLException {
        if (leadId == null || nguoiDungId == null) {
            return false;
        }
        String sql = "UPDATE `lead` SET trang_thai = 'HANG_CHO_PHAN_BO', nguoi_phu_trach_id = NULL, " +
                "ly_do_tu_choi = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND nguoi_phu_trach_id = ? AND trang_thai IN ('MOI', 'DA_PHAN_BO')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lyDoTongHop);
            ps.setLong(2, leadId);
            ps.setLong(3, nguoiDungId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật cờ cảnh báo quá hạn SLA (AC3 của S4-07).
     */
    public boolean capNhatQuaSla(Long leadId, boolean quaSla) {
        if (leadId == null || leadId <= 0) {
            return false;
        }
        String sql = "UPDATE `lead` SET qua_sla = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, quaSla);
            ps.setLong(2, leadId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật cờ quá SLA cho lead id=" + leadId + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật mốc thời gian liên hệ đầu tiên (AC3 của S4-07).
     */
    public boolean capNhatLienHeDau(Long leadId, LocalDateTime lienHeDauLuc) {
        if (leadId == null || leadId <= 0 || lienHeDauLuc == null) {
            return false;
        }
        String sql = "UPDATE `lead` SET lien_he_dau_luc = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND (lien_he_dau_luc IS NULL OR lien_he_dau_luc > ?)";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(lienHeDauLuc));
            ps.setLong(2, leadId);
            ps.setTimestamp(3, Timestamp.valueOf(lienHeDauLuc));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật mốc liên hệ đầu cho lead id=" + leadId + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy danh sách các Lead cần kiểm tra SLA:
     * - Đã được phân bổ cho nhân viên (nguoi_phu_trach_id IS NOT NULL)
     * - Đang trong chu kỳ xử lý (trang_thai IN ('MOI', 'DA_PHAN_BO', 'DANG_CHAM_SOC'))
     * - Chưa bị đánh dấu quá hạn (qua_sla = 0)
     * - Đã có mốc phân bổ hoặc mốc hạn SLA
     */
    public List<Lead> layDanhSachLeadCanKiemTraSla() {
        List<Lead> ds = new ArrayList<>();
        String sql = "SELECT " + COT_SELECT_LEAD + FROM_JOIN_LEAD +
                "WHERE l.nguoi_phu_trach_id IS NOT NULL " +
                "AND l.trang_thai IN ('MOI', 'DA_PHAN_BO', 'DANG_CHAM_SOC') " +
                "AND l.qua_sla = 0 " +
                "AND l.duoc_phan_bo_luc IS NOT NULL " +
                "ORDER BY l.han_phan_hoi_sla ASC";

        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(mapResultSetSangLead(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách lead cần kiểm tra SLA: " + e.getMessage(), e);
        }

        return ds;
    }
}
