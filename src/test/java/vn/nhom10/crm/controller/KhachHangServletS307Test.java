package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.*;
import vn.nhom10.crm.service.BoLocKhachHangService;
import vn.nhom10.crm.service.KhachHangService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử KhachHangServlet - Tìm kiếm, lọc và lưu bộ lọc (Story S3-07)")
class KhachHangServletS307Test {

    private KhachHangServlet servlet;
    private PhanQuyenDuLieuService phanQuyenService;
    private KhachHangService khachHangService;
    private BoLocKhachHangService boLocService;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userA;

    @BeforeEach
    void setUp() {
        phanQuyenService = mock(PhanQuyenDuLieuService.class);
        khachHangService = mock(KhachHangService.class);
        boLocService = mock(BoLocKhachHangService.class);

        servlet = new KhachHangServlet(phanQuyenService, khachHangService, boLocService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        userA = new NguoiDung(101L, "Nguyễn Văn A", "sales.a@crm.vn");
        userA.setNhomKinhDoanhId(1);
        userA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(phanQuyenService.xacDinhPhamViHieuLuc(any(NguoiDungDTO.class), any()))
                .thenReturn(PhamViDuLieu.CA_NHAN);
    }

    @Test
    @DisplayName("AC1: GET /khach-hang tiếp nhận và chuyển giao đầy đủ 5 tiêu chí lọc (trạng thái, ngành nghề, quy mô, khu vực, người sở hữu)")
    void testGet_TiepNhan5TieuChiLoc() throws Exception {
        when(request.getParameter("trangThai")).thenReturn("TIEM_NANG");
        when(request.getParameter("nganhNgheId")).thenReturn("1");
        when(request.getParameter("quyMoId")).thenReturn("2");
        when(request.getParameter("khuVucId")).thenReturn("3");
        when(request.getParameter("nguoiSoHuuId")).thenReturn("101");

        servlet.doGet(request, response);

        verify(khachHangService).timKiemVaLoc(any(NguoiDungDTO.class), argThat(boLoc -> {
            assertEquals("TIEM_NANG", boLoc.getTrangThai());
            assertEquals(1L, boLoc.getNganhNgheId());
            assertEquals(2L, boLoc.getQuyMoId());
            assertEquals(3L, boLoc.getKhuVucId());
            assertEquals(101L, boLoc.getNguoiSoHuuId());
            return true;
        }));

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("boLocHienTai"), any(BoLocKhachHangDTO.class));
        verify(request).setAttribute(eq("dsTrangThai"), any());
        verify(request).setAttribute(eq("dsTrangThaiKhachHang"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC1: Nạp đầy đủ 4 trạng thái khách hàng (TIEM_NANG, DANG_GIAO_DICH, KHACH_HANG, NGUNG_HOP_TAC) vào request attribute dsTrangThai")
    void testNapDayDu4TrangThaiKhachHang() throws Exception {
        servlet.doGet(request, response);

        verify(request).setAttribute(eq("dsTrangThai"), argThat(val -> {
            assertNotNull(val);
            assertTrue(val instanceof TrangThaiKhachHangEnum[]);
            TrangThaiKhachHangEnum[] arr = (TrangThaiKhachHangEnum[]) val;
            assertEquals(4, arr.length);
            assertEquals(TrangThaiKhachHangEnum.TIEM_NANG, arr[0]);
            assertEquals(TrangThaiKhachHangEnum.DANG_GIAO_DICH, arr[1]);
            assertEquals(TrangThaiKhachHangEnum.KHACH_HANG, arr[2]);
            assertEquals(TrangThaiKhachHangEnum.NGUNG_HOP_TAC, arr[3]);
            return true;
        }));
    }

    @Test
    @DisplayName("AC2: GET /khach-hang tiếp nhận tiêu chí tìm kiếm theo tên, MST, SĐT người liên hệ")
    void testGet_TiepNhan3TieuChiTimKiem() throws Exception {
        when(request.getParameter("tenCongTy")).thenReturn("FPT Software");
        when(request.getParameter("maSoThue")).thenReturn("0101234567");
        when(request.getParameter("soDienThoai")).thenReturn("0912345678");

        servlet.doGet(request, response);

        verify(khachHangService).timKiemVaLoc(any(NguoiDungDTO.class), argThat(boLoc -> {
            assertEquals("FPT Software", boLoc.getTenCongTy());
            assertEquals("0101234567", boLoc.getMaSoThue());
            assertEquals("0912345678", boLoc.getSoDienThoai());
            return true;
        }));
    }

    @Test
    @DisplayName("AC3: GET /khach-hang?boLocId=5 tự động nạp tiêu chí từ bộ lọc đã lưu")
    void testGet_ApDungBoLocDaLuu() throws Exception {
        BoLocDaLuu daLuu = new BoLocDaLuu();
        daLuu.setId(5L);
        daLuu.setTenBoLoc("Khách cần gọi thứ 3");
        daLuu.setTieuChiJson("{\"trangThai\":\"TIEM_NANG\",\"khuVucId\":1}");

        when(request.getParameter("boLocId")).thenReturn("5");
        when(boLocService.timBoLocTheoId(5L, userA)).thenReturn(daLuu);

        servlet.doGet(request, response);

        verify(khachHangService).timKiemVaLoc(any(NguoiDungDTO.class), argThat(boLoc -> {
            assertEquals(5L, boLoc.getBoLocId());
            assertEquals("TIEM_NANG", boLoc.getTrangThai());
            assertEquals(1L, boLoc.getKhuVucId());
            return true;
        }));

        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Khách cần gọi thứ 3"));
    }

    @Test
    @DisplayName("AC3: POST action=luu-bo-loc lưu bộ lọc mới và chuyển hướng về danh sách")
    void testPost_LuuBoLoc_Redirect() throws Exception {
        when(request.getParameter("action")).thenReturn("luu-bo-loc");
        when(request.getParameter("tenBoLoc")).thenReturn("Khách IT Hà Nội");
        when(request.getParameter("trangThai")).thenReturn("TIEM_NANG");
        when(request.getParameter("nganhNgheId")).thenReturn("1");
        when(request.getParameter("macDinh")).thenReturn("1");

        BoLocDaLuu saved = new BoLocDaLuu(10L, 101L, BoLocDaLuu.LOAI_KHACH_HANG, "Khách IT Hà Nội", "{}", true);
        when(boLocService.luuBoLoc(eq(userA), eq("Khách IT Hà Nội"), any(BoLocKhachHangDTO.class), eq(true)))
                .thenReturn(saved);

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("/crm/khach-hang?boLocId=10"));
    }

    @Test
    @DisplayName("AC3: POST action=luu-bo-loc hỗ trợ AJAX trả về định dạng JSON")
    void testPost_LuuBoLoc_AjaxJson() throws Exception {
        when(request.getParameter("action")).thenReturn("luu-bo-loc");
        when(request.getParameter("format")).thenReturn("json");
        when(request.getParameter("tenBoLoc")).thenReturn("Bộ lọc nhanh");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        BoLocDaLuu saved = new BoLocDaLuu(12L, 101L, BoLocDaLuu.LOAI_KHACH_HANG, "Bộ lọc nhanh", "{}", false);
        when(boLocService.luuBoLoc(eq(userA), eq("Bộ lọc nhanh"), any(BoLocKhachHangDTO.class), eq(false)))
                .thenReturn(saved);

        servlet.doPost(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String jsonResult = sw.toString();
        assertTrue(jsonResult.contains("\"success\":true"));
        assertTrue(jsonResult.contains("\"id\":12"));
    }

    @Test
    @DisplayName("AC3: POST action=xoa-bo-loc xóa bộ lọc thành công")
    void testPost_XoaBoLoc() throws Exception {
        BoLocDaLuu boLoc = new BoLocDaLuu(8L, 101L, BoLocDaLuu.LOAI_KHACH_HANG, "Bộ lọc cần xóa", "{}", false);
        when(boLocService.timBoLocTheoId(8L, userA)).thenReturn(boLoc);
        when(request.getParameter("action")).thenReturn("xoa-bo-loc");
        when(request.getParameter("boLocId")).thenReturn("8");
        when(boLocService.xoaBoLoc(8L, userA)).thenReturn(true);

        servlet.doPost(request, response);

        verify(boLocService).xoaBoLoc(8L, userA);
        verify(response).sendRedirect(contains("/crm/khach-hang?reset=1"));
    }

    @Test
    @DisplayName("AC3: Xóa bộ lọc đang đặt mặc định, xác nhận redirect reset=1 và khi F5 không xuất hiện lại")
    void testPost_XoaBoLoc_DangDatMacDinh_SauDoGetKhongConApDung_F5KhongXuatHienLai() throws Exception {
        // 1. Giả lập bộ lọc AC3 đang được đặt làm mặc định
        BoLocDaLuu boLocMacDinh = new BoLocDaLuu(1L, 101L, BoLocDaLuu.LOAI_KHACH_HANG, "AC3 - Công nghệ Hà Nội", "{\"nganhNgheId\":1,\"khuVucId\":1}", true);
        when(boLocService.timBoLocTheoId(1L, userA)).thenReturn(boLocMacDinh);
        when(boLocService.xoaBoLoc(1L, userA)).thenReturn(true);

        // 2. Gửi request POST xóa bộ lọc
        when(request.getParameter("action")).thenReturn("xoa-bo-loc");
        when(request.getParameter("boLocId")).thenReturn("1");

        servlet.doPost(request, response);

        verify(boLocService).xoaBoLoc(1L, userA);
        verify(response).sendRedirect(argThat(url ->
                url.contains("/crm/khach-hang?reset=1") && url.contains("thongBaoThanhCong")
        ));

        // 3. Giả lập người dùng F5 hoặc truy cập lại /khach-hang sau khi xóa (bộ lọc mặc định trong DB đã biến mất)
        HttpServletRequest reqGet = mock(HttpServletRequest.class);
        HttpServletResponse respGet = mock(HttpServletResponse.class);
        RequestDispatcher rdGet = mock(RequestDispatcher.class);

        when(reqGet.getContextPath()).thenReturn("/crm");
        when(reqGet.getSession(anyBoolean())).thenReturn(session);
        when(reqGet.getRequestDispatcher(anyString())).thenReturn(rdGet);
        when(boLocService.timBoLocMacDinh(userA)).thenReturn(null); // Đã bị xóa khỏi DB

        servlet.doGet(reqGet, respGet);

        // Xác nhận bộ lọc hiện tại hoàn toàn rỗng, không tự nạp lại bộ lọc mặc định cũ
        verify(reqGet).setAttribute(eq("boLocHienTai"), argThat(val -> {
            BoLocKhachHangDTO dto = (BoLocKhachHangDTO) val;
            assertNull(dto.getBoLocId());
            assertFalse(dto.isMacDinh());
            assertFalse(dto.coDieuKienLoc());
            return true;
        }));
    }

    @Test
    @DisplayName("AC3: POST action=dat-mac-dinh thiết lập bộ lọc mặc định")
    void testPost_DatMacDinh() throws Exception {
        when(request.getParameter("action")).thenReturn("dat-mac-dinh");
        when(request.getParameter("boLocId")).thenReturn("8");
        when(boLocService.datMacDinh(8L, userA)).thenReturn(true);

        servlet.doPost(request, response);

        verify(boLocService).datMacDinh(8L, userA);
        verify(response).sendRedirect(contains("/crm/khach-hang?boLocId=8"));
    }
}
