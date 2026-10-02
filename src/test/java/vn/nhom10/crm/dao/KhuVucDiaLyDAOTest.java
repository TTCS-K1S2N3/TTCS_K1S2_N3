package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.KhuVucDiaLy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử KhuVucDiaLyDAO - Story S2-06 (AC4)")
class KhuVucDiaLyDAOTest {

    private static Connection h2Connection;
    private static KhuVucDiaLyDAO dao;

    @BeforeAll
    static void setUp() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_kv_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_kv_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {}
            return h2Connection;
        });

        dao = new KhuVucDiaLyDAO();

        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khu_vuc VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_khu_vuc VARCHAR(150) NOT NULL, " +
                    "loai_khu_vuc VARCHAR(50), " +
                    "khu_vuc_cha_id BIGINT, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50), ten_nhom VARCHAR(150), mo_ta TEXT, " +
                    "nhom_cha_id BIGINT, khu_vuc_id BIGINT, truong_nhom_id BIGINT, hoat_dong BOOLEAN DEFAULT TRUE)");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @Test
    @DisplayName("AC4: Thêm mới, tìm kiếm và cập nhật khu vực địa lý thành công")
    void testCrudKhuVuc() throws SQLException {
        KhuVucDiaLy kv = new KhuVucDiaLy();
        kv.setMaKhuVuc("KV_MB");
        kv.setTenKhuVuc("Miền Bắc");
        kv.setLoaiKhuVuc("MIEN");
        kv.setThuTuHienThi(1);
        kv.setHoatDong(true);

        long id = dao.themKhuVuc(kv);
        assertTrue(id > 0);

        KhuVucDiaLy timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("KV_MB", timThay.getMaKhuVuc());
        assertEquals("Miền Bắc", timThay.getTenKhuVuc());

        // Kiểm tra trùng mã
        assertTrue(dao.kiemTraTonTaiMa("KV_MB", null));
        assertFalse(dao.kiemTraTonTaiMa("KV_MB", id));
        assertFalse(dao.kiemTraTonTaiMa("KV_UNKNOWN", null));

        // Cập nhật
        timThay.setTenKhuVuc("Vùng Miền Bắc");
        boolean ok = dao.capNhatKhuVuc(timThay);
        assertTrue(ok);

        KhuVucDiaLy sauCapNhat = dao.timTheoMa("KV_MB");
        assertEquals("Vùng Miền Bắc", sauCapNhat.getTenKhuVuc());
    }

    @Test
    @DisplayName("AC4: Cây phân cấp khu vực địa lý cha - con")
    void testCayKhuVuc() throws SQLException {
        KhuVucDiaLy kvCha = new KhuVucDiaLy();
        kvCha.setMaKhuVuc("KV_MN");
        kvCha.setTenKhuVuc("Miền Nam");
        kvCha.setLoaiKhuVuc("MIEN");
        long chaId = dao.themKhuVuc(kvCha);

        KhuVucDiaLy kvCon = new KhuVucDiaLy();
        kvCon.setMaKhuVuc("KV_HCM");
        kvCon.setTenKhuVuc("TP. Hồ Chí Minh");
        kvCon.setLoaiKhuVuc("TINH_THANH");
        kvCon.setKhuVucChaId(chaId);
        long conId = dao.themKhuVuc(kvCon);
        assertTrue(conId > 0);

        List<KhuVucDiaLy> cay = dao.layCayKhuVuc();
        assertFalse(cay.isEmpty());

        KhuVucDiaLy gocMN = cay.stream().filter(k -> k.getId() == chaId).findFirst().orElse(null);
        assertNotNull(gocMN);
        assertEquals(1, gocMN.getCapDo());
        assertFalse(gocMN.getDsKhuVucCon().isEmpty());
        assertEquals("TP. Hồ Chí Minh", gocMN.getDsKhuVucCon().get(0).getTenKhuVuc());
    }
}
