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
import vn.nhom10.crm.dto.DangNhapResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.AuthService;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử LoginServlet")
class LoginServletTest {

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

    private LoginServlet loginServlet;

    @BeforeEach
    void setUp() {
        loginServlet = new LoginServlet();
        loginServlet.setAuthService(authService);
    }

    @Test
    @DisplayName("doGet: Chưa đăng nhập thì chuyển tới trang JSP login")
    void testDoGet_ChuaDangNhap_ForwardToJsp() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp")).thenReturn(requestDispatcher);

        loginServlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Đã đăng nhập rồi thì chuyển hướng tới /home")
    void testDoGet_DaDangNhap_RedirectHome() throws ServletException, IOException {
        NguoiDung user = new NguoiDung();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getContextPath()).thenReturn("/crm");

        loginServlet.doGet(request, response);

        verify(response).sendRedirect("/crm/home");
    }

    @Test
    @DisplayName("doPost: Đăng nhập thành công thì lưu session và chuyển hướng tới /home (AC1)")
    void testDoPost_DangNhapThanhCong_ChuyenHuongTrangChu() throws ServletException, IOException {
        NguoiDung user = new NguoiDung();
        user.setId(10L);
        user.setEmail("director@crm.vn");
        user.setDanhSachVaiTro(List.of(new VaiTro(3L, "DIRECTOR", "Giám đốc kinh doanh", "", "TOAN_BO")));

        lenient().when(request.getParameter("email")).thenReturn("director@crm.vn");
        lenient().when(request.getParameter("matKhau")).thenReturn("Secr3t#123");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("User-Agent")).thenReturn("Chrome");
        lenient().when(request.getContextPath()).thenReturn("/crm");
        lenient().when(request.getSession(false)).thenReturn(null);
        lenient().when(request.getSession(true)).thenReturn(session);
        lenient().when(session.getId()).thenReturn("mock-session-id");

        when(authService.dangNhap(eq("director@crm.vn"), eq("Secr3t#123"), anyString(), anyString(), isNull()))
                .thenReturn(DangNhapResult.thanhCong(user));

        loginServlet.doPost(request, response);

        verify(session).setAttribute("user", user);
        verify(response).sendRedirect("/crm/home");
    }

    @Test
    @DisplayName("doPost: Đăng nhập thất bại thì trả về thông báo lỗi và giữ lại email (AC2 & AC3)")
    void testDoPost_DangNhapThatBai_TraVeFormVoiThongBao() throws ServletException, IOException {
        lenient().when(request.getParameter("email")).thenReturn("user@crm.vn");
        lenient().when(request.getParameter("matKhau")).thenReturn("WrongPass");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("User-Agent")).thenReturn("Chrome");
        lenient().when(request.getSession(false)).thenReturn(null);
        lenient().when(request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp")).thenReturn(requestDispatcher);

        when(authService.dangNhap(eq("user@crm.vn"), eq("WrongPass"), anyString(), any(), isNull()))
                .thenReturn(DangNhapResult.saiThongTin(AuthService.THONG_BAO_SAI_THONG_TIN));

        loginServlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", AuthService.THONG_BAO_SAI_THONG_TIN);
        verify(request).setAttribute("email", "user@crm.vn");
        verify(requestDispatcher).forward(request, response);
    }
}
