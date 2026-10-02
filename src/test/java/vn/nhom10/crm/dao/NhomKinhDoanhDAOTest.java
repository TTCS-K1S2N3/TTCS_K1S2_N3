package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử NhomKinhDoanhDAO - Story S2-06 (AC1, AC2, AC3, AC4)")
class NhomKinhDoanhDAOTest {

    private static Connection h2Connection;
    private static NhomKinhDoanhDAO dao;

    @BeforeAll
    static void setUp() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_nhom_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_nhom_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {}
            return h2Connection;
        });

        dao = new NhomKinhDoanhDAO();

        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(150), loai_khu_vuc VARCHAR(50), " +
                    "khu_vuc_cha_id BIGINT, thu_tu_hien_thi INT, hoat_dong BOOLEAN, created_at TIMESTAMP, updated_at TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), email VARCHAR(255), mat_khau VARCHAR(255), " +
                    "so_dien_thoai VARCHAR(20), trang_thai VARCHAR(30), nhom_kinh_doanh_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nhom VARCHAR(150) NOT NULL, " +
                    "mo_ta TEXT, " +
                    "nhom_cha_id BIGINT, " +
                    "khu_vuc_id BIGINT, " +
                    "truong_nhom_id BIGINT, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Seed user
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (1, 'Trưởng nhóm Bắc', 'lead_bac@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (2, 'Nhân viên 1', 'nv1@crm.vn', 'HOAT_DONG')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, trang_thai) VALUES (3, 'Nhân viên 2', 'nv2@crm.vn', 'HOAT_DONG')");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @Test
    @DisplayName("AC1, AC4: Thêm nhóm, gán trưởng nhóm và gán khu vực địa lý")
    void testThemNhomVaGanTruongNhomKhuVuc() throws SQLException {
        NhomKinhDoanh nhom = new NhomKinhDoanh();
        nhom.setMaNhom("KD_TEST_1");
        nhom.setTenNhom("Nhóm Bán Hàng Số 1");
        nhom.setMoTa("Mô tả nhóm 1");
        nhom.setTruongNhomId(1L);
        nhom.setHoatDong(true);

        long id = dao.themNhom(nhom);
        assertTrue(id > 0);

        NhomKinhDoanh tim = dao.timTheoId(id);
        assertNotNull(tim);
        assertEquals("KD_TEST_1", tim.getMaNhom());
        assertEquals("Nhóm Bán Hàng Số 1", tim.getTenNhom());
        assertEquals(1L, tim.getTruongNhomId());

        // Gán trưởng nhóm mới
        dao.ganTruongNhom(id, 2L);
        NhomKinhDoanh sauGan = dao.timTheoId(id);
        assertEquals(2L, sauGan.getTruongNhomId());
    }

    @Test
    @DisplayName("AC1, AC3: Cây tổ chức và thu thập tất cả ID nhóm con cháu đệ quy")
    void testCayNhomVaThuThapConChau() throws SQLException {
        // Nhóm gốc A (ID = rootId)
        NhomKinhDoanh nhomA = new NhomKinhDoanh();
        nhomA.setMaNhom("NHOM_A");
        nhomA.setTenNhom("Khối Kinh Doanh A");
        long rootId = dao.themNhom(nhomA);

        // Nhóm con B1 (con của A)
        NhomKinhDoanh nhomB1 = new NhomKinhDoanh();
        nhomB1.setMaNhom("NHOM_B1");
        nhomB1.setTenNhom("Phòng Kinh Doanh B1");
        nhomB1.setNhomChaId(rootId);
        long b1Id = dao.themNhom(nhomB1);

        // Nhóm con B2 (con của A)
        NhomKinhDoanh nhomB2 = new NhomKinhDoanh();
        nhomB2.setMaNhom("NHOM_B2");
        nhomB2.setTenNhom("Phòng Kinh Doanh B2");
        nhomB2.setNhomChaId(rootId);
        long b2Id = dao.themNhom(nhomB2);

        // Nhóm cháu C1 (con của B1)
        NhomKinhDoanh nhomC1 = new NhomKinhDoanh();
        nhomC1.setMaNhom("NHOM_C1");
        nhomC1.setTenNhom("Tổ Kinh Doanh C1");
        nhomC1.setNhomChaId(b1Id);
        long c1Id = dao.themNhom(nhomC1);

        // Kiểm tra cây tổ chức
        List<NhomKinhDoanh> cay = dao.layCayNhomKinhDoanh();
        assertFalse(cay.isEmpty());

        // Kiểm tra thu thập ID nhóm con cháu cho A: phải gồm {rootId, b1Id, b2Id, c1Id}
        Set<Long> dsA = dao.layDsIdNhomConVaChau(rootId);
        assertTrue(dsA.contains(rootId));
        assertTrue(dsA.contains(b1Id));
        assertTrue(dsA.contains(b2Id));
        assertTrue(dsA.contains(c1Id));
        assertEquals(4, dsA.size());

        // Kiểm tra thu thập ID nhóm con cháu cho B1: phải gồm {b1Id, c1Id}
        Set<Long> dsB1 = dao.layDsIdNhomConVaChau(b1Id);
        assertTrue(dsB1.contains(b1Id));
        assertTrue(dsB1.contains(c1Id));
        assertFalse(dsB1.contains(rootId));
        assertFalse(dsB1.contains(b2Id));
        assertEquals(2, dsB1.size());

        // Kiểm tra thu thập ID cho C1: chỉ gồm {c1Id}
        Set<Long> dsC1 = dao.layDsIdNhomConVaChau(c1Id);
        assertEquals(1, dsC1.size());
        assertTrue(dsC1.contains(c1Id));
    }

    @Test
    @DisplayName("AC2: Mỗi nhân viên thuộc đúng một nhóm tại một thời điểm")
    void testGanNhanVienVaoNhom() throws SQLException {
        NhomKinhDoanh n1 = new NhomKinhDoanh();
        n1.setMaNhom("TEAM_A1");
        n1.setTenNhom("Team A1");
        long n1Id = dao.themNhom(n1);

        NhomKinhDoanh n2 = new NhomKinhDoanh();
        n2.setMaNhom("TEAM_A2");
        n2.setTenNhom("Team A2");
        long n2Id = dao.themNhom(n2);

        // Gán nhân viên 3 vào Team A1
        dao.ganNhanVienVaoNhom(3L, n1Id);
        List<NguoiDung> ds1 = dao.layDsThanhVien(n1Id);
        assertTrue(ds1.stream().anyMatch(u -> u.getId() == 3L));

        // Chuyển nhân viên 3 sang Team A2
        dao.ganNhanVienVaoNhom(3L, n2Id);
        List<NguoiDung> ds1Sau = dao.layDsThanhVien(n1Id);
        assertFalse(ds1Sau.stream().anyMatch(u -> u.getId() == 3L));

        List<NguoiDung> ds2 = dao.layDsThanhVien(n2Id);
        assertTrue(ds2.stream().anyMatch(u -> u.getId() == 3L));
    }
}
