package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.TruongTuyChinhDAO;
import vn.nhom10.crm.dto.TruongTuyChinhDTO;
import vn.nhom10.crm.model.TruongTuyChinh;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ Quản lý Trường tuỳ chỉnh (Story S2-08)")
class TruongTuyChinhServiceTest {

    @Mock
    private TruongTuyChinhDAO dao;

    private TruongTuyChinhService service;

    @BeforeEach
    void setUp() {
        service = new TruongTuyChinhService(dao);
    }

    // =========================================================================
    // AC1: THÊM TRƯỜNG KIỂU VĂN BẢN, SỐ, NGÀY, DANH SÁCH CHỌN
    // =========================================================================

    @Test
    @DisplayName("AC1.1: Tạo trường tuỳ chỉnh kiểu Văn bản (VAN_BAN) thành công")
    void testTaoTruongVanBanThanhCong() {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong("KHACH_HANG");
        dto.setTenTruong("ghi_chu_dac_biet");
        dto.setNhanHien("Ghi chú đặc biệt");
        dto.setKieuDuLieu("VAN_BAN");
        dto.setBatBuoc(false);
        dto.setThuTu(1);

        when(dao.kiemTraTonTaiMa("KHACH_HANG", "ghi_chu_dac_biet", null)).thenReturn(false);
        when(dao.them(any(TruongTuyChinh.class))).thenReturn(10L);

        long newId = service.taoTruongTuyChinh(dto, 1L);

        assertEquals(10L, newId);
        verify(dao, times(1)).them(argThat(t ->
                "KHACH_HANG".equals(t.getLoaiDoiTuong()) &&
                "ghi_chu_dac_biet".equals(t.getMaTruong()) &&
                "Ghi chú đặc biệt".equals(t.getTenNhanGoc()) &&
                "VAN_BAN".equals(t.getKieuDuLieu()) &&
                !t.isBatBuoc()
        ));
    }

    @Test
    @DisplayName("AC1.2: Tạo trường tuỳ chỉnh kiểu Số (SO) thành công")
    void testTaoTruongSoThanhCong() {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong("CO_HOI");
        dto.setTenTruong("dien_tich_mat_bang");
        dto.setNhanHien("Diện tích mặt bằng (m2)");
        dto.setKieuDuLieu("SO");
        dto.setBatBuoc(false);

        when(dao.kiemTraTonTaiMa("CO_HOI", "dien_tich_mat_bang", null)).thenReturn(false);
        when(dao.them(any(TruongTuyChinh.class))).thenReturn(11L);

        long newId = service.taoTruongTuyChinh(dto, 1L);
        assertEquals(11L, newId);
    }

    @Test
    @DisplayName("AC1.3: Tạo trường tuỳ chỉnh kiểu Ngày (NGAY) thành công")
    void testTaoTruongNgayThanhCong() {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong("KHACH_HANG");
        dto.setTenTruong("ngay_thanh_lap_cty");
        dto.setNhanHien("Ngày thành lập công ty");
        dto.setKieuDuLieu("NGAY");
        dto.setBatBuoc(false);

        when(dao.kiemTraTonTaiMa("KHACH_HANG", "ngay_thanh_lap_cty", null)).thenReturn(false);
        when(dao.them(any(TruongTuyChinh.class))).thenReturn(12L);

        long newId = service.taoTruongTuyChinh(dto, 1L);
        assertEquals(12L, newId);
    }

    @Test
    @DisplayName("AC1.4: Tạo trường tuỳ chỉnh kiểu Danh sách chọn (DANH_SACH_CHON) với các lựa chọn hợp lệ")
    void testTaoTruongDanhSachChonThanhCong() {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong("KHACH_HANG");
        dto.setTenTruong("kenh_tiep_can");
        dto.setNhanHien("Kênh tiếp cận khách hàng");
        dto.setKieuDuLieu("DANH_SACH_CHON");
        dto.setBatBuoc(false);
        dto.setDanhSachLuaChon(List.of("Website", "Hội thảo", "Đối tác giới thiệu", "Cold call"));

        when(dao.kiemTraTonTaiMa("KHACH_HANG", "kenh_tiep_can", null)).thenReturn(false);
        when(dao.them(any(TruongTuyChinh.class))).thenReturn(13L);

        long newId = service.taoTruongTuyChinh(dto, 1L);
        assertEquals(13L, newId);

        verify(dao).them(argThat(t ->
                t.getDanhSachLuaChon().contains("Website") &&
                t.getDanhSachLuaChon().contains("Hội thảo")
        ));
    }

