package vn.nhom10.crm.util;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra định dạng và chuẩn hóa dữ liệu dùng chung cho hệ thống CRM.
 * Phục vụ Story S2-02 (AC3: Kiểm tra định dạng số điện thoại Việt Nam).
 */
public final class DinhDangUtil {

    /**
     * Regex kiểm tra số điện thoại Việt Nam hợp lệ:
     * - Di động 10 số (đầu số 03x, 05x, 07x, 08x, 09x)
     * - Cố định 11 số (đầu số 02x)
     * - Chấp nhận định dạng nội địa (bắt đầu bằng 0) hoặc quốc tế (bắt đầu bằng +84 hoặc 84)
     */
    private static final Pattern SO_DIEN_THOAI_VN_PATTERN = Pattern.compile(
            "^(?:\\+?84|0)(?:(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}|2[0-9]{9})$"
    );

    private DinhDangUtil() {
        // Utility class không cho phép khởi tạo
    }

    /**
     * Kiểm tra số điện thoại có đúng định dạng số điện thoại Việt Nam hay không.
     * Chấp nhận các ký tự phân tách phổ biến như khoảng trắng, dấu chấm, dấu gạch ngang.
     *
     * @param soDienThoai Chuỗi số điện thoại cần kiểm tra
     * @return true nếu hợp lệ, false nếu null, rỗng hoặc sai định dạng
     */
    public static boolean laSoDienThoaiVietNamHopLe(String soDienThoai) {
        if (soDienThoai == null) {
            return false;
        }
        String clean = soDienThoai.trim().replaceAll("[\\s.-]", "");
        if (clean.isEmpty()) {
            return false;
        }
        return SO_DIEN_THOAI_VN_PATTERN.matcher(clean).matches();
    }

    /**
     * Chuẩn hóa số điện thoại về định dạng nội địa chuẩn (bắt đầu bằng 0, không có khoảng trắng/ký tự phân tách).
     * Ví dụ:
     * - "+84 901.234.567" -> "0901234567"
     * - "84901234567"     -> "0901234567"
     * - "090-123-4567"    -> "0901234567"
     *
     * @param soDienThoai Chuỗi số điện thoại
     * @return Chuỗi số điện thoại đã chuẩn hóa, hoặc chuỗi rỗng nếu đầu vào null/rỗng
     */
    public static String chuanHoaSoDienThoai(String soDienThoai) {
        if (soDienThoai == null) {
            return "";
        }
        String clean = soDienThoai.trim().replaceAll("[\\s.-]", "");
        if (clean.startsWith("+84")) {
            clean = "0" + clean.substring(3);
        } else if (clean.startsWith("84") && clean.length() >= 11) {
            clean = "0" + clean.substring(2);
        }
        return clean;
    }
}
