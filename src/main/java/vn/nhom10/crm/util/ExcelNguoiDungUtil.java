package vn.nhom10.crm.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.nhom10.crm.dto.DongExcelNguoiDungDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tiện ích tạo tệp mẫu và đọc danh sách người dùng từ tệp Excel (Apache POI 5.3.0).
 * Phục vụ Story S2-01: Nhập danh sách người dùng hàng loạt từ tệp Excel.
 */
public class ExcelNguoiDungUtil {

    private static final String[] TIEU_DE_COT = {
            "STT",
            "Họ và tên (*)",
            "Email công ty (*)",
            "Số điện thoại",
            "Mã vai trò (*)",
            "Mã nhóm kinh doanh",
            "Mật khẩu khởi tạo"
    };

    /**
     * Tạo tệp Excel mẫu (.xlsx) chuẩn nghiệp vụ CRM gồm 2 sheet:
     * - Sheet 1: DanhSachNguoiDung (bảng nhập mẫu)
     * - Sheet 2: HuongDan (quy tắc và danh mục mã vai trò, nhóm kinh doanh)
     *
     * @return byte[] mảng byte tệp Excel .xlsx
     * @throws IOException khi tạo tệp gặp sự cố
     */
    public static byte[] taoTepMauExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Font chữ chuẩn
            Font fontHeader = workbook.createFont();
            fontHeader.setFontName("Segoe UI");
            fontHeader.setFontHeightInPoints((short) 11);
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());

            Font fontNormal = workbook.createFont();
            fontNormal.setFontName("Segoe UI");
            fontNormal.setFontHeightInPoints((short) 10);

            Font fontBold = workbook.createFont();
            fontBold.setFontName("Segoe UI");
            fontBold.setFontHeightInPoints((short) 10);
            fontBold.setBold(true);

            // Style Header bảng
            CellStyle styleHeader = workbook.createCellStyle();
            styleHeader.setFont(fontHeader);
            styleHeader.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            datVienCell(styleHeader);

            // Style Dòng dữ liệu thường
            CellStyle styleData = workbook.createCellStyle();
            styleData.setFont(fontNormal);
            styleData.setVerticalAlignment(VerticalAlignment.CENTER);
            datVienCell(styleData);

            // Style Dòng dữ liệu căn giữa (STT)
            CellStyle styleCenter = workbook.createCellStyle();
            styleCenter.setFont(fontNormal);
            styleCenter.setAlignment(HorizontalAlignment.CENTER);
            styleCenter.setVerticalAlignment(VerticalAlignment.CENTER);
            datVienCell(styleCenter);

            // === SHEET 1: DanhSachNguoiDung ===
            Sheet sheet1 = workbook.createSheet("DanhSachNguoiDung");
            sheet1.setDisplayGridlines(true);

            // Dòng 0: Header
            Row rowHeader = sheet1.createRow(0);
            rowHeader.setHeightInPoints(28);
            for (int i = 0; i < TIEU_DE_COT.length; i++) {
                Cell cell = rowHeader.createCell(i);
                cell.setCellValue(TIEU_DE_COT[i]);
                cell.setCellStyle(styleHeader);
            }

            // Dữ liệu mẫu tham khảo
            String[][] duLieuMau = {
                    {"1", "Nguyễn Văn Hùng", "nguyenvanhung@crm.vn", "0987654321", "SALES_REP", "KD_MIEN_BAC", ""},
                    {"2", "Trần Thị Mai", "tranthimai@crm.vn", "0912345678", "TEAM_LEAD", "KD_MIEN_NAM", ""},
                    {"3", "Lê Hoàng Nam", "lehoangnam@crm.vn", "0905123456", "MARKETING", "PHONG_MKT", ""},
                    {"4", "Phạm Thu Hương", "phamthuhuong@crm.vn", "0934567890", "CUST_SUCCESS", "PHONG_CSKH", "MatKhau@123"},
                    {"5", "Hoàng Văn Tuấn", "hoangvantuan@crm.vn", "0978901234", "ACCOUNTANT", "PHONG_KT", ""}
            };

            for (int r = 0; r < duLieuMau.length; r++) {
                Row row = sheet1.createRow(r + 1);
                row.setHeightInPoints(20);
                for (int c = 0; c < duLieuMau[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(duLieuMau[r][c]);
                    if (c == 0) {
                        cell.setCellStyle(styleCenter);
                    } else {
                        cell.setCellStyle(styleData);
                    }
                }
            }

            // Auto fit độ rộng cột
            for (int i = 0; i < TIEU_DE_COT.length; i++) {
                sheet1.autoSizeColumn(i);
                sheet1.setColumnWidth(i, Math.max(sheet1.getColumnWidth(i) + 1200, 4200));
            }

            // === SHEET 2: HuongDan ===
            taoSheetHuongDan(workbook, fontNormal, fontBold);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static void datVienCell(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static void taoSheetHuongDan(Workbook workbook, Font fontNormal, Font fontBold) {
        Sheet sheet = workbook.createSheet("HuongDan");
        sheet.setDisplayGridlines(true);

        CellStyle styleTieuDeSection = workbook.createCellStyle();
        Font fontTieuDeSection = workbook.createFont();
        fontTieuDeSection.setFontName("Segoe UI");
        fontTieuDeSection.setFontHeightInPoints((short) 11);
        fontTieuDeSection.setBold(true);
        fontTieuDeSection.setColor(IndexedColors.DARK_BLUE.getIndex());
        styleTieuDeSection.setFont(fontTieuDeSection);

        CellStyle styleHeaderBang = workbook.createCellStyle();
        Font fontHeaderBang = workbook.createFont();
        fontHeaderBang.setFontName("Segoe UI");
        fontHeaderBang.setFontHeightInPoints((short) 10);
        fontHeaderBang.setBold(true);
        styleHeaderBang.setFont(fontHeaderBang);
        styleHeaderBang.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleHeaderBang.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        datVienCell(styleHeaderBang);

        CellStyle styleBorder = workbook.createCellStyle();
        styleBorder.setFont(fontNormal);
        datVienCell(styleBorder);

        int r = 0;

        Row rTitleHD = sheet.createRow(r++);
        Cell cHD = rTitleHD.createCell(0);
        cHD.setCellValue("HƯỚNG DẪN ĐIỀN DỮ LIỆU NHẬP NGƯỜI DÙNG TỪ EXCEL");
        cHD.setCellStyle(styleTieuDeSection);

        Row rGhiChu1 = sheet.createRow(r++);
        rGhiChu1.createCell(0).setCellValue("1. Các cột có dấu (*) là bắt buộc: Họ và tên, Email công ty, Mã vai trò.");

        Row rGhiChu2 = sheet.createRow(r++);
        rGhiChu2.createCell(0).setCellValue("2. Cột Mật khẩu khởi tạo: Nếu để trống, hệ thống sẽ tự động tạo mật khẩu tạm ngẫu nhiên mạnh.");

        Row rGhiChu3 = sheet.createRow(r++);
        rGhiChu3.createCell(0).setCellValue("3. Email phải duy nhất trong hệ thống và không trùng lặp giữa các dòng trong cùng tệp Excel.");

        Row rGhiChu4 = sheet.createRow(r++);
        rGhiChu4.createCell(0).setCellValue("4. Người giữ vai trò Trưởng nhóm kinh doanh (TEAM_LEAD) bắt buộc phải gán một Nhóm kinh doanh cụ thể.");

        Row rGhiChu5 = sheet.createRow(r++);
        rGhiChu5.createCell(0).setCellValue("5. Các dòng dữ liệu hợp lệ sẽ được nhập; các dòng lỗi sẽ được hiển thị chi tiết và bỏ qua, không làm gián đoạn cả tệp.");

        r++;
        // Bảng vai trò hợp lệ
        Row rTitleVT = sheet.createRow(r++);
        Cell cVT = rTitleVT.createCell(0);
        cVT.setCellValue("DANH MỤC MÃ VAI TRÒ HỆ THỐNG");
        cVT.setCellStyle(styleTieuDeSection);

        Row rHeaderVT = sheet.createRow(r++);
        Cell cvth1 = rHeaderVT.createCell(0);
        cvth1.setCellValue("Mã vai trò");
        cvth1.setCellStyle(styleHeaderBang);
        Cell cvth2 = rHeaderVT.createCell(1);
        cvth2.setCellValue("Tên vai trò tiếng Việt");
        cvth2.setCellStyle(styleHeaderBang);
        Cell cvth3 = rHeaderVT.createCell(2);
        cvth3.setCellValue("Mô tả trách nhiệm");
        cvth3.setCellStyle(styleHeaderBang);

        String[][] dsVaiTro = {
                {"ADMIN", "Quản trị hệ thống", "Toàn quyền quản trị tài khoản, phân quyền, cấu hình hệ thống"},
                {"DIRECTOR", "Giám đốc kinh doanh", "Xem báo cáo tổng hợp, theo dõi toàn bộ pipeline và doanh số"},
                {"TEAM_LEAD", "Trưởng nhóm kinh doanh", "Quản lý nhóm kinh doanh, duyệt chiết khấu, phân bổ lead"},
                {"SALES_REP", "Nhân viên kinh doanh", "Trực tiếp chăm sóc khách hàng, cơ hội bán hàng và báo giá"},
                {"MARKETING", "Nhân viên Marketing", "Tạo và phân loại lead từ các chiến dịch truyền thông"},
                {"CUST_SUCCESS", "Chăm sóc khách hàng", "Hỗ trợ khách hàng sau bán, gia hạn hợp đồng và ticket"},
                {"ACCOUNTANT", "Kế toán", "Quản lý công nợ, hợp đồng và hóa đơn thanh toán"}
        };

        for (String[] vt : dsVaiTro) {
            Row row = sheet.createRow(r++);
            for (int col = 0; col < 3; col++) {
                Cell cell = row.createCell(col);
                cell.setCellValue(vt[col]);
                cell.setCellStyle(styleBorder);
            }
        }

        r++;
        // Bảng nhóm kinh doanh tham khảo
        Row rTitleNhom = sheet.createRow(r++);
        Cell cNhom = rTitleNhom.createCell(0);
        cNhom.setCellValue("DANH MỤC MÃ NHÓM KINH DOANH THAM KHẢO");
        cNhom.setCellStyle(styleTieuDeSection);

        Row rHeaderNhom = sheet.createRow(r++);
        Cell cnh1 = rHeaderNhom.createCell(0);
        cnh1.setCellValue("Mã nhóm kinh doanh");
        cnh1.setCellStyle(styleHeaderBang);
        Cell cnh2 = rHeaderNhom.createCell(1);
        cnh2.setCellValue("Tên nhóm kinh doanh");
        cnh2.setCellStyle(styleHeaderBang);

        String[][] dsNhom = {
                {"KHOI_KD", "Khối Kinh Doanh Tổng"},
                {"KD_MIEN_BAC", "Nhóm Kinh Doanh Miền Bắc"},
                {"KD_MIEN_NAM", "Nhóm Kinh Doanh Miền Nam"},
                {"PHONG_MKT", "Phòng Marketing"},
                {"PHONG_CSKH", "Phòng Chăm Sóc Khách Hàng"},
                {"PHONG_KT", "Phòng Tài Chính Kế Toán"}
        };

        for (String[] nhom : dsNhom) {
            Row row = sheet.createRow(r++);
            for (int col = 0; col < 2; col++) {
                Cell cell = row.createCell(col);
                cell.setCellValue(nhom[col]);
                cell.setCellStyle(styleBorder);
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

    /**
     * Đọc các dòng dữ liệu người dùng từ tệp Excel (.xlsx hoặc .xls).
     *
     * @param is luồng dữ liệu tệp Excel tải lên
     * @return danh sách các dòng dữ liệu đọc được kèm số dòng Excel
     * @throws Exception khi tệp hỏng hoặc không đúng định dạng Excel
     */
    public static List<DongExcelNguoiDungDTO> docDanhSachTuExcel(InputStream is) throws Exception {
        List<DongExcelNguoiDungDTO> danhSach = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (workbook.getNumberOfSheets() == 0) {
                return danhSach;
            }

            // Ưu tiên sheet "DanhSachNguoiDung" hoặc sheet đầu tiên
            Sheet sheet = workbook.getSheet("DanhSachNguoiDung");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            if (firstRowNum < 0 || lastRowNum < 0) {
                return danhSach;
            }

            // Tìm dòng tiêu đề (chứa "Họ và tên" hoặc "Email" hoặc dòng đầu tiên)
            int rowHeaderIdx = firstRowNum;
            for (int i = firstRowNum; i <= Math.min(firstRowNum + 5, lastRowNum); i++) {
                Row r = sheet.getRow(i);
                if (r != null) {
                    String rowText = layNoiDungDong(r, formatter).toLowerCase();
                    if (rowText.contains("họ và tên") || rowText.contains("email") || rowText.contains("vai trò")) {
                        rowHeaderIdx = i;
                        break;
                    }
                }
            }

            // Đọc từ dòng kế tiếp sau dòng tiêu đề
            for (int rIdx = rowHeaderIdx + 1; rIdx <= lastRowNum; rIdx++) {
                Row row = sheet.getRow(rIdx);
                if (row == null) {
                    continue;
                }

                if (dongRong(row, formatter)) {
                    continue;
                }

                // Cột thứ tự: STT (0), HoTen (1), Email (2), SDT (3), VaiTro (4), Nhom (5), MatKhau (6)
                String hoTen = layGiaTriO(row, 1, formatter);
                String email = layGiaTriO(row, 2, formatter);
                String soDienThoai = layGiaTriO(row, 3, formatter);
                String vaiTroNhap = layGiaTriO(row, 4, formatter);
                String nhomNhap = layGiaTriO(row, 5, formatter);
                String matKhau = layGiaTriO(row, 6, formatter);

                DongExcelNguoiDungDTO dongDTO = new DongExcelNguoiDungDTO();
                dongDTO.setSoDong(rIdx + 1); // 1-indexed theo số dòng của Excel
                dongDTO.setHoTen(hoTen);
                dongDTO.setEmail(email);
                dongDTO.setSoDienThoai(soDienThoai);
                dongDTO.setVaiTroNhap(vaiTroNhap);
                dongDTO.setNhomNhap(nhomNhap);
                dongDTO.setMatKhau(matKhau);

                danhSach.add(dongDTO);
            }
        }

        return danhSach;
    }

    private static String layGiaTriO(Row row, int colIdx, DataFormatter formatter) {
        Cell cell = row.getCell(colIdx);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static boolean dongRong(Row row, DataFormatter formatter) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = formatter.formatCellValue(cell).trim();
                if (!val.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String layNoiDungDong(Row row, DataFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null) {
                sb.append(formatter.formatCellValue(cell)).append(" ");
            }
        }
        return sb.toString();
    }
}
