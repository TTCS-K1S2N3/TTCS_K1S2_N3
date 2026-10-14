package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.ChienDichDAO;
import vn.nhom10.crm.dto.*;
import vn.nhom10.crm.model.ChienDich;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử ChienDichService - Story S4-03")
class ChienDichServiceTest {

    @Mock
    private ChienDichDAO chienDichDAO;

    private ChienDichService service;

    @BeforeEach
    void setUp() {
        service = new ChienDichService(chienDichDAO);
    }

    @Test
    @DisplayName("AC1: Khai báo chiến dịch hợp lệ tạo thành công và sinh mã tự động")
    void testTaoChienDichHopLe() {
        when(chienDichDAO.kiemTraTonTaiMa(anyString())).thenReturn(false);
        when(chienDichDAO.them(any(ChienDich.class))).thenReturn(101L);

        ChienDichDTO form = new ChienDichDTO();
        form.setName("Sự kiện sản phẩm tháng 10");
        form.setBudgetAmountStr("12000000");
        form.setStartDateStr("2026-10-01");
        form.setEndDateStr("2026-10-31");
        form.setChannelCode("EVENT");
        form.setMoTa("Mô tả sự kiện ra mắt");

        Map<String, String> fieldErrors = new HashMap<>();
        Long createdId = service.taoChienDich(form, 1L, fieldErrors);

        assertNotNull(createdId);
        assertEquals(101L, createdId);
        assertTrue(fieldErrors.isEmpty());
        assertNotNull(form.getMaChienDich());
        assertTrue(form.getMaChienDich().startsWith("CD-"));
        verify(chienDichDAO, times(1)).them(any(ChienDich.class));
    }

    @Test
    @DisplayName("AC1: Từ chối khi thiếu tên chiến dịch")
    void testTaoChienDichThieuTen() {
        ChienDichDTO form = new ChienDichDTO();
        form.setName("   "); // Trắng
        form.setBudgetAmountStr("5000000");
        form.setStartDateStr("2026-10-01");
        form.setEndDateStr("2026-10-31");
        form.setChannelCode("SOCIAL");

        Map<String, String> fieldErrors = new HashMap<>();
        Long createdId = service.taoChienDich(form, 1L, fieldErrors);

        assertNull(createdId);
        assertTrue(fieldErrors.containsKey("name"));
        assertEquals("Tên chiến dịch không được để trống.", fieldErrors.get("name"));
        verify(chienDichDAO, never()).them(any(ChienDich.class));
    }

    @Test
    @DisplayName("AC1 / MOCK 03 / CT-02: Ngân sách âm bị từ chối")
    void testTaoChienDichNganSachAm() {
        ChienDichDTO form = new ChienDichDTO();
        form.setName("Sự kiện thử");
        form.setBudgetAmountStr("-100");
        form.setStartDateStr("2026-10-01");
        form.setEndDateStr("2026-10-31");
        form.setChannelCode("EVENT");

        Map<String, String> fieldErrors = new HashMap<>();
        Long createdId = service.taoChienDich(form, 1L, fieldErrors);

        assertNull(createdId);
        assertTrue(fieldErrors.containsKey("budgetAmount"));
        assertEquals("Ngân sách phải là số không âm.", fieldErrors.get("budgetAmount"));
        verify(chienDichDAO, never()).them(any(ChienDich.class));
    }

    @Test
    @DisplayName("AC1 / MOCK 03 / CT-02: Ngày kết thúc trước ngày bắt đầu bị từ chối")
    void testTaoChienDichNgayKetThucTruocNgayBatDau() {
        ChienDichDTO form = new ChienDichDTO();
        form.setName("Chiến dịch đảo ngày");
        form.setBudgetAmountStr("5000000");
        form.setStartDateStr("2026-10-31");
        form.setEndDateStr("2026-10-01"); // Đảo chiều
        form.setChannelCode("SEARCH");

        Map<String, String> fieldErrors = new HashMap<>();
        Long createdId = service.taoChienDich(form, 1L, fieldErrors);

        assertNull(createdId);
        assertTrue(fieldErrors.containsKey("endDate"));
        assertEquals("Ngày kết thúc không được trước ngày bắt đầu.", fieldErrors.get("endDate"));
        verify(chienDichDAO, never()).them(any(ChienDich.class));
    }

    @Test
    @DisplayName("AC1: Kênh không thuộc danh mục bị từ chối")
    void testTaoChienDichKenhKhongHopLe() {
        ChienDichDTO form = new ChienDichDTO();
        form.setName("Chiến dịch kênh lạ");
        form.setBudgetAmountStr("5000000");
        form.setStartDateStr("2026-10-01");
        form.setEndDateStr("2026-10-31");
        form.setChannelCode("UNKNOWN_CHANNEL");

        Map<String, String> fieldErrors = new HashMap<>();
        Long createdId = service.taoChienDich(form, 1L, fieldErrors);

        assertNull(createdId);
        assertTrue(fieldErrors.containsKey("channelCode"));
        verify(chienDichDAO, never()).them(any(ChienDich.class));
    }

