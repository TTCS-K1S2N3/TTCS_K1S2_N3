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
import java.util.List;

/**
 * Controller phục vụ trang danh mục khách hàng (trang nghiệp vụ chính của khối kinh doanh).
 * Tích hợp kiểm tra phân quyền phạm vi dữ liệu (Data Scope) ở server-side (Story S1-05).
 * URL: /khach-hang
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang", "/khach-hang/chi-tiet", "/khach-hang/360"})
public class KhachHangServlet extends HttpServlet {

    private final PhanQuyenDuLieuService phanQuyenService;
    private final vn.nhom10.crm.service.KhachHang360Service khachHang360Service;

    public KhachHangServlet() {
        this.phanQuyenService = new PhanQuyenDuLieuService();
        this.khachHang360Service = new vn.nhom10.crm.service.KhachHang360Service();
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this.phanQuyenService = phanQuyenService;
        this.khachHang360Service = new vn.nhom10.crm.service.KhachHang360Service(new vn.nhom10.crm.dao.KhachHang360DAO(), phanQuyenService);
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, vn.nhom10.crm.service.KhachHang360Service khachHang360Service) {
        this.phanQuyenService = phanQuyenService;
        this.khachHang360Service = khachHang360Service != null ? khachHang360Service : new vn.nhom10.crm.service.KhachHang360Service();
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
        String servletPath = request.getServletPath();
        String action = request.getParameter("action");

        // 0. API lấy danh sách hoạt động dạng JSON (Story S3-03, AC3)
        if ("api-hoat-dong".equalsIgnoreCase(action)) {
            response.setContentType("application/json;charset=UTF-8");
            Long id = null;
            try {
                if (paramId != null) id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("[]");
                return;
            }
            int limit = 500;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                try { limit = Integer.parseInt(limitParam.trim()); } catch (NumberFormatException ignored) {}
            }
            List<vn.nhom10.crm.model.HoatDong> ds = khachHang360Service.layDsHoatDong(id, limit, 0);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(chuyenDsHoatDongSangJson(ds));
            return;
        }

        // 1. Kiểm tra quyền khi xem chi tiết khách hàng trực tiếp bằng ID (AC3, AC4)
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

        // 1.1. Điều hướng canonical sang trang 360 khách hàng (Story S3-03)
        boolean xemChiTiet360 = "/khach-hang/chi-tiet".equals(servletPath)
                || "/khach-hang/360".equals(servletPath)
                || "chi-tiet".equalsIgnoreCase(action)
                || "360".equalsIgnoreCase(action);

        if (xemChiTiet360) {
            if (paramId == null || paramId.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/khach-hang");
                return;
            }

            Long id = null;
            try {
                id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}

            vn.nhom10.crm.dto.KhachHang360DTO khachHang360 = null;
            try {
                khachHang360 = khachHang360Service.layThongTin360(id, userDTO);
            } catch (vn.nhom10.crm.util.LoiKhongTimThayException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
                return;
            } catch (vn.nhom10.crm.util.LoiPhanQuyenException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

            request.setAttribute("currentUser", userDTO);
            request.setAttribute("nguoiDung", user);
            request.setAttribute("khachHang360", khachHang360);
            if (khachHang360 != null) {
                request.setAttribute("khachHang", khachHang360.getKhachHang());
                request.setAttribute("dsNguoiLienHe", khachHang360.getDsNguoiLienHe());
                request.setAttribute("dsCoHoiDangMo", khachHang360.getDsCoHoiDangMo());
                request.setAttribute("dsCoHoiDaDong", khachHang360.getDsCoHoiDaDong());
                request.setAttribute("dsHoatDong", khachHang360.getDsHoatDong());
                request.setAttribute("dsTepDinhKem", khachHang360.getDsTepDinhKem());
                request.setAttribute("dsHopDong", khachHang360.getDsHopDong());
                request.setAttribute("kpiGiaTriDaKy", khachHang360.getTongGiaTriDaKyDinhDang());
                request.setAttribute("kpiGiaTriDangMo", khachHang360.getTongGiaTriCoHoiDangMoDinhDang());
                request.setAttribute("soCoHoiDangMo", khachHang360.getSoCoHoiDangMo());
                request.setAttribute("soCoHoiDaDong", khachHang360.getSoCoHoiDaDong());
                request.setAttribute("tongSoHoatDong", khachHang360.getTongSoHoatDong());
                request.setAttribute("hoatDongJson", chuyenDsHoatDongSangJson(khachHang360.getDsHoatDong()));
            }

            response.setStatus(HttpServletResponse.SC_OK);
            request.getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp").forward(request, response);
            return;
        }

        // 2. Tiếp nhận tham số Data Scope từ người dùng
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

        // 3. Tự động lọc danh sách khách hàng theo Data Scope của user (AC1, AC2)
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

        // 3. Xử lý ghi nhận hoạt động nhanh trên trang 360 (Story S3-03)
        if ("them-hoat-dong".equalsIgnoreCase(action)) {
            response.setContentType("application/json;charset=UTF-8");
            String rawKhId = request.getParameter("khachHangId");
            if (rawKhId == null || rawKhId.isBlank()) {
                rawKhId = request.getParameter("idKhachHang");
            }
            if (rawKhId == null || rawKhId.isBlank()) {
                rawKhId = request.getParameter("id");
            }
            Long khId = null;
            try {
                if (rawKhId != null) khId = Long.parseLong(rawKhId.trim());
            } catch (NumberFormatException ignored) {}

            if (khId == null || khId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"ID khách hàng không hợp lệ.\"}");
                return;
            }

            String loai = request.getParameter("loaiHoatDong");
            if (loai == null || loai.isBlank()) loai = request.getParameter("loai");
            String tieuDe = request.getParameter("tieuDe");
            String noiDung = request.getParameter("noiDung");

            if (tieuDe == null || tieuDe.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Tiêu đề hoạt động không được để trống.\"}");
                return;
            }

            try {
                vn.nhom10.crm.model.HoatDong hd = khachHang360Service.themHoatDong(
                        khId,
                        user.getId(),
                        user.getNhomKinhDoanhId() != null ? (long) user.getNhomKinhDoanhId() : null,
                        loai,
                        tieuDe,
                        noiDung
                );
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"message\":\"Ghi nhận hoạt động thành công.\",\"hoatDong\":" + chuyenHoatDongSangJson(hd) + "}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
            return;
        }

        // 4. Xử lý thao tác sửa khách hàng qua POST /khach-hang (chặn sửa ngoài phạm vi)
        if (paramId != null || "sua".equals(action)) {
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
        } else if ("them".equals(action) || "create".equals(action) || paramId == null) {
            // 5. Xử lý thao tác thêm mới khách hàng (Story S1-05)
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
            // Tuyệt đối không cho phép client giả mạo người sở hữu (no owner spoofing).
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

    private String chuyenDsHoatDongSangJson(List<vn.nhom10.crm.model.HoatDong> ds) {
        if (ds == null || ds.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < ds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(chuyenHoatDongSangJson(ds.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String chuyenHoatDongSangJson(vn.nhom10.crm.model.HoatDong h) {
        if (h == null) return "{}";
        return "{" +
                "\"id\":" + (h.getId() != null ? h.getId() : 0) + "," +
                "\"maHoatDong\":\"" + escapeJson(h.getMaHoatDong()) + "\"," +
                "\"loai\":\"" + escapeJson(h.getLoaiHoatDong()) + "\"," +
                "\"tieuDe\":\"" + escapeJson(h.getTieuDe()) + "\"," +
                "\"thoiGian\":\"" + escapeJson(h.getThoiGianDinhDang()) + "\"," +
                "\"nguoiThucHien\":\"" + escapeJson(h.getTenNguoiThucHien()) + "\"," +
                "\"nguoiLienHe\":\"" + escapeJson(h.getTenNguoiLienHe()) + "\"," +
                "\"noiDung\":\"" + escapeJson(h.getNoiDung()) + "\"" +
                "}";
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}

