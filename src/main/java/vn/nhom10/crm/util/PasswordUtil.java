package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích băm và kiểm tra mật khẩu sử dụng BCrypt.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static String bamMatKhau(String matKhauTho) {
        return hashPassword(matKhauTho);
    }

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

    public static boolean kiemTraMatKhau(String matKhauTho, String matKhauHash) {
        return checkPassword(matKhauTho, matKhauHash);
    }

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
