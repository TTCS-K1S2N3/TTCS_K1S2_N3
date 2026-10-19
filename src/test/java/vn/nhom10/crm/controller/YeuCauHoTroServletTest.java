package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.ThongTinRuiRoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.YeuCauHoTro;
import vn.nhom10.crm.service.YeuCauHoTroService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class YeuCauHoTroServletTest {

    private YeuCauHoTroServlet servlet;
    private YeuCauHoTroService service;
    private KhachHangDAO khachHangDAO;
    private NguoiDungDAO nguoiDungDAO;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung user;

    @BeforeEach
    public void setUp() throws Exception {
        servlet = new YeuCauHoTroServlet();
        service = mock(YeuCauHoTroService.class);
        khachHangDAO = mock(KhachHangDAO.class);
        nguoiDungDAO = mock(NguoiDungDAO.class);

        servlet.setYeuCauHoTroService(service);
        servlet.setKhachHangDAO(khachHangDAO);
        servlet.setNguoiDungDAO(nguoiDungDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        user = new NguoiDung();
        user.setId(10L);
        user.setHoTen("Nguyễn Văn CSKH");
        when(session.getAttribute("nguoiDung")).thenReturn(user);
    }

    @Test
    public void testDoGet_ChuaDangNhap_ChuyenHuong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    public void testDoGet_XemDanhSach_ForwardJSP() throws Exception {
        when(service.layDanhSachYeuCau(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(khachHangDAO.layTatCa()).thenReturn(Collections.emptyList());
        when(nguoiDungDAO.layTatCa()).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/yeu-cau-ho-tro/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testDoGet_ApiThongTinRuiRo() throws Exception {
        when(request.getParameter("action")).thenReturn("api-rui-ro");
        when(request.getParameter("khachHangId")).thenReturn("5");

        ThongTinRuiRoDTO ruiRoDTO = new ThongTinRuiRoDTO(5L, "Công ty ABC", 101L, "Sale 1",
                true, null, 3, 3, "Nguy cơ rời bỏ cao");
        when(service.layThongTinRuiRo(5L)).thenReturn(ruiRoDTO);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doGet(request, response);

        pw.flush();
        String json = sw.toString();
        assertTrue(json.contains("\"thanhCong\":true"));
        assertTrue(json.contains("\"coRuiRo\":true"));
        assertTrue(json.contains("\"soChuaXuLy\":3"));
    }

    @Test
    public void testDoPost_TaoYeuCau_ThanhCongRedirect() throws Exception {
        when(request.getParameter("action")).thenReturn("tao");
        when(request.getParameter("khachHangId")).thenReturn("5");
        when(request.getParameter("tieuDe")).thenReturn("Lỗi kết nối API");
        when(request.getParameter("mucUuTien")).thenReturn("CAO");
        when(request.getParameter("trangThai")).thenReturn("MOI");
        when(request.getParameter("redirectUrl")).thenReturn("/crm/chi-tiet-ban-ghi?id=5");

        YeuCauHoTro ychtSaved = new YeuCauHoTro(5L, "Lỗi kết nối API", null, "CAO", 10L);
        ychtSaved.setId(100L);
        ychtSaved.setMaYeuCau("TK-20261006-1234");
        when(service.ghiNhanYeuCau(any(), eq(user))).thenReturn(ychtSaved);

        ThongTinRuiRoDTO ruiRoDTO = new ThongTinRuiRoDTO(5L, "Công ty ABC", 101L, "Sale 1",
                false, null, 1, 3, "Ổn định");
        when(service.layThongTinRuiRo(5L)).thenReturn(ruiRoDTO);

        servlet.doPost(request, response);

        verify(service).ghiNhanYeuCau(any(), eq(user));
        verify(response).sendRedirect("/crm/chi-tiet-ban-ghi?id=5");
    }

    @Test
    public void testDoPost_CapNhatTrangThai_ThanhCongAjax() throws Exception {
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("action")).thenReturn("cap-nhat-trang-thai");
        when(request.getParameter("id")).thenReturn("100");
        when(request.getParameter("trangThai")).thenReturn("DA_XU_LY");

        YeuCauHoTro ycht = new YeuCauHoTro(5L, "Lỗi kết nối API", null, "CAO", 10L);
        ycht.setId(100L);
        ycht.setMaYeuCau("TK-1234");
        when(service.timTheoId(100L)).thenReturn(ycht);
        when(service.capNhatTrangThai(100L, "DA_XU_LY", user)).thenReturn(true);

        ThongTinRuiRoDTO ruiRoDTO = new ThongTinRuiRoDTO(5L, "Công ty ABC", 101L, "Sale 1",
                false, null, 0, 3, "Ổn định");
        when(service.layThongTinRuiRo(5L)).thenReturn(ruiRoDTO);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doPost(request, response);

        pw.flush();
        String json = sw.toString();
        assertTrue(json.contains("\"thanhCong\":true"));
        assertTrue(json.contains("\"coRuiRo\":false"));
    }
}
