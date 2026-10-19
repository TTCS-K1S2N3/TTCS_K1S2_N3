package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.TruongTuyChinhDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TruongTuyChinh;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.PhienService;
import vn.nhom10.crm.service.TruongTuyChinhService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Servlet điều phối các chức năng quản lý Trường tuỳ chỉnh (Story S2-08).
 * Đáp ứng các URL:
 * - /truong-tuy-chinh: Danh sách trường tuỳ chỉnh cho Khách hàng & Cơ hội
 * - /truong-tuy-chinh/tao hoặc /tao-moi: Form tạo trường mới
 * - /truong-tuy-chinh/sua: Form sửa trường tuỳ chỉnh
 * - /truong-tuy-chinh/trang-thai: Bật / tắt trạng thái hoạt động
 * - /truong-tuy-chinh/xuat-excel: Xuất danh sách trường / bản mẫu Excel
 */
@WebServlet(name = "TruongTuyChinhServlet", urlPatterns = {
        "/truong-tuy-chinh",
        "/truong-tuy-chinh/tao",
        "/truong-tuy-chinh/tao-moi",
        "/truong-tuy-chinh/sua",
        "/truong-tuy-chinh/trang-thai",
        "/truong-tuy-chinh/xuat-excel"
})
public class TruongTuyChinhServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final TruongTuyChinhService service;

    public TruongTuyChinhServlet() {
        this.service = new TruongTuyChinhService();
    }

    public TruongTuyChinhServlet(TruongTuyChinhService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra phân quyền phía server (Chỉ Quản trị viên ADMIN / DIRECTOR được cấu hình)
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String fullPath = path + (pathInfo != null ? pathInfo : "");

        if (fullPath.endsWith("/tao") || fullPath.endsWith("/tao-moi")) {
            hienThiFormTao(request, response);
        } else if (fullPath.endsWith("/sua")) {
            hienThiFormSua(request, response);
        } else if (fullPath.endsWith("/xuat-excel")) {
            xuLyXuatExcel(request, response);
        } else {
            hienThiDanhSach(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra phân quyền phía server
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String fullPath = path + (pathInfo != null ? pathInfo : "");

        if (fullPath.endsWith("/tao") || fullPath.endsWith("/tao-moi")) {
            xuLyTaoTruong(request, response);
        } else if (fullPath.endsWith("/sua")) {
            xuLySuaTruong(request, response);
        } else if (fullPath.endsWith("/trang-thai")) {
            xuLyDoiTrangThai(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/truong-tuy-chinh");
        }
    }

    // =========================================================================
    // XỬ LÝ GET
    // =========================================================================

    private void hienThiDanhSach(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<TruongTuyChinh> dsKhachHang = service.layDanhSachTheoDoiTuong("KHACH_HANG", false);
        List<TruongTuyChinh> dsCoHoi = service.layDanhSachTheoDoiTuong("CO_HOI", false);

        String doiTuong = request.getParameter("doiTuong");
        if (doiTuong == null || doiTuong.isBlank()) {
            doiTuong = "KHACH_HANG";
        }

        // Chuyển thông báo flash từ session nếu có
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("thongBaoThanhCong") != null) {
                request.setAttribute("thongBaoThanhCong", session.getAttribute("thongBaoThanhCong"));
                session.removeAttribute("thongBaoThanhCong");
            }
            if (session.getAttribute("thongBaoLoi") != null) {
                request.setAttribute("thongBaoLoi", session.getAttribute("thongBaoLoi"));
                session.removeAttribute("thongBaoLoi");
            }
        }

        List<TruongTuyChinh> dsKhachHangHoatDong = service.layDanhSachTheoDoiTuong("KHACH_HANG", true);
        List<TruongTuyChinh> dsCoHoiHoatDong = service.layDanhSachTheoDoiTuong("CO_HOI", true);

        request.setAttribute("dsTruongKhachHang", dsKhachHang);
        request.setAttribute("dsTruongCoHoi", dsCoHoi);
        request.setAttribute("dsTruongKhachHangHoatDong", dsKhachHangHoatDong);
        request.setAttribute("dsTruongCoHoiHoatDong", dsCoHoiHoatDong);
        request.setAttribute("doiTuongHienTai", doiTuong);
        request.setAttribute("tabHienTai", doiTuong);

        request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/danh-sach.jsp").forward(request, response);
    }

    private void hienThiFormTao(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String doiTuong = request.getParameter("doiTuong");
        if (doiTuong == null || doiTuong.isBlank()) {
            doiTuong = "KHACH_HANG";
        }
        request.setAttribute("doiTuongMacDinh", doiTuong);

        TruongTuyChinhDTO oldInput = (TruongTuyChinhDTO) request.getAttribute("oldInput");
        if (oldInput == null) {
            oldInput = new TruongTuyChinhDTO();
            oldInput.setDoiTuong(doiTuong);
            oldInput.setThuTu(1);
            oldInput.setHienThiBoDac(true);
            oldInput.setHienThiExcel(true);
            request.setAttribute("oldInput", oldInput);
        }

        request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/tao-truong.jsp").forward(request, response);
    }

    private void hienThiFormSua(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        Long id = parseLong(idStr);

        if (id == null) {
            datThongBaoLoi(request, "ID trường tuỳ chỉnh không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/truong-tuy-chinh");
            return;
        }

        TruongTuyChinh model = service.layTheoId(id);
        if (model == null) {
            datThongBaoLoi(request, "Không tìm thấy trường tuỳ chỉnh cần sửa (ID=" + id + ").");
            response.sendRedirect(request.getContextPath() + "/truong-tuy-chinh");
            return;
        }

        TruongTuyChinhDTO dto = TruongTuyChinhDTO.tuModel(model);
        request.setAttribute("truong", dto);

        request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/sua-truong.jsp").forward(request, response);
    }

    private void xuLyXuatExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String doiTuong = request.getParameter("doiTuong");
        if (doiTuong == null || doiTuong.isBlank()) {
            doiTuong = "KHACH_HANG";
        }
        doiTuong = doiTuong.trim().toUpperCase();
        if (!"KHACH_HANG".equals(doiTuong) && !"CO_HOI".equals(doiTuong)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Loại đối tượng không hợp lệ: " + doiTuong);
            return;
        }

        List<String> cotChuan;
        List<String> keys;
        if ("CO_HOI".equals(doiTuong)) {
            cotChuan = List.of("Mã cơ hội", "Tên cơ hội", "Giá trị dự kiến", "Ngày chốt dự kiến", "Trạng thái");
            keys = List.of("ma_co_hoi", "ten_co_hoi", "gia_tri_du_kien", "ngay_chot_du_kien", "trang_thai");
        } else {
            cotChuan = List.of("Mã khách hàng", "Tên công ty", "Mã số thuế", "Địa chỉ", "Trạng thái");
            keys = List.of("ma_khach_hang", "ten_cong_ty", "ma_so_thue", "dia_chi", "trang_thai");
        }

        // Lấy dữ liệu thực tế từ cơ sở dữ liệu (tuyệt đối không dùng dữ liệu mẫu / mock / hardcode)
        List<Map<String, Object>> danhSachBanGhi = service.layDanhSachThucTeChoXuatExcel(doiTuong);

        List<Long> dsDoiTuongId = new ArrayList<>();
        if (danhSachBanGhi != null) {
            for (Map<String, Object> r : danhSachBanGhi) {
                Object idObj = r.get("id");
                if (idObj instanceof Number) {
                    dsDoiTuongId.add(((Number) idObj).longValue());
                } else if (idObj != null) {
                    try {
                        dsDoiTuongId.add(Long.parseLong(idObj.toString()));
                    } catch (Exception ignored) {}
                }
            }
        }

        Map<Long, Map<String, String>> customVals = service.layTatCaGiaTriTheoDanhSach(doiTuong, dsDoiTuongId);

        String format = request.getParameter("format");
        if ("csv".equalsIgnoreCase(format)) {
            String csv = service.xuatDuLieuExcelCSV(doiTuong, cotChuan, keys, danhSachBanGhi, customVals);
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"danh-sach-" + doiTuong.toLowerCase() + ".csv\"");
            try (OutputStream os = response.getOutputStream()) {
                os.write(csv.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } else {
            byte[] xlsx = service.xuatDuLieuExcelXLSX(doiTuong, cotChuan, keys, danhSachBanGhi, customVals);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"danh-sach-" + doiTuong.toLowerCase() + ".xlsx\"");
            response.setContentLength(xlsx.length);
            try (OutputStream os = response.getOutputStream()) {
                os.write(xlsx);
                os.flush();
            }
        }
    }

    // =========================================================================
    // XỬ LÝ POST
    // =========================================================================

    private void xuLyTaoTruong(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        TruongTuyChinhDTO dto = thuThapDuLieuForm(request, true);

        Map<String, String> formError = service.validateDinhNghiaTruong(dto, true);
        if (!formError.isEmpty()) {
            request.setAttribute("formError", formError);
            request.setAttribute("oldInput", dto);
            request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/tao-truong.jsp").forward(request, response);
            return;
        }

        Long userId = layIdNguoiDungHienTai(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        try {
            service.taoTruongTuyChinh(dto, userId);

            datThongBaoThanhCong(request, "Đã tạo thành công trường tuỳ chỉnh '" + dto.getNhanHien() + "'.");
            response.sendRedirect(request.getContextPath() + "/truong-tuy-chinh?doiTuong=" + dto.getDoiTuong());
        } catch (Exception e) {
            formError.put("global", e.getMessage());
            request.setAttribute("formError", formError);
            request.setAttribute("oldInput", dto);
            request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/tao-truong.jsp").forward(request, response);
        }
    }

    private void xuLySuaTruong(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        TruongTuyChinhDTO dto = thuThapDuLieuForm(request, false);
        String idStr = request.getParameter("id");
        dto.setId(parseLong(idStr));

        Map<String, String> formError = service.validateDinhNghiaTruong(dto, false);
        if (!formError.isEmpty()) {
            request.setAttribute("formError", formError);
            request.setAttribute("truong", dto);
            request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/sua-truong.jsp").forward(request, response);
            return;
        }

        try {
            service.capNhatTruongTuyChinh(dto);

            datThongBaoThanhCong(request, "Đã cập nhật trường tuỳ chỉnh '" + dto.getNhanHien() + "'.");
            response.sendRedirect(request.getContextPath() + "/truong-tuy-chinh?doiTuong=" + dto.getDoiTuong());
        } catch (Exception e) {
            formError.put("global", e.getMessage());
            request.setAttribute("formError", formError);
            request.setAttribute("truong", dto);
            request.getRequestDispatcher("/WEB-INF/views/truong-tuy-chinh/sua-truong.jsp").forward(request, response);
        }
    }

    private void xuLyDoiTrangThai(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        Long id = parseLong(idStr);
        String trangThaiStr = request.getParameter("trangThai");
        boolean hoatDong = parseBooleanParam(trangThaiStr, false);

        if (id != null) {
            service.doiTrangThai(id, hoatDong);
            datThongBaoThanhCong(request, "Đã cập nhật trạng thái hoạt động của trường.");
        }
        response.sendRedirect(request.getContextPath() + "/truong-tuy-chinh");
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private boolean parseBooleanParam(String param, boolean defaultValue) {
        if (param == null) {
            return defaultValue;
        }
        String p = param.trim().toLowerCase();
        if ("true".equals(p) || "on".equals(p) || "1".equals(p)) {
            return true;
        }
        if ("false".equals(p) || "off".equals(p) || "0".equals(p) || p.isEmpty()) {
            return false;
        }
        return false;
    }

    private TruongTuyChinhDTO thuThapDuLieuForm(HttpServletRequest request, boolean isCreate) {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong(request.getParameter("doiTuong"));
        dto.setTenTruong(request.getParameter("tenTruong"));
        dto.setNhanHien(request.getParameter("nhanHien"));
        dto.setKieuDuLieu(request.getParameter("kieuDuLieu"));

        // Checkbox values: parse an toàn không để chuỗi "false" biến thành true
        dto.setBatBuoc(parseBooleanParam(request.getParameter("batBuoc"), false));
        dto.setHienThiBoDac(parseBooleanParam(request.getParameter("hienThiBoDac"), false));
        dto.setHienThiExcel(parseBooleanParam(request.getParameter("hienThiExcel"), false));

        if (isCreate) {
            // Khi tạo mới: mặc định bật hoạt động nếu không có checkbox này
            dto.setDangHoatDong(parseBooleanParam(request.getParameter("dangHoatDong"), true));
        } else {
            // Khi sửa: lấy từ checkbox form sửa (unchecked gửi null -> false)
            dto.setDangHoatDong(parseBooleanParam(request.getParameter("dangHoatDong"), false));
        }

        String thuTuStr = request.getParameter("thuTu");
        try {
            dto.setThuTu(thuTuStr != null ? Integer.parseInt(thuTuStr.trim()) : 1);
        } catch (NumberFormatException e) {
            dto.setThuTu(1);
        }

        // Danh sách lựa chọn (name="danhSachLuaChon[]" hoặc "danhSachLuaChon")
        String[] options = request.getParameterValues("danhSachLuaChon[]");
        if (options == null) {
            options = request.getParameterValues("danhSachLuaChon");
        }
        if (options != null) {
            List<String> list = new ArrayList<>();
            for (String opt : options) {
                if (opt != null && !opt.trim().isEmpty()) {
                    list.add(opt.trim());
                }
            }
            dto.setDanhSachLuaChon(list);
        }

        return dto;
    }

    private boolean kiemTraQuyenQuanTri(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return false;
        }

        NguoiDung loggedInUser = (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return false;
        }

        // Story S2-08 Actor: Quản trị hệ thống (ADMIN)
        // Chỉ ADMIN (hoặc QUAN_TRI) được cấu hình Trường tuỳ chỉnh.
        if (!loggedInUser.coVaiTro(VaiTroEnum.ADMIN) && !loggedInUser.coVaiTro("QUAN_TRI")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền quản lý Trường tuỳ chỉnh. Chức năng này chỉ dành cho Quản trị hệ thống.");
            return false;
        }
        return true;
    }

    private Long layIdNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            NguoiDung nd = (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
            if (nd != null) {
                return nd.getId();
            }
        }
        return null;
    }

    private Long parseLong(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void datThongBaoThanhCong(HttpServletRequest request, String msg) {
        HttpSession session = request.getSession(true);
        session.setAttribute("thongBaoThanhCong", msg);
    }

    private void datThongBaoLoi(HttpServletRequest request, String msg) {
        HttpSession session = request.getSession(true);
        session.setAttribute("thongBaoLoi", msg);
    }
}
