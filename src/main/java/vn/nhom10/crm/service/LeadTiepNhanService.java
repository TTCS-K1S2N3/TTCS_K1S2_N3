package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.*;
import vn.nhom10.crm.dto.DanhMucLyDoTuChoiDTO;
import vn.nhom10.crm.dto.LeadChiTietViewDTO;
import vn.nhom10.crm.dto.LeadChiTietViewDTO.TrangThaiSlaEnum;
import vn.nhom10.crm.dto.XuLyLeadRequestDTO;
import vn.nhom10.crm.model.*;
import vn.nhom10.crm.util.DatabaseConnection;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ tiếp nhận/từ chối Lead và kiểm tra ràng buộc SLA phản hồi (Story S4-07).
 * Đáp ứng đầy đủ các tiêu chí chấp nhận:
 * - AC1: Nhân viên nhận lead -> chuyển sang Đang chăm sóc (DANG_CHAM_SOC).
 * - AC2: Từ chối bắt buộc chọn danh mục + nhập lý do giải thích, trả lead về hàng chờ phân bổ trong 1 transaction.
 *        Cấm từ chối sau khi đã nhận (Q3).
 * - AC3: Quá SLA phản hồi mà chưa liên hệ thì lead được gắn cờ (qua_sla = 1) và báo cho trưởng nhóm qua chuông hệ thống.
 */
public class LeadTiepNhanService {

    private static final Logger LOGGER = Logger.getLogger(LeadTiepNhanService.class.getName());

    public static final String CAU_HINH_SLA_LEAD_PHUT = "SLA_PHAN_HOI_LEAD_PHUT";
    public static final int SLA_LEAD_PHUT_MAC_DINH = 1440; // 24 giờ liên tục theo đề xuất P1
    public static final long LOAI_THONG_BAO_LEAD_QUA_SLA = 8L;

    private final LeadDAO leadDAO;
    private final LichSuPhanBoLeadDAO lichSuPhanBoLeadDAO;
    private final HoatDongDAO hoatDongDAO;
    private final ThongBaoDAO thongBaoDAO;
    private final CauHinhHeThongDAO cauHinhHeThongDAO;
    private final NguoiDungDAO nguoiDungDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final PermissionService permissionService;

    public LeadTiepNhanService() {
        this(new LeadDAO(), new LichSuPhanBoLeadDAO(), new HoatDongDAO(), new ThongBaoDAO(),
                new CauHinhHeThongDAO(), new NguoiDungDAO(), new NhomKinhDoanhDAO(), PermissionService.getInstance());
    }

    public LeadTiepNhanService(LeadDAO leadDAO, LichSuPhanBoLeadDAO lichSuPhanBoLeadDAO, HoatDongDAO hoatDongDAO,
                               ThongBaoDAO thongBaoDAO, CauHinhHeThongDAO cauHinhHeThongDAO, NguoiDungDAO nguoiDungDAO,
                               NhomKinhDoanhDAO nhomKinhDoanhDAO, PermissionService permissionService) {
        this.leadDAO = (leadDAO != null) ? leadDAO : new LeadDAO();
        this.lichSuPhanBoLeadDAO = (lichSuPhanBoLeadDAO != null) ? lichSuPhanBoLeadDAO : new LichSuPhanBoLeadDAO();
        this.hoatDongDAO = (hoatDongDAO != null) ? hoatDongDAO : new HoatDongDAO();
        this.thongBaoDAO = (thongBaoDAO != null) ? thongBaoDAO : new ThongBaoDAO();
        this.cauHinhHeThongDAO = (cauHinhHeThongDAO != null) ? cauHinhHeThongDAO : new CauHinhHeThongDAO();
        this.nguoiDungDAO = (nguoiDungDAO != null) ? nguoiDungDAO : new NguoiDungDAO();
        this.nhomKinhDoanhDAO = (nhomKinhDoanhDAO != null) ? nhomKinhDoanhDAO : new NhomKinhDoanhDAO();
        this.permissionService = (permissionService != null) ? permissionService : PermissionService.getInstance();
    }

