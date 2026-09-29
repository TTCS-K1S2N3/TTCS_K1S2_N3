package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích băm và kiểm tra mật khẩu sử dụng BCrypt.
 * Đáp ứng các quy tắc Acceptance Criteria:
 * - Bắt buộc mật khẩu mới tối thiểu 8 ký tự, có chữ và số.
 * - Mã hóa bảo mật không lưu plain-text.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    /**
     * Băm mật khẩu người dùng bằng BCrypt.
     *
     * @param rawPassword mật khẩu thô
     * @return chuỗi hash BCrypt
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * So khớp mật khẩu thô với hash BCrypt đã lưu trong DB.
     *
     * @param rawPassword    mật khẩu thô do người dùng nhập
     * @param hashedPassword chuỗi hash BCrypt trong database
     * @return true nếu mật khẩu chính xác
     */
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * AC 2: Kiểm tra mật khẩu mới:
     * - Tối thiểu 8 ký tự.
     * - Chứa ít nhất một chữ cái (a-z, A-Z).
     * - Chứa ít nhất một chữ số (0-9).
     *
     * @param password mật khẩu cần kiểm tra
     * @return true nếu thỏa mãn toàn bộ tiêu chí
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean coChu = false;
        boolean coSo = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                coChu = true;
            } else if (Character.isDigit(c)) {
                coSo = true;
            }
            if (coChu && coSo) {
                return true;
            }
        }
        return false;
    }
}
