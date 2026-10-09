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

        Long adminId = layIdNguoiDungHienTai(request);
        String previewToken = request.getParameter("previewToken");

        // Luồng 1: Thực hiện nhập trực tiếp từ dữ liệu Preview bằng previewToken (S2-01 Review Fix)
        if ("nhap-du-lieu".equalsIgnoreCase(action) && previewToken != null && !previewToken.isBlank()) {
            xuLyNhapTuPreviewToken(request, response, previewToken.trim(), adminId);
            return;
        }

        // Luồng 2: Upload file từ máy tính
        Part filePart = null;
        try {
            filePart = request.getPart("fileExcel");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc part fileExcel: " + e.getMessage());
        }

        if (filePart == null || filePart.getSize() == 0) {
            if ("nhap-du-lieu".equalsIgnoreCase(action)) {
                request.setAttribute("thongBaoLoi", "Dữ liệu xem trước đã hết hạn hoặc không còn hiệu lực. Vui lòng chọn lại tệp Excel.");
            } else {
                request.setAttribute("thongBaoLoi", "Vui lòng chọn một tệp Excel từ máy tính của bạn trước khi bấm thao tác.");
            }
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        String fileName = layTenTep(filePart);
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            request.setAttribute("thongBaoLoi", "Định dạng tệp không được hỗ trợ. Vui lòng chọn tệp bảng tính Excel (.xlsx hoặc .xls).");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        byte[] fileBytes;
        try (InputStream is = filePart.getInputStream()) {
            fileBytes = is.readAllBytes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi đọc tệp Excel: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể đọc tệp Excel. Chi tiết: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        try {
            if ("nhap-du-lieu".equalsIgnoreCase(action)) {
                // AC 3: Nhập dữ liệu trực tiếp khi upload kèm file (tương thích backward)
                try (InputStream is = new java.io.ByteArrayInputStream(fileBytes)) {
                    BaoCaoNhapExcelDTO baoCao = importService.thucHienNhap(is, adminId, fileName);
                    request.setAttribute("baoCao", baoCao);
                    request.setAttribute("cheDo", "ket-qua");
                    request.setAttribute("tenTep", fileName);
                    String thongDiep = String.format("Đã nhập thành công %d/%d tài khoản. %d dòng lỗi đã được bỏ qua.",
                            baoCao.getSoDongThanhCong(), baoCao.getTongSoDong(), baoCao.getSoDongThatBai());
                    request.setAttribute("thongBaoThanhCong", thongDiep);
                }
            } else {
                // AC 2: Xem trước và báo lỗi theo từng dòng trước khi nhập
                try (InputStream is = new java.io.ByteArrayInputStream(fileBytes)) {
                    BaoCaoNhapExcelDTO baoCao = importService.xemTruoc(is, adminId, fileName);

                    // Tạo preview token và lưu vào session
                    String token = java.util.UUID.randomUUID().toString();
                    vn.nhom10.crm.dto.ImportPreviewSession previewData = new vn.nhom10.crm.dto.ImportPreviewSession(
                            token, adminId, fileName, fileBytes, baoCao.getSoDongHopLe()
                    );
                    HttpSession session = request.getSession(false);
                    if (session == null) {
                        session = request.getSession(true);
                    }
                    if (session != null) {
                        session.setAttribute("IMPORT_PREVIEW_" + token, previewData);
                    }

                    request.setAttribute("previewToken", token);
                    request.setAttribute("baoCao", baoCao);
                    request.setAttribute("cheDo", "xem-truoc");
                    request.setAttribute("tenTep", fileName);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi phân tích tệp Excel tải lên: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể đọc tệp Excel. Chi tiết: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
    }

    /**
     * Xử lý nhập trực tiếp từ dữ liệu xem trước đã lưu trong phiên làm việc (Story S2-01).
     */
    private void xuLyNhapTuPreviewToken(HttpServletRequest request, HttpServletResponse response,
                                       String previewToken, Long adminId) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            request.setAttribute("thongBaoLoi", "Phiên làm việc đã hết hạn. Vui lòng chọn lại tệp Excel.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        String sessionKey = "IMPORT_PREVIEW_" + previewToken;
        vn.nhom10.crm.dto.ImportPreviewSession preview = (vn.nhom10.crm.dto.ImportPreviewSession) session.getAttribute(sessionKey);

        if (preview == null) {
            request.setAttribute("thongBaoLoi", "Dữ liệu xem trước đã hết hạn hoặc không tồn tại. Vui lòng chọn lại tệp Excel.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        // Kiểm tra đúng người dùng tạo preview mới được nhập
        if (preview.getUserId() == null || !preview.getUserId().equals(adminId)) {
            request.setAttribute("thongBaoLoi", "Bạn không có quyền thao tác trên đợt xem trước của tài khoản khác. Vui lòng chọn lại tệp Excel.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        // Kiểm tra hết hạn TTL
        if (preview.isExpired()) {
            session.removeAttribute(sessionKey);
            request.setAttribute("thongBaoLoi", "Đợt xem trước đã hết hạn. Vui lòng chọn lại tệp Excel.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        // Kiểm tra token đã dùng chưa (chống double submit / refresh)
        if (!preview.markUsed()) {
            request.setAttribute("thongBaoLoi", "Đợt xem trước này đã được nhập trước đó. Vui lòng chọn lại tệp Excel nếu muốn nhập đợt mới.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        // Dọn dẹp dữ liệu preview trong session ngay khi đã bắt đầu xử lý
        session.removeAttribute(sessionKey);

        // Kiểm tra nếu preview không có dòng hợp lệ nào -> không cho nhập
        if (preview.getSoDongHopLe() <= 0) {
            request.setAttribute("thongBaoLoi", "Không có dòng dữ liệu nào hợp lệ để nhập. Vui lòng kiểm tra lại báo cáo lỗi và chọn tệp khác.");
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/import-excel.jsp").forward(request, response);
            return;
        }

        try (InputStream is = new java.io.ByteArrayInputStream(preview.getFileBytes())) {
            // Re-validate và insert vào DB
            BaoCaoNhapExcelDTO baoCao = importService.thucHienNhap(is, adminId, preview.getFileName());
            request.setAttribute("baoCao", baoCao);
            request.setAttribute("cheDo", "ket-qua");
            request.setAttribute("tenTep", preview.getFileName());
            String thongDiep = String.format("Đã nhập thành công %d/%d tài khoản. %d dòng lỗi đã được bỏ qua.",
                    baoCao.getSoDongThanhCong(), baoCao.getTongSoDong(), baoCao.getSoDongThatBai());
            request.setAttribute("thongBaoThanhCong", thongDiep);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi nhập dữ liệu từ preview: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Đã xảy ra lỗi trong quá trình nhập dữ liệu: " + e.getMessage());
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
