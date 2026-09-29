package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DangNhapServiceTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    private DangNhapService dangNhapService;

    @BeforeEach
    void setUp() {
        dangNhapService = new DangNhapService(nguoiDungDAO);
    }

    @Test
    @DisplayName("Đăng nhập thành công với email và mật khẩu đúng")
    void testDangNhapThanhCong() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(1L);
        nguoiDung.setEmail("admin@congty.vn");
        nguoiDung.setMatKhau(PasswordUtil.bamMatKhau("123456"));
        nguoiDung.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        nguoiDung.setSoLanSai(0);

        when(nguoiDungDAO.timTheoEmail("admin@congty.vn")).thenReturn(nguoiDung);

        DangNhapService.KetQuaDangNhap ketQua = dangNhapService.dangNhap("admin@congty.vn", "123456");

        assertTrue(ketQua.isThanhCong());
        assertNotNull(ketQua.getNguoiDung());
        verify(nguoiDungDAO).capNhatDangNhapThanhCong(eq(1L), any(Timestamp.class));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu tăng số lần sai và trả về thông báo chung")
    void testDangNhapSaiMatKhau() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(1L);
        nguoiDung.setEmail("admin@congty.vn");
        nguoiDung.setMatKhau(PasswordUtil.bamMatKhau("123456"));
        nguoiDung.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        nguoiDung.setSoLanSai(1);

        when(nguoiDungDAO.timTheoEmail("admin@congty.vn")).thenReturn(nguoiDung);

        DangNhapService.KetQuaDangNhap ketQua = dangNhapService.dangNhap("admin@congty.vn", "saiMatKhau");

        assertFalse(ketQua.isThanhCong());
        assertEquals("Email hoặc mật khẩu không chính xác.", ketQua.getThongBaoLoi());
        verify(nguoiDungDAO).capNhatDangNhapThatBai(eq(1L), eq(2), isNull());
    }

    @Test
    @DisplayName("Đăng nhập sai lần thứ 5 thì bị khóa tạm 15 phút")
    void testDangNhapSai5LanBiKhoa15Phut() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(1L);
        nguoiDung.setEmail("admin@congty.vn");
        nguoiDung.setMatKhau(PasswordUtil.bamMatKhau("123456"));
        nguoiDung.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        nguoiDung.setSoLanSai(4);

        when(nguoiDungDAO.timTheoEmail("admin@congty.vn")).thenReturn(nguoiDung);

        DangNhapService.KetQuaDangNhap ketQua = dangNhapService.dangNhap("admin@congty.vn", "saiMatKhau");

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBaoLoi().contains("tạm khóa 15 phút"));
        verify(nguoiDungDAO).capNhatDangNhapThatBai(eq(1L), eq(5), any(Timestamp.class));
    }
}