    @Test
    @DisplayName("AC3: Lấy danh sách chiến dịch kèm chỉ số KPI phân trang")
    void testLayDanhSachChienDichPhanTrang() {
        ChienDich cd1 = new ChienDich("CD-01", "Chiến dịch 1", new BigDecimal("10000000"), "EVENT", LocalDate.now(), LocalDate.now());
        cd1.setId(1L);

        when(chienDichDAO.demTongSo()).thenReturn(1L);
        when(chienDichDAO.layDanhSachPhanTrang(0, 20)).thenReturn(List.of(cd1));

        Map<Long, ChienDichThongKeDTO> metricsMap = new HashMap<>();
        metricsMap.put(1L, new ChienDichThongKeDTO(1L, 5, 2, new BigDecimal("30000000")));
        when(chienDichDAO.layThongKeChoDanhSach(List.of(1L))).thenReturn(metricsMap);

        Map<String, Object> result = service.layDanhSachChienDich(1, 20);

        assertNotNull(result);
        assertTrue(result.containsKey("campaignRows"));
        assertTrue(result.containsKey("pagination"));

        List<ChienDichRowDTO> rows = (List<ChienDichRowDTO>) result.get("campaignRows");
        assertEquals(1, rows.size());
        assertEquals("Chiến dịch 1", rows.get(0).getCampaign().getName());
        assertEquals(5, rows.get(0).getMetrics().getLeadCount());
        assertEquals(2, rows.get(0).getMetrics().getOpportunityCount());
        assertEquals(new BigDecimal("30000000"), rows.get(0).getMetrics().getClosedWonAmount());
    }

    @Test
    @DisplayName("AC3: Lấy chi tiết chiến dịch thành công và khi không tìm thấy")
    void testLayChiTietChienDich() {
        ChienDich cd = new ChienDich("CD-02", "Chiến dịch chi tiết", new BigDecimal("5000000"), "SOCIAL", LocalDate.now(), LocalDate.now());
        cd.setId(2L);

        when(chienDichDAO.timTheoId(2L)).thenReturn(cd);
        when(chienDichDAO.layThongKeChiTieu(2L)).thenReturn(new ChienDichThongKeDTO(2L, 10, 3, new BigDecimal("15000000")));

        ChienDichRowDTO row = service.layChiTietChienDich(2L);
        assertNotNull(row);
        assertEquals("Chiến dịch chi tiết", row.getCampaign().getName());
        assertEquals(10, row.getMetrics().getLeadCount());

        when(chienDichDAO.timTheoId(999L)).thenReturn(null);
        ChienDichRowDTO notFound = service.layChiTietChienDich(999L);
        assertNull(notFound);
    }

    @Test
    @DisplayName("Phân quyền: Marketing, Admin, Director, Team Lead có toàn quyền, Sales Rep chỉ xem")
    void testPhanQuyenNguoiDung() {
        // Marketing
        NguoiDung marketingUser = new NguoiDung();
        marketingUser.setTrangThai("HOAT_DONG");
        marketingUser.themVaiTro(VaiTroEnum.MARKETING);

        ChienDichPermissionsDTO mktPerms = service.layQuyenNguoiDung(marketingUser);
        assertTrue(mktPerms.isCanView());
        assertTrue(mktPerms.isCanCreate());
        assertTrue(mktPerms.isCanViewTotals());

        // Admin
        NguoiDung adminUser = new NguoiDung();
        adminUser.setTrangThai("HOAT_DONG");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);

        ChienDichPermissionsDTO adminPerms = service.layQuyenNguoiDung(adminUser);
        assertTrue(adminPerms.isCanView());
        assertTrue(adminPerms.isCanCreate());
        assertTrue(adminPerms.isCanViewTotals());

        // Sales Rep
        NguoiDung salesUser = new NguoiDung();
        salesUser.setTrangThai("HOAT_DONG");
        salesUser.themVaiTro(VaiTroEnum.SALES_REP);

        ChienDichPermissionsDTO salesPerms = service.layQuyenNguoiDung(salesUser);
        assertTrue(salesPerms.isCanView());
        assertFalse(salesPerms.isCanCreate());
        assertFalse(salesPerms.isCanViewTotals());

        // User khoá hoặc null
        ChienDichPermissionsDTO nullPerms = service.layQuyenNguoiDung(null);
        assertFalse(nullPerms.isCanView());
    }
}
