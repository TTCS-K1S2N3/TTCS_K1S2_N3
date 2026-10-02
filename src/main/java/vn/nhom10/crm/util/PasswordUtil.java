package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility băm và kiểm tra mật khẩu sử dụng BCrypt (chuẩn an toàn).
 * Cung cấp SHA-256 hex để băm mã phiên và token bảo mật.
 */
public class PasswordUtil {

    private PasswordUtil() {
        // utility class
    }

    /**
     * Băm mật khẩu dạng plaintext bằng BCrypt với log_rounds mặc định (10).
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống khi băm");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Kiểm tra mật khẩu plaintext với chuỗi hash BCrypt.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Chuỗi hash không đúng format BCrypt
            return false;
        }
    }

    /**
     * Băm chuỗi bằng SHA-256 và trả về dạng hex 64 ký tự.
     * Sử dụng cho ma_phien_hash và token_hash trong database.
     */
    public static String sha256Hex(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hệ thống không hỗ trợ thuật toán SHA-256", e);
        }
    }
}
