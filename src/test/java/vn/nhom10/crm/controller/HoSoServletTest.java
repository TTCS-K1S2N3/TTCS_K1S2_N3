package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.HoSoService;
import vn.nhom10.crm.service.PhienService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Controller HoSoServlet (Story S2-02)")
class HoSoServletTest {

    private HoSoServlet servlet;
    private HoSoService hoSoService;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher requestDispatcher;

    private NguoiDung nguoiDungDangNhap;

    @BeforeEach
    void setUp() {
        servlet = new HoSoServlet();
        hoSoService = mock(HoSoService.class);
        servlet.setHoSoService(hoSoService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        requestDispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm-ban-hang");
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        nguoiDungDangNhap = new NguoiDung();
        nguoiDungDangNhap.setId(9L);
        nguoiDungDangNhap.setHoTen("Phan Duy Hưng");
        nguoiDungDangNhap.setEmail("dtc245200134@ictu.edu.vn");
        nguoiDungDangNhap.setSoDienThoai("0901234567");
        nguoiDungDangNhap.setChuKyEmail("Trân trọng,\nPhan Duy Hưng");
    }

    @Test
    @DisplayName("GET /ho-so: Người dùng chưa đăng nhập phải chuyển hướng về trang đăng nhập")
    void testDoGet_ChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm-ban-hang/dang-nhap?error=auth_required");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("GET /ho-so: Người dùng đã đăng nhập xem được thông tin hồ sơ của chính mình")
    void testDoGet_DaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDungDangNhap);
        when(hoSoService.layHoSo(9L)).thenReturn(nguoiDungDangNhap);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("nguoiDung"), eq(nguoiDungDangNhap));
        verify(request).getRequestDispatcher("/WEB-INF/views/nguoi-dung/ho-so.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /ho-so: Nhận param thanhCong=true và hiển thị thông báo thành công")
    void testDoGet_ThongBaoThanhCong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDungDangNhap);
        when(hoSoService.layHoSo(9L)).thenReturn(nguoiDungDangNhap);
        when(request.getParameter("thanhCong")).thenReturn("true");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("thongBaoThanhCong"), eq("Cập nhật hồ sơ cá nhân thành công."));
    }

    @Test
    @DisplayName("POST /ho-so: Chưa đăng nhập bị từ chối và chuyển hướng về đăng nhập")
    void testDoPost_ChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/crm-ban-hang/dang-nhap?error=auth_required");
        verify(hoSoService, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /ho-so: Cập nhật thành công họ tên, số điện thoại, chữ ký, đồng bộ session và redirect")
    void testDoPost_CapNhatThanhCong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDungDangNhap);

        when(request.getParameter("hoTen")).thenReturn("Phan Duy Hưng Mới");
        when(request.getParameter("soDienThoai")).thenReturn("0987654321");
        when(request.getParameter("chuKyEmail")).thenReturn("Chữ ký mới");

        NguoiDung ndCapNhat = new NguoiDung();
        ndCapNhat.setId(9L);
        ndCapNhat.setHoTen("Phan Duy Hưng Mới");
        ndCapNhat.setSoDienThoai("0987654321");
        ndCapNhat.setChuKyEmail("Chữ ký mới");

        KetQuaNguoiDungDTO ketQuaThanhCong = KetQuaNguoiDungDTO.thanhCong(ndCapNhat, "Cập nhật hồ sơ cá nhân thành công.", null);
        when(hoSoService.capNhatHoSo(eq(9L), eq("Phan Duy Hưng Mới"), eq("0987654321"), eq("Chữ ký mới"), any(), any(), any()))
                .thenReturn(ketQuaThanhCong);

        servlet.doPost(request, response);

        // Kiểm tra session được đồng bộ thông tin mới
        verify(session).setAttribute(eq(PhienService.SESSION_USER_KEY), eq(ndCapNhat));
        verify(response).sendRedirect("/crm-ban-hang/ho-so?thanhCong=true");
    }

    @Test
    @DisplayName("POST /ho-so: Lỗi validation (số điện thoại sai định dạng) forward lại view kèm thông báo lỗi")
    void testDoPost_LoiValidation() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDungDangNhap);

        when(request.getParameter("hoTen")).thenReturn("Phan Duy Hưng");
        when(request.getParameter("soDienThoai")).thenReturn("0123456789");
        when(request.getParameter("chuKyEmail")).thenReturn("Chữ ký");

        KetQuaNguoiDungDTO ketQuaLoi = new KetQuaNguoiDungDTO();
        ketQuaLoi.setThanhCong(false);
        ketQuaLoi.themLoi("soDienThoai", "Số điện thoại không đúng định dạng số điện thoại Việt Nam");
        ketQuaLoi.setThongBao("Thông tin nhập vào không hợp lệ.");

        when(hoSoService.capNhatHoSo(eq(9L), eq("Phan Duy Hưng"), eq("0123456789"), eq("Chữ ký"), any(), any(), any()))
                .thenReturn(ketQuaLoi);
        when(hoSoService.layHoSo(9L)).thenReturn(nguoiDungDangNhap);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("formError"), eq(ketQuaLoi.getDanhSachLoi()));
        verify(request).setAttribute(eq("thongBaoLoi"), eq("Thông tin nhập vào không hợp lệ."));
        verify(request).getRequestDispatcher("/WEB-INF/views/nguoi-dung/ho-so.jsp");
        verify(requestDispatcher).forward(request, response);
    }
}
