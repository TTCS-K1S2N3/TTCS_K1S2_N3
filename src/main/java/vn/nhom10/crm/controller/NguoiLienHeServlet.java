package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.LichSuLienHeCongTy;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;
import vn.nhom10.crm.service.NguoiLienHeService;
import vn.nhom10.crm.service.PermissionService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

/**
 * Servlet điều khiển các thao tác Người liên hệ & Vai trò quyết định mua (Story S3-02).
 * Hỗ trợ cả Form submit lẫn AJAX/JSON API.
 * URL: /nguoi-lien-he
 */
@WebServlet(name = "NguoiLienHeServlet", urlPatterns = {"/nguoi-lien-he"})
public class NguoiLienHeServlet extends HttpServlet {

    private final NguoiLienHeService nguoiLienHeService;
    private final PermissionService permissionService;

    public NguoiLienHeServlet() {
        this(new NguoiLienHeService(), new PermissionService());
    }

    public NguoiLienHeServlet(NguoiLienHeService nguoiLienHeService) {
        this(nguoiLienHeService, new PermissionService());
    }

    public NguoiLienHeServlet(NguoiLienHeService nguoiLienHeService, PermissionService permissionService) {
        this.nguoiLienHeService = nguoiLienHeService != null ? nguoiLienHeService : new NguoiLienHeService();
        this.permissionService = permissionService != null ? permissionService : new PermissionService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung user = xacThucNguoiDung(request, response);
        if (user == null) return;

        if (!permissionService.coQuyen(user, "KHACH_HANG", MucQuyen.READ)) {
            phanHoiLoi(request, response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem thông tin người liên hệ.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "detail":
                    xuLyXemChiTiet(request, response, user);
                    break;
                case "lich-su":
                    xuLyXemLichSu(request, response, user);
                    break;
                case "list":
                default:
                    xuLyLayDanhSach(request, response, user);
                    break;
            }
        } catch (SecurityException e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung user = xacThucNguoiDung(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            phanHoiLoi(request, response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số action.");
            return;
        }

        if ("delete".equals(action)) {
            if (!permissionService.coQuyen(user, "KHACH_HANG", MucQuyen.FULL)) {
                phanHoiLoi(request, response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xóa người liên hệ (yêu cầu quyền FULL).");
                return;
            }
        } else {
            if (!permissionService.coQuyen(user, "KHACH_HANG", MucQuyen.WRITE)) {
                phanHoiLoi(request, response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền chỉnh sửa người liên hệ (yêu cầu quyền WRITE trở lên).");
                return;
            }
        }

        try {
            switch (action) {
                case "create":
                    xuLyThemNguoiLienHe(request, response, user);
                    break;
                case "update":
                    xuLyCapNhatNguoiLienHe(request, response, user);
                    break;
                case "set-main":
                    xuLyDatDauMoiChinh(request, response, user);
                    break;
                case "unset-main":
                    xuLyBoDauMoiChinh(request, response, user);
                    break;
                case "transfer-company":
                    xuLyChuyenCongTy(request, response, user);
                    break;
                case "delete":
                    xuLyXoaNguoiLienHe(request, response, user);
                    break;
                default:
                    phanHoiLoi(request, response, HttpServletResponse.SC_BAD_REQUEST, "Action không hợp lệ: " + action);
                    break;
            }
        } catch (SecurityException e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void xuLyLayDanhSach(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        String khIdStr = request.getParameter("khachHangId");
        if (khIdStr == null || khIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu khachHangId.");
        }
        long khachHangId = Long.parseLong(khIdStr.trim());
        List<NguoiLienHe> list = nguoiLienHeService.layDanhSachTheoKhachHang(user, khachHangId);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"data\":[");
        for (int i = 0; i < list.size(); i++) {
            NguoiLienHe nlh = list.get(i);
            sb.append(chuyenJsonNguoiLienHe(nlh));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]}");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(sb.toString());
    }

    private void xuLyXemChiTiet(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu tham số id.");
        }
        long id = Long.parseLong(idStr.trim());
        Optional<NguoiLienHe> opt = nguoiLienHeService.timTheoId(user, id);
        if (opt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy người liên hệ\"}");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":true,\"data\":" + chuyenJsonNguoiLienHe(opt.get()) + "}");
    }

    private void xuLyXemLichSu(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu tham số id.");
        }
        long id = Long.parseLong(idStr.trim());
        List<LichSuLienHeCongTy> history = nguoiLienHeService.layLichSuCongTy(user, id);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":true,\"data\":[");
        for (int i = 0; i < history.size(); i++) {
            LichSuLienHeCongTy item = history.get(i);
            sb.append("{")
                    .append("\"id\":").append(item.getId()).append(",")
                    .append("\"nguoiLienHeId\":").append(item.getNguoiLienHeId()).append(",")
                    .append("\"khachHangId\":").append(item.getKhachHangId()).append(",")
                    .append("\"tenCongTy\":\"").append(escapeJson(item.getTenCongTy())).append("\",")
                    .append("\"chucDanh\":\"").append(escapeJson(item.getChucDanh())).append("\",")
                    .append("\"vaiTro\":\"").append(escapeJson(item.getVaiTroQuyetDinh() != null ? item.getVaiTroQuyetDinh().getMa() : "")).append("\",")
                    .append("\"vaiTroQuyetDinh\":\"").append(escapeJson(item.getVaiTroQuyetDinh() != null ? item.getVaiTroQuyetDinh().getMa() : "")).append("\",")
                    .append("\"tenVaiTro\":\"").append(escapeJson(item.getTenVaiTroHienThi())).append("\",")
                    .append("\"badgeVaiTro\":\"").append(item.getBadgeClassVaiTro()).append("\",")
                    .append("\"tuNgay\":\"").append(item.getTuNgay() != null ? item.getTuNgay().toString() : "").append("\",")
                    .append("\"denNgay\":\"").append(item.getDenNgay() != null ? item.getDenNgay().toString() : "Hiện tại").append("\",")
                    .append("\"ghiChu\":\"").append(escapeJson(item.getGhiChu())).append("\"")
                    .append("}");
            if (i < history.size() - 1) sb.append(",");
        }
        sb.append("]}");

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(sb.toString());
    }

