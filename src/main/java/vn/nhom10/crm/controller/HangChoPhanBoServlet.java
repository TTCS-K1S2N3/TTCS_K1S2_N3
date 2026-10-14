package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dto.LeadHangChoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.QuyTacPhanBoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet quản lý Hàng chờ phân bổ lead (Story S4-06, AC3).
 * Dành cho Trưởng nhóm kinh doanh (TEAM_LEAD) và Giám đốc (DIRECTOR/ADMIN).
 * Ràng buộc: Trưởng nhóm chỉ được phân bổ lead cho nhân viên trong nhóm mình quản lý.
 */
@WebServlet(name = "HangChoPhanBoServlet", urlPatterns = {
        "/lead/hang-cho-phan-bo",
        "/lead/hang-cho-phan-bo/phan-tay"
})
public class HangChoPhanBoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HangChoPhanBoServlet.class.getName());

    private final QuyTacPhanBoService quyTacPhanBoService;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public HangChoPhanBoServlet() {
        this(new QuyTacPhanBoService(), new NhomKinhDoanhDAO(), new NguoiDungDAO());
    }

    public HangChoPhanBoServlet(QuyTacPhanBoService quyTacPhanBoService, NhomKinhDoanhDAO nhomKinhDoanhDAO, NguoiDungDAO nguoiDungDAO) {
        this.quyTacPhanBoService = quyTacPhanBoService;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
        this.nguoiDungDAO = nguoiDungDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!kiemTraQuyen(request, response)) {
            return;
        }

        hienThiDanhSachHangCho(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!kiemTraQuyen(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/lead/hang-cho-phan-bo/phan-tay".equals(path)) {
            xuLyPhanTay(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/lead/hang-cho-phan-bo");
        }
    }

    private void hienThiDanhSachHangCho(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");

        List<LeadHangChoDTO> dsLead = quyTacPhanBoService.layDanhSachHangCho(50, 0);
        int tongSo = quyTacPhanBoService.demLeadHangCho();

        List<NguoiDung> dsNhanVien = new ArrayList<>();
        if (user.coVaiTro(VaiTroEnum.TEAM_LEAD) && !user.coVaiTro(VaiTroEnum.DIRECTOR) && !user.coVaiTro(VaiTroEnum.ADMIN)) {
            // Team Lead chỉ lấy nhân viên trong nhóm mình
            Integer nhomId = user.getNhomKinhDoanhId();
            if (nhomId != null) {
                dsNhanVien = nhomKinhDoanhDAO.layDsThanhVien(nhomId.longValue());
            }
        } else {
            // Giám đốc / Admin lấy danh sách toàn bộ nhân viên hoạt động
            dsNhanVien = nguoiDungDAO.layDanhSachHoatDong();
        }

        request.setAttribute("dsLeadHangCho", dsLead);
        request.setAttribute("tongSoLeadHangCho", tongSo);
        request.setAttribute("dsNhanVienPhanBo", dsNhanVien);

        request.getRequestDispatcher("/WEB-INF/views/lead/hang-cho-phan-bo.jsp").forward(request, response);
    }

    private void xuLyPhanTay(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");

        try {
            String leadIdStr = request.getParameter("lead_id");
            String nhanVienIdStr = request.getParameter("nhan_vien_id");
            String ghiChu = request.getParameter("ghi_chu");

            if (leadIdStr == null || nhanVienIdStr == null) {
                throw new IllegalArgumentException("Vui lòng chọn lead và nhân viên cần phân bổ.");
            }

            Long leadId = Long.parseLong(leadIdStr.trim());
            Long nhanVienId = Long.parseLong(nhanVienIdStr.trim());

            quyTacPhanBoService.phanTayTuHangCho(leadId, nhanVienId, user, ghiChu);

            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_OK, true, "Phân bổ lead thành công.");
            } else {
                session.setAttribute("successMessage", "Phân bổ lead thành công.");
                response.sendRedirect(request.getContextPath() + "/lead/hang-cho-phan-bo");
            }
        } catch (SecurityException e) {
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_FORBIDDEN, false, e.getMessage());
            } else {
                request.setAttribute("errorMessage", e.getMessage());
                hienThiDanhSachHangCho(request, response);
            }
        } catch (IllegalArgumentException e) {
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, e.getMessage());
            } else {
                request.setAttribute("errorMessage", e.getMessage());
                hienThiDanhSachHangCho(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi phân tay lead: " + e.getMessage(), e);
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi hệ thống: " + e.getMessage());
            } else {
                request.setAttribute("errorMessage", "Lỗi phân bổ lead: " + e.getMessage());
                hienThiDanhSachHangCho(request, response);
            }
        }
    }

    private boolean kiemTraQuyen(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null) ? (NguoiDung) session.getAttribute("nguoiDung") : null;

        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return false;
        }

        // Quyền truy cập: TEAM_LEAD, DIRECTOR, ADMIN
        boolean coQuyen = nguoiDung.coVaiTro(VaiTroEnum.TEAM_LEAD)
                || nguoiDung.coVaiTro(VaiTroEnum.DIRECTOR)
                || nguoiDung.coVaiTro(VaiTroEnum.ADMIN);

        if (!coQuyen) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_FORBIDDEN, false,
                        "Bạn không có quyền truy cập vào Hàng chờ phân bổ lead.");
            } else {
                request.setAttribute("errorMessage", "Bạn không có quyền truy cập vào Hàng chờ phân bổ lead.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return false;
        }
        return true;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xReq = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xReq) || (accept != null && accept.contains("application/json"));
    }

    private void traVeJson(HttpServletResponse response, int status, boolean success, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        String json = "{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}";
        response.getWriter().write(json);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
