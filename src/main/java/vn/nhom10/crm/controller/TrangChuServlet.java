package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.DangNhapService;

import java.io.IOException;

/**
 * Controller điều hướng trang chủ dựa trên trạng thái đăng nhập và vai trò người dùng.
 * URL: /trang-chu
 */
@WebServlet(name = "TrangChuServlet", urlPatterns = {"/trang-chu"})
public class TrangChuServlet extends HttpServlet {

    private DangNhapService dangNhapService;

    @Override
    public void init() throws ServletException {
        this.dangNhapService = new DangNhapService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        String trangChu = dangNhapService.xacDinhTrangChu(user);
        response.sendRedirect(request.getContextPath() + trangChu);
    }
}
