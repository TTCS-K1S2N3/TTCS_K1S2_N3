package vn.nhom10.crm.util;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.DongExcelLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử ExcelLeadUtil - Story S4-02: AC2 (Tệp mẫu, xem trước) & AC3 (Bắt buộc nguồn)")
class ExcelLeadUtilTest {

    @Test
    @DisplayName("AC2: Tạo tệp mẫu Excel có đủ 2 sheet (DanhSachLead, HuongDan_QuyTac) và đúng định dạng")
    void testTaoTepMauExcel() throws Exception {
        byte[] bytes = ExcelLeadUtil.taoTepMauExcel();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (InputStream is = new ByteArrayInputStream(bytes);
             Workbook workbook = WorkbookFactory.create(is)) {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("DanhSachLead", workbook.getSheetAt(0).getSheetName());
            assertEquals("HuongDan_QuyTac", workbook.getSheetAt(1).getSheetName());

            Sheet dataSheet = workbook.getSheetAt(0);
            assertTrue(dataSheet.getLastRowNum() >= 3, "Bảng mẫu phải có ít nhất 3 dòng dữ liệu minh họa");
            assertEquals("STT", dataSheet.getRow(0).getCell(0).getStringCellValue());
            assertTrue(dataSheet.getRow(0).getCell(2).getStringCellValue().contains("Nguồn Lead"), "Cột nguồn phải có tiêu đề rõ ràng");
        }
    }

    @Test
    @DisplayName("AC2 & AC3: Đọc tệp mẫu chuẩn thành công, các dòng mẫu đều hợp lệ và có nguồn")
    void testDocDanhSachLeadTuTepMau() throws Exception {
        byte[] bytes = ExcelLeadUtil.taoTepMauExcel();
        List<MucDanhMucDTO> dsNguon = taoDanhSachNguonMau();

        try (InputStream is = new ByteArrayInputStream(bytes)) {
            List<DongExcelLeadDTO> dsDong = ExcelLeadUtil.docDanhSachLead(is, dsNguon);
            assertNotNull(dsDong);
            assertEquals(3, dsDong.size(), "Tệp mẫu chứa 3 dòng dữ liệu minh họa");

            for (DongExcelLeadDTO dong : dsDong) {
                assertTrue(dong.isHopLe(), "Dòng mẫu phải hợp lệ: " + dong.getChuoiLoi());
                assertNotNull(dong.getHoTen());
                assertNotNull(dong.getNguonLeadId(), "AC3: Mọi dòng phải nhận diện được nguồn");
                assertFalse(dong.getTenNguonLead().isBlank());
            }
        }
    }

