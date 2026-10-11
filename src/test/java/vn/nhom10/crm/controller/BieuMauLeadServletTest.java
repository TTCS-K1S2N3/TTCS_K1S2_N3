package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dto.MaNhungDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.BieuMauLeadService;
import vn.nhom10.crm.service.MenuService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử BieuMauLeadServlet - Quản lý biểu mẫu cho Marketing (Story S4-01)")
class BieuMauLeadServletTest {

    private BieuMauLeadService service;
    private DanhMucBanHangDAO danhMucDAO;
    private MenuService menuService;
    private BieuMauLeadServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    private NguoiDung userMarketing;
    private NguoiDung userAccountant;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(BieuMauLeadService.class);
        danhMucDAO = mock(DanhMucBanHangDAO.class);
        menuService = mock(MenuService.class);
        servlet = new BieuMauLeadServlet(service, danhMucDAO, menuService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        userMarketing = new NguoiDung();
        userMarketing.setId(5L);
        userMarketing.setHoTen("Phạm Marketing");
        userMarketing.themVaiTro(new VaiTro(VaiTroEnum.MARKETING));

        userAccountant = new NguoiDung();
        userAccountant.setId(7L);
        userAccountant.setHoTen("Nguyễn Kế Toán");
        userAccountant.themVaiTro(new VaiTro(VaiTroEnum.ACCOUNTANT));

        when(menuService.kiemTraQuyenTruyCap(eq(userMarketing), eq(ModuleHeThong.LEAD))).thenReturn(true);
        when(menuService.kiemTraQuyenTruyCap(eq(userAccountant), eq(ModuleHeThong.LEAD))).thenReturn(false);
    }

    @Test
    @DisplayName("Phân quyền: Người dùng chưa đăng nhập -> Chuyển hướng /dang-nhap")
    void testChuaDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);
        when(request.getServletPath()).thenReturn("/bieu-mau");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap");
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Phân quyền: Kế toán không có quyền LEAD -> Trả về 403 Forbidden")
    void testKhongCoQuyenTruyCap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);
        when(request.getServletPath()).thenReturn("/bieu-mau");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /bieu-mau: Marketing có quyền -> Forward tới bieu-mau-danh-sach.jsp")
    void testHienThiDanhSach() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/bieu-mau");

        BieuMauLead bm = new BieuMauLead("BM_01", "Biểu mẫu 1", "token_1", 1L);
        when(service.layDanhSachBieuMau(null)).thenReturn(List.of(bm));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsBieuMau"), anyList());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /bieu-mau/tao: Nạp danh mục Nguồn Lead và hiển thị form tạo")
    void testHienThiTrangTao() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/bieu-mau/tao");

        MucDanhMucDTO nguon = new MucDanhMucDTO();
        nguon.setId(1L);
        nguon.setLoaiDanhMuc(LoaiDanhMuc.NGUON_LEAD);
        nguon.setMaMuc("WEB");
        nguon.setTenMuc("Website");
        when(danhMucDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true)).thenReturn(List.of(nguon));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsNguonLead"), anyList());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-form.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /bieu-mau/tao: Tạo biểu mẫu thành công và chuyển hướng danh sách")
    void testXuLyTaoBieuMauThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/bieu-mau/tao");

        when(request.getParameter("ma_bieu_mau")).thenReturn("BM_NEW");
        when(request.getParameter("ten_bieu_mau")).thenReturn("Biểu mẫu mới");
        when(request.getParameter("nguon_lead_id")).thenReturn("1");
        when(request.getParameter("gioi_han_gui_moi_gio")).thenReturn("10");
        when(request.getParameter("bat_honeypot")).thenReturn("1");

        servlet.doPost(request, response);

        verify(service).taoBieuMau(any(BieuMauLead.class), eq(5L));
        verify(session).setAttribute(eq("thongBaoThanhCong"), anyString());
        verify(response).sendRedirect("/crm/bieu-mau");
    }

    @Test
    @DisplayName("AC1: GET /bieu-mau/ma-nhung: Trả về JSON mã nhúng iframe & script")
    void testLayMaNhungJson() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/bieu-mau/ma-nhung");
        when(request.getParameter("id")).thenReturn("12");

        MaNhungDTO dto = new MaNhungDTO("BM_12", "Form 12", "token_12",
                "<iframe src=\"http://localhost:8080/crm/bieu-mau/nhung?token=token_12\"></iframe>",
                "<script src=\"http://localhost:8080/crm/bieu-mau/embed.js?token=token_12\"></script>",
                "http://localhost:8080/crm/bieu-mau/nhung?token=token_12");

        when(service.sinhMaNhung(eq(12L), anyString())).thenReturn(dto);

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = responseWriter.toString();
        assertTrue(json.contains("\"thanhCong\":true"));
        assertTrue(json.contains("token_12"));
        assertTrue(json.contains("iframe"));
    }

    @Test
    @DisplayName("Điều hướng: GET /lead được chuyển hướng an toàn về /bieu-mau")
    void testDieuHuongTuLeadSangBieuMau() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/bieu-mau");
        verifyNoInteractions(service);
    }
}
