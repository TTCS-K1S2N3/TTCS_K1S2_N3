package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.GuiFormLeadDTO;
import vn.nhom10.crm.dto.KetQuaGuiFormDTO;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.service.BieuMauLeadService;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử BieuMauLeadPublicServlet - Public Embed Form Controller (Story S4-01)")
class BieuMauLeadPublicServletTest {

    private BieuMauLeadService service;
    private BieuMauLeadPublicServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(BieuMauLeadService.class);
        servlet = new BieuMauLeadPublicServlet(service);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("crm.vn");
        when(request.getServerPort()).thenReturn(443);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("AC1: GET /bieu-mau/nhung với token hợp lệ -> Forward tới bieu-mau-nhung.jsp")
    void testGetNhungTokenHopLe() throws Exception {
        when(request.getServletPath()).thenReturn("/bieu-mau/nhung");
        when(request.getParameter("token")).thenReturn("token_valid_123");

        BieuMauLead bm = new BieuMauLead("BM_01", "Biểu mẫu tư vấn", "token_valid_123", 1L);
        bm.setHoatDong(true);
        when(service.layBieuMauTheoToken("token_valid_123")).thenReturn(bm);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("bieuMau"), eq(bm));
        verify(request).getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-nhung.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC1: GET /bieu-mau/embed.js -> Trả về mã script nhúng JS")
    void testGetEmbedJs() throws Exception {
        when(request.getServletPath()).thenReturn("/bieu-mau/embed.js");
        when(request.getParameter("token")).thenReturn("token_valid_123");

        servlet.doGet(request, response);

        verify(response).setContentType("application/javascript;charset=UTF-8");
        String output = responseWriter.toString();
        assertTrue(output.contains("token_valid_123"));
        assertTrue(output.contains("iframe.src = 'https://crm.vn/crm/bieu-mau/nhung?token="));
    }

    @Test
    @DisplayName("AC2 & AC4: POST /bieu-mau/gui qua AJAX thành công -> Trả về JSON 200 và mã lead")
    void testPostGuiAjaxThanhCong() throws Exception {
        when(request.getServletPath()).thenReturn("/bieu-mau/gui");
        when(request.getParameter("token")).thenReturn("token_abc");
        when(request.getParameter("ho_ten")).thenReturn("Lê Văn Khách");
        when(request.getParameter("email")).thenReturn("khach@gmail.com");
        when(request.getParameter("so_dien_thoai")).thenReturn("0912345678");
        when(request.getParameter("ten_cong_ty")).thenReturn("Công ty CP Sao Vàng");
        when(request.getParameter("nhu_cau_quan_tam")).thenReturn("Nhu cầu tư vấn giải pháp");
        when(request.getParameter("website_url")).thenReturn(""); // Không dính honeypot
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");

        when(service.xuLyGuiForm(any(GuiFormLeadDTO.class)))
                .thenReturn(KetQuaGuiFormDTO.thanhCong("LEAD-261011-0001", 101L, "Gửi thông tin thành công!"));

        servlet.doPost(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("application/json;charset=UTF-8");
        String json = responseWriter.toString();
        assertTrue(json.contains("\"thanhCong\":true"));
        assertTrue(json.contains("LEAD-261011-0001"));
    }

    @Test
    @DisplayName("AC3: POST /bieu-mau/gui dính honeypot spam -> Trả về lỗi 400 SPAM_HONEYPOT")
    void testPostGuiAjaxSpamHoneypot() throws Exception {
        when(request.getServletPath()).thenReturn("/bieu-mau/gui");
        when(request.getParameter("token")).thenReturn("token_abc");
        when(request.getParameter("ho_ten")).thenReturn("Spam Bot");
        when(request.getParameter("website_url")).thenReturn("http://spam-link.org");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");

        when(service.xuLyGuiForm(any(GuiFormLeadDTO.class)))
                .thenReturn(KetQuaGuiFormDTO.thatBai(400, "Yêu cầu bị từ chối do phát hiện nghi vấn spam.", "SPAM_HONEYPOT"));

        servlet.doPost(request, response);

        verify(response).setStatus(400);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"thanhCong\":false"));
        assertTrue(json.contains("SPAM_HONEYPOT"));
    }

    @Test
    @DisplayName("AC3: POST /bieu-mau/gui vượt giới hạn IP -> Trả về HTTP 429 GIOI_HAN_TAN_SUAT_IP")
    void testPostGuiAjaxRateLimitIp() throws Exception {
        when(request.getServletPath()).thenReturn("/bieu-mau/gui");
        when(request.getParameter("token")).thenReturn("token_abc");
        when(request.getParameter("ho_ten")).thenReturn("Người dùng thử");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");

        when(service.xuLyGuiForm(any(GuiFormLeadDTO.class)))
                .thenReturn(KetQuaGuiFormDTO.thatBai(429, "Bạn đã gửi quá số lần cho phép.", "GIOI_HAN_TAN_SUAT_IP"));

        servlet.doPost(request, response);

        verify(response).setStatus(429);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"thanhCong\":false"));
        assertTrue(json.contains("GIOI_HAN_TAN_SUAT_IP"));
    }

    @Test
    @DisplayName("AC3: Trích xuất IP client đúng qua X-Forwarded-For")
    void testLayDiaChiIpClientXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.162.0.1, 10.0.0.1");
        String ip = servlet.layDiaChiIpClient(request);
        assertEquals("203.162.0.1", ip);
    }
}
