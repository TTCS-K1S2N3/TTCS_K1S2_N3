package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BaoCaoNhapLeadExcelDTO;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LeadImportService;
import vn.nhom10.crm.service.PermissionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LeadImportServlet - Story S4-02: AC2 (Tệp mẫu, xem trước & báo lỗi dòng) & AC3 (Bắt buộc nguồn)")
class LeadImportServletTest {

    private LeadImportService importService;
    private PermissionService permissionService;
    private LeadImportServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userMarketing;
    private NguoiDung userAccountant;

    @BeforeEach
    void setUp() {
        importService = mock(LeadImportService.class);
        permissionService = mock(PermissionService.class);
        servlet = new LeadImportServlet(importService, permissionService);

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
    @DisplayName("AC2: GET /lead/tai-tep-mau tải về tệp Excel mẫu đúng Content-Type và header")
    void testTaiTepMauExcel() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/tai-tep-mau");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {}
            @Override
            public void write(int b) throws IOException { baos.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        servlet.doGet(request, response);

        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), contains("tep_mau_nhap_lead.xlsx"));
        assertTrue(baos.size() > 0, "Dữ liệu tệp mẫu phải có nội dung");
    }

    @Test
    @DisplayName("GET /lead/import-excel: Hiển thị giao diện kéo thả tệp")
    void testHienThiGiaoDienNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getServletPath()).thenReturn("/lead/import-excel");

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC2 & AC3: POST /lead/import-excel action=xem-truoc -> Thẩm định và hiển thị xem trước")
    void testXuLyXemTruocTepExcel() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getParameter("action")).thenReturn("xem-truoc");

        Part filePart = mock(Part.class);
        when(request.getPart("fileExcel")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(2048L);
        when(filePart.getHeader("content-disposition")).thenReturn("form-data; name=\"fileExcel\"; filename=\"danh_sach.xlsx\"");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        BaoCaoNhapLeadExcelDTO baoCao = new BaoCaoNhapLeadExcelDTO("danh_sach.xlsx");
        baoCao.setTongSoDong(5);
        baoCao.setSoDongHopLe(4);
        baoCao.setSoDongLoi(1);
        when(importService.thamDinhTepExcel(any(), eq("danh_sach.xlsx"))).thenReturn(baoCao);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq(LeadImportServlet.SESSION_BAO_CAO_LEAD), eq(baoCao));
        verify(request).setAttribute(eq("baoCao"), eq(baoCao));
        verify(request).setAttribute(eq("cheDo"), eq("xem-truoc"));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC2: POST /lead/import-excel action=nhap-du-lieu -> Thực hiện nhập và chuyển sang cheDo ket-qua")
    void testXuLyXacNhanNhapDuLieu() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userMarketing);
        when(request.getParameter("action")).thenReturn("nhap-du-lieu");

        BaoCaoNhapLeadExcelDTO baoCao = new BaoCaoNhapLeadExcelDTO("leads.xlsx");
        baoCao.setTongSoDong(3);
        baoCao.setSoDongHopLe(3);
        when(session.getAttribute(LeadImportServlet.SESSION_BAO_CAO_LEAD)).thenReturn(baoCao);
        when(session.getAttribute(LeadImportServlet.SESSION_FILE_NAME_LEAD)).thenReturn("leads.xlsx");

        servlet.doPost(request, response);

        verify(importService).thucHienNhapDuLieu(eq(baoCao), eq(10L));
        verify(session).removeAttribute(eq(LeadImportServlet.SESSION_BAO_CAO_LEAD));
        verify(request).setAttribute(eq("cheDo"), eq("ket-qua"));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp");
        verify(dispatcher).forward(request, response);
    }
}
