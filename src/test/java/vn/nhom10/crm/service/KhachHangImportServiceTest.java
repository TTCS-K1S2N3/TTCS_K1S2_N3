package vn.nhom10.crm.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.KhachHangImportDAO;
import vn.nhom10.crm.dto.BaoCaoNhapKhachHangExcelDTO;
import vn.nhom10.crm.dto.DongExcelKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.util.DatabaseConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Kiểm thử KhachHangImportService (Story S3-06)")
public class KhachHangImportServiceTest {

    private static Connection initConn;
    private static KhachHangImportDAO importDAO;
    private static KhachHangImportService service;

    private NguoiDungDTO currentUser;

    @BeforeAll
    static void setUpAll() throws Exception {
        initConn = DriverManager.getConnection("jdbc:h2:mem:crm_import_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_import_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        importDAO = new KhachHangImportDAO();
        service = new KhachHangImportService(importDAO);

        try (Statement st = initConn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255), " +
                    "ma_so_thue VARCHAR(50) UNIQUE, " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT, " +
                    "website VARCHAR(255), " +
                    "dia_chi VARCHAR(500), " +
                    "khu_vuc_id BIGINT, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (initConn != null && !initConn.isClosed()) {
            initConn.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = DatabaseConnection.layKetNoi();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM khach_hang");
        }

        currentUser = new NguoiDungDTO();
        currentUser.setId(100L);
        currentUser.setHoTen("Lê Văn Kinh Doanh");
        currentUser.setNhomKinhDoanhId(10L);
        currentUser.setPhamViToiDa(PhamViDuLieu.TOAN_BO);
        currentUser.setPhamViHienTai(PhamViDuLieu.TOAN_BO);
    }

