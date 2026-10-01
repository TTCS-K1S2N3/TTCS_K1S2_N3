package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;

/**
 * Controller phục vụ trang danh mục khách hàng (trang đích sau đăng nhập của khối kinh doanh).
 * Tích hợp không gian làm việc ghi chú cuộc gặp và duy trì phiên tự động (S1-02).
 * URL: /khach-hang
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang"})
public class KhachHangServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        NguoiDung user = null;
        if (session != null) {
            user = (NguoiDung) session.getAttribute("nguoiDung");
            if (user == null) {
                user = (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
            }
        }

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        request.setAttribute("nguoiDung", user);
        request.setAttribute("nguoiDungHienTai", user);

        request.getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp").forward(request, response);
    }
}
