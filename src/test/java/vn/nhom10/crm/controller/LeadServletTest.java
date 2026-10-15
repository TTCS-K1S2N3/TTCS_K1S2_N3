package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.model.BoLocDaLuu;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.BoLocLeadService;
import vn.nhom10.crm.service.PermissionService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LeadServlet - Điều hướng danh sách và bộ lọc Lead (Story S4-09)")
class LeadServletTest {

    private BoLocLeadService boLocLeadService;
    private PermissionService permissionService;
    private LeadServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung user;

    @BeforeEach
    void setUp() {
        boLocLeadService = mock(BoLocLeadService.class);
        permissionService = mock(PermissionService.class);
        servlet = new LeadServlet(boLocLeadService, permissionService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        user = new NguoiDung();
        user.setId(101L);
        user.setHoTen("Nguyễn Văn An");
        user.setEmail("sales.an@crm.vn");

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet: Chưa đăng nhập chuyển hướng về /dang-nhap")
    void testDoGet_ChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doGet: Người dùng không có quyền READ module LEAD trả về 403 Forbidden")
    void testDoGet_KhongCoQuyen() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(permissionService.coQuyen(user, "LEAD", MucQuyen.READ)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Lọc 5 tiêu chí thành công và chuyển tiếp danh-sach.jsp (AC1 & AC2)")
    void testDoGet_Loc5TieuChiThanhCong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(permissionService.coQuyen(user, "LEAD", MucQuyen.READ)).thenReturn(true);

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

        when(boLocLeadService.timKiemLead(any(BoLocLeadDTO.class), eq(user), eq(100), eq(0)))
                .thenReturn(Collections.singletonList(leadMock));
        when(boLocLeadService.demTongSoLead(any(BoLocLeadDTO.class), eq(user))).thenReturn(1);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsLead"), anyList());
        verify(request).setAttribute(eq("boLocHienTai"), any(BoLocLeadDTO.class));
        verify(request).setAttribute(eq("tongBanGhi"), eq(1));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet: Áp dụng bộ lọc đã lưu qua savedFilterId (AC3, Contract C-03)")
    void testDoGet_ApDungSavedFilterId() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(permissionService.coQuyen(user, "LEAD", MucQuyen.READ)).thenReturn(true);

        when(request.getParameter("savedFilterId")).thenReturn("55");

        BoLocDaLuu bl = new BoLocDaLuu();
        bl.setId(55L);
        bl.setTenBoLoc("Lead Nóng Website");
        bl.setTieuChiJson("{\"phanLoai\":\"NONG\",\"nguonLeadId\":1}");

        when(boLocLeadService.timBoLocTheoId(55L, user)).thenReturn(bl);
        when(boLocLeadService.timKiemLead(any(BoLocLeadDTO.class), eq(user), eq(100), eq(0)))
                .thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(boLocLeadService).timBoLocTheoId(55L, user);
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Lưu bộ lọc cá nhân thành công chuyển hướng với savedFilterId (AC3, Contract C-02)")
    void testDoPost_LuuBoLocThanhCong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");

        when(request.getParameter("tenBoLoc")).thenReturn("Lead Nóng cần gọi");
        when(request.getParameter("phanLoai")).thenReturn("NONG");

        BoLocDaLuu savedMock = new BoLocDaLuu();
        savedMock.setId(99L);
        savedMock.setTenBoLoc("Lead Nóng cần gọi");

        when(boLocLeadService.luuBoLoc(eq(user), eq("Lead Nóng cần gọi"), any(BoLocLeadDTO.class), eq(false)))
                .thenReturn(savedMock);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/crm/lead?savedFilterId=99&thanhCong=luu_bo_loc");
    }

    @Test
    @DisplayName("doPost: Lưu bộ lọc thất bại khi tên rỗng")
    void testDoPost_LuuBoLoc_TenRong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(permissionService.coQuyen(user, "LEAD", MucQuyen.READ)).thenReturn(true);
        when(request.getServletPath()).thenReturn("/lead/bo-loc/luu");

        when(request.getParameter("tenBoLoc")).thenReturn("   ");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), anyString());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }
}
