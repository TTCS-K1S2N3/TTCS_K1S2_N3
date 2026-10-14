package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.KhuVucDiaLyDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dto.KetQuaPhanBoDTO;
import vn.nhom10.crm.dto.PhanBoLeadRequestDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.QuyTacPhanBoLead;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LeadAssignmentBackgroundWorker;
import vn.nhom10.crm.service.QuyTacPhanBoService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử QuyTacPhanBoServlet - Story S4-06")
class QuyTacPhanBoServletTest {

    private QuyTacPhanBoService service;
    private KhuVucDiaLyDAO khuVucDAO;
    private DanhMucBanHangDAO danhMucDAO;
    private NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private LeadAssignmentBackgroundWorker worker;
    private QuyTacPhanBoServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    private NguoiDung userDirector;
    private NguoiDung userSalesRep;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(QuyTacPhanBoService.class);
        khuVucDAO = mock(KhuVucDiaLyDAO.class);
        danhMucDAO = mock(DanhMucBanHangDAO.class);
        nhomKinhDoanhDAO = mock(NhomKinhDoanhDAO.class);
        worker = mock(LeadAssignmentBackgroundWorker.class);

        servlet = new QuyTacPhanBoServlet(service, khuVucDAO, danhMucDAO, nhomKinhDoanhDAO, worker);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        userDirector = new NguoiDung();
        userDirector.setId(1L);
        userDirector.setHoTen("Giám đốc kinh doanh");
        userDirector.themVaiTro(VaiTroEnum.DIRECTOR);

        userSalesRep = new NguoiDung();
        userSalesRep.setId(2L);
        userSalesRep.setHoTen("Nhân viên Sales");
        userSalesRep.themVaiTro(VaiTroEnum.SALES_REP);
    }

    @Test
    @DisplayName("Chưa đăng nhập -> Chuyển hướng về trang đăng nhập")
    void testChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap");
    }

    @Test
    @DisplayName("Nhân viên Sales Rep truy cập -> Bị chặn 403 Forbidden")
    void testSalesRepKhongCoQuyen() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSalesRep);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Giám đốc kinh doanh truy cập -> Hiển thị trang cấu hình quy tắc")
    void testDirectorTruyCapThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getServletPath()).thenReturn("/admin/lead/phan-bo");

        servlet.doGet(request, response);

        verify(service).layDanhSachQuyTac(false);
        verify(khuVucDAO).layTatCaDangHoatDong();
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/quy-tac-phan-bo.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Tạo mới quy tắc phân bổ thành công qua form submit")
    void testTaoMoiQuyTacThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getServletPath()).thenReturn("/admin/lead/phan-bo/tao-moi");
        when(request.getParameter("ten_quy_tac")).thenReturn("Rule Miền Bắc");
        when(request.getParameter("kieu_phan_bo")).thenReturn("THEO_KHU_VUC");
        when(request.getParameterValues("khu_vuc_ids")).thenReturn(new String[]{"1", "2"});

        when(service.taoQuyTac(any(PhanBoLeadRequestDTO.class), eq(1L))).thenReturn(new QuyTacPhanBoLead());

        servlet.doPost(request, response);

        verify(service).taoQuyTac(any(PhanBoLeadRequestDTO.class), eq(1L));
        verify(response).sendRedirect("/crm/admin/lead/phan-bo");
    }

    @Test
    @DisplayName("Đổi thứ tự ưu tiên quy tắc qua AJAX")
    void testDoiThuTuAJAX() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getServletPath()).thenReturn("/admin/lead/phan-bo/doi-thu-tu");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("huong")).thenReturn("LEN");
        when(service.doiThuTuUuTien(5L, "LEN")).thenReturn(true);

        servlet.doPost(request, response);

        verify(service).doiThuTuUuTien(5L, "LEN");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"success\":true"));
    }

    @Test
    @DisplayName("Kích hoạt chạy phân bổ nền ngay lập tức (Test / Admin)")
    void testChayNgay() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userDirector);
        when(request.getServletPath()).thenReturn("/admin/lead/phan-bo/chay-ngay");
        when(worker.chayNgay(50)).thenReturn(new KetQuaPhanBoDTO(10, 8, 2, 120));

        servlet.doPost(request, response);

        verify(worker).chayNgay(50);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("\"soLeadDaQuet\":10"));
        assertTrue(responseWriter.toString().contains("\"soLeadPhanBoThanhCong\":8"));
    }
}
