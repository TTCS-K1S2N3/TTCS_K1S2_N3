package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.KetQuaPhanTrangDTO;
import vn.nhom10.crm.dto.NhatKyThayDoiDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.NhatKyThayDoiService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử NhatKyThayDoiServlet - Phân quyền và Bảo mật Story S2-04")
class NhatKyThayDoiServletTest {

    private NhatKyThayDoiServlet servlet;

    @Mock
    private NhatKyThayDoiService nhatKyService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private NguoiDung adminUser;

    @BeforeEach
    void setUp() {
        servlet = new NhatKyThayDoiServlet(nhatKyService);

        adminUser = new NguoiDung();
        adminUser.setId(103L);
        adminUser.setHoTen("Nguyễn Thị Thu Hà");
        adminUser.setEmail("ha.ntt@crm.vn");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        when(nhatKyService.timKiemNhatKy(any())).thenReturn(
                new KetQuaPhanTrangDTO<>(Collections.emptyList(), 1, 10, 0));
        when(nhatKyService.layThongKe(any())).thenReturn(
                new ThongKeNhatKyDTO(0, 0, 0, 0, 0, 0));
        when(nhatKyService.layDanhSachNguoiDung()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("RBAC: ADMIN truy cập thành công vào /nhat-ky-thay-doi")
    void testAdminTruyCapThanhCong() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("RBAC: DIRECTOR truy cập bị từ chối 403 Forbidden")
    void testDirectorBi403() throws ServletException, IOException {
        NguoiDung director = new NguoiDung();
        director.setId(201L);
        director.setHoTen("Nguyễn Giám Đốc");
        director.themVaiTro(VaiTroEnum.DIRECTOR);

        when(session.getAttribute("nguoiDung")).thenReturn(director);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("errorMessage"), contains("Chỉ Quản trị hệ thống (Admin)"));
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("RBAC: SALES_REP truy cập bị từ chối 403 Forbidden")
    void testSalesRepBi403() throws ServletException, IOException {
        NguoiDung sales = new NguoiDung();
        sales.setId(202L);
        sales.setHoTen("Lê Nhân Viên");
        sales.themVaiTro(VaiTroEnum.SALES_REP);

        when(session.getAttribute("nguoiDung")).thenReturn(sales);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("errorMessage"), contains("Chỉ Quản trị hệ thống (Admin)"));
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
    }

    @Test
    @DisplayName("RBAC: TEAM_LEAD truy cập bị từ chối 403 Forbidden")
    void testTeamLeadBi403() throws ServletException, IOException {
        NguoiDung lead = new NguoiDung();
        lead.setId(203L);
        lead.setHoTen("Trần Trưởng Nhóm");
        lead.themVaiTro(VaiTroEnum.TEAM_LEAD);

        when(session.getAttribute("nguoiDung")).thenReturn(lead);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("errorMessage"), contains("Chỉ Quản trị hệ thống (Admin)"));
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập bị chuyển hướng về /dang-nhap")
    void testChuaDangNhapBiChuyenHuong() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect(contains("/dang-nhap"));
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Empty DB: Trả về empty state, không có dữ liệu mẫu nào")
    void testEmptyDbRendersEmptyState() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(nhatKyService.timKiemNhatKy(any())).thenReturn(
                new KetQuaPhanTrangDTO<>(Collections.emptyList(), 1, 10, 0));

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("danhSachNhatKy"), eq(Collections.emptyList()));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Validation khoảng ngày: 'Từ ngày' lớn hơn 'Đến ngày' hiển thị thông báo lỗi")
    void testValidateKhoangNgayKhongHopLe() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getParameter("tuNgay")).thenReturn("2026-09-30");
        when(request.getParameter("denNgay")).thenReturn("2026-09-01");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), contains("Khoảng thời gian không hợp lệ"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Validation định dạng ngày xấu: không gây sập 500")
    void testDinhDangNgayXauKhongGay500() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getParameter("tuNgay")).thenReturn("abc-invalid-date");
        when(request.getParameter("denNgay")).thenReturn("2026-99-99");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Detail JSON: ID không hợp lệ (<= 0 hoặc chuỗi) trả về 400 Bad Request")
    void testDetailJsonIdKhongHopLe() throws IOException, ServletException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi/chi-tiet");
        when(request.getParameter("id")).thenReturn("-5");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(sw.toString().contains("error"));
    }

    @Test
    @DisplayName("Detail JSON: ID không tồn tại trả về 404 Not Found")
    void testDetailJsonKhongTonTai() throws IOException, ServletException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi/chi-tiet");
        when(request.getParameter("id")).thenReturn("9999");
        when(nhatKyService.layChiTiet(9999L)).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(sw.toString().contains("Không tìm thấy"));
    }

    @Test
    @DisplayName("Detail JSON: Tuyệt đối không leak secret/password/token thô trong response")
    void testDetailJsonKhongLeakSecret() throws IOException, ServletException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi/chi-tiet");
        when(request.getParameter("id")).thenReturn("1");

        NhatKyThayDoiDTO item = new NhatKyThayDoiDTO(
                1L, "LOG-000001",
                103L, "Thu Hà", "ha@crm.vn", "Quản trị viên",
                LocalDateTime.of(2026, 9, 30, 10, 0, 0),
                LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                "ND-105", "User Nam",
                "mat_khau", "P@ssw0rdRawSecret!", "NewSecretKey999!",
                HanhDongThayDoi.CAP_NHAT, "Đổi thông tin", "127.0.0.1", "Chrome"
        );

        when(nhatKyService.layChiTiet(1L)).thenReturn(item);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = sw.toString();

        assertFalse(json.contains("P@ssw0rdRawSecret!"), "Không được leak mật khẩu thô trong detail JSON");
        assertFalse(json.contains("NewSecretKey999!"), "Không được leak mật khẩu mới thô trong detail JSON");
        assertTrue(json.contains("\"giaTriTruoc\":\"******\""));
        assertTrue(json.contains("\"giaTriSau\":\"******\""));
    }
}
