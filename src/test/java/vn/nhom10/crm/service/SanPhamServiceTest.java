package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import vn.nhom10.crm.dao.SanPhamDAO;
import vn.nhom10.crm.dto.KetQuaSanPhamDTO;
import vn.nhom10.crm.dto.PhanTrangDTO;
import vn.nhom10.crm.model.LoaiSanPhamEnum;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.TrangThaiSanPhamEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử toàn diện tầng Service cho Story S2-05 theo 4 Acceptance Criteria.
 */
public class SanPhamServiceTest {

    private SanPhamDAO sanPhamDAOMock;
    private SanPhamService sanPhamService;

    private NguoiDung directorUser;
    private NguoiDung salesRepUser;

    @BeforeEach
    public void setUp() {
        sanPhamDAOMock = Mockito.mock(SanPhamDAO.class);
        sanPhamService = new SanPhamService(sanPhamDAOMock);

        directorUser = new NguoiDung(1, "Nguyễn Văn Giám Đốc", "director@crm.vn");
        directorUser.themVaiTro(VaiTroEnum.DIRECTOR);

        salesRepUser = new NguoiDung(2, "Trần Thị Sales", "sales@crm.vn");
        salesRepUser.themVaiTro(VaiTroEnum.SALES_REP);
    }

    // ====================================================================
    // AC 1: Khai báo mã, tên, loại, đơn vị tính, giá niêm yết, giá sàn
    // ====================================================================

    @Test
    public void testTaoSanPhamThanhCong() throws SQLException {
        SanPham sp = new SanPham();
        sp.setMaSanPham("CRM-PRO-01");
        sp.setTenSanPham("Gói CRM Chuyên Nghiệp");
        sp.setLoai(LoaiSanPhamEnum.DICH_VU_THUE_BAO);
        sp.setDonViTinh("Người dùng/Tháng");
        sp.setGiaNiemYet(new BigDecimal("300000.00"));
        sp.setGiaSan(new BigDecimal("250000.00"));
        sp.setGiaVon(new BigDecimal("150000.00"));

        when(sanPhamDAOMock.kiemTraMaTonTai("CRM-PRO-01", null)).thenReturn(false);
        when(sanPhamDAOMock.themSanPham(any(SanPham.class), eq(true))).thenReturn(10);

        KetQuaSanPhamDTO ketQua = sanPhamService.taoSanPham(sp, directorUser);

        assertTrue(ketQua.isThanhCong());
        assertEquals(10, ketQua.getSanPham().getId());
        verify(sanPhamDAOMock, times(1)).themSanPham(sp, true);
    }

    @Test
    public void testTaoSanPhamThatBaiKhiGiaSanLonHonGiaNiemYet() {
        SanPham sp = new SanPham();
        sp.setMaSanPham("CRM-FAIL-01");
        sp.setTenSanPham("Sản phẩm lỗi giá");
        sp.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        sp.setDonViTinh("Gói");
        sp.setGiaNiemYet(new BigDecimal("100000.00"));
        sp.setGiaSan(new BigDecimal("150000.00")); // Giá sàn > Giá niêm yết

        KetQuaSanPhamDTO ketQua = sanPhamService.taoSanPham(sp, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("giaSan"));
        assertTrue(ketQua.getDanhSachLoi().get("giaSan").contains("không được vượt quá giá niêm yết"));
    }

    @Test
    public void testTaoSanPhamThatBaiKhiTrungMa() {
        SanPham sp = new SanPham();
        sp.setMaSanPham("TRUNG-MA");
        sp.setTenSanPham("Sản phẩm trùng mã");
        sp.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        sp.setDonViTinh("Bộ");
        sp.setGiaNiemYet(new BigDecimal("100000.00"));
        sp.setGiaSan(new BigDecimal("80000.00"));

        when(sanPhamDAOMock.kiemTraMaTonTai("TRUNG-MA", null)).thenReturn(true);

        KetQuaSanPhamDTO ketQua = sanPhamService.taoSanPham(sp, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("maSanPham"));
        assertTrue(ketQua.getDanhSachLoi().get("maSanPham").contains("đã tồn tại"));
    }

