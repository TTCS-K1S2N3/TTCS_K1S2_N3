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

    @Test
    @DisplayName("Test 1: DB chỉ có nguồn 'cntt'. File sử dụng ba nguồn giả định khác DB phải bị báo lỗi nguồn ngay ở bước xem trước")
    void test1_DBChiCoNguonCNTT_FileDungNguonGiaDinh_BaoLoiXemTruoc() throws Exception {
        // Thiết lập DB test chỉ có 1 nguồn duy nhất: ID 1: 'cntt'
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'cntt', 'cntt')");
        }

        // Tạo file Excel chứa 3 nguồn giả định khác DB: Sự kiện / Hội thảo, Danh thiếp đối tác, Khách hàng giới thiệu
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachLead");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ và tên (*)");
            header.createCell(2).setCellValue("Nguồn Lead (*)");

            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("Nguyễn Văn An");
            r1.createCell(2).setCellValue("Sự kiện / Hội thảo");

            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Trần Thị Bình");
            r2.createCell(2).setCellValue("Danh thiếp đối tác");

            var r3 = sheet.createRow(3);
            r3.createCell(1).setCellValue("Lê Hoàng Cường");
            r3.createCell(2).setCellValue("Khách hàng giới thiệu");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            BaoCaoNhapLeadExcelDTO baoCao = importService.thamDinhTepExcel(is, "tep_mau_nhap_lead.xlsx");
            assertNotNull(baoCao);
            assertEquals(3, baoCao.getTongSoDong());
            assertEquals(0, baoCao.getSoDongHopLe(), "Cả 3 dòng dùng nguồn không có trong DB phải không hợp lệ");
            assertEquals(3, baoCao.getSoDongLoi(), "Cả 3 dòng phải bị đánh dấu lỗi ngay ở bước xem trước");

            // Kiểm tra từng dòng đều bị báo lỗi nguồn không tồn tại
            for (DongExcelLeadDTO dong : baoCao.getDanhSachTatCaDong()) {
                assertFalse(dong.isHopLe());
                assertNull(dong.getNguonLeadId(), "Tuyệt đối không được ánh xạ nhầm sang ID 1 (cntt)");
                assertTrue(dong.getChuoiLoi().contains("không tồn tại trong danh mục nguồn"),
                        "Thông báo lỗi phải chỉ rõ nguồn không tồn tại: " + dong.getChuoiLoi());
            }

            // Tuyệt đối chưa ghi bản ghi nào vào CSDL
            List<Lead> leadsInDb = leadDAO.layDanhSach(10, 0);
            assertEquals(0, leadsInDb.size());
        }
    }

    @Test
    @DisplayName("Test 2: File có 3 dòng với nguồn thật 'cntt' và dữ liệu hợp lệ. Xác nhận nhập phải lưu đúng 3 bản ghi")
    void test2_FileCo3DongNguonThatCNTT_XacNhanNhapLuuDung3BanGhi() throws Exception {
        // DB có nguồn thật: ID 1: 'cntt'
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'cntt', 'cntt')");
        }

        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachLead");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ và tên (*)");
            header.createCell(2).setCellValue("Nguồn Lead (*)");
            header.createCell(3).setCellValue("Số điện thoại");
            header.createCell(4).setCellValue("Email");

            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("Nguyễn Văn An");
            r1.createCell(2).setCellValue("cntt");
            r1.createCell(3).setCellValue("0912345678");
            r1.createCell(4).setCellValue("an@cntt.vn");

            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Trần Thị Bình");
            r2.createCell(2).setCellValue("cntt");
            r2.createCell(3).setCellValue("0987654321");
            r2.createCell(4).setCellValue("binh@cntt.vn");

            var r3 = sheet.createRow(3);
            r3.createCell(1).setCellValue("Lê Hoàng Cường");
            r3.createCell(2).setCellValue("cntt");
            r3.createCell(3).setCellValue("0903112233");
            r3.createCell(4).setCellValue("cuong@cntt.vn");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            BaoCaoNhapLeadExcelDTO baoCao = importService.thamDinhTepExcel(is, "leads_cntt.xlsx");
            assertEquals(3, baoCao.getTongSoDong());
            assertEquals(3, baoCao.getSoDongHopLe());
            assertEquals(0, baoCao.getSoDongLoi());

            // Tiến hành nhập
            BaoCaoNhapLeadExcelDTO ketQua = importService.thucHienNhapDuLieu(baoCao, 1L);
            assertTrue(ketQua.isDaThucHienNhap());
            assertEquals(3, ketQua.getSoDongThanhCong(), "Cả 3 dòng phải được lưu thành công");
            assertEquals(0, ketQua.getSoDongThatBai(), "Không có dòng thất bại");

            // Kiểm tra CSDL
            List<Lead> leadsInDb = leadDAO.layDanhSach(10, 0);
            assertEquals(3, leadsInDb.size(), "CSDL phải lưu đúng 3 bản ghi Lead");
            for (Lead lead : leadsInDb) {
                assertEquals(1L, lead.getNguonLeadId());
            }

            // Kiểm tra dot_nhap_du_lieu ghi nhận chính xác
            try (Statement st = rootConnection.createStatement();
                 var rs = st.executeQuery("SELECT so_dong_hop_le, so_dong_loi, so_dong_da_nhap FROM dot_nhap_du_lieu WHERE id = " + ketQua.getDotNhapId())) {
                assertTrue(rs.next());
                assertEquals(3, rs.getInt("so_dong_hop_le"));
                assertEquals(0, rs.getInt("so_dong_loi"));
                assertEquals(3, rs.getInt("so_dong_da_nhap"));
            }
        }
    }

    @Test
    @DisplayName("Test 3: File có 1 dòng hợp lệ, 1 dòng thiếu nguồn và 1 dòng email sai. Chỉ dòng hợp lệ được nhập")
    void test3_FileCo1HopLe_1ThieuNguon_1EmailSai_ChiDongHopLeDuocNhap() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'cntt', 'cntt')");
        }

        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachLead");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ và tên (*)");
            header.createCell(2).setCellValue("Nguồn Lead (*)");
            header.createCell(3).setCellValue("Số điện thoại");
            header.createCell(4).setCellValue("Email");

            // Dòng 1: Hợp lệ
            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("Nguyễn Hợp Lệ");
            r1.createCell(2).setCellValue("cntt");
            r1.createCell(3).setCellValue("0912345678");
            r1.createCell(4).setCellValue("hople@cntt.vn");

            // Dòng 2: Thiếu nguồn (AC3)
            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Trần Thiếu Nguồn");
            r2.createCell(2).setCellValue(""); // RỖNG
            r2.createCell(3).setCellValue("0987654321");
            r2.createCell(4).setCellValue("thieunguon@cntt.vn");

            // Dòng 3: Email sai cú pháp
            var r3 = sheet.createRow(3);
            r3.createCell(1).setCellValue("Lê Email Sai");
            r3.createCell(2).setCellValue("cntt");
            r3.createCell(3).setCellValue("0903112233");
            r3.createCell(4).setCellValue("email-sai-dinh-dang");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            BaoCaoNhapLeadExcelDTO baoCao = importService.thamDinhTepExcel(is, "mix_leads.xlsx");
            assertEquals(3, baoCao.getTongSoDong());
            assertEquals(1, baoCao.getSoDongHopLe());
            assertEquals(2, baoCao.getSoDongLoi());

            BaoCaoNhapLeadExcelDTO ketQua = importService.thucHienNhapDuLieu(baoCao, 1L);
            assertEquals(1, ketQua.getSoDongThanhCong(), "Chỉ 1 dòng hợp lệ được lưu");
            assertEquals(2, ketQua.getSoDongThatBai(), "2 dòng lỗi phải bị bỏ qua");

            // Kiểm tra CSDL
            List<Lead> leadsInDb = leadDAO.layDanhSach(10, 0);
            assertEquals(1, leadsInDb.size());
            assertEquals("Nguyễn Hợp Lệ", leadsInDb.get(0).getHoTen());

            // Kiểm tra dot_nhap_du_lieu và loi_nhap_du_lieu
            try (Statement st = rootConnection.createStatement();
                 var rs = st.executeQuery("SELECT so_dong_hop_le, so_dong_loi, so_dong_da_nhap FROM dot_nhap_du_lieu WHERE id = " + ketQua.getDotNhapId())) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("so_dong_hop_le"));
                assertEquals(2, rs.getInt("so_dong_loi"));
                assertEquals(1, rs.getInt("so_dong_da_nhap"));
            }

            try (Statement st = rootConnection.createStatement();
                 var rs = st.executeQuery("SELECT COUNT(*) AS total_errors FROM loi_nhap_du_lieu WHERE dot_nhap_id = " + ketQua.getDotNhapId())) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("total_errors"), "Bảng loi_nhap_du_lieu phải ghi đủ 2 dòng lỗi (dòng 2 và 3)");
            }
        }
    }

    @Test
    @DisplayName("Test 4: Giả lập lỗi DAO khi lưu một dòng đã qua validation. Tổng hợp kết quả đúng và có lỗi theo dòng")
    void test4_GiaLapLoiDAOKhiLuuDongDaQuaValidation_TongHopKetQuaDungVaCoLoiTheoDong() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'cntt', 'cntt')");
        }

        // Tạo DAO giả lập: Dòng 1 lưu thành công, Dòng 2 ném exception giả lập lỗi DB/FK
        LeadDAO daoGiaLap = new LeadDAO() {
            @Override
            public Long them(Lead lead) {
                if ("Lead Gặp Sự Cố DB".equals(lead.getHoTen())) {
                    throw new RuntimeException("Lỗi khóa ngoại FK hoặc SQL internal constraint violation");
                }
                return super.them(lead);
            }
        };

        DanhMucBanHangDAO dmDAO = new DanhMucBanHangDAO();
        DotNhapDuLieuDAO dotDAO = new DotNhapDuLieuDAO();
        LeadService svc = new LeadService(daoGiaLap, dmDAO);
        LeadImportService importServiceGiaLap = new LeadImportService(daoGiaLap, dmDAO, dotDAO, svc);

        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachLead");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ và tên (*)");
            header.createCell(2).setCellValue("Nguồn Lead (*)");

            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("Lead Bình Thường");
            r1.createCell(2).setCellValue("cntt");

            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Lead Gặp Sự Cố DB");
            r2.createCell(2).setCellValue("cntt");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            // Bước xem trước: cả 2 dòng đều hợp lệ theo định dạng
            BaoCaoNhapLeadExcelDTO baoCao = importServiceGiaLap.thamDinhTepExcel(is, "test4.xlsx");
            assertEquals(2, baoCao.getTongSoDong());
            assertEquals(2, baoCao.getSoDongHopLe());
            assertEquals(0, baoCao.getSoDongLoi());

            // Tiến hành nhập
            BaoCaoNhapLeadExcelDTO ketQua = importServiceGiaLap.thucHienNhapDuLieu(baoCao, 1L);

            // Kết quả tổng hợp phải phản ánh đúng thực tế
            assertEquals(1, ketQua.getSoDongThanhCong(), "Chỉ dòng 1 được lưu thành công");
            assertEquals(1, ketQua.getSoDongThatBai(), "Dòng 2 thất bại khi lưu DB");
            assertEquals(1, ketQua.getSoDongLoi(), "Tổng dòng lỗi sau nhập là 1");
            assertEquals(1, ketQua.getSoDongHopLe(), "Dòng hợp lệ thực tế là 1");
            assertEquals(1, ketQua.getSoDongLoiLuuDb(), "1 dòng hợp lệ lúc xem trước nhưng lưu DB thất bại");

            // Kiểm tra lỗi của dòng 2: An toàn cho người dùng, không lộ chi tiết SQL nội bộ
            DongExcelLeadDTO dong2 = ketQua.getDanhSachTatCaDong().get(1);
            assertFalse(dong2.isThanhCong());
            assertFalse(dong2.getChuoiLoi().contains("SQL internal constraint violation"),
                    "Không được hiển thị chi tiết SQL hoặc thông tin database nhạy cảm ra chuỗi lỗi: " + dong2.getChuoiLoi());
            assertTrue(dong2.getChuoiLoi().contains("Lưu vào cơ sở dữ liệu thất bại"));

            // Kiểm tra dot_nhap_du_lieu và loi_nhap_du_lieu được lưu chính xác
            try (Statement st = rootConnection.createStatement();
                 var rs = st.executeQuery("SELECT so_dong_hop_le, so_dong_loi, so_dong_da_nhap FROM dot_nhap_du_lieu WHERE id = " + ketQua.getDotNhapId())) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("so_dong_hop_le"));
                assertEquals(1, rs.getInt("so_dong_loi"), "dot_nhap_du_lieu phải ghi nhận so_dong_loi = 1");
                assertEquals(1, rs.getInt("so_dong_da_nhap"));
            }

            try (Statement st = rootConnection.createStatement();
                 var rs = st.executeQuery("SELECT so_dong, thong_bao_loi FROM loi_nhap_du_lieu WHERE dot_nhap_id = " + ketQua.getDotNhapId())) {
                assertTrue(rs.next());
                assertEquals(3, rs.getInt("so_dong"), "Số dòng Excel của dòng 2 phải là 3 (1-based row)");
                assertFalse(rs.getString("thong_bao_loi").contains("SQL internal constraint violation"));
            }
        }
    }

    @Test
    @DisplayName("Test 5: Upload file Excel sai mẫu phải có thông báo phù hợp, không phân tích sai thứ tự cột rồi ghi nhầm dữ liệu")
    void test5_UploadFileExcelSaiMau_BaoLoiPhuHop_KhongPhanTichSaiThuTuCot() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nguon_lead");
            st.execute("INSERT INTO nguon_lead (id, ma_nguon, ten_nguon) VALUES (1, 'cntt', 'cntt')");
        }

        // Trường hợp A: File sai mẫu (không có cột Họ tên hoặc Nguồn)
        byte[] fileSaiMau;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("BangGia");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã dịch vụ");
            header.createCell(1).setCellValue("Tên dịch vụ");
            header.createCell(2).setCellValue("Giá cước");

            var r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("DV01");
            r1.createCell(1).setCellValue("Internet Cáp Quang");
            r1.createCell(2).setCellValue("350000");

            wb.write(out);
            fileSaiMau = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileSaiMau)) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    importService.thamDinhTepExcel(is, "bang_gia.xlsx")
            );
            assertTrue(ex.getMessage().contains("không đúng cấu trúc mẫu nhập Lead"));
        }

        // Trường hợp B: File đảo thứ tự cột (Email ở cột 1, Họ tên ở cột 2, Nguồn ở cột 3)
        // Hệ thống phải nhận diện theo header và đọc đúng giá trị, không phân tích sai thứ tự
        byte[] fileDaoCot;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachLead");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Email");
            header.createCell(2).setCellValue("Họ và tên (*)");
            header.createCell(3).setCellValue("Nguồn Lead (*)");

            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("daocot@cntt.vn");
            r1.createCell(2).setCellValue("Nguyễn Đảo Cột");
            r1.createCell(3).setCellValue("cntt");

            wb.write(out);
            fileDaoCot = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileDaoCot)) {
            BaoCaoNhapLeadExcelDTO baoCao = importService.thamDinhTepExcel(is, "dao_cot.xlsx");
            assertEquals(1, baoCao.getTongSoDong());
            assertEquals(1, baoCao.getSoDongHopLe());

            DongExcelLeadDTO d = baoCao.getDanhSachTatCaDong().get(0);
            assertEquals("Nguyễn Đảo Cột", d.getHoTen(), "Họ tên phải được trích xuất chính xác");
            assertEquals("daocot@cntt.vn", d.getEmail(), "Email phải được trích xuất chính xác");
            assertEquals("cntt", d.getTenNguonLead(), "Nguồn Lead phải được trích xuất chính xác");
            assertEquals(1L, d.getNguonLeadId());

            // Tiến hành nhập và kiểm tra dữ liệu trong CSDL được ghi đúng cột
            BaoCaoNhapLeadExcelDTO ketQua = importService.thucHienNhapDuLieu(baoCao, 1L);
            assertEquals(1, ketQua.getSoDongThanhCong());

            List<Lead> leadsInDb = leadDAO.layDanhSach(10, 0);
            Lead lead = leadsInDb.get(0);
            assertEquals("Nguyễn Đảo Cột", lead.getHoTen(), "Không được ghi nhầm email vào trường họ tên");
            assertEquals("daocot@cntt.vn", lead.getEmail(), "Không được ghi nhầm họ tên vào trường email");
            assertEquals(1L, lead.getNguonLeadId());
        }
    }
}
