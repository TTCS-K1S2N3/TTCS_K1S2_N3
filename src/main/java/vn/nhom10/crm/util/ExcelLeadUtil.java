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
    /**
     * Tạo tệp mẫu Excel (.xlsx) chuẩn nghiệp vụ CRM không kèm danh sách nguồn động.
     */
    public static byte[] taoTepMauExcel() throws IOException {
        return taoTepMauExcel(null);
    }

    /**
     * Tạo tệp mẫu Excel (.xlsx) chuẩn nghiệp vụ CRM gồm 2 sheet:
     * - Sheet 1: DanhSachLead (bảng nhập mẫu kèm 3 dòng dữ liệu mẫu sử dụng nguồn từ CSDL)
     * - Sheet 2: HuongDan_QuyTac (hướng dẫn quy tắc cột bắt buộc và AC3 theo danh mục nguồn thực tế)
     */
    public static byte[] taoTepMauExcel(List<MucDanhMucDTO> dsNguonLead) throws IOException {
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

            // Lấy danh sách nguồn hợp lệ thực tế từ CSDL
            List<String> tenNguonHopLe = new ArrayList<>();
            if (dsNguonLead != null) {
                for (MucDanhMucDTO dm : dsNguonLead) {
                    if (dm != null && dm.isKichHoat() && dm.getTenMuc() != null && !dm.getTenMuc().isBlank()) {
                        tenNguonHopLe.add(dm.getTenMuc().trim());
                    }
                }
            }

            // Nếu có danh mục thực tế truyền vào từ CSDL thì sử dụng danh mục CSDL; nếu không thì dùng mẫu chuẩn
            String nguon1;
            String nguon2;
            String nguon3;
            String huongDanNguon;

            if (!tenNguonHopLe.isEmpty()) {
                nguon1 = tenNguonHopLe.get(0);
                nguon2 = tenNguonHopLe.size() > 1 ? tenNguonHopLe.get(1) : nguon1;
                nguon3 = tenNguonHopLe.size() > 2 ? tenNguonHopLe.get(2) : nguon2;
                huongDanNguon = "QUY TẮC BẮT BUỘC (AC3): Mỗi lead nhập vào bắt buộc phải có Nguồn. Các giá trị hợp lệ theo danh mục hiện hành: "
                        + String.join(", ", tenNguonHopLe) + ".";
            } else {
                nguon1 = "Sự kiện / Hội thảo";
                nguon2 = "Danh thiếp đối tác";
                nguon3 = "Khách hàng giới thiệu";
                huongDanNguon = "QUY TẮC BẮT BUỘC (AC3): Mỗi lead nhập vào bắt buộc phải có Nguồn. Các giá trị hợp lệ: Sự kiện / Hội thảo, Danh thiếp đối tác, Khách hàng giới thiệu, Website / Biểu mẫu trực tuyến, Gọi điện thoại trực tiếp, Nguồn tiếp cận khác.";
            }

            // Dữ liệu mẫu (3 dòng)
            String[][] duLieuMau = {
                    {"1", "Nguyễn Văn An", nguon1, "0912345678", "an.nguyen@congty.vn", "Công ty Cổ phần Công nghệ Alpha", "CAO", "Tư vấn phần mềm quản trị quan hệ khách hàng CRM"},
                    {"2", "Trần Thị Bình", nguon2, "0987654321", "binh.tran@doanhnghiep.vn", "Tập đoàn Bán lẻ Beta", "TRUNG_BINH", "Tìm hiểu giải pháp quản lý bán hàng đa kênh"},
                    {"3", "Lê Hoàng Cường", nguon3, "0903112233", "cuong.le@startup.vn", "Công ty TNHH Dịch vụ Gamma", "THAP", "Yêu cầu gửi tài liệu giới thiệu giải pháp"}
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
                    {"Nguồn Lead (*) [AC3]", "CÓ", huongDanNguon},
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

            // Nhận diện linh hoạt thứ tự các cột từ dòng tiêu đề
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Tệp Excel không đúng cấu trúc: Thiếu dòng tiêu đề cột.");
            }

            int colHoTen = -1;
            int colNguon = -1;
            int colSdt = -1;
            int colEmail = -1;
            int colCongTy = -1;
            int colMucDo = -1;
            int colNhuCau = -1;

            int lastCellNum = headerRow.getLastCellNum();
            for (int c = 0; c < lastCellNum; c++) {
                String headerVal = layGiaTriChuoi(headerRow.getCell(c), formatter).toLowerCase();
                if (headerVal.isEmpty()) continue;

                if (colHoTen == -1 && (headerVal.contains("họ và tên") || headerVal.contains("họ tên") || headerVal.contains("ho ten") || headerVal.contains("fullname") || headerVal.contains("full name"))) {
                    colHoTen = c;
                } else if (colNguon == -1 && (headerVal.contains("nguồn") || headerVal.contains("nguon") || headerVal.contains("source"))) {
                    colNguon = c;
                } else if (colSdt == -1 && (headerVal.contains("điện thoại") || headerVal.contains("dien thoai") || headerVal.contains("sđt") || headerVal.contains("sdt") || headerVal.contains("phone"))) {
                    colSdt = c;
                } else if (colEmail == -1 && (headerVal.contains("email") || headerVal.contains("thư điện tử") || headerVal.contains("mail"))) {
                    colEmail = c;
                } else if (colCongTy == -1 && (headerVal.contains("công ty") || headerVal.contains("cong ty") || headerVal.contains("doanh nghiệp") || headerVal.contains("company"))) {
                    colCongTy = c;
                } else if (colMucDo == -1 && (headerVal.contains("mức độ") || headerVal.contains("muc do") || headerVal.contains("quan tâm") || headerVal.contains("priority"))) {
                    colMucDo = c;
                } else if (colNhuCau == -1 && (headerVal.contains("nhu cầu") || headerVal.contains("nhu cau") || headerVal.contains("ghi chú") || headerVal.contains("note"))) {
                    colNhuCau = c;
                }
            }

            // Kiểm tra cấu trúc mẫu: Bắt buộc phải nhận diện được cột Họ tên và Nguồn Lead
            if (colHoTen == -1 || colNguon == -1) {
                throw new IllegalArgumentException("Tệp Excel không đúng cấu trúc mẫu nhập Lead. Yêu cầu tệp phải có các cột: 'Họ và tên' và 'Nguồn Lead'.");
            }

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                // Đọc các ô theo đúng vị trí cột nhận diện được từ tiêu đề
                String valHoTen = colHoTen >= 0 ? layGiaTriChuoi(row.getCell(colHoTen), formatter) : "";
                String valNguon = colNguon >= 0 ? layGiaTriChuoi(row.getCell(colNguon), formatter) : "";
                String valSdt = colSdt >= 0 ? layGiaTriChuoi(row.getCell(colSdt), formatter) : "";
                String valEmail = colEmail >= 0 ? layGiaTriChuoi(row.getCell(colEmail), formatter) : "";
                String valCongTy = colCongTy >= 0 ? layGiaTriChuoi(row.getCell(colCongTy), formatter) : "";
                String valMucDo = colMucDo >= 0 ? layGiaTriChuoi(row.getCell(colMucDo), formatter) : "";
                String valNhuCau = colNhuCau >= 0 ? layGiaTriChuoi(row.getCell(colNhuCau), formatter) : "";

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
     * Chỉ chấp nhận nguồn thực tế tồn tại và kích hoạt trong CSDL (dsNguonLead).
     * Tuyệt đối không fallback sang ID giả định.
     */
    public static Long timIdNguonLead(String tenNguon, List<MucDanhMucDTO> dsNguonLead) {
        if (tenNguon == null || tenNguon.trim().isEmpty() || dsNguonLead == null || dsNguonLead.isEmpty()) {
            return null;
        }

        String search = tenNguon.trim();

        // 1. Đối chiếu theo ID số nếu người dùng điền trực tiếp ID nguồn
        try {
            long idNum = Long.parseLong(search);
            for (MucDanhMucDTO dm : dsNguonLead) {
                if (dm != null && dm.getId() != null && dm.getId() == idNum && dm.isKichHoat()) {
                    return dm.getId();
                }
            }
        } catch (NumberFormatException ignored) {
        }

        // 2. Khớp chính xác tên mục hoặc mã mục (không phân biệt hoa/thường)
        for (MucDanhMucDTO dm : dsNguonLead) {
            if (dm == null || !dm.isKichHoat() || dm.getId() == null) {
                continue;
            }
            if (dm.getTenMuc() != null && dm.getTenMuc().trim().equalsIgnoreCase(search)) {
                return dm.getId();
            }
            if (dm.getMaMuc() != null && dm.getMaMuc().trim().equalsIgnoreCase(search)) {
                return dm.getId();
            }
        }

        // 3. Khớp chuẩn hóa đối với tên có dấu gạch chéo '/' (ví dụ: "Website / Biểu mẫu")
        String searchNorm = search.toLowerCase();
        for (MucDanhMucDTO dm : dsNguonLead) {
            if (dm == null || !dm.isKichHoat() || dm.getId() == null) {
                continue;
            }
            String tenNorm = dm.getTenMuc() != null ? dm.getTenMuc().trim().toLowerCase() : "";
            if (tenNorm.isEmpty()) continue;

            if (tenNorm.contains("/")) {
                String[] parts = tenNorm.split("/");
                for (String part : parts) {
                    if (part.trim().equalsIgnoreCase(searchNorm)) {
                        return dm.getId();
                    }
                }
            }
            if (searchNorm.contains("/")) {
                String[] parts = searchNorm.split("/");
                for (String part : parts) {
                    if (part.trim().equalsIgnoreCase(tenNorm)) {
                        return dm.getId();
                    }
                }
            }
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
