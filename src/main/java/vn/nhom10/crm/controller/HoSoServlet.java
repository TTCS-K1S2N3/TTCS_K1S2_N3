package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.HoSoService;
import vn.nhom10.crm.service.MenuService;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller tiếp nhận yêu cầu Xem và Cập nhật hồ sơ cá nhân người dùng (Story S2-02 & S2-03).
 * URLs:
 * - GET  /ho-so: Xem thông tin hồ sơ, avatar và chữ ký email hiện tại.
 * - POST /ho-so hoặc /ho-so/cap-nhat: Xử lý cập nhật họ tên, số điện thoại, chữ ký email.
 *
 * Phân quyền & Bảo mật (AC2):
 * - Chỉ cho phép người dùng đang đăng nhập truy cập hồ sơ của chính mình (lấy ID trực tiếp từ session chuẩn).
 * - Không cho phép người dùng tự đổi email, nhóm kinh doanh hoặc vai trò.
 */
@WebServlet(name = "HoSoServlet", urlPatterns = {"/ho-so", "/ho-so/cap-nhat"})
public class HoSoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HoSoServlet.class.getName());

    private HoSoService hoSoService;

    public HoSoServlet() {
    }

    public HoSoServlet(HoSoService hoSoService) {
        this.hoSoService = hoSoService;
    }

    public HoSoServlet(HoSoService hoSoService, MenuService menuService, NguoiDungDAO nguoiDungDAO) {
        this.hoSoService = hoSoService;
    }

    @Override
    public void init() throws ServletException {
        this.hoSoService = new HoSoService();
    }

    public void setHoSoService(HoSoService hoSoService) {
        this.hoSoService = hoSoService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        NguoiDung nguoiDungHienTai = layNguoiDungDangNhap(session);

        if (nguoiDungHienTai == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        // Tải thông tin người dùng mới nhất từ Database
        NguoiDung thongTinHoSo = hoSoService.layHoSo(nguoiDungHienTai.getId());
        if (thongTinHoSo == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=user_not_found");
            return;
        }

        // Kiểm tra thông báo thành công sau redirect
        String thanhCongParam = request.getParameter("thanhCong");
        if ("true".equalsIgnoreCase(thanhCongParam)) {
            request.setAttribute("thongBaoThanhCong", "Cập nhật hồ sơ cá nhân thành công.");
        }

        // Đọc flash message nếu có từ upload avatar
        if (session != null) {
            String flashSuccess = (String) session.getAttribute("flashMessageSuccess");
            String flashError = (String) session.getAttribute("flashMessageError");
            if (flashSuccess != null) {
                request.setAttribute("thongBaoThanhCong", flashSuccess);
                session.removeAttribute("flashMessageSuccess");
            }
            if (flashError != null) {
                request.setAttribute("thongBaoLoi", flashError);
                session.removeAttribute("flashMessageError");
            }
        }

        request.setAttribute("nguoiDung", thongTinHoSo);
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/ho-so.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        NguoiDung nguoiDungHienTai = layNguoiDungDangNhap(session);

        if (nguoiDungHienTai == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        // Đọc các trường được phép sửa (AC1)
        String hoTen = request.getParameter("hoTen");
        String soDienThoai = request.getParameter("soDienThoai");
        String chuKyEmail = request.getParameter("chuKyEmail");

        // Đọc các trường không được phép tự sửa (AC2 - phòng thủ can thiệp HTTP request)
        String emailMoi = request.getParameter("email");
        String nhomIdStr = request.getParameter("nhomId");
        String[] vaiTroIdsArr = request.getParameterValues("vaiTroIds");

        Integer nhomIdMoi = parseInteger(nhomIdStr);
        List<Integer> vaiTroIdsMoi = null;
        if (vaiTroIdsArr != null) {
            vaiTroIdsMoi = new ArrayList<>();
            for (String s : vaiTroIdsArr) {
                Integer vtId = parseInteger(s);
                if (vtId != null) {
                    vaiTroIdsMoi.add(vtId);
                }
            }
        }

        // Gọi tầng Service xử lý cập nhật kèm kiểm tra AC1, AC2, AC3
        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                nguoiDungHienTai.getId(),
                hoTen,
                soDienThoai,
                chuKyEmail,
                emailMoi,
                nhomIdMoi,
                vaiTroIdsMoi
        );

        if (ketQua.isThanhCong()) {
            // Cập nhật lại session với thông tin mới để header/sidebar hiển thị đồng bộ ngay lập tức
            NguoiDung ndMoi = ketQua.getNguoiDung();
            if (ndMoi != null) {
                session.setAttribute(PhienService.SESSION_USER_KEY, ndMoi);
            }
            response.sendRedirect(request.getContextPath() + "/ho-so?thanhCong=true");
        } else {
            // Khi có lỗi validation hoặc bị từ chối quyền đổi email/nhóm/role:
            // Tải dữ liệu gốc từ DB nhưng giữ lại giá trị người dùng vừa nhập để sửa tiếp
            NguoiDung thongTinGoc = hoSoService.layHoSo(nguoiDungHienTai.getId());
            if (thongTinGoc != null) {
                thongTinGoc.setHoTen(hoTen);
                thongTinGoc.setSoDienThoai(soDienThoai);
                thongTinGoc.setChuKyEmail(chuKyEmail);
                request.setAttribute("nguoiDung", thongTinGoc);
            } else {
                request.setAttribute("nguoiDung", nguoiDungHienTai);
            }

            request.setAttribute("formError", ketQua.getDanhSachLoi());
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/ho-so.jsp").forward(request, response);
        }
    }

    private NguoiDung layNguoiDungDangNhap(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object obj = session.getAttribute(PhienService.SESSION_USER_KEY);
        if (obj instanceof NguoiDung) {
            return (NguoiDung) obj;
        }
        // Hỗ trợ fallback key nếu có
        obj = session.getAttribute("nguoiDung");
        if (obj instanceof NguoiDung) {
            return (NguoiDung) obj;
        }
        obj = session.getAttribute("user");
        if (obj instanceof NguoiDung) {
            return (NguoiDung) obj;
        }
        return null;
    }

    private Integer parseInteger(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
