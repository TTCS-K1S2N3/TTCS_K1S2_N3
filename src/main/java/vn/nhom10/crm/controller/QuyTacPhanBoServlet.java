package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.KhuVucDiaLyDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dto.KetQuaPhanBoDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.dto.PhanBoLeadRequestDTO;
import vn.nhom10.crm.dto.QuyTacPhanBoDTO;
import vn.nhom10.crm.model.KhuVucDiaLy;
import vn.nhom10.crm.model.LoaiDanhMuc;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.LeadAssignmentBackgroundWorker;
import vn.nhom10.crm.service.QuyTacPhanBoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet điều khiển cấu hình quy tắc phân bổ lead tự động (Story S4-06).
 * Phân quyền (RBAC): Chỉ dành cho Giám đốc kinh doanh (DIRECTOR) và Quản trị viên (ADMIN).
 */
@WebServlet(name = "QuyTacPhanBoServlet", urlPatterns = {
        "/admin/lead/phan-bo",
        "/admin/lead/phan-bo/tao-moi",
        "/admin/lead/phan-bo/chinh-sua",
        "/admin/lead/phan-bo/doi-thu-tu",
        "/admin/lead/phan-bo/doi-trang-thai",
        "/admin/lead/phan-bo/xoa",
        "/admin/lead/phan-bo/chay-ngay",
        "/lead/phan-bo"
})
public class QuyTacPhanBoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(QuyTacPhanBoServlet.class.getName());

    private final QuyTacPhanBoService quyTacPhanBoService;
    private final KhuVucDiaLyDAO khuVucDAO;
    private final DanhMucBanHangDAO danhMucDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final LeadAssignmentBackgroundWorker backgroundWorker;

    public QuyTacPhanBoServlet() {
        this(new QuyTacPhanBoService(), new KhuVucDiaLyDAO(), new DanhMucBanHangDAO(),
                new NhomKinhDoanhDAO(), LeadAssignmentBackgroundWorker.getInstance());
    }

    public QuyTacPhanBoServlet(QuyTacPhanBoService quyTacPhanBoService, KhuVucDiaLyDAO khuVucDAO,
                               DanhMucBanHangDAO danhMucDAO, NhomKinhDoanhDAO nhomKinhDoanhDAO,
                               LeadAssignmentBackgroundWorker backgroundWorker) {
        this.quyTacPhanBoService = quyTacPhanBoService;
        this.khuVucDAO = khuVucDAO;
        this.danhMucDAO = danhMucDAO;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
        this.backgroundWorker = backgroundWorker;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!kiemTraQuyen(request, response)) {
            return;
        }

        hienThiTrangQuanLy(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!kiemTraQuyen(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/lead/phan-bo/tao-moi".equals(path)) {
            xuLyTaoMoi(request, response);
        } else if ("/admin/lead/phan-bo/chinh-sua".equals(path)) {
            xuLyChinhSua(request, response);
        } else if ("/admin/lead/phan-bo/doi-thu-tu".equals(path)) {
            xuLyDoiThuTu(request, response);
        } else if ("/admin/lead/phan-bo/doi-trang-thai".equals(path)) {
            xuLyDoiTrangThai(request, response);
        } else if ("/admin/lead/phan-bo/xoa".equals(path)) {
            xuLyXoa(request, response);
        } else if ("/admin/lead/phan-bo/chay-ngay".equals(path)) {
            xuLyChayNgay(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/lead/phan-bo");
        }
    }

    private void hienThiTrangQuanLy(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<QuyTacPhanBoDTO> danhSachQuyTac = quyTacPhanBoService.layDanhSachQuyTac(false);
        List<KhuVucDiaLy> danhSachKhuVuc = khuVucDAO.layTatCaDangHoatDong();
        List<MucDanhMucDTO> danhSachNganhNghe = danhMucDAO.layDanhSach(LoaiDanhMuc.NGANH_NGHE, true);
        List<NhomKinhDoanh> danhSachNhomKinhDoanh = nhomKinhDoanhDAO.layTatCaDangHoatDong();
        int soLuongHangCho = quyTacPhanBoService.demLeadHangCho();

        request.setAttribute("danhSachQuyTac", danhSachQuyTac);
        request.setAttribute("danhSachKhuVuc", danhSachKhuVuc);
        request.setAttribute("danhSachNganhNghe", danhSachNganhNghe);
        request.setAttribute("danhSachNhomKinhDoanh", danhSachNhomKinhDoanh);
        request.setAttribute("soLuongHangCho", soLuongHangCho);

        request.getRequestDispatcher("/WEB-INF/views/lead/quy-tac-phan-bo.jsp").forward(request, response);
    }

    private void xuLyTaoMoi(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");

        try {
            PhanBoLeadRequestDTO dto = docRequestDTO(request);
            quyTacPhanBoService.taoQuyTac(dto, user.getId());

            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_OK, true, "Tạo quy tắc phân bổ thành công.", null);
            } else {
                session.setAttribute("successMessage", "Tạo quy tắc phân bổ thành công.");
                response.sendRedirect(request.getContextPath() + "/admin/lead/phan-bo");
            }
        } catch (IllegalArgumentException e) {
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, e.getMessage(), null);
            } else {
                request.setAttribute("errorMessage", e.getMessage());
                hienThiTrangQuanLy(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo quy tắc phân bổ: " + e.getMessage(), e);
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi hệ thống: " + e.getMessage(), null);
            } else {
                request.setAttribute("errorMessage", "Lỗi lưu quy tắc: " + e.getMessage());
                hienThiTrangQuanLy(request, response);
            }
        }
    }

    private void xuLyChinhSua(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        NguoiDung user = (NguoiDung) session.getAttribute("nguoiDung");

        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isBlank()) {
                throw new IllegalArgumentException("Thiếu ID quy tắc cần chỉnh sửa.");
            }
            Long id = Long.parseLong(idStr.trim());
            PhanBoLeadRequestDTO dto = docRequestDTO(request);

            quyTacPhanBoService.suaQuyTac(id, dto, user.getId());

            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_OK, true, "Cập nhật quy tắc phân bổ thành công.", null);
            } else {
                session.setAttribute("successMessage", "Cập nhật quy tắc phân bổ thành công.");
                response.sendRedirect(request.getContextPath() + "/admin/lead/phan-bo");
            }
        } catch (IllegalArgumentException e) {
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, e.getMessage(), null);
            } else {
                request.setAttribute("errorMessage", e.getMessage());
                hienThiTrangQuanLy(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi sửa quy tắc: " + e.getMessage(), e);
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi hệ thống: " + e.getMessage(), null);
            } else {
                request.setAttribute("errorMessage", "Lỗi cập nhật quy tắc: " + e.getMessage());
                hienThiTrangQuanLy(request, response);
            }
        }
    }

    private void xuLyDoiThuTu(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String idStr = request.getParameter("id");
            String huong = request.getParameter("huong"); // "LEN" hoặc "XUONG"

            if (idStr == null || huong == null) {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Thiếu tham số id hoặc huong.", null);
                return;
            }

            Long id = Long.parseLong(idStr.trim());
            boolean ok = quyTacPhanBoService.doiThuTuUuTien(id, huong);
            if (ok) {
                traVeJson(response, HttpServletResponse.SC_OK, true, "Đã cập nhật thứ tự ưu tiên.", null);
            } else {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Không thể thay đổi thứ tự ưu tiên.", null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi đổi thứ tự: " + e.getMessage(), e);
            traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi: " + e.getMessage(), null);
        }
    }

    private void xuLyDoiTrangThai(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String idStr = request.getParameter("id");
            String hoatDongStr = request.getParameter("hoat_dong");

            if (idStr == null || hoatDongStr == null) {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Thiếu tham số id hoặc hoat_dong.", null);
                return;
            }

            Long id = Long.parseLong(idStr.trim());
            boolean hoatDong = "1".equals(hoatDongStr) || "true".equalsIgnoreCase(hoatDongStr);
            boolean ok = quyTacPhanBoService.doiTrangThai(id, hoatDong);

            if (ok) {
                traVeJson(response, HttpServletResponse.SC_OK, true, "Đã cập nhật trạng thái quy tắc.", null);
            } else {
                traVeJson(response, HttpServletResponse.SC_BAD_REQUEST, false, "Không thể cập nhật trạng thái.", null);
            }
        } catch (Exception e) {
            traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi: " + e.getMessage(), null);
        }
    }

    private void xuLyXoa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                quyTacPhanBoService.xoaQuyTac(Long.parseLong(idStr.trim()));
            }
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_OK, true, "Đã xóa quy tắc phân bổ.", null);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/lead/phan-bo");
            }
        } catch (Exception e) {
            traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi: " + e.getMessage(), null);
        }
    }

    private void xuLyChayNgay(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            KetQuaPhanBoDTO kq = backgroundWorker.chayNgay(50);
            String msg = String.format("Đã quét %d lead: %d phân bổ thành công, %d vào hàng chờ (xử lý trong %d ms).",
                    kq.getSoLeadDaQuet(), kq.getSoLeadPhanBoThanhCong(), kq.getSoLeadVaoHangCho(), kq.getThoiGianXuLyMs());
            traVeJson(response, HttpServletResponse.SC_OK, true, msg, kq);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi chạy phân bổ ngay: " + e.getMessage(), e);
            traVeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi: " + e.getMessage(), null);
        }
    }

    private PhanBoLeadRequestDTO docRequestDTO(HttpServletRequest request) {
        PhanBoLeadRequestDTO dto = new PhanBoLeadRequestDTO();
        dto.setTenQuyTac(request.getParameter("ten_quy_tac"));
        dto.setKieuPhanBo(request.getParameter("kieu_phan_bo"));

        // Khu vực IDs
        String[] kvArr = request.getParameterValues("khu_vuc_ids");
        if (kvArr != null) {
            List<Long> kvList = new ArrayList<>();
            for (String kv : kvArr) {
                try {
                    kvList.add(Long.parseLong(kv.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            dto.setKhuVucIds(kvList);
        }

        // Ngành nghề IDs
        String[] nnArr = request.getParameterValues("nganh_nghe_ids");
        if (nnArr != null) {
            List<Long> nnList = new ArrayList<>();
            for (String nn : nnArr) {
                try {
                    nnList.add(Long.parseLong(nn.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            dto.setNganhNgheIds(nnList);
        }

        // Nhóm đích
        String nhomDichStr = request.getParameter("nhom_dich_id");
        if (nhomDichStr != null && !nhomDichStr.isBlank()) {
            try {
                dto.setNhomDichId(Long.parseLong(nhomDichStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        // Người dùng đích
        String ndDichStr = request.getParameter("nguoi_dung_dich_id");
        if (ndDichStr != null && !ndDichStr.isBlank()) {
            try {
                dto.setNguoiDungDichId(Long.parseLong(ndDichStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        String hdStr = request.getParameter("hoat_dong");
        dto.setHoatDong(!"0".equals(hdStr) && !"false".equalsIgnoreCase(hdStr));

        return dto;
    }

    private boolean kiemTraQuyen(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        NguoiDung nguoiDung = (session != null) ? (NguoiDung) session.getAttribute("nguoiDung") : null;

        if (nguoiDung == null) {
            response.sendRedirect(request.getContextPath() + "/dang-nhap");
            return false;
        }

        // Chỉ Giám đốc kinh doanh (DIRECTOR) và Quản trị viên (ADMIN) mới có quyền cấu hình
        boolean coQuyen = nguoiDung.coVaiTro(VaiTroEnum.DIRECTOR) || nguoiDung.coVaiTro(VaiTroEnum.ADMIN);
        if (!coQuyen) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isAjaxRequest(request)) {
                traVeJson(response, HttpServletResponse.SC_FORBIDDEN, false,
                        "Bạn không có quyền truy cập vào chức năng cấu hình quy tắc phân bổ lead.", null);
            } else {
                request.setAttribute("errorMessage", "Bạn không có quyền truy cập vào chức năng cấu hình quy tắc phân bổ lead.");
                request.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(request, response);
            }
            return false;
        }
        return true;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xReq = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xReq) || (accept != null && accept.contains("application/json"));
    }

    private void traVeJson(HttpServletResponse response, int status, boolean success, String message, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);

        StringBuilder json = new StringBuilder("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":\"").append(escapeJson(message)).append("\"");
        if (data instanceof KetQuaPhanBoDTO) {
            KetQuaPhanBoDTO kq = (KetQuaPhanBoDTO) data;
            json.append(",\"data\":{");
            json.append("\"soLeadDaQuet\":").append(kq.getSoLeadDaQuet()).append(",");
            json.append("\"soLeadPhanBoThanhCong\":").append(kq.getSoLeadPhanBoThanhCong()).append(",");
            json.append("\"soLeadVaoHangCho\":").append(kq.getSoLeadVaoHangCho()).append(",");
            json.append("\"thoiGianXuLyMs\":").append(kq.getThoiGianXuLyMs());
            json.append("}");
        }
        json.append("}");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
