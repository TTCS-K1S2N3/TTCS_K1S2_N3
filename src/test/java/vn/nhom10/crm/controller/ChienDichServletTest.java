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
import vn.nhom10.crm.dto.*;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.ChienDichService;
import vn.nhom10.crm.service.PhienService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử ChienDichServlet - Story S4-03")
class ChienDichServletTest {

    @Mock
    private ChienDichService chienDichService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;

    private ChienDichServlet servlet;
    private NguoiDung userMarketing;

    @BeforeEach
    void setUp() {
        servlet = new ChienDichServlet();
        servlet.setChienDichService(chienDichService);

        userMarketing = new NguoiDung();
        userMarketing.setId(10L);
        userMarketing.setHoTen("Marketing User");
        userMarketing.setTrangThai("HOAT_DONG");
        userMarketing.themVaiTro(VaiTroEnum.MARKETING);
    }

    @Test
    @DisplayName("doGet: Chưa đăng nhập chuyển hướng về /dang-nhap")
    void testDoGetChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("doGet: Người dùng không có quyền truy cập trả về 403 Forbidden")
    void testDoGetKhongCoQuyenTraVe403() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(false, false, false));
        when(request.getRequestDispatcher("/WEB-INF/views/common/403.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Hiển thị danh sách chiến dịch forward đúng view kèm request attributes")
    void testDoGetDanhSach() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(true, true, true));
        when(request.getServletPath()).thenReturn("/marketing/campaigns");
        when(request.getParameter("action")).thenReturn(null);

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("campaignRows", new ArrayList<ChienDichRowDTO>());
        mockData.put("pagination", Map.of("page", 1, "pageSize", 20, "totalItems", 0L, "totalPages", 0L));
        when(chienDichService.layDanhSachChienDich(1, 20)).thenReturn(mockData);

        when(request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-danh-sach.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("campaignRows"), any());
        verify(request).setAttribute(eq("pagination"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Hiển thị form tạo chiến dịch action=new")
    void testDoGetFormTao() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(true, true, true));
        when(request.getServletPath()).thenReturn("/marketing/campaign");
        when(request.getParameter("action")).thenReturn("new");
        when(chienDichService.layDanhSachKenh()).thenReturn(List.of(new ChannelOptionDTO("EVENT", "Sự kiện", true)));
        when(request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-form.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("campaignForm"), any());
        verify(request).setAttribute(eq("channelOptions"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Xem chi tiết chiến dịch tồn tại action=detail")
    void testDoGetChiTietTonTai() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(true, true, true));
        when(request.getServletPath()).thenReturn("/marketing/campaign");
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("101");

        ChienDichDTO dto = new ChienDichDTO();
        dto.setCampaignId(101L);
        dto.setName("Sự kiện ra mắt");
        ChienDichRowDTO row = new ChienDichRowDTO(dto, new ChienDichThongKeDTO(101L, 3, 2, new BigDecimal("45000000")));
        when(chienDichService.layChiTietChienDich(101L)).thenReturn(row);

        when(request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-chi-tiet.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("campaign"), eq(dto));
        verify(request).setAttribute(eq("metrics"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Xem chi tiết chiến dịch không tồn tại trả về 404")
    void testDoGetChiTietKhongTonTai() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(true, true, true));
        when(request.getServletPath()).thenReturn("/marketing/campaign");
        when(request.getParameter("action")).thenReturn("detail");
        when(request.getParameter("id")).thenReturn("999");
        when(chienDichService.layChiTietChienDich(999L)).thenReturn(null);

        when(request.getRequestDispatcher("/WEB-INF/views/common/404.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Khai báo chiến dịch thành công redirect PRG 303 sang /marketing/campaigns")
    void testDoPostThanhCongPRG() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(true, true, true));

        when(request.getParameter("name")).thenReturn("Chiến dịch thành công");
        when(request.getParameter("budgetAmount")).thenReturn("10000000");
        when(request.getParameter("startDate")).thenReturn("2026-10-01");
        when(request.getParameter("endDate")).thenReturn("2026-10-31");
        when(request.getParameter("channelCode")).thenReturn("EVENT");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession()).thenReturn(session);

        when(chienDichService.taoChienDich(any(ChienDichDTO.class), eq(10L), anyMap())).thenReturn(101L);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_SEE_OTHER);
        verify(response).sendRedirect("/crm/marketing/campaigns");
        verify(session).setAttribute(eq("flashMessage"), anyString());
    }

    @Test
    @DisplayName("doPost: Lỗi validation trả về 400 và forward lại form kèm fieldErrors")
    void testDoPostLoiValidation() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(userMarketing);
        when(chienDichService.layQuyenNguoiDung(userMarketing))
                .thenReturn(new ChienDichPermissionsDTO(true, true, true));

        when(request.getParameter("name")).thenReturn("Chiến dịch lỗi");
        when(request.getParameter("budgetAmount")).thenReturn("-500"); // Âm

        doAnswer(invocation -> {
            Map<String, String> errors = invocation.getArgument(2);
            errors.put("budgetAmount", "Ngân sách phải là số không âm.");
            return null;
        }).when(chienDichService).taoChienDich(any(ChienDichDTO.class), eq(10L), anyMap());

        when(chienDichService.layDanhSachKenh()).thenReturn(List.of());
        when(request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-form.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("fieldErrors"), anyMap());
        verify(dispatcher).forward(request, response);
    }
}
