package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dto.MaNhungDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.ModuleHeThong;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.BieuMauLeadService;
import vn.nhom10.crm.service.MenuService;

import java.io.IOException;
import java.util.List;

/**
 * Servlet quản lý biểu mẫu lead dành cho Nhân viên Marketing / Quản trị viên (Story S4-01).
 * URL:
 * - /bieu-mau            : Danh sách các biểu mẫu đã tạo kèm trạng thái và số lượt gửi
 * - /bieu-mau/tao        : Giao diện và xử lý tạo biểu mẫu mới
 * - /bieu-mau/ma-nhung   : Lấy mã nhúng HTML/Iframe/Script để dán vào website bất kỳ (AC1)
 * - /bieu-mau/trang-thai : Bật/tắt trạng thái hoạt động của biểu mẫu
 */
@WebServlet(name = "BieuMauLeadServlet", urlPatterns = {"/bieu-mau", "/bieu-mau/tao", "/bieu-mau/ma-nhung", "/bieu-mau/trang-thai"})
public class BieuMauLeadServlet extends HttpServlet {

    private final BieuMauLeadService bieuMauLeadService;
    private final DanhMucBanHangDAO danhMucBanHangDAO;
    private final MenuService menuService;

    public BieuMauLeadServlet() {
        this(new BieuMauLeadService(), new DanhMucBanHangDAO(), MenuService.getInstance());
    }

    public BieuMauLeadServlet(BieuMauLeadService bieuMauLeadService, DanhMucBanHangDAO danhMucBanHangDAO, MenuService menuService) {
        this.bieuMauLeadService = bieuMauLeadService;
        this.danhMucBanHangDAO = danhMucBanHangDAO;
        this.menuService = menuService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!kiemTraQuyen(request, response)) {
            return;
        }

        String path = request.getServletPath();

        if ("/lead".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/bieu-mau");
            return;
        }

        if ("/bieu-mau/tao".equals(path)) {
            hienThiTrangTao(request, response);
            return;
        }

        if ("/bieu-mau/ma-nhung".equals(path)) {
            xuLyLayMaNhung(request, response);
            return;
        }

        // Mặc định: /bieu-mau - Danh sách biểu mẫu
        hienThiDanhSach(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!kiemTraQuyen(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/bieu-mau/tao".equals(path)) {
            xuLyTaoBieuMau(request, response);
            return;
        }

        if ("/bieu-mau/trang-thai".equals(path)) {
            xuLyDoiTrangThai(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/bieu-mau");
    }

    private void hienThiDanhSach(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<BieuMauLead> dsBieuMau = bieuMauLeadService.layDanhSachBieuMau(null);
        request.setAttribute("dsBieuMau", dsBieuMau);

        String contextPath = request.getContextPath();
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()) +
                contextPath;
        request.setAttribute("baseUrl", baseUrl);

        request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-danh-sach.jsp").forward(request, response);
    }

    private void hienThiTrangTao(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<MucDanhMucDTO> dsNguonLead = danhMucBanHangDAO.layDanhSach(LoaiDanhMuc.NGUON_LEAD, true);
        request.setAttribute("dsNguonLead", dsNguonLead);
        request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-form.jsp").forward(request, response);
    }

    private void xuLyTaoBieuMau(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null) ? (NguoiDung) session.getAttribute("nguoiDung") : null;
        Long nguoiTaoId = (nguoiDung != null) ? nguoiDung.getId() : null;

        String maBieuMau = request.getParameter("ma_bieu_mau");
        String tenBieuMau = request.getParameter("ten_bieu_mau");
        String nguonLeadIdStr = request.getParameter("nguon_lead_id");
        String chienDichIdStr = request.getParameter("chien_dich_id");
        String gioiHanStr = request.getParameter("gioi_han_gui_moi_gio");
        String batHoneypotStr = request.getParameter("bat_honeypot");

        try {
            if (maBieuMau == null || maBieuMau.trim().isEmpty()) {
                throw new IllegalArgumentException("Mã biểu mẫu không được để trống.");
            }
            if (tenBieuMau == null || tenBieuMau.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên biểu mẫu không được để trống.");
            }
            if (nguonLeadIdStr == null || nguonLeadIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn Nguồn Lead cho biểu mẫu.");
            }

            Long nguonLeadId = Long.parseLong(nguonLeadIdStr.trim());
            Long chienDichId = (chienDichIdStr != null && !chienDichIdStr.trim().isEmpty())
                    ? Long.parseLong(chienDichIdStr.trim()) : null;

            Integer gioiHan = (gioiHanStr != null && !gioiHanStr.trim().isEmpty())
                    ? Integer.parseInt(gioiHanStr.trim()) : 10;

            boolean batHoneypot = "1".equals(batHoneypotStr) || "true".equalsIgnoreCase(batHoneypotStr) || "on".equalsIgnoreCase(batHoneypotStr);

            BieuMauLead bm = new BieuMauLead();
            bm.setMaBieuMau(maBieuMau.trim());
            bm.setTenBieuMau(tenBieuMau.trim());
            bm.setNguonLeadId(nguonLeadId);
            bm.setChienDichId(chienDichId);
            bm.setGioiHanGuiMoiGio(gioiHan);
            bm.setBatHoneypot(batHoneypot);
            bm.setHoatDong(true);

            bieuMauLeadService.taoBieuMau(bm, nguoiTaoId);

            session.setAttribute("thongBaoThanhCong", "Tạo biểu mẫu thu thập lead '" + bm.getTenBieuMau() + "' thành công!");
            response.sendRedirect(request.getContextPath() + "/bieu-mau");
        } catch (IllegalArgumentException e) {
            request.setAttribute("thongBaoLoi", e.getMessage());
            request.setAttribute("ma_bieu_mau", maBieuMau);
            request.setAttribute("ten_bieu_mau", tenBieuMau);
            request.setAttribute("nguon_lead_id", nguonLeadIdStr);
            request.setAttribute("gioi_han_gui_moi_gio", gioiHanStr);
            hienThiTrangTao(request, response);
        } catch (Exception e) {
            request.setAttribute("thongBaoLoi", "Lỗi tạo biểu mẫu: " + e.getMessage());
            hienThiTrangTao(request, response);
        }
    }

