package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import vn.nhom10.crm.dto.DieuKienRoiGiaiDoanDTO;
import vn.nhom10.crm.dto.KetQuaGiaiDoanDTO;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.GiaiDoanPipelineService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử tầng Controller cho GiaiDoanPipelineServlet.
 */
public class GiaiDoanPipelineServletTest {

    private GiaiDoanPipelineServlet servlet;
    private GiaiDoanPipelineService serviceMock;

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private NguoiDung directorUser;
    private NguoiDung salesRepUser;

    @BeforeEach
    public void setUp() {
        servlet = new GiaiDoanPipelineServlet();
        serviceMock = Mockito.mock(GiaiDoanPipelineService.class);
        servlet.setGiaiDoanPipelineService(serviceMock);

        req = Mockito.mock(HttpServletRequest.class);
        resp = Mockito.mock(HttpServletResponse.class);
        session = Mockito.mock(HttpSession.class);
        dispatcher = Mockito.mock(RequestDispatcher.class);

        when(req.getSession()).thenReturn(session);
        when(req.getSession(false)).thenReturn(session);
        when(req.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        directorUser = new NguoiDung(1, "Giám đốc", "director@crm.vn");
        directorUser.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));

        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(directorUser);

        salesRepUser = new NguoiDung(2, "Sales Rep", "sales@crm.vn");
        salesRepUser.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
    }

    @Test
    public void testDoGetDanhSachGiaiDoan() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(directorUser);
        when(serviceMock.layTatCaGiaiDoan()).thenReturn(Collections.emptyList());
        when(serviceMock.layThongKeDuBaoPipeline()).thenReturn(Collections.emptyList());
        when(serviceMock.coQuyenCauHinh(directorUser)).thenReturn(true);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("danhSachGiaiDoan"), any());
        verify(req).setAttribute(eq("coQuyenCauHinh"), eq(true));
        verify(req).getRequestDispatcher("/WEB-INF/views/co-hoi/cau-hinh-pipeline.jsp");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    public void testDoGetFormTaoChanSalesRep() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/tao");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(salesRepUser);
        when(serviceMock.coQuyenCauHinh(salesRepUser)).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thêm mới giai đoạn.");
        verify(dispatcher, never()).forward(req, resp);
    }

    @Test
    public void testDoGetApiKiemTraDieuKien() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn("2");
        when(req.getParameter("soCuocGap")).thenReturn("0");
        when(req.getParameter("soCuocGoi")).thenReturn("1");

        GiaiDoanPipeline mockStage = new GiaiDoanPipeline(2, "KHAO_SAT", "Xác định nhu cầu", 2, 20, "");
        when(serviceMock.timTheoId(2)).thenReturn(mockStage);

        DieuKienRoiGiaiDoanDTO dto = new DieuKienRoiGiaiDoanDTO(2, "Xác định nhu cầu");
        dto.themYeuCauThieu("Phải có ít nhất 1 cuộc gặp");
        when(serviceMock.kiemTraDieuKienRoiGiaiDoan(eq(2), eq(0), eq(1), eq(false), eq(false)))
                .thenReturn(dto);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":false"));
        assertTrue(json.contains("Phải có ít nhất 1 cuộc gặp"));
    }

    @Test
    public void testKhaoSatCase1FailThieuGapVaKhaoSat() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn("2");
        when(req.getParameter("soCuocGap")).thenReturn("0");
        when(req.getParameter("daKhaoSat")).thenReturn("false");

        GiaiDoanPipeline mockStage = new GiaiDoanPipeline(2, "KHAO_SAT", "Khảo sát nhu cầu thực tế", 2, 20, "");
        when(serviceMock.timTheoId(2)).thenReturn(mockStage);

        DieuKienRoiGiaiDoanDTO dto = new DieuKienRoiGiaiDoanDTO(2, "Khảo sát nhu cầu thực tế");
        dto.themYeuCauThieu("Phải có ít nhất 1 cuộc gặp trực tiếp với khách hàng");
        dto.themYeuCauThieu("Bắt buộc phải hoàn tất xác nhận bảng khảo sát nhu cầu khách hàng");
        when(serviceMock.kiemTraDieuKienRoiGiaiDoan(eq(2), eq(0), eq(0), eq(false), eq(false)))
                .thenReturn(dto);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":false"), "Case 1: meeting=0, survey=false phải trả thoaDieuKien=false");
        assertTrue(json.contains("cuộc gặp"), "Phải báo thiếu cuộc gặp");
        assertTrue(json.contains("khảo sát"), "Phải báo thiếu khảo sát");
    }

    @Test
    public void testKhaoSatCase2FailChiThieuKhaoSat() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn("2");
        when(req.getParameter("soCuocGap")).thenReturn("1");
        when(req.getParameter("daKhaoSat")).thenReturn("false");

        GiaiDoanPipeline mockStage = new GiaiDoanPipeline(2, "KHAO_SAT", "Khảo sát nhu cầu thực tế", 2, 20, "");
        when(serviceMock.timTheoId(2)).thenReturn(mockStage);

        DieuKienRoiGiaiDoanDTO dto = new DieuKienRoiGiaiDoanDTO(2, "Khảo sát nhu cầu thực tế");
        dto.themYeuCauThieu("Bắt buộc phải hoàn tất xác nhận bảng khảo sát nhu cầu khách hàng");
        when(serviceMock.kiemTraDieuKienRoiGiaiDoan(eq(2), eq(1), eq(0), eq(false), eq(false)))
                .thenReturn(dto);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":false"), "Case 2: meeting=1, survey=false phải trả thoaDieuKien=false");
        assertFalse(json.contains("cuộc gặp"), "Không được báo thiếu cuộc gặp");
        assertTrue(json.contains("khảo sát"), "Phải báo thiếu khảo sát");
    }

    @Test
    public void testKhaoSatCase3PassThoaManTatCa() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn("2");
        when(req.getParameter("soCuocGap")).thenReturn("1");
        when(req.getParameter("daKhaoSat")).thenReturn("true");

        GiaiDoanPipeline mockStage = new GiaiDoanPipeline(2, "KHAO_SAT", "Khảo sát nhu cầu thực tế", 2, 20, "");
        when(serviceMock.timTheoId(2)).thenReturn(mockStage);

        DieuKienRoiGiaiDoanDTO dto = new DieuKienRoiGiaiDoanDTO(2, "Khảo sát nhu cầu thực tế");
        dto.setThoaDieuKien(true);
        when(serviceMock.kiemTraDieuKienRoiGiaiDoan(eq(2), eq(1), eq(0), eq(false), eq(true)))
                .thenReturn(dto);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":true"), "Case 3: meeting=1, survey=true phải trả thoaDieuKien=true");
    }

    @Test
    public void testKiemTraDieuKienInvalidIdNull() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn(null);
        when(req.getParameter("id")).thenReturn(null);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":false"));
        assertTrue(json.contains("ID giai đoạn không hợp lệ."));
    }

    @Test
    public void testKiemTraDieuKienInvalidIdAbc() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn("abc");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":false"));
        assertTrue(json.contains("ID giai đoạn không hợp lệ."));
    }

    @Test
    public void testKiemTraDieuKienInvalidIdNonExistent() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/kiem-tra-dieu-kien");
        when(req.getParameter("giaiDoanId")).thenReturn("999999");

        when(serviceMock.timTheoId(999999)).thenReturn(null);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thoaDieuKien\":false"));
        assertTrue(json.contains("ID giai đoạn không hợp lệ."));
    }

    @Test
    public void testDoGetApiTinhDuBao() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/tinh-du-bao");
        when(req.getParameter("giaTri")).thenReturn("100000000");
        when(req.getParameter("xacSuat")).thenReturn("25");

        when(serviceMock.tinhDuBaoDoanhSo(any(BigDecimal.class), eq(25)))
                .thenReturn(new BigDecimal("25000000.00"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        String json = sw.toString();
        assertTrue(json.contains("\"thanhCong\":true"));
        assertTrue(json.contains("25000000.00"));
    }

    @Test
    public void testDoPostXoaGiaiDoanBiChanDoCoCoHoi() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/xoa");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(directorUser);
        when(serviceMock.coQuyenCauHinh(directorUser)).thenReturn(true);
        when(req.getParameter("id")).thenReturn("5");
        when(req.getContextPath()).thenReturn("/crm");

        KetQuaGiaiDoanDTO errorDto = KetQuaGiaiDoanDTO.loiDangCoCoHoi("Đàm phán", 4);
        when(serviceMock.xoaGiaiDoan(5, directorUser)).thenReturn(errorDto);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect(contains("error="));
    }

    @Test
    public void testDoGetFormTaoSetsFormAttributes() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/tao");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(directorUser);
        when(serviceMock.coQuyenCauHinh(directorUser)).thenReturn(true);
        when(serviceMock.layTatCaGiaiDoan()).thenReturn(Collections.emptyList());

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("danhSachLoai"), any());
        verify(req).setAttribute(eq("danhSachTrangThai"), any());
        verify(req).getRequestDispatcher("/WEB-INF/views/co-hoi/tao-giai-doan.jsp");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    public void testDoGetFormSuaSetsFormAttributes() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/sua");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(directorUser);
        when(serviceMock.coQuyenCauHinh(directorUser)).thenReturn(true);
        when(req.getParameter("id")).thenReturn("3");

        GiaiDoanPipeline gd = new GiaiDoanPipeline(3, "GD_3", "Giai đoạn 3", 3, 40, "");
        when(serviceMock.timTheoId(3)).thenReturn(gd);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("danhSachLoai"), any());
        verify(req).setAttribute(eq("danhSachTrangThai"), any());
        verify(req).setAttribute(eq("giaiDoan"), eq(gd));
        verify(req).getRequestDispatcher("/WEB-INF/views/co-hoi/sua-giai-doan.jsp");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    public void testDoGetNullSessionRedirectsToLogin() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendRedirect(contains("/dang-nhap?error=auth_required"));
        verify(dispatcher, never()).forward(req, resp);
    }

    @Test
    public void testRoleQueryParameterDoesNotBypassAuthorization() throws Exception {
        // Even if attacker passes ?role=DIRECTOR, real session user is SALES_REP
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/tao");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(salesRepUser);
        when(req.getParameter("role")).thenReturn("DIRECTOR");
        when(serviceMock.coQuyenCauHinh(salesRepUser)).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thêm mới giai đoạn.");
        verify(dispatcher, never()).forward(req, resp);
    }

    @Test
    public void testDoPostTaoGiaiDoanChanSalesRep() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/tao");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(salesRepUser);
        when(serviceMock.coQuyenCauHinh(salesRepUser)).thenReturn(false);

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
        verify(serviceMock, never()).themGiaiDoan(any(), any());
    }

    @Test
    public void testDoPostTaoGiaiDoanStrictNumberValidation() throws Exception {
        when(req.getServletPath()).thenReturn("/pipeline/giai-doan/tao");
        when(session.getAttribute(GiaiDoanPipelineServlet.SESSION_USER)).thenReturn(directorUser);
        when(serviceMock.coQuyenCauHinh(directorUser)).thenReturn(true);

        when(req.getParameter("maGiaiDoan")).thenReturn("TEST_STRICT");
        when(req.getParameter("tenGiaiDoan")).thenReturn("Test Strict");
        when(req.getParameter("thuTu")).thenReturn("abc"); // Invalid number
        when(req.getParameter("xacSuatThang")).thenReturn("xyz"); // Invalid number
        when(req.getParameter("loaiGiaiDoan")).thenReturn("FAKE_ENUM"); // Invalid enum

        servlet.doPost(req, resp);

        // Must forward back to form with validation errors, NOT save with silent defaults
        verify(serviceMock, never()).themGiaiDoan(any(), any());
        verify(req).setAttribute(eq("thongBaoLoi"), any());
        verify(dispatcher).forward(req, resp);
    }
}
