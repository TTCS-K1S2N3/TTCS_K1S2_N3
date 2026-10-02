package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.ThongTinLoi;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ xử lý và báo lỗi BaoLoiService (Story S1-07)")
class BaoLoiServiceTest {

    private BaoLoiService service;
    private static final String CONTEXT_PATH = "/crm-ban-hang";

    @BeforeEach
    void setUp() {
        service = new BaoLoiService();
    }

    @Test
    @DisplayName("AC1 & AC2 - Lỗi 403: Không đủ quyền / ngoài phạm vi - Cung cấp giao diện cảnh báo và gợi ý về bàn làm việc")
    void testLoi403_KhongDuQuyen() {
        ThongTinLoi thongTin = service.taoThongTinLoi(403, "/admin/cai-dat", null, CONTEXT_PATH, null);

        assertNotNull(thongTin);
        assertEquals(403, thongTin.getMaLoi());
        assertTrue(thongTin.getTieuDe().contains("403"));
        assertTrue(thongTin.getMoTa().contains("quyền"));
        assertEquals("warning", thongTin.getLoaiGiaoDien());

        // AC2: Hành động gợi ý quay lại luồng làm việc
        assertNotNull(thongTin.getUrlHanhDongChinh());
        assertNotNull(thongTin.getTenHanhDongChinh());
        assertTrue(thongTin.getTenHanhDongChinh().contains("Bàn Làm Việc") || thongTin.getTenHanhDongChinh().contains("Trang"));
        assertEquals(CONTEXT_PATH, thongTin.getUrlHanhDongChinh());

        assertNotNull(thongTin.getUrlHanhDongPhu());
        assertTrue(thongTin.getUrlHanhDongPhu().contains("/dang-nhap"));
    }

    @Test
    @DisplayName("AC1 & AC2 - Lỗi 404: Truy cập nhầm chỗ - Cung cấp giao diện thông tin và gợi ý về trang chủ / trang trước")
    void testLoi404_TruyCapNhamCho() {
        String uriSai = "/khach-hang/khong-ton-tai-123";
        ThongTinLoi thongTin = service.taoThongTinLoi(404, uriSai, null, CONTEXT_PATH, null);

        assertNotNull(thongTin);
        assertEquals(404, thongTin.getMaLoi());
        assertTrue(thongTin.getTieuDe().contains("404"));
        assertTrue(thongTin.getMoTa().contains("không tồn tại"));
        assertTrue(thongTin.getChiTiet().contains(uriSai));
        assertEquals("info", thongTin.getLoaiGiaoDien());

        // AC2: Hành động gợi ý quay lại luồng làm việc
        assertNotNull(thongTin.getUrlHanhDongChinh());
        assertEquals(CONTEXT_PATH, thongTin.getUrlHanhDongChinh());
        assertTrue(thongTin.getTenHanhDongChinh().contains("Trang Chủ"));

        assertNotNull(thongTin.getUrlHanhDongPhu());
        assertTrue(thongTin.getUrlHanhDongPhu().contains("history.back"));
    }

    @Test
    @DisplayName("AC1 & AC2 - Lỗi 500: Sự cố hệ thống - Cung cấp mã tham chiếu an toàn và gợi ý tải lại trang")
    void testLoi500_SuCoHeThong() {
        Exception exception = new RuntimeException("Lỗi kết nối cơ sở dữ liệu");
        ThongTinLoi thongTin = service.taoThongTinLoi(500, "/co-hoi/danh-sach", exception, CONTEXT_PATH, null);

        assertNotNull(thongTin);
        assertEquals(500, thongTin.getMaLoi());
        assertTrue(thongTin.getTieuDe().contains("500"));
        assertEquals("danger", thongTin.getLoaiGiaoDien());

        // Bảo mật: Mã tham chiếu được sinh ra, không để lộ thông tin lỗi kỹ thuật
        assertNotNull(thongTin.getMaThamChieu());
        assertTrue(thongTin.getMaThamChieu().startsWith("ERR-"));
        assertTrue(thongTin.getChiTiet().contains(thongTin.getMaThamChieu()));
        assertFalse(thongTin.getChiTiet().contains("SQLException"));

        // AC2: Hành động gợi ý quay lại luồng làm việc
        assertNotNull(thongTin.getUrlHanhDongChinh());
        assertTrue(thongTin.getUrlHanhDongChinh().contains("reload"));
        assertNotNull(thongTin.getUrlHanhDongPhu());
        assertEquals(CONTEXT_PATH, thongTin.getUrlHanhDongPhu());
    }

    @Test
    @DisplayName("Hỗ trợ thông điệp tùy chỉnh khi nghiệp vụ yêu cầu thông báo cụ thể")
    void testThongBaoTuyChon() {
        String thongBaoTuyChon = "Khách hàng này đã được bàn giao cho nhân viên khác. Bạn không thể xem hồ sơ này.";
        ThongTinLoi thongTin = service.taoThongTinLoi(403, "/khach-hang/100", null, CONTEXT_PATH, thongBaoTuyChon);

        assertEquals(403, thongTin.getMaLoi());
        assertEquals(thongBaoTuyChon, thongTin.getMoTa());
        assertNotNull(thongTin.getUrlHanhDongChinh());
    }
}
