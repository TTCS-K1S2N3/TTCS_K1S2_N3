package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.util.PasswordUtil;

import java.io.IOException;

/**
 * Controller xử lý đăng xuất an toàn khỏi hệ thống.
 * URL: /logout
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);
    private PhienDangNhapDAO phienDangNhapDAO;

    @Override
    public void init() {
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }

    public void setPhienDangNhapDAO(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        xuLyDangXuat(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        xuLyDangXuat(req, resp);
    }

    private void xuLyDangXuat(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String rawSessionId = session.getId();
            try {
                String maPhienHash = PasswordUtil.sha256Hex(rawSessionId);
                phienDangNhapDAO.thuHoiPhien(maPhienHash, "Nguoi dung chu dong dang xuat");
            } catch (Exception e) {
                logger.warn("Không thu hồi được bản ghi phiên: {}", e.getMessage());
            }
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login?logout=1");
    }
}
