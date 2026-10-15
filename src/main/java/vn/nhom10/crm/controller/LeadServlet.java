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
 * Controller phục vụ Quản lý Khách hàng tiềm năng (Lead), Tạo/Sửa/Xóa Lead và Chấm điểm Lead.
 * Phục vụ Story S4-02:
 * - AC1: Nhập tay một lead từ sự kiện hoặc danh thiếp
 * - AC2: Cập nhật / Sửa thông tin Lead, Xóa Lead
 * - AC3: Bắt buộc mọi lead nhập vào phải có nguồn
 * Phục vụ Story S4-05:
 * - Tự động tính lại điểm & phân loại khi sửa Lead (AC2)
 * - Tích hợp chức năng chấm điểm trực tiếp từ màn hình Lead
 * URLs:
 * - /lead               : Danh sách khách hàng tiềm năng
 * - /lead/tao-moi       : Giao diện và xử lý tạo lead thủ công
 * - /lead/tao-lead      : Alias đường dẫn tạo lead
 * - /lead/sua           : Giao diện và xử lý cập nhật lead
 * - /lead/xoa           : Xóa lead khỏi hệ thống
 * - /lead/cham-diem-lead: Tính lại điểm cho 1 lead
 * - /lead/tinh-lai-diem : Tính lại điểm toàn bộ lead
 */
@WebServlet(name = "LeadServlet", urlPatterns = {
        "/lead",
        "/lead/tao-moi",
        "/lead/tao-lead",
        "/lead/sua",
        "/lead/xoa",
        "/lead/cham-diem-lead",
        "/lead/tinh-lai-diem"
})
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
            request.setAttribute("isEditMode", false);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
            return;
        }

        // 2. Màn hình chỉnh sửa lead (/lead/sua) - Yêu cầu WRITE
        if ("/lead/sua".equals(path)) {
            if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("errorMessage", "Bạn không có quyền chỉnh sửa khách hàng tiềm năng.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/lead");
                return;
            }

            try {
                Long id = Long.parseLong(idStr.trim());
                Lead lead = leadService.timTheoId(id);
                if (lead == null) {
                    response.sendRedirect(request.getContextPath() + "/lead?loi=not_found");
                    return;
                }

                request.setAttribute("formData", lead);
                request.setAttribute("isEditMode", true);
                request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("nguoiDung", user);
                request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
                return;
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/lead");
                return;
            }
        }

        // 3. Màn hình danh sách lead (/lead) - Yêu cầu READ
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
        String path = request.getServletPath();
        String actionParam = request.getParameter("action");

        // Kiểm tra quyền WRITE để thao tác
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isAjax(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Từ chối thao tác: Yêu cầu quyền WRITE.\"}");
                return;
            }
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác trên khách hàng tiềm năng.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        // A. XỬ LÝ XÓA LEAD (/lead/xoa hoặc /lead với action=xoa)
        if ("/lead/xoa".equals(path) || ("xoa".equals(actionParam) && "/lead".equals(path))) {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isBlank()) {
                idStr = request.getParameter("leadId");
            }
            if (idStr != null && !idStr.isBlank()) {
                try {
                    Long id = Long.parseLong(idStr.trim());
                    boolean daXoa = leadService.xoaLead(id, user.getId());
                    if (isAjax(request)) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":" + daXoa + ",\"message\":\"" +
                                (daXoa ? "Xóa khách hàng tiềm năng thành công." : "Không thể xóa khách hàng tiềm năng.") + "\"}");
                        return;
                    }
                    if (daXoa) {
                        response.sendRedirect(request.getContextPath() + "/lead?thanhCong=xoa");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/lead?loi=xoa-that-bai");
                    }
                    return;
                } catch (NumberFormatException ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/lead");
            return;
        }

        // B. XỬ LÝ CHẤM ĐIỂM 1 LEAD (/lead/cham-diem-lead)
        if ("/lead/cham-diem-lead".equals(path)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isBlank()) {
                try {
                    Long id = Long.parseLong(idStr.trim());
                    Lead lead = leadService.chamDiemLead(id);
                    if (isAjax(request)) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":true,\"diem\":" + (lead != null ? lead.getDiem() : 0) +
                                ",\"phanLoai\":\"" + (lead != null ? lead.getPhanLoai() : "") + "\"}");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/lead?thanhCong=cham-diem&maLead=" + (lead != null ? lead.getMaLead() : ""));
                    return;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Lỗi chấm điểm lead ID=" + idStr + ": " + e.getMessage(), e);
                }
            }
            response.sendRedirect(request.getContextPath() + "/lead");
            return;
        }

        // C. XỬ LÝ TÍNH LẠI ĐIỂM TOÀN BỘ LEAD (/lead/tinh-lai-diem)
        if ("/lead/tinh-lai-diem".equals(path)) {
            try {
                int count = leadService.tinhLaiDiemToanBoLead();
                if (isAjax(request)) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":true,\"count\":" + count + ",\"message\":\"Đã tính lại điểm cho " + count + " lead.\"}");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/lead?thanhCong=tinh-lai-diem&count=" + count);
                return;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi tính lại điểm toàn bộ lead: " + e.getMessage(), e);
                response.sendRedirect(request.getContextPath() + "/lead?loi=tinh-diem-that-bai");
                return;
            }
        }

        // D. XỬ LÝ TẠO MỚI HOẶC SỬA LEAD
        boolean isEdit = "/lead/sua".equals(path) || "sua".equals(actionParam);
        String idStr = request.getParameter("id");
        Long id = null;
        if (isEdit && idStr != null && !idStr.isBlank()) {
            try {
                id = Long.parseLong(idStr.trim());
            } catch (NumberFormatException ignored) {}
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
        lead.setId(id);
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
            request.setAttribute("isEditMode", isEdit);
            request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
            return;
        }

        try {
            if (isEdit) {
                // Xử lý CẬP NHẬT Lead
                Lead leadCapNhat = leadService.capNhatLead(lead, user.getId());
                response.sendRedirect(request.getContextPath() + "/lead?thanhCong=cap-nhat&maLead=" + leadCapNhat.getMaLead());
            } else {
                // Xử lý TẠO MỚI Lead
                Lead leadMoi = leadService.taoLeadThuCong(lead, user.getId());

                if ("true".equalsIgnoreCase(tiepTuc)) {
                    // Người dùng chọn "Lưu & Tiếp tục thêm mới"
                    request.setAttribute("thongBaoThanhCong", "Tạo khách hàng tiềm năng thành công (" + leadMoi.getMaLead() + "). Bạn có thể tiếp tục tạo lead mới.");
                    request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
                    request.setAttribute("isEditMode", false);
                    request.setAttribute("currentUser", userDTO);
                    request.setAttribute("nguoiDung", user);
                    request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
                } else {
                    // Điều hướng về trang danh sách lead
                    response.sendRedirect(request.getContextPath() + "/lead?thanhCong=tao-moi&maLead=" + leadMoi.getMaLead());
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật Lead: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể lưu thông tin khách hàng tiềm năng: " + e.getMessage());
            request.setAttribute("formData", lead);
            request.setAttribute("isEditMode", isEdit);
            request.setAttribute("dsNguonLead", leadService.layDanhSachNguonLead());
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.getRequestDispatcher("/WEB-INF/views/lead/tao-lead.jsp").forward(request, response);
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xrw) || (accept != null && accept.contains("application/json"));
    }
}
