package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.KetQuaKiemTraDongCoHoiDTO;
import vn.nhom10.crm.model.DoiThu;
import vn.nhom10.crm.model.LyDoThangThua;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LyDoThangThuaService;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller phục vụ khai báo và quản lý danh mục lý do thắng thua và đối thủ cạnh tranh (Story S2-10).
 * Cho phép Giám đốc kinh doanh (DIRECTOR) và Quản trị viên (ADMIN) cấu hình danh mục;
 * các vai trò bán hàng khác có quyền xem và tra cứu phục vụ đóng cơ hội (Sprint 5).
 * URL: /danh-muc/ly-do-thang-thua, /co-hoi/ly-do-thang-thua, /danh-muc/doi-thu
 */
@WebServlet(name = "LyDoThangThuaServlet", urlPatterns = {
        "/danh-muc/ly-do-thang-thua",
        "/co-hoi/ly-do-thang-thua",
        "/danh-muc/doi-thu"
})
public class LyDoThangThuaServlet extends HttpServlet {

    private final LyDoThangThuaService lyDoService;

    public LyDoThangThuaServlet() {
        this.lyDoService = new LyDoThangThuaService();
    }

    public LyDoThangThuaServlet(LyDoThangThuaService lyDoService) {
        this.lyDoService = lyDoService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        boolean coQuyenQuanLy = kiemTraQuyenQuanLy(user);
        request.setAttribute("coQuyenQuanLy", coQuyenQuanLy);
        request.setAttribute("userHienTai", user);

        // Đọc query parameters
        String tab = request.getParameter("tab");
        String uri = request.getRequestURI();
        if (tab == null || tab.isBlank()) {
            if (uri.endsWith("/doi-thu")) {
                tab = "doi-thu";
            } else {
                tab = "thang";
            }
        }
        request.setAttribute("currentTab", tab);

        String tuKhoa = request.getParameter("tuKhoa");
        String trangThaiLoc = request.getParameter("trangThai");
        request.setAttribute("tuKhoa", tuKhoa);
        request.setAttribute("trangThaiLoc", trangThaiLoc);

        // Lấy danh sách đầy đủ
        List<LyDoThangThua> dsLyDoThang = lyDoService.layDanhSachLyDoThang();
        List<LyDoThangThua> dsLyDoThua = lyDoService.layDanhSachLyDoThua();
        List<DoiThu> dsDoiThu = lyDoService.layDanhSachDoiThu();

        // Thống kê KPIs
        long soThangHoatDong = dsLyDoThang.stream().filter(LyDoThangThua::isHoatDong).count();
        long soThuaHoatDong = dsLyDoThua.stream().filter(LyDoThangThua::isHoatDong).count();
        long soDoiThuHoatDong = dsDoiThu.stream().filter(DoiThu::isHoatDong).count();
        int tongThamChieu = dsLyDoThang.stream().mapToInt(LyDoThangThua::getSoCoHoiThamChieu).sum()
                + dsLyDoThua.stream().mapToInt(LyDoThangThua::getSoCoHoiThamChieu).sum()
                + dsDoiThu.stream().mapToInt(DoiThu::getSoCoHoiThamChieu).sum();

        request.setAttribute("tongLyDoThang", dsLyDoThang.size());
        request.setAttribute("soThangHoatDong", soThangHoatDong);
        request.setAttribute("tongLyDoThua", dsLyDoThua.size());
        request.setAttribute("soThuaHoatDong", soThuaHoatDong);
        request.setAttribute("tongDoiThu", dsDoiThu.size());
        request.setAttribute("soDoiThuHoatDong", soDoiThuHoatDong);
        request.setAttribute("tongThamChieu", tongThamChieu);

        // Áp dụng bộ lọc tìm kiếm nếu có
        List<LyDoThangThua> dsLyDoThangHienThi = locDanhSachLyDo(dsLyDoThang, tuKhoa, trangThaiLoc);
        List<LyDoThangThua> dsLyDoThuaHienThi = locDanhSachLyDo(dsLyDoThua, tuKhoa, trangThaiLoc);
        List<DoiThu> dsDoiThuHienThi = locDanhSachDoiThu(dsDoiThu, tuKhoa, trangThaiLoc);

        request.setAttribute("dsLyDoThang", dsLyDoThangHienThi);
        request.setAttribute("dsLyDoThua", dsLyDoThuaHienThi);
        request.setAttribute("dsDoiThu", dsDoiThuHienThi);

        // Danh sách phục vụ Sprint 5 Dropdowns (chỉ các mục đang hoạt động)
        request.setAttribute("dsLyDoThangKhaDung", lyDoService.layLyDoThangKhaDung());
        request.setAttribute("dsLyDoThuaKhaDung", lyDoService.layLyDoThuaKhaDung());
        request.setAttribute("dsDoiThuKhaDung", lyDoService.layDoiThuKhaDung());

        // Hỗ trợ trả về API JSON nếu có tham số 'ajax=1'
        if ("1".equals(request.getParameter("ajax"))) {
            traVeJson(response, dsLyDoThangHienThi, dsLyDoThuaHienThi, dsDoiThuHienThi);
            return;
        }

        // Chuyển tiếp tới giao diện JSP
        request.getRequestDispatcher("/WEB-INF/views/co-hoi/ly-do-thang-thua.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            phanHoiLoi(request, response, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            phanHoiLoi(request, response, "Hành động (action) không hợp lệ.");
            return;
        }

        // 1. Mô phỏng kiểm tra đóng cơ hội Sprint 5 (Mọi nhân viên bán hàng đều có thể thử nghiệm AC3)
        if ("kiem-tra-dong-co-hoi".equals(action)) {
            xuLyKiemTraDongCoHoi(request, response);
            return;
        }

        // 2. Các thao tác thêm, sửa, xóa, bật/tắt yêu cầu quyền Giám đốc kinh doanh hoặc Quản trị viên
        try {
            lyDoService.kiemTraQuyenQuanLy(user);
        } catch (LoiPhanQuyenException e) {
            phanHoiLoi(request, response, e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            switch (action) {
                case "them-ly-do":
                case "sua-ly-do":
                    xuLyLuuLyDo(request, response, "sua-ly-do".equals(action));
                    break;
                case "doi-trang-thai-ly-do":
                    xuLyDoiTrangThaiLyDo(request, response);
                    break;
                case "sap-xep-ly-do":
                    xuLySapXepLyDo(request, response);
                    break;
                case "xoa-ly-do":
                    xuLyXoaLyDo(request, response);
                    break;
                case "them-doi-thu":
                case "sua-doi-thu":
                    xuLyLuuDoiThu(request, response, "sua-doi-thu".equals(action));
                    break;
                case "doi-trang-thai-doi-thu":
                    xuLyDoiTrangThaiDoiThu(request, response);
                    break;
                case "xoa-doi-thu":
                    xuLyXoaDoiThu(request, response);
                    break;
                case "nap-du-lieu-mau":
                    xuLyNapDuLieuMau(request, response);
                    break;
                default:
                    phanHoiLoi(request, response, "Hành động '" + action + "' không được hỗ trợ.");
                    break;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            phanHoiLoi(request, response, e.getMessage());
        } catch (Exception e) {
            phanHoiLoi(request, response, "Đã xảy ra lỗi trong quá trình xử lý: " + e.getMessage());
        }
    }

    // =========================================================================
    // XỬ LÝ NGUỒN DỮ LIỆU & NGIỆP VỤ
    // =========================================================================

    private void xuLyNapDuLieuMau(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int count = lyDoService.napDuLieuMauNeuTrong();
        String tab = request.getParameter("tab");
        if (tab == null || tab.isBlank()) tab = "thang";
        if (count > 0) {
            phanHoiThanhCong(request, response, "Đã nạp thành công " + count + " bản ghi danh mục mẫu chuẩn doanh nghiệp.", tab);
        } else {
            phanHoiThanhCong(request, response, "Danh mục đã có dữ liệu từ trước, không cần nạp lại.", tab);
        }
    }

    private void xuLyLuuLyDo(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws IOException {
        String idStr = request.getParameter("id");
        String maLyDo = request.getParameter("maLyDo");
        String tenLyDo = request.getParameter("tenLyDo");
        String loai = request.getParameter("loai");
        String thuTuStr = request.getParameter("thuTuHienThi");
        String hoatDongStr = request.getParameter("hoatDong");

        LyDoThangThua lyDo = new LyDoThangThua();
        if (isUpdate) {
            if (idStr == null || idStr.isBlank()) {
                throw new IllegalArgumentException("Thiếu ID lý do cần cập nhật");
            }
            try {
                long parsedId = Long.parseLong(idStr.trim());
                if (parsedId <= 0) {
                    throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
                }
                lyDo.setId(parsedId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
            }
        }
        lyDo.setMaLyDo(maLyDo);
        lyDo.setTenLyDo(tenLyDo);
        lyDo.setLoai(loai);

        int thuTu = 0;
        if (thuTuStr != null && !thuTuStr.isBlank()) {
            try {
                thuTu = Integer.parseInt(thuTuStr.trim());
            } catch (NumberFormatException ignored) {}
        }
        lyDo.setThuTuHienThi(thuTu);
        lyDo.setHoatDong("1".equals(hoatDongStr) || "true".equalsIgnoreCase(hoatDongStr) || "on".equalsIgnoreCase(hoatDongStr));

        lyDoService.luuLyDo(lyDo);

        String redirectTab = LyDoThangThua.LOAI_THUA.equalsIgnoreCase(loai) ? "thua" : "thang";
        phanHoiThanhCong(request, response,
                (isUpdate ? "Cập nhật" : "Thêm mới") + " lý do thành công.",
                redirectTab);
    }

    private void xuLyDoiTrangThaiLyDo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        String hoatDongStr = request.getParameter("hoatDong");
        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("Thiếu ID lý do cần thay đổi trạng thái");
        }
        Long id;
        try {
            id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
        }
        boolean hoatDong = "1".equals(hoatDongStr) || "true".equalsIgnoreCase(hoatDongStr);

        lyDoService.doiTrangThaiLyDo(id, hoatDong);

        String tab = request.getParameter("tab");
        phanHoiThanhCong(request, response,
                "Đã " + (hoatDong ? "kích hoạt" : "tắt") + " lý do thành công.",
                (tab != null && !tab.isBlank()) ? tab : "thang");
    }

    private void xuLySapXepLyDo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        String thuTuStr = request.getParameter("thuTuHienThi");
        if (idStr == null || idStr.isBlank() || thuTuStr == null || thuTuStr.isBlank()) {
            throw new IllegalArgumentException("Thiếu thông tin ID hoặc thứ tự sắp xếp");
        }
        Long id;
        try {
            id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
        }
        int thuTu;
        try {
            thuTu = Integer.parseInt(thuTuStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Thứ tự hiển thị phải là số nguyên: " + thuTuStr);
        }

        lyDoService.capNhatThuTuLyDo(id, thuTu);

        String tab = request.getParameter("tab");
        phanHoiThanhCong(request, response, "Cập nhật thứ tự hiển thị thành công.", (tab != null) ? tab : "thang");
    }

    private void xuLyXoaLyDo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("Thiếu ID lý do cần xóa");
        }
        Long id;
        try {
            id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID lý do không hợp lệ: " + idStr);
        }
        lyDoService.xoaLyDo(id);

        String tab = request.getParameter("tab");
        phanHoiThanhCong(request, response, "Đã xóa lý do thành công.", (tab != null) ? tab : "thang");
    }

