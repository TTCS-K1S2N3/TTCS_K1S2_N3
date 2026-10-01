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
import vn.nhom10.crm.service.PhienService;
import vn.nhom10.crm.util.SessionRegistry;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller tiếp nhận và xử lý yêu cầu đăng nhập vào hệ thống CRM.
 * Hỗ trợ xác thực bảo mật (S1-01), quản lý phiên an toàn (S1-02), và đăng ký phiên SessionRegistry (S1-04).
 * URL: /dang-nhap, /login
 */
@WebServlet(name = "DangNhapServlet", urlPatterns = {"/dang-nhap", "/login"})
public class DangNhapServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DangNhapServlet.class.getName());

    private DangNhapService dangNhapService;
    private PhienService phienService;

    @Override
    public void init() throws ServletException {
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
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Nếu người dùng đã có phiên đăng nhập hợp lệ, chuyển thẳng tới trang chủ
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoiDung") != null) {
            NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
            String maPhien = (String) session.getAttribute(PhienService.SESSION_TOKEN_KEY);
            if (phienService == null || maPhien == null || phienService.kiemTraPhienHopLe(maPhien)) {
                String trangChu = dangNhapService != null ? dangNhapService.xacDinhTrangChu(user) : "/khach-hang";
                response.sendRedirect(request.getContextPath() + trangChu);
                return;
            }
        }

        // Xử lý thông báo từ các luồng khác chuyển tới
        String error = request.getParameter("error");
        if ("session_expired".equalsIgnoreCase(error)) {
            // AC3 (S1-02): Phiên hết hạn đưa về trang đăng nhập kèm thông báo rõ ràng
            request.setAttribute("thongBaoLoi", "Phiên làm việc của bạn đã hết hạn do không có hoạt động. Vui lòng đăng nhập lại để tiếp tục làm việc an toàn.");
            request.setAttribute("maLoi", "SESSION_EXPIRED");
        } else if ("session_revoked".equalsIgnoreCase(error)) {
            // S1-04: Phiên bị thu hồi do đổi mật khẩu ở thiết bị khác
            request.setAttribute("thongBaoLoi", "Phiên đăng nhập đã bị thu hồi do tài khoản đã đổi mật khẩu trên thiết bị khác. Vui lòng đăng nhập lại với mật khẩu mới.");
            request.setAttribute("maLoi", "SESSION_REVOKED");
        } else if ("auth_required".equalsIgnoreCase(error) || "chua_dang_nhap".equalsIgnoreCase(error)) {
            request.setAttribute("thongBaoLoi", "Vui lòng đăng nhập để truy cập hệ thống CRM.");
        }

        String thongBao = request.getParameter("thongBao");
        if ("dang_xuat".equalsIgnoreCase(thongBao)) {
            request.setAttribute("thongBaoThanhCong", "Bạn đã đăng xuất an toàn khỏi hệ thống CRM.");
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

        if (dangNhapService == null) {
            dangNhapService = new DangNhapService();
        }

        KetQuaDangNhapDTO ketQua = dangNhapService.dangNhap(email, matKhau);

        if (ketQua.isThanhCong()) {
            NguoiDung user = ketQua.getNguoiDung();
            HttpSession session = request.getSession(true);
            session.setAttribute("nguoiDung", user);

            // S1-02: Khởi tạo phiên làm việc bảo mật trên server và database
            if (phienService != null) {
                try {
                    phienService.taoPhienMoi(user, session, request.getRemoteAddr(), request.getHeader("User-Agent"));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Không thể lưu phiên đăng nhập vào DB: " + e.getMessage());
                }
            }

            // S1-04: Đăng ký phiên vào SessionRegistry trong bộ nhớ container
            try {
                SessionRegistry.getInstance().dangKyPhien(user.getId(), session);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể đăng ký phiên vào SessionRegistry: " + e.getMessage());
            }

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
