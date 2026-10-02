package vn.nhom10.crm.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaUploadAvatarDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.AvatarService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Part filePart;

    @Mock
    private AvatarService avatarService;

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    private AvatarServlet servlet;

    static class DelegatingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream target;

        DelegatingServletOutputStream(ByteArrayOutputStream target) {
            this.target = target;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) throws IOException {
            target.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            target.write(b, off, len);
        }
    }

    @BeforeEach
    void setUp() {
        servlet = new AvatarServlet(avatarService, nguoiDungDAO);
    }

    @Test
    @DisplayName("GET /avatar khi chưa đăng nhập và không truyền id: xuất SVG mặc định")
    void testDoGet_ChuaDangNhap_XuatSvgMacDinh() throws Exception {
        when(request.getParameter("id")).thenReturn(null);
        when(request.getSession(false)).thenReturn(null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));

        servlet.doGet(request, response);

        verify(response).setContentType("image/svg+xml;charset=UTF-8");
        assertTrue(out.toString().contains("<svg"), "Nội dung xuất ra phải là SVG");
    }

    @Test
    @DisplayName("GET /avatar?id=1: người dùng có file ảnh -> xuất stream ảnh")
    void testDoGet_NguoiDungCoAnh(@TempDir Path tempDir) throws Exception {
        Path dummyImage = tempDir.resolve("avatar.png");
        Files.write(dummyImage, new byte[]{1, 2, 3, 4});

        when(request.getParameter("id")).thenReturn("1");
        when(request.getParameter("thumb")).thenReturn("false");

        NguoiDung nd = new NguoiDung(1, "Bàn Thị Linh", "linh.ban@crm.vn");
        when(nguoiDungDAO.timTheoId(1)).thenReturn(nd);
        when(avatarService.layFileAnhNguoiDung(1, false)).thenReturn(dummyImage.toFile());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));

        servlet.doGet(request, response);

        verify(response).setContentType("image/png");
        assertEquals(4, out.size());
    }

    @Test
    @DisplayName("POST /avatar khi chưa đăng nhập: trả về lỗi hoặc chuyển hướng")
    void testDoPost_ChuaDangNhap() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(sw.toString().contains("false"));
    }

    @Test
    @DisplayName("POST /avatar qua AJAX: tải lên thành công trả về JSON chứa avatarUrl và thumbUrl")
    void testDoPost_Ajax_ThanhCong() throws Exception {
        NguoiDung user = new NguoiDung(5, "Bàn Thị Linh", "linh.ban@crm.vn");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession()).thenReturn(session);

        when(request.getPart("avatar")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(1024L);
        when(filePart.getContentType()).thenReturn("image/png");
        when(filePart.getHeader("content-disposition")).thenReturn("form-data; name=\"avatar\"; filename=\"avatar.png\"");
        when(filePart.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(request.getContextPath()).thenReturn("/crm");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        KetQuaUploadAvatarDTO ketQua = KetQuaUploadAvatarDTO.thanhCong(
                "Tải lên thành công",
                "user_5/avatar.png",
                "user_5/thumb.png",
                "/crm/avatar?id=5",
                "/crm/avatar?id=5&thumb=true",
                1024L
        );
        when(avatarService.xuLyUploadAvatar(eq(5), any(InputStream.class), eq("avatar.png"), eq("image/png"), eq(1024L), eq("/crm")))
                .thenReturn(ketQua);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("user"), any(NguoiDung.class));
        assertTrue(sw.toString().contains("\"success\":true"));
        assertTrue(sw.toString().contains("/crm/avatar?id=5"));
        assertTrue(sw.toString().contains("thumbUrl"));
    }

    @Test
    @DisplayName("POST /avatar khi không chọn file: trả về lỗi yêu cầu chọn file")
    void testDoPost_KhongChonFile() throws Exception {
        NguoiDung user = new NguoiDung(5, "Bàn Thị Linh", "linh.ban@crm.vn");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);

        when(request.getPart("avatar")).thenReturn(filePart);
        when(filePart.getSize()).thenReturn(0L);
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(sw.toString().contains("Vui lòng chọn một file ảnh"));
    }
}