    @Test
    @DisplayName("AC1.5: Báo lỗi khi tạo trường Danh sách chọn mà không có lựa chọn nào")
    void testTaoTruongDanhSachChonLoiKhongCoLuaChon() {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong("KHACH_HANG");
        dto.setTenTruong("kenh_tiep_can");
        dto.setNhanHien("Kênh tiếp cận");
        dto.setKieuDuLieu("DANH_SACH_CHON");
        dto.setDanhSachLuaChon(Collections.emptyList());

        Map<String, String> errors = service.validateDinhNghiaTruong(dto, true);
        assertTrue(errors.containsKey("danhSachLuaChon"));
        assertTrue(errors.get("danhSachLuaChon").contains("ít nhất một lựa chọn"));
    }

    @Test
    @DisplayName("AC1.6: Báo lỗi khi tên kỹ thuật chứa ký tự không hợp lệ hoặc trùng lặp")
    void testValidationTenTruongKyThuat() {
        // Tên chứa dấu tiếng Việt hoặc khoảng trắng
        TruongTuyChinhDTO dto1 = new TruongTuyChinhDTO();
        dto1.setDoiTuong("KHACH_HANG");
        dto1.setTenTruong("tên trường");
        dto1.setNhanHien("Tên trường");
        dto1.setKieuDuLieu("VAN_BAN");

        Map<String, String> errors1 = service.validateDinhNghiaTruong(dto1, true);
        assertTrue(errors1.containsKey("tenTruong"));

        // Tên trùng với trường đã tồn tại trong database
        TruongTuyChinhDTO dto2 = new TruongTuyChinhDTO();
        dto2.setDoiTuong("KHACH_HANG");
        dto2.setTenTruong("nguon_gioi_thieu");
        dto2.setNhanHien("Nguồn giới thiệu");
        dto2.setKieuDuLieu("VAN_BAN");

        when(dao.kiemTraTonTaiMa("KHACH_HANG", "nguon_gioi_thieu", null)).thenReturn(true);
        Map<String, String> errors2 = service.validateDinhNghiaTruong(dto2, true);
        assertTrue(errors2.containsKey("tenTruong"));
        assertTrue(errors2.get("tenTruong").contains("đã tồn tại"));
    }

    // =========================================================================
    // AC2: ĐẶT ĐƯỢC TRƯỜNG LÀ BẮT BUỘC HAY KHÔNG
    // =========================================================================

    @Test
    @DisplayName("AC2.1: Khai báo trường bắt buộc nhập (batBuoc = true) và lưu vào database")
    void testDatTruongBatBuoc() {
        TruongTuyChinhDTO dto = new TruongTuyChinhDTO();
        dto.setDoiTuong("CO_HOI");
        dto.setTenTruong("ngan_sach_du_kien");
        dto.setNhanHien("Ngân sách dự kiến");
        dto.setKieuDuLieu("SO");
        dto.setBatBuoc(true); // BẮT BUỘC

        when(dao.kiemTraTonTaiMa("CO_HOI", "ngan_sach_du_kien", null)).thenReturn(false);
        when(dao.them(any(TruongTuyChinh.class))).thenReturn(20L);

        long id = service.taoTruongTuyChinh(dto, 1L);
        assertEquals(20L, id);

        verify(dao).them(argThat(TruongTuyChinh::isBatBuoc));
    }

    @Test
    @DisplayName("AC2.2: Thay đổi thuộc tính bắt buộc của trường khi cập nhật")
    void testCapNhatThuocTinhBatBuoc() {
        TruongTuyChinh existing = new TruongTuyChinh("KHACH_HANG", "ma_so_thue_phu", "MST Phụ", "VAN_BAN", false);
        existing.setId(5L);
        when(dao.layTheoId(5L)).thenReturn(existing);
        when(dao.capNhat(any(TruongTuyChinh.class))).thenReturn(true);

        TruongTuyChinhDTO updateDto = new TruongTuyChinhDTO();
        updateDto.setId(5L);
        updateDto.setDoiTuong("KHACH_HANG");
        updateDto.setTenTruong("ma_so_thue_phu");
        updateDto.setNhanHien("Mã số thuế chi nhánh phụ");
        updateDto.setKieuDuLieu("VAN_BAN");
        updateDto.setBatBuoc(true); // Đổi sang bắt buộc

        boolean ok = service.capNhatTruongTuyChinh(updateDto);
        assertTrue(ok);
        verify(dao).capNhat(argThat(t -> t.isBatBuoc() && "Mã số thuế chi nhánh phụ".equals(t.getTenNhanGoc())));
    }

