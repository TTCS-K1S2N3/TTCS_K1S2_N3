package vn.nhom10.crm.service;

import org.junit.jupiter.api.*;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.CauHinhHeThongDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.dao.TokenDatLaiMatKhauDAO;
import vn.nhom10.crm.dto.DatLaiMatKhauResult;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TokenDatLaiMatKhau;
import vn.nhom10.crm.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử tích hợp đặt lại mật khẩu với MySQL thực tế (S1-03)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResetPasswordDatabaseIntegrationTest {

    private static final String TEST_EMAIL = "test.s103.integration@crm.vn";
    private static final String INITIAL_PASSWORD = "OldPassword#123";

    private static AuthService authService;
    private static NguoiDungDAO nguoiDungDAO;
    private static CauHinhHeThongDAO cauHinhHeThongDAO;
    private static PhienDangNhapDAO phienDangNhapDAO;
    private static TokenDatLaiMatKhauDAO tokenDAO;
    private static EmailService mockEmailService;

    private static Long createdUserId;
    private static boolean dbAvailable = false;
    private static String capturedRawToken;

    @BeforeAll
    static void initDatabaseAndSeedUser() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            dbAvailable = true;
            cauHinhHeThongDAO = new CauHinhHeThongDAO();
            nguoiDungDAO = new NguoiDungDAO();
            phienDangNhapDAO = new PhienDangNhapDAO();
            tokenDAO = new TokenDatLaiMatKhauDAO();

            // Mock email service để bắt link và raw token được gửi đi
            mockEmailService = new EmailService() {
                @Override
                public boolean guiEmailDatLaiMatKhau(String toEmail, String tenNguoiDung, String resetLink, int phutHetHan) {
                    // Trích xuất rawToken từ resetLink (...?token=xyz)
                    if (resetLink != null && resetLink.contains("token=")) {
                        capturedRawToken = resetLink.substring(resetLink.indexOf("token=") + 6);
                    }
                    return true;
                }
            };

            authService = new AuthService(nguoiDungDAO, cauHinhHeThongDAO, phienDangNhapDAO, tokenDAO, mockEmailService);

            // Dọn dẹp dữ liệu cũ nếu có
            xoaDuLieuKiemThu(conn, TEST_EMAIL);

            // Tạo người dùng kiểm thử
            NguoiDung nd = new NguoiDung();
            nd.setHoTen("Nguyễn Văn Kiểm Thử S103");
            nd.setEmail(TEST_EMAIL);
            nd.setMatKhauHash(PasswordUtil.hashPassword(INITIAL_PASSWORD));
            nd.setTrangThai("HOAT_DONG");
            nd.setSoLanDangNhapSai(0);
            nd.setBatBuocDoiMatKhau(false);
            nd.setSessionVersion(1);

            createdUserId = nguoiDungDAO.taoNguoiDung(nd);
            assertNotNull(createdUserId, "Phải tạo được người dùng kiểm thử trong MySQL");

        } catch (SQLException e) {
            System.err.println("Không thể kết nối MySQL thực tế cho S1-03: " + e.getMessage());
            dbAvailable = false;
        }
    }

    @AfterAll
    static void cleanUpDatabase() {
        if (!dbAvailable) return;
        try (Connection conn = DatabaseConfig.getConnection()) {
            xoaDuLieuKiemThu(conn, TEST_EMAIL);
        } catch (SQLException e) {
            System.err.println("Lỗi dọn dẹp DB sau kiểm thử S1-03: " + e.getMessage());
        }
    }

    private static void xoaDuLieuKiemThu(Connection conn, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM token_dat_lai_mat_khau WHERE nguoi_dung_id IN (SELECT id FROM nguoi_dung WHERE email = ?)")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM phien_dang_nhap WHERE nguoi_dung_id IN (SELECT id FROM nguoi_dung WHERE email = ?)")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nguoi_dung_vai_tro WHERE nguoi_dung_id IN (SELECT id FROM nguoi_dung WHERE email = ?)")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nguoi_dung WHERE email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("S1-03-AC1: Nhập email nhận liên kết đặt lại mật khẩu có hiệu lực 30 phút trong MySQL")
    void testAC1_TaoTokenHieuLuc30Phut() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "MySQL không khả dụng, bỏ qua test tích hợp");

        capturedRawToken = null;
        DatLaiMatKhauResult result = authService.yeuCauDatLaiMatKhau(TEST_EMAIL, "127.0.0.1", "http://localhost:8080/crm-ban-hang");

        assertTrue(result.isThanhCong(), "Yêu cầu đặt lại mật khẩu phải thành công");
        assertEquals(AuthService.THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG, result.getThongBao());
        assertNotNull(capturedRawToken, "Email service phải nhận được rawToken trong link");

        // Kiểm tra trong MySQL
        String tokenHash = PasswordUtil.sha256Hex(capturedRawToken);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nguoi_dung_id, tao_luc, het_han_luc, da_su_dung_luc, " +
                     "TIMESTAMPDIFF(MINUTE, tao_luc, het_han_luc) AS phut_hieu_luc " +
                     "FROM token_dat_lai_mat_khau WHERE token_hash = ?")) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Token hash phải tồn tại trong bảng token_dat_lai_mat_khau");
                assertEquals(createdUserId, rs.getLong("nguoi_dung_id"));
                assertNull(rs.getTimestamp("da_su_dung_luc"), "Token mới tạo chưa được sử dụng");
                int phutHieuLuc = rs.getInt("phut_hieu_luc");
                assertEquals(30, phutHieuLuc, "Thời hạn hiệu lực phải chính xác 30 phút theo S1-03-AC1 và cấu hình hệ thống");
            }
        }

        // Kiểm tra token hợp lệ khi gọi kiểm tra
        DatLaiMatKhauResult checkResult = authService.kiemTraTokenDatLaiMatKhau(capturedRawToken);
        assertTrue(checkResult.isTokenHopLe(), "Token mới tạo phải hợp lệ");
    }

    @Test
    @Order(2)
    @DisplayName("S1-03-AC1: Token hết hạn (> 30 phút) bị từ chối")
    void testAC1_TokenHetHanBiTuChoi() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "MySQL không khả dụng, bỏ qua test tích hợp");
        assertNotNull(capturedRawToken, "Phải có token từ test 1");

        String tokenHash = PasswordUtil.sha256Hex(capturedRawToken);
        // Cố tình chỉnh het_han_luc lùi về quá khứ trong MySQL
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE token_dat_lai_mat_khau SET het_han_luc = DATE_SUB(NOW(), INTERVAL 5 MINUTE) WHERE token_hash = ?")) {
            ps.setString(1, tokenHash);
            ps.executeUpdate();
        }

        // Thử kiểm tra token
        DatLaiMatKhauResult checkResult = authService.kiemTraTokenDatLaiMatKhau(capturedRawToken);
        assertFalse(checkResult.isTokenHopLe(), "Token hết hạn không được xem là hợp lệ");
        assertEquals(AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE, checkResult.getThongBao());

        // Thử đặt lại mật khẩu với token hết hạn
        DatLaiMatKhauResult resetResult = authService.datLaiMatKhau(capturedRawToken, "NewPass#2026", "NewPass#2026");
        assertFalse(resetResult.isThanhCong(), "Không thể đặt lại mật khẩu với token hết hạn");
        assertEquals(AuthService.THONG_BAO_TOKEN_KHONG_HOP_LE, resetResult.getThongBao());
    }

    @Test
    @Order(3)
    @DisplayName("S1-03-AC2: Liên kết chỉ dùng được một lần duy nhất (Single-Use Token)")
    void testAC2_TokenChiDungMotLan() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "MySQL không khả dụng, bỏ qua test tích hợp");

        // Tạo token mới tinh
        capturedRawToken = null;
        authService.yeuCauDatLaiMatKhau(TEST_EMAIL, "127.0.0.1", "http://localhost:8080/crm-ban-hang");
        assertNotNull(capturedRawToken, "Phải có token mới");

        String newPassword = "BrandNewSecretPassword#2026";

        // Lần sử dụng thứ nhất: thành công
        DatLaiMatKhauResult lan1 = authService.datLaiMatKhau(capturedRawToken, newPassword, newPassword);
        assertTrue(lan1.isThanhCong(), "Lần sử dụng đầu tiên phải thành công");

        // Kiểm tra trong MySQL: da_su_dung_luc phải NOT NULL
        String tokenHash = PasswordUtil.sha256Hex(capturedRawToken);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT da_su_dung_luc FROM token_dat_lai_mat_khau WHERE token_hash = ?")) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertNotNull(rs.getTimestamp("da_su_dung_luc"), "da_su_dung_luc phải được gán thời gian hiện tại");
            }
        }

        // Kiểm tra mật khẩu trong bảng nguoi_dung đã đổi
        NguoiDung nd = nguoiDungDAO.timTheoEmail(TEST_EMAIL);
        assertNotNull(nd);
        assertTrue(PasswordUtil.checkPassword(newPassword, nd.getMatKhauHash()), "Mật khẩu mới phải khớp");
        assertEquals(0, nd.getSoLanDangNhapSai(), "so_lan_dang_nhap_sai phải reset về 0");
        assertNull(nd.getKhoaDen(), "khoa_den phải được mở");
        assertEquals(2, nd.getSessionVersion(), "session_version phải được tăng lên 2 để thu hồi phiên cũ");

        // Lần sử dụng thứ hai với CÙNG TOKEN: phải THẤT BẠI (S1-03-AC2)
        DatLaiMatKhauResult lan2 = authService.datLaiMatKhau(capturedRawToken, "AnotherPassword#2026", "AnotherPassword#2026");
        assertFalse(lan2.isThanhCong(), "Lần sử dụng thứ hai của cùng token phải bị từ chối");
        assertEquals(AuthService.THONG_BAO_TOKEN_DA_SU_DUNG, lan2.getThongBao());

        // Kiểm tra kiểm tra token cũng trả về không hợp lệ
        DatLaiMatKhauResult check = authService.kiemTraTokenDatLaiMatKhau(capturedRawToken);
        assertFalse(check.isTokenHopLe(), "Token đã sử dụng không thể kiểm tra hợp lệ");
    }

    @Test
    @Order(4)
    @DisplayName("S1-03-AC3: Email không tồn tại vẫn hiển thị cùng một thông báo và không tạo token")
    void testAC3_EmailKhongTonTai_CungThongBao() throws SQLException {
        Assumptions.assumeTrue(dbAvailable, "MySQL không khả dụng, bỏ qua test tích hợp");

        String fakeEmail = "khong.ton.tai." + UUID.randomUUID() + "@crm.vn";
        capturedRawToken = null;

        DatLaiMatKhauResult result = authService.yeuCauDatLaiMatKhau(fakeEmail, "127.0.0.1", "http://localhost:8080/crm-ban-hang");

        assertTrue(result.isThanhCong(), "Phải trả về kết quả thành công giả định để tránh user enumeration");
        assertEquals(AuthService.THONG_BAO_DAT_LAI_MAT_KHAU_CHUNG, result.getThongBao(),
                "Thông báo phải giống hệt thông báo khi email tồn tại");
        assertNull(capturedRawToken, "Không được gửi email khi email không tồn tại");

        // Đảm bảo không có dòng nào được chèn vào token_dat_lai_mat_khau cho email này
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM token_dat_lai_mat_khau t JOIN nguoi_dung u ON t.nguoi_dung_id = u.id WHERE u.email = ?")) {
            ps.setString(1, fakeEmail);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Không được có token nào tạo ra cho email không tồn tại");
            }
        }
    }
}
