package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dto.MucDanhMucDTO;
import vn.nhom10.crm.model.LoaiDanhMuc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử nghiệp vụ Quản lý danh mục bán hàng dùng chung (Story S2-07)")
class DanhMucBanHangServiceTest {

    @Mock
    private DanhMucBanHangDAO dao;

    private DanhMucBanHangService service;

    @BeforeEach
    void setUp() {
        service = new DanhMucBanHangService(dao);
    }

    @Test
    @DisplayName("AC1: Lấy danh sách mục theo từng loại danh mục khi đã có dữ liệu")
    void testLayDanhSachTheoLoai_CoSanDuLieu() {
        MucDanhMucDTO item1 = new MucDanhMucDTO(1L, LoaiDanhMuc.NGANH_NGHE, "CNTT", "Công nghệ thông tin", "Mô tả CNTT", 1, true, 2, LocalDate.now(), "Admin");
        MucDanhMucDTO item2 = new MucDanhMucDTO(2L, LoaiDanhMuc.NGANH_NGHE, "BAN_LE", "Bán lẻ", "Mô tả Bán lẻ", 2, true, 0, LocalDate.now(), "Admin");
        when(dao.layDanhSach(LoaiDanhMuc.NGANH_NGHE, false)).thenReturn(Arrays.asList(item1, item2));

        List<MucDanhMucDTO> ketQua = service.layDanhSachTheoLoai(LoaiDanhMuc.NGANH_NGHE);

        assertNotNull(ketQua);
        assertEquals(2, ketQua.size());
        assertEquals("CNTT", ketQua.get(0).getMaMuc());
        assertEquals("BAN_LE", ketQua.get(1).getMaMuc());
        verify(dao, atLeastOnce()).layDanhSach(LoaiDanhMuc.NGANH_NGHE, false);
    }

    @Test
    @DisplayName("AC1: Thêm mới mục danh mục thành công khi mã hợp lệ và chưa tồn tại")
    void testThemMuc_ThanhCong() {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.NGUON_LEAD);
        dto.setMaMuc("TIKTOK_ADS");
        dto.setTenMuc("Quảng cáo TikTok Shop");
        dto.setMoTa("Chiến dịch chuyển đổi từ TikTok");
        dto.setKichHoat(true);

        when(dao.kiemTraTonTaiMa(LoaiDanhMuc.NGUON_LEAD, "TIKTOK_ADS", null)).thenReturn(false);
        when(dao.layDanhSach(LoaiDanhMuc.NGUON_LEAD, false)).thenReturn(Collections.emptyList());
        when(dao.them(eq(LoaiDanhMuc.NGUON_LEAD), any(MucDanhMucDTO.class))).thenReturn(100L);

        long newId = service.themMuc(dto);

