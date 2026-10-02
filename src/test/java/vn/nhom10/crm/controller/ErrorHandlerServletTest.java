package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.nhom10.crm.dto.ThongTinLoi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Controller xử lý lỗi ErrorHandlerServlet")
class ErrorHandlerServletTest {

    @Test
    @DisplayName("Xử lý lỗi 403: Forward đến bao-loi.jsp, đặt status 403 và chuẩn bị hành động gợi ý")
    void testProcessError403() throws Exception {
        ErrorHandlerServlet servlet = new ErrorHandlerServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(403);
        when(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).thenReturn("/admin/users");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getServletPath()).thenReturn("/loi-403");
        when(request.getRequestDispatcher("/WEB-INF/views/error/bao-loi.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        // Xác nhận HTTP response status được đặt là 403
        verify(response).setStatus(403);

        // Xác nhận thongTinLoi attribute được đưa vào request
        ArgumentCaptor<ThongTinLoi> captor = ArgumentCaptor.forClass(ThongTinLoi.class);
        verify(request).setAttribute(eq("thongTinLoi"), captor.capture());

        ThongTinLoi thongTin = captor.getValue();
        assertEquals(403, thongTin.getMaLoi());
        assertNotNull(thongTin.getUrlHanhDongChinh());
        assertNotNull(thongTin.getTenHanhDongChinh());

        // Xác nhận forward đến view bao-loi.jsp
        verify(request).getRequestDispatcher("/WEB-INF/views/error/bao-loi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Xử lý lỗi 404: Forward đến bao-loi.jsp, đặt status 404 và gợi ý quay về trang chủ")
    void testProcessError404() throws Exception {
        ErrorHandlerServlet servlet = new ErrorHandlerServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(404);
        when(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI)).thenReturn("/trang-khong-ton-tai");
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getServletPath()).thenReturn("/loi-404");
        when(request.getRequestDispatcher("/WEB-INF/views/error/bao-loi.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(response).setStatus(404);

        ArgumentCaptor<ThongTinLoi> captor = ArgumentCaptor.forClass(ThongTinLoi.class);
        verify(request).setAttribute(eq("thongTinLoi"), captor.capture());

        ThongTinLoi thongTin = captor.getValue();
        assertEquals(404, thongTin.getMaLoi());
        assertEquals("/crm", thongTin.getUrlHanhDongChinh());

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Xử lý lỗi 500: Forward đến bao-loi.jsp, đặt status 500 và có mã tham chiếu sự cố")
    void testProcessError500() throws Exception {
        ErrorHandlerServlet servlet = new ErrorHandlerServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);
        when(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).thenReturn(new NullPointerException("Null reference"));
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getServletPath()).thenReturn("/loi-500");
        when(request.getRequestDispatcher("/WEB-INF/views/error/bao-loi.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(response).setStatus(500);

        ArgumentCaptor<ThongTinLoi> captor = ArgumentCaptor.forClass(ThongTinLoi.class);
        verify(request).setAttribute(eq("thongTinLoi"), captor.capture());

        ThongTinLoi thongTin = captor.getValue();
        assertEquals(500, thongTin.getMaLoi());
        assertNotNull(thongTin.getMaThamChieu());

        verify(dispatcher).forward(request, response);
    }
}
