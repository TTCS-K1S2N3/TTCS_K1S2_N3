package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử Controller QuenMatKhauServlet và DatLaiMatKhauServlet")
class AuthServletTest {

    @Test
    @DisplayName("QuenMatKhauServlet: doGet forward đến quen-mat-khau.jsp")
    void testQuenMatKhauDoGet() throws Exception {
        QuenMatKhauServlet servlet = new QuenMatKhauServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher("/WEB-INF/views/auth/quen-mat-khau.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/auth/quen-mat-khau.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("DatLaiMatKhauServlet: doGet thiếu token forward với thông báo lỗi")
    void testDatLaiMatKhauDoGetThieuToken() throws Exception {
        DatLaiMatKhauServlet servlet = new DatLaiMatKhauServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getParameter("token")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/auth/dat-lai-mat-khau.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("tokenHopLe"), eq(false));
        verify(request).setAttribute(eq("thongBaoLoi"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("DangNhapServlet: doGet forward đến dang-nhap.jsp")
    void testDangNhapDoGet() throws Exception {
        DangNhapServlet servlet = new DangNhapServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/auth/dang-nhap.jsp");
        verify(dispatcher).forward(request, response);
    }
}
