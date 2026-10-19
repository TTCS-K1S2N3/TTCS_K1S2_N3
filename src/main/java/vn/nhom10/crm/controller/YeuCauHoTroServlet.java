package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.ThongTinRuiRoDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.MucUuTienYeuCauEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TrangThaiYeuCauEnum;
import vn.nhom10.crm.model.YeuCauHoTro;
import vn.nhom10.crm.service.YeuCauHoTroService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.service.PermissionService;

/**
 * Servlet tiếp nhận và xử lý Yêu cầu hỗ trợ sau bán & Cảnh báo rủi ro rời bỏ (Story S3-08).
 * Mapping: /yeu-cau-ho-tro, /yeu-cau-ho-tro/*
 */
@WebServlet(name = "YeuCauHoTroServlet", urlPatterns = {"/yeu-cau-ho-tro", "/yeu-cau-ho-tro/*"})
public class YeuCauHoTroServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(YeuCauHoTroServlet.class.getName());

    private YeuCauHoTroService yeuCauHoTroService;
    private KhachHangDAO khachHangDAO;
    private NguoiDungDAO nguoiDungDAO;
    private PermissionService permissionService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.yeuCauHoTroService = new YeuCauHoTroService();
        this.khachHangDAO = new KhachHangDAO();
        this.nguoiDungDAO = new NguoiDungDAO();
        this.permissionService = new PermissionService();
    }

    public void setYeuCauHoTroService(YeuCauHoTroService service) {
        this.yeuCauHoTroService = service;
    }

    public void setKhachHangDAO(KhachHangDAO khachHangDAO) {
        this.khachHangDAO = khachHangDAO;
    }

    public void setNguoiDungDAO(NguoiDungDAO nguoiDungDAO) {
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        if (permissionService != null && !permissionService.coQuyen(currentUser, "KHACH_HANG", MucQuyen.READ)) {
            if (laYeuCauAjax(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Bạn không có quyền truy cập module Khách hàng.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Bạn không có quyền truy cập module Khách hàng.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return;
        }

        String action = request.getParameter("action");

        // 1. API lấy thông tin trạng thái cờ rủi ro rời bỏ dạng JSON
        if ("api-rui-ro".equalsIgnoreCase(action)) {
            xuLyApiThongTinRuiRo(request, response);
            return;
        }

        // 2. API lấy danh sách yêu cầu hỗ trợ của một khách hàng dạng JSON
        if ("api-danh-sach".equalsIgnoreCase(action)) {
            xuLyApiDanhSach(request, response);
            return;
        }

        // 3. Hiển thị trang quản lý Yêu cầu hỗ trợ (dành cho CSKH / Kỹ thuật / NVKD)
        xuLyXemDanhSach(request, response, currentUser);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        if (permissionService != null && !permissionService.coQuyen(currentUser, "KHACH_HANG", MucQuyen.WRITE)) {
            if (laYeuCauAjax(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Bạn không có quyền thao tác trên khách hàng (yêu cầu quyền WRITE trở lên).\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Bạn không có quyền thao tác trên khách hàng (yêu cầu quyền WRITE trở lên).");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "tao";
        }

        switch (action.toLowerCase()) {
            case "cap-nhat-trang-thai":
                xuLyCapNhatTrangThai(request, response, currentUser);
                break;
            case "cap-nhat":
                xuLyCapNhatYeuCau(request, response, currentUser);
                break;
            case "tao":
            default:
                xuLyTaoYeuCau(request, response, currentUser);
                break;
        }
    }

    /**
     * Xử lý tạo mới yêu cầu hỗ trợ (AC1, AC2).
     */
    private void xuLyTaoYeuCau(HttpServletRequest request, HttpServletResponse response, NguoiDung currentUser)
            throws IOException, ServletException {
        boolean laAjax = laYeuCauAjax(request);

        try {
            String paramKhachHangId = request.getParameter("khachHangId");
            if (paramKhachHangId == null || paramKhachHangId.isBlank()) {
                guiPhanHoiLoi(request, response, "Vui lòng chọn khách hàng cần ghi nhận hỗ trợ.", laAjax, null);
                return;
            }
            Long khachHangId = Long.parseLong(paramKhachHangId.trim());

            String tieuDe = request.getParameter("tieuDe");
            if (tieuDe == null || tieuDe.trim().isEmpty()) {
                guiPhanHoiLoi(request, response, "Tiêu đề yêu cầu hỗ trợ không được để trống.", laAjax, khachHangId);
                return;
            }

            String noiDung = request.getParameter("noiDung");
            String mucUuTien = request.getParameter("mucUuTien");
            String trangThai = request.getParameter("trangThai");

            Long nguoiXuLyId = null;
            String paramNguoiXuLyId = request.getParameter("nguoiXuLyId");
            if (paramNguoiXuLyId != null && !paramNguoiXuLyId.isBlank()) {
                try {
                    nguoiXuLyId = Long.parseLong(paramNguoiXuLyId.trim());
                } catch (NumberFormatException ignored) {}
            }

            Long nguoiLienHeId = null;
            String paramNguoiLienHeId = request.getParameter("nguoiLienHeId");
            if (paramNguoiLienHeId != null && !paramNguoiLienHeId.isBlank()) {
                try {
                    nguoiLienHeId = Long.parseLong(paramNguoiLienHeId.trim());
                } catch (NumberFormatException ignored) {}
            }

            YeuCauHoTro ycht = new YeuCauHoTro();
            ycht.setKhachHangId(khachHangId);
            ycht.setNguoiLienHeId(nguoiLienHeId);
            ycht.setNguoiXuLyId(nguoiXuLyId);
            ycht.setTieuDe(tieuDe.trim());
            ycht.setNoiDung(noiDung != null ? noiDung.trim() : null);
            ycht.setMucUuTien(mucUuTien);
            ycht.setTrangThai(trangThai != null && !trangThai.isBlank() ? trangThai : "MOI");

            YeuCauHoTro daTao = yeuCauHoTroService.ghiNhanYeuCau(ycht, currentUser);

            // Kiểm tra trạng thái rủi ro rời bỏ sau khi ghi nhận
            ThongTinRuiRoDTO ruiRo = yeuCauHoTroService.layThongTinRuiRo(khachHangId);
            boolean coRuiRo = ruiRo != null && ruiRo.isCoRuiRo();

            String thongBao = "Đã ghi nhận yêu cầu hỗ trợ [" + daTao.getMaYeuCau() + "] thành công!";
            if (coRuiRo) {
                thongBao += " LƯU Ý: Khách hàng đã đạt ngưỡng " + ruiRo.getSoYeuCauChuaXuLy() +
                        "/" + ruiRo.getNguongRuiRo() + " yêu cầu chưa xử lý và ĐÃ ĐƯỢC TỰ ĐỘNG GẮN CỜ RỦI RO!";
            }

            if (laAjax) {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("{\"thanhCong\":true,\"thongBao\":\"" + escapeJson(thongBao) + "\"," +
                        "\"maYeuCau\":\"" + daTao.getMaYeuCau() + "\"," +
                        "\"coRuiRo\":" + coRuiRo + "}");
                out.flush();
            } else {
                String redirectUrl = request.getParameter("redirectUrl");
                if (redirectUrl == null || redirectUrl.isBlank()) {
                    redirectUrl = request.getContextPath() + "/chi-tiet-ban-ghi?id=" + khachHangId;
                }
                request.getSession().setAttribute("thongBaoThanhCong", thongBao);
                response.sendRedirect(redirectUrl);
            }

        } catch (IllegalArgumentException e) {
            guiPhanHoiLoi(request, response, e.getMessage(), laAjax, null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi ghi nhận yêu cầu hỗ trợ: " + e.getMessage(), e);
            guiPhanHoiLoi(request, response, "Đã xảy ra lỗi trong quá trình ghi nhận yêu cầu: " + e.getMessage(), laAjax, null);
        }
    }

    /**
     * Xử lý cập nhật trạng thái yêu cầu hỗ trợ (AC1, AC2).
     */
    private void xuLyCapNhatTrangThai(HttpServletRequest request, HttpServletResponse response, NguoiDung currentUser)
            throws IOException, ServletException {
        boolean laAjax = laYeuCauAjax(request);

        try {
            String paramId = request.getParameter("id");
            String trangThaiMoi = request.getParameter("trangThai");

            if (paramId == null || paramId.isBlank() || trangThaiMoi == null || trangThaiMoi.isBlank()) {
                guiPhanHoiLoi(request, response, "Thiếu ID hoặc trạng thái cần cập nhật.", laAjax, null);
                return;
            }

            Long id = Long.parseLong(paramId.trim());
            YeuCauHoTro hienTai = yeuCauHoTroService.timTheoId(id);
            if (hienTai == null) {
                guiPhanHoiLoi(request, response, "Không tìm thấy yêu cầu hỗ trợ với ID: " + id, laAjax, null);
                return;
            }

            boolean ok = yeuCauHoTroService.capNhatTrangThai(id, trangThaiMoi.trim(), currentUser);
            if (!ok) {
                guiPhanHoiLoi(request, response, "Không thể cập nhật trạng thái yêu cầu.", laAjax, hienTai.getKhachHangId());
                return;
            }

            ThongTinRuiRoDTO ruiRo = yeuCauHoTroService.layThongTinRuiRo(hienTai.getKhachHangId());
            boolean coRuiRo = ruiRo != null && ruiRo.isCoRuiRo();

            String thongBao = "Đã cập nhật trạng thái yêu cầu [" + hienTai.getMaYeuCau() + "] thành công.";
            if (!coRuiRo) {
                thongBao += " Khách hàng hiện không còn bị gắn cờ rủi ro rời bỏ.";
            }

            if (laAjax) {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print("{\"thanhCong\":true,\"thongBao\":\"" + escapeJson(thongBao) + "\",\"coRuiRo\":" + coRuiRo + "}");
                out.flush();
            } else {
                String redirectUrl = request.getParameter("redirectUrl");
                if (redirectUrl == null || redirectUrl.isBlank()) {
                    redirectUrl = request.getContextPath() + "/chi-tiet-ban-ghi?id=" + hienTai.getKhachHangId();
                }
                request.getSession().setAttribute("thongBaoThanhCong", thongBao);
                response.sendRedirect(redirectUrl);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật trạng thái: " + e.getMessage(), e);
            guiPhanHoiLoi(request, response, "Lỗi hệ thống: " + e.getMessage(), laAjax, null);
        }
    }

    /**
     * Xử lý cập nhật toàn bộ yêu cầu hỗ trợ.
     */
    private void xuLyCapNhatYeuCau(HttpServletRequest request, HttpServletResponse response, NguoiDung currentUser)
            throws IOException, ServletException {
        boolean laAjax = laYeuCauAjax(request);
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            YeuCauHoTro ycht = yeuCauHoTroService.timTheoId(id);
            if (ycht == null) {
                guiPhanHoiLoi(request, response, "Không tìm thấy yêu cầu cần sửa.", laAjax, null);
                return;
            }

            String tieuDe = request.getParameter("tieuDe");
            String noiDung = request.getParameter("noiDung");
            String mucUuTien = request.getParameter("mucUuTien");
            String trangThai = request.getParameter("trangThai");
            String paramNguoiXuLyId = request.getParameter("nguoiXuLyId");

            if (tieuDe != null && !tieuDe.isBlank()) ycht.setTieuDe(tieuDe.trim());
            if (noiDung != null) ycht.setNoiDung(noiDung.trim());
            if (mucUuTien != null && !mucUuTien.isBlank()) ycht.setMucUuTien(mucUuTien.trim());
            if (trangThai != null && !trangThai.isBlank()) ycht.setTrangThai(trangThai.trim());

            if (paramNguoiXuLyId != null && !paramNguoiXuLyId.isBlank()) {
                try {
                    ycht.setNguoiXuLyId(Long.parseLong(paramNguoiXuLyId.trim()));
                } catch (NumberFormatException ignored) {}
            }

            yeuCauHoTroService.capNhatYeuCau(ycht, currentUser);

            if (laAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print("{\"thanhCong\":true,\"thongBao\":\"Cập nhật thành công.\"}");
            } else {
                String redirectUrl = request.getParameter("redirectUrl");
                if (redirectUrl == null || redirectUrl.isBlank()) {
                    redirectUrl = request.getContextPath() + "/chi-tiet-ban-ghi?id=" + ycht.getKhachHangId();
                }
                request.getSession().setAttribute("thongBaoThanhCong", "Cập nhật yêu cầu hỗ trợ thành công.");
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            guiPhanHoiLoi(request, response, "Lỗi cập nhật: " + e.getMessage(), laAjax, null);
        }
    }

    /**
     * Hiển thị giao diện danh sách yêu cầu hỗ trợ.
     */
    private void xuLyXemDanhSach(HttpServletRequest request, HttpServletResponse response, NguoiDung currentUser)
            throws ServletException, IOException {
        String paramKhachHangId = request.getParameter("khachHangId");
        Long khachHangId = null;
        if (paramKhachHangId != null && !paramKhachHangId.isBlank()) {
            try { khachHangId = Long.parseLong(paramKhachHangId.trim()); } catch (NumberFormatException ignored) {}
        }

        String trangThai = request.getParameter("trangThai");
        String mucUuTien = request.getParameter("mucUuTien");

        String paramNguoiXuLyId = request.getParameter("nguoiXuLyId");
        Long nguoiXuLyId = null;
        if (paramNguoiXuLyId != null && !paramNguoiXuLyId.isBlank()) {
            try { nguoiXuLyId = Long.parseLong(paramNguoiXuLyId.trim()); } catch (NumberFormatException ignored) {}
        }

        String tuKhoa = request.getParameter("tuKhoa");

        List<YeuCauHoTro> danhSach = yeuCauHoTroService.layDanhSachYeuCau(
                khachHangId, trangThai, mucUuTien, nguoiXuLyId, tuKhoa, 100, 0
        );

        List<KhachHang> danhSachKhachHang = khachHangDAO.layTatCa();
        List<NguoiDung> danhSachNhanVien = nguoiDungDAO.layTatCa();

        request.setAttribute("currentUser", currentUser);
        request.setAttribute("danhSachYeuCau", danhSach);
        request.setAttribute("danhSachKhachHang", danhSachKhachHang);
        request.setAttribute("danhSachNhanVien", danhSachNhanVien);
        request.setAttribute("mucUuTienList", MucUuTienYeuCauEnum.values());
        request.setAttribute("trangThaiList", TrangThaiYeuCauEnum.values());
        request.setAttribute("khachHangIdChon", khachHangId);
        request.setAttribute("trangThaiChon", trangThai);
        request.setAttribute("mucUuTienChon", mucUuTien);
        request.setAttribute("nguoiXuLyIdChon", nguoiXuLyId);
        request.setAttribute("tuKhoa", tuKhoa != null ? tuKhoa : "");

        response.setStatus(HttpServletResponse.SC_OK);
        request.getRequestDispatcher("/WEB-INF/views/yeu-cau-ho-tro/danh-sach.jsp").forward(request, response);
    }

    /**
     * API trả về thông tin rủi ro rời bỏ dạng JSON.
     */
    private void xuLyApiThongTinRuiRo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String paramKhId = request.getParameter("khachHangId");
        if (paramKhId == null || paramKhId.isBlank()) {
            out.print("{\"thanhCong\":false,\"thongBao\":\"Thiếu khachHangId.\"}");
            return;
        }

        try {
            Long khId = Long.parseLong(paramKhId.trim());
            ThongTinRuiRoDTO ruiRo = yeuCauHoTroService.layThongTinRuiRo(khId);
            if (ruiRo == null) {
                out.print("{\"thanhCong\":false,\"thongBao\":\"Không tìm thấy khách hàng.\"}");
                return;
            }

            out.print("{\"thanhCong\":true," +
                    "\"khachHangId\":" + ruiRo.getKhachHangId() + "," +
                    "\"coRuiRo\":" + ruiRo.isCoRuiRo() + "," +
                    "\"soChuaXuLy\":" + ruiRo.getSoYeuCauChuaXuLy() + "," +
                    "\"nguong\":" + ruiRo.getNguongRuiRo() + "," +
                    "\"thongDiep\":\"" + escapeJson(ruiRo.getThongDiepCanhBao()) + "\"}");
        } catch (NumberFormatException e) {
            out.print("{\"thanhCong\":false,\"thongBao\":\"khachHangId không hợp lệ.\"}");
        }
    }

    /**
     * API trả về danh sách yêu cầu hỗ trợ dạng JSON.
     */
    private void xuLyApiDanhSach(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String paramKhId = request.getParameter("khachHangId");
        if (paramKhId == null || paramKhId.isBlank()) {
            out.print("[]");
            return;
        }

        try {
            Long khId = Long.parseLong(paramKhId.trim());
            List<YeuCauHoTro> list = yeuCauHoTroService.layDanhSachTheoKhachHang(khId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                YeuCauHoTro y = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(y.getId()).append(",")
                        .append("\"maYeuCau\":\"").append(escapeJson(y.getMaYeuCau())).append("\",")
                        .append("\"tieuDe\":\"").append(escapeJson(y.getTieuDe())).append("\",")
                        .append("\"mucUuTien\":\"").append(y.getMucUuTien()).append("\",")
                        .append("\"trangThai\":\"").append(y.getTrangThai()).append("\",")
                        .append("\"tenNguoiXuLy\":\"").append(escapeJson(y.getTenNguoiXuLy() != null ? y.getTenNguoiXuLy() : "Chưa phân công")).append("\"")
                        .append("}");
            }
            sb.append("]");
            out.print(sb.toString());
        } catch (NumberFormatException e) {
            out.print("[]");
        }
    }

    private void guiPhanHoiLoi(HttpServletRequest request, HttpServletResponse response,
                               String thongBaoLoi, boolean laAjax, Long khachHangId)
            throws IOException, ServletException {
        if (laAjax) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"thanhCong\":false,\"thongBao\":\"" + escapeJson(thongBaoLoi) + "\"}");
            out.flush();
        } else {
            request.getSession().setAttribute("thongBaoLoi", thongBaoLoi);
            if (khachHangId != null) {
                response.sendRedirect(request.getContextPath() + "/chi-tiet-ban-ghi?id=" + khachHangId);
            } else {
                response.sendRedirect(request.getContextPath() + "/yeu-cau-ho-tro");
            }
        }
    }

    private boolean laYeuCauAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String paramAjax = request.getParameter("ajax");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith) ||
                "true".equalsIgnoreCase(paramAjax) ||
                (accept != null && accept.contains("application/json"));
    }

    private NguoiDung layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoiDung") != null) {
            return (NguoiDung) session.getAttribute("nguoiDung");
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
