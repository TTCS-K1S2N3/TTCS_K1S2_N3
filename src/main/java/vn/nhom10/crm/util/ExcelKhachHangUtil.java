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
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.DongExcelKhachHangDTO;
import vn.nhom10.crm.model.KhachHang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tiện ích tạo tệp mẫu và đọc danh sách khách hàng từ tệp Excel (Apache POI).
 * Phục vụ Story S3-06:
 * - Tải tệp mẫu Excel chuẩn cho Khách hàng
 * - Thẩm định và báo lỗi theo từng dòng
 * - Đánh dấu các bản ghi trùng lặp (Mã số thuế, Mã KH, Tên công ty)
 */
public class ExcelKhachHangUtil {

    private static final String[] TIEU_DE_COT = {
            "STT",
            "Mã khách hàng",
            "Tên công ty / Khách hàng (*)",
            "Mã số thuế",
            "Ngành nghề",
            "Quy mô",
            "Website",
            "Địa chỉ",
            "Doanh thu ước tính (VNĐ)",
            "Trạng thái",
            "Ghi chú / Mô tả chi tiết"
    };

    /**
     * Tạo tệp mẫu Excel (.xlsx) chuẩn nghiệp vụ CRM gồm 2 sheet:
     * - Sheet 1: DanhSachKhachHang (bảng nhập mẫu kèm 3 dòng dữ liệu mẫu)
     * - Sheet 2: HuongDan_QuyTac (hướng dẫn cột bắt buộc và quy tắc xử lý trùng lặp)
     */
    public static byte[] taoTepMauExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Font chữ Segoe UI
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

            // Style Dòng dữ liệu thường
            CellStyle styleData = workbook.createCellStyle();
            styleData.setFont(fontNormal);
            styleData.setVerticalAlignment(VerticalAlignment.CENTER);
            datVienCell(styleData);

            // Style Dòng dữ liệu căn giữa
            CellStyle styleDataCenter = workbook.createCellStyle();
            styleDataCenter.cloneStyleFrom(styleData);
            styleDataCenter.setAlignment(HorizontalAlignment.CENTER);

            // Style Dòng dữ liệu căn phải (tiền tệ / doanh thu)
            CellStyle styleDataRight = workbook.createCellStyle();
            styleDataRight.cloneStyleFrom(styleData);
            styleDataRight.setAlignment(HorizontalAlignment.RIGHT);

            // ================== SHEET 1: DanhSachKhachHang ==================
            Sheet sheet1 = workbook.createSheet("DanhSachKhachHang");
            sheet1.setDisplayGridlines(true);

            // Row 0: Tiêu đề cột
            Row rowHeader = sheet1.createRow(0);
            rowHeader.setHeightInPoints(28);
            for (int i = 0; i < TIEU_DE_COT.length; i++) {
                Cell cell = rowHeader.createCell(i);
                cell.setCellValue(TIEU_DE_COT[i]);
                cell.setCellStyle(styleHeader);
            }

            // Dữ liệu mẫu (3 dòng để hướng dẫn người dùng nhập đúng)
            String[][] mauDuLieu = {
                    {"1", "KH-001", "Công ty Cổ phần Công nghệ ABC", "0108998877", "Công nghệ thông tin", "50-200 người", "https://abctech.vn", "Tòa nhà Keangnam, Nam Từ Liêm, Hà Nội", "1500000000", "TIEM_NANG", "Đang có nhu cầu triển khai phần mềm CRM cho 50 nhân viên"},
                    {"2", "KH-002", "Tập đoàn Đầu tư & Xây dựng Toàn Cầu", "0309112233", "Xây dựng & Bất động sản", "Trên 500 người", "https://toancaucorp.com", "Quận 1, TP. Hồ Chí Minh", "5000000000", "DANG_CHAM_SOC", "Khách hàng VIP, đàm phán hợp đồng cung ứng giai đoạn 1"},
                    {"3", "KH-003", "Công ty TNHH Thương mại Quốc tế Minh Châu", "0401234567", "Thương mại & Bán lẻ", "20-50 người", "https://minhchau.vn", "Hải An, TP. Hải Phòng", "800000000", "TIEM_NANG", "Giới thiệu qua hội chợ thương mại"}
            };

