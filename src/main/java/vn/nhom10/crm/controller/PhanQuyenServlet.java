package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.GanVaiTroNhomDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.PhanQuyenService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý gán vai trò và nhóm kinh doanh cho người dùng (Story S1-09).
 * Chỉ Admin mới được truy cập — kiểm tra ở cả server-side (servlet) và NavigationFilter.
 */
@WebServlet(name = "PhanQuyenServlet", urlPatterns = {"/nguoi-dung/phan-quyen"})
public class PhanQuyenServlet extends HttpServlet {

    private PhanQuyenService phanQuyenService;

    @Override
    public void init() throws ServletException {
        phanQuyenService = PhanQuyenService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung nguoiDungHienTai = (NguoiDung) session.getAttribute("nguoiDung");

        // Kiểm tra phân quyền: Chỉ Admin mới có quyền truy cập
        if (!nguoiDungHienTai.coVaiTro(VaiTroEnum.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Chỉ Quản trị hệ thống (Admin) mới có quyền gán vai trò và gắn nhóm kinh doanh.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        napDuLieuTrang(request, null);
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen.jsp").forward(request, response);
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

        NguoiDung nguoiDungHienTai = (NguoiDung) session.getAttribute("nguoiDung");

        // Kiểm tra phân quyền Admin
        if (!nguoiDungHienTai.coVaiTro(VaiTroEnum.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Chỉ Quản trị hệ thống (Admin) mới có quyền thực hiện thao tác này.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return;
        }

        int nguoiThucHienId = (int) nguoiDungHienTai.getId();

        String idStr = request.getParameter("nguoiDungId");
        int nguoiDungId = 0;
        try {
            nguoiDungId = Integer.parseInt(idStr);
        } catch (NumberFormatException ignored) {
        }

        String[] vaiTroParams = request.getParameterValues("vaiTroIds");
        List<Integer> dsVaiTroId = new ArrayList<>();
        if (vaiTroParams != null) {
            for (String vtStr : vaiTroParams) {
                try {
                    dsVaiTroId.add(Integer.parseInt(vtStr));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String nhomStr = request.getParameter("nhomKinhDoanhId");
        Integer nhomKinhDoanhId = null;
        if (nhomStr != null && !nhomStr.isBlank()) {
            try {
                int val = Integer.parseInt(nhomStr);
                if (val > 0) {
                    nhomKinhDoanhId = val;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String diaChiIp = request.getRemoteAddr();
        String thietBi = request.getHeader("User-Agent");

        GanVaiTroNhomDTO ketQua = phanQuyenService.ganVaiTroVaNhomKinhDoanh(
                nguoiDungId, dsVaiTroId, nhomKinhDoanhId, nguoiThucHienId, diaChiIp, thietBi);

        request.setAttribute("thanhCong", ketQua.isThanhCong());
        request.setAttribute("thongBao", ketQua.getThongBao());

        napDuLieuTrang(request, nguoiDungId);
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen.jsp").forward(request, response);
    }

    private void napDuLieuTrang(HttpServletRequest request, Integer selectedUserId) {
        List<NguoiDung> dsNguoiDung = phanQuyenService.layDanhSachNguoiDung();
        List<VaiTro> dsVaiTro = phanQuyenService.layDanhSachTatCaVaiTro();
        List<NhomKinhDoanh> dsNhomKinhDoanh = phanQuyenService.layDanhSachTatCaNhomKinhDoanh();

        int targetUserId = 0;
        if (selectedUserId != null && selectedUserId > 0) {
            targetUserId = selectedUserId;
        } else {
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                try {
                    targetUserId = Integer.parseInt(idParam);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        NguoiDung nguoiDungDuocChon = null;
        if (targetUserId > 0) {
            nguoiDungDuocChon = phanQuyenService.layThongTinNguoiDung(targetUserId);
        }

        if (nguoiDungDuocChon == null && !dsNguoiDung.isEmpty()) {
            nguoiDungDuocChon = dsNguoiDung.get(0);
        }

        request.setAttribute("dsNguoiDung", dsNguoiDung);
        request.setAttribute("dsVaiTro", dsVaiTro);
        request.setAttribute("dsNhomKinhDoanh", dsNhomKinhDoanh);
        request.setAttribute("nguoiDungDuocChon", nguoiDungDuocChon);
    }
}
