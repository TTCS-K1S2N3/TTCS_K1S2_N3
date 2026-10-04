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

        HttpSession session = httpRequest.getSession(false);

        // Lấy thông tin người dùng từ session chuẩn duy nhất "nguoiDung"
        NguoiDung nguoiDung = (session != null) ? (NguoiDung) session.getAttribute("nguoiDung") : null;

        // Chuẩn bị dữ liệu hiển thị cho menu và thông tin người dùng
        ThongTinDieuHuongDTO thongTinDieuHuong = menuService.layThongTinDieuHuong(nguoiDung, relativeUri, contextPath);
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
}
