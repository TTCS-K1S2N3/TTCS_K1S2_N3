package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dao.NguoiLienHeDAO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.LichSuLienHeCongTy;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NguoiLienHe;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.model.VaiTroQuyetDinhEnum;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử NguoiLienHeService - Nghiệp vụ người liên hệ & Phân quyền Data Scope (Story S3-02)")
class NguoiLienHeServiceTest {

    private NguoiLienHeDAO nlhDAO;
    private KhachHangDAO khDAO;
    private KhachHangService khService;
    private NguoiLienHeService service;

    private NguoiDung salesA;
    private NguoiDung salesB;
    private NguoiDung teamLead;
    private NguoiDung director;

    private KhachHang khachHangA;
    private KhachHang khachHangB;

    @BeforeEach
    void setUp() {
        nlhDAO = mock(NguoiLienHeDAO.class);
        khDAO = mock(KhachHangDAO.class);
        khService = mock(KhachHangService.class);
        service = new NguoiLienHeService(nlhDAO, khDAO, khService);

        // Sales A: ID 101, Nhóm 1, Scope CA_NHAN
        salesA = new NguoiDung();
        salesA.setId(101L);
        salesA.setHoTen("Nguyễn Văn Sales A");
        salesA.setNhomKinhDoanhId(1);
        salesA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        // Sales B: ID 102, Nhóm 1, Scope CA_NHAN
        salesB = new NguoiDung();
        salesB.setId(102L);
        salesB.setHoTen("Trần Thị Sales B");
        salesB.setNhomKinhDoanhId(1);
        salesB.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        // Team Lead: ID 100, Nhóm 1, Scope NHOM
        teamLead = new NguoiDung();
        teamLead.setId(100L);
        teamLead.setHoTen("Lê Trưởng Nhóm");
        teamLead.setNhomKinhDoanhId(1);
        teamLead.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.TEAM_LEAD, PhamViDuLieu.NHOM)));

        // Director: ID 999, Scope TOAN_BO
        director = new NguoiDung();
        director.setId(999L);
        director.setHoTen("Giám Đốc Kinh Doanh");
        director.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.DIRECTOR, PhamViDuLieu.TOAN_BO)));

        // Khách hàng A của Sales A (ID 1)
        khachHangA = new KhachHang();
        khachHangA.setId(1L);
        khachHangA.setTenCongTy("Công ty A");
        khachHangA.setNguoiSoHuuId(101L);
        khachHangA.setNhomKinhDoanhId(1L);

        // Khách hàng B của Sales B (ID 2)
        khachHangB = new KhachHang();
        khachHangB.setId(2L);
        khachHangB.setTenCongTy("Công ty B");
        khachHangB.setNguoiSoHuuId(102L);
        khachHangB.setNhomKinhDoanhId(1L);

        when(khDAO.timTheoId(1L)).thenReturn(khachHangA);
        when(khDAO.timTheoId(2L)).thenReturn(khachHangB);

        // Mặc định phân quyền theo S3-01 / S1-05
        when(khService.kiemTraQuyenXem(eq(salesA), eq(khachHangA))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(true, "OK", khachHangA));
        when(khService.kiemTraQuyenSua(eq(salesA), eq(khachHangA))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(true, "OK", khachHangA));

        when(khService.kiemTraQuyenXem(eq(salesA), eq(khachHangB))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(false, "Từ chối xem khách người khác", khachHangB));
        when(khService.kiemTraQuyenSua(eq(salesA), eq(khachHangB))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(false, "Từ chối sửa khách người khác", khachHangB));

        when(khService.kiemTraQuyenXem(eq(teamLead), any(KhachHang.class))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(true, "OK", null));
        when(khService.kiemTraQuyenSua(eq(teamLead), any(KhachHang.class))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(true, "OK", null));

        when(khService.kiemTraQuyenXem(eq(director), any(KhachHang.class))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(true, "OK", null));
        when(khService.kiemTraQuyenSua(eq(director), any(KhachHang.class))).thenReturn(new KhachHangService.KetQuaQuyenKhachHang(true, "OK", null));
    }

    @Test
    @DisplayName("AC1: Thêm người liên hệ thành công khi có quyền trên khách hàng")
    void testThemNguoiLienHe_ThanhCong() {
        NguoiLienHe nlh = new NguoiLienHe(null, 1L, "Nguyễn Văn Hùng", "Giám đốc Dự án", "hung.nv@congty.com", "0912345678", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, true);
        when(nlhDAO.themNguoiLienHe(nlh)).thenReturn(50L);

        NguoiLienHe result = service.themNguoiLienHe(salesA, nlh);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        verify(nlhDAO).themNguoiLienHe(nlh);
    }

    @Test
    @DisplayName("Bảo mật & AC4: Sales A cố tình thêm người liên hệ vào khách hàng của B bị chặn SecurityException")
    void testThemNguoiLienHe_ChặnNgoaiPhamVi() {
        NguoiLienHe nlh = new NguoiLienHe(null, 2L, "Lê Thị Trộm", "Trưởng phòng", "trom@congtyb.com", "0988888888", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG, false);

        assertThrows(SecurityException.class, () -> {
            service.themNguoiLienHe(salesA, nlh);
        });

        verify(nlhDAO, never()).themNguoiLienHe(any());
    }

    @Test
    @DisplayName("Validation AC1: Bắt buộc họ tên người liên hệ không được để trống")
    void testValidation_HoTenBatBuoc() {
        NguoiLienHe nlhRong = new NguoiLienHe(null, 1L, "", "Chức danh", "test@email.com", "0912345678", null, false);
        NguoiLienHe nlhNull = new NguoiLienHe(null, 1L, null, "Chức danh", "test@email.com", "0912345678", null, false);

        assertThrows(IllegalArgumentException.class, () -> service.themNguoiLienHe(salesA, nlhRong));
        assertThrows(IllegalArgumentException.class, () -> service.themNguoiLienHe(salesA, nlhNull));
    }

    @Test
    @DisplayName("Validation AC1: Email không đúng định dạng bị từ chối")
    void testValidation_EmailSaiDinhDang() {
        NguoiLienHe nlh = new NguoiLienHe(null, 1L, "Nguyễn Văn Test", "Nhân viên", "email-sai-dinh-dang", "0912345678", null, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.themNguoiLienHe(salesA, nlh);
        });
        assertTrue(ex.getMessage().contains("email không hợp lệ"));
    }

    @Test
    @DisplayName("Validation AC1: Số điện thoại không đúng định dạng bị từ chối")
    void testValidation_SoDienThoaiSaiDinhDang() {
        NguoiLienHe nlh = new NguoiLienHe(null, 1L, "Nguyễn Văn Test", "Nhân viên", "test@email.com", "123", null, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.themNguoiLienHe(salesA, nlh);
        });
        assertTrue(ex.getMessage().contains("Số điện thoại không hợp lệ"));
    }

    @Test
    @DisplayName("AC2: Hỗ trợ 4 vai trò quyết định mua hợp lệ")
    void testVaiTroQuyetDinhMua_HopLe() {
        assertEquals("Người quyết định", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH.getTenHienThi());
        assertEquals("Người ảnh hưởng", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG.getTenHienThi());
        assertEquals("Người dùng cuối", VaiTroQuyetDinhEnum.NGUOI_DUNG_CUOI.getTenHienThi());
        assertEquals("Người cản trở", VaiTroQuyetDinhEnum.NGUOI_CAN_TRO.getTenHienThi());

        assertEquals(VaiTroQuyetDinhEnum.NGUOI_CAN_TRO, VaiTroQuyetDinhEnum.fromMa("NGUOI_CAN_TRO"));
        assertEquals(VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, VaiTroQuyetDinhEnum.fromMa("Người quyết định"));
    }

    @Test
    @DisplayName("AC3: Đánh dấu người liên hệ là đầu mối chính thành công")
    void testDatLamDauMoiChinh_ThanhCong() {
        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Người Liên Hệ 1", "Trưởng phòng", "nlh1@fpt.com", "0911", null, false);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh));
        when(nlhDAO.datLamDauMoiChinh(10L, 1L)).thenReturn(true);

        boolean result = service.datLamDauMoiChinh(salesA, 10L, 1L);

        assertTrue(result);
        verify(nlhDAO).datLamDauMoiChinh(10L, 1L);
    }

    @Test
    @DisplayName("AC3: Từ chối bỏ đầu mối chính cuối cùng khi khách hàng chỉ có 1 đầu mối chính")
    void testBoDauMoiChinh_TuChoiKhiLaDauMoiDuyNhat() {
        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Người Đầu Mối Duy Nhất", "Trưởng phòng", "nlh1@fpt.com", "0911", null, true);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh));
        when(nlhDAO.layDanhSachTheoKhachHang(1L)).thenReturn(List.of(nlh));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.boDauMoiChinh(salesA, 10L, 1L);
        });

        assertTrue(ex.getMessage().contains("ít nhất một người liên hệ làm đầu mối chính"));
        verify(nlhDAO, never()).boDauMoiChinh(anyLong(), anyLong());
    }

    @Test
    @DisplayName("AC3: Bỏ đầu mối chính thành công khi khách hàng còn đầu mối chính khác")
    void testBoDauMoiChinh_ThanhCongKhiConDauMoiKhac() {
        NguoiLienHe nlh1 = new NguoiLienHe(10L, 1L, "Đầu mối 1", "Trưởng phòng", "nlh1@fpt.com", "0911", null, true);
        NguoiLienHe nlh2 = new NguoiLienHe(11L, 1L, "Đầu mối 2", "Phó phòng", "nlh2@fpt.com", "0922", null, true);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh1));
        when(nlhDAO.layDanhSachTheoKhachHang(1L)).thenReturn(List.of(nlh1, nlh2));
        when(nlhDAO.boDauMoiChinh(10L, 1L)).thenReturn(true);

        boolean result = service.boDauMoiChinh(salesA, 10L, 1L);

        assertTrue(result);
        verify(nlhDAO).boDauMoiChinh(10L, 1L);
    }

    @Test
    @DisplayName("AC4: Chuyển công ty giữ nguyên lịch sử khi có quyền trên cả công ty cũ và công ty mới")
    void testChuyenCongTy_ThanhCongVoiQuyenHopLe() {
        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Vũ Chuyển Công Tác", "Phó ban", "vu@fpt.com", "0933", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG, true);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh));
        when(nlhDAO.chuyenCongTy(eq(10L), eq(2L), anyString(), any(), anyString())).thenReturn(true);

        // Trưởng nhóm có quyền trên cả khách hàng 1 và 2 (cùng nhóm 1)
        boolean result = service.chuyenCongTy(teamLead, 10L, 2L, "Trưởng ban Mua sắm", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, "Chuyển đơn vị");

        assertTrue(result);
        verify(nlhDAO).chuyenCongTy(10L, 2L, "Trưởng ban Mua sắm", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, "Chuyển đơn vị");
    }

    @Test
    @DisplayName("AC4: Sales A cố tình chuyển liên hệ sang khách hàng của B bị từ chối do không có quyền trên công ty đích")
    void testChuyenCongTy_TuChoiKhiKhongCoQuyenCongTyDich() {
        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Vũ Chuyển Công Tác", "Phó ban", "vu@fpt.com", "0933", VaiTroQuyetDinhEnum.NGUOI_ANH_HUONG, true);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh));

        // Sales A không có quyền sửa khách hàng B (ID 2)
        assertThrows(SecurityException.class, () -> {
            service.chuyenCongTy(salesA, 10L, 2L, "Chức danh", VaiTroQuyetDinhEnum.NGUOI_QUYET_DINH, "Ghi chú");
        });

        verify(nlhDAO, never()).chuyenCongTy(anyLong(), anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("AC4: Chặn chuyển sang chính công ty hiện tại")
    void testChuyenCongTy_KhongChoChuyenTrungCongTy() {
        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Vũ Chuyển", "Phó ban", "vu@fpt.com", "0933", null, false);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh));

        assertThrows(IllegalArgumentException.class, () -> {
            service.chuyenCongTy(salesA, 10L, 1L, "Chức danh mới", null, "Chuyển cùng công ty");
        });
    }

    @Test
    @DisplayName("AC4: Lấy lịch sử công ty của người liên hệ thành công")
    void testLayLichSuCongTy_ThanhCong() {
        NguoiLienHe nlh = new NguoiLienHe(10L, 1L, "Người Lịch Sử", "Trưởng phòng", "history@fpt.com", "0911", null, false);
        when(nlhDAO.timTheoId(10L)).thenReturn(Optional.of(nlh));

        LichSuLienHeCongTy ls = new LichSuLienHeCongTy();
        ls.setId(100L);
        ls.setTenCongTy("Công ty A");
        when(nlhDAO.layLichSuCongTy(10L)).thenReturn(Collections.singletonList(ls));

        List<LichSuLienHeCongTy> history = service.layLichSuCongTy(salesA, 10L);

        assertEquals(1, history.size());
        assertEquals("Công ty A", history.get(0).getTenCongTy());
    }
}
