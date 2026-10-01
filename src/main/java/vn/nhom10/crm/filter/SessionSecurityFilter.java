package vn.nhom10.crm.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;

/**
 * Filter bảo vệ phiên đăng nhập và tự động gia hạn khi còn hoạt động (AC1, AC2, AC3).
 * Mọi phiên không còn ở trạng thái HOAT_DONG trong database (hết hạn, đăng xuất hoặc bị thu hồi)
 * đều sẽ bị từ chối truy cập và chuyển hướng về đăng nhập an toàn.
 * Áp dụng cho toàn bộ ứng dụng CRM.
 */
@WebFilter(filterName = "SessionSecurityFilter", urlPatterns = {"/*"})
public class SessionSecurityFilter implements Filter {

    private PhienService phienService;

    @Override
    public void init(FilterConfig filterConfig) {
        if (this.phienService == null) {
            this.phienService = new PhienService();
        }
    }

    public void setPhienService(PhienService phienService) {
        this.phienService = phienService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        // Kiểm tra đường dẫn công khai (không yêu cầu đăng nhập)
        if (laDuongDanCongKhai(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Thiết lập header bảo mật chống cache trang quản trị khi đăng xuất/hết hạn
        datHeaderChongCache(httpResponse);

        HttpSession session = httpRequest.getSession(false);
        boolean coPhien = (session != null && session.getAttribute(PhienService.SESSION_USER_KEY) != null);

        if (!coPhien) {
            xuLyPhienKhongHopLe(httpRequest, httpResponse, "session_expired");
            return;
        }

        NguoiDung nguoiDung = (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
        String maPhien = (String) session.getAttribute(PhienService.SESSION_TOKEN_KEY);
        if (maPhien == null) {
            maPhien = session.getId();
        }

        // Kiểm tra tính hợp lệ của phiên trong DB (AC3).
        // Mọi phiên không còn HOAT_DONG (hết hạn, đăng xuất hoặc đã bị thu hồi) đều không hợp lệ.
        boolean phienHopLe = phienService.kiemTraPhienHopLe(maPhien);
        if (!phienHopLe) {
            // Hủy phiên server ngay lập tức
            phienService.dangXuat(httpRequest, httpResponse);
            xuLyPhienKhongHopLe(httpRequest, httpResponse, "session_expired");
            return;
        }

        // Tự động gia hạn phiên khi còn hoạt động (AC1)
        phienService.giaHanPhien(maPhien, session);

        chain.doFilter(request, response);
    }

    private boolean laDuongDanCongKhai(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return true;
        }

        return path.startsWith("/assets/")
                || path.startsWith("/dang-nhap")
                || path.startsWith("/dang-xuat")
                || path.startsWith("/quen-mat-khau")
                || path.startsWith("/dat-lai-mat-khau")
                || path.startsWith("/favicon.ico");
    }

    private void datHeaderChongCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies
    }

    private void xuLyPhienKhongHopLe(HttpServletRequest request, HttpServletResponse response, String lyDo)
            throws IOException {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        boolean laAjax = "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
                || (acceptHeader != null && acceptHeader.contains("application/json"));

        if (laAjax) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"thanhCong\":false,\"maLoi\":\"SESSION_EXPIRED\",\"thongBao\":\"Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.\"}");
        } else {
            String contextPath = request.getContextPath();
            String redirectUrl = contextPath + "/dang-nhap?error=" + lyDo;
            response.sendRedirect(redirectUrl);
        }
    }

    @Override
    public void destroy() {
    }
}
