package vn.nhom10.crm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.CapKhachHangTrungDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử thuật toán phát hiện trùng lặp khách hàng (Story S3-04, AC1)")
class DuplicateCustomerDetectorTest {

    @Test
    @DisplayName("AC1 MST: Phát hiện trùng khi 2 khách hàng có cùng Mã số thuế (bất kể định dạng dấu gạch/khoảng trắng)")
    void testPhatHienTrung_TheoMaSoThue_ChuanHoa() {
        BanGhiNghiepVuDTO a = new BanGhiNghiepVuDTO(
                1L, "KH-001", "Công ty Cổ phần Công nghệ FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A", 1L, "Nhóm Miền Bắc",
                "VIP", "Đang hợp tác", LocalDate.now(), "Mô tả",
                "0101-248-141", "https://fpt.com.vn"
        );

        BanGhiNghiepVuDTO b = new BanGhiNghiepVuDTO(
                2L, "KH-002", "FPT Telecom Chi nhánh Hà Nội", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "Tiềm năng", "Tiềm năng", LocalDate.now(), "Mô tả",
                "0101248141", "https://telecom.fpt.vn"
        );

        CapKhachHangTrungDTO cap = DuplicateCustomerDetector.kiemTraCapKhachHang(a, b);
        assertNotNull(cap, "Phải phát hiện trùng khi trùng mã số thuế");
        assertTrue(cap.isTrungMst(), "Cờ trungMst phải bật true");
        assertTrue(cap.isXungDotNhanVien(), "Hai nhân viên A (101) và B (102) phải được gắn cờ xung đột");
        assertTrue(cap.getDanhSachLyDo().stream().anyMatch(r -> r.contains("0101-248-141") || r.contains("Mã số thuế")));
    }

    @Test
    @DisplayName("AC1 Website: Phát hiện trùng khi 2 khách hàng có cùng tên miền website chính")
    void testPhatHienTrung_TheoWebsite_ChuanHoa() {
        BanGhiNghiepVuDTO a = new BanGhiNghiepVuDTO(
                1L, "KH-001", "Tập đoàn Viễn thông Viettel", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A", 1L, "Nhóm Miền Bắc",
                "VIP", "Đang hợp tác", LocalDate.now(), "Mô tả",
                "0100109106", "https://www.viettel.com.vn/gioi-thieu"
        );

        BanGhiNghiepVuDTO b = new BanGhiNghiepVuDTO(
                2L, "KH-002", "Trung tâm Giải pháp Doanh nghiệp Viettel", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "Tiềm năng", "Tiềm năng", LocalDate.now(), "Mô tả",
                null, "http://viettel.com.vn"
        );

        CapKhachHangTrungDTO cap = DuplicateCustomerDetector.kiemTraCapKhachHang(a, b);
        assertNotNull(cap, "Phải phát hiện trùng khi trùng website");
        assertTrue(cap.isTrungWebsite(), "Cờ trungWebsite phải bật true");
    }

    @Test
    @DisplayName("AC1 Tên công ty: Phát hiện trùng khi tên gần giống (loại bỏ loại hình pháp lý Cty, TNHH, CP)")
    void testPhatHienTrung_TheoTenCongTy_GanGiong() {
        BanGhiNghiepVuDTO a = new BanGhiNghiepVuDTO(
                1L, "KH-001", "Công ty Cổ phần Công nghệ Thông tin VNG", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A", 1L, "Nhóm Miền Bắc",
                "VIP", "Đang hợp tác", LocalDate.now(), "Mô tả",
                null, null
        );

        BanGhiNghiepVuDTO b = new BanGhiNghiepVuDTO(
                2L, "KH-002", "Công ty TNHH VNG", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                102L, "Trần Thị B", 1L, "Nhóm Miền Bắc",
                "Tiềm năng", "Tiềm năng", LocalDate.now(), "Mô tả",
                null, null
        );

        CapKhachHangTrungDTO cap = DuplicateCustomerDetector.kiemTraCapKhachHang(a, b);
        assertNotNull(cap, "Phải phát hiện trùng tên công ty gần giống");
        assertTrue(cap.isTrungTen(), "Cờ trungTen phải bật true");
        assertTrue(cap.getTyLeTuongDongTen() >= 70, "Tỷ lệ tương đồng phải >= 70%");
    }

    @Test
    @DisplayName("AC1 An toàn: Hai công ty hoàn toàn khác nhau không bị báo trùng")
    void testKhongTrung_KhiKhacBietHoanToan() {
        BanGhiNghiepVuDTO a = new BanGhiNghiepVuDTO(
                1L, "KH-001", "Công ty Cổ phần Sữa Vinamilk", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A", 1L, "Nhóm Miền Bắc",
                "VIP", "Đang hợp tác", LocalDate.now(), "Mô tả",
                "0300588569", "https://vinamilk.com.vn"
        );

        BanGhiNghiepVuDTO b = new BanGhiNghiepVuDTO(
                2L, "KH-002", "Ngân hàng Thương mại Cổ phần Ngoại thương Vietcombank", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                101L, "Nguyễn Văn A", 1L, "Nhóm Miền Bắc",
                "VIP", "Đang hợp tác", LocalDate.now(), "Mô tả",
                "0100112437", "https://vietcombank.com.vn"
        );

        CapKhachHangTrungDTO cap = DuplicateCustomerDetector.kiemTraCapKhachHang(a, b);
        assertNull(cap, "Hai khách hàng khác nhau tuyệt đối không được báo trùng");
    }

    @Test
    @DisplayName("AC1 Quét danh sách: Tìm thấy đầy đủ các cặp trùng trong danh mục nhiều khách hàng")
    void testQuetDanhSachTrung_TimThayTatCaCacCap() {
        List<BanGhiNghiepVuDTO> list = new ArrayList<>();
        list.add(new BanGhiNghiepVuDTO(1L, "KH-1", "Công ty FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG, 101L, "A", 1L, "N1", "", "", LocalDate.now(), "", "0101248141", "fpt.com.vn"));
        list.add(new BanGhiNghiepVuDTO(2L, "KH-2", "Viettel", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG, 102L, "B", 1L, "N1", "", "", LocalDate.now(), "", "0100109106", "viettel.com.vn"));
        list.add(new BanGhiNghiepVuDTO(3L, "KH-3", "Tập đoàn FPT", BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG, 102L, "B", 1L, "N1", "", "", LocalDate.now(), "", "0101248141", "fpt.com.vn"));

        List<CapKhachHangTrungDTO> dsCap = DuplicateCustomerDetector.quetDanhSachTrung(list);
        assertEquals(1, dsCap.size(), "Phải phát hiện chính xác 1 cặp trùng (KH-1 và KH-3)");
        assertEquals("1-3", dsCap.get(0).getId());
    }
}