    // =========================================================================
    // AC3: XUẤT HIỆN TRONG BIỂU MẪU, BỘ LỌC VÀ BẢN XUẤT EXCEL
    // =========================================================================

    @Test
    @DisplayName("AC3.1: Kiểm tra tính hợp lệ dữ liệu biểu mẫu (Bắt buộc, Số, Ngày, Danh sách)")
    void testValidateGiaTriBieuMau() {
        TruongTuyChinh tBatBuoc = new TruongTuyChinh("KHACH_HANG", "ly_do_mua", "Lý do mua", "VAN_BAN", true);
        TruongTuyChinh tSo = new TruongTuyChinh("KHACH_HANG", "so_nhan_vien", "Số lượng nhân viên", "SO", false);
        TruongTuyChinh tNgay = new TruongTuyChinh("KHACH_HANG", "ngay_gap", "Ngày hẹn gặp", "NGAY", false);
        TruongTuyChinh tSelect = new TruongTuyChinh("KHACH_HANG", "phan_khuc", "Phân khúc", "DANH_SACH_CHON", false);
        tSelect.setDanhSachLuaChon(List.of("VIP", "Doanh nghiệp", "Bán lẻ"));

        when(dao.layDanhSachTheoDoiTuong("KHACH_HANG", true)).thenReturn(List.of(tBatBuoc, tSo, tNgay, tSelect));

        // Trường hợp lỗi: bỏ trống trường bắt buộc, số sai định dạng, ngày sai định dạng, chọn option ngoài danh mục
        Map<String, String> invalidInput = new HashMap<>();
        invalidInput.put("ttc_ly_do_mua", ""); // Bỏ trống
        invalidInput.put("ttc_so_nhan_vien", "khong_phai_so"); // Sai kiểu số
        invalidInput.put("ttc_ngay_gap", "32/13/2026"); // Sai ngày YYYY-MM-DD
        invalidInput.put("ttc_phan_khuc", "Option_Khong_Ton_Tai"); // Không thuộc danh mục

        Map<String, String> errors = service.validateGiaTriBieuMau("KHACH_HANG", invalidInput);
        assertEquals(4, errors.size());
        assertTrue(errors.containsKey("ly_do_mua"));
        assertTrue(errors.containsKey("so_nhan_vien"));
        assertTrue(errors.containsKey("ngay_gap"));
        assertTrue(errors.containsKey("phan_khuc"));

        // Trường hợp hợp lệ
        Map<String, String> validInput = new HashMap<>();
        validInput.put("ttc_ly_do_mua", "Nhu cầu nâng cấp hệ thống ERP");
        validInput.put("ttc_so_nhan_vien", "150");
        validInput.put("ttc_ngay_gap", "2026-10-15");
        validInput.put("ttc_phan_khuc", "VIP");

        Map<String, String> noErrors = service.validateGiaTriBieuMau("KHACH_HANG", validInput);
        assertTrue(noErrors.isEmpty());
    }

    @Test
    @DisplayName("AC3.2: Lưu thành công giá trị trường tuỳ chỉnh khi người dùng submit biểu mẫu")
    void testLuuGiaTriBieuMauThanhCong() {
        TruongTuyChinh t1 = new TruongTuyChinh("CO_HOI", "yeu_cau_ky_thuat", "Yêu cầu kỹ thuật", "VAN_BAN", false);
        t1.setId(101L);
        when(dao.layDanhSachTheoDoiTuong("CO_HOI", true)).thenReturn(List.of(t1));
        when(dao.luuNhieuGiaTri(eq("CO_HOI"), eq(50L), anyMap())).thenReturn(true);

        Map<String, String> input = Map.of("ttc_yeu_cau_ky_thuat", "Cần hỗ trợ Single Sign-On (SSO)");
        boolean ok = service.luuGiaTriBieuMau("CO_HOI", 50L, input);

        assertTrue(ok);
        verify(dao).luuNhieuGiaTri(eq("CO_HOI"), eq(50L), argThat(map ->
                "Cần hỗ trợ Single Sign-On (SSO)".equals(map.get("yeu_cau_ky_thuat"))
        ));
    }

