package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.QuyTacPhanBoService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử HangChoPhanBoServlet - Story S4-06 AC3")
class HangChoPhanBoServletTest {

    private QuyTacPhanBoService service;
    private NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private NguoiDungDAO nguoiDungDAO;
    private HangChoPhanBoServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    private NguoiDung userTeamLead;
    private NguoiDung userSalesRep;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(QuyTacPhanBoService.class);
        nhomKinhDoanhDAO = mock(NhomKinhDoanhDAO.class);
        nguoiDungDAO = mock(NguoiDungDAO.class);

        servlet = new HangChoPhanBoServlet(service, nhomKinhDoanhDAO, nguoiDungDAO);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        userTeamLead = new NguoiDung();
        userTeamLead.setId(10L);
        userTeamLead.setHoTen("Trưởng nhóm 1");
        userTeamLead.setNhomKinhDoanhId(1);
        userTeamLead.themVaiTro(VaiTroEnum.TEAM_LEAD);

        userSalesRep = new NguoiDung();
        userSalesRep.setId(20L);
        userSalesRep.setHoTen("Nhân viên Sales");
        userSalesRep.themVaiTro(VaiTroEnum.SALES_REP);
    }

    @Test
    @DisplayName("Nhân viên Sales Rep không có quyền vào hàng chờ phân bổ -> 403 Forbidden")
    void testSalesRepKhongCoQuyen() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSalesRep);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
    }

    @Test
    @DisplayName("Trưởng nhóm truy cập hàng chờ -> Lấy danh sách thành viên thuộc nhóm mình")
    void testTeamLeadTruyCapHangCho() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userTeamLead);
        when(request.getServletPath()).thenReturn("/lead/hang-cho-phan-bo");

        servlet.doGet(request, response);

        verify(service).layDanhSachHangCho(anyInt(), anyInt());
        verify(service).demLeadHangCho();
        verify(nhomKinhDoanhDAO).layDsThanhVien(1L); // Nhóm ID 1 của Team Lead
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/hang-cho-phan-bo.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Giám đốc kinh doanh truy cập hàng chờ -> Thành công (200 OK)")
    void testDirectorTruyCapHangCho() throws Exception {
        NguoiDung director = new NguoiDung();
        director.setId(30L);
        director.setHoTen("Giám đốc KD");
        director.themVaiTro(VaiTroEnum.DIRECTOR);

        when(session.getAttribute("nguoiDung")).thenReturn(director);
        when(request.getServletPath()).thenReturn("/lead/hang-cho-phan-bo");

        servlet.doGet(request, response);

        verify(service).layDanhSachHangCho(anyInt(), anyInt());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/hang-cho-phan-bo.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Quản trị viên truy cập hàng chờ -> Thành công (200 OK)")
    void testAdminTruyCapHangCho() throws Exception {
        NguoiDung admin = new NguoiDung();
        admin.setId(1L);
        admin.setHoTen("Admin");
        admin.themVaiTro(VaiTroEnum.ADMIN);

        when(session.getAttribute("nguoiDung")).thenReturn(admin);
        when(request.getServletPath()).thenReturn("/lead/hang-cho-phan-bo");

        servlet.doGet(request, response);

        verify(service).layDanhSachHangCho(anyInt(), anyInt());
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/hang-cho-phan-bo.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Trưởng nhóm phân tay thành công cho nhân viên trong nhóm")
    void testPhanTayThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userTeamLead);
        when(request.getServletPath()).thenReturn("/lead/hang-cho-phan-bo/phan-tay");
        when(request.getParameter("lead_id")).thenReturn("100");
        when(request.getParameter("nhan_vien_id")).thenReturn("15");
        when(request.getParameter("ghi_chu")).thenReturn("Giao lead tiềm năng");

        when(service.phanTayTuHangCho(eq(100L), eq(15L), eq(userTeamLead), anyString())).thenReturn(true);

        servlet.doPost(request, response);

        verify(service).phanTayTuHangCho(eq(100L), eq(15L), eq(userTeamLead), eq("Giao lead tiềm năng"));
        verify(session).setAttribute(eq("successMessage"), anyString());
        verify(response).sendRedirect("/crm/lead/hang-cho-phan-bo");
    }

    @Test
    @DisplayName("Trưởng nhóm phân tay vi phạm Data Scope -> Báo lỗi SecurityException")
    void testPhanTayViPhamDataScope() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userTeamLead);
        when(request.getServletPath()).thenReturn("/lead/hang-cho-phan-bo/phan-tay");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("lead_id")).thenReturn("100");
        when(request.getParameter("nhan_vien_id")).thenReturn("99");

        doThrow(new SecurityException("Bạn chỉ có quyền phân bổ cho nhân viên thuộc nhóm của mình."))
                .when(service).phanTayTuHangCho(eq(100L), eq(99L), eq(userTeamLead), any());

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("\"success\":false"));
        assertTrue(responseWriter.toString().contains("thuộc nhóm của mình"));
    }
}
