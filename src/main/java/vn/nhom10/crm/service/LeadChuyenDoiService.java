package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.CoHoiDAO;
import vn.nhom10.crm.dao.GiaiDoanPipelineDAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dao.NguoiLienHeDAO;
import vn.nhom10.crm.dto.CanhBaoTrungDTO;
import vn.nhom10.crm.dto.ChuyenDoiLeadPreviewDTO;
import vn.nhom10.crm.dto.GiaiDoanOptionDTO;
import vn.nhom10.crm.dto.KetQuaChuyenDoiLeadDTO;
import vn.nhom10.crm.model.CoHoi;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.PipelineBanHang;
import vn.nhom10.crm.model.LoaiGiaiDoanEnum;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.DatabaseConnection;
import vn.nhom10.crm.util.DuplicateCustomerDetector;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service xử lý nghiệp vụ Chuyển đổi Lead thành Khách hàng, Người liên hệ và Cơ hội (Story S4-08).
 *
 * Đáp ứng đầy đủ các tiêu chí chấp nhận:
 * - AC1: Một thao tác sinh đồng thời khách hàng doanh nghiệp, người liên hệ và cơ hội bán hàng.
 * - AC2: Dữ liệu lead được chuyển sang, không phải nhập lại.
 * - AC3: Lead chuyển sang trạng thái Đã chuyển đổi (DA_CHUYEN_DOI) và không sửa được nữa.
 * - AC4: Toàn bộ hoạt động đã ghi trên lead được giữ lại trên khách hàng mới (bảo toàn lịch sử qua quan hệ Lead -> Khách hàng).
 *
 * Các quyết định kỹ thuật v1.1 (15/10/2026):
 * - Q1: Tiêu chí đủ điều kiện: đã nhận, Đang chăm sóc, đầy đủ công ty/contact/kênh liên hệ.
 * - Q2: Trùng doanh nghiệp thì CHẶN chuyển đổi tạo mới (409 Conflict).
 * - Q3: 4 thuộc tính cơ hội (tên, giai đoạn active đầu tiên, 0 VND, +30 ngày) do server tự sinh mặc định.
 * - Q4: Giữ nguyên hoạt động gốc của Lead, trang 360 truy vấn qua liên kết chuyển đổi.
 * - Q5: Sales Rep (sở hữu), Team Lead (nhóm), Director/Admin (toàn bộ) được quyền chuyển; owner kế thừa từ Lead.
 */
public class LeadChuyenDoiService {

