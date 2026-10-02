package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.model.KhuVucDiaLy;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.CoCauToChucService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller xử lý màn hình và API Khai báo cơ cấu tổ chức kinh doanh (Story S2-06).
 * Phân quyền chặt chẽ phía máy chủ: Chỉ Giám đốc kinh doanh (DIRECTOR) và Quản trị hệ thống (ADMIN) được thao tác.
 */
@WebServlet(name = "CoCauToChucServlet", urlPatterns = {"/co-cau-to-chuc", "/co-cau-to-chuc/*"})
public class CoCauToChucServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CoCauToChucServlet.class.getName());

    private CoCauToChucService coCauToChucService;
    private NguoiDungDAO nguoiDungDAO;

    @Override
    public void init() throws ServletException {
        this.coCauToChucService = new CoCauToChucService();
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public void setCoCauToChucService(CoCauToChucService service) {
        this.coCauToChucService = service;
    }

    public void setNguoiDungDAO(NguoiDungDAO dao) {
        this.nguoiDungDAO = dao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra xác thực (Authentication)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung currentUser = (NguoiDung) session.getAttribute("nguoiDung");

        // 2. Kiểm tra phân quyền (Server-side Authorization): Chỉ Director và Admin được quyền xem và cấu hình
        if (!currentUser.coVaiTro(VaiTroEnum.DIRECTOR) && !currentUser.coVaiTro(VaiTroEnum.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Chỉ Giám đốc kinh doanh (Director) và Quản trị hệ thống (Admin) mới có quyền truy cập cơ cấu tổ chức.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        String pathInfo = request.getPathInfo();

        // 3. Xử lý API JSON
        if ("/api/nhom".equals(pathInfo)) {
            xuLyApiChiTietNhom(request, response);
            return;
        } else if ("/api/cay-nhom".equals(pathInfo)) {
            xuLyApiCayNhom(request, response);
            return;
        } else if ("/api/khu-vuc".equals(pathInfo)) {
            xuLyApiKhuVuc(request, response);
            return;
        }

        // 4. Màn hình chính
        List<NhomKinhDoanh> cayNhom = coCauToChucService.layCayNhomKinhDoanh();
        List<NhomKinhDoanh> dsNhom = coCauToChucService.layTatCaNhom();
        List<KhuVucDiaLy> dsKhuVuc = coCauToChucService.layTatCaKhuVuc();
        List<NguoiDung> dsNhanVienChuaCoNhom = coCauToChucService.layDsNhanVienChuaCoNhom();
        List<NguoiDung> dsTatCaNguoiDung = nguoiDungDAO.layTatCa();

        request.setAttribute("cayNhom", cayNhom);
        request.setAttribute("dsNhom", dsNhom);
        request.setAttribute("dsKhuVuc", dsKhuVuc);
        request.setAttribute("dsNhanVienChuaCoNhom", dsNhanVienChuaCoNhom);
        request.setAttribute("dsTatCaNguoiDung", dsTatCaNguoiDung);

        request.getRequestDispatcher("/WEB-INF/views/co-cau-to-chuc/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung currentUser = (NguoiDung) session.getAttribute("nguoiDung");

        // Kiểm tra phân quyền thao tác
        if (!currentUser.coVaiTro(VaiTroEnum.DIRECTOR) && !currentUser.coVaiTro(VaiTroEnum.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Từ chối thao tác: Chỉ Giám đốc kinh doanh hoặc Quản trị hệ thống mới có quyền chỉnh sửa cơ cấu tổ chức.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "them-nhom":
                    xuLyThemNhom(request, session);
                    break;
                case "cap-nhat-nhom":
                    xuLyCapNhatNhom(request, session);
                    break;
                case "gan-truong-nhom":
                    xuLyGanTruongNhom(request, session);
                    break;
                case "gan-khu-vuc":
                    xuLyGanKhuVuc(request, session);
                    break;
                case "chuyen-nhom-nhan-vien":
                    xuLyChuyenNhomNhanVien(request, session);
                    break;
                case "them-khu-vuc":
                    xuLyThemKhuVuc(request, session);
                    break;
                case "cap-nhat-khu-vuc":
                    xuLyCapNhatKhuVuc(request, session);
                    break;
                default:
                    session.setAttribute("flashError", "Hành động không hợp lệ: " + action);
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý cơ cấu tổ chức: " + e.getMessage(), e);
            session.setAttribute("flashError", "Lỗi xử lý: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/co-cau-to-chuc");
    }

    private void xuLyThemNhom(HttpServletRequest request, HttpSession session) {
        String maNhom = request.getParameter("maNhom");
        String tenNhom = request.getParameter("tenNhom");
        String moTa = request.getParameter("moTa");
        Long nhomChaId = parseLongOrNull(request.getParameter("nhomChaId"));
        Long khuVucId = parseLongOrNull(request.getParameter("khuVucId"));
        Long truongNhomId = parseLongOrNull(request.getParameter("truongNhomId"));

        coCauToChucService.themNhomKinhDoanh(maNhom, tenNhom, moTa, nhomChaId, khuVucId, truongNhomId);
        session.setAttribute("flashSuccess", "Thêm nhóm kinh doanh '" + tenNhom + "' thành công.");
    }

    private void xuLyCapNhatNhom(HttpServletRequest request, HttpSession session) {
        long id = Long.parseLong(request.getParameter("id"));
        String tenNhom = request.getParameter("tenNhom");
        String moTa = request.getParameter("moTa");
        Long nhomChaId = parseLongOrNull(request.getParameter("nhomChaId"));
        Long khuVucId = parseLongOrNull(request.getParameter("khuVucId"));
        Long truongNhomId = parseLongOrNull(request.getParameter("truongNhomId"));
        boolean hoatDong = "1".equals(request.getParameter("hoatDong")) || "true".equalsIgnoreCase(request.getParameter("hoatDong"));

        coCauToChucService.capNhatNhomKinhDoanh(id, tenNhom, moTa, nhomChaId, khuVucId, truongNhomId, hoatDong);
        session.setAttribute("flashSuccess", "Cập nhật nhóm kinh doanh '" + tenNhom + "' thành công.");
    }

    private void xuLyGanTruongNhom(HttpServletRequest request, HttpSession session) {
        long nhomId = Long.parseLong(request.getParameter("nhomId"));
        Long truongNhomId = parseLongOrNull(request.getParameter("truongNhomId"));

        coCauToChucService.ganTruongNhom(nhomId, truongNhomId);
        session.setAttribute("flashSuccess", "Gán trưởng nhóm thành công.");
    }

    private void xuLyGanKhuVuc(HttpServletRequest request, HttpSession session) {
        long nhomId = Long.parseLong(request.getParameter("nhomId"));
        Long khuVucId = parseLongOrNull(request.getParameter("khuVucId"));

        coCauToChucService.ganKhuVucChoNhom(nhomId, khuVucId);
        session.setAttribute("flashSuccess", "Gán khu vực địa lý cho nhóm thành công.");
    }

    private void xuLyChuyenNhomNhanVien(HttpServletRequest request, HttpSession session) {
        long nguoiDungId = Long.parseLong(request.getParameter("nguoiDungId"));
        Long nhomId = parseLongOrNull(request.getParameter("nhomId"));

        coCauToChucService.chuyenNhomNhanVien(nguoiDungId, nhomId);
        session.setAttribute("flashSuccess", "Chuyển nhóm nhân viên thành công.");
    }

    private void xuLyThemKhuVuc(HttpServletRequest request, HttpSession session) {
        String maKhuVuc = request.getParameter("maKhuVuc");
        String tenKhuVuc = request.getParameter("tenKhuVuc");
        String loaiKhuVuc = request.getParameter("loaiKhuVuc");
        Long khuVucChaId = parseLongOrNull(request.getParameter("khuVucChaId"));
        int thuTu = parseIntOrDefault(request.getParameter("thuTuHienThi"), 0);

        coCauToChucService.themKhuVucDiaLy(maKhuVuc, tenKhuVuc, loaiKhuVuc, khuVucChaId, thuTu);
        session.setAttribute("flashSuccess", "Thêm khu vực địa lý '" + tenKhuVuc + "' thành công.");
    }

    private void xuLyCapNhatKhuVuc(HttpServletRequest request, HttpSession session) {
        long id = Long.parseLong(request.getParameter("id"));
        String tenKhuVuc = request.getParameter("tenKhuVuc");
        String loaiKhuVuc = request.getParameter("loaiKhuVuc");
        Long khuVucChaId = parseLongOrNull(request.getParameter("khuVucChaId"));
        int thuTu = parseIntOrDefault(request.getParameter("thuTuHienThi"), 0);
        boolean hoatDong = "1".equals(request.getParameter("hoatDong")) || "true".equalsIgnoreCase(request.getParameter("hoatDong"));

        coCauToChucService.capNhatKhuVucDiaLy(id, tenKhuVuc, loaiKhuVuc, khuVucChaId, thuTu, hoatDong);
        session.setAttribute("flashSuccess", "Cập nhật khu vực địa lý '" + tenKhuVuc + "' thành công.");
    }

    private void xuLyApiChiTietNhom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long id = parseLongOrNull(request.getParameter("id"));
        PrintWriter out = response.getWriter();

        if (id == null) {
            out.print("{\"error\":\"Thiếu id nhóm\"}");
            return;
        }

        NhomKinhDoanh nhom = coCauToChucService.timNhomTheoId(id);
        if (nhom == null) {
            out.print("{\"error\":\"Không tìm thấy nhóm\"}");
            return;
        }

        List<NguoiDung> dsThanhVien = coCauToChucService.layDsNhanVienThuocNhom(id);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(nhom.getIdLong()).append(",");
        sb.append("\"maNhom\":\"").append(escapeJson(nhom.getMaNhom())).append("\",");
        sb.append("\"tenNhom\":\"").append(escapeJson(nhom.getTenNhom())).append("\",");
        sb.append("\"moTa\":\"").append(escapeJson(nhom.getMoTa())).append("\",");
        sb.append("\"nhomChaId\":").append(nhom.getNhomChaIdLong()).append(",");
        sb.append("\"tenNhomCha\":\"").append(escapeJson(nhom.getTenNhomCha())).append("\",");
        sb.append("\"khuVucId\":").append(nhom.getKhuVucId()).append(",");
        sb.append("\"tenKhuVuc\":\"").append(escapeJson(nhom.getTenKhuVuc())).append("\",");
        sb.append("\"truongNhomId\":").append(nhom.getTruongNhomId()).append(",");
        sb.append("\"tenTruongNhom\":\"").append(escapeJson(nhom.getTenTruongNhom())).append("\",");
        sb.append("\"hoatDong\":").append(nhom.isHoatDong()).append(",");
        sb.append("\"soLuongThanhVien\":").append(dsThanhVien.size()).append(",");
        sb.append("\"thanhVien\":[");
        for (int i = 0; i < dsThanhVien.size(); i++) {
            NguoiDung tv = dsThanhVien.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":").append(tv.getId()).append(",");
            sb.append("\"hoTen\":\"").append(escapeJson(tv.getHoTen())).append("\",");
            sb.append("\"email\":\"").append(escapeJson(tv.getEmail())).append("\",");
            sb.append("\"soDienThoai\":\"").append(escapeJson(tv.getSoDienThoai())).append("\"");
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");

        out.print(sb.toString());
    }

    private void xuLyApiCayNhom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<NhomKinhDoanh> cayNhom = coCauToChucService.layCayNhomKinhDoanh();
        PrintWriter out = response.getWriter();
        out.print(serializeNhomListJson(cayNhom));
    }

    private void xuLyApiKhuVuc(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<KhuVucDiaLy> dsKhuVuc = coCauToChucService.layTatCaKhuVuc();
        PrintWriter out = response.getWriter();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < dsKhuVuc.size(); i++) {
            KhuVucDiaLy kv = dsKhuVuc.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":").append(kv.getId()).append(",");
            sb.append("\"maKhuVuc\":\"").append(escapeJson(kv.getMaKhuVuc())).append("\",");
            sb.append("\"tenKhuVuc\":\"").append(escapeJson(kv.getTenKhuVuc())).append("\",");
            sb.append("\"loaiKhuVuc\":\"").append(escapeJson(kv.getLoaiKhuVuc())).append("\",");
            sb.append("\"khuVucChaId\":").append(kv.getKhuVucChaId()).append(",");
            sb.append("\"thuTuHienThi\":").append(kv.getThuTuHienThi()).append(",");
            sb.append("\"hoatDong\":").append(kv.isHoatDong());
            sb.append("}");
        }
        sb.append("]");
        out.print(sb.toString());
    }

    private String serializeNhomListJson(List<NhomKinhDoanh> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            NhomKinhDoanh nkd = list.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":").append(nkd.getIdLong()).append(",");
            sb.append("\"maNhom\":\"").append(escapeJson(nkd.getMaNhom())).append("\",");
            sb.append("\"tenNhom\":\"").append(escapeJson(nkd.getTenNhom())).append("\",");
            sb.append("\"capDo\":").append(nkd.getCapDo()).append(",");
            sb.append("\"truongNhomId\":").append(nkd.getTruongNhomId()).append(",");
            sb.append("\"tenTruongNhom\":\"").append(escapeJson(nkd.getTenTruongNhom())).append("\",");
            sb.append("\"khuVucId\":").append(nkd.getKhuVucId()).append(",");
            sb.append("\"tenKhuVuc\":\"").append(escapeJson(nkd.getTenKhuVuc())).append("\",");
            sb.append("\"hoatDong\":").append(nkd.isHoatDong()).append(",");
            sb.append("\"soLuongThanhVien\":").append(nkd.getSoLuongThanhVien()).append(",");
            sb.append("\"dsNhomCon\":").append(serializeNhomListJson(nkd.getDsNhomCon()));
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private Long parseLongOrNull(String val) {
        if (val == null || val.trim().isEmpty() || "0".equals(val.trim())) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntOrDefault(String val, int def) {
        if (val == null || val.trim().isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return def;
        }
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
