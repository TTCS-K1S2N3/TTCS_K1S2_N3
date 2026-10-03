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
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.DanhMucBanHangService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử DanhMucBanHangServlet Controller (Story S2-07)")
class DanhMucBanHangServletTest {

    @Mock
    private DanhMucBanHangService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private DanhMucBanHangServlet servlet;

    private NguoiDung directorUser;
    private NguoiDung adminUser;
    private NguoiDung salesUser;

    @BeforeEach
    void setUp() {
        servlet = new DanhMucBanHangServlet();
        servlet.setService(service);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        directorUser = new NguoiDung(10, "Bàn Thị Linh", "director@crm.vn");
        directorUser.themVaiTro(VaiTroEnum.DIRECTOR);
        directorUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);

        adminUser = new NguoiDung(1, "Quản Trị Viên", "admin@crm.vn");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);
        adminUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);

        salesUser = new NguoiDung(4, "Nhân Viên Sale", "sales@crm.vn");
        salesUser.themVaiTro(VaiTroEnum.SALES_REP);
        salesUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
    }

    @Test
    @DisplayName("Quyền truy cập: Người dùng chưa đăng nhập bị chuyển hướng đến /dang-nhap")
    void testDoGet_ChuaDangNhap_ChuyenHuongLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("Quyền truy cập: Vai trò SALES_REP không có quyền truy cập bị 403 Forbidden")
    void testDoGet_SalesUser_BiCam403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Quyền truy cập: SALES_REP gửi request POST bị chặn 403 Forbidden")
    void testDoPost_SalesUser_BiCam403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).getRequestDispatcher("/WEB-INF/views/common/403.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Loại danh mục: GET với tham số loại không hợp lệ bị từ chối 400 Bad Request")
    void testDoGet_InvalidCategory_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("loai")).thenReturn("INVALID_TRASH_CATEGORY");

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Loại danh mục không hợp lệ"));
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("Loại danh mục: POST với loại danh mục không hợp lệ bị từ chối 400 Bad Request")
    void testDoPost_InvalidCategory_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("loaiDanhMuc")).thenReturn("UNKNOWN_CATEGORY");

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Loại danh mục không hợp lệ"));
    }

    @Test
    @DisplayName("Loại danh mục: GET không truyền loai (null) mặc định hiển thị NGANH_NGHE")
    void testDoGet_LoaiNull_MacDinhNganhNghe() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("loai")).thenReturn(null);
        when(service.timKiem(eq(LoaiDanhMuc.NGANH_NGHE), any())).thenReturn(Collections.emptyList());
        when(service.tinhThongKe(eq(LoaiDanhMuc.NGANH_NGHE))).thenReturn(new long[]{0, 0, 0});

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("loaiHienTai"), eq(LoaiDanhMuc.NGANH_NGHE));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Quyền truy cập: Giám đốc kinh doanh (DIRECTOR) truy cập thành công")
    void testDoGet_Director_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("loai")).thenReturn("NGANH_NGHE");
        when(service.timKiem(eq(LoaiDanhMuc.NGANH_NGHE), any())).thenReturn(Collections.emptyList());
        when(service.tinhThongKe(eq(LoaiDanhMuc.NGANH_NGHE))).thenReturn(new long[]{0, 0, 0});

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("loaiHienTai"), eq(LoaiDanhMuc.NGANH_NGHE));
        verify(request).getRequestDispatcher("/WEB-INF/views/danh-muc/quan-ly-danh-muc.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Quyền truy cập: Quản trị viên (ADMIN) truy cập thành công")
    void testDoGet_Admin_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(adminUser);
        when(request.getParameter("loai")).thenReturn("QUY_MO");
        when(service.timKiem(eq(LoaiDanhMuc.QUY_MO), any())).thenReturn(Collections.emptyList());
        when(service.tinhThongKe(eq(LoaiDanhMuc.QUY_MO))).thenReturn(new long[]{5, 5, 12});

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("loaiHienTai"), eq(LoaiDanhMuc.QUY_MO));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST action=them: Thêm mới mục danh mục thành công")
    void testDoPost_ThemMuc() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("loaiDanhMuc")).thenReturn("NGUON_LEAD");
        when(request.getParameter("maMuc")).thenReturn("EVENT_2026");
        when(request.getParameter("tenMuc")).thenReturn("Sự kiện triển lãm 2026");
        when(request.getParameter("moTa")).thenReturn("Khách từ sự kiện");
        when(request.getParameter("kichHoat")).thenReturn("true");

        servlet.doPost(request, response);

        verify(service).themMuc(any(MucDanhMucDTO.class));
        verify(session).setAttribute(eq("thongBaoThanhCong"), contains("Đã thêm mới"));
        verify(response).sendRedirect("/crm/danh-muc-ban-hang?loai=NGUON_LEAD");
    }

    @Test
    @DisplayName("POST action=sua: Cập nhật mục danh mục thành công")
    void testDoPost_SuaMuc() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("sua");
        when(request.getParameter("loaiDanhMuc")).thenReturn("NGANH_NGHE");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("tenMuc")).thenReturn("Công nghệ tài chính (Fintech)");
        when(request.getParameter("moTa")).thenReturn("Mô tả mới");
        when(request.getParameter("kichHoat")).thenReturn("true");

        MucDanhMucDTO itemHienTai = new MucDanhMucDTO();
        itemHienTai.setId(5L);
        itemHienTai.setLoaiDanhMuc(LoaiDanhMuc.NGANH_NGHE);
        when(service.layTheoId(LoaiDanhMuc.NGANH_NGHE, 5L)).thenReturn(itemHienTai);

        servlet.doPost(request, response);

        verify(service).capNhatMuc(itemHienTai);
        verify(session).setAttribute(eq("thongBaoThanhCong"), contains("Đã cập nhật"));
        verify(response).sendRedirect("/crm/danh-muc-ban-hang?loai=NGANH_NGHE");
    }

    @Test
    @DisplayName("POST action=xoa: Xóa thành công khi mục không có tham chiếu")
    void testDoPost_XoaMuc_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("xoa");
        when(request.getParameter("loaiDanhMuc")).thenReturn("LOAI_HOAT_DONG");
        when(request.getParameter("id")).thenReturn("12");

        when(service.xoaMuc(LoaiDanhMuc.LOAI_HOAT_DONG, 12L)).thenReturn(true);

        servlet.doPost(request, response);

        verify(service).xoaMuc(LoaiDanhMuc.LOAI_HOAT_DONG, 12L);
        verify(session).setAttribute(eq("thongBaoThanhCong"), contains("Đã xóa"));
        verify(response).sendRedirect("/crm/danh-muc-ban-hang?loai=LOAI_HOAT_DONG");
    }

    @Test
    @DisplayName("POST action=xoa: Thất bại và hiển thị thông báo lỗi khi mục đang có tham chiếu (AC2)")
    void testDoPost_XoaMuc_LoiThamChieu_AC2() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("xoa");
        when(request.getParameter("loaiDanhMuc")).thenReturn("NGANH_NGHE");
        when(request.getParameter("id")).thenReturn("1");

        doThrow(new IllegalStateException("Không thể xóa mục vì đang có 10 bản ghi nghiệp vụ đang tham chiếu."))
                .when(service).xoaMuc(LoaiDanhMuc.NGANH_NGHE, 1L);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("thongBaoLoi"), contains("Không thể xóa mục vì đang có 10 bản ghi nghiệp vụ"));
        verify(response).sendRedirect("/crm/danh-muc-ban-hang?loai=NGANH_NGHE");
    }

    @Test
    @DisplayName("POST action=doi-thu-tu: Hoán đổi thứ tự hiển thị thành công (AC3)")
    void testDoPost_DoiThuTu_AC3() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("doi-thu-tu");
        when(request.getParameter("loaiDanhMuc")).thenReturn("QUY_MO");
        when(request.getParameter("id")).thenReturn("3");
        when(request.getParameter("huong")).thenReturn("len");

        servlet.doPost(request, response);

        verify(service).thayDoiThuTu(LoaiDanhMuc.QUY_MO, 3L, true);
        verify(session).setAttribute(eq("thongBaoThanhCong"), contains("Đã thay đổi thứ tự"));
        verify(response).sendRedirect("/crm/danh-muc-ban-hang?loai=QUY_MO");
    }

    @Test
    @DisplayName("POST action=chuyen-trang-thai: Đổi trạng thái kích hoạt thành công")
    void testDoPost_ChuyenTrangThai() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("chuyen-trang-thai");
        when(request.getParameter("loaiDanhMuc")).thenReturn("LOAI_HOAT_DONG");
        when(request.getParameter("id")).thenReturn("2");

        servlet.doPost(request, response);

        verify(service).chuyenTrangThaiKichHoat(LoaiDanhMuc.LOAI_HOAT_DONG, 2L);
        verify(response).sendRedirect("/crm/danh-muc-ban-hang?loai=LOAI_HOAT_DONG");
    }
}
