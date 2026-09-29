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
 * Servlet hiển thị màn hình danh sách khách hàng và không gian làm việc ghi chú cuộc gặp.
 * Bảo vệ bởi SessionSecurityFilter.
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang"})
public class KhachHangServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null)
                ? (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY)
                : null;

        request.setAttribute("nguoiDung", nguoiDung);
        request.getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp").forward(request, response);
    }
}
