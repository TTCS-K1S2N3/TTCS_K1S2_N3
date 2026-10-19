package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.model.NhatKyGuiFormLead;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử BieuMauLeadDAO - Story S4-01")
class BieuMauLeadDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_01_bml_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private BieuMauLeadDAO dao;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        rootConnection = DriverManager.getConnection(H2_URL, "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL, "sa", "");
            } catch (SQLException e) {
                return null;
            }
        });

        try (Statement st = rootConnection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nguon_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nguon VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nguon VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_chien_dich VARCHAR(200) NOT NULL, " +
                    "trang_thai VARCHAR(30) DEFAULT 'DANG_CHAY', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE)");

            st.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_bieu_mau VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_bieu_mau VARCHAR(200) NOT NULL, " +
                    "token_cong_khai VARCHAR(128) NOT NULL UNIQUE, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "cau_hinh_truong_json TEXT NULL, " +
                    "gioi_han_gui_moi_gio INT NULL, " +
                    "bat_honeypot TINYINT DEFAULT 1, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nhat_ky_gui_form_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "bieu_mau_id BIGINT NOT NULL, " +
                    "dia_chi_ip VARCHAR(45) NOT NULL, " +
                    "thanh_cong TINYINT DEFAULT 0, " +
                    "ly_do_tu_choi VARCHAR(255) NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'WEB_INBOUND', 'Website Inbound')");
            st.execute("INSERT INTO chien_dich (id, ma_chien_dich, ten_chien_dich) VALUES (1, 'CD_Q4_2026', 'Chiến dịch Chuyển đổi số Q4')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Phạm Marketing', 'marketing@crm.vn')");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @BeforeEach
    void setUp() throws Exception {
        dao = new BieuMauLeadDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nhat_ky_gui_form_lead");
            st.execute("DELETE FROM bieu_mau_lead");
        }
    }

    @Test
    @DisplayName("AC1: Thêm mới biểu mẫu và truy vấn theo Token công khai")
    void testThemVaTimTheoToken() {
        BieuMauLead bm = new BieuMauLead();
        bm.setMaBieuMau("BM_WEB_01");
        bm.setTenBieuMau("Biểu mẫu tư vấn giải pháp CRM");
        bm.setTokenCongKhai("token_test_1234567890abcdef");
        bm.setNguonLeadId(1L);
        bm.setChienDichId(1L);
        bm.setGioiHanGuiMoiGio(15);
        bm.setBatHoneypot(true);
        bm.setHoatDong(true);
        bm.setCreatedBy(1L);

        Long id = dao.them(bm);
        assertNotNull(id);
        assertTrue(id > 0);

        BieuMauLead timTheoToken = dao.timTheoToken("token_test_1234567890abcdef");
        assertNotNull(timTheoToken);
        assertEquals("BM_WEB_01", timTheoToken.getMaBieuMau());
        assertEquals("Biểu mẫu tư vấn giải pháp CRM", timTheoToken.getTenBieuMau());
        assertEquals("Website Inbound", timTheoToken.getTenNguonLead());
        assertEquals("Chiến dịch Chuyển đổi số Q4", timTheoToken.getTenChienDich());
        assertEquals(15, timTheoToken.getGioiHanGuiMoiGio());
        assertTrue(timTheoToken.isBatHoneypot());
        assertTrue(timTheoToken.isHoatDong());
    }

    @Test
    @DisplayName("AC1: Cập nhật thông tin và trạng thái biểu mẫu")
    void testCapNhatBieuMau() {
        BieuMauLead bm = new BieuMauLead("BM_UPDATE", "Tên cũ", "token_update_abc", 1L);
        Long id = dao.them(bm);
        assertNotNull(id);

        bm.setTenBieuMau("Tên mới cập nhật");
        bm.setHoatDong(false);
        bm.setGioiHanGuiMoiGio(20);
        boolean ok = dao.capNhat(bm);
        assertTrue(ok);

        BieuMauLead sauCapNhat = dao.timTheoId(id);
        assertNotNull(sauCapNhat);
        assertEquals("Tên mới cập nhật", sauCapNhat.getTenBieuMau());
        assertFalse(sauCapNhat.isHoatDong());
        assertEquals(20, sauCapNhat.getGioiHanGuiMoiGio());
    }

    @Test
    @DisplayName("AC1: Kiểm tra trùng mã biểu mẫu")
    void testKiemTraTonTaiMa() {
        BieuMauLead bm = new BieuMauLead("BM_UNIQUE", "Form duy nhất", "token_uniq", 1L);
        dao.them(bm);

        assertTrue(dao.kiemTraTonTaiMa("BM_UNIQUE", null));
        assertFalse(dao.kiemTraTonTaiMa("BM_KHAC", null));
        assertFalse(dao.kiemTraTonTaiMa("BM_UNIQUE", bm.getId()));
    }

    @Test
    @DisplayName("AC3: Ghi nhận nhật ký và đếm số lượt gửi trong 1 giờ theo IP")
    void testGhiNhatKyVaDemLuotGuiTrongMotGio() {
        BieuMauLead bm = new BieuMauLead("BM_RATE", "Form test rate limit", "token_rate", 1L);
        Long bmId = dao.them(bm);

        String ipTest = "192.168.1.100";
        assertEquals(0, dao.demSoLanGuiTrongMotGio(bmId, ipTest));

        // Ghi 3 lượt gửi
        dao.ghiNhatKy(new NhatKyGuiFormLead(bmId, ipTest, true, null));
        dao.ghiNhatKy(new NhatKyGuiFormLead(bmId, ipTest, false, "SPAM_HONEYPOT"));
        dao.ghiNhatKy(new NhatKyGuiFormLead(bmId, ipTest, true, null));

        // Ghi 1 lượt gửi từ IP khác
        dao.ghiNhatKy(new NhatKyGuiFormLead(bmId, "10.0.0.1", true, null));

        // Kiểm tra số lượt đếm đúng theo IP
        assertEquals(3, dao.demSoLanGuiTrongMotGio(bmId, ipTest));
        assertEquals(1, dao.demSoLanGuiTrongMotGio(bmId, "10.0.0.1"));

        List<NhatKyGuiFormLead> lichSu = dao.layLichSuGui(bmId, 10);
        assertEquals(4, lichSu.size());
    }
}
