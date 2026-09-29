package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO;
import vn.nhom10.crm.dto.ThongTinBanGiaoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyBanGiao;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.KhoaTaiKhoanService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet tiếp nhận và xử lý yêu cầu khoá tài khoản và bàn giao dữ liệu khi nhân viên nghỉ việc (Story S1-10).
 * Đường dẫn: /nguoi-dung/khoa-tai-khoan
 */
@WebServlet(name = "KhoaTaiKhoanServlet", urlPatterns = {"/nguoi-dung/khoa-tai-khoan"})
public class KhoaTaiKhoanServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhoaTaiKhoanServlet.class.getName());

    private KhoaTaiKhoanService khoaTaiKhoanService;

    @Override
    public void init() throws ServletException {
        this.khoaTaiKhoanService = new KhoaTaiKhoanService();
    }

    public void setKhoaTaiKhoanService(KhoaTaiKhoanService service) {
        this.khoaTaiKhoanService = service;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null || !currentUser.coVaiTro(VaiTroEnum.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Chỉ Quản trị hệ thống (Admin) mới có quyền khoá tài khoản và bàn giao dữ liệu.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        String userIdParam = request.getParameter("id");
        if (userIdParam == null || userIdParam.isBlank()) {
            // Không có ID cụ thể, chuyển tiếp kèm danh sách lịch sử bàn giao
            List<NhatKyBanGiao> dsLichSu = khoaTaiKhoanService.layLichSuBanGiao(20);
            request.setAttribute("dsLichSu", dsLichSu);
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/khoa-tai-khoan.jsp").forward(request, response);
            return;
        }

        try {
            int userId = Integer.parseInt(userIdParam.trim());
            ThongTinBanGiaoDTO thongTin = khoaTaiKhoanService.layThongTinBanGiao(userId);

            if ("json".equalsIgnoreCase(request.getParameter("format")) ||
                "application/json".equalsIgnoreCase(request.getHeader("Accept"))) {
                phanHoiJsonThongTinBanGiao(response, thongTin);
                return;
            }

            List<NhatKyBanGiao> dsLichSu = khoaTaiKhoanService.layLichSuBanGiao(10);
            request.setAttribute("thongTinBanGiao", thongTin);
            request.setAttribute("dsLichSu", dsLichSu);
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/khoa-tai-khoan.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID người dùng không hợp lệ.");
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi nạp thông tin bàn giao: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi máy chủ khi nạp thông tin bàn giao.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null || !currentUser.coVaiTro(VaiTroEnum.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (laYeuCauAjax(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Bạn không có quyền quản trị để thực hiện thao tác này.\"}");
            } else {
                request.setAttribute("errorMessage", "Chỉ Quản trị hệ thống mới có quyền khoá tài khoản và bàn giao dữ liệu.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return;
        }

        String nguoiBiKhoaParam = request.getParameter("nguoiBiKhoaId");
        String nguoiTiepNhanParam = request.getParameter("nguoiTiepNhanId");
        String lyDo = request.getParameter("lyDo");

        int nguoiBiKhoaId = 0;
        Integer nguoiTiepNhanId = null;

        try {
            if (nguoiBiKhoaParam != null && !nguoiBiKhoaParam.isBlank()) {
                nguoiBiKhoaId = Integer.parseInt(nguoiBiKhoaParam.trim());
            }
            if (nguoiTiepNhanParam != null && !nguoiTiepNhanParam.isBlank()) {
                nguoiTiepNhanId = Integer.parseInt(nguoiTiepNhanParam.trim());
            }
        } catch (NumberFormatException e) {
            KetQuaKhoaVaBanGiaoDTO loiDto = new KetQuaKhoaVaBanGiaoDTO(false, "Mã người dùng hoặc người tiếp nhận không hợp lệ.");
            phanHoiKetQua(request, response, loiDto, nguoiBiKhoaId);
            return;
        }

        int nguoiThucHienId = currentUser.getId();

        KetQuaKhoaVaBanGiaoDTO ketQua = khoaTaiKhoanService.khoaVaBanGiao(
                nguoiBiKhoaId, nguoiTiepNhanId, nguoiThucHienId, lyDo
        );

        phanHoiKetQua(request, response, ketQua, nguoiBiKhoaId);
    }

    private void phanHoiKetQua(HttpServletRequest request, HttpServletResponse response,
                               KetQuaKhoaVaBanGiaoDTO ketQua, int nguoiBiKhoaId)
            throws ServletException, IOException {

        if (laYeuCauAjax(request)) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(String.format(
                    "{\"thanhCong\":%b,\"thongBao\":\"%s\",\"soKhachHangChuyen\":%d,\"soCoHoiChuyen\":%d,\"nhatKyId\":%d}",
                    ketQua.isThanhCong(),
                    escapeJson(ketQua.getThongBao()),
                    ketQua.getSoKhachHangChuyen(),
                    ketQua.getSoCoHoiChuyen(),
                    ketQua.getNhatKyId()
            ));
            out.flush();
            return;
        }

        request.setAttribute("ketQua", ketQua);
        if (nguoiBiKhoaId > 0) {
            try {
                ThongTinBanGiaoDTO thongTin = khoaTaiKhoanService.layThongTinBanGiao(nguoiBiKhoaId);
                request.setAttribute("thongTinBanGiao", thongTin);
            } catch (Exception ignored) {
            }
        }
        List<NhatKyBanGiao> dsLichSu = khoaTaiKhoanService.layLichSuBanGiao(10);
        request.setAttribute("dsLichSu", dsLichSu);
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/khoa-tai-khoan.jsp").forward(request, response);
    }

    private void phanHoiJsonThongTinBanGiao(HttpServletResponse response, ThongTinBanGiaoDTO dto) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        NguoiDung u = dto.getNguoiBiKhoa();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"nguoiBiKhoa\":{");
        sb.append("\"id\":").append(u.getId()).append(",");
        sb.append("\"hoTen\":\"").append(escapeJson(u.getHoTen())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(u.getEmail())).append("\",");
        sb.append("\"vaiTro\":\"").append(escapeJson(u.getChuoiVaiTroHienThi())).append("\",");
        sb.append("\"nhomKinhDoanh\":\"").append(escapeJson(u.getTenNhomKinhDoanh())).append("\",");
        sb.append("\"trangThai\":\"").append(escapeJson(u.getTrangThai())).append("\"");
        sb.append("},");
        sb.append("\"soKhachHang\":").append(dto.getSoKhachHangHienTai()).append(",");
        sb.append("\"soCoHoi\":").append(dto.getSoCoHoiHienTai()).append(",");
        sb.append("\"danhSachNguoiNhan\":[");
        for (int i = 0; i < dto.getDanhSachNguoiTiepNhan().size(); i++) {
            NguoiDung r = dto.getDanhSachNguoiTiepNhan().get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":").append(r.getId()).append(",");
            sb.append("\"hoTen\":\"").append(escapeJson(r.getHoTen())).append("\",");
            sb.append("\"email\":\"").append(escapeJson(r.getEmail())).append("\",");
            sb.append("\"nhomKinhDoanh\":\"").append(escapeJson(r.getTenNhomKinhDoanh())).append("\"");
            sb.append("}");
        }
        sb.append("]}");

        out.print(sb.toString());
        out.flush();
    }

    private boolean laYeuCauAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        String ajaxParam = request.getParameter("ajax");
        return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith) ||
               (accept != null && accept.contains("application/json")) ||
               "true".equalsIgnoreCase(ajaxParam);
    }

    private NguoiDung layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            NguoiDung u = (NguoiDung) session.getAttribute("nguoiDung");
            if (u != null) return u;
            return (NguoiDung) session.getAttribute("currentUser");
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
