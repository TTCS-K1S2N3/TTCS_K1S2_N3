package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử HoSoService - Xem và cập nhật hồ sơ cá nhân (Story S2-02)")
class HoSoServiceTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    private HoSoService hoSoService;

    private NguoiDung nguoiDungMau;

    @BeforeEach
    void setUp() {
        hoSoService = new HoSoService(nguoiDungDAO);

        nguoiDungMau = new NguoiDung();
        nguoiDungMau.setId(9L);
        nguoiDungMau.setHoTen("Phan Duy Hưng");
        nguoiDungMau.setEmail("dtc245200134@ictu.edu.vn");
        nguoiDungMau.setSoDienThoai("0901234567");
        nguoiDungMau.setChuKyEmail("Trân trọng,\nPhan Duy Hưng - Sales Rep");
        nguoiDungMau.setNhomKinhDoanhId(2);
        nguoiDungMau.setTenNhomKinhDoanh("Kinh Doanh Miền Nam");
        nguoiDungMau.setDanhSachVaiTro(Set.of(new VaiTro(4, VaiTroEnum.SALES_REP.getMaVaiTro(), "Nhân viên kinh doanh", "Mô tả")));
    }

    @Test
    @DisplayName("Lấy hồ sơ cá nhân thành công theo ID")
    void testLayHoSoThanhCong() {
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);

        NguoiDung ketQua = hoSoService.layHoSo(9L);

        assertNotNull(ketQua);
        assertEquals("Phan Duy Hưng", ketQua.getHoTen());
        assertEquals("dtc245200134@ictu.edu.vn", ketQua.getEmail());
        verify(nguoiDungDAO, times(1)).timTheoId(9L);
    }

    @Test
    @DisplayName("Lấy hồ sơ với ID không hợp lệ (<= 0) trả về null và không gọi DAO")
    void testLayHoSoIdKhongHopLe() {
        assertNull(hoSoService.layHoSo(0));
        assertNull(hoSoService.layHoSo(-5));
        verify(nguoiDungDAO, never()).timTheoId(anyLong());
    }

    @Test
    @DisplayName("AC1: Cập nhật thành công họ tên, số điện thoại và chữ ký email")
    void testCapNhatHoSoThanhCong_AC1() throws SQLException {
        when(nguoiDungDAO.capNhatHoSo(eq(9L), eq("Phan Duy Hưng Mới"), eq("0987654321"), anyString()))
                .thenReturn(true);
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);

        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "  Phan Duy Hưng Mới  ",
                "0987654321",
                "Chữ ký báo giá mới\nHotline: 0987654321"
        );

        assertTrue(ketQua.isThanhCong());
        assertEquals("Cập nhật hồ sơ cá nhân thành công.", ketQua.getThongBao());
        verify(nguoiDungDAO).capNhatHoSo(9L, "Phan Duy Hưng Mới", "0987654321", "Chữ ký báo giá mới\nHotline: 0987654321");
    }

    @Test
    @DisplayName("AC3: Chuẩn hóa số điện thoại Việt Nam trước khi lưu xuống database")
    void testChuanHoaSoDienThoaiKhiCapNhat_AC3() throws SQLException {
        when(nguoiDungDAO.capNhatHoSo(eq(9L), anyString(), eq("0901234567"), anyString()))
                .thenReturn(true);
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);

        // Đầu vào có dạng quốc tế +84 và dấu chấm phân cách
        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "+84 901.234.567",
                "Chữ ký email"
        );

        assertTrue(ketQua.isThanhCong());
        // Số điện thoại phải được chuẩn hóa thành 0901234567 khi lưu DB
        verify(nguoiDungDAO).capNhatHoSo(9L, "Phan Duy Hưng", "0901234567", "Chữ ký email");
    }

    @Test
    @DisplayName("AC3: Từ chối số điện thoại không đúng định dạng Việt Nam")
    void testTuChoiSoDienThoaiSaiDinhDang_AC3() throws SQLException {
        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "0123456789", // Đầu số 01 không còn hợp lệ
                "Chữ ký"
        );

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("soDienThoai"));
        verify(nguoiDungDAO, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("AC3: Từ chối khi số điện thoại để trống hoặc chỉ có khoảng trắng")
    void testTuChoiSoDienThoaiRong_AC3() throws SQLException {
        KetQuaNguoiDungDTO ketQua1 = hoSoService.capNhatHoSo(9L, "Phan Duy Hưng", "", "Chữ ký");
        assertFalse(ketQua1.isThanhCong());
        assertTrue(ketQua1.getDanhSachLoi().containsKey("soDienThoai"));

        KetQuaNguoiDungDTO ketQua2 = hoSoService.capNhatHoSo(9L, "Phan Duy Hưng", "   ", "Chữ ký");
        assertFalse(ketQua2.isThanhCong());
        assertTrue(ketQua2.getDanhSachLoi().containsKey("soDienThoai"));

        verify(nguoiDungDAO, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Từ chối khi họ và tên để trống hoặc vượt quá 150 ký tự")
    void testTuChoiHoTenKhongHopLe() throws SQLException {
        KetQuaNguoiDungDTO ketQuaRong = hoSoService.capNhatHoSo(9L, "", "0901234567", "Chữ ký");
        assertFalse(ketQuaRong.isThanhCong());
        assertTrue(ketQuaRong.getDanhSachLoi().containsKey("hoTen"));

        String tenQuaDai = "A".repeat(151);
        KetQuaNguoiDungDTO ketQuaQuaDai = hoSoService.capNhatHoSo(9L, tenQuaDai, "0901234567", "Chữ ký");
        assertFalse(ketQuaQuaDai.isThanhCong());
        assertTrue(ketQuaQuaDai.getDanhSachLoi().containsKey("hoTen"));

        verify(nguoiDungDAO, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("AC2: Từ chối khi người dùng cố tình can thiệp gửi đổi địa chỉ email")
    void testTuChoiKhiCoTinhDoiEmail_AC2() throws SQLException {
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);

        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "0901234567",
                "Chữ ký",
                "email_moi_hack@crm.vn", // Cố tình đổi email
                2,                       // Giữ nguyên nhóm
                List.of(4)               // Giữ nguyên vai trò
        );

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("email"));
        assertEquals("Bạn không có quyền tự thay đổi địa chỉ email.", ketQua.getDanhSachLoi().get("email"));
        verify(nguoiDungDAO, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("AC2: Từ chối khi người dùng cố tình can thiệp gửi đổi nhóm kinh doanh")
    void testTuChoiKhiCoTinhDoiNhom_AC2() throws SQLException {
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);

        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "0901234567",
                "Chữ ký",
                "dtc245200134@ictu.edu.vn", // Giữ nguyên email
                1,                          // Cố tình đổi sang nhóm 1
                List.of(4)                  // Giữ nguyên vai trò
        );

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("nhomId"));
        assertEquals("Bạn không có quyền tự thay đổi nhóm kinh doanh.", ketQua.getDanhSachLoi().get("nhomId"));
        verify(nguoiDungDAO, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("AC2: Từ chối khi người dùng cố tình can thiệp gửi đổi vai trò (ví dụ tự nâng lên ADMIN)")
    void testTuChoiKhiCoTinhDoiVaiTro_AC2() throws SQLException {
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);

        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "0901234567",
                "Chữ ký",
                "dtc245200134@ictu.edu.vn", // Giữ nguyên email
                2,                          // Giữ nguyên nhóm
                List.of(1)                  // Cố tình đổi sang role 1 (ADMIN)
        );

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getDanhSachLoi().containsKey("vaiTro"));
        assertEquals("Bạn không có quyền tự thay đổi vai trò hệ thống.", ketQua.getDanhSachLoi().get("vaiTro"));
        verify(nguoiDungDAO, never()).capNhatHoSo(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("AC2: Chấp nhận cập nhật khi các trường không được đổi gửi đúng giá trị hiện tại")
    void testChapNhanKhiGiuNguyenThongTinCam_AC2() throws SQLException {
        when(nguoiDungDAO.timTheoId(9L)).thenReturn(nguoiDungMau);
        when(nguoiDungDAO.capNhatHoSo(eq(9L), eq("Phan Duy Hưng"), eq("0901234567"), eq("Chữ ký mới")))
                .thenReturn(true);

        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "0901234567",
                "Chữ ký mới",
                "dtc245200134@ictu.edu.vn",
                2,
                List.of(4)
        );

        assertTrue(ketQua.isThanhCong());
        verify(nguoiDungDAO).capNhatHoSo(9L, "Phan Duy Hưng", "0901234567", "Chữ ký mới");
    }

    @Test
    @DisplayName("Xử lý an toàn khi DAO ném lỗi SQLException")
    void testXuLyLoiDatabase() throws SQLException {
        when(nguoiDungDAO.capNhatHoSo(anyLong(), anyString(), anyString(), anyString()))
                .thenThrow(new SQLException("Database connection timeout"));

        KetQuaNguoiDungDTO ketQua = hoSoService.capNhatHoSo(
                9L,
                "Phan Duy Hưng",
                "0901234567",
                "Chữ ký"
        );

        assertFalse(ketQua.isThanhCong());
        assertTrue(ketQua.getThongBao().contains("Lỗi hệ thống khi cập nhật hồ sơ"));
    }
}
