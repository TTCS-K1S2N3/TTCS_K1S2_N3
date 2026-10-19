package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.ThongBaoDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.ThongBao;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet quản lý thông báo và mở thông báo chuông (Story S4-07, AC3, Q5).
 * URL:
 * - POST /thong-bao/mo : Đánh dấu đã đọc cho đúng recipient khi nhấn item và redirect an toàn (LEAD07_NOTIFY_OPEN)
 * - GET  /thong-bao    : Lấy danh sách thông báo của tài khoản đăng nhập (LEAD07_ALERT_VIEW)
 */
@WebServlet(name = "ThongBaoServlet", urlPatterns = {"/thong-bao", "/thong-bao/mo"})
public class ThongBaoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ThongBaoServlet.class.getName());

    private final ThongBaoDAO thongBaoDAO;

    public ThongBaoServlet() {
        this(new ThongBaoDAO());
    }

    public ThongBaoServlet(ThongBaoDAO thongBaoDAO) {
        this.thongBaoDAO = (thongBaoDAO != null) ? thongBaoDAO : new ThongBaoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        NguoiDung currentUser = layNguoiDungDangNhap(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        List<ThongBao> dsThongBao = thongBaoDAO.layDanhSachTheoNguoiDung((long) currentUser.getId(), false, 50);
        int soChuaDoc = thongBaoDAO.demThongBaoChuaDoc((long) currentUser.getId());

        request.setAttribute("danhSachThongBaoCuaToi", dsThongBao);
        request.setAttribute("soThongBaoChuaDoc", soChuaDoc);

        // Forward to common notifications view or dashboard
        request.getRequestDispatcher("/WEB-INF/views/common/thong-bao.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungDangNhap(request);
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yêu cầu đăng nhập");
            return;
        }

        String path = request.getServletPath();
        if ("/thong-bao/mo".equals(path)) {
            String thongBaoIdParam = request.getParameter("thongBaoId");
            if (thongBaoIdParam == null || thongBaoIdParam.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã thông báo (thongBaoId)");
                return;
            }

            Long thongBaoId;
            try {
                thongBaoId = Long.parseLong(thongBaoIdParam.trim());
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã thông báo không hợp lệ");
                return;
            }

            ThongBao tb = thongBaoDAO.timTheoId(thongBaoId);
            if (tb == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thông báo");
                return;
            }

            // Chống IDOR: Chỉ người nhận (recipient) trong session mới có quyền đánh dấu đọc
            if (tb.getNguoiDungId() == null || !tb.getNguoiDungId().equals((long) currentUser.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thao tác trên thông báo này");
                return;
            }

            // Đánh dấu đã đọc khi nhấn item (idempotent)
            thongBaoDAO.danhDauDaDoc(thongBaoId, (long) currentUser.getId());

            String duongDanMo = tb.getDuongDanMo();
            if (duongDanMo != null && !duongDanMo.isBlank() && duongDanMo.startsWith("/")) {
                response.sendRedirect(request.getContextPath() + duongDanMo);
            } else {
                response.sendRedirect(request.getContextPath() + "/tong-quan");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action không hợp lệ");
        }
    }

    private NguoiDung layNguoiDungDangNhap(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute("nguoiDung");
            if (obj instanceof NguoiDung) {
                return (NguoiDung) obj;
            }
        }
        return null;
    }
}
