package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import vn.nhom10.crm.dto.KetQuaSanPhamDTO;
import vn.nhom10.crm.dto.PhanTrangDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.SanPhamService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử tầng Controller cho SanPhamServlet.
 */
public class SanPhamServletTest {

    private SanPhamServlet servlet;
    private SanPhamService serviceMock;

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung directorUser;
    private NguoiDung salesRepUser;

    @BeforeEach
    public void setUp() {
        servlet = new SanPhamServlet();
        serviceMock = Mockito.mock(SanPhamService.class);
        servlet.setSanPhamService(serviceMock);

        req = Mockito.mock(HttpServletRequest.class);
        resp = Mockito.mock(HttpServletResponse.class);
        session = Mockito.mock(HttpSession.class);
        dispatcher = Mockito.mock(RequestDispatcher.class);

        when(req.getSession()).thenReturn(session);
        when(req.getSession(false)).thenReturn(session);
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        directorUser = new NguoiDung(1, "Giám đốc", "director@crm.vn");
        directorUser.themVaiTro(VaiTroEnum.DIRECTOR);

        salesRepUser = new NguoiDung(2, "Sales Rep", "sales@crm.vn");
        salesRepUser.themVaiTro(VaiTroEnum.SALES_REP);
    }

    @Test
    public void testDoGetDanhSachSanPham() throws Exception {
        when(req.getServletPath()).thenReturn("/san-pham");
        when(session.getAttribute(SanPhamServlet.SESSION_USER)).thenReturn(directorUser);

        PhanTrangDTO<SanPham> phanTrang = new PhanTrangDTO<>(Collections.emptyList(), 1, 20, 0);
        when(serviceMock.layDanhSachSanPham(any(), any(), any(), eq(1), eq(20), eq(directorUser)))
                .thenReturn(phanTrang);
        when(serviceMock.coQuyenGiaVon(directorUser)).thenReturn(true);
        when(serviceMock.coQuyenQuanLy(directorUser)).thenReturn(true);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("phanTrang"), eq(phanTrang));
        verify(req).setAttribute(eq("coQuyenGiaVon"), eq(true));
        verify(req).setAttribute(eq("coQuyenQuanLy"), eq(true));
        verify(req).getRequestDispatcher("/WEB-INF/views/san-pham/danh-sach.jsp");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    public void testDoGetFormTaoChanSalesRep() throws Exception {
        when(req.getServletPath()).thenReturn("/san-pham/tao");
        when(session.getAttribute(SanPhamServlet.SESSION_USER)).thenReturn(salesRepUser);
        when(serviceMock.coQuyenQuanLy(salesRepUser)).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thêm mới sản phẩm bảng giá.");
        verify(dispatcher, never()).forward(req, resp);
    }

    @Test
    public void testDoGetKiemTraGiaSanApiCanDuyet() throws Exception {
        when(req.getServletPath()).thenReturn("/san-pham/kiem-tra-gia-san");
        when(req.getParameter("id")).thenReturn("1");
        when(req.getParameter("donGia")).thenReturn("700000");

        SanPham sp = new SanPham();
        sp.setId(1);
        sp.setGiaNiemYet(new BigDecimal("1000000.00"));
        sp.setGiaSan(new BigDecimal("800000.00")); // Giá sàn 800,000 > Đơn giá 700,000

        when(serviceMock.layChiTietSanPham(1, null)).thenReturn(sp);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String responseJson = sw.toString();
        assertTrue(responseJson.contains("\"canDuyetChiMon\":true"));
        assertTrue(responseJson.contains("thấp hơn Giá sàn"));
    }

    @Test
    public void testDoPostXoaSanPhamBiChanDoTrongBaoGia() throws Exception {
        when(req.getServletPath()).thenReturn("/san-pham/xoa");
        when(session.getAttribute(SanPhamServlet.SESSION_USER)).thenReturn(directorUser);
        when(serviceMock.coQuyenQuanLy(directorUser)).thenReturn(true);
        when(req.getParameter("id")).thenReturn("5");
        when(req.getContextPath()).thenReturn("/crm");

        KetQuaSanPhamDTO loiBaoGia = KetQuaSanPhamDTO.loiDaCoTrongBaoGia("Sản phẩm mẫu");
        when(serviceMock.xoaSanPham(5, directorUser)).thenReturn(loiBaoGia);

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("thongBaoLoi"), contains("đã xuất hiện trong báo giá"));
        verify(resp).sendRedirect("/crm/san-pham");
    }
}
