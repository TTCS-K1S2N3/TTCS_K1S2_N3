package vn.nhom10.crm.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.util.PasswordUtil;

/**
 * Listener theo dõi vòng đời của HttpSession.
 * S1-02-AC1: Thiết lập thời gian chờ 30 phút mặc định cho mỗi phiên tạo mới.
 * S1-02-AC3: Khi phiên hết hạn do timeout, tự động cập nhật trạng thái HET_HAN trong bảng phien_dang_nhap.
 */
@WebListener
public class SessionListener implements HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);
    private PhienDangNhapDAO phienDangNhapDAO;

    public SessionListener() {
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }

    public void setPhienDangNhapDAO(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        session.setMaxInactiveInterval(30 * 60); // 30 phút
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        try {
            String maPhienHash = (String) session.getAttribute("maPhienHash");
            if (maPhienHash == null) {
                maPhienHash = PasswordUtil.sha256Hex(session.getId());
            }
            if (maPhienHash != null) {
                // Đánh dấu HET_HAN trong DB nếu phiên chưa bị thu hồi
                phienDangNhapDAO.danhDauHetHan(maPhienHash);
                logger.info("Phiên {} đã bị hủy hoặc hết hạn, cập nhật trạng thái HET_HAN trong DB", maPhienHash);
            }
        } catch (IllegalStateException e) {
            // Khi session đã hoàn tất invalidate, bỏ qua ngoại lệ này
            logger.debug("Session đã invalidate: {}", e.getMessage());
        } catch (Exception e) {
            logger.warn("Lỗi khi cập nhật trạng thái phiên hết hạn: {}", e.getMessage());
        }
    }
}
