package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dto.DatLaiMatKhauResult;
import vn.nhom10.crm.service.AuthService;

import java.io.IOException;

/**
 * Controller xử lý yêu cầu quên mật khẩu (S1-03).
 * URL: /forgot-password
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordServlet.class);
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

        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");

        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        String baseUrl = buildBaseUrl(req);

        DatLaiMatKhauResult result = authService.yeuCauDatLaiMatKhau(email, ip, baseUrl);

        if (result.isThanhCong()) {
            // S1-03-AC3: Hiển thị thông báo chung thống nhất (chống user enumeration)
            req.setAttribute("successMessage", result.getThongBao());
            req.setAttribute("email", "");
        } else {
            req.setAttribute("errorMessage", result.getThongBao());
            req.setAttribute("email", email != null ? email.trim() : "");
        }

        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    private String buildBaseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        String contextPath = req.getContextPath();

        if ((scheme.equalsIgnoreCase("http") && serverPort == 80) ||
            (scheme.equalsIgnoreCase("https") && serverPort == 443)) {
            return scheme + "://" + serverName + contextPath;
        } else {
            return scheme + "://" + serverName + ":" + serverPort + contextPath;
        }
    }
}
