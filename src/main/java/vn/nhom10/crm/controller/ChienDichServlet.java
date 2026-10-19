package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.ChienDichDTO;
import vn.nhom10.crm.dto.ChienDichPermissionsDTO;
import vn.nhom10.crm.dto.ChienDichRowDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.ChienDichService;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý các yêu cầu liên quan đến Quản lý Chiến dịch Marketing (Story S4-03).
 * Khớp hoàn toàn với Contract FE-BE đã duyệt (mục 3.1 và 3.2):
 * - CAM_LIST:     GET  /marketing/campaigns (hoặc /marketing/campaign, /chien-dich)
 * - CAM_NEW_FORM: GET  /marketing/campaign?action=new (hoặc /chien-dich/tao)
 * - CAM_CREATE:   POST /marketing/campaign?action=create (hoặc /chien-dich/tao)
 * - CAM_DETAIL:   GET  /marketing/campaign?action=detail&id=... (hoặc /chien-dich/chi-tiet)
 */
@WebServlet(name = "ChienDichServlet", urlPatterns = {
        "/marketing/campaigns",
        "/marketing/campaign",
        "/chien-dich",
        "/chien-dich/tao",
        "/chien-dich/chi-tiet"
})
public class ChienDichServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ChienDichService chienDichService;

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.chienDichService == null) {
            this.chienDichService = new ChienDichService();
        }
    }

    public void setChienDichService(ChienDichService chienDichService) {
        this.chienDichService = chienDichService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        NguoiDung nguoiDung = layNguoiDungHienTai(request);
        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return;
        }

        ChienDichPermissionsDTO permissions = chienDichService.layQuyenNguoiDung(nguoiDung);
        if (!permissions.isCanView()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Bạn không có quyền truy cập vào chức năng Chiến dịch marketing.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        String action = layHanhDong(request);

        switch (action) {
            case "new":
            case "tao":
                xuLyHienThiFormTao(request, response, permissions);
                break;
            case "detail":
            case "chi-tiet":
                xuLyHienThiChiTiet(request, response, permissions);
                break;
            case "list":
            default:
                xuLyHienThiDanhSach(request, response, permissions);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung nguoiDung = layNguoiDungHienTai(request);
        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return;
        }

        ChienDichPermissionsDTO permissions = chienDichService.layQuyenNguoiDung(nguoiDung);
        if (!permissions.isCanCreate()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Bạn không có quyền khai báo chiến dịch marketing.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        // Đọc các trường từ request theo đúng Contract FE-BE (mục 3.1)
        String name = request.getParameter("name");
        if (name == null || name.isBlank()) {
            // Hỗ trợ alias tên tiếng Việt
            name = request.getParameter("tenChienDich");
        }

        String budgetAmount = request.getParameter("budgetAmount");
        if (budgetAmount == null || budgetAmount.isBlank()) {
            budgetAmount = request.getParameter("nganSach");
        }

        String startDate = request.getParameter("startDate");
        if (startDate == null || startDate.isBlank()) {
            startDate = request.getParameter("ngayBatDau");
        }

        String endDate = request.getParameter("endDate");
        if (endDate == null || endDate.isBlank()) {
            endDate = request.getParameter("ngayKetThuc");
        }

        String channelCode = request.getParameter("channelCode");
        if (channelCode == null || channelCode.isBlank()) {
            channelCode = request.getParameter("kenh");
        }

        String moTa = request.getParameter("moTa");

        ChienDichDTO form = new ChienDichDTO();
        form.setName(name);
        form.setBudgetAmountStr(budgetAmount);
        form.setStartDateStr(startDate);
        form.setEndDateStr(endDate);
        form.setChannelCode(channelCode);
        form.setMoTa(moTa);

        Map<String, String> fieldErrors = new HashMap<>();
        Long newId = chienDichService.taoChienDich(form, nguoiDung.getId(), fieldErrors);

        if (newId != null) {
            // Post/Redirect/Get (PRG) thành công
            HttpSession session = request.getSession();
            session.setAttribute("flashMessage", "Đã tạo chiến dịch thành công.");
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.sendRedirect(request.getContextPath() + "/marketing/campaigns");
        } else {
            // Validation thất bại: HTTP 400 và forward lại form giữ nguyên dữ liệu
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("campaignForm", form);
            request.setAttribute("channelOptions", chienDichService.layDanhSachKenh());
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("permissions", permissions);
            request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-form.jsp").forward(request, response);
        }
    }

    private void xuLyHienThiDanhSach(HttpServletRequest request, HttpServletResponse response, ChienDichPermissionsDTO permissions)
            throws ServletException, IOException {

        int page = 1;
        int pageSize = 20;

        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String sizeParam = request.getParameter("pageSize");
        if (sizeParam != null && !sizeParam.isBlank()) {
            try {
                pageSize = Integer.parseInt(sizeParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Map<String, Object> data = chienDichService.layDanhSachChienDich(page, pageSize);

        request.setAttribute("campaignRows", data.get("campaignRows"));
        request.setAttribute("pagination", data.get("pagination"));
        request.setAttribute("permissions", permissions);

        // Lấy flash message nếu có sau redirect
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("flashMessage") != null) {
            request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
            session.removeAttribute("flashMessage");
        }

        request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-danh-sach.jsp").forward(request, response);
    }

    private void xuLyHienThiFormTao(HttpServletRequest request, HttpServletResponse response, ChienDichPermissionsDTO permissions)
            throws ServletException, IOException {

        if (!permissions.isCanCreate()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Bạn không có quyền khai báo chiến dịch marketing.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        request.setAttribute("campaignForm", new ChienDichDTO());
        request.setAttribute("channelOptions", chienDichService.layDanhSachKenh());
        request.setAttribute("fieldErrors", new HashMap<String, String>());
        request.setAttribute("permissions", permissions);

        request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-form.jsp").forward(request, response);
    }

    private void xuLyHienThiChiTiet(HttpServletRequest request, HttpServletResponse response, ChienDichPermissionsDTO permissions)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errorMessage", "Yêu cầu không hợp lệ: thiếu mã định danh chiến dịch.");
            request.getRequestDispatcher("/WEB-INF/views/common/404.jsp").forward(request, response);
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errorMessage", "Yêu cầu không hợp lệ: mã định danh chiến dịch sai định dạng.");
            request.getRequestDispatcher("/WEB-INF/views/common/404.jsp").forward(request, response);
            return;
        }

        ChienDichRowDTO detail = chienDichService.layChiTietChienDich(id);
        if (detail == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("errorMessage", "Không tìm thấy chiến dịch phù hợp.");
            request.getRequestDispatcher("/WEB-INF/views/common/404.jsp").forward(request, response);
            return;
        }

        request.setAttribute("campaign", detail.getCampaign());
        request.setAttribute("metrics", detail.getMetrics());
        request.setAttribute("permissions", permissions);

        request.getRequestDispatcher("/WEB-INF/views/lead/chien-dich-chi-tiet.jsp").forward(request, response);
    }

    private String layHanhDong(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if ("/chien-dich/tao".equals(servletPath)) {
            return "new";
        }
        if ("/chien-dich/chi-tiet".equals(servletPath)) {
            return "detail";
        }

        String action = request.getParameter("action");
        if (action != null && !action.isBlank()) {
            return action.trim().toLowerCase();
        }

        return "list";
    }

    private NguoiDung layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        NguoiDung user = (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
        if (user == null) {
            user = (NguoiDung) session.getAttribute("nguoiDung");
        }
        return user;
    }
}
