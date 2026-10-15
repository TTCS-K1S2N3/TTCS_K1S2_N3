package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LeadService;
import vn.nhom10.crm.service.PermissionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LeadServlet - Story S4-02: AC1 (Tạo lead thủ công) & AC3 (Bắt buộc nguồn)")
class LeadServletTest {

    private LeadService leadService;
    private PermissionService permissionService;
    private LeadServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userMarketing;
    private NguoiDung userAccountant;

    @BeforeEach
    void setUp() {
        leadService = mock(LeadService.class);
        permissionService = mock(PermissionService.class);
        servlet = new LeadServlet(leadService, permissionService);

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

        userAccountant = new NguoiDung();
        userAccountant.setId(20L);
        userAccountant.setHoTen("Lê Kế Toán");
        userAccountant.themVaiTro(new VaiTro(VaiTroEnum.ACCOUNTANT));

        when(permissionService.coQuyen(eq(userMarketing), eq("LEAD"), any(MucQuyen.class))).thenReturn(true);
        when(permissionService.coQuyen(eq(userAccountant), eq("LEAD"), any(MucQuyen.class))).thenReturn(false);
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập -> Chuyển hướng /dang-nhap")
    void testChuaDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);
        when(request.getServletPath()).thenReturn("/lead");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
        verifyNoInteractions(leadService);
    }

    @Test
    @DisplayName("Phân quyền: Role không có quyền LEAD -> Trả về 403 Forbidden")
    void testKhongCoQuyenTruyCap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);
        when(request.getServletPath()).thenReturn("/lead/tao-moi");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /lead: Marketing xem danh sách Lead thành công")
    void testXemDanhSachLead() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead");
        when(leadService.layDanhSachLead(anyInt(), anyInt())).thenReturn(List.of(new Lead()));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsLead"), anyList());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /lead/tao-moi: Hiển thị form tạo lead kèm danh mục nguồn")
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
    @DisplayName("AC3: POST /lead/tao-moi thiếu nguồn -> Báo lỗi AC3 và giữ lại form")
    void testTaoLeadThieuNguon() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
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
    @DisplayName("AC1: POST /lead/tao-moi thiếu họ tên -> Báo lỗi và không lưu")
    void testTaoLeadThieuHoTen() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
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
    @DisplayName("AC1 & AC3: POST /lead/tao-moi hợp lệ -> Lưu thành công và chuyển hướng")
    void testTaoLeadThanhCongChuyenHuong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
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
    @DisplayName("AC1: POST /lead/tao-moi chọn Lưu và Tiếp tục -> Ở lại form kèm thông báo")
    void testTaoLeadLuuVaTiepTuc() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
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
}
