package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.DotNhapDuLieuDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.BaoCaoNhapLeadExcelDTO;
import vn.nhom10.crm.dto.DongExcelLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.util.ExcelLeadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý toàn bộ luồng Nhập Lead hàng loạt từ Excel.
 * Phục vụ Story S4-02:
 * - AC2: Đọc tệp mẫu, xem trước và báo lỗi theo từng dòng
 * - AC3: Bắt buộc mọi lead nhập vào phải có nguồn hợp lệ
 */
public class LeadImportService {

    private static final Logger LOGGER = Logger.getLogger(LeadImportService.class.getName());

    private final LeadDAO leadDAO;
    private final DanhMucBanHangDAO danhMucBanHangDAO;
    private final DotNhapDuLieuDAO dotNhapDuLieuDAO;
    private final LeadService leadService;

    public LeadImportService() {
        this(new LeadDAO(), new DanhMucBanHangDAO(), new DotNhapDuLieuDAO(), new LeadService());
    }

    public LeadImportService(LeadDAO leadDAO, DanhMucBanHangDAO danhMucBanHangDAO, DotNhapDuLieuDAO dotNhapDuLieuDAO, LeadService leadService) {
        this.leadDAO = leadDAO != null ? leadDAO : new LeadDAO();
        this.danhMucBanHangDAO = danhMucBanHangDAO != null ? danhMucBanHangDAO : new DanhMucBanHangDAO();
        this.dotNhapDuLieuDAO = dotNhapDuLieuDAO != null ? dotNhapDuLieuDAO : new DotNhapDuLieuDAO();
        this.leadService = leadService != null ? leadService : new LeadService();
    }

    /**
     * Thẩm định tệp Excel tải lên và sinh báo cáo xem trước (AC2 & AC3).
     * Chưa ghi dữ liệu vào CSDL ở bước này.
     *
     * @param is     luồng dữ liệu tệp Excel
     * @param tenTep tên tệp tải lên
     * @return Báo cáo xem trước gồm tổng dòng, số dòng hợp lệ, số dòng lỗi và chi tiết từng dòng
     */
    public BaoCaoNhapLeadExcelDTO thamDinhTepExcel(InputStream is, String tenTep) throws IOException {
        List<MucDanhMucDTO> dsNguon = danhMucBanHangDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true);
        List<DongExcelLeadDTO> dsDong = ExcelLeadUtil.docDanhSachLead(is, dsNguon);

        BaoCaoNhapLeadExcelDTO baoCao = new BaoCaoNhapLeadExcelDTO(tenTep);
        baoCao.napDanhSachDong(dsDong);
        return baoCao;
    }

    /**
     * Xác nhận nhập dữ liệu thực tế vào CSDL (AC2 & AC3).
     * Chỉ lưu các dòng hợp lệ, bỏ qua các dòng lỗi và lưu vết vào dot_nhap_du_lieu, loi_nhap_du_lieu.
     *
     * @param baoCao          báo cáo xem trước đã được người dùng kiểm tra
     * @param nguoiThucHienId ID người dùng thực hiện nhập (Marketing / Admin)
     * @return Báo cáo tổng kết sau khi lưu
     */
    public BaoCaoNhapLeadExcelDTO thucHienNhapDuLieu(BaoCaoNhapLeadExcelDTO baoCao, Long nguoiThucHienId) {
        if (baoCao == null) {
            return new BaoCaoNhapLeadExcelDTO();
        }

        baoCao.setDaThucHienNhap(true);

        for (DongExcelLeadDTO dong : baoCao.getDanhSachTatCaDong()) {
            if (!dong.isHopLe()) {
                dong.setDaNhap(false);
                dong.setThanhCong(false);
                dong.setGhiChuKetQua("Bỏ qua do dữ liệu không hợp lệ: " + dong.getChuoiLoi());
                baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
                continue;
            }

            try {
                // Tạo đối tượng Lead từ dòng dữ liệu hợp lệ
                Lead lead = new Lead();
                lead.setMaLead(leadService.sinhMaLeadMoi());
                lead.setHoTen(dong.getHoTen().trim());

                // AC3: Gắn nguồn lead bắt buộc
                lead.setNguonLeadId(dong.getNguonLeadId());

                if (dong.getEmail() != null && !dong.getEmail().isBlank()) {
                    String em = dong.getEmail().trim();
                    lead.setEmail(em);
                    lead.setEmailChuanHoa(em.toLowerCase());
                }

                if (dong.getSoDienThoai() != null && !dong.getSoDienThoai().isBlank()) {
                    String sdt = dong.getSoDienThoai().trim();
                    lead.setSoDienThoai(sdt);
                    lead.setSoDienThoaiChuanHoa(sdt.replaceAll("[^0-9]", ""));
                }

                if (dong.getTenCongTy() != null && !dong.getTenCongTy().isBlank()) {
                    String cty = dong.getTenCongTy().trim();
                    lead.setTenCongTy(cty);
                    lead.setTenCongTyChuanHoa(cty.toUpperCase());
                }

                lead.setMucDoQuanTam(dong.getMucDoQuanTam() != null ? dong.getMucDoQuanTam() : "TRUNG_BINH");
                lead.setNhuCauQuanTam(dong.getNhuCauQuanTam());
                lead.setTrangThai(TrangThaiLeadEnum.MOI);
                lead.setDiem(0);
                lead.setQuaSla(false);
                lead.setCreatedBy(nguoiThucHienId);
                lead.setCreatedAt(LocalDateTime.now());

                Long leadId = leadDAO.them(lead);
                dong.setIdLead(leadId);
                dong.setMaLead(lead.getMaLead());
                dong.setGhiChuKetQua("Đã thêm mới thành công vào CRM (" + lead.getMaLead() + ")");

                baoCao.capNhatKetQuaNhap(dong, true);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi nhập dòng " + dong.getSoDong() + " (" + dong.getHoTen() + "): " + e.getMessage(), e);
                dong.themLoi("Lỗi cơ sở dữ liệu khi lưu: " + e.getMessage());
                baoCao.capNhatKetQuaNhap(dong, false);
            }
        }

        // Lưu vết lịch sử đợt nhập vào dot_nhap_du_lieu và loi_nhap_du_lieu
        dotNhapDuLieuDAO.luuDotNhapLead(baoCao, nguoiThucHienId);

        baoCao.setThongDiep(String.format("Kết quả nhập danh sách khách hàng tiềm năng: %d/%d lead nhập thành công, %d dòng lỗi bị bỏ qua.",
                baoCao.getSoDongThanhCong(), baoCao.getTongSoDong(), baoCao.getSoDongLoi()));

        return baoCao;
    }
}
