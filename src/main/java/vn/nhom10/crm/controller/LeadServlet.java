package vn.nhom10.crm.controller;

import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.BoLocDaLuu;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.BoLocLeadService;
import vn.nhom10.crm.service.PermissionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet điều khiển màn hình Danh sách Lead và Bộ lọc (Story S4-09):
 * - GET /lead: Hiển thị danh sách lead với bộ lọc 5 tiêu chí (AC1), nổi bật Lead quá hạn SLA (AC2) và áp dụng bộ lọc đã lưu (AC3).
 * - POST /lead/bo-loc/luu: Lưu bộ lọc cá nhân có tên (AC3, Contract C-02).
 */
@WebServlet(name = "LeadServlet", urlPatterns = {"/lead", "/lead/*"})
public class LeadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadServlet.class.getName());

    private final BoLocLeadService boLocLeadService;
    private final PermissionService permissionService;

    public LeadServlet() {
        this(new BoLocLeadService(), PermissionService.getInstance());
    }

    public LeadServlet(BoLocLeadService boLocLeadService, PermissionService permissionService) {
        this.boLocLeadService = boLocLeadService != null ? boLocLeadService : new BoLocLeadService();
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung user = kiemTraDangNhap(request, response);
        if (user == null) {
            return;
        }

        // Kiểm tra quyền READ module LEAD (Sales Rep W*, Team Lead, Director, Marketing, Admin)
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.READ)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", NguoiDungDTO.tuNguoiDung(user));
            request.setAttribute("errorMessage", "Bạn không có quyền xem danh sách khách hàng tiềm năng (Lead).");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && (pathInfo.equals("/tao-moi") || pathInfo.equals("/tao-lead"))) {
            chuyenHuongTaoMoi(request, response, user);
            return;
        }

        hienThiDanhSachLead(request, response, user);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung user = kiemTraDangNhap(request, response);
        if (user == null) {
            return;
        }

        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        // Xử lý lưu bộ lọc cá nhân (AC3, Contract C-02)
        if ("/bo-loc/luu".equals(pathInfo) || "/lead/bo-loc/luu".equals(servletPath + (pathInfo != null ? pathInfo : ""))) {
            xuLyLuuBoLoc(request, response, user);
            return;
        }

        // Mặc định chuyển về GET
        doGet(request, response);
    }

    /**
     * Hiển thị danh sách Lead và form bộ lọc (AC1, AC2, AC3).
     */
    private void hienThiDanhSachLead(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws ServletException, IOException {

        BoLocLeadDTO boLoc = new BoLocLeadDTO();

        // Kiểm tra xem có yêu cầu áp dụng Bộ lọc đã lưu qua savedFilterId không (Contract C-03)
        String savedFilterIdStr = request.getParameter("savedFilterId");
        if (savedFilterIdStr != null && !savedFilterIdStr.trim().isEmpty()) {
            try {
                long filterId = Long.parseLong(savedFilterIdStr.trim());
                BoLocDaLuu bl = boLocLeadService.timBoLocTheoId(filterId, user);
                if (bl != null && bl.getTieuChiJson() != null) {
                    boLoc = BoLocLeadDTO.tuJson(bl.getTieuChiJson());
                    boLoc.setSavedFilterId(bl.getId());
                    boLoc.setTenBoLoc(bl.getTenBoLoc());
                } else {
                    request.setAttribute("thongBaoLoi", "Bộ lọc đã lưu không tồn tại hoặc bạn không có quyền truy cập.");
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Mã bộ lọc không hợp lệ: " + savedFilterIdStr);
            }
        } else {
            // Đọc 5 nhóm tiêu chí từ Query Parameters (AC1)
            boLoc.setTrangThai(request.getParameter("trangThai"));

            String nguonStr = request.getParameter("nguonLeadId");
            if (nguonStr != null && !nguonStr.trim().isEmpty()) {
                try {
                    boLoc.setNguonLeadId(Long.parseLong(nguonStr.trim()));
                } catch (NumberFormatException ignored) {
                }
            }

            boLoc.setPhanLoai(request.getParameter("phanLoai"));

            String nguoiPhuTrachStr = request.getParameter("nguoiPhuTrachId");
            if (nguoiPhuTrachStr != null && !nguoiPhuTrachStr.trim().isEmpty()) {
                try {
                    boLoc.setNguoiPhuTrachId(Long.parseLong(nguoiPhuTrachStr.trim()));
                } catch (NumberFormatException ignored) {
                }
            }

            boLoc.setTuNgay(request.getParameter("tuNgay"));
            boLoc.setDenNgay(request.getParameter("denNgay"));
        }

        // Truy vấn danh sách Lead theo bộ lọc và Data Scope
        List<Lead> dsLead = boLocLeadService.timKiemLead(boLoc, user, 100, 0);
        int tongBanGhi = boLocLeadService.demTongSoLead(boLoc, user);

        // Nạp danh mục và danh sách bộ lọc đã lưu của cá nhân
        List<BoLocDaLuu> dsBoLocDaLuu = boLocLeadService.layDanhSachBoLoc(user);
        List<MucDanhMucDTO> luaChonNguon = boLocLeadService.layDanhSachNguonLead();
        List<NguoiDung> luaChonNguoiPhuTrach = boLocLeadService.layDanhSachNguoiPhuTrach(user);

        // Xử lý thông báo thành công (nếu chuyển hướng từ lưu bộ lọc)
        String thanhCong = request.getParameter("thanhCong");
        if ("luu_bo_loc".equals(thanhCong)) {
            request.setAttribute("thongBaoThanhCong", "Đã lưu bộ lọc cá nhân thành công.");
        }

        request.setAttribute("dsLead", dsLead);
        request.setAttribute("boLocHienTai", boLoc);
        request.setAttribute("dsBoLocDaLuu", dsBoLocDaLuu);
        request.setAttribute("luaChonNguon", luaChonNguon);
        request.setAttribute("luaChonNguoiPhuTrach", luaChonNguoiPhuTrach);
        request.setAttribute("tongBanGhi", tongBanGhi);
        request.setAttribute("currentUser", NguoiDungDTO.tuNguoiDung(user));
        request.setAttribute("nguoiDung", user);

        request.getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp").forward(request, response);
    }

    /**
     * Xử lý lưu bộ lọc cá nhân (AC3, Contract C-02).
     */
    private void xuLyLuuBoLoc(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws ServletException, IOException {

        String tenBoLoc = request.getParameter("tenBoLoc");
        if (tenBoLoc == null || tenBoLoc.trim().isEmpty()) {
            request.setAttribute("thongBaoLoi", "Vui lòng nhập tên bộ lọc.");
            hienThiDanhSachLead(request, response, user);
            return;
        }

        String tenClean = tenBoLoc.trim();
        if (tenClean.length() > 80) {
            request.setAttribute("thongBaoLoi", "Tên bộ lọc tối đa 80 ký tự.");
            hienThiDanhSachLead(request, response, user);
            return;
        }

        BoLocLeadDTO tieuChi = new BoLocLeadDTO();
        tieuChi.setTrangThai(request.getParameter("trangThai"));

        String nguonStr = request.getParameter("nguonLeadId");
        if (nguonStr != null && !nguonStr.trim().isEmpty()) {
            try {
                tieuChi.setNguonLeadId(Long.parseLong(nguonStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        tieuChi.setPhanLoai(request.getParameter("phanLoai"));

        String nguoiPhuTrachStr = request.getParameter("nguoiPhuTrachId");
        if (nguoiPhuTrachStr != null && !nguoiPhuTrachStr.trim().isEmpty()) {
            try {
                tieuChi.setNguoiPhuTrachId(Long.parseLong(nguoiPhuTrachStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        tieuChi.setTuNgay(request.getParameter("tuNgay"));
        tieuChi.setDenNgay(request.getParameter("denNgay"));

        try {
            BoLocDaLuu saved = boLocLeadService.luuBoLoc(user, tenClean, tieuChi, false);
            response.sendRedirect(request.getContextPath() + "/lead?savedFilterId=" + saved.getId() + "&thanhCong=luu_bo_loc");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu bộ lọc: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể lưu bộ lọc: " + e.getMessage());
            hienThiDanhSachLead(request, response, user);
        } catch (IllegalArgumentException e) {
            request.setAttribute("thongBaoLoi", e.getMessage());
            hienThiDanhSachLead(request, response, user);
        }
    }

    private void chuyenHuongTaoMoi(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws ServletException, IOException {
        request.setAttribute("currentUser", NguoiDungDTO.tuNguoiDung(user));
        request.setAttribute("nguoiDung", user);
        request.setAttribute("dsNguonLead", boLocLeadService.layDanhSachNguonLead());
        request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
    }

    private NguoiDung kiemTraDangNhap(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return null;
        }
        return (NguoiDung) session.getAttribute("nguoiDung");
    }
}
