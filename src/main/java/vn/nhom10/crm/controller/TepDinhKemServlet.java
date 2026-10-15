package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TepDinhKem;
import vn.nhom10.crm.service.KhachHang360Service;
import vn.nhom10.crm.service.PermissionService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet xử lý tải lên, tải xuống và quản lý tệp đính kèm khách hàng (Story S3-03 AC1 & Task 1.1).
 * Hỗ trợ lưu trữ file thật ngoài thư mục WAR, chống path traversal, kiểm soát Data Scope chặt chẽ.
 */
@WebServlet(name = "TepDinhKemServlet", urlPatterns = {"/khach-hang/tep-dinh-kem", "/khach-hang/tep-dinh-kem/*"})
@MultipartConfig(
        maxFileSize = 10L * 1024 * 1024,      // 10MB tối đa cho 1 file
        maxRequestSize = 12L * 1024 * 1024,   // 12MB tối đa cho toàn bộ request
        fileSizeThreshold = 1024 * 1024       // 1MB lưu bộ nhớ đệm
)
public class TepDinhKemServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TepDinhKemServlet.class.getName());

    private static final Set<String> EXTENSIONS_CHO_PHEP = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "png", "jpg", "jpeg"
    ));

    private final KhachHang360Service khachHang360Service;
    private final PhanQuyenDuLieuService phanQuyenService;
    private final PermissionService permissionService;

    public TepDinhKemServlet() {
        this(new KhachHang360Service(), new PhanQuyenDuLieuService(), PermissionService.getInstance());
    }

    public TepDinhKemServlet(KhachHang360Service khachHang360Service,
                            PhanQuyenDuLieuService phanQuyenService,
                            PermissionService permissionService) {
        this.khachHang360Service = khachHang360Service != null ? khachHang360Service : new KhachHang360Service();
        this.phanQuyenService = phanQuyenService != null ? phanQuyenService : new PhanQuyenDuLieuService();
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
    }

    /**
     * GET /khach-hang/tep-dinh-kem?id={tepId}[&download=true]
     * Xuất stream tải xuống hoặc xem trực tiếp tài liệu.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập.");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);

        if (!permissionService.coQuyen(user, "KHACH_HANG", MucQuyen.READ)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem tệp đính kèm.");
            return;
        }

        Long tepId = layIdTuRequest(request);
        if (tepId == null || tepId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID tệp đính kèm không hợp lệ.");
            return;
        }

        TepDinhKem tdk = khachHang360Service.layTepDinhKemTheoId(tepId);
        if (tdk == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy tệp đính kèm.");
            return;
        }

        // Kiểm tra quyền theo Data Scope của khách hàng sở hữu tệp
        BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(tdk.getKhachHangId(), "KHACH_HANG");
        if (banGhi != null) {
            PhanQuyenDuLieuService.KetQuaKiemTra kq = phanQuyenService.kiemTraQuyenTruyCap(userDTO, banGhi);
            if (!kq.isCoQuyen()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài liệu thuộc khách hàng nằm ngoài phạm vi phân quyền của bạn.");
                return;
            }
        }

        // Tìm file vật lý trên đĩa
        File fileVatLy = xacDinhFileVatLy(tdk);
        if (fileVatLy == null || !fileVatLy.exists() || !fileVatLy.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tệp tin vật lý không tồn tại trên máy chủ lưu trữ.");
            return;
        }

        boolean laDownload = "true".equalsIgnoreCase(request.getParameter("download"));
        String mimeType = tdk.getMimeType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = Files.probeContentType(fileVatLy.toPath());
        }
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);
        response.setContentLengthLong(fileVatLy.length());

        String tenFileGoc = (tdk.getTenFileGoc() != null && !tdk.getTenFileGoc().isBlank())
                ? tdk.getTenFileGoc() : fileVatLy.getName();
        String encodedName = URLEncoder.encode(tenFileGoc, StandardCharsets.UTF_8).replace("+", "%20");
        String dispositionType = laDownload ? "attachment" : "inline";
        response.setHeader("Content-Disposition", dispositionType + "; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

        try (InputStream in = new FileInputStream(fileVatLy);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }

    /**
     * POST /khach-hang/tep-dinh-kem
     * Tải lên tài liệu mới hoặc xóa tài liệu (action=xoa).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập để thực hiện.\"}");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);

        if (!permissionService.coQuyen(user, "KHACH_HANG", MucQuyen.WRITE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Từ chối thao tác: Yêu cầu quyền WRITE trở lên.\"}");
            return;
        }

        String action = request.getParameter("action");
        if ("xoa".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action)) {
            xuLyXoaTep(request, response, user, userDTO);
            return;
        }

        // Mặc định: Xử lý upload tệp
        xuLyUploadTep(request, response, user, userDTO);
    }

    /**
     * Xử lý tải lên tệp mới cho khách hàng.
     */
    private void xuLyUploadTep(HttpServletRequest request, HttpServletResponse response,
                               NguoiDung user, NguoiDungDTO userDTO) throws IOException {
        Long khachHangId = layLongParam(request, "khachHangId");
        if (khachHangId == null || khachHangId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"ID khách hàng không hợp lệ.\"}");
            return;
        }

        // BẢO MẬT & DATA SCOPE: Kiểm tra quyền thao tác trên khách hàng
        BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(khachHangId, "KHACH_HANG");
        if (banGhi == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy khách hàng ID: " + khachHangId + "\"}");
            return;
        }
        PhanQuyenDuLieuService.KetQuaKiemTra ketQuaScope = phanQuyenService.kiemTraQuyenSua(userDTO, banGhi);
        if (ketQuaScope != null && !ketQuaScope.isCoQuyen()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Từ chối thao tác: Khách hàng nằm ngoài phạm vi phân quyền của bạn.\"}");
            return;
        }

        // Lấy File Part
        Part filePart = null;
        try {
            filePart = request.getPart("tepTin");
            if (filePart == null) {
                filePart = request.getPart("file");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi đọc file part: " + e.getMessage());
        }

        if (filePart == null || filePart.getSize() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng chọn một tệp tài liệu hợp lệ để tải lên.\"}");
            return;
        }

        long fileSize = filePart.getSize();
        if (fileSize > 10L * 1024 * 1024) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Dung lượng tệp vượt quá giới hạn cho phép (tối đa 10 MB).\"}");
            return;
        }

        String submittedFileName = filePart.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Tên tệp không hợp lệ.\"}");
            return;
        }

        // Phòng chống Path Traversal: Trích xuất tên file thuần túy
        String safeOriginalName = Paths.get(submittedFileName).getFileName().toString().trim();
        int lastDotIndex = safeOriginalName.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex == safeOriginalName.length() - 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Tệp phải có phần mở rộng hợp lệ (PDF, DOC, DOCX, XLS, XLSX, PNG, JPG, JPEG).\"}");
            return;
        }

        String extension = safeOriginalName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONS_CHO_PHEP.contains(extension)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Định dạng tệp '." + extension + "' không được hỗ trợ. Vui lòng chọn tệp PDF, DOC, DOCX, XLS, XLSX, PNG, JPG hoặc JPEG.\"}");
            return;
        }

        String loaiTep = request.getParameter("loaiTaiLieu");
        if (loaiTep == null || loaiTep.isBlank()) {
            loaiTep = request.getParameter("loaiTep");
        }
        if (loaiTep == null || loaiTep.isBlank()) {
            loaiTep = "TAI_LIEU";
        }

        String tenTaiLieuTuyChinh = request.getParameter("tenTaiLieu");
        String tenLuuHienThi = (tenTaiLieuTuyChinh != null && !tenTaiLieuTuyChinh.trim().isEmpty())
                ? tenTaiLieuTuyChinh.trim() : safeOriginalName;

        String moTa = request.getParameter("moTa");

        // Lưu file thật ra ổ đĩa (thư mục ngoài WAR)
        String baseUploadDir = DatabaseConfig.getFileUploadDir();
        Path customerUploadFolder = Paths.get(baseUploadDir, "khach_hang_" + khachHangId);
        try {
            Files.createDirectories(customerUploadFolder);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Không thể tạo thư mục lưu file: " + customerUploadFolder, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Không thể khởi tạo thư mục lưu trữ trên máy chủ.\"}");
            return;
        }

        String safeStoredFileName = "kh_" + khachHangId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
        Path targetFilePath = customerUploadFolder.resolve(safeStoredFileName);

        try (InputStream inStream = filePart.getInputStream()) {
            Files.copy(inStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi ghi file ra đĩa: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Không thể lưu tệp lên hệ thống lưu trữ: " + e.getMessage() + "\"}");
            return;
        }

        // Tạo bản ghi CSDL
        TepDinhKem tdk = new TepDinhKem();
        tdk.setKhachHangId(khachHangId);
        tdk.setLoaiTep(loaiTep.trim());
        tdk.setTenFileGoc(tenLuuHienThi);
        tdk.setTenFileLuu(safeStoredFileName);
        // Đường dẫn chuẩn hóa để servlet phục vụ:
        tdk.setDuongDan("/khach-hang/tep-dinh-kem?id=");
        tdk.setMimeType(filePart.getContentType());
        tdk.setKichThuocByte(fileSize);
        tdk.setNguoiTaiLenId(user != null ? user.getId() : null);
        tdk.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        Long newId;
        try {
            newId = khachHang360Service.themTepDinhKem(tdk);
            if (newId != null) {
                tdk.setId(newId);
                tdk.setDuongDan("/khach-hang/tep-dinh-kem?id=" + newId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu tệp đính kèm vào DB: " + e.getMessage(), e);
            // Xóa file vật lý vừa lưu nếu lưu CSDL thất bại để tránh file rác
            try {
                Files.deleteIfExists(targetFilePath);
            } catch (IOException ignored) {}
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Lỗi ghi dữ liệu vào cơ sở dữ liệu: " + e.getMessage() + "\"}");
            return;
        }

        // Ghi nhận sự kiện vào Dòng Thời Gian Tương Tác & Hoạt Động (AC1 & AC3)
        try {
            Long nhomId = user != null && user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null;
            String chiTietMoTa = "Tải lên tệp " + safeOriginalName + " (" + tdk.getDungLuongHienThi() + ") - Phân loại: " + tdk.getTenLoaiHienThi();
            if (moTa != null && !moTa.trim().isEmpty()) {
                chiTietMoTa += "\nGhi chú: " + moTa.trim();
            }
            khachHang360Service.themHoatDong(
                    khachHangId,
                    user != null ? user.getId() : null,
                    nhomId,
                    "TEP_TIN",
                    "Đính kèm tài liệu: " + tenLuuHienThi,
                    chiTietMoTa
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể ghi log hoạt động khi đính kèm tài liệu: " + e.getMessage());
        }

        // Phản hồi thành công
        String jsonResp = String.format(Locale.US,
                "{\"success\":true,\"message\":\"Tải tài liệu lên thành công!\",\"id\":%d,\"tenFileGoc\":\"%s\",\"dungLuong\":\"%s\",\"ngayTai\":\"%s\"}",
                newId != null ? newId : 0L,
                escapeJson(tenLuuHienThi),
                escapeJson(tdk.getDungLuongHienThi()),
                escapeJson(tdk.getNgayTaiLenDinhDang())
        );
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonResp);
    }

    /**
     * Xử lý xóa tệp đính kèm và file vật lý.
     */
    private void xuLyXoaTep(HttpServletRequest request, HttpServletResponse response,
                            NguoiDung user, NguoiDungDTO userDTO) throws IOException {
        Long tepId = layLongParam(request, "id");
        if (tepId == null || tepId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"ID tệp không hợp lệ.\"}");
            return;
        }

        TepDinhKem tdk = khachHang360Service.layTepDinhKemTheoId(tepId);
        if (tdk == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy tệp đính kèm cần xóa.\"}");
            return;
        }

        // Kiểm tra quyền theo Data Scope
        BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(tdk.getKhachHangId(), "KHACH_HANG");
        if (banGhi != null) {
            PhanQuyenDuLieuService.KetQuaKiemTra kq = phanQuyenService.kiemTraQuyenSua(userDTO, banGhi);
            if (kq != null && !kq.isCoQuyen()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\":false,\"message\":\"Từ chối thao tác: Bạn không có quyền xóa tài liệu của khách hàng này.\"}");
                return;
            }
        }

        // Xóa file vật lý
        File fileVatLy = xacDinhFileVatLy(tdk);
        if (fileVatLy != null && fileVatLy.exists()) {
            try {
                Files.deleteIfExists(fileVatLy.toPath());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Không thể xóa file vật lý: " + fileVatLy.getAbsolutePath());
            }
        }

        // Xóa bản ghi trong CSDL
        boolean daXoa = khachHang360Service.xoaTepDinhKem(tepId);
        if (daXoa) {
            // Ghi nhật ký vào timeline
            try {
                Long nhomId = user != null && user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null;
                khachHang360Service.themHoatDong(
                        tdk.getKhachHangId(),
                        user != null ? user.getId() : null,
                        nhomId,
                        "TEP_TIN",
                        "Xóa tệp đính kèm: " + tdk.getTenFileGoc(),
                        "Người dùng đã xóa tài liệu '" + tdk.getTenFileGoc() + "' khỏi hồ sơ khách hàng."
                );
            } catch (Exception ignored) {}

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"success\":true,\"message\":\"Đã xóa tệp đính kèm thành công.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Không thể xóa tệp đính kèm trong cơ sở dữ liệu.\"}");
        }
    }

    /**
     * Xác định đối tượng File vật lý từ thông tin bản ghi TepDinhKem.
     */
    private File xacDinhFileVatLy(TepDinhKem tdk) {
        if (tdk == null) return null;
        String baseUploadDir = DatabaseConfig.getFileUploadDir();
        String tenFileLuu = tdk.getTenFileLuu();

        if (tenFileLuu != null && !tenFileLuu.isBlank()) {
            Path p = Paths.get(baseUploadDir, "khach_hang_" + tdk.getKhachHangId(), tenFileLuu);
            if (Files.exists(p)) {
                return p.toFile();
            }
            // Thử tìm thẳng trong baseUploadDir
            Path pBase = Paths.get(baseUploadDir, tenFileLuu);
            if (Files.exists(pBase)) {
                return pBase.toFile();
            }
        }

        // Nếu đường dẫn lưu dạng relative path
        String duongDan = tdk.getDuongDan();
        if (duongDan != null && !duongDan.isBlank() && !duongDan.contains("?id=")) {
            Path p = Paths.get(baseUploadDir, duongDan);
            if (Files.exists(p)) {
                return p.toFile();
            }
        }

        return null;
    }

    private Long layIdTuRequest(HttpServletRequest request) {
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            try {
                return Long.parseLong(idParam.trim());
            } catch (NumberFormatException ignored) {}
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String part = pathInfo.substring(1).trim();
            try {
                return Long.parseLong(part);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Long layLongParam(HttpServletRequest request, String paramName) {
        String val = request.getParameter(paramName);
        if (val == null || val.isBlank()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
