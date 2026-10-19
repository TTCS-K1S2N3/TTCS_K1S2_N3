package vn.nhom10.crm.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.DongExcelKhachHangDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Kiểm thử tiện ích Excel Khách hàng: ExcelKhachHangUtil (Story S3-06)")
public class ExcelKhachHangUtilTest {

    @Test
    @DisplayName("AC 1: Tạo tệp mẫu Excel thành công với 2 sheet chuẩn nghiệp vụ")
    void testTaoTepMauExcel() throws Exception {
        byte[] bytes = ExcelKhachHangUtil.taoTepMauExcel();

        assertNotNull(bytes, "Mảng byte của tệp mẫu không được null");
        assertTrue(bytes.length > 0, "Dung lượng tệp mẫu phải > 0");

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, wb.getNumberOfSheets(), "Tệp mẫu phải có đúng 2 sheet");
            assertNotNull(wb.getSheet("DanhSachKhachHang"), "Phải có sheet DanhSachKhachHang");
            assertNotNull(wb.getSheet("HuongDan_QuyTac"), "Phải có sheet HuongDan_QuyTac");

            Sheet sheet1 = wb.getSheet("DanhSachKhachHang");
            Row headerRow = sheet1.getRow(0);
            assertNotNull(headerRow, "Dòng header không được null");
            assertEquals("STT", headerRow.getCell(0).getStringCellValue());
            assertEquals("Mã khách hàng", headerRow.getCell(1).getStringCellValue());
            assertTrue(headerRow.getCell(2).getStringCellValue().contains("Tên công ty / Khách hàng"));
            assertTrue(headerRow.getCell(3).getStringCellValue().contains("Mã số thuế"));

            // Có 3 dòng mẫu
            assertTrue(sheet1.getLastRowNum() >= 3, "Phải có dữ liệu mẫu");
        }
    }

    @Test
    @DisplayName("AC 1: Đọc dữ liệu từ tệp Excel và báo lỗi theo từng dòng")
    void testDocDanhSachBaoLoiTungDong() throws Exception {
        byte[] excelBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachKhachHang");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Mã KH");
            header.createCell(2).setCellValue("Tên công ty (*)");
            header.createCell(3).setCellValue("Mã số thuế");
            header.createCell(4).setCellValue("Ngành");
            header.createCell(5).setCellValue("Quy mô");
            header.createCell(6).setCellValue("Website");
            header.createCell(7).setCellValue("Địa chỉ");
            header.createCell(8).setCellValue("Doanh thu");

            // Dòng 1: Hợp lệ đầy đủ
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("1");
            r1.createCell(1).setCellValue("KH-100");
            r1.createCell(2).setCellValue("Công ty Giải pháp Công nghệ Việt");
            r1.createCell(3).setCellValue("0109988776");
            r1.createCell(8).setCellValue("1200000000");

            // Dòng 2: Lỗi thiếu tên công ty (trường bắt buộc)
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("2");
            r2.createCell(1).setCellValue("KH-101");
            r2.createCell(2).setCellValue(""); // Trống tên
            r2.createCell(3).setCellValue("0108877665");

            // Dòng 3: Lỗi mã số thuế không hợp lệ
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("3");
            r3.createCell(1).setCellValue("KH-102");
            r3.createCell(2).setCellValue("Doanh nghiệp Tư nhân Nam Phát");
            r3.createCell(3).setCellValue("ABC"); // Quá ngắn / sai định dạng

            wb.write(out);
            excelBytes = out.toByteArray();
        }

        List<BanGhiNghiepVuDTO> danhSachHienCo = new ArrayList<>();
        List<DongExcelKhachHangDTO> ketQua = ExcelKhachHangUtil.docDanhSachKhachHang(
                new ByteArrayInputStream(excelBytes), danhSachHienCo
        );

        assertEquals(3, ketQua.size(), "Phải đọc đúng 3 dòng dữ liệu");

        // Dòng 1: Hợp lệ
        assertTrue(ketQua.get(0).isHopLe(), "Dòng 1 phải hợp lệ");
        assertEquals("KH-100", ketQua.get(0).getMaKhachHang());

        // Dòng 2: Lỗi thiếu tên
        assertFalse(ketQua.get(1).isHopLe(), "Dòng 2 phải bị báo lỗi do thiếu tên");
        assertTrue(ketQua.get(1).getChuoiLoi().contains("Tên công ty / Khách hàng là trường bắt buộc"));

        // Dòng 3: Lỗi MST
        assertFalse(ketQua.get(2).isHopLe(), "Dòng 3 phải bị báo lỗi do MST không đúng chuẩn");
        assertTrue(ketQua.get(2).getChuoiLoi().contains("Mã số thuế không đúng định dạng"));
    }

    @Test
    @DisplayName("AC 2: Đánh dấu rõ ràng bản ghi trùng lặp để chọn bỏ qua hoặc cập nhật")
    void testDanhDauBanGhiTrungLap() throws Exception {
        byte[] excelBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachKhachHang");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Mã KH");
            header.createCell(2).setCellValue("Tên công ty (*)");
            header.createCell(3).setCellValue("Mã số thuế");

            // Dòng 1: Trùng với khách hàng đã có trong CRM theo mã ban ghi
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("1");
            r1.createCell(1).setCellValue("KH-EXISTING-01");
            r1.createCell(2).setCellValue("Công ty Cổ phần Thương mại Hoàng Kim");
            r1.createCell(3).setCellValue("0107778899");

            // Dòng 2: Khách hàng hoàn toàn mới
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("2");
            r2.createCell(1).setCellValue("KH-NEW-02");
            r2.createCell(2).setCellValue("Công ty TNHH Phần mềm Sao Mai");
            r2.createCell(3).setCellValue("0309991122");

            wb.write(out);
            excelBytes = out.toByteArray();
        }

        // Tạo danh sách khách hàng mẫu đã có trong hệ thống CRM
        List<BanGhiNghiepVuDTO> danhSachHienCo = new ArrayList<>();
        BanGhiNghiepVuDTO khCu = new BanGhiNghiepVuDTO();
        khCu.setId(99L);
        khCu.setMaBanGhi("KH-EXISTING-01");
        khCu.setTieuDe("Công ty Cổ phần Thương mại Hoàng Kim Cũ");
        danhSachHienCo.add(khCu);

        List<DongExcelKhachHangDTO> ketQua = ExcelKhachHangUtil.docDanhSachKhachHang(
                new ByteArrayInputStream(excelBytes), danhSachHienCo
        );

        assertEquals(2, ketQua.size());

        // Dòng 1: Phải được đánh dấu là trùng lặp
        DongExcelKhachHangDTO dongTrung = ketQua.get(0);
        assertTrue(dongTrung.isBiTrung(), "Dòng 1 phải được đánh dấu là bị trùng");
        assertEquals("TRUNG_MA", dongTrung.getLoaiTrung(), "Loại trùng phải là TRUNG_MA");
        assertEquals("BO_QUA", dongTrung.getLuaChonXuLy(), "Mặc định lựa chọn xử lý phải là BO_QUA");
        assertEquals(99L, dongTrung.getIdKhachHangTrung());

        // Dòng 2: Không bị trùng
        DongExcelKhachHangDTO dongMoi = ketQua.get(1);
        assertFalse(dongMoi.isBiTrung(), "Dòng 2 không được bị đánh dấu trùng");
        assertTrue(dongMoi.isHopLe(), "Dòng 2 phải hợp lệ");
    }
}
