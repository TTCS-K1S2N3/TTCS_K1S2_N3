package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.ChuyenDoiLeadPreviewDTO;
import vn.nhom10.crm.dto.KetQuaChuyenDoiLeadDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.LeadChuyenDoiService;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servlet tiếp nhận và xử lý chuyển đổi Lead thành Khách hàng, Người liên hệ và Cơ hội (Story S4-08).
 *
 * Routes:
 * - GET  /lead/chuyen-doi?id={leadId} : Hiển thị màn hình preview chỉ đọc 3 đối tượng sẽ tạo.
 * - POST /lead/chuyen-doi             : Xác nhận chuyển đổi trong 1 atomic transaction (chỉ gửi leadId + csrfToken).
 */
@WebServlet(name = "LeadChuyenDoiServlet", urlPatterns = {"/lead/chuyen-doi"})
public class LeadChuyenDoiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadChuyenDoiServlet.class.getName());

    private final LeadChuyenDoiService leadChuyenDoiService;

    public LeadChuyenDoiServlet() {
        this(new LeadChuyenDoiService());
    }

    public LeadChuyenDoiServlet(LeadChuyenDoiService leadChuyenDoiService) {
        this.leadChuyenDoiService = leadChuyenDoiService != null ? leadChuyenDoiService : new LeadChuyenDoiService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return;
        }

        String idStr = request.getParameter("id");
        Long leadId = parseLong(idStr);
        if (leadId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ: thiếu ID Lead");
            return;
        }

        ChuyenDoiLeadPreviewDTO preview = leadChuyenDoiService.layPreviewChuyenDoi(leadId, currentUser);
        if (preview.getLeadChuyenDoi() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy Lead với ID: " + leadId);
            return;
        }

        if (!preview.isQuyenChuyenDoi()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền chuyển đổi Lead này");
            return;
        }

        if ("DA_CHUYEN_DOI".equalsIgnoreCase(preview.getLeadChuyenDoi().getTrangThaiLead())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }

        // Tạo hoặc lấy CSRF token trong session
        HttpSession session = request.getSession();
        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null || csrfToken.isBlank()) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
        }
        preview.setCsrfToken(csrfToken);

        // Gắn các request attributes theo đúng Contract v1.1
        ganThuocTinhRequest(request, preview, csrfToken);

        request.getRequestDispatcher("/WEB-INF/views/lead/chuyen-doi.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập");
            return;
        }

        String idStr = request.getParameter("leadId");
        Long leadId = parseLong(idStr);
        if (leadId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ: thiếu ID Lead");
            return;
        }

        // Kiểm tra CSRF token
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionCsrf = (String) session.getAttribute("csrfToken");
            String formCsrf = request.getParameter("csrfToken");
            if (sessionCsrf != null && !sessionCsrf.equals(formCsrf)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ");
                return;
            }
        }

        // Thực hiện chuyển đổi qua Service (chỉ gửi leadId, toàn bộ giá trị khác do server tự sinh)
        KetQuaChuyenDoiLeadDTO ketQua = leadChuyenDoiService.chuyenDoiLead(leadId, currentUser);

        if (ketQua.isThanhCong()) {
            // Post/Redirect/Get (PRG) với HTTP 303 See Other sang trang Customer 360
            if (session != null) {
                session.setAttribute("flashSuccess", "Chuyển đổi lead thành công");
            }
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.sendRedirect(request.getContextPath() + "/khach-hang/chi-tiet?id=" + ketQua.getKhachHangId());
            return;
        }

        // Xử lý các mã lỗi theo đúng Contract v1.1
        int statusCode = xacDinhMaHttpLoi(ketQua.getMaLoi());
        response.setStatus(statusCode);

        if (statusCode == HttpServletResponse.SC_BAD_REQUEST || statusCode == HttpServletResponse.SC_NOT_FOUND || statusCode == HttpServletResponse.SC_FORBIDDEN) {
            response.sendError(statusCode, ketQua.getThongBao());
            return;
        }

        // Với lỗi 422 (thiếu thông tin nguồn Lead) hoặc 409 (đã chuyển đổi / trùng): render lại trang preview kèm lỗi
        ChuyenDoiLeadPreviewDTO preview = leadChuyenDoiService.layPreviewChuyenDoi(leadId, currentUser);
        preview.setThongBao(ketQua.getThongBao());
        if (ketQua.getLoiTruong() != null && !ketQua.getLoiTruong().isEmpty()) {
            preview.getLoiTruong().putAll(ketQua.getLoiTruong());
        }

        String csrfToken = session != null ? (String) session.getAttribute("csrfToken") : "";
        ganThuocTinhRequest(request, preview, csrfToken);

        request.getRequestDispatcher("/WEB-INF/views/lead/chuyen-doi.jsp").forward(request, response);
    }

    private int xacDinhMaHttpLoi(String maLoi) {
        if (maLoi == null) return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        switch (maLoi) {
            case "LEAD_INVALID_ID":
                return HttpServletResponse.SC_BAD_REQUEST;
            case "LEAD_NOT_FOUND":
                return HttpServletResponse.SC_NOT_FOUND;
            case "LEAD_ACCESS_DENIED":
                return HttpServletResponse.SC_FORBIDDEN;
            case "LEAD_SOURCE_INVALID":
                return 422; // Unprocessable Entity
            case "LEAD_ALREADY_CONVERTED":
            case "CUSTOMER_ALREADY_EXISTS":
            case "LEAD_NOT_QUALIFIED":
            case "PIPELINE_DEFAULT_UNAVAILABLE":
                return HttpServletResponse.SC_CONFLICT; // 409
            default:
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
        }
    }

    private void ganThuocTinhRequest(HttpServletRequest request, ChuyenDoiLeadPreviewDTO preview, String csrfToken) {
        request.setAttribute("preview", preview);
        request.setAttribute("leadChuyenDoi", preview.getLeadChuyenDoi());
        request.setAttribute("thongTinTaoKhachHang", preview.getThongTinTaoKhachHang());
        request.setAttribute("thongTinTaoLienHe", preview.getThongTinTaoLienHe());
        request.setAttribute("thongTinTaoCoHoi", preview.getThongTinTaoCoHoi());
        request.setAttribute("danhSachGiaiDoan", preview.getDanhSachGiaiDoan());
        request.setAttribute("quyenChuyenDoi", preview.isQuyenChuyenDoi());
        request.setAttribute("loiTruong", preview.getLoiTruong());
        request.setAttribute("canhBaoTrung", preview.getCanhBaoTrung());
        request.setAttribute("thongBao", preview.getThongBao());
        request.setAttribute("csrfToken", csrfToken);
    }

    private NguoiDung layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object user = session.getAttribute(PhienService.SESSION_USER_KEY);
        if (user instanceof NguoiDung) {
            return (NguoiDung) user;
        }
        Object userAlt = session.getAttribute("nguoiDung");
        if (userAlt instanceof NguoiDung) {
            return (NguoiDung) userAlt;
        }
        return null;
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
