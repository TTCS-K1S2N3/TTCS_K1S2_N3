package vn.nhom10.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.model.LichSuLienHeCongTy;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;
import vn.nhom10.crm.service.NguoiLienHeService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử NguoiLienHeServlet - Controller Người liên hệ & Vai trò quyết định mua (Story S3-02)")
class NguoiLienHeServletTest {

    private NguoiLienHeServlet servlet;
    private NguoiLienHeService service;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseWriter;

    private vn.nhom10.crm.model.NguoiDung user;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(NguoiLienHeService.class);
        servlet = new NguoiLienHeServlet(service);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getSession(anyBoolean())).thenReturn(session);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        user = new vn.nhom10.crm.model.NguoiDung();
        user.setId(101L);
        user.setHoTen("Nguyễn Văn A");
        when(session.getAttribute("nguoiDung")).thenReturn(user);
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập truy cập GET /nguoi-lien-he bị chuyển hướng về /dang-nhap")
    void testChuaDangNhap_RedirectDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("GET /nguoi-lien-he?action=list&khachHangId=1 trả về JSON danh sách")
    void testGetDanhSach_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("list");
        when(request.getParameter("khachHangId")).thenReturn("1");

        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Nguyễn Văn Test", "Trưởng phòng", "test@crm.vn", "0912345678", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, true);
        when(service.layDanhSachTheoKhachHang(user, 1L)).thenReturn(Collections.singletonList(nlh));

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Nguyễn Văn Test"));
        assertTrue(json.contains("NGUOI_QUYET_DINH"));
        assertTrue(json.contains("\"laDauMoiChinh\":true"));
    }

    @Test
    @DisplayName("GET /nguoi-lien-he?action=lich-su&id=10 trả về JSON lịch sử công tác (AC4)")
    void testGetLichSu_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("lich-su");
        when(request.getParameter("id")).thenReturn("10");

        LichSuLienHeCongTy ls = new LichSuLienHeCongTy();
        ls.setId(1L);
        ls.setNguoiLienHeId(10L);
        ls.setKhachHangId(1L);
        ls.setTenCongTy("Công ty Cũ");
        ls.setChucDanh("Giám đốc Mua sắm");
        ls.setVaiTroQuyetDinh(VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG);

        when(service.layLichSuCongTy(user, 10L)).thenReturn(Collections.singletonList(ls));

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Công ty Cũ"));
        assertTrue(json.contains("Giám đốc Mua sắm"));
    }

    @Test
    @DisplayName("POST /nguoi-lien-he?action=create tạo người liên hệ thành công (AC1, AC2, AC3)")
    void testPostCreate_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("khachHangId")).thenReturn("1");
        when(request.getParameter("hoTen")).thenReturn("Lê Văn Mới");
        when(request.getParameter("chucDanh")).thenReturn("Chuyên viên");
        when(request.getParameter("email")).thenReturn("moi.lv@fpt.com");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");
        when(request.getParameter("vaiTroQuyetDinh")).thenReturn("NGUOI_DUNG_CUOI");
        when(request.getParameter("laDauMoiChinh")).thenReturn("true");

        NguoiLienHe saved = new NguoiLienHe(99L, 1L, "Lê Văn Mới", "Chuyên viên", "moi.lv@fpt.com", "0912345678", VaiTroQuyetDinhEnum.NGUOI_DUNG_CUOI, true);
        when(service.themNguoiLienHe(eq(user), any(NguoiLienHe.class))).thenReturn(saved);

        servlet.doPost(request, response);

        verify(service).themNguoiLienHe(eq(user), argThat(nlh -> {
            assertEquals("Lê Văn Mới", nlh.getHoTen());
            assertEquals(VaiTroQuyetDinhEnum.NGUOI_DUNG_CUOI, nlh.getVaiTroQuyetDinh());
            assertTrue(nlh.isLaDauMoiChinh());
            return true;
        }));
        verify(response).sendRedirect("/crm/khach-hang?id=1");
    }

    @Test
    @DisplayName("POST /nguoi-lien-he?action=set-main đánh dấu đầu mối chính (AC3)")
    void testPostSetMain_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("set-main");
        when(request.getParameter("id")).thenReturn("15");
        when(request.getParameter("khachHangId")).thenReturn("1");

        when(service.datLamDauMoiChinh(user, 15L, 1L)).thenReturn(true);

        servlet.doPost(request, response);

        verify(service).datLamDauMoiChinh(user, 15L, 1L);
        verify(response).sendRedirect("/crm/khach-hang?id=1");
    }

    @Test
    @DisplayName("POST /nguoi-lien-he?action=transfer-company chuyển công ty thành công (AC4)")
    void testPostTransferCompany_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("transfer-company");
        when(request.getParameter("id")).thenReturn("15");
        when(request.getParameter("khachHangMoiId")).thenReturn("2");
        when(request.getParameter("chucDanhMoi")).thenReturn("Trưởng ban Mua sắm");
        when(request.getParameter("vaiTroMoi")).thenReturn("NGUOI_QUYET_DINH");
        when(request.getParameter("ghiChu")).thenReturn("Chuyển công tác");

        when(service.chuyenCongTy(eq(user), eq(15L), eq(2L), eq("Trưởng ban Mua sắm"), eq(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH), eq("Chuyển công tác")))
                .thenReturn(true);

        servlet.doPost(request, response);

        verify(service).chuyenCongTy(user, 15L, 2L, "Trưởng ban Mua sắm", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, "Chuyển công tác");
        verify(response).sendRedirect("/crm/khach-hang?id=2");
    }

    @Test
    @DisplayName("Bảo mật: Vi phạm Data Scope bị trả về 403 Forbidden")
    void testSecurityException_TraVe403() throws Exception {
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("khachHangId")).thenReturn("2");
        when(request.getParameter("hoTen")).thenReturn("Hacker");

        doThrow(new SecurityException("Từ chối thao tác ngoài phạm vi"))
                .when(service).themNguoiLienHe(eq(user), any());

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        String json = responseWriter.toString();
        assertTrue(json.contains("Từ chối thao tác ngoài phạm vi"));
    }
}
