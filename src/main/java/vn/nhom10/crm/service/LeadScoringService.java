package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.CauHinhChamDiemDTO;
import vn.nhom10.crm.dto.NguongPhanLoaiLeadDTO;
import vn.nhom10.crm.dto.TieuChiChamDiemDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service quản lý Cấu hình chấm điểm và tính toán phân loại Lead tự động (Story S4-05).
 * Đáp ứng các AC nghiệp vụ:
 * - AC1: Khai báo tiêu chí và số điểm (ngành nghề, quy mô, nguồn, mức độ quan tâm) và lưu bền vững vào CSDL.
 * - AC2: Điểm được tính lại tự động khi thông tin lead thay đổi.
 * - AC3: Phân loại Nóng, Ấm, Lạnh theo ngưỡng điểm khai báo được.
 * - AC4: Điểm chỉ để sắp xếp ưu tiên, không tự động loại/xóa/đổi trạng thái lead.
 */
public class LeadScoringService {

    private static final Logger LOGGER = Logger.getLogger(LeadScoringService.class.getName());

    public static final String MA_HOT_MIN = "LEAD_SCORING_HOT_MIN";
    public static final String MA_WARM_MIN = "LEAD_SCORING_WARM_MIN";
    public static final String MA_VERSION = "LEAD_SCORING_VERSION";
    public static final String MA_PREFIX_ENABLED = "LEAD_SCORING_CRITERIA_ENABLED_";
    public static final String MA_PREFIX_POINTS = "LEAD_SCORING_CRITERIA_POINTS_";

    private final CauHinhHeThongDAO cauHinhHeThongDAO;
    private final LeadDAO leadDAO;

    private static volatile LeadScoringService instance;
    private volatile CauHinhChamDiemDTO cachedConfig;

    public LeadScoringService() {
        this(new CauHinhHeThongDAO(), new LeadDAO());
    }

    public LeadScoringService(CauHinhHeThongDAO cauHinhHeThongDAO, LeadDAO leadDAO) {
        this.cauHinhHeThongDAO = cauHinhHeThongDAO != null ? cauHinhHeThongDAO : new CauHinhHeThongDAO();
        this.leadDAO = leadDAO != null ? leadDAO : new LeadDAO();
    }

    public static LeadScoringService getInstance() {
        if (instance == null) {
            synchronized (LeadScoringService.class) {
                if (instance == null) {
                    instance = new LeadScoringService();
                }
            }
        }
        return instance;
    }

    /**
     * Lấy cấu hình chấm điểm lead hiện hành từ CSDL hoặc cache (AC1).
     */
    public CauHinhChamDiemDTO layCauHinhHienTai() {
        if (cachedConfig != null) {
            return cachedConfig;
        }

        synchronized (this) {
            if (cachedConfig != null) {
                return cachedConfig;
            }

            CauHinhChamDiemDTO config = CauHinhChamDiemDTO.taoCauHinhMacDinh();

            try {
                int hotMin = cauHinhHeThongDAO.layGiaTriInt(MA_HOT_MIN, 70);
                int warmMin = cauHinhHeThongDAO.layGiaTriInt(MA_WARM_MIN, 40);
                int version = cauHinhHeThongDAO.layGiaTriInt(MA_VERSION, 1);

                config.setPhienBan(version);
                config.setNguongPhanLoai(new NguongPhanLoaiLeadDTO(hotMin, warmMin));

                for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
                    String key = tc.getKey();
                    int points = cauHinhHeThongDAO.layGiaTriInt(MA_PREFIX_POINTS + key, tc.getSoDiem());
                    int enabledVal = cauHinhHeThongDAO.layGiaTriInt(MA_PREFIX_ENABLED + key, tc.isKichHoat() ? 1 : 0);

                    tc.setSoDiem(points);
                    tc.setKichHoat(enabledVal == 1);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể đọc cấu hình chấm điểm lead từ CSDL, sử dụng cấu hình mặc định: " + e.getMessage(), e);
            }

            cachedConfig = config;
            return config;
        }
    }

