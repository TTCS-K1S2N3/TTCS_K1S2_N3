package vn.nhom10.crm.dao;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.HanhDongPhanBoEnum;
import vn.nhom10.crm.model.LichSuPhanBoLead;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LichSuPhanBoLeadDAO - Story S4-06")
class LichSuPhanBoLeadDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_06_lichsu_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private LichSuPhanBoLeadDAO dao;

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
            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS quy_tac_phan_bo_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_quy_tac VARCHAR(200) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_phan_bo_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "lead_id BIGINT NOT NULL, " +
                    "quy_tac_id BIGINT NULL, " +
                    "nguoi_dung_cu_id BIGINT NULL, " +
                    "nguoi_dung_moi_id BIGINT NULL, " +
                    "nhom_id BIGINT NULL, " +
                    "hanh_dong VARCHAR(30) NOT NULL, " +
                    "ly_do VARCHAR(500) NULL, " +
                    "thuc_hien_boi_id BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    void cleanUp() throws Exception {
        dao = new LichSuPhanBoLeadDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM lich_su_phan_bo_lead");
        }
    }

    @Test
    @DisplayName("Thêm mới và lấy danh sách lịch sử phân bổ theo Lead ID")
    void testThemVaLayTheoLeadId() {
        LichSuPhanBoLead ls1 = new LichSuPhanBoLead(
                10L, 1L, 100L, 5L,
                HanhDongPhanBoEnum.TU_DONG_THEO_QUY_TAC,
                "Khớp quy tắc phân bổ Miền Bắc",
                null
        );

        Long id1 = dao.them(ls1);
        assertNotNull(id1);
        assertTrue(id1 > 0);

        LichSuPhanBoLead ls2 = new LichSuPhanBoLead(
                10L, null, 101L, 5L,
                HanhDongPhanBoEnum.TRUONG_NHOM_PHAN_TAY,
                "Trưởng nhóm điều phối lại",
                2L
        );
        Long id2 = dao.them(ls2);
        assertNotNull(id2);

        List<LichSuPhanBoLead> lichSuList = dao.layTheoLeadId(10L);
        assertEquals(2, lichSuList.size());
        assertEquals(HanhDongPhanBoEnum.TRUONG_NHOM_PHAN_TAY, lichSuList.get(0).getHanhDong());
        assertEquals(HanhDongPhanBoEnum.TU_DONG_THEO_QUY_TAC, lichSuList.get(1).getHanhDong());
    }
}
