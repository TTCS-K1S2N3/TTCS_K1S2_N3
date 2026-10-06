package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.DanhMucBanHangService;
import vn.nhom10.crm.service.KhachHangService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller phục vụ quản lý hồ sơ khách hàng doanh nghiệp (Story S3-01).
 * Đáp ứng đầy đủ Acceptance Criteria:
 * - AC1: Khai báo tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ, người sở hữu
 * - AC2: Mã số thuế nếu có thì phải là duy nhất
 * - AC3: Khách hàng có 4 trạng thái: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác
 * - AC4: Nhân viên chỉ thấy khách hàng mình sở hữu; trưởng nhóm thấy toàn nhóm
 * Đồng thời duy trì tính tương thích ngược với Data Scope của Sprint 1 (S1-05).
 * URL: /khach-hang
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang"})
public class KhachHangServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhachHangServlet.class.getName());

    private final PhanQuyenDuLieuService phanQuyenService;
    private final KhachHangService khachHangService;
    private final DanhMucBanHangService danhMucBanHangService;
    private final NguoiDungDAO nguoiDungDAO;

    public KhachHangServlet() {
        this(new PhanQuyenDuLieuService(), new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this(phanQuyenService, new KhachHangService(), new DanhMucBanHangService(), new NguoiDungDAO());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, KhachHangService khachHangService) {
        this(phanQuyenService, khachHangService, new DanhMucBanHangService(), new NguoiDungDAO());
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService,
                            KhachHangService khachHangService,
                            DanhMucBanHangService danhMucBanHangService,
                            NguoiDungDAO nguoiDungDAO) {
        this.phanQuyenService = phanQuyenService != null ? phanQuyenService : new PhanQuyenDuLieuService();
        this.khachHangService = khachHangService != null ? khachHangService : new KhachHangService();
        this.danhMucBanHangService = danhMucBanHangService != null ? danhMucBanHangService : new DanhMucBanHangService();
        this.nguoiDungDAO = nguoiDungDAO != null ? nguoiDungDAO : new NguoiDungDAO();
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

        // 1. Kiểm tra quyền khi xem chi tiết khách hàng trực tiếp bằng ID (AC3, AC4)
        if (paramId != null && !paramId.trim().isEmpty()) {
            Long id = null;
            try {
                id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}

            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            KhachHang khachHang = null;
            try {
                if (id != null) {
                    khachHang = khachHangService.timTheoIdVaKiemTraQuyen(id, user);
                }
            } catch (SecurityException se) {
                // Chặn ngoài phạm vi
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", se.getMessage());
                request.setAttribute("banGhi", banGhi);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi tìm khách hàng model: " + e.getMessage());
            }

            if (id == null || (banGhi == null && khachHang == null)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy khách hàng.");
                return;
            }

            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(userDTO, banGhi);
            if (banGhi != null && !ketQua.isCoQuyen()) {
                // CHẶN TRUY CẬP NGOÀI PHẠM VỊ: Trả về HTTP 403 Forbidden
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ketQua.getThongBao());
                request.setAttribute("banGhi", banGhi);
                request.setAttribute("khachHang", khachHang);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

            request.setAttribute("banGhiChiTiet", banGhi);
            request.setAttribute("khachHangChiTiet", khachHang);
            request.setAttribute("banGhi", banGhi);
            request.setAttribute("khachHang", khachHang);
            request.setAttribute("thongBaoThanhCong", ketQua != null ? ketQua.getThongBao() : "Truy cập khách hàng thành công.");
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
        String trangThai = request.getParameter("trangThai");
        Long nganhNgheId = parseLongOrNull(request.getParameter("nganhNgheId"));
        Long quyMoId = parseLongOrNull(request.getParameter("quyMoId"));

        int trang = 1;
        try {
            String pTrang = request.getParameter("trang");
            if (pTrang != null && !pTrang.trim().isEmpty()) {
                trang = Math.max(1, Integer.parseInt(pTrang.trim()));
            }
        } catch (NumberFormatException ignored) {}

        int kichThuocTrang = 20;

        // 3. Tự động lọc danh sách khách hàng theo Data Scope của user (AC1, AC4)
        List<BanGhiNghiepVuDTO> danhSachKhachHang = phanQuyenService.layDanhSachDuLieu(
                userDTO, phamViHieuLuc, tuKhoa, "KHACH_HANG"
        );

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

        // 4. Xử lý Xuất Excel danh mục khách hàng (.xlsx)
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

        // 5. Nạp danh mục ngành nghề, quy mô doanh nghiệp và người sở hữu cho form & filter
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

        String paramId = request.getParameter("id");
        String action = request.getParameter("action");

        // 1. Xử lý thao tác sửa khách hàng qua POST /khach-hang (chặn sửa ngoài phạm vi AC4)
        if (paramId != null || "sua".equals(action)) {
            Long id = null;
            try {
                if (paramId != null) id = Long.parseLong(paramId.trim());
            } catch (NumberFormatException ignored) {}

            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenSua(userDTO, banGhi);

            if (banGhi != null && !ketQua.isCoQuyen()) {
                // CHẶN SỬA NGOÀI PHẠM VỊ: HTTP 403 Forbidden
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("currentUser", userDTO);
                request.setAttribute("thongBaoLoi", ketQua.getThongBao());
                request.setAttribute("banGhi", banGhi);
                request.getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp").forward(request, response);
                return;
            }

            // Tiếp nhận dữ liệu sửa
            String tieuDeMoi = request.getParameter("tieuDe");
            if (tieuDeMoi == null) tieuDeMoi = request.getParameter("tenCongTy");
            String giaTriMoi = request.getParameter("giaTri");
            if (giaTriMoi == null) giaTriMoi = request.getParameter("doanhThuUocTinh");
            String trangThaiMoi = request.getParameter("trangThai");
            String moTaMoi = request.getParameter("moTaChiTiet");
            String maSoThueMoi = request.getParameter("maSoThue");
            Long nganhNgheId = parseLongOrNull(request.getParameter("nganhNgheId"));
            Long quyMoId = parseLongOrNull(request.getParameter("quyMoId"));
            String website = request.getParameter("website");
            String diaChi = request.getParameter("diaChi");
            Long nguoiSoHuuId = parseLongOrNull(request.getParameter("nguoiSoHuuId"));

            // Validate tên công ty
            if (tieuDeMoi == null || tieuDeMoi.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Tên công ty / khách hàng không được để trống.");
                doGet(request, response);
                return;
            }

            // Validate trạng thái (AC3)
            if (trangThaiMoi != null && !trangThaiMoi.trim().isEmpty() && !TrangThaiKhachHangEnum.laHopLe(trangThaiMoi)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Trạng thái khách hàng không hợp lệ. Chỉ chấp nhận: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác.");
                doGet(request, response);
                return;
            }

            // Cập nhật KhachHang Model nếu tồn tại trong database
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

            // Đồng bộ với banGhi DTO nếu có
            if (banGhi != null) {
                banGhi.setTieuDe(tieuDeMoi.trim());
                if (giaTriMoi != null) banGhi.setGiaTri(giaTriMoi.trim());
                if (trangThaiMoi != null) banGhi.setTrangThai(TrangThaiKhachHangEnum.chuanHoaTen(trangThaiMoi));
                if (moTaMoi != null) banGhi.setMoTaChiTiet(moTaMoi.trim());
                phanQuyenService.capNhatBanGhi(banGhi);
            }

            request.setAttribute("thongBaoThanhCong", "Cập nhật dữ liệu khách hàng thành công.");
        } else if ("them".equals(action) || "create".equals(action) || paramId == null) {
            // 2. Xử lý thao tác thêm mới khách hàng (Story S3-01 & S1-05)
            String tenCongTy = request.getParameter("tenCongTy");
            if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
                tenCongTy = request.getParameter("tieuDe");
            }

            String maKhachHang = request.getParameter("maKhachHang");
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                maKhachHang = request.getParameter("maBanGhi");
            }

            String maSoThue = request.getParameter("maSoThue");
            Long nganhNgheId = parseLongOrNull(request.getParameter("nganhNgheId"));
            Long quyMoId = parseLongOrNull(request.getParameter("quyMoId"));
            String website = request.getParameter("website");
            String diaChi = request.getParameter("diaChi");

            String giaTri = request.getParameter("doanhThuUocTinh");
            if (giaTri == null || giaTri.trim().isEmpty()) {
                giaTri = request.getParameter("giaTri");
            }

            String trangThai = request.getParameter("trangThai");
            String moTaChiTiet = request.getParameter("moTaChiTiet");

            // Server-side validation AC1: Bắt buộc tên công ty / khách hàng
            if (tenCongTy == null || tenCongTy.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Tên công ty / khách hàng không được để trống.");
                doGet(request, response);
                return;
            }

            // Server-side validation AC3: Trạng thái nếu gửi lên phải thuộc 4 trạng thái chuẩn
            if (trangThai != null && !trangThai.trim().isEmpty() && !TrangThaiKhachHangEnum.laHopLe(trangThai)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Trạng thái khách hàng không hợp lệ. Chỉ chấp nhận: Tiềm năng, Đang giao dịch, Khách hàng, Ngừng hợp tác.");
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

            // Server-side validation AC2 & AC4: Tạo khách hàng qua KhachHangService
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

            // BẢO MẬT & DATA SCOPE (AC4):
            // Tuyệt đối không cho phép client giả mạo người sở hữu (no owner spoofing).
            // Nếu là Sales Rep, server luôn ép người sở hữu về session user.
            Long paramNguoiSoHuu = parseLongOrNull(request.getParameter("nguoiSoHuuId"));
            if (user.coVaiTro(VaiTroEnum.ADMIN) || user.coVaiTro(VaiTroEnum.DIRECTOR) || user.coVaiTro(VaiTroEnum.TEAM_LEAD)) {
                khachHangMoi.setNguoiSoHuuId(paramNguoiSoHuu != null ? paramNguoiSoHuu : user.getId());
            } else {
                khachHangMoi.setNguoiSoHuuId(user.getId());
                khachHangMoi.setNhomKinhDoanhId(user.getNhomKinhDoanhId() != null ? Long.valueOf(user.getNhomKinhDoanhId()) : null);
            }

            try {
                KhachHang khLuu = khachHangService.taoKhachHang(user, khachHangMoi);

                // Đồng bộ với phanQuyenService để duy trì tương thích các test suite S1-05
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

    private void napDanhMucBaoTro(HttpServletRequest request, NguoiDung user) {
        try {
            List<MucDanhMucDTO> dsNganhNghe = danhMucBanHangService.layDanhSachTheoLoai(LoaiDanhMuc.NGANH_NGHE);
            request.setAttribute("dsNganhNghe", dsNganhNghe);

            List<MucDanhMucDTO> dsQuyMo = danhMucBanHangService.layDanhSachTheoLoai(LoaiDanhMuc.QUY_MO);
            request.setAttribute("dsQuyMo", dsQuyMo);

            request.setAttribute("dsTrangThaiKhachHang", TrangThaiKhachHangEnum.values());

            // Load danh sách người dùng khả dĩ làm người sở hữu
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
}
