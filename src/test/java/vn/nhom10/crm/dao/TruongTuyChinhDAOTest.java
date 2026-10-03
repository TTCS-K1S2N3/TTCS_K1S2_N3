package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.TruongTuyChinh;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử TruongTuyChinhDAO với CSDL H2 in-memory (Story S2-08)")
class TruongTuyChinhDAOTest {

    private Connection connection;
    private TruongTuyChinhDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_ttc;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_ttc;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            // Bảng truong_tuy_chinh
            st.execute("CREATE TABLE truong_tuy_chinh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "loai_doi_tuong VARCHAR(30) NOT NULL, " +
                    "ma_truong VARCHAR(80) NOT NULL, " +
                    "ten_truong VARCHAR(150) NOT NULL, " +
                    "kieu_du_lieu VARCHAR(30) NOT NULL, " +
                    "bat_buoc TINYINT(1) NOT NULL DEFAULT 0, " +
                    "lua_chon_json TEXT NULL, " +
                    "gia_tri_mac_dinh_json TEXT NULL, " +
                    "thu_tu_hien_thi INT NOT NULL DEFAULT 0, " +
                    "hoat_dong TINYINT(1) NOT NULL DEFAULT 1, " +
                    "created_by BIGINT NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (loai_doi_tuong, ma_truong)" +
                    ")");

            // Bảng gia_tri_truong_tuy_chinh_khach_hang
            st.execute("CREATE TABLE gia_tri_truong_tuy_chinh_khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "truong_tuy_chinh_id BIGINT NOT NULL, " +
                    "gia_tri_van_ban TEXT NULL, " +
                    "gia_tri_so DECIMAL(30,6) NULL, " +
                    "gia_tri_ngay DATE NULL, " +
                    "gia_tri_json TEXT NULL, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (khach_hang_id, truong_tuy_chinh_id)" +
                    ")");

