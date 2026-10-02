package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import vn.nhom10.crm.dao.DoiThuDAO;
import vn.nhom10.crm.dao.LyDoThangThuaDAO;
import vn.nhom10.crm.dto.KetQuaKiemTraDongCoHoiDTO;
import vn.nhom10.crm.model.DoiThu;
import vn.nhom10.crm.model.LyDoThangThua;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử nghiệp vụ LyDoThangThuaService (Story S2-10 AC1, AC2, AC3)")
class LyDoThangThuaServiceTest {

    private LyDoThangThuaDAO lyDoDAO;
    private DoiThuDAO doiThuDAO;
    private LyDoThangThuaService service;

    @BeforeEach
    void setUp() {
        lyDoDAO = Mockito.mock(LyDoThangThuaDAO.class);
        doiThuDAO = Mockito.mock(DoiThuDAO.class);
        service = new LyDoThangThuaService(lyDoDAO, doiThuDAO);
    }

    // =========================================================================
    // PHÂN QUYỀN SERVER-SIDE
    // =========================================================================

    @Test
    @DisplayName("Kiểm tra phân quyền server-side: Cho phép Giám đốc và Admin, chặn các vai trò khác")
    void testKiemTraQuyenQuanLy() {
        // Null user -> Lỗi
        assertThrows(LoiPhanQuyenException.class, () -> service.kiemTraQuyenQuanLy(null));

        // User bị khóa -> Lỗi
        NguoiDung lockedUser = new NguoiDung();
        lockedUser.setId(1L);
        lockedUser.setTrangThai(NguoiDung.TRANG_THAI_KHOA);
        lockedUser.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.DIRECTOR)));
        assertThrows(LoiPhanQuyenException.class, () -> service.kiemTraQuyenQuanLy(lockedUser));

        // Sales Rep -> Lỗi
        NguoiDung sales = new NguoiDung();
        sales.setId(2L);
        sales.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        sales.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP)));
        assertThrows(LoiPhanQuyenException.class, () -> service.kiemTraQuyenQuanLy(sales));

        // Director -> Hợp lệ
        NguoiDung director = new NguoiDung();
        director.setId(3L);
        director.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        director.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.DIRECTOR)));
        assertDoesNotThrow(() -> service.kiemTraQuyenQuanLy(director));

        // Admin -> Hợp lệ
        NguoiDung admin = new NguoiDung();
        admin.setId(4L);
        admin.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        admin.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.ADMIN)));
        assertDoesNotThrow(() -> service.kiemTraQuyenQuanLy(admin));
    }

    // =========================================================================
    // AC1: DANH SÁCH LÝ DO THẮNG VÀ LÝ DO THUA KHAI BÁO RIÊNG
    // =========================================================================

    @Test
    @DisplayName("AC1: Lấy riêng danh sách lý do thắng và lý do thua")
    void testLayDanhSachRieng() {
        LyDoThangThua win = new LyDoThangThua("WIN_PRICE", "Giá tốt", LyDoThangThua.LOAI_THANG, 1, true);
        LyDoThangThua loss = new LyDoThangThua("LOSS_FEATURE", "Thiếu tính năng", LyDoThangThua.LOAI_THUA, 1, true);

        when(lyDoDAO.layTheoLoai(LyDoThangThua.LOAI_THANG)).thenReturn(List.of(win));
        when(lyDoDAO.layTheoLoai(LyDoThangThua.LOAI_THUA)).thenReturn(List.of(loss));

        List<LyDoThangThua> dsThang = service.layDanhSachLyDoThang();
        assertEquals(1, dsThang.size());
        assertTrue(dsThang.get(0).laLyDoThang());

        List<LyDoThangThua> dsThua = service.layDanhSachLyDoThua();
        assertEquals(1, dsThua.size());
        assertTrue(dsThua.get(0).laLyDoThua());
    }

    @Test
    @DisplayName("Validate khi lưu lý do: Mã hợp lệ, tên không rỗng, loại hợp lệ")
    void testValidateLuuLyDo() {
        // Lỗi null
        assertThrows(IllegalArgumentException.class, () -> service.luuLyDo(null));

        // Lỗi mã rỗng
        LyDoThangThua item = new LyDoThangThua("", "Tên", LyDoThangThua.LOAI_THANG, 0, true);
        assertThrows(IllegalArgumentException.class, () -> service.luuLyDo(item));

        // Lỗi mã chứa ký tự đặc biệt không hợp lệ
        item.setMaLyDo("WIN@PRICE#$");
        assertThrows(IllegalArgumentException.class, () -> service.luuLyDo(item));

        // Lỗi tên rỗng
        item.setMaLyDo("WIN_PRICE");
        item.setTenLyDo("   ");
        assertThrows(IllegalArgumentException.class, () -> service.luuLyDo(item));

        // Lỗi loại không hợp lệ
        item.setTenLyDo("Giá hợp lý");
        item.setLoai("UNKNOWN_TYPE");
        assertThrows(IllegalArgumentException.class, () -> service.luuLyDo(item));

        // Lỗi trùng mã
        item.setLoai(LyDoThangThua.LOAI_THANG);
        when(lyDoDAO.tonTaiMa(eq("WIN_PRICE"), isNull())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.luuLyDo(item));

        // Hợp lệ -> tạo mới thành công
        when(lyDoDAO.tonTaiMa(eq("WIN_PRICE"), isNull())).thenReturn(false);
        when(lyDoDAO.taoMoi(any(LyDoThangThua.class))).thenReturn(10L);
        assertDoesNotThrow(() -> service.luuLyDo(item));
    }

    @Test
    @DisplayName("Chặn xóa lý do khi đang được cơ hội bán hàng tham chiếu")
    void testChanXoaLyDoKhiCoThamChieu() {
        when(lyDoDAO.demSoCoHoiThamChieu(1L)).thenReturn(3);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.xoaLyDo(1L));
        assertTrue(ex.getMessage().contains("đang được 3 cơ hội bán hàng tham chiếu"));

        verify(lyDoDAO, never()).xoa(1L);
    }

    // =========================================================================
    // AC2: DANH SÁCH ĐỐI THỦ CẠNH TRANH
    // =========================================================================

    @Test
    @DisplayName("AC2: Quản lý danh sách đối thủ cạnh tranh")
    void testQuanLyDoiThu() {
        DoiThu dt = new DoiThu("DT_MISA", "Công ty MISA", "https://misa.vn", "Phần mềm tốt", true);
        when(doiThuDAO.layTatCa()).thenReturn(List.of(dt));

        List<DoiThu> ds = service.layDanhSachDoiThu();
        assertEquals(1, ds.size());
        assertEquals("DT_MISA", ds.get(0).getMaDoiThu());

        // Test trùng mã đối thủ
        when(doiThuDAO.tonTaiMa(eq("DT_MISA"), isNull())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.luuDoiThu(dt));

        // Test chặn xóa đối thủ khi có cơ hội tham chiếu
        when(doiThuDAO.demSoCoHoiThamChieu(2L)).thenReturn(5);
        assertThrows(IllegalStateException.class, () -> service.xoaDoiThu(2L));
        verify(doiThuDAO, never()).xoa(2L);
    }

    // =========================================================================
    // AC3: KIỂM TRA QUY TẮC RÀNG BUỘC KHI ĐÓNG CƠ HỘI SPRINT 5 (S5-05)
    // =========================================================================

    @Test
    @DisplayName("AC3: Đóng THẮNG bắt buộc chọn Lý do thắng, nhập Giá trị chốt và Ngày ký")
    void testDongThangBatBuocDuLieu() {
        // 1. Thiếu lý do thắng -> Lỗi
        KetQuaKiemTraDongCoHoiDTO kq1 = service.kiemTraDongCoHoiSprint5(
                "THANG", null, null, new BigDecimal("100000000"), LocalDate.now(), null);
        assertFalse(kq1.isHopLe());
        assertTrue(kq1.getDanhSachLoi().stream().anyMatch(e -> e.contains("bắt buộc phải chọn Lý do thắng")));

        // 2. Chọn nhầm lý do thua khi đóng thắng -> Lỗi
        LyDoThangThua lyDoThua = new LyDoThangThua(1L, "LOSS_1", "Giá cao", LyDoThangThua.LOAI_THUA, 1, true, null);
        when(lyDoDAO.timTheoId(1L)).thenReturn(lyDoThua);

        KetQuaKiemTraDongCoHoiDTO kq2 = service.kiemTraDongCoHoiSprint5(
                "THANG", 1L, null, new BigDecimal("100000000"), LocalDate.now(), null);
        assertFalse(kq2.isHopLe());
        assertTrue(kq2.getDanhSachLoi().stream().anyMatch(e -> e.contains("không thuộc danh mục Lý do thắng")));

        // 3. Thiếu giá trị chốt thực tế -> Lỗi
        LyDoThangThua lyDoThang = new LyDoThangThua(2L, "WIN_1", "Tính năng vượt trội", LyDoThangThua.LOAI_THANG, 1, true, null);
        when(lyDoDAO.timTheoId(2L)).thenReturn(lyDoThang);

        KetQuaKiemTraDongCoHoiDTO kq3 = service.kiemTraDongCoHoiSprint5(
                "THANG", 2L, null, BigDecimal.ZERO, LocalDate.now(), null);
        assertFalse(kq3.isHopLe());
        assertTrue(kq3.getDanhSachLoi().stream().anyMatch(e -> e.contains("Giá trị chốt thực tế lớn hơn 0")));

        // 4. Thiếu ngày ký -> Lỗi
        KetQuaKiemTraDongCoHoiDTO kq4 = service.kiemTraDongCoHoiSprint5(
                "THANG", 2L, null, new BigDecimal("50000000"), null, null);
        assertFalse(kq4.isHopLe());
        assertTrue(kq4.getDanhSachLoi().stream().anyMatch(e -> e.contains("Ngày ký hợp đồng")));

        // 5. Đầy đủ dữ liệu hợp lệ -> ĐẠT
        KetQuaKiemTraDongCoHoiDTO kq5 = service.kiemTraDongCoHoiSprint5(
                "THANG", 2L, null, new BigDecimal("50000000"), LocalDate.now(), "Thương vụ thành công");
        assertTrue(kq5.isHopLe());
        assertTrue(kq5.getDanhSachLoi().isEmpty());
        assertTrue(kq5.getThongBaoChiTiet().contains("HỢP LỆ THEO SPRINT 5"));
    }

    @Test
    @DisplayName("AC3: Đóng THUA bắt buộc chọn Lý do thua")
    void testDongThuaBatBuocDuLieu() {
        // 1. Thiếu lý do thua -> Lỗi
        KetQuaKiemTraDongCoHoiDTO kq1 = service.kiemTraDongCoHoiSprint5(
                "THUA", null, null, null, null, null);
        assertFalse(kq1.isHopLe());
        assertTrue(kq1.getDanhSachLoi().stream().anyMatch(e -> e.contains("bắt buộc phải chọn Lý do thua")));

        // 2. Chọn nhầm lý do thắng khi đóng thua -> Lỗi
        LyDoThangThua lyDoThang = new LyDoThangThua(2L, "WIN_1", "Tính năng vượt trội", LyDoThangThua.LOAI_THANG, 1, true, null);
        when(lyDoDAO.timTheoId(2L)).thenReturn(lyDoThang);

        KetQuaKiemTraDongCoHoiDTO kq2 = service.kiemTraDongCoHoiSprint5(
                "THUA", 2L, null, null, null, null);
        assertFalse(kq2.isHopLe());
        assertTrue(kq2.getDanhSachLoi().stream().anyMatch(e -> e.contains("không thuộc danh mục Lý do thua")));

        // 3. Đầy đủ lý do thua và có đối thủ cạnh tranh -> ĐẠT
        LyDoThangThua lyDoThua = new LyDoThangThua(3L, "LOSS_DT", "Thua đối thủ về giá", LyDoThangThua.LOAI_THUA, 1, true, null);
        DoiThu doiThu = new DoiThu(10L, "DT_MISA", "Công ty MISA", "https://misa.vn", "Phần mềm", true, null, null);
        when(lyDoDAO.timTheoId(3L)).thenReturn(lyDoThua);
        when(doiThuDAO.timTheoId(10L)).thenReturn(doiThu);

        KetQuaKiemTraDongCoHoiDTO kq3 = service.kiemTraDongCoHoiSprint5(
                "THUA", 3L, 10L, null, null, "Khách hàng chọn đối thủ MISA");
        assertTrue(kq3.isHopLe());
        assertTrue(kq3.getDanhSachLoi().isEmpty());
        assertTrue(kq3.getThongBaoChiTiet().contains("Đối thủ thắng thầu: Công ty MISA"));
    }

    @Test
    @DisplayName("Nạp dữ liệu mẫu chuẩn khi danh mục trống")
    void testNapDuLieuMauNeuTrong() {
        when(lyDoDAO.layTheoLoai(LyDoThangThua.LOAI_THANG)).thenReturn(List.of());
        when(lyDoDAO.layTheoLoai(LyDoThangThua.LOAI_THUA)).thenReturn(List.of());
        when(doiThuDAO.layTatCa()).thenReturn(List.of());

        when(lyDoDAO.tonTaiMa(anyString(), any())).thenReturn(false);
        when(lyDoDAO.taoMoi(any(LyDoThangThua.class))).thenReturn(1L);
        when(doiThuDAO.tonTaiMa(anyString(), any())).thenReturn(false);
        when(doiThuDAO.taoMoi(any(DoiThu.class))).thenReturn(1L);

        int dem = service.napDuLieuMauNeuTrong();
        assertEquals(12, dem);
        verify(lyDoDAO, times(8)).taoMoi(any(LyDoThangThua.class));
        verify(doiThuDAO, times(4)).taoMoi(any(DoiThu.class));
    }
}
