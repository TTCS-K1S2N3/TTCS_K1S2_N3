package vn.nhom10.crm.util;

import java.util.regex.Pattern;

/**
 * Tiện ích xử lý và kiểm tra định dạng email.
 */
public class EmailUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Kiểm tra cú pháp email hợp lệ.
     *
     * @param email chuỗi email
     * @return true nếu email hợp lệ
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty() || trimmed.length() > 150) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }
}
