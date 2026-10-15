package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.LichSuPhanBoLead;
import vn.nhom10.crm.model.TrangThaiLeadEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tích hợp DAO cho Story S4-07 trên H2 in-memory cách ly")
class LeadDAOSlaIntegrationTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_07_dao_integration_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private static LeadDAO leadDAO;
    private static LichSuPhanBoLeadDAO lichSuDAO;
    private static HoatDongDAO hoatDongDAO;
    private static ThongBaoDAO thongBaoDAO;

    @BeforeAll
    static void setUp() throws Exception {
        rootConnection = DriverManager.getConnection(H2_URL, "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL, "sa", "");
            } catch (SQLException e) {
                return null;
            }
        });

        leadDAO = new LeadDAO();
        lichSuDAO = new LichSuPhanBoLeadDAO();
        hoatDongDAO = new HoatDongDAO();
        thongBaoDAO = new ThongBaoDAO();

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
                    "email VARCHAR(255) NOT NULL UNIQUE)");

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
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_phan_bo_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "lead_id BIGINT NOT NULL, " +
                    "quy_tac_id BIGINT NULL, " +
                    "nguoi_dung_cu_id BIGINT NULL, " +
                    "nguoi_dung_moi_id BIGINT NULL, " +
                    "nhom_id BIGINT NULL, " +
                    "hanh_dong VARCHAR(30) NOT NULL, " +
                    "ly_do VARCHAR(500) NULL, " +
                    "thuc_hien_boi_id BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_hoat_dong VARCHAR(50) NOT NULL UNIQUE, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "loai_hoat_dong VARCHAR(50) NOT NULL DEFAULT 'CUOC_GOI', " +
                    "lead_id BIGINT NULL, " +
                    "nguoi_phu_trach_id BIGINT NOT NULL, " +
                    "thoi_gian_bat_dau DATETIME NULL, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'HOAN_THANH', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS thong_bao (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "loai_thong_bao_id BIGINT NOT NULL, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "noi_dung TEXT NULL, " +
                    "loai_doi_tuong VARCHAR(50) NULL, " +
                    "doi_tuong_id BIGINT NULL, " +
                    "duong_dan_mo VARCHAR(500) NULL, " +
                    "da_doc TINYINT(1) DEFAULT 0, " +
                    "doc_luc DATETIME NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // Chèn dữ liệu danh mục mẫu
            st.execute("MERGE INTO nguon_lead KEY(id) VALUES (1, 'FORM_WEB', 'Biểu mẫu website')");
            st.execute("MERGE INTO nguoi_dung KEY(id) VALUES (101, 'Sales Rep A', 'salesA@msys.vn')");
            st.execute("MERGE INTO nguoi_dung KEY(id) VALUES (201, 'Team Lead A', 'leadA@msys.vn')");
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
    @DisplayName("DAO: Thao tác Nhận Lead cập nhật trạng thái DANG_CHAM_SOC và duoc_nhan_luc")
    void testNhanLead_DAO() {
        Lead lead = new Lead();
        lead.setMaLead("LEAD-INT-01");
        lead.setHoTen("Khách hàng Test Nhận");
        lead.setNguonLeadId(1L);
        lead.setNguoiPhuTrachId(101L);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);
        Long id = leadDAO.them(lead);
        assertNotNull(id);

        LocalDateTime now = LocalDateTime.now();
        boolean thanhCong = leadDAO.nhanLead(id, 101L, now);
        assertTrue(thanhCong);

        Lead sauKhiNhan = leadDAO.timTheoId(id);
        assertNotNull(sauKhiNhan);
        assertEquals(TrangThaiLeadEnum.DANG_CHAM_SOC, sauKhiNhan.getTrangThai());
        assertNotNull(sauKhiNhan.getDuocNhanLuc());
    }

    @Test
    @DisplayName("DAO: Thao tác Từ chối Lead trong transaction gỡ người phụ trách và lưu lịch sử phân bổ")
    void testTuChoiLead_DAO_Transaction() throws Exception {
        Lead lead = new Lead();
        lead.setMaLead("LEAD-INT-02");
        lead.setHoTen("Khách hàng Test Từ Chối");
        lead.setNguonLeadId(1L);
        lead.setNguoiPhuTrachId(101L);
        lead.setTrangThai(TrangThaiLeadEnum.MOI);
        Long id = leadDAO.them(lead);
        assertNotNull(id);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            boolean capNhat = leadDAO.tuChoiLead(conn, id, 101L, "[SAI_KHU_VUC] Địa bàn ngoài khu vực");
            assertTrue(capNhat);

            LichSuPhanBoLead ls = new LichSuPhanBoLead(id, 101L, null, null, "TU_CHOI", "[SAI_KHU_VUC] Địa bàn ngoài khu vực", 101L);
            Long lsId = lichSuDAO.them(conn, ls);
            assertNotNull(lsId);

            conn.commit();
        }

        Lead sauTuChoi = leadDAO.timTheoId(id);
        assertNotNull(sauTuChoi);
        assertEquals(TrangThaiLeadEnum.MOI, sauTuChoi.getTrangThai());
        assertNull(sauTuChoi.getNguoiPhuTrachId());
        assertEquals("[SAI_KHU_VUC] Địa bàn ngoài khu vực", sauTuChoi.getLyDoTuChoi());

        List<LichSuPhanBoLead> dsLs = lichSuDAO.layLichSuTheoLead(id);
        assertFalse(dsLs.isEmpty());
        assertEquals("TU_CHOI", dsLs.get(0).getHanhDong());
    }

    @Test
    @DisplayName("DAO: HoatDongDAO phát hiện chính xác có/không có bằng chứng liên hệ trước hạn")
    void testHoatDongLienHe_DAO() throws Exception {
        Lead lead = new Lead();
        lead.setMaLead("LEAD-INT-03");
        lead.setHoTen("Khách hàng Test Hoạt Động");
        lead.setNguonLeadId(1L);
        Long id = leadDAO.them(lead);

        LocalDateTime tuLuc = LocalDateTime.of(2026, 10, 14, 9, 0, 0);
        LocalDateTime denLuc = LocalDateTime.of(2026, 10, 15, 9, 0, 0);

        // Chưa có hoạt động nào
        assertFalse(hoatDongDAO.coHoatDongLienHeHopLe(id, tuLuc, denLuc));

        // Thêm một cuộc gọi đã thực hiện lúc 10:00 ngày 14
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO hoat_dong (ma_hoat_dong, tieu_de, loai_hoat_dong, lead_id, nguoi_phu_trach_id, thoi_gian_bat_dau, trang_thai) " +
                    "VALUES ('HD-TEST-01', 'Gọi điện liên hệ', 'CUOC_GOI', " + id + ", 101, '2026-10-14 10:00:00', 'HOAN_THANH')");
        }

        assertTrue(hoatDongDAO.coHoatDongLienHeHopLe(id, tuLuc, denLuc));
    }
}
