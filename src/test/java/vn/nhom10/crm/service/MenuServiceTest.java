package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.MucMenuDTO;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Menu điều hướng phân quyền và thông tin người dùng")
class MenuServiceTest {

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = MenuService.getInstance();
    }

    private NguoiDung taoNguoiDung(String hoTen, String tenNhom, VaiTroEnum... vaiTros) {
        NguoiDung nd = new NguoiDung(1, hoTen, "user@crm.vn");
        if (tenNhom != null) {
            nd.setNhomKinhDoanh(new NhomKinhDoanh(1, tenNhom));
        }
        for (VaiTroEnum vt : vaiTros) {
            nd.themVaiTro(new VaiTro(vt));
        }
        return nd;
    }

    @Test
    @DisplayName("Admin có toàn quyền thấy đủ 12 mục menu")
    void testAdminThoatTatCaMenu() {
        NguoiDung admin = taoNguoiDung("Nguyễn Quản Trị", "Ban Quản Trị", VaiTroEnum.ADMIN);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(admin, "/tong-quan");

        assertEquals(12, dsMenu.size(), "Admin phải nhìn thấy tất cả 12 module");
    }

    @Test
    @DisplayName("Sales Rep không được thấy menu Người dùng & Hệ thống")
    void testSalesRepKhongThayMenuNguoiDung() {
        NguoiDung sales = taoNguoiDung("Thào A Khua", "Nhóm Kinh Doanh Miền Bắc", VaiTroEnum.SALES_REP);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(sales, "/khach-hang");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertFalse(maModules.contains("NGUOI_DUNG"), "Sales Rep không được thấy menu NGUOI_DUNG");
        assertEquals(11, dsMenu.size());
    }

    @Test
    @DisplayName("Người dùng có nhiều vai trò được gộp quyền của các vai trò đó")
    void testNguoiDungNhieuVaiTro() {
        NguoiDung multi = taoNguoiDung("Nguyễn Hỗn Hợp", "Khối Tổng Hợp", VaiTroEnum.MARKETING, VaiTroEnum.ACCOUNTANT);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(multi, "/tong-quan");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertTrue(maModules.contains("LEAD"), "Được quyền Lead từ Marketing");
        assertTrue(maModules.contains("BAO_GIA"), "Được quyền Báo giá từ Accountant");
        assertTrue(maModules.contains("HOP_DONG"), "Được quyền Hợp đồng từ Accountant");
        assertFalse(maModules.contains("NGUOI_DUNG"), "Cả 2 vai trò đều không có quyền Người dùng");
    }

    @Test
    @DisplayName("Hiển thị đầy đủ Tên, Vai trò và Nhóm kinh doanh")
    void testHienThiThongTinNguoiDungVaNhom() {
        NguoiDung nd = taoNguoiDung("Thào A Khua", "Nhóm Kinh Doanh Miền Bắc", VaiTroEnum.SALES_REP);
        ThongTinDieuHuongDTO dto = menuService.layThongTinDieuHuong(nd, "/khach-hang");

        assertEquals("Thào A Khua", dto.getHoTen());
        assertEquals("Nhân viên kinh doanh", dto.getVaiTroHienThi());
        assertEquals("Nhóm Kinh Doanh Miền Bắc", dto.getTenNhomKinhDoanh());
        assertEquals("TK", dto.getTenVietTat());
        assertTrue(dto.isDaDangNhap());
    }
}
