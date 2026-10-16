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
import vn.nhom10.crm.dao.ThongBaoDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.ThongBao;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử ThongBaoServlet - POST /thong-bao/mo & GET /thong-bao (Story S4-07, AC3, Q5)")
class ThongBaoServletTest {

    @Mock
    private ThongBaoDAO thongBaoDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private ThongBaoServlet servlet;
    private NguoiDung teamLead;

    @BeforeEach
    void setUp() {
        servlet = new ThongBaoServlet(thongBaoDAO);

        teamLead = new NguoiDung();
        teamLead.setId(201);
        teamLead.setHoTen("Trần Thị Trưởng Nhóm");
        teamLead.setEmail("lead@msys.vn");
    }

    @Test
    @DisplayName("POST /thong-bao/mo: Nhấn thông báo SLA -> đánh dấu đã đọc và redirect đến lead (Q5)")
    void testMoThongBao_ThanhCong_DanhDauDaDocVaRedirect() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(teamLead);
        when(request.getServletPath()).thenReturn("/thong-bao/mo");
        when(request.getParameter("thongBaoId")).thenReturn("7101");
        when(request.getContextPath()).thenReturn("/crm");

        ThongBao tb = new ThongBao();
        tb.setId(7101L);
        tb.setNguoiDungId(201L); // khớp recipient
        tb.setDuongDanMo("/lead/chi-tiet?leadId=1001");
        tb.setDaDoc(false);

        when(thongBaoDAO.timTheoId(7101L)).thenReturn(tb);

        servlet.doPost(request, response);

        verify(thongBaoDAO).danhDauDaDoc(7101L, 201L);
        verify(response).sendRedirect("/crm/lead/chi-tiet?leadId=1001");
    }

    @Test
    @DisplayName("POST /thong-bao/mo: Chống IDOR - Người khác mở thông báo của Trưởng nhóm -> 403 Forbidden")
    void testMoThongBao_ChongIDOR_403() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        NguoiDung userKhac = new NguoiDung();
        userKhac.setId(999);
        when(session.getAttribute("nguoiDung")).thenReturn(userKhac);

        when(request.getServletPath()).thenReturn("/thong-bao/mo");
        when(request.getParameter("thongBaoId")).thenReturn("7101");

        ThongBao tb = new ThongBao();
        tb.setId(7101L);
        tb.setNguoiDungId(201L); // thuộc người dùng 201

        when(thongBaoDAO.timTheoId(7101L)).thenReturn(tb);

        servlet.doPost(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(thongBaoDAO, never()).danhDauDaDoc(anyLong(), anyLong());
    }

    @Test
    @DisplayName("GET /thong-bao: Lấy danh sách thông báo và số lượng chưa đọc (Q5 badge đỏ)")
    void testDoGet_LayDanhSachThongBao_ThanhCong() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(teamLead);
        when(thongBaoDAO.layDanhSachTheoNguoiDung(201L, false, 50)).thenReturn(Collections.emptyList());
        when(thongBaoDAO.demThongBaoChuaDoc(201L)).thenReturn(2);
        when(request.getRequestDispatcher("/WEB-INF/views/common/thong-bao.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("soThongBaoChuaDoc"), eq(2));
        verify(dispatcher).forward(request, response);
    }
}
