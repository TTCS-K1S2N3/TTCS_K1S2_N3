package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.ChienDichThongKeDTO;
import vn.nhom10.crm.model.ChienDich;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử ChienDichDAO - Story S4-03 (AC1, AC2, AC3)")
class ChienDichDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_03_chien_dich_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private ChienDichDAO dao;

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
            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE)");

            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_chien_dich VARCHAR(200) NOT NULL, " +
                    "ngan_sach DECIMAL(18,2) NULL, " +
                    "kenh VARCHAR(100) NULL, " +
                    "ngay_bat_dau DATE NULL, " +
                    "ngay_ket_thuc DATE NULL, " +
                    "trang_thai VARCHAR(30) NOT NULL DEFAULT 'NHAP', " +
                    "mo_ta TEXT NULL, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_lead VARCHAR(50) NOT NULL UNIQUE, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "lead_id BIGINT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "gia_tri_du_kien DECIMAL(18,2) NOT NULL DEFAULT 0.00, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'MO', " +
                    "gia_tri_chot_thuc_te DECIMAL(18,2) NULL, " +
                    "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        dao = new ChienDichDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM co_hoi");
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM chien_dich");
            st.execute("DELETE FROM nguoi_dung");
        }
    }

    @Test
    @DisplayName("AC1: Thêm mới chiến dịch hợp lệ và tìm lại theo ID")
    void testThemVaTimTheoId() {
        ChienDich cd = new ChienDich();
        cd.setMaChienDich("CD-TEST-001");
        cd.setTenChienDich("Chiến dịch Q4 ra mắt tính năng");
        cd.setNganSach(new BigDecimal("15000000.00"));
        cd.setKenh("EVENT");
        cd.setNgayBatDau(LocalDate.of(2026, 10, 1));
        cd.setNgayKetThuc(LocalDate.of(2026, 10, 31));
        cd.setTrangThai("DANG_CHAY");
        cd.setMoTa("Mô tả chiến dịch test");

        Long id = dao.them(cd);
        assertNotNull(id);
        assertTrue(id > 0);

        ChienDich loaded = dao.timTheoId(id);
        assertNotNull(loaded);
        assertEquals("CD-TEST-001", loaded.getMaChienDich());
        assertEquals("Chiến dịch Q4 ra mắt tính năng", loaded.getTenChienDich());
        assertEquals(0, new BigDecimal("15000000.00").compareTo(loaded.getNganSach()));
        assertEquals("EVENT", loaded.getKenh());
        assertEquals(LocalDate.of(2026, 10, 1), loaded.getNgayBatDau());
        assertEquals(LocalDate.of(2026, 10, 31), loaded.getNgayKetThuc());
        assertEquals("DANG_CHAY", loaded.getTrangThai());
    }

    @Test
    @DisplayName("AC1: Kiểm tra tồn tại mã chiến dịch")
    void testKiemTraTonTaiMa() {
        ChienDich cd = new ChienDich();
        cd.setMaChienDich("CD-UNIQUE-01");
        cd.setTenChienDich("Chiến dịch Unique");
        dao.them(cd);

        assertTrue(dao.kiemTraTonTaiMa("CD-UNIQUE-01"));
        assertFalse(dao.kiemTraTonTaiMa("CD-NOT-EXIST"));
        assertFalse(dao.kiemTraTonTaiMa(""));
        assertFalse(dao.kiemTraTonTaiMa(null));
    }

    @Test
    @DisplayName("AC1: Lấy danh sách phân trang và đếm tổng số")
    void testLayDanhSachPhanTrang() {
        for (int i = 1; i <= 5; i++) {
            ChienDich cd = new ChienDich();
            cd.setMaChienDich("CD-PAGE-" + i);
            cd.setTenChienDich("Chiến dịch trang " + i);
            cd.setNganSach(new BigDecimal(1000000 * i));
            cd.setKenh("SOCIAL");
            dao.them(cd);
        }

        assertEquals(5, dao.demTongSo());

        List<ChienDich> trang1 = dao.layDanhSachPhanTrang(0, 3);
        assertEquals(3, trang1.size());

        List<ChienDich> trang2 = dao.layDanhSachPhanTrang(3, 3);
        assertEquals(2, trang2.size());
    }

    @Test
    @DisplayName("AC3: Thống kê KPI đúng MOCK 01 / CT-08 (3 lead, 2 cơ hội: 1 WON 45M + 1 OPEN 30M)")
    void testThongKeKpiTheoContractMock01() throws Exception {
        // 1. Tạo chiến dịch 101
        ChienDich cd101 = new ChienDich();
        cd101.setMaChienDich("CD-MOCK-101");
        cd101.setTenChienDich("Sự kiện sản phẩm tháng 10");
        cd101.setNganSach(new BigDecimal("12000000"));
        cd101.setKenh("EVENT");
        Long cid101 = dao.them(cd101);

        // 2. Tạo 3 lead gắn chiến dịch 101
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, chien_dich_id) VALUES " +
                    "(201, 'LEAD-201', 'Lead giả 01', " + cid101 + "), " +
                    "(202, 'LEAD-202', 'Lead giả 02', " + cid101 + "), " +
                    "(203, 'LEAD-203', 'Lead giả 03', " + cid101 + ")");

            // 3. Tạo 2 cơ hội:
            // 301: Thắng (DONG_THANG), giá trị chốt thực tế 45.000.000
            // 302: Đang mở (MO), giá trị dự kiến 30.000.000, giá trị chốt null
            st.execute("INSERT INTO co_hoi (id, ma_co_hoi, ten_co_hoi, lead_id, chien_dich_id, gia_tri_du_kien, trang_thai, gia_tri_chot_thuc_te) VALUES " +
                    "(301, 'CH-301', 'Cơ hội chốt 01', 201, " + cid101 + ", 60000000, 'DONG_THANG', 45000000), " +
                    "(302, 'CH-302', 'Cơ hội mở 02', 203, " + cid101 + ", 30000000, 'MO', NULL)");
        }

        // Lấy thống kê
        ChienDichThongKeDTO kpi = dao.layThongKeChiTieu(cid101);
        assertNotNull(kpi);
        assertEquals(cid101, kpi.getCampaignId());
        assertEquals(3, kpi.getLeadCount(), "Số lead phải là 3");
        assertEquals(2, kpi.getOpportunityCount(), "Số cơ hội phải là 2");
        assertEquals(0, new BigDecimal("45000000").compareTo(kpi.getClosedWonAmount()),
                "Giá trị đã chốt phải là 45.000.000 VND (không cộng 30.000.000 của cơ hội Đang mở)");
    }

    @Test
    @DisplayName("AC3: Thống kê chiến dịch rỗng (chưa có lead/cơ hội) trả về 0/0/0 (MOCK 02 / CT-09)")
    void testThongKeChienDichRong() {
        ChienDich cd102 = new ChienDich();
        cd102.setMaChienDich("CD-MOCK-102");
        cd102.setTenChienDich("Giới thiệu trực tuyến");
        cd102.setNganSach(new BigDecimal("3500000"));
        cd102.setKenh("SOCIAL");
        Long cid102 = dao.them(cd102);

        ChienDichThongKeDTO kpi = dao.layThongKeChiTieu(cid102);
        assertNotNull(kpi);
        assertEquals(0, kpi.getLeadCount());
        assertEquals(0, kpi.getOpportunityCount());
        assertEquals(BigDecimal.ZERO, kpi.getClosedWonAmount());
    }

    @Test
    @DisplayName("AC3: Cơ hội đóng THUA (DONG_THUA) không được cộng vào giá trị đã chốt")
    void testCoHoiThuaKhongCongGiaTriChot() throws Exception {
        ChienDich cd = new ChienDich();
        cd.setMaChienDich("CD-LOST-TEST");
        cd.setTenChienDich("Chiến dịch có cơ hội thua");
        Long cid = dao.them(cd);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO co_hoi (id, ma_co_hoi, ten_co_hoi, chien_dich_id, trang_thai, gia_tri_chot_thuc_te) VALUES " +
                    "(401, 'CH-401', 'Cơ hội Thắng', " + cid + ", 'DONG_THANG', 20000000), " +
                    "(402, 'CH-402', 'Cơ hội Thua', " + cid + ", 'DONG_THUA', 99000000)");
        }

        ChienDichThongKeDTO kpi = dao.layThongKeChiTieu(cid);
        assertEquals(2, kpi.getOpportunityCount());
        assertEquals(0, new BigDecimal("20000000").compareTo(kpi.getClosedWonAmount()),
                "Giá trị đã chốt chỉ tính cơ hội Thắng, không tính cơ hội Thua");
    }

    @Test
    @DisplayName("AC3: Thống kê batch cho danh sách chiến dịch layThongKeChoDanhSach")
    void testThongKeChoDanhSach() throws Exception {
        ChienDich cd1 = new ChienDich();
        cd1.setMaChienDich("CD-B1");
        cd1.setTenChienDich("Chiến dịch 1");
        Long id1 = dao.them(cd1);

        ChienDich cd2 = new ChienDich();
        cd2.setMaChienDich("CD-B2");
        cd2.setTenChienDich("Chiến dịch 2");
        Long id2 = dao.them(cd2);

        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, chien_dich_id) VALUES " +
                    "(501, 'L-501', 'Lead 1', " + id1 + ")");
            st.execute("INSERT INTO co_hoi (id, ma_co_hoi, ten_co_hoi, chien_dich_id, trang_thai, gia_tri_chot_thuc_te) VALUES " +
                    "(601, 'CH-601', 'Cơ hội 1', " + id1 + ", 'DONG_THANG', 10000000)");
        }

        Map<Long, ChienDichThongKeDTO> map = dao.layThongKeChoDanhSach(List.of(id1, id2));
        assertEquals(2, map.size());

        ChienDichThongKeDTO kpi1 = map.get(id1);
        assertEquals(1, kpi1.getLeadCount());
        assertEquals(1, kpi1.getOpportunityCount());
        assertEquals(0, new BigDecimal("10000000").compareTo(kpi1.getClosedWonAmount()));

        ChienDichThongKeDTO kpi2 = map.get(id2);
        assertEquals(0, kpi2.getLeadCount());
        assertEquals(0, kpi2.getOpportunityCount());
        assertEquals(BigDecimal.ZERO, kpi2.getClosedWonAmount());
    }
}
