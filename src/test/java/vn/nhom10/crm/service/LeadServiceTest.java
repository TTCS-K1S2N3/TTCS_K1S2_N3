package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.TrangThaiLeadEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LeadService - Story S4-02: AC1 (Tạo lead thủ công) & AC3 (Bắt buộc nguồn)")
class LeadServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_02_lead_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;

    private LeadService leadService;
    private LeadDAO leadDAO;

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

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL UNIQUE)");

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
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'SU_KIEN', 'Sự kiện / Hội thảo')");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (2, 'DANH_THIEP', 'Danh thiếp đối tác')");
        }

        leadDAO = new LeadDAO();
        leadService = new LeadService(leadDAO, new DanhMucBanHangDAO());
    }

    @Test
    @DisplayName("AC1 & AC3: Tạo lead thủ công thành công khi có đủ Họ tên và Nguồn hợp lệ")
    void testTaoLeadThuCongThanhCong() {
        Lead input = new Lead();
        input.setHoTen("Nguyễn Văn An");
        input.setNguonLeadId(1L);
        input.setEmail("An.Nguyen@CongTy.vn");
        input.setSoDienThoai("0912-345-678");
        input.setTenCongTy("Công ty Alpha");
        input.setNhuCauQuanTam("Quan tâm giải pháp CRM");

        Lead result = leadService.taoLeadThuCong(input, 100L);

        assertNotNull(result.getId(), "Phải có ID tự sinh sau khi lưu");
        assertNotNull(result.getMaLead(), "Phải tự sinh mã định danh lead");
        assertTrue(result.getMaLead().startsWith("LEAD-"), "Mã lead phải theo mẫu LEAD-yyMMdd-XXXXX");
        assertEquals("Nguyễn Văn An", result.getHoTen());
        assertEquals(1L, result.getNguonLeadId());
        assertEquals(TrangThaiLeadEnum.MOI, result.getTrangThai(), "Trạng thái ban đầu phải là MỚI");
        assertEquals("an.nguyen@congty.vn", result.getEmailChuanHoa(), "Email phải được chuẩn hóa chữ thường");
        assertEquals("0912345678", result.getSoDienThoaiChuanHoa(), "SĐT phải được chuẩn hóa chỉ còn số");
        assertEquals("CÔNG TY ALPHA", result.getTenCongTyChuanHoa(), "Tên công ty phải chuẩn hóa chữ hoa");
        assertEquals(100L, result.getCreatedBy());

        // Kiểm tra đọc lại từ DAO
        Lead fromDb = leadDAO.timTheoId(result.getId());
        assertNotNull(fromDb);
        assertEquals("Nguyễn Văn An", fromDb.getHoTen());
    }

    @Test
    @DisplayName("AC3: Từ chối tạo lead khi thiếu nguồn lead (null hoặc <= 0)")
    void testTuChoiKhiThieuNguonLead() {
        Lead leadKhongNguon = new Lead();
        leadKhongNguon.setHoTen("Trần Thị Bình");
        leadKhongNguon.setNguonLeadId(null); // Vi phạm AC3

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            leadService.taoLeadThuCong(leadKhongNguon, 100L);
        });
        assertTrue(ex1.getMessage().contains("AC3"), "Thông điệp phải chứa AC3");

        leadKhongNguon.setNguonLeadId(0L); // Nguồn không hợp lệ
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            leadService.taoLeadThuCong(leadKhongNguon, 100L);
        });
        assertTrue(ex2.getMessage().contains("AC3"));
    }

    @Test
    @DisplayName("AC1: Tạo lead thủ công có ghi chú bối cảnh sự kiện/danh thiếp")
    void testTaoLeadThuCongCoGhiChu() {
        Lead lead = new Lead();
        lead.setHoTen("Nguyễn Thị Mai");
        lead.setNguonLeadId(1L);
        lead.setNhuCauQuanTam("Tư vấn phần mềm");
        lead.setGhiChu("Gặp tại TechExpo 2026");

        Lead ketQua = leadService.taoLeadThuCong(lead, 100L);
        assertNotNull(ketQua.getId());
        assertTrue(ketQua.getNhuCauQuanTam().contains("Tư vấn phần mềm"));
        assertTrue(ketQua.getNhuCauQuanTam().contains("Gặp tại TechExpo 2026"));
    }

    @Test
    @DisplayName("AC1: Từ chối tạo lead khi thiếu họ tên hoặc họ tên quá dài")
    void testTuChoiKhiThieuHoTen() {
        Lead leadRong = new Lead();
        leadRong.setHoTen("");
        leadRong.setNguonLeadId(1L);

        assertThrows(IllegalArgumentException.class, () -> leadService.taoLeadThuCong(leadRong, 100L));

        Lead leadQuaDai = new Lead();
        leadQuaDai.setHoTen("A".repeat(151));
        leadQuaDai.setNguonLeadId(1L);

        assertThrows(IllegalArgumentException.class, () -> leadService.taoLeadThuCong(leadQuaDai, 100L));
    }

    @Test
    @DisplayName("AC1: Từ chối khi email hoặc số điện thoại sai định dạng")
    void testTuChoiKhiSaiDinhDang() {
        Lead leadSaiEmail = new Lead();
        leadSaiEmail.setHoTen("Lê Văn C");
        leadSaiEmail.setNguonLeadId(2L);
        leadSaiEmail.setEmail("khong-hop-le");

        assertThrows(IllegalArgumentException.class, () -> leadService.taoLeadThuCong(leadSaiEmail, 100L));

        Lead leadSaiPhone = new Lead();
        leadSaiPhone.setHoTen("Lê Văn C");
        leadSaiPhone.setNguonLeadId(2L);
        leadSaiPhone.setSoDienThoai("abcxyz");

        assertThrows(IllegalArgumentException.class, () -> leadService.taoLeadThuCong(leadSaiPhone, 100L));
    }

    @Test
    @DisplayName("Danh sách Lead: Phân trang và tìm kiếm theo ID")
    void testLayDanhSachLeadVaTimTheoId() {
        Lead l1 = new Lead();
        l1.setHoTen("Lead Số 1");
        l1.setNguonLeadId(1L);
        leadService.taoLeadThuCong(l1, 100L);

        Lead l2 = new Lead();
        l2.setHoTen("Lead Số 2");
        l2.setNguonLeadId(2L);
        leadService.taoLeadThuCong(l2, 100L);

        List<Lead> ds = leadService.layDanhSachLead(10, 0);
        assertEquals(2, ds.size());

        Lead timThay = leadService.timTheoId(l1.getId());
        assertNotNull(timThay);
        assertEquals("Lead Số 1", timThay.getHoTen());
    }
}
