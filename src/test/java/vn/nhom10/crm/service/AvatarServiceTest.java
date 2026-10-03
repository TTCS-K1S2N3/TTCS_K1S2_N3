package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaUploadAvatarDTO;
import vn.nhom10.crm.model.NguoiDung;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho AvatarService kiểm tra toàn trình nghiệp vụ Story S2-03.
 */
class AvatarServiceTest {

    @TempDir
    Path tempUploadDir;

    private AvatarService avatarService;
    private MockNguoiDungDAO mockDAO;

    // Mock DAO lưu trữ trong RAM phục vụ unit test
    static class MockNguoiDungDAO extends NguoiDungDAO {
        final Map<Integer, NguoiDung> db = new HashMap<>();

        @Override
        public NguoiDung timTheoId(int id) {
            return db.get(id);
        }

        @Override
        public boolean capNhatAnhDaiDien(int nguoiDungId, String anhDaiDienPath, String anhDaiDienThumbPath) {
            NguoiDung nd = db.get(nguoiDungId);
            if (nd != null) {
                nd.setAnhDaiDienPath(anhDaiDienPath);
                nd.setAnhDaiDienThumbPath(anhDaiDienThumbPath);
                return true;
            }
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        mockDAO = new MockNguoiDungDAO();
        NguoiDung user = new NguoiDung(10, "Thào A Khua", "khua.thao@crm.vn");
        mockDAO.db.put(10, user);

        avatarService = new AvatarService(mockDAO, tempUploadDir.toString());
    }

    private byte[] taoAnhGiaLap(int rong, int cao, String format) throws IOException {
        BufferedImage img = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, rong, cao);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Upload avatar thành công: ảnh được cắt vuông, tạo thumbnail và cập nhật DB")
    void testUploadAvatarThanhCong() throws IOException {
        // Tạo ảnh chữ nhật 600 x 400
        byte[] imageBytes = taoAnhGiaLap(600, 400, "jpg");

        KetQuaUploadAvatarDTO ketQua = avatarService.xuLyUploadAvatar(
                10,
                new ByteArrayInputStream(imageBytes),
                "my_avatar.jpg",
                "image/jpeg",
                imageBytes.length,
                "/crm"
        );

        assertNotNull(ketQua);
        assertTrue(ketQua.isThanhCong(), "Upload phải thành công");
        assertNotNull(ketQua.getAnhDaiDienPath());
        assertNotNull(ketQua.getAnhDaiDienThumbPath());
        assertTrue(ketQua.getAnhDaiDienUrl().contains("/avatar?id=10"));
        assertTrue(ketQua.getAnhDaiDienThumbUrl().contains("thumb=true"));

        // Kiểm tra file vật lý tồn tại trên đĩa
        File fileAvatar = avatarService.layFileAnhNguoiDung(10, false);
        File fileThumb = avatarService.layFileAnhNguoiDung(10, true);

        assertNotNull(fileAvatar, "File avatar lớn phải tồn tại");
        assertTrue(fileAvatar.exists());
        assertNotNull(fileThumb, "File thumbnail phải tồn tại");
        assertTrue(fileThumb.exists());

        // Kiểm tra kích thước hình ảnh đã lưu
        BufferedImage savedAvatar = ImageIO.read(fileAvatar);
        BufferedImage savedThumb = ImageIO.read(fileThumb);

        assertEquals(400, savedAvatar.getWidth());
        assertEquals(400, savedAvatar.getHeight(), "Avatar chính phải vuông 400x400");

        assertEquals(120, savedThumb.getWidth());
        assertEquals(120, savedThumb.getHeight(), "Thumbnail phải vuông 120x120");

        // Kiểm tra trạng thái DB của người dùng
        NguoiDung userSauUpload = mockDAO.timTheoId(10);
        assertEquals(ketQua.getAnhDaiDienPath(), userSauUpload.getAnhDaiDienPath());
        assertEquals(ketQua.getAnhDaiDienThumbPath(), userSauUpload.getAnhDaiDienThumbPath());
    }

    @Test
    @DisplayName("Upload thất bại khi dung lượng file vượt quá 2MB")
    void testUploadVuotQua2MB() {
        long dungLuongQuaMuc = 2L * 1024 * 1024 + 1024; // > 2MB

        KetQuaUploadAvatarDTO ketQua = avatarService.xuLyUploadAvatar(
                10,
                new ByteArrayInputStream(new byte[10]),
                "large.png",
                "image/png",
                dungLuongQuaMuc,
                "/crm"
        );

        assertNotNull(ketQua);
        assertFalse(ketQua.isThanhCong(), "Dung lượng > 2MB phải bị từ chối");
        assertTrue(ketQua.getThongDiep().contains("2MB"));
    }

    @Test
    @DisplayName("Upload thất bại khi định dạng file không phải JPG/PNG")
    void testUploadSaiDinhDang() {
        KetQuaUploadAvatarDTO ketQua = avatarService.xuLyUploadAvatar(
                10,
                new ByteArrayInputStream("fake-data".getBytes()),
                "script.sh",
                "application/x-sh",
                100,
                "/crm"
        );

        assertNotNull(ketQua);
        assertFalse(ketQua.isThanhCong(), "Định dạng .sh phải bị từ chối");
        assertTrue(ketQua.getThongDiep().contains("JPG") || ketQua.getThongDiep().contains("PNG"));
    }

    @Test
    @DisplayName("Upload thất bại khi người dùng không tồn tại")
    void testUploadNguoiDungKhongTonTai() throws IOException {
        byte[] imageBytes = taoAnhGiaLap(100, 100, "png");

        KetQuaUploadAvatarDTO ketQua = avatarService.xuLyUploadAvatar(
                99999, // ID không có trong DB
                new ByteArrayInputStream(imageBytes),
                "avatar.png",
                "image/png",
                imageBytes.length,
                "/crm"
        );

        assertNotNull(ketQua);
        assertFalse(ketQua.isThanhCong(), "Người dùng không tồn tại phải báo lỗi");
    }
}
