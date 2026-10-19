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

@DisplayName("Kiểm thử Menu điều hướng và phân quyền (Story S1-06 & S2-03)")
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
    @DisplayName("AC1: Admin có toàn quyền thấy đủ 12 mục menu")
    void testAdminThoatTatCaMenu() {
        NguoiDung admin = taoNguoiDung("Nguyễn Quản Trị", "Ban Quản Trị", VaiTroEnum.ADMIN);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(admin, "/tong-quan");

        assertEquals(12, dsMenu.size(), "Admin phải nhìn thấy tất cả 12 module");
        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertTrue(maModules.contains("NGUOI_DUNG"));
        assertTrue(maModules.contains("LEAD"));
        assertTrue(maModules.contains("BAO_GIA"));
        assertTrue(maModules.contains("CHI_TIEU"));
    }

    @Test
    @DisplayName("AC1: Sales Rep không được thấy menu Người dùng nhưng thấy Danh mục (READ)")
    void testSalesRepKhongThayMenuNguoiDung() {
        NguoiDung sales = taoNguoiDung("Thào A Khua", "Nhóm Kinh Doanh Miền Bắc", VaiTroEnum.SALES_REP);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(sales, "/khach-hang");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertFalse(maModules.contains("NGUOI_DUNG"), "Sales Rep không được thấy menu NGUOI_DUNG");
        assertTrue(maModules.contains("DANH_MUC"), "Sales Rep được thấy menu DANH_MUC theo quyền READ trong CSDL");
        assertTrue(maModules.contains("KHACH_HANG"));
        assertTrue(maModules.contains("LEAD"));
        assertTrue(maModules.contains("CO_HOI"));
        assertTrue(maModules.contains("BAO_GIA"));
        assertEquals(11, dsMenu.size());
    }

    @Test
    @DisplayName("AC1: Marketing không được thấy Báo giá, Hợp đồng, Chỉ tiêu, Người dùng nhưng thấy Danh mục (READ)")
    void testMarketingMenuPhanQuyen() {
        NguoiDung mkt = taoNguoiDung("Phạm Marketing", "Phòng Marketing", VaiTroEnum.MARKETING);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(mkt, "/lead");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertFalse(maModules.contains("BAO_GIA"), "Marketing không thấy Báo giá");
        assertFalse(maModules.contains("HOP_DONG"), "Marketing không thấy Hợp đồng");
        assertFalse(maModules.contains("CHI_TIEU"), "Marketing không thấy Chỉ tiêu");
        assertFalse(maModules.contains("NGUOI_DUNG"), "Marketing không thấy Người dùng");
        assertTrue(maModules.contains("DANH_MUC"), "Marketing thấy Danh mục theo quyền READ");

        assertTrue(maModules.contains("LEAD"), "Marketing thấy Lead");
        assertTrue(maModules.contains("TU_DONG_HOA"), "Marketing thấy Tự động hoá");
        assertEquals(8, dsMenu.size());
    }

    @Test
    @DisplayName("AC1: Chăm sóc khách hàng (Cust. Success) không thấy Lead, Chỉ tiêu, Người dùng nhưng thấy Danh mục (READ)")
    void testCustSuccessMenuPhanQuyen() {
        NguoiDung cskh = taoNguoiDung("Hoàng CSKH", "Phòng CSKH", VaiTroEnum.CUST_SUCCESS);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(cskh, "/khach-hang");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertFalse(maModules.contains("LEAD"), "CSKH không thấy Lead");
        assertFalse(maModules.contains("CHI_TIEU"), "CSKH không thấy Chỉ tiêu");
        assertFalse(maModules.contains("NGUOI_DUNG"), "CSKH không thấy Người dùng");
        assertTrue(maModules.contains("DANH_MUC"), "CSKH thấy Danh mục theo quyền READ");

        assertTrue(maModules.contains("KHACH_HANG"), "CSKH thấy Khách hàng");
        assertTrue(maModules.contains("BAO_GIA"), "CSKH thấy Báo giá");
        assertEquals(9, dsMenu.size());
    }

    @Test
    @DisplayName("AC1: Kế toán (Accountant) không thấy Lead, Hoạt động, Tự động hoá, Người dùng nhưng thấy Danh mục (READ)")
    void testAccountantMenuPhanQuyen() {
        NguoiDung acc = taoNguoiDung("Đỗ Kế Toán", "Phòng Kế Toán", VaiTroEnum.ACCOUNTANT);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(acc, "/hop-dong");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertFalse(maModules.contains("LEAD"), "Kế toán không thấy Lead");
        assertFalse(maModules.contains("HOAT_DONG"), "Kế toán không thấy Hoạt động");
        assertFalse(maModules.contains("TU_DONG_HOA"), "Kế toán không thấy Tự động hoá");
        assertFalse(maModules.contains("NGUOI_DUNG"), "Kế toán không thấy Người dùng");
        assertTrue(maModules.contains("DANH_MUC"), "Kế toán thấy Danh mục theo quyền READ");

        assertTrue(maModules.contains("HOP_DONG"), "Kế toán thấy Hợp đồng");
        assertTrue(maModules.contains("BAO_GIA"), "Kế toán thấy Báo giá");
        assertTrue(maModules.contains("CHI_TIEU"), "Kế toán thấy Chỉ tiêu");
        assertEquals(8, dsMenu.size());
    }

    @Test
    @DisplayName("AC1: Người dùng có nhiều vai trò được gộp quyền của các vai trò đó")
    void testNguoiDungNhieuVaiTro() {
        NguoiDung multi = taoNguoiDung("Nguyễn Hỗn Hợp", "Khối Tổng Hợp", VaiTroEnum.MARKETING, VaiTroEnum.ACCOUNTANT);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(multi, "/tong-quan");

        Set<String> maModules = dsMenu.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());
        assertTrue(maModules.contains("LEAD"), "Được quyền Lead từ Marketing");
        assertTrue(maModules.contains("BAO_GIA"), "Được quyền Báo giá từ Accountant");
        assertTrue(maModules.contains("HOP_DONG"), "Được quyền Hợp đồng từ Accountant");
        assertFalse(maModules.contains("NGUOI_DUNG"), "Cả 2 vai trò đều không có quyền Người dùng");
        assertTrue(maModules.contains("DANH_MUC"), "Cả 2 vai trò đều có quyền READ Danh mục theo DB matrix");
    }

    @Test
    @DisplayName("AC1: Tài khoản bị khoá hoặc null không hiển thị menu nào")
    void testTaiKhoanKhoaHoacNull() {
        NguoiDung khoa = taoNguoiDung("Vũ Bị Khoá", "Nhóm 1", VaiTroEnum.SALES_REP);
        khoa.setTrangThai(NguoiDung.TRANG_THAI_KHOA);

        List<MucMenuDTO> dsMenuKhoa = menuService.layDanhSachMenuChoNguoiDung(khoa, "/tong-quan");
        assertTrue(dsMenuKhoa.isEmpty(), "Tài khoản bị khoá không được trả về mục menu nào");

        List<MucMenuDTO> dsMenuNull = menuService.layDanhSachMenuChoNguoiDung(null, "/tong-quan");
        assertTrue(dsMenuNull.isEmpty(), "User null không được trả về mục menu nào");
    }

    @Test
    @DisplayName("AC2: Hiển thị đầy đủ Tên, Vai trò và Nhóm kinh doanh")
    void testHienThiThongTinNguoiDungVaNhom() {
        NguoiDung nd = taoNguoiDung("Thào A Khua", "Nhóm Kinh Doanh Miền Bắc", VaiTroEnum.SALES_REP);
        ThongTinDieuHuongDTO dto = menuService.layThongTinDieuHuong(nd, "/khach-hang");

        assertEquals("Thào A Khua", dto.getHoTen());
        assertEquals("Nhân viên kinh doanh", dto.getVaiTroHienThi());
        assertEquals("Nhóm Kinh Doanh Miền Bắc", dto.getTenNhomKinhDoanh());
        assertEquals("TK", dto.getTenVietTat());
        assertTrue(dto.isDaDangNhap());
    }

    @Test
    @DisplayName("AC2: Hiển thị đúng khi người dùng chưa phân nhóm và có nhiều vai trò")
    void testHienThiChuaPhanNhomVaNhieuVaiTro() {
        NguoiDung nd = taoNguoiDung("Lê Trưởng Nhóm", null, VaiTroEnum.TEAM_LEAD, VaiTroEnum.SALES_REP);
        ThongTinDieuHuongDTO dto = menuService.layThongTinDieuHuong(nd, "/tong-quan");

        assertEquals("Lê Trưởng Nhóm", dto.getHoTen());
        assertEquals("Chưa phân nhóm", dto.getTenNhomKinhDoanh());
        assertTrue(dto.getVaiTroHienThi().contains("Trưởng nhóm kinh doanh"));
        assertTrue(dto.getVaiTroHienThi().contains("Nhân viên kinh doanh"));
    }

    @Test
    @DisplayName("AC2: Hiển thị đầy đủ Tên, Vai trò, Nhóm kinh doanh và Avatar nếu có")
    void testHienThiAvatarVaThongTin() {
        NguoiDung nd = taoNguoiDung("Thào A Khua", "Nhóm Kinh Doanh Miền Bắc", VaiTroEnum.SALES_REP);
        nd.setAnhDaiDienPath("user_1/avatar.png");
        nd.setAnhDaiDienThumbPath("user_1/thumb.png");
        ThongTinDieuHuongDTO dto = menuService.layThongTinDieuHuong(nd, "/khach-hang", "/crm");

        assertEquals("Thào A Khua", dto.getHoTen());
        assertEquals("Nhân viên kinh doanh", dto.getVaiTroHienThi());
        assertEquals("Nhóm Kinh Doanh Miền Bắc", dto.getTenNhomKinhDoanh());
        assertTrue(dto.isCoAnhDaiDien());
        assertEquals("/crm/avatar?id=1", dto.getAnhDaiDienUrl());
        assertEquals("/crm/avatar?id=1&thumb=true", dto.getAnhDaiDienThumbUrl());
    }

    @Test
    @DisplayName("Phân quyền URL phía server (Server-side Authorization check)")
    void testPhanQuyenUrlServerSide() {
        NguoiDung mkt = taoNguoiDung("Phạm Marketing", "Marketing", VaiTroEnum.MARKETING);
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(mkt, "/bao-gia/tao-moi"));
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(mkt, "/hop-dong"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(mkt, "/lead"));

        NguoiDung acc = taoNguoiDung("Đỗ Kế Toán", "Kế toán", VaiTroEnum.ACCOUNTANT);
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(acc, "/lead"));
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(acc, "/hoat-dong"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(acc, "/hop-dong"));

        NguoiDung sales = taoNguoiDung("Thào A Khua", "KD", VaiTroEnum.SALES_REP);
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(sales, "/nguoi-dung"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(sales, "/danh-muc"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(sales, "/danh-muc-ban-hang"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(sales, "/bao-gia"));

        NguoiDung admin = taoNguoiDung("Nguyễn Admin", "Admin", VaiTroEnum.ADMIN);
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(admin, "/nguoi-dung"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(admin, "/danh-muc"));
    }

    @Test
    @DisplayName("Active menu indicator phản hồi đúng theo đường dẫn URL hiện tại")
    void testActiveMenuIndicator() {
        NguoiDung sales = taoNguoiDung("Thào A Khua", "KD", VaiTroEnum.SALES_REP);
        List<MucMenuDTO> dsMenu = menuService.layDanhSachMenuChoNguoiDung(sales, "/khach-hang/chi-tiet");

        MucMenuDTO khachHangMenu = dsMenu.stream()
                .filter(m -> "KHACH_HANG".equals(m.getMaModule()))
                .findFirst()
                .orElse(null);

        assertNotNull(khachHangMenu);
        assertTrue(khachHangMenu.isActive(), "Menu Khách hàng phải có trạng thái active khi truy cập /khach-hang/chi-tiet");

        MucMenuDTO leadMenu = dsMenu.stream()
                .filter(m -> "LEAD".equals(m.getMaModule()))
                .findFirst()
                .orElse(null);

        assertNotNull(leadMenu);
        assertFalse(leadMenu.isActive(), "Menu Lead không được active khi đang ở /khach-hang/chi-tiet");

        // Story S3-08: Menu Khách hàng active khi ở trang Yêu cầu hỗ trợ và Trang 360
        List<MucMenuDTO> dsMenuYcht = menuService.layDanhSachMenuChoNguoiDung(sales, "/yeu-cau-ho-tro");
        MucMenuDTO khYcht = dsMenuYcht.stream().filter(m -> "KHACH_HANG".equals(m.getMaModule())).findFirst().orElse(null);
        assertNotNull(khYcht);
        assertTrue(khYcht.isActive(), "Menu Khách hàng phải active khi truy cập /yeu-cau-ho-tro");

        List<MucMenuDTO> dsMenu360 = menuService.layDanhSachMenuChoNguoiDung(sales, "/chi-tiet-ban-ghi?id=1");
        MucMenuDTO kh360 = dsMenu360.stream().filter(m -> "KHACH_HANG".equals(m.getMaModule())).findFirst().orElse(null);
        assertNotNull(kh360);
        assertTrue(kh360.isActive(), "Menu Khách hàng phải active khi truy cập /chi-tiet-ban-ghi");

        assertTrue(menuService.kiemTraQuyenTruyCapUrl(sales, "/yeu-cau-ho-tro"));
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(sales, "/chi-tiet-ban-ghi?id=1"));
    }
}
