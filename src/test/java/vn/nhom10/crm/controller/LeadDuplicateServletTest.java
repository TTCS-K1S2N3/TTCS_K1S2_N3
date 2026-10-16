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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.dao.GopLeadDAO;
import vn.nhom10.crm.dto.KetQuaGopLeadDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.SoSanhLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.GopLeadService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử LeadDuplicateServlet - Story S4-04 (Controller & Contract)")
class LeadDuplicateServletTest {

    @Mock
    private GopLeadService gopLeadService;

    @Mock
    private GopLeadDAO gopLeadDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private LeadDuplicateServlet servlet;
    private StringWriter responseWriter;
    private NguoiDungDTO userMarketing;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new LeadDuplicateServlet(gopLeadService, gopLeadDAO);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        userMarketing = new NguoiDungDTO(2L, "Marketing User", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);
    }

    @Test
    @DisplayName("Chưa đăng nhập -> Chuyển hướng tới trang đăng nhập")
    void testChuaDangNhapRedirectLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap");
    }

    @Test
    @DisplayName("Không có quyền truy cập -> Trả về HTTP 403 Forbidden")
    void testKhongCoQuyenTraVe403() throws Exception {
        NguoiDungDTO userCskh = new NguoiDungDTO(3L, "CSKH", "cs@crm.vn", VaiTroEnum.CUST_SUCCESS, 1L, "HN", PhamViDuLieu.CA_NHAN);
        when(session.getAttribute("nguoiDung")).thenReturn(userCskh);
        when(gopLeadService.coQuyenXem(any())).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET action=check với ID hợp lệ -> Chuyển tiếp tới duplicate-check.jsp (AC1)")
    void testGetCheckHopLe() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(gopLeadService.coQuyenXem(any())).thenReturn(true);
        when(request.getServletPath()).thenReturn("/lead/duplicate");
        when(request.getParameter("action")).thenReturn("check");
        when(request.getParameter("leadId")).thenReturn("101");

        Lead lead = new Lead();
        lead.setId(101L);
        lead.setHoTen("Lead Demo");
        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(lead);
        when(gopLeadService.layLeadUngVien(eq(101L), any())).thenReturn(Collections.emptyList());
        when(gopLeadService.layCustomerUngVien(eq(101L), any())).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("lead"), eq(lead));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/duplicate-check.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET action=preview với ID trùng nhau -> Trả về lỗi 400 Bad Request")
    void testGetPreviewTrungIdTraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(gopLeadService.coQuyenXem(any())).thenReturn(true);
        when(request.getServletPath()).thenReturn("/lead/duplicate");
        when(request.getParameter("action")).thenReturn("preview");
        when(request.getParameter("sourceLeadId")).thenReturn("101");
        when(request.getParameter("targetLeadId")).thenReturn("101");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Không thể gộp một Lead vào chính nó.");
    }

    @Test
    @DisplayName("GET action=preview hợp lệ -> Chuyển tiếp tới duplicate-preview.jsp (AC3)")
    void testGetPreviewHopLe() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(gopLeadService.coQuyenXem(any())).thenReturn(true);
        when(request.getServletPath()).thenReturn("/lead/duplicate");
        when(request.getParameter("action")).thenReturn("preview");
        when(request.getParameter("sourceLeadId")).thenReturn("102");
        when(request.getParameter("targetLeadId")).thenReturn("101");

        Lead src = new Lead();
        src.setId(102L);
        Lead dst = new Lead();
        dst.setId(101L);

        SoSanhLeadDTO soSanh = new SoSanhLeadDTO(src, dst);
        when(gopLeadService.layDuLieuSoSanh(eq(102L), eq(101L), any())).thenReturn(soSanh);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("soSanh"), eq(soSanh));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/duplicate-preview.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST action=linkCustomer thành công -> Redirect PRG (AC2)")
    void testPostLinkCustomerThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(gopLeadService.coQuyenThaoTac(any())).thenReturn(true);
        when(request.getParameter("action")).thenReturn("linkCustomer");
        when(request.getParameter("leadId")).thenReturn("101");
        when(request.getParameter("customerId")).thenReturn("801");

        when(gopLeadService.ganKhachHang(eq(101L), eq(801L), any(), any(), any())).thenReturn(true);

        servlet.doPost(request, response);

        verify(gopLeadService).ganKhachHang(eq(101L), eq(801L), any(), any(), any());
        verify(session).setAttribute(eq("flashMessage"), contains("Đã gắn Lead"));
        verify(response).sendRedirect("/crm/lead/duplicate?action=check&leadId=101");
    }

    @Test
    @DisplayName("POST action=merge thành công -> Redirect PRG kèm flash message (AC3)")
    void testPostMergeThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(gopLeadService.coQuyenThaoTac(any())).thenReturn(true);
        when(request.getParameter("action")).thenReturn("merge");
        when(request.getParameter("sourceLeadId")).thenReturn("102");
        when(request.getParameter("targetLeadId")).thenReturn("101");
        when(request.getParameter("lyDo")).thenReturn("Trùng số điện thoại");

        KetQuaGopLeadDTO kq = KetQuaGopLeadDTO.thanhCong(101L, 102L, 2, 1, 0, "Gộp thành công");
        when(gopLeadService.thucHienGopLead(eq(102L), eq(101L), anyString(), any(), any(), any())).thenReturn(kq);

        servlet.doPost(request, response);

        verify(gopLeadService).thucHienGopLead(eq(102L), eq(101L), eq("Trùng số điện thoại"), any(), any(), any());
        verify(session).setAttribute(eq("flashMessage"), eq("Gộp thành công"));
        verify(response).sendRedirect("/crm/lead/duplicate?action=check&leadId=101");
    }

    @Test
    @DisplayName("AJAX GET action=check trả về JSON theo đúng Mock A (AC1, AC2)")
    void testAjaxCheckTraVeJson() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(gopLeadService.coQuyenXem(any())).thenReturn(true);
        when(request.getServletPath()).thenReturn("/lead/duplicate");
        when(request.getParameter("action")).thenReturn("check");
        when(request.getParameter("leadId")).thenReturn("101");
        when(request.getParameter("format")).thenReturn("json");

        Lead lead = new Lead();
        lead.setId(101L);
        lead.setHoTen("Lead Demo A");
        lead.setEmail("contact01@example.test");
        lead.setSoDienThoai("0900000000");
        lead.setTenCongTy("Công ty ABC Demo");

        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(lead);
        when(gopLeadService.layLeadUngVien(eq(101L), any())).thenReturn(Collections.emptyList());
        when(gopLeadService.layCustomerUngVien(eq(101L), any())).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        String json = responseWriter.toString();
        assertTrue(json.contains("\"leadId\":101"));
        assertTrue(json.contains("\"displayName\":\"Lead Demo A\""));
        assertTrue(json.contains("\"leadCandidates\":[]"));
    }
}
