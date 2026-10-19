package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.NguoiDungService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử NguoiDungServlet Controller")
class NguoiDungServletTest {

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private NguoiDungServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new NguoiDungServlet(nguoiDungService);
    }

    @Test
    @DisplayName("GET /nguoi-dung: Tải danh sách, phân trang mặc định và forward đến danh-sach.jsp")
    void testDoGetDanhSach() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung");
        when(request.getParameter("trang")).thenReturn("1");
        when(request.getParameter("tuKhoaTim")).thenReturn("Nam");
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/danh-sach.jsp")).thenReturn(dispatcher);

        when(nguoiDungService.layDanhSachNguoiDung(eq("Nam"), isNull(), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(Collections.emptyList());
        when(nguoiDungService.demTongSoNguoiDung(eq("Nam"), isNull(), isNull(), isNull())).thenReturn(0);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("trangHienTai"), eq(1));
        verify(request).setAttribute(eq("soBanGhiMoiTrang"), eq(20));
        verify(request).setAttribute(eq("tuKhoaTim"), eq("Nam"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /nguoi-dung/tao: Forward đến tao-tai-khoan.jsp")
    void testDoGetTaoTaiKhoan() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung/tao");
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/tao-tai-khoan.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /nguoi-dung/tao: Tạo thành công, lưu flash message và redirect về /nguoi-dung")
    void testDoPostTaoTaiKhoanThanhCong() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung/tao");
        when(request.getParameter("hoTen")).thenReturn("Lê Thị Thu");
        when(request.getParameter("email")).thenReturn("thult@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("2");
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"4"});
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getSession()).thenReturn(session);

        NguoiDung nd = new NguoiDung(10, "Lê Thị Thu", "thult@crm.vn");
        KetQuaNguoiDungDTO ketQua = KetQuaNguoiDungDTO.thanhCong(nd, "Tạo thành công", "Pass@123");
        when(nguoiDungService.taoTaiKhoan(any(NguoiDung.class), anyList(), anyString())).thenReturn(ketQua);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("thongBaoThanhCong"), eq("Tạo thành công"));
        verify(response).sendRedirect("/crm/nguoi-dung");
    }

    @Test
    @DisplayName("POST /nguoi-dung/tao: Email trùng lặp -> forward lại form kèm lỗi cụ thể")
    void testDoPostTaoTaiKhoanEmailTrung() throws Exception {
        when(request.getServletPath()).thenReturn("/nguoi-dung/tao");
        when(request.getParameter("hoTen")).thenReturn("Lê Thị Thu");
        when(request.getParameter("email")).thenReturn("admin@crm.vn");
        when(request.getParameter("nhomId")).thenReturn("1");
        when(request.getParameterValues("vaiTroIds")).thenReturn(new String[]{"4"});
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/tao-tai-khoan.jsp")).thenReturn(dispatcher);

        KetQuaNguoiDungDTO ketQuaLoi = KetQuaNguoiDungDTO.loiEmailTrung("admin@crm.vn");
        when(nguoiDungService.taoTaiKhoan(any(NguoiDung.class), anyList(), anyString())).thenReturn(ketQuaLoi);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("formError"), eq(ketQuaLoi.getDanhSachLoi()));
        verify(request).setAttribute(eq("oldInput"), anyMap());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Bảo mật phân quyền: User không có quyền ADMIN bị trả mã lỗi 403 Forbidden")
    void testPhanQuyenAdmin() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        NguoiDung regularUser = new NguoiDung(99, "Nhân Viên", "nv@crm.vn");
        regularUser.themVaiTro(new VaiTro(10, "SALES_REP", "Nhân viên kinh doanh", ""));
        when(session.getAttribute("nguoiDung")).thenReturn(regularUser);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }
}
