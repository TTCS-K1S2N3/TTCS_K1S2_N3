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
import vn.nhom10.crm.dto.DanhMucLyDoTuChoiDTO;
import vn.nhom10.crm.dto.LeadChiTietViewDTO;
import vn.nhom10.crm.dto.XuLyLeadRequestDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.LeadTiepNhanService;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử LeadXuLyServlet - GET /lead/chi-tiet & POST /lead/xu-ly (Story S4-07)")
class LeadXuLyServletTest {

    @Mock
    private LeadTiepNhanService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private LeadXuLyServlet servlet;
    private NguoiDung user;

    @BeforeEach
    void setUp() {
        servlet = new LeadXuLyServlet(service);

        user = new NguoiDung();
        user.setId(101);
        user.setHoTen("Nguyễn Văn Sales");
        user.setEmail("sales1@msys.vn");
        VaiTro vt = new VaiTro();
        vt.setMaVaiTro("SALES_REP");
        user.setDanhSachVaiTro(Collections.singleton(vt));
    }

    // =========================================================================
    // GET /lead/chi-tiet
    // =========================================================================

    @Test
    @DisplayName("GET /lead/chi-tiet: Chưa đăng nhập -> redirect /dang-nhap")
    void testDoGet_ChuaDangNhap_RedirectLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("GET /lead/chi-tiet: Thiếu leadId -> 400 Bad Request")
    void testDoGet_ThieuLeadId_400() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getParameter("leadId")).thenReturn(null);
        when(request.getParameter("id")).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("GET /lead/chi-tiet: Hợp lệ -> set attributes và forward chi-tiet-lead.jsp")
    void testDoGet_ThanhCong_ForwardJsp() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getParameter("leadId")).thenReturn("1001");

        Lead lead = new Lead();
        lead.setId(1001L);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);
        lead.setNguoiPhuTrachId(101L);

        LeadChiTietViewDTO viewDTO = new LeadChiTietViewDTO();
        viewDTO.setLead(lead);
        viewDTO.setCoTheNhan(true);
        viewDTO.setCoTheTuChoi(true);
        viewDTO.setTrangThaiSla(LeadChiTietViewDTO.TrangThaiSlaEnum.DUNG_HAN);
        viewDTO.setDanhMucLyDoTuChoi(DanhMucLyDoTuChoiDTO.layDanhMucMacDinh());
        viewDTO.setSoThongBaoChuaDoc(2);

        when(service.layChiTietLeadChoNguoiDung(1001L, user)).thenReturn(viewDTO);
        when(request.getRequestDispatcher("/WEB-INF/views/lead/chi-tiet-lead.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("leadChiTiet"), eq(lead));
        verify(request).setAttribute(eq("coTheNhan"), eq(true));
        verify(request).setAttribute(eq("coTheTuChoi"), eq(true));
        verify(request).setAttribute(eq("coTheChuyenDoi"), eq(false));
        verify(request).setAttribute(eq("trangThaiSla"), eq("DUNG_HAN"));
        verify(request).setAttribute(eq("soThongBaoChuaDoc"), eq(2));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("GET /lead/chi-tiet: Lead không tồn tại -> 404 Not Found")
    void testDoGet_LeadKhongTonTai_404() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getParameter("leadId")).thenReturn("9999");
        when(service.layChiTietLeadChoNguoiDung(9999L, user)).thenThrow(new LoiKhongTimThayException("Lead không tồn tại"));

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }

    @Test
    @DisplayName("GET /lead/chi-tiet: Không có quyền truy cập -> 403 Forbidden")
    void testDoGet_KhongCoQuyen_403() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getParameter("leadId")).thenReturn("1001");
        when(service.layChiTietLeadChoNguoiDung(1001L, user)).thenThrow(new LoiPhanQuyenException("Không có quyền"));

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // =========================================================================
    // POST /lead/xu-ly
    // =========================================================================

    @Test
    @DisplayName("POST /lead/xu-ly: Nhận lead (NHAN) thành công -> PRG redirect /lead/chi-tiet")
    void testDoPost_NhanLead_ThanhCong_PRG() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/crm");

        when(request.getParameter("leadId")).thenReturn("1001");
        when(request.getParameter("hanhDong")).thenReturn("NHAN");

        servlet.doPost(request, response);

        verify(service).nhanLead(1001L, user);
        verify(session).setAttribute("flashThanhCong", "Đã nhận lead thành công");
        verify(response).sendRedirect("/crm/lead/chi-tiet?leadId=1001");
    }

    @Test
    @DisplayName("POST /lead/xu-ly: Từ chối lead (TU_CHOI) thành công -> PRG redirect /lead")
    void testDoPost_TuChoiLead_ThanhCong_PRG() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/crm");

        when(request.getParameter("leadId")).thenReturn("1001");
        when(request.getParameter("hanhDong")).thenReturn("TU_CHOI");
        when(request.getParameter("maLyDoTuChoi")).thenReturn("SAI_KHU_VUC");
        when(request.getParameter("lyDoTuChoi")).thenReturn("Không thuộc khu vực của tôi");

        servlet.doPost(request, response);

        verify(service).tuChoiLead(any(XuLyLeadRequestDTO.class), eq(user));
        verify(session).setAttribute("flashThanhCong", "Lead đã được trả về hàng chờ phân bổ");
        verify(response).sendRedirect("/crm/lead");
    }

    @Test
    @DisplayName("POST /lead/xu-ly: Từ chối thiếu lý do -> 400 Bad Request và forward lại JSP kèm lỗi")
    void testDoPost_TuChoiThieuLyDo_400() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);

        when(request.getParameter("leadId")).thenReturn("1001");
        when(request.getParameter("hanhDong")).thenReturn("TU_CHOI");
        when(request.getParameter("maLyDoTuChoi")).thenReturn("SAI_KHU_VUC");
        when(request.getParameter("lyDoTuChoi")).thenReturn(""); // thiếu văn bản giải thích

        LeadChiTietViewDTO viewDTO = new LeadChiTietViewDTO();
        viewDTO.setLead(new Lead());
        when(service.layChiTietLeadChoNguoiDung(1001L, user)).thenReturn(viewDTO);
        when(request.getRequestDispatcher("/WEB-INF/views/lead/chi-tiet-lead.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), anyString());
        verify(dispatcher).forward(request, response);
        verify(service, never()).tuChoiLead(any(), any());
    }

    @Test
    @DisplayName("POST /lead/xu-ly: Từ chối sau khi đã nhận (Q3) -> 409 Conflict")
    void testDoPost_TuChoiSauKhiNhan_409Conflict() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);

        when(request.getParameter("leadId")).thenReturn("1001");
        when(request.getParameter("hanhDong")).thenReturn("TU_CHOI");
        when(request.getParameter("maLyDoTuChoi")).thenReturn("KHONG_PHU_HOP");
        when(request.getParameter("lyDoTuChoi")).thenReturn("Không phù hợp");

        doThrow(new IllegalStateException("Không được từ chối sau khi đã nhận lead"))
                .when(service).tuChoiLead(any(XuLyLeadRequestDTO.class), eq(user));

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_CONFLICT), contains("Không được từ chối sau khi đã nhận"));
    }

    @Test
    @DisplayName("POST /lead/xu-ly: Chưa đăng nhập -> 401 Unauthorized")
    void testDoPost_ChuaDangNhap_401() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
    }

    @Test
    @DisplayName("GET /lead/chi-tiet: Lead đủ điều kiện chuyển đổi -> set coTheChuyenDoi true")
    void testDoGet_CoTheChuyenDoi_SetAttribute() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(user);
        when(request.getParameter("leadId")).thenReturn("1002");

        Lead lead = new Lead();
        lead.setId(1002L);
        lead.setTrangThai(TrangThaiLeadEnum.DANG_CHAM_SOC);

        LeadChiTietViewDTO viewDTO = new LeadChiTietViewDTO();
        viewDTO.setLead(lead);
        viewDTO.setCoTheChuyenDoi(true);
        viewDTO.setTrangThaiSla(LeadChiTietViewDTO.TrangThaiSlaEnum.DUNG_HAN);

        when(service.layChiTietLeadChoNguoiDung(1002L, user)).thenReturn(viewDTO);
        when(request.getRequestDispatcher("/WEB-INF/views/lead/chi-tiet-lead.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("coTheChuyenDoi"), eq(true));
        verify(dispatcher).forward(request, response);
    }
}
