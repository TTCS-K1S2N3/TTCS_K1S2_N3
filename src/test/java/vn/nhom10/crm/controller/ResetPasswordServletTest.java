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
@DisplayName("Kiểm thử ResetPasswordServlet (S1-03)")
class ResetPasswordServletTest {

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

    private ResetPasswordServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ResetPasswordServlet();
        servlet.setAuthService(authService);
    }

    @Test
    @DisplayName("doGet: Đã đăng nhập thì redirect về /home")
    void testDoGet_DaDangNhap_RedirectHome() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(new NguoiDung());
        when(request.getContextPath()).thenReturn("/crm-ban-hang");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm-ban-hang/home");
    }

    @Test
    @DisplayName("doGet: Token rỗng hoặc thiếu thì báo lỗi token không hợp lệ")
    void testDoGet_TokenRong_BaoLoi() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("token")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("tokenHopLe", false);
        verify(request).setAttribute(eq("errorMessage"), eq(AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Token hợp lệ thì truyền cờ tokenHopLe=true và token vào request")
    void testDoGet_TokenHopLe_ChoPhepNhapMatKhau() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("token")).thenReturn("valid-token-123");
        when(authService.kiemTraTokenDatLaiMatKhau("valid-token-123"))
                .thenReturn(DatLaiMatKhauResult.tokenHopLe());
        when(request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("tokenHopLe", true);
        verify(request).setAttribute("token", "valid-token-123");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Token đã hết hạn hoặc đã sử dụng (AC1, AC2) thì báo lỗi")
    void testDoGet_TokenHetHanHoacDaDung_BaoLoi() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("token")).thenReturn("expired-or-used-token");
        when(authService.kiemTraTokenDatLaiMatKhau("expired-or-used-token"))
                .thenReturn(DatLaiMatKhauResult.thatBai(AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE));
        when(request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("tokenHopLe", false);
        verify(request).setAttribute("errorMessage", AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Đặt lại mật khẩu thành công thì redirect về /login?resetSuccess=1")
    void testDoPost_ThanhCong_RedirectLogin() throws ServletException, IOException {
        when(request.getParameter("token")).thenReturn("valid-token");
        when(request.getParameter("matKhauMoi")).thenReturn("NewSecret123!");
        when(request.getParameter("xacNhanMatKhau")).thenReturn("NewSecret123!");
        when(request.getContextPath()).thenReturn("/crm-ban-hang");

        when(authService.datLaiMatKhau("valid-token", "NewSecret123!", "NewSecret123!"))
                .thenReturn(DatLaiMatKhauResult.thanhCong("Đặt lại mật khẩu thành công"));

        servlet.doPost(request, response);

        verify(response).sendRedirect("/crm-ban-hang/login?resetSuccess=1");
    }

    @Test
    @DisplayName("doPost: Mật khẩu xác nhận không khớp thì báo lỗi và forward về JSP")
    void testDoPost_MatKhauKhongKhop_BaoLoi() throws ServletException, IOException {
        when(request.getParameter("token")).thenReturn("valid-token");
        when(request.getParameter("matKhauMoi")).thenReturn("NewSecret123!");
        when(request.getParameter("xacNhanMatKhau")).thenReturn("DifferentSecret123!");
        when(request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")).thenReturn(requestDispatcher);

        when(authService.datLaiMatKhau("valid-token", "NewSecret123!", "DifferentSecret123!"))
                .thenReturn(DatLaiMatKhauResult.thatBai("Mật khẩu xác nhận không khớp."));
        when(authService.kiemTraTokenDatLaiMatKhau("valid-token"))
                .thenReturn(DatLaiMatKhauResult.tokenHopLe());

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
        verify(request).setAttribute("token", "valid-token");
        verify(request).setAttribute("tokenHopLe", true);
        verify(requestDispatcher).forward(request, response);
    }
}
