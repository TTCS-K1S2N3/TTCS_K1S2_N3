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

    public LeadService() {
        this(new LeadDAO(), new DanhMucBanHangDAO());
    }

    public LeadService(LeadDAO leadDAO) {
        this(leadDAO, new DanhMucBanHangDAO());
    }

    public LeadService(LeadDAO leadDAO, DanhMucBanHangDAO danhMucBanHangDAO) {
        this.leadDAO = leadDAO != null ? leadDAO : new LeadDAO();
        this.danhMucBanHangDAO = danhMucBanHangDAO != null ? danhMucBanHangDAO : new DanhMucBanHangDAO();
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
}
