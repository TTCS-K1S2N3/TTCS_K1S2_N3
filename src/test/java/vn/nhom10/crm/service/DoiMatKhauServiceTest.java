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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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

        // Tạo schema bảng nguoi_dung và phien_dang_nhap trên H2 theo chuẩn integration
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS phien_dang_nhap");
            stmt.execute("DROP TABLE IF EXISTS nguoi_dung_vai_tro");
            stmt.execute("DROP TABLE IF EXISTS vai_tro");
            stmt.execute("DROP TABLE IF EXISTS nguoi_dung");

            stmt.execute("""
                CREATE TABLE vai_tro (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    ma_vai_tro VARCHAR(50) NOT NULL UNIQUE,
                    ten_vai_tro VARCHAR(100) NOT NULL,
                    mo_ta VARCHAR(255) NULL,
                    pham_vi_toi_da VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN'
                )
            """);

            stmt.execute("""
                CREATE TABLE nguoi_dung_vai_tro (
                    nguoi_dung_id BIGINT NOT NULL,
                    vai_tro_id INT NOT NULL,
                    PRIMARY KEY (nguoi_dung_id, vai_tro_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE nguoi_dung (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ho_ten VARCHAR(150) NOT NULL,
                    email VARCHAR(200) NOT NULL UNIQUE,
                    mat_khau VARCHAR(255) NOT NULL,
                    so_dien_thoai VARCHAR(20) NULL,
                    trang_thai VARCHAR(30) NOT NULL DEFAULT 'HOAT_DONG',
                    so_lan_sai INT NOT NULL DEFAULT 0,
                    thoi_gian_khoa TIMESTAMP NULL,
                    nhom_kinh_doanh_id INT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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

        // Tạo người dùng thử nghiệm trực tiếp trên H2
        String insertSql = "INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, trang_thai, so_lan_sai) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = h2Connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Khoàng Tuấn Hùng");
            ps.setString(2, "hung@crm.vn");
            ps.setString(3, PasswordUtil.hashPassword(currentRawPassword));
            ps.setString(4, "0912345678");
            ps.setString(5, "HOAT_DONG");
            ps.setInt(6, 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    testUserId = rs.getLong(1);
                }
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    private int demSoPhienHoatDong(long nguoiDungId) throws Exception {
        String sql = "SELECT COUNT(*) FROM phien_dang_nhap WHERE nguoi_dung_id = ? AND trang_thai = 'HOAT_DONG'";
        try (PreparedStatement ps = h2Connection.prepareStatement(sql)) {
            ps.setLong(1, nguoiDungId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Test
    @DisplayName("Thành công: Đổi mật khẩu hợp lệ và thu hồi các phiên đăng nhập khác (AC 1, AC 2, AC 3)")
    void testDoiMatKhau_ThanhCong_ThuHoiPhienKhac() throws Exception {
        String currentSessionId = "SESSION_DEVICE_1";
        String otherSessionId1 = "SESSION_DEVICE_2";
        String otherSessionId2 = "SESSION_DEVICE_3";

        // Tạo các phiên trong cơ sở dữ liệu qua API chuẩn luuPhien
        phienDangNhapDAO.luuPhien(new PhienDangNhap(testUserId, currentSessionId, "127.0.0.1", "Chrome"));
        phienDangNhapDAO.luuPhien(new PhienDangNhap(testUserId, otherSessionId1, "192.168.1.10", "Firefox"));
        phienDangNhapDAO.luuPhien(new PhienDangNhap(testUserId, otherSessionId2, "192.168.1.20", "Safari Mobile"));

        assertEquals(3, demSoPhienHoatDong(testUserId));

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

        // Kiểm tra mật khẩu trong DB đã cập nhật hash mới bằng API chuẩn timTheoId
        NguoiDung updatedUser = nguoiDungDAO.timTheoId(testUserId);
        assertNotNull(updatedUser);
        assertTrue(PasswordUtil.checkPassword(newPassword, updatedUser.getMatKhau()));
        assertFalse(PasswordUtil.checkPassword(currentRawPassword, updatedUser.getMatKhau()));

        // Kiểm tra chỉ còn 1 phiên hoạt động (phiên hiện tại), 2 phiên khác đã bị thu hồi
        assertEquals(1, demSoPhienHoatDong(testUserId));

        PhienDangNhap currentPhien = phienDangNhapDAO.timTheoMaPhien(currentSessionId);
        assertNotNull(currentPhien);
        assertTrue(currentPhien.isDangHoatDong());

        PhienDangNhap revokedSession = phienDangNhapDAO.timTheoMaPhien(otherSessionId1);
        assertNotNull(revokedSession);
        assertEquals(PhienDangNhap.TRANG_THAI_DA_DANG_XUAT, revokedSession.getTrangThai());
        assertFalse(revokedSession.isDangHoatDong());
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

        NguoiDung u = nguoiDungDAO.timTheoId(testUserId);
        assertEquals("KHOA", u.getTrangThai(), "Tài khoản KHOA tuyệt đối không được chuyển thành HOAT_DONG");
    }

    @Test
    @DisplayName("S1-08 Regression: Tài khoản CHO_KICH_HOAT đổi mật khẩu thành công chuyển thành HOAT_DONG")
    void testDoiMatKhau_ChoKichHoat_ThanhCong_ChuyenSangHoatDong() throws Exception {
        // Giả lập tài khoản vừa được Admin tạo ở Story S1-08 với trạng thái CHO_KICH_HOAT
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("UPDATE nguoi_dung SET trang_thai = 'CHO_KICH_HOAT' WHERE id = " + testUserId);
        }

        NguoiDung truocKhiDoi = nguoiDungDAO.timTheoId(testUserId);
        assertEquals(NguoiDung.TRANG_THAI_CHO_KICH_HOAT, truocKhiDoi.getTrangThai());

        String matKhauMoi = "KichHoatThanhCong@2026";
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                matKhauMoi,
                matKhauMoi,
                true,
                "SESSION_ACTIVATE"
        );

        assertTrue(ketQua.isThanhCong(), "Đổi mật khẩu lần đầu phải thành công");

        NguoiDung sauKhiDoi = nguoiDungDAO.timTheoId(testUserId);
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, sauKhiDoi.getTrangThai(),
                "Sau khi đổi mật khẩu lần đầu thành công, trạng thái phải tự động chuyển thành HOAT_DONG");
        assertTrue(PasswordUtil.checkPassword(matKhauMoi, sauKhiDoi.getMatKhau()),
                "Mật khẩu mới phải được băm và lưu chính xác trong database");
    }

    @Test
    @DisplayName("S1-08 Regression: Tài khoản HOAT_DONG đổi mật khẩu vẫn giữ nguyên HOAT_DONG")
    void testDoiMatKhau_HoatDong_GiuNguyenHoatDong() throws Exception {
        NguoiDung truocKhiDoi = nguoiDungDAO.timTheoId(testUserId);
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, truocKhiDoi.getTrangThai());

        String matKhauMoi = "VanHoatDongTot@2026";
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                currentRawPassword,
                matKhauMoi,
                matKhauMoi,
                false,
                "SESSION_CURRENT"
        );

        assertTrue(ketQua.isThanhCong());
        NguoiDung sauKhiDoi = nguoiDungDAO.timTheoId(testUserId);
        assertEquals(NguoiDung.TRANG_THAI_HOAT_DONG, sauKhiDoi.getTrangThai(),
                "Tài khoản đã HOAT_DONG thì sau khi đổi mật khẩu vẫn giữ nguyên HOAT_DONG");
    }

    @Test
    @DisplayName("S1-08 Regression: Đổi mật khẩu thất bại -> Trạng thái CHO_KICH_HOAT không thay đổi")
    void testDoiMatKhau_ChoKichHoat_ThatBai_TrangThaiKhongThayDoi() throws Exception {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("UPDATE nguoi_dung SET trang_thai = 'CHO_KICH_HOAT' WHERE id = " + testUserId);
        }

        // Nhập sai mật khẩu hiện tại
        KetQuaDoiMatKhauDTO ketQua = doiMatKhauService.doiMatKhau(
                testUserId,
                "SaiMatKhauHienTai@999",
                "MatKhauMoi@2026",
                "MatKhauMoi@2026",
                true,
                "SESSION_FAIL"
        );

        assertFalse(ketQua.isThanhCong());
        assertEquals("CURRENT_PASSWORD_INCORRECT", ketQua.getMaLoi());

        NguoiDung sauThatBai = nguoiDungDAO.timTheoId(testUserId);
        assertEquals(NguoiDung.TRANG_THAI_CHO_KICH_HOAT, sauThatBai.getTrangThai(),
                "Khi đổi mật khẩu thất bại, trạng thái CHO_KICH_HOAT tuyệt đối không được thay đổi");
    }
}