    private void xuLyThemNguoiLienHe(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        long khachHangId = Long.parseLong(request.getParameter("khachHangId").trim());
        String hoTen = request.getParameter("hoTen");
        String chucDanh = request.getParameter("chucDanh");
        String email = request.getParameter("email");
        String sdt = request.getParameter("soDienThoai");
        String vaiTroStr = request.getParameter("vaiTroQuyetDinh");
        boolean laDauMoi = "true".equalsIgnoreCase(request.getParameter("laDauMoiChinh"))
                || "1".equals(request.getParameter("laDauMoiChinh"))
                || "on".equalsIgnoreCase(request.getParameter("laDauMoiChinh"));

        VaiTroQuyetDinhEnum vaiTro = null;
        if (vaiTroStr != null && !vaiTroStr.trim().isEmpty()) {
            vaiTro = VaiTroQuyetDinhEnum.fromMa(vaiTroStr);
        }

        NguoiLienHe nlh = new NguoiLienHe();
        nlh.setKhachHangId(khachHangId);
        nlh.setHoTen(hoTen);
        nlh.setChucDanh(chucDanh);
        nlh.setEmail(email);
        nlh.setSoDienThoai(sdt);
        nlh.setVaiTroQuyetDinh(vaiTro);
        nlh.setLaDauMoiChinh(laDauMoi);

        NguoiLienHe saved = nguoiLienHeService.themNguoiLienHe(user, nlh);

        if (laAjax(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":true,\"message\":\"Thêm người liên hệ thành công!\",\"id\":" + saved.getId() + "}");
        } else {
            request.getSession().setAttribute("thongBaoThanhCong", "Thêm người liên hệ '" + saved.getHoTen() + "' thành công!");
            request.getSession().setAttribute("flashSuccess", "Thêm người liên hệ '" + saved.getHoTen() + "' thành công!");
            String returnUrl = request.getParameter("returnUrl");
            if (returnUrl != null && !returnUrl.isBlank()) {
                response.sendRedirect(request.getContextPath() + returnUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + khachHangId);
            }
        }
    }

