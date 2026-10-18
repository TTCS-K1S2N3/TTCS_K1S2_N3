package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.BoLocLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tìm kiếm nhanh Lead bằng từ khóa - Story S4-09")
class LeadSearchKeywordDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_09_lead_search_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private LeadDAO leadDAO;
    private NguoiDung adminUser;
    private NguoiDung salesUser1;
    private NguoiDung salesUser2;

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
            st.execute("CREATE TABLE IF NOT EXISTS nguon_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nguon VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nguon VARCHAR(150) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_chien_dich VARCHAR(200) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_bieu_mau VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_bieu_mau VARCHAR(200) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "nhom_kinh_doanh_id BIGINT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS `lead` (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_lead VARCHAR(50) NOT NULL UNIQUE, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NULL, " +
                    "email_chuan_hoa VARCHAR(255) NULL, " +
                    "so_dien_thoai VARCHAR(20) NULL, " +
                    "so_dien_thoai_chuan_hoa VARCHAR(20) NULL, " +
                    "ten_cong_ty VARCHAR(255) NULL, " +
                    "ten_cong_ty_chuan_hoa VARCHAR(255) NULL, " +
                    "nhu_cau_quan_tam TEXT NULL, " +
                    "muc_do_quan_tam VARCHAR(50) NULL, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "chien_dich_id BIGINT NULL, " +
                    "bieu_mau_id BIGINT NULL, " +
                    "diem INT DEFAULT 0, " +
                    "phan_loai VARCHAR(30) NULL, " +
                    "trang_thai VARCHAR(40) DEFAULT 'MOI', " +
                    "nguoi_phu_trach_id BIGINT NULL, " +
                    "nhom_phan_bo_id BIGINT NULL, " +
                    "duoc_phan_bo_luc DATETIME NULL, " +
                    "han_phan_hoi_sla DATETIME NULL, " +
                    "duoc_nhan_luc DATETIME NULL, " +
                    "lien_he_dau_luc DATETIME NULL, " +
                    "ly_do_tu_choi VARCHAR(500) NULL, " +
                    "qua_sla TINYINT(1) DEFAULT 0, " +
                    "khach_hang_chuyen_doi_id BIGINT NULL, " +
                    "nguoi_lien_he_chuyen_doi_id BIGINT NULL, " +
                    "co_hoi_chuyen_doi_id BIGINT NULL, " +
                    "chuyen_doi_luc DATETIME NULL, " +
                    "gop_vao_lead_id BIGINT NULL, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NULL)");

            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'WEB', 'Website')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Admin System', 'admin@crm.vn')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (2, 'Sales Rep 1', 'sales1@crm.vn')");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (3, 'Sales Rep 2', 'sales2@crm.vn')");
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @BeforeEach
    void setUp() throws Exception {
        leadDAO = new LeadDAO();

        adminUser = new NguoiDung(1L, "Admin System", "admin@crm.vn");
        salesUser1 = new NguoiDung(2L, "Sales Rep 1", "sales1@crm.vn");
        salesUser2 = new NguoiDung(3L, "Sales Rep 2", "sales2@crm.vn");

        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM `lead`");

            // Nạp mẫu dữ liệu kiểm thử
            // 1. Lead có họ tên TEST S4-04 (Lead #40)
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, nguon_lead_id, trang_thai, nguoi_phu_trach_id) " +
                    "VALUES (40, 'LEAD-40', 'TEST S4-04 Nguyễn Văn A', 's404_a@example.com', 's404_a@example.com', '0901000040', '0901000040', 'Công ty ABC Alpha', 1, 'MOI', 2)");

            // 2. Lead có tên công ty TEST S4-04 (Lead #41)
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, nguon_lead_id, trang_thai, nguoi_phu_trach_id) " +
                    "VALUES (41, 'LEAD-41', 'Trần Thị B', 's404_b@example.com', 's404_b@example.com', '0901000041', '0901000041', 'Tập đoàn TEST S4-04 Beta', 1, 'DANG_LIEN_HE', 2)");

            // 3. Lead TEST S4-08 (Lead #49)
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, nguon_lead_id, trang_thai, nguoi_phu_trach_id) " +
                    "VALUES (49, 'LEAD-49-S408', 'Lê Văn TEST S4-08', 's408@example.com', 's408@example.com', '0908888049', '0908888049', 'Doanh nghiệp S408 Tech', 1, 'MOI', 3)");

            // 4. Lead có ký tự đặc biệt
            try (var ps = rootConnection.prepareStatement("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, nguon_lead_id, trang_thai, nguoi_phu_trach_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setLong(1, 50L);
                ps.setString(2, "LEAD-50-SPECIAL");
                ps.setString(3, "Hoàng 100% Khách Hàng");
                ps.setString(4, "special_100@example.com");
                ps.setString(5, "special_100@example.com");
                ps.setString(6, "0912345678");
                ps.setString(7, "0912345678");
                ps.setString(8, "Cty TNHH O'Reilly & Co");
                ps.setLong(9, 1L);
                ps.setString(10, "MOI");
                ps.setLong(11, 2L);
                ps.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("Kiểm thử bắt buộc: Tìm 'TEST S4-04' phải tìm được đúng Lead #40 và #41")
    void testTimKiemTestS404() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("TEST S4-04");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(2, ketQua.size(), "Phải tìm thấy 2 Lead chứa từ khóa TEST S4-04");

        List<Long> ids = ketQua.stream().map(Lead::getId).toList();
        assertTrue(ids.contains(40L), "Phải chứa Lead #40");
        assertTrue(ids.contains(41L), "Phải chứa Lead #41");

        int total = leadDAO.demTongSoLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO);
        assertEquals(2, total);
    }

    @Test
    @DisplayName("Kiểm thử bắt buộc: Tìm 'TEST S4-08' phải tìm được đúng Lead #49")
    void testTimKiemTestS408() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("TEST S4-08");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(1, ketQua.size(), "Phải tìm thấy đúng 1 Lead cho từ khóa TEST S4-08");
        assertEquals(49L, ketQua.get(0).getId());

        int total = leadDAO.demTongSoLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO);
        assertEquals(1, total);
    }

    @Test
    @DisplayName("Kiểm thử tìm kiếm không phân biệt chữ hoa / thường (Case Insensitive)")
    void testTimKiemCaseInsensitive() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("test s4-04");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(2, ketQua.size(), "Tìm chữ thường vẫn phải ra kết quả Lead #40 và #41");
    }

    @Test
    @DisplayName("Kiểm thử tìm kiếm theo Email")
    void testTimKiemTheoEmail() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("s408@example.com");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(1, ketQua.size());
        assertEquals(49L, ketQua.get(0).getId());

        // Tìm một phần email
        boLoc.setTuKhoa("s404_a");
        ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertEquals(1, ketQua.size());
        assertEquals(40L, ketQua.get(0).getId());
    }

    @Test
    @DisplayName("Kiểm thử tìm kiếm theo Mã Lead")
    void testTimKiemTheoMaLead() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("LEAD-49-S408");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(1, ketQua.size());
        assertEquals(49L, ketQua.get(0).getId());

        // Tìm một phần mã
        boLoc.setTuKhoa("LEAD-49");
        ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertEquals(1, ketQua.size());
        assertEquals(49L, ketQua.get(0).getId());
    }

    @Test
    @DisplayName("Kiểm thử tìm kiếm theo Số điện thoại")
    void testTimKiemTheoSoDienThoai() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("0908888049");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(1, ketQua.size());
        assertEquals(49L, ketQua.get(0).getId());

        // Tìm 4 số đuôi điện thoại
        boLoc.setTuKhoa("0040");
        ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertEquals(1, ketQua.size());
        assertEquals(40L, ketQua.get(0).getId());
    }

    @Test
    @DisplayName("Kiểm thử từ khóa không tồn tại phải trả danh sách rỗng")
    void testTuKhoaKhongTonTai() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("KHONG_TON_TAI_TRONG_DB_XYZ_9999");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertTrue(ketQua.isEmpty(), "Từ khóa không tồn tại phải trả về danh sách rỗng");

        int total = leadDAO.demTongSoLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO);
        assertEquals(0, total);
    }

    @Test
    @DisplayName("Kiểm thử an toàn SQL với các ký tự đặc biệt %, _, dấu nháy đơn và nháy kép")
    void testKyTuDacBietChongSqlInjection() {
        BoLocLeadDTO boLoc = new BoLocLeadDTO();

        // 1. Dấu %
        boLoc.setTuKhoa("100%");
        assertDoesNotThrow(() -> {
            List<Lead> kq = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
            assertNotNull(kq);
            assertTrue(kq.stream().anyMatch(l -> l.getId() == 50L));
        });

        // 2. Dấu nháy đơn (')
        boLoc.setTuKhoa("O'Reilly");
        assertDoesNotThrow(() -> {
            List<Lead> kq = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
            assertNotNull(kq);
            assertTrue(kq.stream().anyMatch(l -> l.getId() == 50L));
        });

        // 3. Chuỗi tấn công SQL Injection
        boLoc.setTuKhoa("' OR '1'='1");
        assertDoesNotThrow(() -> {
            List<Lead> kq = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
            assertNotNull(kq);
            assertTrue(kq.isEmpty(), "SQL Injection phải bị xử lý an toàn như một chuỗi bình thường và không trả về toàn bộ DB");
        });
    }

    @Test
    @DisplayName("Kiểm thử kết hợp từ khóa và trạng thái (AND condition)")
    void testKetHopTuKhoaVaTrangThai() {
        // Cả #40 và #41 đều chứa TEST S4-04, nhưng #40 có trạng thái MOI, còn #41 có trạng thái DANG_LIEN_HE
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("TEST S4-04");
        boLoc.setTrangThai("MOI");

        List<Lead> ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(1, ketQua.size(), "Chỉ có 1 Lead thỏa mãn cả từ khóa và trạng thái MOI");
        assertEquals(40L, ketQua.get(0).getId());

        // Lọc trạng thái DANG_LIEN_HE
        boLoc.setTrangThai("DANG_LIEN_HE");
        ketQua = leadDAO.layDanhSachLead(boLoc, adminUser, PhamViDuLieu.TOAN_BO, 50, 0);
        assertNotNull(ketQua);
        assertEquals(1, ketQua.size());
        assertEquals(41L, ketQua.get(0).getId());
    }

    @Test
    @DisplayName("Kiểm thử Sales Rep không tìm thấy Lead ngoài phạm vi phân quyền CA_NHAN")
    void testPhanQuyenPhamViDuLieu() {
        // Lead #49 do Sales 2 (ID 3) phụ trách
        // Sales 1 (ID 2) tìm kiếm từ khóa "TEST S4-08" với phạm vi CA_NHAN sẽ không thấy Lead #49
        BoLocLeadDTO boLoc = new BoLocLeadDTO();
        boLoc.setTuKhoa("TEST S4-08");

        List<Lead> kqSales1 = leadDAO.layDanhSachLead(boLoc, salesUser1, PhamViDuLieu.CA_NHAN, 50, 0);
        assertNotNull(kqSales1);
        assertTrue(kqSales1.isEmpty(), "Sales Rep 1 không được nhìn thấy Lead của Sales Rep 2");

        int totalSales1 = leadDAO.demTongSoLead(boLoc, salesUser1, PhamViDuLieu.CA_NHAN);
        assertEquals(0, totalSales1);

        // Nhưng Sales 2 tìm sẽ thấy
        List<Lead> kqSales2 = leadDAO.layDanhSachLead(boLoc, salesUser2, PhamViDuLieu.CA_NHAN, 50, 0);
        assertNotNull(kqSales2);
        assertEquals(1, kqSales2.size());
        assertEquals(49L, kqSales2.get(0).getId());

        int totalSales2 = leadDAO.demTongSoLead(boLoc, salesUser2, PhamViDuLieu.CA_NHAN);
        assertEquals(1, totalSales2);
    }
}
