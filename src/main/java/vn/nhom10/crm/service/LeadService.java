package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.util.ExcelLeadUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ quản lý Khách hàng tiềm năng (Lead).
 * Phục vụ Story S4-02:
 * - AC1: Nhập tay một lead từ sự kiện hoặc danh thiếp
 * - AC3: Bắt buộc mọi lead nhập vào phải có nguồn hợp lệ
 */
public class LeadService {

    private static final Logger LOGGER = Logger.getLogger(LeadService.class.getName());

    private final LeadDAO leadDAO;
    private final DanhMucBanHangDAO danhMucBanHangDAO;
    private final LeadScoringService leadScoringService;

    public LeadService() {
        this(new LeadDAO(), new DanhMucBanHangDAO(), LeadScoringService.getInstance());
    }

    public LeadService(LeadDAO leadDAO) {
        this(leadDAO, new DanhMucBanHangDAO(), LeadScoringService.getInstance());
    }

    public LeadService(LeadDAO leadDAO, DanhMucBanHangDAO danhMucBanHangDAO) {
        this(leadDAO, danhMucBanHangDAO, LeadScoringService.getInstance());
    }

    public LeadService(LeadDAO leadDAO, DanhMucBanHangDAO danhMucBanHangDAO, LeadScoringService leadScoringService) {
        this.leadDAO = leadDAO != null ? leadDAO : new LeadDAO();
        this.danhMucBanHangDAO = danhMucBanHangDAO != null ? danhMucBanHangDAO : new DanhMucBanHangDAO();
        this.leadScoringService = leadScoringService != null ? leadScoringService : LeadScoringService.getInstance();
    }

    /**
     * Tạo lead thủ công (AC1) với quy tắc bắt buộc có nguồn (AC3).
     *
     * @param lead        thông tin lead cần tạo
     * @param nguoiTaoId  ID người dùng đăng nhập thực hiện
     * @return Lead đã được lưu kèm ID và mã tự sinh
     * @throws IllegalArgumentException khi vi phạm validation
     */
    public Lead taoLeadThuCong(Lead lead, Long nguoiTaoId) {
        if (lead == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng tiềm năng không được để trống.");
        }

        // 1. Thẩm định Họ và tên (Bắt buộc)
        String hoTen = lead.getHoTen() != null ? lead.getHoTen().trim() : "";
        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException("Họ và tên khách hàng tiềm năng không được để trống.");
        }
        if (hoTen.length() > 150) {
            throw new IllegalArgumentException("Họ và tên không được vượt quá 150 ký tự.");
        }
        lead.setHoTen(hoTen);

        // 2. Thẩm định Nguồn Lead (BẮT BUỘC THEO AC3)
        if (lead.getNguonLeadId() == null || lead.getNguonLeadId() <= 0) {
            throw new IllegalArgumentException("Mỗi lead nhập vào đều bắt buộc có nguồn (Tiêu chí AC3). Vui lòng chọn nguồn lead.");
        }

        // 3. Thẩm định Email (nếu có)
        String email = lead.getEmail() != null ? lead.getEmail().trim() : null;
        if (email != null && !email.isEmpty()) {
            if (email.length() > 255) {
                throw new IllegalArgumentException("Địa chỉ email không được vượt quá 255 ký tự.");
            }
            if (!ExcelLeadUtil.EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (Ví dụ: ten@congty.vn).");
            }
            lead.setEmail(email);
            lead.setEmailChuanHoa(email.toLowerCase());
        } else {
            lead.setEmail(null);
            lead.setEmailChuanHoa(null);
        }

