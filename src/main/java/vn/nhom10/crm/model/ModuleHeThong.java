package vn.nhom10.crm.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Định nghĩa các module nghiệp vụ và ma trận phân quyền truy cập menu
 * theo tài liệu phân tích nghiệp vụ (Sheet 2. User Roles).
 */
public enum ModuleHeThong {
    TONG_QUAN(
            "TONG_QUAN",
            "Tổng quan",
            "/tong-quan",
            "overview",
            1,
            // Cho phép tất cả 7 vai trò
            VaiTroEnum.values()
    ),
    KHACH_HANG(
            "KHACH_HANG",
            "Khách hàng & Liên hệ",
            "/khach-hang",
            "users",
            2,
            // Cho phép tất cả 7 vai trò
            VaiTroEnum.values()
    ),
    LEAD(
            "LEAD",
            "Lead & Phân bổ",
            "/lead",
            "target",
            3,
            // Cust. Success và Accountant KHÔNG được truy cập
            VaiTroEnum.SALES_REP,
            VaiTroEnum.MARKETING,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    CO_HOI(
            "CO_HOI",
            "Cơ hội & Pipeline",
            "/co-hoi",
            "briefcase",
            4,
            // Cho phép tất cả 7 vai trò
            VaiTroEnum.values()
    ),
    HOAT_DONG(
            "HOAT_DONG",
            "Hoạt động & Lịch",
            "/hoat-dong",
            "calendar",
            5,
            // Accountant KHÔNG được truy cập
            VaiTroEnum.SALES_REP,
            VaiTroEnum.MARKETING,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    BAO_GIA(
            "BAO_GIA",
            "Báo giá",
            "/bao-gia",
            "file-text",
            6,
            // Marketing KHÔNG được truy cập
            VaiTroEnum.SALES_REP,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.ACCOUNTANT,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    HOP_DONG(
            "HOP_DONG",
            "Hợp đồng",
            "/hop-dong",
            "check-circle",
            7,
            // Marketing KHÔNG được truy cập
            VaiTroEnum.SALES_REP,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.ACCOUNTANT,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    CHI_TIEU(
            "CHI_TIEU",
            "Chỉ tiêu & KPI",
            "/chi-tieu",
            "trending-up",
            8,
            // Marketing và Cust. Success KHÔNG được truy cập
            VaiTroEnum.SALES_REP,
            VaiTroEnum.ACCOUNTANT,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    BAO_CAO(
            "BAO_CAO",
            "Báo cáo & Phân tích",
            "/bao-cao",
            "bar-chart",
            9,
            // Cho phép tất cả 7 vai trò
            VaiTroEnum.values()
    ),
    TU_DONG_HOA(
            "TU_DONG_HOA",
            "Tự động hoá & Thông báo",
            "/tu-dong-hoa",
            "bell",
            10,
            // Accountant KHÔNG được truy cập
            VaiTroEnum.SALES_REP,
            VaiTroEnum.MARKETING,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    DANH_MUC(
            "DANH_MUC",
            "Danh mục & Cấu hình",
            "/danh-muc",
            "settings",
            11,
            // Chỉ Director và Admin được xem/quản lý (Story S2-07)
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    NGUOI_DUNG(
            "NGUOI_DUNG",
            "Người dùng & Nhật ký",
            "/nguoi-dung",
            "shield",
            12,
            // Chỉ Director và Admin được xem/quản lý
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    );

    private final String maModule;
    private final String tenHienThi;
    private final String duongDanUrl;
    private final String bieuTuong;
    private final int thuTu;
    private final Set<VaiTroEnum> vaiTroDuocPhep;

    ModuleHeThong(String maModule, String tenHienThi, String duongDanUrl, String bieuTuong, int thuTu, VaiTroEnum... vaiTros) {
        this.maModule = maModule;
        this.tenHienThi = tenHienThi;
        this.duongDanUrl = duongDanUrl;
        this.bieuTuong = bieuTuong;
        this.thuTu = thuTu;
        Set<VaiTroEnum> set = new HashSet<>(Arrays.asList(vaiTros));
        this.vaiTroDuocPhep = Collections.unmodifiableSet(set);
    }

    public String getMaModule() {
        return maModule;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getDuongDanUrl() {
        return duongDanUrl;
    }

    public String getBieuTuong() {
        return bieuTuong;
    }

    public int getThuTu() {
        return thuTu;
    }

    public Set<VaiTroEnum> getVaiTroDuocPhep() {
        return vaiTroDuocPhep;
    }

    /**
     * Kiểm tra xem một vai trò cụ thể có được phép xem module này không.
     */
    public boolean choPhepVaiTro(VaiTroEnum vaiTro) {
        if (vaiTro == null) {
            return false;
        }
        return vaiTroDuocPhep.contains(vaiTro);
    }

    /**
     * Kiểm tra xem trong tập hợp các vai trò, có ít nhất 1 vai trò được phép hay không.
     */
    public boolean choPhepBatKyVaiTro(Set<VaiTroEnum> danhSachVaiTro) {
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return false;
        }
        for (VaiTroEnum vt : danhSachVaiTro) {
            if (choPhepVaiTro(vt)) {
                return true;
            }
        }
        return false;
    }

    public static ModuleHeThong tuDuongDan(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        for (ModuleHeThong mod : values()) {
            if (path.startsWith(mod.duongDanUrl)) {
                return mod;
            }
        }
        return null;
    }
}
