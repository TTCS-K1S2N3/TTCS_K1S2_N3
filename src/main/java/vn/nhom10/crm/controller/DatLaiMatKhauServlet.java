package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.nhom10.crm.service.DatLaiMatKhauService;

import java.io.IOException;

/**
 * Controller xử lý xác thực token và cập nhật mật khẩu mới.
 * URL: /dat-lai-mat-khau
 */
@WebServlet(name = "DatLaiMatKhauServlet", urlPatterns = {"/dat-lai-mat-khau"})
public class DatLaiMatKhauServlet extends HttpServlet {

    private final DatLaiMatKhauService datLaiMatKhauService = new DatLaiMatKhauService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String token = request.getParameter("token");

        if (token == null || token.trim().isEmpty()) {
            request.setAttribute("thongBaoLoi", "Mã liên kết không tồn tại hoặc không hợp lệ.");
            request.setAttribute("tokenHopLe", false);
            request.getRequestDispatcher("/WEB-INF/views/auth/dat-lai-mat-khau.jsp").forward(request, response);
            return;
        }

        DatLaiMatKhauService.KetQuaXuLy ketQua = datLaiMatKhauService.kiemTraToken(token);

        if (!ketQua.isThanhCong()) {
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.setAttribute("tokenHopLe", false);
            request.setAttribute("trangThaiToken", ketQua.getTrangThaiToken() != null ? ketQua.getTrangThaiToken().name() : "");
        } else {
            request.setAttribute("tokenHopLe", true);
            request.setAttribute("token", token);
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/dat-lai-mat-khau.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String token = request.getParameter("token");
        String matKhauMoi = request.getParameter("matKhauMoi");
        String xacNhanMatKhau = request.getParameter("xacNhanMatKhau");

        DatLaiMatKhauService.KetQuaXuLy ketQua = datLaiMatKhauService.datLaiMatKhau(token, matKhauMoi, xacNhanMatKhau);

        if (ketQua.isThanhCong()) {
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            request.setAttribute("tokenHopLe", false);
            request.setAttribute("datLaiThanhCong", true);
            request.getRequestDispatcher("/WEB-INF/views/auth/dat-lai-mat-khau.jsp").forward(request, response);
        } else {
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            // Giữ lại form nếu token vẫn còn hợp lệ nhưng mật khẩu nhập sai quy cách
            if (ketQua.getTrangThaiToken() == null) {
                request.setAttribute("tokenHopLe", true);
                request.setAttribute("token", token);
            } else {
                request.setAttribute("tokenHopLe", false);
            }
            request.getRequestDispatcher("/WEB-INF/views/auth/dat-lai-mat-khau.jsp").forward(request, response);
        }
    }
}