    private void xuLyLayMaNhung(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Thiếu ID biểu mẫu\"}");
            return;
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            String contextPath = request.getContextPath();
            String baseUrl = request.getScheme() + "://" + request.getServerName() +
                    ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()) +
                    contextPath;

            MaNhungDTO maNhung = bieuMauLeadService.sinhMaNhung(id, baseUrl);
            response.setContentType("application/json;charset=UTF-8");

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"thanhCong\":true,");
            json.append("\"maBieuMau\":\"").append(escapeJson(maNhung.getMaBieuMau())).append("\",");
            json.append("\"tenBieuMau\":\"").append(escapeJson(maNhung.getTenBieuMau())).append("\",");
            json.append("\"tokenCongKhai\":\"").append(escapeJson(maNhung.getTokenCongKhai())).append("\",");
            json.append("\"maIframe\":\"").append(escapeJson(maNhung.getMaIframe())).append("\",");
            json.append("\"maScript\":\"").append(escapeJson(maNhung.getMaScript())).append("\",");
            json.append("\"duongDanTrucTiep\":\"").append(escapeJson(maNhung.getDuongDanTrucTiep())).append("\"");
            json.append("}");

            response.getWriter().write(json.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void xuLyDoiTrangThai(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        String hoatDongStr = request.getParameter("hoat_dong");
        if (idStr != null && hoatDongStr != null) {
            try {
                Long id = Long.parseLong(idStr.trim());
                boolean hoatDong = "1".equals(hoatDongStr) || "true".equalsIgnoreCase(hoatDongStr);
                BieuMauLead bm = bieuMauLeadService.layBieuMauTheoId(id);
                if (bm != null) {
                    bm.setHoatDong(hoatDong);
                    bieuMauLeadService.capNhatBieuMau(bm);
                }
            } catch (Exception ignored) {
            }
        }
        response.sendRedirect(request.getContextPath() + "/bieu-mau");
    }

    private boolean kiemTraQuyen(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null) ? (NguoiDung) session.getAttribute("nguoiDung") : null;

        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return false;
        }

        // Kiểm tra phân quyền truy cập module LEAD (Marketing, Sales, Admin, Director, Team Lead)
        if (!menuService.kiemTraQuyenTruyCap(nguoiDung, ModuleHeThong.LEAD)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Bạn không có quyền truy cập vào chức năng quản lý biểu mẫu lead.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return false;
        }
        return true;
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
