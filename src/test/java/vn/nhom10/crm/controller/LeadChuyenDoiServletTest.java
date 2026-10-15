package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.nhom10.crm.dto.ChuyenDoiLeadPreviewDTO;
import vn.nhom10.crm.dto.KetQuaChuyenDoiLeadDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.LeadChuyenDoiService;
import vn.nhom10.crm.service.PhienService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LeadChuyenDoiServlet - Story S4-08")
class LeadChuyenDoiServletTest {

    @Mock
    private LeadChuyenDoiService leadChuyenDoiService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private LeadChuyenDoiServlet servlet;
    private NguoiDung user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LeadChuyenDoiServlet(leadChuyenDoiService);

        user = new NguoiDung(701L, "Nguyễn Văn Sales", "sales701@crm.vn");
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(user);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher("/WEB-INF/views/lead/chuyen-doi.jsp")).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("GET preview không có session -> redirect về trang đăng nhập")
    void testGetPreviewKhongCoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap");
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("GET preview thiếu id -> trả về 400 Bad Request")
    void testGetPreviewThieuId() throws Exception {
        when(request.getParameter("id")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("GET preview không tìm thấy Lead -> trả về 404 Not Found")
    void testGetPreviewKhongTimThayLead() throws Exception {
        when(request.getParameter("id")).thenReturn("9999");
        ChuyenDoiLeadPreviewDTO preview = new ChuyenDoiLeadPreviewDTO();
        when(leadChuyenDoiService.layPreviewChuyenDoi(eq(9999L), any(NguoiDung.class))).thenReturn(preview);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }

    @Test
    @DisplayName("GET preview ngoài scope hoặc không có quyền -> trả về 403 Forbidden")
    void testGetPreviewKhongCoQuyen() throws Exception {
        when(request.getParameter("id")).thenReturn("123");
        ChuyenDoiLeadPreviewDTO preview = new ChuyenDoiLeadPreviewDTO();
        ChuyenDoiLeadPreviewDTO.LeadChuyenDoiViewDTO leadView = new ChuyenDoiLeadPreviewDTO.LeadChuyenDoiViewDTO();
        leadView.setLeadId(123L);
        preview.setLeadChuyenDoi(leadView);
        preview.setQuyenChuyenDoi(false);

        when(leadChuyenDoiService.layPreviewChuyenDoi(eq(123L), any(NguoiDung.class))).thenReturn(preview);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("GET preview hợp lệ -> forward đến chuyen-doi.jsp kèm request attributes và CSRF token")
    void testGetPreviewHopLe() throws Exception {
        when(request.getParameter("id")).thenReturn("100");
        ChuyenDoiLeadPreviewDTO preview = new ChuyenDoiLeadPreviewDTO();
        ChuyenDoiLeadPreviewDTO.LeadChuyenDoiViewDTO leadView = new ChuyenDoiLeadPreviewDTO.LeadChuyenDoiViewDTO();
        leadView.setLeadId(100L);
        leadView.setTrangThaiLead("DANG_CHAM_SOC");
        preview.setLeadChuyenDoi(leadView);
        preview.setQuyenChuyenDoi(true);

        when(leadChuyenDoiService.layPreviewChuyenDoi(eq(100L), any(NguoiDung.class))).thenReturn(preview);
        when(session.getAttribute("csrfToken")).thenReturn("TEST_CSRF_TOKEN");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("preview"), eq(preview));
        verify(request).setAttribute(eq("csrfToken"), eq("TEST_CSRF_TOKEN"));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/chuyen-doi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST chuyển đổi không có session -> trả về 401 Unauthorized")
    void testPostKhongCoSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
    }

    @Test
    @DisplayName("POST chuyển đổi thiếu CSRF token trong session -> trả về 403 Forbidden")
    void testPostThieuCsrfTokenTrongSession() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn(null);
        when(request.getParameter("csrfToken")).thenReturn("SOME_TOKEN");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(leadChuyenDoiService, never()).chuyenDoiLead(anyLong(), any());
    }

    @Test
    @DisplayName("POST chuyển đổi rỗng CSRF token trong session -> trả về 403 Forbidden")
    void testPostRongCsrfTokenTrongSession() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn("   ");
        when(request.getParameter("csrfToken")).thenReturn("SOME_TOKEN");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(leadChuyenDoiService, never()).chuyenDoiLead(anyLong(), any());
    }

    @Test
    @DisplayName("POST chuyển đổi thiếu CSRF token trong form -> trả về 403 Forbidden")
    void testPostThieuCsrfTokenTrongForm() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn("VALID_TOKEN");
        when(request.getParameter("csrfToken")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(leadChuyenDoiService, never()).chuyenDoiLead(anyLong(), any());
    }

    @Test
    @DisplayName("POST chuyển đổi sai CSRF token -> trả về 403 Forbidden")
    void testPostSaiCsrfToken() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn("VALID_TOKEN");
        when(request.getParameter("csrfToken")).thenReturn("INVALID_TOKEN");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(leadChuyenDoiService, never()).chuyenDoiLead(anyLong(), any());
    }

    @Test
    @DisplayName("POST chuyển đổi thành công -> đặt status 303 và redirect sang Customer 360")
    void testPostChuyenDoiThanhCong() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn("VALID_TOKEN");
        when(request.getParameter("csrfToken")).thenReturn("VALID_TOKEN");

        KetQuaChuyenDoiLeadDTO kq = KetQuaChuyenDoiLeadDTO.thanhCong(100L, 3001L, 4001L, 5001L, 3, LocalDateTime.now());
        when(leadChuyenDoiService.chuyenDoiLead(eq(100L), any(NguoiDung.class))).thenReturn(kq);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("flashSuccess"), eq("Chuyển đổi lead thành công"));
        verify(response).setStatus(HttpServletResponse.SC_SEE_OTHER);
        verify(response).sendRedirect("/crm/khach-hang/chi-tiet?id=3001");
    }

    @Test
    @DisplayName("POST chuyển đổi lỗi 422 thiếu dữ liệu nguồn -> đặt status 422 và forward về preview")
    void testPostChuyenDoiLoi422() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn("VALID_TOKEN");
        when(request.getParameter("csrfToken")).thenReturn("VALID_TOKEN");

        Map<String, String> loiTruong = new HashMap<>();
        loiTruong.put("tenCongTy", "Cần bổ sung tên doanh nghiệp");
        KetQuaChuyenDoiLeadDTO kq = KetQuaChuyenDoiLeadDTO.thatBai("LEAD_SOURCE_INVALID", "Thông tin Lead chưa đủ", loiTruong);
        when(leadChuyenDoiService.chuyenDoiLead(eq(100L), any(NguoiDung.class))).thenReturn(kq);

        ChuyenDoiLeadPreviewDTO preview = new ChuyenDoiLeadPreviewDTO();
        when(leadChuyenDoiService.layPreviewChuyenDoi(eq(100L), any(NguoiDung.class))).thenReturn(preview);

        servlet.doPost(request, response);

        verify(response).setStatus(422);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST chuyển đổi lỗi 409 conflict doanh nghiệp đã tồn tại -> đặt status 409 và forward về preview")
    void testPostChuyenDoiLoi409() throws Exception {
        when(request.getParameter("leadId")).thenReturn("100");
        when(session.getAttribute("csrfToken")).thenReturn("VALID_TOKEN");
        when(request.getParameter("csrfToken")).thenReturn("VALID_TOKEN");

        KetQuaChuyenDoiLeadDTO kq = KetQuaChuyenDoiLeadDTO.thatBai("CUSTOMER_ALREADY_EXISTS", "Doanh nghiệp đã tồn tại");
        when(leadChuyenDoiService.chuyenDoiLead(eq(100L), any(NguoiDung.class))).thenReturn(kq);

        ChuyenDoiLeadPreviewDTO preview = new ChuyenDoiLeadPreviewDTO();
        when(leadChuyenDoiService.layPreviewChuyenDoi(eq(100L), any(NguoiDung.class))).thenReturn(preview);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        verify(dispatcher).forward(request, response);
    }
}
