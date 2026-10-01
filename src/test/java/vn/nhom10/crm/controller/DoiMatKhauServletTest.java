package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.KetQuaDoiMatKhauDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.DoiMatKhauService;
import vn.nhom10.crm.util.SessionRegistry;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Controller DoiMatKhauServlet")
class DoiMatKhauServletTest {

    private DoiMatKhauServlet servlet;
    private DoiMatKhauService doiMatKhauService;
    private NguoiDungDAO nguoiDungDAO;
    private PhienDangNhapDAO phienDangNhapDAO;
    private SessionRegistry sessionRegistry;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() {
        servlet = new DoiMatKhauServlet();

        doiMatKhauService = mock(DoiMatKhauService.class);
        nguoiDungDAO = mock(NguoiDungDAO.class);
        phienDangNhapDAO = mock(PhienDangNhapDAO.class);
        sessionRegistry = mock(SessionRegistry.class);

        servlet.setDoiMatKhauService(doiMatKhauService);
        servlet.setNguoiDungDAO(nguoiDungDAO);
        servlet.setPhienDangNhapDAO(phienDangNhapDAO);
        servlet.setSessionRegistry(sessionRegistry);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        requestDispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm-ban-hang");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    private NguoiDung taoUserTest(long id, String hoTen, String email, String matKhau) {
        NguoiDung u = new NguoiDung();
        u.setId(id);
        u.setHoTen(hoTen);
        u.setEmail(email);
        u.setMatKhau(matKhau);
        u.setTrangThai("HOAT_DONG");
        return u;
    }

    @Test
    @DisplayName("GET /doi-mat-khau khi chưa đăng nhập -> Chuyển hướng sang trang đăng nhập")
    void testDoGet_ChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm-ban-hang/dang-nhap?error=auth_required");
        verify(requestDispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("GET /doi-mat-khau khi đã đăng nhập -> Chuyển tiếp tới JSP đổi mật khẩu")
    void testDoGet_DaDangNhap() throws Exception {
        NguoiDung user = taoUserTest(1L, "Khoàng Tuấn Hùng", "hung@crm.vn", "hash");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(session.getId()).thenReturn("SESSION_123");

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/auth/doi-mat-khau.jsp");
        verify(requestDispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("POST /doi-mat-khau thành công -> Lưu thông báo thành công và cập nhật session")
    void testDoPost_ThanhCong() throws Exception {
        NguoiDung user = taoUserTest(1L, "Khoàng Tuấn Hùng", "hung@crm.vn", "oldHash");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(session.getId()).thenReturn("SESSION_123");

        when(request.getParameter("matKhauHienTai")).thenReturn("CurrentPass@123");
        when(request.getParameter("matKhauMoi")).thenReturn("NewPass@2026");
        when(request.getParameter("xacNhanMatKhau")).thenReturn("NewPass@2026");
        when(request.getParameter("thuHoiPhienKhac")).thenReturn("true");

        KetQuaDoiMatKhauDTO ketQua = KetQuaDoiMatKhauDTO.thanhCong("Đổi mật khẩu thành công!", 2);
        when(doiMatKhauService.doiMatKhau(eq(1L), anyString(), anyString(), anyString(), eq(true), eq("SESSION_123")))
                .thenReturn(ketQua);

        NguoiDung updatedUser = taoUserTest(1L, "Khoàng Tuấn Hùng", "hung@crm.vn", "newHash");
        when(nguoiDungDAO.timTheoId(1L)).thenReturn(updatedUser);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Đổi mật khẩu thành công"));
        verify(session).setAttribute("nguoiDung", updatedUser);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /doi-mat-khau thất bại (sai mật khẩu hiện tại) -> Lưu thông báo lỗi")
    void testDoPost_ThatBai() throws Exception {
        NguoiDung user = taoUserTest(1L, "Khoàng Tuấn Hùng", "hung@crm.vn", "oldHash");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(session.getId()).thenReturn("SESSION_123");

        when(request.getParameter("matKhauHienTai")).thenReturn("SaiMatKhau");
        when(request.getParameter("matKhauMoi")).thenReturn("NewPass@2026");
        when(request.getParameter("xacNhanMatKhau")).thenReturn("NewPass@2026");
        when(request.getParameter("thuHoiPhienKhac")).thenReturn("true");

        KetQuaDoiMatKhauDTO ketQua = KetQuaDoiMatKhauDTO.thatBai("Mật khẩu hiện tại không chính xác.", "CURRENT_PASSWORD_INCORRECT");
        when(doiMatKhauService.doiMatKhau(eq(1L), anyString(), anyString(), anyString(), eq(true), eq("SESSION_123")))
                .thenReturn(ketQua);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), contains("Mật khẩu hiện tại không chính xác"));
        verify(requestDispatcher).forward(request, response);
    }
}
