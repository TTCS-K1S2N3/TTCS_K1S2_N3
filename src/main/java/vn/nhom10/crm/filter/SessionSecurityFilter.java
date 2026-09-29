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
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhienDangNhap;

import java.io.IOException;

/**
 * Filter kiểm tra tính hợp lệ của phiên đăng nhập (AC 3: Thu hồi phiên).
 * Nếu phiên của người dùng đã bị thu hồi trong cơ sở dữ liệu sau khi đổi mật khẩu,
 * yêu cầu sẽ bị chặn và chuyển hướng về trang đăng nhập kèm thông báo.
 */
@WebFilter(filterName = "SessionSecurityFilter", urlPatterns = {"/*"})
public class SessionSecurityFilter implements Filter {

    private PhienDangNhapDAO phienDangNhapDAO;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }

    public void setPhienDangNhapDAO(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativeUri = uri.substring(contextPath.length());

        // Bỏ qua tài nguyên tĩnh và trang đăng nhập
        if (laTaiNguyenTinh(relativeUri) || relativeUri.startsWith("/dang-nhap")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            NguoiDung nguoiDung = (NguoiDung) session.getAttribute("nguoiDung");
            if (nguoiDung == null) {
                nguoiDung = (NguoiDung) session.getAttribute("currentUser");
            }

            if (nguoiDung != null) {
                String maPhien = session.getId();
                PhienDangNhap phien = phienDangNhapDAO.findByMaPhien(maPhien);

                // Nếu phiên đã bị thu hồi trong DB (do đổi mật khẩu ở thiết bị khác)
                if (phien != null && phien.isDaThuHoi()) {
                    session.invalidate();
                    httpResponse.sendRedirect(contextPath + "/dang-nhap?error=session_revoked");
                    return;
                }
            }
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
