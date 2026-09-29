package vn.nhom10.crm.service;

import vn.nhom10.crm.dto.MucMenuDTO;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Service xử lý logic điều hướng menu, kiểm tra quyền truy cập module
 * và chuẩn bị dữ liệu hiển thị cho thanh điều hướng hệ thống.
 */
public class MenuService {

    private static final MenuService INSTANCE = new MenuService();

    public static MenuService getInstance() {
        return INSTANCE;
    }

    public boolean kiemTraQuyenTruyCap(NguoiDung nguoiDung, ModuleHeThong module) {
        if (nguoiDung == null || !nguoiDung.dangHoatDong() || module == null) {
            return false;
        }

        Set<VaiTroEnum> danhSachVaiTro = nguoiDung.getDanhSachVaiTroEnum();
        if (danhSachVaiTro == null || danhSachVaiTro.isEmpty()) {
            return false;
        }

        if (danhSachVaiTro.contains(VaiTroEnum.ADMIN)) {
            return true;
        }

        return module.choPhepBatKyVaiTro(danhSachVaiTro);
    }

    public boolean kiemTraQuyenTruyCapUrl(NguoiDung nguoiDung, String urlPath) {
        if (urlPath == null || urlPath.isBlank()) {
            return true;
        }
        ModuleHeThong module = ModuleHeThong.tuDuongDan(urlPath);
        if (module == null) {
            return true;
        }
        return kiemTraQuyenTruyCap(nguoiDung, module);
    }

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
                            || currentUri.startsWith(mod.getDuongDanUrl() + "/");
                }
                MucMenuDTO dto = new MucMenuDTO(
                        mod.getMaModule(),
                        mod.getTenHienThi(),
                        mod.getDuongDanUrl(),
                        mod.getBieuTuong(),
                        mod.getThuTu(),
                        active
                );
                ketQua.add(dto);
            }
        }
        return ketQua;
    }

    public ThongTinDieuHuongDTO layThongTinDieuHuong(NguoiDung nguoiDung, String currentUri) {
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

        return new ThongTinDieuHuongDTO(
                nguoiDung.getHoTen(),
                nguoiDung.getEmail(),
                nguoiDung.getChuoiVaiTroHienThi(),
                nguoiDung.getTenNhomKinhDoanh(),
                nguoiDung.getTenVietTat(),
                true,
                dsMenu
        );
    }
}