    private byte[] taoFileExcelMau(String[][] duLieu) throws Exception {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("DanhSachKhachHang");
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
            header.createCell(9).setCellValue("Trạng thái");
            header.createCell(10).setCellValue("Ghi chú");

            for (int r = 0; r < duLieu.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < duLieu[r].length; c++) {
                    row.createCell(c).setCellValue(duLieu[r][c]);
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    @DisplayName("AC 1: Thẩm định tệp Excel và báo lỗi chi tiết theo từng dòng")
    void testThamDinhTepExcel_AC1_BaoLoiTungDong() throws Exception {
        String[][] duLieu = {
                // Dòng 1: Hợp lệ
                {"1", "KH-001", "Công ty Hợp Lệ A", "0108889991", "CNTT", "50 người", "https://a.vn", "Hà Nội", "1000000000", "TIEM_NANG", "OK"},
                // Dòng 2: Lỗi thiếu tên công ty (trường bắt buộc)
                {"2", "KH-002", "", "0108889992", "Xây dựng", "20 người", "", "Hải Phòng", "500000000", "TIEM_NANG", ""},
                // Dòng 3: Lỗi mã số thuế sai định dạng
                {"3", "KH-003", "Công ty B", "123", "Thương mại", "10 người", "", "Đà Nẵng", "200000000", "TIEM_NANG", ""},
                // Dòng 4: Lỗi doanh thu âm
                {"4", "KH-004", "Công ty C", "0108889994", "Dịch vụ", "5 người", "", "TP.HCM", "-500000", "TIEM_NANG", ""}
        };

        byte[] bytes = taoFileExcelMau(duLieu);
        BaoCaoNhapKhachHangExcelDTO baoCao = service.thamDinhTepExcel(new ByteArrayInputStream(bytes), "test_import.xlsx", currentUser);

        assertNotNull(baoCao);
        assertEquals(4, baoCao.getTongSoDong());
        assertEquals(1, baoCao.getSoDongHopLe());
        assertEquals(3, baoCao.getSoDongLoi());

        DongExcelKhachHangDTO d1 = baoCao.getDanhSachTatCaDong().get(0);
        assertTrue(d1.isHopLe());

        DongExcelKhachHangDTO d2 = baoCao.getDanhSachTatCaDong().get(1);
        assertFalse(d2.isHopLe());
        assertTrue(d2.getChuoiLoi().contains("Tên công ty / Khách hàng là trường bắt buộc"));

        DongExcelKhachHangDTO d3 = baoCao.getDanhSachTatCaDong().get(2);
        assertFalse(d3.isHopLe());
        assertTrue(d3.getChuoiLoi().contains("Mã số thuế không đúng định dạng"));

        DongExcelKhachHangDTO d4 = baoCao.getDanhSachTatCaDong().get(3);
        assertFalse(d4.isHopLe());
        assertTrue(d4.getChuoiLoi().contains("Doanh thu ước tính không được là số âm"));
    }

    @Test
    @DisplayName("AC 2: Đánh dấu rõ ràng bản ghi trùng theo Mã số thuế, Mã KH, và Tên công ty")
    void testThamDinhTepExcel_AC2_DanhDauTrungLap() throws Exception {
        // Tạo trước 1 khách hàng đã có trong DB
        KhachHang existing = new KhachHang();
        existing.setMaKhachHang("KH-EXIST-01");
        existing.setTenCongTy("Công ty Đã Có Trong Hệ Thống");
        existing.setMaSoThue("0107776665");
        existing.setNguoiSoHuuId(currentUser.getId());
        existing.setNhomKinhDoanhId(currentUser.getNhomKinhDoanhId());
        Long existingId = importDAO.themKhachHang(existing);

        String[][] duLieu = {
                // Dòng 1: Trùng theo Mã số thuế
                {"1", "KH-DIFF-01", "Công ty Tên Khác", "0107776665", "CNTT", "", "", "", "1000000", "TIEM_NANG", ""},
                // Dòng 2: Trùng theo Mã khách hàng
                {"2", "KH-EXIST-01", "Công ty Tên Khác Nữa", "0109998887", "", "", "", "", "2000000", "TIEM_NANG", ""},
                // Dòng 3: Trùng theo Tên công ty
                {"3", "KH-NEW-03", "Công ty Đã Có Trong Hệ Thống", "0103332221", "", "", "", "", "3000000", "TIEM_NANG", ""},
                // Dòng 4: Không trùng (Mới)
                {"4", "KH-NEW-04", "Công ty Hoàn Toàn Mới", "0104445556", "", "", "", "", "4000000", "TIEM_NANG", ""}
        };

        byte[] bytes = taoFileExcelMau(duLieu);
        BaoCaoNhapKhachHangExcelDTO baoCao = service.thamDinhTepExcel(new ByteArrayInputStream(bytes), "test_duplicate.xlsx", currentUser);

        assertEquals(4, baoCao.getTongSoDong());
        assertEquals(3, baoCao.getSoDongBiTrung());

        DongExcelKhachHangDTO d1 = baoCao.getDanhSachTatCaDong().get(0);
        assertTrue(d1.isBiTrung());
        assertEquals("TRUNG_MST", d1.getLoaiTrung());
        assertEquals(existingId, d1.getIdKhachHangTrung());

        DongExcelKhachHangDTO d2 = baoCao.getDanhSachTatCaDong().get(1);
        assertTrue(d2.isBiTrung());
        assertEquals("TRUNG_MA", d2.getLoaiTrung());

        DongExcelKhachHangDTO d3 = baoCao.getDanhSachTatCaDong().get(2);
        assertTrue(d3.isBiTrung());
        assertEquals("TRUNG_TEN", d3.getLoaiTrung());

        DongExcelKhachHangDTO d4 = baoCao.getDanhSachTatCaDong().get(3);
        assertFalse(d4.isBiTrung());
        assertTrue(d4.isHopLe());
    }

    @Test
    @DisplayName("AC 2: Thực hiện nhập với lựa chọn BỎ QUA bản ghi trùng (dữ liệu cũ giữ nguyên)")
    void testThucHienNhapDuLieu_BoQuaBanGhiTrung() throws Exception {
        KhachHang existing = new KhachHang();
        existing.setMaKhachHang("KH-OLD");
        existing.setTenCongTy("Công ty Gốc Cũ");
        existing.setMaSoThue("0101112223");
        existing.setDoanhThuUocTinh(new BigDecimal("1000000"));
        existing.setNguoiSoHuuId(currentUser.getId());
        existing.setNhomKinhDoanhId(currentUser.getNhomKinhDoanhId());
        Long existingId = importDAO.themKhachHang(existing);

        String[][] duLieu = {
                // Dòng trùng MST, muốn ghi đè doanh thu 999.000.000
                {"1", "KH-OLD-OVERWRITE", "Tên Muốn Ghi Đè", "0101112223", "", "", "", "", "999000000", "TIEM_NANG", ""},
                // Dòng mới hợp lệ
                {"2", "KH-NEW", "Công ty Mới Hợp Lệ", "0109990001", "", "", "", "", "500000000", "TIEM_NANG", ""}
        };

        byte[] bytes = taoFileExcelMau(duLieu);
        BaoCaoNhapKhachHangExcelDTO baoCao = service.thamDinhTepExcel(new ByteArrayInputStream(bytes), "test.xlsx", currentUser);

        // Chọn BỎ QUA cho toàn bộ dòng trùng
        service.thucHienNhapDuLieu(baoCao, Map.of(), "BO_QUA", currentUser);

        assertTrue(baoCao.isDaThucHienNhap());
        assertEquals(1, baoCao.getSoDongThanhCong(), "1 dòng mới thêm thành công");
        assertEquals(1, baoCao.getSoDongBoQua(), "1 dòng trùng được bỏ qua");
        assertEquals(0, baoCao.getSoDongCapNhat());

        // Kiểm tra database: Khách hàng cũ giữ nguyên dữ liệu gốc
        KhachHang current = importDAO.timTheoId(existingId);
        assertEquals("Công ty Gốc Cũ", current.getTenCongTy());
        assertEquals(0, new BigDecimal("1000000").compareTo(current.getDoanhThuUocTinh()));
    }

    @Test
    @DisplayName("AC 2: Thực hiện nhập với lựa chọn CẬP NHẬT bản ghi trùng (ghi đè dữ liệu mới)")
    void testThucHienNhapDuLieu_CapNhatBanGhiTrung() throws Exception {
        KhachHang existing = new KhachHang();
        existing.setMaKhachHang("KH-UPDATE-ME");
        existing.setTenCongTy("Tên Ban Đầu");
        existing.setMaSoThue("0105556667");
        existing.setDoanhThuUocTinh(new BigDecimal("2000000"));
        existing.setNguoiSoHuuId(currentUser.getId());
        existing.setNhomKinhDoanhId(currentUser.getNhomKinhDoanhId());
        Long existingId = importDAO.themKhachHang(existing);

        String[][] duLieu = {
                {"1", "KH-UPDATE-ME", "Tên Đã Cập Nhật Sau Import", "0105556667", "Công nghệ", "100 người", "https://updated.vn", "Hà Nội Mới", "7700000000", "DANG_CHAM_SOC", "Cập nhật từ Excel"}
        };

        byte[] bytes = taoFileExcelMau(duLieu);
        BaoCaoNhapKhachHangExcelDTO baoCao = service.thamDinhTepExcel(new ByteArrayInputStream(bytes), "update.xlsx", currentUser);

        // Lựa chọn dòng 1 là CAP_NHAT
        Map<Integer, String> luaChonTungDong = new HashMap<>();
        luaChonTungDong.put(2, "CAP_NHAT"); // dòng thứ 2 trong excel (r=1)

        service.thucHienNhapDuLieu(baoCao, luaChonTungDong, "CAP_NHAT", currentUser);

        assertEquals(1, baoCao.getSoDongCapNhat(), "Phải ghi nhận 1 dòng cập nhật thành công");
        assertEquals(0, baoCao.getSoDongBoQua());

        // Kiểm tra database: Thông tin đã được cập nhật
        KhachHang updated = importDAO.timTheoId(existingId);
        assertEquals("Tên Đã Cập Nhật Sau Import", updated.getTenCongTy());
        assertEquals("https://updated.vn", updated.getWebsite());
        assertEquals("Hà Nội Mới", updated.getDiaChi());
        assertEquals("DANG_CHAM_SOC", updated.getTrangThai());
        assertEquals(0, new BigDecimal("7700000000").compareTo(updated.getDoanhThuUocTinh()));
    }

    @Test
    @DisplayName("Data Scope: Khách hàng mới được gắn quyền sở hữu cho người dùng đăng nhập")
    void testThucHienNhapDuLieu_DataScopeEnforcement() throws Exception {
        String[][] duLieu = {
                {"1", "KH-DS-01", "Công ty Kiểm Tra Quyền Sở Hữu", "0108881112", "", "", "", "", "1000000", "TIEM_NANG", ""}
        };

        byte[] bytes = taoFileExcelMau(duLieu);
        BaoCaoNhapKhachHangExcelDTO baoCao = service.thamDinhTepExcel(new ByteArrayInputStream(bytes), "datascope.xlsx", currentUser);

        service.thucHienNhapDuLieu(baoCao, Map.of(), "BO_QUA", currentUser);

        assertEquals(1, baoCao.getSoDongThanhCong());

        // Tìm khách hàng vừa tạo trong DB
        var ds = importDAO.layDanhSachDoiChieuTrung(currentUser.getId(), currentUser.getNhomKinhDoanhId(), PhamViDuLieu.CA_NHAN);
        assertEquals(1, ds.size());
        assertEquals("KH-DS-01", ds.get(0).getMaKhachHang());
        assertEquals(currentUser.getId(), ds.get(0).getNguoiSoHuuId(), "Quyền sở hữu phải gắn với user đang thực hiện");
        assertEquals(currentUser.getNhomKinhDoanhId(), ds.get(0).getNhomKinhDoanhId());
    }
}
