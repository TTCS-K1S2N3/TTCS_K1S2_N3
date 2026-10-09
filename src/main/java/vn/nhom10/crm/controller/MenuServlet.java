package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.filter.NavigationFilter;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.MenuService;

import java.io.IOException;

/**
 * Servlet điều hướng và kiểm thử menu theo phân quyền người dùng (Story S1-06).
 */
@WebServlet(name = "MenuServlet", urlPatterns = {"/dieu-huong", "/menu-demo"})
public class MenuServlet extends HttpServlet {

    private MenuService menuService;

    @Override
    public void init() throws ServletException {
        menuService = MenuService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung nguoiDung = (NguoiDung) session.getAttribute("nguoiDung");
        String currentUri = request.getRequestURI();
        ThongTinDieuHuongDTO thongTinDieuHuong =
    menuService.layThongTinDieuHuong(
        nguoiDung,
        currentUri,
        request.getContextPath()
    );
        request.setAttribute("thongTinDieuHuong", thongTinDieuHuong);
        request.setAttribute("nguoiDungHienTai", nguoiDung);

        request.getRequestDispatcher("/WEB-INF/views/layout/demo-navigation.jsp").forward(request, response);
    }
}
