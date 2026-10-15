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
    /**
     * Lấy danh sách nguồn Lead hiện hành đang kích hoạt trong CSDL.
     */
    public List<MucDanhMucDTO> layDanhSachNguonLeadHienHanh() {
        return danhMucBanHangDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true);
    }

    /**
     * Tạo tệp mẫu Excel với danh mục nguồn Lead thực tế từ CSDL.
     */
    public byte[] taoTepMauExcel() throws IOException {
        List<MucDanhMucDTO> dsNguon = layDanhSachNguonLeadHienHanh();
        return ExcelLeadUtil.taoTepMauExcel(dsNguon);
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
        List<MucDanhMucDTO> dsNguon = layDanhSachNguonLeadHienHanh();
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
        baoCao.khoiTaoTienTrinhNhap();

        for (DongExcelLeadDTO dong : baoCao.getDanhSachTatCaDong()) {
            if (!dong.isHopLe()) {
                dong.setDaNhap(false);
                dong.setThanhCong(false);
                dong.setGhiChuKetQua("Bỏ qua do dữ liệu không hợp lệ: " + dong.getChuoiLoi());
                continue;
            }

            try {
                // Tạo đối tượng Lead từ dòng dữ liệu hợp lệ
                Lead lead = new Lead();
                lead.setMaLead(leadService.sinhMaLeadMoi());
                lead.setHoTen(dong.getHoTen().trim());

                // AC3: Gắn nguồn lead bắt buộc từ ID thực tế đã đối chiếu
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
                if (leadId == null || leadId <= 0) {
                    LOGGER.warning("Không thể thêm Lead dòng " + dong.getSoDong() + ": DAO trả về ID không hợp lệ");
                    dong.themLoi("Lưu vào cơ sở dữ liệu thất bại: Không thể cấp mã khách hàng tiềm năng hợp lệ.");
                    dong.setGhiChuKetQua("Lưu thất bại: Hệ thống không tạo được bản ghi.");
                    baoCao.ghiNhanLoiLuuDb(dong);
                } else {
                    dong.setIdLead(leadId);
                    dong.setMaLead(lead.getMaLead());
                    dong.setGhiChuKetQua("Đã thêm mới thành công vào CRM (" + lead.getMaLead() + ")");
                    baoCao.ghiNhanThanhCong(dong);
                }

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi nhập dòng " + dong.getSoDong() + " (" + dong.getHoTen() + "): " + e.getMessage(), e);
                dong.themLoi("Lưu vào cơ sở dữ liệu thất bại do ràng buộc dữ liệu hoặc lỗi hệ thống.");
                dong.setGhiChuKetQua("Lưu thất bại: Lỗi hệ thống khi lưu trữ.");
                baoCao.ghiNhanLoiLuuDb(dong);
            }
        }

        baoCao.hoanTatTienTrinhNhap();

        // Lưu vết lịch sử đợt nhập vào dot_nhap_du_lieu và loi_nhap_du_lieu
        dotNhapDuLieuDAO.luuDotNhapLead(baoCao, nguoiThucHienId);

        if (baoCao.getSoDongThatBai() == 0) {
            baoCao.setThongDiep(String.format("Nhập hoàn tất toàn bộ %d/%d lead vào hệ thống CRM.",
                    baoCao.getSoDongThanhCong(), baoCao.getTongSoDong()));
        } else if (baoCao.getSoDongThanhCong() > 0) {
            baoCao.setThongDiep(String.format("Kết quả nhập danh sách khách hàng tiềm năng: %d/%d lead nhập thành công, %d dòng lỗi bị bỏ qua.",
                    baoCao.getSoDongThanhCong(), baoCao.getTongSoDong(), baoCao.getSoDongThatBai()));
        } else {
            baoCao.setThongDiep(String.format("Không thể nhập dữ liệu: 0/%d lead được tạo, toàn bộ %d dòng đều gặp lỗi.",
                    baoCao.getTongSoDong(), baoCao.getSoDongThatBai()));
        }

        return baoCao;
    }
}
