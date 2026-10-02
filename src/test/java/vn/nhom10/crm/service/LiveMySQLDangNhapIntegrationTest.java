package vn.nhom10.crm.service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class LiveMySQLDangNhapIntegrationTest {

    @Test
    @DisplayName("Integration Test với MySQL thực tế: Đăng nhập với tài khoản kiểm thử được cấu hình qua môi trường")
    void testLiveMySQLLogin() {
        // Chỉ chạy khi có cờ môi trường kích hoạt opt-in rõ ràng
        String runLive = System.getenv("RUN_LIVE_TESTS");
        if (runLive == null) {
            runLive = System.getenv("LIVE_MYSQL_TEST");
        }
        Assumptions.assumeTrue("true".equalsIgnoreCase(runLive),
                "Bỏ qua: Live MySQL Integration Test chỉ chạy khi được bật chủ động qua biến môi trường RUN_LIVE_TESTS=true");

        String testEmail = System.getenv("LIVE_TEST_EMAIL");
        String testPassword = System.getenv("LIVE_TEST_PASSWORD");
        Assumptions.assumeTrue(testEmail != null && !testEmail.isBlank() && testPassword != null && !testPassword.isBlank(),
                "Bỏ qua: Thiếu biến môi trường LIVE_TEST_EMAIL hoặc LIVE_TEST_PASSWORD cho live test");

        try (Connection conn = DatabaseConfig.getConnection()) {
            assertNotNull(conn, "Kết nối MySQL thực tế thành công");

            NguoiDungDAO dao = new NguoiDungDAO();
            DangNhapService service = new DangNhapService(dao);

            // Kiểm tra đăng nhập đúng với tài khoản từ biến môi trường
            KetQuaDangNhapDTO kq = service.dangNhap(testEmail, testPassword);
            assertTrue(kq.isThanhCong(), "Đăng nhập thành công với thông tin xác thực kiểm thử");
            assertNotNull(kq.getTrangChuUrl());

            // Kiểm tra đăng nhập sai mật khẩu -> thông báo chung
            KetQuaDangNhapDTO kqSai = service.dangNhap(testEmail, "SaiPassKhongDung_9999");
            assertFalse(kqSai.isThanhCong());
            assertEquals(DangNhapService.THONG_BAO_SAI_THONG_TIN, kqSai.getThongBaoLoi());

            // Reset lại để không ảnh hưởng dữ liệu thực tế
            if (kq.getNguoiDung() != null) {
                dao.resetSoLanSai(kq.getNguoiDung().getId());
            }
        } catch (Exception e) {
            fail("Lỗi khi thực thi Live MySQL Integration Test: " + e.getMessage());
        }
    }
}
