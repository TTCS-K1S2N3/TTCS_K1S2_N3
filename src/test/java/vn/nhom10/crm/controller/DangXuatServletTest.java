package vn.nhom10.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DangXuatServletTest {

    @Mock
    private PhienService phienService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private DangXuatServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new DangXuatServlet();
        servlet.setPhienService(phienService);
    }

    @Test
    @DisplayName("AC2: Đăng xuất bằng POST làm mất hiệu lực phiên ngay lập tức phía server và chuyển hướng")
    void testDoPostDangXuatMatHieuLucVaChuyenHuong() throws IOException {
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doPost(request, response);

        verify(phienService).dangXuat(request, response);
        verify(response).sendRedirect("/crm/dang-nhap?thongBao=dang_xuat");
    }

    @Test
    @DisplayName("AC2: Đăng xuất bằng GET làm mất hiệu lực phiên ngay lập tức phía server và chuyển hướng")
    void testDoGetDangXuatMatHieuLucVaChuyenHuong() throws IOException {
        when(request.getContextPath()).thenReturn("/crm");

        servlet.doGet(request, response);

        verify(phienService).dangXuat(request, response);
        verify(response).sendRedirect("/crm/dang-nhap?thongBao=dang_xuat");
    }
}
