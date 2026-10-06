package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeChamSocDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.service.ChamSocKhachHangService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.io.IOException;
import java.util.List;

/**
 * Controller phục vụ trang danh mục khách hàng (trang nghiệp vụ chính của khối kinh doanh).
 * Tích hợp kiểm tra phân quyền phạm vi dữ liệu (Data Scope) ở server-side (Story S1-05).
 * Tích hợp nghiệp vụ Chăm sóc khách hàng định kỳ sau ký hợp đồng (Story S3-09).
 * URL: /khach-hang
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang"})
public class KhachHangServlet extends HttpServlet {

    private final PhanQuyenDuLieuService phanQuyenService;
    private final ChamSocKhachHangService chamSocService;

    public KhachHangServlet() {
        this.phanQuyenService = new PhanQuyenDuLieuService();
        this.chamSocService = new ChamSocKhachHangService();
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this(phanQuyenService, new ChamSocKhachHangService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, ChamSocKhachHangService chamSocService) {
        this.phanQuyenService = (phanQuyenService != null) ? phanQuyenService : new PhanQuyenDuLieuService();
        this.chamSocService = (chamSocService != null) ? chamSocService : new ChamSocKhachHangService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);

        String paramId = request.getParameter("id");

        // 1. Kiểm tra quyền khi xem chi tiết khách hàng trực tiếp bằng ID (AC3, AC4 - S1-05)
        if (paramId != null && !paramId.trim().isEmpty()) {
            Long id = null;
            try {
                id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}

            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            if (id == null || banGhi == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy khách hàng.");
                return;
            }

            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(userDTO, banGhi);

            if (!ketQua.isCoQuyen()) {
                // CHẶN TRUY CẬP NGOÀI PHẠM VỊ: Trả về HTTP 403 Forbidden
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ketQua.getThongBao());
                request.setAttribute("banGhi", banGhi);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

            request.setAttribute("banGhiChiTiet", banGhi);
            request.setAttribute("banGhi", banGhi);
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
        }

        // 2. Tiếp nhận tham số Data Scope từ người dùng (S1-05)
        String paramPhamVi = request.getParameter("phamVi");
        PhamViDuLieu phamViYeuCau = (paramPhamVi != null && !paramPhamVi.trim().isEmpty())
                ? PhamViDuLieu.tuMa(paramPhamVi.trim())
                : userDTO.getPhamViHienTai();

        // Kiểm tra quyền: nếu chọn vượt quá quyền hạn thì tự động ép về phạm vi an toàn
        if (paramPhamVi != null && !userDTO.coQuyenChonPhamVi(phamViYeuCau)) {
            request.setAttribute("thongBaoCanhBao",
                    "Bạn không có quyền truy cập phạm vi '" + phamViYeuCau.getTenHienThi() +
                    "'. Hệ thống đã tự động giới hạn về phạm vi an toàn.");
        }

        PhamViDuLieu phamViHieuLuc = phanQuyenService.xacDinhPhamViHieuLuc(userDTO, phamViYeuCau);
        userDTO.setPhamViHienTai(phamViHieuLuc);

        String tuKhoa = request.getParameter("tuKhoa");

        // 3. Tự động lọc danh sách khách hàng theo Data Scope của user (AC1, AC2 - S1-05)
        List<BanGhiNghiepVuDTO> danhSachKhachHang = phanQuyenService.layDanhSachDuLieu(
                userDTO, phamViHieuLuc, tuKhoa, "KHACH_HANG"
        );

        // 4. Xử lý Xuất Excel danh mục khách hàng (.xlsx)
        String xuatExcel = request.getParameter("xuatExcel");
        if ("true".equalsIgnoreCase(xuatExcel)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"du-lieu-khach-hang-" + phamViHieuLuc.getMa().toLowerCase() + ".xlsx\"");
            byte[] excelBytes = phanQuyenService.xuatDuLieuExcel(danhSachKhachHang);
            response.setContentLength(excelBytes.length);
            try (java.io.OutputStream os = response.getOutputStream()) {
                os.write(excelBytes);
                os.flush();
            }
            return;
        }

        boolean coQuyenDanhMuc = user != null && (user.coVaiTro("ADMIN") || user.coVaiTro("DIRECTOR"));
        request.setAttribute("coQuyenDanhMuc", coQuyenDanhMuc);
        request.setAttribute("nguoiDung", user);
        request.setAttribute("currentUser", userDTO);
        request.setAttribute("phamViHienTai", phamViHieuLuc);
        request.setAttribute("danhSachPhamViChoPhep", userDTO.getDanhSachPhamViChoPhep());
        request.setAttribute("danhSachKhachHang", danhSachKhachHang);
        request.setAttribute("tongSoKhachHang", danhSachKhachHang.size());
        request.setAttribute("tuKhoaHienTai", tuKhoa != null ? tuKhoa : "");

        // =====================================================================
        // 5. NGHIỆP VỤ CHĂM SÓC KHÁCH HÀNG ĐỊNH KỲ (Story S3-09)
        // • AC1: Danh sách khách chưa tương tác trong N ngày, N cấu hình được
        // • AC2: Sắp xếp theo giá trị hợp đồng giảm dần
        // =====================================================================
        String paramTab = request.getParameter("tab");
        String paramSoNgay = request.getParameter("soNgay");
        int soNgay = chamSocService.laySoNgayCauHinh(paramSoNgay);

        boolean laChamSocKhachHang = user != null && user.coVaiTro("CUST_SUCCESS");
        String tabHienTai = (paramTab != null && !paramTab.trim().isEmpty())
                ? paramTab.trim()
                : (laChamSocKhachHang ? "cham-soc" : "tat-ca");

        // Lấy danh sách khách hàng cần chăm sóc định kỳ (kèm Data Scope và N ngày)
        List<KhachHangChamSocDTO> danhSachChamSoc = chamSocService.layDanhSachCanChamSoc(
                userDTO, soNgay, tuKhoa, true
        );
        ThongKeChamSocDTO thongKeChamSoc = chamSocService.tinhThongKe(danhSachChamSoc);
        boolean coQuyenChamSoc = chamSocService.kiemTraQuyenChamSoc(userDTO);

        request.setAttribute("tabHienTai", tabHienTai);
        request.setAttribute("soNgayCauHinh", String.valueOf(soNgay));
        request.setAttribute("laChamSocKhachHang", laChamSocKhachHang);
        request.setAttribute("danhSachChamSoc", danhSachChamSoc);
        request.setAttribute("thongKeChamSoc", thongKeChamSoc);
        request.setAttribute("coQuyenChamSoc", coQuyenChamSoc);

        response.setStatus(HttpServletResponse.SC_OK);
        request.getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("nguoiDung") == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");
        NguoiDungDTO userDTO = NguoiDungDTO.tuNguoiDung(user);

        String paramId = request.getParameter("id");
        String action = request.getParameter("action");

        // 1. Xử lý thao tác sửa khách hàng qua POST /khach-hang (chặn sửa ngoài phạm vi)
        if ("sua".equals(action) || (paramId != null && !"danhDauLienHe".equals(action))) {
            Long id = null;
            try {
                if (paramId != null) id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}

            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenSua(userDTO, banGhi);

            if (!ketQua.isCoQuyen()) {
                // CHẶN SỬA NGOÀI PHẠM VỊ: HTTP 403 Forbidden
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ketQua.getThongBao());
                request.setAttribute("banGhi", banGhi);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

            // Cập nhật thông tin nếu có quyền sửa
            String tieuDeMoi = request.getParameter("tieuDe");
            if (tieuDeMoi == null) tieuDeMoi = request.getParameter("tenCongTy");
            String giaTriMoi = request.getParameter("giaTri");
            if (giaTriMoi == null) giaTriMoi = request.getParameter("doanhThuUocTinh");
            String trangThaiMoi = request.getParameter("trangThai");
            String moTaMoi = request.getParameter("moTaChiTiet");

            if (tieuDeMoi != null && !tieuDeMoi.trim().isEmpty()) banGhi.setTieuDe(tieuDeMoi.trim());
            if (giaTriMoi != null) banGhi.setGiaTri(giaTriMoi.trim());
            if (trangThaiMoi != null) banGhi.setTrangThai(trangThaiMoi.trim());
            if (moTaMoi != null) banGhi.setMoTaChiTiet(moTaMoi.trim());

            phanQuyenService.capNhatBanGhi(banGhi);
            request.setAttribute("thongBaoThanhCong", "Cập nhật dữ liệu khách hàng thành công.");
        } else if ("danhDauLienHe".equals(action)) {
            // =====================================================================
            // 2. Story S3-09: Đánh dấu đã liên hệ ngay trên danh sách (AC3)
            // =====================================================================
            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

            // Server-side authorization: Chỉ những role có quyền chăm sóc mới được thực hiện
            if (!chamSocService.kiemTraQuyenChamSoc(userDTO)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                if (isAjax) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Từ chối thao tác: Vai trò của bạn không có quyền ghi nhận chăm sóc khách hàng.\"}");
                } else {
                    request.setAttribute("currentUser", userDTO);
                    request.setAttribute("thongBaoLoi", "Từ chối thao tác: Vai trò của bạn không có quyền ghi nhận chăm sóc khách hàng.");
                    request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                }
                return;
            }

            // Server-side validation: Mã khách hàng bắt buộc
            String paramKhachHangId = request.getParameter("khachHangId");
            Long khachHangId = null;
            try {
                if (paramKhachHangId != null) {
                    khachHangId = Long.parseLong(paramKhachHangId.trim());
                }
            } catch (NumberFormatException ignored) {}

            if (khachHangId == null || khachHangId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                if (isAjax) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Mã khách hàng không hợp lệ.\"}");
                } else {
                    request.setAttribute("thongBaoLoi", "Mã khách hàng không hợp lệ.");
                    doGet(request, response);
                }
                return;
            }

            String tenCongTy = request.getParameter("tenCongTy");
            String ghiChu = request.getParameter("ghiChu");
            String kenhLienHe = request.getParameter("kenhLienHe");
            String tenKhach = (tenCongTy != null && !tenCongTy.trim().isEmpty()) ? tenCongTy.trim() : "khách hàng";

            try {
                chamSocService.danhDauDaLienHe(userDTO, khachHangId, tenKhach, kenhLienHe, ghiChu);
                if (isAjax) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":true,\"message\":\"Đã đánh dấu liên hệ thành công cho " + escapeJson(tenKhach) + ".\"}");
                    return;
                }
                request.setAttribute("thongBaoThanhCong", "Đã đánh dấu liên hệ thành công cho " + tenKhach + ".");
                request.setAttribute("tabHienTai", "cham-soc");
            } catch (LoiPhanQuyenException ex) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                if (isAjax) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
                    return;
                }
                request.setAttribute("thongBaoLoi", ex.getMessage());
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                if (isAjax) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Lỗi ghi nhận liên hệ: " + escapeJson(ex.getMessage()) + "\"}");
                    return;
                }
                request.setAttribute("thongBaoLoi", "Lỗi ghi nhận liên hệ: " + ex.getMessage());
            }
        } else if ("them".equals(action) || "create".equals(action) || paramId == null) {
            // 3. Xử lý thao tác thêm mới khách hàng (Story S1-05)
            String tenCongTy = request.getParameter("tenCongTy");
            if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
                tenCongTy = request.getParameter("tieuDe");
            }

            String maKhachHang = request.getParameter("maKhachHang");
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                maKhachHang = request.getParameter("maBanGhi");
            }

            String giaTri = request.getParameter("doanhThuUocTinh");
            if (giaTri == null || giaTri.trim().isEmpty()) {
                giaTri = request.getParameter("giaTri");
            }

            String trangThai = request.getParameter("trangThai");
            String moTaChiTiet = request.getParameter("moTaChiTiet");

            // Server-side validation: Bắt buộc tên công ty / khách hàng
            if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Tên công ty / khách hàng không được để trống.");
                doGet(request, response);
                return;
            }

            // Server-side validation: Kiểm tra trùng mã khách hàng nếu có nhập
            if (maKhachHang != null && !maKhachHang.trim().isEmpty() && phanQuyenService.kiemTraTonTaiMaKhachHang(maKhachHang.trim())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Mã khách hàng '" + maKhachHang.trim() + "' đã tồn tại trong hệ thống.");
                doGet(request, response);
                return;
            }

            // BẢO MẬT & DATA SCOPE (S1-05):
            // Người sở hữu luôn tự động quyết định bởi server từ session người dùng (nguoiDung).
            try {
                BanGhiNghiepVuDTO khachHangMoi = phanQuyenService.themKhachHang(
                        userDTO,
                        maKhachHang,
                        tenCongTy,
                        giaTri,
                        trangThai,
                        moTaChiTiet
                );
                request.setAttribute("thongBaoThanhCong", "Thêm mới khách hàng '" + khachHangMoi.getTieuDe() + "' thành công.");
                request.setAttribute("khachHangVuaThem", khachHangMoi);
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", e.getMessage());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("thongBaoLoi", "Lỗi tạo khách hàng: " + e.getMessage());
            }
        }

        doGet(request, response);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