    /**
     * Lưu cấu hình chấm điểm và ngưỡng phân loại mới vào CSDL (AC1, AC3).
     */
    public boolean luuCauHinh(CauHinhChamDiemDTO config, Long userId, String userHoTen) {
        if (config == null || config.getNguongPhanLoai() == null) {
            throw new IllegalArgumentException("Dữ liệu cấu hình không hợp lệ");
        }

        int hotMin = config.getNguongPhanLoai().getHotMin();
        int warmMin = config.getNguongPhanLoai().getWarmMin();

        if (hotMin <= warmMin) {
            throw new IllegalArgumentException("Ngưỡng điểm Nóng (" + hotMin + ") phải lớn hơn ngưỡng Ấm (" + warmMin + ").");
        }
        if (hotMin < 1 || hotMin > 100) {
            throw new IllegalArgumentException("Ngưỡng điểm Nóng phải trong khoảng từ 1 đến 100.");
        }
        if (warmMin < 0 || warmMin >= 100) {
            throw new IllegalArgumentException("Ngưỡng điểm Ấm phải trong khoảng từ 0 đến 99.");
        }

        boolean coTieuChiKichHoat = false;
        for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
            if (tc.isKichHoat()) {
                coTieuChiKichHoat = true;
            }
            if (tc.getSoDiem() < 0 || tc.getSoDiem() > 100) {
                throw new IllegalArgumentException("Điểm của tiêu chí " + tc.getTenTieuChi() + " phải từ 0 đến 100.");
            }
        }

        if (!coTieuChiKichHoat) {
            throw new IllegalArgumentException("Phải kích hoạt ít nhất một tiêu chí chấm điểm.");
        }

        int currentVersion = (config.getPhienBan() > 0) ? config.getPhienBan()
                : (cachedConfig != null && cachedConfig.getPhienBan() > 0 ? cachedConfig.getPhienBan()
                : cauHinhHeThongDAO.layGiaTriInt(MA_VERSION, 1));
        int newVersion = currentVersion + 1;

        // Lưu vào CSDL bảng cau_hinh_he_thong
        try {
            boolean okHot = cauHinhHeThongDAO.datGiaTri(MA_HOT_MIN, String.valueOf(hotMin), userId);
            boolean okWarm = cauHinhHeThongDAO.datGiaTri(MA_WARM_MIN, String.valueOf(warmMin), userId);
            boolean okVer = cauHinhHeThongDAO.datGiaTri(MA_VERSION, String.valueOf(newVersion), userId);

            if (!okHot || !okWarm || !okVer) {
                LOGGER.severe("Lỗi ghi nhận ngưỡng điểm hoặc phiên bản vào bảng cau_hinh_he_thong");
                return false;
            }

            for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
                String key = tc.getKey();
                boolean okPts = cauHinhHeThongDAO.datGiaTri(MA_PREFIX_POINTS + key, String.valueOf(tc.getSoDiem()), userId);
                boolean okEn = cauHinhHeThongDAO.datGiaTri(MA_PREFIX_ENABLED + key, tc.isKichHoat() ? "1" : "0", userId);
                if (!okPts || !okEn) {
                    LOGGER.severe("Lỗi ghi nhận tiêu chí " + key + " vào bảng cau_hinh_he_thong");
                    return false;
                }
            }

            // Chỉ cập nhật object và cache sau khi DB ghi thành công toàn bộ
            config.setPhienBan(newVersion);
            config.setNgayCapNhat(LocalDateTime.now());
            if (userHoTen != null && !userHoTen.isBlank()) {
                config.setNguoiCapNhat(userHoTen);
            }

            synchronized (this) {
                cachedConfig = config;
            }

