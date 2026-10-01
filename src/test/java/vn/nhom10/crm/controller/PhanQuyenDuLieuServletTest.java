package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroNguoiDung;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Servlet phân quyền dữ liệu - Story S1-05 (AC1, AC2, AC3, AC4)")
class PhanQuyenDuLieuServletTest {

    private PhanQuyenDuLieuServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new PhanQuyenDuLieuServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập truy cập /phan-quyen-du-lieu bị redirect sang /dang-nhap")
    void testServlet_ChuaDangNhap_RedirectDangNhap() throws Exception {
        when(request.getServletPath()).thenReturn("/phan-quyen-du-lieu");
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("AC1 & AC2: Servlet xem danh sách với quyền Sales Rep, kiểm tra phạm vi hiệu lực")
    void testServlet_XemDanhSach_SalesA() throws Exception {
        when(request.getServletPath()).thenReturn("/phan-quyen-du-lieu");
        NguoiDungDTO salesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        when(session.getAttribute("nguoiDung")).thenReturn(salesA);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("currentUser"), any(NguoiDungDTO.class));
        verify(request).setAttribute(eq("phamViHienTai"), eq(PhamViDuLieu.CA_NHAN));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC3 & AC4: Truy cập trực tiếp bản ghi bằng ID ngoài phạm vi trả về HTTP 403 Forbidden")
    void testServlet_XemChiTiet_NgoaiPhamVi_TraVe403() throws Exception {
        when(request.getServletPath()).thenReturn("/chi-tiet-ban-ghi");
        when(request.getParameter("id")).thenReturn("5"); // ID 5 là khách hàng Viettel của Sales B

        // User đang đăng nhập là Sales A
        NguoiDungDTO salesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        when(session.getAttribute("nguoiDung")).thenReturn(salesA);

        servlet.doGet(request, response);

        // Bắt buộc phải set HTTP status 403 Forbidden
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối truy cập"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC3: Truy cập bản ghi hợp lệ trong phạm vi sở hữu trả về HTTP 200 OK")
    void testServlet_XemChiTiet_HopLe_TraVe200() throws Exception {
        when(request.getServletPath()).thenReturn("/chi-tiet-ban-ghi");
        when(request.getParameter("id")).thenReturn("1"); // ID 1 là FPT do Sales A phụ trách

        NguoiDungDTO salesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        when(session.getAttribute("nguoiDung")).thenReturn(salesA);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("thongBaoThanhCong"), anyString());
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Security & AC3: Thao tác sửa trực tiếp bằng ID ngoài phạm vi bị từ chối với HTTP 403")
    void testServlet_SuaBanGhi_NgoaiPhamVi_TraVe403() throws Exception {
        when(request.getServletPath()).thenReturn("/chi-tiet-ban-ghi");
        when(request.getParameter("id")).thenReturn("5"); // ID 5 của Sales B
        when(request.getParameter("tieuDe")).thenReturn("Tên mới bị hack");

        NguoiDungDTO salesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        when(session.getAttribute("nguoiDung")).thenReturn(salesA);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối thao tác sửa"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Security: Thao tác sửa trực tiếp bản ghi trong phạm vi thành công HTTP 200")
    void testServlet_SuaBanGhi_TrongPhamVi_ThanhCong() throws Exception {
        when(request.getServletPath()).thenReturn("/chi-tiet-ban-ghi");
        when(request.getParameter("id")).thenReturn("1"); // ID 1 của Sales A
        when(request.getParameter("tieuDe")).thenReturn("Công ty Cổ phần Công nghệ FPT - Cập nhật");

        NguoiDungDTO salesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        when(session.getAttribute("nguoiDung")).thenReturn(salesA);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Cập nhật dữ liệu thành công"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC2: Xuất file Excel / CSV tự động lọc theo phạm vi và trả về HTTP header hợp lệ")
    void testServlet_XuatExcel() throws Exception {
        when(request.getServletPath()).thenReturn("/phan-quyen-du-lieu");
        when(request.getParameter("xuatExcel")).thenReturn("true");

        NguoiDungDTO salesA = new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        when(session.getAttribute("nguoiDung")).thenReturn(salesA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {}

            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        servlet.doGet(request, response);

        verify(response).setContentType("text/csv; charset=UTF-8");
        verify(response).setHeader(eq("Content-Disposition"), contains("attachment; filename="));

        String csvData = baos.toString("UTF-8");
        assertTrue(csvData.contains("FPT"), "File xuất của A phải chứa FPT");
        assertFalse(csvData.contains("Viettel"), "File xuất của A tuyệt đối không được chứa Viettel (của B)");
    }
}
