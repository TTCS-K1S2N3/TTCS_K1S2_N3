package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.DieuKienRoiGiaiDoanDTO;
import vn.nhom10.crm.dto.DuBaoDoanhSoDTO;
import vn.nhom10.crm.dto.KetQuaGiaiDoanDTO;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.LoaiGiaiDoanEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.GiaiDoanPipelineService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller phục vụ cấu hình các giai đoạn Pipeline bán hàng và xác suất thắng (Story S2-09).
 * URL: /pipeline, /pipeline/giai-doan, /co-hoi/pipeline
 */
@WebServlet(name = "GiaiDoanPipelineServlet", urlPatterns = {
        "/pipeline",
        "/pipeline/giai-doan",
        "/pipeline/giai-doan/tao",
        "/pipeline/giai-doan/sua",
        "/pipeline/giai-doan/xoa",
        "/pipeline/giai-doan/doi-thu-tu",
        "/pipeline/giai-doan/trang-thai",
        "/pipeline/giai-doan/kiem-tra-dieu-kien",
        "/pipeline/giai-doan/tinh-du-bao",
        "/co-hoi/pipeline"
})
public class GiaiDoanPipelineServlet extends HttpServlet {

    public static final String SESSION_USER = "nguoiDung";

    private GiaiDoanPipelineService giaiDoanPipelineService;

    public GiaiDoanPipelineServlet() {
        this.giaiDoanPipelineService = new GiaiDoanPipelineService();
    }

    public GiaiDoanPipelineServlet(GiaiDoanPipelineService giaiDoanPipelineService) {
        this.giaiDoanPipelineService = giaiDoanPipelineService;
    }

    public void setGiaiDoanPipelineService(GiaiDoanPipelineService service) {
        this.giaiDoanPipelineService = service;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String servletPath = request.getServletPath();

        if ("/pipeline/giai-doan/kiem-tra-dieu-kien".equals(servletPath)) {
            xuLyKiemTraDieuKienAjax(request, response);
            return;
        }

        if ("/pipeline/giai-doan/tinh-du-bao".equals(servletPath)) {
            xuLyTinhDuBaoAjax(request, response);
            return;
        }

        NguoiDung user = layNguoiDung(request);
        boolean coQuyen = giaiDoanPipelineService.coQuyenCauHinh(user);

        if ("/pipeline/giai-doan/tao".equals(servletPath)) {
            if (!coQuyen) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thêm mới giai đoạn.");
                return;
            }
            GiaiDoanPipeline gd = new GiaiDoanPipeline();
            List<GiaiDoanPipeline> list = giaiDoanPipelineService.layTatCaGiaiDoan();
            gd.setThuTu(list.size() + 1);
            gd.setXacSuatThang(50);
            request.setAttribute("giaiDoan", gd);
            napThuocTinhForm(request);
            request.getRequestDispatcher("/WEB-INF/views/co-hoi/tao-giai-doan.jsp").forward(request, response);
            return;
        }

