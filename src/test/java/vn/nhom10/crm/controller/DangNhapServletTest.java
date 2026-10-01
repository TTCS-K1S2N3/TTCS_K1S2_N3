package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.DangNhapService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DangNhapServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private DangNhapService dangNhapService;

    private DangNhapServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new DangNhapServlet();
        servlet.setDangNhapService(dangNhapService);
    }

    @Test
    @DisplayName("Servlet doGet: Chưa đăng nhập thì chuyển tiếp tới trang JSP")
    void testDoGet_ChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(requestDispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Servlet doGet: Đã đăng nhập thì điều hướng thẳng tới trang chủ vai trò")
    void testDoGet_DaDangNhap() throws Exception {
        NguoiDung user = new NguoiDung();
        user.setEmail("sales@crm.vn");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(dangNhapService.xacDinhTrangChu(user)).thenReturn("/khach-hang");
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/khach-hang");
        verify(requestDispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("Servlet doPost: Đăng nhập thành công -> Lưu session và chuyển hướng tới trang chủ vai trò")
    void testDoPost_ThanhCong() throws Exception {
        NguoiDung user = new NguoiDung();
        user.setEmail("sales@crm.vn");

        when(request.getParameter("email")).thenReturn("sales@crm.vn");
        when(request.getParameter("matKhau")).thenReturn("123456@Aa");
        when(dangNhapService.dangNhap("sales@crm.vn", "123456@Aa"))
                .thenReturn(KetQuaDangNhapDTO.thanhCong(user, "/khach-hang"));
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doPost(request, response);

        verify(session).setAttribute("nguoiDung", user);
        verify(response).sendRedirect("/crm/khach-hang");
    }

    @Test
    @DisplayName("Servlet doPost: Đăng nhập thất bại -> Gán thông báo lỗi và forward về form")
    void testDoPost_ThatBai() throws Exception {
        when(request.getParameter("email")).thenReturn("sales@crm.vn");
        when(request.getParameter("matKhau")).thenReturn("WrongPassword");
        when(dangNhapService.dangNhap("sales@crm.vn", "WrongPassword"))
                .thenReturn(KetQuaDangNhapDTO.thatBai("Thông tin đăng nhập không chính xác."));
        when(request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp")).thenReturn(requestDispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), anyString());
        verify(request).setAttribute(eq("email"), eq("sales@crm.vn"));
        verify(requestDispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
