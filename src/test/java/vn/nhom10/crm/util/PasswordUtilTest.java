package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tiện ích mật khẩu PasswordUtil - AC 2")
class PasswordUtilTest {

    @Test
    @DisplayName("AC 2: Mật khẩu hợp lệ khi có tối thiểu 8 ký tự, gồm cả chữ và số")
    void testValidPassword() {
        assertTrue(PasswordUtil.isValidPassword("Password123"));
        assertTrue(PasswordUtil.isValidPassword("crm2026@Pass"));
        assertTrue(PasswordUtil.isValidPassword("MatKhau@123"));
        assertTrue(PasswordUtil.isValidPassword("1234567a"));
        assertTrue(PasswordUtil.isValidPassword("abcdefg1"));
    }

    @Test
    @DisplayName("AC 2: Mật khẩu không hợp lệ khi dưới 8 ký tự")
    void testInvalidPassword_TooShort() {
        assertFalse(PasswordUtil.isValidPassword("Pass1"));
        assertFalse(PasswordUtil.isValidPassword("Abc1234"));
        assertFalse(PasswordUtil.isValidPassword("1234567"));
        assertFalse(PasswordUtil.isValidPassword(""));
        assertFalse(PasswordUtil.isValidPassword(null));
    }

    @Test
    @DisplayName("AC 2: Mật khẩu không hợp lệ khi chỉ có chữ cái, thiếu chữ số")
    void testInvalidPassword_NoDigit() {
        assertFalse(PasswordUtil.isValidPassword("PasswordOnly"));
        assertFalse(PasswordUtil.isValidPassword("abcdefghijkl"));
        assertFalse(PasswordUtil.isValidPassword("CRMSystemPass"));
    }

    @Test
    @DisplayName("AC 2: Mật khẩu không hợp lệ khi chỉ có số hoặc ký tự đặc biệt, thiếu chữ cái")
    void testInvalidPassword_NoLetter() {
        assertFalse(PasswordUtil.isValidPassword("12345678"));
        assertFalse(PasswordUtil.isValidPassword("1234567890"));
        assertFalse(PasswordUtil.isValidPassword("12345678!"));
    }

    @Test
    @DisplayName("Băm và so khớp mật khẩu bằng thuật toán BCrypt")
    void testHashAndCheckPassword() {
        String rawPassword = "MatKhau@123";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2a$") || hash.startsWith("$2b$"));

        // So khớp đúng
        assertTrue(PasswordUtil.checkPassword(rawPassword, hash));

        // So khớp sai
        assertFalse(PasswordUtil.checkPassword("WrongPassword123", hash));
        assertFalse(PasswordUtil.checkPassword("", hash));
        assertFalse(PasswordUtil.checkPassword(null, hash));
        assertFalse(PasswordUtil.checkPassword(rawPassword, null));
    }
}