    private void xuLyLuuDoiThu(HttpServletRequest request, HttpServletResponse response, boolean isUpdate)
            throws IOException {
        String idStr = request.getParameter("id");
        String maDoiThu = request.getParameter("maDoiThu");
        String tenDoiThu = request.getParameter("tenDoiThu");
        String website = request.getParameter("website");
        String ghiChu = request.getParameter("ghiChu");
        String hoatDongStr = request.getParameter("hoatDong");

        DoiThu doiThu = new DoiThu();
        if (isUpdate) {
            if (idStr == null || idStr.isBlank()) {
                throw new IllegalArgumentException("Thiếu ID đối thủ cần cập nhật");
            }
            try {
                long parsedId = Long.parseLong(idStr.trim());
                if (parsedId <= 0) {
                    throw new IllegalArgumentException("ID đối thủ không hợp lệ: " + idStr);
                }
                doiThu.setId(parsedId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID đối thủ không hợp lệ: " + idStr);
            }
        }
        doiThu.setMaDoiThu(maDoiThu);
        doiThu.setTenDoiThu(tenDoiThu);
        doiThu.setWebsite(website);
        doiThu.setGhiChu(ghiChu);
        doiThu.setHoatDong("1".equals(hoatDongStr) || "true".equalsIgnoreCase(hoatDongStr) || "on".equalsIgnoreCase(hoatDongStr));

        lyDoService.luuDoiThu(doiThu);

        phanHoiThanhCong(request, response,
                (isUpdate ? "Cập nhật" : "Thêm mới") + " đối thủ cạnh tranh thành công.",
                "doi-thu");
    }

    private void xuLyDoiTrangThaiDoiThu(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        String hoatDongStr = request.getParameter("hoatDong");
        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("Thiếu ID đối thủ cần thay đổi trạng thái");
        }
        Long id;
        try {
            id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                throw new IllegalArgumentException("ID đối thủ không hợp lệ: " + idStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID đối thủ không hợp lệ: " + idStr);
        }
        boolean hoatDong = "1".equals(hoatDongStr) || "true".equalsIgnoreCase(hoatDongStr);

        lyDoService.doiTrangThaiDoiThu(id, hoatDong);

        phanHoiThanhCong(request, response,
                "Đã " + (hoatDong ? "kích hoạt theo dõi" : "ngừng theo dõi") + " đối thủ thành công.",
                "doi-thu");
    }

    private void xuLyXoaDoiThu(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("Thiếu ID đối thủ cần xóa");
        }
        Long id;
        try {
            id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                throw new IllegalArgumentException("ID đối thủ không hợp lệ: " + idStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID đối thủ không hợp lệ: " + idStr);
        }
        lyDoService.xoaDoiThu(id);

        phanHoiThanhCong(request, response, "Đã xóa đối thủ cạnh tranh thành công.", "doi-thu");
    }

    private void xuLyKiemTraDongCoHoi(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String trangThai = request.getParameter("trangThaiDong");
        String lyDoIdStr = request.getParameter("lyDoThangThuaId");
        String doiThuIdStr = request.getParameter("doiThuId");
        String giaTriStr = request.getParameter("giaTriChotThucTe");
        String ngayKyStr = request.getParameter("ngayKy");
        String ghiChu = request.getParameter("ghiChu");

        Long lyDoId = null;
        if (lyDoIdStr != null && !lyDoIdStr.isBlank()) {
            try {
                lyDoId = Long.parseLong(lyDoIdStr.trim());
            } catch (NumberFormatException ignored) {
                lyDoId = -1L;
            }
        }
        Long doiThuId = null;
        if (doiThuIdStr != null && !doiThuIdStr.isBlank()) {
            try {
                doiThuId = Long.parseLong(doiThuIdStr.trim());
            } catch (NumberFormatException ignored) {
                doiThuId = -1L;
            }
        }

        BigDecimal giaTri = null;
        if (giaTriStr != null && !giaTriStr.isBlank()) {
            try {
                String cleanVal = giaTriStr.replace(".", "").replace(",", ".");
                giaTri = new BigDecimal(cleanVal);
            } catch (Exception ignored) {}
        }

        LocalDate ngayKy = null;
        if (ngayKyStr != null && !ngayKyStr.isBlank()) {
            try {
                ngayKy = LocalDate.parse(ngayKyStr.trim());
            } catch (DateTimeParseException ignored) {}
        }

        KetQuaKiemTraDongCoHoiDTO ketQua = lyDoService.kiemTraDongCoHoiSprint5(
                trangThai, lyDoId, doiThuId, giaTri, ngayKy, ghiChu);

        // Trả kết quả JSON
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"hopLe\":").append(ketQua.isHopLe()).append(",");
        json.append("\"thongBao\":\"").append(escapeJson(ketQua.getThongBaoChiTiet())).append("\",");
        json.append("\"danhSachLoi\":[");
        for (int i = 0; i < ketQua.getDanhSachLoi().size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(escapeJson(ketQua.getDanhSachLoi().get(i))).append("\"");
        }
        json.append("]");
        json.append("}");
        out.print(json.toString());
        out.flush();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private boolean kiemTraQuyenQuanLy(NguoiDung user) {
        if (user == null || !user.dangHoatDong()) return false;
        Set<VaiTroEnum> vaiTros = user.getDanhSachVaiTroEnum();
        if (vaiTros == null) return false;
        return vaiTros.contains(VaiTroEnum.ADMIN) || vaiTros.contains(VaiTroEnum.DIRECTOR);
    }

    private List<LyDoThangThua> locDanhSachLyDo(List<LyDoThangThua> goc, String tuKhoa, String trangThai) {
        return goc.stream().filter(item -> {
            boolean passTuKhoa = true;
            if (tuKhoa != null && !tuKhoa.isBlank()) {
                String tk = tuKhoa.trim().toLowerCase();
                passTuKhoa = (item.getMaLyDo() != null && item.getMaLyDo().toLowerCase().contains(tk))
                        || (item.getTenLyDo() != null && item.getTenLyDo().toLowerCase().contains(tk));
            }
            boolean passTrangThai = true;
            if (trangThai != null && !trangThai.isBlank()) {
                if ("active".equalsIgnoreCase(trangThai) || "1".equals(trangThai)) {
                    passTrangThai = item.isHoatDong();
                } else if ("inactive".equalsIgnoreCase(trangThai) || "0".equals(trangThai)) {
                    passTrangThai = !item.isHoatDong();
                }
            }
            return passTuKhoa && passTrangThai;
        }).collect(Collectors.toList());
    }

    private List<DoiThu> locDanhSachDoiThu(List<DoiThu> goc, String tuKhoa, String trangThai) {
        return goc.stream().filter(item -> {
            boolean passTuKhoa = true;
            if (tuKhoa != null && !tuKhoa.isBlank()) {
                String tk = tuKhoa.trim().toLowerCase();
                passTuKhoa = (item.getMaDoiThu() != null && item.getMaDoiThu().toLowerCase().contains(tk))
                        || (item.getTenDoiThu() != null && item.getTenDoiThu().toLowerCase().contains(tk))
                        || (item.getWebsite() != null && item.getWebsite().toLowerCase().contains(tk))
                        || (item.getGhiChu() != null && item.getGhiChu().toLowerCase().contains(tk));
            }
            boolean passTrangThai = true;
            if (trangThai != null && !trangThai.isBlank()) {
                if ("active".equalsIgnoreCase(trangThai) || "1".equals(trangThai)) {
                    passTrangThai = item.isHoatDong();
                } else if ("inactive".equalsIgnoreCase(trangThai) || "0".equals(trangThai)) {
                    passTrangThai = !item.isHoatDong();
                }
            }
            return passTuKhoa && passTrangThai;
        }).collect(Collectors.toList());
    }

    private void phanHoiThanhCong(HttpServletRequest request, HttpServletResponse response, String message, String tab)
            throws IOException {
        String isAjax = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(isAjax) || "1".equals(request.getParameter("ajax"))) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"thanhCong\":true,\"thongBao\":\"" + escapeJson(message) + "\",\"tab\":\"" + escapeJson(tab) + "\"}");
            out.flush();
        } else {
            String servletPath = request.getServletPath();
            if (servletPath == null || servletPath.isBlank()) {
                servletPath = "/danh-muc/ly-do-thang-thua";
            }
            response.sendRedirect(request.getContextPath() + servletPath + "?tab=" + tab + "&msg=success&info=" + java.net.URLEncoder.encode(message, "UTF-8"));
        }
    }

    private void phanHoiLoi(HttpServletRequest request, HttpServletResponse response, String errorMsg)
            throws IOException {
        phanHoiLoi(request, response, errorMsg, HttpServletResponse.SC_BAD_REQUEST);
    }

    private void phanHoiLoi(HttpServletRequest request, HttpServletResponse response, String errorMsg, int statusCode)
            throws IOException {
        String isAjax = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(isAjax) || "1".equals(request.getParameter("ajax"))) {
            response.setStatus(statusCode);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"thanhCong\":false,\"thongBao\":\"" + escapeJson(errorMsg) + "\"}");
            out.flush();
        } else {
            String servletPath = request.getServletPath();
            if (servletPath == null || servletPath.isBlank()) {
                servletPath = "/danh-muc/ly-do-thang-thua";
            }
            String tab = request.getParameter("tab");
            if (tab == null || tab.isBlank()) tab = "thang";
            response.sendRedirect(request.getContextPath() + servletPath + "?tab=" + tab + "&msg=error&info=" + java.net.URLEncoder.encode(errorMsg, "UTF-8"));
        }
    }

    private void traVeJson(HttpServletResponse response, List<LyDoThangThua> thang, List<LyDoThangThua> thua, List<DoiThu> doiThu)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"lyDoThang\":[");
        for (int i = 0; i < thang.size(); i++) {
            if (i > 0) sb.append(",");
            LyDoThangThua item = thang.get(i);
            sb.append("{\"id\":").append(item.getId())
              .append(",\"ma\":\"").append(escapeJson(item.getMaLyDo())).append("\"")
              .append(",\"ten\":\"").append(escapeJson(item.getTenLyDo())).append("\"")
              .append(",\"loai\":\"").append(item.getLoai()).append("\"")
              .append(",\"thuTu\":").append(item.getThuTuHienThi())
              .append(",\"hoatDong\":").append(item.isHoatDong())
              .append(",\"thamChieu\":").append(item.getSoCoHoiThamChieu())
              .append("}");
        }
        sb.append("],");
        sb.append("\"lyDoThua\":[");
        for (int i = 0; i < thua.size(); i++) {
            if (i > 0) sb.append(",");
            LyDoThangThua item = thua.get(i);
            sb.append("{\"id\":").append(item.getId())
              .append(",\"ma\":\"").append(escapeJson(item.getMaLyDo())).append("\"")
              .append(",\"ten\":\"").append(escapeJson(item.getTenLyDo())).append("\"")
              .append(",\"loai\":\"").append(item.getLoai()).append("\"")
              .append(",\"thuTu\":").append(item.getThuTuHienThi())
              .append(",\"hoatDong\":").append(item.isHoatDong())
              .append(",\"thamChieu\":").append(item.getSoCoHoiThamChieu())
              .append("}");
        }
        sb.append("],");
        sb.append("\"doiThu\":[");
        for (int i = 0; i < doiThu.size(); i++) {
            if (i > 0) sb.append(",");
            DoiThu item = doiThu.get(i);
            sb.append("{\"id\":").append(item.getId())
              .append(",\"ma\":\"").append(escapeJson(item.getMaDoiThu())).append("\"")
              .append(",\"ten\":\"").append(escapeJson(item.getTenDoiThu())).append("\"")
              .append(",\"website\":\"").append(escapeJson(item.getWebsite())).append("\"")
              .append(",\"ghiChu\":\"").append(escapeJson(item.getGhiChu())).append("\"")
              .append(",\"hoatDong\":").append(item.isHoatDong())
              .append(",\"thamChieu\":").append(item.getSoCoHoiThamChieu())
              .append("}");
        }
        sb.append("]}");
        out.print(sb.toString());
        out.flush();
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
