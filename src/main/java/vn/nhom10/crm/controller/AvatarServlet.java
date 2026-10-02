package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaUploadAvatarDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.AvatarService;
import vn.nhom10.crm.util.AvatarUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller xử lý luồng xem và tải lên ảnh đại diện người dùng.
 * Phục vụ Story S2-03:
 * - Chấp nhận JPG/PNG tối đa 2MB
 * - Cắt vuông và tạo bản thu nhỏ thumbnail
 * - Xuất stream ảnh đại diện hoặc avatar mặc định SVG
 */
@WebServlet(name = "AvatarServlet", urlPatterns = {"/avatar", "/avatar/*"})
@MultipartConfig(
        maxFileSize = AvatarUtil.GIOI_HAN_DUNG_LUONG_BYTES, // 2MB tối đa cho 1 file
        maxRequestSize = 3L * 1024 * 1024,                  // 3MB tối đa cho cả request
        fileSizeThreshold = 512 * 1024                      // 512KB lưu bộ nhớ đệm
)
public class AvatarServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AvatarServlet.class.getName());

    private AvatarService avatarService;
    private NguoiDungDAO nguoiDungDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.avatarService = new AvatarService();
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    // Constructor phục vụ Unit Test
    public AvatarServlet(AvatarService avatarService, NguoiDungDAO nguoiDungDAO) {
        this.avatarService = avatarService;
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public AvatarServlet() {
    }

    /**
     * GET /avatar?id=1[&thumb=true]
     * Trả về file ảnh đại diện dạng stream byte hoặc SVG mặc định nếu chưa có ảnh.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        boolean laThumbnail = "true".equalsIgnoreCase(req.getParameter("thumb"));

        int userId = 0;
        if (idParam != null && !idParam.isBlank()) {
            try {
                userId = Integer.parseInt(idParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // Nếu không có id param, lấy từ session người dùng đăng nhập
        if (userId <= 0) {
            NguoiDung userSession = layNguoiDungHienTai(req);
            if (userSession != null) {
                userId = (int) userSession.getId();
            }
        }

        if (userId <= 0) {
            xuatAvatarMacDinhSvg(resp, "CR");
            return;
        }

        NguoiDung nguoiDung = nguoiDungDAO.timTheoId(userId);
        if (nguoiDung == null) {
            xuatAvatarMacDinhSvg(resp, "CR");
            return;
        }

        File fileAnh = avatarService.layFileAnhNguoiDung(userId, laThumbnail);
        if (fileAnh != null && fileAnh.exists()) {
            String probeContentType = Files.probeContentType(fileAnh.toPath());
            if (probeContentType == null) {
                probeContentType = fileAnh.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            }
            resp.setContentType(probeContentType);
            resp.setContentLengthLong(fileAnh.length());
            resp.setHeader("Cache-Control", "public, max-age=3600");

            try (InputStream in = new FileInputStream(fileAnh);
                 OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } else {
            // Chưa có ảnh đại diện -> Xuất avatar mặc định sinh từ chữ cái viết tắt
            xuatAvatarMacDinhSvg(resp, nguoiDung.getTenVietTat());
        }
    }

    /**
     * POST /avatar
     * Nhận file upload avatar (multipart/form-data), validate, cắt vuông, tạo thumbnail và lưu trữ.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        NguoiDung nguoiDung = layNguoiDungHienTai(req);
        if (nguoiDung == null) {
            traVeLoiHoacRedirect(req, resp, "Bạn cần đăng nhập để thực hiện chức năng tải lên ảnh đại diện.", false);
            return;
        }

        Part filePart;
        try {
            filePart = req.getPart("avatar");
        } catch (IllegalStateException e) {
            // Khi kích thước file vượt ngưỡng @MultipartConfig
            traVeLoiHoacRedirect(req, resp, "Dung lượng ảnh vượt quá giới hạn tối đa 2MB cho phép.", false);
            return;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi đọc file upload: " + e.getMessage());
            traVeLoiHoacRedirect(req, resp, "Không thể đọc dữ liệu file tải lên.", false);
            return;
        }

        if (filePart == null || filePart.getSize() <= 0) {
            traVeLoiHoacRedirect(req, resp, "Vui lòng chọn một file ảnh để tải lên.", false);
            return;
        }

        String tenFileGoc = extractFileName(filePart);
        String contentType = filePart.getContentType();
        long dungLuong = filePart.getSize();

        // Xử lý upload thông qua AvatarService
        KetQuaUploadAvatarDTO ketQua = avatarService.xuLyUploadAvatar(
                (int) nguoiDung.getId(),
                filePart.getInputStream(),
                tenFileGoc,
                contentType,
                dungLuong,
                req.getContextPath()
        );

        if (ketQua.isThanhCong()) {
            // Cập nhật lại thông tin người dùng trong session
            nguoiDung.setAnhDaiDienPath(ketQua.getAnhDaiDienPath());
            nguoiDung.setAnhDaiDienThumbPath(ketQua.getAnhDaiDienThumbPath());
            req.getSession().setAttribute("user", nguoiDung);

            traVeThanhCongHoacRedirect(req, resp, ketQua);
        } else {
            traVeLoiHoacRedirect(req, resp, ketQua.getThongDiep(), false);
        }
    }

    private void xuatAvatarMacDinhSvg(HttpServletResponse resp, String initials) throws IOException {
        resp.setContentType("image/svg+xml;charset=UTF-8");
        resp.setHeader("Cache-Control", "public, max-age=86400");
        String svg = AvatarUtil.sinhAvatarMacDinhSvg(initials);
        resp.getOutputStream().write(svg.getBytes(StandardCharsets.UTF_8));
        resp.getOutputStream().flush();
    }

    private NguoiDung layNguoiDungHienTai(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute("user");
            if (obj instanceof NguoiDung) {
                return (NguoiDung) obj;
            }
        }
        return null;
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String s : contentDisp.split(";")) {
                if (s.trim().startsWith("filename")) {
                    String raw = s.substring(s.indexOf('=') + 1).trim().replace("\"", "");
                    // Loại bỏ đường dẫn nếu client gửi full path
                    return raw.substring(raw.lastIndexOf(File.separator) + 1).substring(raw.lastIndexOf('/') + 1);
                }
            }
        }
        return "avatar.png";
    }

    private boolean laAjaxRequest(HttpServletRequest req) {
        String xReq = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xReq) || (accept != null && accept.contains("application/json"));
    }

    private void traVeThanhCongHoacRedirect(HttpServletRequest req, HttpServletResponse resp, KetQuaUploadAvatarDTO ketQua) throws IOException {
        if (laAjaxRequest(req)) {
            resp.setContentType("application/json;charset=UTF-8");
            String json = String.format(
                    "{\"success\":true,\"message\":\"%s\",\"avatarUrl\":\"%s\",\"thumbUrl\":\"%s\"}",
                    escapeJson(ketQua.getThongDiep()),
                    escapeJson(ketQua.getAnhDaiDienUrl()),
                    escapeJson(ketQua.getAnhDaiDienThumbUrl())
            );
            resp.getWriter().write(json);
        } else {
            req.getSession().setAttribute("flashMessageSuccess", ketQua.getThongDiep());
            resp.sendRedirect(req.getContextPath() + "/ho-so");
        }
    }

    private void traVeLoiHoacRedirect(HttpServletRequest req, HttpServletResponse resp, String thongBaoLoi, boolean forbidden) throws IOException {
        if (laAjaxRequest(req)) {
            resp.setStatus(forbidden ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            String json = String.format("{\"success\":false,\"message\":\"%s\"}", escapeJson(thongBaoLoi));
            resp.getWriter().write(json);
        } else {
            req.getSession().setAttribute("flashMessageError", thongBaoLoi);
            resp.sendRedirect(req.getContextPath() + "/ho-so");
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
