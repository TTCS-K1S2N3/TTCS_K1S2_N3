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
        assertFalse(PasswordUtil.isValidPassword(null));
    }

    @Test
    @DisplayName("AC: Tự động sinh mật khẩu tạm đáp ứng độ mạnh và tính ngẫu nhiên")
    void testTaoMatKhauTam() {
        String temp1 = PasswordUtil.taoMatKhauTam();
        String temp2 = PasswordUtil.taoMatKhauTam();

        assertNotNull(temp1);
        assertNotNull(temp2);
        assertTrue(temp1.length() >= 8);
        assertTrue(PasswordUtil.isValidPassword(temp1));
        assertNotEquals(temp1, temp2, "Hai mật khẩu tạm sinh ra liên tiếp phải khác nhau");

        boolean coHoa = false;
        boolean coThuong = false;
        boolean coSo = false;
        for (char c : temp1.toCharArray()) {
            if (Character.isUpperCase(c)) coHoa = true;
            if (Character.isLowerCase(c)) coThuong = true;
            if (Character.isDigit(c)) coSo = true;
        }
        assertTrue(coHoa, "Mật khẩu tạm phải có chữ hoa");
        assertTrue(coThuong, "Mật khẩu tạm phải có chữ thường");
        assertTrue(coSo, "Mật khẩu tạm phải có chữ số");
    }
}
