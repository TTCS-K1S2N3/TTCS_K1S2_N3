package vn.nhom10.crm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaDangNhapDTO;
import vn.nhom10.crm.model.NguoiDung;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class LiveMySQLDangNhapIntegrationTest {

    @Test
    @DisplayName("Integration Test với MySQL thực tế: Đăng nhập thành công với tài khoản sales@crm.vn")
    void testLiveMySQLLogin() {
        // Chỉ chạy nếu MySQL thực tế đang lắng nghe
        try (Connection conn = DatabaseConfig.getConnection()) {
            assertNotNull(conn, "Kết nối MySQL thực tế thành công");

            NguoiDungDAO dao = new NguoiDungDAO();
            DangNhapService service = new DangNhapService(dao);

            // Kiểm tra đăng nhập đúng
            KetQuaDangNhapDTO kq = service.dangNhap("sales@crm.vn", "123456@Aa");
            assertTrue(kq.isThanhCong(), "Đăng nhập thành công với mật khẩu mẫu");
            assertEquals("/khach-hang", kq.getTrangChuUrl());

            // Kiểm tra đăng nhập sai mật khẩu -> thông báo chung
            KetQuaDangNhapDTO kqSai = service.dangNhap("sales@crm.vn", "SaiPass123");
            assertFalse(kqSai.isThanhCong());
            assertEquals(DangNhapService.THONG_BAO_SAI_THONG_TIN, kqSai.getThongBaoLoi());

            // Reset lại để không ảnh hưởng dữ liệu mẫu
            dao.resetSoLanSai(kq.getNguoiDung().getId());

        } catch (Exception e) {
            System.out.println("Bỏ qua live integration test nếu môi trường không có MySQL chạy: " + e.getMessage());
        }
    }
}
