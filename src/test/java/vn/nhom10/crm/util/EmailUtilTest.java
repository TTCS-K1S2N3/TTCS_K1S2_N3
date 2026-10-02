package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tiện ích kiểm tra email EmailUtil")
class EmailUtilTest {

    @Test
    @DisplayName("Kiểm tra định dạng email hợp lệ và không hợp lệ")
    void testIsValidEmail() {
        assertTrue(EmailUtil.isValidEmail("user@example.com"));
        assertTrue(EmailUtil.isValidEmail("sales.lead@company.vn"));
        assertTrue(EmailUtil.isValidEmail("admin123@crm.com.vn"));

        assertFalse(EmailUtil.isValidEmail("invalid-email"));
        assertFalse(EmailUtil.isValidEmail("user@"));
        assertFalse(EmailUtil.isValidEmail("@domain.com"));
        assertFalse(EmailUtil.isValidEmail("user@domain"));
        assertFalse(EmailUtil.isValidEmail(""));
        assertFalse(EmailUtil.isValidEmail(null));
    }
}
