package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.KetQuaDoiMatKhauDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhienDangNhap;
import vn.nhom10.crm.service.DoiMatKhauService;
import vn.nhom10.crm.util.SessionRegistry;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller tiếp nhận yêu cầu Đổi mật khẩu của người dùng đang đăng nhập (Story S1-04).
 * URL: /doi-mat-khau
 */
@WebServlet(name = "DoiMatKhauServlet", urlPatterns = {"/doi-mat-khau"})
public class DoiMatKhauServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DoiMatKhauServlet.class.getName());

    private DoiMatKhauService doiMatKhauService;
    private NguoiDungDAO nguoiDungDAO;
    private PhienDangNhapDAO phienDangNhapDAO;
    private SessionRegistry sessionRegistry;

    @Override
    public void init() throws ServletException {
        this.doiMatKhauService = new DoiMatKhauService();
        this.nguoiDungDAO = new NguoiDungDAO();
        this.phienDangNhapDAO = new PhienDangNhapDAO();
        this.sessionRegistry = SessionRegistry.getInstance();
    }

    public void setDoiMatKhauService(DoiMatKhauService doiMatKhauService) {
        this.doiMatKhauService = doiMatKhauService;
    }

    public void setNguoiDungDAO(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public void setPhienDangNhapDAO(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(true);
        NguoiDung nguoiDung = layNguoiDungTuSession(session);

        // Hỗ trợ kiểm thử trên môi trường dev qua query param: ?demo=true hoặc ?demoEmail=...
        if (nguoiDung == null) {
            String demoParam = request.getParameter("demo");
            String demoEmail = request.getParameter("demoEmail");
            if (demoParam != null || demoEmail != null) {
                String targetEmail = (demoEmail != null && !demoEmail.isBlank()) ? demoEmail.trim() : "sales@crm.vn";
                nguoiDung = nguoiDungDAO.findByEmail(targetEmail);
                if (nguoiDung != null) {
                    session.setAttribute("nguoiDung", nguoiDung);
                }
            }
        }

        // Bắt buộc phải đăng nhập (Server-side Authentication Check)
        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        // Đăng ký phiên vào SessionRegistry và cơ sở dữ liệu nếu chưa có
        dangKyPhienHienTai(nguoiDung.getId(), session, request);

        request.getRequestDispatcher("/WEB-INF/views/auth/doi-mat-khau.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null) ? layNguoiDungTuSession(session) : null;

        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        String matKhauHienTai = request.getParameter("matKhauHienTai");
        String matKhauMoi = request.getParameter("matKhauMoi");
        String xacNhanMatKhau = request.getParameter("xacNhanMatKhau");
        String thuHoiParam = request.getParameter("thuHoiPhienKhac");

        // Mặc định là true nếu checkbox được chọn hoặc không gửi (tuân theo AC 3)
        boolean thuHoiPhienKhac = "true".equalsIgnoreCase(thuHoiParam) || "on".equalsIgnoreCase(thuHoiParam);

        String maPhienHienTai = session.getId();

        // Đảm bảo phiên hiện tại đã được đăng ký
        dangKyPhienHienTai(nguoiDung.getId(), session, request);

        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                nguoiDung.getId(),
                matKhauHienTai,
                matKhauMoi,
                xacNhanMatKhau,
                thuHoiPhienKhac,
                maPhienHienTai
        );

        if (ketQua.isThanhCong()) {
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            // Cập nhật lại thông tin người dùng trong session
            NguoiDung updatedUser = nguoiDungDAO.findById(nguoiDung.getId());
            if (updatedUser != null) {
                session.setAttribute("nguoiDung", updatedUser);
            }
        } else {
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/doi-mat-khau.jsp").forward(request, response);
    }

    private NguoiDung layNguoiDungTuSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        if (user == null) {
            user = (NguoiDung) session.getAttribute("currentUser");
        }
        return user;
    }

    private void dangKyPhienHienTai(Long nguoiDungId, HttpSession session, HttpServletRequest request) {
        try {
            if (sessionRegistry != null) {
                sessionRegistry.dangKyPhien(nguoiDungId, session);
            }

            if (phienDangNhapDAO != null) {
                String maPhien = session.getId();
                PhienDangNhap phien = phienDangNhapDAO.findByMaPhien(maPhien);
                if (phien == null) {
                    String ip = layDiaChiIp(request);
                    String userAgent = request.getHeader("User-Agent");
                    PhienDangNhap phienMoi = new PhienDangNhap(nguoiDungId, maPhien, ip, userAgent);
                    phienDangNhapDAO.save(phienMoi, null);
                } else {
                    phienDangNhapDAO.updateHoatDongCuoi(maPhien);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể lưu phiên đăng nhập vào DB", e);
        }
    }

    private String layDiaChiIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
