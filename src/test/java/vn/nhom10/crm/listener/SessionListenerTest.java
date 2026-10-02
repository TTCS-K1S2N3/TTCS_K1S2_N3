package vn.nhom10.crm.listener;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.util.PasswordUtil;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử SessionListener - S1-02-AC1 & AC3")
class SessionListenerTest {

    @Mock
    private PhienDangNhapDAO phienDangNhapDAO;

    @Mock
    private HttpSession session;

    @Mock
    private HttpSessionEvent event;

    private SessionListener sessionListener;

    @BeforeEach
    void setUp() {
        sessionListener = new SessionListener();
        sessionListener.setPhienDangNhapDAO(phienDangNhapDAO);
        when(event.getSession()).thenReturn(session);
    }

    @Test
    @DisplayName("S1-02-AC1: Khi phiên được tạo, thiết lập thời hạn không hoạt động 30 phút")
    void testSessionCreated_SetsMaxInactiveInterval() {
        sessionListener.sessionCreated(event);

        verify(session).setMaxInactiveInterval(30 * 60);
    }

    @Test
    @DisplayName("S1-02-AC3: Khi phiên bị hủy do timeout, đánh dấu HET_HAN trong DB")
    void testSessionDestroyed_DanhDauHetHanTrongDB() {
        String sessionId = "timeout-session-123";
        String expectedHash = PasswordUtil.sha256Hex(sessionId);

        when(session.getAttribute("maPhienHash")).thenReturn(expectedHash);

        sessionListener.sessionDestroyed(event);

        verify(phienDangNhapDAO).danhDauHetHan(expectedHash);
    }
}
