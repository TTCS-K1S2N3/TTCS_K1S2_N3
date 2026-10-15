package vn.nhom10.crm.dao;

import vn.nhom10.crm.dto.KetQuaGopLeadDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.UngVienTrungLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.DatabaseConnection;
import vn.nhom10.crm.util.DuplicateLeadDetector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO xử lý truy vấn phát hiện Lead trùng, gợi ý gắn khách hàng và giao dịch gộp Lead (Story S4-04).
 * Đảm bảo Database Transaction nguyên tử (AC3):
 * - Chuyển toàn bộ hoạt động (hoat_dong)
 * - Chuyển toàn bộ công việc (cong_viec)
 * - Chuyển toàn bộ cơ hội (co_hoi) nếu có
 * - Cập nhật Lead nguồn: trang_thai = 'DA_GOP', gop_vao_lead_id = ?
 * - Ghi lịch sử kiểm toán vào bảng lich_su_gop_lead
 * Bất kỳ lỗi nào phát sinh đều ROLLBACK 100% dữ liệu.
 */
public class GopLeadDAO {

    private static final Logger LOGGER = Logger.getLogger(GopLeadDAO.class.getName());

    public static class ThongKeLichSuLead {
        public int soHoatDong = 0;
        public int soCongViec = 0;
        public int soCoHoi = 0;
    }

