package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.*;
import vn.nhom10.crm.service.BoLocLeadService;
import vn.nhom10.crm.service.LeadService;
import vn.nhom10.crm.service.PermissionService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LeadServlet - Hợp nhất Story S4-02 (Tạo Lead thủ công) và Story S4-09 (Lọc & Lưu bộ lọc Lead)")
class LeadServletTest {

    private LeadService leadService;
    private BoLocLeadService boLocLeadService;
    private PermissionService permissionService;
    private LeadServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userMarketing;
    private NguoiDung userSales;
    private NguoiDung userAccountant;

    @BeforeEach
    void setUp() {
        leadService = mock(LeadService.class);
        boLocLeadService = mock(BoLocLeadService.class);
        permissionService = mock(PermissionService.class);
        servlet = new LeadServlet(leadService, boLocLeadService, permissionService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);

        userMarketing = new NguoiDung();
        userMarketing.setId(10L);
        userMarketing.setHoTen("Nguyễn Marketing");
        userMarketing.themVaiTro(new VaiTro(VaiTroEnum.MARKETING));

        userSales = new NguoiDung();
        userSales.setId(101L);
        userSales.setHoTen("Nguyễn Sales Rep");
        userSales.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));

        userAccountant = new NguoiDung();
        userAccountant.setId(20L);
        userAccountant.setHoTen("Lê Kế Toán");
        userAccountant.themVaiTro(new VaiTro(VaiTroEnum.ACCOUNTANT));

        // Default permissions
        when(permissionService.coQuyen(eq(userMarketing), eq("LEAD"), any(MucQuyen.class))).thenReturn(true);
        when(permissionService.coQuyen(eq(userSales), eq("LEAD"), eq(MucQuyen.READ))).thenReturn(true);
        when(permissionService.coQuyen(eq(userSales), eq("LEAD"), eq(MucQuyen.WRITE))).thenReturn(false);
        when(permissionService.coQuyen(eq(userAccountant), eq("LEAD"), any(MucQuyen.class))).thenReturn(false);
    }

    // =========================================================================
    // 1. KIỂM THỬ XÁC THỰC VÀ PHÂN QUYỀN (S4-02 & S4-09)
    // =========================================================================

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập -> Chuyển hướng /dang-nhap")
    void testChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getServletPath()).thenReturn("/lead");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
        verify(dispatcher, never()).forward(any(), any());
        verifyNoInteractions(leadService);
        verifyNoInteractions(boLocLeadService);
    }

    @Test
    @DisplayName("Phân quyền: Role không có quyền READ module LEAD -> Trả về 403 Forbidden")
    void testKhongCoQuyenTruyCap_Read() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);
        when(request.getServletPath()).thenReturn("/lead");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Phân quyền: Role không có quyền WRITE khi vào /lead/tao-moi -> Trả về 403 Forbidden")
    void testKhongCoQuyenTruyCap_Write() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    // =========================================================================
    // 2. KIỂM THỬ TẠO LEAD THỦ CÔNG (STORY S4-02)
    // =========================================================================

    @Test
    @DisplayName("GET /lead/tao-moi: Hiển thị form tạo lead kèm danh mục nguồn (AC1 & AC3)")
    void testHienThiFormTaoLead() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");
        when(leadService.layDanhSachNguonLead()).thenReturn(List.of(new MucDanhMucDTO()));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsNguonLead"), anyList());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /lead/tao-moi: Thiếu nguồn lead -> Báo lỗi AC3 và giữ lại form")
    void testTaoLeadThieuNguon() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");
        when(request.getParameter("ho_ten")).thenReturn("Nguyễn Văn An");
        when(request.getParameter("nguon_lead_id")).thenReturn(""); // THIẾU NGUỒN

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errors"), argThat(arg -> {
            var errors = (java.util.Map<?, ?>) arg;
            return errors.containsKey("nguonLeadId");
        }));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp");
        verify(dispatcher).forward(request, response);
        verify(leadService, never()).taoLeadThuCong(any(), any());
    }

    @Test
    @DisplayName("POST /lead/tao-moi: Thiếu họ tên -> Báo lỗi và không lưu")
    void testTaoLeadThieuHoTen() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");
        when(request.getParameter("ho_ten")).thenReturn("");
        when(request.getParameter("nguon_lead_id")).thenReturn("1");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errors"), argThat(arg -> {
            var errors = (java.util.Map<?, ?>) arg;
            return errors.containsKey("hoTen");
        }));
        verify(dispatcher).forward(request, response);
        verify(leadService, never()).taoLeadThuCong(any(), any());
    }

    @Test
    @DisplayName("POST /lead/tao-moi: Hợp lệ -> Lưu thành công và chuyển hướng /lead")
    void testTaoLeadThanhCongChuyenHuong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");
        when(request.getParameter("ho_ten")).thenReturn("Nguyễn Văn An");
        when(request.getParameter("nguon_lead_id")).thenReturn("1");
        when(request.getParameter("email")).thenReturn("an@test.vn");
        when(request.getParameter("so_dien_thoai")).thenReturn("0912345678");
        when(request.getParameter("tiepTuc")).thenReturn("false");

        Lead leadDaTao = new Lead();
        leadDaTao.setId(99L);
        leadDaTao.setMaLead("LEAD-261014-ABCDE");
        when(leadService.taoLeadThuCong(any(Lead.class), eq(10L))).thenReturn(leadDaTao);

        servlet.doPost(request, response);

        verify(leadService).taoLeadThuCong(any(Lead.class), eq(10L));
        verify(response).sendRedirect(contains("/crm/lead?thanhCong=tao-moi&maLead=LEAD-261014-ABCDE"));
    }

    @Test
    @DisplayName("POST /lead/tao-moi: Chọn Lưu và Tiếp tục -> Ở lại form kèm thông báo")
    void testTaoLeadLuuVaTiepTuc() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");
        when(request.getParameter("ho_ten")).thenReturn("Nguyễn Văn An");
        when(request.getParameter("nguon_lead_id")).thenReturn("1");
        when(request.getParameter("tiepTuc")).thenReturn("true");

        Lead leadDaTao = new Lead();
        leadDaTao.setId(101L);
        leadDaTao.setMaLead("LEAD-261014-XYZ12");
        when(leadService.taoLeadThuCong(any(Lead.class), eq(10L))).thenReturn(leadDaTao);

        servlet.doPost(request, response);

        verify(leadService).taoLeadThuCong(any(Lead.class), eq(10L));
        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("LEAD-261014-XYZ12"));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp");
        verify(dispatcher).forward(request, response);
    }

    // =========================================================================
    // 3. KIỂM THỬ LỌC 5 TIÊU CHÍ VÀ DATA SCOPE (STORY S4-09)
    // =========================================================================

    @Test
    @DisplayName("GET /lead: Lọc 5 tiêu chí thành công và chuyển tiếp danh-sach.jsp (AC1 & AC2)")
    void testXemDanhSachLead_Loc5TieuChi() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead");

        when(request.getParameter("trangThai")).thenReturn("MOI");
        when(request.getParameter("nguonLeadId")).thenReturn("2");
        when(request.getParameter("phanLoai")).thenReturn("NONG");
        when(request.getParameter("nguoiPhuTrachId")).thenReturn("101");
        when(request.getParameter("tuNgay")).thenReturn("2026-10-01");
        when(request.getParameter("denNgay")).thenReturn("2026-10-15");

        Lead leadMock = new Lead();
        leadMock.setId(1L);
        leadMock.setMaLead("LEAD-001");
        leadMock.setQuaSla(true);

        when(boLocLeadService.timKiemLead(any(BoLocLeadDTO.class), eq(userSales), eq(100), eq(0)))
                .thenReturn(Collections.singletonList(leadMock));
        when(boLocLeadService.demTongSoLead(any(BoLocLeadDTO.class), eq(userSales))).thenReturn(1);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsLead"), anyList());
        verify(request).setAttribute(eq("boLocHienTai"), any(BoLocLeadDTO.class));
        verify(request).setAttribute(eq("tongBanGhi"), eq(1));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /lead: Kết hợp nhiều tiêu chí AND (trạng thái + phân loại)")
    void testLocKetHopNhieuTieuChiAnd() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead");

        when(request.getParameter("trangThai")).thenReturn("DANG_CHAM_SOC");
        when(request.getParameter("phanLoai")).thenReturn("AM");

        servlet.doGet(request, response);

        ArgumentCaptor<BoLocLeadDTO> captor = ArgumentCaptor.forClass(BoLocLeadDTO.class);
        verify(boLocLeadService).timKiemLead(captor.capture(), eq(userSales), eq(100), eq(0));

        BoLocLeadDTO captured = captor.getValue();
        assertEquals("DANG_CHAM_SOC", captured.getTrangThai());
        assertEquals("AM", captured.getPhanLoai());
    }

    @Test
    @DisplayName("GET /lead: Ưu tiên hiển thị Lead quá hạn SLA lên trước (AC2)")
    void testUuTienLeadQuaHanSla() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead");

        Lead overdueLead = new Lead();
        overdueLead.setId(1L);
        overdueLead.setQuaSla(true);

        Lead normalLead = new Lead();
        normalLead.setId(2L);
        normalLead.setQuaSla(false);

        when(boLocLeadService.timKiemLead(any(BoLocLeadDTO.class), eq(userSales), eq(100), eq(0)))
                .thenReturn(List.of(overdueLead, normalLead));

        servlet.doGet(request, response);

        verify(request).setAttribute("dsLead", List.of(overdueLead, normalLead));
        verify(dispatcher).forward(request, response);
    }

    // =========================================================================
    // 4. KIỂM THỬ BỘ LỌC ĐÃ LƯU & BẢO MẬT (STORY S4-09: AC3)
    // =========================================================================

    @Test
    @DisplayName("GET /lead: Áp dụng bộ lọc đã lưu qua savedFilterId (AC3, Contract C-03)")
    void testApDungSavedFilterId() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead");
        when(request.getParameter("savedFilterId")).thenReturn("55");

        BoLocDaLuu bl = new BoLocDaLuu();
        bl.setId(55L);
        bl.setTenBoLoc("Lead Nóng Website");
        bl.setTieuChiJson("{\"phanLoai\":\"NONG\",\"nguonLeadId\":1}");

        when(boLocLeadService.timBoLocTheoId(55L, userSales)).thenReturn(bl);
        when(boLocLeadService.timKiemLead(any(BoLocLeadDTO.class), eq(userSales), eq(100), eq(0)))
                .thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(boLocLeadService).timBoLocTheoId(55L, userSales);
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Bảo mật: Không xem được bộ lọc của tài khoản khác (ID không khớp trả về lỗi)")
    void testKhongXemDuocBoLocCuaTaiKhoanKhac() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead");
        when(request.getParameter("savedFilterId")).thenReturn("999");

        // Trả về null vì bộ lọc 999 không thuộc userSales
        when(boLocLeadService.timBoLocTheoId(999L, userSales)).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), contains("không tồn tại hoặc bạn không có quyền truy cập"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /lead/bo-loc/luu: Lưu bộ lọc cá nhân thành công với CSRF token hợp lệ")
    void testLuuBoLoc_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(session.getAttribute("csrfToken")).thenReturn("token-12345");
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");
        when(request.getParameter("csrfToken")).thenReturn("token-12345");

        when(request.getParameter("tenBoLoc")).thenReturn("Lead Nóng cần gọi");
        when(request.getParameter("phanLoai")).thenReturn("NONG");

        BoLocDaLuu savedMock = new BoLocDaLuu();
        savedMock.setId(99L);
        savedMock.setTenBoLoc("Lead Nóng cần gọi");

        when(boLocLeadService.luuBoLoc(eq(userSales), eq("Lead Nóng cần gọi"), any(BoLocLeadDTO.class), eq(false)))
                .thenReturn(savedMock);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/crm/lead?savedFilterId=99&thanhCong=luu_bo_loc");
    }

    @Test
    @DisplayName("POST /lead/bo-loc/luu: Lỗi khi tên bộ lọc rỗng")
    void testLuuBoLoc_TenRong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");
        when(request.getParameter("tenBoLoc")).thenReturn("   ");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), contains("Vui lòng nhập tên"));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /lead/bo-loc/luu: Lỗi khi tên bộ lọc vượt quá 80 ký tự")
    void testLuuBoLoc_TenQuaDai() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");
        when(request.getParameter("tenBoLoc")).thenReturn("A".repeat(85));

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), contains("tối đa 80 ký tự"));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /lead/bo-loc/luu: Người dùng không có quyền READ bị chặn 403")
    void testLuuBoLoc_KhongCoQuyenRead() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /lead/bo-loc/luu: Sai CSRF token -> Trả về 403 Forbidden")
    void testLuuBoLoc_CsrfTokenSai() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(session.getAttribute("csrfToken")).thenReturn("token-hop-le");
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");
        when(request.getParameter("csrfToken")).thenReturn("token-gia-mao");

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ hoặc bị thiếu");
        verify(boLocLeadService, never()).luuBoLoc(any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("POST /lead/bo-loc/luu: Thiếu CSRF token -> Trả về 403 Forbidden")
    void testLuuBoLoc_CsrfTokenThieu() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSales);
        when(session.getAttribute("csrfToken")).thenReturn("token-hop-le");
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");
        when(request.getParameter("csrfToken")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ hoặc bị thiếu");
        verify(boLocLeadService, never()).luuBoLoc(any(), any(), any(), anyBoolean());
    }

    // =========================================================================
    // 5. KIỂM THỬ ĐẢM BẢO KHÔNG LÀM HỎNG ROUTE KHÁC
    // =========================================================================

    @Test
    @DisplayName("URL Mapping: Không dùng wildcard /lead/* để tránh chặn nhầm các route khác")
    void testKhongLamHongCacRouteKhac() {
        jakarta.servlet.annotation.WebServlet annotation =
                LeadServlet.class.getAnnotation(jakarta.servlet.annotation.WebServlet.class);
        assertNotNull(annotation, "@WebServlet phải tồn tại");

        String[] patterns = annotation.urlPatterns();
        for (String pattern : patterns) {
            assertNotEquals("/lead/*", pattern, "Không được dùng /lead/* làm che lấp các route /lead/chi-tiet, /lead/chuyen-doi");
        }
        assertTrue(List.of(patterns).contains("/lead"));
        assertTrue(List.of(patterns).contains("/lead/tao-moi"));
        assertTrue(List.of(patterns).contains("/lead/bo-loc/luu"));
    }
}
