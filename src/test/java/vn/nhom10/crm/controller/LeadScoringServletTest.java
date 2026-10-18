package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.MenuService;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LeadScoringServlet - Cấu hình chấm điểm Lead (Story S4-05)")
class LeadScoringServletTest {

    private MenuService menuService;
    private vn.nhom10.crm.service.LeadScoringService leadScoringService;
    private LeadScoringServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    private NguoiDung userDirector;
    private NguoiDung userSalesRep;
    private NguoiDung userAccountant;

    @BeforeEach
    void setUp() throws Exception {
        menuService = mock(MenuService.class);
        leadScoringService = mock(vn.nhom10.crm.service.LeadScoringService.class);
        servlet = new LeadScoringServlet(menuService, leadScoringService);

        vn.nhom10.crm.dto.CauHinhChamDiemDTO dummyConfig = new vn.nhom10.crm.dto.CauHinhChamDiemDTO();
        dummyConfig.setPhienBan(1);
        dummyConfig.setNguongPhanLoai(new vn.nhom10.crm.dto.NguongPhanLoaiLeadDTO(70, 40));
        dummyConfig.getDanhSachTieuChi().add(new vn.nhom10.crm.dto.TieuChiChamDiemDTO("INDUSTRY_FIT", "R1", "Ngành", "Mô tả", "business", true, 25, 1));
        dummyConfig.getDanhSachTieuChi().add(new vn.nhom10.crm.dto.TieuChiChamDiemDTO("COMPANY_SIZE", "R2", "Quy mô", "Mô tả", "groups", true, 20, 2));
        dummyConfig.getDanhSachTieuChi().add(new vn.nhom10.crm.dto.TieuChiChamDiemDTO("SOURCE", "R3", "Nguồn", "Mô tả", "campaign", true, 15, 3));
        dummyConfig.getDanhSachTieuChi().add(new vn.nhom10.crm.dto.TieuChiChamDiemDTO("INTEREST_LEVEL", "R4", "Quan tâm", "Mô tả", "favorite", true, 40, 4));

        when(leadScoringService.layCauHinhHienTai()).thenReturn(dummyConfig);
        when(leadScoringService.luuCauHinh(any(), any(), any())).thenAnswer(inv -> {
            vn.nhom10.crm.dto.CauHinhChamDiemDTO cfg = inv.getArgument(0);
            cfg.setPhienBan(cfg.getPhienBan() + 1);
            return true;
        });

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Director
        userDirector = new NguoiDung(1L, "Trần Giám Đốc", "director@crm.vn");
        userDirector.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));

        // Sales Rep
        userSalesRep = new NguoiDung(2L, "Lê Nhân Viên", "sales@crm.vn");
        userSalesRep.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));

        // Accountant
        userAccountant = new NguoiDung(3L, "Nguyễn Kế Toán", "accountant@crm.vn");
        userAccountant.themVaiTro(new VaiTro(VaiTroEnum.ACCOUNTANT));

        when(menuService.kiemTraQuyenTruyCap(eq(userDirector), eq(ModuleHeThong.LEAD))).thenReturn(true);
        when(menuService.kiemTraQuyenTruyCap(eq(userSalesRep), eq(ModuleHeThong.LEAD))).thenReturn(true);
        when(menuService.kiemTraQuyenTruyCap(eq(userAccountant), eq(ModuleHeThong.LEAD))).thenReturn(false);

        ThongTinDieuHuongDTO navDTO = new ThongTinDieuHuongDTO();
        when(menuService.layThongTinDieuHuong(any(), anyString())).thenReturn(navDTO);
    }

    @Test
    @DisplayName("GET: Chưa đăng nhập -> Chuyển hướng sang trang đăng nhập")
    void testGet_ChuaDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("GET: Kế toán không có quyền module Lead -> 403 Forbidden")
    void testGet_KhongCoQuyenTruyCap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
    }

    @Test
    @DisplayName("GET: Giám đốc kinh doanh xem cấu hình -> canEdit = true và forward JSP")
    void testGet_DirectorThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("canEditScoringConfig"), eq(true));
        verify(request).setAttribute(eq("scoringConfig"), notNull());
        verify(request).setAttribute(eq("thresholds"), notNull());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/cau-hinh-cham-diem.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET: Sales Rep xem cấu hình -> canEdit = false (chỉ xem)")
    void testGet_SalesChỉXem() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSalesRep);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("canEditScoringConfig"), eq(false));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST: Giám đốc kinh doanh lưu cấu hình hợp lệ -> Thành công, chuyển hướng")
    void testPost_DirectorLuuThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getParameter("hotMin")).thenReturn("75");
        when(request.getParameter("warmMin")).thenReturn("45");
        when(request.getParameter("enabled_INDUSTRY_FIT")).thenReturn("true");
        when(request.getParameter("points_INDUSTRY_FIT")).thenReturn("30");
        when(request.getParameter("enabled_COMPANY_SIZE")).thenReturn("true");
        when(request.getParameter("points_COMPANY_SIZE")).thenReturn("20");
        when(request.getParameter("enabled_SOURCE")).thenReturn("true");
        when(request.getParameter("points_SOURCE")).thenReturn("15");
        when(request.getParameter("enabled_INTEREST_LEVEL")).thenReturn("true");
        when(request.getParameter("points_INTEREST_LEVEL")).thenReturn("35");

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("flashSuccess"), contains("thành công"));
        verify(response).sendRedirect("/crm/lead/cham-diem");
    }

    @Test
    @DisplayName("POST: Lỗi validation khi ngưỡng Nóng nhỏ hơn ngưỡng Ấm (hotMin <= warmMin)")
    void testPost_LoiNguongDiem() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getParameter("hotMin")).thenReturn("30");
        when(request.getParameter("warmMin")).thenReturn("50");
        when(request.getParameter("enabled_INDUSTRY_FIT")).thenReturn("true");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("không hợp lệ"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST: Sales Rep không có quyền chỉnh sửa cấu hình -> 403 Forbidden")
    void testPost_SalesKhongDuocSua() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSalesRep);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
    }

    @Test
    @DisplayName("POST: Yêu cầu AJAX thành công -> Trả về JSON đúng contract")
    void testPost_AjaxThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("hotMin")).thenReturn("80");
        when(request.getParameter("warmMin")).thenReturn("50");
        when(request.getParameter("enabled_INDUSTRY_FIT")).thenReturn("true");

        servlet.doPost(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String output = responseWriter.toString();
        assertTrue(output.contains("\"success\":true"));
        assertTrue(output.contains("thành công"));
    }

    @Test
    @DisplayName("POST & BUG-06: Chặn lưu khi Ấm = 70, Nóng = 40 (hotMin <= warmMin)")
    void testPost_Warm70_Hot40_ChanLuu() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getParameter("hotMin")).thenReturn("40");
        when(request.getParameter("warmMin")).thenReturn("70");
        when(request.getParameter("enabled_INDUSTRY_FIT")).thenReturn("true");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("không hợp lệ"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST & BUG-06: Chặn lưu khi Ấm = 70, Nóng = 70 (hotMin == warmMin)")
    void testPost_Warm70_Hot70_ChanLuu() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getParameter("hotMin")).thenReturn("70");
        when(request.getParameter("warmMin")).thenReturn("70");
        when(request.getParameter("enabled_INDUSTRY_FIT")).thenReturn("true");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST & BUG-05: Lưu cấu hình thất bại từ Service -> Trả về 500 và không báo thành công")
    void testPost_LuuThatBai_KhongBaoThanhCong() throws Exception {
        vn.nhom10.crm.service.LeadScoringService mockService = mock(vn.nhom10.crm.service.LeadScoringService.class);
        LeadScoringServlet customServlet = new LeadScoringServlet(menuService, mockService);

        vn.nhom10.crm.dto.CauHinhChamDiemDTO dummyConfig = new vn.nhom10.crm.dto.CauHinhChamDiemDTO();
        dummyConfig.setPhienBan(1);
        dummyConfig.getDanhSachTieuChi().add(new vn.nhom10.crm.dto.TieuChiChamDiemDTO("INDUSTRY_FIT", "R1", "Ngành", "Mô tả", "business", true, 25, 1));
        when(mockService.layCauHinhHienTai()).thenReturn(dummyConfig);
        when(mockService.luuCauHinh(any(), any(), any())).thenReturn(false); // Lưu thất bại

        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getParameter("hotMin")).thenReturn("80");
        when(request.getParameter("warmMin")).thenReturn("50");
        when(request.getParameter("enabled_INDUSTRY_FIT")).thenReturn("true");

        customServlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(session, never()).setAttribute(eq("flashSuccess"), any());
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Có lỗi xảy ra"));
    }
}
