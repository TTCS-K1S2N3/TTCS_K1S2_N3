package vn.nhom10.crm.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.DongExcelNguoiDungDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Kiểm thử tiện ích Excel: ExcelNguoiDungUtil (Story S2-01)")
public class ExcelNguoiDungUtilTest {

    @Test
    @DisplayName("AC 1: Tạo tệp mẫu Excel thành công với 2 sheet chuẩn")
    void testTaoTepMauExcel() throws Exception {
        byte[] bytes = ExcelNguoiDungUtil.taoTepMauExcel();

        assertNotNull(bytes, "Mảng byte của tệp Excel mẫu không được null");
        assertTrue(bytes.length > 0, "Dung lượng tệp Excel mẫu phải lớn hơn 0");

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, wb.getNumberOfSheets(), "Tệp mẫu phải có đúng 2 sheet");
            assertNotNull(wb.getSheet("DanhSachNguoiDung"), "Phải có sheet DanhSachNguoiDung");
            assertNotNull(wb.getSheet("HuongDan"), "Phải có sheet HuongDan");

            Sheet sheet1 = wb.getSheet("DanhSachNguoiDung");
            Row headerRow = sheet1.getRow(0);
            assertNotNull(headerRow, "Dòng header không được null");
            assertEquals("STT", headerRow.getCell(0).getStringCellValue());
            assertTrue(headerRow.getCell(1).getStringCellValue().contains("Họ và tên"));
            assertTrue(headerRow.getCell(2).getStringCellValue().contains("Email"));
        }
    }

    @Test
    @DisplayName("AC 2: Đọc dữ liệu từ tệp Excel và trích xuất đúng các dòng")
    void testDocDanhSachTuExcel() throws Exception {
        byte[] excelBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachNguoiDung");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Họ và tên");
            header.createCell(2).setCellValue("Email công ty");
            header.createCell(3).setCellValue("Số điện thoại");
            header.createCell(4).setCellValue("Mã vai trò");
            header.createCell(5).setCellValue("Mã nhóm kinh doanh");
            header.createCell(6).setCellValue("Mật khẩu");

            // Dòng 1
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("1");
            r1.createCell(1).setCellValue("Trần Văn Nam");
            r1.createCell(2).setCellValue("nam.tran@crm.vn");
            r1.createCell(3).setCellValue("0981234567");
            r1.createCell(4).setCellValue("SALES_REP");
            r1.createCell(5).setCellValue("KD_MIEN_BAC");
            r1.createCell(6).setCellValue("Pass@123");

            // Dòng 2
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("2");
            r2.createCell(1).setCellValue("Lê Thị Hoa");
            r2.createCell(2).setCellValue("hoa.le@crm.vn");
            r2.createCell(3).setCellValue("0912345678");
            r2.createCell(4).setCellValue("MARKETING");
            r2.createCell(5).setCellValue("");
            r2.createCell(6).setCellValue("");

            // Dòng trống (phải bị bỏ qua)
            sheet.createRow(3);

            wb.write(out);
            excelBytes = out.toByteArray();
        }

        List<DongExcelNguoiDungDTO> danhSach = ExcelNguoiDungUtil.docDanhSachTuExcel(new ByteArrayInputStream(excelBytes));

        assertNotNull(danhSach);
        assertEquals(2, danhSach.size(), "Phải đọc được 2 dòng dữ liệu, bỏ qua dòng trống");

        DongExcelNguoiDungDTO d1 = danhSach.get(0);
        assertEquals(2, d1.getSoDong(), "Dòng 1 trong dữ liệu tương ứng dòng 2 Excel");
        assertEquals("Trần Văn Nam", d1.getHoTen());
        assertEquals("nam.tran@crm.vn", d1.getEmail());
        assertEquals("0981234567", d1.getSoDienThoai());
        assertEquals("SALES_REP", d1.getVaiTroNhap());
        assertEquals("KD_MIEN_BAC", d1.getNhomNhap());
        assertEquals("Pass@123", d1.getMatKhau());

        DongExcelNguoiDungDTO d2 = danhSach.get(1);
        assertEquals(3, d2.getSoDong(), "Dòng 2 trong dữ liệu tương ứng dòng 3 Excel");
        assertEquals("Lê Thị Hoa", d2.getHoTen());
        assertEquals("hoa.le@crm.vn", d2.getEmail());
        assertEquals("MARKETING", d2.getVaiTroNhap());
    }
}
