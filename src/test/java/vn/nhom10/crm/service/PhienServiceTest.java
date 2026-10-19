package vn.nhom10.crm.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhienDangNhap;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhienServiceTest {

    @Mock
    private PhienDangNhapDAO phienDangNhapDAO;

    @Mock
    private HttpSession httpSession;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private PhienService phienService;

    @BeforeEach
    void setUp() {
        phienService = new PhienService(phienDangNhapDAO);
    }

    @Test
    @DisplayName("AC1: Tạo phiên mới thành công và lưu thông tin vào Session và Database")
    void testTaoPhienMoiThanhCong() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(10L);
        nguoiDung.setEmail("sales@congty.vn");

        when(httpSession.getId()).thenReturn("TEST-SESSION-ID-123");

        PhienDangNhap phien = phienService.taoPhienMoi(nguoiDung, httpSession, "192.168.1.10", "Mozilla/5.0");

        assertNotNull(phien);
        assertEquals("TEST-SESSION-ID-123", phien.getMaPhien());
        assertEquals(10L, phien.getNguoiDungId());
        assertEquals(PhienDangNhap.TRANG_THAI_HOAT_DONG, phien.getTrangThai());

        verify(phienDangNhapDAO).luuPhien(any(PhienDangNhap.class));
        verify(httpSession).setMaxInactiveInterval(30 * 60);
        verify(httpSession).setAttribute(eq(PhienService.SESSION_USER_KEY), eq(nguoiDung));
        verify(httpSession).setAttribute(eq(PhienService.SESSION_TOKEN_KEY), eq("TEST-SESSION-ID-123"));
    }

    @Test
    @DisplayName("AC1: Gia hạn phiên tự động thành công khi phiên còn hoạt động")
    void testGiaHanPhienThanhCongKhiDangHoatDong() {
        String maPhien = "ACTIVE-SESSION-999";
        PhienDangNhap phien = new PhienDangNhap();
        phien.setMaPhien(maPhien);
        phien.setTrangThai(PhienDangNhap.TRANG_THAI_HOAT_DONG);
        long now = System.currentTimeMillis();
        phien.setThoiGianHoatDongCuoi(new Timestamp(now - 5 * 60 * 1000L));

        when(phienDangNhapDAO.timTheoMaPhien(maPhien)).thenReturn(phien);
        when(phienDangNhapDAO.capNhatHoatDongCuoi(eq(maPhien))).thenReturn(true);

        boolean ketQua = phienService.giaHanPhien(maPhien, httpSession);

        assertTrue(ketQua, "Phiên còn hoạt động phải được gia hạn thành công");
        verify(phienDangNhapDAO).capNhatHoatDongCuoi(eq(maPhien));
        verify(httpSession).setMaxInactiveInterval(30 * 60);
    }

    @Test
    @DisplayName("AC3: Phiên hết hạn quá 30 phút không hoạt động thì gia hạn thất bại và bị đánh dấu HET_HAN")
    void testGiaHanPhienThatBaiKhiQuaThoiGianKhongHoatDong() {
        String maPhien = "EXPIRED-SESSION-111";
        PhienDangNhap phien = new PhienDangNhap();
        phien.setMaPhien(maPhien);
        phien.setTrangThai(PhienDangNhap.TRANG_THAI_HOAT_DONG);
        // Hoạt động cuối cách đây 35 phút (> 30 phút)
        long now = System.currentTimeMillis();
        phien.setThoiGianHoatDongCuoi(new Timestamp(now - 35 * 60 * 1000L));

        when(phienDangNhapDAO.timTheoMaPhien(maPhien)).thenReturn(phien);

        boolean ketQua = phienService.giaHanPhien(maPhien, httpSession);

        assertFalse(ketQua, "Phiên quá 30 phút không hoạt động không được gia hạn");
        verify(phienDangNhapDAO).voHieuHoaPhien(eq(maPhien), eq(PhienDangNhap.TRANG_THAI_HET_HAN));
        verify(phienDangNhapDAO, never()).capNhatHoatDongCuoi(anyString());
    }

    @Test
    @DisplayName("AC2: Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server và database")
    void testDangXuatMatHieuLucPhienNgayLapTuc() {
        String maPhien = "SESSION-TO-LOGOUT";
        when(request.getSession(false)).thenReturn(httpSession);
        when(httpSession.getAttribute(PhienService.SESSION_TOKEN_KEY)).thenReturn(maPhien);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("JSESSIONID", "SOME-COOKIE-VALUE")});
        when(request.getContextPath()).thenReturn("/crm");

        phienService.dangXuat(request, response);

        // 1. Session bị hủy ngay lập tức
        verify(httpSession).removeAttribute(PhienService.SESSION_USER_KEY);
        verify(httpSession).removeAttribute(PhienService.SESSION_TOKEN_KEY);
        verify(httpSession).invalidate();

        // 2. Database cập nhật trạng thái DA_DANG_XUAT
        verify(phienDangNhapDAO).voHieuHoaPhien(maPhien, PhienDangNhap.TRANG_THAI_DA_DANG_XUAT);

        // 3. Cookie JSESSIONID được đặt MaxAge = 0
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie clearedCookie = cookieCaptor.getValue();
        assertEquals("JSESSIONID", clearedCookie.getName());
        assertEquals(0, clearedCookie.getMaxAge());
    }

    @Test
    @DisplayName("AC3: Kiểm tra phiên hợp lệ trả về false nếu phiên đã hết hạn trong DB")
    void testKiemTraPhienHopLeTraVeFalseKhiHetHan() {
        String maPhien = "EXPIRED-SESSION";
        PhienDangNhap phien = new PhienDangNhap();
        phien.setMaPhien(maPhien);
        phien.setTrangThai(PhienDangNhap.TRANG_THAI_HOAT_DONG);
        long now = System.currentTimeMillis();
        phien.setThoiGianHoatDongCuoi(new Timestamp(now - 31 * 60 * 1000L));

        when(phienDangNhapDAO.timTheoMaPhien(maPhien)).thenReturn(phien);

        boolean hopLe = phienService.kiemTraPhienHopLe(maPhien);

        assertFalse(hopLe);
        verify(phienDangNhapDAO).voHieuHoaPhien(maPhien, PhienDangNhap.TRANG_THAI_HET_HAN);
    }
}
