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
import vn.nhom10.crm.service.GiaiDoanPipelineService;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public static final String SESSION_USER = PhienService.SESSION_USER_KEY;

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

        NguoiDung user = layNguoiDung(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        String servletPath = request.getServletPath();

        if ("/pipeline/giai-doan/kiem-tra-dieu-kien".equals(servletPath)) {
            xuLyKiemTraDieuKienAjax(request, response);
            return;
        }

        if ("/pipeline/giai-doan/tinh-du-bao".equals(servletPath)) {
            xuLyTinhDuBaoAjax(request, response);
            return;
        }

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
            gd.setSoNgayCanhBaoDinhTre(7);
            gd.setLoaiGiaiDoan(LoaiGiaiDoanEnum.DANG_TIEN_HANH);
            gd.setTrangThai(TrangThaiGiaiDoanEnum.DANG_AP_DUNG);
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
            Integer id = parseIntegerOrNull(request.getParameter("id"));
            if (id == null || id <= 0) {
                response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=not_found");
                return;
            }
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

        NguoiDung user = layNguoiDung(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        String servletPath = request.getServletPath();

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
        KetQuaGiaiDoanDTO ketQuaForm = new KetQuaGiaiDoanDTO();
        GiaiDoanPipeline gd = trichXuatDuLieuForm(request, ketQuaForm);

        KetQuaGiaiDoanDTO ketQua;
        if (!ketQuaForm.getDanhSachLoi().isEmpty()) {
            ketQua = ketQuaForm;
            ketQua.setThongBao("Dữ liệu giai đoạn không hợp lệ. Vui lòng kiểm tra lại các trường.");
        } else {
            ketQua = giaiDoanPipelineService.themGiaiDoan(gd, user);
        }

        if (ketQua.isThanhCong()) {
            String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?thanhCong=" + encoded);
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
        Integer id = parseIntegerOrNull(request.getParameter("id"));
        if (id == null || id <= 0) {
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=" + URLEncoder.encode("ID giai đoạn không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }

        KetQuaGiaiDoanDTO ketQuaForm = new KetQuaGiaiDoanDTO();
        GiaiDoanPipeline gd = trichXuatDuLieuForm(request, ketQuaForm);
        gd.setId(id);

        KetQuaGiaiDoanDTO ketQua;
        if (!ketQuaForm.getDanhSachLoi().isEmpty()) {
            ketQua = ketQuaForm;
            ketQua.setThongBao("Dữ liệu giai đoạn không hợp lệ. Vui lòng kiểm tra lại các trường.");
        } else {
            ketQua = giaiDoanPipelineService.capNhatGiaiDoan(gd, user);
        }

        if (ketQua.isThanhCong()) {
            String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?thanhCong=" + encoded);
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
        Integer id1 = parseIntegerOrNull(request.getParameter("id1"));
        Integer id2 = parseIntegerOrNull(request.getParameter("id2"));
        if (id1 == null || id2 == null || id1 <= 0 || id2 <= 0) {
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=" + URLEncoder.encode("Tham số đổi thứ tự không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }

        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.hoanDoiThuTu(id1, id2, user);
        String msg = ketQua.isThanhCong() ? "thanhCong=" : "error=";
        String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?" + msg + encoded);
    }

    private void xuLyTrangThai(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        Integer id = parseIntegerOrNull(request.getParameter("id"));
        if (id == null || id <= 0) {
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=" + URLEncoder.encode("ID giai đoạn không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }
        String trangThaiStr = request.getParameter("trangThai");
        if (trangThaiStr == null || trangThaiStr.isBlank()) {
            trangThaiStr = request.getParameter("trangThaiMoi");
        }
        TrangThaiGiaiDoanEnum moi = TrangThaiGiaiDoanEnum.tuMaStrict(trangThaiStr);
        if (moi == null) {
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=" + URLEncoder.encode("Trạng thái giai đoạn không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }

        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.chuyenTrangThai(id, moi, user);
        String msg = ketQua.isThanhCong() ? "thanhCong=" : "error=";
        String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?" + msg + encoded);
    }

    private void xuLyXoaGiaiDoan(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException {
        Integer id = parseIntegerOrNull(request.getParameter("id"));
        if (id == null || id <= 0) {
            response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?error=" + URLEncoder.encode("ID giai đoạn không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }
        KetQuaGiaiDoanDTO ketQua = giaiDoanPipelineService.xoaGiaiDoan(id, user);

        String msg = ketQua.isThanhCong() ? "thanhCong=" : "error=";
        String encoded = URLEncoder.encode(ketQua.getThongBao(), StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/pipeline/giai-doan?" + msg + encoded);
    }

    private void xuLyKiemTraDieuKienAjax(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Integer id = parseIntegerOrNull(request.getParameter("giaiDoanId"));
        if (id == null || id <= 0) {
            response.getWriter().write("{\"thoaDieuKien\":false,\"giaiDoanId\":-1,\"tenGiaiDoan\":\"Không hợp lệ\",\"thongBao\":\"ID giai đoạn không hợp lệ.\",\"danhSachYeuCauThieu\":[\"ID giai đoạn không hợp lệ.\"]}");
            return;
        }
        int soGap = parseIntegerOrDefault(request.getParameter("soCuocGap"), 0);
        int soGoi = parseIntegerOrDefault(request.getParameter("soCuocGoi"), 0);
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
        BigDecimal giaTri = BigDecimal.ZERO;
        try {
            String giaTriStr = request.getParameter("giaTri");
            if (giaTriStr != null && !giaTriStr.isBlank()) {
                giaTri = new BigDecimal(giaTriStr.trim());
            }
        } catch (Exception ignored) {
            response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Giá trị cơ hội không hợp lệ.\"}");
            return;
        }

        Integer xacSuat = parseIntegerOrNull(request.getParameter("xacSuat"));
        if (xacSuat == null || xacSuat < 0 || xacSuat > 100) {
            response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Xác suất phải từ 0% đến 100%.\"}");
            return;
        }

        BigDecimal kq = giaiDoanPipelineService.tinhDuBaoDoanhSo(giaTri, xacSuat);
        response.getWriter().write("{\"thanhCong\":true,\"doanhSoDuBao\":" + kq.toPlainString() + "}");
    }

    private GiaiDoanPipeline trichXuatDuLieuForm(HttpServletRequest request, KetQuaGiaiDoanDTO ketQua) {
        GiaiDoanPipeline gd = new GiaiDoanPipeline();
        gd.setMaGiaiDoan(request.getParameter("maGiaiDoan"));
        gd.setTenGiaiDoan(request.getParameter("tenGiaiDoan"));

        String thuTuStr = request.getParameter("thuTu");
        if (thuTuStr == null || thuTuStr.isBlank()) {
            ketQua.themLoi("thuTu", "Thứ tự giai đoạn không được để trống.");
            gd.setThuTu(-1);
        } else {
            try {
                gd.setThuTu(Integer.parseInt(thuTuStr.trim()));
            } catch (NumberFormatException e) {
                ketQua.themLoi("thuTu", "Thứ tự giai đoạn phải là số nguyên.");
                gd.setThuTu(-1);
            }
        }

        String xacSuatStr = request.getParameter("xacSuatThang");
        if (xacSuatStr == null || xacSuatStr.isBlank()) {
            ketQua.themLoi("xacSuatThang", "Xác suất thắng không được để trống.");
            gd.setXacSuatThang(-1);
        } else {
            try {
                gd.setXacSuatThang(Integer.parseInt(xacSuatStr.trim()));
            } catch (NumberFormatException e) {
                ketQua.themLoi("xacSuatThang", "Xác suất thắng phải là số nguyên từ 0 đến 100.");
                gd.setXacSuatThang(-1);
            }
        }

        String dinhTreStr = request.getParameter("soNgayCanhBaoDinhTre");
        if (dinhTreStr != null && !dinhTreStr.isBlank()) {
            try {
                gd.setSoNgayCanhBaoDinhTre(Integer.parseInt(dinhTreStr.trim()));
            } catch (NumberFormatException e) {
                ketQua.themLoi("soNgayCanhBaoDinhTre", "Số ngày cảnh báo đình trệ phải là số nguyên.");
                gd.setSoNgayCanhBaoDinhTre(-1);
            }
        } else {
            gd.setSoNgayCanhBaoDinhTre(0);
        }

        String loaiStr = request.getParameter("loaiGiaiDoan");
        if (loaiStr != null && !loaiStr.isBlank()) {
            LoaiGiaiDoanEnum loai = LoaiGiaiDoanEnum.tuMaStrict(loaiStr);
            if (loai == null) {
                ketQua.themLoi("loaiGiaiDoan", "Phân loại giai đoạn không hợp lệ.");
            } else {
                gd.setLoaiGiaiDoan(loai);
            }
        } else {
            gd.setLoaiGiaiDoan(LoaiGiaiDoanEnum.DANG_TIEN_HANH);
        }

        String trangThaiStr = request.getParameter("trangThai");
        if (trangThaiStr != null && !trangThaiStr.isBlank()) {
            TrangThaiGiaiDoanEnum tt = TrangThaiGiaiDoanEnum.tuMaStrict(trangThaiStr);
            if (tt == null) {
                ketQua.themLoi("trangThai", "Trạng thái giai đoạn không hợp lệ.");
            } else {
                gd.setTrangThai(tt);
            }
        } else {
            gd.setTrangThai(TrangThaiGiaiDoanEnum.DANG_AP_DUNG);
        }

        String soGapStr = request.getParameter("soCuocGapToiThieu");
        if (soGapStr != null && !soGapStr.isBlank()) {
            try {
                gd.setSoCuocGapToiThieu(Integer.parseInt(soGapStr.trim()));
            } catch (NumberFormatException e) {
                ketQua.themLoi("soCuocGapToiThieu", "Số cuộc gặp tối thiểu phải là số nguyên.");
            }
        }

        String soGoiStr = request.getParameter("soCuocGoiToiThieu");
        if (soGoiStr != null && !soGoiStr.isBlank()) {
            try {
                gd.setSoCuocGoiToiThieu(Integer.parseInt(soGoiStr.trim()));
            } catch (NumberFormatException e) {
                ketQua.themLoi("soCuocGoiToiThieu", "Số cuộc gọi tối thiểu phải là số nguyên.");
            }
        }

        gd.setYeuCauBaoGia("true".equalsIgnoreCase(request.getParameter("yeuCauBaoGia")) || "on".equalsIgnoreCase(request.getParameter("yeuCauBaoGia")));
        gd.setYeuCauKhaoSatNhuCau("true".equalsIgnoreCase(request.getParameter("yeuCauKhaoSatNhuCau")) || "on".equalsIgnoreCase(request.getParameter("yeuCauKhaoSatNhuCau")));
        gd.setDieuKienBatBuoc(request.getParameter("dieuKienBatBuoc"));

        return gd;
    }

    private NguoiDung layNguoiDung(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute(PhienService.SESSION_USER_KEY);
            if (obj instanceof NguoiDung) {
                return (NguoiDung) obj;
            }
        }
        return null;
    }

    private Integer parseIntegerOrNull(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntegerOrDefault(String val, int defaultVal) {
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
