package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.TrangThaiLeadEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LeadDAO - Story S4-01")
class LeadDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_01_lead_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private LeadDAO dao;

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
                    "ten_nguon VARCHAR(150) NOT NULL, " +
                    "thu_tu_hien_thi INT DEFAULT 0, " +
                    "hoat_dong TINYINT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS chien_dich (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_chien_dich VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_chien_dich VARCHAR(200) NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS bieu_mau_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_bieu_mau VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_bieu_mau VARCHAR(200) NOT NULL, " +
                    "token_cong_khai VARCHAR(128) NOT NULL UNIQUE, " +
                    "nguon_lead_id BIGINT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

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
                    "qua_sla TINYINT DEFAULT 0, " +
                    "khach_hang_chuyen_doi_id BIGINT NULL, " +
                    "nguoi_lien_he_chuyen_doi_id BIGINT NULL, " +
                    "co_hoi_chuyen_doi_id BIGINT NULL, " +
                    "chuyen_doi_luc DATETIME NULL, " +
                    "gop_vao_lead_id BIGINT NULL, " +
                    "created_by BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE)");

            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'WEB_INBOUND', 'Website Inbound')");
            st.execute("INSERT INTO bieu_mau_lead (id, ma_bieu_mau, ten_bieu_mau, token_cong_khai, nguon_lead_id) " +
                    "VALUES (10, 'BM_TEST', 'Form Test', 'token_bm_test', 1)");
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
        dao = new LeadDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM `lead`");
        }
    }

    @Test
    @DisplayName("AC4: Thêm Lead ở trạng thái MỚI, gắn đúng nguồn và truy vấn")
    void testThemLeadTrangThaiMoi() {
        Lead lead = new Lead();
        lead.setMaLead("LEAD-261011-00001");
        lead.setHoTen("Nguyễn Văn Khách Hàng");
        lead.setEmail("khachhang@example.com");
        lead.setEmailChuanHoa("khachhang@example.com");
        lead.setSoDienThoai("0987 654 321");
        lead.setSoDienThoaiChuanHoa("0987654321");
        lead.setTenCongTy("Công ty CP Công Nghệ Sao Mai");
        lead.setTenCongTyChuanHoa("CÔNG TY CP CÔNG NGHỆ SAO MAI");
        lead.setNhuCauQuanTam("Tư vấn giải pháp CRM quản lý bán hàng");
        lead.setNguonLeadId(1L);
        lead.setBieuMauId(10L);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);

        Long id = dao.them(lead);
        assertNotNull(id);
        assertTrue(id > 0);

        Lead timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("LEAD-261011-00001", timThay.getMaLead());
        assertEquals("Nguyễn Văn Khách Hàng", timThay.getHoTen());
        assertEquals("khachhang@example.com", timThay.getEmail());
        assertEquals("0987 654 321", timThay.getSoDienThoai());
        assertEquals("0987654321", timThay.getSoDienThoaiChuanHoa());
        assertEquals("Công ty CP Công Nghệ Sao Mai", timThay.getTenCongTy());
        assertEquals("Tư vấn giải pháp CRM quản lý bán hàng", timThay.getNhuCauQuanTam());
        assertEquals(1L, timThay.getNguonLeadId());
        assertEquals("Website Inbound", timThay.getTenNguonLead());
        assertEquals(10L, timThay.getBieuMauId());
        assertEquals("Form Test", timThay.getTenBieuMau());
        assertEquals(TrangThaiLeadEnum.MOI, timThay.getTrangThai());
    }

    @Test
    @DisplayName("AC4: Thêm Lead qua Connection transaction và kiểm tra rollback")
    void testTransactionRollback() throws Exception {
        Lead lead = new Lead();
        lead.setMaLead("LEAD-TX-ROLLBACK");
        lead.setHoTen("Trần Thị Test Rollback");
        lead.setNguonLeadId(1L);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);

        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "")) {
            conn.setAutoCommit(false);
            dao.them(conn, lead);
            // Cố tình rollback
            conn.rollback();
        }

        Lead khongTimThay = dao.timTheoMa("LEAD-TX-ROLLBACK");
        assertNull(khongTimThay, "Lead đã bị rollback không được tồn tại trong DB");
    }
}
