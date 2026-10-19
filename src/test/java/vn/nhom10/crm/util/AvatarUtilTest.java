package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho AvatarUtil theo Story S2-03:
 * - Chấp nhận JPG/PNG tối đa 2MB
 * - Ảnh được cắt vuông và tạo bản thu nhỏ (thumbnail)
 */
class AvatarUtilTest {

    @Test
    @DisplayName("Kiểm tra dung lượng: hợp lệ khi <= 2MB, lỗi khi > 2MB hoặc <= 0")
    void testKiemTraDungLuong() {
        // Hợp lệ
        assertNull(AvatarUtil.kiemTraDungLuong(1024)); // 1 KB
        assertNull(AvatarUtil.kiemTraDungLuong(1024 * 1024)); // 1 MB
        assertNull(AvatarUtil.kiemTraDungLuong(2 * 1024 * 1024)); // Đúng 2 MB

        // Không hợp lệ
        assertNotNull(AvatarUtil.kiemTraDungLuong(0), "File 0 byte phải báo lỗi");
        assertNotNull(AvatarUtil.kiemTraDungLuong(-100), "Dung lượng âm phải báo lỗi");
        assertNotNull(AvatarUtil.kiemTraDungLuong(2 * 1024 * 1024 + 1), "Vượt quá 2MB dù 1 byte cũng phải báo lỗi");
        assertNotNull(AvatarUtil.kiemTraDungLuong(5 * 1024 * 1024), "5MB phải báo lỗi vượt giới hạn");
    }

    @Test
    @DisplayName("Kiểm tra định dạng: chỉ chấp nhận JPG, JPEG, PNG")
    void testKiemTraDinhDang() {
        // Hợp lệ
        assertNull(AvatarUtil.kiemTraDinhDang("avatar.jpg", "image/jpeg"));
        assertNull(AvatarUtil.kiemTraDinhDang("photo.jpeg", "image/jpeg"));
        assertNull(AvatarUtil.kiemTraDinhDang("picture.png", "image/png"));
        assertNull(AvatarUtil.kiemTraDinhDang("PROFILE.PNG", "image/png"));
        assertNull(AvatarUtil.kiemTraDinhDang("my_photo.JPG", null)); // null contentType vẫn chấp nhận theo đuôi

        // Không hợp lệ
        assertNotNull(AvatarUtil.kiemTraDinhDang("document.pdf", "application/pdf"));
        assertNotNull(AvatarUtil.kiemTraDinhDang("script.sh", "text/plain"));
        assertNotNull(AvatarUtil.kiemTraDinhDang("shell.jsp", "text/html"));
        assertNotNull(AvatarUtil.kiemTraDinhDang("animation.gif", "image/gif"));
        assertNotNull(AvatarUtil.kiemTraDinhDang("data.exe", "application/octet-stream"));
        assertNotNull(AvatarUtil.kiemTraDinhDang("", "image/jpeg"));
        assertNotNull(AvatarUtil.kiemTraDinhDang(null, "image/jpeg"));
    }

    @Test
    @DisplayName("Cắt ảnh vuông (Center Square Crop) cho ảnh chữ nhật ngang")
    void testCatAnhVuongNgang() {
        // Ảnh 600 x 400 (ngang)
        BufferedImage anhNgang = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        BufferedImage anhVuong = AvatarUtil.catAnhVuong(anhNgang);

        assertNotNull(anhVuong);
        assertEquals(400, anhVuong.getWidth(), "Chiều rộng sau khi crop phải bằng cạnh nhỏ nhất (400)");
        assertEquals(400, anhVuong.getHeight(), "Chiều cao sau khi crop phải bằng cạnh nhỏ nhất (400)");
    }

    @Test
    @DisplayName("Cắt ảnh vuông (Center Square Crop) cho ảnh chữ nhật dọc")
    void testCatAnhVuongDoc() {
        // Ảnh 300 x 500 (dọc)
        BufferedImage anhDoc = new BufferedImage(300, 500, BufferedImage.TYPE_INT_RGB);
        BufferedImage anhVuong = AvatarUtil.catAnhVuong(anhDoc);

        assertNotNull(anhVuong);
        assertEquals(300, anhVuong.getWidth(), "Chiều rộng sau khi crop phải bằng cạnh nhỏ nhất (300)");
        assertEquals(300, anhVuong.getHeight(), "Chiều cao sau khi crop phải bằng cạnh nhỏ nhất (300)");
    }

    @Test
    @DisplayName("Cắt ảnh vuông khi ảnh vốn đã vuông thì giữ nguyên kích thước")
    void testCatAnhVuongDaVuong() {
        BufferedImage anhVuongGoc = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
        BufferedImage ketQua = AvatarUtil.catAnhVuong(anhVuongGoc);

        assertNotNull(ketQua);
        assertEquals(250, ketQua.getWidth());
        assertEquals(250, ketQua.getHeight());
    }

    @Test
    @DisplayName("Tạo bản thu nhỏ thumbnail (120x120) và ảnh chuẩn (400x400)")
    void testThayDoiKichThuocThumbnail() {
        BufferedImage anhVuong = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);

        // Thumbnail 120x120
        BufferedImage thumb = AvatarUtil.thayDoiKichThuoc(anhVuong, AvatarUtil.KICH_THUOC_THUMBNAIL_CHUAN, false);
        assertNotNull(thumb);
        assertEquals(120, thumb.getWidth());
        assertEquals(120, thumb.getHeight());

        // Chuẩn 400x400
        BufferedImage avatarChuan = AvatarUtil.thayDoiKichThuoc(anhVuong, AvatarUtil.KICH_THUOC_AVATAR_CHUAN, false);
        assertNotNull(avatarChuan);
        assertEquals(400, avatarChuan.getWidth());
        assertEquals(400, avatarChuan.getHeight());
    }

    @Test
    @DisplayName("Đọc ảnh từ stream và chuyển đổi thành mảng byte")
    void testDocVaChuyenThanhBytes() throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage loaded = AvatarUtil.docAnhTuStream(bais);

        assertNotNull(loaded);
        assertEquals(100, loaded.getWidth());
        assertEquals(100, loaded.getHeight());

        byte[] outputBytes = AvatarUtil.chuyenThanhBytes(loaded, "png");
        assertTrue(outputBytes.length > 0);
    }

    @Test
    @DisplayName("Sinh avatar mặc định SVG theo tên viết tắt")
    void testSinhAvatarMacDinhSvg() {
        String svg = AvatarUtil.sinhAvatarMacDinhSvg("TK");
        assertNotNull(svg);
        assertTrue(svg.contains("<svg"));
        assertTrue(svg.contains("TK"));
        assertTrue(svg.contains("</svg>"));

        String svgNull = AvatarUtil.sinhAvatarMacDinhSvg(null);
        assertTrue(svgNull.contains("CR"));
    }
}
