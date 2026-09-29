package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dto.KetQuaDoiMatKhauDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhienDangNhap;
import vn.nhom10.crm.util.PasswordUtil;
import vn.nhom10.crm.util.SessionRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử nghiệp vụ DoiMatKhauService (AC 1, AC 2, AC 3)")
class DoiMatKhauServiceTest {

    private Connection h2Connection;
    private DoiMatKhauService doiMatKhauService;
    private NguoiDungDAO nguoiDungDAO;
    private PhienDangNhapDAO phienDangNhapDAO;
    private SessionRegistry sessionRegistry;

    private Long testUserId;
    private final String currentRawPassword = "CurrentPass@123";

    @BeforeEach
    void setUp() throws Exception {
        // Khởi tạo kết nối H2 in-memory cho kiểm thử tự động
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test;DB_CLOSE_DELAY=-1;MODE=MySQL");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test;DB_CLOSE_DELAY=-1;MODE=MySQL");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Tạo schema bảng nguoi_dung và phien_dang_nhap trên H2
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS phien_dang_nhap");
            stmt.execute("DROP TABLE IF EXISTS nguoi_dung");

            stmt.execute("""
                CREATE TABLE nguoi_dung (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ho_ten VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    mat_khau VARCHAR(255) NOT NULL,
                    so_dien_thoai VARCHAR(20) NULL,
                    trang_thai VARCHAR(50) NOT NULL DEFAULT 'HOAT_DONG',
                    so_lan_sai INT NOT NULL DEFAULT 0,
                    thoi_gian_khoa TIMESTAMP NULL,
                    ngay_doi_mat_khau TIMESTAMP NULL,
                    session_version INT NOT NULL DEFAULT 1,
                    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    ngay_cap_nhat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE phien_dang_nhap (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    nguoi_dung_id BIGINT NOT NULL,
                    ma_phien VARCHAR(255) NOT NULL UNIQUE,
                    dia_chi_ip VARCHAR(50) NULL,
                    thong_tin_thiet_bi VARCHAR(500) NULL,
                    trang_thai VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG',
                    thoi_gian_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    thoi_gian_hoat_dong_cuoi TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    thoi_gian_thu_hoi TIMESTAMP NULL
                )
            """);
        }

        nguoiDungDAO = new NguoiDungDAO();
        phienDangNhapDAO = new PhienDangNhapDAO();
        sessionRegistry = SessionRegistry.getInstance();
        sessionRegistry.clear();

        doiMatKhauService = new DoiMatKhauService(nguoiDungDAO, phienDangNhapDAO, sessionRegistry);

        // Tạo người dùng thử nghiệm
        NguoiDung user = new NguoiDung();
        user.setHoTen("Khoàng Tuấn Hùng");
        user.setEmail("hung@crm.vn");
        user.setMatKhau(PasswordUtil.hashPassword(currentRawPassword));
        user.setSoDienThoai("0912345678");
        user.setTrangThai("HOAT_DONG");
        user.setSoLanSai(0);
        testUserId = nguoiDungDAO.save(user);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @Test
    @DisplayName("Thành công: Đổi mật khẩu hợp lệ và thu hồi các phiên đăng nhập khác (AC 1, AC 2, AC 3)")
    void testDoiMatKhau_ThanhCong_ThuHoiPhienKhac() throws Exception {
        String currentSessionId = "SESSION_DEVICE_1";
        String otherSessionId1 = "SESSION_DEVICE_2";
        String otherSessionId2 = "SESSION_DEVICE_3";

        // Tạo các phiên trong cơ sở dữ liệu
        phienDangNhapDAO.save(new PhienDangNhap(testUserId, currentSessionId, "127.0.0.1", "Chrome"), null);
        phienDangNhapDAO.save(new PhienDangNhap(testUserId, otherSessionId1, "192.168.1.10", "Firefox"), null);
        phienDangNhapDAO.save(new PhienDangNhap(testUserId, otherSessionId2, "192.168.1.20", "Safari Mobile"), null);

        assertEquals(3, phienDangNhapDAO.findActiveByNguoiDungId(testUserId).size());

        String newPassword = "NewSecurePassword@2026";
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                newPassword,
                newPassword,
                true,
                currentSessionId
        );

        assertTrue(ketQua.isThanhCong());
        assertEquals(2, ketQua.getSoPhienDaThuHoi());
        assertTrue(ketQua.getThongBao().contains("thu hồi"));

        // Kiểm tra mật khẩu trong DB đã cập nhật hash mới
        NguoiDung updatedUser = nguoiDungDAO.findById(testUserId);
        assertTrue(PasswordUtil.checkPassword(newPassword, updatedUser.getMatKhau()));
        assertFalse(PasswordUtil.checkPassword(currentRawPassword, updatedUser.getMatKhau()));

        // Kiểm tra chỉ còn 1 phiên hoạt động (phiên hiện tại), 2 phiên khác đã bị thu hồi
        List<PhienDangNhap> activeSessions = phienDangNhapDAO.findActiveByNguoiDungId(testUserId);
        assertEquals(1, activeSessions.size());
        assertEquals(currentSessionId, activeSessions.get(0).getMaPhien());

        PhienDangNhap revokedSession = phienDangNhapDAO.findByMaPhien(otherSessionId1);
        assertTrue(revokedSession.isDaThuHoi());
        assertNotNull(revokedSession.getThoiGianThuHoi());
    }

    @Test
    @DisplayName("AC 1: Bắt buộc nhập mật khẩu hiện tại - Báo lỗi khi để trống")
    void testDoiMatKhau_AC1_MatKhauHienTaiTrong() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                "",
                "NewPass@123",
                "NewPass@123",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("CURRENT_PASSWORD_EMPTY", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("AC 1: Bắt buộc nhập mật khẩu hiện tại - Báo lỗi khi sai mật khẩu")
    void testDoiMatKhau_AC1_SaiMatKhauHienTai() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                "SaiMatKhau@999",
                "NewPass@123",
                "NewPass@123",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("CURRENT_PASSWORD_INCORRECT", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("AC 2: Mật khẩu mới tối thiểu 8 ký tự - Báo lỗi khi ngắn hơn 8 ký tự")
    void testDoiMatKhau_AC2_MatKhauMoiNgan() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                "Pass1",
                "Pass1",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("PASSWORD_WEAK", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("AC 2: Mật khẩu mới có cả chữ và số - Báo lỗi khi thiếu chữ số")
    void testDoiMatKhau_AC2_MatKhauMoiKhongCoSo() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                "PasswordOnlyNoDigits",
                "PasswordOnlyNoDigits",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("PASSWORD_WEAK", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("AC 2: Mật khẩu mới có cả chữ và số - Báo lỗi khi thiếu chữ cái")
    void testDoiMatKhau_AC2_MatKhauMoiKhongCoChu() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                "1234567890",
                "1234567890",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("PASSWORD_WEAK", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("Mật khẩu mới không được trùng với mật khẩu hiện tại")
    void testDoiMatKhau_AC2_MatKhauMoiTrungHienTai() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                currentRawPassword,
                currentRawPassword,
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("PASSWORD_SAME_AS_CURRENT", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("Xác nhận mật khẩu mới không trùng khớp")
    void testDoiMatKhau_XacNhanKhongKhop() {
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                "NewPass@123",
                "KhongTrungKhop@456",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("CONFIRM_PASSWORD_MISMATCH", ketQua.getMaLoi());
    }

    @Test
    @DisplayName("Tài khoản đang bị khóa không được phép đổi mật khẩu")
    void testDoiMatKhau_TaiKhoanBiKhoa() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("UPDATE nguoi_dung SET trang_thai = 'KHOA' WHERE id = " + testUserId);
        }

        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                "NewPass@123",
                "NewPass@123",
                true,
                "SESSION_1"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("ACCOUNT_INACTIVE", ketQua.getMaLoi());
    }
}
