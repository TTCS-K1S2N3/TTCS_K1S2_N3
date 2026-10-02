package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.KetQuaKiemTraDongCoHoiDTO;
import vn.nhom10.crm.model.LyDoThangThua;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LyDoThangThuaService;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử LyDoThangThuaServlet (Story S2-10)")
class LyDoThangThuaServletTest {

    private LyDoThangThuaService service;
    private LyDoThangThuaServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung directorUser;
    private NguoiDung salesUser;

    @BeforeEach
    void setUp() {
        service = mock(LyDoThangThuaService.class);
        servlet = new LyDoThangThuaServlet(service);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestURI()).thenReturn("/crm/danh-muc/ly-do-thang-thua");
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Director
        directorUser = new NguoiDung();
        directorUser.setId(1L);
        directorUser.setHoTen("Giám Đốc Kinh Doanh");
        directorUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        directorUser.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.DIRECTOR)));

        // Sales Rep
        salesUser = new NguoiDung();
        salesUser.setId(2L);
        salesUser.setHoTen("Nhân Viên Sales");
        salesUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        salesUser.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP)));
    }

    @Test
    @DisplayName("doGet chưa đăng nhập -> Chuyển hướng sang đăng nhập")
    void testDoGetChuaDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doGet với Giám đốc kinh doanh -> Nạp danh sách và forward đến JSP")
    void testDoGetGiamDocThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(service.layDanhSachLyDoThang()).thenReturn(List.of(
                new LyDoThangThua("WIN_1", "Lý do thắng 1", LyDoThangThua.LOAI_THANG, 1, true)
        ));
        when(service.layDanhSachLyDoThua()).thenReturn(List.of(
                new LyDoThangThua("LOSS_1", "Lý do thua 1", LyDoThangThua.LOAI_THUA, 1, true)
        ));
        when(service.layDanhSachDoiThu()).thenReturn(Collections.emptyList());

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("coQuyenQuanLy"), eq(true));
        verify(request).setAttribute(eq("currentTab"), anyString());
        verify(request).setAttribute(eq("dsLyDoThang"), anyList());
        verify(request).setAttribute(eq("dsLyDoThua"), anyList());
        verify(request).setAttribute(eq("dsDoiThu"), anyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost thêm lý do với Giám đốc kinh doanh -> Lưu thành công")
    void testDoPostThemLyDoDirector() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("them-ly-do");
        when(request.getParameter("maLyDo")).thenReturn("WIN_SERVICE");
        when(request.getParameter("tenLyDo")).thenReturn("Dịch vụ xuất sắc");
        when(request.getParameter("loai")).thenReturn("THANG");
        when(request.getParameter("thuTuHienThi")).thenReturn("1");
        when(request.getParameter("hoatDong")).thenReturn("1");

        servlet.doPost(request, response);

        verify(service).kiemTraQuyenQuanLy(directorUser);
        verify(service).luuLyDo(any(LyDoThangThua.class));
        verify(response).sendRedirect(contains("/crm/danh-muc/ly-do-thang-thua?tab=thang&msg=success"));
    }

    @Test
    @DisplayName("doPost sửa lý do không có quyền -> Bị từ chối")
    void testDoPostTuChoiKhiKhongCoQuyen() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);
        when(request.getParameter("action")).thenReturn("sua-ly-do");
        doThrow(new LoiPhanQuyenException("Chỉ Giám đốc kinh doanh mới có quyền")).when(service).kiemTraQuyenQuanLy(salesUser);

        servlet.doPost(request, response);

        verify(service, never()).luuLyDo(any());
        verify(response).sendRedirect(contains("msg=error"));
    }

    @Test
    @DisplayName("doPost kiểm tra đóng cơ hội Sprint 5 (AC3) -> Trả về JSON")
    void testDoPostKiemTraSprint5() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(salesUser);
        when(request.getParameter("action")).thenReturn("kiem-tra-dong-co-hoi");
        when(request.getParameter("trangThaiDong")).thenReturn("THANG");
        when(request.getParameter("lyDoThangThuaId")).thenReturn("10");
        when(request.getParameter("giaTriChotThucTe")).thenReturn("100000000");
        when(request.getParameter("ngayKy")).thenReturn("2026-10-02");

        KetQuaKiemTraDongCoHoiDTO kqMock = new KetQuaKiemTraDongCoHoiDTO();
        kqMock.setHopLe(true);
        kqMock.setThongBaoChiTiet("HỢP LỆ THEO SPRINT 5");
        when(service.kiemTraDongCoHoiSprint5(any(), any(), any(), any(), any(), any()))
                .thenReturn(kqMock);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doPost(request, response);

        pw.flush();
        String json = sw.toString();
        assertTrue(json.contains("\"hopLe\":true"));
        assertTrue(json.contains("HỢP LỆ THEO SPRINT 5"));
    }

    @Test
    @DisplayName("doPost nạp dữ liệu mẫu với Giám đốc kinh doanh -> Nạp thành công")
    void testDoPostNapDuLieuMauDirector() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(directorUser);
        when(request.getParameter("action")).thenReturn("nap-du-lieu-mau");
        when(request.getParameter("tab")).thenReturn("thang");
        when(service.napDuLieuMauNeuTrong()).thenReturn(12);

        servlet.doPost(request, response);

        verify(service).kiemTraQuyenQuanLy(directorUser);
        verify(service).napDuLieuMauNeuTrong();
        verify(response).sendRedirect(contains("msg=success"));
    }
}
