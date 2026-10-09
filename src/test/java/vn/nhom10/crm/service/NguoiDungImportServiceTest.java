package vn.nhom10.crm.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import vn.nhom10.crm.dao.DotNhapDuLieuDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.BaoCaoNhapExcelDTO;
import vn.nhom10.crm.dto.DongExcelNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử nghiệp vụ NguoiDungImportService (Story S2-01)")
public class NguoiDungImportServiceTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    @Mock
    private VaiTroDAO vaiTroDAO;

    @Mock
    private NhomKinhDoanhDAO nhomKinhDoanhDAO;

    @Mock
    private DotNhapDuLieuDAO dotNhapDuLieuDAO;

    private NguoiDungImportService importService;

    @BeforeEach
    void setUp() {
        importService = new NguoiDungImportService(nguoiDungDAO, vaiTroDAO, nhomKinhDoanhDAO, dotNhapDuLieuDAO);

        // Giả lập danh sách vai trò hệ thống
        List<VaiTro> dsVaiTro = new ArrayList<>();
        dsVaiTro.add(new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin"));
        dsVaiTro.add(new VaiTro(2, "DIRECTOR", "Giám đốc kinh doanh", "Director"));
        dsVaiTro.add(new VaiTro(3, "TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead"));
        dsVaiTro.add(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep"));
        dsVaiTro.add(new VaiTro(5, "MARKETING", "Nhân viên Marketing", "Marketing"));
        when(vaiTroDAO.layTatCa()).thenReturn(dsVaiTro);

        // Giả lập danh sách nhóm kinh doanh
        List<NhomKinhDoanh> dsNhom = new ArrayList<>();
        dsNhom.add(new NhomKinhDoanh(1, "KD_MIEN_BAC", "Nhóm Kinh Doanh Miền Bắc", "Phía Bắc", null));
        dsNhom.add(new NhomKinhDoanh(2, "KD_MIEN_NAM", "Nhóm Kinh Doanh Miền Nam", "Phía Nam", null));
        when(nhomKinhDoanhDAO.layTatCa()).thenReturn(dsNhom);
    }

    @Test
    @DisplayName("AC 2: Xem trước và thẩm định thành công dòng dữ liệu hợp lệ")
    void testKiemTraVaPhanTich_DongHopLe() {
        DongExcelNguoiDungDTO dong = new DongExcelNguoiDungDTO(
                2, "Nguyễn Văn Hùng", "hung.nguyen@crm.vn", "0987654321", "SALES_REP", "KD_MIEN_BAC"
        );

        when(nguoiDungDAO.kiemTraEmailTonTai("hung.nguyen@crm.vn", null)).thenReturn(false);

        List<DongExcelNguoiDungDTO> ds = List.of(dong);
        BaoCaoNhapExcelDTO baoCao = importService.kiemTraVaPhanTich(ds, "test.xlsx");

        assertNotNull(baoCao);
        assertEquals(1, baoCao.getTongSoDong());
        assertEquals(1, baoCao.getSoDongHopLe());
        assertEquals(0, baoCao.getSoDongLoi());
        assertTrue(dong.isHopLe());
        assertEquals(List.of(4), dong.getDsVaiTroIds());
        assertEquals(Integer.valueOf(1), dong.getNhomKinhDoanhId());
    }

    @Test
    @DisplayName("AC 2: Xem trước báo lỗi chi tiết theo từng dòng khi vi phạm quy tắc")
    void testKiemTraVaPhanTich_BaoLoiTungDong() {
        // Dòng 1: Thiếu họ tên, email sai định dạng, mã vai trò không tồn tại
        DongExcelNguoiDungDTO d1 = new DongExcelNguoiDungDTO(2, "", "invalid-email", "0987654321", "UNKNOWN_ROLE", "");

        // Dòng 2: Email trùng lặp với CSDL
        DongExcelNguoiDungDTO d2 = new DongExcelNguoiDungDTO(3, "Trần Văn A", "trung@crm.vn", "0912345678", "SALES_REP", "");
        when(nguoiDungDAO.kiemTraEmailTonTai("trung@crm.vn", null)).thenReturn(true);

        // Dòng 3: TEAM_LEAD nhưng không gán nhóm kinh doanh (ràng buộc S1-09)
        DongExcelNguoiDungDTO d3 = new DongExcelNguoiDungDTO(4, "Lê Trưởng Nhóm", "lead@crm.vn", "0905123456", "TEAM_LEAD", "");

        // Dòng 4: Trùng email với chính dòng trước đó trong cùng tệp
        DongExcelNguoiDungDTO d4 = new DongExcelNguoiDungDTO(5, "Lê Trùng Lặp", "lead@crm.vn", "0934567890", "SALES_REP", "");

        List<DongExcelNguoiDungDTO> ds = List.of(d1, d2, d3, d4);
        BaoCaoNhapExcelDTO baoCao = importService.kiemTraVaPhanTich(ds, "test_loi.xlsx");

        assertEquals(4, baoCao.getTongSoDong());
        assertEquals(0, baoCao.getSoDongHopLe());
        assertEquals(4, baoCao.getSoDongLoi());

        assertFalse(d1.isHopLe());
        assertTrue(d1.getChuoiLoi().contains("Họ và tên không được để trống"));
        assertTrue(d1.getChuoiLoi().contains("không đúng định dạng chuẩn"));
        assertTrue(d1.getChuoiLoi().contains("không tồn tại"));

        assertFalse(d2.isHopLe());
        assertTrue(d2.getChuoiLoi().contains("đã tồn tại trong hệ thống"));

        assertFalse(d3.isHopLe());
        assertTrue(d3.getChuoiLoi().contains("Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm"));

        assertFalse(d4.isHopLe());
        assertTrue(d4.getChuoiLoi().contains("bị trùng lặp với dòng khác trong cùng tệp Excel"));
    }

    @Test
    @DisplayName("AC 3: Thực hiện nhập: Dòng lỗi bị bỏ qua, dòng hợp lệ vẫn được nhập, có báo cáo tổng kết")
    void testThucHienNhap_BoQuaDongLoi_NhapDongHopLe() throws Exception {
        // Tạo file Excel giả lập gồm 2 dòng: dòng 1 hợp lệ, dòng 2 lỗi (thiếu email)
        byte[] excelBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachNguoiDung");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Họ và tên");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Số điện thoại");
            header.createCell(4).setCellValue("Mã vai trò");
            header.createCell(5).setCellValue("Nhóm");
            header.createCell(6).setCellValue("Mật khẩu");

            // Dòng 1: Hợp lệ
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("1");
            r1.createCell(1).setCellValue("Hoàng Đức Nam");
            r1.createCell(2).setCellValue("nam.hoang@crm.vn");
            r1.createCell(3).setCellValue("0988776655");
            r1.createCell(4).setCellValue("SALES_REP");
            r1.createCell(5).setCellValue("KD_MIEN_BAC");
            r1.createCell(6).setCellValue("");

            // Dòng 2: Lỗi (thiếu email)
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("2");
            r2.createCell(1).setCellValue("Phạm Lỗi");
            r2.createCell(2).setCellValue(""); // Trống email
            r2.createCell(3).setCellValue("0911223344");
            r2.createCell(4).setCellValue("MARKETING");
            r2.createCell(5).setCellValue("");
            r2.createCell(6).setCellValue("");

            wb.write(out);
            excelBytes = out.toByteArray();
        }

        when(nguoiDungDAO.kiemTraEmailTonTai("nam.hoang@crm.vn", null)).thenReturn(false);
        when(nguoiDungDAO.themNguoiDung(any(NguoiDung.class), anyList())).thenReturn(105);

        BaoCaoNhapExcelDTO baoCao = importService.thucHienNhap(new ByteArrayInputStream(excelBytes), 1L, "danh_sach.xlsx");

        assertNotNull(baoCao);
        assertEquals(2, baoCao.getTongSoDong(), "Tổng số dòng đọc được là 2");
        assertEquals(1, baoCao.getSoDongHopLe(), "Số dòng hợp lệ là 1");
        assertEquals(1, baoCao.getSoDongLoi(), "Số dòng lỗi là 1");
        assertEquals(1, baoCao.getSoDongThanhCong(), "Dòng hợp lệ được nhập thành công");
        assertEquals(1, baoCao.getSoDongThatBai(), "Dòng lỗi bị bỏ qua");

        // Xác minh chỉ gọi themNguoiDung 1 lần cho dòng hợp lệ
        verify(nguoiDungDAO).themNguoiDung(any(NguoiDung.class), anyList());
        // Xác minh gọi lưu vết vào dot_nhap_du_lieu
        verify(dotNhapDuLieuDAO).luuDotNhap(eq(baoCao), eq(1L));

        assertTrue(baoCao.getThongDiep().contains("1/2 tài khoản nhập thành công"));
        assertTrue(baoCao.getThongDiep().contains("1 dòng lỗi bị bỏ qua"));
    }

    @Test
    @DisplayName("AC 1: Tải được tệp mẫu Excel thành công")
    void testTaoTepMauExcel() throws Exception {
        byte[] fileBytes = importService.taoTepMauExcel();
        assertNotNull(fileBytes, "Mảng byte tệp mẫu không được null");
        assertTrue(fileBytes.length > 0, "Dung lượng tệp mẫu phải lớn hơn 0");
    }

    @Test
    @DisplayName("AC 3: Import batch có 3 dòng (1 hợp lệ, 1 email sai định dạng, 1 email trùng) - chỉ dòng hợp lệ được insert, báo cáo đúng số lượng")
    void testThucHienNhap_Batch3Dong_1HopLe_1SaiDinhDang_1TrungEmail() throws Exception {
        byte[] excelBytes;
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachNguoiDung");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("STT");
            header.createCell(1).setCellValue("Họ và tên");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Số điện thoại");
            header.createCell(4).setCellValue("Mã vai trò");
            header.createCell(5).setCellValue("Nhóm");
            header.createCell(6).setCellValue("Mật khẩu");

            // Dòng 1: Hợp lệ
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("1");
            r1.createCell(1).setCellValue("Nguyễn Hợp Lệ");
            r1.createCell(2).setCellValue("hople@crm.vn");
            r1.createCell(3).setCellValue("0988776655");
            r1.createCell(4).setCellValue("SALES_REP");
            r1.createCell(5).setCellValue("KD_MIEN_BAC");
            r1.createCell(6).setCellValue("");

            // Dòng 2: Email sai định dạng
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("2");
            r2.createCell(1).setCellValue("Trần Sai Định Dạng");
            r2.createCell(2).setCellValue("email-sai-dinh-dang");
            r2.createCell(3).setCellValue("0911223344");
            r2.createCell(4).setCellValue("MARKETING");
            r2.createCell(5).setCellValue("");
            r2.createCell(6).setCellValue("");

            // Dòng 3: Email trùng (đã tồn tại trong hệ thống)
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("3");
            r3.createCell(1).setCellValue("Lê Email Trùng");
            r3.createCell(2).setCellValue("trung.email@crm.vn");
            r3.createCell(3).setCellValue("0933445566");
            r3.createCell(4).setCellValue("ADMIN");
            r3.createCell(5).setCellValue("");
            r3.createCell(6).setCellValue("");

            wb.write(out);
            excelBytes = out.toByteArray();
        }

        when(nguoiDungDAO.kiemTraEmailTonTai("hople@crm.vn", null)).thenReturn(false);
        when(nguoiDungDAO.kiemTraEmailTonTai("trung.email@crm.vn", null)).thenReturn(true);
        when(nguoiDungDAO.themNguoiDung(any(NguoiDung.class), anyList())).thenReturn(201);

        BaoCaoNhapExcelDTO baoCao = importService.thucHienNhap(new ByteArrayInputStream(excelBytes), 1L, "batch3.xlsx");

        assertNotNull(baoCao);
        assertEquals(3, baoCao.getTongSoDong(), "Tổng số dòng phải là 3");
        assertEquals(1, baoCao.getSoDongHopLe(), "Số dòng hợp lệ phải là 1");
        assertEquals(2, baoCao.getSoDongLoi(), "Số dòng lỗi phải là 2");
        assertEquals(1, baoCao.getSoDongThanhCong(), "Chỉ 1 dòng hợp lệ được insert thành công");
        assertEquals(2, baoCao.getSoDongThatBai(), "2 dòng lỗi bị bỏ qua");

        // Xác minh chỉ gọi themNguoiDung đúng 1 lần cho dòng hợp lệ
        verify(nguoiDungDAO, times(1)).themNguoiDung(any(NguoiDung.class), anyList());
        verify(dotNhapDuLieuDAO).luuDotNhap(eq(baoCao), eq(1L));
    }
}
