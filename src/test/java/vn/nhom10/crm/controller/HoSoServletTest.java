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
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.HoSoService;
import vn.nhom10.crm.service.MenuService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoSoServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private HoSoService hoSoService;

    @Mock
    private MenuService menuService;

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    private HoSoServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new HoSoServlet(hoSoService, menuService, nguoiDungDAO);
    }

    @Test
    @DisplayName("GET /ho-so: forward tới ho-so-ca-nhan.jsp với thông tin người dùng và điều hướng")
    void testDoGet_HienThiHoSo() throws Exception {
        NguoiDung user = new NguoiDung(1, "Bàn Thị Linh", "linh.ban@crm.vn");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(nguoiDungDAO.timTheoId(1L)).thenReturn(user);

        when(request.getRequestURI()).thenReturn("/crm/ho-so");
        when(request.getContextPath()).thenReturn("/crm");

        ThongTinDieuHuongDTO dieuHuong = new ThongTinDieuHuongDTO();
        when(menuService.layThongTinDieuHuong(any(NguoiDung.class), eq("/crm/ho-so"), eq("/crm"))).thenReturn(dieuHuong);

        when(request.getRequestDispatcher("/WEB-INF/views/ho-so/ho-so-ca-nhan.jsp")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("nguoiDung"), any(NguoiDung.class));
        verify(request).setAttribute(eq("dieuHuong"), any(ThongTinDieuHuongDTO.class));
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /ho-so?action=capNhatThongTin: cập nhật họ tên và chữ ký thành công")
    void testDoPost_CapNhatThongTin() throws Exception {
        NguoiDung user = new NguoiDung(1, "Bàn Thị Linh", "linh.ban@crm.vn");
        when(request.getSession(true)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getParameter("action")).thenReturn("capNhatThongTin");
        when(request.getParameter("hoTen")).thenReturn("Bàn Thị Linh Mới");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameter("chuKyEmail")).thenReturn("Trân trọng");
        when(request.getContextPath()).thenReturn("/crm");

        when(hoSoService.capNhatHoSo(1, "Bàn Thị Linh Mới", "0912345678", "Trân trọng")).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("flashMessageSuccess"), anyString());
        verify(response).sendRedirect("/crm/ho-so");
    }
}
