package vn.nhom10.crm.dao;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.KieuPhanBoEnum;
import vn.nhom10.crm.model.QuyTacPhanBoLead;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử QuyTacPhanBoLeadDAO - Story S4-06")
class QuyTacPhanBoLeadDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_06_quytac_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private QuyTacPhanBoLeadDAO dao;

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
            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_nhom VARCHAR(150) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS quy_tac_phan_bo_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_quy_tac VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_quy_tac VARCHAR(200) NOT NULL, " +
                    "thu_tu_uu_tien INT NOT NULL, " +
                    "kieu_phan_bo VARCHAR(30) NOT NULL, " +
                    "dieu_kien_json TEXT NULL, " +
                    "nhom_dich_id BIGINT NULL, " +
                    "nguoi_dung_dich_id BIGINT NULL, " +
                    "nguoi_dung_lan_cuoi_id BIGINT NULL, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "CONSTRAINT uk_qtpb_tt UNIQUE (thu_tu_uu_tien))");
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
        dao = new QuyTacPhanBoLeadDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM quy_tac_phan_bo_lead");
        }
    }

    @Test
    @DisplayName("Thêm mới quy tắc và tìm lại theo ID / mã thành công")
    void testThemVaTimTheoId() {
        QuyTacPhanBoLead qt = new QuyTacPhanBoLead(
                "QT_TEST_01", "Quy tắc Miền Bắc", 1,
                KieuPhanBoEnum.THEO_KHU_VUC, "{\"khu_vuc_ids\":[1,2]}",
                10L, null
        );

        Long id = dao.them(qt);
        assertNotNull(id);
        assertTrue(id > 0);

        QuyTacPhanBoLead timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("QT_TEST_01", timThay.getMaQuyTac());
        assertEquals("Quy tắc Miền Bắc", timThay.getTenQuyTac());
        assertEquals(1, timThay.getThuTuUuTien());
        assertEquals(KieuPhanBoEnum.THEO_KHU_VUC, timThay.getKieuPhanBo());
        assertTrue(timThay.isHoatDong());

        QuyTacPhanBoLead timTheoMa = dao.timTheoMa("QT_TEST_01");
        assertNotNull(timTheoMa);
        assertEquals(id, timTheoMa.getId());
    }

    @Test
    @DisplayName("Cập nhật quy tắc và con trỏ Round-Robin thành công")
    void testCapNhatVaCapNhatNguoiDungLanCuoi() throws SQLException {
        QuyTacPhanBoLead qt = new QuyTacPhanBoLead(
                "QT_TEST_RR", "Quy tắc xoay vòng", 1,
                KieuPhanBoEnum.XOAY_VONG, "{}",
                5L, null
        );
        Long id = dao.them(qt);
        assertNotNull(id);

        qt.setTenQuyTac("Quy tắc xoay vòng mới");
        qt.setHoatDong(false);
        boolean capNhatOk = dao.capNhat(qt);
        assertTrue(capNhatOk);

        QuyTacPhanBoLead sauCapNhat = dao.timTheoId(id);
        assertEquals("Quy tắc xoay vòng mới", sauCapNhat.getTenQuyTac());
        assertFalse(sauCapNhat.isHoatDong());

        // Cập nhật người dùng lần cuối (Round-Robin)
        try (Connection conn = rootConnection) {
            boolean capNhatLCOk = dao.capNhatNguoiDungLanCuoi(conn, id, 100L);
            assertTrue(capNhatLCOk);
        }

        QuyTacPhanBoLead sauLC = dao.timTheoId(id);
        assertEquals(100L, sauLC.getNguoiDungLanCuoiId());
    }

    @Test
    @DisplayName("Đổi thứ tự ưu tiên (LÊN / XUỐNG) an toàn không vi phạm ràng buộc Unique")
    void testDoiThuTuUuTien() {
        QuyTacPhanBoLead qt1 = new QuyTacPhanBoLead("QT_1", "Rule 1", 1, KieuPhanBoEnum.THEO_KHU_VUC, "{}", null, null);
        QuyTacPhanBoLead qt2 = new QuyTacPhanBoLead("QT_2", "Rule 2", 2, KieuPhanBoEnum.THEO_NGANH_NGHE, "{}", null, null);
        QuyTacPhanBoLead qt3 = new QuyTacPhanBoLead("QT_3", "Rule 3", 3, KieuPhanBoEnum.XOAY_VONG, "{}", null, null);

        Long id1 = dao.them(qt1);
        Long id2 = dao.them(qt2);
        Long id3 = dao.them(qt3);

        // Đổi Rule 2 lên trên Rule 1
        boolean doiOk = dao.doiThuTuUuTien(id2, "LEN");
        assertTrue(doiOk);

        QuyTacPhanBoLead rule2Sau = dao.timTheoId(id2);
        QuyTacPhanBoLead rule1Sau = dao.timTheoId(id1);
        assertEquals(1, rule2Sau.getThuTuUuTien());
        assertEquals(2, rule1Sau.getThuTuUuTien());

        // Đổi Rule 2 xuống lại
        boolean doiXuongOk = dao.doiThuTuUuTien(id2, "XUONG");
        assertTrue(doiXuongOk);

        assertEquals(2, dao.timTheoId(id2).getThuTuUuTien());
        assertEquals(1, dao.timTheoId(id1).getThuTuUuTien());
    }

    @Test
    @DisplayName("Bật/tắt trạng thái và xóa quy tắc thành công")
    void testDoiTrangThaiVaXoa() {
        QuyTacPhanBoLead qt = new QuyTacPhanBoLead("QT_DEL", "Quy tắc xóa", 1, KieuPhanBoEnum.THEO_KHU_VUC, "{}", null, null);
        Long id = dao.them(qt);

        assertTrue(dao.capNhatTrangThai(id, false));
        assertFalse(dao.timTheoId(id).isHoatDong());

        assertTrue(dao.xoa(id));
        assertNull(dao.timTheoId(id));
    }
}
