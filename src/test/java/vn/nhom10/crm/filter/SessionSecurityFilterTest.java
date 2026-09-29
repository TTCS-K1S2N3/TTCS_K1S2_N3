package vn.nhom10.crm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionSecurityFilterTest {

    @Mock
    private PhienService phienService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private SessionSecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SessionSecurityFilter();
        filter.setPhienService(phienService);
    }

    @Test
    @DisplayName("Đường dẫn công khai (/dang-nhap, /assets/*) được bỏ qua filter mà không cần phiên")
    void testDuongDanCongKhaiBoQuaFilter() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/crm/dang-nhap");
        when(request.getContextPath()).thenReturn("/crm");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(phienService);
    }

    @Test
    @DisplayName("AC3: Truy cập trang bảo vệ khi chưa đăng nhập hoặc phiên hết hạn thì chuyển hướng về trang đăng nhập")
    void testTrangBaoVeKhongCoPhienChuyenHuongDangNhap() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/crm/khach-hang");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/crm/dang-nhap?error=session_expired");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("AC3: Yêu cầu AJAX khi phiên hết hạn trả về HTTP 401 kèm JSON lỗi")
    void testAjaxKhiPhienHetHanTraVe401Json() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/crm/api/khach-hang");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("SESSION_EXPIRED"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("AC1: Phiên đang hoạt động thì tự động gia hạn và tiếp tục filter chain")
    void testPhienDangHoatDongTuDongGiaHanVaTiepTuc() throws IOException, ServletException {
        String maPhien = "VALID-SESSION-123";
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(1L);

        when(request.getRequestURI()).thenReturn("/crm/khach-hang");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDung);
        when(session.getAttribute(PhienService.SESSION_TOKEN_KEY)).thenReturn(maPhien);

        when(phienService.kiemTraPhienHopLe(maPhien)).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(phienService).giaHanPhien(maPhien, session);
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("AC3: Phiên hết hạn trong database sẽ bị đăng xuất ngay và chuyển hướng")
    void testPhienHetHanTrongDatabaseBiDangXuatVaChuyenHuong() throws IOException, ServletException {
        String maPhien = "EXPIRED-IN-DB";
        NguoiDung nguoiDung = new NguoiDung();

        when(request.getRequestURI()).thenReturn("/crm/khach-hang");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDung);
        when(session.getAttribute(PhienService.SESSION_TOKEN_KEY)).thenReturn(maPhien);

        when(phienService.kiemTraPhienHopLe(maPhien)).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(phienService).dangXuat(request, response);
        verify(response).sendRedirect("/crm/dang-nhap?error=session_expired");
        verify(chain, never()).doFilter(any(), any());
    }
}
