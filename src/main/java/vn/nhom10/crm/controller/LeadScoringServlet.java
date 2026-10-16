package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.CauHinhChamDiemDTO;
import vn.nhom10.crm.dto.NguongPhanLoaiLeadDTO;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.dto.TieuChiChamDiemDTO;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LeadScoringService;
import vn.nhom10.crm.service.MenuService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ Cấu hình chấm điểm và Ngưỡng phân loại Lead (Story S4-05).
 * Vai trò chính: Giám đốc kinh doanh (Director) / Quản trị viên (Admin).
 * 
 * Các AC đáp ứng:
 * - AC1: Khai báo tiêu chí và số điểm (ngành nghề, quy mô, nguồn, mức độ quan tâm)
 * - AC2: Điểm được tính lại tự động khi thông tin lead thay đổi
 * - AC3: Phân loại Nóng, Ấm, Lạnh theo ngưỡng điểm khai báo được
 * - AC4: Điểm chỉ để sắp xếp ưu tiên, không tự động loại lead
 * 
 * URLs:
 * - /lead/cham-diem      : Trang giao diện cấu hình chấm điểm lead
 * - /lead-scoring/config : Đường dẫn theo chuẩn REST / Contract Mock chung
 */
@WebServlet(name = "LeadScoringServlet", urlPatterns = {"/lead/cham-diem", "/lead-scoring/config"})
public class LeadScoringServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadScoringServlet.class.getName());
    private static final long serialVersionUID = 1L;

    private final MenuService menuService;
    private final LeadScoringService leadScoringService;

    public LeadScoringServlet() {
        this(MenuService.getInstance(), LeadScoringService.getInstance());
    }

    public LeadScoringServlet(MenuService menuService) {
        this(menuService, LeadScoringService.getInstance());
    }

    public LeadScoringServlet(MenuService menuService, LeadScoringService leadScoringService) {
        this.menuService = menuService != null ? menuService : MenuService.getInstance();
        this.leadScoringService = leadScoringService != null ? leadScoringService : LeadScoringService.getInstance();
    }

    public LeadScoringServlet(LeadScoringService leadScoringService) {
        this(MenuService.getInstance(), leadScoringService);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung nguoiDung = kiemTraDangNhap(request, response);
        if (nguoiDung == null) {
            return;
        }

        // Kiểm tra quyền module Lead
        if (!menuService.kiemTraQuyenTruyCap(nguoiDung, ModuleHeThong.LEAD)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Bạn không có quyền truy cập vào chức năng cấu hình chấm điểm lead.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        boolean canEdit = kiemTraQuyenChinhSua(nguoiDung);
        CauHinhChamDiemDTO currentConfig = leadScoringService.layCauHinhHienTai();

        // Chuẩn bị dữ liệu hiển thị cho JSP
        request.setAttribute("scoringConfig", currentConfig);
        request.setAttribute("criteriaRows", currentConfig.getDanhSachTieuChi());
        request.setAttribute("thresholds", currentConfig.getNguongPhanLoai());
        request.setAttribute("canEditScoringConfig", canEdit);

        // Dữ liệu sidebar layout S1-06
        ThongTinDieuHuongDTO thongTinDieuHuong = menuService.layThongTinDieuHuong(nguoiDung, request.getContextPath());
        request.setAttribute("thongTinDieuHuong", thongTinDieuHuong);

        // Xử lý thông báo flash từ session (nếu có)
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("flashSuccess") != null) {
                request.setAttribute("thongBaoThanhCong", session.getAttribute("flashSuccess"));
                session.removeAttribute("flashSuccess");
            }
            if (session.getAttribute("flashError") != null) {
                request.setAttribute("thongBaoLoi", session.getAttribute("flashError"));
                session.removeAttribute("flashError");
            }
        }

        // Nếu request yêu cầu JSON (API Contract Mock)
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            traVeJsonCauHinh(response, currentConfig);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/lead/cau-hinh-cham-diem.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung nguoiDung = kiemTraDangNhap(request, response);
        if (nguoiDung == null) {
            return;
        }

        if (!kiemTraQuyenChinhSua(nguoiDung)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"Chỉ Giám đốc kinh doanh hoặc Quản trị viên mới có quyền cập nhật cấu hình chấm điểm.\"}}");
            } else {
                request.setAttribute("errorMessage", "Bạn không có quyền chỉnh sửa cấu hình chấm điểm lead.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return;
        }

        CauHinhChamDiemDTO currentConfig = leadScoringService.layCauHinhHienTai();

        // Nhận dữ liệu form
        String hotMinStr = request.getParameter("hotMin");
        String warmMinStr = request.getParameter("warmMin");

        Map<String, String> fieldErrors = new HashMap<>();

        int hotMin = 70;
        int warmMin = 40;

        try {
            if (hotMinStr == null || hotMinStr.isBlank()) {
                fieldErrors.put("thresholds.hotMin", "Ngưỡng điểm Nóng không được để trống.");
            } else {
                hotMin = Integer.parseInt(hotMinStr.trim());
                if (hotMin < 1 || hotMin > 100) {
                    fieldErrors.put("thresholds.hotMin", "Ngưỡng điểm Nóng phải từ 1 đến 100.");
                }
            }
        } catch (NumberFormatException e) {
            fieldErrors.put("thresholds.hotMin", "Ngưỡng điểm Nóng phải là số nguyên.");
        }

        try {
            if (warmMinStr == null || warmMinStr.isBlank()) {
                fieldErrors.put("thresholds.warmMin", "Ngưỡng điểm Ấm không được để trống.");
            } else {
                warmMin = Integer.parseInt(warmMinStr.trim());
                if (warmMin < 0 || warmMin >= 100) {
                    fieldErrors.put("thresholds.warmMin", "Ngưỡng điểm Ấm phải từ 0 đến 99.");
                }
            }
        } catch (NumberFormatException e) {
            fieldErrors.put("thresholds.warmMin", "Ngưỡng điểm Ấm phải là số nguyên.");
        }

        if (hotMin <= warmMin && !fieldErrors.containsKey("thresholds.hotMin") && !fieldErrors.containsKey("thresholds.warmMin")) {
            fieldErrors.put("thresholds.hotMin", "Ngưỡng Nóng (" + hotMin + ") phải lớn hơn ngưỡng Ấm (" + warmMin + ").");
        }

        // Đọc cấu hình các tiêu chí
        CauHinhChamDiemDTO updatedConfig = new CauHinhChamDiemDTO();
        updatedConfig.setPhienBan(currentConfig.getPhienBan());
        updatedConfig.setNgayCapNhat(LocalDateTime.now());
        updatedConfig.setNguoiCapNhat(nguoiDung.getHoTen() != null ? nguoiDung.getHoTen() : "Giám đốc kinh doanh");
        updatedConfig.setNguongPhanLoai(new NguongPhanLoaiLeadDTO(hotMin, warmMin));

        boolean coTieuChiKichHoat = false;

        for (TieuChiChamDiemDTO tcGoc : currentConfig.getDanhSachTieuChi()) {
            String key = tcGoc.getKey();
            String enabledParam = request.getParameter("enabled_" + key);
            String pointsParam = request.getParameter("points_" + key);

            boolean enabled = "true".equalsIgnoreCase(enabledParam) || "1".equals(enabledParam) || "on".equalsIgnoreCase(enabledParam);
            int points = tcGoc.getSoDiem();

            if (pointsParam != null && !pointsParam.isBlank()) {
                try {
                    points = Integer.parseInt(pointsParam.trim());
                    if (points < 0 || points > 100) {
                        fieldErrors.put("criteria[" + key + "].points", "Điểm của tiêu chí " + tcGoc.getTenTieuChi() + " phải từ 0 đến 100.");
                    }
                } catch (NumberFormatException e) {
                    fieldErrors.put("criteria[" + key + "].points", "Điểm của tiêu chí " + tcGoc.getTenTieuChi() + " phải là số nguyên.");
                }
            }

            TieuChiChamDiemDTO tcMoi = new TieuChiChamDiemDTO(
                    tcGoc.getKey(), tcGoc.getMaQuyTac(), tcGoc.getTenTieuChi(),
                    tcGoc.getMoTa(), tcGoc.getIcon(), enabled, points, tcGoc.getThuTu()
            );
            tcMoi.setTuyChonGiaTri(tcGoc.getTuyChonGiaTri());
            updatedConfig.getDanhSachTieuChi().add(tcMoi);

            if (enabled) {
                coTieuChiKichHoat = true;
            }
        }

        if (!coTieuChiKichHoat) {
            fieldErrors.put("criteria", "Phải kích hoạt ít nhất một tiêu chí chấm điểm.");
        }

        // Nếu có lỗi validation
        if (!fieldErrors.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            if (isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                StringBuilder json = new StringBuilder("{\"success\":false,\"error\":{\"code\":\"VALIDATION_ERROR\",\"message\":\"Vui lòng kiểm tra lại cấu hình.\",\"fieldErrors\":{");
                boolean first = true;
                for (Map.Entry<String, String> entry : fieldErrors.entrySet()) {
                    if (!first) json.append(",");
                    json.append("\"").append(entry.getKey()).append("\":\"").append(escapeJson(entry.getValue())).append("\"");
                    first = false;
                }
                json.append("}}}");
                response.getWriter().write(json.toString());
            } else {
                request.setAttribute("thongBaoLoi", "Dữ liệu cấu hình không hợp lệ. Vui lòng kiểm tra lại các trường.");
                request.setAttribute("fieldErrors", fieldErrors);
                request.setAttribute("scoringConfig", updatedConfig);
                request.setAttribute("criteriaRows", updatedConfig.getDanhSachTieuChi());
                request.setAttribute("thresholds", updatedConfig.getNguongPhanLoai());
                request.setAttribute("canEditScoringConfig", true);
                ThongTinDieuHuongDTO thongTinDieuHuong = menuService.layThongTinDieuHuong(nguoiDung, request.getContextPath());
                request.setAttribute("thongTinDieuHuong", thongTinDieuHuong);
                request.getRequestDispatcher("/WEB-INF/views/lead/cau-hinh-cham-diem.jsp").forward(request, response);
            }
            return;
        }

        // Cập nhật cấu hình qua Service
        boolean luuThanhCong = leadScoringService.luuCauHinh(updatedConfig, nguoiDung.getId(), nguoiDung.getHoTen());

        if (!luuThanhCong) {
            LOGGER.log(Level.SEVERE, "Lưu cấu hình chấm điểm lead thất bại cho user {0}", nguoiDung.getId());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if (isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"INTERNAL_ERROR\",\"message\":\"Không thể lưu cấu hình chấm điểm lead vào cơ sở dữ liệu. Vui lòng thử lại sau.\"}}");
            } else {
                request.setAttribute("thongBaoLoi", "Có lỗi xảy ra khi lưu cấu hình vào hệ thống. Vui lòng thử lại sau.");
                request.setAttribute("scoringConfig", currentConfig);
                request.setAttribute("criteriaRows", currentConfig.getDanhSachTieuChi());
                request.setAttribute("thresholds", currentConfig.getNguongPhanLoai());
                request.setAttribute("canEditScoringConfig", true);
                ThongTinDieuHuongDTO thongTinDieuHuong = menuService.layThongTinDieuHuong(nguoiDung, request.getContextPath());
                request.setAttribute("thongTinDieuHuong", thongTinDieuHuong);
                request.getRequestDispatcher("/WEB-INF/views/lead/cau-hinh-cham-diem.jsp").forward(request, response);
            }
            return;
        }

        LOGGER.log(Level.INFO, "Cấu hình chấm điểm lead đã được cập nhật bởi user {0}", nguoiDung.getId());

        if (isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":true,\"message\":\"Đã lưu cấu hình chấm điểm lead thành công!\",\"configVersion\":" + updatedConfig.getPhienBan() + "}");
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("flashSuccess", "Đã lưu cấu hình chấm điểm lead thành công (Phiên bản " + updatedConfig.getPhienBan() + ")!");
            response.sendRedirect(request.getContextPath() + "/lead/cham-diem");
        }
    }

    private NguoiDung kiemTraDangNhap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null) ? (NguoiDung) session.getAttribute("nguoiDung") : null;
        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return null;
        }
        return nguoiDung;
    }

    private boolean kiemTraQuyenChinhSua(NguoiDung nguoiDung) {
        if (nguoiDung == null) return false;
        Set<VaiTroEnum> vaiTros = nguoiDung.getDanhSachVaiTroEnum();
        return vaiTros.contains(VaiTroEnum.DIRECTOR) || vaiTros.contains(VaiTroEnum.ADMIN);
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith) || (accept != null && accept.contains("application/json"));
    }

    private void traVeJsonCauHinh(HttpServletResponse response, CauHinhChamDiemDTO config) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"configVersion\":").append(config.getPhienBan()).append(",");
        json.append("\"thresholds\":{\"warmMin\":").append(config.getNguongPhanLoai().getWarmMin())
            .append(",\"hotMin\":").append(config.getNguongPhanLoai().getHotMin()).append("},");
        json.append("\"criteria\":[");
        boolean first = true;
        for (TieuChiChamDiemDTO tc : config.getDanhSachTieuChi()) {
            if (!first) json.append(",");
            json.append("{\"key\":\"").append(escapeJson(tc.getKey())).append("\",")
                .append("\"label\":\"").append(escapeJson(tc.getTenTieuChi())).append("\",")
                .append("\"enabled\":").append(tc.isKichHoat()).append(",")
                .append("\"points\":").append(tc.getSoDiem()).append("}");
            first = false;
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
