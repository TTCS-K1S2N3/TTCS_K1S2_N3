package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tiện ích mật khẩu PasswordUtil")
class PasswordUtilTest {

    @Test
    @DisplayName("Băm mật khẩu và xác thực bằng BCrypt thành công")
    void testHashAndCheckPassword() {
        String rawPassword = "Password@123";
        String hashed = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hashed);
        assertNotEquals(rawPassword, hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$"));

        assertTrue(PasswordUtil.checkPassword(rawPassword, hashed));
        assertFalse(PasswordUtil.checkPassword("WrongPassword123", hashed));
    }

    @Test
    @DisplayName("Kiểm tra quy tắc mật khẩu tối thiểu 8 ký tự, có cả chữ và số")
    void testIsValidPassword() {
        // Hợp lệ: >= 8 ký tự, có chữ và số
        assertTrue(PasswordUtil.isValidPassword("MatKhau123"));
        assertTrue(PasswordUtil.isValidPassword("Abcdefgh8"));
        assertTrue(PasswordUtil.isValidPassword("P@ssw0rdCRM"));

        // Không hợp lệ: dưới 8 ký tự
        assertFalse(PasswordUtil.isValidPassword("Abc12"));
        assertFalse(PasswordUtil.isValidPassword("1234567"));

        // Không hợp lệ: chỉ có chữ, không có số
        assertFalse(PasswordUtil.isValidPassword("OnlyLettersPassword"));

        // Không hợp lệ: chỉ có số, không có chữ
        assertFalse(PasswordUtil.isValidPassword("1234567890"));

        // Không hợp lệ: null hoặc rỗng
        assertFalse(PasswordUtil.isValidPassword(null));
        assertFalse(PasswordUtil.isValidPassword(""));
    }
}
