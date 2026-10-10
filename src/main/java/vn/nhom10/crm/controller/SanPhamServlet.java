package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.KetQuaSanPhamDTO;
import vn.nhom10.crm.dto.PhanTrangDTO;
import vn.nhom10.crm.model.LoaiSanPhamEnum;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.TrangThaiSanPhamEnum;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.PermissionService;
import vn.nhom10.crm.service.PhienService;
import vn.nhom10.crm.service.SanPhamService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller tiếp nhận và điều hướng các yêu cầu liên quan đến Danh mục sản phẩm & Bảng giá niêm yết (S2-05).
 */
@WebServlet(name = "SanPhamServlet", urlPatterns = {
        "/san-pham",
        "/san-pham/tao",
        "/san-pham/sua",
        "/san-pham/xoa",
        "/san-pham/trang-thai",
        "/san-pham/kiem-tra-gia-san"
})
public class SanPhamServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SanPhamServlet.class.getName());

    private SanPhamService sanPhamService;
    private PermissionService permissionService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.sanPhamService = new SanPhamService();
        this.permissionService = new PermissionService();
    }

    public void setSanPhamService(SanPhamService sanPhamService) {
        this.sanPhamService = sanPhamService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        NguoiDung nguoiDung = layNguoiDungHienTai(req);

        if (nguoiDung == null) {
            if ("/san-pham/kiem-tra-gia-san".equals(path)) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"thanhCong\":false,\"thongDiep\":\"Vui lòng đăng nhập hệ thống\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/dang-nhap");
            return;
        }

        if (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.READ)) {
            if ("/san-pham/kiem-tra-gia-san".equals(path)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"thanhCong\":false,\"thongDiep\":\"Bạn không có quyền truy cập module Danh mục.\"}");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMessage", "Bạn không có quyền truy cập module Danh mục.");
            req.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(req, resp);
            return;
        }

        try {
            switch (path) {
                case "/san-pham/tao":
                    hienThiFormTao(req, resp, nguoiDung);
                    break;
                case "/san-pham/sua":
                    hienThiFormSua(req, resp, nguoiDung);
                    break;
                case "/san-pham/kiem-tra-gia-san":
                    xuLyKiemTraGiaSanApi(req, resp);
                    break;
                case "/san-pham":
                default:
                    hienThiDanhSach(req, resp, nguoiDung);
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý GET " + path + ": " + e.getMessage(), e);
            req.setAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/san-pham/danh-sach.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        NguoiDung nguoiDung = layNguoiDungHienTai(req);

        if (nguoiDung == null) {
            if ("/san-pham/kiem-tra-gia-san".equals(path)) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"thanhCong\":false,\"thongDiep\":\"Vui lòng đăng nhập hệ thống\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/dang-nhap");
            return;
        }

        if (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.READ)) {
            if ("/san-pham/kiem-tra-gia-san".equals(path)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"thanhCong\":false,\"thongDiep\":\"Bạn không có quyền truy cập module Danh mục.\"}");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMessage", "Bạn không có quyền truy cập module Danh mục.");
            req.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(req, resp);
            return;
        }

        try {
            switch (path) {
                case "/san-pham/tao":
                    xuLyTaoSanPham(req, resp, nguoiDung);
                    break;
                case "/san-pham/sua":
                    xuLyCapNhatSanPham(req, resp, nguoiDung);
                    break;
                case "/san-pham/xoa":
                    xuLyXoaSanPham(req, resp, nguoiDung);
                    break;
                case "/san-pham/trang-thai":
                    xuLyChuyenTrangThai(req, resp, nguoiDung);
                    break;
                case "/san-pham/kiem-tra-gia-san":
                    xuLyKiemTraGiaSanApi(req, resp);
                    break;
                default:
                    resp.sendRedirect(req.getContextPath() + "/san-pham");
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xử lý POST " + path + ": " + e.getMessage(), e);
            HttpSession session = req.getSession();
            session.setAttribute("thongBaoLoi", "Có lỗi xảy ra: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/san-pham");
        }
    }

    /**
     * Hiển thị danh sách sản phẩm, hỗ trợ tìm kiếm, lọc và phân trang.
     */
    private void hienThiDanhSach(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        String tuKhoa = req.getParameter("tuKhoa");
        String loaiStr = req.getParameter("loai");
        String trangThaiStr = req.getParameter("trangThai");
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        LoaiSanPhamEnum loai = LoaiSanPhamEnum.tuMa(loaiStr);
        TrangThaiSanPhamEnum trangThai = TrangThaiSanPhamEnum.tuMa(trangThaiStr);

        int page = 1;
        int pageSize = 20;
        try {
            if (pageStr != null && !pageStr.isBlank()) {
                page = Integer.parseInt(pageStr);
            }
            if (pageSizeStr != null && !pageSizeStr.isBlank()) {
                pageSize = Integer.parseInt(pageSizeStr);
            }
        } catch (NumberFormatException ignored) {
        }

        PhanTrangDTO<SanPham> phanTrang = sanPhamService.layDanhSachSanPham(
                tuKhoa, loai, trangThai, page, pageSize, nguoiDung);

        boolean coQuyenGiaVon = sanPhamService.coQuyenGiaVon(nguoiDung);
        boolean coQuyenQuanLy = sanPhamService.coQuyenQuanLy(nguoiDung)
                && (permissionService == null || permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.WRITE));

        req.setAttribute("phanTrang", phanTrang);
        req.setAttribute("tuKhoa", tuKhoa != null ? tuKhoa : "");
        req.setAttribute("loaiChon", loaiStr != null ? loaiStr : "");
        req.setAttribute("trangThaiChon", trangThaiStr != null ? trangThaiStr : "");
        req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
        req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
        req.setAttribute("coQuyenGiaVon", coQuyenGiaVon);
        req.setAttribute("coQuyenQuanLy", coQuyenQuanLy);
        req.setAttribute("nguoiDungHienTai", nguoiDung);

        // Flash messages từ session nếu có
        HttpSession session = req.getSession(false);
        if (session != null) {
            if (session.getAttribute("thongBaoThanhCong") != null) {
                req.setAttribute("thongBaoThanhCong", session.getAttribute("thongBaoThanhCong"));
                session.removeAttribute("thongBaoThanhCong");
            }
            if (session.getAttribute("thongBaoLoi") != null) {
                req.setAttribute("thongBaoLoi", session.getAttribute("thongBaoLoi"));
                session.removeAttribute("thongBaoLoi");
            }
        }

        req.getRequestDispatcher("/WEB-INF/views/san-pham/danh-sach.jsp").forward(req, resp);
    }

    /**
     * Hiển thị form thêm mới sản phẩm/dịch vụ.
     */
    private void hienThiFormTao(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)
                || (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.WRITE))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thêm mới sản phẩm bảng giá.");
            return;
        }

        req.setAttribute("sanPham", new SanPham());
        req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
        req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
        req.setAttribute("coQuyenGiaVon", sanPhamService.coQuyenGiaVon(nguoiDung));
        req.setAttribute("nguoiDungHienTai", nguoiDung);

        req.getRequestDispatcher("/WEB-INF/views/san-pham/tao-san-pham.jsp").forward(req, resp);
    }

    /**
     * Hiển thị form chỉnh sửa sản phẩm dịch vụ.
     */
    private void hienThiFormSua(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)
                || (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.WRITE))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền sửa sản phẩm bảng giá.");
            return;
        }

        String idStr = req.getParameter("id");
        int id = 0;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/san-pham");
            return;
        }

        SanPham sp = sanPhamService.layChiTietSanPham(id, nguoiDung);
        if (sp == null) {
            HttpSession session = req.getSession();
            session.setAttribute("thongBaoLoi", "Không tìm thấy sản phẩm có ID=" + id);
            resp.sendRedirect(req.getContextPath() + "/san-pham");
            return;
        }

        req.setAttribute("sanPham", sp);
        req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
        req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
        req.setAttribute("coQuyenGiaVon", sanPhamService.coQuyenGiaVon(nguoiDung));
        req.setAttribute("nguoiDungHienTai", nguoiDung);

        req.getRequestDispatcher("/WEB-INF/views/san-pham/sua-san-pham.jsp").forward(req, resp);
    }

    /**
     * Tiếp nhận dữ liệu thêm mới sản phẩm.
     */
    private void xuLyTaoSanPham(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)
                || (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.WRITE))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMessage", "Không có quyền thực hiện thêm mới sản phẩm.");
            req.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(req, resp);
            return;
        }

        KetQuaSanPhamDTO ketQua = new KetQuaSanPhamDTO();
        SanPham sp = docDuLieuTuRequest(req, ketQua);

        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Dữ liệu nhập vào chưa hợp lệ. Vui lòng kiểm tra lại các trường báo lỗi.");
            req.setAttribute("sanPham", sp);
            req.setAttribute("thongBaoLoi", ketQua.getThongBao());
            req.setAttribute("danhSachLoi", ketQua.getDanhSachLoi());
            req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
            req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
            req.setAttribute("coQuyenGiaVon", sanPhamService.coQuyenGiaVon(nguoiDung));
            req.setAttribute("nguoiDungHienTai", nguoiDung);
            req.getRequestDispatcher("/WEB-INF/views/san-pham/tao-san-pham.jsp").forward(req, resp);
            return;
        }

        ketQua = sanPhamService.taoSanPham(sp, nguoiDung);

        if (ketQua.isThanhCong()) {
            HttpSession session = req.getSession();
            session.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            resp.sendRedirect(req.getContextPath() + "/san-pham");
        } else {
            req.setAttribute("sanPham", sp);
            req.setAttribute("thongBaoLoi", ketQua.getThongBao());
            req.setAttribute("danhSachLoi", ketQua.getDanhSachLoi());
            req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
            req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
            req.setAttribute("coQuyenGiaVon", sanPhamService.coQuyenGiaVon(nguoiDung));
            req.setAttribute("nguoiDungHienTai", nguoiDung);
            req.getRequestDispatcher("/WEB-INF/views/san-pham/tao-san-pham.jsp").forward(req, resp);
        }
    }

    /**
     * Tiếp nhận dữ liệu cập nhật sản phẩm.
     */
    private void xuLyCapNhatSanPham(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)
                || (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.WRITE))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMessage", "Không có quyền thực hiện cập nhật sản phẩm.");
            req.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(req, resp);
            return;
        }

        KetQuaSanPhamDTO ketQua = new KetQuaSanPhamDTO();
        SanPham sp = docDuLieuTuRequest(req, ketQua);
        String idStr = req.getParameter("id");
        try {
            sp.setId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/san-pham");
            return;
        }

        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThanhCong(false);
            ketQua.setThongBao("Dữ liệu cập nhật chưa hợp lệ. Vui lòng kiểm tra lại.");
            req.setAttribute("sanPham", sp);
            req.setAttribute("thongBaoLoi", ketQua.getThongBao());
            req.setAttribute("danhSachLoi", ketQua.getDanhSachLoi());
            req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
            req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
            req.setAttribute("coQuyenGiaVon", sanPhamService.coQuyenGiaVon(nguoiDung));
            req.setAttribute("nguoiDungHienTai", nguoiDung);
            req.getRequestDispatcher("/WEB-INF/views/san-pham/sua-san-pham.jsp").forward(req, resp);
            return;
        }

        ketQua = sanPhamService.capNhatSanPham(sp, nguoiDung);

        if (ketQua.isThanhCong()) {
            HttpSession session = req.getSession();
            session.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            resp.sendRedirect(req.getContextPath() + "/san-pham");
        } else {
            req.setAttribute("sanPham", sp);
            req.setAttribute("thongBaoLoi", ketQua.getThongBao());
            req.setAttribute("danhSachLoi", ketQua.getDanhSachLoi());
            req.setAttribute("danhSachLoai", LoaiSanPhamEnum.values());
            req.setAttribute("danhSachTrangThai", TrangThaiSanPhamEnum.values());
            req.setAttribute("coQuyenGiaVon", sanPhamService.coQuyenGiaVon(nguoiDung));
            req.setAttribute("nguoiDungHienTai", nguoiDung);
            req.getRequestDispatcher("/WEB-INF/views/san-pham/sua-san-pham.jsp").forward(req, resp);
        }
    }

    /**
     * Xử lý xóa sản phẩm (chỉ xóa được nếu chưa xuất hiện trong báo giá).
     */
    private void xuLyXoaSanPham(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)
                || (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.FULL))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMessage", "Không có quyền thực hiện xóa sản phẩm (yêu cầu quyền FULL).");
            req.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(req, resp);
            return;
        }

        String idStr = req.getParameter("id");
        int id = 0;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/san-pham");
            return;
        }

        KetQuaSanPhamDTO ketQua = sanPhamService.xoaSanPham(id, nguoiDung);
        HttpSession session = req.getSession();
        if (ketQua.isThanhCong()) {
            session.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
        } else {
            session.setAttribute("thongBaoLoi", ketQua.getThongBao());
        }
        resp.sendRedirect(req.getContextPath() + "/san-pham");
    }

    /**
     * Xử lý chuyển đổi trạng thái sản phẩm (Đang kinh doanh / Ngừng kinh doanh).
     */
    private void xuLyChuyenTrangThai(HttpServletRequest req, HttpServletResponse resp, NguoiDung nguoiDung)
            throws ServletException, IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)
                || (permissionService != null && !permissionService.coQuyen(nguoiDung, "DANH_MUC", MucQuyen.WRITE))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMessage", "Không có quyền thực hiện thay đổi trạng thái sản phẩm.");
            req.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(req, resp);
            return;
        }

        String idStr = req.getParameter("id");
        String trangThaiStr = req.getParameter("trangThaiMoi");
        int id = 0;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/san-pham");
            return;
        }

        TrangThaiSanPhamEnum trangThaiMoi = TrangThaiSanPhamEnum.tuMa(trangThaiStr);
        if (trangThaiMoi == null) {
            trangThaiMoi = TrangThaiSanPhamEnum.NGUNG_KINH_DOANH;
        }

        KetQuaSanPhamDTO ketQua = sanPhamService.chuyenTrangThai(id, trangThaiMoi, nguoiDung);
        HttpSession session = req.getSession();
        if (ketQua.isThanhCong()) {
            session.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
        } else {
            session.setAttribute("thongBaoLoi", ketQua.getThongBao());
        }
        resp.sendRedirect(req.getContextPath() + "/san-pham");
    }

    /**
     * AC: Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không.
     * Cung cấp endpoint API kiểm tra đơn giá có dưới giá sàn không.
     */
    private void xuLyKiemTraGiaSanApi(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String idStr = req.getParameter("id");
        String donGiaStr = req.getParameter("donGia");

        int id = 0;
        BigDecimal donGia = null;
        try {
            if (idStr != null) id = Integer.parseInt(idStr);
            if (donGiaStr != null && !donGiaStr.isBlank()) {
                donGia = chuyenDoiTienTe(donGiaStr);
            }
        } catch (Exception e) {
            out.print("{\"thanhCong\":false,\"thongDiep\":\"Dữ liệu đầu vào không hợp lệ\"}");
            return;
        }

        SanPham sp = sanPhamService.layChiTietSanPham(id, null);
        if (sp == null) {
            out.print("{\"thanhCong\":false,\"thongDiep\":\"Không tìm thấy sản phẩm\"}");
            return;
        }

        boolean canDuyet = donGia != null && sp.kiemTraDuoiGiaSan(donGia);

        out.print(String.format(
                "{\"thanhCong\":true,\"sanPhamId\":%d,\"giaNiemYet\":%s,\"giaSan\":%s,\"donGia\":%s,\"canDuyetChiMon\":%b,\"canDuyet\":%b,\"canhBao\":\"%s\"}",
                sp.getId(),
                sp.getGiaNiemYet().toString(),
                sp.getGiaSan().toString(),
                donGia != null ? donGia.toString() : "null",
                canDuyet,
                canDuyet,
                canDuyet ? "Đơn giá thấp hơn Giá sàn niêm yết (" + sp.getGiaSanDinhDang() + "). Báo giá này sẽ cần Giám đốc kinh doanh phê duyệt!" : "Đơn giá hợp lệ theo khung giá niêm yết."
        ));
    }

    /**
     * Trích xuất thông tin sản phẩm từ Form submission.
     */
    private SanPham docDuLieuTuRequest(HttpServletRequest req, KetQuaSanPhamDTO ketQua) {
        SanPham sp = new SanPham();
        sp.setMaSanPham(req.getParameter("maSanPham"));
        sp.setTenSanPham(req.getParameter("tenSanPham"));
        sp.setLoai(LoaiSanPhamEnum.tuMa(req.getParameter("loai")));
        sp.setDonViTinh(req.getParameter("donViTinh"));
        sp.setMoTa(req.getParameter("moTa"));

        String trangThaiStr = req.getParameter("trangThai");
        sp.setTrangThai(TrangThaiSanPhamEnum.tuMa(trangThaiStr));

        // Parse các trường giá (tiền tệ) - Bắt lỗi nếu sai định dạng tiền tệ
        try {
            sp.setGiaNiemYet(chuyenDoiTienTe(req.getParameter("giaNiemYet")));
        } catch (IllegalArgumentException e) {
            ketQua.themLoi("giaNiemYet", "Giá niêm yết không hợp lệ. Vui lòng nhập số tiền đúng định dạng.");
        }

        try {
            sp.setGiaSan(chuyenDoiTienTe(req.getParameter("giaSan")));
        } catch (IllegalArgumentException e) {
            ketQua.themLoi("giaSan", "Giá sàn không hợp lệ. Vui lòng nhập số tiền đúng định dạng.");
        }

        try {
            sp.setGiaVon(chuyenDoiTienTe(req.getParameter("giaVon")));
        } catch (IllegalArgumentException e) {
            ketQua.themLoi("giaVon", "Giá vốn không hợp lệ. Vui lòng nhập số tiền đúng định dạng.");
        }

        return sp;
    }

    /**
     * Chuyển đổi chuỗi tiền tệ thành BigDecimal một cách an toàn và chính xác.
     * Hỗ trợ định dạng số nguyên, số thực, dấu chấm hàng nghìn chuẩn VN (1.000.000)
     * hoặc dấu phẩy hàng nghìn (1,000,000). Reject mọi định dạng không hợp lệ.
     */
    public static BigDecimal chuyenDoiTienTe(String giaStr) {
        if (giaStr == null || giaStr.isBlank()) {
            return null;
        }
        String s = giaStr.trim().replaceAll("(?i)[₫đvnd]+$", "").trim();
        if (s.isEmpty()) {
            return null;
        }
        // 1. Raw integer hoặc decimal với dấu chấm: vd 1000000 hoặc 1000000.50
        if (s.matches("^[-+]?\\d+(\\.\\d+)?$")) {
            return new BigDecimal(s);
        }
        // 2. Định dạng hàng nghìn dấu chấm kiểu Việt Nam: vd 1.000.000 hoặc 1.000.000,50
        if (s.matches("^[-+]?\\d{1,3}(\\.\\d{3})+(,\\d+)?$")) {
            String normalized = s.replace(".", "").replace(",", ".");
            return new BigDecimal(normalized);
        }
        // 3. Định dạng hàng nghìn dấu phẩy kiểu quốc tế: vd 1,000,000 hoặc 1,000,000.50
        if (s.matches("^[-+]?\\d{1,3}(,\\d{3})+(\\.\\d+)?$")) {
            String normalized = s.replace(",", "");
            return new BigDecimal(normalized);
        }
        // 4. Số thập phân dùng dấu phẩy đơn: vd 1000000,50
        if (s.matches("^[-+]?\\d+(,\\d+)?$")) {
            String normalized = s.replace(",", ".");
            return new BigDecimal(normalized);
        }
        throw new IllegalArgumentException("Định dạng số tiền không hợp lệ: " + giaStr);
    }

    /**
     * Lấy người dùng hiện tại từ session chuẩn của hệ thống (PhienService.SESSION_USER_KEY).
     * Tuyệt đối không fallback sang tài khoản giả hoặc vai trò mặc định.
     */
    public NguoiDung layNguoiDungHienTai(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            return (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
        }
        return null;
    }
}
