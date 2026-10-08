package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.nhom10.crm.dao.BoLocDaLuuDAO;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.model.BoLocDaLuu;
import vn.nhom10.crm.model.NguoiDung;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử BoLocKhachHangService - Quản lý bộ lọc đã lưu (Story S3-07, AC3)")
class BoLocKhachHangServiceTest {

    private BoLocDaLuuDAO boLocDAO;
    private BoLocKhachHangService service;
    private NguoiDung user;

    @BeforeEach
    void setUp() {
        boLocDAO = mock(BoLocDaLuuDAO.class);
        service = new BoLocKhachHangService(boLocDAO);

        user = new NguoiDung();
        user.setId(101L);
        user.setHoTen("Nguyễn Văn A");
        user.setEmail("sales.a@crm.vn");
    }

    @Test
    @DisplayName("AC3: Lưu bộ lọc thành công khi thông tin hợp lệ")
    void testLuuBoLoc_ThanhCong() throws Exception {
        BoLocKhachHangDTO tieuChi = new BoLocKhachHangDTO();
        tieuChi.setTrangThai("TIEM_NANG");
        tieuChi.setNganhNgheId(1L);

        when(boLocDAO.luuBoLoc(any(BoLocDaLuu.class))).thenReturn(10L);

        BoLocDaLuu result = service.luuBoLoc(user, "Khách tiềm năng CNTT", tieuChi, false);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Khách tiềm năng CNTT", result.getTenBoLoc());
        assertFalse(result.isMacDinh());

        ArgumentCaptor<BoLocDaLuu> captor = ArgumentCaptor.forClass(BoLocDaLuu.class);
        verify(boLocDAO).luuBoLoc(captor.capture());
        BoLocDaLuu saved = captor.getValue();
        assertEquals(101L, saved.getNguoiDungId());
        assertEquals(BoLocDaLuu.LOAI_KHACH_HANG, saved.getLoaiDoiTuong());
        assertTrue(saved.getTieuChiJson().contains("\"trangThai\":\"TIEM_NANG\""));
        verify(boLocDAO, never()).datMacDinh(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("AC3: Lưu bộ lọc và kích hoạt trạng thái mặc định")
    void testLuuBoLoc_DatMacDinh() throws Exception {
        BoLocKhachHangDTO tieuChi = new BoLocKhachHangDTO();
        tieuChi.setTrangThai("TIEM_NANG");

        when(boLocDAO.luuBoLoc(any(BoLocDaLuu.class))).thenReturn(15L);

        BoLocDaLuu result = service.luuBoLoc(user, "Bộ lọc gọi thứ Hai", tieuChi, true);

        assertNotNull(result);
        assertTrue(result.isMacDinh());
        verify(boLocDAO).datMacDinh(eq(15L), eq(101L), eq(BoLocDaLuu.LOAI_KHACH_HANG));
    }

    @Test
    @DisplayName("Validation: Tên bộ lọc rỗng ném ngoại lệ IllegalArgumentException")
    void testLuuBoLoc_TenRong_NemLoi() {
        BoLocKhachHangDTO tieuChi = new BoLocKhachHangDTO();

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                service.luuBoLoc(user, "", tieuChi, false));
        assertTrue(ex1.getMessage().contains("không được để trống"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                service.luuBoLoc(user, "   ", tieuChi, false));
        assertTrue(ex2.getMessage().contains("không được để trống"));
    }

    @Test
    @DisplayName("Validation: Tên bộ lọc vượt quá 150 ký tự ném ngoại lệ IllegalArgumentException")
    void testLuuBoLoc_TenQuaDai_NemLoi() {
        String tenDai = "A".repeat(151);
        BoLocKhachHangDTO tieuChi = new BoLocKhachHangDTO();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.luuBoLoc(user, tenDai, tieuChi, false));
        assertTrue(ex.getMessage().contains("tối đa 150 ký tự"));
    }

    @Test
    @DisplayName("Bảo mật: Người dùng chưa đăng nhập không được phép lưu bộ lọc")
    void testLuuBoLoc_ChuaDangNhap_NemLoi() {
        BoLocKhachHangDTO tieuChi = new BoLocKhachHangDTO();

        assertThrows(IllegalArgumentException.class, () ->
                service.luuBoLoc(null, "Test", tieuChi, false));

        NguoiDung userChuaCoId = new NguoiDung();
        assertThrows(IllegalArgumentException.class, () ->
                service.luuBoLoc(userChuaCoId, "Test", tieuChi, false));
    }

    @Test
    @DisplayName("AC3: Lấy danh sách bộ lọc của người dùng")
    void testLayDanhSachBoLoc() {
        BoLocDaLuu item = new BoLocDaLuu(1L, 101L, BoLocDaLuu.LOAI_KHACH_HANG, "Bộ lọc 1", "{}", false);
        when(boLocDAO.layDanhSachTheoNguoiDung(101L, BoLocDaLuu.LOAI_KHACH_HANG))
                .thenReturn(Collections.singletonList(item));

        List<BoLocDaLuu> list = service.layDanhSachBoLoc(user);
        assertEquals(1, list.size());
        assertEquals("Bộ lọc 1", list.get(0).getTenBoLoc());

        // Null user
        assertTrue(service.layDanhSachBoLoc(null).isEmpty());
    }

    @Test
    @DisplayName("AC3: Xóa bộ lọc thành công")
    void testXoaBoLoc() throws SQLException {
        when(boLocDAO.xoaBoLoc(5L, 101L)).thenReturn(true);

        boolean result = service.xoaBoLoc(5L, user);
        assertTrue(result);
        verify(boLocDAO).xoaBoLoc(5L, 101L);

        // ID không hợp lệ
        assertFalse(service.xoaBoLoc(0L, user));
    }

    @Test
    @DisplayName("AC3: Đặt mặc định bộ lọc")
    void testDatMacDinh() throws SQLException {
        when(boLocDAO.datMacDinh(5L, 101L, BoLocDaLuu.LOAI_KHACH_HANG)).thenReturn(true);

        boolean result = service.datMacDinh(5L, user);
        assertTrue(result);
        verify(boLocDAO).datMacDinh(5L, 101L, BoLocDaLuu.LOAI_KHACH_HANG);
    }
}
