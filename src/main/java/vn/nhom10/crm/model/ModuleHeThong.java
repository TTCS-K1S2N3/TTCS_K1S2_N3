package vn.nhom10.crm.model;

import vn.nhom10.crm.service.PermissionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Định nghĩa 12 module hiển thị trên thanh điều hướng và giao diện hệ thống.
 * Đóng vai trò metadata hiển thị (icon, url, thứ tự, trạng thái triển khai, mapping canonical module).
 * Nguồn quyền runtime chính thức là Database (vai_tro, module_he_thong, vai_tro_module).
 */
public enum ModuleHeThong {
    TONG_QUAN(
            "TONG_QUAN",
            "TONG_QUAN", // Ngoại lệ: CSDL chưa có canonical riêng, xử lý an toàn tại PermissionService
            "Tổng quan",
            "/tong-quan",
            "overview",
            1,
            VaiTroEnum.values()
    ),
    KHACH_HANG(
            "KHACH_HANG",
            "KHACH_HANG",
            "Khách hàng & Liên hệ",
            "/khach-hang",
            "users",
            2,
            VaiTroEnum.values()
    ),
    LEAD(
            "LEAD",
            "LEAD",
            "Lead & Phân bổ",
            "/lead",
            "target",
            3,
            VaiTroEnum.SALES_REP,
            VaiTroEnum.MARKETING,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    CO_HOI(
            "CO_HOI",
            "CO_HOI",
            "Cơ hội & Pipeline",
            "/co-hoi",
            "briefcase",
            4,
            VaiTroEnum.values()
    ),
    HOAT_DONG(
            "HOAT_DONG",
            "HOAT_DONG",
            "Hoạt động & Lịch",
            "/hoat-dong",
            "calendar",
            5,
            VaiTroEnum.SALES_REP,
            VaiTroEnum.MARKETING,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    BAO_GIA(
            "BAO_GIA",
            "BAO_GIA_HOP_DONG",
            "Báo giá",
            "/bao-gia",
            "file-text",
            6,
            VaiTroEnum.SALES_REP,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.ACCOUNTANT,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    HOP_DONG(
            "HOP_DONG",
            "BAO_GIA_HOP_DONG",
            "Hợp đồng",
            "/hop-dong",
            "check-circle",
            7,
            VaiTroEnum.SALES_REP,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.ACCOUNTANT,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    CHI_TIEU(
            "CHI_TIEU",
            "KPI",
            "Chỉ tiêu & KPI",
            "/chi-tieu",
            "trending-up",
            8,
            VaiTroEnum.SALES_REP,
            VaiTroEnum.ACCOUNTANT,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    BAO_CAO(
            "BAO_CAO",
            "BAO_CAO",
            "Báo cáo & Phân tích",
            "/bao-cao",
            "bar-chart",
            9,
            VaiTroEnum.values()
    ),
    TU_DONG_HOA(
            "TU_DONG_HOA",
            "TU_DONG_HOA",
            "Tự động hoá & Thông báo",
            "/tu-dong-hoa",
            "bell",
            10,
            VaiTroEnum.SALES_REP,
            VaiTroEnum.MARKETING,
            VaiTroEnum.CUST_SUCCESS,
            VaiTroEnum.TEAM_LEAD,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    ),
    DANH_MUC(
            "DANH_MUC",
            "DANH_MUC",
            "Danh mục & Cấu hình",
            "/danh-muc",
            "settings",
            11,
            VaiTroEnum.values() // DB matrix cho phép nhiều vai trò có quyền READ
    ),
    NGUOI_DUNG(
            "NGUOI_DUNG",
            "NGUOI_DUNG_NHAT_KY",
            "Người dùng & Nhật ký",
            "/nguoi-dung",
            "shield",
            12,
            VaiTroEnum.DIRECTOR,
            VaiTroEnum.ADMIN
    );

    private final String maModule;
    private final String maCanonical;
    private final String tenHienThi;
    private final String duongDanUrl;
    private final String bieuTuong;
    private final int thuTu;
    private final boolean daTrienKhai;
    private final Set<VaiTroEnum> vaiTroMacDinhFallback;

    ModuleHeThong(String maModule, String maCanonical, String tenHienThi, String duongDanUrl, String bieuTuong, int thuTu, boolean daTrienKhai, VaiTroEnum... vaiTros) {
        this.maModule = maModule;
        this.maCanonical = maCanonical;
        this.tenHienThi = tenHienThi;
        this.duongDanUrl = duongDanUrl;
        this.bieuTuong = bieuTuong;
        this.thuTu = thuTu;
        this.daTrienKhai = daTrienKhai;
        Set<VaiTroEnum> set = new HashSet<>(Arrays.asList(vaiTros));
        this.vaiTroMacDinhFallback = Collections.unmodifiableSet(set);
    }

    ModuleHeThong(String maModule, String maCanonical, String tenHienThi, String duongDanUrl, String bieuTuong, int thuTu, VaiTroEnum... vaiTros) {
        this(maModule, maCanonical, tenHienThi, duongDanUrl, bieuTuong, thuTu,
                ("KHACH_HANG".equals(maModule) || "DANH_MUC".equals(maModule) || "NGUOI_DUNG".equals(maModule)),
                vaiTros);
    }

    public boolean isDaTrienKhai() {
        return daTrienKhai;
    }

    public String getMaModule() {
        return maModule;
    }

    public String getMaCanonical() {
        return maCanonical;
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
        return vaiTroMacDinhFallback;
    }

    /**
     * Kiểm tra xem một vai trò cụ thể có được phép xem module này không.
     * Nguồn quyền chính thức là Database thông qua PermissionService.
     */
    public boolean choPhepVaiTro(VaiTroEnum vaiTro) {
        if (vaiTro == null) {
            return false;
        }
        try {
            return PermissionService.getInstance().coQuyen(vaiTro, maCanonical, MucQuyen.READ);
        } catch (Exception e) {
            return vaiTroMacDinhFallback.contains(vaiTro);
        }
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
