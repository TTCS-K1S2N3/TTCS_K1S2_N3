package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.LoaiDanhMuc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử DanhMucBanHangDAO - Reference checks & Fail-closed (Story S2-07)")
class DanhMucBanHangDAOTest {

    private static Connection rootConnection;
    private static DanhMucBanHangDAO dao;

    @BeforeAll
    static void setUp() throws Exception {
        rootConnection = DriverManager.getConnection("jdbc:h2:mem:crm_danhmuc_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_danhmuc_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                return null;
            }
        });

        dao = new DanhMucBanHangDAO();

        try (Statement st = rootConnection.createStatement()) {
            // 4 bảng danh mục
            st.execute("CREATE TABLE IF NOT EXISTS nganh_nghe (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nganh VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nganh VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS quy_mo_doanh_nghiep (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_quy_mo VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_quy_mo VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguon_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nguon VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nguon VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS loai_hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_loai VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_loai VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong BOOLEAN DEFAULT TRUE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Các bảng nghiệp vụ tham chiếu (schema chuẩn)
            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT)");

            // Bảng `lead` là từ khóa MySQL cần backtick
            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguon_lead_id BIGINT, " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguon_lead_id BIGINT)");

            // Canonical schema: hoat_dong có cột loai_hoat_dong VARCHAR(50) lưu ma_loai, không phải loai_hoat_dong_id
            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "loai_hoat_dong VARCHAR(50) NOT NULL)");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @Test
    @DisplayName("Test 1: NGUON_LEAD không tham chiếu -> count = 0, xóa được")
    void test1_NguonLead_KhongThamChieu_CountBangKhong_XoaDuoc() {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.NGUON_LEAD);
        dto.setMaMuc("FACEBOOK_TEST");
        dto.setTenMuc("Facebook Test Campaign");
        dto.setThuTuHienThi(1);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.NGUON_LEAD, dto);
        assertTrue(id > 0);

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.NGUON_LEAD, id);
        assertEquals(0, count);

        boolean xoaOk = dao.xoa(LoaiDanhMuc.NGUON_LEAD, id);
        assertTrue(xoaOk);
    }

    @Test
    @DisplayName("Test 2: NGUON_LEAD có reference trong `lead` -> count > 0")
    void test2_NguonLead_CoThamChieuTrongLead() throws SQLException {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.NGUON_LEAD);
        dto.setMaMuc("TIKTOK_ADS");
        dto.setTenMuc("TikTok Ads");
        dto.setThuTuHienThi(2);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.NGUON_LEAD, dto);
        assertTrue(id > 0);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (nguon_lead_id) VALUES (" + id + ")");
        }

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.NGUON_LEAD, id);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test 3: NGUON_LEAD có reference trong co_hoi -> count > 0")
    void test3_NguonLead_CoThamChieuTrongCoHoi() throws SQLException {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.NGUON_LEAD);
        dto.setMaMuc("GOOGLE_ADS");
        dto.setTenMuc("Google Ads");
        dto.setThuTuHienThi(3);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.NGUON_LEAD, dto);
        assertTrue(id > 0);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO co_hoi (nguon_lead_id) VALUES (" + id + ")");
        }

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.NGUON_LEAD, id);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test 4: NGANH_NGHE reference trong `lead` -> query hoạt động, không syntax error")
    void test4_NganhNghe_ReferenceTrongLead_KhongSyntaxError() throws SQLException {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.NGANH_NGHE);
        dto.setMaMuc("CNTT");
        dto.setTenMuc("Công nghệ thông tin");
        dto.setThuTuHienThi(1);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.NGANH_NGHE, dto);
        assertTrue(id > 0);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (nganh_nghe_id) VALUES (" + id + ")");
        }

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.NGANH_NGHE, id);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test 5: QUY_MO reference trong `lead` -> query hoạt động, không syntax error")
    void test5_QuyMo_ReferenceTrongLead_KhongSyntaxError() throws SQLException {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.QUY_MO);
        dto.setMaMuc("QM_DOANH_NGHIEP_NHO");
        dto.setTenMuc("Doanh nghiệp nhỏ (10-50)");
        dto.setThuTuHienThi(1);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.QUY_MO, dto);
        assertTrue(id > 0);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (quy_mo_id) VALUES (" + id + ")");
        }

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.QUY_MO, id);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test 6: LOAI_HOAT_DONG ma_loai = CUOC_GOI, hoat_dong.loai_hoat_dong = CUOC_GOI -> count > 0")
    void test6_LoaiHoatDong_KhopTheoMaLoai_CountLonHonKhong() throws SQLException {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.LOAI_HOAT_DONG);
        dto.setMaMuc("CUOC_GOI");
        dto.setTenMuc("Cuộc gọi điện thoại");
        dto.setThuTuHienThi(1);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.LOAI_HOAT_DONG, dto);
        assertTrue(id > 0);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO hoat_dong (loai_hoat_dong) VALUES ('CUOC_GOI')");
        }

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.LOAI_HOAT_DONG, id);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test 7: LOAI_HOAT_DONG chưa dùng -> count = 0, xóa được")
    void test7_LoaiHoatDong_ChuaDung_XoaDuoc() {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.LOAI_HOAT_DONG);
        dto.setMaMuc("ZALO_ZNS");
        dto.setTenMuc("Gửi thông báo Zalo ZNS");
        dto.setThuTuHienThi(2);
        dto.setKichHoat(true);

        long id = dao.them(LoaiDanhMuc.LOAI_HOAT_DONG, dto);
        assertTrue(id > 0);

        int count = dao.demSoLuongThamChieu(LoaiDanhMuc.LOAI_HOAT_DONG, id);
        assertEquals(0, count);

        boolean xoaOk = dao.xoa(LoaiDanhMuc.LOAI_HOAT_DONG, id);
        assertTrue(xoaOk);
    }

    @Test
    @DisplayName("Test 8: SQL exception -> fail-closed, trả về -1")
    void test8_LoiTruyVan_FailClosed_TraVeAmMot() throws SQLException {
        // Kiểm tra với loai null trả về -1
        int countNull = dao.demSoLuongThamChieu(null, 1L);
        assertEquals(-1, countNull);

        // Giả lập lỗi kết nối đóng
        Connection closedConn = DriverManager.getConnection("jdbc:h2:mem:crm_danhmuc_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        closedConn.close();

        try {
            DatabaseConfig.setConnectionSupplier(() -> closedConn);
            int countLoi = dao.demSoLuongThamChieu(LoaiDanhMuc.NGUON_LEAD, 999L);
            assertEquals(-1, countLoi);
        } finally {
            DatabaseConfig.setConnectionSupplier(() -> {
                try {
                    return DriverManager.getConnection("jdbc:h2:mem:crm_danhmuc_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                } catch (SQLException e) {
                    return null;
                }
            });
        }
    }
}
