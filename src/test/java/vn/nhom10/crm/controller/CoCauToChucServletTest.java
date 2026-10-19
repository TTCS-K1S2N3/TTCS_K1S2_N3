package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.CoCauToChucService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử CoCauToChucServlet - Story S2-06 (AC1, AC2, AC3, AC4)")
class CoCauToChucServletTest {

    private CoCauToChucServlet servlet;
    private CoCauToChucService service;
    private NguoiDungDAO nguoiDungDAO;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new CoCauToChucServlet();
        service = mock(CoCauToChucService.class);
        nguoiDungDAO = mock(NguoiDungDAO.class);
        servlet.setCoCauToChucService(service);
        servlet.setNguoiDungDAO(nguoiDungDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private NguoiDung taoUser(long id, String hoTen, VaiTroEnum vaiTroEnum) {
        NguoiDung u = new NguoiDung(id, hoTen, "user" + id + "@crm.vn");
        VaiTro vt = new VaiTro();
        vt.setMaVaiTro(vaiTroEnum.getMaVaiTro());
        vt.setTenVaiTro(vaiTroEnum.getTenTiengViet());
        u.setDanhSachVaiTro(new HashSet<>(Collections.singletonList(vt)));
        return u;
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập truy cập /co-cau-to-chuc bị chuyển hướng đăng nhập")
    void testChuaDangNhap_RedirectDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("Bảo mật: Nhân viên không có quyền (ví dụ SALES_REP) bị trả về lỗi 403")
    void testKhongCoQuyen_TraVe403() throws Exception {
        NguoiDung sales = taoUser(101L, "Sales Rep", VaiTroEnum.SALES_REP);
        when(session.getAttribute("nguoiDung")).thenReturn(sales);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Phân quyền: Giám đốc kinh doanh (DIRECTOR) được phép truy cập màn hình cơ cấu tổ chức")
    void testGiamDoc_TruyCapThanhCong() throws Exception {
        NguoiDung director = taoUser(1L, "Giám đốc kinh doanh", VaiTroEnum.DIRECTOR);
        when(session.getAttribute("nguoiDung")).thenReturn(director);
        when(request.getPathInfo()).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/co-cau-to-chuc/index.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST them-nhom: Thêm nhóm kinh doanh thành công và redirect")
    void testPostThemNhom() throws Exception {
        NguoiDung admin = taoUser(1L, "Admin", VaiTroEnum.ADMIN);
        when(session.getAttribute("nguoiDung")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("them-nhom");
        when(request.getParameter("maNhom")).thenReturn("KD_MOI");
        when(request.getParameter("tenNhom")).thenReturn("Khối Kinh Doanh Mới");
        when(request.getParameter("moTa")).thenReturn("Mô tả");
        when(request.getParameter("nhomChaId")).thenReturn("");
        when(request.getParameter("khuVucId")).thenReturn("1");
        when(request.getParameter("truongNhomId")).thenReturn("10");

        servlet.doPost(request, response);

        verify(service).themNhomKinhDoanh(eq("KD_MOI"), eq("Khối Kinh Doanh Mới"), eq("Mô tả"), isNull(), eq(1L), eq(10L));
        verify(session).setAttribute(eq("flashSuccess"), anyString());
        verify(response).sendRedirect("/crm/co-cau-to-chuc");
    }

    @Test
    @DisplayName("POST chuyen-nhom-nhan-vien: Chuyển nhóm nhân viên (AC2)")
    void testPostChuyenNhomNhanVien() throws Exception {
        NguoiDung director = taoUser(1L, "Giám đốc", VaiTroEnum.DIRECTOR);
        when(session.getAttribute("nguoiDung")).thenReturn(director);
        when(request.getParameter("action")).thenReturn("chuyen-nhom-nhan-vien");
        when(request.getParameter("nguoiDungId")).thenReturn("101");
        when(request.getParameter("nhomId")).thenReturn("5");

        servlet.doPost(request, response);

        verify(service).chuyenNhomNhanVien(101L, 5L);
        verify(session).setAttribute(eq("flashSuccess"), anyString());
        verify(response).sendRedirect("/crm/co-cau-to-chuc");
    }

    @Test
    @DisplayName("API: Lấy chi tiết nhóm và danh sách thành viên trả về JSON")
    void testApiChiTietNhom() throws Exception {
        NguoiDung admin = taoUser(1L, "Admin", VaiTroEnum.ADMIN);
        when(session.getAttribute("nguoiDung")).thenReturn(admin);
        when(request.getPathInfo()).thenReturn("/api/nhom");
        when(request.getParameter("id")).thenReturn("10");

        NhomKinhDoanh nkd = new NhomKinhDoanh();
        nkd.setId(10L);
        nkd.setMaNhom("KD_10");
        nkd.setTenNhom("Nhóm 10");
        when(service.timNhomTheoId(10L)).thenReturn(nkd);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = sw.toString();
        assertTrue(json.contains("\"maNhom\":\"KD_10\""));
        assertTrue(json.contains("\"tenNhom\":\"Nhóm 10\""));
    }
}
