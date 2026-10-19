package vn.nhom10.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiaHanPhienServletTest {

    @Mock
    private PhienService phienService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private GiaHanPhienServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new GiaHanPhienServlet();
        servlet.setPhienService(phienService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("AC1: Khi phiên còn hoạt động, API gia hạn trả về 200 kèm JSON xác nhận")
    void testGiaHanThanhCongKhiPhienHoatDong() throws IOException {
        String maPhien = "SESSION-123";
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setEmail("sales@congty.vn");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDung);
        when(session.getAttribute(PhienService.SESSION_TOKEN_KEY)).thenReturn(maPhien);
        when(phienService.giaHanPhien(maPhien, session)).thenReturn(true);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String output = responseWriter.toString();
        assertTrue(output.contains("\"thanhCong\":true"));
        assertTrue(output.contains("sales@congty.vn"));
    }

    @Test
    @DisplayName("AC3: Khi phiên đã hết hạn, API gia hạn trả về 401 kèm JSON lỗi")
    void testGiaHanThatBaiKhiPhienHetHan() throws IOException {
        String maPhien = "EXPIRED-SESSION";
        NguoiDung nguoiDung = new NguoiDung();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(PhienService.SESSION_USER_KEY)).thenReturn(nguoiDung);
        when(session.getAttribute(PhienService.SESSION_TOKEN_KEY)).thenReturn(maPhien);
        when(phienService.giaHanPhien(maPhien, session)).thenReturn(false);

        servlet.doPost(request, response);

        verify(phienService).dangXuat(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String output = responseWriter.toString();
        assertTrue(output.contains("SESSION_EXPIRED"));
    }
}
