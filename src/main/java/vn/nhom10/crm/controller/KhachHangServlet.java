package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.YeuCauHoTroDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeChamSocDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.dto.ThongTinRuiRoDTO;
import vn.nhom10.crm.model.MucUuTienYeuCauEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiYeuCauEnum;
import vn.nhom10.crm.service.ChamSocKhachHangService;
import vn.nhom10.crm.service.CongTyMeConService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;
import vn.nhom10.crm.service.YeuCauHoTroService;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ trang danh mục khách hàng, trang chi tiết khách hàng và quản lý quan hệ công ty mẹ - con.
 * Tích hợp:
 * - Data Scope server-side (S1-05)
 * - Khai báo quan hệ công ty mẹ và công ty con (S3-05 AC1)
 * - Hiển thị tổng giá trị hợp đồng của cả nhóm công ty (S3-05 AC2)
 * - Ghi nhận yêu cầu hỗ trợ sau bán và theo dõi cờ rủi ro rời bỏ khách hàng (S3-08)
 * - Chăm sóc khách hàng định kỳ sau ký hợp đồng (S3-09)
 *
 * URL Patterns: /khach-hang, /khach-hang/chi-tiet, /khach-hang/cong-ty-con
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang", "/khach-hang/chi-tiet", "/khach-hang/cong-ty-con"})
public class KhachHangServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhachHangServlet.class.getName());

    private final PhanQuyenDuLieuService phanQuyenService;
    private final CongTyMeConService congTyMeConService;
    private final ChamSocKhachHangService chamSocService;

    public KhachHangServlet() {
        this(new PhanQuyenDuLieuService(), new CongTyMeConService(), new ChamSocKhachHangService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this(phanQuyenService, new CongTyMeConService(), new ChamSocKhachHangService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, CongTyMeConService congTyMeConService) {
        this(phanQuyenService, congTyMeConService, new ChamSocKhachHangService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, ChamSocKhachHangService chamSocService) {
        this(phanQuyenService, new CongTyMeConService(), chamSocService);
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, CongTyMeConService congTyMeConService, ChamSocKhachHangService chamSocService) {
        this.phanQuyenService = (phanQuyenService != null) ? phanQuyenService : new PhanQuyenDuLieuService();
        this.congTyMeConService = (congTyMeConService != null) ? congTyMeConService : new CongTyMeConService();
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

        // Nhận flash message từ session nếu có
        String flashThanhCong = (String) session.getAttribute("flashThanhCong");
        if (flashThanhCong != null) {
            request.setAttribute("thongBaoThanhCong", flashThanhCong);
            session.removeAttribute("flashThanhCong");
        }
        String flashLoi = (String) session.getAttribute("flashLoi");
        if (flashLoi != null) {
            request.setAttribute("thongBaoLoi", flashLoi);
            session.removeAttribute("flashLoi");
        }

        String paramId = request.getParameter("id");
        String servletPath = request.getServletPath();
        String viewParam = request.getParameter("view");
        String actionParam = request.getParameter("action");

        // 1. Kiểm tra quyền khi xem chi tiết khách hàng trực tiếp bằng ID (S1-05 & S3-05)
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
            if (request.getAttribute("thongBaoThanhCong") == null) {
                request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            }

            napDuLieuChiTietKhachHang(request, id, banGhi);

            // Nếu người dùng yêu cầu trang chi tiết (/khach-hang/chi-tiet hoặc view=chi-tiet hoặc action=chi-tiet hoặc chiTiet=true)
            if ("/khach-hang/chi-tiet".equals(servletPath) || "chi-tiet".equalsIgnoreCase(viewParam)
                    || "chi-tiet".equalsIgnoreCase(actionParam) || "true".equalsIgnoreCase(request.getParameter("chiTiet"))) {
                request.setAttribute("currentUser", userDTO);
                response.setStatus(HttpServletResponse.SC_OK);
                request.getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp").forward(request, response);
                return;
            }
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

        // Gắn cờ rủi ro rời bỏ và số yêu cầu chưa xử lý cho từng khách hàng (Story S3-08 AC3)
        try {
            KhachHangDAO khDao = new KhachHangDAO();
            YeuCauHoTroDAO ychtDao = new YeuCauHoTroDAO();
            for (BanGhiNghiepVuDTO khDto : danhSachKhachHang) {
                if (khDto.getId() != null) {
                    boolean coRuiRo = khDao.kiemTraCoRuiRo(khDto.getId());
                    khDto.setCoRuiRo(coRuiRo);
                    if (coRuiRo) {
                        khDto.setSoYeuCauChuaXuLy(ychtDao.demYeuCauChuaXuLy(khDto.getId()));
                    }
                }
            }
        } catch (Exception ignored) {}

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

        // STORY S3-05 AC1: Gắn một khách hàng làm công ty con của khách hàng khác
        if ("gan-cong-ty-con".equals(action)) {
            xuLyGanCongTyCon(request, response, userDTO);
            return;
        }

        // STORY S3-05 AC1: Gỡ bỏ quan hệ công ty con
        if ("go-cong-ty-con".equals(action)) {
            xuLyGoCongTyCon(request, response, userDTO);
            return;
        }

        // STORY S3-05: Cập nhật công ty mẹ cho một khách hàng
        if ("cap-nhat-cong-ty-me".equals(action)) {
            xuLyCapNhatCongTyMe(request, response, userDTO);
            return;
        }

        // STORY S3-09: Đánh dấu đã liên hệ ngay trên danh sách (AC3)
        if ("danhDauLienHe".equals(action)) {
            xuLyDanhDauLienHe(request, response, userDTO);
            return;
        }

        // Xử lý thao tác sửa khách hàng qua POST /khach-hang (chặn sửa ngoài phạm vi)
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
        } else if ("them".equals(action) || "create".equals(action) || paramId == null) {
            // Xử lý thao tác thêm mới khách hàng (Story S1-05)
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

            // BẢO MẬT & DATA SCOPE (S1-05): Người sở hữu quyết định bởi server từ session người dùng
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

    /**
     * Nạp dữ liệu chi tiết khách hàng gồm: thống kê công ty mẹ-con (S3-05) và thông tin cờ rủi ro / ticket hỗ trợ (S3-08).
     */
    private void napDuLieuChiTietKhachHang(HttpServletRequest request, Long id, BanGhiNghiepVuDTO banGhi) {
        if (id == null) return;

        // Story S3-05: Nạp dữ liệu thống kê nhóm công ty & danh sách công ty con
        try {
            ThongKeNhomCongTyDTO thongKeNhom = congTyMeConService.layThongKeNhomCongTy(id);
            request.setAttribute("thongKeNhomCongTy", thongKeNhom);
            request.setAttribute("dsKhaDungLamCon", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyCon(id));
            request.setAttribute("dsKhaDungLamMe", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyMe(id));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể tải số liệu nhóm công ty cho ID " + id + ": " + e.getMessage());
        }

        // Story S3-08: Nạp thông tin rủi ro & danh sách yêu cầu hỗ trợ
        try {
            YeuCauHoTroService ychtService = new YeuCauHoTroService();
            ThongTinRuiRoDTO ruiRo = ychtService.layThongTinRuiRo(id);
            if (ruiRo != null) {
                if (banGhi != null) {
                    banGhi.setCoRuiRo(ruiRo.isCoRuiRo());
                    banGhi.setSoYeuCauChuaXuLy(ruiRo.getSoYeuCauChuaXuLy());
                }
                request.setAttribute("thongTinRuiRo", ruiRo);
            }
            request.setAttribute("danhSachYeuCauHoTro", ychtService.layDanhSachTheoKhachHang(id));
            request.setAttribute("danhSachNhanVien", new NguoiDungDAO().layTatCa());
            request.setAttribute("mucUuTienList", MucUuTienYeuCauEnum.values());
            request.setAttribute("trangThaiList", TrangThaiYeuCauEnum.values());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể tải dữ liệu hỗ trợ & rủi ro cho ID " + id + ": " + e.getMessage());
        }
    }

    /**
     * Xử lý gắn khách hàng làm công ty con của khách hàng khác (Story S3-05 AC1).
     */
    private void xuLyGanCongTyCon(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO userDTO)
            throws ServletException, IOException {
        String congTyMeIdStr = request.getParameter("congTyMeId");
        String congTyConIdStr = request.getParameter("congTyConId");

        Long congTyMeId = parseLong(congTyMeIdStr);
        Long congTyConId = parseLong(congTyConIdStr);

        try {
            congTyMeConService.ganCongTyCon(congTyConId, congTyMeId, userDTO);
            request.getSession().setAttribute("flashThanhCong", "Gắn khách hàng làm công ty con thành công.");
            response.sendRedirect(request.getContextPath() + "/khach-hang/chi-tiet?id=" + congTyMeId);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("thongBaoLoi", e.getMessage());
            chuyenHuongChiTiet(request, response, congTyMeId, userDTO);
        } catch (LoiKhongTimThayException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (LoiPhanQuyenException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("thongBaoLoi", e.getMessage());
            request.setAttribute("currentUser", userDTO);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi gán công ty con: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("thongBaoLoi", "Lỗi hệ thống khi gán công ty con: " + e.getMessage());
            chuyenHuongChiTiet(request, response, congTyMeId, userDTO);
        }
    }

    /**
     * Xử lý gỡ bỏ quan hệ công ty con (Story S3-05 AC1).
     */
    private void xuLyGoCongTyCon(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO userDTO)
            throws ServletException, IOException {
        String congTyConIdStr = request.getParameter("congTyConId");
        String congTyMeIdStr = request.getParameter("congTyMeId");

        Long congTyConId = parseLong(congTyConIdStr);
        Long congTyMeId = parseLong(congTyMeIdStr);

        try {
            congTyMeConService.goCongTyCon(congTyConId, userDTO);
            request.getSession().setAttribute("flashThanhCong", "Đã gỡ bỏ quan hệ công ty con thành công.");
            Long idDieuHuong = congTyMeId != null ? congTyMeId : congTyConId;
            response.sendRedirect(request.getContextPath() + "/khach-hang/chi-tiet?id=" + idDieuHuong);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("thongBaoLoi", e.getMessage());
            chuyenHuongChiTiet(request, response, congTyMeId, userDTO);
        } catch (LoiKhongTimThayException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (LoiPhanQuyenException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("thongBaoLoi", e.getMessage());
            request.setAttribute("currentUser", userDTO);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi gỡ bỏ công ty con: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("thongBaoLoi", "Lỗi hệ thống khi gỡ bỏ công ty con: " + e.getMessage());
            chuyenHuongChiTiet(request, response, congTyMeId, userDTO);
        }
    }

    /**
     * Xử lý cập nhật công ty mẹ từ trang của khách hàng con (Story S3-05).
     */
    private void xuLyCapNhatCongTyMe(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO userDTO)
            throws ServletException, IOException {
        String khachHangIdStr = request.getParameter("khachHangId");
        String congTyMeIdStr = request.getParameter("congTyMeId");

        Long khachHangId = parseLong(khachHangIdStr);
        Long congTyMeId = parseLong(congTyMeIdStr);

        try {
            if (congTyMeId == null || congTyMeId <= 0) {
                congTyMeConService.goCongTyCon(khachHangId, userDTO);
                request.getSession().setAttribute("flashThanhCong", "Đã hủy liên kết công ty mẹ thành công.");
            } else {
                congTyMeConService.ganCongTyCon(khachHangId, congTyMeId, userDTO);
                request.getSession().setAttribute("flashThanhCong", "Cập nhật công ty mẹ thành công.");
            }
            response.sendRedirect(request.getContextPath() + "/khach-hang/chi-tiet?id=" + khachHangId);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("thongBaoLoi", e.getMessage());
            chuyenHuongChiTiet(request, response, khachHangId, userDTO);
        } catch (LoiKhongTimThayException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (LoiPhanQuyenException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("thongBaoLoi", e.getMessage());
            request.setAttribute("currentUser", userDTO);
            request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật công ty mẹ: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("thongBaoLoi", "Lỗi hệ thống: " + e.getMessage());
            chuyenHuongChiTiet(request, response, khachHangId, userDTO);
        }
    }

    /**
     * Xử lý đánh dấu đã liên hệ chăm sóc khách hàng (Story S3-09 AC3).
     */
    private void xuLyDanhDauLienHe(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO userDTO)
            throws ServletException, IOException {
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
            doGet(request, response);
        } catch (LoiPhanQuyenException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
                return;
            }
            request.setAttribute("thongBaoLoi", ex.getMessage());
            doGet(request, response);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Lỗi ghi nhận liên hệ: " + escapeJson(ex.getMessage()) + "\"}");
                return;
            }
            request.setAttribute("thongBaoLoi", "Lỗi ghi nhận liên hệ: " + ex.getMessage());
            doGet(request, response);
        }
    }

    private void chuyenHuongChiTiet(HttpServletRequest request, HttpServletResponse response, Long id, NguoiDungDTO userDTO)
            throws ServletException, IOException {
        if (id != null) {
            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            request.setAttribute("banGhi", banGhi);
            request.setAttribute("banGhiChiTiet", banGhi);
            napDuLieuChiTietKhachHang(request, id, banGhi);
        }
        request.setAttribute("currentUser", userDTO);
        request.getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp").forward(request, response);
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
