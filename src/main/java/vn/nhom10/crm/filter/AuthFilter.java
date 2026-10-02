package vn.nhom10.crm.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter bảo vệ các tuyến đường nội bộ của CRM.
 * Tuân thủ quy tắc bảo mật và static assets safety:
 * - /assets/** không bao giờ bị redirect hoặc chặn.
 * - /login và /logout được phép truy cập công khai.
 * - Chưa đăng nhập khi vào /home hoặc các tài nguyên bảo vệ sẽ chuyển hướng về /login.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        // 1. Tuyệt đối không chặn hoặc chuyển hướng static assets (/assets/**)
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Cho phép các route công khai liên quan đến xác thực
        if (path.equals("/login") || path.equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Kiểm tra phiên đăng nhập của người dùng
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            // Chưa đăng nhập -> chuyển hướng về trang đăng nhập
            httpResponse.sendRedirect(contextPath + "/login");
        }
    }

    @Override
    public void destroy() {
    }
}