        if ("/pipeline/giai-doan/sua".equals(servletPath)) {
            if (!coQuyen) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền chỉnh sửa cấu hình giai đoạn.");
                return;
            }
            String paramId = request.getParameter("id");
            int id = parseIntSafe(paramId, -1);
            GiaiDoanPipeline gd = giaiDoanPipelineService.timTheoId(id);
            if (gd == null) {
                response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=not_found");
                return;
            }
            request.setAttribute("giaiDoan", gd);
            napThuocTinhForm(request);
            request.getRequestDispatcher("/WEB-INF/views/co-hoi/sua-giai-doan.jsp").forward(request, response);
            return;
        }

        // Mặc định: Hiển thị bảng cấu hình pipeline, stepper chuỗi giai đoạn và công cụ dự báo doanh số
        List<GiaiDoanPipeline> danhSach = giaiDoanPipelineService.layTatCaGiaiDoan();
        List<DuBaoDoanhSoDTO> duBao = giaiDoanPipelineService.layThongKeDuBaoPipeline();

        request.setAttribute("danhSachGiaiDoan", danhSach);
        request.setAttribute("duBaoPipeline", duBao);
        request.setAttribute("coQuyenCauHinh", coQuyen);

        String successMsg = request.getParameter("thanhCong");
        if (successMsg != null && !successMsg.isBlank()) {
            request.setAttribute("thongBaoThanhCong", successMsg);
        }
        String errorMsg = request.getParameter("error");
        if (errorMsg != null && !errorMsg.isBlank()) {
            request.setAttribute("thongBaoLoi", errorMsg);
        }

        request.getRequestDispatcher("/WEB-INF/views/co-hoi/cau-hinh-pipeline.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();
        NguoiDung user = layNguoiDung(request);

        if ("/pipeline/giai-doan/kiem-tra-dieu-kien".equals(servletPath)) {
            xuLyKiemTraDieuKienAjax(request, response);
            return;
        }

        if (!giaiDoanPipelineService.coQuyenCauHinh(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
            return;
        }

        if ("/pipeline/giai-doan/tao".equals(servletPath)) {
            xuLyThemGiaiDoan(request, response, user);
        } else if ("/pipeline/giai-doan/sua".equals(servletPath)) {
            xuLyCapNhatGiaiDoan(request, response, user);
        } else if ("/pipeline/giai-doan/doi-thu-tu".equals(servletPath)) {
            xuLyDoiThuTu(request, response, user);
        } else if ("/pipeline/giai-doan/trang-thai".equals(servletPath)) {
            xuLyTrangThai(request, response, user);
        } else if ("/pipeline/giai-doan/xoa".equals(servletPath)) {
            xuLyXoaGiaiDoan(request, response, user);
        } else {
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan");
        }
    }

    private void xuLyThemGiaiDoan(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws ServletException, IOException {
        GiaiDoanPipeline gd = trichXuatDuLieuForm(request);
        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.themGiaiDoan(gd, user);

        if (ketQua.isThanhCong()) {
            String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
            String roleParam = request.getParameter("role") != null ? "&role=" + request.getParameter("role") : "";
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?thanhCong=" + encoded + roleParam);
        } else {
            request.setAttribute("giaiDoan", gd);
            request.setAttribute("danhSachLoi", ketQua.getDanhSachLoi());
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            napThuocTinhForm(request);
            request.getRequestDispatcher("/WEB-INF/views/co-hoi/tao-giai-doan.jsp").forward(request, response);
        }
    }

    private void xuLyCapNhatGiaiDoan(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws ServletException, IOException {
        int id = parseIntSafe(request.getParameter("id"), -1);
        GiaiDoanPipeline gd = trichXuatDuLieuForm(request);
        gd.setId(id);

        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.capNhatGiaiDoan(gd, user);

        if (ketQua.isThanhCong()) {
            String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
            String roleParam = request.getParameter("role") != null ? "&role=" + request.getParameter("role") : "";
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?thanhCong=" + encoded + roleParam);
        } else {
            request.setAttribute("giaiDoan", gd);
            request.setAttribute("danhSachLoi", ketQua.getDanhSachLoi());
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            napThuocTinhForm(request);
            request.getRequestDispatcher("/WEB-INF/views/co-hoi/sua-giai-doan.jsp").forward(request, response);
        }
    }

    private void napThuocTinhForm(HttpServletRequest request) {
        request.setAttribute("danhSachLoai", LoaiGiaiDoanEnum.values());
        request.setAttribute("danhSachTrangThai", TrangThaiGiaiDoanEnum.values());
    }

    private void xuLyDoiThuTu(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        int id1 = parseIntSafe(request.getParameter("id1"), -1);
        int id2 = parseIntSafe(request.getParameter("id2"), -1);

        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.hoanDoiThuTu(id1, id2, user);
        String msg = ketQua.isThanhCong() ? "thanhCong=" : "error=";
        String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
        String roleParam = request.getParameter("role") != null ? "&role=" + request.getParameter("role") : "";
        response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?" + msg + encoded + roleParam);
    }

    private void xuLyTrangThai(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        int id = parseIntSafe(request.getParameter("id"), -1);
        String trangThaiStr = request.getParameter("trangThai");
        TrangThaiGiaiDoanEnum moi = TrangThaiGiaiDoanEnum.tuMa(trangThaiStr);

        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.chuyenTrangThai(id, moi, user);
        String msg = ketQua.isThanhCong() ? "thanhCong=" : "error=";
        String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
        String roleParam = request.getParameter("role") != null ? "&role=" + request.getParameter("role") : "";
        response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?" + msg + encoded + roleParam);
    }

    private void xuLyXoaGiaiDoan(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        int id = parseIntSafe(request.getParameter("id"), -1);
        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.xoaGiaiDoan(id, user);

        String msg = ketQua.isThanhCong() ? "thanhCong=" : "error=";
        String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
        String roleParam = request.getParameter("role") != null ? "&role=" + request.getParameter("role") : "";
        response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?" + msg + encoded + roleParam);
    }

    private void xuLyKiemTraDieuKienAjax(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        int id = parseIntSafe(request.getParameter("giaiDoanId"), -1);
        int soGap = parseIntSafe(request.getParameter("soCuocGap"), 0);
        int soGoi = parseIntSafe(request.getParameter("soCuocGoi"), 0);
        boolean baoGia = "true".equalsIgnoreCase(request.getParameter("daBaoGia")) || "1".equals(request.getParameter("daBaoGia"));
        boolean khaoSat = "true".equalsIgnoreCase(request.getParameter("daKhaoSat")) || "1".equals(request.getParameter("daKhaoSat"));

        DieuKienRoiGiaiDoanDTO kq = giaiDoanPipelineService.kiemTraDieuKienRoiGiaiDoan(id, soGap, soGoi, baoGia, khaoSat);

        StringBuilder json = new StringBuilder("{");
        json.append("\"thoaDieuKien\":").append(kq.isThoaDieuKien()).append(",");
        json.append("\"giaiDoanId\":").append(kq.getGiaiDoanId()).append(",");
        json.append("\"tenGiaiDoan\":\"").append(escapeJson(kq.getTenGiaiDoan())).append("\",");
        json.append("\"thongBao\":\"").append(escapeJson(kq.getThongBaoChiTiet())).append("\",");
        json.append("\"danhSachYeuCauThieu\":[");
        for (int i = 0; i < kq.getDanhSachYeuCauThieu().size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(escapeJson(kq.getDanhSachYeuCauThieu().get(i))).append("\"");
        }
        json.append("]}");

        response.getWriter().write(json.toString());
    }

    private void xuLyTinhDuBaoAjax(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        java.math.BigDecimal giaTri = java.math.BigDecimal.ZERO;
        try {
            String giaTriStr = request.getParameter("giaTri");
            if (giaTriStr != null && !giaTriStr.isBlank()) {
                giaTri = new java.math.BigDecimal(giaTriStr.trim());
            }
        } catch (Exception ignored) {
        }
        int xacSuat = parseIntSafe(request.getParameter("xacSuat"), 0);
        java.math.BigDecimal kq = giaiDoanPipelineService.tinhDuBaoDoanhSo(giaTri, xacSuat);
        response.getWriter().write("{\"thanhCong\":true,\"doanhSoDuBao\":" + kq.toPlainString() + "}");
    }

    private GiaiDoanPipeline trichXuatDuLieuForm(HttpServletRequest request) {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan(request.getParameter("maGiaiDoan"));
        gd.setTenGiaiDoan(request.getParameter("tenGiaiDoan"));
        gd.setThuTu(parseIntSafe(request.getParameter("thuTu"), 1));
        gd.setXacSuatThang(parseIntSafe(request.getParameter("xacSuatThang"), 0));
        gd.setSoNgayCanhBaoDinhTre(parseIntSafe(request.getParameter("soNgayCanhBaoDinhTre"), 7));
        gd.setLoaiGiaiDoan(LoaiGiaiDoanEnum.tuMa(request.getParameter("loaiGiaiDoan")));
        gd.setTrangThai(TrangThaiGiaiDoanEnum.tuMa(request.getParameter("trangThai")));

        // AC 3: Điều kiện rời bước
        gd.setSoCuocGapToiThieu(parseIntSafe(request.getParameter("soCuocGapToiThieu"), 0));
        gd.setSoCuocGoiToiThieu(parseIntSafe(request.getParameter("soCuocGoiToiThieu"), 0));
        gd.setYeuCauBaoGia("true".equalsIgnoreCase(request.getParameter("yeuCauBaoGia")) || "on".equalsIgnoreCase(request.getParameter("yeuCauBaoGia")));
        gd.setYeuCauKhaoSatNhuCau("true".equalsIgnoreCase(request.getParameter("yeuCauKhaoSatNhuCau")) || "on".equalsIgnoreCase(request.getParameter("yeuCauKhaoSatNhuCau")));
        gd.setDieuKienBatBuoc(request.getParameter("dieuKienBatBuoc"));

        return gd;
    }

    private NguoiDung layNguoiDung(HttpServletRequest request) {
        String roleParam = request.getParameter("role");
        if (roleParam != null && !roleParam.isBlank()) {
            NguoiDung fakeUser = new NguoiDung();
            fakeUser.setHoTen("Người Dùng Thử Nghiệm");
            VaiTro vt = new VaiTro();
            vt.setMaVaiTro(roleParam.trim().toUpperCase());
            fakeUser.setDanhSachVaiTro(Collections.singleton(vt));
            return fakeUser;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoiDung") != null) {
            return (NguoiDung) session.getAttribute("nguoiDung");
        }

        // Mặc định Giám đốc kinh doanh khi truy cập dev trực tiếp
        NguoiDung defaultDirector = new NguoiDung();
        defaultDirector.setHoTen("Giám đốc kinh doanh");
        VaiTro vt = new VaiTro();
        vt.setMaVaiTro(VaiTroEnum.DIRECTOR.name());
        defaultDirector.setDanhSachVaiTro(Collections.singleton(vt));
        return defaultDirector;
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
