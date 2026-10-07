package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.service.CongTyMeConService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;
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
 *
 * URL Patterns: /khach-hang, /khach-hang/chi-tiet, /khach-hang/cong-ty-con
 */
@WebServlet(name = "KhachHangServlet", urlPatterns = {"/khach-hang", "/khach-hang/chi-tiet", "/khach-hang/cong-ty-con"})
public class KhachHangServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(KhachHangServlet.class.getName());

    private final PhanQuyenDuLieuService phanQuyenService;
    private final CongTyMeConService congTyMeConService;

    public KhachHangServlet() {
        this.phanQuyenService = new PhanQuyenDuLieuService();
        this.congTyMeConService = new CongTyMeConService();
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService) {
        this.phanQuyenService = phanQuyenService;
        this.congTyMeConService = new CongTyMeConService();
    }

    public KhachHangServlet(PhanQuyenDuLieuService phanQuyenService, CongTyMeConService congTyMeConService) {
        this.phanQuyenService = phanQuyenService;
        this.congTyMeConService = congTyMeConService;
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

            // Story S3-05: Nạp dữ liệu thống kê nhóm công ty & danh sách công ty con
            try {
                ThongKeNhomCongTyDTO thongKeNhom = congTyMeConService.layThongKeNhomCongTy(id);
                request.setAttribute("thongKeNhomCongTy", thongKeNhom);
                request.setAttribute("dsKhaDungLamCon", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyCon(id));
                request.setAttribute("dsKhaDungLamMe", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyMe(id));
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Không thể tải số liệu nhóm công ty cho ID " + id + ": " + e.getMessage());
            }

            // Nếu người dùng yêu cầu trang chi tiết (/khach-hang/chi-tiet hoặc view=chi-tiet)
            if ("/khach-hang/chi-tiet".equals(servletPath) || "chi-tiet".equalsIgnoreCase(viewParam) || "chi-tiet".equalsIgnoreCase(actionParam)) {
                request.setAttribute("currentUser", userDTO);
                response.setStatus(HttpServletResponse.SC_OK);
                request.getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp").forward(request, response);
                return;
            }
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

        // Xử lý thao tác sửa khách hàng qua POST /khach-hang (chặn sửa ngoài phạm vi)
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

            // BẢO MẬT & DATA SCOPE (S1-05)
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
     * Xử lý gỡ bỏ quan hệ công ty con.
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
     * Xử lý cập nhật công ty mẹ từ trang của khách hàng con.
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

    private void chuyenHuongChiTiet(HttpServletRequest request, HttpServletResponse response, Long id, NguoiDungDTO userDTO)
            throws ServletException, IOException {
        if (id != null) {
            BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(id, "KHACH_HANG");
            request.setAttribute("banGhi", banGhi);
            request.setAttribute("banGhiChiTiet", banGhi);
            try {
                ThongKeNhomCongTyDTO thongKeNhom = congTyMeConService.layThongKeNhomCongTy(id);
                request.setAttribute("thongKeNhomCongTy", thongKeNhom);
                request.setAttribute("dsKhaDungLamCon", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyCon(id));
                request.setAttribute("dsKhaDungLamMe", congTyMeConService.layDanhSachKhachHangKhaDungLamCongTyMe(id));
            } catch (SQLException ignored) {}
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
}
