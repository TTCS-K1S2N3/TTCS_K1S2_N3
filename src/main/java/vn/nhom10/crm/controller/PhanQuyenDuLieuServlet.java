package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller phục vụ hiển thị và xử lý phân quyền theo phạm vi dữ liệu (Story S1-05).
 * URL Patterns:
 * - /phan-quyen-du-lieu : Xem danh sách theo phạm vi, tìm kiếm, xuất file Excel
 * - /chi-tiet-ban-ghi   : Xem chi tiết bản ghi và kiểm tra quyền truy cập (AC3, AC4)
 */
@WebServlet(name = "PhanQuyenDuLieuServlet", urlPatterns = {"/phan-quyen-du-lieu", "/chi-tiet-ban-ghi"})
public class PhanQuyenDuLieuServlet extends HttpServlet {

    public static final String SESSION_USER_KEY = "nguoiDung";
    private final PhanQuyenDuLieuService phanQuyenService;

    public PhanQuyenDuLieuServlet() {
        this.phanQuyenService = new PhanQuyenDuLieuService();
    }

    public PhanQuyenDuLieuServlet(PhanQuyenDuLieuService phanQuyenService) {
        this.phanQuyenService = phanQuyenService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();
        if ("/chi-tiet-ban-ghi".equals(servletPath)) {
            xuLyXemChiTiet(request, response);
            return;
        }

        xuLyXemDanhSach(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();
        String action = request.getParameter("action");
        if ("/chi-tiet-ban-ghi".equals(servletPath) || "sua".equals(action)) {
            xuLySuaBanGhi(request, response);
            return;
        }

        doGet(request, response);
    }

    /**
     * Lấy thông tin người dùng từ session chuẩn 'nguoiDung'.
     */
    private NguoiDungDTO layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userObj = session.getAttribute(SESSION_USER_KEY);
        if (userObj instanceof NguoiDungDTO) {
            return (NguoiDungDTO) userObj;
        }
        if (userObj instanceof NguoiDung) {
            return NguoiDungDTO.tuNguoiDung((NguoiDung) userObj);
        }
        return null;
    }

    /**
     * Xử lý hiển thị danh sách theo phạm vi dữ liệu, tìm kiếm và xuất Excel (AC1, AC2).
     */
    private void xuLyXemDanhSach(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDungDTO currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        // 1. Tiếp nhận tham số phạm vi dữ liệu người dùng yêu cầu
        String paramPhamVi = request.getParameter("phamVi");
        PhamViDuLieu phamViYeuCau = (paramPhamVi != null && !paramPhamVi.trim().isEmpty())
                ? PhamViDuLieu.tuMa(paramPhamVi.trim())
                : currentUser.getPhamViHienTai();

        // Kiểm tra quyền: nếu chọn phạm vi vượt quá quyền hạn thì thông báo lỗi trực quan
        if (paramPhamVi != null && !currentUser.coQuyenChonPhamVi(phamViYeuCau)) {
            request.setAttribute("thongBaoCanhBao",
                    "Bạn không có quyền truy cập phạm vi '" + phamViYeuCau.getTenHienThi() +
                    "'. Hệ thống đã tự động giới hạn về phạm vi an toàn.");
        }

        // Xác định phạm vi hiệu lực cuối cùng sau khi kiểm tra server-side
        PhamViDuLieu phamViHieuLuc = phanQuyenService.xacDinhPhamViHieuLuc(currentUser, phamViYeuCau);
        currentUser.setPhamViHienTai(phamViHieuLuc);

        // 2. Tiếp nhận tham số tìm kiếm và bộ lọc loại nghiệp vụ
        String tuKhoa = request.getParameter("tuKhoa");
        String loaiNghiepVu = request.getParameter("loai");
        if (loaiNghiepVu == null || loaiNghiepVu.trim().isEmpty()) {
            loaiNghiepVu = "ALL";
        }

        // 3. Lấy dữ liệu và thực hiện lọc theo phạm vi (AC1, AC2)
        List<BanGhiNghiepVuDTO> danhSachDaLoc = phanQuyenService.layDanhSachDuLieu(
                currentUser, phamViHieuLuc, tuKhoa, loaiNghiepVu
        );

        // 4. Kiểm tra nếu là yêu cầu Xuất Excel / CSV (AC2)
        String xuatExcel = request.getParameter("xuatExcel");
        if ("true".equalsIgnoreCase(xuatExcel)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"du-lieu-crm-" + phamViHieuLuc.getMa().toLowerCase() + ".csv\"");
            String csvContent = phanQuyenService.xuatDuLieuCSV(danhSachDaLoc);
            try (OutputStream os = response.getOutputStream()) {
                os.write(csvContent.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            return;
        }

        // 5. Gắn dữ liệu vào request và chuyển sang view JSP
        request.setAttribute("currentUser", currentUser);
        request.setAttribute("phamViHienTai", phamViHieuLuc);
        request.setAttribute("danhSachPhamViChoPhep", currentUser.getDanhSachPhamViChoPhep());
        request.setAttribute("danhSachBanGhi", danhSachDaLoc);
        request.setAttribute("tongSoBanGhi", danhSachDaLoc.size());
        request.setAttribute("tongSoBanGhiGoc", phanQuyenService.layDanhSachDuLieuMau().size());
        request.setAttribute("tuKhoaHienTai", tuKhoa != null ? tuKhoa : "");
        request.setAttribute("loaiHienTai", loaiNghiepVu);

        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp").forward(request, response);
    }

    /**
     * Xử lý kiểm tra quyền truy cập bản ghi chi tiết (AC3, AC4).
     */
    private void xuLyXemChiTiet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDungDTO currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        String paramId = request.getParameter("id");
        Long id = null;
        try {
            if (paramId != null) id = Long.parseLong(paramId.trim());
        } catch (NumberFormatException ignored) {}

        String loaiNghiepVu = request.getParameter("loai");
        BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, loaiNghiepVu);

