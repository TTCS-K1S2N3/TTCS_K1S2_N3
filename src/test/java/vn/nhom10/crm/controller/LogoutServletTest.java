package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.util.PasswordUtil;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử LogoutServlet - S1-02-AC2")
class LogoutServletTest {

    @Mock
    private PhienDangNhapDAO phienDangNhapDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private LogoutServlet logoutServlet;

    @BeforeEach
    void setUp() {
        logoutServlet = new LogoutServlet();
        logoutServlet.setPhienDangNhapDAO(phienDangNhapDAO);
        lenient().when(request.getContextPath()).thenReturn("/crm");
    }

    @Test
    @DisplayName("S1-02-AC2: doGet hủy phiên ngay lập tức phía server và thu hồi phiên trong database")
    void testDoGet_DangXuat_MatHieuLucPhienVaThuHoiDB() throws ServletException, IOException {
        String sessionId = "active-session-id-123";
        String expectedHash = PasswordUtil.sha256Hex(sessionId);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("maPhienHash")).thenReturn(expectedHash);

        logoutServlet.doGet(request, response);

        // Thu hồi bản ghi phiên trong database
        verify(phienDangNhapDAO).thuHoiPhien(eq(expectedHash), eq("Đăng xuất chủ động"));
        // Hủy phiên ngay lập tức trên server
        verify(session).invalidate();
        // Chuyển hướng tới login kèm param logout=1
        verify(response).sendRedirect("/crm/login?logout=1");
    }

    @Test
    @DisplayName("S1-02-AC2: doPost cũng thực hiện thu hồi và hủy phiên phía server")
    void testDoPost_DangXuat_MatHieuLucPhien() throws ServletException, IOException {
        String sessionId = "active-session-id-post";
        String expectedHash = PasswordUtil.sha256Hex(sessionId);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("maPhienHash")).thenReturn(expectedHash);

        logoutServlet.doPost(request, response);

        verify(phienDangNhapDAO).thuHoiPhien(eq(expectedHash), eq("Đăng xuất chủ động"));
        verify(session).invalidate();
        verify(response).sendRedirect("/crm/login?logout=1");
    }

    @Test
    @DisplayName("Đăng xuất khi không có session vẫn chuyển hướng an toàn về login?logout=1")
    void testDangXuat_KhongCoSession_ChuyenHuongAnToan() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);

        logoutServlet.doGet(request, response);

        verify(phienDangNhapDAO, never()).thuHoiPhien(anyString(), anyString());
        verify(response).sendRedirect("/crm/login?logout=1");
    }
}
