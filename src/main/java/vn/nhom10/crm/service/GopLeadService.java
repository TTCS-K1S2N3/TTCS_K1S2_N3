package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.GopLeadDAO;
import vn.nhom10.crm.dto.DuplicatePermissionsDTO;
import vn.nhom10.crm.dto.KetQuaGopLeadDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.SoSanhLeadDTO;
import vn.nhom10.crm.dto.UngVienTrungLeadDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.DuplicateLeadDetector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý toàn bộ nghiệp vụ Cảnh báo và Gộp Lead trùng (Story S4-04).
 * Tiêu chí chấp nhận (AC):
 * - AC1: Phát hiện Lead trùng theo email, số điện thoại và tên công ty.
 * - AC2: Lead trùng với khách hàng đã có được gợi ý gắn thẳng vào khách hàng đó.
 * - AC3: Gộp giữ nguyên lịch sử (hoạt động, công việc, cơ hội) của cả hai bản ghi trong DB Transaction.
 */
public class GopLeadService {

    private static final Logger LOGGER = Logger.getLogger(GopLeadService.class.getName());

    private final GopLeadDAO gopLeadDAO;
    private final NhatKyThayDoiService nhatKyThayDoiService;

    public GopLeadService() {
        this(new GopLeadDAO(), new NhatKyThayDoiService());
    }

    public GopLeadService(GopLeadDAO gopLeadDAO, NhatKyThayDoiService nhatKyThayDoiService) {
        this.gopLeadDAO = gopLeadDAO != null ? gopLeadDAO : new GopLeadDAO();
        this.nhatKyThayDoiService = nhatKyThayDoiService != null ? nhatKyThayDoiService : new NhatKyThayDoiService();
    }

    // =========================================================================
    // PHÂN QUYỀN VÀ KIỂM SOÁT PHẠM VI DỮ LIỆU (RBAC & DATA SCOPE)
    // =========================================================================

    /**
     * Kiểm tra người dùng có quyền xem danh sách cảnh báo trùng hay không.
     */
    public boolean coQuyenXem(NguoiDungDTO user) {
        if (user == null || user.getVaiTro() == null) {
            return false;
        }
        VaiTroEnum vt = user.getVaiTro();
        return vt == VaiTroEnum.ADMIN
                || vt == VaiTroEnum.DIRECTOR
                || vt == VaiTroEnum.MARKETING
                || vt == VaiTroEnum.TEAM_LEAD
                || vt == VaiTroEnum.SALES_REP;
    }

    /**
     * Kiểm tra quyền thực hiện thao tác gộp Lead (AC3) và gắn Khách hàng (AC2).
     * Theo workbook và quyết định duyệt Q2: Marketing (F module Lead) và Team Lead trở lên.
     */
    public boolean coQuyenThaoTac(NguoiDungDTO user) {
        if (user == null || user.getVaiTro() == null) {
            return false;
        }
        VaiTroEnum vt = user.getVaiTro();
        return vt == VaiTroEnum.ADMIN
                || vt == VaiTroEnum.DIRECTOR
                || vt == VaiTroEnum.MARKETING
                || vt == VaiTroEnum.TEAM_LEAD;
    }

    /**
     * Kiểm tra chi tiết thẩm quyền gộp đối với 2 Lead cụ thể theo Data Scope (Q2).
     */
    public boolean kiemTraQuyenGop(NguoiDungDTO user, Lead sourceLead, Lead targetLead) {
        if (!coQuyenThaoTac(user)) {
            return false;
        }
        VaiTroEnum vt = user.getVaiTro();
        if (vt == VaiTroEnum.ADMIN || vt == VaiTroEnum.DIRECTOR || vt == VaiTroEnum.MARKETING
                || user.getPhamViToiDa() == PhamViDuLieu.TOAN_BO) {
            return true;
        }
        // Trưởng nhóm: phải có ít nhất 1 trong 2 lead thuộc nhóm quản lý của mình
        if (user.getNhomKinhDoanhId() != null) {
            boolean srcThuocNhom = sourceLead != null && user.getNhomKinhDoanhId().equals(sourceLead.getNhomPhanBoId());
            boolean dstThuocNhom = targetLead != null && user.getNhomKinhDoanhId().equals(targetLead.getNhomPhanBoId());
            return srcThuocNhom || dstThuocNhom;
        }
        return false;
    }