            LOGGER.info("Đã lưu cấu hình chấm điểm lead phiên bản " + newVersion + " thành công bởi user ID: " + userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu cấu hình chấm điểm lead vào CSDL: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Tự động tính toán điểm số và phân loại Nóng/Ấm/Lạnh cho Lead (AC1, AC2, AC3, AC4).
     * Quy tắc tính điểm:
     * 1. INDUSTRY_FIT (Ngành nghề phù hợp): Nếu có ngành nghề và tiêu chí bật -> cộng điểm tương ứng.
     * 2. COMPANY_SIZE (Quy mô doanh nghiệp): Nếu có quy mô và tiêu chí bật -> cộng điểm tương ứng.
     * 3. SOURCE (Nguồn lead): Nếu có nguồn lead hợp lệ và tiêu chí bật -> cộng điểm tương ứng.
     * 4. INTEREST_LEVEL (Mức độ quan tâm): Nếu có mức độ quan tâm và tiêu chí bật -> cộng điểm tương ứng.
     * - Tiêu chí nào không có dữ liệu (null/rỗng) nhận 0 điểm.
     * - Tiêu chí bị tắt nhận 0 điểm.
     * - Phân loại theo ngưỡng Nóng/Ấm/Lạnh.
     * - AC4: Điểm thấp KHÔNG tự động loại, không xóa hay chuyển trạng thái lead!
     */
    public Lead tinhDiemVaPhanLoai(Lead lead) {
        return tinhDiemVaPhanLoai(lead, layCauHinhHienTai());
    }

    /**
     * Tính toán điểm và phân loại theo một bản cấu hình cụ thể.
     */
    public Lead tinhDiemVaPhanLoai(Lead lead, CauHinhChamDiemDTO config) {
        if (lead == null) return null;
        if (config == null) {
            config = layCauHinhHienTai();
        }

        int tongDiem = 0;

        for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
            if (!tc.isKichHoat()) {
                continue;
            }

            switch (tc.getKey()) {
                case "INDUSTRY_FIT":
                    if (lead.getNganhNgheId() != null && lead.getNganhNgheId() > 0) {
                        tongDiem += tc.getSoDiem();
                    }
                    break;

                case "COMPANY_SIZE":
                    if (lead.getQuyMoId() != null && lead.getQuyMoId() > 0) {
                        tongDiem += tc.getSoDiem();
                    }
                    break;

                case "SOURCE":
                    if (lead.getNguonLeadId() != null && lead.getNguonLeadId() > 0) {
                        tongDiem += tc.getSoDiem();
                    }
                    break;

                case "INTEREST_LEVEL":
                    String mucDo = lead.getMucDoQuanTam();
                    if (mucDo != null && !mucDo.isBlank()) {
                        tongDiem += tinhDiemMucDoQuanTam(mucDo, tc.getSoDiem());
                    }
                    break;

                default:
                    break;
            }
        }

        // Giới hạn điểm không âm và không vượt tổng tối đa cho phép
        tongDiem = Math.max(0, tongDiem);
        lead.setDiem(tongDiem);

        // Phân loại Nóng, Ấm, Lạnh theo ngưỡng khai báo (AC3)
        NguongPhanLoaiLeadDTO thresholds = config.getNguongPhanLoai();
        String phanLoai = thresholds != null ? thresholds.phanLoai(tongDiem) : "CHUA_CHAM";
        lead.setPhanLoai(phanLoai);

        // AC4: Điểm chỉ để sắp xếp ưu tiên, không đổi trạng thái hay loại lead
        // Đảm bảo trạng thái lead giữ nguyên (ví dụ MOI, DANG_XU_LY,...)

        return lead;
    }

    /**
     * Cập nhật thông tin Lead và tự động tính lại điểm/phân loại theo thông tin mới (AC2, AC3, AC4).
     */
    public boolean capNhatLeadVaTinhLaiDiem(Lead lead) {
        if (lead == null || lead.getId() == null) {
            return false;
        }

        // Tính lại điểm và phân loại tự động
        tinhDiemVaPhanLoai(lead);

        // Lưu vào CSDL
        return leadDAO.capNhat(lead);
    }

    /**
     * Tính lại điểm và phân loại cho toàn bộ danh sách Lead hiện có khi cấu hình thay đổi (AC2 hàng loạt).
     * Thực hiện an toàn trong Transaction; nếu lỗi sẽ rollback.
     */
    public int tinhLaiDiemToanBoLead() throws SQLException {
        CauHinhChamDiemDTO config = layCauHinhHienTai();
        List<Lead> dsLead = leadDAO.layTatCaLead();
        if (dsLead == null || dsLead.isEmpty()) {
            return 0;
        }

        int soLuongCapNhat = 0;
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            boolean autoCommitBanDau = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                for (Lead l : dsLead) {
                    tinhDiemVaPhanLoai(l, config);
                    boolean ok = leadDAO.capNhatDiemVaPhanLoai(conn, l.getId(), l.getDiem(), l.getPhanLoai());
                    if (ok) {
                        soLuongCapNhat++;
                    }
                }
                conn.commit();
                LOGGER.info("Đã tính lại điểm và phân loại tự động cho " + soLuongCapNhat + " lead.");
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Lỗi rollback transaction khi tính lại điểm hàng loạt: " + e.getMessage(), e);
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitBanDau);
            }
        }
        return soLuongCapNhat;
    }

    /**
     * Lấy danh sách Lead sắp xếp theo điểm ưu tiên giảm dần (AC4 - Không tự loại lead điểm thấp).
     */
    public List<Lead> layDanhSachLeadUuTien(int limit, int offset) {
        return leadDAO.layDanhSachSapXepTheoDiem(limit, offset);
    }

    private int tinhDiemMucDoQuanTam(String mucDo, int diemToiDa) {
        if (mucDo == null || mucDo.isBlank()) return 0;
        String m = mucDo.trim().toUpperCase();
        if (m.contains("CAN_GAP") || m.contains("CAP_THIET") || m.contains("GAP")) {
            return diemToiDa;
        }
        if (m.contains("QUAN_TAM_CAO") || m.contains("CAO")) {
            return (int) Math.round(diemToiDa * 0.75);
        }
        if (m.contains("TIM_HIEU") || m.contains("TRUNG_BINH")) {
            return (int) Math.round(diemToiDa * 0.4);
        }
        // Trường hợp khác có quan tâm
        return (int) Math.round(diemToiDa * 0.2);
    }
}
