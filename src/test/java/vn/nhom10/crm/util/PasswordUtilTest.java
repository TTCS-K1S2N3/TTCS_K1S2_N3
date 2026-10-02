package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử PasswordUtil")
class PasswordUtilTest {

    @Test
    @DisplayName("Băm mật khẩu BCrypt và xác thực mật khẩu chính xác")
    void testHashPassword_Va_CheckPassword_Dung() {
        String plainPassword = "Password@123";
        String hashed = PasswordUtil.hashPassword(plainPassword);

        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$"));
        assertTrue(PasswordUtil.checkPassword(plainPassword, hashed));
    }

    @Test
    @DisplayName("Kiểm tra mật khẩu sai trả về false")
    void testCheckPassword_SaiMatKhau() {
        String plainPassword = "Password@123";
        String hashed = PasswordUtil.hashPassword(plainPassword);

        assertFalse(PasswordUtil.checkPassword("WrongPassword", hashed));
    }

    @Test
    @DisplayName("SHA-256 Hex trả về chuỗi 64 ký tự chuẩn")
    void testSha256Hex() {
        String input = "test-session-id-123456";
        String hash = PasswordUtil.sha256Hex(input);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        // Hash của cùng 1 chuỗi phải đồng nhất
        assertEquals(hash, PasswordUtil.sha256Hex(input));
    }
}