    // ====================================================================
    // AC 2: Giá sàn là ngưỡng để xác định báo giá có cần duyệt chiết khấu hay không
    // ====================================================================

    @Test
    public void testKiemTraCanDuyetChietKhauKhiDuoiGiaSan() {
        SanPham sp = new SanPham();
        sp.setId(1);
        sp.setGiaNiemYet(new BigDecimal("1000000.00"));
        sp.setGiaSan(new BigDecimal("800000.00"));

        when(sanPhamDAOMock.timTheoId(1, false)).thenReturn(sp);

        // Đơn giá đề xuất 750,000 < giá sàn 800,000 -> BẮT BUỘC DUYỆT
        boolean canDuyet1 = sanPhamService.kiemTraCanDuyetChietKhau(1, new BigDecimal("750000.00"));
        assertTrue(canDuyet1, "Đơn giá nhỏ hơn giá sàn phải trả về true (cần duyệt)");

        // Đơn giá đề xuất 800,000 = giá sàn -> KHÔNG CẦN DUYỆT
        boolean canDuyet2 = sanPhamService.kiemTraCanDuyetChietKhau(1, new BigDecimal("800000.00"));
        assertFalse(canDuyet2, "Đơn giá bằng giá sàn không cần duyệt");

        // Đơn giá đề xuất 900,000 > giá sàn -> KHÔNG CẦN DUYỆT
        boolean canDuyet3 = sanPhamService.kiemTraCanDuyetChietKhau(1, new BigDecimal("900000.00"));
        assertFalse(canDuyet3, "Đơn giá lớn hơn giá sàn không cần duyệt");
    }

    // ====================================================================
    // AC 3: Giá vốn chỉ Giám đốc kinh doanh xem và sửa được
    // ====================================================================

    @Test
    public void testQuyenGiaVonPhanBietGiuaDirectorVaSalesRep() {
        assertTrue(sanPhamService.coQuyenGiaVon(directorUser), "Giám đốc kinh doanh phải có quyền xem/sửa giá vốn");
        assertFalse(sanPhamService.coQuyenGiaVon(salesRepUser), "Nhân viên Sales không được có quyền xem/sửa giá vốn");
        assertFalse(sanPhamService.coQuyenGiaVon(null), "Người dùng rỗng không được có quyền");
    }

    @Test
    public void testLayChiTietSanPhamBaoMatGiaVon() {
        SanPham spGoc = new SanPham();
        spGoc.setId(5);
        spGoc.setTenSanPham("Sản phẩm bảo mật");
        spGoc.setGiaVon(new BigDecimal("200000.00"));

        // Khi Giám đốc gọi, DAO nhận coQuyenGiaVon = true
        when(sanPhamDAOMock.timTheoId(5, true)).thenReturn(spGoc);

        SanPham spDirector = sanPhamService.layChiTietSanPham(5, directorUser);
        assertNotNull(spDirector);
        assertNotNull(spDirector.getGiaVon());

        // Khi Sales Rep gọi, DAO nhận coQuyenGiaVon = false
        SanPham spAnGiaVon = new SanPham();
        spAnGiaVon.setId(5);
        spAnGiaVon.setTenSanPham("Sản phẩm bảo mật");
        spAnGiaVon.setGiaVon(null); // Bị che đi

        when(sanPhamDAOMock.timTheoId(5, false)).thenReturn(spAnGiaVon);

        SanPham spSales = sanPhamService.layChiTietSanPham(5, salesRepUser);
        assertNotNull(spSales);
        assertNull(spSales.getGiaVon(), "Nhân viên Sales không được thấy giá vốn");
    }

    @Test
    public void testSalesRepKhongCoQuyenQuanLyVaTaoSanPham() {
        SanPham sp = new SanPham();
        sp.setMaSanPham("SALES-TRY");
        sp.setTenSanPham("Thử tạo bởi Sales");

        KetQuaSanPhamDTO ketQua = sanPhamService.taoSanPham(sp, salesRepUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("không có quyền"));
        verifyNoInteractions(sanPhamDAOMock);
    }

    // ====================================================================
    // AC 4: Sản phẩm đã xuất hiện trong báo giá thì không xoá được, chỉ ngừng kinh doanh
    // ====================================================================