            for (int r = 0; r < mauDuLieu.length; r++) {
                Row row = sheet1.createRow(r + 1);
                row.setHeightInPoints(22);
                for (int c = 0; c < mauDuLieu[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(mauDuLieu[r][c]);
                    if (c == 0) {
                        cell.setCellStyle(styleDataCenter);
                    } else if (c == 8) {
                        cell.setCellStyle(styleDataRight);
                    } else {
                        cell.setCellStyle(styleData);
                    }
                }
            }

            // Tự động căn chỉnh độ rộng cột
            int[] columnWidths = {2000, 4200, 10500, 4500, 6500, 4500, 7000, 11000, 6500, 4500, 12000};
            for (int i = 0; i < columnWidths.length; i++) {
                sheet1.setColumnWidth(i, columnWidths[i]);
            }

            // ================== SHEET 2: HuongDan_QuyTac ==================
            Sheet sheet2 = workbook.createSheet("HuongDan_QuyTac");
            sheet2.setDisplayGridlines(true);

            String[][] huongDan = {
                    {"HƯỚNG DẪN QUY TẮC NHẬP DANH SÁCH KHÁCH HÀNG TỪ EXCEL (STORY S3-06)", ""},
                    {"", ""},
                    {"1. Các trường dữ liệu bắt buộc:", "Cột 'Tên công ty / Khách hàng (*)' là trường BẮT BUỘC. Dòng nào để trống tên công ty sẽ bị báo lỗi."},
                    {"2. Kiểm tra và phát hiện trùng lặp:", "Hệ thống tự động đối chiếu Mã số thuế, Mã khách hàng và Tên công ty với dữ liệu hiện có trong CRM và giữa các dòng trong tệp."},
                    {"3. Tùy chọn xử lý bản ghi trùng:", "Khi xem trước, bản ghi trùng sẽ được đánh dấu rõ ràng. Bạn có thể chọn:"},
                    {"", "   • [Bỏ qua]: Giữ nguyên dữ liệu cũ trong CRM, không ghi đè."},
                    {"", "   • [Cập nhật]: Cập nhật thông tin mới nhất từ tệp Excel vào khách hàng hiện có."},
                    {"4. Định dạng doanh thu ước tính:", "Nhập dạng số nguyên hoặc số thập phân (ví dụ: 1500000000), không chèn ký tự chữ."},
                    {"5. Trạng thái khách hàng:", "TIEM_NANG (Tiềm năng), DANG_CHAM_SOC (Đang chăm sóc), DA_KY_HOP_DONG (Đã ký hợp đồng), NGUNG_GIAO_DICH (Ngừng giao dịch)."},
                    {"6. Định dạng tệp hỗ trợ:", "Hỗ trợ tệp Microsoft Excel định dạng .xlsx hoặc .xls, dung lượng tối đa 10MB."}
            };

            for (int i = 0; i < huongDan.length; i++) {
                Row r = sheet2.createRow(i);
                Cell c0 = r.createCell(0);
                Cell c1 = r.createCell(1);
                c0.setCellValue(huongDan[i][0]);
                c1.setCellValue(huongDan[i][1]);
                if (i == 0) {
                    c0.setCellStyle(styleHeader);
                } else if (huongDan[i][0].startsWith("1.") || huongDan[i][0].startsWith("2.") || huongDan[i][0].startsWith("3.") || huongDan[i][0].startsWith("4.") || huongDan[i][0].startsWith("5.") || huongDan[i][0].startsWith("6.")) {
                    c0.setCellStyle(styleData);
                    c1.setCellStyle(styleData);
                }
            }
            sheet2.setColumnWidth(0, 11000);
            sheet2.setColumnWidth(1, 22000);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Đọc và thẩm định danh sách khách hàng từ InputStream tệp Excel (đối chiếu qua List<BanGhiNghiepVuDTO>).
     */
    public static List<DongExcelKhachHangDTO> docDanhSachKhachHang(InputStream is, List<BanGhiNghiepVuDTO> danhSachHienCo) throws IOException {
        return docDanhSachKhachHangNoiBo(is, danhSachHienCo, null);
    }

    /**
     * Đọc và thẩm định danh sách khách hàng từ InputStream tệp Excel (đối chiếu qua List<KhachHang> có đầy đủ MST).
     */
    public static List<DongExcelKhachHangDTO> docDanhSachKhachHangTuEntities(InputStream is, List<KhachHang> danhSachHienCo) throws IOException {
        return docDanhSachKhachHangNoiBo(is, null, danhSachHienCo);
    }

    private static List<DongExcelKhachHangDTO> docDanhSachKhachHangNoiBo(
            InputStream is,
            List<BanGhiNghiepVuDTO> danhSachDTO,
            List<KhachHang> danhSachEntities) throws IOException {

        List<DongExcelKhachHangDTO> danhSach = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        Set<String> mstTrongTep = new HashSet<>();
        Set<String> maKhachHangTrongTep = new HashSet<>();
        Set<String> tenCongTyTrongTep = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return danhSach;
            }

            int lastRowNum = sheet.getLastRowNum();
            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null || laDongRong(row, formatter)) {
                    continue;
                }

