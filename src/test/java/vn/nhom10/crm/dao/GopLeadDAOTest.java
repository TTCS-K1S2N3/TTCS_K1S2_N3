package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.KetQuaGopLeadDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.UngVienTrungLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử GopLeadDAO trên H2 cách ly - Story S4-04 (AC1, AC2, AC3)")
class GopLeadDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_04_gop_lead_dao_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private GopLeadDAO dao;

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
                    "email VARCHAR(150) NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255) NULL, " +
                    "nguoi_so_huu_id BIGINT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "gop_vao_khach_hang_id BIGINT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(255) NULL, " +
                    "so_dien_thoai VARCHAR(20) NULL)");

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
                    "nguon_lead_id BIGINT DEFAULT 1, " +
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
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "lead_id BIGINT NULL, " +
                    "khach_hang_id BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS cong_viec (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "lead_id BIGINT NULL, " +
                    "khach_hang_id BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "lead_id BIGINT NULL, " +
                    "khach_hang_id BIGINT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS lich_su_gop_lead (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "lead_nguon_id BIGINT NOT NULL, " +
                    "lead_dich_id BIGINT NOT NULL, " +
                    "thuc_hien_boi_id BIGINT NOT NULL, " +
                    "du_lieu_gop_json JSON NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
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
    void cleanTables() throws Exception {
        dao = new GopLeadDAO();
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM lich_su_gop_lead");
            st.execute("DELETE FROM hoat_dong");
            st.execute("DELETE FROM cong_viec");
            st.execute("DELETE FROM co_hoi");
            st.execute("DELETE FROM `lead`");
            st.execute("DELETE FROM nguoi_lien_he");
            st.execute("DELETE FROM khach_hang");
            st.execute("DELETE FROM nguoi_dung");

            // Seed user
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Admin Demo', 'admin@crm.vn')");
        }
    }

    @Test
    @DisplayName("AC1: Phát hiện Lead ứng viên trùng theo Email, Số điện thoại và Tên công ty")
    void testTimUngVienLeadTrung() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, ten_cong_ty_chuan_hoa) " +
                    "VALUES (101, 'LEAD-001', 'Nguyen Van A', 'demo@crm.vn', 'demo@crm.vn', '0901234567', '0901234567', 'Cong ty ABC', 'abc')");

            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, ten_cong_ty_chuan_hoa) " +
                    "VALUES (102, 'LEAD-002', 'Nguyen Van B', 'demo@crm.vn', 'demo@crm.vn', '0988777666', '0988777666', 'Khac Cty', 'khac cty')");

            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, ten_cong_ty_chuan_hoa) " +
                    "VALUES (103, 'LEAD-003', 'Nguyen Van C', 'khac@crm.vn', 'khac@crm.vn', '0901234567', '0901234567', 'Khac Cty', 'khac cty')");
        }

        Lead lead101 = dao.layChiTietLead(101L);
        assertNotNull(lead101);

        NguoiDungDTO admin = new NguoiDungDTO(1L, "Admin", "admin@crm.vn", VaiTroEnum.ADMIN, 1L, "HN", PhamViDuLieu.TOAN_BO);
        List<UngVienTrungLeadDTO> ungVien = dao.timUngVienLeadTrung(lead101, admin, Collections.emptySet());

        assertEquals(2, ungVien.size());
        boolean co102 = ungVien.stream().anyMatch(u -> u.getRecordId().equals(102L) && u.getMatchReasons().contains("EMAIL"));
        boolean co103 = ungVien.stream().anyMatch(u -> u.getRecordId().equals(103L) && u.getMatchReasons().contains("PHONE"));

        assertTrue(co102);
        assertTrue(co103);
    }

    @Test
    @DisplayName("AC2: Phát hiện Khách hàng hiện có trùng để gợi ý liên kết")
    void testTimUngVienCustomerTrung() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa) VALUES (801, 'KH-801', 'Cong ty ABC Demo', 'abc demo')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, email, so_dien_thoai) VALUES (1, 801, 'Dai Dien', 'contact801@ac2.test', '0905555555')");

            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, email, email_chuan_hoa, so_dien_thoai, so_dien_thoai_chuan_hoa, ten_cong_ty, ten_cong_ty_chuan_hoa) " +
                    "VALUES (101, 'LEAD-101', 'Lead A', 'contact801@ac2.test', 'contact801@ac2.test', '0911111111', '0911111111', 'Cong ty ABC Demo', 'abc demo')");
        }

        Lead lead101 = dao.layChiTietLead(101L);
        NguoiDungDTO admin = new NguoiDungDTO(1L, "Admin", "admin@crm.vn", VaiTroEnum.ADMIN, 1L, "HN", PhamViDuLieu.TOAN_BO);

        List<UngVienTrungLeadDTO> customers = dao.timUngVienCustomerTrung(lead101, admin, Collections.emptySet());
        assertEquals(1, customers.size());
        UngVienTrungLeadDTO c = customers.get(0);
        assertEquals(801L, c.getRecordId());
        assertTrue(c.getMatchReasons().contains("COMPANY"));
        assertTrue(c.getMatchReasons().contains("EMAIL"));
    }

    @Test
    @DisplayName("AC2: Gắn Lead vào Khách hàng thành công")
    void testGanKhachHang() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten) VALUES (101, 'LEAD-101', 'Lead A')");
        }

        boolean ok = dao.ganKhachHang(101L, 801L);
        assertTrue(ok);

        Lead lead = dao.layChiTietLead(101L);
        assertEquals(801L, lead.getKhachHangChuyenDoiId());
    }

    @Test
    @DisplayName("AC3: Gộp Lead bảo toàn toàn bộ lịch sử hoạt động, công việc, cơ hội")
    void testThucHienGopLeadBaoToanLichSu() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            // Seed 2 leads
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, trang_thai) VALUES (101, 'LEAD-101', 'Lead Dich', 'MOI')");
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, trang_thai) VALUES (102, 'LEAD-102', 'Lead Nguon', 'DANG_CHAM_SOC')");

            // Seed lịch sử của lead nguồn: 2 hoạt động, 1 công việc, 1 cơ hội
            st.execute("INSERT INTO hoat_dong (id, tieu_de, lead_id) VALUES (1, 'Cuoc goi A', 102)");
            st.execute("INSERT INTO hoat_dong (id, tieu_de, lead_id) VALUES (2, 'Email B', 102)");
            st.execute("INSERT INTO cong_viec (id, tieu_de, lead_id) VALUES (1, 'Gui bao gia', 102)");
            st.execute("INSERT INTO co_hoi (id, ma_co_hoi, ten_co_hoi, lead_id) VALUES (1, 'CH-01', 'Co hoi mua CRM', 102)");

            // Seed lịch sử của lead đích: 1 hoạt động
            st.execute("INSERT INTO hoat_dong (id, tieu_de, lead_id) VALUES (3, 'Lien he ban dau', 101)");
        }

        // Thực hiện gộp 102 vào 101
        KetQuaGopLeadDTO kq = dao.thucHienGopLead(102L, 101L, 1L, "Trùng thông tin liên hệ", "{\"test\":true}");
        assertTrue(kq.isThanhCong());
        assertEquals(101L, kq.getSurvivorLeadId());
        assertEquals(102L, kq.getMergedSourceLeadId());
        assertEquals(2, kq.getSoHoatDongDaChuyen());
        assertEquals(1, kq.getSoCongViecDaChuyen());
        assertEquals(1, kq.getSoCoHoiDaChuyen());

        // Kiểm tra sau khi gộp: Lead nguồn có trang_thai = 'DA_GOP', gop_vao_lead_id = 101
        Lead srcAfter = dao.layChiTietLead(102L);
        assertEquals(TrangThaiLeadEnum.DA_GOP, srcAfter.getTrangThai());
        assertEquals(101L, srcAfter.getGopVaoLeadId());

        // Kiểm tra toàn bộ hoạt động, công việc, cơ hội đã reparent sang Lead đích (101)
        GopLeadDAO.ThongKeLichSuLead tkDst = dao.layThongKeLichSu(101L);
        assertEquals(3, tkDst.soHoatDong); // 1 ban đầu + 2 từ nguồn
        assertEquals(1, tkDst.soCongViec); // 1 từ nguồn
        assertEquals(1, tkDst.soCoHoi);    // 1 từ nguồn

        // Lead nguồn không còn lịch sử gắn trực tiếp
        GopLeadDAO.ThongKeLichSuLead tkSrc = dao.layThongKeLichSu(102L);
        assertEquals(0, tkSrc.soHoatDong);
        assertEquals(0, tkSrc.soCongViec);
        assertEquals(0, tkSrc.soCoHoi);
    }

    @Test
    @DisplayName("AC3: Transaction Rollback khi lỗi - không mất dữ liệu và không cập nhật dở dang")
    void testTransactionRollbackKhiLoi() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, trang_thai) VALUES (101, 'LEAD-101', 'Lead Dich', 'MOI')");
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, trang_thai) VALUES (102, 'LEAD-102', 'Lead Nguon', 'MOI')");
            st.execute("INSERT INTO hoat_dong (id, tieu_de, lead_id) VALUES (1, 'Hoat dong A', 102)");
        }

        // Bắt buộc lỗi giữa chừng để test rollback
        assertThrows(SQLException.class, () -> {
            try (Connection conn = DatabaseConfig.getConnection()) {
                dao.thucHienGopLeadTrenKetNoi(conn, 102L, 101L, 1L, "Test rollback", "{}", true);
            }
        });

        // Kiểm chứng sau rollback: Lead nguồn vẫn giữ nguyên MOI, gop_vao_lead_id = null
        Lead src = dao.layChiTietLead(102L);
        assertEquals(TrangThaiLeadEnum.MOI, src.getTrangThai());
        assertNull(src.getGopVaoLeadId());

        // Hoạt động vẫn thuộc lead nguồn 102
        GopLeadDAO.ThongKeLichSuLead tkSrc = dao.layThongKeLichSu(102L);
        assertEquals(1, tkSrc.soHoatDong);

        GopLeadDAO.ThongKeLichSuLead tkDst = dao.layThongKeLichSu(101L);
        assertEquals(0, tkDst.soHoatDong);
    }

    @Test
    @DisplayName("Từ chối gộp Lead đã bị gộp trước đó")
    void testTuChoiGopLeadDaBiGop() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, trang_thai, gop_vao_lead_id) VALUES (101, 'LEAD-101', 'Lead Dich', 'MOI', NULL)");
            st.execute("INSERT INTO `lead` (id, ma_lead, ho_ten, trang_thai, gop_vao_lead_id) VALUES (102, 'LEAD-102', 'Lead Da Gop', 'DA_GOP', 99L)");
        }

        assertThrows(SQLException.class, () -> {
            dao.thucHienGopLead(102L, 101L, 1L, "Gop lai", "{}");
        });
    }
}
