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
import vn.nhom10.crm.dto.BaoCaoNhapKhachHangExcelDTO;
import vn.nhom10.crm.dto.DongExcelKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.KhachHangImportService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Controller KhachHangImportServlet (Story S3-06)")
public class KhachHangImportServletTest {

    @Mock
    private PhanQuyenDuLieuService phanQuyenService;

    @Mock
    private KhachHangImportService importService;

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

    private KhachHangImportServlet servlet;
    private NguoiDung salesUser;

    @BeforeEach
    void setUp() {
        servlet = new KhachHangImportServlet(phanQuyenService, importService);
        salesUser = new NguoiDung();
        salesUser.setId(10L);
        salesUser.setHoTen("Nguyễn Văn Bán Hàng");
        salesUser.setEmail("sales@crm.vn");
    }

    @Test
    @DisplayName("Chưa đăng nhập -> Chuyển hướng về trang đăng nhập")
    void testDoGetChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("Đã đăng nhập -> Điều hướng vào giao diện import-excel.jsp")
    void testDoGetHienThiGiaoDien() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);
        when(request.getServletPath()).thenReturn("/khach-hang/import");
        when(request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC 1: Tải tệp mẫu Excel thành công qua URL /khach-hang/tai-tep-mau")
    void testDoGetTaiTepMau() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);
        when(request.getServletPath()).thenReturn("/khach-hang/tai-tep-mau");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };

        when(response.getOutputStream()).thenReturn(servletOut);

        servlet.doGet(request, response);

        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"mau_nhap_khach_hang.xlsx\"");
        assertTrue(out.size() > 0, "Dữ liệu tệp mẫu gửi về response phải có nội dung");
    }

    @Test
    @DisplayName("AC 1 & AC 2: POST action=xem-truoc -> Thẩm định tệp, lưu session và chuyển sang màn xem trước")
    void testDoPostXemTruocThanhCong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);
        when(request.getParameter("action")).thenReturn("xem-truoc");
        when(request.getPart("fileExcel")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(1024L);
        when(filePart.getSubmittedFileName()).thenReturn("danh_sach_khach.xlsx");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));

        BaoCaoNhapKhachHangExcelDTO mockBaoCao = new BaoCaoNhapKhachHangExcelDTO("danh_sach_khach.xlsx");
        DongExcelKhachHangDTO dong = new DongExcelKhachHangDTO(1);
        dong.setTenCongTy("Công ty Thử Nghiệm");
        mockBaoCao.napDanhSachDong(List.of(dong));

        when(importService.thamDinhTepExcel(any(InputStream.class), eq("danh_sach_khach.xlsx"), any(NguoiDungDTO.class)))
                .thenReturn(mockBaoCao);
        when(request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(session).setAttribute(KhachHangImportServlet.SESSION_BAO_CAO, mockBaoCao);
        verify(session).setAttribute(KhachHangImportServlet.SESSION_FILE_NAME, "danh_sach_khach.xlsx");
        verify(request).setAttribute("cheDo", "xem-truoc");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC 2: POST action=nhap-du-lieu -> Lấy báo cáo từ session, gọi service nhập và chuyển sang kết quả")
    void testDoPostXacNhanNhapDuLieu() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);
        when(request.getParameter("action")).thenReturn("nhap-du-lieu");
        when(request.getParameter("xuLyTrungLapChung")).thenReturn("BO_QUA");

        BaoCaoNhapKhachHangExcelDTO sessionBaoCao = new BaoCaoNhapKhachHangExcelDTO("danh_sach_khach.xlsx");
        DongExcelKhachHangDTO dong = new DongExcelKhachHangDTO(1);
        dong.setTenCongTy("Công ty ABC");
        dong.danhDauTrung("TRUNG_MST", "Trùng MST", 99L);
        sessionBaoCao.napDanhSachDong(List.of(dong));

        when(session.getAttribute(KhachHangImportServlet.SESSION_BAO_CAO)).thenReturn(sessionBaoCao);
        when(session.getAttribute(KhachHangImportServlet.SESSION_FILE_NAME)).thenReturn("danh_sach_khach.xlsx");
        when(request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(importService).thucHienNhapDuLieu(eq(sessionBaoCao), anyMap(), eq("BO_QUA"), any(NguoiDungDTO.class));
        verify(session).removeAttribute(KhachHangImportServlet.SESSION_BAO_CAO);
        verify(request).setAttribute("cheDo", "ket-qua");
        verify(dispatcher).forward(request, response);
    }
}