    private static final Logger LOGGER = Logger.getLogger(LeadChuyenDoiService.class.getName());

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+() -]{8,20}$");

    private final LeadDAO leadDAO;
    private final KhachHangDAO khachHangDAO;
    private final NguoiLienHeDAO nguoiLienHeDAO;
    private final CoHoiDAO coHoiDAO;
    private final GiaiDoanPipelineDAO giaiDoanPipelineDAO;

    public LeadChuyenDoiService() {
        this(new LeadDAO(), new KhachHangDAO(), new NguoiLienHeDAO(), new CoHoiDAO(), new GiaiDoanPipelineDAO());
    }

    public LeadChuyenDoiService(LeadDAO leadDAO,
                               KhachHangDAO khachHangDAO,
                               NguoiLienHeDAO nguoiLienHeDAO,
                               CoHoiDAO coHoiDAO,
                               GiaiDoanPipelineDAO giaiDoanPipelineDAO) {
        this.leadDAO = leadDAO != null ? leadDAO : new LeadDAO();
        this.khachHangDAO = khachHangDAO != null ? khachHangDAO : new KhachHangDAO();
        this.nguoiLienHeDAO = nguoiLienHeDAO != null ? nguoiLienHeDAO : new NguoiLienHeDAO();
        this.coHoiDAO = coHoiDAO != null ? coHoiDAO : new CoHoiDAO();
        this.giaiDoanPipelineDAO = giaiDoanPipelineDAO != null ? giaiDoanPipelineDAO : new GiaiDoanPipelineDAO();
    }

    /**
     * Lấy dữ liệu xem trước chỉ đọc phục vụ GET /lead/chuyen-doi?id={leadId}.
     */
    public ChuyenDoiLeadPreviewDTO layPreviewChuyenDoi(Long leadId, NguoiDung currentUser) {
        ChuyenDoiLeadPreviewDTO dto = new ChuyenDoiLeadPreviewDTO();
        Map<String, String> loiTruong = new HashMap<>();
        List<String> canConvertReasons = new ArrayList<>();
        List<CanhBaoTrungDTO> canhBaoTrung = new ArrayList<>();

        if (leadId == null || leadId <= 0) {
            dto.setQuyenChuyenDoi(false);
            dto.setThongBao("ID Lead không hợp lệ");
            return dto;
        }

        Lead lead = leadDAO.timTheoId(leadId);
        if (lead == null) {
            dto.setQuyenChuyenDoi(false);
            dto.setThongBao("Không tìm thấy Lead với ID: " + leadId);
            return dto;
        }

        // 1. Kiểm tra quyền và Data Scope
        boolean coQuyen = kiemTraQuyenChuyenDoi(currentUser, lead);
        dto.setQuyenChuyenDoi(coQuyen);
        if (!coQuyen) {
            canConvertReasons.add("Bạn không có quyền chuyển đổi Lead này (ngoài phạm vi dữ liệu được phân công).");
        }

        // 2. Kiểm tra trạng thái Lead (Q1 & AC3)
        boolean daChuyenDoi = lead.getTrangThai() == TrangThaiLeadEnum.DA_CHUYEN_DOI;
        if (daChuyenDoi) {
            canConvertReasons.add("Lead đã được chuyển đổi trước đó. Không thể thực hiện chuyển đổi lại.");
        } else if (lead.getTrangThai() != TrangThaiLeadEnum.DANG_CHAM_SOC) {
            canConvertReasons.add("Lead cần được tiếp nhận và ở trạng thái 'Đang chăm sóc' mới đủ điều kiện chuyển đổi.");
        }

        // 3. Kiểm tra dữ liệu nguồn Lead (Q1)
        String tenCongTy = lead.getTenCongTy() != null ? lead.getTenCongTy().trim() : "";
        if (tenCongTy.isEmpty()) {
            loiTruong.put("tenCongTy", "Cần bổ sung tên doanh nghiệp vào Lead trước khi chuyển đổi");
            canConvertReasons.add("Lead chưa có tên doanh nghiệp.");
        }

        String hoTen = lead.getHoTen() != null ? lead.getHoTen().trim() : "";
        if (hoTen.isEmpty()) {
            loiTruong.put("hoTen", "Cần bổ sung tên người liên hệ vào Lead");
            canConvertReasons.add("Lead chưa có tên người liên hệ.");
        }

        boolean coEmail = hopLeEmail(lead.getEmail());
        boolean coSdt = hopLeSoDienThoai(lead.getSoDienThoai());
        if (!coEmail && !coSdt) {
            loiTruong.put("email", "Email hoặc số điện thoại của Lead không hợp lệ");
            loiTruong.put("soDienThoai", "Cần ít nhất một email hoặc số điện thoại hợp lệ");
            canConvertReasons.add("Cần ít nhất một email hoặc số điện thoại hợp lệ để tạo Người liên hệ.");
        }

        // 4. Kiểm tra trùng khách hàng đã tồn tại (Q2)
        kiemTraTrungDoanhNghiep(tenCongTy, canhBaoTrung, canConvertReasons);

        // 5. Lấy giai đoạn pipeline mặc định đang hoạt động (Q3)
        PipelineBanHang pipeline = giaiDoanPipelineDAO.layPipelineMacDinh();
        List<GiaiDoanPipeline> dsGiaiDoan = (pipeline != null) ? giaiDoanPipelineDAO.layTatCaGiaiDoanTheoPipeline(pipeline.getId()) : new ArrayList<>();
        GiaiDoanPipeline stageMacDinh = timGiaiDoanActiveDauTien(dsGiaiDoan);
        if (stageMacDinh == null && !daChuyenDoi) {
            canConvertReasons.add("Hệ thống chưa cấu hình giai đoạn pipeline bán hàng mặc định đang hoạt động.");
        }

        List<GiaiDoanOptionDTO> optList = new ArrayList<>();
        for (GiaiDoanPipeline g : dsGiaiDoan) {
            optList.add(new GiaiDoanOptionDTO(
                    (long) g.getId(),
                    g.getTenGiaiDoan(),
                    g.getXacSuatThang(),
                    g.getThuTu(),
                    g.getTrangThai() == TrangThaiGiaiDoanEnum.DANG_AP_DUNG
            ));
        }
        dto.setDanhSachGiaiDoan(optList);

        // 6. Đếm số hoạt động đã ghi trên Lead (AC4)
        int activityCount = demSoHoatDongCuaLead(lead.getId());

        // 7. Xây dựng DTO hiển thị
        ChuyenDoiLeadPreviewDTO.LeadChuyenDoiViewDTO leadView = new ChuyenDoiLeadPreviewDTO.LeadChuyenDoiViewDTO();
        leadView.setLeadId(lead.getId());
        leadView.setMaLead(lead.getMaLead());
        leadView.setTrangThaiLead(lead.getTrangThai() != null ? lead.getTrangThai().getMa() : "");
        leadView.setTenCongTy(lead.getTenCongTy());
        leadView.setHoTen(lead.getHoTen());
        leadView.setEmail(lead.getEmail());
        leadView.setSoDienThoai(lead.getSoDienThoai());
        leadView.setNguon(lead.getTenNguonLead());
        leadView.setNhuCau(lead.getNhuCauQuanTam());
        leadView.setChuSoHuuId(lead.getNguoiPhuTrachId());
        leadView.setTenChuSoHuu(lead.getTenNguoiPhuTrach());
        leadView.setActivityCount(activityCount);

        boolean canConvert = coQuyen && !daChuyenDoi && canConvertReasons.isEmpty();
        leadView.setCanConvert(canConvert);
        leadView.setCanConvertReasons(canConvertReasons);
        dto.setLeadChuyenDoi(leadView);

        // Preview Khách hàng (AC2)
        ChuyenDoiLeadPreviewDTO.KhachHangPreviewDTO khView = new ChuyenDoiLeadPreviewDTO.KhachHangPreviewDTO();
        khView.setTenCongTy(lead.getTenCongTy());
        khView.setNganhNgheId(lead.getNganhNgheId());
        khView.setQuyMoId(lead.getQuyMoId());
        khView.setKhuVucId(lead.getKhuVucId());
        khView.setNguoiSoHuuId(lead.getNguoiPhuTrachId());
        khView.setNhomKinhDoanhId(lead.getNhomPhanBoId());
        dto.setThongTinTaoKhachHang(khView);

        // Preview Người liên hệ (AC2)
        ChuyenDoiLeadPreviewDTO.NguoiLienHePreviewDTO nlhView = new ChuyenDoiLeadPreviewDTO.NguoiLienHePreviewDTO();
        nlhView.setHoTen(lead.getHoTen());
        nlhView.setEmail(lead.getEmail());
        nlhView.setSoDienThoai(lead.getSoDienThoai());
        nlhView.setLaDauMoiChinh(true);
        dto.setThongTinTaoLienHe(nlhView);

        // Preview Cơ hội với các giá trị mặc định của server (Q3)
        ChuyenDoiLeadPreviewDTO.CoHoiPreviewDTO chView = new ChuyenDoiLeadPreviewDTO.CoHoiPreviewDTO();
        chView.setTenCoHoi("Cơ hội - " + tenCongTy);
        if (stageMacDinh != null) {
            chView.setGiaiDoanId((long) stageMacDinh.getId());
            chView.setTenGiaiDoan(stageMacDinh.getTenGiaiDoan());
        }
        chView.setGiaTriDuKien(BigDecimal.ZERO);
        chView.setNgayChotDuKien(LocalDate.now().plusDays(30));
        chView.setNguon(lead.getTenNguonLead());
        chView.setNguoiPhuTrachId(lead.getNguoiPhuTrachId());
        dto.setThongTinTaoCoHoi(chView);

        dto.setLoiTruong(loiTruong);
        dto.setCanhBaoTrung(canhBaoTrung);

        return dto;
    }

    /**
     * Thực hiện chuyển đổi Lead trong 1 Atomic Transaction (AC1, AC2, AC3, AC4).
     */
    public KetQuaChuyenDoiLeadDTO chuyenDoiLead(Long leadId, NguoiDung currentUser) {
        if (leadId == null || leadId <= 0) {
            return KetQuaChuyenDoiLeadDTO.thatBai("LEAD_INVALID_ID", "ID Lead không hợp lệ");
        }
        if (currentUser == null) {
            return KetQuaChuyenDoiLeadDTO.thatBai("UNAUTHORIZED", "Người dùng chưa đăng nhập");
        }

        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DatabaseConnection.layKetNoi();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Khóa bản ghi Lead để chống race condition (CONV-GUARD / AC3)
            Lead lead = leadDAO.timTheoIdDeKhoa(conn, leadId);
            if (lead == null) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("LEAD_NOT_FOUND", "Không tìm thấy Lead với ID: " + leadId);
            }

            // 2. Kiểm tra phân quyền và Data Scope tại thời điểm POST (Q5)
            if (!kiemTraQuyenChuyenDoi(currentUser, lead)) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("LEAD_ACCESS_DENIED", "Bạn không có quyền chuyển đổi Lead này.");
            }

            // 3. Kiểm tra trạng thái Lead (Q1 & AC3)
            if (lead.getTrangThai() == TrangThaiLeadEnum.DA_CHUYEN_DOI) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("LEAD_ALREADY_CONVERTED", "Lead đã được chuyển đổi trước đó. Tải lại trang để xem trạng thái mới.");
            }
            if (lead.getTrangThai() != TrangThaiLeadEnum.DANG_CHAM_SOC) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("LEAD_NOT_QUALIFIED", "Lead chưa ở trạng thái Đang chăm sóc.");
            }

            // 4. Kiểm tra dữ liệu nguồn Lead bắt buộc (Q1)
            String tenCongTy = lead.getTenCongTy() != null ? lead.getTenCongTy().trim() : "";
            String hoTen = lead.getHoTen() != null ? lead.getHoTen().trim() : "";
            boolean coEmail = hopLeEmail(lead.getEmail());
            boolean coSdt = hopLeSoDienThoai(lead.getSoDienThoai());

            Map<String, String> loiTruong = new HashMap<>();
            if (tenCongTy.isEmpty()) {
                loiTruong.put("tenCongTy", "Cần bổ sung tên doanh nghiệp vào Lead trước khi chuyển đổi");
            }
            if (hoTen.isEmpty()) {
                loiTruong.put("hoTen", "Cần bổ sung tên người liên hệ vào Lead");
            }
            if (!coEmail && !coSdt) {
                loiTruong.put("email", "Email hoặc số điện thoại của Lead không hợp lệ");
                loiTruong.put("soDienThoai", "Cần ít nhất một email hoặc số điện thoại hợp lệ");
            }

            if (!loiTruong.isEmpty()) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("LEAD_SOURCE_INVALID", "Thông tin Lead chưa đủ. Hãy sửa Lead rồi thử lại.", loiTruong);
            }

            // 5. Kiểm tra trùng doanh nghiệp (Q2)
            List<CanhBaoTrungDTO> canhBaoTrung = new ArrayList<>();
            List<String> lyDoTrung = new ArrayList<>();
            kiemTraTrungDoanhNghiep(tenCongTy, canhBaoTrung, lyDoTrung);
            for (CanhBaoTrungDTO cb : canhBaoTrung) {
                if (cb.isXacNhanTrung()) {
                    conn.rollback();
                    return KetQuaChuyenDoiLeadDTO.thatBai("CUSTOMER_ALREADY_EXISTS", "Doanh nghiệp đã có trong CRM. Không thể tạo khách hàng mới từ Lead này.");
                }
            }

            // 6. Lấy giai đoạn pipeline active đầu tiên (Q3)
            PipelineBanHang pipeline = giaiDoanPipelineDAO.layPipelineMacDinh(conn);
            if (pipeline == null) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("PIPELINE_DEFAULT_UNAVAILABLE", "Hệ thống chưa có pipeline bán hàng.");
            }
            List<GiaiDoanPipeline> dsGiaiDoan = giaiDoanPipelineDAO.layTatCaGiaiDoanTheoPipeline(pipeline.getId());
            GiaiDoanPipeline stageActive = timGiaiDoanActiveDauTien(dsGiaiDoan);
            if (stageActive == null) {
                conn.rollback();
                return KetQuaChuyenDoiLeadDTO.thatBai("PIPELINE_DEFAULT_UNAVAILABLE", "Hệ thống chưa cấu hình giai đoạn pipeline bán hàng đang hoạt động.");
            }

            // Kế thừa người sở hữu từ Lead (Q5)
            Long ownerId = lead.getNguoiPhuTrachId() != null ? lead.getNguoiPhuTrachId() : currentUser.getId();
            Long nhomId = lead.getNhomPhanBoId();

            // 7. Bước 1: Tạo Khách hàng doanh nghiệp mới (AC1, AC2)
            KhachHang kh = new KhachHang();
            kh.setTenCongTy(tenCongTy);
            kh.setTenChuanHoa(DuplicateCustomerDetector.chuanHoaTenCongTy(tenCongTy));
            kh.setNganhNgheId(lead.getNganhNgheId());
            kh.setQuyMoId(lead.getQuyMoId());
            kh.setKhuVucId(lead.getKhuVucId());
            kh.setNguoiSoHuuId(ownerId);
            kh.setNhomKinhDoanhId(nhomId);
            kh.setTrangThai(TrangThaiKhachHangEnum.TIEM_NANG.getMa());
            kh.setDoanhThuUocTinh(BigDecimal.ZERO);
            kh.setMoTaChiTiet(lead.getNhuCauQuanTam());
            kh.setNgayTao(LocalDate.now());

            Long khachHangId = khachHangDAO.themKhachHang(conn, kh);
            if (khachHangId == null || khachHangId <= 0) {
                throw new SQLException("Không thể tạo khách hàng mới trong transaction.");
            }

            // 8. Bước 2: Tạo Người liên hệ mới (AC1, AC2)
            NguoiLienHe nlh = new NguoiLienHe();
            nlh.setKhachHangId(khachHangId);
            nlh.setHoTen(hoTen);
            nlh.setEmail(lead.getEmail() != null ? lead.getEmail().trim() : null);
            nlh.setSoDienThoai(lead.getSoDienThoai() != null ? lead.getSoDienThoai().trim() : null);
            nlh.setLaDauMoiChinh(true);
            nlh.setTrangThai("DANG_HOAT_DONG");

            long nlhId = nguoiLienHeDAO.themNguoiLienHe(conn, nlh);
            if (nlhId <= 0) {
                throw new SQLException("Không thể tạo người liên hệ mới trong transaction.");
            }

            // 9. Bước 3: Tạo Cơ hội bán hàng mới với các mặc định của server (AC1, Q3)
            CoHoi ch = new CoHoi();
            String tenCoHoi = "Cơ hội - " + tenCongTy;
            if (tenCoHoi.length() > 255) {
                tenCoHoi = tenCoHoi.substring(0, 255);
            }
            ch.setTenCoHoi(tenCoHoi);
            ch.setKhachHangId(khachHangId);
            ch.setNguoiLienHeChinhId(nlhId);
            ch.setLeadId(lead.getId());
            ch.setChienDichId(lead.getChienDichId());
            ch.setNguonLeadId(lead.getNguonLeadId());
            ch.setGiaiDoanId((long) stageActive.getId());
            ch.setNguoiPhuTrachId(ownerId);
            ch.setNhomKinhDoanhId(nhomId);
            ch.setGiaTriDuKien(BigDecimal.ZERO);
            ch.setXacSuat(stageActive.getXacSuatThang());
            ch.setNgayChotDuKien(LocalDate.now().plusDays(30));
            ch.setTrangThai("MO");
            ch.setNgayTao(LocalDate.now());

            Long coHoiId = coHoiDAO.themCoHoi(conn, ch);
            if (coHoiId == null || coHoiId <= 0) {
                throw new SQLException("Không thể tạo cơ hội bán hàng mới trong transaction.");
            }

            // 10. Bước 4: Cập nhật trạng thái Lead -> DA_CHUYEN_DOI và gắn FK (AC3, Q4)
            LocalDateTime now = LocalDateTime.now();
            boolean leadUpdated = leadDAO.chuyenDoiLead(conn, lead.getId(), khachHangId, nlhId, coHoiId, now);
            if (!leadUpdated) {
                throw new SQLException("Không thể cập nhật trạng thái chuyển đổi của Lead (có thể đã bị chuyển đổi đồng thời).");
            }

            // 11. Đếm số hoạt động đã bảo toàn (AC4)
            int soHoatDong = demSoHoatDongCuaLead(conn, lead.getId());

            // 12. Commit Transaction
            conn.commit();

            return KetQuaChuyenDoiLeadDTO.thanhCong(lead.getId(), khachHangId, nlhId, coHoiId, soHoatDong, now);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi rollback transaction: " + ex.getMessage(), ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Lỗi thực hiện chuyển đổi Lead: " + e.getMessage(), e);
            return KetQuaChuyenDoiLeadDTO.thatBai("CONVERSION_NOT_COMPLETED", "Chưa thể hoàn tất chuyển đổi. Vui lòng kiểm tra trạng thái lead rồi thử lại.");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Kiểm tra quyền chuyển đổi Lead theo ma trận phân quyền (Q5).
     * Sales Rep: chỉ lead của mình.
     * Team Lead: lead của mình hoặc trong nhóm mình quản lý.
     * Director, Admin: toàn quyền.
     * Marketing, Cust. Success, Accountant: không có quyền tạo Cơ hội hoặc không quản lý Lead.
     */
    public boolean kiemTraQuyenChuyenDoi(NguoiDung user, Lead lead) {
        if (user == null || lead == null) {
            return false;
        }

        Set<VaiTroEnum> roles = user.getDanhSachVaiTroEnum();
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        // Admin & Director có toàn quyền
        if (roles.contains(VaiTroEnum.ADMIN) || roles.contains(VaiTroEnum.DIRECTOR)) {
            return true;
        }

        // Team Lead quản lý nhóm kinh doanh
        if (roles.contains(VaiTroEnum.TEAM_LEAD)) {
            if (user.getNhomKinhDoanhId() != null && lead.getNhomPhanBoId() != null) {
                if (user.getNhomKinhDoanhId().longValue() == lead.getNhomPhanBoId().longValue()) {
                    return true;
                }
            }
            // Hoặc là người phụ trách trực tiếp
            if (lead.getNguoiPhuTrachId() != null && lead.getNguoiPhuTrachId() == user.getId()) {
                return true;
            }
        }

        // Sales Rep chỉ chuyển đổi lead được phân công cho mình
        if (roles.contains(VaiTroEnum.SALES_REP)) {
            return lead.getNguoiPhuTrachId() != null && lead.getNguoiPhuTrachId() == user.getId();
        }

        // Marketing, Cust. Success, Accountant không có quyền chuyển đổi Lead thành Khách hàng & Cơ hội
        return false;
    }

    private void kiemTraTrungDoanhNghiep(String tenCongTy, List<CanhBaoTrungDTO> canhBaoTrung, List<String> canConvertReasons) {
        if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
            return;
        }

        String tenChuanHoa = DuplicateCustomerDetector.chuanHoaTenCongTy(tenCongTy);
        if (tenChuanHoa.isEmpty()) {
            return;
        }

        // Kiểm tra xem tên công ty đã tồn tại trong khach_hang chưa
        String sql = "SELECT id, ten_cong_ty, ma_khach_hang FROM khach_hang WHERE LOWER(ten_chuan_hoa) = ? OR LOWER(ten_cong_ty) = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.layKetNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenChuanHoa.toLowerCase());
            ps.setString(2, tenCongTy.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenTonTai = rs.getString("ten_cong_ty");
                    CanhBaoTrungDTO cb = new CanhBaoTrungDTO("TEN_CONG_TY", true, true, false,
                            "Doanh nghiệp '" + tenTonTai + "' đã tồn tại trong CRM.");
                    canhBaoTrung.add(cb);
                    canConvertReasons.add("Doanh nghiệp '" + tenTonTai + "' đã có trong CRM. Không thể chuyển đổi tạo mới.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi kiểm tra trùng doanh nghiệp: " + e.getMessage());
        }
    }

    private GiaiDoanPipeline timGiaiDoanActiveDauTien(List<GiaiDoanPipeline> dsGiaiDoan) {
        if (dsGiaiDoan == null) return null;
        for (GiaiDoanPipeline g : dsGiaiDoan) {
            boolean active = (g.getTrangThai() == TrangThaiGiaiDoanEnum.DANG_AP_DUNG);
            boolean dangTienHanh = (g.getLoaiGiaiDoan() == LoaiGiaiDoanEnum.DANG_TIEN_HANH || g.getLoaiGiaiDoan() == null);
            if (active && dangTienHanh) {
                return g;
            }
        }
        return null;
    }

    private int demSoHoatDongCuaLead(Long leadId) {
        if (leadId == null || leadId <= 0) return 0;
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            return demSoHoatDongCuaLead(conn, leadId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi đếm số hoạt động của Lead: " + e.getMessage());
            return 0;
        }
    }

    private int demSoHoatDongCuaLead(Connection conn, Long leadId) {
        if (leadId == null || leadId <= 0) return 0;
        String sql = "SELECT COUNT(*) FROM hoat_dong WHERE lead_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi đếm số hoạt động của Lead trong transaction: " + e.getMessage());
        }
        return 0;
    }

    private boolean hopLeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean hopLeSoDienThoai(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}
