package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.dto.TruongTuyChinhDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TruongTuyChinh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.TruongTuyChinhService;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Servlet điều hướng Trường tuỳ chỉnh (Story S2-08)")
class TruongTuyChinhServletTest {

    @Mock
    private TruongTuyChinhService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private TruongTuyChinhServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TruongTuyChinhServlet(service);
    }

    private void mockAdminUser() {
        when(request.getSession(false)).thenReturn(session);
        NguoiDung admin = new NguoiDung(1L, "Nguyễn Quản Trị", "admin@crm.vn");
        VaiTro vt = new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Toàn quyền quản trị");
        admin.setDanhSachVaiTro(Set.of(vt));
        when(session.getAttribute("nguoiDung")).thenReturn(admin);
    }

    private void mockRegularUser() {
        when(request.getSession(false)).thenReturn(session);
        NguoiDung sales = new NguoiDung(2L, "Trần Bán Hàng", "sales@crm.vn");
        VaiTro vt = new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Kinh doanh");
        sales.setDanhSachVaiTro(Set.of(vt));
        when(session.getAttribute("nguoiDung")).thenReturn(sales);
    }

    @Test
    @DisplayName("GET /truong-tuy-chinh: Hiển thị danh sách trường tuỳ chỉnh cho Khách hàng và Cơ hội")
    void testHienThiDanhSach() throws Exception {
        mockAdminUser();
        when(request.getServletPath()).thenReturn("/truong-tuy-chinh");
        when(request.getParameter("doiTuong")).thenReturn("KHACH_HANG");
        when(request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/danh-sach.jsp")).thenReturn(dispatcher);

        List<TruongTuyChinh> mockKh = List.of(new TruongTuyChinh("KHACH_HANG", "nguon_kh", "Nguồn KH", "VAN_BAN", false));
        List<TruongTuyChinh> mockCh = List.of(new TruongTuyChinh("CO_HOI", "ngan_sach", "Ngân sách", "SO", false));

        when(service.layDanhSachTheoDoiTuong("KHACH_HANG", false)).thenReturn(mockKh);
        when(service.layDanhSachTheoDoiTuong("CO_HOI", false)).thenReturn(mockCh);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsTruongKhachHang"), eq(mockKh));
        verify(request).setAttribute(eq("dsTruongCoHoi"), eq(mockCh));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /truong-tuy-chinh/tao: Hiển thị form tạo mới trường tuỳ chỉnh")
    void testHienThiFormTao() throws Exception {
        mockAdminUser();
        when(request.getServletPath()).thenReturn("/truong-tuy-chinh/tao");
        when(request.getParameter("doiTuong")).thenReturn("CO_HOI");
        when(request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/tao-truong.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("oldInput"), any(TruongTuyChinhDTO.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /truong-tuy-chinh/tao: Tạo trường thành công và chuyển hướng")
    void testXuLyTaoTruongThanhCong() throws Exception {
        mockAdminUser();
        when(request.getServletPath()).thenReturn("/truong-tuy-chinh/tao");
        when(request.getParameter("doiTuong")).thenReturn("KHACH_HANG");
        when(request.getParameter("tenTruong")).thenReturn("nguon_gioi_thieu");
        when(request.getParameter("nhanHien")).thenReturn("Nguồn giới thiệu");
        when(request.getParameter("kieuDuLieu")).thenReturn("VAN_BAN");
        when(request.getParameter("batBuoc")).thenReturn("true");
        when(request.getParameter("thuTu")).thenReturn("1");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(true)).thenReturn(session);

        when(service.validateDinhNghiaTruong(any(TruongTuyChinhDTO.class), eq(true))).thenReturn(Collections.emptyMap());
        when(service.taoTruongTuyChinh(any(TruongTuyChinhDTO.class), anyLong())).thenReturn(100L);

        servlet.doPost(request, response);

        verify(service).taoTruongTuyChinh(any(TruongTuyChinhDTO.class), eq(1L));
        verify(session).setAttribute(eq("thongBaoThanhCong"), contains("Đã tạo thành công"));
        verify(response).sendRedirect("/crm/truong-tuy-chinh?doiTuong=KHACH_HANG");
    }

    @Test
    @DisplayName("POST /truong-tuy-chinh/tao: Báo lỗi khi dữ liệu form không hợp lệ")
    void testXuLyTaoTruongLoi() throws Exception {
        mockAdminUser();
        when(request.getServletPath()).thenReturn("/truong-tuy-chinh/tao");
        when(request.getParameter("doiTuong")).thenReturn("KHACH_HANG");
        when(request.getParameter("tenTruong")).thenReturn(""); // Trống
        when(request.getParameter("nhanHien")).thenReturn("");  // Trống
        when(request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/tao-truong.jsp")).thenReturn(dispatcher);

        Map<String, String> errors = Map.of(
                "nhanHien", "Nhãn hiển thị không được để trống.",
                "tenTruong", "Tên kỹ thuật không được để trống."
        );
        when(service.validateDinhNghiaTruong(any(TruongTuyChinhDTO.class), eq(true))).thenReturn(errors);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("formError"), eq(errors));
        verify(dispatcher).forward(request, response);
        verify(service, never()).taoTruongTuyChinh(any(), any());
    }

    @Test
    @DisplayName("GET /truong-tuy-chinh/sua: Hiển thị form sửa trường tuỳ chỉnh theo ID")
    void testHienThiFormSua() throws Exception {
        mockAdminUser();
        when(request.getServletPath()).thenReturn("/truong-tuy-chinh/sua");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/sua-truong.jsp")).thenReturn(dispatcher);

        TruongTuyChinh t = new TruongTuyChinh("CO_HOI", "muc_do_uu_tien", "Mức độ ưu tiên", "DANH_SACH_CHON", true);
        t.setId(5L);
        t.setDanhSachLuaChon(List.of("Cao", "Trung bình", "Thấp"));
        when(service.layTheoId(5L)).thenReturn(t);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("truong"), any(TruongTuyChinhDTO.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST /truong-tuy-chinh/trang-thai: Đổi trạng thái bật/tắt trường tuỳ chỉnh")
    void testDoiTrangThai() throws Exception {
        mockAdminUser();
        when(request.getServletPath()).thenReturn("/truong-tuy-chinh/trang-thai");
        when(request.getParameter("id")).thenReturn("8");
        when(request.getParameter("trangThai")).thenReturn("false");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(true)).thenReturn(session);

        servlet.doPost(request, response);

        verify(service).doiTrangThai(8L, false);
        verify(response).sendRedirect("/crm/truong-tuy-chinh");
    }

    @Test
    @DisplayName("Phân quyền: Người dùng không có vai trò ADMIN/DIRECTOR bị chặn 403 Forbidden")
    void testChanNguoiDungKhongCoQuyen() throws Exception {
        mockRegularUser(); // SALES_REP

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains("không có quyền"));
        verify(dispatcher, never()).forward(request, response);
    }
}
