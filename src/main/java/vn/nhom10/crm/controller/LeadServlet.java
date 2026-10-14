package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.LeadService;
import vn.nhom10.crm.service.PermissionService;
import vn.nhom10.crm.util.ExcelLeadUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ Quản lý Khách hàng tiềm năng (Lead) và Tạo Lead thủ công.
 * Phục vụ Story S4-02:
 * - AC1: Nhập tay một lead từ sự kiện hoặc danh thiếp
 * - AC3: Bắt buộc mọi lead nhập vào phải có nguồn
 * URLs:
 * - /lead          : Danh sách khách hàng tiềm năng
 * - /lead/tao-moi  : Giao diện và xử lý tạo lead thủ công
 * - /lead/tao-lead : Alias đường dẫn tạo lead
 */
@WebServlet(name = "LeadServlet", urlPatterns = {"/lead", "/lead/tao-moi", "/lead/tao-lead"})
public class LeadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadServlet.class.getName());

    private final LeadService leadService;
    private final PermissionService permissionService;

    public LeadServlet() {
        this(new LeadService(), PermissionService.getInstance());
    }

    public LeadServlet(LeadService leadService) {
        this(leadService, PermissionService.getInstance());
    }

    public LeadServlet(LeadService leadService, PermissionService permissionService) {
        this.leadService = leadService != null ? leadService : new LeadService();
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

        // 1. Màn hình tạo lead thủ công (/lead/tao-moi, /lead/tao-lead) - Yêu cầu WRITE
        if ("/lead/tao-moi".equals(path) || "/lead/tao-lead".equals(path)) {
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
            request.setAttribute("errorMessage", "Bạn không có quyền xem danh sách khách hàng tiềm năng.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        List<Lead> dsLead = leadService.layDanhSachLead(100, 0);
        request.setAttribute("dsLead", dsLead);
        request.setAttribute("currentUser", userDTO);
        request.setAttribute("nguoiDung", user);
        request.getRequestDispatcher("/WEB-INF/views/lead/danh-sach.jsp").forward(request, response);
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

        // Kiểm tra quyền WRITE để tạo Lead
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền tạo khách hàng tiềm năng.");
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
