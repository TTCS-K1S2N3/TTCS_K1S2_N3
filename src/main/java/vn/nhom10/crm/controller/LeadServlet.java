package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.BoLocDaLuu;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.BoLocLeadService;
import vn.nhom10.crm.service.LeadService;
import vn.nhom10.crm.service.PermissionService;
import vn.nhom10.crm.util.ExcelLeadUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ Quản lý Khách hàng tiềm năng (Lead):
 * - S4-02: Tạo lead thủ công, danh sách lead, validation, bắt buộc nguồn.
 * - S4-09: Lọc Lead theo 5 tiêu chí (AND), nổi bật SLA, lưu và áp dụng bộ lọc cá nhân.
 *
 * URLs:
 * - /lead          : Danh sách khách hàng tiềm năng kèm bộ lọc
 * - /lead/tao-moi  : Giao diện và xử lý tạo lead thủ công (GET/POST)
 * - /lead/tao-lead : Alias đường dẫn tạo lead (GET/POST)
 * - /lead/bo-loc/luu: Lưu bộ lọc tìm kiếm cá nhân (POST)
 */
@WebServlet(name = "LeadServlet", urlPatterns = {"/lead", "/lead/tao-moi", "/lead/tao-lead", "/lead/bo-loc/luu"})
public class LeadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadServlet.class.getName());

    private final LeadService leadService;
    private final BoLocLeadService boLocLeadService;
    private final PermissionService permissionService;

    public LeadServlet() {
        this(new LeadService(), new BoLocLeadService(), PermissionService.getInstance());
    }

    public LeadServlet(LeadService leadService) {
        this(leadService, new BoLocLeadService(), PermissionService.getInstance());
    }

    public LeadServlet(BoLocLeadService boLocLeadService) {
        this(new LeadService(), boLocLeadService, PermissionService.getInstance());
    }

    public LeadServlet(BoLocLeadService boLocLeadService, PermissionService permissionService) {
        this(new LeadService(), boLocLeadService, permissionService);
    }

    public LeadServlet(LeadService leadService, PermissionService permissionService) {
        this(leadService, new BoLocLeadService(), permissionService);
    }

    public LeadServlet(LeadService leadService, BoLocLeadService boLocLeadService, PermissionService permissionService) {
        this.leadService = leadService != null ? leadService : new LeadService();
        this.boLocLeadService = boLocLeadService != null ? boLocLeadService : new BoLocLeadService();
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);
        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();

        // 1. Màn hình tạo lead thủ công (/lead/tao-moi, /lead/tao-lead) - Yêu cầu WRITE
        if ("/lead/tao-moi".equals(path) || "/lead/tao-lead".equals(path)
                || (pathInfo != null && (pathInfo.equals("/tao-moi") || pathInfo.equals("/tao-lead")))) {
            if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("errorMessage", "Bạn không có quyền tạo khách hàng tiềm năng (yêu cầu quyền WRITE).");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
                return;
            }

            List<MucDanhMucDTO> dsNguonLead = leadService.layDanhSachNguonLead();
            request.setAttribute("dsNguonLead", dsNguonLead);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
            return;
        }

        // 2. Màn hình danh sách lead (/lead) - Yêu cầu READ
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.READ)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền xem danh sách khách hàng tiềm năng (Lead).");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        hienThiDanhSachLead(request, response, user, userDTO);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);
        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String fullPath = path + (pathInfo != null ? pathInfo : "");

        // 1. Xử lý lưu bộ lọc cá nhân (S4-09, Contract C-02, POST /lead/bo-loc/luu)
        if ("/lead/bo-loc/luu".equals(path) || "/bo-loc/luu".equals(pathInfo) || "/lead/bo-loc/luu".equals(fullPath)) {
            xuLyLuuBoLoc(request, response, user, userDTO);
            return;
        }

        // 2. Xử lý tạo Lead thủ công (S4-02, POST /lead/tao-moi, /lead/tao-lead)
        if ("/lead/tao-moi".equals(path) || "/lead/tao-lead".equals(path)
                || (pathInfo != null && (pathInfo.equals("/tao-moi") || pathInfo.equals("/tao-lead")))) {
            xuLyTaoLeadThuCong(request, response, user, userDTO);
            return;
        }

        // Mặc định chuyển về GET
        doGet(request, response);
    }

    /**
     * Hiển thị danh sách Lead và form bộ lọc (Story S4-02 & S4-09, AC1, AC2, AC3).
     */
    private void hienThiDanhSachLead(HttpServletRequest request, HttpServletResponse response,
                                     NguoiDung user, NguoiDungDTO userDTO)
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

        // Xử lý thông báo thành công (nếu chuyển hướng từ lưu bộ lọc hoặc tạo mới)
        String thanhCong = request.getParameter("thanhCong");
        if ("luu_bo_loc".equals(thanhCong)) {
            request.setAttribute("thongBaoThanhCong", "Đã lưu bộ lọc cá nhân thành công.");
        } else if ("tao-moi".equals(thanhCong)) {
            String maLead = request.getParameter("maLead");
            request.setAttribute("thongBaoThanhCong", "Tạo khách hàng tiềm năng thành công" +
                    (maLead != null && !maLead.isBlank() ? " (" + maLead + ")" : "") + ".");
        }

        // CSRF Token cho modal lưu bộ lọc
        HttpSession session = request.getSession(false);
        if (session != null) {
            String csrfToken = (String) session.getAttribute("csrfToken");
            if (csrfToken == null || csrfToken.isBlank()) {
                csrfToken = UUID.randomUUID().toString();
                session.setAttribute("csrfToken", csrfToken);
            }
            request.setAttribute("csrfToken", csrfToken);
        }

        request.setAttribute("dsLead", dsLead);
        request.setAttribute("boLocHienTai", boLoc);
        request.setAttribute("dsBoLocDaLuu", dsBoLocDaLuu);
        request.setAttribute("luaChonNguon", luaChonNguon);
        request.setAttribute("luaChonNguoiPhuTrach", luaChonNguoiPhuTrach);
        request.setAttribute("tongBanGhi", tongBanGhi);
        request.setAttribute("currentUser", userDTO);
        request.setAttribute("nguoiDung", user);

        request.getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp").forward(request, response);
    }

    /**
     * Xử lý lưu bộ lọc cá nhân (AC3, Contract C-02).
     */
    private void xuLyLuuBoLoc(HttpServletRequest request, HttpServletResponse response,
                              NguoiDung user, NguoiDungDTO userDTO)
            throws ServletException, IOException {

        // Kiểm tra quyền READ module LEAD
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.READ)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền lưu bộ lọc khách hàng tiềm năng.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        // Kiểm tra CSRF token nếu session đã cấp token
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionCsrf = (String) session.getAttribute("csrfToken");
            if (sessionCsrf != null && !sessionCsrf.isBlank()) {
                String formCsrf = request.getParameter("csrfToken");
                if (formCsrf == null || formCsrf.isBlank() || !sessionCsrf.equals(formCsrf)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ hoặc bị thiếu");
                    return;
                }
            }
        }

        String tenBoLoc = request.getParameter("tenBoLoc");
        if (tenBoLoc == null || tenBoLoc.trim().isEmpty()) {
            request.setAttribute("thongBaoLoi", "Vui lòng nhập tên bộ lọc.");
            hienThiDanhSachLead(request, response, user, userDTO);
            return;
        }

        String tenClean = tenBoLoc.trim();
        if (tenClean.length() > 80) {
            request.setAttribute("thongBaoLoi", "Tên bộ lọc tối đa 80 ký tự.");
            hienThiDanhSachLead(request, response, user, userDTO);
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
            hienThiDanhSachLead(request, response, user, userDTO);
        } catch (IllegalArgumentException e) {
            request.setAttribute("thongBaoLoi", e.getMessage());
            hienThiDanhSachLead(request, response, user, userDTO);
        }
    }

    /**
     * Xử lý tạo Lead thủ công (Story S4-02: AC1 & AC3).
     */
    private void xuLyTaoLeadThuCong(HttpServletRequest request, HttpServletResponse response,
                                    NguoiDung user, NguoiDungDTO userDTO)
            throws ServletException, IOException {

        // Kiểm tra quyền WRITE để tạo Lead
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền tạo khách hàng tiềm năng (yêu cầu quyền WRITE).");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        // Đọc các trường từ form submit
        String hoTen = request.getParameter("ho_ten");
        String tenCongTy = request.getParameter("ten_cong_ty");
        String soDienThoai = request.getParameter("so_dien_thoai");
        String email = request.getParameter("email");
        String nguonLeadIdStr = request.getParameter("nguon_lead_id");
        String mucDoQuanTam = request.getParameter("muc_do_quan_tam");
        String nhuCauQuanTam = request.getParameter("nhu_cau_quan_tam");
        String ghiChu = request.getParameter("ghi_chu");
        String tiepTuc = request.getParameter("tiepTuc");

        Map<String, String> errors = new HashMap<>();

        // Thẩm định Họ và tên (Bắt buộc)
        if (hoTen == null || hoTen.trim().isEmpty()) {
            errors.put("hoTen", "Vui lòng nhập họ và tên của khách hàng tiềm năng.");
        } else if (hoTen.trim().length() > 150) {
            errors.put("hoTen", "Họ và tên không được vượt quá 150 ký tự.");
        }

        // Thẩm định Nguồn Lead (BẮT BUỘC THEO AC3)
        Long nguonLeadId = null;
        if (nguonLeadIdStr == null || nguonLeadIdStr.trim().isEmpty()) {
            errors.put("nguonLeadId", "Mỗi lead nhập vào đều bắt buộc có nguồn (Tiêu chí AC3). Vui lòng chọn nguồn lead.");
        } else {
            try {
                nguonLeadId = Long.parseLong(nguonLeadIdStr.trim());
                if (nguonLeadId <= 0) {
                    errors.put("nguonLeadId", "Mỗi lead nhập vào đều bắt buộc có nguồn (Tiêu chí AC3). Vui lòng chọn nguồn lead.");
                }
            } catch (NumberFormatException e) {
                errors.put("nguonLeadId", "Mã nguồn lead không hợp lệ.");
            }
        }

        // Thẩm định Email (nếu có)
        if (email != null && !email.trim().isEmpty()) {
            String em = email.trim();
            if (em.length() > 255) {
                errors.put("email", "Địa chỉ email không được vượt quá 255 ký tự.");
            } else if (!ExcelLeadUtil.EMAIL_PATTERN.matcher(em).matches()) {
                errors.put("email", "Địa chỉ email không đúng định dạng (Ví dụ: ten@congty.vn).");
            }
        }

        // Thẩm định Số điện thoại (nếu có)
        if (soDienThoai != null && !soDienThoai.trim().isEmpty()) {
            String sdtClean = soDienThoai.trim().replaceAll("[\\s.-]", "");
            if (sdtClean.length() > 20) {
                errors.put("soDienThoai", "Số điện thoại không được vượt quá 20 ký tự.");
            } else if (!ExcelLeadUtil.PHONE_PATTERN.matcher(sdtClean).matches()) {
                errors.put("soDienThoai", "Số điện thoại phải gồm 8-15 chữ số hợp lệ.");
            }
        }

        // Chuẩn bị đối tượng Lead lưu giữ dữ liệu người dùng đã nhập
        Lead lead = new Lead();
        lead.setHoTen(hoTen != null ? hoTen.trim() : "");
        lead.setTenCongTy(tenCongTy != null ? tenCongTy.trim() : null);
        lead.setSoDienThoai(soDienThoai != null ? soDienThoai.trim() : null);
        lead.setEmail(email != null ? email.trim() : null);
        lead.setNguonLeadId(nguonLeadId);
        lead.setMucDoQuanTam(mucDoQuanTam != null && !mucDoQuanTam.isBlank() ? mucDoQuanTam.trim() : "TRUNG_BINH");
        lead.setNhuCauQuanTam(nhuCauQuanTam != null ? nhuCauQuanTam.trim() : null);
        lead.setGhiChu(ghiChu != null && !ghiChu.isBlank() ? ghiChu.trim() : null);

        // Nếu có lỗi validation -> Trả lại giao diện kèm thông báo lỗi và giữ nguyên dữ liệu
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("formData", lead);
            request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
            return;
        }

        try {
            Lead leadMoi = leadService.taoLeadThuCong(lead, user.getId());

            if ("true".equalsIgnoreCase(tiepTuc)) {
                // Người dùng chọn "Lưu & Tiếp tục thêm mới"
                request.setAttribute("thongBaoThanhCong", "Tạo khách hàng tiềm năng thành công (" + leadMoi.getMaLead() + "). Bạn có thể tiếp tục tạo lead mới.");
                request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("nguoiDung", user);
                request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
            } else {
                // Điều hướng về trang danh sách lead
                response.sendRedirect(request.getContextPath() + "/lead?thanhCong=tao-moi&maLead=" + leadMoi.getMaLead());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu Lead thủ công: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể lưu thông tin khách hàng tiềm năng: " + e.getMessage());
            request.setAttribute("formData", lead);
            request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
        }
    }
}
