package vn.nhom10.crm.util;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý các phiên đăng nhập (HttpSession) đang hoạt động trong hệ thống.
 * Phục vụ Acceptance Criteria 1: "Tài khoản bị khoá không đăng nhập được và bị thu hồi phiên đang mở".
 */
@WebListener
public class ActiveSessionManager implements HttpSessionListener {

    private static final Logger LOGGER = Logger.getLogger(ActiveSessionManager.class.getName());
    private static final ActiveSessionManager INSTANCE = new ActiveSessionManager();

    // Map: userId -> Set<HttpSession>
    private final Map<Integer, Set<HttpSession>> userSessions = new ConcurrentHashMap<>();

    // Map: sessionId -> userId (để dọn dẹp khi session hết hạn tự nhiên)
    private final Map<String, Integer> sessionUserMap = new ConcurrentHashMap<>();

    public static ActiveSessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Đăng ký một phiên làm việc với userId tương ứng khi đăng nhập thành công.
     */
    public void dangKySession(int userId, HttpSession session) {
        if (session == null) return;

        userSessions.computeIfAbsent(userId, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
        sessionUserMap.put(session.getId(), userId);
        LOGGER.info("Đã đăng ký session ID=" + session.getId() + " cho userId=" + userId);
    }

    /**
     * Thu hồi và huỷ bỏ tất cả các phiên làm việc đang mở của một người dùng.
     * Sử dụng ngay khi khoá tài khoản nhân viên nghỉ việc.
     *
     * @param userId ID của người dùng bị khoá
     * @return số lượng phiên làm việc đã bị thu hồi
     */
    public int thuHoiTatCaSessionCuaUser(int userId) {
        Set<HttpSession> sessions = userSessions.remove(userId);
        if (sessions == null || sessions.isEmpty()) {
            LOGGER.info("Không có phiên làm việc nào đang mở cho userId=" + userId);
            return 0;
        }

        int count = 0;
        synchronized (sessions) {
            for (HttpSession session : sessions) {
                try {
                    sessionUserMap.remove(session.getId());
                    session.invalidate(); // Thu hồi phiên ngay lập tức
                    count++;
                } catch (IllegalStateException e) {
                    // Session có thể đã hết hạn hoặc bị huỷ trước đó
                    LOGGER.log(Level.FINE, "Session đã bị huỷ trước đó: " + e.getMessage());
                }
            }
        }

        LOGGER.info("Đã thu hồi thành công " + count + " phiên làm việc của userId=" + userId);
        return count;
    }

    /**
     * Đếm số lượng phiên đang mở của một người dùng.
     */
    public int demSoSessionDangMo(int userId) {
        Set<HttpSession> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Phiên mới được tạo
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        if (session != null) {
            String sId = session.getId();
            Integer userId = sessionUserMap.remove(sId);
            if (userId != null) {
                Set<HttpSession> sessions = userSessions.get(userId);
                if (sessions != null) {
                    sessions.remove(session);
                    if (sessions.isEmpty()) {
                        userSessions.remove(userId);
                    }
                }
            }
        }
    }
}
