package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.nhom10.crm.dto.BaoCaoNhapExcelDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.NguoiDungImportService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller tiếp nhận HTTP request cho chức năng nhập người dùng hàng loạt từ Excel.
 * Phục vụ Story S2-01:
 * - Tải tệp mẫu Excel
 * - Xem trước dữ liệu và báo lỗi từng dòng
 * - Thực hiện nhập: bỏ qua dòng lỗi, nhập dòng hợp lệ và xuất báo cáo tổng kết
 */
@WebServlet(name = "NguoiDungImportServlet", urlPatterns = {"/nguoi-dung/import", "/nguoi-dung/tai-tep-mau"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,       // 1 MB
        maxFileSize = 10 * 1024 * 1024,          // 10 MB
        maxRequestSize = 20 * 1024 * 1024        // 20 MB
)
public class NguoiDungImportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungImportServlet.class.getName());

    private final NguoiDungImportService importService;

    public NguoiDungImportServlet() {
        this.importService = new NguoiDungImportService();
    }

    public NguoiDungImportServlet(NguoiDungImportService importService) {
        this.importService = importService != null ? importService : new NguoiDungImportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Kiểm tra phân quyền truy cập: Quản trị hệ thống
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String path = request.getServletPath();
        String action = request.getParameter("action");

        if ("/nguoi-dung/tai-tep-mau".equals(path) || "tai-mau".equalsIgnoreCase(action)) {
            xuLyTaiTepMau(response);
            return;
        }

        // Mặc định hiển thị giao diện nhập Excel
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Kiểm tra phân quyền truy cập: Quản trị hệ thống
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "xem-truoc";
        }

        Part filePart = null;
        try {
            filePart = request.getPart("fileExcel");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc part fileExcel: " + e.getMessage());
        }

        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("thongBaoLoi", "Vui lòng chọn một tệp Excel từ máy tính của bạn trước khi bấm thao tác.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        String fileName = layTenTep(filePart);
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            request.setAttribute("thongBaoLoi", "Định dạng tệp không được hỗ trợ. Vui lòng chọn tệp bảng tính Excel (.xlsx hoặc .xls).");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        Long adminId = layIdNguoiDungHienTai(request);

        try (InputStream is = filePart.getInputStream()) {
            if ("nhap-du-lieu".equalsIgnoreCase(action)) {
                // AC 3: Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết
                BaoCaoNhapExcelDTO baoCao = importService.thucHienNhap(is, adminId, fileName);
                request.setAttribute("baoCao", baoCao);
                request.setAttribute("cheDo", "ket-qua");
                request.setAttribute("tenTep", fileName);
            } else {
                // AC 2: Xem trước và báo lỗi theo từng dòng trước khi nhập
                BaoCaoNhapExcelDTO baoCao = importService.xemTruoc(is, adminId, fileName);
                request.setAttribute("baoCao", baoCao);
                request.setAttribute("cheDo", "xem-truoc");
                request.setAttribute("tenTep", fileName);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi phân tích tệp Excel tải lên: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể đọc tệp Excel. Chi tiết: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
    }

    /**
     * Tải tệp Excel mẫu về máy người dùng.
     */
    private void xuLyTaiTepMau(HttpServletResponse response) throws IOException {
        try {
            byte[] fileBytes = importService.taoTepMauExcel();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"mau_nhap_nguoi_dung.xlsx\"");
            response.setContentLength(fileBytes.length);

            try (OutputStream os = response.getOutputStream()) {
                os.write(fileBytes);
                os.flush();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi sinh tệp mẫu Excel: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể tạo tệp mẫu Excel.");
        }
    }

    /**
     * Kiểm tra người dùng có quyền Quản trị hệ thống (ADMIN) hay không theo CODING_RULES.md.
     */
    private boolean kiemTraQuyenQuanTri(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        Object userObj = session.getAttribute("nguoiDung");
        if (userObj == null) {
            userObj = session.getAttribute("user");
        }

        if (userObj instanceof NguoiDung) {
            NguoiDung nd = (NguoiDung) userObj;
            if (nd.coVaiTro("ADMIN") || nd.coVaiTro("QUAN_TRI")) {
                return true;
            }
            // Đã đăng nhập nhưng không có vai trò ADMIN -> 403 Forbidden
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
            return false;
        }

        return true;
    }

    private Long layIdNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("nguoiDung");
            if (userObj == null) {
                userObj = session.getAttribute("user");
            }
            if (userObj instanceof NguoiDung) {
                return ((NguoiDung) userObj).getId();
            }
        }
        return 1L; // Mặc định ID 1 nếu chạy demo trực tiếp
    }

    private String layTenTep(Part part) {
        if (part == null) return null;
        String fileName = part.getSubmittedFileName();
        if (fileName != null && !fileName.isBlank()) {
            int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
            return lastSlash >= 0 ? fileName.substring(lastSlash + 1) : fileName;
        }

        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String token : contentDisp.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                    int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                    return lastSlash >= 0 ? name.substring(lastSlash + 1) : name;
                }
            }
        }
        return "tep_tai_len.xlsx";
    }
}