            // Bảng gia_tri_truong_tuy_chinh_co_hoi
            st.execute("CREATE TABLE gia_tri_truong_tuy_chinh_co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "co_hoi_id BIGINT NOT NULL, " +
                    "truong_tuy_chinh_id BIGINT NOT NULL, " +
                    "gia_tri_van_ban TEXT NULL, " +
                    "gia_tri_so DECIMAL(30,6) NULL, " +
                    "gia_tri_ngay DATE NULL, " +
                    "gia_tri_json TEXT NULL, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE (co_hoi_id, truong_tuy_chinh_id)" +
                    ")");
        }

        dao = new TruongTuyChinhDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @Test
    @DisplayName("DAO: Thêm các loại trường TEXT, NUMBER, DATE, SELECT thành công")
    void testThemCacKieuTruong() {
        // 1. TEXT
        TruongTuyChinh tText = new TruongTuyChinh("KHACH_HANG", "ghi_chu_dac_biet", "Ghi chú đặc biệt", "VAN_BAN", false);
        tText.setCreatedBy(1L);
        long idText = dao.them(tText);
        assertTrue(idText > 0);

        // 2. NUMBER
        TruongTuyChinh tNum = new TruongTuyChinh("KHACH_HANG", "so_chi_nhanh", "Số chi nhánh", "SO", false);
        tNum.setCreatedBy(1L);
        long idNum = dao.them(tNum);
        assertTrue(idNum > 0);

        // 3. DATE
        TruongTuyChinh tDate = new TruongTuyChinh("KHACH_HANG", "ngay_thanh_lap", "Ngày thành lập", "NGAY", false);
        tDate.setCreatedBy(1L);
        long idDate = dao.them(tDate);
        assertTrue(idDate > 0);

        // 4. SELECT
        TruongTuyChinh tSelect = new TruongTuyChinh("KHACH_HANG", "phan_khuc", "Phân khúc", "DANH_SACH_CHON", true);
        tSelect.setDanhSachLuaChon(List.of("VIP", "Doanh nghiệp", "Bán lẻ"));
        tSelect.setCreatedBy(1L);
        long idSelect = dao.them(tSelect);
        assertTrue(idSelect > 0);

        // Kiểm tra lấy danh sách
        List<TruongTuyChinh> list = dao.layDanhSachTheoDoiTuong("KHACH_HANG", false);
        assertEquals(4, list.size());
    }

    @Test
    @DisplayName("DAO: Chặn trùng ma_truong trong cùng đối tượng, nhưng cho phép trùng ma_truong khác đối tượng")
    void testUniqueMaTruongTheoDoiTuong() {
        TruongTuyChinh t1 = new TruongTuyChinh("KHACH_HANG", "nguon_goc", "Nguồn gốc KH", "VAN_BAN", false);
        long id1 = dao.them(t1);
        assertTrue(id1 > 0);

        // Cùng KHACH_HANG, cùng ma_truong -> kiemTraTonTaiMa trả về true
        assertTrue(dao.kiemTraTonTaiMa("KHACH_HANG", "nguon_goc", null));

        // Khác đối tượng (CO_HOI), cùng ma_truong -> kiemTraTonTaiMa trả về false
        assertFalse(dao.kiemTraTonTaiMa("CO_HOI", "nguon_goc", null));

        // Thêm trường cùng ma_truong sang CO_HOI -> thành công
        TruongTuyChinh t2 = new TruongTuyChinh("CO_HOI", "nguon_goc", "Nguồn gốc Cơ hội", "VAN_BAN", false);
        long id2 = dao.them(t2);
        assertTrue(id2 > 0);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("DAO: Lưu và đọc typed column đúng chuẩn (TEXT, NUMBER, DATE, SELECT)")
    void testLuuVaDocTypedColumns() throws Exception {
        // Tạo các trường cho KHACH_HANG
        TruongTuyChinh tText = new TruongTuyChinh("KHACH_HANG", "hang_khach", "Hạng khách", "VAN_BAN", false);
        long idText = dao.them(tText);

        TruongTuyChinh tSo = new TruongTuyChinh("KHACH_HANG", "doanh_thu", "Doanh thu", "SO", false);
        long idSo = dao.them(tSo);

        TruongTuyChinh tNgay = new TruongTuyChinh("KHACH_HANG", "ngay_ky", "Ngày ký", "NGAY", false);
        long idNgay = dao.them(tNgay);

        TruongTuyChinh tSelect = new TruongTuyChinh("KHACH_HANG", "muc_do", "Mức độ", "DANH_SACH_CHON", false);
        long idSelect = dao.them(tSelect);

        long khachHangId = 888L;

        // Lưu giá trị
        assertTrue(dao.luuGiaTri("KHACH_HANG", khachHangId, idText, "VAN_BAN", "Khách VIP miền Bắc"));
        assertTrue(dao.luuGiaTri("KHACH_HANG", khachHangId, idSo, "SO", "12345.67"));
        assertTrue(dao.luuGiaTri("KHACH_HANG", khachHangId, idNgay, "NGAY", "2026-10-03"));
        assertTrue(dao.luuGiaTri("KHACH_HANG", khachHangId, idSelect, "DANH_SACH_CHON", "Ưu tiên cao"));

        // Kiểm tra vật lý trong database: đảm bảo lưu đúng cột
        try (Statement st = connection.createStatement()) {
            // Text -> gia_tri_van_ban
            try (ResultSet rs = st.executeQuery("SELECT gia_tri_van_ban, gia_tri_so, gia_tri_ngay FROM gia_tri_truong_tuy_chinh_khach_hang WHERE truong_tuy_chinh_id = " + idText)) {
                assertTrue(rs.next());
                assertEquals("Khách VIP miền Bắc", rs.getString("gia_tri_van_ban"));
                assertNull(rs.getBigDecimal("gia_tri_so"));
                assertNull(rs.getDate("gia_tri_ngay"));
            }

            // So -> gia_tri_so
            try (ResultSet rs = st.executeQuery("SELECT gia_tri_van_ban, gia_tri_so, gia_tri_ngay FROM gia_tri_truong_tuy_chinh_khach_hang WHERE truong_tuy_chinh_id = " + idSo)) {
                assertTrue(rs.next());
                assertNull(rs.getString("gia_tri_van_ban"));
                assertEquals(new BigDecimal("12345.670000"), rs.getBigDecimal("gia_tri_so"));
                assertNull(rs.getDate("gia_tri_ngay"));
            }

            // Ngay -> gia_tri_ngay
            try (ResultSet rs = st.executeQuery("SELECT gia_tri_van_ban, gia_tri_so, gia_tri_ngay FROM gia_tri_truong_tuy_chinh_khach_hang WHERE truong_tuy_chinh_id = " + idNgay)) {
                assertTrue(rs.next());
                assertNull(rs.getString("gia_tri_van_ban"));
                assertNull(rs.getBigDecimal("gia_tri_so"));
                assertEquals("2026-10-03", rs.getDate("gia_tri_ngay").toString());
            }

            // Select -> gia_tri_van_ban
            try (ResultSet rs = st.executeQuery("SELECT gia_tri_van_ban, gia_tri_json FROM gia_tri_truong_tuy_chinh_khach_hang WHERE truong_tuy_chinh_id = " + idSelect)) {
                assertTrue(rs.next());
                assertEquals("Ưu tiên cao", rs.getString("gia_tri_van_ban"));
                assertNotNull(rs.getString("gia_tri_json"));
            }
        }

        // Đọc lại qua DAO
        Map<String, String> values = dao.layGiaTriTheoDoiTuong("KHACH_HANG", khachHangId);
        assertEquals("Khách VIP miền Bắc", values.get("hang_khach"));
        assertEquals("12345.67", values.get("doanh_thu"));
        assertEquals("2026-10-03", values.get("ngay_ky"));
        assertEquals("Ưu tiên cao", values.get("muc_do"));
    }

    @Test
    @DisplayName("DAO: UPSERT cập nhật giá trị mà không sinh duplicate record")
    void testUpsertKhongSinhDuplicate() throws Exception {
        TruongTuyChinh t = new TruongTuyChinh("CO_HOI", "ghi_chu_deal", "Ghi chú deal", "VAN_BAN", false);
        long id = dao.them(t);
        long coHoiId = 999L;

        // Lưu lần 1
        assertTrue(dao.luuGiaTri("CO_HOI", coHoiId, id, "VAN_BAN", "Giá trị ban đầu"));
        Map<String, String> vals1 = dao.layGiaTriTheoDoiTuong("CO_HOI", coHoiId);
        assertEquals("Giá trị ban đầu", vals1.get("ghi_chu_deal"));

        // Lưu lần 2 (cùng coHoiId và truongId) -> UPSERT
        assertTrue(dao.luuGiaTri("CO_HOI", coHoiId, id, "VAN_BAN", "Giá trị cập nhật"));
        Map<String, String> vals2 = dao.layGiaTriTheoDoiTuong("CO_HOI", coHoiId);
        assertEquals("Giá trị cập nhật", vals2.get("ghi_chu_deal"));

        // Kiểm tra số dòng thực tế trong DB
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total FROM gia_tri_truong_tuy_chinh_co_hoi WHERE co_hoi_id = " + coHoiId)) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("total"), "Chỉ được có đúng 1 bản ghi duy nhất, không nhân đôi");
        }
    }

    @Test
    @DisplayName("DAO: Ngừng áp dụng trường tuỳ chỉnh (hoat_dong = 0) không xoá giá trị lịch sử")
    void testNgungApDungBaoToanGiaTriLichSu() {
        TruongTuyChinh t = new TruongTuyChinh("KHACH_HANG", "ma_cu", "Mã cũ", "VAN_BAN", false);
        long id = dao.them(t);
        long khachHangId = 777L;

        dao.luuGiaTri("KHACH_HANG", khachHangId, id, "VAN_BAN", "OLD-CODE-123");

        // Ngừng áp dụng trường
        boolean ok = dao.doiTrangThai(id, false);
        assertTrue(ok);

        TruongTuyChinh reloaded = dao.layTheoId(id);
        assertFalse(reloaded.isDangHoatDong());

        // Giá trị lịch sử trong DB vẫn còn nguyên
        Map<String, String> values = dao.layGiaTriTheoDoiTuong("KHACH_HANG", khachHangId);
        assertEquals("OLD-CODE-123", values.get("ma_cu"));
    }

    @Test
    @DisplayName("DAO: Reject định dạng số hoặc ngày không hợp lệ")
    void testRejectKieuDuLieuSaiDinhDang() {
        TruongTuyChinh tSo = new TruongTuyChinh("KHACH_HANG", "so_tien", "Số tiền", "SO", false);
        long idSo = dao.them(tSo);

        TruongTuyChinh tNgay = new TruongTuyChinh("KHACH_HANG", "ngay_sinh", "Ngày sinh", "NGAY", false);
        long idNgay = dao.them(tNgay);

        // Số không hợp lệ: "abc"
        assertThrows(IllegalArgumentException.class, () ->
                dao.luuGiaTri("KHACH_HANG", 1L, idSo, "SO", "abc")
        );

        // Ngày không hợp lệ: "2026-99-99"
        assertThrows(IllegalArgumentException.class, () ->
                dao.luuGiaTri("KHACH_HANG", 1L, idNgay, "NGAY", "2026-99-99")
        );
    }

    @Test
    @DisplayName("DAO: Whitelist table name chặn entity không hợp lệ")
    void testWhitelistTableNameChặnEntityKhongHopLe() {
        assertThrows(IllegalArgumentException.class, () ->
                dao.layGiaTriTheoDoiTuong("SAN_PHAM", 1L)
        );
        assertThrows(IllegalArgumentException.class, () ->
                dao.luuGiaTri("USERS", 1L, 1L, "VAN_BAN", "Test")
        );
    }
}
