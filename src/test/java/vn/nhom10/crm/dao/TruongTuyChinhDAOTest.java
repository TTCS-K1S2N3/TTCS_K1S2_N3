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

            // Bảng khach_hang thực tế phục vụ kiểm thử xuất Excel
            st.execute("CREATE TABLE khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ma_so_thue VARCHAR(50) NULL, " +
                    "dia_chi VARCHAR(500) NULL, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'TIEM_NANG'" +
                    ")");

            // Bảng co_hoi thực tế phục vụ kiểm thử xuất Excel
            st.execute("CREATE TABLE co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "gia_tri_du_kien DECIMAL(18,2) NOT NULL DEFAULT 0.00, " +
                    "ngay_chot_du_kien DATE NULL, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'MO'" +
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

    // =========================================================================
    // KIỂM THỬ XUẤT EXCEL THỰC TẾ (REAL DB DATA - CHỐNG DỮ LIỆU DEMO/SAMPLE)
    // =========================================================================

    @Test
    @DisplayName("Excel Real: Khi DB trống không có KH-001 -> Excel chỉ có header, không được tự sinh dòng demo")
    void testExportKhongCoRecordKhongSinhDemoData() throws Exception {
        vn.nhom10.crm.service.TruongTuyChinhService service = new vn.nhom10.crm.service.TruongTuyChinhService(dao);

        // Đảm bảo bảng khach_hang hoàn toàn trống
        List<Map<String, Object>> danhSach = dao.layDanhSachThucTeChoXuatExcel("KHACH_HANG");
        assertNotNull(danhSach);
        assertTrue(danhSach.isEmpty(), "Bảng khach_hang phải trống khi chưa có dữ liệu");

        List<String> cotChuan = List.of("Mã khách hàng", "Tên công ty", "Mã số thuế", "Địa chỉ", "Trạng thái");
        List<String> keys = List.of("ma_khach_hang", "ten_cong_ty", "ma_so_thue", "dia_chi", "trang_thai");

        byte[] xlsxBytes = service.xuatDuLieuExcelXLSX("KHACH_HANG", cotChuan, keys, danhSach, java.util.Collections.emptyMap());
        assertNotNull(xlsxBytes);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsxBytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("Khách hàng");
            assertNotNull(sheet);

            // Dòng tiêu đề phải tồn tại
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("Mã khách hàng", headerRow.getCell(0).getStringCellValue());
            assertEquals("Tên công ty", headerRow.getCell(1).getStringCellValue());

            // Số dòng vật lý trong sheet chỉ được là 1 (chỉ có dòng tiêu đề, 0 dòng dữ liệu)
            assertEquals(1, sheet.getPhysicalNumberOfRows(), "Khi DB không có record, file chỉ được có 1 dòng header");
            assertNull(sheet.getRow(1), "Tuyệt đối không được tự sinh dòng dữ liệu mẫu (KH-001)");
        }
    }

    @Test
    @DisplayName("Excel Real: Tạo 1 khách hàng thật trong DB + custom fields (NUMBER, DATE, SELECT, TEXT) -> Verify từng ô đúng dữ liệu DB")
    void testExportKhachHangThatKemCustomValuesThat() throws Exception {
        vn.nhom10.crm.service.TruongTuyChinhService service = new vn.nhom10.crm.service.TruongTuyChinhService(dao);

        // 1. Tạo 1 khách hàng thật trong database
        long realCustomerId;
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO khach_hang (ma_khach_hang, ten_cong_ty, ma_so_thue, dia_chi, trang_thai) " +
                    "VALUES ('KH-REAL-888', 'Tập đoàn Công nghệ Real Corp', '0109998888', 'Cầu Giấy, Hà Nội', 'KHACH_HANG')",
                    Statement.RETURN_GENERATED_KEYS);
            try (ResultSet rs = st.getGeneratedKeys()) {
                assertTrue(rs.next());
                realCustomerId = rs.getLong(1);
            }
        }
        assertTrue(realCustomerId > 0);

        // 2. Tạo 4 trường tuỳ chỉnh chuẩn
        TruongTuyChinh tText = new TruongTuyChinh("KHACH_HANG", "ghi_chu_vip", "Ghi chú VIP", "VAN_BAN", false);
        tText.setHienThiExcel(true);
        long idText = dao.them(tText);

        TruongTuyChinh tNumber = new TruongTuyChinh("KHACH_HANG", "doanh_thu_nam", "Doanh thu năm", "SO", false);
        tNumber.setHienThiExcel(true);
        long idNumber = dao.them(tNumber);

        TruongTuyChinh tDate = new TruongTuyChinh("KHACH_HANG", "ngay_thanh_lap", "Ngày thành lập", "NGAY", false);
        tDate.setHienThiExcel(true);
        long idDate = dao.them(tDate);

        TruongTuyChinh tSelect = new TruongTuyChinh("KHACH_HANG", "loai_hop_dong", "Loại hợp đồng", "DANH_SACH_CHON", false);
        tSelect.setDanhSachLuaChon(List.of("Hợp đồng khung", "Hợp đồng thử nghiệm"));
        tSelect.setHienThiExcel(true);
        long idSelect = dao.them(tSelect);

        // 3. Gán giá trị thật cho khách hàng này trong database
        assertTrue(dao.luuGiaTri("KHACH_HANG", realCustomerId, idText, "VAN_BAN", "Khách VIP miền Bắc"));
        assertTrue(dao.luuGiaTri("KHACH_HANG", realCustomerId, idNumber, "SO", "12345.67"));
        assertTrue(dao.luuGiaTri("KHACH_HANG", realCustomerId, idDate, "NGAY", "2026-10-03"));
        assertTrue(dao.luuGiaTri("KHACH_HANG", realCustomerId, idSelect, "DANH_SACH_CHON", "Hợp đồng khung"));

        // 4. Lấy dữ liệu thực tế và xuất Excel
        List<Map<String, Object>> danhSach = dao.layDanhSachThucTeChoXuatExcel("KHACH_HANG");
        assertEquals(1, danhSach.size());
        assertEquals(realCustomerId, danhSach.get(0).get("id"));
        assertEquals("KH-REAL-888", danhSach.get(0).get("ma_khach_hang"));
        assertEquals("Tập đoàn Công nghệ Real Corp", danhSach.get(0).get("ten_cong_ty"));

        Map<Long, Map<String, String>> customVals = dao.layTatCaGiaTriTheoDanhSach("KHACH_HANG", List.of(realCustomerId));

        List<String> cotChuan = List.of("Mã khách hàng", "Tên công ty", "Mã số thuế", "Địa chỉ", "Trạng thái");
        List<String> keys = List.of("ma_khach_hang", "ten_cong_ty", "ma_so_thue", "dia_chi", "trang_thai");

        byte[] xlsxBytes = service.xuatDuLieuExcelXLSX("KHACH_HANG", cotChuan, keys, danhSach, customVals);
        assertNotNull(xlsxBytes);

        // 5. Kiểm chứng từng ô trong file Excel khớp đúng dữ liệu DB
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsxBytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("Khách hàng");
            assertNotNull(sheet);
            assertEquals(2, sheet.getPhysicalNumberOfRows()); // 1 header + 1 data row

            org.apache.poi.ss.usermodel.Row row = sheet.getRow(1);
            assertNotNull(row);

            // Cột chuẩn
            assertEquals("KH-REAL-888", row.getCell(0).getStringCellValue());
            assertEquals("Tập đoàn Công nghệ Real Corp", row.getCell(1).getStringCellValue());
            assertEquals("0109998888", row.getCell(2).getStringCellValue());
            assertEquals("Cầu Giấy, Hà Nội", row.getCell(3).getStringCellValue());
            assertEquals("KHACH_HANG", row.getCell(4).getStringCellValue());

            // Cột trường tuỳ chỉnh
            assertEquals("Khách VIP miền Bắc", row.getCell(5).getStringCellValue());
            assertEquals("12345.67", row.getCell(6).getStringCellValue());
            assertEquals("2026-10-03", row.getCell(7).getStringCellValue());
            assertEquals("Hợp đồng khung", row.getCell(8).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Excel Real: Hai trường cùng display name -> header được tự động gán [ma_truong] chống mơ hồ")
    void testDuplicateDisplayNameHeaderDisambiguation() throws Exception {
        vn.nhom10.crm.service.TruongTuyChinhService service = new vn.nhom10.crm.service.TruongTuyChinhService(dao);

        TruongTuyChinh t1 = new TruongTuyChinh("KHACH_HANG", "phan_khuc_kh", "Phân khúc khách hàng", "VAN_BAN", false);
        t1.setHienThiExcel(true);
        dao.them(t1);

        TruongTuyChinh t2 = new TruongTuyChinh("KHACH_HANG", "phan_khuc_khach_hang", "Phân khúc khách hàng", "DANH_SACH_CHON", false);
        t2.setDanhSachLuaChon(List.of("VIP", "Thường"));
        t2.setHienThiExcel(true);
        dao.them(t2);

        TruongTuyChinh t3 = new TruongTuyChinh("KHACH_HANG", "nguon_lead", "Nguồn giới thiệu", "VAN_BAN", false);
        t3.setHienThiExcel(true);
        dao.them(t3);

        List<String> cotChuan = List.of("Mã KH");
        List<String> keys = List.of("ma");

        byte[] xlsxBytes = service.xuatDuLieuExcelXLSX("KHACH_HANG", cotChuan, keys, java.util.Collections.emptyList(), java.util.Collections.emptyMap());

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsxBytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("Khách hàng");
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);

            assertEquals("Mã KH", headerRow.getCell(0).getStringCellValue());
            // Hai trường trùng display name phải kèm [ma_truong]
            assertEquals("Phân khúc khách hàng [phan_khuc_kh]", headerRow.getCell(1).getStringCellValue());
            assertEquals("Phân khúc khách hàng [phan_khuc_khach_hang]", headerRow.getCell(2).getStringCellValue());
            // Trường không trùng thì giữ nguyên display name bình thường
            assertEquals("Nguồn giới thiệu", headerRow.getCell(3).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Excel Real: Field tắt Excel hoặc Field inactive không được xuất ra file")
    void testFieldTatExcelVaFieldInactiveKhongXuat() throws Exception {
        vn.nhom10.crm.service.TruongTuyChinhService service = new vn.nhom10.crm.service.TruongTuyChinhService(dao);

        TruongTuyChinh tExcelOff = new TruongTuyChinh("KHACH_HANG", "field_excel_tat", "Field Tắt Excel", "VAN_BAN", false);
        tExcelOff.setHienThiExcel(false); // Tắt Excel
        dao.them(tExcelOff);

        TruongTuyChinh tInactive = new TruongTuyChinh("KHACH_HANG", "field_inactive", "Field Ngừng Áp Dụng", "VAN_BAN", false);
        tInactive.setHienThiExcel(true);
        tInactive.setHoatDong(false); // Ngừng áp dụng
        dao.them(tInactive);

        TruongTuyChinh tActive = new TruongTuyChinh("KHACH_HANG", "field_active", "Field Hoạt Động Chuẩn", "VAN_BAN", false);
        tActive.setHienThiExcel(true);
        tActive.setHoatDong(true);
        dao.them(tActive);

        List<String> cotChuan = List.of("Mã KH");
        List<String> keys = List.of("ma");

        byte[] xlsxBytes = service.xuatDuLieuExcelXLSX("KHACH_HANG", cotChuan, keys, java.util.Collections.emptyList(), java.util.Collections.emptyMap());

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsxBytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("Khách hàng");
            org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);

            assertEquals("Mã KH", headerRow.getCell(0).getStringCellValue());
            assertEquals("Field Hoạt Động Chuẩn", headerRow.getCell(1).getStringCellValue());
            // Chỉ có 2 cột (1 cột chuẩn + 1 cột active có bật Excel)
            assertNull(headerRow.getCell(2), "Không được có cột của field tắt Excel hoặc field inactive");
        }
    }
}
