package vn.nhom10.crm.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dto.ThongTinDieuHuongDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.MenuService;

import java.io.IOException;

/**
 * Filter tự động nạp thông tin điều hướng (menu, vai trò, nhóm kinh doanh) vào request
 * và thực thi kiểm tra phân quyền truy cập server-side.
 */
@WebFilter(filterName = "NavigationFilter", urlPatterns = {"/*"})
public class NavigationFilter implements Filter {

    private MenuService menuService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.menuService = MenuService.getInstance();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();
        String relativeUri = uri.substring(contextPath.length());

        // Bỏ qua tài nguyên tĩnh (CSS, JS, hình ảnh, font)
        if (laTaiNguyenTinh(relativeUri)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(true);

        // Lấy thông tin người dùng từ session
        NguoiDung nguoiDung = (NguoiDung) session.getAttribute("nguoiDung");
        if (nguoiDung == null) {
            nguoiDung = (NguoiDung) session.getAttribute("currentUser");
        }

        // Hỗ trợ chuyển đổi vai trò nhanh trên giao diện kiểm thử qua tham số demoRole
        String demoRoleParam = httpRequest.getParameter("demoRole");
        if (demoRoleParam != null && !demoRoleParam.isBlank()) {
            nguoiDung = taoNguoiDungMauTheoVaiTro(demoRoleParam);
            session.setAttribute("nguoiDung", nguoiDung);
        }

        // Chuẩn bị dữ liệu hiển thị cho menu và thông tin người dùng
        ThongTinDieuHuongDTO thongTinDieuHuong = menuService.layThongTinDieuHuong(nguoiDung, relativeUri);
        httpRequest.setAttribute("thongTinDieuHuong", thongTinDieuHuong);

        // Kiểm tra phân quyền truy cập phía server (Server-side authorization)
        // Không chỉ ẩn menu ở UI, người dùng gõ trực tiếp URL không thuộc quyền phải bị từ chối
        if (nguoiDung != null && !menuService.kiemTraQuyenTruyCapUrl(nguoiDung, relativeUri)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpRequest.setAttribute("errorMessage", "Bạn không có quyền truy cập vào chức năng này theo vai trò của bạn.");
            httpRequest.getRequestDispatcher("/WEB-INF/views/common/403.jsp").forward(httpRequest, httpResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean laTaiNguyenTinh(String uri) {
        String lower = uri.toLowerCase();
        return lower.startsWith("/assets/")
                || lower.endsWith(".css")
                || lower.endsWith(".js")
                || lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".svg")
                || lower.endsWith(".ico")
                || lower.endsWith(".woff")
                || lower.endsWith(".woff2")
                || lower.endsWith(".ttf");
    }

    /**
     * Tiện ích tạo nhanh người dùng mẫu theo vai trò để kiểm thử trực quan trên môi trường dev.
     */
    public static NguoiDung taoNguoiDungMauTheoVaiTro(String maVaiTro) {
        NguoiDung nd = new NguoiDung();
        nd.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);

        VaiTroEnum vtEnum = VaiTroEnum.tuMa(maVaiTro);
        if (vtEnum == null) {
            vtEnum = VaiTroEnum.SALES_REP;
        }

        switch (vtEnum) {
            case ADMIN -> {
                nd.setId(1);
                nd.setHoTen("Nguyễn Quản Trị");
                nd.setEmail("admin@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(1, "Ban Quản Trị Hệ Thống"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.ADMIN));
            }
            case DIRECTOR -> {
                nd.setId(2);
                nd.setHoTen("Trần Giám Đốc");
                nd.setEmail("director@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(2, "Ban Giám Đốc Kinh Doanh"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.DIRECTOR));
            }
            case TEAM_LEAD -> {
                nd.setId(3);
                nd.setHoTen("Lê Trưởng Nhóm");
                nd.setEmail("teamlead@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(3, "Nhóm Kinh Doanh Miền Bắc"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.TEAM_LEAD));
            }
            case SALES_REP -> {
                nd.setId(4);
                nd.setHoTen("Thào A Khua");
                nd.setEmail("sales@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(3, "Nhóm Kinh Doanh Miền Bắc"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.SALES_REP));
            }
            case MARKETING -> {
                nd.setId(5);
                nd.setHoTen("Phạm Marketing");
                nd.setEmail("marketing@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(4, "Phòng Marketing"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.MARKETING));
            }
            case CUST_SUCCESS -> {
                nd.setId(6);
                nd.setHoTen("Hoàng CSKH");
                nd.setEmail("cskh@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(5, "Phòng Chăm Sóc Khách Hàng"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.CUST_SUCCESS));
            }
            case ACCOUNTANT -> {
                nd.setId(7);
                nd.setHoTen("Đỗ Kế Toán");
                nd.setEmail("accountant@crm.vn");
                nd.setNhomKinhDoanh(new NhomKinhDoanh(6, "Phòng Tài Chính Kế Toán"));
                nd.themVaiTro(new VaiTro(VaiTroEnum.ACCOUNTANT));
            }
        }
        return nd;
    }
}