        // Kiểm tra quyền truy cập ở server-side
        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(currentUser, banGhi);

        response.setContentType("text/html;charset=UTF-8");
        if (!ketQua.isCoQuyen()) {
            // AC3: Trả về HTTP 403 Forbidden và hiển thị trang thông báo tiếng Việt rõ ràng
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.setAttribute("banGhi", banGhi);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
        } else {
            // Có quyền truy cập -> hiển thị chi tiết (HTTP 200 OK)
            response.setStatus(HttpServletResponse.SC_OK);
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            request.setAttribute("banGhi", banGhi);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp").forward(request, response);
        }
    }

    /**
     * Xử lý sửa bản ghi và kiểm tra quyền sửa chặt chẽ ở server-side.
     */
    private void xuLySuaBanGhi(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        NguoiDungDTO currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        String paramId = request.getParameter("id");
        Long id = null;
        try {
            if (paramId != null) id = Long.parseLong(paramId.trim());
        } catch (NumberFormatException ignored) {}

        String loaiNghiepVu = request.getParameter("loai");
        BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, loaiNghiepVu);

        // Kiểm tra quyền sửa
        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenSua(currentUser, banGhi);

        response.setContentType("text/html;charset=UTF-8");
        if (!ketQua.isCoQuyen()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.setAttribute("banGhi", banGhi);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
            return;
        }

        // Cập nhật thông tin nếu có truyền lên
        String tieuDeMoi = request.getParameter("tieuDe");
        String giaTriMoi = request.getParameter("giaTri");
        String trangThaiMoi = request.getParameter("trangThai");
        String moTaMoi = request.getParameter("moTaChiTiet");

        if (tieuDeMoi != null && !tieuDeMoi.trim().isEmpty()) {
            banGhi.setTieuDe(tieuDeMoi.trim());
        }
        if (giaTriMoi != null) {
            banGhi.setGiaTri(giaTriMoi.trim());
        }
        if (trangThaiMoi != null) {
            banGhi.setTrangThai(trangThaiMoi.trim());
        }
        if (moTaMoi != null) {
            banGhi.setMoTaChiTiet(moTaMoi.trim());
        }

        phanQuyenService.capNhatBanGhi(banGhi);

        response.setStatus(HttpServletResponse.SC_OK);
        request.setAttribute("currentUser", currentUser);
        request.setAttribute("thongBaoThanhCong", "Cập nhật dữ liệu thành công.");
        request.setAttribute("banGhi", banGhi);
        request.getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp").forward(request, response);
    }
}
