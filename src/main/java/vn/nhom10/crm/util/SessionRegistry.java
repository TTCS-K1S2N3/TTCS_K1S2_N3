package vn.nhom10.crm.util;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý và theo dõi các phiên đăng nhập (HttpSession) trong bộ nhớ container.
 * Thực thi Acceptance Criteria 3: Thu hồi các phiên đăng nhập khác khi người dùng đổi mật khẩu.
 */
@WebListener
public class SessionRegistry implements HttpSessionListener {

    private static final Logger LOGGER = Logger.getLogger(SessionRegistry.class.getName());

    private static final SessionRegistry INSTANCE = new SessionRegistry();

    // Map: nguoiDungId -> Set<HttpSession>
    private final Map<Long, Set<HttpSession>> userSessionsMap = new ConcurrentHashMap<>();

    // Map: sessionId -> nguoiDungId
    private final Map<String, Long> sessionIdToUserMap = new ConcurrentHashMap<>();

    public static SessionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Đăng ký một phiên đăng nhập đang hoạt động cho người dùng.
     *
     * @param nguoiDungId ID người dùng
     * @param session     đối tượng HttpSession
     */
    public void dangKyPhien(Long nguoiDungId, HttpSession session) {
        if (nguoiDungId == null || session == null) {
            return;
        }

        try {
            String sessionId = session.getId();
            userSessionsMap.computeIfAbsent(nguoiDungId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(session);
            sessionIdToUserMap.put(sessionId, nguoiDungId);
            LOGGER.info(() -> String.format("Đã đăng ký phiên [ID: %s] cho người dùng [ID: %d]", sessionId, nguoiDungId));
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Không thể đăng ký phiên do session đã bị invalidate", e);
        }
    }

    /**
     * Thu hồi tất cả các phiên đăng nhập KHÁC của người dùng ngoại trừ phiên hiện tại.
     *
     * @param nguoiDungId        ID người dùng đổi mật khẩu
     * @param maPhienHienTai     mã phiên hiện tại của thiết bị đang thực hiện đổi mật khẩu
     * @return số lượng phiên đã thu hồi (invalidated)
     */
    public int thuHoiCacPhienKhac(Long nguoiDungId, String maPhienHienTai) {
        if (nguoiDungId == null) {
            return 0;
        }

        Set<HttpSession> sessions = userSessionsMap.get(nguoiDungId);
        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (HttpSession s : sessions) {
            try {
                String sId = s.getId();
                // Không hủy phiên hiện tại
                if (maPhienHienTai != null && maPhienHienTai.equals(sId)) {
                    continue;
                }

                sessionIdToUserMap.remove(sId);
                sessions.remove(s);
                s.invalidate();
                count++;
                LOGGER.info(() -> String.format("Đã thu hồi phiên khác [ID: %s] của người dùng [ID: %d]", sId, nguoiDungId));
            } catch (IllegalStateException ignored) {
                // Phiên đã hết hạn trước đó
                sessions.remove(s);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Lỗi khi thu hồi phiên", e);
            }
        }
        return count;
    }

    /**
     * Hủy đăng ký phiên khi người dùng đăng xuất hoặc phiên hết hạn.
     *
     * @param session đối tượng HttpSession
     */
    public void huyDangKyPhien(HttpSession session) {
        if (session == null) {
            return;
        }
        try {
            String sessionId = session.getId();
            Long nguoiDungId = sessionIdToUserMap.remove(sessionId);
            if (nguoiDungId != null) {
                Set<HttpSession> set = userSessionsMap.get(nguoiDungId);
                if (set != null) {
                    set.remove(session);
                    if (set.isEmpty()) {
                        userSessionsMap.remove(nguoiDungId);
                    }
                }
            }
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * Lấy số lượng phiên đang hoạt động của người dùng.
     *
     * @param nguoiDungId ID người dùng
     * @return số lượng phiên còn hiệu lực
     */
    public int soPhienDangHoatDong(Long nguoiDungId) {
        if (nguoiDungId == null) {
            return 0;
        }
        Set<HttpSession> sessions = userSessionsMap.get(nguoiDungId);
        if (sessions == null) {
            return 0;
        }
        int active = 0;
        for (HttpSession s : sessions) {
            try {
                s.getLastAccessedTime();
                active++;
            } catch (IllegalStateException e) {
                // Đã bị invalidate
                sessions.remove(s);
            }
        }
        return active;
    }

    /**
     * Xóa sạch dữ liệu (phục vụ kiểm thử unit test).
     */
    public void clear() {
        userSessionsMap.clear();
        sessionIdToUserMap.clear();
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Phiên mới tạo chưa gắn người dùng
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        huyDangKyPhien(se.getSession());
    }
}