    private void xuLyCapNhatNguoiLienHe(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        long id = Long.parseLong(request.getParameter("id").trim());
        String hoTen = request.getParameter("hoTen");
        String chucDanh = request.getParameter("chucDanh");
        String email = request.getParameter("email");
        String sdt = request.getParameter("soDienThoai");
        String vaiTroStr = request.getParameter("vaiTroQuyetDinh");

        VaiTroQuyetDinhEnum vaiTro = null;
        if (vaiTroStr != null && !vaiTroStr.trim().isEmpty()) {
            vaiTro = VaiTroQuyetDinhEnum.fromMa(vaiTroStr);
        }

        NguoiLienHe nlh = new NguoiLienHe();
        nlh.setId(id);
        nlh.setHoTen(hoTen);
        nlh.setChucDanh(chucDanh);
        nlh.setEmail(email);
        nlh.setSoDienThoai(sdt);
        nlh.setVaiTroQuyetDinh(vaiTro);

        boolean updated = nguoiLienHeService.capNhatNguoiLienHe(user, nlh);

        if (laAjax(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":" + updated + ",\"message\":\"Cập nhật thông tin người liên hệ thành công!\"}");
        } else {
            String redirectKhId = request.getParameter("khachHangId");
            request.getSession().setAttribute("thongBaoThanhCong", "Cập nhật người liên hệ thành công!");
            request.getSession().setAttribute("flashSuccess", "Cập nhật người liên hệ thành công!");
            String returnUrl = request.getParameter("returnUrl");
            if (returnUrl != null && !returnUrl.isBlank()) {
                response.sendRedirect(request.getContextPath() + returnUrl);
            } else if (redirectKhId != null && !redirectKhId.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + redirectKhId);
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang");
            }
        }
    }

    private void xuLyDatDauMoiChinh(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        long id = Long.parseLong(request.getParameter("id").trim());
        long khachHangId = Long.parseLong(request.getParameter("khachHangId").trim());

        try {
            boolean ok = nguoiLienHeService.datLamDauMoiChinh(user, id, khachHangId);

            if (laAjax(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":" + ok + ",\"message\":\"Đã đánh dấu là đầu mối chính!\"}");
            } else {
                request.getSession().setAttribute("thongBaoThanhCong", "Đã đánh dấu đầu mối chính thành công!");
                request.getSession().setAttribute("flashSuccess", "Đã đánh dấu đầu mối chính thành công!");
                String returnUrl = request.getParameter("returnUrl");
                if (returnUrl != null && !returnUrl.isBlank()) {
                    response.sendRedirect(request.getContextPath() + returnUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + khachHangId);
                }
            }
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void xuLyBoDauMoiChinh(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        long id = Long.parseLong(request.getParameter("id").trim());
        long khachHangId = Long.parseLong(request.getParameter("khachHangId").trim());

        try {
            boolean ok = nguoiLienHeService.boDauMoiChinh(user, id, khachHangId);

            if (laAjax(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":" + ok + ",\"message\":\"Đã bỏ đánh dấu đầu mối chính!\"}");
            } else {
                request.getSession().setAttribute("thongBaoThanhCong", "Đã bỏ đánh dấu đầu mối chính!");
                request.getSession().setAttribute("flashSuccess", "Đã bỏ đánh dấu đầu mối chính!");
                String returnUrl = request.getParameter("returnUrl");
                if (returnUrl != null && !returnUrl.isBlank()) {
                    response.sendRedirect(request.getContextPath() + returnUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + khachHangId);
                }
            }
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            phanHoiLoi(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void xuLyChuyenCongTy(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        long nlhId = Long.parseLong(request.getParameter("id").trim());
        long khachHangMoiId = Long.parseLong(request.getParameter("khachHangMoiId").trim());
        String chucDanhMoi = request.getParameter("chucDanhMoi");
        String vaiTroStr = request.getParameter("vaiTroMoi");
        String ghiChu = request.getParameter("ghiChu");

        VaiTroQuyetDinhEnum vaiTroMoi = null;
        if (vaiTroStr != null && !vaiTroStr.trim().isEmpty()) {
            vaiTroMoi = VaiTroQuyetDinhEnum.fromMa(vaiTroStr);
        }

        boolean ok = nguoiLienHeService.chuyenCongTy(user, nlhId, khachHangMoiId, chucDanhMoi, vaiTroMoi, ghiChu);

        if (laAjax(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":" + ok + ",\"message\":\"Đã chuyển người liên hệ sang công ty mới và lưu lịch sử thành công!\"}");
        } else {
            request.getSession().setAttribute("thongBaoThanhCong", "Đã chuyển người liên hệ sang công ty mới và lưu trữ lịch sử làm việc!");
            request.getSession().setAttribute("flashSuccess", "Đã chuyển người liên hệ sang công ty mới và lưu trữ lịch sử làm việc!");
            String returnUrl = request.getParameter("returnUrl");
            if (returnUrl != null && !returnUrl.isBlank()) {
                response.sendRedirect(request.getContextPath() + returnUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + khachHangMoiId);
            }
        }
    }

    private void xuLyXoaNguoiLienHe(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        long id = Long.parseLong(request.getParameter("id").trim());
        String khIdStr = request.getParameter("khachHangId");

        boolean ok = nguoiLienHeService.xoaNguoiLienHe(user, id);

        if (laAjax(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":" + ok + ",\"message\":\"Đã xóa người liên hệ!\"}");
        } else {
            request.getSession().setAttribute("thongBaoThanhCong", "Đã xóa người liên hệ!");
            request.getSession().setAttribute("flashSuccess", "Đã xóa người liên hệ!");
            String returnUrl = request.getParameter("returnUrl");
            if (returnUrl != null && !returnUrl.isBlank()) {
                response.sendRedirect(request.getContextPath() + returnUrl);
            } else if (khIdStr != null && !khIdStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + khIdStr);
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang");
            }
        }
    }

    private NguoiDung xacThucNguoiDung(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            if (laAjax(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Phiên đăng nhập đã hết hạn.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            }
            return null;
        }
        return (NguoiDung) session.getAttribute("nguoiDung");
    }

    private boolean laAjax(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(header)
                || (accept != null && accept.contains("application/json"))
                || "json".equalsIgnoreCase(request.getParameter("format"));
    }

    private void phanHoiLoi(HttpServletRequest request, HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        if (laAjax(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}");
        } else {
            request.getSession().setAttribute("thongBaoLoi", message);
            request.getSession().setAttribute("flashError", message);
            String returnUrl = request.getParameter("returnUrl");
            if (returnUrl != null && !returnUrl.isBlank()) {
                response.sendRedirect(request.getContextPath() + returnUrl);
            } else {
                String khId = request.getParameter("khachHangId");
                if (khId != null && !khId.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/khach-hang?id=" + khId);
                } else {
                    response.sendRedirect(request.getContextPath() + "/khach-hang");
                }
            }
        }
    }

    private String chuyenJsonNguoiLienHe(NguoiLienHe nlh) {
        return "{" +
                "\"id\":" + nlh.getId() + "," +
                "\"khachHangId\":" + nlh.getKhachHangId() + "," +
                "\"hoTen\":\"" + escapeJson(nlh.getHoTen()) + "\"," +
                "\"chucDanh\":\"" + escapeJson(nlh.getChucDanh()) + "\"," +
                "\"email\":\"" + escapeJson(nlh.getEmail()) + "\"," +
                "\"soDienThoai\":\"" + escapeJson(nlh.getSoDienThoai()) + "\"," +
                "\"vaiTro\":\"" + (nlh.getVaiTroQuyetDinh() != null ? nlh.getVaiTroQuyetDinh().getMa() : "") + "\"," +
                "\"vaiTroQuyetDinh\":\"" + (nlh.getVaiTroQuyetDinh() != null ? nlh.getVaiTroQuyetDinh().getMa() : "") + "\"," +
                "\"tenVaiTro\":\"" + escapeJson(nlh.getTenVaiTroHienThi()) + "\"," +
                "\"badgeVaiTro\":\"" + nlh.getBadgeClassVaiTro() + "\"," +
                "\"laDauMoiChinh\":" + nlh.isLaDauMoiChinh() + "," +
                "\"trangThai\":\"" + escapeJson(nlh.getTrangThai()) + "\"" +
                "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
