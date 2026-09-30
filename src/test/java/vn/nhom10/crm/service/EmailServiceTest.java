package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử EmailService (Story S1-08)")
class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    @Test
    @DisplayName("Kiểm tra gửi email trong chế độ dev/test khi chưa cấu hình SMTP credential")
    void testGuiEmailDevTestMode() {
        boolean result = emailService.guiEmailKichHoatTaiKhoan(
                "nhanvien@crm.vn",
                "Đặng Nguyễn Thị Ánh",
                "TempPass123!",
                "http://localhost:8080/crm/dang-nhap"
        );

        assertTrue(result, "Gửi email trong chế độ DEV/TEST phải thành công");
        assertEquals("TempPass123!", emailService.getLastSentPassword("nhanvien@crm.vn"));
    }

    @Test
    @DisplayName("Kiểm tra xử lý email rỗng hoặc null")
    void testGuiEmailToEmailRong() {
        assertFalse(emailService.guiEmailKichHoatTaiKhoan(null, "Tên", "Pass123", "/dang-nhap"));
        assertFalse(emailService.guiEmailKichHoatTaiKhoan("   ", "Tên", "Pass123", "/dang-nhap"));
    }
}
