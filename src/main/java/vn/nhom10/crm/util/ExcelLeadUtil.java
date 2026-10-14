package vn.nhom10.crm.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.nhom10.crm.dto.DongExcelLeadDTO;
import vn.nhom10.crm.dto.MucDanhMucDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tiện ích tạo tệp mẫu và phân tích dữ liệu nhập Lead từ tệp Excel (Apache POI).
 * Phục vụ Story S4-02:
 * - AC2: Cung cấp tệp mẫu chuẩn, xem trước và báo lỗi theo từng dòng
 * - AC3: Bắt buộc mọi lead nhập vào phải có nguồn hợp lệ
 */
public class ExcelLeadUtil {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    public static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84|84)?[0-9]{8,15}$");

    private static final String[] TIEU_DE_COT = {
            "STT",
            "Họ và tên (*)",
            "Nguồn Lead (*) [AC3]",
            "Số điện thoại",
            "Email",
            "Tên công ty / Doanh nghiệp",
            "Mức độ quan tâm (CAO/TRUNG_BINH/THAP)",
            "Nhu cầu quan tâm"
    };

    /**
     * Tạo tệp mẫu Excel (.xlsx) chuẩn nghiệp vụ CRM gồm 2 sheet:
     * - Sheet 1: DanhSachLead (bảng nhập mẫu kèm 3 dòng dữ liệu mẫu)
     * - Sheet 2: HuongDan_QuyTac (hướng dẫn quy tắc cột bắt buộc và AC3)
     */
    public static byte[] taoTepMauExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Font chữ giao diện
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
            styleHeader.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            datVienCell(styleHeader);

            // Style dữ liệu thường
            CellStyle styleData = workbook.createCellStyle();
            styleData.setFont(fontNormal);
            styleData.setVerticalAlignment(VerticalAlignment.CENTER);
            datVienCell(styleData);

            // Style dữ liệu căn giữa
            CellStyle styleDataCenter = workbook.createCellStyle();
            styleDataCenter.cloneStyleFrom(styleData);
            styleDataCenter.setAlignment(HorizontalAlignment.CENTER);

            // ================= SHEET 1: DANH SÁCH LEAD MẪU =================
            Sheet sheetData = workbook.createSheet("DanhSachLead");
            sheetData.setDisplayGridlines(true);

            // Header row
            Row headerRow = sheetData.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < TIEU_DE_COT.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(TIEU_DE_COT[i]);
                cell.setCellStyle(styleHeader);
            }

            // Dữ liệu mẫu (3 dòng)
            String[][] duLieuMau = {
                    {"1", "Nguyễn Văn An", "Sự kiện / Hội thảo", "0912345678", "an.nguyen@congty.vn", "Công ty Cổ phần Công nghệ Alpha", "CAO", "Tư vấn phần mềm quản trị quan hệ khách hàng CRM"},
                    {"2", "Trần Thị Bình", "Danh thiếp đối tác", "0987654321", "binh.tran@doanhnghiep.vn", "Tập đoàn Bán lẻ Beta", "TRUNG_BINH", "Tìm hiểu giải pháp quản lý bán hàng đa kênh"},
                    {"3", "Lê Hoàng Cường", "Khách hàng giới thiệu", "0903112233", "cuong.le@startup.vn", "Công ty TNHH Dịch vụ Gamma", "THAP", "Yêu cầu gửi tài liệu giới thiệu giải pháp"}
            };

            for (int r = 0; r < duLieuMau.length; r++) {
                Row row = sheetData.createRow(r + 1);
                row.setHeightInPoints(22);
                for (int c = 0; c < duLieuMau[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(duLieuMau[r][c]);
                    if (c == 0 || c == 3 || c == 6) {
                        cell.setCellStyle(styleDataCenter);
                    } else {
                        cell.setCellStyle(styleData);
                    }
                }
            }

            // Tự động căn chỉnh độ rộng cột
            sheetData.setColumnWidth(0, 8 * 256);   // STT
            sheetData.setColumnWidth(1, 24 * 256);  // Họ tên
            sheetData.setColumnWidth(2, 26 * 256);  // Nguồn Lead
            sheetData.setColumnWidth(3, 16 * 256);  // SĐT
            sheetData.setColumnWidth(4, 28 * 256);  // Email
            sheetData.setColumnWidth(5, 36 * 256);  // Công ty
            sheetData.setColumnWidth(6, 22 * 256);  // Mức độ
            sheetData.setColumnWidth(7, 45 * 256);  // Nhu cầu

            // ================= SHEET 2: HƯỚNG DẪN QUY TẮC =================
            Sheet sheetGuide = workbook.createSheet("HuongDan_QuyTac");
            sheetGuide.setDisplayGridlines(true);

            Row r0 = sheetGuide.createRow(0);
            r0.setHeightInPoints(26);
            Cell cTitle = r0.createCell(0);
            cTitle.setCellValue("QUY TẮC NHẬP DỮ LIỆU LEAD TỪ BẢNG TÍNH EXCEL (STORY S4-02)");
            CellStyle styleTitle = workbook.createCellStyle();
            styleTitle.setFont(fontBold);
            cTitle.setCellStyle(styleTitle);

            String[][] huongDan = {
                    {"Cột", "Bắt buộc", "Quy tắc thẩm định chi tiết"},
                    {"Họ và tên (*)", "CÓ", "Tối đa 150 ký tự, hỗ trợ tiếng Việt có dấu. Không được để trống."},
                    {"Nguồn Lead (*) [AC3]", "CÓ", "QUY TẮC BẮT BUỘC (AC3): Mỗi lead nhập vào bắt buộc phải có Nguồn. Các giá trị hợp lệ: Sự kiện / Hội thảo, Danh thiếp đối tác, Khách hàng giới thiệu, Website / Biểu mẫu trực tuyến, Gọi điện thoại trực tiếp, Nguồn tiếp cận khác."},
                    {"Số điện thoại", "KHÔNG", "Định dạng số điện thoại Việt Nam (8-15 chữ số)."},
                    {"Email", "KHÔNG", "Địa chỉ email hợp lệ theo chuẩn RFC (ví dụ: hoten@congty.vn)."},
                    {"Tên công ty", "KHÔNG", "Tên doanh nghiệp / cơ quan của khách hàng tiềm năng, tối đa 255 ký tự."},
                    {"Mức độ quan tâm", "KHÔNG", "Nhận các giá trị: CAO (Nhu cầu cao), TRUNG_BINH (Trung bình), THAP (Thấp). Mặc định là TRUNG_BINH."},
                    {"Nhu cầu quan tâm", "KHÔNG", "Ghi chú tóm tắt nhu cầu ban đầu của khách hàng qua trao đổi."}
            };

            for (int r = 0; r < huongDan.length; r++) {
                Row row = sheetGuide.createRow(r + 2);
                row.setHeightInPoints(22);
                for (int c = 0; c < huongDan[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(huongDan[r][c]);
                    if (r == 0) {
                        cell.setCellStyle(styleHeader);
                    } else if (c == 1) {
                        cell.setCellStyle(styleDataCenter);
                    } else {
                        cell.setCellStyle(styleData);
                    }
                }
            }

            sheetGuide.setColumnWidth(0, 26 * 256);
            sheetGuide.setColumnWidth(1, 14 * 256);
            sheetGuide.setColumnWidth(2, 85 * 256);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Đọc và thẩm định danh sách Lead từ tệp Excel tải lên.
     * Báo lỗi chi tiết gắn với từng số dòng (AC2) và kiểm tra bắt buộc nguồn (AC3).
     */
    public static List<DongExcelLeadDTO> docDanhSachLead(InputStream is, List<MucDanhMucDTO> dsNguonLead) throws IOException {
        List<DongExcelLeadDTO> danhSach = new ArrayList<>();
        if (is == null) {
            return danhSach;
        }

        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (workbook.getNumberOfSheets() == 0) {
                return danhSach;
            }

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 1) {
                return danhSach; // Bảng chỉ có header hoặc rỗng
            }

            DataFormatter formatter = new DataFormatter();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                // Đọc các ô theo thứ tự cột chuẩn
                String valHoTen = layGiaTriChuoi(row.getCell(1), formatter);
                String valNguon = layGiaTriChuoi(row.getCell(2), formatter);
                String valSdt = layGiaTriChuoi(row.getCell(3), formatter);
                String valEmail = layGiaTriChuoi(row.getCell(4), formatter);
                String valCongTy = layGiaTriChuoi(row.getCell(5), formatter);
                String valMucDo = layGiaTriChuoi(row.getCell(6), formatter);
                String valNhuCau = layGiaTriChuoi(row.getCell(7), formatter);

                // Nếu tất cả các ô trong dòng đều rỗng -> bỏ qua dòng trống
                if (valHoTen.isEmpty() && valNguon.isEmpty() && valSdt.isEmpty() &&
                        valEmail.isEmpty() && valCongTy.isEmpty() && valMucDo.isEmpty() && valNhuCau.isEmpty()) {
                    continue;
                }

                DongExcelLeadDTO dong = new DongExcelLeadDTO(r + 1); // Số dòng thực tế trong Excel (1-based)
                dong.setHoTen(valHoTen);
                dong.setTenNguonLead(valNguon);
                dong.setSoDienThoai(valSdt.isEmpty() ? null : valSdt);
                dong.setEmail(valEmail.isEmpty() ? null : valEmail);
                dong.setTenCongTy(valCongTy.isEmpty() ? null : valCongTy);
                dong.setNhuCauQuanTam(valNhuCau.isEmpty() ? null : valNhuCau);

                // Chuẩn hóa mức độ quan tâm
                if ("CAO".equalsIgnoreCase(valMucDo) || "Cao".equalsIgnoreCase(valMucDo)) {
                    dong.setMucDoQuanTam("CAO");
                } else if ("THAP".equalsIgnoreCase(valMucDo) || "Thấp".equalsIgnoreCase(valMucDo)) {
                    dong.setMucDoQuanTam("THAP");
                } else {
                    dong.setMucDoQuanTam("TRUNG_BINH");
                }

                // ================= KIỂM TRA THẨM ĐỊNH LỖI (AC2, AC3) =================

                // 1. Thẩm định Họ và tên (Bắt buộc)
                if (valHoTen.isEmpty()) {
                    dong.themLoi("Họ và tên khách hàng không được để trống.");
                } else if (valHoTen.length() > 150) {
                    dong.themLoi("Họ và tên vượt quá 150 ký tự cho phép.");
                }

                // 2. Thẩm định Nguồn Lead (BẮT BUỘC THEO AC3)
                if (valNguon.isEmpty()) {
                    dong.themLoi("Mọi lead nhập vào đều bắt buộc có nguồn (Tiêu chí AC3). Cột Nguồn Lead không được để trống.");
                } else {
                    // Ánh xạ nguồn lead sang ID nguồn trong CSDL
                    Long nguonId = timIdNguonLead(valNguon, dsNguonLead);
                    if (nguonId != null) {
                        dong.setNguonLeadId(nguonId);
                    } else {
                        // Nguồn không nhận diện được trong danh mục
                        dong.themLoi("Nguồn Lead '" + valNguon + "' không tồn tại trong danh mục nguồn được phép của hệ thống.");
                    }
                }

                // 3. Thẩm định Email (Nếu có nhập thì phải hợp lệ)
                if (!valEmail.isEmpty()) {
                    if (valEmail.length() > 255) {
                        dong.themLoi("Địa chỉ email vượt quá 255 ký tự.");
                    } else if (!EMAIL_PATTERN.matcher(valEmail).matches()) {
                        dong.themLoi("Địa chỉ email không đúng định dạng chuẩn (ví dụ: ten@domain.vn).");
                    }
                }

                // 4. Thẩm định Số điện thoại (Nếu có nhập thì phải hợp lệ)
                if (!valSdt.isEmpty()) {
                    String cleanPhone = valSdt.replaceAll("[\\s.-]", "");
                    if (cleanPhone.length() > 20) {
                        dong.themLoi("Số điện thoại vượt quá 20 ký tự.");
                    } else if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                        dong.themLoi("Số điện thoại không hợp lệ (từ 8 đến 15 chữ số).");
                    } else {
                        dong.setSoDienThoai(cleanPhone);
                    }
                }

                // 5. Thẩm định Tên công ty (Tối đa 255 ký tự)
                if (!valCongTy.isEmpty() && valCongTy.length() > 255) {
                    dong.themLoi("Tên công ty vượt quá 255 ký tự cho phép.");
                }

                danhSach.add(dong);
            }
        }

        return danhSach;
    }

    /**
     * Tìm ID nguồn lead khớp với tên hoặc mã được ghi trong tệp Excel.
     */
    public static Long timIdNguonLead(String tenNguon, List<MucDanhMucDTO> dsNguonLead) {
        if (tenNguon == null || tenNguon.trim().isEmpty()) {
            return null;
        }

        String search = tenNguon.trim().toLowerCase();

        // 1. Thử đối chiếu trực tiếp với danh sách cấu hình từ CSDL
        if (dsNguonLead != null && !dsNguonLead.isEmpty()) {
            // Khớp theo ID nếu người dùng điền số ID
            try {
                long idNum = Long.parseLong(search);
                for (MucDanhMucDTO dm : dsNguonLead) {
                    if (dm.getId() != null && dm.getId() == idNum) {
                        return dm.getId();
                    }
                }
            } catch (NumberFormatException ignored) {
            }

            // Khớp chính xác tên mục hoặc mã mục
            for (MucDanhMucDTO dm : dsNguonLead) {
                if (dm.getTenMuc() != null && dm.getTenMuc().trim().equalsIgnoreCase(tenNguon.trim())) {
                    return dm.getId();
                }
                if (dm.getMaMuc() != null && dm.getMaMuc().trim().equalsIgnoreCase(tenNguon.trim())) {
                    return dm.getId();
                }
            }

            // Khớp gần đúng (chứa từ khóa)
            for (MucDanhMucDTO dm : dsNguonLead) {
                String tenLower = dm.getTenMuc() != null ? dm.getTenMuc().toLowerCase() : "";
                if (!tenLower.isEmpty() && (tenLower.contains(search) || search.contains(tenLower))) {
                    return dm.getId();
                }
            }
        }

        // 2. Fallback nhận diện theo các nguồn chuẩn của hệ thống CRM
        if (search.contains("sự kiện") || search.contains("su kien") || search.contains("hội thảo") || search.contains("hoi thao") || search.contains("event")) {
            return 1L;
        }
        if (search.contains("danh thiếp") || search.contains("danh thiep") || search.contains("namecard") || search.contains("card")) {
            return 2L;
        }
        if (search.contains("giới thiệu") || search.contains("gioi thieu") || search.contains("referral")) {
            return 3L;
        }
        if (search.contains("website") || search.contains("web") || search.contains("biểu mẫu") || search.contains("bieu mau") || search.contains("form")) {
            return 4L;
        }
        if (search.contains("điện thoại") || search.contains("dien thoai") || search.contains("gọi") || search.contains("call")) {
            return 5L;
        }
        if (search.contains("khác") || search.contains("khac") || search.contains("other")) {
            return 6L;
        }

        // Nếu danh sách từ CSDL rỗng và người dùng nhập ID hợp lệ
        try {
            long id = Long.parseLong(search);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    private static String layGiaTriChuoi(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        String val = formatter.formatCellValue(cell);
        return val != null ? val.trim() : "";
    }

    private static void datVienCell(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }
}
