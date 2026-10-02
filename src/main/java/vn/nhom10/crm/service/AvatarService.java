package vn.nhom10.crm.service;

import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaUploadAvatarDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.util.AvatarUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ tải lên, cắt vuông, tạo thumbnail và quản lý ảnh đại diện.
 * Đáp ứng Story S2-03:
 * - Chấp nhận JPG/PNG tối đa 2MB
 * - Ảnh được cắt vuông và tạo bản thu nhỏ (thumbnail)
 * - Cập nhật hồ sơ và hiển thị cho đồng nghiệp nhận diện
 */
public class AvatarService {

    private static final Logger LOGGER = Logger.getLogger(AvatarService.class.getName());

    private final NguoiDungDAO nguoiDungDAO;
    private final String uploadThuMucGoc;

    public AvatarService() {
        this(new NguoiDungDAO(), DatabaseConfig.getAvatarUploadDir());
    }

    public AvatarService(NguoiDungDAO nguoiDungDAO, String uploadThuMucGoc) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.uploadThuMucGoc = (uploadThuMucGoc != null && !uploadThuMucGoc.isBlank())
                ? uploadThuMucGoc : "uploads/avatars";
    }

    /**
     * Xử lý toàn trình nghiệp vụ tải lên ảnh đại diện:
     * 1. Kiểm tra dung lượng (tối đa 2MB)
     * 2. Kiểm tra định dạng (JPG, PNG)
     * 3. Giải mã và cắt vuông từ tâm (1:1)
     * 4. Tạo thumbnail (120x120) và ảnh chuẩn (400x400)
     * 5. Lưu ra hệ thống file an toàn
     * 6. Cập nhật đường dẫn vào cơ sở dữ liệu
     *
     * @param nguoiDungId  ID người dùng sở hữu ảnh
     * @param inputStream  dữ liệu stream của file tải lên
     * @param tenFileGoc   tên file gửi lên từ client
     * @param contentType  MIME type gửi lên
     * @param dungLuong    kích thước file tính theo bytes
     * @param contextPath  context-path của ứng dụng web để sinh URL
     * @return KetQuaUploadAvatarDTO kết quả thao tác
     */
    public KetQuaUploadAvatarDTO xuLyUploadAvatar(int nguoiDungId,
                                                InputStream inputStream,
                                                String tenFileGoc,
                                                String contentType,
                                                long dungLuong,
                                                String contextPath) {

        // 1. Kiểm tra người dùng tồn tại
        NguoiDung nguoiDung = nguoiDungDAO.timTheoId(nguoiDungId);
        if (nguoiDung == null) {
            return KetQuaUploadAvatarDTO.thatBai("Không tìm thấy thông tin tài khoản người dùng ID=" + nguoiDungId);
        }

        // 2. Validate dung lượng tối đa 2MB (AC: Chấp nhận tối đa 2MB)
        String loiDungLuong = AvatarUtil.kiemTraDungLuong(dungLuong);
        if (loiDungLuong != null) {
            return KetQuaUploadAvatarDTO.thatBai(loiDungLuong);
        }

        // 3. Validate định dạng JPG / PNG (AC: Chấp nhận JPG/PNG)
        String loiDinhDang = AvatarUtil.kiemTraDinhDang(tenFileGoc, contentType);
        if (loiDinhDang != null) {
            return KetQuaUploadAvatarDTO.thatBai(loiDinhDang);
        }

        try {
            // 4. Đọc dữ liệu ảnh vào bộ nhớ
            BufferedImage anhGoc = AvatarUtil.docAnhTuStream(inputStream);

            // 5. Cắt vuông 1:1 từ tâm ảnh (AC: Ảnh được cắt vuông)
            BufferedImage anhVuong = AvatarUtil.catAnhVuong(anhGoc);

            // 6. Tạo 2 phiên bản: ảnh chuẩn (400x400) và bản thu nhỏ thumbnail (120x120)
            boolean laPng = "png".equalsIgnoreCase(AvatarUtil.layPhanMoRong(tenFileGoc));
            BufferedImage anhChuan = AvatarUtil.thayDoiKichThuoc(anhVuong, AvatarUtil.KICH_THUOC_AVATAR_CHUAN, laPng);
            BufferedImage anhThumbnail = AvatarUtil.thayDoiKichThuoc(anhVuong, AvatarUtil.KICH_THUOC_THUMBNAIL_CHUAN, laPng);

            // 7. Tạo tên file ngẫu nhiên chống trùng lặp và path traversal
            String duoiFile = laPng ? "png" : "jpg";
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String tenFileAvatar = "avatar_user_" + nguoiDungId + "_" + uuid + "." + duoiFile;
            String tenFileThumb = "thumb_user_" + nguoiDungId + "_" + uuid + "." + duoiFile;

            // Đường dẫn lưu trữ vật lý
            Path thuMucLuu = Paths.get(uploadThuMucGoc, "user_" + nguoiDungId).toAbsolutePath().normalize();
            Files.createDirectories(thuMucLuu);

            Path duongDanAvatarFile = thuMucLuu.resolve(tenFileAvatar);
            Path duongDanThumbFile = thuMucLuu.resolve(tenFileThumb);

            AvatarUtil.luuAnhRaFile(anhChuan, duoiFile, duongDanAvatarFile);
            AvatarUtil.luuAnhRaFile(anhThumbnail, duoiFile, duongDanThumbFile);

            // Đường dẫn tương đối lưu vào database
            String relativeAvatarPath = "user_" + nguoiDungId + "/" + tenFileAvatar;
            String relativeThumbPath = "user_" + nguoiDungId + "/" + tenFileThumb;

            // Xóa file ảnh cũ nếu có để tiết kiệm dung lượng
            xoaAnhCu(nguoiDung.getAnhDaiDienPath());
            xoaAnhCu(nguoiDung.getAnhDaiDienThumbPath());

            // 8. Cập nhật vào cơ sở dữ liệu
            boolean capNhatThanhCong = nguoiDungDAO.capNhatAnhDaiDien(nguoiDungId, relativeAvatarPath, relativeThumbPath);
            if (!capNhatThanhCong) {
                // Rollback xóa file nếu DB lỗi
                Files.deleteIfExists(duongDanAvatarFile);
                Files.deleteIfExists(duongDanThumbFile);
                return KetQuaUploadAvatarDTO.thatBai("Lưu đường dẫn ảnh vào cơ sở dữ liệu thất bại.");
            }

            // Sinh URL phục vụ hiển thị
            String prefix = (contextPath != null && !contextPath.isBlank() && !"/".equals(contextPath)) ? contextPath : "";
            String avatarUrl = prefix + "/avatar?id=" + nguoiDungId + "&v=" + System.currentTimeMillis();
            String thumbUrl = prefix + "/avatar?id=" + nguoiDungId + "&thumb=true&v=" + System.currentTimeMillis();

            return KetQuaUploadAvatarDTO.thanhCong(
                    "Tải lên và xử lý ảnh đại diện thành công!",
                    relativeAvatarPath,
                    relativeThumbPath,
                    avatarUrl,
                    thumbUrl,
                    dungLuong
            );

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý file ảnh upload: " + e.getMessage(), e);
            return KetQuaUploadAvatarDTO.thatBai("Xử lý file ảnh thất bại: " + e.getMessage());
        }
    }

    /**
     * Lấy File vật lý an toàn từ đường dẫn lưu trữ, có kiểm tra chống path traversal.
     */
    public File layFileAnh(String duongDanTuongDoi) {
        if (duongDanTuongDoi == null || duongDanTuongDoi.isBlank()) {
            return null;
        }

        try {
            Path thuMucGoc = Paths.get(uploadThuMucGoc).toAbsolutePath().normalize();
            Path duongDanFile = thuMucGoc.resolve(duongDanTuongDoi).normalize();

            // Kiểm tra chống Path Traversal (file phải nằm trong thư mục upload)
            if (!duongDanFile.startsWith(thuMucGoc)) {
                LOGGER.warning("Phát hiện nguy cơ Path Traversal: " + duongDanTuongDoi);
                return null;
            }

            File file = duongDanFile.toFile();
            if (file.exists() && file.isFile() && file.canRead()) {
                return file;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc file ảnh: " + duongDanTuongDoi, e);
        }
        return null;
    }

    /**
     * Lấy File ảnh của người dùng (chính hoặc thumbnail).
     */
    public File layFileAnhNguoiDung(int nguoiDungId, boolean laThumbnail) {
        NguoiDung nd = nguoiDungDAO.timTheoId(nguoiDungId);
        if (nd == null) {
            return null;
        }
        String duongDan = laThumbnail ? nd.getAnhDaiDienThumbPath() : nd.getAnhDaiDienPath();
        if (duongDan == null || duongDan.isBlank()) {
            // Thử lấy đường dẫn còn lại nếu 1 trong 2 null
            duongDan = nd.getAnhDaiDienPath();
        }
        return layFileAnh(duongDan);
    }

    private void xoaAnhCu(String duongDanTuongDoi) {
        if (duongDanTuongDoi == null || duongDanTuongDoi.isBlank()) {
            return;
        }
        try {
            Path thuMucGoc = Paths.get(uploadThuMucGoc).toAbsolutePath().normalize();
            Path duongDanFile = thuMucGoc.resolve(duongDanTuongDoi).normalize();
            if (duongDanFile.startsWith(thuMucGoc)) {
                Files.deleteIfExists(duongDanFile);
            }
        } catch (Exception ignored) {
        }
    }
}