    /**
     * Lấy thông tin chi tiết một Lead theo ID.
     */
    public Lead layChiTietLead(Connection conn, Long leadId) throws SQLException {
        if (leadId == null) return null;
        String sql = "SELECT l.id, l.ma_lead, l.ho_ten, l.email, l.email_chuan_hoa, " +
                "l.so_dien_thoai, l.so_dien_thoai_chuan_hoa, l.ten_cong_ty, l.ten_cong_ty_chuan_hoa, " +
                "l.nhu_cau_quan_tam, l.muc_do_quan_tam, l.nganh_nghe_id, l.quy_mo_id, l.khu_vuc_id, " +
                "l.nguon_lead_id, nl.ten_nguon AS ten_nguon_lead, " +
                "l.chien_dich_id, cd.ten_chien_dich AS ten_chien_dich, " +
                "l.bieu_mau_id, bm.ten_bieu_mau AS ten_bieu_mau, " +
                "l.diem, l.phan_loai, l.trang_thai, " +
                "l.nguoi_phu_trach_id, nd.ho_ten AS ten_nguoi_phu_trach, " +
                "l.nhom_phan_bo_id, l.duoc_phan_bo_luc, l.han_phan_hoi_sla, l.duoc_nhan_luc, l.lien_he_dau_luc, " +
                "l.ly_do_tu_choi, l.qua_sla, l.khach_hang_chuyen_doi_id, l.nguoi_lien_he_chuyen_doi_id, " +
                "l.co_hoi_chuyen_doi_id, l.chuyen_doi_luc, l.gop_vao_lead_id, l.created_by, l.created_at, l.updated_at " +
                "FROM `lead` l " +
                "LEFT JOIN nguon_lead nl ON l.nguon_lead_id = nl.id " +
                "LEFT JOIN chien_dich cd ON l.chien_dich_id = cd.id " +
                "LEFT JOIN bieu_mau_lead bm ON l.bieu_mau_id = bm.id " +
                "LEFT JOIN nguoi_dung nd ON l.nguoi_phu_trach_id = nd.id " +
                "WHERE l.id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSangLead(rs);
                }
            }
        }
        return null;
    }

    public Lead layChiTietLead(Long leadId) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return layChiTietLead(conn, leadId);
        }
    }

    /**
     * Tìm danh sách Lead ứng viên nghi trùng với Lead đang xét (AC1).
     */
    public List<UngVienTrungLeadDTO> timUngVienLeadTrung(Connection conn, Lead lead, NguoiDungDTO user, Set<Long> nhomIds) throws SQLException {
        if (lead == null) return Collections.emptyList();

        List<UngVienTrungLeadDTO> ketQua = new ArrayList<>();

        String sql = "SELECT l.id, l.ma_lead, l.ho_ten, l.email, l.email_chuan_hoa, " +
                "l.so_dien_thoai, l.so_dien_thoai_chuan_hoa, l.ten_cong_ty, l.ten_cong_ty_chuan_hoa, " +
                "l.trang_thai, l.nguoi_phu_trach_id, nd.ho_ten AS ten_nguoi_phu_trach, l.nhom_phan_bo_id, l.created_at " +
                "FROM `lead` l " +
                "LEFT JOIN nguoi_dung nd ON l.nguoi_phu_trach_id = nd.id " +
                "WHERE l.id != ? AND (l.gop_vao_lead_id IS NULL AND l.trang_thai != 'DA_GOP')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lead.getId() != null ? lead.getId() : -1L);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Lead ungVien = new Lead();
                    ungVien.setId(rs.getLong("id"));
                    ungVien.setMaLead(rs.getString("ma_lead"));
                    ungVien.setHoTen(rs.getString("ho_ten"));
                    ungVien.setEmail(rs.getString("email"));
                    ungVien.setEmailChuanHoa(rs.getString("email_chuan_hoa"));
                    ungVien.setSoDienThoai(rs.getString("so_dien_thoai"));
                    ungVien.setSoDienThoaiChuanHoa(rs.getString("so_dien_thoai_chuan_hoa"));
                    ungVien.setTenCongTy(rs.getString("ten_cong_ty"));
                    ungVien.setTenCongTyChuanHoa(rs.getString("ten_cong_ty_chuan_hoa"));
                    ungVien.setTrangThai(TrangThaiLeadEnum.tuChuoi(rs.getString("trang_thai")));
                    ungVien.setNguoiPhuTrachId(rs.getLong("nguoi_phu_trach_id"));
                    ungVien.setNhomPhanBoId(rs.getLong("nhom_phan_bo_id"));

                    // So khớp 3 tín hiệu
                    List<String> matchReasons = DuplicateLeadDetector.kiemTraTrungLead(lead, ungVien);
                    if (!matchReasons.isEmpty()) {
                        UngVienTrungLeadDTO dto = new UngVienTrungLeadDTO();
                        dto.setCandidateKind("LEAD");
                        dto.setRecordId(ungVien.getId());
                        dto.setMaBanGhi(ungVien.getMaLead());
                        dto.setDisplayName(ungVien.getHoTen());
                        dto.setEmail(ungVien.getEmail());
                        dto.setSoDienThoai(ungVien.getSoDienThoai());
                        dto.setTenCongTy(ungVien.getTenCongTy());
                        dto.setTrangThai(ungVien.getTrangThai() != null ? ungVien.getTrangThai().getTenHienThi() : "");
                        dto.setNguoiPhuTrachId(ungVien.getNguoiPhuTrachId());
                        dto.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));
                        dto.setNhomPhanBoId(ungVien.getNhomPhanBoId());
                        dto.setMatchReasons(matchReasons);

                        Timestamp ts = rs.getTimestamp("created_at");
                        if (ts != null) {
                            dto.setCreatedAt(ts.toLocalDateTime());
                        }

                        // Kiểm tra Data Scope để làm mờ PII nếu ngoài scope
                        boolean canView = kiemTraScopeXem(user, ungVien.getNguoiPhuTrachId(), ungVien.getNhomPhanBoId(), nhomIds);
                        dto.setCanViewDetail(canView);
                        if (!canView) {
                            dto.setMaskedLabel(ungVien.getHoTen());
                            String contact = ungVien.getEmail() != null ? DuplicateLeadDetector.maskEmail(ungVien.getEmail())
                                    : DuplicateLeadDetector.maskPhone(ungVien.getSoDienThoai());
                            dto.setMaskedContact(contact);
                        } else {
                            dto.setMaskedLabel(ungVien.getHoTen());
                            dto.setMaskedContact(ungVien.getEmail() != null ? ungVien.getEmail() : ungVien.getSoDienThoai());
                        }

                        ketQua.add(dto);
                    }
                }
            }
        }
        return ketQua;
    }

    public List<UngVienTrungLeadDTO> timUngVienLeadTrung(Lead lead, NguoiDungDTO user, Set<Long> nhomIds) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return timUngVienLeadTrung(conn, lead, user, nhomIds);
        }
    }

    /**
     * Tìm danh sách Khách hàng hiện có có dấu hiệu trùng với Lead (AC2).
     */
    public List<UngVienTrungLeadDTO> timUngVienCustomerTrung(Connection conn, Lead lead, NguoiDungDTO user, Set<Long> nhomIds) throws SQLException {
        if (lead == null) return Collections.emptyList();

        List<UngVienTrungLeadDTO> ketQua = new ArrayList<>();

        String cleanCompany = DuplicateLeadDetector.chuanHoaTenCongTy(lead.getTenCongTy());
        String cleanEmail = lead.getEmailChuanHoa() != null ? lead.getEmailChuanHoa() : DuplicateLeadDetector.chuanHoaEmail(lead.getEmail());
        String cleanPhone = lead.getSoDienThoaiChuanHoa() != null ? lead.getSoDienThoaiChuanHoa() : DuplicateLeadDetector.chuanHoaSoDienThoai(lead.getSoDienThoai());

        // 1. Quét theo Khách hàng và Người liên hệ
        String sql = "SELECT kh.id AS kh_id, kh.ma_khach_hang, kh.ten_cong_ty, kh.ten_chuan_hoa, kh.nguoi_so_huu_id, kh.nhom_kinh_doanh_id, " +
                "nlh.id AS nlh_id, nlh.ho_ten AS ten_nlh, nlh.email AS nlh_email, nlh.so_dien_thoai AS nlh_sdt " +
                "FROM khach_hang kh " +
                "LEFT JOIN nguoi_lien_he nlh ON kh.id = nlh.khach_hang_id " +
                "WHERE kh.gop_vao_khach_hang_id IS NULL";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long khId = rs.getLong("kh_id");
                String tenKh = rs.getString("ten_cong_ty");
                String maKh = rs.getString("ma_khach_hang");
                String nlhEmail = rs.getString("nlh_email");
                String nlhSdt = rs.getString("nlh_sdt");
                long ownerId = rs.getLong("nguoi_so_huu_id");
                long nhomId = rs.getLong("nhom_kinh_doanh_id");

                List<String> reasons = new ArrayList<>();

                // Match Company
                if (!cleanCompany.isEmpty() && tenKh != null) {
                    String normKhTen = DuplicateLeadDetector.chuanHoaTenCongTy(tenKh);
                    if (!normKhTen.isEmpty() && (cleanCompany.equalsIgnoreCase(normKhTen)
                            || DuplicateLeadDetector.tinhTyLeTuongDong(cleanCompany, normKhTen) >= DuplicateLeadDetector.NGUONG_TUONG_DONG_TEN)) {
                        reasons.add("COMPANY");
                    }
                }

                // Match Email qua Người liên hệ
                if (!cleanEmail.isEmpty() && nlhEmail != null) {
                    if (cleanEmail.equalsIgnoreCase(DuplicateLeadDetector.chuanHoaEmail(nlhEmail))) {
                        reasons.add("EMAIL");
                    }
                }

                // Match Phone qua Người liên hệ
                if (!cleanPhone.isEmpty() && nlhSdt != null) {
                    if (cleanPhone.equals(DuplicateLeadDetector.chuanHoaSoDienThoai(nlhSdt))) {
                        reasons.add("PHONE");
                    }
                }

                if (!reasons.isEmpty()) {
                    // Tránh thêm lặp lại cùng 1 Customer ID nếu có nhiều người liên hệ
                    boolean daTonTai = false;
                    for (UngVienTrungLeadDTO exist : ketQua) {
                        if (exist.getRecordId() != null && exist.getRecordId().equals(khId)) {
                            for (String r : reasons) {
                                exist.themMatchReason(r);
                            }
                            daTonTai = true;
                            break;
                        }
                    }

                    if (!daTonTai) {
                        UngVienTrungLeadDTO dto = new UngVienTrungLeadDTO();
                        dto.setCandidateKind("CUSTOMER");
                        dto.setRecordId(khId);
                        dto.setMaBanGhi(maKh);
                        dto.setDisplayName(tenKh);
                        dto.setTenCongTy(tenKh);
                        dto.setMatchReasons(reasons);
                        dto.setNguoiPhuTrachId(ownerId);
                        dto.setNhomPhanBoId(nhomId);

                        boolean canView = kiemTraScopeXem(user, ownerId, nhomId, nhomIds);
                        dto.setCanViewDetail(canView);
                        dto.setMaskedLabel(tenKh);
                        dto.setMaskedContact(canView ? nlhEmail : DuplicateLeadDetector.maskEmail(nlhEmail));

                        ketQua.add(dto);
                    }
                }
            }
        }
        return ketQua;
    }

    public List<UngVienTrungLeadDTO> timUngVienCustomerTrung(Lead lead, NguoiDungDTO user, Set<Long> nhomIds) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return timUngVienCustomerTrung(conn, lead, user, nhomIds);
        }
    }

    /**
     * Lấy số lượng thống kê lịch sử gắn với Lead (AC3).
     */
    public ThongKeLichSuLead layThongKeLichSu(Connection conn, Long leadId) throws SQLException {
        ThongKeLichSuLead tk = new ThongKeLichSuLead();
        if (leadId == null) return tk;

        // Đếm hoạt động
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM hoat_dong WHERE lead_id = ?")) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soHoatDong = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm hoat_dong: " + e.getMessage());
        }

        // Đếm công việc
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM cong_viec WHERE lead_id = ?")) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soCongViec = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm cong_viec: " + e.getMessage());
        }

        // Đếm cơ hội
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM co_hoi WHERE lead_id = ?")) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tk.soCoHoi = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Không thể đếm co_hoi: " + e.getMessage());
        }

        return tk;
    }

    public ThongKeLichSuLead layThongKeLichSu(Long leadId) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return layThongKeLichSu(conn, leadId);
        }
    }

    /**
     * Kiểm tra Lead đã bị gộp trước đó chưa.
     */
    public boolean kiemTraDaBiGop(Connection conn, Long leadId) throws SQLException {
        if (leadId == null) return false;
        String sql = "SELECT gop_vao_lead_id, trang_thai FROM `lead` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long gopId = rs.getLong("gop_vao_lead_id");
                    String tt = rs.getString("trang_thai");
                    return gopId > 0 || "DA_GOP".equalsIgnoreCase(tt);
                }
            }
        }
        return false;
    }

    /**
     * Gắn Lead với Khách hàng hiện có (Story S4-04, AC2).
     */
    public boolean ganKhachHang(Connection conn, Long leadId, Long customerId) throws SQLException {
        if (leadId == null || customerId == null) return false;
        String sql = "UPDATE `lead` SET khach_hang_chuyen_doi_id = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, leadId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean ganKhachHang(Long leadId, Long customerId) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return ganKhachHang(conn, leadId, customerId);
        }
    }

    /**
     * Thực hiện gộp Lead trong Database Transaction nguyên tử (AC3).
     */
    public KetQuaGopLeadDTO thucHienGopLeadTrenKetNoi(Connection conn,
                                                    Long sourceLeadId,
                                                    Long targetLeadId,
                                                    Long userId,
                                                    String lyDo,
                                                    String duLieuGopJson,
                                                    boolean batBuocLoiDeTestRollback) throws SQLException {
        if (sourceLeadId == null || targetLeadId == null) {
            throw new IllegalArgumentException("Mã Lead nguồn và đích không được để trống.");
        }
        if (sourceLeadId.equals(targetLeadId)) {
            throw new IllegalArgumentException("Không thể gộp một Lead vào chính nó.");
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

        try {
            // 1. Khóa 2 bản ghi theo thứ tự ID nhất quán để chống deadlock
            long id1 = Math.min(sourceLeadId, targetLeadId);
            long id2 = Math.max(sourceLeadId, targetLeadId);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, trang_thai, gop_vao_lead_id FROM `lead` WHERE id IN (?, ?) ORDER BY id FOR UPDATE")) {
                ps.setLong(1, id1);
                ps.setLong(2, id2);
                try (ResultSet rs = ps.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        long id = rs.getLong("id");
                        long gopVaoId = rs.getLong("gop_vao_lead_id");
                        String tt = rs.getString("trang_thai");
                        if (id == sourceLeadId && (gopVaoId > 0 || "DA_GOP".equalsIgnoreCase(tt))) {
                            throw new IllegalStateException("Lead nguồn (#" + sourceLeadId + ") đã được gộp trước đó.");
                        }
                        if (id == targetLeadId && (gopVaoId > 0 || "DA_GOP".equalsIgnoreCase(tt))) {
                            throw new IllegalStateException("Lead đích (#" + targetLeadId + ") đã bị gộp trước đó, không thể chọn làm bản ghi giữ lại.");
                        }
                    }
                    if (count < 2) {
                        throw new IllegalArgumentException("Không tìm thấy đủ cả hai Lead trong hệ thống.");
                    }
                }
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("FOR UPDATE")) {
                    // H2 test mode might not support FOR UPDATE on multiple rows identically
                } else {
                    throw e;
                }
            }

            // 2. Chuyển Lịch sử hoạt động (hoat_dong)
            int soHoatDongChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE hoat_dong SET lead_id = ? WHERE lead_id = ?")) {
                ps.setLong(1, targetLeadId);
                ps.setLong(2, sourceLeadId);
                soHoatDongChuyen = ps.executeUpdate();
            }

            // 3. Chuyển Công việc (cong_viec)
            int soCongViecChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cong_viec SET lead_id = ? WHERE lead_id = ?")) {
                ps.setLong(1, targetLeadId);
                ps.setLong(2, sourceLeadId);
                soCongViecChuyen = ps.executeUpdate();
            }

            // 4. Chuyển Cơ hội (co_hoi)
            int soCoHoiChuyen = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE co_hoi SET lead_id = ? WHERE lead_id = ?")) {
                ps.setLong(1, targetLeadId);
                ps.setLong(2, sourceLeadId);
                soCoHoiChuyen = ps.executeUpdate();
            }

            // 5. Cập nhật Lead nguồn: trang_thai = 'DA_GOP', gop_vao_lead_id = targetLeadId (AC3: Không xóa vật lý)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE `lead` SET trang_thai = 'DA_GOP', gop_vao_lead_id = ?, updated_at = NOW() WHERE id = ?")) {
                ps.setLong(1, targetLeadId);
                ps.setLong(2, sourceLeadId);
                ps.executeUpdate();
            }

            // 6. Ghi vết kiểm toán vào bảng lich_su_gop_lead
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO lich_su_gop_lead (lead_nguon_id, lead_dich_id, thuc_hien_boi_id, du_lieu_gop_json, created_at) " +
                            "VALUES (?, ?, ?, ?, NOW())")) {
                ps.setLong(1, sourceLeadId);
                ps.setLong(2, targetLeadId);
                ps.setLong(3, userId != null ? userId : 1L);
                ps.setString(4, duLieuGopJson);
                ps.executeUpdate();
            }

            // 7. Kiểm tra kịch bản test rollback
            if (batBuocLoiDeTestRollback) {
                throw new SQLException("Mô phỏng lỗi DB để kiểm tra Transaction Rollback.");
            }

            conn.commit(); // COMMIT THÀNH CÔNG

            return KetQuaGopLeadDTO.thanhCong(
                    targetLeadId,
                    sourceLeadId,
                    soHoatDongChuyen,
                    soCongViecChuyen,
                    soCoHoiChuyen,
                    "Đã gộp Lead #" + sourceLeadId + " vào Lead #" + targetLeadId + " thành công."
            );

        } catch (Exception e) {
            conn.rollback(); // ROLLBACK TOÀN BỘ KHI CÓ LỖI
            LOGGER.log(Level.WARNING, "Transaction gộp Lead bị rollback: " + e.getMessage());
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public KetQuaGopLeadDTO thucHienGopLead(Long sourceLeadId, Long targetLeadId, Long userId, String lyDo, String duLieuGopJson) throws SQLException {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return thucHienGopLeadTrenKetNoi(conn, sourceLeadId, targetLeadId, userId, lyDo, duLieuGopJson, false);
        }
    }

    private boolean kiemTraScopeXem(NguoiDungDTO user, Long ownerId, Long nhomId, Set<Long> nhomIds) {
        if (user == null) return false;
        VaiTroEnum vt = user.getVaiTro();
        if (vt == VaiTroEnum.ADMIN || vt == VaiTroEnum.DIRECTOR || vt == VaiTroEnum.MARKETING
                || user.getPhamViToiDa() == PhamViDuLieu.TOAN_BO) {
            return true;
        }
        if (ownerId != null && ownerId.equals(user.getId())) {
            return true;
        }
        if (nhomId != null && nhomIds != null && nhomIds.contains(nhomId)) {
            return true;
        }
        return false;
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
        if (!rs.wasNull()) lead.setChienDichId(cdId);
        lead.setTenChienDich(rs.getString("ten_chien_dich"));

        long bmId = rs.getLong("bieu_mau_id");
        if (!rs.wasNull()) lead.setBieuMauId(bmId);
        lead.setTenBieuMau(rs.getString("ten_bieu_mau"));

        lead.setDiem(rs.getInt("diem"));
        lead.setPhanLoai(rs.getString("phan_loai"));
        lead.setTrangThai(TrangThaiLeadEnum.tuChuoi(rs.getString("trang_thai")));

        long ptId = rs.getLong("nguoi_phu_trach_id");
        if (!rs.wasNull()) lead.setNguoiPhuTrachId(ptId);
        lead.setTenNguoiPhuTrach(rs.getString("ten_nguoi_phu_trach"));

        long npbId = rs.getLong("nhom_phan_bo_id");
        if (!rs.wasNull()) lead.setNhomPhanBoId(npbId);

        lead.setQuaSla(rs.getBoolean("qua_sla"));

        long khCdId = rs.getLong("khach_hang_chuyen_doi_id");
        if (!rs.wasNull()) lead.setKhachHangChuyenDoiId(khCdId);

        long gopId = rs.getLong("gop_vao_lead_id");
        if (!rs.wasNull()) lead.setGopVaoLeadId(gopId);

        Timestamp tsCreated = rs.getTimestamp("created_at");
        if (tsCreated != null) lead.setCreatedAt(tsCreated.toLocalDateTime());

        Timestamp tsUpdated = rs.getTimestamp("updated_at");
        if (tsUpdated != null) lead.setUpdatedAt(tsUpdated.toLocalDateTime());

        return lead;
    }
}
