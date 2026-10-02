package vn.nhom10.crm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;

/**
 * Servlet xử lý đăng xuất an toàn (AC2 của S1-02).
 * Làm mất hiệu lực phiên ngay lập tức phía server:
 * - Hủy session (session.invalidate())
 * - Đánh dấu thu hồi phiên trong database
 * - Xóa cookie JSESSIONID
 * - Chuyển hướng về trang đăng nhập với thông báo xác nhận
 * URL: /dang-xuat, /logout
 */
@WebServlet(name = "DangXuatServlet", urlPatterns = {"/dang-xuat", "/logout", "/auth/logout"})
public class DangXuatServlet extends HttpServlet {

    private PhienService phienService;

    @Override
    public void init() {
        if (this.phienService == null) {
            this.phienService = new PhienService();
        }
    }

    public void setPhienService(PhienService phienService) {
        this.phienService = phienService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        thucHienDangXuat(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        thucHienDangXuat(request, response);
    }

    private void thucHienDangXuat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Vô hiệu hóa phiên ngay lập tức phía server và database (AC2)
        if (phienService != null) {
            phienService.dangXuat(request, response);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }

        // Chuyển hướng về trang đăng nhập kèm thông báo đã đăng xuất an toàn
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/dang-nhap?thongBao=dang_xuat");
    }
}
