package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
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
import vn.nhom10.crm.dto.DatLaiMatKhauResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.AuthService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử ForgotPasswordServlet (S1-03)")
class ForgotPasswordServletTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;

    private ForgotPasswordServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ForgotPasswordServlet();
        servlet.setAuthService(authService);
    }

    @Test
    @DisplayName("doGet: Người dùng đã đăng nhập thì redirect về /home")
    void testDoGet_DaDangNhap_RedirectHome() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new NguoiDung());
        when(request.getContextPath()).thenReturn("/crm-ban-hang");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm-ban-hang/home");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("doGet: Chưa đăng nhập thì forward tới JSP forgot-password.jsp")
    void testDoGet_ChuaDangNhap_ForwardJsp() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Email hợp lệ nhận thông báo thành công thống nhất (S1-03-AC1, S1-03-AC3)")
    void testDoPost_ThanhCong_HienThiThongBaoChung() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("user@company.vn");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("/crm-ban-hang");
        when(request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp")).thenReturn(requestDispatcher);

        DatLaiMatKhauResult result = DatLaiMatKhauResult.thanhCong(AuthService.THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG);
        when(authService.yeuCauDatLaiMatKhau(eq("user@company.vn"), eq("127.0.0.1"), eq("http://localhost:8080/crm-ban-hang")))
                .thenReturn(result);

        servlet.doPost(request, response);

        verify(request).setAttribute("successMessage", AuthService.THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG);
        verify(request).setAttribute("email", "");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Email để trống hoặc không hợp lệ trả lỗi validation")
    void testDoPost_EmailKhongHopLe_BaoLoi() throws ServletException, IOException {
        when(request.getParameter("email")).thenReturn("invalid-email");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("crm.example.com");
        when(request.getServerPort()).thenReturn(443);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp")).thenReturn(requestDispatcher);

        DatLaiMatKhauResult result = DatLaiMatKhauResult.thatBai("Định dạng email không hợp lệ");
        when(authService.yeuCauDatLaiMatKhau(eq("invalid-email"), eq("192.168.1.100"), eq("https://crm.example.com")))
                .thenReturn(result);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Định dạng email không hợp lệ");
        verify(request).setAttribute("email", "invalid-email");
        verify(requestDispatcher).forward(request, response);
    }
}
