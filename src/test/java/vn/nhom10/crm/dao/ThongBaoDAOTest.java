package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.ThongBao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ThongBaoDAOTest {

    private static Connection h2Connection;
    private final ThongBaoDAO dao = new ThongBaoDAO();

    @BeforeAll
    public static void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_thongbao;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS thong_bao (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "loai_thong_bao_id BIGINT NOT NULL, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "noi_dung TEXT NULL, " +
                    "loai_doi_tuong VARCHAR(50) NULL, " +
                    "doi_tuong_id BIGINT NULL, " +
                    "duong_dan_mo VARCHAR(500) NULL, " +
                    "da_doc TINYINT(1) DEFAULT 0, " +
                    "doc_luc TIMESTAMP NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_thongbao;MODE=MySQL");
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
    public void testTaoVaLayThongBao() {
        ThongBao tb = new ThongBao(101L, ThongBaoDAO.LOAI_THONG_BAO_KHACH_HANG_RUI_RO,
                "[Cảnh báo rủi ro] Khách hàng ABC", "Khách hàng có nhiều yêu cầu chưa xử lý",
                "KHACH_HANG", 1L, "/chi-tiet-ban-ghi?id=1");

        Long id = dao.taoThongBao(tb);
        assertNotNull(id);
        assertTrue(id > 0);

        List<ThongBao> ds = dao.layDanhSachTheoNguoiDung(101L, false, 10);
        assertFalse(ds.isEmpty());
        assertEquals("[Cảnh báo rủi ro] Khách hàng ABC", ds.get(0).getTieuDe());
        assertEquals(1, dao.demThongBaoChuaDoc(101L));

        // Đánh dấu đã đọc
        boolean marked = dao.danhDauDaDoc(id, 101L);
        assertTrue(marked);
        assertEquals(0, dao.demThongBaoChuaDoc(101L));
    }
}
