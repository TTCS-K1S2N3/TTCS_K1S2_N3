package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tiện ích sinh mã TokenUtil")
class TokenUtilTest {

    @Test
    @DisplayName("Sinh token ngẫu nhiên có độ dài 64 ký tự hex và không trùng nhau")
    void testGenerateSecureToken() {
        String token1 = TokenUtil.generateSecureToken();
        String token2 = TokenUtil.generateSecureToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertEquals(64, token1.length());
        assertEquals(64, token2.length());
        assertNotEquals(token1, token2);
    }
}
