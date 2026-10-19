package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.nhom10.crm.dto.ThongTinLoi;
import vn.nhom10.crm.service.BaoLoiService;

import java.io.IOException;

/**
 * Controller trung tâm tiếp nhận và điều hướng toàn bộ các trang báo lỗi trong ứng dụng.
 * Phục vụ Story S1-07:
 * - Tiếp nhận các lỗi HTTP từ container hoặc ứng dụng chủ động forward.
 * - Chuẩn hóa thông tin hiển thị và cung cấp hành động gợi ý quay lại công việc.
 */
@WebServlet(name = "ErrorHandlerServlet", urlPatterns = {
        "/loi",
        "/loi-400",
        "/loi-401",
        "/loi-403",
        "/loi-404",
        "/loi-500"
})
public class ErrorHandlerServlet extends HttpServlet {

    private final BaoLoiService baoLoiService = new BaoLoiService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // Lấy thông tin lỗi từ RequestDispatcher attributes
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        // Nếu gọi trực tiếp qua URL mapping (/loi-403, /loi-404, /loi-500...)
        String servletPath = request.getServletPath();
        if (statusCode == null || statusCode == 0) {
            if ("/loi-403".equals(servletPath)) {
                statusCode = HttpServletResponse.SC_FORBIDDEN;
            } else if ("/loi-404".equals(servletPath)) {
                statusCode = HttpServletResponse.SC_NOT_FOUND;
            } else if ("/loi-401".equals(servletPath)) {
                statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            } else if ("/loi-400".equals(servletPath)) {
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
            } else {
                statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }

        // Kiểm tra thông điệp lỗi tùy chỉnh được set trước đó qua attribute
        String customMessage = (String) request.getAttribute("thongBaoLoiTuyChon");
        if (customMessage == null && message != null && !message.trim().isEmpty()) {
            customMessage = message;
        }

        if (requestUri == null) {
            requestUri = (String) request.getAttribute("uriBiLoi");
        }

        ThongTinLoi thongTinLoi = baoLoiService.taoThongTinLoi(
                statusCode,
                requestUri,
                exception,
                request.getContextPath(),
                customMessage
        );

        // Đặt mã trạng thái HTTP thực tế cho response
        response.setStatus(thongTinLoi.getMaLoi());

        request.setAttribute("thongTinLoi", thongTinLoi);
        request.getRequestDispatcher("/WEB-INF/views/error/bao-loi.jsp").forward(request, response);
    }
}