    @Test
    public void testKhongDuocXoaSanPhamDaCoTrongBaoGia() throws SQLException {
        SanPham spTrongBaoGia = new SanPham();
        spTrongBaoGia.setId(9);
        spTrongBaoGia.setTenSanPham("Gói dịch vụ đã ký kết");

        when(sanPhamDAOMock.timTheoId(9, false)).thenReturn(spTrongBaoGia);
        when(sanPhamDAOMock.kiemTraXuatHienTrongBaoGia(9)).thenReturn(true); // Đã có trong báo giá

        KetQuaSanPhamDTO ketQua = sanPhamService.xoaSanPham(9, directorUser);

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("đã xuất hiện trong báo giá nên không thể xoá"));
        assertTrue(ketQua.getThongBao().contains("Ngừng kinh doanh"));
        verify(sanPhamDAOMock, never()).xoaSanPham(9);
    }

    @Test
    public void testXoaThanhCongSanPhamChuaCoTrongBaoGia() throws SQLException {
        SanPham spChuaBaoGia = new SanPham();
        spChuaBaoGia.setId(11);
        spChuaBaoGia.setTenSanPham("Sản phẩm mới chưa báo giá");

        when(sanPhamDAOMock.timTheoId(11, false)).thenReturn(spChuaBaoGia);
        when(sanPhamDAOMock.kiemTraXuatHienTrongBaoGia(11)).thenReturn(false); // Chưa có trong báo giá
        when(sanPhamDAOMock.xoaSanPham(11)).thenReturn(true);

        KetQuaSanPhamDTO ketQua = sanPhamService.xoaSanPham(11, directorUser);

        assertTrue(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Đã xoá hoàn toàn"));
        verify(sanPhamDAOMock, times(1)).xoaSanPham(11);
    }

    @Test
    public void testChuyenTrangThaiNgungKinhDoanhThanhCong() throws SQLException {
        SanPham sp = new SanPham();
        sp.setId(9);
        sp.setTenSanPham("Sản phẩm chuyển trạng thái");
        sp.setTrangThai(TrangThaiSanPhamEnum.DANG_KINH_DOANH);

        when(sanPhamDAOMock.timTheoId(9, false)).thenReturn(sp);
        when(sanPhamDAOMock.capNhatTrangThai(9, TrangThaiSanPhamEnum.NGUNG_KINH_DOANH)).thenReturn(true);

        KetQuaSanPhamDTO ketQua = sanPhamService.chuyenTrangThai(9, TrangThaiSanPhamEnum.NGUNG_KINH_DOANH, directorUser);

        assertTrue(ketQua.isThanhCong());
        assertEquals(TrangThaiSanPhamEnum.NGUNG_KINH_DOANH, sp.getTrangThai());
        assertTrue(ketQua.getThongBao().contains("Ngừng kinh doanh"));
        verify(sanPhamDAOMock, times(1)).capNhatTrangThai(9, TrangThaiSanPhamEnum.NGUNG_KINH_DOANH);
    }

    @Test
    public void testLoaiSanPhamEnumPropertyVaTuMa() {
        assertNotNull(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN.getMoTa());
        assertNotNull(LoaiSanPhamEnum.DICH_VU_THUE_BAO.getMoTa());
        assertEquals("SAN_PHAM_MOT_LAN", LoaiSanPhamEnum.SAN_PHAM_MOT_LAN.getMaLoai());
        assertEquals("DICH_VU_THUE_BAO", LoaiSanPhamEnum.DICH_VU_THUE_BAO.getMaLoai());

        assertEquals(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN, LoaiSanPhamEnum.tuMa("SAN_PHAM_MOT_LAN"));
        assertEquals(LoaiSanPhamEnum.DICH_VU_THUE_BAO, LoaiSanPhamEnum.tuMa("dich_vu_thue_bao"));
        assertNull(LoaiSanPhamEnum.tuMa(null));
        assertNull(LoaiSanPhamEnum.tuMa(""));
        assertNull(LoaiSanPhamEnum.tuMa("KHONG_TON_TAI"));
    }
}
