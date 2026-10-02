package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dto.DangNhapResult;
import vn.nhom10.crm.service.AuthService;

import java.io.IOException;

/**
 * Controller xử lý đăng nhập hệ thống CRM.
 * URL: /login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private AuthService authService;

    @Override
    public void init() {
        this.authService = new AuthService();
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String matKhau = req.getParameter("matKhau");

        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        String thietBi = req.getHeader("User-Agent");

        // Chuẩn bị session mới
        HttpSession oldSession = req.getSession(false);

        DangNhapResult result = authService.dangNhap(email, matKhau, ip, thietBi, null);

        if (result.isThanhCong()) {
            // Đăng nhập thành công: tạo phiên an toàn, chống session fixation
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("user", result.getNguoiDung());
            newSession.setMaxInactiveInterval(30 * 60);

            String rawNewSessionId = newSession.getId();
            String maPhienHash = vn.nhom10.crm.util.PasswordUtil.sha256Hex(rawNewSessionId);
            newSession.setAttribute("maPhienHash", maPhienHash);
            authService.ghiNhanPhienDangNhap(
                    result.getNguoiDung().getId(),
                    maPhienHash,
                    result.getNguoiDung().getSessionVersion(),
                    ip,
                    thietBi,
                    30
            );

            logger.info("Đăng nhập thành công cho người dùng {}, chuyển hướng tới /home", email);
            resp.sendRedirect(req.getContextPath() + "/home");
        } else {
            // Đăng nhập thất bại: trả lại form với thông báo lỗi
            req.setAttribute("errorMessage", result.getThongBao());
            req.setAttribute("email", email != null ? email.trim() : "");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }
}
