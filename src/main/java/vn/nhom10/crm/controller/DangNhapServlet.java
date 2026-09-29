package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller hiển thị trang đăng nhập.
 * URL: /dang-nhap
 */
@WebServlet(name = "DangNhapServlet", urlPatterns = {"/dang-nhap"})
public class DangNhapServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp").forward(request, response);
    }
}
