package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.CoHoi;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử CoHoiDAO - Story S4-08")
class CoHoiDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_08_co_hoi_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private CoHoiDAO dao;

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
            st.execute("CREATE TABLE IF NOT EXISTS giai_doan_pipeline (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_giai_doan VARCHAR(150), " +
                    "hoat_dong TINYINT DEFAULT 1)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS ly_do_thang_thua (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_ly_do VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "khach_hang_id BIGINT NULL, " +
                    "nguoi_lien_he_chinh_id BIGINT NULL, " +
                    "lead_id BIGINT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "nguon_lead_id BIGINT NULL, " +
                    "giai_doan_id BIGINT NULL, " +
                    "nguoi_phu_trach_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "gia_tri_du_kien DECIMAL(18,2) NOT NULL DEFAULT 0.00, " +
                    "xac_suat INT NOT NULL DEFAULT 0, " +
                    "ly_do_sua_xac_suat VARCHAR(500) NULL, " +
                    "ngay_chot_du_kien DATE NULL, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'MO', " +
                    "gia_tri_chot_thuc_te DECIMAL(18,2) NULL, " +
                    "ngay_ky DATE NULL, " +
                    "ly_do_thang_thua_id BIGINT NULL, " +
                    "doi_thu_id BIGINT NULL, " +
                    "ngay_hoat_dong_cuoi DATETIME NULL, " +
                    "bi_dinh_tre TINYINT NOT NULL DEFAULT 0, " +
                    "mo_ta_chi_tiet TEXT NULL, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dao = new CoHoiDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM co_hoi");
        }
    }

    @Test
    @DisplayName("Thêm mới cơ hội thành công và sinh mã tự động")
    void testThemCoHoiVaSinhMa() throws SQLException {
        CoHoi ch = new CoHoi();
        ch.setTenCoHoi("Cơ hội - Công ty ABC");
        ch.setKhachHangId(100L);
        ch.setNguoiLienHeChinhId(200L);
        ch.setLeadId(300L);
        ch.setNguoiPhuTrachId(1L);
        ch.setGiaTriDuKien(BigDecimal.ZERO);
        ch.setXacSuat(10);
        ch.setNgayChotDuKien(LocalDate.now().plusDays(30));

        Long id = dao.themCoHoi(ch);
        assertNotNull(id);
        assertTrue(id > 0);
        assertNotNull(ch.getMaCoHoi());
        assertTrue(ch.getMaCoHoi().startsWith("CH"));

        CoHoi timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("Cơ hội - Công ty ABC", timThay.getTenCoHoi());
        assertEquals(100L, timThay.getKhachHangId());
        assertEquals(300L, timThay.getLeadId());
        assertEquals(0, BigDecimal.ZERO.compareTo(timThay.getGiaTriDuKien()));
    }

    @Test
    @DisplayName("Tìm cơ hội theo Lead ID")
    void testTimTheoLeadId() throws SQLException {
        CoHoi ch = new CoHoi();
        ch.setTenCoHoi("Cơ hội từ Lead 555");
        ch.setKhachHangId(101L);
        ch.setLeadId(555L);
        ch.setNguoiPhuTrachId(1L);

        dao.themCoHoi(ch);

        CoHoi timThay = dao.timTheoLeadId(555L);
        assertNotNull(timThay);
        assertEquals("Cơ hội từ Lead 555", timThay.getTenCoHoi());
        assertEquals(555L, timThay.getLeadId());

        assertNull(dao.timTheoLeadId(999L));
    }
}
