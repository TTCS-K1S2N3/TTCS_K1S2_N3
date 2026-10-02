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
import vn.nhom10.crm.dao.TokenDatLaiMatKhauDAO;
import vn.nhom10.crm.dto.DatLaiMatKhauResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TokenDatLaiMatKhau;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử AuthService - Đặt lại mật khẩu qua email (S1-03)")
class AuthServiceResetPasswordTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    @Mock
    private CauHinhHeThongDAO cauHinhHeThongDAO;

    @Mock
    private PhienDangNhapDAO phienDangNhapDAO;

    @Mock
    private TokenDatLaiMatKhauDAO tokenDatLaiMatKhauDAO;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(nguoiDungDAO, cauHinhHeThongDAO, phienDangNhapDAO,
                tokenDatLaiMatKhauDAO, emailService);
    }

    // =========================================================================
    // S1-03-AC1: Nhập email nhận được liên kết đặt lại có hiệu lực 30 phút
    // =========================================================================

    @Test
    @DisplayName("S1-03-AC1: Yêu cầu đặt lại mật khẩu thành công tạo token với thời hạn 30 phút và gửi email")
    void testAC1_YeuCauDatLaiMatKhau_TaoTokenHieuLuc30PhutVaGuiEmail() {
        String email = "nhanvien@crm.vn";
        NguoiDung user = new NguoiDung();
        user.setId(10L);
        user.setEmail(email);
        user.setHoTen("Nguyễn Văn A");
        user.setTrangThai("HOAT_DONG");

        when(nguoiDungDAO.timTheoEmail(email)).thenReturn(user);
        when(cauHinhHeThongDAO.layGiaTriInt(eq("PHUT_HET_HAN_RESET_MAT_KHAU"), eq(30))).thenReturn(30);
        when(tokenDatLaiMatKhauDAO.taoToken(eq(10L), anyString(), eq(30), anyString())).thenReturn(true);

        DatLaiMatKhauResult result = authService.yeuCauDatLaiMatKhau(email, "127.0.0.1", "http://localhost:8080/crm");

        assertTrue(result.isThanhCong());
        assertEquals(AuthService.THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG, result.getThongBao());

        // Kiểm tra token được tạo với thời hạn đúng 30 phút
        verify(tokenDatLaiMatKhauDAO).taoToken(eq(10L), anyString(), eq(30), eq("127.0.0.1"));

        // Kiểm tra email được gửi đi chứa link reset
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).guiEmailDatLaiMatKhau(eq(email), eq("Nguyễn Văn A"), linkCaptor.capture(), eq(30));
        assertTrue(linkCaptor.getValue().startsWith("http://localhost:8080/crm/reset-password?token="));
    }

    @Test
    @DisplayName("S1-03-AC1: Token quá hạn 30 phút bị từ chối khi kiểm tra")
    void testAC1_TokenQuaHan30Phut_BiTuChoi() {
        String rawToken = "raw-expired-token-123";
        String tokenHash = PasswordUtil.sha256Hex(rawToken);

        TokenDatLaiMatKhau token = new TokenDatLaiMatKhau();
        token.setId(1L);
        token.setNguoiDungId(10L);
        token.setTokenHash(tokenHash);
        token.setUserTrangThai("HOAT_DONG");
        // Hết hạn cách đây 5 phút
        token.setHetHanLuc(new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000L));
        token.setDaSuDungLuc(null);

        when(tokenDatLaiMatKhauDAO.timToken(tokenHash)).thenReturn(token);
        when(tokenDatLaiMatKhauDAO.layThoiGianHienTaiDB()).thenReturn(new Timestamp(System.currentTimeMillis()));

        DatLaiMatKhauResult result = authService.kiemTraTokenDatLaiMatKhau(rawToken);

        assertFalse(result.isTokenHopLe());
        assertEquals(AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE, result.getThongBao());
    }

    // =========================================================================
    // S1-03-AC2: Liên kết chỉ dùng được một lần
    // =========================================================================

    @Test
    @DisplayName("S1-03-AC2: Đặt lại mật khẩu thành công thì đánh dấu đã sử dụng và cập nhật mật khẩu")
    void testAC2_DatLaiMatKhauThanhCong_DanhDauDaSuDung() {
        String rawToken = "valid-single-use-token";
        String tokenHash = PasswordUtil.sha256Hex(rawToken);

        TokenDatLaiMatKhau token = new TokenDatLaiMatKhau();
        token.setId(2L);
        token.setNguoiDungId(15L);
        token.setTokenHash(tokenHash);
        token.setUserTrangThai("HOAT_DONG");
        token.setHetHanLuc(new Timestamp(System.currentTimeMillis() + 25 * 60 * 1000L));
        token.setDaSuDungLuc(null); // Chưa dùng

        when(tokenDatLaiMatKhauDAO.timToken(tokenHash)).thenReturn(token);
        when(tokenDatLaiMatKhauDAO.layThoiGianHienTaiDB()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(tokenDatLaiMatKhauDAO.danhDauDaSuDung(tokenHash)).thenReturn(true);
        when(nguoiDungDAO.datLaiMatKhau(eq(15L), anyString())).thenReturn(true);

        DatLaiMatKhauResult result = authService.datLaiMatKhau(rawToken, "NewPass#2026", "NewPass#2026");

        assertTrue(result.isThanhCong());
        assertEquals(AuthService.THONG_BAO_DAT_LAI_THANH_CONG, result.getThongBao());

        // Đánh dấu token đã sử dụng trong DB
        verify(tokenDatLaiMatKhauDAO).danhDauDaSuDung(tokenHash);
        // Cập nhật mật khẩu băm mới
        verify(nguoiDungDAO).datLaiMatKhau(eq(15L), anyString());
    }

    @Test
    @DisplayName("S1-03-AC2: Token đã sử dụng (da_su_dung_luc != null) bị từ chối ngay lập tức")
    void testAC2_TokenDaSuDung_TuChoiNgayLapTuc() {
        String rawToken = "already-used-token";
        String tokenHash = PasswordUtil.sha256Hex(rawToken);

        TokenDatLaiMatKhau token = new TokenDatLaiMatKhau();
        token.setId(3L);
        token.setNguoiDungId(15L);
        token.setTokenHash(tokenHash);
        token.setUserTrangThai("HOAT_DONG");
        token.setHetHanLuc(new Timestamp(System.currentTimeMillis() + 20 * 60 * 1000L));
        token.setDaSuDungLuc(new Timestamp(System.currentTimeMillis() - 60 * 1000L)); // Đã dùng 1 phút trước

        when(tokenDatLaiMatKhauDAO.timToken(tokenHash)).thenReturn(token);

        DatLaiMatKhauResult checkResult = authService.kiemTraTokenDatLaiMatKhau(rawToken);
        assertFalse(checkResult.isTokenHopLe());
        assertEquals(AuthService.THONG_BAO_TOKEN_DA_SU_DUNG, checkResult.getThongBao());

        // Nếu cố tình gọi datLaiMatKhau với token này
        DatLaiMatKhauResult resetResult = authService.datLaiMatKhau(rawToken, "NewPass#2026", "NewPass#2026");
        assertFalse(resetResult.isThanhCong());
        assertEquals(AuthService.THONG_BAO_TOKEN_DA_SU_DUNG, resetResult.getThongBao());

        // Không bao giờ cập nhật mật khẩu cho token đã sử dụng
        verify(nguoiDungDAO, never()).datLaiMatKhau(anyLong(), anyString());
    }

    // =========================================================================
    // S1-03-AC3: Email không tồn tại vẫn hiển thị cùng một thông báo
    // =========================================================================

    @Test
    @DisplayName("S1-03-AC3: Email không tồn tại và email tồn tại trả về thông báo đồng nhất (chống enumeration)")
    void testAC3_EmailKhongTonTai_TraVeCungThongBaoDongNhat() {
        String emailKhongTonTai = "unknown.user@crm.vn";
        String emailTonTai = "real.user@crm.vn";

        NguoiDung realUser = new NguoiDung();
        realUser.setId(20L);
        realUser.setEmail(emailTonTai);
        realUser.setHoTen("Lê Văn Thực");
        realUser.setTrangThai("HOAT_DONG");

        when(nguoiDungDAO.timTheoEmail(emailKhongTonTai)).thenReturn(null);
        when(nguoiDungDAO.timTheoEmail(emailTonTai)).thenReturn(realUser);
        when(cauHinhHeThongDAO.layGiaTriInt(anyString(), anyInt())).thenReturn(30);
        when(tokenDatLaiMatKhauDAO.taoToken(anyLong(), anyString(), anyInt(), anyString())).thenReturn(true);

        // 1. Gọi với email không tồn tại
        DatLaiMatKhauResult resultKhongTonTai = authService.yeuCauDatLaiMatKhau(emailKhongTonTai, "127.0.0.1", "http://localhost:8080/crm");

        // 2. Gọi với email tồn tại
        DatLaiMatKhauResult resultTonTai = authService.yeuCauDatLaiMatKhau(emailTonTai, "127.0.0.1", "http://localhost:8080/crm");

        // Thông báo phải hoàn toàn giống hệt nhau
        assertTrue(resultKhongTonTai.isThanhCong());
        assertTrue(resultTonTai.isThanhCong());
        assertEquals(resultTonTai.getThongBao(), resultKhongTonTai.getThongBao(),
                "Thông báo trả về cho email tồn tại và không tồn tại phải hoàn toàn giống nhau");
        assertEquals(AuthService.THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG, resultKhongTonTai.getThongBao());

        // Email không tồn tại thì không được gửi email hoặc tạo token
        verify(emailService, never()).guiEmailDatLaiMatKhau(eq(emailKhongTonTai), any(), any(), anyInt());
        verify(tokenDatLaiMatKhauDAO, never()).taoToken(eq(0L), any(), anyInt(), any());
    }

    // =========================================================================
    // Validation tests: Mật khẩu mới không hợp lệ
    // =========================================================================

    @Test
    @DisplayName("Validation: Mật khẩu mới không khớp xác nhận mật khẩu -> Thất bại")
    void testValidation_MatKhauKhongKhop_ThatBai() {
        DatLaiMatKhauResult result = authService.datLaiMatKhau("token-xyz", "PassWord#123", "KhacPass#123");
        assertFalse(result.isThanhCong());
        assertTrue(result.getThongBao().contains("không khớp"));
    }

    @Test
    @DisplayName("Validation: Mật khẩu mới dưới 8 ký tự hoặc thiếu chữ/số -> Thất bại")
    void testValidation_MatKhauYeu_ThatBai() {
        // Dưới 8 ký tự
        DatLaiMatKhauResult res1 = authService.datLaiMatKhau("token-xyz", "Pass1", "Pass1");
        assertFalse(res1.isThanhCong());
        assertEquals(AuthService.THONG_BAO_MAT_KHAU_KHONG_HOP_LE, res1.getThongBao());

        // Toàn chữ không có số
        DatLaiMatKhauResult res2 = authService.datLaiMatKhau("token-xyz", "OnlyLettersPass", "OnlyLettersPass");
        assertFalse(res2.isThanhCong());
        assertEquals(AuthService.THONG_BAO_MAT_KHAU_KHONG_HOP_LE, res2.getThongBao());

        // Toàn số không có chữ
        DatLaiMatKhauResult res3 = authService.datLaiMatKhau("token-xyz", "1234567890", "1234567890");
        assertFalse(res3.isThanhCong());
        assertEquals(AuthService.THONG_BAO_MAT_KHAU_KHONG_HOP_LE, res3.getThongBao());
    }
}
