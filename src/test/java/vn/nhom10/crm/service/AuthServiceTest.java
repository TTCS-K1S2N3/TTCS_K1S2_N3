package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.DangNhapResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử AuthService - Toàn bộ tiêu chí chấp nhận của S1-01")
class AuthServiceTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    @Mock
    private CauHinhHeThongDAO cauHinhHeThongDAO;

    @Mock
    private PhienDangNhapDAO phienDangNhapDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(nguoiDungDAO, cauHinhHeThongDAO, phienDangNhapDAO);
    }

    // =========================================================================
    // S1-01-AC1: Đăng nhập đúng thì vào được trang chủ tương ứng với vai trò
    // =========================================================================

    @Test
    @DisplayName("S1-01-AC1: Đăng nhập đúng trả về thành công kèm thông tin vai trò người dùng")
    void testS1_01_AC1_DangNhapDung_ThanhCongVaTraVeVaiTro() {
        String email = "admin@crm.vn";
        String password = "CorrectPassword@123";
        String passwordHash = PasswordUtil.hashPassword(password);

        NguoiDung mockUser = new NguoiDung();
        mockUser.setId(1L);
        mockUser.setHoTen("Nguyễn Văn Quản Trị");
        mockUser.setEmail(email);
        mockUser.setMatKhauHash(passwordHash);
        mockUser.setTrangThai("HOAT_DONG");
        mockUser.setSoLanDangNhapSai(0);
        mockUser.setSessionVersion(1);

        VaiTro roleAdmin = new VaiTro(7L, "ADMIN", "Quản trị hệ thống", "Toàn quyền", "TOAN_BO");
        mockUser.setDanhSachVaiTro(List.of(roleAdmin));

        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(mockUser);
        when(nguoiDungDAO.capNhatDangNhapThanhCong(1L)).thenReturn(true);
        when(phienDangNhapDAO.taoPhien(eq(1L), anyString(), eq(1), anyString(), anyString(), eq(30))).thenReturn(true);

        DangNhapResult result = authService.dangNhap(email, password, "127.0.0.1", "Mozilla/5.0", "sess-123");

        assertTrue(result.isThanhCong(), "Đăng nhập phải thành công");
        assertEquals(DangNhapResult.Status.THANH_CONG, result.getStatus());
        assertNotNull(result.getNguoiDung());
        assertEquals("ADMIN", result.getNguoiDung().getVaiTroChinh().getMaVaiTro());
        verify(nguoiDungDAO).capNhatDangNhapThanhCong(1L);
        verify(phienDangNhapDAO).taoPhien(eq(1L), anyString(), eq(1), anyString(), anyString(), eq(30));
    }

    // =========================================================================
    // S1-01-AC2: Sai thông tin hiển thị thông báo chung, không tiết lộ email có tồn tại hay không
    // =========================================================================

    @Test
    @DisplayName("S1-01-AC2: Email không tồn tại trả về thông báo chung")
    void testS1_01_AC2_EmailKhongTonTai_ThongBaoChung() {
        String email = "khongtontai@crm.vn";
        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(null);

        DangNhapResult result = authService.dangNhap(email, "AnyPassword@123", "127.0.0.1", "Browser", "sess-1");

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapResult.Status.SAI_THONG_TIN, result.getStatus());
        assertEquals(AuthService.THONG_BAO_SAI_THONG_TIN, result.getThongBao());
    }

    @Test
    @DisplayName("S1-01-AC2: Mật khẩu sai trả về cùng một thông báo chung với khi email không tồn tại")
    void testS1_01_AC2_MatKhauSai_ThongBaoChungDongNhat() {
        String email = "sales@crm.vn";
        NguoiDung mockUser = new NguoiDung();
        mockUser.setId(2L);
        mockUser.setEmail(email);
        mockUser.setMatKhauHash(PasswordUtil.hashPassword("CorrectPass@123"));
        mockUser.setTrangThai("HOAT_DONG");
        mockUser.setSoLanDangNhapSai(1);

        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(mockUser);
        when(cauHinhHeThongDAO.layGiaTriInt("SO_LAN_DANG_NHAP_SAI_TOI_DA", 5)).thenReturn(5);
        when(cauHinhHeThongDAO.layGiaTriInt("PHUT_KHOA_DANG_NHAP", 15)).thenReturn(15);

        DangNhapResult result = authService.dangNhap(email, "WrongPassword@123", "127.0.0.1", "Browser", "sess-2");

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapResult.Status.SAI_THONG_TIN, result.getStatus());
        assertEquals(AuthService.THONG_BAO_SAI_THONG_TIN, result.getThongBao(),
                "Thông báo khi sai mật khẩu phải giống hệt thông báo khi email không tồn tại (không tiết lộ email)");

        // Chứng minh số lần sai được tăng lên 2
        verify(nguoiDungDAO).capNhatDangNhapSai(eq(2L), eq(2), isNull());
    }

    // =========================================================================
    // S1-01-AC3: Khoá tạm 15 phút sau 5 lần sai liên tiếp
    // =========================================================================

    @Test
    @DisplayName("S1-01-AC3: Nhập sai mật khẩu lần thứ 5 liên tiếp sẽ khóa tạm 15 phút")
    void testS1_01_AC3_DangNhapSai5Lan_KhoaTam15Phut() {
        String email = "user@crm.vn";
        NguoiDung mockUser = new NguoiDung();
        mockUser.setId(3L);
        mockUser.setEmail(email);
        mockUser.setMatKhauHash(PasswordUtil.hashPassword("RealSecret@123"));
        mockUser.setTrangThai("HOAT_DONG");
        mockUser.setSoLanDangNhapSai(4); // Đã sai 4 lần trước đó

        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(mockUser);
        when(cauHinhHeThongDAO.layGiaTriInt("SO_LAN_DANG_NHAP_SAI_TOI_DA", 5)).thenReturn(5);
        when(cauHinhHeThongDAO.layGiaTriInt("PHUT_KHOA_DANG_NHAP", 15)).thenReturn(15);

        long beforeCall = System.currentTimeMillis();
        DangNhapResult result = authService.dangNhap(email, "WrongPasswordAgain", "127.0.0.1", "Browser", "sess-3");
        long afterCall = System.currentTimeMillis();

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapResult.Status.KHOA_TAM_15_PHUT, result.getStatus());
        assertEquals(AuthService.THONG_BAO_KHOA_TAM_15_PHUT, result.getThongBao());

        // Kiểm tra DAO đã lưu thời điểm khóa là now + 15 phút
        ArgumentCaptor<Timestamp> khoaDenCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(nguoiDungDAO).capNhatDangNhapSai(eq(3L), eq(5), khoaDenCaptor.capture());

        Timestamp khoaDen = khoaDenCaptor.getValue();
        assertNotNull(khoaDen);
        long expectedMin = beforeCall + 15 * 60 * 1000;
        long expectedMax = afterCall + 15 * 60 * 1000 + 1000;
        assertTrue(khoaDen.getTime() >= expectedMin && khoaDen.getTime() <= expectedMax,
                "Thời gian khóa phải xấp xỉ 15 phút từ thời điểm hiện tại");
    }

    @Test
    @DisplayName("S1-01-AC3: Đăng nhập trong lúc đang bị khóa tạm sẽ bị từ chối ngay cả khi nhập đúng mật khẩu")
    void testS1_01_AC3_DangBiKhoaTam_TuChoiDangNhap() {
        String email = "locked@crm.vn";
        String realPass = "RealPassword@123";

        NguoiDung mockUser = new NguoiDung();
        mockUser.setId(4L);
        mockUser.setEmail(email);
        mockUser.setMatKhauHash(PasswordUtil.hashPassword(realPass));
        mockUser.setTrangThai("HOAT_DONG");
        mockUser.setSoLanDangNhapSai(5);
        // Khóa đến 10 phút sau
        mockUser.setKhoaDen(new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000));

        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(mockUser);

        DangNhapResult result = authService.dangNhap(email, realPass, "127.0.0.1", "Browser", "sess-4");

        assertFalse(result.isThanhCong());
        assertEquals(DangNhapResult.Status.KHOA_TAM_15_PHUT, result.getStatus());
        assertEquals(AuthService.THONG_BAO_DANG_BI_KHOA_TAM, result.getThongBao());
        verify(nguoiDungDAO, never()).capNhatDangNhapThanhCong(anyLong());
    }

    @Test
    @DisplayName("S1-01-AC3: Sau khi hết 15 phút khóa tạm, đăng nhập đúng sẽ thành công và reset số lần sai")
    void testS1_01_AC3_SauKhiHetHanKhoa_DangNhapDung_ThanhCongVaReset() {
        String email = "unlocked@crm.vn";
        String realPass = "RealPassword@123";

        NguoiDung mockUser = new NguoiDung();
        mockUser.setId(5L);
        mockUser.setEmail(email);
        mockUser.setMatKhauHash(PasswordUtil.hashPassword(realPass));
        mockUser.setTrangThai("HOAT_DONG");
        mockUser.setSoLanDangNhapSai(5);
        // Thời điểm khóa là 1 phút trước (đã hết hạn khóa)
        mockUser.setKhoaDen(new Timestamp(System.currentTimeMillis() - 60 * 1000));

        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(mockUser);
        when(nguoiDungDAO.capNhatDangNhapThanhCong(5L)).thenReturn(true);
        when(phienDangNhapDAO.taoPhien(eq(5L), anyString(), anyInt(), anyString(), anyString(), eq(30))).thenReturn(true);

        DangNhapResult result = authService.dangNhap(email, realPass, "127.0.0.1", "Browser", "sess-5");

        assertTrue(result.isThanhCong());
        verify(nguoiDungDAO).moKhoaVaResetDangNhapSai(5L);
        verify(nguoiDungDAO).capNhatDangNhapThanhCong(5L);
    }
}
