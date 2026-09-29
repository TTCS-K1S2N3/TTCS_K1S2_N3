package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tiện ích PasswordUtil")
class PasswordUtilTest {

    @Test
    @DisplayName("Băm mật khẩu bằng BCrypt và kiểm tra khớp đúng")
    void testHashAndCheckPassword() {
        String raw = "MatKhau123@";
        String hashed = PasswordUtil.hashPassword(raw);

        assertNotNull(hashed);
        assertNotEquals(raw, hashed);
        assertTrue(PasswordUtil.checkPassword(raw, hashed));
        assertFalse(PasswordUtil.checkPassword("SaiMatKhau123@", hashed));
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ của mật khẩu (tối thiểu 8 ký tự, có chữ và số)")
    void testIsValidPassword() {
        assertTrue(PasswordUtil.isValidPassword("Admin123"));
        assertTrue(PasswordUtil.isValidPassword("TempPass@99"));

        assertFalse(PasswordUtil.isValidPassword(null));
        assertFalse(PasswordUtil.isValidPassword("Short1")); // < 8 ký tự
        assertFalse(PasswordUtil.isValidPassword("12345678")); // không có chữ
        assertFalse(PasswordUtil.isValidPassword("abcdefgh")); // không có số
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

        // Kiểm tra có chứa chữ hoa, chữ thường, chữ số
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
