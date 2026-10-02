package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.HoSoService;
import vn.nhom10.crm.service.MenuService;

import java.io.IOException;

/**
 * Controller hiển thị trang Hồ sơ cá nhân của người dùng.
 * Phục vụ Acceptance Criteria:
 * - Hiển thị tên, vai trò và nhóm kinh doanh đang thuộc về
 * - Mục menu không thuộc quyền thì không hiển thị
 * - Tải lên ảnh đại diện và hiển thị ảnh vuông + thumbnail
 * - Dùng được thuận tiện trên màn hình 360px
 */
@WebServlet(name = "HoSoServlet", urlPatterns = {"/ho-so"})
public class HoSoServlet extends HttpServlet {

    private HoSoService hoSoService;
    private MenuService menuService;
    private NguoiDungDAO nguoiDungDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.hoSoService = new HoSoService();
        this.menuService = MenuService.getInstance();
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public HoSoServlet(HoSoService hoSoService, MenuService menuService, NguoiDungDAO nguoiDungDAO) {
        this.hoSoService = hoSoService;
        this.menuService = menuService;
        this.nguoiDungDAO = nguoiDungDAO;
    }

    public HoSoServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);

        // Hỗ trợ kiểm thử chuyển đổi vai trò nhanh qua query param: ?switchRole=ADMIN / SALES_REP / TEAM_LEAD
        String switchRoleParam = req.getParameter("switchRole");
        if (switchRoleParam != null && !switchRoleParam.isBlank()) {
            NguoiDung userDemo = taoUserDemoTheoVaiTro(switchRoleParam.trim().toUpperCase());
            session.setAttribute("user", userDemo);
        }

        NguoiDung nguoiDungHienTai = (NguoiDung) session.getAttribute("user");

        // Nếu chưa đăng nhập và trong DB có sẵn user, lấy user id=1; nếu không có DB thì dùng tài khoản demo
        if (nguoiDungHienTai == null) {
            nguoiDungHienTai = nguoiDungDAO.timTheoId(1);
            if (nguoiDungHienTai == null) {
                nguoiDungHienTai = taoUserDemoTheoVaiTro("SALES_REP");
            }
            session.setAttribute("user", nguoiDungHienTai);
        } else {
            // Đồng bộ lại từ DB để lấy avatar mới nhất nếu có ID thực
            if (nguoiDungHienTai.getId() > 0) {
                NguoiDung capNhat = nguoiDungDAO.timTheoId(nguoiDungHienTai.getId());
                if (capNhat != null) {
                    nguoiDungHienTai = capNhat;
                    session.setAttribute("user", nguoiDungHienTai);
                }
            }
        }

        // Lấy thông tin điều hướng bao gồm tên, vai trò, nhóm kinh doanh, avatar và danh sách menu theo quyền
        String currentUri = req.getRequestURI();
        ThongTinDieuHuongDTO thongTinDieuHuong = menuService.layThongTinDieuHuong(
                nguoiDungHienTai,
                currentUri,
                req.getContextPath()
        );

        // Lấy thông báo flash nếu có từ upload trước đó
        String flashSuccess = (String) session.getAttribute("flashMessageSuccess");
        String flashError = (String) session.getAttribute("flashMessageError");
        if (flashSuccess != null) {
            req.setAttribute("thongBaoThanhCong", flashSuccess);
            session.removeAttribute("flashMessageSuccess");
        }
        if (flashError != null) {
            req.setAttribute("thongBaoLoi", flashError);
            session.removeAttribute("flashMessageError");
        }

        req.setAttribute("nguoiDung", nguoiDungHienTai);
        req.setAttribute("dieuHuong", thongTinDieuHuong);

        req.getRequestDispatcher("/WEB-INF/views/ho-so/ho-so-ca-nhan.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);
        NguoiDung nguoiDungHienTai = (NguoiDung) session.getAttribute("user");

        if (nguoiDungHienTai == null) {
            resp.sendRedirect(req.getContextPath() + "/ho-so");
            return;
        }

        String action = req.getParameter("action");
        if ("capNhatThongTin".equalsIgnoreCase(action)) {
            String hoTen = req.getParameter("hoTen");
            String soDienThoai = req.getParameter("soDienThoai");
            String chuKyEmail = req.getParameter("chuKyEmail");

            if (hoTen == null || hoTen.trim().isBlank()) {
                session.setAttribute("flashMessageError", "Họ và tên không được để trống.");
            } else {
                boolean capNhatDb = hoSoService.capNhatHoSo((int) nguoiDungHienTai.getId(), hoTen.trim(), soDienThoai, chuKyEmail);
                if (capNhatDb || nguoiDungHienTai.getId() <= 0) {
                    nguoiDungHienTai.setHoTen(hoTen.trim());
                    nguoiDungHienTai.setSoDienThoai(soDienThoai);
                    nguoiDungHienTai.setChuKyEmail(chuKyEmail);
                    session.setAttribute("user", nguoiDungHienTai);
                    session.setAttribute("flashMessageSuccess", "Cập nhật thông tin hồ sơ thành công!");
                } else {
                    session.setAttribute("flashMessageError", "Cập nhật vào cơ sở dữ liệu thất bại.");
                }
            }
        }

        resp.sendRedirect(req.getContextPath() + "/ho-so");
    }

    private NguoiDung taoUserDemoTheoVaiTro(String maVaiTro) {
        VaiTroEnum vtEnum = VaiTroEnum.tuMa(maVaiTro);
        if (vtEnum == null) {
            vtEnum = VaiTroEnum.SALES_REP;
        }

        NguoiDung nd = new NguoiDung();
        nd.setId(1);
        nd.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);

        NhomKinhDoanh nhom = new NhomKinhDoanh(1, "KD_MIEN_BAC", "Nhóm Kinh Doanh Miền Bắc", "Phụ trách thị trường phía Bắc", null);
        nd.setNhomKinhDoanh(nhom);
        nd.setNhomKinhDoanhId(1);

        switch (vtEnum) {
            case ADMIN:
                nd.setHoTen("Nguyễn Quản Trị");
                nd.setEmail("admin@crm.vn");
                nd.setSoDienThoai("0901234567");
                nd.themVaiTro(new VaiTro(VaiTroEnum.ADMIN));
                break;
            case DIRECTOR:
                nd.setHoTen("Trần Giám Đốc");
                nd.setEmail("director@crm.vn");
                nd.setSoDienThoai("0912345678");
                nd.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));
                break;
            case TEAM_LEAD:
                nd.setHoTen("Lê Trưởng Nhóm");
                nd.setEmail("teamlead@crm.vn");
                nd.setSoDienThoai("0923456789");
                nd.themVaiTro(new VaiTro(VaiTroEnum.TEAM_LEAD));
                break;
            case MARKETING:
                nd.setHoTen("Phạm Marketing");
                nd.setEmail("marketing@crm.vn");
                nd.setSoDienThoai("0934567890");
                nd.themVaiTro(new VaiTro(VaiTroEnum.MARKETING));
                break;
            default:
                nd.setHoTen("Thào A Khua");
                nd.setEmail("khua.thao@crm.vn");
                nd.setSoDienThoai("0987654321");
                nd.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
                break;
        }

        nd.setChuKyEmail("--\nTrân trọng,\n" + nd.getHoTen() + " | " + nd.getChuoiVaiTroHienThi() + "\nĐiện thoại: " + nd.getSoDienThoai());
        return nd;
    }
}
