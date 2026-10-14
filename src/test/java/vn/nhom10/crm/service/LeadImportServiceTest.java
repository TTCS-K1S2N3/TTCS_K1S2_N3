package vn.nhom10.crm.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.DotNhapDuLieuDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.BaoCaoNhapLeadExcelDTO;
import vn.nhom10.crm.dto.DongExcelLeadDTO;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.util.ExcelLeadUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử LeadImportService - Story S4-02: AC2 (Xem trước & Báo lỗi dòng) & AC3 (Bắt buộc nguồn)")
class LeadImportServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_s4_02_lead_import_test;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;

    private LeadImportService importService;
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

            st.execute("CREATE TABLE IF NOT EXISTS dot_nhap_du_lieu (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "loai_doi_tuong VARCHAR(50) NOT NULL, " +
                    "ten_file_goc VARCHAR(255) NOT NULL, " +
                    "duong_dan_file VARCHAR(500) NULL, " +
                    "trang_thai VARCHAR(30) NOT NULL DEFAULT 'TAI_LEN', " +
                    "tong_dong INT DEFAULT 0, " +
                    "so_dong_hop_le INT DEFAULT 0, " +
                    "so_dong_loi INT DEFAULT 0, " +
                    "so_dong_da_nhap INT DEFAULT 0, " +
                    "xem_truoc_json TEXT NULL, " +
                    "nguoi_thuc_hien_id BIGINT NOT NULL, " +
                    "tao_luc DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "hoan_tat_luc DATETIME NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS loi_nhap_du_lieu (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "dot_nhap_id BIGINT NOT NULL, " +
                    "so_dong INT NOT NULL, " +
                    "ma_loi VARCHAR(80) NULL, " +
                    "thong_bao_loi VARCHAR(1000) NOT NULL, " +
                    "du_lieu_dong_json TEXT NULL, " +
                    "co_the_bo_qua TINYINT DEFAULT 1)");
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
            st.execute("DELETE FROM loi_nhap_du_lieu");
            st.execute("DELETE FROM dot_nhap_du_lieu");
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'SU_KIEN', 'Sự kiện / Hội thảo')");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (2, 'DANH_THIEP', 'Danh thiếp đối tác')");
        }

        leadDAO = new LeadDAO();
        DanhMucBanHangDAO danhMucDAO = new DanhMucBanHangDAO();
        DotNhapDuLieuDAO dotNhapDAO = new DotNhapDuLieuDAO();
        LeadService leadService = new LeadService(leadDAO, danhMucDAO);
        importService = new LeadImportService(leadDAO, danhMucDAO, dotNhapDAO, leadService);
    }

    @Test
    @DisplayName("AC2: Xem trước tệp Excel - Phân loại đúng số dòng hợp lệ và dòng lỗi, chưa ghi CSDL")
    void testThamDinhTepExcel() throws Exception {
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Data");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ tên");
            header.createCell(2).setCellValue("Nguồn");

            // Dòng 1: Hợp lệ
            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("Nguyễn Văn An");
            r1.createCell(2).setCellValue("Sự kiện / Hội thảo");

            // Dòng 2: LỖI THIẾU NGUỒN (AC3)
            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Trần Thị Bình");
            r2.createCell(2).setCellValue("");

            // Dòng 3: Hợp lệ
            var r3 = sheet.createRow(3);
            r3.createCell(1).setCellValue("Lê Hoàng Cường");
            r3.createCell(2).setCellValue("Danh thiếp đối tác");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            BaoCaoNhapLeadExcelDTO baoCao = importService.thamDinhTepExcel(is, "danh_sach_lead.xlsx");
            assertNotNull(baoCao);
            assertEquals(3, baoCao.getTongSoDong());
            assertEquals(2, baoCao.getSoDongHopLe());
            assertEquals(1, baoCao.getSoDongLoi());
            assertFalse(baoCao.isDaThucHienNhap(), "Bước xem trước không được đánh dấu đã nhập");

            // Kiểm tra chưa có dòng nào ghi vào CSDL
            List<Lead> leadsInDb = leadDAO.layDanhSach(10, 0);
            assertEquals(0, leadsInDb.size(), "Bước xem trước tuyệt đối không được ghi vào CSDL");
        }
    }

    @Test
    @DisplayName("AC2 & AC3: Xác nhận nhập dữ liệu - Chỉ nhập dòng hợp lệ, bỏ qua dòng lỗi và ghi vết đợt nhập")
    void testThucHienNhapDuLieu() throws Exception {
        BaoCaoNhapLeadExcelDTO baoCao = new BaoCaoNhapLeadExcelDTO("leads.xlsx");

        DongExcelLeadDTO d1 = new DongExcelLeadDTO(2);
        d1.setHoTen("Nguyễn Văn An");
        d1.setNguonLeadId(1L);
        d1.setEmail("an@test.vn");
        d1.setHopLe(true);

        DongExcelLeadDTO d2 = new DongExcelLeadDTO(3);
        d2.setHoTen("Lỗi Nguồn");
        d2.setNguonLeadId(null);
        d2.themLoi("Thiếu nguồn lead (AC3)");

        DongExcelLeadDTO d3 = new DongExcelLeadDTO(4);
        d3.setHoTen("Lê Hoàng Cường");
        d3.setNguonLeadId(2L);
        d3.setSoDienThoai("0903112233");
        d3.setHopLe(true);

        baoCao.napDanhSachDong(List.of(d1, d2, d3));

        BaoCaoNhapLeadExcelDTO ketQua = importService.thucHienNhapDuLieu(baoCao, 50L);

        assertTrue(ketQua.isDaThucHienNhap());
        assertEquals(2, ketQua.getSoDongThanhCong(), "2 dòng hợp lệ phải được nhập thành công");
        assertEquals(1, ketQua.getSoDongThatBai(), "1 dòng lỗi phải bị bỏ qua");

        // Kiểm tra trong CSDL thật
        List<Lead> leadsInDb = leadDAO.layDanhSach(10, 0);
        assertEquals(2, leadsInDb.size());

        // Kiểm tra dot_nhap_du_lieu được lưu
        assertNotNull(ketQua.getDotNhapId(), "Đợt nhập phải được lưu vào CSDL vết");
        assertTrue(ketQua.getThongDiep().contains("2/3 lead nhập thành công"));
    }
}
