package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.nhom10.crm.dto.BaoCaoNhapKhachHangExcelDTO;
import vn.nhom10.crm.dto.DongExcelKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.KhachHangImportService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;
import vn.nhom10.crm.util.ExcelKhachHangUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ chức năng nhập danh sách khách hàng hàng loạt từ tệp Excel.
 * Phục vụ Story S3-06:
 * - Tải tệp mẫu Excel (.xlsx)
 * - Xem trước và báo lỗi theo từng dòng (AC 1)
 * - Đánh dấu bản ghi trùng để người dùng chọn bỏ qua hoặc cập nhật (AC 2)
 * - Thực hiện nhập dữ liệu và báo cáo tổng kết
 */
@WebServlet(name = "KhachHangImportServlet", urlPatterns = {"/khach-hang/import", "/khach-hang/import-excel", "/khach-hang/tai-tep-mau"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,        // 1 MB
        maxFileSize = 10 * 1024 * 1024,           // 10 MB
        maxRequestSize = 20 * 1024 * 1024         // 20 MB
)
public class KhachHangImportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhachHangImportServlet.class.getName());

    public static final String SESSION_BAO_CAO = "IMPORT_KHACH_HANG_BAO_CAO";
    public static final String SESSION_FILE_NAME = "IMPORT_KHACH_HANG_FILE_NAME";

    private final PhanQuyenDuLieuService phanQuyenService;
    private final KhachHangImportService importService;

    public KhachHangImportServlet() {
        this.phanQuyenService = new PhanQuyenDuLieuService();
        this.importService = new KhachHangImportService();
    }

    public KhachHangImportServlet(PhanQuyenDuLieuService phanQuyenService) {
        this.phanQuyenService = phanQuyenService != null ? phanQuyenService : new PhanQuyenDuLieuService();
        this.importService = new KhachHangImportService();
    }

    public KhachHangImportServlet(KhachHangImportService importService) {
        this.phanQuyenService = new PhanQuyenDuLieuService();
        this.importService = importService != null ? importService : new KhachHangImportService();
    }

    public KhachHangImportServlet(PhanQuyenDuLieuService phanQuyenService, KhachHangImportService importService) {
        this.phanQuyenService = phanQuyenService != null ? phanQuyenService : new PhanQuyenDuLieuService();
        this.importService = importService != null ? importService : new KhachHangImportService();
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

        // AC 1: Tải tệp mẫu Excel
        if ("/khach-hang/tai-tep-mau".equals(path) || "tai-mau".equalsIgnoreCase(action)) {
            xuLyTaiTepMau(response);
            return;
        }

        // Mặc định hiển thị giao diện nhập Excel cho khách hàng
        request.setAttribute("currentUser", userDTO);
        request.setAttribute("nguoiDung", user);
        request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp").forward(request, response);
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
     * Xử lý tải lên tệp Excel, thẩm định lỗi từng dòng và đánh dấu trùng lặp (AC 1 & AC 2).
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
            request.setAttribute("thongBaoLoi", "Vui lòng chọn một tệp Excel từ máy tính của bạn trước khi bấm thao tác.");
            request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp").forward(request, response);
            return;
        }

        String fileName = layTenTep(filePart);
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            request.setAttribute("thongBaoLoi", "Định dạng tệp không được hỗ trợ. Vui lòng chọn tệp bảng tính Excel (.xlsx hoặc .xls).");
            request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp").forward(request, response);
            return;
        }

        try (InputStream is = filePart.getInputStream()) {
            BaoCaoNhapKhachHangExcelDTO baoCao = importService.thamDinhTepExcel(is, fileName, userDTO);

            // Lưu báo cáo vào session để phục vụ bước xác nhận nhập
            session.setAttribute(SESSION_BAO_CAO, baoCao);
            session.setAttribute(SESSION_FILE_NAME, fileName);

            request.setAttribute("baoCao", baoCao);
            request.setAttribute("cheDo", "xem-truoc");
            request.setAttribute("tenTep", fileName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý tệp Excel khách hàng: " + e.getMessage(), e);
            request.setAttribute("thongBaoLoi", "Không thể phân tích tệp Excel. Chi tiết: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp").forward(request, response);
    }

    /**
     * Xử lý xác nhận nhập dữ liệu thực tế vào database dựa trên các lựa chọn Bỏ qua / Cập nhật (AC 2).
     */
    private void xuLyXacNhanNhapDuLieu(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            NguoiDungDTO userDTO) throws ServletException, IOException {

        BaoCaoNhapKhachHangExcelDTO baoCao = (BaoCaoNhapKhachHangExcelDTO) session.getAttribute(SESSION_BAO_CAO);
        String fileName = (String) session.getAttribute(SESSION_FILE_NAME);

        // Trường hợp người dùng submit kèm file mới hoặc session bị rỗng
        if (baoCao == null) {
            Part filePart = null;
            try {
                filePart = request.getPart("fileExcel");
            } catch (Exception ignored) {
            }

            if (filePart != null && filePart.getSize() > 0) {
                fileName = layTenTep(filePart);
                try (InputStream is = filePart.getInputStream()) {
                    baoCao = importService.thamDinhTepExcel(is, fileName, userDTO);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi đọc lại tệp Excel: " + e.getMessage(), e);
                }
            }
        }

        if (baoCao == null) {
            request.setAttribute("thongBaoLoi", "Phiên làm việc đã hết hạn hoặc không tìm thấy dữ liệu tệp xem trước. Vui lòng tải lại tệp Excel.");
            request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp").forward(request, response);
            return;
        }

        // Lấy lựa chọn xử lý trùng lặp chung và riêng từng dòng
        String xuLyTrungLapChung = request.getParameter("xuLyTrungLapChung");
        if (xuLyTrungLapChung == null || xuLyTrungLapChung.isBlank()) {
            xuLyTrungLapChung = "BO_QUA";
        }

        Map<Integer, String> luaChonTungDong = new HashMap<>();
        for (DongExcelKhachHangDTO dong : baoCao.getDanhSachTatCaDong()) {
            String lc = request.getParameter("xuLyDong_" + dong.getSoDong());
            if (lc != null && !lc.isBlank()) {
                luaChonTungDong.put(dong.getSoDong(), lc);
            }
        }

        // Thực hiện nhập dữ liệu thật
        importService.thucHienNhapDuLieu(baoCao, luaChonTungDong, xuLyTrungLapChung, userDTO);

        // Xóa báo cáo khỏi session để hoàn tất phiên
        session.removeAttribute(SESSION_BAO_CAO);
        session.removeAttribute(SESSION_FILE_NAME);

        request.setAttribute("baoCao", baoCao);
        request.setAttribute("cheDo", "ket-qua");
        request.setAttribute("tenTep", fileName != null ? fileName : baoCao.getTenTep());

        request.getRequestDispatcher("/WEB-INF/views/khach-hang/import-excel.jsp").forward(request, response);
    }

    /**
     * Tải tệp mẫu Excel về máy người dùng.
     */
    private void xuLyTaiTepMau(HttpServletResponse response) throws IOException {
        try {
            byte[] fileBytes = ExcelKhachHangUtil.taoTepMauExcel();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"mau_nhap_khach_hang.xlsx\"");
            response.setContentLength(fileBytes.length);

            try (OutputStream os = response.getOutputStream()) {
                os.write(fileBytes);
                os.flush();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi sinh tệp mẫu Excel khách hàng: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể tạo tệp mẫu Excel.");
        }
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
        return "tep_khach_hang.xlsx";
    }
}