    /**
     * Lấy chi tiết Lead theo ID và người dùng đăng nhập, kèm tính toán trạng thái SLA và quyền thao tác (LEAD07_VIEW).
     */
    public LeadChiTietViewDTO layChiTietLeadChoNguoiDung(Long leadId, NguoiDung nguoiDung) {
        if (leadId == null || leadId <= 0) {
            throw new IllegalArgumentException("Mã định danh Lead không hợp lệ");
        }
        if (nguoiDung == null) {
            throw new LoiPhanQuyenException("Yêu cầu đăng nhập để truy cập thông tin Lead");
        }

        Lead lead = leadDAO.timTheoId(leadId);
        if (lead == null) {
            throw new LoiKhongTimThayException("Không tìm thấy thông tin Lead với mã ID=" + leadId);
        }

        // Kiểm tra phân quyền truy cập theo Data Scope
        kiemTraQuyenXemLead(lead, nguoiDung);

        LeadChiTietViewDTO view = new LeadChiTietViewDTO();
        view.setLead(lead);

        // Kiểm tra xem nhân viên hiện tại có quyền Nhận hoặc Từ chối không
        boolean laNguoiPhuTrach = (lead.getNguoiPhuTrachId() != null && lead.getNguoiPhuTrachId().equals((long) nguoiDung.getId()));
        boolean dangChoNhan = (lead.getTrangThai() == TrangThaiLeadEnum.DA_PHAN_BO || lead.getTrangThai() == TrangThaiLeadEnum.MOI);

        view.setCoTheNhan(laNguoiPhuTrach && dangChoNhan);
        view.setCoTheTuChoi(laNguoiPhuTrach && dangChoNhan);

        // Kiểm tra điều kiện và quyền Chuyển đổi Lead (Story S4-08)
        boolean dangChamSoc = (lead.getTrangThai() == TrangThaiLeadEnum.DANG_CHAM_SOC);
        boolean coQuyenChuyenDoi = LeadChuyenDoiService.kiemTraQuyenChuyenDoi(nguoiDung, lead);
        view.setCoTheChuyenDoi(dangChamSoc && coQuyenChuyenDoi);

        // Xác định trạng thái SLA
        LocalDateTime now = LocalDateTime.now();
        TrangThaiSlaEnum ttSla = xacDinhTrangThaiSla(lead, now);
        view.setTrangThaiSla(ttSla);

        // Nếu phát hiện quá hạn mà DB chưa gắn cờ, tự động đồng bộ ngay
        if (ttSla == TrangThaiSlaEnum.QUA_HAN && !lead.isQuaSla()) {
            leadDAO.capNhatQuaSla(lead.getId(), true);
            lead.setQuaSla(true);
            guiThongBaoQuaSlaChoTruongNhom(lead, now);
        }

        view.setDanhMucLyDoTuChoi(DanhMucLyDoTuChoiDTO.layDanhMucMacDinh());
        view.setSoThongBaoChuaDoc(thongBaoDAO.demThongBaoChuaDoc((long) nguoiDung.getId()));

        return view;
    }

    /**
     * Thực hiện Nhận Lead (AC1 của S4-07):
     * Cập nhật lead sang trạng thái DANG_CHAM_SOC và ghi nhận thời điểm duoc_nhan_luc.
     */
    public Lead nhanLead(Long leadId, NguoiDung nguoiDung) {
        if (leadId == null || leadId <= 0) {
            throw new IllegalArgumentException("Mã định danh Lead không hợp lệ");
        }
        if (nguoiDung == null) {
            throw new LoiPhanQuyenException("Yêu cầu đăng nhập để tiếp nhận Lead");
        }

        Lead lead = leadDAO.timTheoId(leadId);
        if (lead == null) {
            throw new LoiKhongTimThayException("Lead không tồn tại");
        }

        // Kiểm tra ownership: Chỉ nhân viên đang được phân lead mới được nhận
        if (lead.getNguoiPhuTrachId() == null || !lead.getNguoiPhuTrachId().equals((long) nguoiDung.getId())) {
            throw new LoiPhanQuyenException("Bạn không có quyền tiếp nhận Lead này vì không được phân công");
        }

        // Chặn nhận lại hoặc nhận khi trạng thái không còn là DA_PHAN_BO hoặc MOI
        if (lead.getTrangThai() != TrangThaiLeadEnum.DA_PHAN_BO && lead.getTrangThai() != TrangThaiLeadEnum.MOI) {
            throw new IllegalStateException("Lead đã thay đổi trạng thái hoặc đã được tiếp nhận trước đó");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean thanhCong = leadDAO.nhanLead(leadId, (long) nguoiDung.getId(), now);
        if (!thanhCong) {
            throw new IllegalStateException("Không thể tiếp nhận Lead do dữ liệu đã thay đổi đồng thời");
        }

        // Ghi nhận lịch sử phân bổ
        try {
            LichSuPhanBoLead ls = new LichSuPhanBoLead(
                    leadId,
                    (long) nguoiDung.getId(),
                    (long) nguoiDung.getId(),
                    lead.getNhomPhanBoId(),
                    "NHAN",
                    "Nhân viên tiếp nhận chăm sóc lead",
                    (long) nguoiDung.getId()
            );
            ls.setCreatedAt(now);
            lichSuPhanBoLeadDAO.them(ls);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi ghi lịch sử nhận lead id=" + leadId + ": " + e.getMessage(), e);
        }

        lead.setTrangThai(TrangThaiLeadEnum.DANG_CHAM_SOC);
        lead.setDuocNhanLuc(now);
        return lead;
    }

    /**
     * Thực hiện Từ chối Lead (AC2 của S4-07):
     * Bắt buộc có lý do (danh mục + giải thích chi tiết), trả lead về hàng chờ phân bổ trong 1 transaction.
     * Tuân thủ Q3: Cấm từ chối sau khi đã nhận lead (trạng thái DANG_CHAM_SOC).
     */
    public boolean tuChoiLead(XuLyLeadRequestDTO request, NguoiDung nguoiDung) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu yêu cầu từ chối không được để trống");
        }
        if (nguoiDung == null) {
            throw new LoiPhanQuyenException("Yêu cầu đăng nhập để từ chối Lead");
        }

