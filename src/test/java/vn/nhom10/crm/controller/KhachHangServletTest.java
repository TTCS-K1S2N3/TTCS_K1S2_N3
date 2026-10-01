package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử KhachHangServlet - Chặn bypass Data Scope trên route thật /khach-hang (S1-05)")
class KhachHangServletTest {

    private KhachHangServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userA;
    private NguoiDung userLeadBac;

    @BeforeEach
    void setUp() {
        servlet = new KhachHangServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // User A: Nhân viên kinh doanh (Sales Rep - Nhóm Miền Bắc, ID = 101)
        userA = new NguoiDung();
        userA.setId(101L);
        userA.setHoTen("Nguyễn Văn A (Sales)");
        userA.setEmail("sales.a@crm.vn");
        userA.setNhomKinhDoanhId(1);
        userA.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        userA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP)));

        // User Lead: Trưởng nhóm kinh doanh (Team Lead - Nhóm Miền Bắc, ID = 100)
        userLeadBac = new NguoiDung();
        userLeadBac.setId(100L);
        userLeadBac.setHoTen("Lê Thị Trưởng Nhóm");
        userLeadBac.setEmail("lead.bac@crm.vn");
        userLeadBac.setNhomKinhDoanhId(1);
        userLeadBac.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        userLeadBac.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.TEAM_LEAD)));
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập truy cập /khach-hang bị chuyển hướng về /dang-nhap")
    void testKhachHang_ChuaDangNhap_RedirectDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("AC1 & AC2: User A (CA_NHAN) truy cập /khach-hang chỉ thấy khách của mình, không thấy khách của B")
    void testKhachHang_NhanVienA_XemDanhSach_ChiThayKhachCuaMinh() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("danhSachKhachHang"), argThat(list -> {
            List<?> ds = (List<?>) list;
            assertFalse(ds.isEmpty());
            for (Object obj : ds) {
                BanGhiNghiepVuDTO bg = (BanGhiNghiepVuDTO) obj;
                assertEquals(101L, bg.getNguoiPhuTrachId(), "Mọi khách hàng trả về phải do A phụ trách");
                assertNotEquals(102L, bg.getNguoiPhuTrachId(), "Khách hàng của B tuyệt đối không được xuất hiện");
            }
            return true;
        }));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC3 & AC4: User A cố tình gõ URL /khach-hang?id=5 (khách của B) bị từ chối với HTTP 403")
    void testKhachHang_NhanVienA_XemChiTiet_KhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("5"); // ID 5 là khách Viettel của Sales B (102)

        servlet.doGet(request, response);

        // Bắt buộc chặn với HTTP 403 Forbidden
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối truy cập"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC3: User A truy cập URL /khach-hang?id=1 (khách của chính mình) trả về HTTP 200")
    void testKhachHang_NhanVienA_XemChiTiet_KhachCuaMinh_TraVe200() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("1"); // ID 1 là FPT của chính A (101)

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("banGhiChiTiet"), any(BanGhiNghiepVuDTO.class));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Security & AC3: User A cố tình gửi POST sửa /khach-hang?id=5 (khách của B) bị chặn HTTP 403")
    void testKhachHang_NhanVienA_SuaKhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("action")).thenReturn("sua");
        when(request.getParameter("tieuDe")).thenReturn("Sửa trộm dữ liệu");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối thao tác sửa"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC1: Trưởng nhóm kinh doanh (NHOM) xem khách hàng của B (cùng nhóm) trả về HTTP 200")
    void testKhachHang_TruongNhom_XemKhachTrongNhom_TraVe200() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userLeadBac);
        when(request.getParameter("id")).thenReturn("5"); // Khách hàng của B trong nhóm 1

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("banGhiChiTiet"), any(BanGhiNghiepVuDTO.class));
    }

    @Test
    @DisplayName("AC1: Trưởng nhóm kinh doanh cố tình xem khách hàng của C (nhóm 2 khác) bị chặn HTTP 403")
    void testKhachHang_TruongNhom_XemKhachNhomKhac_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userLeadBac);
        when(request.getParameter("id")).thenReturn("9"); // Khách hàng của C trong nhóm 2 (Miền Nam)

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("không nằm trong phạm vi nhóm quản lý của bạn"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }
}
