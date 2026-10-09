package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class CauHinhHeThongDAOTest {

    private static Connection h2Connection;
    private final CauHinhHeThongDAO dao = new CauHinhHeThongDAO();

    @BeforeAll
    public static void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_cauhinh;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cau_hinh_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_cau_hinh VARCHAR(100) NOT NULL UNIQUE, " +
                    "nhom_cau_hinh VARCHAR(60) NOT NULL, " +
                    "kieu_du_lieu VARCHAR(30) NOT NULL, " +
                    "gia_tri TEXT NULL, " +
                    "mo_ta VARCHAR(1000) NULL, " +
                    "bat_buoc_cau_hinh TINYINT(1) DEFAULT 0, " +
                    "bao_mat TINYINT(1) DEFAULT 0, " +
                    "updated_by BIGINT NULL, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_cauhinh;MODE=MySQL");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @Test
    public void testLayGiaTriInt_MacDinhKhiChuaCo() {
        int val = dao.layGiaTriInt("CHUA_TON_TAI_CONFIG", 3);
        assertEquals(3, val);
    }

    @Test
    public void testDatGiaTriVaLayGiaTriInt() {
        boolean ok = dao.datGiaTri(CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO, "5", 1L);
        assertTrue(ok);

        int val = dao.layGiaTriInt(CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO, 3);
        assertEquals(5, val);

        // Cập nhật lại giá trị
        boolean okUpdate = dao.datGiaTri(CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO, "3", 1L);
        assertTrue(okUpdate);
        assertEquals(3, dao.layGiaTriInt(CauHinhHeThongDAO.NGUONG_YEU_CAU_HO_TRO_RUI_RO, 5));
    }
}