        Map<String, String> loi = request.kiemTraHopLe();
        if (!loi.isEmpty()) {
            throw new IllegalArgumentException(loi.values().iterator().next());
        }

        Lead lead = leadDAO.timTheoId(request.getLeadId());
        if (lead == null) {
            throw new LoiKhongTimThayException("Lead không tồn tại");
        }

        // Kiểm tra quyền ownership
        if (lead.getNguoiPhuTrachId() == null || !lead.getNguoiPhuTrachId().equals((long) nguoiDung.getId())) {
            throw new LoiPhanQuyenException("Bạn không có quyền từ chối Lead này");
        }

        // Q3: Cấm từ chối sau khi đã nhận lead
        if (lead.getTrangThai() == TrangThaiLeadEnum.DANG_CHAM_SOC) {
            throw new IllegalStateException("Không được từ chối sau khi đã nhận lead");
        }

        if (lead.getTrangThai() != TrangThaiLeadEnum.DA_PHAN_BO && lead.getTrangThai() != TrangThaiLeadEnum.MOI) {
            throw new IllegalStateException("Lead đã thay đổi trạng thái, không thể từ chối");
        }

        String lyDoTongHop = request.layLyDoTongHop();
        LocalDateTime now = LocalDateTime.now();

        // Thực hiện cập nhật lead và ghi nhận lịch sử trong cùng Transaction
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            try {
                boolean capNhat = leadDAO.tuChoiLead(conn, request.getLeadId(), (long) nguoiDung.getId(), lyDoTongHop);
                if (!capNhat) {
                    conn.rollback();
                    throw new IllegalStateException("Lead đã thay đổi trạng thái trước khi gửi yêu cầu");
                }

                LichSuPhanBoLead ls = new LichSuPhanBoLead(
                        request.getLeadId(),
                        (long) nguoiDung.getId(),
                        null,
                        lead.getNhomPhanBoId(),
                        "TU_CHOI",
                        lyDoTongHop,
                        (long) nguoiDung.getId()
                );
                ls.setCreatedAt(now);
                lichSuPhanBoLeadDAO.them(conn, ls);

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Lỗi rollback transaction từ chối lead: " + e.getMessage(), e);
                throw new RuntimeException("Lỗi hệ thống khi từ chối lead: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Tác vụ kiểm tra SLA phản hồi (AC3 của S4-07):
     * Quét các Lead đã phân bổ mà chưa liên hệ trước hạn SLA -> Gắn cờ (qua_sla = 1) và gửi thông báo chuông cho Trưởng nhóm.
     *
     * @param mocKiemTra Mốc thời gian kiểm tra (truyền vào để test clock giả lập, nếu null lấy giờ hiện tại)
     * @return Số lượng lead bị phát hiện quá hạn SLA và đã gửi cảnh báo
     */
    public int kiemTraVaCapNhatQuaSla(LocalDateTime mocKiemTra) {
        LocalDateTime now = (mocKiemTra != null) ? mocKiemTra : LocalDateTime.now();
        int soLeadQuaSla = 0;

        // Đọc cấu hình thời lượng SLA từ DB (mặc định 1440 phút = 24h nếu chưa có)
        int slaPhut = cauHinhHeThongDAO.layGiaTriInt(CAU_HINH_SLA_LEAD_PHUT, SLA_LEAD_PHUT_MAC_DINH);

        List<Lead> dsLead = leadDAO.layDanhSachLeadCanKiemTraSla();
        for (Lead lead : dsLead) {
            LocalDateTime duocPhanBoLuc = lead.getDuocPhanBoLuc();
            LocalDateTime hanSla = lead.getHanPhanHoiSla();
            if (hanSla == null) {
                if (duocPhanBoLuc == null) {
                    continue;
                }
                hanSla = duocPhanBoLuc.plusMinutes(slaPhut);
            }

            // Biên SLA: chỉ khi now > hanSla mới coi là quá hạn
            if (now.isAfter(hanSla)) {
                // AC3 + Q2: Kiểm tra xem đã có hoạt động liên hệ hợp lệ (CUOC_GOI, EMAIL, CUOC_HOP) trước hạn chưa
                LocalDateTime mocBatDau = (duocPhanBoLuc != null) ? duocPhanBoLuc : (lead.getCreatedAt() != null ? lead.getCreatedAt() : now.minusDays(1));
                boolean daLienHeDungHan = hoatDongDAO.coHoatDongLienHeHopLe(lead.getId(), mocBatDau, hanSla);

                if (!daLienHeDungHan) {
                    // Gắn cờ quá SLA trên Lead trong DB
                    boolean ganCo = leadDAO.capNhatQuaSla(lead.getId(), true);
                    if (ganCo) {
                        lead.setQuaSla(true);
                        soLeadQuaSla++;

                        // Gửi thông báo cho Trưởng nhóm trực tiếp (đã có cơ chế chống gửi trùng)
                        guiThongBaoQuaSlaChoTruongNhom(lead, now);
                    }
                } else {
                    // Đã liên hệ đúng hạn -> cập nhật mốc liên hệ nếu chưa có
                    LocalDateTime mocLienHe = hoatDongDAO.layMocLienHeDauTien(lead.getId(), mocBatDau);
                    if (mocLienHe != null && lead.getLienHeDauLuc() == null) {
                        leadDAO.capNhatLienHeDau(lead.getId(), mocLienHe);
                    }
                }
            }
        }

        return soLeadQuaSla;
    }

    /**
     * Tìm trưởng nhóm trực tiếp và gửi thông báo chuông (AC3, Q5).
     */
    private void guiThongBaoQuaSlaChoTruongNhom(Lead lead, LocalDateTime now) {
        if (lead.getNguoiPhuTrachId() == null) {
            return;
        }

        Long truongNhomId = timTruongNhomCuaNhanVien(lead.getNguoiPhuTrachId(), lead.getNhomPhanBoId());
        if (truongNhomId == null) {
            LOGGER.log(Level.WARNING, "Không tìm thấy trưởng nhóm quản lý nhân viên id=" + lead.getNguoiPhuTrachId() + " để gửi cảnh báo SLA");
            return;
        }

        // Chống gửi thông báo trùng lặp cho cùng một lead
        if (thongBaoDAO.kiemTraDaCoThongBaoSla(truongNhomId, lead.getId())) {
            return;
        }

        String tenLeadHienThi = (lead.getTenCongTy() != null && !lead.getTenCongTy().isBlank())
                ? lead.getTenCongTy() : lead.getHoTen();

        ThongBao tb = new ThongBao();
        tb.setNguoiDungId(truongNhomId);
        tb.setLoaiThongBaoId(LOAI_THONG_BAO_LEAD_QUA_SLA);
        tb.setTieuDe("Cảnh báo quá SLA phản hồi: " + tenLeadHienThi);
        tb.setNoiDung("Lead " + lead.getMaLead() + " (" + tenLeadHienThi + ") được phân công cho nhân viên nhưng đã quá hạn SLA mà chưa ghi nhận hoạt động liên hệ.");
        tb.setLoaiDoiTuong("LEAD");
        tb.setDoiTuongId(lead.getId());
        tb.setDuongDanMo("/lead/chi-tiet?leadId=" + lead.getId());
        tb.setDaDoc(false);
        tb.setCreatedAt(now);

        thongBaoDAO.taoThongBao(tb);
    }

    /**
     * Tìm ID Trưởng nhóm của nhân viên phụ trách Lead.
     */
    public Long timTruongNhomCuaNhanVien(Long nhanVienId, Long nhomGoiYId) {
        if (nhanVienId == null) {
            return null;
        }

        NguoiDung sales = nguoiDungDAO.timTheoId(nhanVienId);
        Long nhomId = (sales != null && sales.getNhomKinhDoanhId() != null)
                ? (long) sales.getNhomKinhDoanhId() : nhomGoiYId;

        if (nhomId == null || nhomId <= 0) {
            return null;
        }

        NhomKinhDoanh nhom = nhomKinhDoanhDAO.timTheoId(nhomId);
        if (nhom != null && nhom.getTruongNhomId() != null && nhom.getTruongNhomId() > 0) {
            // Tránh gửi lại cho chính người phụ trách nếu người đó kiêm trưởng nhóm
            if (!nhom.getTruongNhomId().equals(nhanVienId)) {
                return nhom.getTruongNhomId();
            }
        }

        // Tìm lên nhóm cha nếu nhóm hiện tại chưa có trưởng nhóm
        if (nhom != null && nhom.getNhomChaId() != null && nhom.getNhomChaId() > 0) {
            NhomKinhDoanh nhomCha = nhomKinhDoanhDAO.timTheoId(nhom.getNhomChaId());
            if (nhomCha != null && nhomCha.getTruongNhomId() != null && nhomCha.getTruongNhomId() > 0) {
                return nhomCha.getTruongNhomId();
            }
        }

        return null;
    }

    private TrangThaiSlaEnum xacDinhTrangThaiSla(Lead lead, LocalDateTime now) {
        if (lead.getTrangThai() == TrangThaiLeadEnum.DA_GOP
                || lead.getTrangThai() == TrangThaiLeadEnum.TU_CHOI
                || lead.getTrangThai() == TrangThaiLeadEnum.DA_CHUYEN_DOI) {
            return TrangThaiSlaEnum.KHONG_XAC_DINH;
        }
        if (lead.isQuaSla()) {
            return TrangThaiSlaEnum.QUA_HAN;
        }
        if (lead.getHanPhanHoiSla() == null) {
            return TrangThaiSlaEnum.KHONG_XAC_DINH;
        }
        if (now.isAfter(lead.getHanPhanHoiSla())) {
            // Kiểm tra xem trước hạn đã liên hệ chưa
            LocalDateTime mocBatDau = (lead.getDuocPhanBoLuc() != null) ? lead.getDuocPhanBoLuc() : lead.getCreatedAt();
            boolean daLienHe = hoatDongDAO.coHoatDongLienHeHopLe(lead.getId(), mocBatDau, lead.getHanPhanHoiSla());
            return daLienHe ? TrangThaiSlaEnum.DUNG_HAN : TrangThaiSlaEnum.QUA_HAN;
        }
        return TrangThaiSlaEnum.DUNG_HAN;
    }

    private void kiemTraQuyenXemLead(Lead lead, NguoiDung user) {
        // ADMIN / DIRECTOR / MARKETING có toàn quyền xem
        if (user.coVaiTro("ADMIN") || user.coVaiTro("DIRECTOR") || user.coVaiTro("MARKETING")) {
            return;
        }

        // SALES_REP: Chỉ được xem Lead do mình phụ trách
        if (user.coVaiTro("SALES_REP")) {
            if (lead.getNguoiPhuTrachId() != null && lead.getNguoiPhuTrachId().equals((long) user.getId())) {
                return;
            }
        }

        // TEAM_LEAD: Được xem Lead của các thành viên trong nhóm mình quản lý
        if (user.coVaiTro("TEAM_LEAD")) {
            if (user.getNhomKinhDoanhId() != null) {
                Long userNhomId = (long) user.getNhomKinhDoanhId();
                if (lead.getNhomPhanBoId() != null && lead.getNhomPhanBoId().equals(userNhomId)) {
                    return;
                }
                if (lead.getNguoiPhuTrachId() != null) {
                    NguoiDung sales = nguoiDungDAO.timTheoId(lead.getNguoiPhuTrachId());
                    if (sales != null && sales.getNhomKinhDoanhId() != null && sales.getNhomKinhDoanhId().equals(user.getNhomKinhDoanhId())) {
                        return;
                    }
                }
            }
        }

        throw new LoiPhanQuyenException("Bạn không có quyền xem thông tin Lead này");
    }
}
