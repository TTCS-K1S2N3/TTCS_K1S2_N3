package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DangNhapServiceTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    private DangNhapService dangNhapService;

    private final String rawPassword = "Password@123";
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        dangNhapService = new DangNhapService(nguoiDungDAO);
        hashedPassword = PasswordUtil.hashPassword(rawPassword);
    }

    private NguoiDung taoUserMau(String email, VaiTroEnum vaiTroEnum) {
        NguoiDung user = new NguoiDung();
        user.setId(10L);
        user.setHoTen("Nguyễn Văn Bán Hàng");
        user.setEmail(email);
        user.setMatKhau(hashedPassword);
        user.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        user.setSoLanSai(0);
        user.setThoiGianKhoa(null);
        if (vaiTroEnum != null) {
            user.themVaiTro(new VaiTro(vaiTroEnum));
        }
        return user;
    }

    @Test
    @DisplayName("AC1: Đăng nhập đúng với vai trò SALES_REP thì điều hướng tới /khach-hang")
    void testDangNhapDung_RoleSalesRep() {
        NguoiDung user = taoUserMau("sales@crm.vn", VaiTroEnum.SALES_REP);
        when(nguoiDungDAO.timTheoEmail("sales@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("sales@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        assertNotNull(result.getNguoiDung());
        assertEquals("sales@crm.vn", result.getNguoiDung().getEmail());
        assertEquals("/khach-hang", result.getTrangChuUrl());
        verify(nguoiDungDAO, never()).tangSoLanSai(anyLong());
    }

    @Test
    @DisplayName("AC1: Đăng nhập đúng với vai trò ADMIN thì điều hướng tới /nguoi-dung")
    void testDangNhapDung_RoleAdmin() {
        NguoiDung user = taoUserMau("admin@crm.vn", VaiTroEnum.ADMIN);
        when(nguoiDungDAO.timTheoEmail("admin@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("admin@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        assertEquals("/nguoi-dung", result.getTrangChuUrl());
    }

    @Test
    @DisplayName("AC1: Đăng nhập đúng với vai trò MARKETING thì điều hướng tới /lead")
    void testDangNhapDung_RoleMarketing() {
        NguoiDung user = taoUserMau("marketing@crm.vn", VaiTroEnum.MARKETING);
        when(nguoiDungDAO.timTheoEmail("marketing@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("marketing@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        assertEquals("/lead", result.getTrangChuUrl());
    }

    @Test
    @DisplayName("AC1: Đăng nhập đúng với vai trò ACCOUNTANT thì điều hướng tới /hop-dong")
    void testDangNhapDung_RoleAccountant() {
        NguoiDung user = taoUserMau("accountant@crm.vn", VaiTroEnum.ACCOUNTANT);
        when(nguoiDungDAO.timTheoEmail("accountant@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("accountant@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        assertEquals("/hop-dong", result.getTrangChuUrl());
    }

    @Test
    @DisplayName("AC1: Đăng nhập đúng thì reset số lần sai nếu trước đó có lần sai")
    void testDangNhapDung_ResetSoLanSai() {
        NguoiDung user = taoUserMau("sales@crm.vn", VaiTroEnum.SALES_REP);
        user.setSoLanSai(3);
        when(nguoiDungDAO.timTheoEmail("sales@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("sales@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        verify(nguoiDungDAO).resetSoLanSai(10L);
    }

    @Test
    @DisplayName("AC2: Email không tồn tại -> Thông báo chung, không tiết lộ email có tồn tại hay không")
    void testSaiThongTin_EmailKhongTonTai() {
        when(nguoiDungDAO.timTheoEmail("unknown@crm.vn")).thenReturn(null);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("unknown@crm.vn", "BatKy123!");

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapService.THONG_BAO_SAI_THONG_TIN, result.getThongBaoLoi());
        assertFalse(result.getThongBaoLoi().toLowerCase().contains("email không tồn tại"));
    }

    @Test
    @DisplayName("AC2: Email có tồn tại nhưng sai mật khẩu -> Hiển thị thông báo chung giống hệt khi email không tồn tại")
    void testSaiThongTin_SaiMatKhau() {
        NguoiDung user = taoUserMau("sales@crm.vn", VaiTroEnum.SALES_REP);
        user.setSoLanSai(1);
        when(nguoiDungDAO.timTheoEmail("sales@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("sales@crm.vn", "WrongPassword123");

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapService.THONG_BAO_SAI_THONG_TIN, result.getThongBaoLoi());
        verify(nguoiDungDAO).tangSoLanSai(10L);
    }

    @Test
    @DisplayName("AC3: Khóa tạm 15 phút sau 5 lần sai liên tiếp")
    void testKhoaTam_Sau5LanSaiLienTiep() {
        NguoiDung user = taoUserMau("sales@crm.vn", VaiTroEnum.SALES_REP);
        user.setSoLanSai(4); // Lần thứ 5 sẽ bị khóa
        when(nguoiDungDAO.timTheoEmail("sales@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("sales@crm.vn", "WrongPassword5th");

        assertFalse(result.isThanhCong());
        assertTrue(result.isBiKhoaTam());
        assertFalse(result.isBiKhoaAdmin());
        assertEquals(15, result.getSoPhutKhoaConLai());
        assertTrue(result.getThongBaoLoi().contains("bị khóa"));
        verify(nguoiDungDAO).khoaTam(10L, 15);
    }

    @Test
    @DisplayName("AC3: Tài khoản đang trong thời gian khóa tạm -> Bị chặn đăng nhập và thông báo thời gian còn lại")
    void testDangNhap_KhiDangBiKhoaTam() {
        NguoiDung user = taoUserMau("sales@crm.vn", VaiTroEnum.SALES_REP);
        user.setSoLanSai(5);
        // Đặt thời gian khóa tạm còn 10 phút nữa
        user.setThoiGianKhoa(new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000L));
        when(nguoiDungDAO.timTheoEmail("sales@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("sales@crm.vn", rawPassword);

        assertFalse(result.isThanhCong());
        assertTrue(result.isBiKhoaTam());
        assertFalse(result.isBiKhoaAdmin());
        assertEquals(10, result.getSoPhutKhoaConLai());
        assertTrue(result.getThongBaoLoi().contains("khóa"));
        verify(nguoiDungDAO, never()).tangSoLanSai(anyLong());
    }

    @Test
    @DisplayName("AC3: Đã hết thời gian khóa tạm 15 phút -> Tự động mở khóa và đăng nhập thành công nếu đúng mật khẩu")
    void testDangNhap_HetThoiGianKhoaTam() {
        NguoiDung user = taoUserMau("sales@crm.vn", VaiTroEnum.SALES_REP);
        user.setSoLanSai(5);
        // Thời gian khóa trong quá khứ (đã hết hạn)
        user.setThoiGianKhoa(new Timestamp(System.currentTimeMillis() - 60 * 1000L));
        when(nguoiDungDAO.timTheoEmail("sales@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("sales@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        verify(nguoiDungDAO, atLeastOnce()).resetSoLanSai(10L);
    }

    @Test
    @DisplayName("S1-10: Tài khoản trạng thái KHOA do Admin khóa -> Login bị từ chối, xác định là khóa quản trị, không có countdown")
    void testTaiKhoanBiKhoaVinhVien() {
        NguoiDung user = taoUserMau("locked@crm.vn", VaiTroEnum.SALES_REP);
        user.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
        when(nguoiDungDAO.timTheoEmail("locked@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("locked@crm.vn", rawPassword);

        assertFalse(result.isThanhCong());
        assertTrue(result.isBiKhoaAdmin());
        assertFalse(result.isBiKhoaTam());
        assertEquals(0, result.getSoPhutKhoaConLai());
        assertEquals("Tài khoản của bạn đã bị khóa hoặc ngừng hoạt động. Vui lòng liên hệ quản trị viên.", result.getThongBaoLoi());
        verify(nguoiDungDAO, never()).khoaTam(anyLong(), anyInt());
        verify(nguoiDungDAO, never()).tangSoLanSai(anyLong());
    }

    @Test
    @DisplayName("Tài khoản trạng thái NGUNG_HOAT_DONG -> Login bị từ chối, xác định là khóa quản trị")
    void testTaiKhoanNgungHoatDong() {
        NguoiDung user = taoUserMau("inactive@crm.vn", VaiTroEnum.SALES_REP);
        user.setTrangThai(NguoiDung.TRANG_THAI_NGUNG_HOAT_DONG);
        when(nguoiDungDAO.timTheoEmail("inactive@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("inactive@crm.vn", rawPassword);

        assertFalse(result.isThanhCong());
        assertTrue(result.isBiKhoaAdmin());
        assertFalse(result.isBiKhoaTam());
        assertEquals(0, result.getSoPhutKhoaConLai());
        assertTrue(result.getThongBaoLoi().contains("quản trị viên"));
        verify(nguoiDungDAO, never()).khoaTam(anyLong(), anyInt());
        verify(nguoiDungDAO, never()).tangSoLanSai(anyLong());
    }

    @Test
    @DisplayName("Validate dữ liệu đầu vào: Email hoặc mật khẩu để trống")
    void testValidateDauVao_DeTrong() {
        KetQuaDangNhapDTO r1 = dangNhapService.dangNhap("", "pass");
        assertFalse(r1.isThanhCong());
        assertTrue(r1.getThongBaoLoi().contains("đầy đủ"));

        KetQuaDangNhapDTO r2 = dangNhapService.dangNhap("test@crm.vn", " ");
        assertFalse(r2.isThanhCong());
        assertTrue(r2.getThongBaoLoi().contains("đầy đủ"));
    }

    @Test
    @DisplayName("S1-08/S2-01: Tài khoản CHO_KICH_HOAT đăng nhập đúng mật khẩu -> Kích hoạt sang HOAT_DONG")
    void testDangNhap_ChoKichHoat_DungMatKhau_KichHoatSangHoatDong() {
        NguoiDung user = taoUserMau("newuser@crm.vn", VaiTroEnum.SALES_REP);
        user.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
        when(nguoiDungDAO.timTheoEmail("newuser@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("newuser@crm.vn", rawPassword);

        assertTrue(result.isThanhCong(), "Đăng nhập phải thành công với mật khẩu hợp lệ");
        verify(nguoiDungDAO).kichHoatTaiKhoan(10L);
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, result.getNguoiDung().getTrangThai(),
                "Trạng thái người dùng phải được kích hoạt sang HOAT_DONG");
        assertTrue(result.getNguoiDung().dangHoatDong(), "dangHoatDong() phải trả về true");
        assertEquals("/khach-hang", result.getTrangChuUrl());
    }

    @Test
    @DisplayName("S1-08/S2-01: Tài khoản CHO_KICH_HOAT đăng nhập sai mật khẩu -> Báo lỗi, không kích hoạt")
    void testDangNhap_ChoKichHoat_SaiMatKhau_KhongKichHoat() {
        NguoiDung user = taoUserMau("newuser@crm.vn", VaiTroEnum.SALES_REP);
        user.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
        when(nguoiDungDAO.timTheoEmail("newuser@crm.vn")).thenReturn(user);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("newuser@crm.vn", "SaiMatKhau@123");

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapService.THONG_BAO_SAI_THONG_TIN, result.getThongBaoLoi());
        verify(nguoiDungDAO, never()).kichHoatTaiKhoan(anyLong());
        assertEquals(NguoiDung.TRANG_THAI_CHO_KICH_HOAT, user.getTrangThai(),
                "Khi sai mật khẩu, trạng thái CHO_KICH_HOAT phải được giữ nguyên");
    }

    @Test
    @DisplayName("Integration S1-08: User tạo thủ công đăng nhập bằng mật khẩu tạm -> Kích hoạt HOAT_DONG và truy cập được menu")
    void testDangNhap_UserMoiTuS108_KichHoatVaCoQuyenTruyCapModule() {
        NguoiDung userS108 = taoUserMau("s108_nvkd@crm.vn", VaiTroEnum.SALES_REP);
        userS108.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
        when(nguoiDungDAO.timTheoEmail("s108_nvkd@crm.vn")).thenReturn(userS108);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("s108_nvkd@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, result.getNguoiDung().getTrangThai());

        // Kiểm tra quyền truy cập module Khách hàng và điều hướng hệ thống
        MenuService menuService = MenuService.getInstance();
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(result.getNguoiDung(), "/khach-hang"),
                "User sau khi kích hoạt phải có quyền truy cập /khach-hang (không bị 403)");
        assertFalse(menuService.layDanhSachMenuChoNguoiDung(result.getNguoiDung(), "/khach-hang").isEmpty(),
                "Danh sách menu không được rỗng (không bị 0/12 module)");
    }

    @Test
    @DisplayName("Integration S2-01: User import từ Excel đăng nhập -> Kích hoạt HOAT_DONG và không bị 403 toàn hệ thống")
    void testDangNhap_UserMoiTuS201_KichHoatVaCoQuyenTruyCapModule() {
        NguoiDung userS201 = taoUserMau("s201_valid1@crm.vn", VaiTroEnum.SALES_REP);
        userS201.setTrangThai(NguoiDung.TRANG_THAI_CHO_KICH_HOAT);
        when(nguoiDungDAO.timTheoEmail("s201_valid1@crm.vn")).thenReturn(userS201);

        KetQuaDangNhapDTO result = dangNhapService.dangNhap("s201_valid1@crm.vn", rawPassword);

        assertTrue(result.isThanhCong());
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, result.getNguoiDung().getTrangThai());
        assertTrue(result.getNguoiDung().dangHoatDong());

        MenuService menuService = MenuService.getInstance();
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(result.getNguoiDung(), "/khach-hang"),
                "User S2-01 không được bị 403 trên /khach-hang sau khi đăng nhập");
        assertEquals(11, menuService.layDanhSachMenuChoNguoiDung(result.getNguoiDung(), "/khach-hang").size(),
                "User S2-01 SALES_REP phải thấy đủ 11/12 module khả dụng");
    }
}
