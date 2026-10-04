package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.DoiThu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử DoiThuDAO với H2 in-memory database")
class DoiThuDAOTest {

    private Connection connection;
    private DoiThuDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_doithu;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_doithu;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE doi_thu (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_doi_thu VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_doi_thu VARCHAR(200) NOT NULL, " +
                    "website VARCHAR(255), " +
                    "ghi_chu TEXT, " +
                    "hoat_dong TINYINT(1) NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "doi_thu_id BIGINT" +
                    ")");
        }

        dao = new DoiThuDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Tạo mới và tìm đối thủ theo ID và Mã")
    void testTaoMoiVaTim() {
        DoiThu dt = new DoiThu("DT_MISA", "Công ty CP MISA", "https://misa.vn", "Phần mềm kế toán mạnh", true);
        Long id = dao.taoMoi(dt);
        assertNotNull(id);
        assertTrue(id > 0);

        DoiThu timId = dao.timTheoId(id);
        assertNotNull(timId);
        assertEquals("DT_MISA", timId.getMaDoiThu());
        assertEquals("Công ty CP MISA", timId.getTenDoiThu());
        assertEquals("https://misa.vn", timId.getWebsite());
        assertTrue(timId.isHoatDong());

        DoiThu timMa = dao.timTheoMa("dt_misa");
        assertNotNull(timMa);
        assertEquals(id, timMa.getId());
    }

    @Test
    @DisplayName("Lấy danh sách tất cả và lọc đang hoạt động")
    void testLayDanhSach() {
        dao.taoMoi(new DoiThu("DT_1", "Đối thủ 1", "https://dt1.vn", "Ghi chú 1", true));
        dao.taoMoi(new DoiThu("DT_2", "Đối thủ 2", "https://dt2.vn", "Ghi chú 2", false));

        List<DoiThu> tatCa = dao.layTatCa();
        assertEquals(2, tatCa.size());

        List<DoiThu> dangHoatDong = dao.layDangHoatDong();
        assertEquals(1, dangHoatDong.size());
        assertEquals("DT_1", dangHoatDong.get(0).getMaDoiThu());
    }

    @Test
    @DisplayName("Cập nhật thông tin và trạng thái")
    void testCapNhat() {
        Long id = dao.taoMoi(new DoiThu("DT_BRAVO", "Phần mềm Bravo", "https://bravo.com.vn", "ERP lớn", true));

        DoiThu dt = dao.timTheoId(id);
        dt.setTenDoiThu("Công ty Cổ phần Phần mềm BRAVO");
        dt.setWebsite("https://bravo.vn");
        boolean ok = dao.capNhat(dt);
        assertTrue(ok);

        DoiThu updated = dao.timTheoId(id);
        assertEquals("Công ty Cổ phần Phần mềm BRAVO", updated.getTenDoiThu());
        assertEquals("https://bravo.vn", updated.getWebsite());

        dao.capNhatTrangThai(id, false);
        assertFalse(dao.timTheoId(id).isHoatDong());
    }

    @Test
    @DisplayName("Kiểm tra trùng mã và xóa")
    void testTonTaiMaVaXoa() {
        Long id = dao.taoMoi(new DoiThu("DT_FAST", "Phần mềm Fast", "https://fast.com.vn", "Kế toán", true));

        assertTrue(dao.tonTaiMa("DT_FAST", null));
        assertFalse(dao.tonTaiMa("DT_FAST", id));
        assertFalse(dao.tonTaiMa("DT_UNKNOWN", null));

        assertTrue(dao.xoa(id));
        assertNull(dao.timTheoId(id));
    }

    @Test
    @DisplayName("Đếm số lượng cơ hội tham chiếu đối thủ")
    void testDemSoCoHoiThamChieu() throws Exception {
        Long id = dao.taoMoi(new DoiThu("DT_SAP", "SAP Vietnam", "https://sap.com", "ERP Toàn cầu", true));
        assertEquals(0, dao.demSoCoHoiThamChieu(id));

        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO co_hoi (ma_co_hoi, ten_co_hoi, doi_thu_id) " +
                    "VALUES ('CH002', 'Triển khai CRM Tập đoàn', " + id + ")");
        }

        assertEquals(1, dao.demSoCoHoiThamChieu(id));
    }
}
