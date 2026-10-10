package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.model.VaiTroModule;
import vn.nhom10.crm.service.PermissionService;
import vn.nhom10.crm.service.RolePermissionCeilingPolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller quản lý Ma trận Vai trò & Phân quyền module chức năng (DB-backed).
 * URL: /nguoi-dung/phan-quyen-vai-tro
 *
 * Phân quyền truy cập:
 * - ADMIN: Xem và chỉnh sửa ma trận (GET + POST).
 * - DIRECTOR: Chỉ xem read-only (GET), không được lưu (POST trả về 403).
 * - Các vai trò khác: Chặn toàn bộ (403 Forbidden).
 */
@WebServlet(name = "PhanQuyenVaiTroServlet", urlPatterns = {"/nguoi-dung/phan-quyen-vai-tro"})
public class PhanQuyenVaiTroServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PhanQuyenVaiTroServlet.class.getName());

    private final PermissionService permissionService;
    private final VaiTroDAO vaiTroDAO;

    public PhanQuyenVaiTroServlet() {
        this(PermissionService.getInstance(), new VaiTroDAO());
    }

    public PhanQuyenVaiTroServlet(PermissionService permissionService, VaiTroDAO vaiTroDAO) {
        this.permissionService = permissionService != null ? permissionService : PermissionService.getInstance();
        this.vaiTroDAO = vaiTroDAO != null ? vaiTroDAO : new VaiTroDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        boolean isAdmin = currentUser.coVaiTro(VaiTroEnum.ADMIN) || currentUser.coVaiTro("ADMIN");
        boolean isDirector = currentUser.coVaiTro(VaiTroEnum.DIRECTOR) || currentUser.coVaiTro("DIRECTOR");

        // Chỉ Admin và Director mới được vào trang ma trận phân quyền
        if (!isAdmin && !isDirector) {
            tuChoiTruyCap(request, response, "Chỉ Quản trị hệ thống (Admin) hoặc Giám đốc kinh doanh (Director) mới có quyền truy cập trang phân quyền vai trò.");
            return;
        }

        napDuLieuTrang(request, currentUser, isAdmin, isDirector);
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        NguoiDung currentUser = layNguoiDungHienTai(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap?error=auth_required");
            return;
        }

        boolean isAdmin = currentUser.coVaiTro(VaiTroEnum.ADMIN) || currentUser.coVaiTro("ADMIN");

        // BẢO MẬT SERVER-SIDE: Chỉ ADMIN mới được thực hiện POST (lưu cấu hình phân quyền)
        if (!isAdmin) {
            tuChoiTruyCap(request, response, "Chỉ Quản trị hệ thống (Admin) mới có quyền chỉnh sửa và lưu phân quyền.");
            return;
        }

        String maVaiTro = request.getParameter("maVaiTro");
        if (maVaiTro == null || maVaiTro.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/nguoi-dung/phan-quyen-vai-tro");
            return;
        }

        maVaiTro = maVaiTro.trim().toUpperCase();

        // BẢO VỆ ADMIN: Tuyệt đối không cho phép hạ quyền hoặc sửa đổi vai trò ADMIN
        if (VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(maVaiTro)) {
            request.setAttribute("thongBaoLoi", "Vai trò Quản trị hệ thống (Admin) luôn có toàn quyền và được bảo vệ, không thể sửa đổi.");
            napDuLieuTrang(request, currentUser, true, false);
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp").forward(request, response);
            return;
        }

        VaiTro vaiTro = vaiTroDAO.timTheoMa(maVaiTro);
        if (vaiTro == null) {
            request.setAttribute("thongBaoLoi", "Vai trò '" + maVaiTro + "' không tồn tại trong hệ thống.");
            napDuLieuTrang(request, currentUser, true, false);
            request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp").forward(request, response);
            return;
        }

        List<VaiTroModule> danhSachHienTai = permissionService.layMatrixChoVaiTro(maVaiTro);
        List<VaiTroModule> danhSachCapNhat = new ArrayList<>();
        PhamViDuLieu phamViToiDa = vaiTro.getPhamViToiDa();

        RolePermissionCeilingPolicy ceilingPolicy = RolePermissionCeilingPolicy.getInstance();

        for (VaiTroModule vtm : danhSachHienTai) {
            int modId = vtm.getModuleId();
            String paramLevel = request.getParameter("mucQuyen_" + modId);
            String paramScope = request.getParameter("phamVi_" + modId);

            MucQuyen levelMoi = MucQuyen.tuMa(paramLevel);
            PhamViDuLieu scopeMoi = (paramScope != null && !paramScope.isBlank()) ? PhamViDuLieu.tuMa(paramScope) : null;

            // Server-side validation 1: Khóa trần cấp quyền theo Sheet 2 User Roles
            if (!ceilingPolicy.kiemTraHopLe(maVaiTro, vtm.getMaModule(), levelMoi, scopeMoi)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Cấu hình phân quyền cho module '" + vtm.getTenModule() +
                        "' [Mức: " + levelMoi.getTenHienThi() + ", Phạm vi: " + (scopeMoi != null ? scopeMoi.getTenHienThi() : "Không áp dụng") +
                        "] vượt quá trần tối đa theo quy định của vai trò " + vaiTro.getTenVaiTro() + ".");
                napDuLieuTrang(request, currentUser, true, false);
                request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp").forward(request, response);
                return;
            }

            // Server-side validation 2: Phạm vi không được vượt quá pham_vi_toi_da của vai trò
            if (scopeMoi != null && soSanhPhamVi(scopeMoi, phamViToiDa) > 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("thongBaoLoi", "Phạm vi dữ liệu '" + scopeMoi.getTenHienThi() +
                        "' vượt quá giới hạn tối đa (" + phamViToiDa.getTenHienThi() + ") của vai trò " + vaiTro.getTenVaiTro() + ".");
                napDuLieuTrang(request, currentUser, true, false);
                request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp").forward(request, response);
                return;
            }

            VaiTroModule updatedRow = new VaiTroModule();
            updatedRow.setId(vtm.getId());
            updatedRow.setVaiTroId(vaiTro.getId());
            updatedRow.setModuleId(modId);
            updatedRow.setMaModule(vtm.getMaModule());
            updatedRow.setMucQuyen(levelMoi);
            updatedRow.setPhamViDuLieu(scopeMoi);
            danhSachCapNhat.add(updatedRow);
        }

        int actorId = (int) currentUser.getId();
        String ip = request.getRemoteAddr();
        String thietBi = request.getHeader("User-Agent");

        boolean thanhCong = permissionService.capNhatMatrixChoVaiTro(maVaiTro, danhSachCapNhat, actorId, ip, thietBi);

        if (thanhCong) {
            request.setAttribute("thongBaoThanhCong", "Cập nhật phân quyền thành công cho vai trò " + vaiTro.getTenVaiTro() + ".");
        } else {
            request.setAttribute("thongBaoLoi", "Có lỗi xảy ra khi lưu phân quyền vào cơ sở dữ liệu.");
        }

        napDuLieuTrang(request, currentUser, true, false);
        request.getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp").forward(request, response);
    }

    private void napDuLieuTrang(HttpServletRequest request, NguoiDung currentUser, boolean isAdmin, boolean isDirector) {
        List<VaiTro> dsVaiTro = vaiTroDAO.layTatCa();
        if (dsVaiTro == null || dsVaiTro.isEmpty()) {
            // Nạp 7 vai trò chuẩn nếu DAO trả về rỗng trong test
            dsVaiTro = new ArrayList<>();
            for (VaiTroEnum vte : VaiTroEnum.values()) {
                dsVaiTro.add(new VaiTro(vte));
            }
        }

        String paramRole = request.getParameter("vaiTro");
        if (paramRole == null || paramRole.isBlank()) {
            paramRole = "SALES_REP"; // Mặc định mở vai trò nhân viên kinh doanh
        }

        VaiTro selectedRole = null;
        for (VaiTro vt : dsVaiTro) {
            if (vt.getMaVaiTro().equalsIgnoreCase(paramRole.trim())) {
                selectedRole = vt;
                break;
            }
        }
        if (selectedRole == null && !dsVaiTro.isEmpty()) {
            selectedRole = dsVaiTro.get(0);
        }

        List<VaiTroModule> matrix = selectedRole != null
                ? permissionService.layMatrixChoVaiTro(selectedRole.getMaVaiTro())
                : new ArrayList<>();

        boolean isRoleAdmin = selectedRole != null && VaiTroEnum.ADMIN.getMaVaiTro().equalsIgnoreCase(selectedRole.getMaVaiTro());
        // Chế độ read-only khi người xem là Director hoặc đang xem vai trò Admin
        boolean isReadOnly = isDirector || isRoleAdmin;

        request.setAttribute("dsVaiTro", dsVaiTro);
        request.setAttribute("selectedRole", selectedRole);
        request.setAttribute("matrixRows", matrix);
        request.setAttribute("isReadOnly", isReadOnly);
        request.setAttribute("isAdmin", isAdmin);
        request.setAttribute("isDirector", isDirector);
        request.setAttribute("isRoleAdmin", isRoleAdmin);
        request.setAttribute("nguoiDungHienTai", currentUser);
    }

    private NguoiDung layNguoiDungHienTai(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object obj = session.getAttribute("nguoiDung");
        if (obj instanceof NguoiDung) {
            return (NguoiDung) obj;
        }
        return null;
    }

    private void tuChoiTruyCap(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
    }

    private int soSanhPhamVi(PhamViDuLieu a, PhamViDuLieu b) {
        int valA = getScopeWeight(a);
        int valB = getScopeWeight(b);
        return Integer.compare(valA, valB);
    }

    private int getScopeWeight(PhamViDuLieu p) {
        if (p == null) return 0;
        switch (p) {
            case CA_NHAN: return 1;
            case NHOM: return 2;
            case TOAN_BO: return 3;
            default: return 0;
        }
    }
}
