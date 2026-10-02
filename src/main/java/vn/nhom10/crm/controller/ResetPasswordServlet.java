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
 * Controller xử lý đặt lại mật khẩu bằng liên kết token (S1-03).
 * URL: /reset-password
 */
@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordServlet.class);
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

        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            req.setAttribute("tokenHopLe", false);
            req.setAttribute("errorMessage", AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE);
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }

        DatLaiMatKhauResult result = authService.kiemTraTokenDatLaiMatKhau(token);
        if (result.isTokenHopLe()) {
            req.setAttribute("tokenHopLe", true);
            req.setAttribute("token", token);
        } else {
            req.setAttribute("tokenHopLe", false);
            req.setAttribute("errorMessage", result.getThongBao());
        }

        req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("token");
        String matKhauMoi = req.getParameter("matKhauMoi");
        String xacNhanMatKhau = req.getParameter("xacNhanMatKhau");

        DatLaiMatKhauResult result = authService.datLaiMatKhau(token, matKhauMoi, xacNhanMatKhau);

        if (result.isThanhCong()) {
            logger.info("Đặt lại mật khẩu thành công bằng token, chuyển hướng về trang đăng nhập");
            resp.sendRedirect(req.getContextPath() + "/login?resetSuccess=1");
        } else {
            req.setAttribute("errorMessage", result.getThongBao());
            req.setAttribute("token", token);
            // Kiểm tra xem token còn hạn/hợp lệ hay đã bị vô hiệu hóa
            DatLaiMatKhauResult check = authService.kiemTraTokenDatLaiMatKhau(token);
            req.setAttribute("tokenHopLe", check.isTokenHopLe());
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
        }
    }
}
