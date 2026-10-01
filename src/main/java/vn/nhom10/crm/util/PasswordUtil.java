package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa và kiểm tra mật khẩu bằng thuật toán BCrypt.
 * Tuân thủ yêu cầu bảo mật tài khoản người dùng CRM.
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

    public static String bamMatKhau(String matKhauTho) {
        return hashPassword(matKhauTho);
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
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean kiemTraMatKhau(String matKhauTho, String matKhauHash) {
        return checkPassword(matKhauTho, matKhauHash);
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
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }
        return false;
    }
}
