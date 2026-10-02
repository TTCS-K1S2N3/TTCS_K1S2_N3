package vn.nhom10.crm.filter;

import jakarta.servlet.FilterChain;
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
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử AuthFilter - Quản lý phiên S1-02 (AC1, AC3)")
class AuthFilterSessionTest {

    @Mock
    private PhienDangNhapDAO phienDangNhapDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private AuthFilter authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        authFilter.setPhienDangNhapDAO(phienDangNhapDAO);

        lenient().when(request.getContextPath()).thenReturn("/crm");
    }

    // =========================================================================
    // Test A: HttpSession còn tồn tại nhưng DB session đã hết hạn -> MUST redirect login timeout
    // =========================================================================
    @Test
    @DisplayName("Test A: HttpSession còn tồn tại nhưng DB session đã hết hạn -> MUST redirect login timeout và đánh dấu HET_HAN")
    void testA_HttpSessionConTonTai_NhungDBSessionHetHan_RedirectLoginTimeout() throws ServletException, IOException {
        String maPhienHash = PasswordUtil.sha256Hex("session-expired-in-db");
        NguoiDung user = new NguoiDung();
        user.setId(20L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn(maPhienHash);
        // DB trả về trạng thái HET_HAN (het_han_luc <= NOW())
        when(phienDangNhapDAO.kiemTraChiTietPhien(maPhienHash)).thenReturn(PhienDangNhapDAO.KetQuaKiemTraPhien.HET_HAN);

        authFilter.doFilter(request, response, chain);

        // Xác nhận quy tắc 4:
        // 1. Cập nhật trạng thái HET_HAN trong DB
        verify(phienDangNhapDAO).danhDauHetHan(maPhienHash);
        // 2. Không được gia hạn
        verify(phienDangNhapDAO, never()).giaHanPhien(anyString(), anyInt());
        // 3. Invalidate HttpSession
        verify(session).invalidate();
        // 4. Redirect /login?timeout=1
        verify(response).sendRedirect("/crm/login?timeout=1");
        // 5. KHÔNG chain.doFilter (không được vào /home)
        verify(chain, never()).doFilter(request, response);
    }

    // =========================================================================
    // Test B: HttpSession có user nhưng thiếu maPhienHash -> MUST NOT access /home
    // =========================================================================
    @Test
    @DisplayName("Test B: HttpSession có user nhưng thiếu maPhienHash -> MUST NOT access /home, invalidate và redirect /login")
    void testB_HttpSessionCoUser_ThieuMaPhienHash_MustNotAccessHome() throws ServletException, IOException {
        NguoiDung user = new NguoiDung();
        user.setId(5L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn(null);

        authFilter.doFilter(request, response, chain);

        // Xác nhận quy tắc 2:
        // 1. Invalidate session
        verify(session).invalidate();
        // 2. Redirect /login (không kèm timeout)
        verify(response).sendRedirect("/crm/login");
        // 3. KHÔNG chain.doFilter (MUST NOT access /home)
        verify(chain, never()).doFilter(request, response);
        // 4. Không gọi kiểm tra hay gia hạn DB
        verify(phienDangNhapDAO, never()).kiemTraChiTietPhien(anyString());
        verify(phienDangNhapDAO, never()).giaHanPhien(anyString(), anyInt());
    }

    @Test
    @DisplayName("Test B2: HttpSession có user nhưng maPhienHash rỗng -> MUST NOT access /home")
    void testB2_HttpSessionCoUser_MaPhienHashRong_MustNotAccessHome() throws ServletException, IOException {
        NguoiDung user = new NguoiDung();
        user.setId(5L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn("   ");

        authFilter.doFilter(request, response, chain);

        verify(session).invalidate();
        verify(response).sendRedirect("/crm/login");
        verify(chain, never()).doFilter(request, response);
        verify(phienDangNhapDAO, never()).kiemTraChiTietPhien(anyString());
    }

    // =========================================================================
    // Test C: DB session THU_HOI -> MUST NOT access /home
    // =========================================================================
    @Test
    @DisplayName("Test C: DB session THU_HOI -> MUST NOT access /home, invalidate và redirect /login")
    void testC_DBSessionThuHoi_MustNotAccessHome() throws ServletException, IOException {
        String maPhienHash = PasswordUtil.sha256Hex("revoked-session-xyz");
        NguoiDung user = new NguoiDung();
        user.setId(8L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn(maPhienHash);
        when(phienDangNhapDAO.kiemTraChiTietPhien(maPhienHash)).thenReturn(PhienDangNhapDAO.KetQuaKiemTraPhien.THU_HOI);

        authFilter.doFilter(request, response, chain);

        // Xác nhận:
        // 1. Invalidate session
        verify(session).invalidate();
        // 2. Redirect /login
        verify(response).sendRedirect("/crm/login");
        // 3. KHÔNG chain.doFilter (MUST NOT access /home)
        verify(chain, never()).doFilter(request, response);
        // 4. Không gia hạn
        verify(phienDangNhapDAO, never()).giaHanPhien(anyString(), anyInt());
    }

    @Test
    @DisplayName("Test C2: DB session KHONG_TON_TAI -> MUST NOT access /home, invalidate và redirect /login")
    void testC2_DBSessionKhongTonTai_MustNotAccessHome() throws ServletException, IOException {
        String maPhienHash = PasswordUtil.sha256Hex("non-existent-session");
        NguoiDung user = new NguoiDung();
        user.setId(9L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn(maPhienHash);
        when(phienDangNhapDAO.kiemTraChiTietPhien(maPhienHash)).thenReturn(PhienDangNhapDAO.KetQuaKiemTraPhien.KHONG_TON_TAI);

        authFilter.doFilter(request, response, chain);

        verify(session).invalidate();
        verify(response).sendRedirect("/crm/login");
        verify(chain, never()).doFilter(request, response);
        verify(phienDangNhapDAO, never()).giaHanPhien(anyString(), anyInt());
    }

    // =========================================================================
    // Test D: DB session hợp lệ -> request PASS và được gia hạn
    // =========================================================================
    @Test
    @DisplayName("Test D: DB session hợp lệ (HOP_LE) -> request PASS và được gia hạn trong DB + container")
    void testD_DBSessionHopLe_RequestPassVaGiaHan() throws ServletException, IOException {
        String maPhienHash = PasswordUtil.sha256Hex("valid-active-session");
        NguoiDung user = new NguoiDung();
        user.setId(1L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn(maPhienHash);
        when(phienDangNhapDAO.kiemTraChiTietPhien(maPhienHash)).thenReturn(PhienDangNhapDAO.KetQuaKiemTraPhien.HOP_LE);

        authFilter.doFilter(request, response, chain);

        // Xác nhận quy tắc 5:
        // 1. Gia hạn phiên trong DB thêm 30 phút
        verify(phienDangNhapDAO).giaHanPhien(eq(maPhienHash), eq(30));
        // 2. Gia hạn phiên trên container
        verify(session).setMaxInactiveInterval(30 * 60);
        // 3. Cho phép request đi tiếp tới Controller/JSP
        verify(chain).doFilter(request, response);
        // 4. Không redirect
        verify(response, never()).sendRedirect(anyString());
        // 5. Gửi headers chống browser cache sau khi logout
        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        verify(response).setHeader("Pragma", "no-cache");
        verify(response).setHeader("Expires", "0");
    }

    // =========================================================================
    // Edge Cases: Không có session & Static Assets & Cache Headers
    // =========================================================================
    @Test
    @DisplayName("Hardening: Protected response bắt buộc gửi đầy đủ Cache-Control, Pragma, Expires để chống Back button cache")
    void testProtectedResponse_BatBuocGuiNoCacheHeaders() throws ServletException, IOException {
        String maPhienHash = PasswordUtil.sha256Hex("active-session-cache-test");
        NguoiDung user = new NguoiDung();
        user.setId(10L);

        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("maPhienHash")).thenReturn(maPhienHash);
        when(phienDangNhapDAO.kiemTraChiTietPhien(maPhienHash)).thenReturn(PhienDangNhapDAO.KetQuaKiemTraPhien.HOP_LE);

        authFilter.doFilter(request, response, chain);

        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        verify(response).setHeader("Pragma", "no-cache");
        verify(response).setHeader("Expires", "0");
    }

    @Test
    @DisplayName("Quy tắc 1: Không có HttpSession hợp lệ -> redirect /login")
    void testKhongCoHttpSession_RedirectLogin() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestedSessionId()).thenReturn(null);

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/crm/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Container Session hết hạn (requestedSessionId invalid) -> redirect /login?timeout=1")
    void testContainerSessionHetHan_RedirectLoginTimeout() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/crm/home");
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestedSessionId()).thenReturn("old-invalid-cookie-session");
        when(request.isRequestedSessionIdValid()).thenReturn(false);

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/crm/login?timeout=1");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Tài nguyên tĩnh /assets/** không bao giờ bị redirect dù chưa có session và không bị gắn no-cache headers")
    void testAssets_KhongBiRedirectVaKhongApDungNoCache() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/crm/assets/css/style.css");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        verify(response, never()).setHeader(eq("Cache-Control"), anyString());
        verify(response, never()).setHeader(eq("Pragma"), anyString());
        verify(response, never()).setHeader(eq("Expires"), anyString());
    }
}
