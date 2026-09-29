package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích băm và xác thực mật khẩu bằng thuật toán BCrypt.
 * Tuân thủ quy định:
 * - Mật khẩu mới tối thiểu 8 ký tự, có chữ và số.
 * - Không lưu mật khẩu dạng plain text.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    /**
     * Băm mật khẩu người dùng bằng BCrypt với salt ngẫu nhiên.
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
     * So khớp mật khẩu thô với chuỗi hash đã lưu trong DB.
     *
     * @param rawPassword    mật khẩu thô do người dùng nhập
     * @param hashedPassword chuỗi hash BCrypt trong cơ sở dữ liệu
     * @return true nếu mật khẩu trùng khớp
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
     * Kiểm tra độ phức tạp của mật khẩu:
     * - Tối thiểu 8 ký tự.
     * - Phải chứa ít nhất một chữ cái và một chữ số.
     *
     * @param password mật khẩu cần kiểm tra
     * @return true nếu hợp lệ
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
