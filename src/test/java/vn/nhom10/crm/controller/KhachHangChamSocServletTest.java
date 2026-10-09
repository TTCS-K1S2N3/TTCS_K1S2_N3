package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.dto.ThongKeChamSocDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.ChamSocKhachHangService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử KhachHangServlet - Chăm sóc khách hàng định kỳ (Story S3-09)")
class KhachHangChamSocServletTest {

    private KhachHangServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userCSKH;
    private NguoiDung userSalesA;
    private NguoiDung userAccountant;

    @BeforeEach
    void setUp() {
        servlet = new KhachHangServlet(new PhanQuyenDuLieuService(null), new ChamSocKhachHangService(null, null));
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // User CSKH: Nhân viên Chăm sóc khách hàng
        userCSKH = new NguoiDung();
        userCSKH.setId(10L);
        userCSKH.setHoTen("Trần Chăm Sóc");
        userCSKH.setEmail("cskh@crm.vn");
        userCSKH.setNhomKinhDoanhId(1);
        userCSKH.setTenNhomKinhDoanh("Phòng CSKH");
        userCSKH.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.CUST_SUCCESS, PhamViDuLieu.TOAN_BO)));

        // User Sales A: Nhân viên kinh doanh
        userSalesA = new NguoiDung();
        userSalesA.setId(101L);
        userSalesA.setHoTen("Nguyễn Văn A (Sales)");
        userSalesA.setEmail("sales.a@crm.vn");
        userSalesA.setNhomKinhDoanhId(1);
        userSalesA.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        userSalesA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        // User Kế toán: Không có quyền thao tác chăm sóc
        userAccountant = new NguoiDung();
        userAccountant.setId(301L);
        userAccountant.setHoTen("Vũ Kế Toán");
        userAccountant.setEmail("ketoan@crm.vn");
        userAccountant.setNhomKinhDoanhId(1);
        userAccountant.setTenNhomKinhDoanh("Phòng Kế Toán");
        userAccountant.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.ACCOUNTANT, PhamViDuLieu.TOAN_BO)));
    }

    @Test
    @DisplayName("AC1: doGet tiếp nhận tab=cham-soc và soNgay cấu hình, truyền danh sách và thống kê")
    void testDoGet_TabChamSoc_DayDuThongTin() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userCSKH);
        when(request.getParameter("tab")).thenReturn("cham-soc");
        when(request.getParameter("soNgay")).thenReturn("45");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("tabHienTai"), eq("cham-soc"));
        verify(request).setAttribute(eq("soNgayCauHinh"), eq("45"));
        verify(request).setAttribute(eq("laChamSocKhachHang"), eq(true));
        verify(request).setAttribute(eq("coQuyenChamSoc"), eq(true));

        // Kiểm tra nạp danh sách và thẻ thống kê
        verify(request).setAttribute(eq("danhSachChamSoc"), argThat(arg -> {
            List<?> list = (List<?>) arg;
            assertNotNull(list);
            return true;
        }));
        verify(request).setAttribute(eq("thongKeChamSoc"), argThat(arg -> {
            ThongKeChamSocDTO stats = (ThongKeChamSocDTO) arg;
            assertNotNull(stats);
            return true;
        }));
    }

    @Test
    @DisplayName("Persona CSKH: Tự động mặc định vào tab chăm sóc khi không chỉ định tab")
    void testDoGet_CustomerSuccess_DefaultTabChamSoc() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userCSKH);
        when(request.getParameter("tab")).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("tabHienTai"), eq("cham-soc"));
        verify(request).setAttribute(eq("laChamSocKhachHang"), eq(true));
    }

    @Test
    @DisplayName("AC3: doPost action=danhDauLienHe thành công khi submit form thông thường")
    void testDoPost_DanhDauLienHe_ThanhCong_FormSubmit() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userSalesA);
        when(request.getParameter("action")).thenReturn("danhDauLienHe");
        when(request.getParameter("khachHangId")).thenReturn("1");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty Cổ phần Công nghệ FPT");
        when(request.getParameter("ghiChu")).thenReturn("Đã gọi điện hỏi thăm vận hành hệ thống.");
        when(request.getParameter("kenhLienHe")).thenReturn("CUOC_GOI");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Đã đánh dấu liên hệ thành công"));
        verify(request).setAttribute(eq("tabHienTai"), eq("cham-soc"));
    }

    @Test
    @DisplayName("AC3: doPost action=danhDauLienHe qua AJAX trả về JSON chuẩn")
    void testDoPost_DanhDauLienHe_AjaxJson_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userCSKH);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("action")).thenReturn("danhDauLienHe");
        when(request.getParameter("khachHangId")).thenReturn("1");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty FPT");
        when(request.getParameter("ghiChu")).thenReturn("Trao đổi tích cực");
        when(request.getParameter("kenhLienHe")).thenReturn("CUOC_GOI");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json;charset=UTF-8");
        String json = sw.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Công ty FPT"));
    }

    @Test
    @DisplayName("Server Security: Kế toán (ACCOUNTANT) bị chặn HTTP 403 Forbidden khi đánh dấu liên hệ")
    void testDoPost_DanhDauLienHe_Accountant_BiChan403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);
        when(request.getParameter("action")).thenReturn("danhDauLienHe");
        when(request.getParameter("khachHangId")).thenReturn("1");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty FPT");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Server Security Ajax: Kế toán bị trả về HTTP 403 Forbidden kèm JSON lỗi")
    void testDoPost_DanhDauLienHe_Accountant_Ajax_BiChan403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userAccountant);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("action")).thenReturn("danhDauLienHe");
        when(request.getParameter("khachHangId")).thenReturn("1");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json;charset=UTF-8");
        String json = sw.toString();
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("không có quyền"));
    }

    @Test
    @DisplayName("Server Validation: Thiếu ID khách hàng trả về HTTP 400 Bad Request")
    void testDoPost_DanhDauLienHe_ThieuId_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userCSKH);
        when(request.getParameter("action")).thenReturn("danhDauLienHe");
        when(request.getParameter("khachHangId")).thenReturn("");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("không hợp lệ"));
    }
}