    @Test
    @DisplayName("AC3.3: Lọc dữ liệu theo trường tuỳ chỉnh (Văn bản, Số, Ngày, Danh sách chọn)")
    void testLocTheoTruongTuyChinh() {
        TruongTuyChinh tText = new TruongTuyChinh("KHACH_HANG", "khu_cong_nghiep", "Khu công nghiệp", "VAN_BAN", false);
        TruongTuyChinh tNum = new TruongTuyChinh("KHACH_HANG", "so_xe", "Số xe tải", "SO", false);
        TruongTuyChinh tSelect = new TruongTuyChinh("KHACH_HANG", "hang_khach", "Hạng khách", "DANH_SACH_CHON", false);
        tSelect.setDanhSachLuaChon(List.of("Vàng", "Bạc", "Đồng"));

        when(dao.layDanhSachTheoDoiTuong("KHACH_HANG", true)).thenReturn(List.of(tText, tNum, tSelect));

        // Bản ghi 1: Khớp bộ lọc
        Map<String, String> r1 = Map.of(
                "khu_cong_nghiep", "KCN VSIP Bắc Ninh",
                "so_xe", "10",
                "hang_khach", "Vàng"
        );

        // Bản ghi 2: Không khớp bộ lọc số và hạng
        Map<String, String> r2 = Map.of(
                "khu_cong_nghiep", "KCN VSIP Bắc Ninh",
                "so_xe", "5",
                "hang_khach", "Đồng"
        );

        Map<String, String> filterParams = Map.of(
                "ttcf_khu_cong_nghiep", "VSIP",
                "ttcf_so_xe", "10",
                "ttcf_hang_khach", "Vàng"
        );

        assertTrue(service.kiemTraKhopBoLoc("KHACH_HANG", r1, filterParams));
        assertFalse(service.kiemTraKhopBoLoc("KHACH_HANG", r2, filterParams));
    }

    @Test
    @DisplayName("AC3.4: Xuất bản ghi kèm các cột trường tuỳ chỉnh sang file CSV UTF-8 mở được trên Excel")
    void testXuatDuLieuExcelCSV() {
        TruongTuyChinh t1 = new TruongTuyChinh("KHACH_HANG", "nguon_lead", "Nguồn giới thiệu", "DANH_SACH_CHON", false);
        TruongTuyChinh t2 = new TruongTuyChinh("KHACH_HANG", "doanh_thu_nam", "Doanh thu năm", "SO", false);

        when(dao.layDanhSachTheoDoiTuong("KHACH_HANG", true)).thenReturn(List.of(t1, t2));

        List<String> headers = List.of("Mã KH", "Tên công ty");
        List<String> keys = List.of("ma", "ten");

        List<Map<String, Object>> records = List.of(
                Map.of("id", 1L, "ma", "KH-001", "ten", "Tập đoàn Viễn thông Viettel"),
                Map.of("id", 2L, "ma", "KH-002", "ten", "Công ty TNHH VNG")
        );

        Map<Long, Map<String, String>> customFields = Map.of(
                1L, Map.of("nguon_lead", "Hội thảo 2026", "doanh_thu_nam", "5000000000"),
                2L, Map.of("nguon_lead", "Online", "doanh_thu_nam", "2000000000")
        );

        String csv = service.xuatDuLieuExcelCSV("KHACH_HANG", headers, keys, records, customFields);

        assertNotNull(csv);
        // Kiểm tra BOM UTF-8
        assertTrue(csv.startsWith("\uFEFF"));
        // Kiểm tra Header có các cột chuẩn và các cột trường tuỳ chỉnh
        assertTrue(csv.contains("\"Mã KH\",\"Tên công ty\",\"Nguồn giới thiệu\",\"Doanh thu năm\""));
        // Kiểm tra dữ liệu được xuất đúng dòng và giá trị trường tuỳ chỉnh tương ứng
        assertTrue(csv.contains("\"KH-001\",\"Tập đoàn Viễn thông Viettel\",\"Hội thảo 2026\",\"5000000000\""));
        assertTrue(csv.contains("\"KH-002\",\"Công ty TNHH VNG\",\"Online\",\"2000000000\""));
    }
}
