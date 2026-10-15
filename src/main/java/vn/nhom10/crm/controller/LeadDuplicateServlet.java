package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.GopLeadDAO;
import vn.nhom10.crm.dto.DuplicatePermissionsDTO;
import vn.nhom10.crm.dto.KetQuaGopLeadDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.SoSanhLeadDTO;
import vn.nhom10.crm.dto.UngVienTrungLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.GopLeadService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller xử lý luồng Cảnh báo và Gộp Lead trùng (Story S4-04).
 * Hỗ trợ các hành động theo Contract FE–BE:
 * - DUP_LIST: GET /lead/duplicates (hoặc GET /lead/duplicate?action=list)
 * - DUP_CHECK_LEAD: GET /lead/duplicate?action=check&leadId={id}
 * - DUP_PREVIEW: GET /lead/duplicate?action=preview&sourceLeadId={id1}&targetLeadId={id2}
 * - DUP_LINK_CUSTOMER: POST /lead/duplicate?action=linkCustomer
 * - DUP_MERGE: POST /lead/duplicate?action=merge
 */
@WebServlet(name = "LeadDuplicateServlet", urlPatterns = {"/lead/duplicate", "/lead/duplicates"})
public class LeadDuplicateServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadDuplicateServlet.class.getName());

    private GopLeadService gopLeadService;
    private GopLeadDAO gopLeadDAO;

    public LeadDuplicateServlet() {
        this(new GopLeadService(), new GopLeadDAO());
    }

    public LeadDuplicateServlet(GopLeadService gopLeadService, GopLeadDAO gopLeadDAO) {
        this.gopLeadService = gopLeadService != null ? gopLeadService : new GopLeadService();
        this.gopLeadDAO = gopLeadDAO != null ? gopLeadDAO : new GopLeadDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        NguoiDungDTO user = layNguoiDungHienTai(request);
        if (user == null) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập để tiếp tục.");
            } else {
                response.sendRedirect(request.getContextPath() + "/dang-nhap");
            }
            return;
        }

        if (!gopLeadService.coQuyenXem(user)) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Bạn không có quyền truy cập hoặc thao tác trên bản ghi này.");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Bạn không có quyền truy cập chức năng cảnh báo và gộp Lead.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return;
        }

        String path = request.getServletPath();
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "/lead/duplicates".equals(path) ? "list" : "check";
        }

        // Đọc flash message nếu có từ PRG trước đó
        xuLyFlashMessage(request);

        switch (action.trim()) {
            case "list":
                xuLyDanhSachTrung(request, response, user);
                break;
            case "check":
                xuLyKiemTraLead(request, response, user);
                break;
            case "preview":
                xuLyXemTruocGop(request, response, user);
                break;
            default:
                if (isAjaxOrJsonRequest(request)) {
                    sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "Hành động không hợp lệ.");
                } else {
                    response.sendRedirect(request.getContextPath() + "/lead/duplicates");
                }
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        NguoiDungDTO user = layNguoiDungHienTai(request);
        if (user == null) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập để tiếp tục.");
            } else {
                response.sendRedirect(request.getContextPath() + "/dang-nhap");
            }
            return;
        }

        if (!gopLeadService.coQuyenThaoTac(user)) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này.");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action.trim()) {
            case "linkCustomer":
                xuLyPostGanKhachHang(request, response, user);
                break;
            case "merge":
                xuLyPostGopLead(request, response, user);
                break;
            default:
                if (isAjaxOrJsonRequest(request)) {
                    sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "Hành động POST không hợp lệ.");
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
                }
                break;
        }
    }

    // =========================================================================
    // XỬ LÝ GET
    // =========================================================================

    private void xuLyDanhSachTrung(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO user)
            throws ServletException, IOException {
        DuplicatePermissionsDTO permissions = new DuplicatePermissionsDTO(
                gopLeadService.coQuyenXem(user),
                gopLeadService.coQuyenThaoTac(user),
                gopLeadService.coQuyenThaoTac(user)
        );

        request.setAttribute("permissions", permissions);

        if (isAjaxOrJsonRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":true,\"leadCandidates\":[],\"customerCandidates\":[]}");
            }
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/lead/duplicate-list.jsp").forward(request, response);
    }

    private void xuLyKiemTraLead(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO user)
            throws ServletException, IOException {
        String leadIdStr = request.getParameter("leadId");
        if (leadIdStr == null || leadIdStr.trim().isEmpty()) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "Mã Lead không được để trống.");
            } else {
                response.sendRedirect(request.getContextPath() + "/lead/duplicates");
            }
            return;
        }

        Long leadId;
        try {
            leadId = Long.parseLong(leadIdStr.trim());
            if (leadId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Mã Lead không hợp lệ.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã Lead không hợp lệ.");
            }
            return;
        }

        Lead lead;
        try {
            lead = gopLeadDAO.layChiTietLead(leadId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy chi tiết Lead: " + e.getMessage(), e);
            lead = null;
        }

        if (lead == null) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Không tìm thấy bản ghi hợp lệ.");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy Lead.");
            }
            return;
        }

        List<UngVienTrungLeadDTO> leadCandidates = gopLeadService.layLeadUngVien(leadId, user);
        List<UngVienTrungLeadDTO> customerCandidates = gopLeadService.layCustomerUngVien(leadId, user);

        DuplicatePermissionsDTO permissions = gopLeadService.layQuyenThaoTac(user, lead, null);
        if (permissions == null) {
            permissions = new DuplicatePermissionsDTO(gopLeadService.coQuyenXem(user), gopLeadService.coQuyenThaoTac(user), false);
        }

        request.setAttribute("lead", lead);
        request.setAttribute("leadCandidates", leadCandidates);
        request.setAttribute("customerCandidates", customerCandidates);
        request.setAttribute("permissions", permissions);
        request.setAttribute("fieldErrors", Collections.emptyMap());

        if (isAjaxOrJsonRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                StringBuilder sb = new StringBuilder();
                sb.append("{")
                        .append("\"lead\":{\"leadId\":").append(lead.getId())
                        .append(",\"displayName\":\"").append(escapeJson(lead.getHoTen())).append("\"")
                        .append(",\"email\":").append(lead.getEmail() != null ? "\"" + escapeJson(lead.getEmail()) + "\"" : "null")
                        .append(",\"phone\":").append(lead.getSoDienThoai() != null ? "\"" + escapeJson(lead.getSoDienThoai()) + "\"" : "null")
                        .append(",\"companyName\":").append(lead.getTenCongTy() != null ? "\"" + escapeJson(lead.getTenCongTy()) + "\"" : "null")
                        .append("},")
                        .append("\"leadCandidates\":").append(toJsonCandidates(leadCandidates)).append(",")
                        .append("\"customerCandidates\":").append(toJsonCandidates(customerCandidates)).append(",")
                        .append("\"permissions\":{")
                        .append("\"canViewCandidates\":").append(permissions.isCanViewCandidates()).append(",")
                        .append("\"canLinkCustomer\":").append(permissions.isCanLinkCustomer()).append(",")
                        .append("\"canMerge\":").append(permissions.isCanMerge())
                        .append("},")
                        .append("\"fieldErrors\":{}")
                        .append("}");
                out.print(sb.toString());
            }
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/lead/duplicate-check.jsp").forward(request, response);
    }

    private void xuLyXemTruocGop(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO user)
            throws ServletException, IOException {
        String sourceIdStr = request.getParameter("sourceLeadId");
        String targetIdStr = request.getParameter("targetLeadId");

        if (sourceIdStr == null || targetIdStr == null || sourceIdStr.trim().isEmpty() || targetIdStr.trim().isEmpty()) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "Vui lòng chọn đầy đủ Lead nguồn và Lead đích.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số Lead.");
            }
            return;
        }

        Long sourceId;
        Long targetId;
        try {
            sourceId = Long.parseLong(sourceIdStr.trim());
            targetId = Long.parseLong(targetIdStr.trim());
        } catch (NumberFormatException e) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "Mã Lead không hợp lệ.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã Lead không hợp lệ.");
            }
            return;
        }

        if (sourceId.equals(targetId)) {
            if (isAjaxOrJsonRequest(request)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"httpStatus\":400,\"fieldErrors\":{\"sourceLeadId\":\"Lead nguồn phải khác lead được giữ lại.\",\"targetLeadId\":\"Lead được giữ lại phải khác lead nguồn.\"},\"mergeResult\":null}");
                }
            } else {
                request.setAttribute("errorMessage", "Không thể gộp một Lead vào chính nó.");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Không thể gộp một Lead vào chính nó.");
            }
            return;
        }

        SoSanhLeadDTO soSanh = gopLeadService.layDuLieuSoSanh(sourceId, targetId, user);
        if (soSanh == null || soSanh.getSourceLead() == null || soSanh.getTargetLead() == null) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Không tìm thấy bản ghi hợp lệ.");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bản ghi hợp lệ.");
            }
            return;
        }

        DuplicatePermissionsDTO permissions = gopLeadService.layQuyenThaoTac(user, soSanh.getSourceLead(), soSanh.getTargetLead());

        request.setAttribute("sourceLead", soSanh.getSourceLead());
        request.setAttribute("targetLead", soSanh.getTargetLead());
        request.setAttribute("soSanh", soSanh);
        request.setAttribute("permissions", permissions);

        if (isAjaxOrJsonRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"sourceLead\":{\"leadId\":" + sourceId + "},\"targetLead\":{\"leadId\":" + targetId + "},\"canMerge\":" + permissions.isCanMerge() + "}");
            }
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/lead/duplicate-preview.jsp").forward(request, response);
    }

    // =========================================================================
    // XỬ LÝ POST
    // =========================================================================

    private void xuLyPostGanKhachHang(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO user)
            throws IOException {
        String leadIdStr = request.getParameter("leadId");
        String customerIdStr = request.getParameter("customerId");

        if (leadIdStr == null || customerIdStr == null || leadIdStr.trim().isEmpty() || customerIdStr.trim().isEmpty()) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "Vui lòng chọn đầy đủ Lead và Khách hàng.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số.");
            }
            return;
        }

        Long leadId;
        Long customerId;
        try {
            leadId = Long.parseLong(leadIdStr.trim());
            customerId = Long.parseLong(customerIdStr.trim());
        } catch (NumberFormatException e) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "ID không hợp lệ.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
            }
            return;
        }

        boolean ok = gopLeadService.ganKhachHang(leadId, customerId, user, request.getRemoteAddr(), request.getHeader("User-Agent"));
        if (ok) {
            String msg = "Đã gắn Lead với khách hàng hiện có thành công.";
            if (isAjaxOrJsonRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"leadId\":" + leadId + ",\"suggestedCustomerId\":" + customerId + ",\"linkResult\":{\"leadId\":" + leadId + ",\"customerId\":" + customerId + ",\"linked\":true},\"flashMessage\":\"" + msg + "\"}");
                }
                return;
            }
            datFlashMessage(request, msg);
            response.sendRedirect(request.getContextPath() + "/lead/duplicate?action=check&leadId=" + leadId);
        } else {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR", "Không thể liên kết Khách hàng với Lead.");
            } else {
                datFlashMessage(request, "Không thể gắn Khách hàng vào Lead. Vui lòng thử lại.");
                response.sendRedirect(request.getContextPath() + "/lead/duplicate?action=check&leadId=" + leadId);
            }
        }
    }

    private void xuLyPostGopLead(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO user)
            throws IOException {
        String sourceIdStr = request.getParameter("sourceLeadId");
        String targetIdStr = request.getParameter("targetLeadId");
        String lyDo = request.getParameter("lyDo");

        if (sourceIdStr == null || targetIdStr == null || sourceIdStr.trim().isEmpty() || targetIdStr.trim().isEmpty()) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "Vui lòng chọn Lead nguồn và Lead đích.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số.");
            }
            return;
        }

        Long sourceId;
        Long targetId;
        try {
            sourceId = Long.parseLong(sourceIdStr.trim());
            targetId = Long.parseLong(targetIdStr.trim());
        } catch (NumberFormatException e) {
            if (isAjaxOrJsonRequest(request)) {
                sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ID", "ID không hợp lệ.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID không hợp lệ.");
            }
            return;
        }

        if (sourceId.equals(targetId)) {
            if (isAjaxOrJsonRequest(request)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"httpStatus\":400,\"fieldErrors\":{\"sourceLeadId\":\"Lead nguồn phải khác lead được giữ lại.\",\"targetLeadId\":\"Lead được giữ lại phải khác lead nguồn.\"},\"mergeResult\":null}");
                }
            } else {
                datFlashMessage(request, "Không thể gộp một Lead vào chính nó.");
                response.sendRedirect(request.getContextPath() + "/lead/duplicate?action=preview&sourceLeadId=" + sourceId + "&targetLeadId=" + targetId);
            }
            return;
        }

        if (lyDo == null || lyDo.trim().isEmpty()) {
            lyDo = "Gộp lead trùng thông tin liên hệ";
        }

        KetQuaGopLeadDTO kq = gopLeadService.thucHienGopLead(sourceId, targetId, lyDo, user, request.getRemoteAddr(), request.getHeader("User-Agent"));

        if (kq.isThanhCong()) {
            if (isAjaxOrJsonRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"mergeResult\":{\"survivorLeadId\":" + kq.getSurvivorLeadId()
                            + ",\"mergedSourceLeadId\":" + kq.getMergedSourceLeadId()
                            + ",\"soHoatDongDaChuyen\":" + kq.getSoHoatDongDaChuyen()
                            + ",\"soCongViecDaChuyen\":" + kq.getSoCongViecDaChuyen()
                            + ",\"soCoHoiDaChuyen\":" + kq.getSoCoHoiDaChuyen()
                            + "},\"flashMessage\":\"" + escapeJson(kq.getThongDiep()) + "\"}");
                }
                return;
            }
            datFlashMessage(request, kq.getThongDiep());
            response.sendRedirect(request.getContextPath() + "/lead/duplicate?action=check&leadId=" + targetId);
        } else {
            int statusCode = kq.getThongDiep() != null && kq.getThongDiep().contains("đã được gộp")
                    ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST;

            if (isAjaxOrJsonRequest(request)) {
                response.setStatus(statusCode);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"httpStatus\":" + statusCode + ",\"error\":{\"code\":\"MERGE_ERROR\",\"message\":\"" + escapeJson(kq.getThongDiep()) + "\"},\"mergeResult\":null}");
                }
                return;
            }
            datFlashMessage(request, kq.getThongDiep());
            response.sendRedirect(request.getContextPath() + "/lead/duplicate?action=preview&sourceLeadId=" + sourceId + "&targetLeadId=" + targetId);
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private NguoiDungDTO layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object objUser = session.getAttribute("nguoiDung");
        if (objUser instanceof NguoiDung) {
            return NguoiDungDTO.tuNguoiDung((NguoiDung) objUser);
        }
        if (objUser instanceof NguoiDungDTO) {
            return (NguoiDungDTO) objUser;
        }

        Object objDto = session.getAttribute("user");
        if (objDto instanceof NguoiDungDTO) {
            return (NguoiDungDTO) objDto;
        }
        return null;
    }

    private boolean isAjaxOrJsonRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        String format = request.getParameter("format");
        return (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || "json".equalsIgnoreCase(format);
    }

    private void sendJsonError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print("{\"httpStatus\":" + status + ",\"error\":{\"code\":\"" + escapeJson(code) + "\",\"message\":\"" + escapeJson(message) + "\"}}");
        }
    }

    private void xuLyFlashMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object msg = session.getAttribute("flashMessage");
            if (msg != null) {
                request.setAttribute("flashMessage", msg);
                session.removeAttribute("flashMessage");
            }
        }
    }

    private void datFlashMessage(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flashMessage", message);
    }

    private String toJsonCandidates(List<UngVienTrungLeadDTO> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            UngVienTrungLeadDTO c = list.get(i);
            sb.append("{")
                    .append("\"candidateKind\":\"").append(escapeJson(c.getCandidateKind())).append("\"")
                    .append(",\"recordId\":").append(c.getRecordId())
                    .append(",\"maskedLabel\":\"").append(escapeJson(c.getMaskedLabel())).append("\"")
                    .append(",\"maskedContact\":").append(c.getMaskedContact() != null ? "\"" + escapeJson(c.getMaskedContact()) + "\"" : "null")
                    .append(",\"matchReasons\":[");
            for (int j = 0; j < c.getMatchReasons().size(); j++) {
                sb.append("\"").append(escapeJson(c.getMatchReasons().get(j))).append("\"");
                if (j < c.getMatchReasons().size() - 1) sb.append(",");
            }
            sb.append("]")
                    .append(",\"canViewDetail\":").append(c.isCanViewDetail())
                    .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
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
