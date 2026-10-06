package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.KhuVucDiaLyDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.*;
import vn.nhom10.crm.service.BoLocKhachHangService;
import vn.nhom10.crm.service.KhachHangService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ trang danh mục khách hàng, tìm kiếm và lọc đa điều kiện (Story S3-07):
 * - AC1: Lọc theo trạng thái, ngành nghề, quy mô, khu vực, người sở hữu
 * - AC2: Tìm theo tên, mã số thuế, số điện thoại người liên hệ
 * - AC3: Lưu lại được bộ lọc hay dùng, áp dụng bộ lọc đã lưu, đặt mặc định
 * URL: /khach-hang
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang"})
public class KhachHangServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhachHangServlet.class.getName());

    private final PhanQuyenDuLieuService phanQuyenService;
    private final KhachHangService khachHangService;
    private final BoLocKhachHangService boLocService;
    private final DanhMucBanHangDAO danhMucDAO;
    private final KhuVucDiaLyDAO khuVucDAO;
    private final NguoiDungDAO nguoiDungDAO;

    public KhachHangServlet() {
        this(new PhanQuyenDuLieuService(), new KhachHangService(), new BoLocKhachHangService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this(phanQuyenService, new KhachHangService(), new BoLocKhachHangService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            KhachHangService khachHangService,
                            BoLocKhachHangService boLocService) {
        this.phanQuyenService = phanQuyenService != null ? phanQuyenService : new PhanQuyenDuLieuService();
        this.khachHangService = khachHangService != null ? khachHangService : new KhachHangService();
        this.boLocService = boLocService != null ? boLocService : new BoLocKhachHangService();
        this.danhMucDAO = new DanhMucBanHangDAO();
        this.khuVucDAO = new KhuVucDiaLyDAO();
        this.nguoiDungDAO = new NguoiDungDAO();
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

        // 1. Kiểm tra quyền khi xem chi tiết khách hàng trực tiếp bằng ID (S1-05 AC3, AC4)
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

        // 2. Tiếp nhận tham số Data Scope từ người dùng
        String paramPhamVi = request.getParameter("phamVi");
        PhamViDuLieu phamViYeuCau = (paramPhamVi != null && !paramPhamVi.trim().isEmpty())
                ? PhamViDuLieu.tuMa(paramPhamVi.trim())
                : userDTO.getPhamViHienTai();

        if (paramPhamVi != null && !userDTO.coQuyenChonPhamVi(phamViYeuCau)) {
            request.setAttribute("thongBaoCanhBao",
                    "Bạn không có quyền truy cập phạm vi '" + phamViYeuCau.getTenHienThi() +
                    "'. Hệ thống đã tự động giới hạn về phạm vi an toàn.");
        }

        PhamViDuLieu phamViHieuLuc = phanQuyenService.xacDinhPhamViHieuLuc(userDTO, phamViYeuCau);
        userDTO.setPhamViHienTai(phamViHieuLuc);

        // 3. Khởi tạo và thiết lập các tiêu chí tìm kiếm & lọc (Story S3-07)
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        String boLocIdParam = request.getParameter("boLocId");

        if (boLocIdParam != null && !boLocIdParam.trim().isEmpty()) {
            // AC3: Áp dụng bộ lọc đã lưu
            try {
                long boLocId = Long.parseLong(boLocIdParam.trim());
                BoLocDaLuu daLuu = boLocService.timBoLocTheoId(boLocId, user);
                if (daLuu != null) {
                    boLoc = BoLocKhachHangDTO.tuJson(daLuu.getTieuChiJson());
                    boLoc.setBoLocId(daLuu.getId());
                    boLoc.setTenBoLoc(daLuu.getTenBoLoc());
                    boLoc.setMacDinh(daLuu.isMacDinh());
                    request.setAttribute("thongBaoThanhCong", "Đang áp dụng bộ lọc đã lưu: \"" + daLuu.getTenBoLoc() + "\"");
                }
            } catch (NumberFormatException ignored) {}
        } else {
            // Đọc các tiêu chí gửi lên từ URL query string
            String tuKhoa = request.getParameter("tuKhoa");
            String tenCongTy = request.getParameter("tenCongTy");
            String maSoThue = request.getParameter("maSoThue");
            String soDienThoai = request.getParameter("soDienThoai");
            String trangThai = request.getParameter("trangThai");
            Long nganhNgheId = parseLongOrNull(request.getParameter("nganhNgheId"));
            Long quyMoId = parseLongOrNull(request.getParameter("quyMoId"));
            Long khuVucId = parseLongOrNull(request.getParameter("khuVucId"));
            Long nguoiSoHuuId = parseLongOrNull(request.getParameter("nguoiSoHuuId"));

            boLoc.setTuKhoa(tuKhoa);
            boLoc.setTenCongTy(tenCongTy);
            boLoc.setMaSoThue(maSoThue);
            boLoc.setSoDienThoai(soDienThoai);
            boLoc.setTrangThai(trangThai);
            boLoc.setNganhNgheId(nganhNgheId);
            boLoc.setQuyMoId(quyMoId);
            boLoc.setKhuVucId(khuVucId);
            boLoc.setNguoiSoHuuId(nguoiSoHuuId);

            // Nếu người dùng không gửi điều kiện nào và không yêu cầu reset, tự động nạp bộ lọc mặc định (AC3)
            String reset = request.getParameter("reset");
            if (!boLoc.coDieuKienLoc() && reset == null) {
                BoLocDaLuu macDinh = boLocService.timBoLocMacDinh(user);
                if (macDinh != null) {
                    boLoc = BoLocKhachHangDTO.tuJson(macDinh.getTieuChiJson());
                    boLoc.setBoLocId(macDinh.getId());
                    boLoc.setTenBoLoc(macDinh.getTenBoLoc());
                    boLoc.setMacDinh(true);
                }
            }
        }

        boLoc.setPhamVi(phamViHieuLuc);

        // 4. Truy vấn danh sách khách hàng theo tiêu chí lọc và Data Scope
        List<KhachHang> dsKhachHangModel = khachHangService.timKiemVaLoc(userDTO, boLoc);

        // Duy trì danh sách BanGhiNghiepVuDTO để tương thích hoàn toàn các test suite S1-05
        String tuKhoaGoc = boLoc.getTuKhoa() != null ? boLoc.getTuKhoa() : request.getParameter("tuKhoa");
        List<BanGhiNghiepVuDTO> danhSachKhachHang = phanQuyenService.layDanhSachDuLieu(
                userDTO, phamViHieuLuc, tuKhoaGoc, "KHACH_HANG"
        );

        // 5. Xử lý Xuất Excel danh mục khách hàng (.xlsx)
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

        // 6. Nạp danh mục phụ trợ cho bộ lọc và giao diện (AC1, AC3)
        napDanhMucPhuTro(request, user);

        // 7. Thiết lập thuộc tính request
        boolean coQuyenDanhMuc = user.coVaiTro("ADMIN") || user.coVaiTro("DIRECTOR");
        request.setAttribute("coQuyenDanhMuc", coQuyenDanhMuc);
        request.setAttribute("nguoiDung", user);
        request.setAttribute("currentUser", userDTO);
        request.setAttribute("phamViHienTai", phamViHieuLuc);
        request.setAttribute("danhSachPhamViChoPhep", userDTO.getDanhSachPhamViChoPhep());
        request.setAttribute("danhSachKhachHang", danhSachKhachHang);
        request.setAttribute("dsKhachHangModel", dsKhachHangModel);
        request.setAttribute("tongSoKhachHang", dsKhachHangModel.isEmpty() ? danhSachKhachHang.size() : dsKhachHangModel.size());
        request.setAttribute("tuKhoaHienTai", boLoc.getTuKhoa() != null ? boLoc.getTuKhoa() : "");
        request.setAttribute("boLocHienTai", boLoc);

        // Danh sách bộ lọc đã lưu của người dùng hiện tại (AC3)
        List<BoLocDaLuu> dsBoLocDaLuu = boLocService.layDanhSachBoLoc(user);
        request.setAttribute("dsBoLocDaLuu", dsBoLocDaLuu);

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

        String action = request.getParameter("action");

        // Thao tác AC3: Lưu bộ lọc đã lưu
        if ("luu-bo-loc".equals(action)) {
            xuLyLuuBoLoc(request, response, user);
            return;
        }

        // Thao tác AC3: Xóa bộ lọc đã lưu
        if ("xoa-bo-loc".equals(action)) {
            xuLyXoaBoLoc(request, response, user);
            return;
        }

        // Thao tác AC3: Đặt bộ lọc làm mặc định
        if ("dat-mac-dinh".equals(action)) {
            xuLyDatMacDinh(request, response, user);
            return;
        }

        String paramId = request.getParameter("id");

        // Xử lý thao tác sửa khách hàng qua POST /khach-hang
        if (paramId != null || "sua".equals(action)) {
            Long id = null;
            try {
                if (paramId != null) id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}

            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenSua(userDTO, banGhi);

            if (!ketQua.isCoQuyen()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ketQua.getThongBao());
                request.setAttribute("banGhi", banGhi);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

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
            // Xử lý thao tác thêm mới khách hàng
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

            if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Tên công ty / khách hàng không được để trống.");
                doGet(request, response);
                return;
            }

            if (maKhachHang != null && !maKhachHang.trim().isEmpty() && phanQuyenService.kiemTraTonTaiMaKhachHang(maKhachHang.trim())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Mã khách hàng '" + maKhachHang.trim() + "' đã tồn tại trong hệ thống.");
                doGet(request, response);
                return;
            }

            try {
                // BẢO MẬT & DATA SCOPE (S1-05): Tự động gán người sở hữu từ session
                BanGhiNghiepVuDTO khachHangMoi = phanQuyenService.themKhachHang(
                        userDTO,
                        maKhachHang,
                        tenCongTy,
                        giaTri,
                        trangThai,
                        moTaChiTiet
                );

                // Đồng bộ tạo bản ghi vào KhachHangDAO
                KhachHang khModel = new KhachHang();
                khModel.setTenCongTy(tenCongTy.trim());
                khModel.setMaKhachHang(maKhachHang != null ? maKhachHang.trim() : null);
                khModel.setMaSoThue(request.getParameter("maSoThue"));
                khModel.setNganhNgheId(parseLongOrNull(request.getParameter("nganhNgheId")));
                khModel.setQuyMoId(parseLongOrNull(request.getParameter("quyMoId")));
                khModel.setKhuVucId(parseLongOrNull(request.getParameter("khuVucId")));
                khModel.setDiaChi(request.getParameter("diaChi"));
                khModel.setWebsite(request.getParameter("website"));
                khModel.setTrangThai(trangThai != null ? TrangThaiKhachHangEnum.chuanHoaMa(trangThai) : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
                khModel.setMoTaChiTiet(moTaChiTiet);
                khModel.setNguoiSoHuuId(user.getId());
                khModel.setNhomKinhDoanhId(user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null);

                try {
                    khachHangService.taoKhachHang(user, khModel);
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Ghi nhận tạo KhachHang model song song: " + ex.getMessage());
                }

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

    private void xuLyLuuBoLoc(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException, ServletException {
        String tenBoLoc = request.getParameter("tenBoLoc");
        boolean macDinh = "1".equals(request.getParameter("macDinh")) || "true".equalsIgnoreCase(request.getParameter("macDinh"));

        BoLocKhachHangDTO tieuChi = new BoLocKhachHangDTO();
        tieuChi.setTuKhoa(request.getParameter("tuKhoa"));
        tieuChi.setTenCongTy(request.getParameter("tenCongTy"));
        tieuChi.setMaSoThue(request.getParameter("maSoThue"));
        tieuChi.setSoDienThoai(request.getParameter("soDienThoai"));
        tieuChi.setTrangThai(request.getParameter("trangThai"));
        tieuChi.setNganhNgheId(parseLongOrNull(request.getParameter("nganhNgheId")));
        tieuChi.setQuyMoId(parseLongOrNull(request.getParameter("quyMoId")));
        tieuChi.setKhuVucId(parseLongOrNull(request.getParameter("khuVucId")));
        tieuChi.setNguoiSoHuuId(parseLongOrNull(request.getParameter("nguoiSoHuuId")));

        boolean isAjax = "application/json".equalsIgnoreCase(request.getHeader("Accept"))
                || "json".equalsIgnoreCase(request.getParameter("format"));

        try {
            BoLocDaLuu daLuu = boLocService.luuBoLoc(user, tenBoLoc, tieuChi, macDinh);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":true,\"message\":\"Lưu bộ lọc thành công.\",\"id\":" + daLuu.getId() + "}");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang?boLocId=" + daLuu.getId() +
                        "&thongBaoThanhCong=" + java.net.URLEncoder.encode("Đã lưu bộ lọc '" + daLuu.getTenBoLoc() + "' thành công.", "UTF-8"));
            }
        } catch (IllegalArgumentException e) {
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                request.setAttribute("thongBaoLoi", e.getMessage());
                doGet(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lưu bộ lọc: " + e.getMessage(), e);
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":false,\"message\":\"Không thể lưu bộ lọc: " + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                request.setAttribute("thongBaoLoi", "Lỗi lưu bộ lọc: " + e.getMessage());
                doGet(request, response);
            }
        }
    }

    private void xuLyXoaBoLoc(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException, ServletException {
        Long id = parseLongOrNull(request.getParameter("boLocId"));
        boolean isAjax = "application/json".equalsIgnoreCase(request.getHeader("Accept"))
                || "json".equalsIgnoreCase(request.getParameter("format"));

        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Mã bộ lọc không hợp lệ.");
            }
            boolean xoa = boLocService.xoaBoLoc(id, user);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":" + xoa + ",\"message\":\"" + (xoa ? "Đã xóa bộ lọc." : "Không thể xóa bộ lọc.") + "\"}");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang?reset=1" +
                        "&thongBaoThanhCong=" + java.net.URLEncoder.encode("Đã xóa bộ lọc thành công.", "UTF-8"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa bộ lọc: " + e.getMessage(), e);
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                request.setAttribute("thongBaoLoi", "Lỗi xóa bộ lọc: " + e.getMessage());
                doGet(request, response);
            }
        }
    }

    private void xuLyDatMacDinh(HttpServletRequest request, HttpServletResponse response, NguoiDung user)
            throws IOException, ServletException {
        Long id = parseLongOrNull(request.getParameter("boLocId"));
        boolean isAjax = "application/json".equalsIgnoreCase(request.getHeader("Accept"))
                || "json".equalsIgnoreCase(request.getParameter("format"));

        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Mã bộ lọc không hợp lệ.");
            }
            boolean ok = boLocService.datMacDinh(id, user);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":" + ok + ",\"message\":\"Đã đặt bộ lọc làm mặc định.\"}");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/khach-hang?boLocId=" + id +
                        "&thongBaoThanhCong=" + java.net.URLEncoder.encode("Đã thiết lập bộ lọc mặc định thành công.", "UTF-8"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi đặt bộ lọc mặc định: " + e.getMessage(), e);
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                request.setAttribute("thongBaoLoi", "Lỗi đặt mặc định: " + e.getMessage());
                doGet(request, response);
            }
        }
    }

    private void napDanhMucPhuTro(HttpServletRequest request, NguoiDung user) {
        try {
            // Ngành nghề & Quy mô từ danh mục bán hàng (S2-07)
            List<MucDanhMucDTO> dsNganhNghe = danhMucDAO.layDanhSach(LoaiDanhMuc.NGANH_NGHE, true);
            request.setAttribute("dsNganhNghe", dsNganhNghe);

            List<MucDanhMucDTO> dsQuyMo = danhMucDAO.layDanhSach(LoaiDanhMuc.QUY_MO, true);
            request.setAttribute("dsQuyMo", dsQuyMo);

            // Khu vực địa lý (S2-06)
            List<KhuVucDiaLy> dsKhuVuc = khuVucDAO.layTatCa();
            request.setAttribute("dsKhuVuc", dsKhuVuc);

            // Trạng thái khách hàng chuẩn
            request.setAttribute("dsTrangThai", TrangThaiKhachHangEnum.values());

            // Danh sách người dùng phụ trách / người sở hữu
            boolean coQuyenChonOwner = user != null && (user.coVaiTro(VaiTroEnum.ADMIN)
                    || user.coVaiTro(VaiTroEnum.DIRECTOR)
                    || user.coVaiTro(VaiTroEnum.TEAM_LEAD));
            request.setAttribute("coQuyenChonOwner", coQuyenChonOwner);

            if (coQuyenChonOwner) {
                Integer nhomId = null;
                if (user.coVaiTro(VaiTroEnum.TEAM_LEAD) && !user.coVaiTro(VaiTroEnum.ADMIN) && !user.coVaiTro(VaiTroEnum.DIRECTOR)) {
                    nhomId = user.getNhomKinhDoanhId();
                }
                List<NguoiDung> dsNhanVien = nguoiDungDAO.timKiemVaPhanTrang(null, nhomId, null, "HOAT_DONG", 100, 0);
                request.setAttribute("dsNguoiSoHuu", dsNhanVien);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Lỗi nạp danh mục phụ trợ: " + e.getMessage());
        }
    }

    private Long parseLongOrNull(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
