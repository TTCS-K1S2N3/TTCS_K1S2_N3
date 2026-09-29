package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích băm và kiểm tra mật khẩu sử dụng BCrypt.
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
     * So khớp mật khẩu thô với hash BCrypt đã lưu trong database.
     *
     * @param rawPassword    mật khẩu thô người dùng nhập
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
     * Kiểm tra độ mạnh của mật khẩu:
     * - Tối thiểu 8 ký tự.
     * - Chứa ít nhất một chữ cái.
     * - Chứa ít nhất một chữ số.
     *
     * @param password mật khẩu cần kiểm tra
     * @return true nếu thỏa mãn
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
