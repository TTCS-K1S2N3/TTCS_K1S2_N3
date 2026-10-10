package vn.nhom10.crm.service;

import vn.nhom10.crm.dto.MucMenuDTO;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Service xử lý logic điều hướng menu, kiểm tra quyền truy cập module
 * và chuẩn bị dữ liệu hiển thị cho thanh điều hướng hệ thống (bao gồm avatar).
 * Quyền truy cập được xác định từ cơ sở dữ liệu qua PermissionService.
 */
public class MenuService {

    private static final MenuService INSTANCE = new MenuService();

    private final PermissionService permissionService;

    public static MenuService getInstance() {
        return INSTANCE;
    }

    public MenuService() {
        this(PermissionService.getInstance());
    }

    public MenuService(PermissionService permissionService) {
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
    }

    /**
     * Kiểm tra người dùng có quyền truy cập vào module hay không.
     * Áp dụng quy tắc phân quyền:
     * - Admin: Toàn quyền truy cập mọi module.
     * - User không đăng nhập hoặc bị khoá: Không có quyền.
     * - Tổng quan: Mọi tài khoản hợp lệ đều được xem.
     * - Các module khác: Kiểm tra DB permission >= READ thông qua PermissionService.
     */
    public boolean kiemTraQuyenTruyCap(NguoiDung nguoiDung, ModuleHeThong module) {
        if (nguoiDung == null || !nguoiDung.dangHoatDong() || module == null) {
            return false;
        }

        Set<VaiTroEnum> danhSachVaiTro = nguoiDung.getDanhSachVaiTroEnum();
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return false;
        }

        // Admin có toàn quyền truy cập tất cả module
        if (danhSachVaiTro.contains(VaiTroEnum.ADMIN) || nguoiDung.coVaiTro("ADMIN")) {
            return true;
        }

        if (module == ModuleHeThong.TONG_QUAN) {
            return true;
        }

