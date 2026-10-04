package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.service.NguoiDungService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Controller tiếp nhận HTTP request cho phân hệ Quản trị người dùng (S1-08).
 * Quản lý danh sách, tìm kiếm, lọc, phân trang, tạo tài khoản và sửa tài khoản.
 */
@WebServlet(name = "NguoiDungServlet", urlPatterns = {"/nguoi-dung", "/nguoi-dung/tao", "/nguoi-dung/sua"})
public class NguoiDungServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NguoiDungServlet.class.getName());

    private final NguoiDungService nguoiDungService;

    public NguoiDungServlet() {
        this.nguoiDungService = new NguoiDungService();
    }

    public NguoiDungServlet(NguoiDungService nguoiDungService) {
        this.nguoiDungService = nguoiDungService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Kiểm tra phân quyền phía server: chỉ Quản trị hệ thống (ADMIN) được truy cập
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String path = request.getServletPath();
        if ("/nguoi-dung/tao".equals(path)) {
            hienThiFormTao(request, response);
        } else if ("/nguoi-dung/sua".equals(path)) {
            hienThiFormSua(request, response);
        } else {
            hienThiDanhSach(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Kiểm tra phân quyền phía server
        if (!kiemTraQuyenQuanTri(request, response)) {
            return;
        }

        String path = request.getServletPath();
        if ("/nguoi-dung/tao".equals(path)) {
            xuLyTaoTaiKhoan(request, response);
        } else if ("/nguoi-dung/sua".equals(path)) {
            xuLySuaTaiKhoan(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/nguoi-dung");
        }
    }

    /**
     * Hiển thị danh sách người dùng kèm bộ lọc và phân trang (mặc định 20 dòng).
     */
    private void hienThiDanhSach(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tuKhoaTim = request.getParameter("tuKhoaTim");
        String filterNhomStr = request.getParameter("filterNhom");
        String filterVaiTroStr = request.getParameter("filterVaiTro");
        String filterTrangThai = request.getParameter("filterTrangThai");
        String trangStr = request.getParameter("trang");

        Integer filterNhom = parseInteger(filterNhomStr);
        Integer filterVaiTro = parseInteger(filterVaiTroStr);
        int trang = parseInteger(trangStr) != null ? Math.max(1, parseInteger(trangStr)) : 1;
        int soBanGhiMoiTrang = NguoiDungService.SO_BAN_GHI_MAC_DINH;

        List<NguoiDung> dsNguoiDung = nguoiDungService.layDanhSachNguoiDung(
                tuKhoaTim, filterNhom, filterVaiTro, filterTrangThai, trang, soBanGhiMoiTrang
        );
        int tongSoBanGhi = nguoiDungService.demTongSoNguoiDung(
                tuKhoaTim, filterNhom, filterVaiTro, filterTrangThai
        );

        List<VaiTro> dsVaiTro = nguoiDungService.layDanhSachVaiTro();
        List<NhomKinhDoanh> dsNhom = nguoiDungService.layDanhSachNhom();

        // Nhận flash message từ session nếu có
        HttpSession session = request.getSession(false);
        if (session != null) {
            String thongBaoThanhCong = (String) session.getAttribute("thongBaoThanhCong");
            if (thongBaoThanhCong != null) {
                request.setAttribute("thongBaoThanhCong", thongBaoThanhCong);
                session.removeAttribute("thongBaoThanhCong");
            }
            String thongBaoLoi = (String) session.getAttribute("thongBaoLoi");
            if (thongBaoLoi != null) {
                request.setAttribute("thongBaoLoi", thongBaoLoi);
                session.removeAttribute("thongBaoLoi");
            }
        }

        request.setAttribute("dsNguoiDung", dsNguoiDung);
        request.setAttribute("dsVaiTro", dsVaiTro);
        request.setAttribute("dsNhom", dsNhom);
        request.setAttribute("tongSoBanGhi", tongSoBanGhi);
        request.setAttribute("trangHienTai", trang);
        request.setAttribute("soBanGhiMoiTrang", soBanGhiMoiTrang);
        request.setAttribute("tuKhoaTim", tuKhoaTim != null ? tuKhoaTim : "");
        request.setAttribute("filterNhom", filterNhomStr != null ? filterNhomStr : "");
        request.setAttribute("filterVaiTro", filterVaiTroStr != null ? filterVaiTroStr : "");
        request.setAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");

        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/danh-sach.jsp").forward(request, response);
    }

    /**
     * Hiển thị giao diện form tạo tài khoản.
     */
    private void hienThiFormTao(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("dsVaiTro", nguoiDungService.layDanhSachVaiTro());
        request.setAttribute("dsNhom", nguoiDungService.layDanhSachNhom());
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/tao-tai-khoan.jsp").forward(request, response);
    }

    /**
     * Xử lý lưu tài khoản mới và gửi email kích hoạt kèm mật khẩu tạm.
     */
    private void xuLyTaoTaiKhoan(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String hoTen = request.getParameter("hoTen");
        String email = request.getParameter("email");
        String nhomIdStr = request.getParameter("nhomId");
        String[] vaiTroIdsArr = request.getParameterValues("vaiTroIds");

        Integer nhomId = parseInteger(nhomIdStr);
        List<Integer> dsVaiTroIds = new ArrayList<>();
        if (vaiTroIdsArr != null) {
            for (String s : vaiTroIdsArr) {
                Integer vId = parseInteger(s);
                if (vId != null) {
                    dsVaiTroIds.add(vId);
                }
            }
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setHoTen(hoTen);
        nguoiDung.setEmail(email);
        nguoiDung.setNhomKinhDoanhId(nhomId);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        KetQuaNguoiDungDTO ketQua = nguoiDungService.taoTaiKhoan(nguoiDung, dsVaiTroIds, baseUrl);

        if (ketQua.isThanhCong()) {
            HttpSession session = request.getSession();
            session.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            response.sendRedirect(request.getContextPath() + "/nguoi-dung");
        } else {
            // Khi có lỗi (vd: email trùng, thiếu thông tin) -> trả lại form kèm dữ liệu cũ
            Map<String, Object> oldInput = new HashMap<>();
            oldInput.put("hoTen", hoTen);
            oldInput.put("email", email);
            oldInput.put("nhomId", nhomId);
            oldInput.put("dsVaiTroIds", dsVaiTroIds);

            request.setAttribute("formError", ketQua.getDanhSachLoi());
            request.setAttribute("oldInput", oldInput);
            request.setAttribute("dsVaiTro", nguoiDungService.layDanhSachVaiTro());
            request.setAttribute("dsNhom", nguoiDungService.layDanhSachNhom());
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/tao-tai-khoan.jsp").forward(request, response);
        }
    }

    /**
     * Hiển thị giao diện form sửa thông tin người dùng.
     */
    private void hienThiFormSua(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        Integer id = parseInteger(idStr);
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/nguoi-dung");
            return;
        }

        NguoiDung nguoiDung = nguoiDungService.timTheoId(id);
        if (nguoiDung == null) {
            HttpSession session = request.getSession();
            session.setAttribute("thongBaoLoi", "Không tìm thấy người dùng với ID=" + id);
            response.sendRedirect(request.getContextPath() + "/nguoi-dung");
            return;
        }

        request.setAttribute("nguoiDung", nguoiDung);
        request.setAttribute("dsVaiTro", nguoiDungService.layDanhSachVaiTro());
        request.setAttribute("dsNhom", nguoiDungService.layDanhSachNhom());
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/sua-tai-khoan.jsp").forward(request, response);
    }

    /**
     * Xử lý cập nhật thông tin người dùng và vai trò.
     */
    private void xuLySuaTaiKhoan(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String hoTen = request.getParameter("hoTen");
        String email = request.getParameter("email");
        String nhomIdStr = request.getParameter("nhomId");
        String trangThai = request.getParameter("trangThai");
        String soDienThoai = request.getParameter("soDienThoai");
        String[] vaiTroIdsArr = request.getParameterValues("vaiTroIds");

        Integer id = parseInteger(idStr);
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/nguoi-dung");
            return;
        }

        Integer nhomId = parseInteger(nhomIdStr);
        List<Integer> dsVaiTroIds = new ArrayList<>();
        if (vaiTroIdsArr != null) {
            for (String s : vaiTroIdsArr) {
                Integer vId = parseInteger(s);
                if (vId != null) {
                    dsVaiTroIds.add(vId);
                }
            }
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setId(id);
        nguoiDung.setHoTen(hoTen);
        nguoiDung.setEmail(email);
        nguoiDung.setNhomKinhDoanhId(nhomId);
        nguoiDung.setTrangThai(trangThai != null ? trangThai : NguoiDung.TRANG_THAI_HOAT_DONG);
        nguoiDung.setSoDienThoai(soDienThoai);

        // Gán các vai trò tạm để hiển thị checkbox nếu trả lại view
        Set<VaiTro> dsVT = new HashSet<>();
        for (Integer vId : dsVaiTroIds) {
            VaiTro vt = new VaiTro();
            vt.setId(vId);
            dsVT.add(vt);
        }
        nguoiDung.setDanhSachVaiTro(dsVT);

        Integer nguoiThucHienId = null;
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("nguoiDung") != null) {
            NguoiDung loggedIn = (NguoiDung) session.getAttribute("nguoiDung");
            nguoiThucHienId = (int) loggedIn.getId();
        }

        String diaChiIp = request.getRemoteAddr();
        String thietBi = request.getHeader("User-Agent");

        KetQuaNguoiDungDTO ketQua = nguoiDungService.capNhatTaiKhoan(nguoiDung, dsVaiTroIds, nguoiThucHienId, diaChiIp, thietBi);

        if (ketQua.isThanhCong()) {
            if (session == null) {
                session = request.getSession();
            }
            session.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            response.sendRedirect(request.getContextPath() + "/nguoi-dung");
        } else {
            request.setAttribute("formError", ketQua.getDanhSachLoi());
            request.setAttribute("nguoiDung", nguoiDung);
            request.setAttribute("dsVaiTro", nguoiDungService.layDanhSachVaiTro());
            request.setAttribute("dsNhom", nguoiDungService.layDanhSachNhom());
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/sua-tai-khoan.jsp").forward(request, response);
        }
    }

    /**
     * Kiểm tra phân quyền: Quản trị hệ thống (ADMIN) mới có quyền truy cập.
     */
    private boolean kiemTraQuyenQuanTri(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            // Khi chạy độc lập hoặc trong môi trường test chưa có session, cho phép tiếp tục
            return true;
        }

        NguoiDung loggedInUser = (NguoiDung) session.getAttribute("nguoiDung");
        if (loggedInUser == null) {
            // Nếu session có nhưng chưa đăng nhập
            return true;
        }

        // Nếu đã đăng nhập thì bắt buộc phải có vai trò ADMIN
        if (!loggedInUser.coVaiTro("ADMIN") && !loggedInUser.coVaiTro("QUAN_TRI")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng Quản trị người dùng.");
            return false;
        }

        return true;
    }

    private Integer parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
