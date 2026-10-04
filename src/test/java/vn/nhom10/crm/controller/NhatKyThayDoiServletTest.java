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
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử NhatKyThayDoiServlet - Story S2-04")
class NhatKyThayDoiServletTest {

    private NhatKyThayDoiServlet servlet;

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
        servlet = new NhatKyThayDoiServlet();

        adminUser = new NguoiDung();
        adminUser.setId(103L);
        adminUser.setHoTen("Nguyễn Thị Thu Hà");
        adminUser.setEmail("ha.ntt@crm.vn");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Bảo mật Server-side: Chưa đăng nhập bị chuyển hướng về /dang-nhap")
    void testChuaDangNhapBiChuyenHuong() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect(contains("/dang-nhap"));
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Bảo mật Server-side: Người dùng không phải ADMIN bị từ chối 403 Forbidden")
    void testKhongPhaiAdminBiTuChoi() throws ServletException, IOException {
        NguoiDung salesUser = new NguoiDung();
        salesUser.setId(102L);
        salesUser.setHoTen("Phạm Hoàng Long");
        salesUser.themVaiTro(VaiTroEnum.SALES_REP);

        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("errorMessage"), contains("Chỉ Quản trị hệ thống (Admin)"));
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
    }

    @Test
    @DisplayName("doGet điều hướng thành công đến JSP và gắn đủ thuộc tính cần thiết")
    void testDoGetDieuHuongJsp() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("boLoc"), any(BoLocNhatKyDTO.class));
        verify(request).setAttribute(eq("phanTrang"), any(KetQuaPhanTrangDTO.class));
        verify(request).setAttribute(eq("danhSachNhatKy"), anyList());
        verify(request).setAttribute(eq("thongKe"), any());
        verify(request).setAttribute(eq("danhSachNguoiDung"), anyList());
        verify(request).setAttribute(eq("danhSachLoaiDoiTuong"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet với tham số lọc người dùng, loại đối tượng và ngày hợp lệ")
    void testDoGetCoThamSoLoc() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getParameter("nguoiDungId")).thenReturn("101");
        when(request.getParameter("loaiDoiTuong")).thenReturn("CHI_TIEU");
        when(request.getParameter("tuNgay")).thenReturn("2026-09-01");
        when(request.getParameter("denNgay")).thenReturn("2026-09-30");
        when(request.getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        ArgumentCaptor<BoLocNhatKyDTO> captor = ArgumentCaptor.forClass(BoLocNhatKyDTO.class);
        verify(request).setAttribute(eq("boLoc"), captor.capture());

        BoLocNhatKyDTO boLoc = captor.getValue();
        assertEquals(101L, boLoc.getNguoiDungId());
        assertEquals("CHI_TIEU", boLoc.getLoaiDoiTuong());
        assertEquals("2026-09-01", boLoc.getTuNgayChuoi());
        assertEquals("2026-09-30", boLoc.getDenNgayChuoi());
        assertTrue(boLoc.isKhoangThoiGianHopLe());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet với khoảng thời gian không hợp lệ (từ ngày sau đến ngày) tạo thông báo lỗi")
    void testDoGetKhoangThoiGianKhongHopLe() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getParameter("tuNgay")).thenReturn("2026-09-30");
        when(request.getParameter("denNgay")).thenReturn("2026-09-01");
        when(request.getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("thongBaoLoi"), contains("Khoảng thời gian không hợp lệ"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet với quickPeriod=quarter thiết lập đúng mốc ngày theo quý")
    void testDoGetQuickPeriodQuarter() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi");
        when(request.getParameter("quickPeriod")).thenReturn("quarter");
        when(request.getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        ArgumentCaptor<BoLocNhatKyDTO> captor = ArgumentCaptor.forClass(BoLocNhatKyDTO.class);
        verify(request).setAttribute(eq("boLoc"), captor.capture());

        BoLocNhatKyDTO boLoc = captor.getValue();
        assertNotNull(boLoc.getTuNgay());
        assertNotNull(boLoc.getDenNgay());
        assertTrue(boLoc.isKhoangThoiGianHopLe());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Yêu cầu AJAX chi tiết trả về JSON hợp lệ với HTTP 200")
    void testXemChiTietJsonThanhCong() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi/chi-tiet");
        when(request.getParameter("id")).thenReturn("1");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = stringWriter.toString();
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"maTruyVet\":"));
        assertTrue(json.contains("\"giaTriTruoc\":"));
        assertTrue(json.contains("\"giaTriSau\":"));
        assertTrue(json.contains("\"truongThayDoi\":"));
    }

    @Test
    @DisplayName("Yêu cầu AJAX chi tiết với ID không tồn tại trả về HTTP 404")
    void testXemChiTietJsonKhongTonTai() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/nhat-ky-thay-doi/chi-tiet");
        when(request.getParameter("id")).thenReturn("99999");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