        // 4. Thẩm định Số điện thoại (nếu có)
        String phone = lead.getSoDienThoai() != null ? lead.getSoDienThoai().trim() : null;
        if (phone != null && !phone.isEmpty()) {
            String cleanPhone = phone.replaceAll("[\\s.-]", "");
            if (cleanPhone.length() > 20) {
                throw new IllegalArgumentException("Số điện thoại không được vượt quá 20 ký tự.");
            }
            if (!ExcelLeadUtil.PHONE_PATTERN.matcher(cleanPhone).matches()) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ (gồm 8-15 chữ số).");
            }
            lead.setSoDienThoai(phone);
            lead.setSoDienThoaiChuanHoa(cleanPhone);
        } else {
            lead.setSoDienThoai(null);
            lead.setSoDienThoaiChuanHoa(null);
        }

        // 5. Chuẩn hóa Tên công ty
        String tenCongTy = lead.getTenCongTy() != null ? lead.getTenCongTy().trim() : null;
        if (tenCongTy != null && !tenCongTy.isEmpty()) {
            if (tenCongTy.length() > 255) {
                tenCongTy = tenCongTy.substring(0, 255);
            }
            lead.setTenCongTy(tenCongTy);
            lead.setTenCongTyChuanHoa(tenCongTy.toUpperCase());
        } else {
            lead.setTenCongTy(null);
            lead.setTenCongTyChuanHoa(null);
        }

        // 6. Mức độ và nhu cầu quan tâm
        if (lead.getMucDoQuanTam() == null || lead.getMucDoQuanTam().isBlank()) {
            lead.setMucDoQuanTam("TRUNG_BINH");
        }
        String nhuCau = lead.getNhuCauQuanTam() != null ? lead.getNhuCauQuanTam().trim() : null;
        String ghiChu = lead.getGhiChu() != null ? lead.getGhiChu().trim() : null;
        if (ghiChu != null && !ghiChu.isEmpty()) {
            if (nhuCau != null && !nhuCau.isEmpty()) {
                lead.setNhuCauQuanTam(nhuCau + " (Ghi chú: " + ghiChu + ")");
            } else {
                lead.setNhuCauQuanTam(ghiChu);
            }
        } else {
            lead.setNhuCauQuanTam(nhuCau);
        }

        // 7. Sinh mã định danh duy nhất nếu chưa có
        if (lead.getMaLead() == null || lead.getMaLead().trim().isEmpty()) {
            lead.setMaLead(sinhMaLeadMoi());
        }

        // 8. Trạng thái mặc định: MỚI
        if (lead.getTrangThai() == null) {
            lead.setTrangThai(TrangThaiLeadEnum.MOI);
        }

        lead.setDiem(0);
        lead.setQuaSla(false);
        lead.setCreatedBy(nguoiTaoId);
        lead.setCreatedAt(LocalDateTime.now());

        // 9. Lưu vào cơ sở dữ liệu
        Long id = leadDAO.them(lead);
        lead.setId(id);

        LOGGER.info(String.format("Tạo lead thủ công thành công: ID=%d, Ma=%s, HoTen=%s, NguonId=%d",
                id, lead.getMaLead(), lead.getHoTen(), lead.getNguonLeadId()));

        return lead;
    }

    /**
     * Lấy danh sách khách hàng tiềm năng phân trang.
     */
    public List<Lead> layDanhSachLead(int limit, int offset) {
        return leadDAO.layDanhSach(limit, offset);
    }

    /**
     * Tìm khách hàng tiềm năng theo ID.
     */
    public Lead timTheoId(Long id) {
        return leadDAO.timTheoId(id);
    }

    /**
     * Lấy danh mục nguồn lead dùng chung trong hệ thống bán hàng (LoaiDanhMuc.NGUON_LEAD).
     */
    public List<MucDanhMucDTO> layDanhSachNguonLead() {
        return danhMucBanHangDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true);
    }

    /**
     * Sinh mã định danh Lead duy nhất: LEAD-yyMMdd-XXXXX
     */
    public synchronized String sinhMaLeadMoi() {
        String ngay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String ngauNhien = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
        String maLead = "LEAD-" + ngay + "-" + ngauNhien;

        while (leadDAO.kiemTraTonTaiMa(maLead)) {
            ngauNhien = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
            maLead = "LEAD-" + ngay + "-" + ngauNhien;
        }
        return maLead;
    }

    /**
     * Cập nhật thông tin Lead (Story S4-02 & S4-05 AC2).
     * Tự động tính lại điểm và phân loại khi thông tin lead thay đổi.
     */
    public Lead capNhatLead(Lead input, Long nguoiSuaId) {
        if (input == null || input.getId() == null) {
            throw new IllegalArgumentException("Dữ liệu cập nhật khách hàng tiềm năng không hợp lệ.");
        }

        Lead existing = leadDAO.timTheoId(input.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng tiềm năng với ID: " + input.getId());
        }

        // 1. Thẩm định Họ và tên (Bắt buộc)
        String hoTen = input.getHoTen() != null ? input.getHoTen().trim() : "";
        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException("Họ và tên khách hàng tiềm năng không được để trống.");
        }
        if (hoTen.length() > 150) {
            throw new IllegalArgumentException("Họ và tên không được vượt quá 150 ký tự.");
        }
        existing.setHoTen(hoTen);

        // 2. Thẩm định Nguồn Lead (BẮT BUỘC THEO AC3)
        if (input.getNguonLeadId() == null || input.getNguonLeadId() <= 0) {
            throw new IllegalArgumentException("Mỗi lead nhập vào đều bắt buộc có nguồn (Tiêu chí AC3). Vui lòng chọn nguồn lead.");
        }
        existing.setNguonLeadId(input.getNguonLeadId());

        // 3. Thẩm định Email (nếu có)
        String email = input.getEmail() != null ? input.getEmail().trim() : null;
        if (email != null && !email.isEmpty()) {
            if (email.length() > 255) {
                throw new IllegalArgumentException("Địa chỉ email không được vượt quá 255 ký tự.");
            }
            if (!ExcelLeadUtil.EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (Ví dụ: ten@congty.vn).");
            }
            existing.setEmail(email);
            existing.setEmailChuanHoa(email.toLowerCase());
        } else {
            existing.setEmail(null);
            existing.setEmailChuanHoa(null);
        }

        // 4. Thẩm định Số điện thoại (nếu có)
        String phone = input.getSoDienThoai() != null ? input.getSoDienThoai().trim() : null;
        if (phone != null && !phone.isEmpty()) {
            String cleanPhone = phone.replaceAll("[\\s.-]", "");
            if (cleanPhone.length() > 20) {
                throw new IllegalArgumentException("Số điện thoại không được vượt quá 20 ký tự.");
            }
            if (!ExcelLeadUtil.PHONE_PATTERN.matcher(cleanPhone).matches()) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ (gồm 8-15 chữ số).");
            }
            existing.setSoDienThoai(phone);
            existing.setSoDienThoaiChuanHoa(cleanPhone);
        } else {
            existing.setSoDienThoai(null);
            existing.setSoDienThoaiChuanHoa(null);
        }

        // 5. Chuẩn hóa Tên công ty
        String tenCongTy = input.getTenCongTy() != null ? input.getTenCongTy().trim() : null;
        if (tenCongTy != null && !tenCongTy.isEmpty()) {
            if (tenCongTy.length() > 255) {
                tenCongTy = tenCongTy.substring(0, 255);
            }
            existing.setTenCongTy(tenCongTy);
            existing.setTenCongTyChuanHoa(tenCongTy.toUpperCase());
        } else {
            existing.setTenCongTy(null);
            existing.setTenCongTyChuanHoa(null);
        }

        // 6. Mức độ và nhu cầu quan tâm
        if (input.getMucDoQuanTam() != null && !input.getMucDoQuanTam().isBlank()) {
            existing.setMucDoQuanTam(input.getMucDoQuanTam().trim());
        }
        String nhuCau = input.getNhuCauQuanTam() != null ? input.getNhuCauQuanTam().trim() : null;
        String ghiChu = input.getGhiChu() != null ? input.getGhiChu().trim() : null;
        if (ghiChu != null && !ghiChu.isEmpty()) {
            if (nhuCau != null && !nhuCau.isEmpty()) {
                existing.setNhuCauQuanTam(nhuCau + " (Ghi chú: " + ghiChu + ")");
            } else {
                existing.setNhuCauQuanTam(ghiChu);
            }
        } else if (nhuCau != null) {
            existing.setNhuCauQuanTam(nhuCau);
        }

        // Cập nhật ngành nghề, quy mô nếu có
        if (input.getNganhNgheId() != null) {
            existing.setNganhNgheId(input.getNganhNgheId());
        }
        if (input.getQuyMoId() != null) {
            existing.setQuyMoId(input.getQuyMoId());
        }
        if (input.getKhuVucId() != null) {
            existing.setKhuVucId(input.getKhuVucId());
        }

        // 7. Tự động tính lại điểm & phân loại theo AC2 Story S4-05
        leadScoringService.tinhDiemVaPhanLoai(existing);
        existing.setUpdatedAt(LocalDateTime.now());

        boolean ok = leadDAO.capNhat(existing);
        if (!ok) {
            throw new RuntimeException("Cập nhật thông tin khách hàng tiềm năng thất bại.");
        }

        LOGGER.info(String.format("Cập nhật lead thành công: ID=%d, Ma=%s, HoTen=%s, Diem=%d, PhanLoai=%s",
                existing.getId(), existing.getMaLead(), existing.getHoTen(), existing.getDiem(), existing.getPhanLoai()));

        return existing;
    }

    /**
     * Xóa khách hàng tiềm năng theo ID (Story S4-02).
     */
    public boolean xoaLead(Long id, Long nguoiDungId) {
        if (id == null || id <= 0) {
            return false;
        }
        Lead lead = leadDAO.timTheoId(id);
        if (lead == null) {
            return false;
        }
        boolean ok = leadDAO.xoa(id);
        if (ok) {
            LOGGER.info(String.format("Đã xóa Lead ID=%d, Ma=%s bởi User ID=%d", id, lead.getMaLead(), nguoiDungId));
        }
        return ok;
    }

    /**
     * Chấm điểm và phân loại lại cho 1 lead cụ thể (Story S4-05).
     */
    public Lead chamDiemLead(Long id) {
        if (id == null || id <= 0) return null;
        Lead lead = leadDAO.timTheoId(id);
        if (lead == null) return null;

        leadScoringService.tinhDiemVaPhanLoai(lead);
        leadDAO.capNhatDiemVaPhanLoai(lead.getId(), lead.getDiem(), lead.getPhanLoai());
        return lead;
    }

    /**
     * Tính lại điểm và phân loại toàn bộ lead theo cấu hình hiện tại (Story S4-05).
     */
    public int tinhLaiDiemToanBoLead() throws Exception {
        return leadScoringService.tinhLaiDiemToanBoLead();
    }
}