                DongExcelKhachHangDTO dto = new DongExcelKhachHangDTO(r + 1);

                // Đọc các cột
                String stt = layGiaTriO(row, 0, formatter);
                String maKH = layGiaTriO(row, 1, formatter);
                String tenCongTy = layGiaTriO(row, 2, formatter);
                String mst = layGiaTriO(row, 3, formatter);
                String nganhNghe = layGiaTriO(row, 4, formatter);
                String quyMo = layGiaTriO(row, 5, formatter);
                String website = layGiaTriO(row, 6, formatter);
                String diaChi = layGiaTriO(row, 7, formatter);
                String doanhThu = layGiaTriO(row, 8, formatter);
                String trangThai = layGiaTriO(row, 9, formatter);
                String moTa = layGiaTriO(row, 10, formatter);

                dto.setMaKhachHang(maKH);
                dto.setTenCongTy(tenCongTy);
                dto.setMaSoThue(mst);
                dto.setNganhNghe(nganhNghe);
                dto.setQuyMo(quyMo);
                dto.setWebsite(website);
                dto.setDiaChi(diaChi);
                dto.setDoanhThuUocTinh(doanhThu);
                dto.setTrangThai(trangThai != null && !trangThai.isBlank() ? trangThai : "TIEM_NANG");
                dto.setMoTaChiTiet(moTa);

                // 1. Thẩm định lỗi dữ liệu từng dòng (AC 1)
                if (tenCongTy == null || tenCongTy.isBlank()) {
                    dto.themLoi("Tên công ty / Khách hàng là trường bắt buộc, không được để trống");
                }

                if (mst != null && !mst.isBlank()) {
                    String mstClean = mst.trim().replaceAll("\\s+", "");
                    dto.setMaSoThue(mstClean);
                    if (!mstClean.matches("^[0-9A-Za-z\\-]{8,20}$")) {
                        dto.themLoi("Mã số thuế không đúng định dạng chuẩn (8 - 20 ký tự số/chữ/dấu gạch nối)");
                    }
                }

                if (doanhThu != null && !doanhThu.isBlank()) {
                    try {
                        String cleanNum = doanhThu.trim().replace(",", "").replace(".", "");
                        BigDecimal val = new BigDecimal(cleanNum);
                        if (val.compareTo(BigDecimal.ZERO) < 0) {
                            dto.themLoi("Doanh thu ước tính không được là số âm");
                        }
                    } catch (Exception e) {
                        dto.themLoi("Doanh thu ước tính không đúng định dạng số tiền hợp lệ");
                    }
                }

                if (website != null && !website.isBlank()) {
                    String web = website.trim().toLowerCase();
                    if (!web.startsWith("http://") && !web.startsWith("https://") && !web.contains(".")) {
                        dto.themLoi("Địa chỉ website không đúng định dạng (cần chứa tên miền)");
                    }
                }

                // 2. Phát hiện và đánh dấu bản ghi trùng (AC 2)
                kiemTraVaDanhDauTrungLap(dto, danhSachDTO, danhSachEntities, mstTrongTep, maKhachHangTrongTep, tenCongTyTrongTep);

