package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.nhom10.crm.service.DatLaiMatKhauService;

import java.io.IOException;

/**
 * Controller tiếp nhận yêu cầu gửi liên kết đặt lại mật khẩu qua email.
 * URL: /quen-mat-khau
 */
@WebServlet(name = "QuenMatKhauServlet", urlPatterns = {"/quen-mat-khau"})
public class QuenMatKhauServlet extends HttpServlet {

    private final DatLaiMatKhauService datLaiMatKhauService = new DatLaiMatKhauService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher("/WEB-INF/views/auth/quen-mat-khau.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("email");

        // Xác định baseUrl của hệ thống để gắn vào liên kết gửi trong email
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        baseUrl.append(contextPath);

        DatLaiMatKhauService.KetQuaXuLy ketQua = datLaiMatKhauService.yeuCauDatLaiMatKhau(email, baseUrl.toString());

        if (ketQua.isThanhCong()) {
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            request.setAttribute("emailNhapLai", email);
        } else {
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.setAttribute("emailNhapLai", email);
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/quen-mat-khau.jsp").forward(request, response);
    }
}
