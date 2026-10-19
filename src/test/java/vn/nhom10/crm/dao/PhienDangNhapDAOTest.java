package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.PhienDangNhap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class PhienDangNhapDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_test_s1_02;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private PhienDangNhapDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL, "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS phien_dang_nhap");
            stmt.execute("DROP TABLE IF EXISTS nguoi_dung");

            stmt.execute("CREATE TABLE nguoi_dung ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "ho_ten VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL UNIQUE, "
                    + "mat_khau VARCHAR(255) NOT NULL, "
                    + "so_dien_thoai VARCHAR(20), "
                    + "trang_thai VARCHAR(20) DEFAULT 'HOAT_DONG', "
                    + "so_lan_sai INT DEFAULT 0, "
                    + "thoi_gian_khoa TIMESTAMP NULL, "
                    + "nhom_kinh_doanh_id INT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE phien_dang_nhap ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "nguoi_dung_id BIGINT NOT NULL, "
                    + "ma_phien VARCHAR(128) NOT NULL UNIQUE, "
                    + "dia_chi_ip VARCHAR(45) NULL, "
                    + "thong_tin_thiet_bi TEXT NULL, "
                    + "trang_thai VARCHAR(20) DEFAULT 'HOAT_DONG', "
                    + "thoi_gian_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "thoi_gian_hoat_dong_cuoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "thoi_gian_thu_hoi TIMESTAMP NULL)");

            stmt.execute("INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau) "
                    + "VALUES (1, 'Nguyen Van A', 'a@crm.vn', 'hash')");
        }

        dao = new PhienDangNhapDAO();
    }

    @AfterEach
    void tearDown() {
        DatabaseConfig.setConnectionSupplier(null);
    }

    @Test
    @DisplayName("Lưu phiên và tìm lại theo mã phiên thành công")
    void testLuuVaTimTheoMaPhien() {
        PhienDangNhap phien = new PhienDangNhap(1L, "SESSION-ABC-1", "127.0.0.1", "Mozilla/5.0");
        dao.luuPhien(phien);

        PhienDangNhap timThay = dao.timTheoMaPhien("SESSION-ABC-1");
        assertNotNull(timThay);
        assertEquals(1L, timThay.getNguoiDungId());
        assertEquals("SESSION-ABC-1", timThay.getMaPhien());
        assertEquals("127.0.0.1", timThay.getDiaChiIp());
        assertEquals(PhienDangNhap.TRANG_THAI_HOAT_DONG, timThay.getTrangThai());
        assertTrue(timThay.isDangHoatDong());
    }

    @Test
    @DisplayName("AC1: Cập nhật hoạt động cuối gia hạn thành công khi phiên đang hoạt động")
    void testCapNhatHoatDongCuoi() throws InterruptedException {
        PhienDangNhap phien = new PhienDangNhap(1L, "SESSION-RENEW-1", "127.0.0.1", "Chrome");
        dao.luuPhien(phien);

        Thread.sleep(50); // đảm bảo thời gian khác biệt

        boolean capNhatThanhCong = dao.capNhatHoatDongCuoi("SESSION-RENEW-1");
        assertTrue(capNhatThanhCong);

        PhienDangNhap sauCapNhat = dao.timTheoMaPhien("SESSION-RENEW-1");
        assertNotNull(sauCapNhat);
        assertTrue(sauCapNhat.getThoiGianHoatDongCuoi().getTime() >= phien.getThoiGianHoatDongCuoi().getTime());
    }

    @Test
    @DisplayName("AC2: Vô hiệu hóa phiên ngay lập tức khi đăng xuất")
    void testVoHieuHoaPhienDangXuat() {
        PhienDangNhap phien = new PhienDangNhap(1L, "SESSION-LOGOUT-1", "127.0.0.1", "Safari");
        dao.luuPhien(phien);

        dao.voHieuHoaPhien("SESSION-LOGOUT-1", PhienDangNhap.TRANG_THAI_DA_DANG_XUAT);

        PhienDangNhap sauThuHoi = dao.timTheoMaPhien("SESSION-LOGOUT-1");
        assertNotNull(sauThuHoi);
        assertEquals(PhienDangNhap.TRANG_THAI_DA_DANG_XUAT, sauThuHoi.getTrangThai());
        assertNotNull(sauThuHoi.getThoiGianThuHoi());
        assertFalse(sauThuHoi.isDangHoatDong());
    }

    @Test
    @DisplayName("AC3: Quét và cập nhật các phiên hết hạn quá thời gian không hoạt động")
    void testQuetVaCapNhatPhienHetHan() throws SQLException {
        PhienDangNhap phien = new PhienDangNhap(1L, "SESSION-EXPIRED-OLD", "127.0.0.1", "Firefox");
        dao.luuPhien(phien);

        // Giả lập thời gian hoạt động cuối cách đây 40 phút
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement stmt = conn.createStatement()) {
            Timestamp pastTime = new Timestamp(System.currentTimeMillis() - 40 * 60 * 1000L);
            stmt.execute("UPDATE phien_dang_nhap SET thoi_gian_hoat_dong_cuoi = '" + pastTime + "' WHERE ma_phien = 'SESSION-EXPIRED-OLD'");
        }

        dao.quetVaCapNhatPhienHetHan(30);

        PhienDangNhap daQuet = dao.timTheoMaPhien("SESSION-EXPIRED-OLD");
        assertNotNull(daQuet);
        assertEquals(PhienDangNhap.TRANG_THAI_HET_HAN, daQuet.getTrangThai());
        assertNotNull(daQuet.getThoiGianThuHoi());
    }
}
