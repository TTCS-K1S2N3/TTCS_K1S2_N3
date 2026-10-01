package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.DangNhapService;

import java.io.IOException;

/**
 * Controller tiếp nhận và xử lý yêu cầu đăng nhập vào hệ thống CRM.
 * URL: /dang-nhap, /login
 */
@WebServlet(name = "DangNhapServlet", urlPatterns = {"/dang-nhap", "/login"})
public class DangNhapServlet extends HttpServlet {

    private DangNhapService dangNhapService;

    @Override
    public void init() throws ServletException {
        this.dangNhapService = new DangNhapService();
    }

    public void setDangNhapService(DangNhapService dangNhapService) {
        this.dangNhapService = dangNhapService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Nếu người dùng đã có phiên đăng nhập hợp lệ, chuyển thẳng tới trang chủ
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoiDung") != null) {
            NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
            String trangChu = dangNhapService.xacDinhTrangChu(user);
            response.sendRedirect(request.getContextPath() + trangChu);
            return;
        }

        // Xử lý thông báo từ các luồng khác chuyển tới
        String error = request.getParameter("error");
        if ("auth_required".equalsIgnoreCase(error)) {
            request.setAttribute("thongBaoLoi", "Vui lòng đăng nhập để truy cập chức năng này.");
        } else if ("session_expired".equalsIgnoreCase(error)) {
            request.setAttribute("thongBaoLoi", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        String thongBao = request.getParameter("thongBao");
        if ("dang_xuat".equalsIgnoreCase(thongBao)) {
            request.setAttribute("thongBaoThanhCong", "Bạn đã đăng xuất khỏi hệ thống thành công.");
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

        KetQuaDangNhapDTO ketQua = dangNhapService.dangNhap(email, matKhau);

        if (ketQua.isThanhCong()) {
            // Đăng nhập đúng: Lưu thông tin người dùng vào HttpSession
            HttpSession session = request.getSession(true);
            session.setAttribute("nguoiDung", ketQua.getNguoiDung());

            // Chuyển hướng tới trang chủ tương ứng với vai trò
            response.sendRedirect(request.getContextPath() + ketQua.getTrangChuUrl());
        } else {
            // Đăng nhập thất bại: Hiển thị thông báo lỗi và giữ lại email đã nhập
            request.setAttribute("thongBaoLoi", ketQua.getThongBaoLoi());
            request.setAttribute("email", email != null ? email.trim() : "");
            request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
        }
    }
}
