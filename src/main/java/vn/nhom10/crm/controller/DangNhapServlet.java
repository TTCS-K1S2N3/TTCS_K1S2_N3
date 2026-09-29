package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.DangNhapService;
import vn.nhom10.crm.service.DangNhapService.KetQuaDangNhap;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;

/**
 * Servlet điều khiển đăng nhập và hiển thị các thông báo trạng thái phiên (AC3).
 */
@WebServlet(name = "DangNhapServlet", urlPatterns = {"/dang-nhap"})
public class DangNhapServlet extends HttpServlet {

    private DangNhapService dangNhapService;
    private PhienService phienService;

    @Override
    public void init() {
        if (this.dangNhapService == null) {
            this.dangNhapService = new DangNhapService();
        }
        if (this.phienService == null) {
            this.phienService = new PhienService();
        }
    }

    public void setDangNhapService(DangNhapService dangNhapService) {
        this.dangNhapService = dangNhapService;
    }

    public void setPhienService(PhienService phienService) {
        this.phienService = phienService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession currentSession = request.getSession(false);
        if (currentSession != null && currentSession.getAttribute(PhienService.SESSION_USER_KEY) != null) {
            String maPhien = (String) currentSession.getAttribute(PhienService.SESSION_TOKEN_KEY);
            if (phienService.kiemTraPhienHopLe(maPhien)) {
                response.sendRedirect(request.getContextPath() + "/khach-hang");
                return;
            }
        }

        // Xử lý thông báo từ query params
        String errorParam = request.getParameter("error");
        if ("session_expired".equalsIgnoreCase(errorParam)) {
            // AC3: Thông báo phiên hết hạn rõ ràng
            request.setAttribute("thongBaoLoi", "Phiên làm việc của bạn đã hết hạn do không có hoạt động. Vui lòng đăng nhập lại để tiếp tục làm việc an toàn.");
            request.setAttribute("maLoi", "SESSION_EXPIRED");
        } else if ("chua_dang_nhap".equalsIgnoreCase(errorParam)) {
            request.setAttribute("thongBaoLoi", "Vui lòng đăng nhập để truy cập hệ thống CRM.");
        }

        String thongBaoParam = request.getParameter("thongBao");
        if ("dang_xuat".equalsIgnoreCase(thongBaoParam)) {
            request.setAttribute("thongBaoThanhCong", "Bạn đã đăng xuất an toàn khỏi hệ thống CRM.");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String matKhau = request.getParameter("matKhau");

        KetQuaDangNhap ketQua = dangNhapService.dangNhap(email, matKhau);

        if (!ketQua.isThanhCong()) {
            request.setAttribute("email", email);
            request.setAttribute("thongBaoLoi", ketQua.getThongBaoLoi());
            request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
            return;
        }

        // Đăng nhập thành công -> Tạo phiên mới an toàn
        NguoiDung nguoiDung = ketQua.getNguoiDung();
        HttpSession session = request.getSession(true);

        String diaChiIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        phienService.taoPhienMoi(nguoiDung, session, diaChiIp, userAgent);

        // Điều hướng đến danh mục khách hàng / làm việc
        response.sendRedirect(request.getContextPath() + "/khach-hang");
    }
}
