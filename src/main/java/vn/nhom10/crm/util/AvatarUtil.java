package vn.nhom10.crm.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Tiện ích xử lý ảnh đại diện (Avatar):
 * - Kiểm tra định dạng (JPG, PNG) và dung lượng tối đa 2MB.
 * - Cắt ảnh vuông từ tâm (Center Square Crop).
 * - Tạo bản thu nhỏ (Thumbnail resize).
 * - Sinh avatar mặc định theo tên viết tắt.
 */
public class AvatarUtil {

    /**
     * Dung lượng ảnh tối đa cho phép: 2MB (2 * 1024 * 1024 bytes).
     */
    public static final long GIOI_HAN_DUNG_LUONG_BYTES = 2L * 1024 * 1024;

    /**
     * Kích thước ảnh đại diện chuẩn vuông: 400x400 pixel.
     */
    public static final int KICH_THUOC_AVATAR_CHUAN = 400;

    /**
     * Kích thước ảnh thu nhỏ (thumbnail): 120x120 pixel.
     */
    public static final int KICH_THUOC_THUMBNAIL_CHUAN = 120;

    private static final Set<String> DUOI_FILE_HOP_LE = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png")
    );

    private static final Set<String> MIME_TYPE_HOP_LE = new HashSet<>(
            Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/pjpeg", "image/x-png")
    );

    /**
     * Kiểm tra tính hợp lệ của dung lượng file (tối đa 2MB).
     *
     * @param dungLuongBytes kích thước file tính theo bytes
     * @return null nếu hợp lệ, thông báo lỗi nếu không hợp lệ
     */
    public static String kiemTraDungLuong(long dungLuongBytes) {
        if (dungLuongBytes <= 0) {
            return "File tải lên không có dữ liệu hoặc rỗng.";
        }
        if (dungLuongBytes > GIOI_HAN_DUNG_LUONG_BYTES) {
            double dungLuongMb = (double) dungLuongBytes / (1024 * 1024);
            return String.format(Locale.US, "Dung lượng ảnh (%.2f MB) vượt quá giới hạn tối đa 2MB cho phép.", dungLuongMb);
        }
        return null;
    }

    /**
     * Kiểm tra định dạng file ảnh (chỉ chấp nhận JPG, JPEG, PNG).
     *
     * @param tenFile     tên file gốc tải lên
     * @param contentType MIME type từ request header (có thể null)
     * @return null nếu hợp lệ, thông báo lỗi nếu không hợp lệ
     */
    public static String kiemTraDinhDang(String tenFile, String contentType) {
        if (tenFile == null || tenFile.isBlank()) {
            return "Tên file không hợp lệ.";
        }

        String duoiFile = layPhanMoRong(tenFile).toLowerCase(Locale.ROOT);
        if (!DUOI_FILE_HOP_LE.contains(duoiFile)) {
            return "Hệ thống chỉ chấp nhận định dạng ảnh JPG, JPEG hoặc PNG.";
        }

        if (contentType != null && !contentType.isBlank()) {
            String mime = contentType.trim().toLowerCase(Locale.ROOT);
            if (!MIME_TYPE_HOP_LE.contains(mime)) {
                return "Loại dữ liệu ảnh không hợp lệ: " + contentType + ". Chỉ chấp nhận JPG/PNG.";
            }
        }

        return null;
    }

    /**
     * Lấy phần mở rộng của tên file.
     */
    public static String layPhanMoRong(String tenFile) {
        if (tenFile == null) return "";
        int dotIndex = tenFile.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < tenFile.length() - 1) {
            return tenFile.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * Cắt ảnh gốc thành hình vuông theo tỷ lệ 1:1 tính từ tâm ảnh (Center Crop).
     *
     * @param anhGoc BufferedImage ảnh gốc
     * @return BufferedImage ảnh đã được cắt vuông
     */
    public static BufferedImage catAnhVuong(BufferedImage anhGoc) {
        if (anhGoc == null) {
            throw new IllegalArgumentException("Ảnh gốc không được null");
        }

        int chieuRong = anhGoc.getWidth();
        int chieuCao = anhGoc.getHeight();

        // Nếu đã vuông thì trả về luôn
        if (chieuRong == chieuCao) {
            return anhGoc;
        }

        int canhVuong = Math.min(chieuRong, chieuCao);
        int toaDoX = (chieuRong - canhVuong) / 2;
        int toaDoY = (chieuCao - canhVuong) / 2;

        return anhGoc.getSubimage(toaDoX, toaDoY, canhVuong, canhVuong);
    }

    /**
     * Thay đổi kích thước ảnh vuông về độ phân giải mong muốn với thuật toán nội suy chất lượng cao.
     *
     * @param anhVuong   ảnh vuông đầu vào
     * @param kichThuoc  chiều dài cạnh mong muốn (pixel)
     * @param giuAlpha   true nếu giữ kênh trong suốt (PNG), false nếu nền trắng (JPG)
     * @return BufferedImage kết quả
     */
    public static BufferedImage thayDoiKichThuoc(BufferedImage anhVuong, int kichThuoc, boolean giuAlpha) {
        if (anhVuong == null || kichThuoc <= 0) {
            throw new IllegalArgumentException("Tham số thay đổi kích thước không hợp lệ");
        }

        int imageType = giuAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage ketQua = new BufferedImage(kichThuoc, kichThuoc, imageType);

        Graphics2D g2d = ketQua.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!giuAlpha) {
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, kichThuoc, kichThuoc);
            } else {
                g2d.setComposite(AlphaComposite.Src);
            }

            g2d.drawImage(anhVuong, 0, 0, kichThuoc, kichThuoc, null);
        } finally {
            g2d.dispose();
        }

        return ketQua;
    }

    /**
     * Đọc ảnh từ InputStream một cách an toàn.
     */
    public static BufferedImage docAnhTuStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException("Stream dữ liệu ảnh rỗng");
        }
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("Không thể giải mã dữ liệu ảnh. Định dạng có thể bị hỏng hoặc không được hỗ trợ.");
        }
        return image;
    }

    /**
     * Ghi ảnh ra file trên đĩa cứng.
     */
    public static void luuAnhRaFile(BufferedImage image, String format, Path duongDanDich) throws IOException {
        if (duongDanDich.getParent() != null) {
            Files.createDirectories(duongDanDich.getParent());
        }
        String formatChuan = "png".equalsIgnoreCase(format) ? "png" : "jpg";
        boolean ghiThanhCong = ImageIO.write(image, formatChuan, duongDanDich.toFile());
        if (!ghiThanhCong) {
            throw new IOException("Không thể ghi file ảnh theo định dạng " + formatChuan);
        }
    }

    /**
     * Chuyển đổi BufferedImage thành mảng byte để stream ra HTTP response.
     */
    public static byte[] chuyenThanhBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatChuan = "png".equalsIgnoreCase(format) ? "png" : "jpg";
        ImageIO.write(image, formatChuan, baos);
        return baos.toByteArray();
    }

    /**
     * Tạo mã SVG cho avatar mặc định hiển thị tên viết tắt khi người dùng chưa có ảnh đại diện.
     *
     * @param tenVietTat 2 chữ cái viết tắt của người dùng (ví dụ: "TK", "AD")
     * @return chuỗi nội dung SVG hoàn chỉnh
     */
    public static String sinhAvatarMacDinhSvg(String tenVietTat) {
        String initials = (tenVietTat != null && !tenVietTat.isBlank()) ? tenVietTat.trim().toUpperCase() : "CR";
        if (initials.length() > 2) {
            initials = initials.substring(0, 2);
        }

        // Chọn mã màu dựa trên mã băm của tên
        int hash = Math.abs(initials.hashCode());
        String[][] bangMau = {
                {"#2563eb", "#1d4ed8"}, // Xanh dương
                {"#059669", "#047857"}, // Xanh lá
                {"#7c3aed", "#6d28d9"}, // Tím
                {"#ea580c", "#c2410c"}, // Cam
                {"#0891b2", "#0e7490"}, // Lam
                {"#db2777", "#be185d"}  // Hồng
        };
        String[] mau = bangMau[hash % bangMau.length];

        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 120 120\" width=\"120\" height=\"120\">"
                + "<defs>"
                + "<linearGradient id=\"grad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">"
                + "<stop offset=\"0%\" style=\"stop-color:" + mau[0] + ";stop-opacity:1\" />"
                + "<stop offset=\"100%\" style=\"stop-color:" + mau[1] + ";stop-opacity:1\" />"
                + "</linearGradient>"
                + "</defs>"
                + "<rect width=\"120\" height=\"120\" rx=\"60\" fill=\"url(#grad)\" />"
                + "<text x=\"50%\" y=\"54%\" font-family=\"'Segoe UI', -apple-system, Roboto, sans-serif\" "
                + "font-size=\"42\" font-weight=\"bold\" fill=\"#ffffff\" text-anchor=\"middle\" dominant-baseline=\"middle\">"
                + initials
                + "</text>"
                + "</svg>";
    }
}
