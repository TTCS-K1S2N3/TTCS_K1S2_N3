package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroNguoiDung;
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

    private static final String SESSION_USER_KEY = "nguoiDungHienTai";
    private final PhanQuyenDuLieuService phanQuyenService = new PhanQuyenDuLieuService();

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
        // Cho phép chuyển đổi vai trò hoặc phạm vi bằng POST và điều hướng lại
        doGet(request, response);
    }

    /**
     * Xử lý hiển thị danh sách theo phạm vi dữ liệu, tìm kiếm và xuất Excel (AC1, AC2).
     */
    private void xuLyXemDanhSach(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);

        // 1. Giả lập vai trò người dùng (thuận tiện cho việc demo trực tiếp hoặc kiểm thử)
        String giaLapRole = request.getParameter("giaLapUser");
        NguoiDungDTO currentUser = (NguoiDungDTO) session.getAttribute(SESSION_USER_KEY);

        if (giaLapRole != null && !giaLapRole.trim().isEmpty()) {
            currentUser = taoNguoiDungGiaLap(giaLapRole.trim());
            session.setAttribute(SESSION_USER_KEY, currentUser);
        } else if (currentUser == null) {
            // Mặc định khởi tạo là Nhân viên A (Sales Rep) để kiểm chứng phạm vi chặt chẽ nhất
            currentUser = taoNguoiDungGiaLap("sales_a");
            session.setAttribute(SESSION_USER_KEY, currentUser);
        }

        // 2. Tiếp nhận tham số phạm vi dữ liệu người dùng yêu cầu
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

        // 3. Tiếp nhận tham số tìm kiếm và bộ lọc loại nghiệp vụ
        String tuKhoa = request.getParameter("tuKhoa");
        String loaiNghiepVu = request.getParameter("loai");
        if (loaiNghiepVu == null || loaiNghiepVu.trim().isEmpty()) {
            loaiNghiepVu = "ALL";
        }

        // 4. Lấy dữ liệu và thực hiện lọc theo phạm vi (AC1, AC2)
        List<BanGhiNghiepVuDTO> danhSachGoc = phanQuyenService.layDanhSachDuLieuMau();
        List<BanGhiNghiepVuDTO> danhSachDaLoc = phanQuyenService.locTheoPhamVi(
                danhSachGoc, currentUser, phamViHieuLuc, tuKhoa, loaiNghiepVu
        );

        // 5. Kiểm tra nếu là yêu cầu Xuất Excel / CSV (AC2)
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

        // 6. Gắn dữ liệu vào request và chuyển sang view JSP
        request.setAttribute("currentUser", currentUser);
        request.setAttribute("phamViHienTai", phamViHieuLuc);
        request.setAttribute("danhSachPhamViChoPhep", currentUser.getDanhSachPhamViChoPhep());
        request.setAttribute("danhSachBanGhi", danhSachDaLoc);
        request.setAttribute("tongSoBanGhi", danhSachDaLoc.size());
        request.setAttribute("tongSoBanGhiGoc", danhSachGoc.size());
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
        HttpSession session = request.getSession(true);
        NguoiDungDTO currentUser = (NguoiDungDTO) session.getAttribute(SESSION_USER_KEY);
        if (currentUser == null) {
            currentUser = taoNguoiDungGiaLap("sales_a");
            session.setAttribute(SESSION_USER_KEY, currentUser);
        }

        String paramId = request.getParameter("id");
        Long id = null;
        try {
            if (paramId != null) id = Long.parseLong(paramId.trim());
        } catch (NumberFormatException ignored) {}

        List<BanGhiNghiepVuDTO> danhSachGoc = phanQuyenService.layDanhSachDuLieuMau();
        BanGhiNghiepVuDTO banGhi = null;
        if (id != null) {
            for (BanGhiNghiepVuDTO bg : danhSachGoc) {
                if (bg.getId().equals(id)) {
                    banGhi = bg;
                    break;
                }
            }
        }

        // Kiểm tra quyền truy cập
        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(currentUser, banGhi);

        response.setContentType("text/html;charset=UTF-8");
        if (!ketQua.isCoQuyen()) {
            // AC3: Hiển thị trang lỗi từ chối với thông báo tiếng Việt rõ ràng
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.setAttribute("banGhi", banGhi);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
        } else {
            // Có quyền truy cập -> hiển thị chi tiết
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            request.setAttribute("banGhi", banGhi);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/danh-sach-theo-pham-vi.jsp").forward(request, response);
        }
    }

    /**
     * Phương thức tiện ích sinh thông tin tài khoản demo cho 4 vai trò chính.
     */
    private NguoiDungDTO taoNguoiDungGiaLap(String maVaiTro) {
        if ("sales_b".equalsIgnoreCase(maVaiTro)) {
            return new NguoiDungDTO(102L, "Trần Thị B (Sales)", "sales.b@crm.vn",
                    VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        } else if ("team_lead".equalsIgnoreCase(maVaiTro)) {
            return new NguoiDungDTO(100L, "Lê Thị Trưởng Nhóm", "lead.bac@crm.vn",
                    VaiTroNguoiDung.TEAM_LEAD, 1L, "Nhóm Miền Bắc");
        } else if ("director".equalsIgnoreCase(maVaiTro)) {
            return new NguoiDungDTO(1L, "Bàn Thị Linh (Giám đốc)", "linh.ban@crm.vn",
                    VaiTroNguoiDung.DIRECTOR, null, "Toàn công ty");
        } else {
            // Mặc định: Sales A
            return new NguoiDungDTO(101L, "Nguyễn Văn A (Sales)", "sales.a@crm.vn",
                    VaiTroNguoiDung.SALES_REP, 1L, "Nhóm Miền Bắc");
        }
    }
}
