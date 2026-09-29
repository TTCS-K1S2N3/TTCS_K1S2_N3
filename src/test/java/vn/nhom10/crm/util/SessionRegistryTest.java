package vn.nhom10.crm.util;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử SessionRegistry - AC 3: Thu hồi các phiên đăng nhập khác")
class SessionRegistryTest {

    private SessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        sessionRegistry = SessionRegistry.getInstance();
        sessionRegistry.clear();
    }

    @Test
    @DisplayName("AC 3: Đăng ký nhiều phiên và thu hồi các phiên khác, giữ lại phiên hiện tại")
    void testRevokeOtherSessions() {
        Long userId = 1L;

        HttpSession sessionHienTai = mock(HttpSession.class);
        when(sessionHienTai.getId()).thenReturn("SESSION_CURRENT");

        HttpSession sessionKhac1 = mock(HttpSession.class);
        when(sessionKhac1.getId()).thenReturn("SESSION_OTHER_1");

        HttpSession sessionKhac2 = mock(HttpSession.class);
        when(sessionKhac2.getId()).thenReturn("SESSION_OTHER_2");

        // Đăng ký 3 phiên cho cùng một người dùng
        sessionRegistry.dangKyPhien(userId, sessionHienTai);
        sessionRegistry.dangKyPhien(userId, sessionKhac1);
        sessionRegistry.dangKyPhien(userId, sessionKhac2);

        assertEquals(3, sessionRegistry.soPhienDangHoatDong(userId));

        // Thực hiện thu hồi các phiên khác ngoại trừ SESSION_CURRENT
        int soPhienThuHoi = sessionRegistry.thuHoiCacPhienKhac(userId, "SESSION_CURRENT");

        assertEquals(2, soPhienThuHoi);

        // Xác minh 2 phiên khác bị invalidate
        verify(sessionKhac1, times(1)).invalidate();
        verify(sessionKhac2, times(1)).invalidate();

        // Phiên hiện tại KHÔNG bị invalidate
        verify(sessionHienTai, never()).invalidate();

        // Số phiên còn hoạt động là 1
        assertEquals(1, sessionRegistry.soPhienDangHoatDong(userId));
    }

    @Test
    @DisplayName("Hủy đăng ký phiên khi người dùng đăng xuất")
    void testUnregisterSession() {
        Long userId = 2L;
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("SESSION_TEST");

        sessionRegistry.dangKyPhien(userId, session);
        assertEquals(1, sessionRegistry.soPhienDangHoatDong(userId));

        sessionRegistry.huyDangKyPhien(session);
        assertEquals(0, sessionRegistry.soPhienDangHoatDong(userId));
    }
}
