package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.LyDoThangThua;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LyDoThangThuaDAO với H2 in-memory database")
class LyDoThangThuaDAOTest {

    private Connection connection;
    private LyDoThangThuaDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_lydo;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_lydo;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE ly_do_thang_thua (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_ly_do VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_ly_do VARCHAR(150) NOT NULL, " +
                    "loai VARCHAR(20) NOT NULL, " +
                    "thu_tu_hien_thi INT NOT NULL DEFAULT 0, " +
                    "hoat_dong TINYINT(1) NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "ly_do_thang_thua_id BIGINT" +
                    ")");
        }

        dao = new LyDoThangThuaDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Tạo mới và tìm lý do theo ID và Mã")
    void testTaoMoiVaTim() {
        LyDoThangThua lyDo = new LyDoThangThua("WIN_PRICE", "Giá thành cạnh tranh", LyDoThangThua.LOAI_THANG, 1, true);
        Long id = dao.taoMoi(lyDo);
        assertNotNull(id);
        assertTrue(id > 0);

        LyDoThangThua timId = dao.timTheoId(id);
        assertNotNull(timId);
        assertEquals("WIN_PRICE", timId.getMaLyDo());
        assertEquals("Giá thành cạnh tranh", timId.getTenLyDo());
        assertTrue(timId.laLyDoThang());
        assertTrue(timId.isHoatDong());

        LyDoThangThua timMa = dao.timTheoMa("win_price");
        assertNotNull(timMa);
        assertEquals(id, timMa.getId());
    }

    @Test
    @DisplayName("AC1: Lấy riêng danh sách lý do Thắng và lý do Thua")
    void testLayTheoLoai() {
        dao.taoMoi(new LyDoThangThua("WIN_1", "Thắng 1", LyDoThangThua.LOAI_THANG, 1, true));
        dao.taoMoi(new LyDoThangThua("WIN_2", "Thắng 2", LyDoThangThua.LOAI_THANG, 2, false));
        dao.taoMoi(new LyDoThangThua("LOSS_1", "Thua 1", LyDoThangThua.LOAI_THUA, 1, true));

        List<LyDoThangThua> dsThang = dao.layTheoLoai(LyDoThangThua.LOAI_THANG);
        assertEquals(2, dsThang.size());
        assertTrue(dsThang.stream().allMatch(LyDoThangThua::laLyDoThang));

        List<LyDoThangThua> dsThua = dao.layTheoLoai(LyDoThangThua.LOAI_THUA);
        assertEquals(1, dsThua.size());
        assertTrue(dsThua.stream().allMatch(LyDoThangThua::laLyDoThua));

        List<LyDoThangThua> dsThangKhaDung = dao.layDangHoatDongTheoLoai(LyDoThangThua.LOAI_THANG);
        assertEquals(1, dsThangKhaDung.size());
        assertEquals("WIN_1", dsThangKhaDung.get(0).getMaLyDo());
    }

    @Test
    @DisplayName("Cập nhật thông tin, trạng thái và thứ tự")
    void testCapNhat() {
        LyDoThangThua item = new LyDoThangThua("LOSS_PRICE", "Giá đắt", LyDoThangThua.LOAI_THUA, 1, true);
        Long id = dao.taoMoi(item);

        item.setId(id);
        item.setTenLyDo("Giá thành quá cao so với ngân sách");
        item.setThuTuHienThi(5);
        boolean ok = dao.capNhat(item);
        assertTrue(ok);

        LyDoThangThua updated = dao.timTheoId(id);
        assertEquals("Giá thành quá cao so với ngân sách", updated.getTenLyDo());
        assertEquals(5, updated.getThuTuHienThi());

        dao.capNhatTrangThai(id, false);
        assertFalse(dao.timTheoId(id).isHoatDong());

        dao.capNhatThuTu(id, 10);
        assertEquals(10, dao.timTheoId(id).getThuTuHienThi());
    }

    @Test
    @DisplayName("Kiểm tra tồn tại mã và xóa")
    void testTonTaiMaVaXoa() {
        LyDoThangThua item = new LyDoThangThua("LOSS_TIME", "Chậm tiến độ", LyDoThangThua.LOAI_THUA, 1, true);
        Long id = dao.taoMoi(item);

        assertTrue(dao.tonTaiMa("LOSS_TIME", null));
        assertFalse(dao.tonTaiMa("LOSS_TIME", id));
        assertFalse(dao.tonTaiMa("KHONG_CO", null));

        assertTrue(dao.xoa(id));
        assertNull(dao.timTheoId(id));
    }

    @Test
    @DisplayName("Đếm số lượng cơ hội tham chiếu")
    void testDemSoCoHoiThamChieu() throws Exception {
        LyDoThangThua item = new LyDoThangThua("WIN_BRAND", "Thương hiệu uy tín", LyDoThangThua.LOAI_THANG, 1, true);
        Long id = dao.taoMoi(item);

        assertEquals(0, dao.demSoCoHoiThamChieu(id));

        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO co_hoi (ma_co_hoi, ten_co_hoi, ly_do_thang_thua_id) " +
                    "VALUES ('CH001', 'Hợp đồng ERP', " + id + ")");
        }

        assertEquals(1, dao.demSoCoHoiThamChieu(id));
    }
}