    @Test
    @DisplayName("AC3: Báo lỗi chi tiết khi dòng thiếu Nguồn Lead (Mọi lead bắt buộc có nguồn)")
    void testBaoLoiKhiThieuNguonLead() throws Exception {
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Data");
            // Header
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ và tên (*)");
            header.createCell(2).setCellValue("Nguồn Lead (*)");

            // Dòng 1: Hợp lệ
            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("Nguyễn Văn An");
            r1.createCell(2).setCellValue("Sự kiện / Hội thảo");

            // Dòng 2: THIẾU NGUỒN (Vi phạm AC3)
            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Trần Thị Bình");
            r2.createCell(2).setCellValue(""); // RỖNG

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        List<MucDanhMucDTO> dsNguon = taoDanhSachNguonMau();
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            List<DongExcelLeadDTO> dsDong = ExcelLeadUtil.docDanhSachLead(is, dsNguon);
            assertEquals(2, dsDong.size());

            // Dòng 1 hợp lệ
            assertTrue(dsDong.get(0).isHopLe());
            assertEquals(1L, dsDong.get(0).getNguonLeadId());

            // Dòng 2 vi phạm AC3
            DongExcelLeadDTO dong2 = dsDong.get(1);
            assertFalse(dong2.isHopLe(), "Dòng thiếu nguồn không được đánh dấu hợp lệ");
            assertTrue(dong2.getChuoiLoi().contains("AC3"), "Thông báo lỗi phải đề cập rõ tiêu chí AC3");
            assertTrue(dong2.getChuoiLoi().contains("Nguồn Lead không được để trống"));
        }
    }

    @Test
    @DisplayName("AC2: Báo lỗi khi thiếu Họ tên hoặc email/SĐT sai định dạng")
    void testBaoLoiHoTenVaDinhDangLienHe() throws Exception {
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Data");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("Họ tên");
            header.createCell(2).setCellValue("Nguồn");
            header.createCell(3).setCellValue("SĐT");
            header.createCell(4).setCellValue("Email");

            // Dòng 1: Thiếu họ tên
            var r1 = sheet.createRow(1);
            r1.createCell(1).setCellValue("");
            r1.createCell(2).setCellValue("Danh thiếp đối tác");

            // Dòng 2: Email sai cú pháp, SĐT sai
            var r2 = sheet.createRow(2);
            r2.createCell(1).setCellValue("Lê Văn C");
            r2.createCell(2).setCellValue("Khách hàng giới thiệu");
            r2.createCell(3).setCellValue("abc12345"); // SĐT chứa chữ
            r2.createCell(4).setCellValue("sai-email"); // Email không @

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        List<MucDanhMucDTO> dsNguon = taoDanhSachNguonMau();
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            List<DongExcelLeadDTO> dsDong = ExcelLeadUtil.docDanhSachLead(is, dsNguon);
            assertEquals(2, dsDong.size());

            // Dòng 1: lỗi thiếu họ tên
            assertFalse(dsDong.get(0).isHopLe());
            assertTrue(dsDong.get(0).getChuoiLoi().contains("Họ và tên khách hàng không được để trống"));

            // Dòng 2: lỗi email và SĐT
            assertFalse(dsDong.get(1).isHopLe());
            assertTrue(dsDong.get(1).getChuoiLoi().contains("email không đúng định dạng"));
            assertTrue(dsDong.get(1).getChuoiLoi().contains("Số điện thoại không hợp lệ"));
        }
    }

    @Test
    @DisplayName("Biên: Tệp rỗng hoặc chỉ có Header không gây exception")
    void testTepRongKhongGayLoi() throws Exception {
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Empty");
            sheet.createRow(0).createCell(0).setCellValue("Header only");
            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            List<DongExcelLeadDTO> ds = ExcelLeadUtil.docDanhSachLead(is, new ArrayList<>());
            assertNotNull(ds);
            assertTrue(ds.isEmpty());
        }
    }

    @Test
    @DisplayName("S4-02: Tạo tệp mẫu Excel sử dụng danh mục nguồn thực tế từ CSDL")
    void testTaoTepMauExcelTheoDanhMucDB() throws Exception {
        List<MucDanhMucDTO> dsNguon = List.of(
                new MucDanhMucDTO(1L, null, "CNTT", "cntt", "", 1, true, 0, null, "")
        );
        byte[] bytes = ExcelLeadUtil.taoTepMauExcel(dsNguon);
        assertNotNull(bytes);

        try (InputStream is = new ByteArrayInputStream(bytes);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet dataSheet = workbook.getSheetAt(0);
            // Dòng mẫu 1, 2, 3 đều phải sử dụng nguồn thực tế từ DB là "cntt"
            assertEquals("cntt", dataSheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals("cntt", dataSheet.getRow(2).getCell(2).getStringCellValue());
            assertEquals("cntt", dataSheet.getRow(3).getCell(2).getStringCellValue());

            Sheet guideSheet = workbook.getSheetAt(1);
            String guideText = guideSheet.getRow(4).getCell(2).getStringCellValue();
            assertTrue(guideText.contains("cntt"), "Hướng dẫn phải chứa nguồn cntt thực tế từ DB");
            assertFalse(guideText.contains("Sự kiện / Hội thảo"), "Không được hard-code các nguồn giả định");
        }
    }

    @Test
    @DisplayName("S4-02: Đối chiếu nguồn nghiêm ngặt với CSDL - Tuyệt đối không fallback sang ID 1-6")
    void testTimIdNguonLeadChuanXacKhongFallbackGiaDinh() {
        List<MucDanhMucDTO> dsNguon = List.of(
                new MucDanhMucDTO(1L, null, "CNTT", "cntt", "", 1, true, 0, null, "")
        );

        // Khớp nguồn thật "cntt"
        assertEquals(1L, ExcelLeadUtil.timIdNguonLead("cntt", dsNguon));
        assertEquals(1L, ExcelLeadUtil.timIdNguonLead("CNTT", dsNguon));
        assertEquals(1L, ExcelLeadUtil.timIdNguonLead("1", dsNguon));

        // Các nguồn giả định không có trong DB phải trả về null, KHÔNG được gán nhầm sang 1L (cntt)
        assertNull(ExcelLeadUtil.timIdNguonLead("Sự kiện / Hội thảo", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead("Danh thiếp đối tác", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead("Khách hàng giới thiệu", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead("Website / Biểu mẫu trực tuyến", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead("2", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead("999", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead("", dsNguon));
        assertNull(ExcelLeadUtil.timIdNguonLead(null, dsNguon));
    }

    @Test
    @DisplayName("Test 5: Tệp Excel sai mẫu (thiếu cột bắt buộc) phải ném ngoại lệ rõ ràng")
    void testUploadFileExcelSaiMau_BaoLoi() throws Exception {
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("InvalidTemplate");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã sản phẩm");
            header.createCell(1).setCellValue("Tên sản phẩm");
            header.createCell(2).setCellValue("Đơn giá");

            var r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("SP01");
            r1.createCell(1).setCellValue("Laptop Dell");
            r1.createCell(2).setCellValue("15000000");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    ExcelLeadUtil.docDanhSachLead(is, taoDanhSachNguonMau())
            );
            assertTrue(ex.getMessage().contains("không đúng cấu trúc mẫu nhập Lead"));
            assertTrue(ex.getMessage().contains("Họ và tên"));
            assertTrue(ex.getMessage().contains("Nguồn Lead"));
        }
    }

    @Test
    @DisplayName("Test 5: Tệp Excel đảo thứ tự các cột vẫn phân tích chính xác từng trường")
    void testUploadFileExcelDaoThuTuCot_VanDocDung() throws Exception {
        byte[] fileBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("ReorderedColumns");
            var header = sheet.createRow(0);
            // Đảo thứ tự: Email ở cột 0, Nguồn Lead ở cột 1, Họ và tên ở cột 2, SĐT ở cột 3
            header.createCell(0).setCellValue("Email");
            header.createCell(1).setCellValue("Nguồn Lead (*)");
            header.createCell(2).setCellValue("Họ và tên (*)");
            header.createCell(3).setCellValue("Số điện thoại");

            var r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("hoang.nam@congty.vn");
            r1.createCell(1).setCellValue("Sự kiện / Hội thảo");
            r1.createCell(2).setCellValue("Hoàng Văn Nam");
            r1.createCell(3).setCellValue("0912998877");

            wb.write(out);
            fileBytes = out.toByteArray();
        }

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            List<DongExcelLeadDTO> ds = ExcelLeadUtil.docDanhSachLead(is, taoDanhSachNguonMau());
            assertEquals(1, ds.size());
            DongExcelLeadDTO d = ds.get(0);
            assertTrue(d.isHopLe());
            // Họ tên được đọc từ cột 2
            assertEquals("Hoàng Văn Nam", d.getHoTen());
            // Nguồn được đọc từ cột 1
            assertEquals("Sự kiện / Hội thảo", d.getTenNguonLead());
            assertEquals(1L, d.getNguonLeadId());
            // Email được đọc từ cột 0
            assertEquals("hoang.nam@congty.vn", d.getEmail());
            // SĐT được đọc từ cột 3
            assertEquals("0912998877", d.getSoDienThoai());
        }
    }

    private List<MucDanhMucDTO> taoDanhSachNguonMau() {
        List<MucDanhMucDTO> list = new ArrayList<>();
        list.add(new MucDanhMucDTO(1L, null, "SU_KIEN", "Sự kiện / Hội thảo", "", 1, true, 0, null, ""));
        list.add(new MucDanhMucDTO(2L, null, "DANH_THIEP", "Danh thiếp đối tác", "", 2, true, 0, null, ""));
        list.add(new MucDanhMucDTO(3L, null, "GIOI_THIEU", "Khách hàng giới thiệu", "", 3, true, 0, null, ""));
        list.add(new MucDanhMucDTO(4L, null, "WEBSITE", "Website / Biểu mẫu trực tuyến", "", 4, true, 0, null, ""));
        list.add(new MucDanhMucDTO(5L, null, "DIEN_THOAI", "Gọi điện thoại trực tiếp", "", 5, true, 0, null, ""));
        list.add(new MucDanhMucDTO(6L, null, "KHAC", "Nguồn tiếp cận khác", "", 6, true, 0, null, ""));
        return list;
    }
}
