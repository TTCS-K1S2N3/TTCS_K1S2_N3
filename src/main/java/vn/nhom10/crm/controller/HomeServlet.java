package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;

import java.io.IOException;

/**
 * Controller điều hướng và hiển thị trang chủ tương ứng với vai trò người dùng (AC1).
 * URL: /home và root context
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home", ""})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("user");
        VaiTro vaiTroChinh = user.getVaiTroChinh();

        String tieuDeTrangChu = "Trang chủ hệ thống CRM";
        String moTaVaiTro = "Không gian làm việc chung";

        if (vaiTroChinh != null) {
            String code = vaiTroChinh.getMaVaiTro();
            switch (code) {
                case "ADMIN" -> {
                    tieuDeTrangChu = "Trang chủ Quản trị hệ thống";
                    moTaVaiTro = "Không gian quản trị tài khoản, phân quyền và giám sát hệ thống CRM";
                }
                case "DIRECTOR" -> {
                    tieuDeTrangChu = "Trang chủ Giám đốc kinh doanh";
                    moTaVaiTro = "Không gian điều hành toàn bộ dữ liệu kinh doanh và chiến lược bán hàng";
                }
                case "TEAM_LEAD" -> {
                    tieuDeTrangChu = "Trang chủ Trưởng nhóm kinh doanh";
                    moTaVaiTro = "Không gian theo dõi dữ liệu, chỉ tiêu và hoạt động của đội ngũ kinh doanh";
                }
                case "SALES_REP" -> {
                    tieuDeTrangChu = "Trang chủ Nhân viên kinh doanh";
                    moTaVaiTro = "Không gian quản lý khách hàng, cơ hội và công việc được giao phụ trách";
                }
                case "MARKETING" -> {
                    tieuDeTrangChu = "Trang chủ Marketing";
                    moTaVaiTro = "Không gian quản lý nguồn lead, chiến dịch và thu hút khách hàng tiềm năng";
                }
                case "CUST_SUCCESS" -> {
                    tieuDeTrangChu = "Trang chủ Chăm sóc khách hàng";
                    moTaVaiTro = "Không gian theo dõi sức khỏe khách hàng, hỗ trợ và phòng ngừa rủi ro rời bỏ";
                }
                case "ACCOUNTANT" -> {
                    tieuDeTrangChu = "Trang chủ Kế toán";
                    moTaVaiTro = "Không gian theo dõi báo giá, hợp đồng đã duyệt và tiến độ thanh toán";
                }
                default -> {
                    tieuDeTrangChu = "Trang chủ: " + vaiTroChinh.getTenVaiTro();
                    moTaVaiTro = vaiTroChinh.getMoTa();
                }
            }
        }

        req.setAttribute("tieuDeTrangChu", tieuDeTrangChu);
        req.setAttribute("moTaVaiTro", moTaVaiTro);
        req.setAttribute("vaiTroChinh", vaiTroChinh);

        req.getRequestDispatcher("/WEB-INF/views/home/index.jsp").forward(req, resp);
    }
}