                danhSach.add(dto);
            }
        }

        return danhSach;
    }

    private static void kiemTraVaDanhDauTrungLap(
            DongExcelKhachHangDTO dto,
            List<BanGhiNghiepVuDTO> danhSachDTO,
            List<KhachHang> danhSachEntities,
            Set<String> mstTrongTep,
            Set<String> maKhachHangTrongTep,
            Set<String> tenCongTyTrongTep) {

        String mst = dto.getMaSoThue();
        String maKH = dto.getMaKhachHang();
        String ten = dto.getTenCongTy();

        // A. Kiểm tra trùng trong nội bộ chính tệp Excel tải lên
        if (mst != null && !mst.isBlank()) {
            if (mstTrongTep.contains(mst.toUpperCase())) {
                dto.danhDauTrung("TRUNG_TRONG_TEP", "Trùng Mã số thuế với một dòng khác ngay trong tệp tải lên", null);
            } else {
                mstTrongTep.add(mst.toUpperCase());
            }
        }

        if (maKH != null && !maKH.isBlank()) {
            if (maKhachHangTrongTep.contains(maKH.toUpperCase())) {
                dto.danhDauTrung("TRUNG_TRONG_TEP", "Trùng Mã khách hàng với một dòng khác ngay trong tệp tải lên", null);
            } else {
                maKhachHangTrongTep.add(maKhachHangTrongTep != null ? maKH.toUpperCase() : "");
            }
        }

        if (ten != null && !ten.isBlank()) {
            String tenChuan = ten.trim().toLowerCase();
            if (tenCongTyTrongTep.contains(tenChuan)) {
                if (!dto.isBiTrung()) {
                    dto.danhDauTrung("TRUNG_TRONG_TEP", "Trùng Tên công ty với một dòng khác ngay trong tệp tải lên", null);
                }
            } else {
                tenCongTyTrongTep.add(tenChuan);
            }
        }

        // B1. Đối chiếu trùng với danh mục khách hàng entity (có đầy đủ MST)
        if (danhSachEntities != null && !danhSachEntities.isEmpty()) {
            for (KhachHang kh : danhSachEntities) {
                // Đối chiếu theo Mã số thuế (ưu tiên độ chính xác pháp nhân cao nhất)
                if (mst != null && !mst.isBlank() && kh.getMaSoThue() != null && mst.equalsIgnoreCase(kh.getMaSoThue().trim())) {
                    dto.danhDauTrung("TRUNG_MST", "Trùng Mã số thuế '" + mst + "' đã có trong CRM (ID: " + kh.getId() + ")", kh.getId());
                    return;
                }

                // Đối chiếu theo Mã khách hàng
                if (maKH != null && !maKH.isBlank() && kh.getMaKhachHang() != null && maKH.equalsIgnoreCase(kh.getMaKhachHang().trim())) {
                    dto.danhDauTrung("TRUNG_MA", "Trùng Mã khách hàng '" + maKH + "' đã tồn tại trong CRM (ID: " + kh.getId() + ")", kh.getId());
                    return;
                }

                // Đối chiếu theo Tên khách hàng / Công ty
                if (ten != null && !ten.isBlank() && kh.getTenCongTy() != null && ten.trim().equalsIgnoreCase(kh.getTenCongTy().trim())) {
                    dto.danhDauTrung("TRUNG_TEN", "Trùng Tên khách hàng '" + kh.getTenCongTy() + "' đã có trong CRM (ID: " + kh.getId() + ")", kh.getId());
                    return;
                }
            }
        }

        // B2. Đối chiếu trùng với danh mục khách hàng DTO (hỗ trợ kiểm thử và fallback)
        if (danhSachDTO != null && !danhSachDTO.isEmpty()) {
            for (BanGhiNghiepVuDTO kh : danhSachDTO) {
                // Đối chiếu theo Mã khách hàng (maBanGhi)
                if (maKH != null && !maKH.isBlank() && maKH.equalsIgnoreCase(kh.getMaBanGhi())) {
                    dto.danhDauTrung("TRUNG_MA", "Trùng Mã khách hàng '" + maKH + "' đã tồn tại trong CRM (ID: " + kh.getId() + ")", kh.getId());
                    return;
                }

                // Đối chiếu theo Tên khách hàng / Công ty (tieuDe)
                if (ten != null && !ten.isBlank() && kh.getTieuDe() != null && ten.trim().equalsIgnoreCase(kh.getTieuDe().trim())) {
                    dto.danhDauTrung("TRUNG_TEN", "Trùng Tên khách hàng '" + kh.getTieuDe() + "' đã có trong CRM (ID: " + kh.getId() + ")", kh.getId());
                    return;
                }
            }
        }
    }

    private static String layGiaTriO(Row row, int colIndex, DataFormatter formatter) {
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static boolean laDongRong(Row row, DataFormatter formatter) {
        for (int c = 0; c < TIEU_DE_COT.length; c++) {
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

    private static void datVienCell(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
    }
}
