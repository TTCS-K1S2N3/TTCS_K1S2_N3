package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.model.Lead;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử DuplicateLeadDetector - Story S4-04 (AC1)")
class DuplicateLeadDetectorTest {

    @Test
    @DisplayName("Chuẩn hóa email: trim, lowercase và xử lý null/rỗng")
    void testChuanHoaEmail() {
        assertEquals("demo01@example.test", DuplicateLeadDetector.chuanHoaEmail("  DEMO01@example.TEST  "));
        assertEquals("test@crm.vn", DuplicateLeadDetector.chuanHoaEmail("Test@CRM.vn"));
        assertEquals("", DuplicateLeadDetector.chuanHoaEmail(null));
        assertEquals("", DuplicateLeadDetector.chuanHoaEmail("   "));
    }

    @Test
    @DisplayName("Chuẩn hóa số điện thoại: loại bỏ ký tự không phải số và đưa +84 về 0")
    void testChuanHoaSoDienThoai() {
        assertEquals("0901234567", DuplicateLeadDetector.chuanHoaSoDienThoai("090.123.4567"));
        assertEquals("0901234567", DuplicateLeadDetector.chuanHoaSoDienThoai("090-123-4567"));
        assertEquals("0901234567", DuplicateLeadDetector.chuanHoaSoDienThoai("(090) 123 4567"));
        assertEquals("0901234567", DuplicateLeadDetector.chuanHoaSoDienThoai("+84 901 234 567"));
        assertEquals("0988000111", DuplicateLeadDetector.chuanHoaSoDienThoai("84988000111"));
        assertEquals("", DuplicateLeadDetector.chuanHoaSoDienThoai(null));
        assertEquals("", DuplicateLeadDetector.chuanHoaSoDienThoai("   "));
    }

    @Test
    @DisplayName("Chuẩn hóa tên công ty: loại bỏ từ khóa pháp lý, dấu tiếng Việt và ký tự đặc biệt")
    void testChuanHoaTenCongTy() {
        assertEquals("abc", DuplicateLeadDetector.chuanHoaTenCongTy("Công ty TNHH ABC"));
        assertEquals("alpha beta", DuplicateLeadDetector.chuanHoaTenCongTy("Công ty Cổ phần Alpha Beta"));
        assertEquals("viet nam tech 1", DuplicateLeadDetector.chuanHoaTenCongTy("Tập đoàn Việt Nam Tech - Chi nhánh 1"));
        assertEquals("", DuplicateLeadDetector.chuanHoaTenCongTy(null));
    }

    @Test
    @DisplayName("Độ tương đồng tên công ty: Levenshtein similarity")
    void testTinhTyLeTuongDong() {
        double tyLe = DuplicateLeadDetector.tinhTyLeTuongDong("cong ty hoa sen", "cong ty hoa sen");
        assertEquals(1.0, tyLe, 0.001);

        double tyLeGanGiong = DuplicateLeadDetector.tinhTyLeTuongDong("cong nghe fpt", "cong nghe fpi");
        assertTrue(tyLeGanGiong >= 0.70);

        double tyLeKhacBiet = DuplicateLeadDetector.tinhTyLeTuongDong("cong ty may mac", "dien may xanh");
        assertTrue(tyLeKhacBiet < 0.50);
    }

    @Test
    @DisplayName("Phát hiện trùng theo Email, Số điện thoại và Tên công ty (AC1)")
    void testKiemTraTrungLead() {
        Lead leadA = new Lead();
        leadA.setId(101L);
        leadA.setEmail("contact01@example.test");
        leadA.setSoDienThoai("0900000000");
        leadA.setTenCongTy("Công ty ABC Demo");

        Lead leadB = new Lead();
        leadB.setId(102L);
        leadB.setEmail("CONTACT01@EXAMPLE.TEST");
        leadB.setSoDienThoai("0900000000");
        leadB.setTenCongTy("Cty TNHH ABC Demo");

        List<String> reasons = DuplicateLeadDetector.kiemTraTrungLead(leadA, leadB);
        assertTrue(reasons.contains("EMAIL"));
        assertTrue(reasons.contains("PHONE"));
        assertTrue(reasons.contains("COMPANY"));
    }

    @Test
    @DisplayName("Không báo trùng khi so sánh Lead với chính nó")
    void testKhongBaoTrungChinhNo() {
        Lead leadA = new Lead();
        leadA.setId(101L);
        leadA.setEmail("same@test.com");
        leadA.setSoDienThoai("0912345678");

        List<String> reasons = DuplicateLeadDetector.kiemTraTrungLead(leadA, leadA);
        assertTrue(reasons.isEmpty());
    }

    @Test
    @DisplayName("Không báo trùng giả khi email, số điện thoại hoặc tên công ty bị null/rỗng")
    void testKhongBaoTrungGiaKhiNull() {
        Lead leadA = new Lead();
        leadA.setId(101L);
        leadA.setEmail(null);
        leadA.setSoDienThoai(null);
        leadA.setTenCongTy(null);

        Lead leadB = new Lead();
        leadB.setId(102L);
        leadB.setEmail("");
        leadB.setSoDienThoai("   ");
        leadB.setTenCongTy(null);

        List<String> reasons = DuplicateLeadDetector.kiemTraTrungLead(leadA, leadB);
        assertTrue(reasons.isEmpty());
    }

    @Test
    @DisplayName("Làm mờ (mask) email và số điện thoại bảo vệ PII")
    void testMaskPii() {
        String maskedEmail = DuplicateLeadDetector.maskEmail("contact01@example.test");
        assertTrue(maskedEmail.startsWith("co***"));
        assertTrue(maskedEmail.endsWith("@example.test"));

        String maskedPhone = DuplicateLeadDetector.maskPhone("0901234567");
        assertEquals("090****567", maskedPhone);
    }
}
