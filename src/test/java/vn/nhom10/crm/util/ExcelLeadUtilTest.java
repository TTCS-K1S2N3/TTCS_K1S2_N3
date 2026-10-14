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
