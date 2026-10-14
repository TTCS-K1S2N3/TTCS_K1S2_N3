package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.nhom10.crm.dto.BaoCaoNhapLeadExcelDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.LeadImportService;
import vn.nhom10.crm.service.PermissionService;
import vn.nhom10.crm.util.ExcelLeadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ chức năng nhập Lead hàng loạt từ tệp Excel (Story S4-02).
 * - Tải tệp mẫu Excel chuẩn (.xlsx)
 * - Tải lên, xem trước và báo lỗi chi tiết theo từng dòng (AC2)
 * - Bắt buộc mọi lead nhập vào phải có nguồn (AC3)
 * - Xác nhận nhập dữ liệu và báo cáo tổng kết (AC2)
 * URLs:
 * - /lead/import-excel : Giao diện và xử lý xem trước, xác nhận nhập
 * - /lead/import       : Alias đường dẫn nhập Excel
 * - /lead/tai-tep-mau  : Tải tệp mẫu bảng tính Excel
 */
@WebServlet(name = "LeadImportServlet", urlPatterns = {"/lead/import", "/lead/import-excel", "/lead/tai-tep-mau"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize = 10 * 1024 * 1024,           // 10 MB
        maxRequestSize = 20 * 1024 * 1024         // 20 MB
)
public class LeadImportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LeadImportServlet.class.getName());

    public static final String SESSION_BAO_CAO_LEAD = "IMPORT_LEAD_BAO_CAO";
    public static final String SESSION_FILE_NAME_LEAD = "IMPORT_LEAD_FILE_NAME";

    private final LeadImportService importService;
    private final PermissionService permissionService;

    public LeadImportServlet() {
        this(new LeadImportService(), PermissionService.getInstance());
    }

    public LeadImportServlet(LeadImportService importService) {
        this(importService, PermissionService.getInstance());
    }

    public LeadImportServlet(LeadImportService importService, PermissionService permissionService) {
        this.importService = importService != null ? importService : new LeadImportService();
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
        String action = request.getParameter("action");

        // 1. Tải tệp mẫu Excel (.xlsx) (AC2)
        if ("/lead/tai-tep-mau".equals(path) || "tai-mau".equalsIgnoreCase(action)) {
            if (!permissionService.coQuyen(user, "LEAD", MucQuyen.READ)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Bạn không có quyền tải tệp mẫu khách hàng tiềm năng.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
                return;
            }
            xuLyTaiTepMau(response);
            return;
        }

        // 2. Màn hình nhập Lead từ Excel - Yêu cầu quyền WRITE
        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền nhập dữ liệu khách hàng tiềm năng (yêu cầu quyền WRITE).");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        request.setAttribute("currentUser", userDTO);
        request.setAttribute("nguoiDung", user);
        request.getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp").forward(request, response);
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

        if (!permissionService.coQuyen(user, "LEAD", MucQuyen.WRITE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", userDTO);
            request.setAttribute("errorMessage", "Bạn không có quyền nhập khách hàng tiềm năng (yêu cầu quyền WRITE).");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        request.setAttribute("currentUser", userDTO);
        request.setAttribute("nguoiDung", user);

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "xem-truoc";
        }

        if ("nhap-du-lieu".equalsIgnoreCase(action)) {
            xuLyXacNhanNhapDuLieu(request, response, session, userDTO);
        } else {
            xuLyXemTruoc(request, response, session, userDTO);
        }
    }

    /**
     * Tải tệp mẫu Excel chuẩn (.xlsx) có sẵn hướng dẫn và dữ liệu mẫu.
     */
    private void xuLyTaiTepMau(HttpServletResponse response) throws IOException {
        byte[] fileBytes = ExcelLeadUtil.taoTepMauExcel();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(fileBytes.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"tep_mau_nhap_lead.xlsx\"");

        try (OutputStream os = response.getOutputStream()) {
            os.write(fileBytes);
            os.flush();
        }
    }

    /**
     * Xử lý tải lên tệp Excel, thẩm định lỗi theo từng dòng (AC2, AC3) và hiển thị bản xem trước.
     */
    private void xuLyXemTruoc(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            NguoiDungDTO userDTO) throws ServletException, IOException {

        Part filePart = null;
        try {
            filePart = request.getPart("fileExcel");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc fileExcel part: " + e.getMessage());
        }

        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("thongBaoLoi", "Vui lòng chọn một tệp Excel từ máy tính của bạn trước khi bấm Xem trước dữ liệu.");
            request.getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp").forward(request, response);
            return;
        }

        String fileName = layTenTep(filePart);
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            request.setAttribute("thongBaoLoi", "Định dạng tệp không được hỗ trợ. Vui lòng chọn tệp bảng tính Excel (.xlsx hoặc .xls).");
            request.getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp").forward(request, response);
            return;
        }

        try (InputStream is = filePart.getInputStream()) {
            BaoCaoNhapLeadExcelDTO baoCao = importService.thamDinhTepExcel(is, fileName);

            // Lưu báo cáo vào session để phục vụ bước xác nhận nhập
            session.setAttribute(SESSION_BAO_CAO_LEAD, baoCao);
            session.setAttribute(SESSION_FILE_NAME_LEAD, fileName);

            request.setAttribute("baoCao", baoCao);
            request.setAttribute("cheDo", "xem-truoc");
            request.setAttribute("tenTep", fileName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý tệp Excel lead: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể phân tích tệp Excel. Chi tiết: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp").forward(request, response);
    }

    /**
     * Xử lý xác nhận nhập các dòng hợp lệ vào cơ sở dữ liệu và hiển thị kết quả tổng kết (AC2).
     */
    private void xuLyXacNhanNhapDuLieu(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            NguoiDungDTO userDTO) throws ServletException, IOException {

        BaoCaoNhapLeadExcelDTO baoCao = (BaoCaoNhapLeadExcelDTO) session.getAttribute(SESSION_BAO_CAO_LEAD);
        String fileName = (String) session.getAttribute(SESSION_FILE_NAME_LEAD);

        // Nếu session hết hạn, thử đọc lại file nếu có gửi kèm
        if (baoCao == null) {
            Part filePart = null;
            try {
                filePart = request.getPart("fileExcel");
            } catch (Exception ignored) {
            }

            if (filePart != null && filePart.getSize() > 0) {
                fileName = layTenTep(filePart);
                try (InputStream is = filePart.getInputStream()) {
                    baoCao = importService.thamDinhTepExcel(is, fileName);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi đọc lại tệp Excel: " + e.getMessage(), e);
                }
            }
        }

        if (baoCao == null) {
            request.setAttribute("thongBaoLoi", "Phiên làm việc xem trước đã hết hạn. Vui lòng tải lại tệp Excel để tiếp tục.");
            request.getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp").forward(request, response);
            return;
        }

        // Thực hiện nhập dữ liệu thật
        importService.thucHienNhapDuLieu(baoCao, userDTO.getId());

        // Xóa báo cáo khỏi session để hoàn tất phiên nhập
        session.removeAttribute(SESSION_BAO_CAO_LEAD);
        session.removeAttribute(SESSION_FILE_NAME_LEAD);

        request.setAttribute("baoCao", baoCao);
        request.setAttribute("cheDo", "ket-qua");
        request.setAttribute("tenTep", fileName != null ? fileName : baoCao.getTenTep());

        request.getRequestDispatcher("/WEB-INF/views/lead/import-excel.jsp").forward(request, response);
    }

    private String layTenTep(Part part) {
        if (part == null) return null;
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) return null;

        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                String fileName = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
                if (lastSlash >= 0) {
                    fileName = fileName.substring(lastSlash + 1);
                }
                return fileName;
            }
        }
        return part.getName();
    }
}
