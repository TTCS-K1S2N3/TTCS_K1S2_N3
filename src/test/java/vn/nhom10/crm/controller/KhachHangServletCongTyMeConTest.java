package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.service.CongTyMeConService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử tầng Controller cho KhachHangServlet với các chức năng của Story S3-05:
 * - GET xem chi tiết công ty mẹ & tổng giá trị hợp đồng nhóm công ty
 * - POST gắn công ty con
 * - POST gỡ công ty con
 * - POST cập nhật công ty mẹ
 * - Xử lý các mã lỗi HTTP chuẩn (400, 403, 404, 200).
 */
public class KhachHangServletCongTyMeConTest {

    private PhanQuyenDuLieuService phanQuyenService;
    private CongTyMeConService congTyMeConService;
    private KhachHangServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung userNhanVien;
    private BanGhiNghiepVuDTO banGhiMe;

    @BeforeEach
    void setUp() {
        phanQuyenService = mock(PhanQuyenDuLieuService.class);
        congTyMeConService = mock(CongTyMeConService.class);
        servlet = new KhachHangServlet(phanQuyenService, congTyMeConService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        userNhanVien = new NguoiDung(101L, "Nguyễn Văn Sales", "sales@crm.vn");
        userNhanVien.setTrangThai("HOAT_DONG");
        userNhanVien.setNhomKinhDoanhId(1);
        when(session.getAttribute("nguoiDung")).thenReturn(userNhanVien);

        banGhiMe = new BanGhiNghiepVuDTO(10L, "KH-ME", "Tập Đoàn Mẹ",
                BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG, 101L, "Nguyễn Văn Sales", 1L, "Nhóm 1",
                "1000000000", "Khách hàng", null, "Mô tả công ty mẹ");
    }

    @Test
    @DisplayName("AC2: GET /khach-hang/chi-tiet?id=10 hiển thị trang chi tiết kèm thống kê giá trị nhóm công ty")
    void testGetChiTietKhachHang_HienThiThongKeNhomCongTy() throws Exception {
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("id")).thenReturn("10");

        when(phanQuyenService.timBanGhiTheoId(10L, "KHACH_HANG")).thenReturn(banGhiMe);
        when(phanQuyenService.kiemTraQuyenTruyCap(any(NguoiDungDTO.class), eq(banGhiMe)))
                .thenReturn(new PhanQuyenDuLieuService.KetQuaKiemTra(true, "Có quyền", banGhiMe));

        ThongKeNhomCongTyDTO thongKeMock = new ThongKeNhomCongTyDTO(new KhachHang(10L, "Tập Đoàn Mẹ", 101L));
        thongKeMock.setTongGiaTriHopDongNhomCongTy(new BigDecimal("1500000000.00"));
        when(congTyMeConService.layThongKeNhomCongTy(10L)).thenReturn(thongKeMock);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("thongKeNhomCongTy"), eq(thongKeMock));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC1: POST action=gan-cong-ty-con hợp lệ gọi service và chuyển hướng thành công")
    void testPostGanCongTyCon_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("gan-cong-ty-con");
        when(request.getParameter("congTyMeId")).thenReturn("10");
        when(request.getParameter("congTyConId")).thenReturn("20");

        servlet.doPost(request, response);

        verify(congTyMeConService).ganCongTyCon(eq(20L), eq(10L), any(NguoiDungDTO.class));
        verify(session).setAttribute(eq("flashThanhCong"), contains("Gắn khách hàng làm công ty con thành công"));
        verify(response).sendRedirect("/crm/khach-hang/chi-tiet?id=10");
    }

    @Test
    @DisplayName("AC1 Validation: POST action=gan-cong-ty-con gây vòng lặp trả về HTTP 400 Bad Request")
    void testPostGanCongTyCon_VongLap_TraVe400() throws Exception {
        when(request.getParameter("action")).thenReturn("gan-cong-ty-con");
        when(request.getParameter("congTyMeId")).thenReturn("10");
        when(request.getParameter("congTyConId")).thenReturn("20");

        doThrow(new IllegalArgumentException("Không thể gán quan hệ vì sẽ tạo ra vòng lặp chu kỳ"))
                .when(congTyMeConService).ganCongTyCon(eq(20L), eq(10L), any(NguoiDungDTO.class));
        when(phanQuyenService.timBanGhiTheoId(10L, "KHACH_HANG")).thenReturn(banGhiMe);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("vòng lặp chu kỳ"));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC1: POST action=go-cong-ty-con gỡ bỏ quan hệ thành công và chuyển hướng")
    void testPostGoCongTyCon_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("go-cong-ty-con");
        when(request.getParameter("congTyConId")).thenReturn("20");
        when(request.getParameter("congTyMeId")).thenReturn("10");

        servlet.doPost(request, response);

        verify(congTyMeConService).goCongTyCon(eq(20L), any(NguoiDungDTO.class));
        verify(session).setAttribute(eq("flashThanhCong"), contains("gỡ bỏ quan hệ công ty con thành công"));
        verify(response).sendRedirect("/crm/khach-hang/chi-tiet?id=10");
    }

    @Test
    @DisplayName("AC1: POST action=cap-nhat-cong-ty-me gán mẹ thành công")
    void testPostCapNhatCongTyMe_ThanhCong() throws Exception {
        when(request.getParameter("action")).thenReturn("cap-nhat-cong-ty-me");
        when(request.getParameter("khachHangId")).thenReturn("20");
        when(request.getParameter("congTyMeId")).thenReturn("10");

        servlet.doPost(request, response);

        verify(congTyMeConService).ganCongTyCon(eq(20L), eq(10L), any(NguoiDungDTO.class));
        verify(session).setAttribute(eq("flashThanhCong"), contains("Cập nhật công ty mẹ thành công"));
        verify(response).sendRedirect("/crm/khach-hang/chi-tiet?id=20");
    }

    @Test
    @DisplayName("AC1: POST action=cap-nhat-cong-ty-me với ID mẹ là 0 thì chuyển thành gỡ công ty mẹ")
    void testPostCapNhatCongTyMe_GiaiPhongDocLap() throws Exception {
        when(request.getParameter("action")).thenReturn("cap-nhat-cong-ty-me");
        when(request.getParameter("khachHangId")).thenReturn("20");
        when(request.getParameter("congTyMeId")).thenReturn("0"); // Độc lập

        servlet.doPost(request, response);

        verify(congTyMeConService).goCongTyCon(eq(20L), any(NguoiDungDTO.class));
        verify(session).setAttribute(eq("flashThanhCong"), contains("hủy liên kết công ty mẹ"));
        verify(response).sendRedirect("/crm/khach-hang/chi-tiet?id=20");
    }
}
