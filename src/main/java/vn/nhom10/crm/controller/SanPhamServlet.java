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
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.TrangThaiSanPhamEnum;
import vn.nhom10.crm.model.VaiTroEnum;
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
    public static final String SESSION_USER = "nguoiDungHienTai";

    private SanPhamService sanPhamService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.sanPhamService = new SanPhamService();
    }

    public void setSanPhamService(SanPhamService sanPhamService) {
        this.sanPhamService = sanPhamService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        NguoiDung nguoiDung = layNguoiDungHienTai(req);

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
        boolean coQuyenQuanLy = sanPhamService.coQuyenQuanLy(nguoiDung);

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
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)) {
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
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)) {
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
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện.");
            return;
        }

        SanPham sp = docDuLieuTuRequest(req);
        KetQuaSanPhamDTO ketQua = sanPhamService.taoSanPham(sp, nguoiDung);

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
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện.");
            return;
        }

        SanPham sp = docDuLieuTuRequest(req);
        String idStr = req.getParameter("id");
        try {
            sp.setId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/san-pham");
            return;
        }

        KetQuaSanPhamDTO ketQua = sanPhamService.capNhatSanPham(sp, nguoiDung);

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
            throws IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện.");
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
            throws IOException {
        if (!sanPhamService.coQuyenQuanLy(nguoiDung)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện.");
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
                donGia = new BigDecimal(donGiaStr.replace(",", "").trim());
            }
        } catch (NumberFormatException e) {
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
                "{\"thanhCong\":true,\"sanPhamId\":%d,\"giaNiemYet\":%s,\"giaSan\":%s,\"donGia\":%s,\"canDuyetChiMon\":%b,\"canhBao\":\"%s\"}",
                sp.getId(),
                sp.getGiaNiemYet().toString(),
                sp.getGiaSan().toString(),
                donGia != null ? donGia.toString() : "null",
                canDuyet,
                canDuyet ? "Đơn giá thấp hơn Giá sàn niêm yết (" + sp.getGiaSanDinhDang() + "). Báo giá này sẽ cần Giám đốc kinh doanh phê duyệt!" : "Đơn giá hợp lệ theo khung giá niêm yết."
        ));
    }

    /**
     * Trích xuất thông tin sản phẩm từ Form submission.
     */
    private SanPham docDuLieuTuRequest(HttpServletRequest req) {
        SanPham sp = new SanPham();
        sp.setMaSanPham(req.getParameter("maSanPham"));
        sp.setTenSanPham(req.getParameter("tenSanPham"));
        sp.setLoai(LoaiSanPhamEnum.tuMa(req.getParameter("loai")));
        sp.setDonViTinh(req.getParameter("donViTinh"));
        sp.setMoTa(req.getParameter("moTa"));

        String trangThaiStr = req.getParameter("trangThai");
        sp.setTrangThai(TrangThaiSanPhamEnum.tuMa(trangThaiStr));

        // Parse các trường giá (tiền tệ)
        sp.setGiaNiemYet(chuyenDoiTienTe(req.getParameter("giaNiemYet")));
        sp.setGiaSan(chuyenDoiTienTe(req.getParameter("giaSan")));
        sp.setGiaVon(chuyenDoiTienTe(req.getParameter("giaVon")));

        return sp;
    }

    private BigDecimal chuyenDoiTienTe(String giaStr) {
        if (giaStr == null || giaStr.isBlank()) {
            return null;
        }
        try {
            // Loại bỏ dấu phẩy, chấm ngăn cách hàng nghìn nếu có
            String clean = giaStr.replaceAll("[^0-9.]", "");
            return new BigDecimal(clean);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy người dùng hiện tại từ session hoặc hỗ trợ mô phỏng vai trò phục vụ kiểm thử.
     */
    public NguoiDung layNguoiDungHienTai(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            NguoiDung user = (NguoiDung) session.getAttribute(SESSION_USER);
            if (user != null) {
                return user;
            }
        }

        // Hỗ trợ tham số ?role= để kiểm thử nhanh các vai trò
        String roleParam = req.getParameter("role");
        if (roleParam != null && !roleParam.isBlank()) {
            VaiTroEnum vt = VaiTroEnum.tuMa(roleParam);
            if (vt != null) {
                NguoiDung demoUser = new NguoiDung(99, "Người dùng " + vt.getTenTiengViet(), "demo@" + vt.getMaVaiTro().toLowerCase() + ".crm.vn");
                demoUser.themVaiTro(vt);
                return demoUser;
            }
        }

        // Mặc định Giám đốc kinh doanh để trải nghiệm đầy đủ quyền của Story S2-05
        NguoiDung defaultUser = new NguoiDung(1, "Giám đốc Kinh doanh", "giamdoc@crm.vn");
        defaultUser.themVaiTro(VaiTroEnum.DIRECTOR);
        return defaultUser;
    }
}
