package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.KetQuaPhanTrangDTO;
import vn.nhom10.crm.dto.NguoiDungOptionDTO;
import vn.nhom10.crm.dto.NhatKyThayDoiDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.NhatKyThayDoiService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Servlet điều hướng và xử lý bộ lọc cho màn hình Nhật ký thay đổi trên dữ liệu nhạy cảm (Story S2-04).
 * URL Patterns: /nhat-ky-thay-doi, /nhat-ky-thay-doi/chi-tiet, /nguoi-dung/nhat-ky-thay-doi
 * Đảm bảo phân quyền phía server cho Quản trị hệ thống (System Admin / ADMIN).
 */
@WebServlet(name = "NhatKyThayDoiServlet", urlPatterns = {"/nhat-ky-thay-doi", "/nhat-ky-thay-doi/chi-tiet", "/nguoi-dung/nhat-ky-thay-doi"})
public class NhatKyThayDoiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private NhatKyThayDoiService nhatKyService;

    public NhatKyThayDoiServlet() {
        this(new NhatKyThayDoiService());
    }

    public NhatKyThayDoiServlet(NhatKyThayDoiService nhatKyService) {
        this.nhatKyService = nhatKyService != null ? nhatKyService : new NhatKyThayDoiService();
    }

    public void setNhatKyService(NhatKyThayDoiService nhatKyService) {
        this.nhatKyService = nhatKyService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 0. Phân quyền server-side: Quản trị hệ thống
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String servletPath = request.getServletPath();
        String format = request.getParameter("format");
        boolean isJsonRequest = "json".equalsIgnoreCase(format)
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        // 1. Xử lý trường hợp lấy chi tiết một bản ghi qua AJAX/JSON
        if ("/nhat-ky-thay-doi/chi-tiet".equals(servletPath) || (isJsonRequest && request.getParameter("id") != null)) {
            xuLyXemChiTietJson(request, response);
            return;
        }

        // 2. Tiếp nhận và phân tích các tham số lọc từ giao diện (AC 3)
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        String thongBaoLoi = null;

        // Lọc người dùng
        String nguoiDungParam = request.getParameter("nguoiDungId");
        if (nguoiDungParam != null && !nguoiDungParam.trim().isEmpty()) {
            try {
                boLoc.setNguoiDungId(Long.parseLong(nguoiDungParam.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        // Lọc loại đối tượng nhạy cảm
        String loaiParam = request.getParameter("loaiDoiTuong");
        if (loaiParam != null && !loaiParam.trim().isEmpty()) {
            boLoc.setLoaiDoiTuong(loaiParam.trim());
        }

        // Lọc theo khoảng thời gian
        String tuNgayParam = request.getParameter("tuNgay");
        if (tuNgayParam != null && !tuNgayParam.trim().isEmpty()) {
            try {
                boLoc.setTuNgay(LocalDate.parse(tuNgayParam.trim()));
            } catch (DateTimeParseException e) {
                thongBaoLoi = "Định dạng 'Từ ngày' không hợp lệ (chuẩn: YYYY-MM-DD).";
            }
        }

        String denNgayParam = request.getParameter("denNgay");
        if (denNgayParam != null && !denNgayParam.trim().isEmpty()) {
            try {
                boLoc.setDenNgay(LocalDate.parse(denNgayParam.trim()));
            } catch (DateTimeParseException e) {
                thongBaoLoi = "Định dạng 'Đến ngày' không hợp lệ (chuẩn: YYYY-MM-DD).";
            }
        }

        // Xử lý bộ lọc khoảng thời gian nhanh (Quick Presets)
        String quickPeriod = request.getParameter("quickPeriod");
        if (quickPeriod != null && !quickPeriod.trim().isEmpty()) {
            boLoc.setQuickPeriod(quickPeriod.trim());
            LocalDate homNay = LocalDate.now();
            switch (quickPeriod.trim().toLowerCase()) {
                case "today":
                    boLoc.setTuNgay(homNay);
                    boLoc.setDenNgay(homNay);
                    break;
                case "week":
                    boLoc.setTuNgay(homNay.minusDays(7));
                    boLoc.setDenNgay(homNay);
                    break;
                case "month":
                    boLoc.setTuNgay(homNay.withDayOfMonth(1));
                    boLoc.setDenNgay(homNay);
                    break;
                case "quarter":
                    // Quý 3 (1/7 - 30/9) - điểm chốt quý số liệu không khớp của Story S2-04
                    int currentMonth = homNay.getMonthValue();
                    int startMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    LocalDate startOfQuarter = LocalDate.of(homNay.getYear(), startMonth, 1);
                    LocalDate endOfQuarter = startOfQuarter.plusMonths(3).minusDays(1);
                    boLoc.setTuNgay(startOfQuarter);
                    boLoc.setDenNgay(endOfQuarter.isAfter(homNay) ? homNay : endOfQuarter);
                    break;
                case "all":
                    boLoc.setTuNgay(null);
                    boLoc.setDenNgay(null);
                    break;
                default:
                    break;
            }
        }

        // Validate tính hợp lệ của khoảng thời gian
        if (!boLoc.isKhoangThoiGianHopLe()) {
            thongBaoLoi = "Khoảng thời gian không hợp lệ: 'Từ ngày' (" + boLoc.getTuNgayChuoi()
                    + ") không được lớn hơn 'Đến ngày' (" + boLoc.getDenNgayChuoi() + ").";
        }

        // Từ khóa tìm kiếm nhanh
        String tuKhoaParam = request.getParameter("tuKhoa");
        if (tuKhoaParam != null && !tuKhoaParam.trim().isEmpty()) {
            boLoc.setTuKhoa(tuKhoaParam.trim());
        }

        // Phân trang
        String trangParam = request.getParameter("trang");
        if (trangParam != null && !trangParam.trim().isEmpty()) {
            try {
                boLoc.setTrang(Integer.parseInt(trangParam.trim()));
            } catch (NumberFormatException e) {
                boLoc.setTrang(1);
            }
        }

        String soBanGhiParam = request.getParameter("soBanGhi");
        if (soBanGhiParam != null && !soBanGhiParam.trim().isEmpty()) {
            try {
                boLoc.setSoBanGhiTrenTrang(Integer.parseInt(soBanGhiParam.trim()));
            } catch (NumberFormatException e) {
                boLoc.setSoBanGhiTrenTrang(10);
            }
        }

        // 3. Thực hiện truy vấn dữ liệu từ Service (kết nối DAO/Database)
        KetQuaPhanTrangDTO<NhatKyThayDoiDTO> phanTrang = nhatKyService.timKiemNhatKy(boLoc);
        ThongKeNhatKyDTO thongKe = nhatKyService.layThongKe(boLoc);
        List<NguoiDungOptionDTO> danhSachNguoiDung = nhatKyService.layDanhSachNguoiDung();

        // 4. Lấy thông báo từ Flash Session nếu có
        HttpSession session = request.getSession(false);
        if (session != null) {
            String flashSuccess = (String) session.getAttribute("flashSuccess");
            String flashError = (String) session.getAttribute("flashError");
            if (flashSuccess != null) {
                request.setAttribute("thongBaoThanhCong", flashSuccess);
                session.removeAttribute("flashSuccess");
            }
            if (flashError != null) {
                thongBaoLoi = flashError;
                session.removeAttribute("flashError");
            }
        }

        if (thongBaoLoi != null) {
            request.setAttribute("thongBaoLoi", thongBaoLoi);
        }

        // 5. Gắn dữ liệu vào Request Scope phục vụ JSP
        request.setAttribute("boLoc", boLoc);
        request.setAttribute("phanTrang", phanTrang);
        request.setAttribute("danhSachNhatKy", phanTrang.getDanhSach());
        request.setAttribute("thongKe", thongKe);
        request.setAttribute("danhSachNguoiDung", danhSachNguoiDung);
        request.setAttribute("danhSachLoaiDoiTuong", LoaiDoiTuongNhayCam.values());

        // 6. Điều hướng tới JSP view
        request.getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp")
                .forward(request, response);
    }

    /**
     * Xuất dữ liệu JSON chi tiết một bản ghi nhật ký phục vụ modal xem so sánh trực quan (Side-by-side diff).
     */
    private void xuLyXemChiTietJson(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Thiếu mã bản ghi nhật ký id\"}");
            return;
        }

        try {
            Long id = Long.parseLong(idParam.trim());
            if (id <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Mã bản ghi nhật ký không hợp lệ\"}");
                return;
            }
            NhatKyThayDoiDTO item = nhatKyService.layChiTiet(id);
            if (item == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Không tìm thấy bản ghi nhật ký với ID: " + id + "\"}");
                return;
            }

            // Tạo chuỗi JSON an toàn và redact dữ liệu nhạy cảm
            String giaTriTruocAnToan = vn.nhom10.crm.model.NhatKyThayDoi.cheGiaTriNhayCam(item.getTruongThayDoi(), item.getGiaTriTruoc());
            String giaTriSauAnToan = vn.nhom10.crm.model.NhatKyThayDoi.cheGiaTriNhayCam(item.getTruongThayDoi(), item.getGiaTriSau());

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"id\":").append(item.getId()).append(",");
            json.append("\"maTruyVet\":").append(escapeJson(item.getMaTruyVet())).append(",");
            json.append("\"nguoiThucHienId\":").append(item.getNguoiThucHienId()).append(",");
            json.append("\"tenNguoiThucHien\":").append(escapeJson(item.getTenNguoiThucHien())).append(",");
            json.append("\"emailNguoiThucHien\":").append(escapeJson(item.getEmailNguoiThucHien())).append(",");
            json.append("\"vaiTroNguoiThucHien\":").append(escapeJson(item.getVaiTroNguoiThucHien())).append(",");
            json.append("\"thoiDiem\":").append(escapeJson(item.getThoiDiemDinhDang())).append(",");
            json.append("\"loaiDoiTuongMa\":").append(escapeJson(item.getLoaiDoiTuong() != null ? item.getLoaiDoiTuong().getMa() : "")).append(",");
            json.append("\"loaiDoiTuongTen\":").append(escapeJson(item.getLoaiDoiTuong() != null ? item.getLoaiDoiTuong().getTenHienThi() : "")).append(",");
            json.append("\"loaiDoiTuongClass\":").append(escapeJson(item.getLoaiDoiTuong() != null ? item.getLoaiDoiTuong().getClassMauSac() : "")).append(",");
            json.append("\"maDoiTuong\":").append(escapeJson(item.getMaDoiTuong())).append(",");
            json.append("\"tenDoiTuong\":").append(escapeJson(item.getTenDoiTuong())).append(",");
            json.append("\"truongThayDoi\":").append(escapeJson(item.getTruongThayDoi())).append(",");
            json.append("\"giaTriTruoc\":").append(escapeJson(giaTriTruocAnToan)).append(",");
            json.append("\"giaTriSau\":").append(escapeJson(giaTriSauAnToan)).append(",");
            json.append("\"hanhDong\":").append(escapeJson(item.getHanhDong() != null ? item.getHanhDong().getTenHienThi() : "")).append(",");
            json.append("\"lyDoThayDoi\":").append(escapeJson(item.getLyDoThayDoi())).append(",");
            json.append("\"diaChiIp\":").append(escapeJson(item.getDiaChiIp())).append(",");
            json.append("\"thietBi\":").append(escapeJson(item.getThietBi()));
            json.append("}");

            out.write(json.toString());
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Định dạng ID bản ghi không hợp lệ\"}");
        }
    }

    /**
     * Kiểm tra phân quyền: Quản trị hệ thống (ADMIN) mới có quyền truy xuất nhật ký nhạy cảm.
     */
    private boolean kiemTraQuyenQuanTri(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return false;
        }

        Object userObj = session.getAttribute("nguoiDung");
        if (userObj instanceof NguoiDung) {
            NguoiDung nd = (NguoiDung) userObj;
            if (!nd.coVaiTro(VaiTroEnum.ADMIN)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Chỉ Quản trị hệ thống (Admin) mới có quyền xem nhật ký thay đổi dữ liệu nhạy cảm.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
                return false;
            }
        } else {
            String role = (String) session.getAttribute("vaiTro");
            if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.contains("QUAN_TRI"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("errorMessage", "Chỉ Quản trị hệ thống (Admin) mới có quyền xem nhật ký thay đổi dữ liệu nhạy cảm.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
                return false;
            }
        }

        return true;
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
