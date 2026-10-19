package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.ChienDichDAO;
import vn.nhom10.crm.dto.ChannelOptionDTO;
import vn.nhom10.crm.dto.ChienDichDTO;
import vn.nhom10.crm.dto.ChienDichPermissionsDTO;
import vn.nhom10.crm.dto.ChienDichRowDTO;
import vn.nhom10.crm.dto.ChienDichThongKeDTO;
import vn.nhom10.crm.model.ChienDich;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý logic nghiệp vụ Quản lý Chiến dịch Marketing (Story S4-03).
 * AC1: Khai báo chiến dịch với ngân sách, thời gian chạy, kênh.
 * AC2: Lead và cơ hội giữ liên kết tới chiến dịch đã sinh ra chúng.
 * AC3: Xem được số lead, số cơ hội và giá trị đã chốt của từng chiến dịch.
 */
public class ChienDichService {

    private static final Logger LOGGER = Logger.getLogger(ChienDichService.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ChienDichDAO chienDichDAO;

    // Danh mục kênh hợp lệ theo chuẩn hệ thống
    private static final List<ChannelOptionDTO> DANH_SACH_KENH = List.of(
            new ChannelOptionDTO("EVENT", "Sự kiện", true),
            new ChannelOptionDTO("SOCIAL", "Mạng xã hội", true),
            new ChannelOptionDTO("SEARCH", "Tìm kiếm (Google Ads / SEO)", true),
            new ChannelOptionDTO("EMAIL", "Email Marketing", true),
            new ChannelOptionDTO("DIRECT", "Trực tiếp / Đối tác", true),
            new ChannelOptionDTO("WEBSITE", "Website / Biểu mẫu nhúng", true)
    );

    public ChienDichService() {
        this(new ChienDichDAO());
    }

    public ChienDichService(ChienDichDAO chienDichDAO) {
        this.chienDichDAO = chienDichDAO != null ? chienDichDAO : new ChienDichDAO();
    }

    /**
     * Lấy danh sách tùy chọn kênh được phép cho form khai báo chiến dịch (AC1).
     */
    public List<ChannelOptionDTO> layDanhSachKenh() {
        return new ArrayList<>(DANH_SACH_KENH);
    }

    /**
     * Tìm nhãn hiển thị của kênh theo mã kênh.
     */
    public String layNhanKenh(String channelCode) {
        if (channelCode == null) return "";
        for (ChannelOptionDTO opt : DANH_SACH_KENH) {
            if (opt.getCode().equalsIgnoreCase(channelCode.trim())) {
                return opt.getLabel();
            }
        }
        return channelCode;
    }

    /**
     * Kiểm tra mã kênh có hợp lệ hay không.
     */
    public boolean laKenhHopLe(String channelCode) {
        if (channelCode == null || channelCode.isBlank()) return false;
        for (ChannelOptionDTO opt : DANH_SACH_KENH) {
            if (opt.getCode().equalsIgnoreCase(channelCode.trim()) && opt.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * AC1: Khai báo chiến dịch mới với validation chặt chẽ server-side.
     * @param form DTO chứa thông tin nhập từ form
     * @param nguoiTaoId ID người dùng thực hiện tạo
     * @param fieldErrors Map chứa lỗi từng trường (nếu có lỗi validation)
     * @return ID chiến dịch vừa tạo hoặc null nếu có lỗi
     */
    public Long taoChienDich(ChienDichDTO form, Long nguoiTaoId, Map<String, String> fieldErrors) {
        if (form == null) {
            fieldErrors.put("general", "Dữ liệu yêu cầu không hợp lệ.");
            return null;
        }

        // 1. Validate Tên chiến dịch
        String name = form.getName();
        if (name == null || name.trim().isEmpty()) {
            fieldErrors.put("name", "Tên chiến dịch không được để trống.");
        } else if (name.trim().length() > 200) {
            fieldErrors.put("name", "Tên chiến dịch không được vượt quá 200 ký tự.");
        }

        // 2. Validate Ngân sách
        BigDecimal budgetAmount = null;
        String budgetStr = form.getBudgetAmountStr();
        if (budgetStr == null && form.getBudgetAmount() != null) {
            budgetAmount = form.getBudgetAmount();
        } else if (budgetStr == null || budgetStr.trim().isEmpty()) {
            fieldErrors.put("budgetAmount", "Ngân sách không được để trống.");
        } else {
            try {
                // Xử lý chuẩn hóa số tiền (loại bỏ dấu cách, dấu phân cách hàng nghìn nếu có)
                String sanitized = budgetStr.trim().replace(",", "").replace(".", "");
                // Nếu người dùng nhập chuỗi số nguyên hoặc thập phân
                if (budgetStr.contains(".") && !budgetStr.contains(",")) {
                    // Định dạng dấu chấm thập phân
                    sanitized = budgetStr.trim();
                }
                budgetAmount = new BigDecimal(sanitized);
                if (budgetAmount.compareTo(BigDecimal.ZERO) < 0) {
                    fieldErrors.put("budgetAmount", "Ngân sách phải là số không âm.");
                }
            } catch (NumberFormatException e) {
                fieldErrors.put("budgetAmount", "Ngân sách phải là số không âm.");
            }
        }

        // 3. Validate Thời gian chạy (startDate & endDate)
        LocalDate startDate = null;
        LocalDate endDate = null;

        String startStr = form.getStartDateStr();
        if (startStr == null && form.getStartDate() != null) {
            startDate = form.getStartDate();
        } else if (startStr == null || startStr.trim().isEmpty()) {
            fieldErrors.put("startDate", "Ngày bắt đầu không được để trống.");
        } else {
            try {
                startDate = LocalDate.parse(startStr.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                fieldErrors.put("startDate", "Ngày bắt đầu không đúng định dạng (yyyy-MM-dd).");
            }
        }

        String endStr = form.getEndDateStr();
        if (endStr == null && form.getEndDate() != null) {
            endDate = form.getEndDate();
        } else if (endStr == null || endStr.trim().isEmpty()) {
            fieldErrors.put("endDate", "Ngày kết thúc không được để trống.");
        } else {
            try {
                endDate = LocalDate.parse(endStr.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                fieldErrors.put("endDate", "Ngày kết thúc không đúng định dạng (yyyy-MM-dd).");
            }
        }

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            fieldErrors.put("endDate", "Ngày kết thúc không được trước ngày bắt đầu.");
        }

        // 4. Validate Kênh
        String channelCode = form.getChannelCode();
        if (channelCode == null || channelCode.trim().isEmpty()) {
            fieldErrors.put("channelCode", "Vui lòng chọn kênh chiến dịch.");
        } else if (!laKenhHopLe(channelCode)) {
            fieldErrors.put("channelCode", "Kênh được chọn không hợp lệ trong danh mục.");
        }

        // Nếu có lỗi validation -> dừng và không ghi DB
        if (!fieldErrors.isEmpty()) {
            return null;
        }

        // 5. Sinh mã chiến dịch duy nhất
        String maChienDich = form.getMaChienDich();
        if (maChienDich == null || maChienDich.isBlank()) {
            maChienDich = sinhMaChienDich();
        }

        // 6. Tạo entity và lưu vào DB
        ChienDich cd = new ChienDich();
        cd.setMaChienDich(maChienDich);
        cd.setTenChienDich(name.trim());
        cd.setNganSach(budgetAmount);
        cd.setKenh(channelCode.trim().toUpperCase());
        cd.setNgayBatDau(startDate);
        cd.setNgayKetThuc(endDate);
        cd.setTrangThai("DANG_CHAY");
        cd.setMoTa(form.getMoTa());
        cd.setCreatedBy(nguoiTaoId);

        try {
            Long id = chienDichDAO.them(cd);
            form.setCampaignId(id);
            form.setMaChienDich(maChienDich);
            return id;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu chiến dịch: " + e.getMessage(), e);
            fieldErrors.put("general", "Có lỗi xảy ra khi lưu chiến dịch. Vui lòng thử lại.");
            return null;
        }
    }

    /**
     * AC3: Lấy danh sách chiến dịch kèm 3 chỉ số KPI (phân trang).
     */
    public Map<String, Object> layDanhSachChienDich(int page, int pageSize) {
        int trang = Math.max(1, page);
        int kichThuoc = pageSize > 0 ? pageSize : 20;
        int offset = (trang - 1) * kichThuoc;

        long totalItems = chienDichDAO.demTongSo();
        long totalPages = (totalItems + kichThuoc - 1) / kichThuoc;

        List<ChienDich> danhSachEntity = chienDichDAO.layDanhSachPhanTrang(offset, kichThuoc);
        List<ChienDichRowDTO> rows = new ArrayList<>();

        if (!danhSachEntity.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (ChienDich cd : danhSachEntity) {
                ids.add(cd.getId());
            }

            Map<Long, ChienDichThongKeDTO> metricsMap = chienDichDAO.layThongKeChoDanhSach(ids);

            for (ChienDich cd : danhSachEntity) {
                ChienDichDTO dto = chuyenEntitySangDTO(cd);
                ChienDichThongKeDTO metrics = metricsMap.getOrDefault(cd.getId(),
                        new ChienDichThongKeDTO(cd.getId(), 0, 0, BigDecimal.ZERO));
                rows.add(new ChienDichRowDTO(dto, metrics));
            }
        }

        Map<String, Object> ketQua = new HashMap<>();
        ketQua.put("campaignRows", rows);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", trang);
        pagination.put("pageSize", kichThuoc);
        pagination.put("totalItems", totalItems);
        pagination.put("totalPages", totalPages);
        ketQua.put("pagination", pagination);

        return ketQua;
    }

    /**
     * AC3: Lấy chi tiết chiến dịch kèm 3 chỉ số KPI.
     */
    public ChienDichRowDTO layChiTietChienDich(Long campaignId) {
        if (campaignId == null) return null;
        ChienDich cd = chienDichDAO.timTheoId(campaignId);
        if (cd == null) return null;

        ChienDichDTO dto = chuyenEntitySangDTO(cd);
        ChienDichThongKeDTO metrics = chienDichDAO.layThongKeChiTieu(campaignId);
        return new ChienDichRowDTO(dto, metrics);
    }

    /**
     * AC2: Lấy danh sách chiến dịch đang hoạt động cho dropdown chọn khi tạo/gán Lead.
     */
    public List<ChienDichDTO> layDanhSachChienDichHoatDong() {
        List<ChienDich> list = chienDichDAO.layDanhSachHoatDong();
        List<ChienDichDTO> dtoList = new ArrayList<>();
        for (ChienDich cd : list) {
            dtoList.add(chuyenEntitySangDTO(cd));
        }
        return dtoList;
    }

    /**
     * AC2: Kiểm tra chiến dịch có tồn tại và đang ở trạng thái hoạt động (DANG_CHAY hoặc NHAP) hay không.
     */
    public boolean laChienDichHoatDongHopLe(Long campaignId) {
        if (campaignId == null || campaignId <= 0) {
            return false;
        }
        ChienDich cd = chienDichDAO.timTheoId(campaignId);
        if (cd == null) {
            return false;
        }
        String tt = cd.getTrangThai();
        return "DANG_CHAY".equalsIgnoreCase(tt) || "NHAP".equalsIgnoreCase(tt);
    }

    /**
     * Xác định quyền hạn của người dùng đối với Chiến dịch (CampaignPermissions).
     */
    public ChienDichPermissionsDTO layQuyenNguoiDung(NguoiDung nguoiDung) {
        if (nguoiDung == null || !nguoiDung.dangHoatDong()) {
            return new ChienDichPermissionsDTO(false, false, false);
        }

        Set<VaiTroEnum> vaiTros = nguoiDung.getDanhSachVaiTroEnum();
        if (vaiTros == null || vaiTros.isEmpty()) {
            return new ChienDichPermissionsDTO(false, false, false);
        }

        // Admin và Director có toàn quyền
        if (vaiTros.contains(VaiTroEnum.ADMIN) || vaiTros.contains(VaiTroEnum.DIRECTOR) || nguoiDung.coVaiTro("ADMIN")) {
            return new ChienDichPermissionsDTO(true, true, true);
        }

        // Marketing có toàn quyền module Lead & phân bổ (F) -> được xem, tạo và xem chỉ số tổng hợp
        if (vaiTros.contains(VaiTroEnum.MARKETING)) {
            return new ChienDichPermissionsDTO(true, true, true);
        }

        // Team Lead có quyền F module Lead
        if (vaiTros.contains(VaiTroEnum.TEAM_LEAD)) {
            return new ChienDichPermissionsDTO(true, true, true);
        }

        // Sales Rep chỉ có quyền xem
        if (vaiTros.contains(VaiTroEnum.SALES_REP)) {
            return new ChienDichPermissionsDTO(true, false, false);
        }

        return new ChienDichPermissionsDTO(false, false, false);
    }

    /**
     * Chuyển Entity sang DTO.
     */
    public ChienDichDTO chuyenEntitySangDTO(ChienDich cd) {
        if (cd == null) return null;
        ChienDichDTO dto = new ChienDichDTO();
        dto.setCampaignId(cd.getId());
        dto.setMaChienDich(cd.getMaChienDich());
        dto.setName(cd.getTenChienDich());
        dto.setBudgetAmount(cd.getNganSach());
        dto.setCurrencyCode("VND");
        dto.setStartDate(cd.getNgayBatDau());
        dto.setEndDate(cd.getNgayKetThuc());
        dto.setChannelCode(cd.getKenh());
        dto.setChannelLabel(layNhanKenh(cd.getKenh()));
        dto.setTrangThai(cd.getTrangThai());
        dto.setMoTa(cd.getMoTa());

        if (cd.getNganSach() != null) {
            dto.setBudgetAmountStr(cd.getNganSach().toPlainString());
        }
        if (cd.getNgayBatDau() != null) {
            dto.setStartDateStr(cd.getNgayBatDau().format(DATE_FORMATTER));
        }
        if (cd.getNgayKetThuc() != null) {
            dto.setEndDateStr(cd.getNgayKetThuc().format(DATE_FORMATTER));
        }

        return dto;
    }

    /**
     * Tự động sinh mã chiến dịch duy nhất: CD-yyyyMMdd-XXXX.
     */
    private synchronized String sinhMaChienDich() {
        String datePrefix = "CD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        String ma = datePrefix + randomNum;

        int retry = 0;
        while (chienDichDAO.kiemTraTonTaiMa(ma) && retry < 10) {
            randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
            ma = datePrefix + randomNum;
            retry++;
        }
        return ma;
    }
}
