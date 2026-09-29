package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhienDangNhap;
import vn.nhom10.crm.util.PasswordUtil;
import vn.nhom10.crm.util.SessionRegistry;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ đăng nhập hệ thống CRM.
 * URL: /dang-nhap
 */
@WebServlet(name = "DangNhapServlet", urlPatterns = {"/dang-nhap"})
public class DangNhapServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DangNhapServlet.class.getName());

    private NguoiDungDAO nguoiDungDAO;
    private PhienDangNhapDAO phienDangNhapDAO;
    private SessionRegistry sessionRegistry;

    @Override
    public void init() throws ServletException {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.phienDangNhapDAO = new PhienDangNhapDAO();
        this.sessionRegistry = SessionRegistry.getInstance();
    }

    public void setNguoiDungDAO(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public void setPhienDangNhapDAO(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String error = request.getParameter("error");
        if ("auth_required".equalsIgnoreCase(error)) {
            request.setAttribute("thongBaoLoi", "Vui lòng đăng nhập để truy cập chức năng này.");
        } else if ("session_revoked".equalsIgnoreCase(error)) {
            request.setAttribute("thongBaoLoi", "Phiên đăng nhập đã bị thu hồi do tài khoản đã đổi mật khẩu trên thiết bị khác. Vui lòng đăng nhập lại với mật khẩu mới.");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("email");
        String matKhau = request.getParameter("matKhau");

        if (email == null || email.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
            request.setAttribute("thongBaoLoi", "Vui lòng nhập đầy đủ email và mật khẩu.");
            request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
            return;
        }

        NguoiDung user = nguoiDungDAO.findByEmail(email.trim());
        if (user == null || !PasswordUtil.checkPassword(matKhau, user.getMatKhau())) {
            request.setAttribute("thongBaoLoi", "Email hoặc mật khẩu không chính xác.");
            request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
            return;
        }

        if (!user.isDangHoatDong()) {
            request.setAttribute("thongBaoLoi", "Tài khoản đang bị khóa hoặc ngưng hoạt động.");
            request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
            return;
        }

        // Tạo phiên đăng nhập thành công
        HttpSession session = request.getSession(true);
        session.setAttribute("nguoiDung", user);

        // Đăng ký phiên vào SessionRegistry và database
        sessionRegistry.dangKyPhien(user.getId(), session);
        try {
            String ip = layDiaChiIp(request);
            String userAgent = request.getHeader("User-Agent");
            PhienDangNhap phien = new PhienDangNhap(user.getId(), session.getId(), ip, userAgent);
            phienDangNhapDAO.save(phien, null);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể lưu phiên đăng nhập", e);
        }

        response.sendRedirect(request.getContextPath() + "/doi-mat-khau");
    }

    private String layDiaChiIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