    public DuplicatePermissionsDTO layQuyenThaoTac(NguoiDungDTO user, Lead sourceLead, Lead targetLead) {
        boolean canView = coQuyenXem(user);
        boolean canLink = coQuyenThaoTac(user);
        boolean canMerge = kiemTraQuyenGop(user, sourceLead, targetLead);
        return new DuplicatePermissionsDTO(canView, canLink, canMerge);
    }

    // =========================================================================
    // AC1: PHÁT HIỆN LEAD TRÙNG THEO EMAIL, SỐ ĐIỆN THOẠI VÀ TÊN CÔNG TY
    // =========================================================================

    /**
     * Quét và phát hiện các ứng viên Lead và Khách hàng trùng lặp với Lead đang xét (AC1, AC2).
     */
    public List<UngVienTrungLeadDTO> quetUngVienTrung(Long leadId, NguoiDungDTO user) {
        if (!coQuyenXem(user) || leadId == null) {
            return Collections.emptyList();
        }
        try {
            Lead lead = gopLeadDAO.layChiTietLead(leadId);
            if (lead == null) {
                return Collections.emptyList();
            }

            Set<Long> nhomIds = layDanhSachNhomIds(user);
            List<UngVienTrungLeadDTO> dsLead = gopLeadDAO.timUngVienLeadTrung(lead, user, nhomIds);
            List<UngVienTrungLeadDTO> dsCustomer = gopLeadDAO.timUngVienCustomerTrung(lead, user, nhomIds);

            List<UngVienTrungLeadDTO> tatCa = new ArrayList<>();
            tatCa.addAll(dsLead);
            tatCa.addAll(dsCustomer);
            return tatCa;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi quét ứng viên trùng cho Lead #" + leadId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Tách riêng danh sách Lead ứng viên trùng (AC1).
     */
    public List<UngVienTrungLeadDTO> layLeadUngVien(Long leadId, NguoiDungDTO user) {
        if (!coQuyenXem(user) || leadId == null) return Collections.emptyList();
        try {
            Lead lead = gopLeadDAO.layChiTietLead(leadId);
            if (lead == null) return Collections.emptyList();
            Set<Long> nhomIds = layDanhSachNhomIds(user);
            return gopLeadDAO.timUngVienLeadTrung(lead, user, nhomIds);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi lấy danh sách Lead ứng viên: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Tách riêng danh sách Khách hàng gợi ý (AC2).
     */
    public List<UngVienTrungLeadDTO> layCustomerUngVien(Long leadId, NguoiDungDTO user) {
        if (!coQuyenXem(user) || leadId == null) return Collections.emptyList();
        try {
            Lead lead = gopLeadDAO.layChiTietLead(leadId);
            if (lead == null) return Collections.emptyList();
            Set<Long> nhomIds = layDanhSachNhomIds(user);
            return gopLeadDAO.timUngVienCustomerTrung(lead, user, nhomIds);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi lấy danh sách Khách hàng ứng viên: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Kiểm tra trùng lặp nhanh khi người dùng nhập dữ liệu trên form.
     */
    public List<UngVienTrungLeadDTO> kiemTraTrungKhiNhap(String email, String phone, String tenCongTy, Long currentLeadId, NguoiDungDTO user) {
        if (!coQuyenXem(user)) return Collections.emptyList();
        Lead temp = new Lead();
        temp.setId(currentLeadId != null ? currentLeadId : -999L);
        temp.setEmail(email);
        temp.setEmailChuanHoa(DuplicateLeadDetector.chuanHoaEmail(email));
        temp.setSoDienThoai(phone);
        temp.setSoDienThoaiChuanHoa(DuplicateLeadDetector.chuanHoaSoDienThoai(phone));
        temp.setTenCongTy(tenCongTy);
        temp.setTenCongTyChuanHoa(DuplicateLeadDetector.chuanHoaTenCongTy(tenCongTy));

        try {
            Set<Long> nhomIds = layDanhSachNhomIds(user);
            List<UngVienTrungLeadDTO> dsLead = gopLeadDAO.timUngVienLeadTrung(temp, user, nhomIds);
            List<UngVienTrungLeadDTO> dsCustomer = gopLeadDAO.timUngVienCustomerTrung(temp, user, nhomIds);
            List<UngVienTrungLeadDTO> tatCa = new ArrayList<>();
            tatCa.addAll(dsLead);
            tatCa.addAll(dsCustomer);
            return tatCa;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi kiểm tra trùng khi nhập: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // =========================================================================
    // AC2: GỢI Ý VÀ GẮN THẲNG LEAD VÀO KHÁCH HÀNG ĐÃ CÓ
    // =========================================================================

    /**
     * Thực hiện liên kết Lead với Khách hàng hiện có (AC2).
     * Cập nhật lead.khach_hang_chuyen_doi_id = customerId, bảo toàn trạng thái Lead (không tự đổi sang DA_CHUYEN_DOI).
     */
    public boolean ganKhachHang(Long leadId, Long customerId, NguoiDungDTO user, String ipAddress, String userAgent) {
        if (!coQuyenThaoTac(user)) {
            LOGGER.warning("Từ chối thao tác: Người dùng không có quyền gắn Lead vào Khách hàng.");
            return false;
        }
        if (leadId == null || customerId == null || leadId <= 0 || customerId <= 0) {
            return false;
        }

        try {
            Lead lead = gopLeadDAO.layChiTietLead(leadId);
            if (lead == null) {
                return false;
            }

            boolean ok = gopLeadDAO.ganKhachHang(leadId, customerId);
            if (ok) {
                // Ghi nhận nhật ký kiểm toán (Audit Log)
                try {
                    nhatKyThayDoiService.ghiNhatKyThayDoi(
                            user.getId(),
                            user.getHoTen(),
                            user.getEmail(),
                            LoaiDoiTuongNhayCam.QUYEN_SO_HUU,
                            lead.getMaLead(),
                            lead.getHoTen(),
                            "khach_hang_chuyen_doi_id",
                            lead.getKhachHangChuyenDoiId() != null ? String.valueOf(lead.getKhachHangChuyenDoiId()) : "null",
                            String.valueOf(customerId),
                            HanhDongThayDoi.CAP_NHAT,
                            "Gắn Lead '" + lead.getHoTen() + "' (" + lead.getMaLead() + ") vào Khách hàng ID #" + customerId + " do phát hiện trùng lặp thông tin.",
                            ipAddress,
                            userAgent
                    );
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Không thể ghi audit log gắn khách hàng: " + ex.getMessage());
                }
            }
            return ok;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi gắn Khách hàng cho Lead #" + leadId + ": " + e.getMessage(), e);
            return false;
        }
    }

    // =========================================================================
    // AC3: GỘP GIỮ NGUYÊN LỊCH SỬ CỦA CẢ HAI BẢN GHI
    // =========================================================================

    /**
     * Chuẩn bị dữ liệu hiển thị so sánh cạnh nhau trước khi gộp (AC3).
     */
    public SoSanhLeadDTO layDuLieuSoSanh(Long sourceLeadId, Long targetLeadId, NguoiDungDTO user) {
        if (sourceLeadId == null || targetLeadId == null) {
            return null;
        }
        if (sourceLeadId.equals(targetLeadId)) {
            return null;
        }

        try {
            Lead src = gopLeadDAO.layChiTietLead(sourceLeadId);
            Lead dst = gopLeadDAO.layChiTietLead(targetLeadId);
            if (src == null || dst == null) {
                return null;
            }

            SoSanhLeadDTO soSanh = new SoSanhLeadDTO(src, dst);

            // Kiểm tra các lý do trùng lặp
            List<String> matchReasons = DuplicateLeadDetector.kiemTraTrungLead(src, dst);
            soSanh.setMatchReasons(matchReasons);

            // Đếm thống kê lịch sử hai bản ghi (AC3)
            GopLeadDAO.ThongKeLichSuLead tkSrc = gopLeadDAO.layThongKeLichSu(sourceLeadId);
            soSanh.setSoHoatDongNguon(tkSrc.soHoatDong);
            soSanh.setSoCongViecNguon(tkSrc.soCongViec);
            soSanh.setSoCoHoiNguon(tkSrc.soCoHoi);

            GopLeadDAO.ThongKeLichSuLead tkDst = gopLeadDAO.layThongKeLichSu(targetLeadId);
            soSanh.setSoHoatDongDich(tkDst.soHoatDong);
            soSanh.setSoCongViecDich(tkDst.soCongViec);
            soSanh.setSoCoHoiDich(tkDst.soCoHoi);

            // Quyền gộp
            boolean canMerge = kiemTraQuyenGop(user, src, dst);
            soSanh.setCanMerge(canMerge);

            if (src.getNguoiPhuTrachId() != null && dst.getNguoiPhuTrachId() != null
                    && !src.getNguoiPhuTrachId().equals(dst.getNguoiPhuTrachId())) {
                soSanh.setThongDiepCanhBao("Cảnh báo: Hai Lead này đang được phụ trách bởi hai nhân viên khác nhau (" +
                        src.getTenNguoiPhuTrach() + " và " + dst.getTenNguoiPhuTrach() + ").");
            }

            return soSanh;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi lấy dữ liệu so sánh Lead: " + e.getMessage());
            return null;
        }
    }

    /**
     * Thực hiện gộp Lead trong DB Transaction nguyên tử (AC3).
     */
    public KetQuaGopLeadDTO thucHienGopLead(Long sourceLeadId, Long targetLeadId, String lyDo,
                                          NguoiDungDTO user, String ipAddress, String userAgent) {
        // 1. Kiểm tra thẩm quyền
        if (!coQuyenThaoTac(user)) {
            return KetQuaGopLeadDTO.thatBai("Bạn không có quyền thực hiện gộp Lead.");
        }

        // 2. Validate tham số đầu vào
        if (sourceLeadId == null || targetLeadId == null) {
            return KetQuaGopLeadDTO.thatBai("Vui lòng chọn đầy đủ Lead nguồn và Lead đích để gộp.");
        }
        if (sourceLeadId.equals(targetLeadId)) {
            return KetQuaGopLeadDTO.thatBai("Không thể gộp một Lead vào chính nó.");
        }
        if (lyDo == null || lyDo.trim().isEmpty()) {
            return KetQuaGopLeadDTO.thatBai("Vui lòng nhập lý do thực hiện gộp Lead.");
        }

        try {
            // 3. Kiểm tra bản ghi tồn tại
            Lead src = gopLeadDAO.layChiTietLead(sourceLeadId);
            Lead dst = gopLeadDAO.layChiTietLead(targetLeadId);
            if (src == null) {
                return KetQuaGopLeadDTO.thatBai("Lead nguồn (#" + sourceLeadId + ") không tồn tại.");
            }
            if (dst == null) {
                return KetQuaGopLeadDTO.thatBai("Lead đích (#" + targetLeadId + ") không tồn tại.");
            }

            // 4. Kiểm tra Data Scope
            if (!kiemTraQuyenGop(user, src, dst)) {
                return KetQuaGopLeadDTO.thatBai("Từ chối thao tác: Bạn chỉ được thực hiện gộp Lead trong phạm vi quản lý của mình.");
            }

            // 5. Kiểm tra trạng thái đã gộp
            if (src.getGopVaoLeadId() != null || "DA_GOP".equalsIgnoreCase(src.getTrangThai() != null ? src.getTrangThai().getMa() : "")) {
                return KetQuaGopLeadDTO.thatBai("Lead nguồn (#" + sourceLeadId + ") đã được gộp trước đó.");
            }
            if (dst.getGopVaoLeadId() != null || "DA_GOP".equalsIgnoreCase(dst.getTrangThai() != null ? dst.getTrangThai().getMa() : "")) {
                return KetQuaGopLeadDTO.thatBai("Lead đích (#" + targetLeadId + ") đã bị gộp trước đó, không thể làm bản ghi giữ lại.");
            }

            // Chuẩn bị snapshot JSON kiểm toán
            String duLieuSnapshotJson = String.format(
                    "{\"nguon\":{\"id\":%d,\"ma\":\"%s\",\"ten\":\"%s\",\"email\":\"%s\",\"sdt\":\"%s\",\"cty\":\"%s\",\"owner_id\":%d}," +
                    "\"dich\":{\"id\":%d,\"ma\":\"%s\",\"ten\":\"%s\",\"email\":\"%s\",\"sdt\":\"%s\",\"cty\":\"%s\",\"owner_id\":%d}," +
                    "\"ly_do\":\"%s\"}",
                    src.getId(), src.getMaLead(), src.getHoTen(),
                    src.getEmail() != null ? src.getEmail() : "",
                    src.getSoDienThoai() != null ? src.getSoDienThoai() : "",
                    src.getTenCongTy() != null ? src.getTenCongTy() : "",
                    src.getNguoiPhuTrachId() != null ? src.getNguoiPhuTrachId() : 0,
                    dst.getId(), dst.getMaLead(), dst.getHoTen(),
                    dst.getEmail() != null ? dst.getEmail() : "",
                    dst.getSoDienThoai() != null ? dst.getSoDienThoai() : "",
                    dst.getTenCongTy() != null ? dst.getTenCongTy() : "",
                    dst.getNguoiPhuTrachId() != null ? dst.getNguoiPhuTrachId() : 0,
                    lyDo.trim().replace("\"", "\\\"")
            );

            // 6. Thực hiện gộp trong Transaction DB nguyên tử
            KetQuaGopLeadDTO ketQua = gopLeadDAO.thucHienGopLead(sourceLeadId, targetLeadId, user.getId(), lyDo, duLieuSnapshotJson);

            // 7. Ghi nhận nhật ký kiểm toán (Audit Log)
            try {
                String chiTietAudit = "Gộp Lead '" + src.getHoTen() + "' (" + src.getMaLead() + ") vào Lead '"
                        + dst.getHoTen() + "' (" + dst.getMaLead() + "). Lý do: " + lyDo.trim()
                        + ". Chuyển toàn bộ " + ketQua.getSoHoatDongDaChuyen() + " hoạt động, "
                        + ketQua.getSoCongViecDaChuyen() + " công việc, "
                        + ketQua.getSoCoHoiDaChuyen() + " cơ hội sang Lead #" + dst.getId();

                nhatKyThayDoiService.ghiNhatKyThayDoi(
                        user.getId(),
                        user.getHoTen(),
                        user.getEmail(),
                        LoaiDoiTuongNhayCam.QUYEN_SO_HUU,
                        src.getMaLead(),
                        src.getHoTen(),
                        "trang_thai / gop_vao_lead_id",
                        src.getHoTen(),
                        dst.getHoTen(),
                        HanhDongThayDoi.CAP_NHAT,
                        chiTietAudit,
                        ipAddress,
                        userAgent
                );
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "Không thể ghi nhật ký audit log gộp lead: " + ex.getMessage());
            }

            return ketQua;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thực thi SQL gộp Lead: " + e.getMessage(), e);
            return KetQuaGopLeadDTO.thatBai("Gộp Lead thất bại do lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private Set<Long> layDanhSachNhomIds(NguoiDungDTO user) {
        Set<Long> set = new HashSet<>();
        if (user != null && user.getNhomKinhDoanhId() != null && user.getNhomKinhDoanhId() > 0) {
            set.add(user.getNhomKinhDoanhId());
        }
        return set;
    }
}
