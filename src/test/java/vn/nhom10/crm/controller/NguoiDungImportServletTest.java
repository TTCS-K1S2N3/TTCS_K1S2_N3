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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dto.BaoCaoNhapExcelDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.NguoiDungImportService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Controller NguoiDungImportServlet (Story S2-01)")
public class NguoiDungImportServletTest {

    @Mock
    private NguoiDungImportService importService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private Part filePart;

    private NguoiDungImportServlet servlet;
    private NguoiDung adminUser;

    @BeforeEach
    void setUp() {
        servlet = new NguoiDungImportServlet(importService);

        adminUser = new NguoiDung(1L, "Admin User", "admin@crm.vn");
        adminUser.setDanhSachVaiTro(Set.of(new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin")));
    }

    @Test
    @DisplayName("AC 1: GET /nguoi-dung/tai-tep-mau trả về tệp Excel mẫu đính kèm")
    void testDoGet_TaiTepMau() throws Exception {
        byte[] sampleBytes = new byte[]{1, 2, 3, 4};
        when(importService.taoTepMauExcel()).thenReturn(sampleBytes);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getServletPath()).thenReturn("/nguoi-dung/tai-tep-mau");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {}
            @Override
            public void write(int b) { baos.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        servlet.doGet(request, response);

        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), eq("attachment; filename=\"mau_nhap_nguoi_dung.xlsx\""));
        verify(response).setContentLength(sampleBytes.length);
        assertEquals(4, baos.size());
    }

    @Test
    @DisplayName("GET /nguoi-dung/import hiển thị giao diện nhập Excel")
    void testDoGet_HienThiTrang() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getServletPath()).thenReturn("/nguoi-dung/import");
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC 2: POST xem-truoc gọi importService.xemTruoc và chuyển tiếp đến JSP")
    void testDoPost_XemTruoc() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("xem-truoc");
        when(request.getPart("fileExcel")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(1024L);
        when(filePart.getSubmittedFileName()).thenReturn("users.xlsx");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        BaoCaoNhapExcelDTO mockBaoCao = new BaoCaoNhapExcelDTO("users.xlsx");
        when(importService.xemTruoc(any(InputStream.class), anyLong(), anyString())).thenReturn(mockBaoCao);
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("baoCao"), eq(mockBaoCao));
        verify(request).setAttribute(eq("cheDo"), eq("xem-truoc"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC 3: POST nhap-du-lieu gọi importService.thucHienNhap và chuyển tiếp báo cáo đến JSP")
    void testDoPost_NhapDuLieu() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getParameter("action")).thenReturn("nhap-du-lieu");
        when(request.getPart("fileExcel")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(2048L);
        when(filePart.getSubmittedFileName()).thenReturn("danh_sach_kinh_doanh.xlsx");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        BaoCaoNhapExcelDTO mockBaoCao = new BaoCaoNhapExcelDTO("danh_sach_kinh_doanh.xlsx");
        mockBaoCao.setSoDongThanhCong(5);
        when(importService.thucHienNhap(any(InputStream.class), anyLong(), anyString())).thenReturn(mockBaoCao);
        when(request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("baoCao"), eq(mockBaoCao));
        verify(request).setAttribute(eq("cheDo"), eq("ket-qua"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Bảo mật: Người dùng không có quyền ADMIN bị trả mã lỗi 403 Forbidden")
    void testPhanQuyen_KhongPhaiAdmin_403() throws Exception {
        NguoiDung regularUser = new NguoiDung(2L, "Sales Rep", "sales@crm.vn");
        regularUser.setDanhSachVaiTro(Set.of(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep")));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(regularUser);

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
        verify(dispatcher, never()).forward(request, response);
    }
}
