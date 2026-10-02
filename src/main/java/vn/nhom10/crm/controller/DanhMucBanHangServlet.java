package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.DanhMucBanHangService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller xử lý màn hình và các hành động Khai báo danh mục dùng chung của bán hàng (Story S2-07).
 * URL: /danh-muc-ban-hang
 * Phân quyền: Giám đốc kinh doanh (Director) hoặc Quản trị hệ thống (Admin).
 */
@WebServlet(name = "DanhMucBanHangServlet", urlPatterns = {"/danh-muc-ban-hang", "/danh-muc"})
public class DanhMucBanHangServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DanhMucBanHangServlet.class.getName());

    private DanhMucBanHangService service;

    @Override
    public void init() throws ServletException {
        this.service = new DanhMucBanHangService();
    }

    public void setService(DanhMucBanHangService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!kiemTraQuyenTruyCap(request, response)) {
            return;
        }

        String loaiParam = request.getParameter("loai");
        LoaiDanhMuc loai = LoaiDanhMuc.tuMa(loaiParam);
        String tuKhoa = request.getParameter("tuKhoa");

        List<MucDanhMucDTO> danhSachMuc = service.timKiem(loai, tuKhoa);
        long[] thongKe = service.tinhThongKe(loai);

        HttpSession session = request.getSession(false);
        String thongBaoThanhCong = null;
        String thongBaoLoi = null;
        if (session != null) {
            thongBaoThanhCong = (String) session.getAttribute("thongBaoThanhCong");
            thongBaoLoi = (String) session.getAttribute("thongBaoLoi");
            session.removeAttribute("thongBaoThanhCong");
            session.removeAttribute("thongBaoLoi");
        }

        NguoiDung currentUser = null;
        if (session != null) {
            Object u = session.getAttribute("nguoiDung");
            if (u == null) u = session.getAttribute("user");
            if (u instanceof NguoiDung) {
                currentUser = (NguoiDung) u;
            }
        }

        request.setAttribute("nguoiDungHienTai", currentUser);
        request.setAttribute("loaiHienTai", loai);
        request.setAttribute("danhSachLoaiDanhMuc", LoaiDanhMuc.values());
        request.setAttribute("danhSachMuc", danhSachMuc);
        request.setAttribute("tongSoMuc", thongKe[0]);
        request.setAttribute("soMucKichHoat", thongKe[1]);
        request.setAttribute("tongSoThamChieu", thongKe[2]);
        request.setAttribute("tuKhoaHienTai", tuKhoa != null ? tuKhoa : "");
        request.setAttribute("thongBaoThanhCong", thongBaoThanhCong);
        request.setAttribute("thongBaoLoi", thongBaoLoi);

        request.getRequestDispatcher("/WEB-INF/views/danh-muc/quan-ly-danh-muc.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!kiemTraQuyenTruyCap(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        String loaiParam = request.getParameter("loaiDanhMuc");
        LoaiDanhMuc loai = LoaiDanhMuc.tuMa(loaiParam);

        HttpSession session = request.getSession();

        try {
            if ("them".equalsIgnoreCase(action)) {
                String maMuc = request.getParameter("maMuc");
                String tenMuc = request.getParameter("tenMuc");
                String moTa = request.getParameter("moTa");
                boolean kichHoat = "true".equalsIgnoreCase(request.getParameter("kichHoat"));

                MucDanhMucDTO dto = new MucDanhMucDTO();
                dto.setLoaiDanhMuc(loai);
                dto.setMaMuc(maMuc);
                dto.setTenMuc(tenMuc);
                dto.setMoTa(moTa);
                dto.setKichHoat(kichHoat);

                service.themMuc(dto);
                session.setAttribute("thongBaoThanhCong", "Đã thêm mới mục '" + tenMuc + "' vào danh mục " + loai.getTenHienThi() + " thành công.");

            } else if ("sua".equalsIgnoreCase(action)) {
                String idStr = request.getParameter("id");
                long id = Long.parseLong(idStr);
                String tenMuc = request.getParameter("tenMuc");
                String moTa = request.getParameter("moTa");
                boolean kichHoat = "true".equalsIgnoreCase(request.getParameter("kichHoat"));

                MucDanhMucDTO dto = service.layTheoId(loai, id);
                if (dto != null) {
                    dto.setTenMuc(tenMuc);
                    dto.setMoTa(moTa);
                    dto.setKichHoat(kichHoat);
                    service.capNhatMuc(dto);
                    session.setAttribute("thongBaoThanhCong", "Đã cập nhật mục '" + tenMuc + "' thành công.");
                } else {
                    session.setAttribute("thongBaoLoi", "Mục danh mục không tồn tại.");
                }

            } else if ("xoa".equalsIgnoreCase(action)) {
                String idStr = request.getParameter("id");
                long id = Long.parseLong(idStr);
                service.xoaMuc(loai, id);
                session.setAttribute("thongBaoThanhCong", "Đã xóa mục danh mục thành công.");

            } else if ("doi-thu-tu".equalsIgnoreCase(action)) {
                String idStr = request.getParameter("id");
                long id = Long.parseLong(idStr);
                String huong = request.getParameter("huong");
                boolean diChuyenLen = "len".equalsIgnoreCase(huong);
                service.thayDoiThuTu(loai, id, diChuyenLen);
                session.setAttribute("thongBaoThanhCong", "Đã thay đổi thứ tự hiển thị mục danh mục thành công.");

            } else if ("chuyen-trang-thai".equalsIgnoreCase(action)) {
                String idStr = request.getParameter("id");
                long id = Long.parseLong(idStr);
                service.chuyenTrangThaiKichHoat(loai, id);
                session.setAttribute("thongBaoThanhCong", "Đã thay đổi trạng thái kích hoạt mục danh mục.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Lỗi nghiệp vụ danh mục bán hàng: " + e.getMessage());
            session.setAttribute("thongBaoLoi", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi không xác định khi xử lý danh mục bán hàng", e);
            session.setAttribute("thongBaoLoi", "Đã xảy ra lỗi trong quá trình xử lý: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/danh-muc-ban-hang?loai=" + loai.getMa());
    }

    /**
     * Phân quyền Server-side: Chỉ Giám đốc kinh doanh (DIRECTOR) và Quản trị hệ thống (ADMIN) mới có quyền truy cập.
     */
    private boolean kiemTraQuyenTruyCap(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object userObj = (session != null) ? session.getAttribute("nguoiDung") : null;
        if (userObj == null && session != null) {
            userObj = session.getAttribute("user");
        }
        if (userObj == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return false;
        }

        NguoiDung currentUser = (NguoiDung) userObj;
        boolean coQuyen = currentUser.coVaiTro(VaiTroEnum.DIRECTOR)
                || currentUser.coVaiTro(VaiTroEnum.ADMIN)
                || currentUser.coVaiTro("DIRECTOR")
                || currentUser.coVaiTro("ADMIN");

        if (!coQuyen) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "Chỉ Giám đốc kinh doanh (Director) hoặc Quản trị hệ thống (Admin) mới có quyền truy cập và khai báo danh mục dùng chung của bán hàng.");
            request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            return false;
        }

        return true;
    }
}
