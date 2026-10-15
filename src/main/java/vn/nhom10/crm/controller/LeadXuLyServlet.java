package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.LeadChiTietViewDTO;
import vn.nhom10.crm.dto.XuLyLeadRequestDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.LeadTiepNhanService;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet xử lý hiển thị chi tiết Lead và tiếp nhận/từ chối Lead (Story S4-07, AC1, AC2).
 * Mapping URL:
 * - GET  /lead/chi-tiet : Xem thông tin chi tiết, trạng thái SLA và các nút hành động (LEAD07_VIEW)
 * - POST /lead/xu-ly    : Xử lý hành động Nhận (NHAN) hoặc Từ chối (TU_CHOI) theo chuẩn PRG
 */
@WebServlet(name = "LeadXuLyServlet", urlPatterns = {"/lead/chi-tiet", "/lead/xu-ly"})
public class LeadXuLyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadXuLyServlet.class.getName());
    public static final String VIEW_CHI_TIET_JSP = "/WEB-INF/views/lead/chi-tiet-lead.jsp";

    private final LeadTiepNhanService leadTiepNhanService;

    public LeadXuLyServlet() {
        this(new LeadTiepNhanService());
    }

    public LeadXuLyServlet(LeadTiepNhanService leadTiepNhanService) {
        this.leadTiepNhanService = (leadTiepNhanService != null) ? leadTiepNhanService : new LeadTiepNhanService();
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

        String leadIdParam = request.getParameter("leadId");
        if (leadIdParam == null || leadIdParam.isBlank()) {
            leadIdParam = request.getParameter("id");
        }

        if (leadIdParam == null || leadIdParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số mã định danh Lead (leadId)");
            return;
        }

        Long leadId;
        try {
            leadId = Long.parseLong(leadIdParam.trim());
            if (leadId <= 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã định danh Lead phải là số nguyên dương");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã định danh Lead không hợp lệ");
            return;
        }

        try {
            LeadChiTietViewDTO viewDTO = leadTiepNhanService.layChiTietLeadChoNguoiDung(leadId, currentUser);

            // Xử lý thông báo flash từ session
            HttpSession session = request.getSession(false);
            if (session != null) {
                String flashSuccess = (String) session.getAttribute("flashThanhCong");
                if (flashSuccess != null) {
                    viewDTO.setThongBao(flashSuccess);
                    session.removeAttribute("flashThanhCong");
                }
                String flashError = (String) session.getAttribute("flashLoi");
                if (flashError != null) {
                    request.setAttribute("thongBaoLoi", flashError);
                    session.removeAttribute("flashLoi");
                }
            }

            // Gán dữ liệu sang request attributes theo đúng Contract mục 2.3
            request.setAttribute("leadChiTiet", viewDTO.getLead());
            request.setAttribute("coTheNhan", viewDTO.isCoTheNhan());
            request.setAttribute("coTheTuChoi", viewDTO.isCoTheTuChoi());
            request.setAttribute("trangThaiSla", viewDTO.getTrangThaiSla().name());
            request.setAttribute("danhMucLyDoTuChoi", viewDTO.getDanhMucLyDoTuChoi());
            request.setAttribute("soThongBaoChuaDoc", viewDTO.getSoThongBaoChuaDoc());
            request.setAttribute("thongBao", viewDTO.getThongBao());
            request.setAttribute("viewDTO", viewDTO);

            request.getRequestDispatcher(VIEW_CHI_TIET_JSP).forward(request, response);

        } catch (LoiKhongTimThayException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (LoiPhanQuyenException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hiển thị chi tiết lead id=" + leadId + ": " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi tải chi tiết Lead");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        NguoiDung currentUser = layNguoiDungDangNhap(request);
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yêu cầu đăng nhập");
            return;
        }

        String leadIdParam = request.getParameter("leadId");
        String hanhDong = request.getParameter("hanhDong");
        String maLyDoTuChoi = request.getParameter("maLyDoTuChoi");
        String lyDoTuChoi = request.getParameter("lyDoTuChoi");

        Long leadId = null;
        if (leadIdParam != null && !leadIdParam.isBlank()) {
            try {
                leadId = Long.parseLong(leadIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        XuLyLeadRequestDTO requestDTO = new XuLyLeadRequestDTO(leadId, hanhDong, maLyDoTuChoi, lyDoTuChoi);
        Map<String, String> loi = requestDTO.kiemTraHopLe();

        if (!loi.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            if (leadId != null && leadId > 0) {
                try {
                    LeadChiTietViewDTO viewDTO = leadTiepNhanService.layChiTietLeadChoNguoiDung(leadId, currentUser);
                    viewDTO.setLoiTruong(loi);
                    viewDTO.setMaLyDoTuChoiDaChon(maLyDoTuChoi);
                    viewDTO.setLyDoTuChoiDaNhap(lyDoTuChoi);

                    request.setAttribute("leadChiTiet", viewDTO.getLead());
                    request.setAttribute("coTheNhan", viewDTO.isCoTheNhan());
                    request.setAttribute("coTheTuChoi", viewDTO.isCoTheTuChoi());
                    request.setAttribute("trangThaiSla", viewDTO.getTrangThaiSla().name());
                    request.setAttribute("danhMucLyDoTuChoi", viewDTO.getDanhMucLyDoTuChoi());
                    request.setAttribute("loiTruong", loi);
                    request.setAttribute("maLyDoTuChoiDaChon", maLyDoTuChoi);
                    request.setAttribute("lyDoTuChoiDaNhap", lyDoTuChoi);
                    request.setAttribute("thongBaoLoi", loi.values().iterator().next());
                    request.setAttribute("viewDTO", viewDTO);

                    request.getRequestDispatcher(VIEW_CHI_TIET_JSP).forward(request, response);
                    return;
                } catch (Exception ignored) {
                }
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, loi.values().iterator().next());
            return;
        }

        try {
            if (XuLyLeadRequestDTO.HANH_DONG_NHAN.equalsIgnoreCase(hanhDong)) {
                // AC1: Nhận lead
                leadTiepNhanService.nhanLead(leadId, currentUser);
                HttpSession session = request.getSession();
                session.setAttribute("flashThanhCong", "Đã nhận lead thành công");
                response.sendRedirect(request.getContextPath() + "/lead/chi-tiet?leadId=" + leadId);

            } else if (XuLyLeadRequestDTO.HANH_DONG_TU_CHOI.equalsIgnoreCase(hanhDong)) {
                // AC2: Từ chối lead
                leadTiepNhanService.tuChoiLead(requestDTO, currentUser);
                HttpSession session = request.getSession();
                session.setAttribute("flashThanhCong", "Lead đã được trả về hàng chờ phân bổ");
                response.sendRedirect(request.getContextPath() + "/lead");
            }

        } catch (LoiKhongTimThayException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (LoiPhanQuyenException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalStateException e) {
            // Trạng thái stale hoặc từ chối sau khi đã nhận (Q3) -> 409 Conflict
            response.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý lead id=" + leadId + ": " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi xử lý hệ thống: " + e.getMessage());
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
