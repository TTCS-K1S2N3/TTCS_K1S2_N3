package vn.nhom10.crm.util;

import java.security.SecureRandom;

/**
 * Tiện ích sinh token bảo mật cho chức năng đặt lại mật khẩu.
 * Sử dụng SecureRandom với độ dài 32 bytes (256-bit entropy).
 */
public class TokenUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * Sinh token ngẫu nhiên bảo mật dạng hex (64 ký tự).
     *
     * @return chuỗi token an toàn
     */
    public static String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
