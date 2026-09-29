package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("PasswordUtil: Băm và kiểm tra so khớp mật khẩu chính xác")
    void testHashAndCheckPassword() {
        String raw = "MatKhau@123";
        String hashed = PasswordUtil.hashPassword(raw);

        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$12$") || hashed.startsWith("$2a$"));
        assertTrue(PasswordUtil.checkPassword(raw, hashed));
        assertFalse(PasswordUtil.checkPassword("WrongPass", hashed));
        assertFalse(PasswordUtil.checkPassword(null, hashed));
        assertFalse(PasswordUtil.checkPassword(raw, null));
    }

    @Test
    @DisplayName("PasswordUtil: Kiểm tra độ mạnh mật khẩu tối thiểu 8 ký tự, có chữ và số")
    void testIsValidPassword() {
        assertTrue(PasswordUtil.isValidPassword("Pass1234"));
        assertTrue(PasswordUtil.isValidPassword("Admin@2026"));
        assertFalse(PasswordUtil.isValidPassword("short1")); // < 8 ký tự
        assertFalse(PasswordUtil.isValidPassword("onlyletters")); // Không có số
        assertFalse(PasswordUtil.isValidPassword("12345678")); // Không có chữ
        assertFalse(PasswordUtil.isValidPassword(null));
    }
}