        assertEquals(100L, newId);
        assertEquals(100L, dto.getId());
        assertEquals(1, dto.getThuTuHienThi());
        verify(dao).them(eq(LoaiDanhMuc.NGUON_LEAD), eq(dto));
    }

    @Test
    @DisplayName("AC1: Thêm mới thất bại khi mã định danh bị trùng")
    void testThemMuc_TrungMa_NémNgoạiLệ() {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setLoaiDanhMuc(LoaiDanhMuc.NGANH_NGHE);
        dto.setMaMuc("CNTT");
        dto.setTenMuc("Công nghệ thông tin");

        when(dao.kiemTraTonTaiMa(LoaiDanhMuc.NGANH_NGHE, "CNTT", null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.themMuc(dto));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
        verify(dao, never()).them(any(), any());
    }

    @Test
    @DisplayName("AC1: Thêm mới thất bại khi thiếu thông tin bắt buộc")
    void testThemMuc_ThieuThongTin_NémNgoạiLệ() {
        MucDanhMucDTO dtoThieuMa = new MucDanhMucDTO();
        dtoThieuMa.setLoaiDanhMuc(LoaiDanhMuc.QUY_MO);
        dtoThieuMa.setTenMuc("Quy mô vừa");

        assertThrows(IllegalArgumentException.class, () -> service.themMuc(dtoThieuMa));

        MucDanhMucDTO dtoThieuTen = new MucDanhMucDTO();
        dtoThieuTen.setLoaiDanhMuc(LoaiDanhMuc.QUY_MO);
        dtoThieuTen.setMaMuc("QM_VUA");

        assertThrows(IllegalArgumentException.class, () -> service.themMuc(dtoThieuTen));
    }

    @Test
    @DisplayName("AC1: Cập nhật mục danh mục thành công")
    void testCapNhatMuc_ThanhCong() {
        MucDanhMucDTO dto = new MucDanhMucDTO();
        dto.setId(5L);
        dto.setLoaiDanhMuc(LoaiDanhMuc.LOAI_HOAT_DONG);
        dto.setTenMuc("Cuộc gọi tư vấn chuyên sâu");
        dto.setMoTa("Cuộc gọi có ghi âm và kịch bản");
        dto.setKichHoat(true);

        when(dao.capNhat(eq(LoaiDanhMuc.LOAI_HOAT_DONG), eq(dto))).thenReturn(true);

        boolean ok = service.capNhatMuc(dto);
        assertTrue(ok);
        verify(dao).capNhat(eq(LoaiDanhMuc.LOAI_HOAT_DONG), eq(dto));
    }

    @Test
    @DisplayName("AC2: Giá trị đang được tham chiếu thì KHÔNG xóa được (ném lỗi có thông báo chi tiết)")
    void testXoaMuc_DangDuocThamChieu_ChanXoa() {
        long id = 1L;
        LoaiDanhMuc loai = LoaiDanhMuc.NGANH_NGHE;

        MucDanhMucDTO item = new MucDanhMucDTO(id, loai, "CNTT", "Công nghệ thông tin", "Mô tả", 1, true, 5, LocalDate.now(), "Admin");
        when(dao.layTheoId(loai, id)).thenReturn(item);
        // Có 5 bản ghi đang tham chiếu
        when(dao.demSoLuongThamChieu(loai, id)).thenReturn(5);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.xoaMuc(loai, id));
        assertTrue(ex.getMessage().contains("Không thể xóa"));
        assertTrue(ex.getMessage().contains("5 bản ghi nghiệp vụ đang tham chiếu"));

        // Tuyệt đối không được gọi hàm xóa trong DAO
        verify(dao, never()).xoa(any(), anyLong());
    }

    @Test
    @DisplayName("AC2: Giá trị KHÔNG được tham chiếu (0 bản ghi) thì xóa thành công")
    void testXoaMuc_KhongCoThamChieu_XoaThanhCong() {
        long id = 2L;
        LoaiDanhMuc loai = LoaiDanhMuc.NGANH_NGHE;

        MucDanhMucDTO item = new MucDanhMucDTO(id, loai, "DU_LICH", "Du lịch & Khách sạn", "Mô tả", 9, true, 0, LocalDate.now(), "Admin");
        when(dao.layTheoId(loai, id)).thenReturn(item);
        when(dao.demSoLuongThamChieu(loai, id)).thenReturn(0);
        when(dao.xoa(loai, id)).thenReturn(true);

        boolean ketQua = service.xoaMuc(loai, id);

        assertTrue(ketQua);
        verify(dao).xoa(loai, id);
    }

    @Test
    @DisplayName("AC3: Sắp xếp thứ tự hiển thị - Di chuyển mục lên trên thành công")
    void testThayDoiThuTu_DiChuyenLen() {
        LoaiDanhMuc loai = LoaiDanhMuc.QUY_MO;
        MucDanhMucDTO item1 = new MucDanhMucDTO(10L, loai, "DUOI_10", "Dưới 10", "", 1, true, 0, LocalDate.now(), "Admin");
        MucDanhMucDTO item2 = new MucDanhMucDTO(20L, loai, "TU_10_50", "Từ 10-50", "", 2, true, 0, LocalDate.now(), "Admin");

        when(dao.layDanhSach(loai, false)).thenReturn(Arrays.asList(item1, item2));

        // Di chuyển item2 lên trên (vị trí 1 thay cho item1)
        boolean ketQua = service.thayDoiThuTu(loai, 20L, true);

        assertTrue(ketQua);
        verify(dao).capNhatThuTu(loai, 20L, 1);
        verify(dao).capNhatThuTu(loai, 10L, 2);
    }

    @Test
    @DisplayName("AC3: Sắp xếp thứ tự hiển thị - Mục ở đầu danh sách di chuyển lên trên sẽ bị bỏ qua")
    void testThayDoiThuTu_MucOViTriDauTien_KhongTheLenTiep() {
        LoaiDanhMuc loai = LoaiDanhMuc.QUY_MO;
        MucDanhMucDTO item1 = new MucDanhMucDTO(10L, loai, "DUOI_10", "Dưới 10", "", 1, true, 0, LocalDate.now(), "Admin");
        when(dao.layDanhSach(loai, false)).thenReturn(Collections.singletonList(item1));

        boolean ketQua = service.thayDoiThuTu(loai, 10L, true);

        assertFalse(ketQua);
        verify(dao, never()).capNhatThuTu(any(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("AC3: Thay đổi trạng thái kích hoạt (áp dụng / tạm ngưng) thành công")
    void testChuyenTrangThaiKichHoat() {
        LoaiDanhMuc loai = LoaiDanhMuc.LOAI_HOAT_DONG;
        MucDanhMucDTO item = new MucDanhMucDTO(1L, loai, "DEMO", "Demo phần mềm", "", 1, true, 0, LocalDate.now(), "Admin");
        when(dao.layTheoId(loai, 1L)).thenReturn(item);
        when(dao.doiTrangThai(loai, 1L, false)).thenReturn(true);

        boolean ketQua = service.chuyenTrangThaiKichHoat(loai, 1L);

        assertTrue(ketQua);
        verify(dao).doiTrangThai(loai, 1L, false);
    }

    @Test
    @DisplayName("Tìm kiếm mục danh mục theo từ khóa tên hoặc mã")
    void testTimKiem() {
        LoaiDanhMuc loai = LoaiDanhMuc.NGUON_LEAD;
        MucDanhMucDTO item1 = new MucDanhMucDTO(1L, loai, "FACEBOOK", "Facebook Ads & Fanpage", "Quảng cáo FB", 1, true, 0, LocalDate.now(), "Admin");
        MucDanhMucDTO item2 = new MucDanhMucDTO(2L, loai, "GOOGLE", "Google Search & Ads", "Quảng cáo Google", 2, true, 0, LocalDate.now(), "Admin");
        when(dao.layDanhSach(loai, false)).thenReturn(Arrays.asList(item1, item2));

        List<MucDanhMucDTO> kqTimFB = service.timKiem(loai, "face");
        assertEquals(1, kqTimFB.size());
        assertEquals("FACEBOOK", kqTimFB.get(0).getMaMuc());

        List<MucDanhMucDTO> kqTimChung = service.timKiem(loai, "ads");
        assertEquals(2, kqTimChung.size());
    }
}