        return permissionService.coQuyen(nguoiDung, module.getMaCanonical(), MucQuyen.READ);
    }

    /**
     * Kiểm tra quyền truy cập dựa trên đường dẫn URL.
     */
    public boolean kiemTraQuyenTruyCapUrl(NguoiDung nguoiDung, String urlPath) {
        if (urlPath == null || urlPath.isBlank()) {
            return true;
        }

        // Các route xác thực và cá nhân: Luôn cho phép mọi tài khoản hợp lệ
        if (urlPath.startsWith("/ho-so")
                || urlPath.startsWith("/doi-mat-khau")
                || urlPath.startsWith("/avatar")
                || urlPath.startsWith("/dang-xuat")
                || urlPath.startsWith("/logout")
                || urlPath.startsWith("/trang-chu")
                || urlPath.startsWith("/api/phien")
                || urlPath.startsWith("/phien/")) {
            return true;
        }

        // Module NGUOI_DUNG_NHAT_KY
        if (urlPath.startsWith("/nhat-ky-thay-doi") || urlPath.startsWith("/nguoi-dung/nhat-ky-thay-doi")) {
            return nguoiDung != null && (nguoiDung.coVaiTro(VaiTroEnum.ADMIN)
                    || permissionService.coQuyen(nguoiDung, "NGUOI_DUNG_NHAT_KY", MucQuyen.READ));
        }
        if (urlPath.startsWith("/nguoi-dung/phan-quyen-vai-tro")) {
            return nguoiDung != null && (nguoiDung.coVaiTro(VaiTroEnum.ADMIN) || nguoiDung.coVaiTro(VaiTroEnum.DIRECTOR));
        }
        if (urlPath.startsWith("/nguoi-dung/phan-quyen")
                || urlPath.startsWith("/nguoi-dung/tao")
                || urlPath.startsWith("/nguoi-dung/sua")
                || urlPath.startsWith("/nguoi-dung/khoa-tai-khoan")
                || urlPath.startsWith("/nguoi-dung/import")
                || urlPath.startsWith("/nguoi-dung/tai-tep-mau")) {
            return nguoiDung != null && nguoiDung.coVaiTro(VaiTroEnum.ADMIN);
        }
        if (urlPath.startsWith("/phan-quyen-du-lieu")) {
            return nguoiDung != null && (nguoiDung.coVaiTro(VaiTroEnum.ADMIN)
                    || permissionService.coQuyen(nguoiDung, "NGUOI_DUNG_NHAT_KY", MucQuyen.READ));
        }
        if (urlPath.startsWith("/nguoi-dung")) {
            return nguoiDung != null && (nguoiDung.coVaiTro(VaiTroEnum.ADMIN)
                    || permissionService.coQuyen(nguoiDung, "NGUOI_DUNG_NHAT_KY", MucQuyen.READ));
        }

        // Module KHACH_HANG
        if (urlPath.startsWith("/khach-hang")
                || urlPath.startsWith("/nguoi-lien-he")
                || urlPath.startsWith("/yeu-cau-ho-tro")
                || urlPath.startsWith("/chi-tiet-ban-ghi")) {
            return kiemTraQuyenTruyCap(nguoiDung, ModuleHeThong.KHACH_HANG);
        }

        // Module DANH_MUC
        if (urlPath.startsWith("/danh-muc")
                || urlPath.startsWith("/danh-muc-ban-hang")
                || urlPath.startsWith("/san-pham")
                || urlPath.startsWith("/truong-tuy-chinh")
                || urlPath.startsWith("/pipeline")
                || urlPath.startsWith("/giai-doan-pipeline")
                || urlPath.startsWith("/co-hoi/pipeline")
                || urlPath.startsWith("/ly-do-thang-thua")
                || urlPath.startsWith("/co-cau-to-chuc")) {
            return kiemTraQuyenTruyCap(nguoiDung, ModuleHeThong.DANH_MUC);
        }

        ModuleHeThong module = ModuleHeThong.tuDuongDan(urlPath);
        if (module == null) {
            // URL không thuộc phạm vi các module nghiệp vụ cần phân quyền
            return true;
        }
        return kiemTraQuyenTruyCap(nguoiDung, module);
    }

    /**
     * Lấy danh sách các mục menu mà người dùng ĐƯỢC PHÉP truy cập.
     * Các mục menu không thuộc quyền sẽ bị LOẠI BỎ hoàn toàn khỏi danh sách.
     */
    public List<MucMenuDTO> layDanhSachMenuChoNguoiDung(NguoiDung nguoiDung, String currentUri) {
        List<MucMenuDTO> ketQua = new ArrayList<>();
        if (nguoiDung == null || !nguoiDung.dangHoatDong()) {
            return ketQua;
        }

        List<ModuleHeThong> danhSachModule = Arrays.asList(ModuleHeThong.values());
        danhSachModule.sort(Comparator.comparingInt(ModuleHeThong::getThuTu));

        for (ModuleHeThong mod : danhSachModule) {
            if (kiemTraQuyenTruyCap(nguoiDung, mod)) {
                boolean active = false;
                if (currentUri != null && !currentUri.isBlank()) {
                    active = currentUri.equals(mod.getDuongDanUrl())
                            || currentUri.startsWith(mod.getDuongDanUrl() + "/")
                            || (mod == ModuleHeThong.KHACH_HANG && (
                                    currentUri.startsWith("/yeu-cau-ho-tro")
                                    || currentUri.startsWith("/chi-tiet-ban-ghi")
                               ))
                            || (mod == ModuleHeThong.NGUOI_DUNG && (
                                    currentUri.startsWith("/nhat-ky-thay-doi")
                                    || currentUri.startsWith("/nguoi-dung/nhat-ky-thay-doi")
                                    || currentUri.startsWith("/co-cau-to-chuc")
                                    || currentUri.startsWith("/phan-quyen-du-lieu")
                                    || currentUri.startsWith("/nguoi-dung/phan-quyen-vai-tro")
                                    || currentUri.startsWith("/nguoi-dung/phan-quyen")
                               ))
                            || (mod == ModuleHeThong.DANH_MUC && (
                                    currentUri.startsWith("/danh-muc-ban-hang")
                                    || currentUri.startsWith("/san-pham")
                                    || currentUri.startsWith("/truong-tuy-chinh")
                                    || currentUri.startsWith("/pipeline")
                                    || currentUri.startsWith("/co-hoi/pipeline")
                                    || currentUri.startsWith("/danh-muc/ly-do-thang-thua")
                                    || currentUri.startsWith("/co-hoi/ly-do-thang-thua")
                                    || currentUri.startsWith("/danh-muc/doi-thu")
                               ));
                }
                MucMenuDTO dto = new MucMenuDTO(
                        mod.getMaModule(),
                        mod.getTenHienThi(),
                        mod.getDuongDanUrl(),
                        mod.getBieuTuong(),
                        mod.getThuTu(),
                        active,
                        mod.isDaTrienKhai()
                );
                ketQua.add(dto);
            }
        }
        return ketQua;
    }

    /**
     * Tạo thông tin điều hướng hoàn chỉnh cho thanh header/sidebar.
     */
    public ThongTinDieuHuongDTO layThongTinDieuHuong(NguoiDung nguoiDung, String currentUri) {
        return layThongTinDieuHuong(nguoiDung, currentUri, "");
    }

    /**
     * Tạo đối tượng ThongTinDieuHuongDTO chứa toàn bộ thông tin người dùng:
     * - Tên
     * - Vai trò
     * - Nhóm kinh doanh
     * - Đường dẫn ảnh đại diện và thumbnail
     * - Danh sách menu đúng quyền
     */
    public ThongTinDieuHuongDTO layThongTinDieuHuong(NguoiDung nguoiDung, String currentUri, String contextPath) {
        String prefix = (contextPath != null && !contextPath.isBlank() && !"/".equals(contextPath)) ? contextPath : "";

        if (nguoiDung == null) {
            return new ThongTinDieuHuongDTO(
                    "Khách",
                    "",
                    "Chưa đăng nhập",
                    "Chưa phân nhóm",
                    "CRM",
                    false,
                    new ArrayList<>()
            );
        }

        List<MucMenuDTO> dsMenu = layDanhSachMenuChoNguoiDung(nguoiDung, currentUri);

        String avatarUrl = null;
        String thumbUrl = null;
        if (nguoiDung.coAnhDaiDien()) {
            avatarUrl = prefix + "/avatar?id=" + nguoiDung.getId();
            thumbUrl = prefix + "/avatar?id=" + nguoiDung.getId() + "&thumb=true";
        }

        ThongTinDieuHuongDTO dto = new ThongTinDieuHuongDTO(
                (int) nguoiDung.getId(),
                nguoiDung.getHoTen(),
                nguoiDung.getEmail(),
                nguoiDung.getChuoiVaiTroHienThi(),
                nguoiDung.getTenNhomKinhDoanh(),
                nguoiDung.getTenVietTat(),
                true,
                avatarUrl,
                thumbUrl,
                dsMenu
        );
        return dto;
    }
}
