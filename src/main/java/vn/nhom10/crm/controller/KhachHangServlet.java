package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.KhachHang360DAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.KhachHang360DTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.dto.KhachHangChamSocDTO;
import vn.nhom10.crm.dto.ThongKeChamSocDTO;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;
import vn.nhom10.crm.dto.ThongTinRuiRoDTO;
import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.MucUuTienYeuCauEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.model.TrangThaiYeuCauEnum;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.ChamSocKhachHangService;
import vn.nhom10.crm.service.CongTyMeConService;
import vn.nhom10.crm.service.DanhMucBanHangService;
import vn.nhom10.crm.service.KhachHang360Service;
import vn.nhom10.crm.service.KhachHangService;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;
import vn.nhom10.crm.service.NguoiLienHeService;

import vn.nhom10.crm.service.PhanQuyenDuLieuService;
import vn.nhom10.crm.service.YeuCauHoTroService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller xử lý các tác vụ quản lý khách hàng:
 * - Quản lý hồ sơ doanh nghiệp & Data Scope server-side (Story S3-01)
 * - Trang 360° khách hàng (Story S3-03)
 * - Quản lý quan hệ công ty mẹ - con (Story S3-05)
 * - Cảnh báo cờ rủi ro rời bỏ & dịch vụ sau bán (Story S3-08)
 * - Quản lý chăm sóc định kỳ sau ký hợp đồng (Story S3-09)
 *
 * URL Patterns: /khach-hang, /khach-hang/chi-tiet, /khach-hang/cong-ty-con, /khach-hang/360
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang", "/khach-hang/chi-tiet", "/khach-hang/cong-ty-con", "/khach-hang/360"})
public class KhachHangServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhachHangServlet.class.getName());

    private final PhanQuyenDuLieuService phanQuyenService;
    private final CongTyMeConService congTyMeConService;
    private final ChamSocKhachHangService chamSocService;
    private final KhachHang360Service khachHang360Service;
    private final KhachHangService khachHangService;
    private final DanhMucBanHangService danhMucBanHangService;
    private final NguoiDungDAO nguoiDungDAO;
    private final NguoiLienHeService nguoiLienHeService;

    public KhachHangServlet() {
        this(new PhanQuyenDuLieuService(), new CongTyMeConService(), new ChamSocKhachHangService(),
                new KhachHang360Service(), new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this(phanQuyenService, new CongTyMeConService(), new ChamSocKhachHangService(),
                new KhachHang360Service(), new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, KhachHangService khachHangService) {
        this(phanQuyenService, new CongTyMeConService(), new ChamSocKhachHangService(),
                new KhachHang360Service(), khachHangService, new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, CongTyMeConService congTyMeConService) {
        this(phanQuyenService, congTyMeConService, new ChamSocKhachHangService(),
                new KhachHang360Service(), new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, ChamSocKhachHangService chamSocService) {
        this(phanQuyenService, new CongTyMeConService(), chamSocService,
                new KhachHang360Service(), new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, KhachHang360Service khachHang360Service) {
        this(phanQuyenService, new CongTyMeConService(), new ChamSocKhachHangService(),
                khachHang360Service, new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            CongTyMeConService congTyMeConService,
                            ChamSocKhachHangService chamSocService) {
        this(phanQuyenService, congTyMeConService, chamSocService,
                new KhachHang360Service(), new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            CongTyMeConService congTyMeConService,
                            ChamSocKhachHangService chamSocService,
                            KhachHang360Service khachHang360Service) {
        this(phanQuyenService, congTyMeConService, chamSocService,
                khachHang360Service, new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO(), new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            KhachHangService khachHangService,
                            DanhMucBanHangService danhMucBanHangService,
                            NguoiDungDAO nguoiDungDAO) {
        this(phanQuyenService, new CongTyMeConService(), new ChamSocKhachHangService(),
                new KhachHang360Service(), khachHangService, danhMucBanHangService, nguoiDungDAO, new NguoiLienHeService());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            KhachHangService khachHangService,
                            DanhMucBanHangService danhMucBanHangService,
                            NguoiDungDAO nguoiDungDAO,
                            NguoiLienHeService nguoiLienHeService) {
        this(phanQuyenService, new CongTyMeConService(), new ChamSocKhachHangService(),
                new KhachHang360Service(), khachHangService, danhMucBanHangService, nguoiDungDAO, nguoiLienHeService);
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            CongTyMeConService congTyMeConService,
                            ChamSocKhachHangService chamSocService,
                            KhachHang360Service khachHang360Service,
                            KhachHangService khachHangService,
                            DanhMucBanHangService danhMucBanHangService,
                            NguoiDungDAO nguoiDungDAO,
                            NguoiLienHeService nguoiLienHeService) {
        this.phanQuyenService = (phanQuyenService != null) ? phanQuyenService : new PhanQuyenDuLieuService();
        this.congTyMeConService = (congTyMeConService != null) ? congTyMeConService : new CongTyMeConService();
        this.chamSocService = (chamSocService != null) ? chamSocService : new ChamSocKhachHangService();
        this.khachHang360Service = (khachHang360Service != null) ? khachHang360Service : new KhachHang360Service(new KhachHang360DAO(), this.phanQuyenService);
        this.khachHangService = (khachHangService != null) ? khachHangService : new KhachHangService();
        this.danhMucBanHangService = (danhMucBanHangService != null) ? danhMucBanHangService : new DanhMucBanHangService();
        this.nguoiDungDAO = (nguoiDungDAO != null) ? nguoiDungDAO : new NguoiDungDAO();
        this.nguoiLienHeService = (nguoiLienHeService != null) ? nguoiLienHeService : new NguoiLienHeService();
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

        String servletPath = request.getServletPath();
        String action = request.getParameter("action");
        String paramId = request.getParameter("id");

        // 1. STORY S3-03: API lấy danh sách hoạt động phân trang dạng JSON (Bảo vệ bằng Data Scope)
        if ("api-hoat-dong".equalsIgnoreCase(action)) {
            response.setContentType("application/json;charset=UTF-8");
            Long id = null;
            if (paramId != null && !paramId.trim().isEmpty()) {
                try { id = Long.parseLong(paramId.trim()); } catch (NumberFormatException ignored) {}
            }
            if (id == null) {
                String khIdParam = request.getParameter("khachHangId");
                if (khIdParam != null && !khIdParam.trim().isEmpty()) {
                    try { id = Long.parseLong(khIdParam.trim()); } catch (NumberFormatException ignored) {}
                }
            }
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("[]");
                return;
            }

            // BẢO VỆ BẰNG DATA SCOPE SERVER-SIDE: Không cho truy cập khách ngoài phạm vi
            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            if (banGhi == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("[]");
                return;
            }
            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(userDTO, banGhi);
            if (!ketQua.isCoQuyen()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("[]");
                return;
            }

            int limit = 50;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                try { limit = Integer.parseInt(limitParam.trim()); } catch (NumberFormatException ignored) {}
            }
            List<HoatDong> ds = khachHang360Service.layDsHoatDong(id, limit, 0);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(chuyenDsHoatDongSangJson(ds));
            return;
        }

        // 2. Xác định yêu cầu trang chi tiết hoặc 360
        boolean isChiTietEndpoint = "/khach-hang/chi-tiet".equals(servletPath)
                || "/khach-hang/360".equals(servletPath)
                || "chi-tiet".equalsIgnoreCase(action)
                || "360".equalsIgnoreCase(action);

        Long id = parseLong(paramId);

        // Chặn truy cập xem chi tiết nếu thiếu ID
        if (id == null && isChiTietEndpoint) {
            response.sendRedirect(request.getContextPath() + "/khach-hang");
            return;
        }

        // Kiểm tra quyền khi xem chi tiết khách hàng bằng ID (Story S3-01 & S3-03)
        if (id != null) {
            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            KhachHang khachHang = null;
            try {
                khachHang = khachHangService.timTheoIdVaKiemTraQuyen(id, user);
            } catch (SecurityException se) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", se.getMessage());
                request.setAttribute("banGhi", banGhi);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi kiểm tra quyền khách hàng: " + e.getMessage());
            }

            if (banGhi == null && khachHang == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy khách hàng.");
                return;
            }

            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(userDTO, banGhi);
            if (banGhi != null && !ketQua.isCoQuyen()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ketQua.getThongBao());
                request.setAttribute("banGhi", banGhi);
                request.setAttribute("khachHang", khachHang);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

            // Gán thông tin khách hàng chi tiết
            request.setAttribute("banGhiChiTiet", banGhi);
            request.setAttribute("khachHangChiTiet", khachHang);
            request.setAttribute("banGhi", banGhi);
            request.setAttribute("khachHang", khachHang != null ? khachHang : banGhi);
            request.setAttribute("currentUser", userDTO);

            // Nạp dữ liệu bổ trợ chi tiết: Nhóm công ty (S3-05), Yêu cầu hỗ trợ & rủi ro (S3-08)
            napDuLieuChiTietKhachHang(request, id, banGhi);

            // Nạp dữ liệu 360 (Story S3-03)
            try {
                KhachHang360DTO khachHang360 = khachHang360Service.layThongTin360(id, userDTO);
                if (khachHang360 != null) {
                    request.setAttribute("khachHang360", khachHang360);
                    if (khachHang360.getKhachHang() != null) {
                        request.setAttribute("khachHang", khachHang360.getKhachHang());
                    }
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
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Không thể tải dữ liệu 360 cho ID " + id + ": " + e.getMessage());
            }

            // Lấy danh sách người liên hệ của khách hàng này (Story S3-02 AC1, AC2, AC3)
            try {
                List<NguoiLienHe> dsNguoiLienHe = nguoiLienHeService.layDanhSachTheoKhachHang(user, id);
                if (dsNguoiLienHe != null && !dsNguoiLienHe.isEmpty()) {
                    request.setAttribute("dsNguoiLienHe", dsNguoiLienHe);
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi nạp danh sách người liên hệ: " + e.getMessage());
            }

            request.setAttribute("dsVaiTroQuyetDinh", VaiTroQuyetDinhEnum.values());

            // Nạp danh sách khách hàng để hỗ trợ chọn công ty chuyển đến (Story S3-02 AC4)
            try {
                List<KhachHang> dsKhachChuyen = khachHangService.layDanhSachTheoQuyen(user, userDTO.getPhamViHienTai(), null, null, null, null, 1, 100);
                request.setAttribute("dsKhachHangChuyen", dsKhachChuyen);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi lấy ds khách chuyển: " + e.getMessage());
            }

            // Nếu người dùng yêu cầu trang chi tiết trực tiếp, forward về chi-tiet.jsp
            String viewParam = request.getParameter("view");
            if (isChiTietEndpoint
                    || "/khach-hang/cong-ty-con".equals(servletPath)
                    || "360".equalsIgnoreCase(viewParam)
                    || "chi-tiet".equalsIgnoreCase(viewParam)) {
                napDanhMucBaoTro(request, user);
                response.setStatus(HttpServletResponse.SC_OK);
                request.getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp").forward(request, response);
                return;
            }
        }

        // 3. TIẾP NHẬN BỘ LỌC DATA SCOPE & PHÂN TRANG DANH SÁCH KHÁCH HÀNG
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

        String tuKhoa = request.getParameter("tuKhoa");
        String trangThai = request.getParameter("trangThai");
        Long nganhNgheId = parseLong(request.getParameter("nganhNgheId"));
        Long quyMoId = parseLong(request.getParameter("quyMoId"));

        int trang = 1;
        try {
            String pTrang = request.getParameter("trang");
            if (pTrang != null && !pTrang.trim().isEmpty()) {
                trang = Math.max(1, Integer.parseInt(pTrang.trim()));
            }
        } catch (NumberFormatException ignored) {}

        int kichThuocTrang = 20;

        // Tự động lọc danh sách khách hàng DTO (Story S1-05)
        List<BanGhiNghiepVuDTO> danhSachKhachHang = phanQuyenService.layDanhSachDuLieu(
                userDTO, phamViHieuLuc, tuKhoa, "KHACH_HANG"
        );

        // Truy vấn danh sách khách hàng Model (Story S3-01)
        List<KhachHang> danhSachKhachHangModel = new ArrayList<>();
        int tongSoKhachHangModel = 0;
        try {
            danhSachKhachHangModel = khachHangService.layDanhSachTheoQuyen(
                    user, phamViHieuLuc, tuKhoa, trangThai, nganhNgheId, quyMoId, trang, kichThuocTrang
            );
            tongSoKhachHangModel = khachHangService.demTongSoTheoQuyen(
                    user, phamViHieuLuc, tuKhoa, trangThai, nganhNgheId, quyMoId
            );
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "Lỗi truy vấn danh sách khách hàng từ database: " + e.getMessage());
        }

        // 4. XỬ LÝ XUẤT EXCEL DANH MỤC KHÁCH HÀNG (.XLSX)
        String xuatExcel = request.getParameter("xuatExcel");
        if ("true".equalsIgnoreCase(xuatExcel)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"du-lieu-khach-hang-" + phamViHieuLuc.getMa().toLowerCase() + ".xlsx\"");
            byte[] excelBytes;
            if (danhSachKhachHangModel != null && !danhSachKhachHangModel.isEmpty()) {
                excelBytes = khachHangService.xuatDanhSachExcel(danhSachKhachHangModel);
            } else {
                excelBytes = phanQuyenService.xuatDuLieuExcel(danhSachKhachHang);
            }
            response.setContentLength(excelBytes.length);
            try (java.io.OutputStream os = response.getOutputStream()) {
                os.write(excelBytes);
                os.flush();
            }
            return;
        }

        // 5. NGHIỆP VỤ CHĂM SÓC KHÁCH HÀNG ĐỊNH KỲ (Story S3-09)
        String paramTab = request.getParameter("tab");
        String paramSoNgay = request.getParameter("soNgay");
        int soNgay = chamSocService.laySoNgayCauHinh(paramSoNgay);

        boolean laChamSocKhachHang = user != null && user.coVaiTro("CUST_SUCCESS");
        String tabHienTai = (paramTab != null && !paramTab.trim().isEmpty())
                ? paramTab.trim()
                : (laChamSocKhachHang ? "cham-soc" : "tat-ca");

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

        // 6. NẠP DANH MỤC BỔ TRỢ FORM VÀ BỘ LỌC (STORY S3-01)
        napDanhMucBaoTro(request, user);

        boolean coQuyenDanhMuc = user != null && (user.coVaiTro("ADMIN") || user.coVaiTro("DIRECTOR"));
        request.setAttribute("coQuyenDanhMuc", coQuyenDanhMuc);
        request.setAttribute("nguoiDung", user);
        request.setAttribute("currentUser", userDTO);
        request.setAttribute("phamViHienTai", phamViHieuLuc);
        request.setAttribute("danhSachPhamViChoPhep", userDTO.getDanhSachPhamViChoPhep());
        request.setAttribute("danhSachKhachHang", danhSachKhachHang);
        request.setAttribute("danhSachKhachHangModel", danhSachKhachHangModel);
        request.setAttribute("tongSoKhachHang", !danhSachKhachHangModel.isEmpty() ? tongSoKhachHangModel : danhSachKhachHang.size());
        request.setAttribute("tuKhoaHienTai", tuKhoa != null ? tuKhoa : "");
        request.setAttribute("trangThaiHienTai", trangThai != null ? trangThai : "");
        request.setAttribute("nganhNgheIdHienTai", nganhNgheId);
        request.setAttribute("quyMoIdHienTai", quyMoId);
        request.setAttribute("trangHienTai", trang);
        request.setAttribute("kichThuocTrang", kichThuocTrang);

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
        String paramId = request.getParameter("id");

        // 1. STORY S3-05: Gắn công ty con
        if ("gan-cong-ty-con".equals(action)) {
            xuLyGanCongTyCon(request, response, userDTO);
            return;
        }

        // 2. STORY S3-05: Gỡ công ty con
        if ("go-cong-ty-con".equals(action)) {
            xuLyGoCongTyCon(request, response, userDTO);
            return;
        }

        // 3. STORY S3-05: Cập nhật công ty mẹ
        if ("cap-nhat-cong-ty-me".equals(action)) {
            xuLyCapNhatCongTyMe(request, response, userDTO);
            return;
        }

        // 4. STORY S3-09: Đánh dấu đã liên hệ chăm sóc khách hàng
        if ("danhDauLienHe".equals(action)) {
            xuLyDanhDauLienHe(request, response, userDTO);
            return;
        }

        // 5. STORY S3-03: Ghi nhận hoạt động nhanh trên trang 360 (Bảo vệ bằng Data Scope)
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

            // BẢO MẬT & DATA SCOPE: Chặn ghi hoạt động vào khách ngoài phạm vi
            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(khId, "KHACH_HANG");
            if (banGhi == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy khách hàng.\"}");
                return;
            }
            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(userDTO, banGhi);
            if (!ketQua.isCoQuyen()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\":false,\"message\":\"Từ chối thao tác: Khách hàng nằm ngoài phạm vi phân quyền của bạn.\"}");
                return;
            }

            String loai = request.getParameter("loaiHoatDong");
            if (loai == null || loai.isBlank()) loai = request.getParameter("loai");
            String tieuDe = request.getParameter("tieuDe");
            String noiDung = request.getParameter("noiDung");
            if (noiDung == null) noiDung = request.getParameter("moTa");

            if (tieuDe == null || tieuDe.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Tiêu đề hoạt động không được để trống.\"}");
                return;
            }

            try {
                HoatDong hd = khachHang360Service.themHoatDong(
                        khId,
                        user != null ? user.getId() : (userDTO != null ? userDTO.getId() : null),
                        (user != null && user.getNhomKinhDoanhId() != null) ? (long) user.getNhomKinhDoanhId() : (userDTO != null && userDTO.getNhomKinhDoanhId() != null ? Long.valueOf(userDTO.getNhomKinhDoanhId()) : null),
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

        // 6. XỬ LÝ THAO TÁC SỬA KHÁCH HÀNG (STORY S3-01 & S1-05)
        if ("sua".equals(action) || (paramId != null && !"danhDauLienHe".equals(action))) {
            Long id = parseLong(paramId);

            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenSua(userDTO, banGhi);

            if (banGhi != null && !ketQua.isCoQuyen()) {
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
            String maSoThueMoi = request.getParameter("maSoThue");
            Long nganhNgheId = parseLong(request.getParameter("nganhNgheId"));
            Long quyMoId = parseLong(request.getParameter("quyMoId"));
            String website = request.getParameter("website");
            String diaChi = request.getParameter("diaChi");
            Long nguoiSoHuuId = parseLong(request.getParameter("nguoiSoHuuId"));

            if (tieuDeMoi == null || tieuDeMoi.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Tên công ty / khách hàng không được để trống.");
                doGet(request, response);
                return;
            }

            if (trangThaiMoi != null && !trangThaiMoi.trim().isEmpty() && !TrangThaiKhachHangEnum.laHopLe(trangThaiMoi)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Trạng thái khách hàng không hợp lệ. Chỉ chấp nhận: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác.");
                doGet(request, response);
                return;
            }

            KhachHang khUpdate = new KhachHang();
            khUpdate.setId(id);
            khUpdate.setTenCongTy(tieuDeMoi.trim());
            khUpdate.setMaSoThue(maSoThueMoi != null ? maSoThueMoi.trim() : null);
            khUpdate.setNganhNgheId(nganhNgheId);
            khUpdate.setQuyMoId(quyMoId);
            khUpdate.setWebsite(website != null ? website.trim() : null);
            khUpdate.setDiaChi(diaChi != null ? diaChi.trim() : null);
            khUpdate.setNguoiSoHuuId(nguoiSoHuuId);
            khUpdate.setDoanhThuUocTinh(parseBigDecimal(giaTriMoi));
            khUpdate.setTrangThai(trangThaiMoi != null ? TrangThaiKhachHangEnum.chuanHoaMa(trangThaiMoi) : null);
            khUpdate.setMoTaChiTiet(moTaMoi != null ? moTaMoi.trim() : null);

            try {
                khachHangService.capNhatKhachHang(user, khUpdate);
            } catch (SecurityException se) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", se.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            } catch (IllegalArgumentException iae) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", iae.getMessage());
                doGet(request, response);
                return;
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi cập nhật KhachHang service: " + e.getMessage());
            }

            if (banGhi != null) {
                banGhi.setTieuDe(tieuDeMoi.trim());
                if (giaTriMoi != null) banGhi.setGiaTri(giaTriMoi.trim());
                if (trangThaiMoi != null) banGhi.setTrangThai(TrangThaiKhachHangEnum.chuanHoaTen(trangThaiMoi));
                if (moTaMoi != null) banGhi.setMoTaChiTiet(moTaMoi.trim());
                phanQuyenService.capNhatBanGhi(banGhi);
            }

            request.setAttribute("thongBaoThanhCong", "Cập nhật dữ liệu khách hàng thành công.");
        } else if ("them".equals(action) || "create".equals(action) || paramId == null) {
            // 7. XỬ LÝ THAO TÁC THÊM MỚI KHÁCH HÀNG (STORY S3-01 & S1-05)
            String tenCongTy = request.getParameter("tenCongTy");
            if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
                tenCongTy = request.getParameter("tieuDe");
            }

            String maKhachHang = request.getParameter("maKhachHang");
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                maKhachHang = request.getParameter("maBanGhi");
            }

            String maSoThue = request.getParameter("maSoThue");
            Long nganhNgheId = parseLong(request.getParameter("nganhNgheId"));
            Long quyMoId = parseLong(request.getParameter("quyMoId"));
            String website = request.getParameter("website");
            String diaChi = request.getParameter("diaChi");

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

            if (trangThai != null && !trangThai.trim().isEmpty() && !TrangThaiKhachHangEnum.laHopLe(trangThai)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Trạng thái khách hàng không hợp lệ. Chỉ chấp nhận: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác.");
                doGet(request, response);
                return;
            }

            if (maKhachHang != null && !maKhachHang.trim().isEmpty() && phanQuyenService.kiemTraTonTaiMaKhachHang(maKhachHang.trim())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Mã khách hàng '" + maKhachHang.trim() + "' đã tồn tại trong hệ thống.");
                doGet(request, response);
                return;
            }

            KhachHang khachHangMoi = new KhachHang();
            khachHangMoi.setTenCongTy(tenCongTy.trim());
            khachHangMoi.setMaKhachHang(maKhachHang != null ? maKhachHang.trim() : null);
            khachHangMoi.setMaSoThue(maSoThue != null ? maSoThue.trim() : null);
            khachHangMoi.setNganhNgheId(nganhNgheId);
            khachHangMoi.setQuyMoId(quyMoId);
            khachHangMoi.setWebsite(website != null ? website.trim() : null);
            khachHangMoi.setDiaChi(diaChi != null ? diaChi.trim() : null);
            khachHangMoi.setDoanhThuUocTinh(parseBigDecimal(giaTri));
            khachHangMoi.setTrangThai(trangThai != null ? TrangThaiKhachHangEnum.chuanHoaMa(trangThai) : TrangThaiKhachHangEnum.TIEM_NANG.getMa());
            khachHangMoi.setMoTaChiTiet(moTaChiTiet != null ? moTaChiTiet.trim() : null);

            // Chống owner spoofing (AC4)
            Long paramNguoiSoHuu = parseLong(request.getParameter("nguoiSoHuuId"));
            if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro(VaiTroEnum.DIRECTOR) || user.coVaiTro(VaiTroEnum.TEAM_LEAD)) {
                khachHangMoi.setNguoiSoHuuId(paramNguoiSoHuu != null ? paramNguoiSoHuu : user.getId());
            } else {
                khachHangMoi.setNguoiSoHuuId(user.getId());
                khachHangMoi.setNhomKinhDoanhId(user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null);
            }

            try {
                KhachHang khLuu = khachHangService.taoKhachHang(user, khachHangMoi);

                BanGhiNghiepVuDTO bgMoi = phanQuyenService.themKhachHang(
                        userDTO,
                        khLuu != null && khLuu.getMaKhachHang() != null ? khLuu.getMaKhachHang() : maKhachHang,
                        tenCongTy,
                        giaTri,
                        TrangThaiKhachHangEnum.chuanHoaTen(trangThai),
                        moTaChiTiet
                );

                request.setAttribute("thongBaoThanhCong", "Thêm mới khách hàng '" + tenCongTy.trim() + "' thành công.");
                request.setAttribute("khachHangVuaThem", bgMoi);
                request.setAttribute("khachHangMoi", khLuu);
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

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH HỖ TRỢ XỬ LÝ CHI TIẾT & MODAL
    // =========================================================================

    private void napDuLieuChiTietKhachHang(HttpServletRequest request, Long id, BanGhiNghiepVuDTO banGhi) {
        if (id == null) return;

        try {
            ThongKeNhomCongTyDTO thongKeNhom = congTyMeConService.layThongKeNhomCongTy(id);
            request.setAttribute("thongKeNhomCongTy", thongKeNhom);
            request.setAttribute("dsKhaDungLamCon", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyCon(id));
            request.setAttribute("dsKhaDungLamMe", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyMe(id));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể tải số liệu nhóm công ty cho ID " + id + ": " + e.getMessage());
        }

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

    private void xuLyDanhDauLienHe(HttpServletRequest request, HttpServletResponse response, NguoiDungDTO userDTO)
            throws ServletException, IOException {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

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

        String paramKhachHangId = request.getParameter("khachHangId");
        Long khachHangId = parseLong(paramKhachHangId);

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
        String kenhLienHe = request.getParameter("kenhLyeHe");
        if (kenhLienHe == null || kenhLienHe.isBlank()) {
            kenhLienHe = request.getParameter("kenhLienHe");
        }
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
            } else {
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ex.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu liên hệ: " + ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Lỗi hệ thống: " + escapeJson(ex.getMessage()) + "\"}");
            } else {
                request.setAttribute("thongBaoLoi", "Lỗi hệ thống: " + ex.getMessage());
                doGet(request, response);
            }
        }
    }

    private void napDanhMucBaoTro(HttpServletRequest request, NguoiDung user) {
        try {
            List<MucDanhMucDTO> dsNganhNghe = danhMucBanHangService.layDanhSachTheoLoai(LoaiDanhMuc.NGANH_NGHE);
            request.setAttribute("dsNganhNghe", dsNganhNghe);

            List<MucDanhMucDTO> dsQuyMo = danhMucBanHangService.layDanhSachTheoLoai(LoaiDanhMuc.QUY_MO);
            request.setAttribute("dsQuyMo", dsQuyMo);

            request.setAttribute("dsTrangThaiKhachHang", TrangThaiKhachHangEnum.values());

            boolean coQuyenChonOwner = user != null && (user.coVaiTro(VaiTroEnum.ADMIN)
                    || user.coVaiTro(VaiTroEnum.DIRECTOR)
                    || user.coVaiTro(VaiTroEnum.TEAM_LEAD));
            request.setAttribute("coQuyenChonOwner", coQuyenChonOwner);

            if (coQuyenChonOwner && nguoiDungDAO != null) {
                Integer nhomIdFilter = null;
                if (user.coVaiTro(VaiTroEnum.TEAM_LEAD) && !user.coVaiTro(VaiTroEnum.ADMIN) && !user.coVaiTro(VaiTroEnum.DIRECTOR)) {
                    nhomIdFilter = user.getNhomKinhDoanhId();
                }
                List<NguoiDung> dsNhanVien = nguoiDungDAO.timKiemVaPhanTrang(null, nhomIdFilter, null, "HOAT_DONG", 100, 0);
                request.setAttribute("dsNhanVienSoHuu", dsNhanVien);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Lỗi nạp danh mục phụ trợ khách hàng: " + e.getMessage());
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

    private Long parseLongOrNull(String val) {
        return parseLong(val);
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

    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String clean = str.replaceAll("[^0-9.]", "");
            if (clean.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String chuyenDsHoatDongSangJson(List<HoatDong> ds) {
        if (ds == null || ds.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(chuyenHoatDongSangJson(ds.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String chuyenHoatDongSangJson(HoatDong h) {
        if (h == null) return "{}";
        return "{" +
                "\"id\":" + (h.getId() != null ? h.getId() : 0) + "," +
                "\"maHoatDong\":\"" + escapeJson(h.getMaHoatDong()) + "\"," +
                "\"loai\":\"" + escapeJson(h.getLoaiHoatDong()) + "\"," +
                "\"loaiHoatDong\":\"" + escapeJson(h.getLoaiHoatDong()) + "\"," +
                "\"tieuDe\":\"" + escapeJson(h.getTieuDe()) + "\"," +
                "\"thoiGian\":\"" + escapeJson(h.getThoiGianDinhDang()) + "\"," +
                "\"thoiGianDinhDang\":\"" + escapeJson(h.getThoiGianDinhDang()) + "\"," +
                "\"nguoiThucHien\":\"" + escapeJson(h.getTenNguoiThucHien()) + "\"," +
                "\"tenNguoiThucHien\":\"" + escapeJson(h.getTenNguoiThucHien()) + "\"," +
                "\"nguoiLienHe\":\"" + escapeJson(h.getTenNguoiLienHe()) + "\"," +
                "\"noiDung\":\"" + escapeJson(h.getNoiDung()) + "\"," +
                "\"moTa\":\"" + escapeJson(h.getNoiDung()) + "\"," +
                "\"ketQua\":\"" + escapeJson(h.getKetQua()) + "\"" +
                "}";
    }

    private static String escapeJson(String text) {
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
