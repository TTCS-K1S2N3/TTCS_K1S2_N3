package vn.nhom10.crm.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.PasswordUtil;

import java.io.IOException;

/**
 * Filter bảo vệ các tuyến đường nội bộ của CRM và quản lý vòng đời phiên (S1-02).
 * - S1-02-AC1: Gia hạn phiên tự động khi người dùng còn hoạt động.
 * - S1-02-AC2: Cho phép /logout làm mất hiệu lực phiên ngay lập tức phía server.
 * - S1-02-AC3: Phiên hết hạn đưa về trang đăng nhập kèm thông báo rõ ràng (?timeout=1).
 * - Đảm bảo /assets/** không bị chặn hoặc chuyển hướng.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private PhienDangNhapDAO phienDangNhapDAO;

    @Override
    public void init(FilterConfig filterConfig) {
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }

    public void setPhienDangNhapDAO(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        // 1. Tuyệt đối không chặn hoặc chuyển hướng static assets (/assets/**)
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Cho phép các route công khai liên quan đến xác thực
        if (path.equals("/login") || path.equals("/logout")
                || path.equals("/forgot-password") || path.equals("/reset-password")) {
            chain.doFilter(request, response);
            return;
        }

        // Hardening chống browser cache sau logout cho các protected routes:
        // Đảm bảo nút Back của trình duyệt không hiển thị lại HTML trang nội bộ từ cache/history.
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setHeader("Expires", "0");

        // 3. Quy tắc 1: Nếu không có HttpSession hợp lệ -> redirect /login
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            // S1-02-AC3: Nhận diện trường hợp phiên vừa bị hết hạn trên trình duyệt
            boolean wasExpired = (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid());
            if (wasExpired) {
                logger.info("Yêu cầu tới {} với phiên đã hết hạn trên container, chuyển hướng tới login?timeout=1", path);
                httpResponse.sendRedirect(contextPath + "/login?timeout=1");
            } else {
                httpResponse.sendRedirect(contextPath + "/login");
            }
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("user");
        if (user == null) {
            session.invalidate();
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        // Quy tắc 2: Nếu có HttpSession nhưng không có maPhienHash tương ứng với bản ghi phien_dang_nhap
        // -> KHÔNG được cho qua.
        // -> invalidate session.
        // -> redirect /login.
        String maPhienHash = (String) session.getAttribute("maPhienHash");
        if (maPhienHash == null || maPhienHash.isBlank()) {
            logger.warn("HttpSession có user nhưng thiếu maPhienHash, hủy phiên và từ chối truy cập {}", path);
            session.invalidate();
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        // Quy tắc 3: Nếu có maPhienHash: phải query MySQL và kiểm tra:
        // - bản ghi tồn tại
        // - trang_thai = HOAT_DONG
        // - thu_hoi_luc IS NULL
        // - het_han_luc > NOW()
        PhienDangNhapDAO.KetQuaKiemTraPhien ketQua = phienDangNhapDAO.kiemTraChiTietPhien(maPhienHash);

        switch (ketQua) {
            case HET_HAN:
                // Quy tắc 4: Nếu het_han_luc <= NOW():
                // - không chain.doFilter
                // - không gia hạn
                // - cập nhật trạng thái HET_HAN nếu canonical contract hỗ trợ
                // - invalidate HttpSession
                // - redirect /login?timeout=1
                logger.info("Phiên {} đã quá hạn trong DB (het_han_luc <= NOW()), đánh dấu HET_HAN và đưa về login?timeout=1", maPhienHash);
                phienDangNhapDAO.danhDauHetHan(maPhienHash);
                session.invalidate();
                httpResponse.sendRedirect(contextPath + "/login?timeout=1");
                return;

            case THU_HOI:
            case KHONG_TON_TAI:
                // Không tồn tại hoặc đã bị thu hồi -> KHÔNG được cho qua, invalidate session, redirect /login
                logger.warn("Phiên {} không tồn tại hoặc đã bị thu hồi ({}), hủy session và đưa về /login", maPhienHash, ketQua);
                session.invalidate();
                httpResponse.sendRedirect(contextPath + "/login");
                return;

            case HOP_LE:
            default:
                if (ketQua != PhienDangNhapDAO.KetQuaKiemTraPhien.HOP_LE) {
                    session.invalidate();
                    httpResponse.sendRedirect(contextPath + "/login");
                    return;
                }
                // Quy tắc 5: Chỉ nếu DB session CÒN HỢP LỆ:
                // - mới cập nhật hoat_dong_cuoi_luc
                // - mới gia hạn het_han_luc
                // - mới chain.doFilter
                phienDangNhapDAO.giaHanPhien(maPhienHash, 30);
                session.setMaxInactiveInterval(30 * 60);
                chain.doFilter(request, response);
                break;
        }
    }

    @Override
    public void destroy() {
    }
}
